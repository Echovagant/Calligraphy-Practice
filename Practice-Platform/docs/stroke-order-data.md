# 笔顺数据说明

本文说明练习页「笔顺引导」用到的三份数据：它们从哪来、什么格式、可信到什么程度。

## 数据来源与许可

| 数据 | 来源 | 许可 |
| --- | --- | --- |
| 笔顺几何（笔顺顺序 / 落笔点 / 走向轨迹） | [Make Me a Hanzi](https://github.com/skishore/makemeahanzi)，经 `hanzi-writer-data@2.0.1` 取用。字形几何提取自 Arphic PL KaitiM GB 楷体，笔顺顺序按大陆规范 | Arphic Public License，许可证原文随数据保留在 `public/stroke-data/ARPHICPL.TXT` |
| 笔画写法（每一类笔画的起笔 / 行笔 / 收笔） | 本项目自制的视频字幕抽取产物，见 `src/data/strokeDictionary.js` | 项目自有 |

覆盖 9574 个简体与繁体汉字，共 112617 笔。繁体与部分异体字只有笔顺、没有用法教学，练习页只在有对应字条时展示提示。

## 三份数据

### 1. 索引 `src/data/strokeOrderIndex.js`

字 → `{ s: 分片号, n: 笔画数 }`。约 232 KB，随主包进入。

```js
import { strokeCount, strokeShard } from '@/data/strokeOrderIndex.js';
strokeCount('明');    // 8
strokeShard('明');   // 'stroke-data/s012.json'
```

### 2. 轨迹分片 `public/stroke-data/s*.json`

38 个分片，每片最多 256 字、单片最大 131 KB、总计 3.63 MB，**按需加载**（一次只会拉用户正在写的那一片）。

每字一条记录：

```json
{ "明": { "t": "<base64>", "c": [6, 8, 4, 3, 10, 11, 3, 4] } }
```

- `t`：所有笔画的轨迹点顺次拼接后的 base64。**每点 4 字节**（x uint16 大端、y uint16 大端）。
- `c`：每一笔各占多少个点，用来把 `t` 切回逐笔的轨迹。

解码：

```js
function decodeStrokes(t, counts) {
  const buf = Buffer.from(t, 'base64');
  const all = [];
  for (let i = 0; i + 3 < buf.length; i += 4) {
    all.push([buf.readUInt16BE(i), buf.readUInt16BE(i + 2)]);
  }
  const strokes = [];
  let k = 0;
  for (const c of counts) { strokes.push(all.slice(k, k + c)); k += c; }
  return strokes;
}
```

浏览器端没有 `Buffer`，用 `atob` + `DataView` 即可解开同样的字节。

### 坐标系统

**已经是屏幕坐标系**（y 向下，原点左上），1024×1024 画布，不需要再翻转。

原始数据用的是 y 轴向上为正的字体坐标（左上 `(0, 900)`、右下 `(1024, -124)`），入库时统一转成 `screenY = 900 - y`，范围落在 `[0, 1024]` 内，可直接用 uint16 存。

画布上的实际显示尺寸由前端按比例缩放，轨迹点本身是 1024 画布下的绝对坐标。

### 3. 笔画类型 `src/data/strokeTypes.js`

字 → 每一笔的类型名数组（顺序即笔顺），例如：

```js
strokeTypes('明');  // ['竖', '横折', '横', '横', '竖', '横折钩', '横', '横']
```

类型名与 `strokeDictionary.js` 的 `category` 通过 `src/data/strokeTypeMap.js` 对接：

| 类型 | 字典分类 |
| --- | --- |
| 横 / 竖 / 撇 / 捺 / 点 / 提 | 横画类 / 竖画类 / 撇画类 / 捺画类 / 点画类 / 提画类 |
| 横折 / 竖折 / 撇折 | 折笔类 / 折画类 |
| 横撇 / 竖撇 / 撇点 | 撇画类 |
| 横折钩 / 竖钩 / 竖弯钩 / 弯钩 / 卧钩 / 横斜钩 / 横折折钩 / 竖折折钩 / 横折弯钩 / 横钩 | 钩画类 |
| 竖提 / 横折提 | 提画类 |

**这份标注是人工逐字写出来的，并且每一字都经过笔数校验**：标注的笔画数必须与笔顺数据的笔画数完全一致，不一致的整字剔除（不进入本文件）。当前收录 172 字，全部通过校验。

几何自动分类做过评估：仅凭笔画中线反推笔画类型，在 66 个常用字的样本上逐笔命中仅约 43%，不足以驱动「这一笔该怎么起笔」的提示，因此没有采用。笔顺几何与笔画类型是两条独立的数据线，前者全量可信，后者只覆盖已人工校核的字。

## 前端怎么用

1. 练习页选定一个字 → `strokeCount(ch)` 拿到笔画数，`strokeShard(ch)` 拿到分片名 → `fetch` 那一片。
2. 解码得到逐笔轨迹。**第 i 笔的落笔点是 `strokes[i][0]`**，终点是末点。
3. 当前待写的笔画高亮：只画 `strokes[0..i]`，其余画成淡色底稿。
4. 类型提示（若该字已被收录）：`strokeTypes(ch)[i]` → `strokeTypeCategory(type)` → 从 `strokeDictionary.js` 里取出该分类下的笔画条目，显示其起笔 / 行笔 / 收笔说明。
5. 用户写完一笔后比对走向：落笔点距离、笔画方向、终点位置。

## 已知限制

- 笔画类型只覆盖 172 个已校核的高频字；其余字有笔顺与轨迹，但没有文字提示。
- 轨迹是**印刷体楷书**的字形中线，不是手写运笔轨迹；它表达「从哪起笔、往哪走」，不表达运笔速度与提按。
- 分片是静态文件，不随账号变化；首屏不加载，写字时才拉。
