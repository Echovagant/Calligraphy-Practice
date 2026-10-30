<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { summarizePressure } from '../utils/device/pressureStats.js'
import { measureStorageQuota } from '../utils/device/storageQuota.js'

/**
 * 设备诊断页：给 iPad 真机测试用，把主观体验变成可导出的客观数据。
 * 采集五类信息：压感分布、指针事件（pointercancel 等）、安全区与视口、
 * 长按 callout、localStorage 容量；全部只在本地采集，导出 JSON 由使用者自发回。
 */

const MAX_SAMPLES = 6000
const MAX_EVENT_LOG = 200
const MAX_VIEWPORT_LOG = 30
const EXPORT_SAMPLE_TAIL = 1500

const envInfo = ref(null)
const pressureSummary = ref(summarizePressure([]))
const eventLog = ref([])
const safeArea = ref(null)
const viewport = ref(null)
const viewportLog = ref([])
const calloutInfo = ref(null)
const calloutConfirm = ref({ textCalloutSeen: '未测', padCalloutSeen: '未测' })
const storageResult = ref(null)
const storageEstimate = ref(null)
const storageRunning = ref(false)
const reportJson = ref('')
const copyState = ref('')

const padRef = ref(null)
const probeTop = ref(null)
const probeRight = ref(null)
const probeBottom = ref(null)
const probeLeft = ref(null)
const dvhProbe = ref(null)
const vhProbe = ref(null)
const textSampleRef = ref(null)
const reportTa = ref(null)

/** 样本量大且每次事件都要追加，放非响应式数组，按节流刷新摘要 */
let samples = []
let moveCount = 0
let viewportDebounce = 0
let lastViewportKey = ''

function collectSample(e, phase) {
  const base = {
    t: Math.round(e.timeStamp),
    phase,
    type: e.pointerType || '',
    id: e.pointerId,
    x: Math.round(e.clientX),
    y: Math.round(e.clientY),
    pressure: e.pressure,
    tiltX: e.tiltX ?? null,
    tiltY: e.tiltY ?? null,
    twist: e.twist ?? null
  }
  const coalesced =
    typeof e.getCoalescedEvents === 'function' ? e.getCoalescedEvents() : null
  if (coalesced && coalesced.length > 0) {
    for (const ce of coalesced) {
      samples.push({
        ...base,
        t: Math.round(ce.timeStamp),
        x: Math.round(ce.clientX),
        y: Math.round(ce.clientY),
        pressure: ce.pressure,
        coalesced: true
      })
    }
  } else {
    samples.push(base)
  }
  if (samples.length > MAX_SAMPLES) {
    samples.splice(0, samples.length - MAX_SAMPLES)
  }
}

function pushEvent(e, kind) {
  eventLog.value.unshift({
    t: Math.round(e.timeStamp),
    kind,
    type: e.pointerType || '',
    id: e.pointerId,
    buttons: e.buttons,
    pressure: e.pressure
  })
  if (eventLog.value.length > MAX_EVENT_LOG) {
    eventLog.value.length = MAX_EVENT_LOG
  }
}

function refreshSummary() {
  pressureSummary.value = summarizePressure(samples)
}

function onPointerDown(e) {
  collectSample(e, 'down')
  pushEvent(e, 'pointerdown')
  refreshSummary()
}

function onPointerMove(e) {
  // 悬空移动（无按键）不算书写样本，只污染压感分布
  if (e.buttons === 0) return
  collectSample(e, 'move')
  moveCount += 1
  if (moveCount % 24 === 0) refreshSummary()
}

function onPointerUp(e) {
  collectSample(e, 'up')
  pushEvent(e, 'pointerup')
  refreshSummary()
}

function onPointerCancel(e) {
  collectSample(e, 'cancel')
  pushEvent(e, 'pointercancel')
  refreshSummary()
}

function onLostCapture(e) {
  pushEvent(e, 'lostpointercapture')
}

function clearRun() {
  samples = []
  moveCount = 0
  eventLog.value = []
  refreshSummary()
}

function readEnv() {
  return {
    capturedAt: new Date().toISOString(),
    userAgent: navigator.userAgent,
    platform: navigator.platform || null,
    maxTouchPoints: navigator.maxTouchPoints,
    devicePixelRatio: window.devicePixelRatio,
    screenW: screen.width,
    screenH: screen.height,
    innerW: window.innerWidth,
    innerH: window.innerHeight,
    protocol: location.protocol,
    host: location.host,
    standalone: matchMedia('(display-mode: standalone)').matches,
    pointerCoarse: matchMedia('(pointer: coarse)').matches,
    hoverHover: matchMedia('(hover: hover)').matches,
    hardwareConcurrency: navigator.hardwareConcurrency ?? null,
    language: navigator.language || null
  }
}

function pxValue(v) {
  return v.endsWith('px') ? `${Math.round(parseFloat(v))}px` : v || '(不支持)'
}

function readSafeArea() {
  const readSide = (el, side) =>
    el ? pxValue(getComputedStyle(el).getPropertyValue(`padding-${side}`)) : null
  safeArea.value = {
    top: readSide(probeTop.value, 'top'),
    right: readSide(probeRight.value, 'right'),
    bottom: readSide(probeBottom.value, 'bottom'),
    left: readSide(probeLeft.value, 'left')
  }
}

function readViewport() {
  const vv = window.visualViewport
  return {
    innerW: window.innerWidth,
    innerH: window.innerHeight,
    docClientH: document.documentElement.clientHeight,
    dvh: dvhProbe.value ? Math.round(dvhProbe.value.getBoundingClientRect().height) : null,
    vh: vhProbe.value ? Math.round(vhProbe.value.getBoundingClientRect().height) : null,
    visualH: vv ? Math.round(vv.height) : null,
    visualScale: vv ? Math.round(vv.scale * 100) / 100 : null,
    visualOffsetTop: vv ? Math.round(vv.offsetTop) : null,
    orientation:
      screen.orientation && screen.orientation.type
        ? screen.orientation.type
        : (window.orientation ?? null),
    angle:
      screen.orientation && typeof screen.orientation.angle === 'number'
        ? screen.orientation.angle
        : null
  }
}

function onViewportChange(cause) {
  clearTimeout(viewportDebounce)
  viewportDebounce = setTimeout(() => {
    const snap = readViewport()
    viewport.value = snap
    const key = JSON.stringify(snap)
    if (key !== lastViewportKey) {
      lastViewportKey = key
      viewportLog.value.unshift({ t: new Date().toISOString(), cause, ...snap })
      if (viewportLog.value.length > MAX_VIEWPORT_LOG) {
        viewportLog.value.length = MAX_VIEWPORT_LOG
      }
    }
  }, 250)
}

function readCallout() {
  const readEl = (el) =>
    el ? getComputedStyle(el).getPropertyValue('-webkit-touch-callout') || '(默认)' : null
  calloutInfo.value = {
    textSample: readEl(textSampleRef.value),
    pad: readEl(padRef.value)
  }
}

async function runStorageTest() {
  storageRunning.value = true
  try {
    storageResult.value = measureStorageQuota(window.localStorage)
    if (navigator.storage && typeof navigator.storage.estimate === 'function') {
      try {
        const est = await navigator.storage.estimate()
        storageEstimate.value = {
          usage: est.usage ?? null,
          quota: est.quota ?? null
        }
      } catch {
        storageEstimate.value = { unavailable: true }
      }
    } else {
      storageEstimate.value = { unavailable: true }
    }
  } finally {
    storageRunning.value = false
  }
}

function buildReport() {
  return {
    schema: 'calligraphy-device-lab/1',
    capturedAt: new Date().toISOString(),
    env: envInfo.value,
    pressure: {
      summary: pressureSummary.value,
      sampleCount: samples.length,
      samplesTail: samples.slice(-EXPORT_SAMPLE_TAIL)
    },
    pointerEvents: eventLog.value,
    safeArea: safeArea.value,
    viewport: { current: viewport.value, changes: viewportLog.value },
    longPress: {
      computedCallout: calloutInfo.value,
      userConfirmed: calloutConfirm.value
    },
    storage: { measured: storageResult.value, estimate: storageEstimate.value }
  }
}

function generateReport() {
  refreshSummary()
  readSafeArea()
  readCallout()
  viewport.value = readViewport()
  // 忘了先点「开始测量」也不让 storage 段是空的
  if (!storageResult.value) {
    storageResult.value = measureStorageQuota(window.localStorage)
  }
  reportJson.value = JSON.stringify(buildReport(), null, 2)
  copyState.value = ''
}

async function copyReport() {
  if (!reportJson.value) generateReport()
  try {
    await navigator.clipboard.writeText(reportJson.value)
    copyState.value = '已复制到剪贴板'
  } catch {
    // http 局域网下 clipboard API 可能被禁用 → 选中文本交给用户手动复制
    if (reportTa.value) {
      reportTa.value.focus()
      reportTa.value.select()
      const ok = document.execCommand('copy')
      copyState.value = ok ? '已复制' : '自动复制被拦，请长按文本框全选复制'
    }
  }
}

function downloadReport() {
  if (!reportJson.value) generateReport()
  const blob = new Blob([reportJson.value], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  const ts = new Date().toISOString().slice(0, 16).replace(/[:T]/g, '-')
  a.href = url
  a.download = `device-lab-${ts}.json`
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

function handleWindowResize() {
  onViewportChange('resize')
}

function handleOrientationChange() {
  onViewportChange('orientationchange')
}

function handleVvResize() {
  onViewportChange('visualViewport.resize')
}

function handleVvScroll() {
  onViewportChange('visualViewport.scroll')
}

onMounted(() => {
  envInfo.value = readEnv()
  readSafeArea()
  viewport.value = readViewport()
  lastViewportKey = JSON.stringify(viewport.value)
  readCallout()
  window.addEventListener('resize', handleWindowResize)
  window.addEventListener('orientationchange', handleOrientationChange)
  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', handleVvResize)
    window.visualViewport.addEventListener('scroll', handleVvScroll)
  }
})

onBeforeUnmount(() => {
  clearTimeout(viewportDebounce)
  window.removeEventListener('resize', handleWindowResize)
  window.removeEventListener('orientationchange', handleOrientationChange)
  if (window.visualViewport) {
    window.visualViewport.removeEventListener('resize', handleVvResize)
    window.visualViewport.removeEventListener('scroll', handleVvScroll)
  }
})

function fmtBytes(n) {
  if (typeof n !== 'number') return '—'
  if (n >= 1024 * 1024) return `${(n / (1024 * 1024)).toFixed(2)} MB`
  if (n >= 1024) return `${(n / 1024).toFixed(1)} KB`
  return `${n} B`
}

const pressureVerdict = () => {
  const s = pressureSummary.value
  if (s.realPressureLikely === null) return '尚无数据'
  return s.realPressureLikely ? '连续压感（真笔特征）' : '只有 0/0.5/1（模拟压感）'
}
</script>

<template>
  <div class="device-lab">
    <header class="lab-header">
      <h1>设备诊断</h1>
      <p>
        按下面 ① → ④ 的顺序操作，最后点「生成报告」导出 JSON 发回。
        所有数据只在本机采集，不会自动上传。
      </p>
    </header>

    <section class="lab-card">
      <h2>环境信息（自动采集）</h2>
      <dl class="kv" v-if="envInfo">
        <div><dt>UA</dt><dd>{{ envInfo.userAgent }}</dd></div>
        <div><dt>maxTouchPoints</dt><dd>{{ envInfo.maxTouchPoints }}</dd></div>
        <div><dt>devicePixelRatio</dt><dd>{{ envInfo.devicePixelRatio }}</dd></div>
        <div><dt>屏幕</dt><dd>{{ envInfo.screenW }} × {{ envInfo.screenH }}</dd></div>
        <div><dt>访问地址</dt><dd>{{ envInfo.protocol }}//{{ envInfo.host }}</dd></div>
        <div><dt>独立窗口(PWA)</dt><dd>{{ envInfo.standalone ? '是（从主屏图标打开）' : '否（浏览器标签）' }}</dd></div>
        <div><dt>粗指针(pointer:coarse)</dt><dd>{{ envInfo.pointerCoarse ? '是' : '否' }}</dd></div>
      </dl>
    </section>

    <section class="lab-card">
      <h2>① 压感与指针事件</h2>
      <p class="hint">
        用 Pencil 在下面区域写字：轻、中、重三种力度各写几个字，中间快速提笔几次。
        写完后再把手掌自然搭到屏幕上继续写——看日志里有没有红色的
        <code>pointercancel</code>，以及它出现前的最后几个事件是什么。
      </p>
      <div
        ref="padRef"
        class="lab-pad"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointercancel="onPointerCancel"
        @lostpointercapture="onLostCapture"
      >
        <span class="pad-tip">在此书写</span>
      </div>
      <div class="stats-grid">
        <div><span>样本数</span><b>{{ pressureSummary.count }}</b></div>
        <div><span>不同压感值</span><b>{{ pressureSummary.distinctCount }}</b></div>
        <div><span>最小</span><b>{{ pressureSummary.min ?? '—' }}</b></div>
        <div><span>最大</span><b>{{ pressureSummary.max ?? '—' }}</b></div>
        <div><span>均值</span><b>{{ pressureSummary.mean ?? '—' }}</b></div>
        <div><span>判定</span><b>{{ pressureVerdict() }}</b></div>
      </div>
      <p class="hint" v-if="pressureSummary.distinctCount">
        压感值：{{ pressureSummary.distinct.join('、') }}{{ pressureSummary.distinctCount >
        pressureSummary.distinct.length ? ' …' : '' }}
      </p>
      <h3>事件日志（新在上；只记 down / up / cancel / lostpointercapture）</h3>
      <ul class="event-log">
        <li v-for="(ev, i) in eventLog" :key="i" :class="{ cancel: ev.kind === 'pointercancel' }">
          {{ ev.t }}ms · {{ ev.kind }} · {{ ev.type }}#{{ ev.id }} · pressure={{ ev.pressure }}
        </li>
        <li v-if="eventLog.length === 0" class="empty">（还没有事件）</li>
      </ul>
      <div class="btn-row">
        <button class="lab-btn" @click="clearRun">清空本次数据</button>
      </div>
    </section>

    <section class="lab-card">
      <h2>② 安全区与视口</h2>
      <p class="hint">把 iPad 竖屏、横屏各停留两秒，日志会记下两方向的数值；再用两指缩放页面一次。</p>
      <dl class="kv" v-if="safeArea">
        <div><dt>安全区 上/下</dt><dd>{{ safeArea.top }} / {{ safeArea.bottom }}</dd></div>
        <div><dt>安全区 左/右</dt><dd>{{ safeArea.left }} / {{ safeArea.right }}</dd></div>
      </dl>
      <dl class="kv" v-if="viewport">
        <div><dt>window 内框</dt><dd>{{ viewport.innerW }} × {{ viewport.innerH }}</dd></div>
        <div><dt>100dvh / 100vh</dt><dd>{{ viewport.dvh }} / {{ viewport.vh }}</dd></div>
        <div><dt>visualViewport</dt><dd>{{ viewport.visualH }}（缩放 {{ viewport.visualScale }}，offsetTop {{ viewport.visualOffsetTop }}）</dd></div>
        <div><dt>方向</dt><dd>{{ viewport.orientation }}（{{ viewport.angle }}°）</dd></div>
      </dl>
      <h3>变更日志</h3>
      <ul class="event-log small">
        <li v-for="(v, i) in viewportLog" :key="i">
          {{ v.t }} · {{ v.cause }} · {{ v.innerW }}×{{ v.innerH }} · dvh={{ v.dvh }} · vv={{ v.visualH }}
        </li>
        <li v-if="viewportLog.length === 0" class="empty">（还没有变更）</li>
      </ul>
    </section>

    <section class="lab-card">
      <h2>③ 长按菜单</h2>
      <p class="hint">分别长按下面两块区域 2 秒：左边的普通文字、右边的书写区。看是否出现系统放大镜/「拷贝」菜单。</p>
      <div class="callout-row">
        <p ref="textSampleRef" class="text-sample">永和九年，岁在癸丑，暮春之初，会于会稽山阴之兰亭</p>
        <div class="lab-pad mini">书写区同款材质</div>
      </div>
      <dl class="kv" v-if="calloutInfo">
        <div><dt>-webkit-touch-callout（文字）</dt><dd>{{ calloutInfo.textSample }}</dd></div>
        <div><dt>-webkit-touch-callout（书写区）</dt><dd>{{ calloutInfo.pad }}</dd></div>
      </dl>
      <div class="btn-row">
        <label>长按文字出现菜单：
          <select v-model="calloutConfirm.textCalloutSeen">
            <option>未测</option><option>出现</option><option>未出现</option>
          </select>
        </label>
        <label>长按书写区出现菜单：
          <select v-model="calloutConfirm.padCalloutSeen">
            <option>未测</option><option>出现</option><option>未出现</option>
          </select>
        </label>
      </div>
    </section>

    <section class="lab-card">
      <h2>④ localStorage 容量</h2>
      <p class="hint">点按钮实测（写满即删，不动已存数据）。iPad 上 Safari 与桌面 Chrome 的配额可能不同。</p>
      <div class="btn-row">
        <button class="lab-btn" :disabled="storageRunning" @click="runStorageTest">
          {{ storageRunning ? '测量中…' : '开始测量' }}
        </button>
      </div>
      <dl class="kv" v-if="storageResult">
        <div><dt>实际可写入</dt><dd>{{ fmtBytes(storageResult.bytes) }}（{{ storageResult.chunks }} 块）</dd></div>
        <div><dt>写到探测上限仍未满</dt><dd>{{ storageResult.hitBudget ? '是（真实容量 ≥ 上述值）' : '否' }}</dd></div>
        <div v-if="storageResult.errorName"><dt>写入错误</dt><dd>{{ storageResult.errorName }}</dd></div>
        <div v-if="storageEstimate && !storageEstimate.unavailable">
          <dt>storage.estimate</dt>
          <dd>已用 {{ fmtBytes(storageEstimate.usage) }} / 配额 {{ fmtBytes(storageEstimate.quota) }}</dd>
        </div>
        <div v-else-if="storageEstimate"><dt>storage.estimate</dt><dd>此浏览器不支持</dd></div>
      </dl>
    </section>

    <section class="lab-card">
      <h2>⑤ 导出报告</h2>
      <p class="hint">生成后「下载」会存进「文件」App；也可以「复制」后粘贴发回。局域网 http 下自动复制可能被拦，那时请长按文本框手动全选复制。</p>
      <div class="btn-row">
        <button class="lab-btn" @click="generateReport">生成报告</button>
        <button class="lab-btn" :disabled="!reportJson" @click="copyReport">复制 JSON</button>
        <button class="lab-btn" :disabled="!reportJson" @click="downloadReport">下载 .json</button>
        <span v-if="copyState" class="copy-state">{{ copyState }}</span>
      </div>
      <textarea
        ref="reportTa"
        v-model="reportJson"
        class="report-ta"
        readonly
        placeholder="点「生成报告」后这里会出现 JSON"
      ></textarea>
    </section>

    <!-- 探针元素：量 env(safe-area-inset-*) 与 100dvh/100vh 的实际像素值 -->
    <div class="lab-probes" aria-hidden="true">
      <div ref="probeTop" style="padding-top: env(safe-area-inset-top)"></div>
      <div ref="probeRight" style="padding-right: env(safe-area-inset-right)"></div>
      <div ref="probeBottom" style="padding-bottom: env(safe-area-inset-bottom)"></div>
      <div ref="probeLeft" style="padding-left: env(safe-area-inset-left)"></div>
      <div ref="dvhProbe" style="height: 100dvh"></div>
      <div ref="vhProbe" style="height: 100vh"></div>
    </div>
  </div>
</template>

<style scoped>
.device-lab {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 16px 48px;
}

.lab-header h1 {
  font-size: 22px;
  margin: 8px 0;
}

.lab-header p {
  color: #6b6152;
  margin: 0 0 16px;
  line-height: 1.6;
}

.lab-card {
  background: #fffdf8;
  border: 1px solid #e4dcc8;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.lab-card h2 {
  font-size: 17px;
  margin: 0 0 10px;
}

.lab-card h3 {
  font-size: 14px;
  margin: 14px 0 6px;
  color: #6b6152;
}

.hint {
  font-size: 13px;
  color: #6b6152;
  line-height: 1.7;
  margin: 0 0 10px;
}

.kv {
  margin: 0;
}

.kv > div {
  display: flex;
  gap: 12px;
  padding: 4px 0;
  font-size: 13px;
}

.kv dt {
  flex: 0 0 auto;
  color: #6b6152;
  min-width: 9em;
}

.kv dd {
  margin: 0;
  word-break: break-all;
}

.lab-pad {
  position: relative;
  height: 240px;
  border: 1px dashed #b9ac8e;
  border-radius: 8px;
  background: #fdfaf2;
  touch-action: none;
  -webkit-user-select: none;
  user-select: none;
  -webkit-touch-callout: none;
}

.lab-pad.mini {
  flex: 1;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9a8f78;
  font-size: 13px;
}

.pad-tip {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: #c7bb9f;
  pointer-events: none;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 12px;
}

.stats-grid > div {
  background: #f6f0e2;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
}

.stats-grid span {
  display: block;
  color: #6b6152;
}

.stats-grid b {
  font-size: 14px;
}

.event-log {
  list-style: none;
  margin: 0;
  padding: 8px 10px;
  background: #2b2620;
  color: #d8cfb8;
  border-radius: 8px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
  max-height: 180px;
  overflow-y: auto;
}

.event-log.small {
  max-height: 140px;
}

.event-log li {
  padding: 2px 0;
}

.event-log li.cancel {
  color: #ff8f7a;
  font-weight: 700;
}

.event-log li.empty {
  color: #7d745f;
}

.btn-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  font-size: 13px;
}

.lab-btn {
  border: 1px solid #b9ac8e;
  background: #f4efe2;
  color: #2b2620;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  min-height: 44px;
}

.lab-btn:disabled {
  opacity: 0.5;
}

.copy-state {
  color: #6b6152;
}

.callout-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.text-sample {
  border: 1px solid #e4dcc8;
  border-radius: 8px;
  padding: 14px;
  margin: 0;
  line-height: 1.9;
  font-size: 15px;
}

.report-ta {
  width: 100%;
  box-sizing: border-box;
  height: 180px;
  margin-top: 12px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 11px;
  border: 1px solid #e4dcc8;
  border-radius: 8px;
  background: #fdfaf2;
  padding: 10px;
}

.lab-probes {
  position: fixed;
  left: -9999px;
  top: 0;
  pointer-events: none;
}

.lab-probes > div {
  width: 1px;
  height: 1px;
  overflow: hidden;
}
</style>
