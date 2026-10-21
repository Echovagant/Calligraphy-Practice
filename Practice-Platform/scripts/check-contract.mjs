/**
 * check-contract.mjs -- 契约一致性检查（前端 <-> contract.yaml <-> 后端 Controller）
 *
 * 权威文件是 openapi/contract.yaml。本脚本做三方比对：
 *
 *   1. 前端实际调用的端点   (src/utils/api.js, src/api/community.js)
 *   2. 契约里声明的端点     (openapi/contract.yaml)
 *   3. 后端已实现的端点     (SpringBoot-Platform/**\/*Controller.java)
 *
 * 两类结论必须分清：
 *
 *   - 违规 (violation)：契约与代码「对不上」且无人计划过。
 *     例如前端调了契约里没有的端点、路径参数名不一致、契约路径没带 /api 前缀。
 *     违规一律导致退出码 1 —— 这是漂移，必须立刻暴露。
 *
 *   - 缺口 (gap)：契约里声明了，但代码里还没有。
 *     例如后端还没写 Controller、前端还没接线。
 *     缺口就是「待实现清单」，本身不是错误；只有 --strict 才因此退出码 1。
 *
 * 退出码：
 *   0  无违规（--strict 时还要求无缺口）
 *   1  有违规，或 --strict 时有缺口
 *
 * 控制台只输出 ASCII（Windows 控制台是 GBK，中文会炸）。
 * 完整的中文报告写到 openapi/contract-diff.md（UTF-8）。
 *
 * 用法：
 *   node scripts/check-contract.mjs            # 报告差异，不因缺口失败
 *   node scripts/check-contract.mjs --strict   # 任何差异都失败（P6 用）
 *   node scripts/check-contract.mjs --json     # 额外输出机器可读摘要
 */

import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const FRONTEND_ROOT = path.resolve(HERE, '..')
const REPO_ROOT = path.resolve(FRONTEND_ROOT, '..')
const BACKEND_ROOT = path.join(REPO_ROOT, 'SpringBoot-Platform')
const CONTRACT_PATH = path.join(FRONTEND_ROOT, 'openapi', 'contract.yaml')
const REPORT_PATH = path.join(FRONTEND_ROOT, 'openapi', 'contract-diff.md')

const FRONTEND_SOURCES = [
  'src/utils/api.js',
  'src/api/community.js',
]

// 前端 api.js 导出的 HTTP 辅助函数名
const FRONTEND_HELPERS = ['get', 'post', 'put', 'del', 'upload']
// HTTP 方法名（契约与后端 Controller 用这一套）
const HTTP_METHODS = ['get', 'post', 'put', 'delete', 'patch', 'head', 'options']


// ---------------------------------------------------------------- YAML 子集解析

/**
 * 极简 YAML 解析器，只支持本项目契约文件实际用到的子集：
 * 缩进映射、标量、标量序列、映射序列。不支持锚点、流式风格、多行标量。
 * 遇到不支持的写法直接抛错 —— 宁可报错，也不要静默解析错。
 */
function parseYaml(text, where) {
  const rawLines = text.split(/\r?\n/)
  const lines = []
  for (let i = 0; i < rawLines.length; i++) {
    const raw = rawLines[i]
    if (/\t/.test(raw.match(/^\s*/)[0])) {
      throw new Error(`${where}:${i + 1} YAML must not use TAB indentation`)
    }
    const content = raw.trim()
    lines.push({
      indent: raw.length - raw.replace(/^\s*/, '').length,
      content,
      raw,
      lineNo: i + 1,
      blank: content === '',
      comment: content.startsWith('#'),
    })
  }

  const meaningful = (pos) => {
    while (pos < lines.length && (lines[pos].blank || lines[pos].comment)) pos++
    return pos
  }

  function parseScalar(raw) {
    let s = raw.trim()
    if (s === '' ) return null
    if (s === 'null' || s === '~') return null
    if (s === 'true') return true
    if (s === 'false') return false
    if (s === '{}') return {}
    if (s === '[]') return []
    if (/^-?\d+$/.test(s)) return Number(s)
    if (/^-?\d+\.\d+$/.test(s)) return Number(s)
    if (s.startsWith('"')) {
      if (!s.endsWith('"') || s.length < 2) throw new Error(`${where}: unterminated double quote: ${raw}`)
      return s.slice(1, -1).replace(/\\n/g, '\n').replace(/\\"/g, '"').replace(/\\\\/g, '\\')
    }
    if (s.startsWith("'")) {
      if (!s.endsWith("'") || s.length < 2) throw new Error(`${where}: unterminated single quote: ${raw}`)
      return s.slice(1, -1).replace(/''/g, "'")
    }
    if (s.startsWith('[') || s.startsWith('{')) {
      throw new Error(`${where}: flow style is not supported, use block style: ${raw}`)
    }
    // 未加引号的标量：去掉行尾注释
    const hash = s.indexOf(' #')
    if (hash >= 0) s = s.slice(0, hash).trim()
    return s
  }

  /**
   * 字面量块标量：`|` 保留换行，`>` 折叠成一行。
   * 块内容按原样取（含空行与 # 开头的行），这是 YAML 的语义。
   */
  function parseBlockScalar(pos, keyIndent, style) {
    const collected = []
    let blockIndent = null
    let i = pos
    while (i < lines.length) {
      const l = lines[i]
      if (l.blank) { collected.push(''); i++; continue }
      if (l.indent <= keyIndent) break
      if (blockIndent === null) blockIndent = l.indent
      collected.push(l.raw.slice(blockIndent))
      i++
    }
    while (collected.length && collected[collected.length - 1] === '') collected.pop()
    const text = style === '>'
      ? collected.join(' ').replace(/\n+/g, ' ')
      : collected.join('\n')
    return [text, i]
  }

  function parseMapping(pos, indent) {
    const obj = {}
    for (;;) {
      pos = meaningful(pos)
      if (pos >= lines.length) break
      if (lines[pos].indent !== indent) break
      const line = lines[pos]
      const m = line.content.match(/^([^:]+):\s*(.*)$/)
      if (!m) throw new Error(`${where}:${line.lineNo} unparsable mapping line: ${line.content}`)
      const key = m[1].trim().replace(/^["']|["']$/g, '')
      const rest = m[2].trim()
      pos++
      if (rest === '|' || rest === '>' || /^[|>][-+]?$/.test(rest)) {
        const style = rest.startsWith('>') ? '>' : '|'
        const [value, np] = parseBlockScalar(pos, indent, style)
        obj[key] = value
        pos = np
      } else if (rest === '') {
        const probe = meaningful(pos)
        if (probe < lines.length && lines[probe].indent > indent) {
          const [value, np] = parseBlock(probe, lines[probe].indent)
          obj[key] = value
          pos = np
        } else {
          obj[key] = null
        }
      } else {
        obj[key] = parseScalar(rest)
      }
    }
    return [obj, pos]
  }

  function parseSequence(pos, indent) {
    const arr = []
    for (;;) {
      pos = meaningful(pos)
      if (pos >= lines.length) break
      if (lines[pos].indent !== indent) break
      if (!lines[pos].content.startsWith('-')) break
      const line = lines[pos]
      const rest = line.content.slice(1).trim()
      if (rest === '') {
        pos++
        const probe = meaningful(pos)
        if (probe < lines.length && lines[probe].indent > indent) {
          const [value, np] = parseBlock(probe, lines[probe].indent)
          arr.push(value)
          pos = np
        } else {
          arr.push(null)
        }
      } else if (/^[^:"']+:\s*(.*)$/.test(rest)) {
        // 序列项本身是一个映射：把 "- " 去掉后当作缩进 +2 的映射首行
        const saved = lines[pos]
        lines[pos] = { indent: indent + 2, content: rest, raw: saved.raw, lineNo: saved.lineNo, blank: false, comment: false }
        const [value, np] = parseMapping(pos, indent + 2)
        arr.push(value)
        pos = np
      } else {
        arr.push(parseScalar(rest))
        pos++
      }
    }
    return [arr, pos]
  }

  function parseBlock(pos, indent) {
    if (pos >= lines.length) return [null, pos]
    if (lines[pos].content.startsWith('-')) return parseSequence(pos, indent)
    return parseMapping(pos, indent)
  }

  const start = meaningful(0)
  if (start >= lines.length) return {}
  const [value] = parseBlock(start, lines[start].indent)
  return value
}

// ------------------------------------------------------------ 前端调用点提取

function readIfExists(p) {
  try {
    return fs.readFileSync(p, 'utf8')
  } catch {
    return null
  }
}

/** 找到 `verb(` 的调用点，返回每个调用的第一个实参原文与行号。 */
function findCalls(text, verb) {
  const calls = []
  const re = new RegExp(`(?<![\\w$.])${verb}\\s*\\(`, 'g')
  let m
  while ((m = re.exec(text)) !== null) {
    const open = m.index + m[0].length - 1
    let depth = 0
    let i = open
    let inStr = null
    for (; i < text.length; i++) {
      const ch = text[i]
      if (inStr) {
        if (ch === '\\') { i++; continue }
        if (ch === inStr) inStr = null
        continue
      }
      if (ch === "'" || ch === '"' || ch === '`') { inStr = ch; continue }
      if (ch === '(') depth++
      else if (ch === ')') { depth--; if (depth === 0) break }
    }
    const args = text.slice(open + 1, i)
    calls.push({
      arg: args.split(',')[0].trim(),
      line: text.slice(0, m.index).split(/\r?\n/).length,
    })
  }
  return calls
}

/** 从实参原文里取出路径字面量；返回 null 表示不是字面量。 */
function literalOf(expr) {
  const s = expr.trim()
  if (s.startsWith("'") || s.startsWith('"')) {
    const quote = s[0]
    const end = s.indexOf(quote, 1)
    if (end < 0) return null
    return { kind: 'literal', value: s.slice(1, end) }
  }
  if (s.startsWith('`')) {
    const end = s.lastIndexOf('`')
    if (end <= 0) return null
    return { kind: 'template', value: s.slice(1, end) }
  }
  if (/^[A-Za-z_$][\w$]*$/.test(s)) {
    return { kind: 'identifier', value: s }
  }
  return null
}

/** 模板字面量 -> OpenAPI 路径：${x} / ${String(x)} -> {x}。 */
function normalizeTemplate(raw) {
  return raw.replace(/\$\{\s*(?:String\(\s*([A-Za-z_$][\w$]*)\s*\)|([A-Za-z_$][\w$]*))\s*\}/g,
    (_m, a, b) => `{${a || b}}`)
}

/**
 * 解析一个变量的赋值表达式，返回它可能代表的若干条路径。
 * 处理 `const endpoint = cond ? '/a' : '/b';` 这种三元。
 */
function resolveIdentifier(text, name, beforeLine) {
  const re = new RegExp(`(?:const|let|var)\\s+${name}\\s*=\\s*([^;\\n]+)`, 'g')
  let best = null
  let m
  while ((m = re.exec(text)) !== null) {
    const line = text.slice(0, m.index).split(/\r?\n/).length
    if (line >= beforeLine) break // 只找调用点之前的赋值
    if (best === null || line > best.line) best = { line, expr: m[1].trim() }
  }
  if (!best) return []
  const expr = best.expr
  // 三元：cond ? 'a' : 'b'
  const ternary = expr.match(/\?([^:]+):(.+)$/)
  if (ternary) {
    const out = []
    for (const branch of [ternary[1], ternary[2]]) {
      const lit = literalOf(branch.trim())
      if (lit && lit.kind === 'literal') out.push(lit.value)
    }
    if (out.length) return out
  }
  const lit = literalOf(expr)
  if (!lit) return []
  return [lit.kind === 'template' ? normalizeTemplate(lit.value) : lit.value]
}

function normalizePath(raw) {
  let p = raw.trim()
  if (!p.startsWith('/')) return null
  p = p.replace(/\/+$/, '') || '/'
  if (!p.startsWith('/api/') && p !== '/api') p = '/api' + p
  return p
}

/** 抽取一个前端文件里所有 API 调用。 */
function extractFromFile(relPath) {
  const abs = path.join(FRONTEND_ROOT, relPath)
  const text = readIfExists(abs)
  if (text === null) return { file: relPath, missing: true, calls: [] }
  const calls = []
  for (const verb of FRONTEND_HELPERS) {
    for (const call of findCalls(text, verb)) {
      const lit = literalOf(call.arg)
      let rawPaths = []
      if (lit && lit.kind === 'literal') rawPaths = [lit.value]
      else if (lit && lit.kind === 'template') rawPaths = [normalizeTemplate(lit.value)]
      else if (lit && lit.kind === 'identifier') rawPaths = resolveIdentifier(text, lit.value, call.line)
      for (const raw of rawPaths) {
        const p = normalizePath(raw)
        if (!p) continue
        calls.push({
          file: relPath,
          line: call.line,
          verb: verb === 'del' ? 'delete' : verb === 'upload' ? 'post' : verb,
          rawVerb: verb,
          path: p,
          multipart: verb === 'upload',
        })
      }
    }
  }
  return { file: relPath, missing: false, calls }
}

function extractFrontend() {
  const out = []
  for (const f of FRONTEND_SOURCES) out.push(extractFromFile(f))
  return out
}

// -------------------------------------------------- 裸 fetch（绕过统一客户端）

/**
 * 找出绕过统一客户端、直接打 API_BASE_URL 的裸 fetch。
 * 这类调用不在 get/post/put/del/upload 体系里，很容易从契约比对中漏掉。
 */
function extractRawFetches() {
  const found = []
  for (const rel of FRONTEND_SOURCES) {
    const text = readIfExists(path.join(FRONTEND_ROOT, rel))
    if (text === null) continue
    const re = /fetch\s*\(\s*`\$\{API_BASE_URL\}([^`]*)`/g
    let m
    while ((m = re.exec(text)) !== null) {
      // 路径是动态表达式（如 ${url}）的，是统一客户端 request() 内部的实现，
      // 不是绕过；只有写死路径的裸 fetch 才是问题。
      if (m[1].includes('${')) continue
      const line = text.slice(0, m.index).split(/\r?\n/).length
      const tail = text.slice(m.index, m.index + 400)
      const methodMatch = tail.match(/method\s*:\s*'(\w+)'/)
      found.push({
        file: rel,
        line,
        verb: (methodMatch ? methodMatch[1] : 'GET').toUpperCase(),
        path: normalizePath(m[1]) || m[1],
      })
    }
  }
  return found
}

// ------------------------------------------------------ 幽灵方法（调用了不存在的 API）

/** 解析 api.js 里 `export const <name> = { ... }` 的对象，列出它们的方法名。 */
function extractApiObjects() {
  const text = readIfExists(path.join(FRONTEND_ROOT, 'src', 'utils', 'api.js'))
  const objects = {}
  if (text === null) return objects
  const re = /export\s+const\s+(\w+)\s*=\s*\{/g
  let m
  while ((m = re.exec(text)) !== null) {
    const open = text.indexOf('{', m.index)
    let depth = 0
    let end = open
    for (let i = open; i < text.length; i++) {
      if (text[i] === '{') depth++
      else if (text[i] === '}') {
        depth--
        if (depth === 0) { end = i; break }
      }
    }
    const body = text.slice(open + 1, end)
    const methods = new Set()
    let d = 0
    for (const line of body.split(/\r?\n/)) {
      const before = d
      d += (line.match(/[{[(]/g) || []).length - (line.match(/[}\])]/g) || []).length
      if (before === 0) {
        const mm = line.match(/^\s*(\w+)\s*[:(]/)
        if (mm) methods.add(mm[1])
      }
    }
    objects[m[1]] = methods
  }
  return objects
}

function listFrontendSources() {
  const acc = []
  const walk = (dir) => {
    let entries = []
    try {
      entries = fs.readdirSync(dir, { withFileTypes: true })
    } catch {
      return
    }
    for (const e of entries) {
      const full = path.join(dir, e.name)
      if (e.isDirectory()) walk(full)
      else if (e.isFile() && (e.name.endsWith('.js') || e.name.endsWith('.vue'))) acc.push(full)
    }
  }
  walk(path.join(FRONTEND_ROOT, 'src'))
  return acc
}

/**
 * 找出 `copybookAPI.getPracticeHistory(...)` 这类调用 —— 被调用的方法在
 * api.js 导出的对象里根本不存在，运行时会直接 TypeError。
 */
function extractGhostMethods() {
  const objects = extractApiObjects()
  const names = Object.keys(objects)
  if (names.length === 0) return []
  const ghosts = []
  const re = new RegExp(`\\b(${names.join('|')})\\.(\\w+)\\s*\\(`, 'g')
  for (const file of listFrontendSources()) {
    const text = fs.readFileSync(file, 'utf8')
    let m
    re.lastIndex = 0
    while ((m = re.exec(text)) !== null) {
      const obj = m[1]
      const method = m[2]
      if (!objects[obj].has(method)) {
        ghosts.push({
          file: path.relative(REPO_ROOT, file).split(path.sep).join('/'),
          line: text.slice(0, m.index).split(/\r?\n/).length,
          symbol: `${obj}.${method}`,
        })
      }
    }
  }
  return ghosts
}

// ------------------------------------------------------------ 后端路由提取

function walkJava(dir, acc = []) {
  let entries = []
  try {
    entries = fs.readdirSync(dir, { withFileTypes: true })
  } catch {
    return acc
  }
  for (const e of entries) {
    const full = path.join(dir, e.name)
    if (e.isDirectory()) walkJava(full, acc)
    else if (e.isFile() && e.name.endsWith('.java')) acc.push(full)
  }
  return acc
}

function extractBackend() {
  const srcRoot = path.join(BACKEND_ROOT, 'src', 'main', 'java')
  const files = walkJava(srcRoot).filter((f) => path.basename(f).endsWith('Controller.java'))
  const routes = []
  for (const file of files) {
    const text = fs.readFileSync(file, 'utf8')
    const rel = path.relative(REPO_ROOT, file).split(path.sep).join('/')
    // 类级 @RequestMapping
    let prefix = ''
    const classIdx = text.search(/\bclass\s+\w+/)
    const head = classIdx >= 0 ? text.slice(0, classIdx) : text
    const classMap = head.match(/@RequestMapping\s*\(\s*(?:value\s*=\s*)?"([^"]*)"/)
    if (classMap) prefix = classMap[1]
    // 映射注解的路径参数是可选的：@GetMapping（不带路径，路径来自类级 @RequestMapping）
    // 也合法。路径组匹配不到时按空串处理，否则会漏统计这类端点。
    const methodRe = /@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\b(?:\s*\(\s*(?:value\s*=\s*)?"([^"]*)")?/g
    const reqMapRe = /@RequestMapping\s*\(\s*(?:value\s*=\s*)?"([^"]*)"\s*,\s*method\s*=\s*RequestMethod\.(\w+)/g
    let m
    while ((m = methodRe.exec(text)) !== null) {
      const verb = m[1].replace('Mapping', '').toUpperCase()
      routes.push({ file: rel, line: text.slice(0, m.index).split(/\r?\n/).length, verb, path: prefix + (m[2] || '') })
    }
    while ((m = reqMapRe.exec(text)) !== null) {
      const verb = m[2].toUpperCase()
      routes.push({ file: rel, line: text.slice(0, m.index).split(/\r?\n/).length, verb, path: prefix + m[1] })
    }
  }
  return routes
}

// ------------------------------------------------------------------ 契约读取

function loadContract() {
  const text = readIfExists(CONTRACT_PATH)
  if (text === null) {
    return { missing: true, ops: [] }
  }
  const doc = parseYaml(text, path.relative(REPO_ROOT, CONTRACT_PATH))
  const ops = []
  const paths = doc.paths || {}
  for (const [p, item] of Object.entries(paths)) {
    if (!item || typeof item !== 'object') continue
    for (const [verb, op] of Object.entries(item)) {
      if (!HTTP_METHODS.includes(verb.toLowerCase())) continue
      ops.push({
        path: p,
        verb: verb.toUpperCase(),
        operationId: op.operationId || null,
        summary: op.summary || '',
        tags: op.tags || [],
        frontendCall: op['x-frontend-call'] || null,
      })
    }
  }
  const schemas = Object.keys((doc.components && doc.components.schemas) || {})
  return { missing: false, ops, schemas, doc }
}

// ------------------------------------------------------------------ 比对

function key(verb, p) {
  return `${verb.toUpperCase()} ${p}`
}

/** 路径参数名必须一致，否则运行时 400。 */
function pathParams(p) {
  return (p.match(/\{[^}]+\}/g) || []).sort()
}

/**
 * 校验 x-frontend-call 指向的符号真实存在。
 * 形如 `copybookAPI.getCopybooks` 的，要检查 api.js 导出的那个对象里确实有这个方法；
 * 形如 `getDiscussions` 的独立函数，要在源码里能找到这个标识符。
 * 返回 null 表示没问题，否则返回一句说明。
 */
function checkFrontendSymbol(symbol) {
  const dot = symbol.indexOf('.')
  if (dot > 0) {
    const obj = symbol.slice(0, dot)
    const method = symbol.slice(dot + 1)
    const objects = extractApiObjects()
    if (objects[obj]) {
      if (!objects[obj].has(method)) {
        return `在 api.js 导出的 ${obj} 里不存在（该对象没有 ${method} 方法）`
      }
      return null
    }
  }
  const haystack = listFrontendSources()
    .map((f) => fs.readFileSync(f, 'utf8'))
    .join('\n')
  if (!haystack.includes(symbol)) {
    return '在前端源码里找不到'
  }
  return null
}

function compare(contract, frontend, backend, rawFetches, ghosts) {
  const violations = []
  const gaps = []
  const warnings = []

  const contractByKey = new Map()
  for (const op of contract.ops) {
    const k = key(op.verb, op.path)
    if (contractByKey.has(k)) {
      violations.push({ kind: 'duplicate-operation', key: k, detail: '契约里同一个 method+path 出现了两次' })
    }
    contractByKey.set(k, op)
    if (!op.operationId) {
      violations.push({ kind: 'missing-operation-id', key: k, detail: 'operation 缺少 operationId' })
    }
    if (!op.path.startsWith('/api/') && op.path !== '/api') {
      violations.push({ kind: 'bad-prefix', key: k, detail: '契约路径必须以 /api 开头（规则二 · 前缀归一）' })
    }
  }

  const seenIds = new Map()
  for (const op of contract.ops) {
    if (!op.operationId) continue
    if (seenIds.has(op.operationId)) {
      violations.push({
        kind: 'duplicate-operation-id',
        key: key(op.verb, op.path),
        detail: `operationId "${op.operationId}" 被 ${seenIds.get(op.operationId)} 和 ${key(op.verb, op.path)} 同时使用`,
      })
    } else {
      seenIds.set(op.operationId, key(op.verb, op.path))
    }
  }

  const frontendKeys = new Set()
  for (const f of frontend) {
    for (const call of f.calls) {
      const k = key(call.verb, call.path)
      frontendKeys.add(k)
      const op = contractByKey.get(k)
      if (!op) {
        violations.push({
          kind: 'frontend-not-in-contract',
          key: k,
          detail: `${call.file}:${call.line} 调用了契约里没有的端点`,
        })
        continue
      }
      // 路径参数名一致性：契约 /api/works/{workId} vs 前端 /api/works/{id}
      const contractParams = pathParams(op.path)
      const frontendParams = pathParams(call.path)
      if (contractParams.join(',') !== frontendParams.join(',')) {
        violations.push({
          kind: 'path-param-mismatch',
          key: k,
          detail: `路径参数名不一致：契约 [${contractParams}] vs 前端 [${frontendParams}]（${call.file}:${call.line}）`,
        })
      }
      if (op.frontendCall) {
        // 只有当「前端确实在调这个端点、但契约指向的符号却不存在」时才是漂移。
        // 端点还没接线时，x-frontend-call 是**待实现**的指向，按缺口处理。
        const bad = checkFrontendSymbol(op.frontendCall)
        if (bad) {
          violations.push({
            kind: 'stale-frontend-call',
            key: k,
            detail: `前端已经在调这个端点，但 x-frontend-call 指向的符号 "${op.frontendCall}" ${bad}`,
          })
        }
      }
    }
  }

  const backendKeys = new Set()
  for (const route of backend) {
    const k = key(route.verb, route.path)
    backendKeys.add(k)
    if (!contractByKey.has(k)) {
      violations.push({
        kind: 'backend-not-in-contract',
        key: k,
        detail: `${route.file}:${route.line} 实现了契约里没有的端点`,
      })
    }
  }

  for (const op of contract.ops) {
    const k = key(op.verb, op.path)
    if (!backendKeys.has(k)) {
      gaps.push({ kind: 'backend-missing', key: k, detail: '后端还没有实现这个端点' })
    }
    if (!frontendKeys.has(k)) {
      let detail = '前端还没有调用这个端点'
      if (op.frontendCall) {
        const bad = checkFrontendSymbol(op.frontendCall)
        if (bad) {
          detail += `；x-frontend-call 指向的 "${op.frontendCall}" 尚不存在（${bad}），P5 必须先建出来`
        }
      }
      gaps.push({ kind: 'frontend-missing', key: k, detail })
    }
  }

  // 裸 fetch：绕过统一客户端。这类调用既不在 get/post/put/del/upload 体系里，
  // 也不该作为独立端点存在（P5 接线时必须并入带用户类型的 refresh 端点）。
  for (const f of rawFetches) {
    const k = key(f.verb, f.path)
    warnings.push({
      kind: 'raw-fetch-bypass',
      key: k,
      detail: `${f.file}:${f.line} 绕过统一客户端直接 fetch；P5 接线时必须并入 api.js 的封装`,
    })
  }

  // 幽灵方法：调用了 api.js 里不存在的 API 方法，运行时直接 TypeError。
  for (const g of ghosts) {
    warnings.push({
      kind: 'ghost-api-method',
      key: g.symbol,
      detail: `${g.file}:${g.line} 调用了 api.js 里不存在的方法 ${g.symbol}`,
    })
  }

  return { violations, gaps, warnings }
}

// ------------------------------------------------------------------ 报告

function writeReport(contract, frontend, backend, result) {
  const lines = []
  lines.push('# 契约一致性报告')
  lines.push('')
  lines.push('> 由 `scripts/check-contract.mjs` 自动生成，请勿手改。')
  lines.push('> 权威文件是 `openapi/contract.yaml`；本文件只是它与当前代码的比对结果。')
  lines.push('')
  lines.push('## 统计')
  lines.push('')
  lines.push('| 项 | 数量 |')
  lines.push('|---|---|')
  lines.push(`| 契约声明的端点 | ${contract.ops.length} |`)
  lines.push(`| 前端实际调用的端点 | ${frontend.reduce((n, f) => n + f.calls.length, 0)} |`)
  lines.push(`| 后端已实现的端点 | ${backend.length} |`)
  lines.push(`| 违规 (violation) | ${result.violations.length} |`)
  lines.push(`| 缺口 (gap) | ${result.gaps.length} |`)
  lines.push(`| 警告 (warning) | ${result.warnings.length} |`)
  lines.push('')

  lines.push('## 违规 —— 契约与代码对不上，必须立刻处理')
  lines.push('')
  if (result.violations.length === 0) {
    lines.push('无。')
  } else {
    lines.push('| 类型 | 端点 | 说明 |')
    lines.push('|---|---|---|')
    for (const v of result.violations) {
      lines.push(`| ${v.kind} | \`${v.key}\` | ${v.detail} |`)
    }
  }
  lines.push('')

  lines.push('## 缺口 —— 待实现清单')
  lines.push('')
  if (result.gaps.length === 0) {
    lines.push('无。契约与代码已经完全对齐。')
  } else {
    lines.push('| 类型 | 端点 | 说明 |')
    lines.push('|---|---|---|')
    for (const g of result.gaps) {
      lines.push(`| ${g.kind} | \`${g.key}\` | ${g.detail} |`)
    }
  }
  lines.push('')

  lines.push('## 警告 —— 已知缺陷，需要排期修，但不是契约漂移')
  lines.push('')
  if (result.warnings.length === 0) {
    lines.push('无。')
  } else {
    lines.push('| 类型 | 位置 | 说明 |')
    lines.push('|---|---|---|')
    for (const w of result.warnings) {
      lines.push(`| ${w.kind} | \`${w.key}\` | ${w.detail} |`)
    }
  }
  lines.push('')

  lines.push('## 契约端点明细')
  lines.push('')
  lines.push('| Method | Path | operationId | 后端 | 前端 |')
  lines.push('|---|---|---|---|---|')
  const backendKeys = new Set(backend.map((r) => key(r.verb, r.path)))
  const frontendKeys = new Set()
  for (const f of frontend) for (const c of f.calls) frontendKeys.add(key(c.verb, c.path))
  for (const op of contract.ops) {
    const k = key(op.verb, op.path)
    lines.push(`| ${op.verb} | \`${op.path}\` | \`${op.operationId || ''}\` | ${backendKeys.has(k) ? 'Y' : '-'} | ${frontendKeys.has(k) ? 'Y' : '-'} |`)
  }
  lines.push('')

  fs.mkdirSync(path.dirname(REPORT_PATH), { recursive: true })
  fs.writeFileSync(REPORT_PATH, lines.join('\n'), 'utf8')
}

// ------------------------------------------------------------------ main

function main() {
  const strict = process.argv.includes('--strict')
  const wantJson = process.argv.includes('--json')

  if (!fs.existsSync(CONTRACT_PATH)) {
    console.error(`[contract-check] FATAL: contract file missing: ${CONTRACT_PATH}`)
    process.exit(2)
  }

  const contract = loadContract()
  const frontend = extractFrontend()
  const backend = extractBackend()
  const rawFetches = extractRawFetches()
  const ghosts = extractGhostMethods()
  const result = compare(contract, frontend, backend, rawFetches, ghosts)

  if (process.argv.includes('--dump')) {
    for (const op of contract.ops) {
      console.log('  ' + key(op.verb, op.path) + '   -> ' + (op.operationId || '(no operationId)'))
    }
  }

  writeReport(contract, frontend, backend, result)

  const nf = frontend.reduce((n, f) => n + f.calls.length, 0)
  const ng = result.gaps.length
  const nv = result.violations.length
  const nw = result.warnings.length

  console.log('[contract-check] contract: ' + contract.ops.length + ' operation(s)')
  console.log('[contract-check] frontend: ' + nf + ' call site(s)')
  console.log('[contract-check] backend : ' + backend.length + ' controller route(s)')
  console.log('[contract-check] violations: ' + nv + '   gaps: ' + ng + '   warnings: ' + nw)

  if (nv > 0) {
    console.error('[contract-check] FAILED: ' + nv + ' violation(s) -- contract and code disagree')
    for (const v of result.violations.slice(0, 20)) {
      console.error('  - ' + v.kind + ' | ' + v.key + ' | ' + v.detail)
    }
    console.error('[contract-check] see openapi/contract-diff.md for the full report')
    process.exit(1)
  }
  if (strict && ng > 0) {
    console.error('[contract-check] FAILED (--strict): ' + ng + ' gap(s) remain')
    console.error('[contract-check] see openapi/contract-diff.md for the full report')
    process.exit(1)
  }
  if (wantJson) {
    console.log(JSON.stringify({
      contract: contract.ops.length,
      frontend: nf,
      backend: backend.length,
      violations: result.violations,
      gaps: result.gaps,
    }, null, 2))
  }
  console.log('[contract-check] OK (report: openapi/contract-diff.md)')
  process.exit(0)
}

main()
