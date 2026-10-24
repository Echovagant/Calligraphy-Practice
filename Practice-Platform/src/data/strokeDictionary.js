// 本文件由字幕抽取产物聚合生成，请勿手改。
// 来源：软笔楷书全套教程（B站 BV1A9sbzTEUT）21 个分P 的字幕抽取，
// 每条都带出处（分P + 时间码），原话可在 .local/ink-knowledge/ 对应文件里回查。
// 生成脚本：.local/tools/aggregate-extraction.mjs

/**
 * 按笔画名查笔画条目。
 * 支持精确名、复合名的基名（「撇（斜撇、小短撇）」→ 撇）、以及包含匹配。
 * 包含匹配时取**最短**的名字：查「竖」应命中「竖」而不是「垂露竖」「竖钩」。
 *
 * 按书体过滤：不同书体的同名笔画不是同一条（硬笔没有锋与提按，隶书是蚕头雁尾）。
 * 默认查楷书 —— 本项目的毛笔练习用的是这一套。
 */
export function findStroke(name, font = '楷书') {
  if (!name) return null;
  const target = String(name).trim();
  const pool = STROKE_DICTIONARY.strokes.filter((s) => s.font === font);
  const exact = pool.find((s) => s.name === target);
  if (exact) return exact;
  const byBase = pool.find((s) => s.name.split('（')[0].trim() === target);
  if (byBase) return byBase;
  const partial = pool.filter((s) => s.name.includes(target));
  if (!partial.length) return null;
  return partial.reduce((best, s) => (s.name.length < best.name.length ? s : best));
}

/** 某个书体下有哪些笔画 */
export function strokesByFont(font) {
  return STROKE_DICTIONARY.strokes.filter((s) => s.font === font);
}

export const STROKE_DICTIONARY = {
  "version": "1.0",
  "fonts": [
    "楷书",
    "隶书",
    "硬笔"
  ],
  "source": "软笔楷书全套教程 BV1A9sbzTEUT",
  "parts": [
    1,
    1,
    1,
    1,
    1,
    2,
    2,
    2,
    2,
    2,
    3,
    3,
    3,
    3,
    3,
    4,
    4,
    4,
    4,
    4,
    5,
    5,
    5,
    5,
    5,
    6,
    6,
    6,
    6,
    6,
    7,
    7,
    7,
    7,
    7,
    8,
    8,
    8,
    8,
    8,
    9,
    9,
    9,
    9,
    9,
    10,
    10,
    10,
    10,
    10,
    11,
    11,
    11,
    11,
    11,
    12,
    12,
    12,
    12,
    12,
    13,
    13,
    13,
    13,
    14,
    14,
    14,
    14,
    15,
    15,
    15,
    15,
    16,
    16,
    16,
    16,
    17,
    17,
    17,
    18,
    18,
    18,
    19,
    19,
    19,
    20,
    20,
    20,
    21,
    21,
    22,
    23,
    24,
    25,
    26
  ],
  "strokes": [
    {
      "id": "stroke:垂露竖",
      "name": "垂露竖",
      "font": "楷书",
      "category": "竖画类",
      "tags": [
        "垂露竖",
        "颜勤礼碑",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 1,
        "title": "第1节 基础笔画横竖",
        "t": "01:10:59",
        "evidence_count": 9
      },
      "actions": [
        {
          "id": "stroke:垂露竖:起笔",
          "segment": "起笔",
          "action": "藏锋起笔，逆入比悬针竖更饱满，逆入动作可稍重一些；落下后稍聚笔锋再往下行笔",
          "measurable": {
            "direction": "左上方逆入→正下方",
            "entry": "藏锋逆入（比悬针竖稍重）",
            "exit": null,
            "pressure_profile": "饱满厚实",
            "speed_hint": null,
            "stop_points": [
              "落下聚锋处的顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "01:13:18",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:垂露竖:行笔",
          "segment": "行笔",
          "action": "聚锋后行中锋向下走，手部自然下行会带出一点弧度（外拓）；刻意写直也会带出自然的弯曲",
          "measurable": {
            "direction": "上→下",
            "entry": null,
            "exit": null,
            "pressure_profile": "中段略提",
            "speed_hint": "顺着手部自然下行",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "01:13:53",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:垂露竖:收笔",
          "segment": "收笔",
          "action": "笔尖先提到左下角，再往右下带、下顿（助笔：停住感受压力），然后在笔画之内往上回收锋",
          "measurable": {
            "direction": "提向左下→带向右下→笔内上收",
            "entry": null,
            "exit": "中锋回锋＋笔肚小压力，收拢成圆",
            "pressure_profile": "收笔处按压后回锋",
            "speed_hint": null,
            "stop_points": [
              "右下顿笔的驻留点（助笔处）",
              "笔内回锋的收束点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "01:14:32",
            "evidence_count": 13
          }
        }
      ]
    },
    {
      "id": "stroke:点",
      "name": "点",
      "font": "楷书",
      "category": "点画类",
      "tags": [
        "点",
        "颜勤礼碑",
        "基础笔画"
      ],
      "certainty": "inferred",
      "source": {
        "part": 1,
        "title": "第1节 基础笔画横竖",
        "t": "01:21:49",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:点:起笔",
          "segment": "起笔",
          "action": "点画藏锋也行、不藏锋也没毛病；直接落锋往下走更顺畅，但如果落笔后笔锋铺不开、写得太细，就必须先藏一下笔锋再顺势压",
          "measurable": {
            "direction": "落笔向左下—右下方取势",
            "entry": "逆锋轻入，随即立柱下压铺开笔锋",
            "exit": null,
            "pressure_profile": "轻入→压实铺厚",
            "speed_hint": "果断，忌犹犹豫豫",
            "stop_points": [
              "落笔处（点的起点，落笔即顿）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "01:21:49",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:点:行笔",
          "segment": "行笔",
          "action": "点画行笔往右下方走（竖点往正下方走），边走边把笔锋铺开，形成中锋线；中段最粗，压力顶住笔锋，不要在纸面上原地搓磨停顿。",
          "measurable": {
            "direction": "左上→右下（竖点：上→下）",
            "entry": null,
            "exit": null,
            "pressure_profile": "起笔压实→中段最粗",
            "speed_hint": "行笔不拖不停，边走边提",
            "stop_points": [
              "行笔中段（压力顶住处，最粗点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:09:56",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:点:收笔",
          "segment": "收笔",
          "action": "笔锋铺开后压住收锋，在笔画之内回锋收束",
          "measurable": {
            "direction": "右下→左下角收势",
            "entry": null,
            "exit": "底端顶住笔锋顺势提锋（空提），不回锋",
            "pressure_profile": "由粗到细",
            "speed_hint": "收笔顺势带出尖，不甩",
            "stop_points": [
              "左下角收笔处（顶住笔锋／顿住）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "01:21:52",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:点:整笔",
          "segment": "整笔",
          "action": "点画起笔即收笔，是最短促的笔画：落下即行，行到端点即收，整体呈长方形，中段最饱满，末端收出自然的尖。",
          "measurable": {
            "direction": "点：直→扭→往右鼓；势态往左，随即向右回转",
            "entry": null,
            "exit": "不拖长、不牵丝",
            "pressure_profile": "轻→轻→轻（收笔提笔）",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:03:46",
            "evidence_count": 17
          }
        }
      ]
    },
    {
      "id": "stroke:点画",
      "name": "点画（点）",
      "font": "楷书",
      "category": "点画类",
      "tags": [
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 9,
        "title": "第9节 笔画总汇",
        "t": "00:12:42",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:点画:起笔",
          "segment": "起笔",
          "action": "左点直接露锋入，往右下走，提起再往下转的过程必须走中锋线，不能用笔面侧抹过去；笔尖压住后往回弹，从中间出钩。右点直接逆入进去压下来，铺开厚度，再往下去带",
          "measurable": {
            "entry": "逆锋进入、笔尖落下即起",
            "direction": null,
            "exit": null,
            "pressure_profile": "落下压重→提起",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:13:15",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:点画:行笔",
          "segment": "行笔",
          "action": "落下后立即铺开笔锋再行笔；上半段方向向右下，下半段接近正下方；不要做成夸张的S形",
          "measurable": {
            "direction": "上半段向右下→下半段接近正下方",
            "entry": null,
            "exit": null,
            "pressure_profile": "落下压开→行笔渐收",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 13,
            "title": "第13节 结构规律覆盖",
            "t": "00:37:39",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:点画:收笔",
          "segment": "收笔",
          "action": "经顿笔压粗后提起，由粗到细往下收，收成一个瘦、略方的形态",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "提起收细，收成瘦瘦的略方形",
            "pressure_profile": "重→渐收细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 13,
            "title": "第13节 结构规律覆盖",
            "t": "00:22:14",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:点画（当小横写）",
      "name": "点画（当小横写）",
      "font": "楷书",
      "category": null,
      "tags": [],
      "certainty": "verbatim",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:点画（当小横写）:整笔",
          "segment": "整笔",
          "action": "小横基本都当点来写：往下一压，往左一提，稍微收着一点，别写太大",
          "measurable": {
            "direction": "右→左",
            "entry": null,
            "exit": "回圆",
            "pressure_profile": "压后提起",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第12节 结构规律相迭",
            "t": "01:09:47",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:短撇",
      "name": "短撇（小平撇）",
      "font": "楷书",
      "category": "撇画类",
      "tags": [
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 9,
        "title": "第9节 笔画总汇",
        "t": "00:05:00",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:短撇:整笔",
          "segment": "整笔",
          "action": "斜度比较平，没有夸张的弧度，尽量走直；起笔时把笔尖转一圈往右下顿、把它藏住，提起来后往左下方压住笔锋送出去",
          "measurable": {
            "direction": "右上→左下",
            "entry": "笔尖转一圈后往右下顿，藏锋"
          },
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:05:00",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:钩",
      "name": "钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "笔画名",
        "钩画类"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 6,
        "title": "第6节 基础笔画钩",
        "t": "01:32:06",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:钩:整笔",
          "segment": "整笔",
          "action": "钩画共用骨架（沿用前面几节立下的逆锋→落下→立笔调锋→铺锋）：逆锋上去→落下（带方向感、不是直来直去地落）→立起来顶住笔，把笔锋铺开走中锋→到底停住→转锋→压住弹性送出钩。竖钩与弯钩本质没有不同，区别只在弧度的大小。",
          "measurable": {
            "direction": "竖钩：上→下→向左上出钩；弯钩：上→下→左下转→向左平出钩",
            "entry": "逆锋上去→落下",
            "exit": "停住→转锋→压住弹性送出去",
            "pressure_profile": "落下压住→立笔铺开→走中锋→停住→转锋处压重",
            "speed_hint": "落下之后笔不能停，要带着它往下走",
            "stop_points": [
              "落下立笔处",
              "底端停住处",
              "转锋出钩处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "00:37:28",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:横",
      "name": "横",
      "font": "楷书",
      "category": "横画类",
      "tags": [
        "横",
        "藏锋",
        "颜勤礼碑",
        "B档",
        "横细竖粗",
        "残破"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 1,
        "title": "第1节 基础笔画横竖",
        "t": "00:25:34",
        "evidence_count": 14
      },
      "actions": [
        {
          "id": "stroke:横:起笔",
          "segment": "起笔",
          "action": "逆锋入笔：笔尖往左上方轻轻逆入，落下压住，把笔画宽度定准，再立笔（调锋）后向右铺开笔锋",
          "measurable": {
            "direction": "向右",
            "entry": "落下压住笔毫，稍停即起",
            "exit": null,
            "pressure_profile": "压住→稍停→饱满",
            "speed_hint": "方切需顶得快，这里要停一下",
            "stop_points": [
              "横画起笔的小三角"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:28:41",
            "evidence_count": 20
          }
        },
        {
          "id": "stroke:横:行笔",
          "segment": "行笔",
          "action": "行笔保持中锋线条，线条略带弧度、向上挺拔，不要像波浪线那样夸张",
          "measurable": {
            "direction": "左→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "起头圆→中间提→往后压",
            "speed_hint": "平走",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:31:03",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:横:收笔",
          "segment": "收笔",
          "action": "收笔不要着急，略微轻提笔尖后往下方转（带一点弧度），下顿后回锋",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "圆收（提一下再压）",
            "pressure_profile": "两头粗→中间细→收笔回圆",
            "speed_hint": null,
            "stop_points": [
              "收笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:31:51",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:横:转折",
          "segment": "转折",
          "action": "收笔时把笔尖提起来再往下方转、落回，用这个转折动作做出圆形的收笔弧度（写棱角时才需要把尖带出去）",
          "measurable": {
            "direction": "左→右→下",
            "entry": null,
            "exit": null,
            "pressure_profile": "转角处承接",
            "speed_hint": null,
            "stop_points": [
              "折角"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:32:17",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:横:整笔",
          "segment": "整笔",
          "action": "完整流程：逆锋入笔→笔尖落下→立住（调锋）→向右铺开笔锋行笔→收笔轻提→下顿→回锋",
          "measurable": {
            "direction": "左→右上斜",
            "entry": "逆锋轻入",
            "exit": "下顿回锋",
            "pressure_profile": "变化不大（轻→轻→轻）",
            "speed_hint": "起收动作快",
            "stop_points": [
              "起笔立住铺锋处",
              "收笔下顿回锋处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:33:44",
            "evidence_count": 10
          }
        }
      ]
    },
    {
      "id": "stroke:横（宽底横）",
      "name": "横（宽底横）",
      "font": "楷书",
      "category": null,
      "tags": [],
      "certainty": "verbatim",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:横（宽底横）:起笔",
          "segment": "起笔",
          "action": "逆入：落下之后往回一带，逆进去，再往下一转压住，往右走（隶书篆书常见的中锋起笔姿态）",
          "measurable": {
            "direction": "落下→往回一带→右",
            "entry": "逆锋（逆入）",
            "exit": null,
            "pressure_profile": "落下后一转压住",
            "speed_hint": null,
            "stop_points": [
              "起笔（逆入）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第12节 结构规律相迭",
            "t": "01:10:21",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:横钩",
      "name": "横钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "钩",
        "宝盖头",
        "复合笔画",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 7,
        "title": "第7节 基础笔画钩2",
        "t": "00:00:45",
        "evidence_count": 9
      },
      "actions": [
        {
          "id": "stroke:横钩:起笔",
          "segment": "起笔",
          "action": "横钩（宝盖头里的那一钩）起笔是圆笔，必须逆锋入笔：往左、往下带一点，把笔锋稍微藏一下，写出一个略带斜度的圆式起笔；手要沉住气，等毛笔锋翻转过来之后才让它走。",
          "measurable": {
            "direction": "起笔向左下带笔，随后向右行笔",
            "entry": "圆笔逆锋轻入，往左往下带一点藏锋，略带斜度的圆式",
            "exit": null,
            "pressure_profile": "轻入→藏锋后稍沉",
            "speed_hint": "起笔慢，等笔锋翻转过来再走",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:04:44",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横钩:行笔",
          "segment": "行笔",
          "action": "横钩的横段与横画写法相同：逆入后向右行，由粗到细、略带弧度，斜度尽量平直，不要往上斜得太过；行笔要中锋为主，不能拿毛笔偏着抹过去，否则下侧面虚空、线条不扎实。",
          "measurable": {
            "direction": "左→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "由粗到细",
            "speed_hint": "起笔沉住气，行笔平稳",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:06:35",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横钩:转折",
          "segment": "转折",
          "action": "转折处是本节的重点：一是往下一顿要「抢」住笔锋——扎住、顶住、把笔锋压住往左上去弹，这里要用比较快、比较准的力；二是调锋必须在笔画之内完成（提起来调锋、再压下去往左下走），调到笔画外面就与前一段完全脱开、没有关联性。",
          "measurable": {
            "direction": "右→下（顿）→提→左下（行笔）",
            "entry": null,
            "exit": null,
            "pressure_profile": "顿住压实→提细→再压",
            "speed_hint": "抢笔那一下要快而准，调锋动作可小",
            "stop_points": [
              "横画末端往下一顿的顿点（抢住笔锋处）",
              "顿后轻提、在笔画之内调锋的位置"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:06:47",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横钩:钩",
          "segment": "钩",
          "action": "出钩：笔尖一提，压住笔锋往上一提带出一个小尖，方向往左下方送出；因为这个尖又小又短，所以压得要狠、要重，提得要快，犹豫一下就会拖长、线条交代不清楚。",
          "measurable": {
            "direction": "向左下方送出小尖",
            "entry": null,
            "exit": "顿下→提起来调锋→自上侧面向下挑",
            "pressure_profile": "压得狠重→急速提起",
            "speed_hint": "提得要快，尖短促",
            "stop_points": [
              "出钩前压住笔锋的顿点",
              "小尖出锋处（左侧下方）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:03:43",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:横钩:整笔",
          "segment": "整笔",
          "action": "横钩是横折与钩连起来写的复合笔画：折的部分在上一节学过、钩的部分也学过，合起来只是把两个动作合二为一；不同位置的横钩动作相似，只按轻重和方向略有不同。",
          "measurable": {
            "direction": "左→右→下→左下（出钩）",
            "entry": "圆笔逆锋轻入",
            "exit": "沿上侧面往左下出小尖",
            "pressure_profile": "起笔藏锋→行笔由粗到细→顿→调锋→压→提→出尖",
            "speed_hint": "整体一气，幅度可小",
            "stop_points": [
              "横末转折的顿点",
              "出钩处的顿点与出锋点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:05:29",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:横撇",
      "name": "横撇",
      "font": "楷书",
      "category": "撇画类",
      "tags": [
        "横撇",
        "处字"
      ],
      "certainty": "inferred",
      "source": {
        "part": 19,
        "title": "第19节 结构规律回抱",
        "t": "01:30:52",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:横撇:转折",
          "segment": "转折",
          "action": "接笔要干脆：直接落下来、笔画很细、马上提起来，像拼两块积木；不要带出多余的方切形态",
          "measurable": {
            "direction": "横→撇",
            "entry": "直接落下",
            "exit": "立即提起接撇",
            "pressure_profile": "细→提",
            "speed_hint": "动作干脆不拖",
            "stop_points": [
              "横撇接笔处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 19,
            "title": "第19节 结构规律回抱",
            "t": "01:30:52",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:横折",
      "name": "横折",
      "font": "楷书",
      "category": "折笔类",
      "tags": [
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 9,
        "title": "第9节 笔画总汇",
        "t": "00:19:37",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:横折:起笔",
          "segment": "起笔",
          "action": "横折的起笔与横相同：先把笔锋藏住、逆着送进去，再落下来；横折就是先写这个小横，然后才转下去。",
          "measurable": {
            "direction": "左→右",
            "entry": "藏锋逆入后落下",
            "exit": null,
            "pressure_profile": "轻入，落下后铺锋",
            "speed_hint": "起笔慢",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:02:03",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折:行笔",
          "segment": "行笔",
          "action": "横画写得细挺即可，笔尖慢慢往上提，再往右下一顿；横与竖之间不宜有特别大的粗细变化，右边这个竖不要出现往回包的外拓，要保持直挺",
          "measurable": {
            "pressure_profile": "细挺，粗细变化不宜过大"
          },
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:19:53",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:横折:转折",
          "segment": "转折",
          "action": "小横过来之后笔尖慢慢往下去走，但一定要跟着它压下去再转下来；渐渐压住，略微带一点圆转，往下去铺开笔锋",
          "measurable": {
            "direction": "横→转向下",
            "stop_points": [
              "折角重压处"
            ],
            "entry": null,
            "exit": "出小钩",
            "pressure_profile": "转角加重→再变细",
            "speed_hint": "转角处加压"
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:20:50",
            "evidence_count": 14
          }
        }
      ]
    },
    {
      "id": "stroke:横折（庚字·折笔略方）",
      "name": "横折（庚字·折笔略方）",
      "font": "楷书",
      "category": "折画类",
      "tags": [
        "折笔",
        "横折"
      ],
      "certainty": "inferred",
      "source": {
        "part": 5,
        "title": "第5节 基础笔画折",
        "t": "00:56:36",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:横折（庚字·折笔略方）:起笔",
          "segment": "起笔",
          "action": "横画起笔用逆锋入纸，转过来时在起头位置转一个小圈，出来的是圆头；再由下往上略平直地过渡，然后往右下方走。",
          "measurable": {
            "direction": "向右",
            "entry": "逆锋入纸后回过来转一个小圈（圆头）",
            "exit": null,
            "pressure_profile": "轻入 → 转圈时最细 → 渐粗",
            "speed_hint": "起笔慢，转圈后提速",
            "stop_points": [
              "起笔点：横画左端转小圈处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:28:48",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折（庚字·折笔略方）:行笔",
          "segment": "行笔",
          "action": "横画圆过来后向上略平直地过渡一段，再往右下方走；这一段对整个横画来说是略平整的，最下方位置慢慢变粗并有一个顿笔。",
          "measurable": {
            "direction": "向右（略上平 → 右下）",
            "entry": null,
            "exit": null,
            "pressure_profile": "略平直 → 渐粗 → 顿笔",
            "speed_hint": "过渡段匀速",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:29:19",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折（庚字·折笔略方）:收笔",
          "segment": "收笔",
          "action": "折下来的竖由粗到细，最粗处之后向内收住；这个折角处是一个原地的提按动作，做完就立柱往内、由粗到细。",
          "measurable": {
            "direction": "向下 → 向内收",
            "entry": null,
            "exit": "向内收住，末端渐细",
            "pressure_profile": "由粗到细（转折后最粗处向内收）",
            "speed_hint": "渐收时提速",
            "stop_points": [
              "收笔点：竖画末端向内收住处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "01:01:57",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折（庚字·折笔略方）:转折",
          "segment": "转折",
          "action": "横画到折角处提起来、落下、立住，把笔的方向转过来（这一处折笔略方）；一定要往下去走，不能停在这里调锋，停住笔尖就会刺上去；调锋时要把方向转过来，笔锋保持中锋。",
          "measurable": {
            "direction": "向右 → 向下（正下方）",
            "entry": "提笔 → 落下 → 立住笔锋",
            "exit": "把方向转成正下方，边走边压",
            "pressure_profile": "折角处顿住最重，随后渐细",
            "speed_hint": "落下后立刻往下走，不停笔",
            "stop_points": [
              "折角顿点：横与竖相接的折角处，落下后下顿，不要顿大"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:56:40",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:横折（首字·暗转圆转）",
      "name": "横折（首字·暗转圆转）",
      "font": "楷书",
      "category": "折画类",
      "tags": [
        "折笔",
        "横折"
      ],
      "certainty": "inferred",
      "source": {
        "part": 5,
        "title": "第5节 基础笔画折",
        "t": "01:08:55",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:横折（首字·暗转圆转）:起笔",
          "segment": "起笔",
          "action": "横画直接落笔即可，起头自然成略圆的圆头；露锋走时要在前半截把圆肚子压下去。",
          "measurable": {
            "direction": "向右",
            "entry": "直接落笔（露锋），起头略圆",
            "exit": null,
            "pressure_profile": "轻入 → 前半截压下 → 渐粗",
            "speed_hint": "起笔可露锋直接落",
            "stop_points": [
              "起笔点：横画左端圆头处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "01:12:41",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折（首字·暗转圆转）:行笔",
          "segment": "行笔",
          "action": "横画整体一路提着写，往右拉；行笔时保持略平的态势，不要把肩（脚）抬得太高，也不要写出太硬的直线。",
          "measurable": {
            "direction": "向右（略带上斜）",
            "entry": null,
            "exit": null,
            "pressure_profile": "略平 → 渐粗",
            "speed_hint": "匀速，肩不要抬",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "01:12:41",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折（首字·暗转圆转）:收笔",
          "segment": "收笔",
          "action": "竖画下半段最粗之后向内收，慢慢聚起来；不要带出随意的外拓，也不要让最粗处与最底端连成一片，否则会显得肥而油腻。",
          "measurable": {
            "direction": "向下 → 向内收",
            "entry": null,
            "exit": "向内聚收",
            "pressure_profile": "渐粗至最粗 → 向内收聚",
            "speed_hint": "收聚时提速",
            "stop_points": [
              "收笔点：竖画最粗处之后向内收的收聚处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "01:16:21",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折（首字·暗转圆转）:转折",
          "segment": "转折",
          "action": "转折处先压一压，再换方向落下，落下时略微转一点点即可；内侧一定要先直下来，只求一个「暗转」，里外两侧都不能鼓得太厉害，否则字的框架会往外站得太开。",
          "measurable": {
            "direction": "向右 → 向下（正下方）",
            "entry": "压笔 → 换方向落下",
            "exit": "立柱后往正下方走，再往内收",
            "pressure_profile": "压后落下，落下处较厚，随后渐细",
            "speed_hint": "落下一小转后马上回来，不要提得过高",
            "stop_points": [
              "转折点：横与竖相接处，先压后落；内侧先直下来的转折起点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "01:15:10",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:横折钩",
      "name": "横折钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "钩",
        "横折钩",
        "有字"
      ],
      "certainty": "inferred",
      "source": {
        "part": 7,
        "title": "第7节 基础笔画钩2",
        "t": "00:54:38",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:横折钩:行笔",
          "segment": "行笔",
          "action": "横折后往下走：铺开这个弧度、铺开笔锋往下拉，整个过程保持中锋。粗细变化的正常规律是到下半段微微粗一点点；绝不能出现速写式的夸张变化。",
          "measurable": {
            "direction": "上→下（圆转包弧后下行）",
            "entry": null,
            "exit": null,
            "pressure_profile": "上半段细→下半段微微粗一点",
            "speed_hint": "匀速下行",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:55:21",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横折钩:转折",
          "segment": "转折",
          "action": "「有」字月部的横折处：起笔要压住、圆起一点（厚一点）再往右走；到折处轻提然后顿下，但不要把这个尖提出来、顶上去——要含住它、顿住再往下走，把弧度铺开、笔锋铺开往下拉。顿如果下大了，后面必须先去铺厚；而且调锋一定要在笔画之内完成，否则与前一段完全脱开、没有关联性。",
          "measurable": {
            "direction": "右→（轻提顿下）→下（圆转包弧）",
            "entry": "压住起笔，圆起一点，厚一点",
            "exit": null,
            "pressure_profile": "起笔压厚→轻提→顿住→铺开笔锋",
            "speed_hint": "折处动作要准，不要提过头",
            "stop_points": [
              "横折处轻提后往右下一顿的顿点（小肩膀）",
              "顿住后往右下的落笔点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:54:43",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横折钩:钩",
          "segment": "钩",
          "action": "月部走到左下角：慢慢提到左下角、顿住，然后往回收拢笔锋，往左边弹出这个尖，方向感要往内去侧（往内去测）；出来一定是方中带圆的感觉，不要出太长。",
          "measurable": {
            "direction": "走到左下角→回走→向左弹出尖（方向往内）",
            "entry": null,
            "exit": "回走收拢笔锋后往左边出尖",
            "pressure_profile": "顿住→收拢→弹出",
            "speed_hint": "不要拖长，动作很快地回身",
            "stop_points": [
              "左下角的顿点",
              "向左出尖的出锋点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:55:54",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:横折弯钩",
      "name": "横折弯钩",
      "font": "楷书",
      "category": "折笔类",
      "tags": [
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 9,
        "title": "第9节 笔画总汇",
        "t": "00:28:35",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:横折弯钩:整笔",
          "segment": "整笔",
          "action": "可以两笔写；断开另起一笔的时候一定要把笔尖藏住、把笔头藏圆。虚势是必须保证的——笔断意连的关系不能断得太厉害（与竖折、竖撇同理，可以断开写也可以连起来写）",
          "measurable": {},
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:28:56",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:横折斜钩",
      "name": "横折斜钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "横折斜钩",
        "钩画类",
        "别名"
      ],
      "certainty": "inferred",
      "source": {
        "part": 8,
        "title": "第8节 基础笔画钩3",
        "t": "01:07:41",
        "evidence_count": 8
      },
      "actions": [
        {
          "id": "stroke:横折斜钩:起笔",
          "segment": "起笔",
          "action": "横折斜钩的横：藏锋往右走，斜上去一段，然后突然平下来。它不是纯粹的斜直线——这一下方向上的变化是刻意留的，为的是不让里面的内白被挤压掉。",
          "measurable": {
            "direction": "起笔向右→斜上去一段→突然平下来",
            "entry": "藏锋起笔",
            "exit": null,
            "pressure_profile": "带一点粗细变化",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "01:09:01",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横折斜钩:行笔",
          "segment": "行笔",
          "action": "弧度分三段：上边这一段略直一点；跟着第一个落下的顿笔，先有一个往内收的方向；然后往下走直；最后再往外拉开，把中间的空撑开（外拓）。不要整个弧度全往内收——内收的体式做得太夸张，就违背了外拓的取向。",
          "measurable": {
            "direction": "上段略直→往内收→往下走直→再往外拉开",
            "entry": null,
            "exit": null,
            "pressure_profile": "整体以中正直挺为主，略带上扬",
            "speed_hint": "内收与外放都要有度",
            "stop_points": [
              "顿笔之后的内收起转点",
              "往外拉开的起转点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "01:15:13",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横折斜钩:收笔",
          "segment": "收笔",
          "action": "钩尖由粗到细；上侧面可以稍微胖一点点。出钩前那一处如果太尖，是提得太高了。",
          "measurable": {
            "direction": "向上收尖",
            "entry": null,
            "exit": "由粗到细的钩尖，上侧面可略胖",
            "pressure_profile": "由粗到细",
            "speed_hint": "提不要过高",
            "stop_points": [
              "钩尖末端"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "01:15:40",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折斜钩:转折",
          "segment": "转折",
          "action": "转折处：直接提过来，往上一藏、往下一顿；这个顿从落笔的一瞬间就开始往内收，所以外侧很干净、方向感纯粹往内。不能只靠随意往下一顿——那样骨头很大、往外跑、往外散；正确做法是紧紧地搂住、提起来收住，然后往内走，转折才做得既干脆又果断。",
          "measurable": {
            "direction": "提过来→往下一顿→顿住后立刻往内收→继续往下走",
            "entry": null,
            "exit": null,
            "pressure_profile": "提→圆顿（顿得很清楚、很方、很硬），不外散",
            "speed_hint": "顿与收要连贯，转折处干脆不迟疑",
            "stop_points": [
              "转折处的圆顿（往下一顿的顿点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "01:09:14",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横折斜钩:钩",
          "segment": "钩",
          "action": "收笔和斜钩一样：走到底部顿住，往回一带就出勾。钩同样从笔画内部出，不能从外边直接转上去——写成那样说明完全没有调锋、完全没有在笔画内部做提按动作。",
          "measurable": {
            "direction": "向上出勾；从笔画内部出",
            "entry": null,
            "exit": "顿住→往回一带→出小勾",
            "pressure_profile": "顿住→回带→由粗到细",
            "speed_hint": "带回去的动作要短促",
            "stop_points": [
              "底部顿住处（出钩前顿点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "01:15:35",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横折斜钩:整笔",
          "segment": "整笔",
          "action": "横折斜钩全链：横藏锋往右走（斜上去一段后平一下）→转折处提过来、往下一顿并立刻往内收→往下走直→再往外拉开→到底顿住→往回一带出小勾。这条笔画在帖里名字很多：横折斜钩、横折背钩、横折弯钩、飞钩。",
          "measurable": {
            "direction": "向右（斜后平）→下（内收后走直）→外拉→向上出勾",
            "entry": "藏锋起笔",
            "exit": "由粗到细出小勾（笔画内部）",
            "pressure_profile": "顿笔方硬、干脆；整体中正直挺",
            "speed_hint": "转折干脆，收笔一气",
            "stop_points": [
              "转折圆顿处",
              "底部顿住处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "01:07:59",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:捺",
      "name": "捺（斜捺）",
      "font": "楷书",
      "category": "捺画类",
      "tags": [
        "捺",
        "捺角"
      ],
      "certainty": "inferred",
      "source": {
        "part": 3,
        "title": "第3节 基础笔画撇捺",
        "t": "00:07:30",
        "evidence_count": 8
      },
      "actions": [
        {
          "id": "stroke:捺:起笔",
          "segment": "起笔",
          "action": "斜捺起笔：直接落笔（露锋入），落笔后往下去压；也可以逆锋藏锋入（视情况而定）。起笔之后要自然接成下行的斜捺，而不是像竖撇那样直直往下",
          "measurable": {
            "direction": "起笔后即向右下展开",
            "entry": "直接落笔（露锋入）或逆锋藏锋入",
            "exit": null,
            "pressure_profile": "轻落→压",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:07:20",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:捺:行笔",
          "segment": "行笔",
          "action": "行笔：往下压，慢慢把笔锋铺开，沿着斜线的角度走；到最重的捺角位置稍停一下再顺势聚拢笔锋",
          "measurable": {
            "direction": "先内收 → 下沉 → 加粗 → 向右平出",
            "entry": null,
            "exit": null,
            "pressure_profile": "细 → 下沉 → 加粗",
            "speed_hint": "到出捺角处提笔",
            "stop_points": [
              "捺角（笔画最重处），稍停"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:07:30",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:捺:收笔",
          "segment": "收笔",
          "action": "捺角处稍微一驻笔，扭一下压住，一定要压住（压不住笔画就会变细），然后往右一转，边转边提，由粗到细，用笔尖把尖送出去；收的时候方向要平着走，不要上翘",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "用笔尖一提一松出锋，带出自然的弧度",
            "pressure_profile": "收笔前加粗",
            "speed_hint": null,
            "stop_points": [
              "捺角驻笔处（笔画下侧）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:42:42",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:捺:整笔",
          "segment": "整笔",
          "action": "弧度要圆、直接，往下带再渐加粗；到「角」的位置及时收到框架上，不能只盯捺尖；出锋前提起来换一个面送出去",
          "measurable": {
            "direction": "右下→平出",
            "entry": "起头往里靠、弯一点",
            "exit": "提笔换面向平推出锋",
            "pressure_profile": "细→渐粗→提",
            "speed_hint": null,
            "stop_points": [
              "捺角停笔处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 19,
            "title": "第19节 结构规律回抱",
            "t": "01:06:01",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:撇",
      "name": "撇（斜撇、小短撇）",
      "font": "楷书",
      "category": "撇画类",
      "tags": [
        "撇",
        "序字",
        "印字"
      ],
      "certainty": "inferred",
      "source": {
        "part": 3,
        "title": "第3节 基础笔画撇捺",
        "t": "00:00:56",
        "evidence_count": 9
      },
      "actions": [
        {
          "id": "stroke:撇:起笔",
          "segment": "起笔",
          "action": "撇的起笔要轻、要小：夫字的撇起笔是一个小圆头，用笔尖轻轻绕一个圈就成；一定要用笔尖去写，用笔肚写起笔会很大",
          "measurable": {
            "direction": "右→左下",
            "entry": "藏锋（断开）",
            "exit": null,
            "pressure_profile": "起头厚实",
            "speed_hint": null,
            "stop_points": [
              "起笔（藏锋）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:29:30",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:撇:行笔",
          "segment": "行笔",
          "action": "撇（斜撇、小短撇）行笔时一律往左边、平着走；竖撇则不同，是先直接向下走，过半之后才向下弯曲，先出竖的部分再转成撇",
          "measurable": {
            "direction": "右上→左下",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:01:12",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:撇:收笔",
          "segment": "收笔",
          "action": "撇的收笔：从粗到细渐渐提笔，力送笔端，把尖送出去；不能往外甩",
          "measurable": {
            "direction": "向左下送出尖（斜度做直）",
            "entry": null,
            "exit": "由粗到细的尖（送出去即可，不延长）",
            "pressure_profile": "由粗到细",
            "speed_hint": "渐渐送出去，不要停顿",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:03:49",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:撇:整笔",
          "segment": "整笔",
          "action": "撇的完整动作链与两种形态：竖撇与撇的基本动作「大差不差」，大与夫两个字的撇形态不同（大起笔重、夫起笔圆且逐渐加粗），但基本用笔动作没太大区别；临帖时还要打破「起头重—中间细—下半段粗」的惯性粗细变化",
          "measurable": {
            "direction": "向左下",
            "entry": "藏锋后向左下走",
            "exit": "直挺饱满出尖，不拖长",
            "pressure_profile": "重→渐细",
            "speed_hint": null,
            "stop_points": [
              "撇尖收笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:00:56",
            "evidence_count": 14
          }
        }
      ]
    },
    {
      "id": "stroke:撇画",
      "name": "撇画",
      "font": "楷书",
      "category": null,
      "tags": [],
      "certainty": "inferred",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:撇画:行笔",
          "segment": "行笔",
          "action": "撇画按竖的方向往下走、略带斜度；把它当竖写而不是当撇写，否则容易写斜",
          "measurable": {
            "direction": "由上向下（略带斜度）",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 13,
            "title": "第13节 结构规律覆盖",
            "t": "01:44:36",
            "evidence_count": 2
          }
        }
      ]
    },
    {
      "id": "stroke:撇折",
      "name": "撇折",
      "font": "楷书",
      "category": "折画类",
      "tags": [
        "折笔",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 5,
        "title": "第5节 基础笔画折",
        "t": "00:00:38",
        "evidence_count": 8
      },
      "actions": [
        {
          "id": "stroke:撇折:起笔",
          "segment": "起笔",
          "action": "起笔写法与撇类似：逆锋入纸后落下，随即立笔调锋；可藏锋也可露锋入，前期先练熟藏锋。",
          "measurable": {
            "direction": "上→下→提（往右）→再下",
            "entry": "第一个撇折带一点藏锋（转圈下行）；第二个撇折直接露锋落下",
            "exit": null,
            "pressure_profile": "起头落笔较重",
            "speed_hint": "起笔慢，落笔稳",
            "stop_points": [
              "起笔点：笔尖着纸处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:04:18",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:撇折:行笔",
          "segment": "行笔",
          "action": "落下后由粗到细往左下方行笔；撇画外形要饱满、挺直，不能写出弧度和弯度，否则上下夹角会过紧。",
          "measurable": {
            "direction": "左下方",
            "entry": null,
            "exit": null,
            "pressure_profile": "由粗到细",
            "speed_hint": "匀速下行，不宜过快甩出",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:04:23",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:撇折:收笔",
          "segment": "收笔",
          "action": "提的末端先平铺一段把笔锋的势调正，再向右上自然提起带出尖；尖要送到位，参照小撇起笔处定长度，太短会缺口、太长会与后面的点产生或连或断的关系。",
          "measurable": {
            "direction": "右上",
            "entry": null,
            "exit": "自然提起送尖到位",
            "pressure_profile": "中 → 渐收至尖",
            "speed_hint": "收笔可稍快，不必拖长",
            "stop_points": [
              "提尖终止点：以小撇起笔处为参照，送到位为止"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:10:54",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:撇折:转折",
          "segment": "转折",
          "action": "撇到末端先把笔提起来（可在空中续势），再落下换方向、铺开笔锋向右接提；两个笔画相接处很细，可以断开写另起一笔，也可以直接提起来连着写，关键是不要把气断得太厉害。",
          "measurable": {
            "direction": "右上→左下→右",
            "entry": "提笔离纸 → 空中续势 → 落下铺开笔锋",
            "exit": "向右上带出",
            "pressure_profile": "细 → 铺锋转中 → 渐收",
            "speed_hint": "转折处略提速，不要停顿",
            "stop_points": [
              "转折接续点：撇末端与提的起笔相接处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:03:44",
            "evidence_count": 11
          }
        }
      ]
    },
    {
      "id": "stroke:平捺",
      "name": "平捺",
      "font": "楷书",
      "category": "捺画类",
      "tags": [
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 3,
        "title": "第3节 基础笔画撇捺",
        "t": "00:04:14",
        "evidence_count": 11
      },
      "actions": [
        {
          "id": "stroke:平捺:起笔",
          "segment": "起笔",
          "action": "平捺起笔：绕动入笔（露锋）或藏锋入，视情况而定，老师说大部分是藏锋多；关键在起笔之后要立马改方向走平势，而不是像斜捺那样往右下方斜下去",
          "measurable": {
            "direction": "起笔之后立刻改向平出",
            "entry": "可藏可漏",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:04:37",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:平捺:行笔",
          "segment": "行笔",
          "action": "平捺行笔走平势（平着往右走），它与斜捺最大的区别就是角度；平捺的捺角比斜捺更长，向右延伸出去，有一种更优雅的感觉",
          "measurable": {
            "direction": "左→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "渐压粗，过渡匀称",
            "speed_hint": null,
            "stop_points": [
              "捺角（比斜捺更长）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:04:30",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:平捺:收笔",
          "segment": "收笔",
          "action": "压住的时候利用一个往右去行进的方向赶，然后慢慢收束笔锋聚起来往右平着送出去",
          "measurable": {
            "exit": "右向行进，收束聚锋，平送"
          },
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:11:52",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:平捺:整笔",
          "segment": "整笔",
          "action": "平捺的用法：它用在之字这类字里非常多，也会出现在走之旁这样的偏旁里；写法上起笔后立刻走平势，捺角比斜捺更长，伸出去",
          "measurable": {
            "direction": "平出",
            "entry": "藏锋入（多）或露锋绕动入",
            "exit": "捺角延伸后由粗到细送出",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "捺角"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:04:14",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:三点水",
      "name": "三点水",
      "font": "楷书",
      "category": "点画类",
      "tags": [
        "基础笔画",
        "偏旁"
      ],
      "certainty": "inferred",
      "source": {
        "part": 4,
        "title": "第4节 基础笔画点提",
        "t": "00:23:23",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:三点水:起笔",
          "segment": "起笔",
          "action": "三点水的起笔就是第一个点：方切。顺着斜向的手势去落，起笔处比较方，甚至有一点回望的方向；落下后只落一个点，随即立柱往下走。",
          "measurable": {
            "direction": "斜向左上—右下取势（方切）",
            "entry": "方切落笔，顺着斜势走，起笔处略带回望",
            "exit": null,
            "pressure_profile": "落笔即压",
            "speed_hint": "落笔果断",
            "stop_points": [
              "方切落笔处（第一个点的起点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:22:59",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:三点水:行笔",
          "segment": "行笔",
          "action": "三点水是一笔连着写的：第一点立柱往下走，要找「刷」的感觉，边走边把宽度铺开；第二点顺着刚写完那个点的最自然笔锋露锋落下，慢慢压到笔肚、带一点转；第三点落下往右切，立柱向右上方带出弧度。",
          "measurable": {
            "direction": "第一点下行 → 第二点右下 → 第三点右上挑出",
            "entry": null,
            "exit": null,
            "pressure_profile": "落下压实 → 边走边提铺开 → 压到笔肚",
            "speed_hint": "每点落下稍快，行笔不停顿",
            "stop_points": [
              "第二点压到笔肚处（顿住收的位置）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:24:03",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:三点水:收笔",
          "segment": "收笔",
          "action": "整个三点水的收笔落在第三点：落下后往右切，顿一下（这个位置需要停一下），利用手上的惯性往上一提、立起来，把尖送出去；尖不能甩出去，也不能虚。",
          "measurable": {
            "direction": "右下→右上（送出尖锋）",
            "entry": null,
            "exit": "顿一下后往上一提，带弧度送出尖锋",
            "pressure_profile": "切 → 一顿 → 提送出",
            "speed_hint": "一顿即走，不甩",
            "stop_points": [
              "第三点顿下的位置",
              "送出尖锋处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:24:44",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:三点水第二点",
      "name": "三点水第二点",
      "font": "楷书",
      "category": null,
      "tags": [],
      "certainty": "inferred",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:三点水第二点:起笔",
          "segment": "起笔",
          "action": "氵第二点顺着上一个点写完时最自然的笔锋状态直接露锋落下，再慢慢把笔锋铺开、压到笔肚，然后带一点转。",
          "measurable": {
            "direction": "顺上一笔的笔势向右下",
            "entry": "露锋落下，接自然笔锋状态",
            "exit": null,
            "pressure_profile": "落下→渐压到笔肚",
            "speed_hint": "慢压，不抢",
            "stop_points": [
              "落笔处",
              "压到笔肚处（顿住收的位置）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:27:52",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:三点水第二点:收笔",
          "segment": "收笔",
          "action": "氵第二点压到笔肚后带一点转，顿住收笔即可；不必把笔尖提得太高，否则会变成一个特别瘦的尾巴。",
          "measurable": {
            "direction": "右下→右下收住",
            "entry": null,
            "exit": "带一点转后顿住收笔",
            "pressure_profile": "压到最粗→顿住",
            "speed_hint": "收笔不拖",
            "stop_points": [
              "顿住收笔处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:28:13",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:三点水第三点",
      "name": "三点水第三点",
      "font": "楷书",
      "category": null,
      "tags": [],
      "certainty": "inferred",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:三点水第三点:整笔",
          "segment": "整笔",
          "action": "氵第三点落下后向右下带，然后立柱往右上方带出弧度送出，收成一个小尖；尖不能太虚，也不能甩出去，落笔要稳、要往右切、到位后一顿。",
          "measurable": {
            "direction": "落下→右下带→右上挑出（带弧度）",
            "entry": "落下往右切，稳一些",
            "exit": "向上带弧度送出尖锋",
            "pressure_profile": "细→一顿→送出",
            "speed_hint": "一顿即走，不甩",
            "stop_points": [
              "顿一下的位置",
              "送出尖锋处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:29:21",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:三点水第一点",
      "name": "三点水第一点",
      "font": "楷书",
      "category": null,
      "tags": [],
      "certainty": "inferred",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:三点水第一点:起笔",
          "segment": "起笔",
          "action": "氵的第一点起笔是方切：顺着斜向的手势落下，起笔处比较方、略带回望的方向；只落下一个点，随后立柱往下走。",
          "measurable": {
            "direction": "斜向左上—右下取势（方切）",
            "entry": "方切落笔，顺着斜势走",
            "exit": null,
            "pressure_profile": "落笔即压",
            "speed_hint": "落笔果断，不迟疑",
            "stop_points": [
              "方切落笔处（点画起点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:22:59",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:三点水第一点:行笔",
          "segment": "行笔",
          "action": "氵第一点落下后立柱往下走，边走边提起来，动作要稍快，同时把宽度铺开；落稳之后笔尖才不会凸出去弹出去。",
          "measurable": {
            "direction": "竖直向下（带一点弧度）",
            "entry": null,
            "exit": null,
            "pressure_profile": "落笔压实→边走边提→宽度铺开",
            "speed_hint": "落下稍快，行笔不停顿",
            "stop_points": [
              "立柱下行途中（提笔的过渡点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:23:40",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:竖",
      "name": "竖",
      "font": "楷书",
      "category": "竖画类",
      "tags": [
        "横细竖粗",
        "撑住",
        "竖钩"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 12,
        "title": "第12节 结构规律相迭",
        "t": "00:04:43",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:竖:行笔",
          "segment": "行笔",
          "action": "在字框内要挺直为主、略带倾斜，不能有弧度；必要时保持直挺",
          "measurable": {
            "direction": "上→下，向内收",
            "entry": null,
            "exit": null,
            "pressure_profile": "略粗（比横粗）",
            "speed_hint": "尽量走直线",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第12节 结构规律相迭",
            "t": "00:08:52",
            "evidence_count": 11
          }
        }
      ]
    },
    {
      "id": "stroke:竖钩",
      "name": "竖钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "笔画名",
        "钩画类",
        "钩",
        "竖钩",
        "对比",
        "位置",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 6,
        "title": "第6节 基础笔画钩",
        "t": "00:05:35",
        "evidence_count": 21
      },
      "actions": [
        {
          "id": "stroke:竖钩:起笔",
          "segment": "起笔",
          "action": "竖钩起笔用藏锋：先逆锋向上，再向右平着落下去、平落处一顿，随即立柱（立笔）往正下方走；起笔头要方中带圆，不能太圆也不能太尖。",
          "measurable": {
            "direction": "上→下（起笔后带出弧度往左下）",
            "entry": "逆锋上去顺势落下来",
            "exit": null,
            "pressure_profile": "轻入→沉住气落下",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "00:06:00",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖钩:行笔",
          "segment": "行笔",
          "action": "竖钩行笔一直往正下方走，要走中锋；从中段开始带一点弧度并向左内收，这一段是为出钩和把势态往左包做准备；不能写得笔直，也不能弧度太大。中段变细是提起来了，不要有过于粗细的突变。",
          "measurable": {
            "direction": "上→下（带弧度）",
            "entry": "落下即粗厚",
            "exit": null,
            "pressure_profile": "落笔粗→带弧度渐提",
            "speed_hint": "慢，带弧度",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "00:04:19",
            "evidence_count": 31
          }
        },
        {
          "id": "stroke:竖钩:收笔",
          "segment": "收笔",
          "action": "竖钩收笔（钩尖）：钩要小而且精神，尖不能太大、也不能太细太爽利，要与笔画本身的粗度相称，从粗到细有一个过渡，不能太小。",
          "measurable": {
            "direction": "向左上收尖",
            "entry": null,
            "exit": "小而有精神的尖钩，由粗到细过渡",
            "pressure_profile": "由粗到细（不可过细）",
            "speed_hint": "送出即止，不拖长",
            "stop_points": [
              "钩尖末端（收尖处）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "00:48:56",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖钩:转折",
          "segment": "转折",
          "action": "竖画到底后：手顺应过来顿住、停住，然后才往左边出钩。停顿位置老师有明确要求——一般在起笔和收笔处会做夸张的停顿，中段不敢停就把动作放慢；中间一停整个字就「阴」掉了。",
          "measurable": {
            "direction": "下→停→左",
            "entry": null,
            "exit": null,
            "pressure_profile": "顿住停住",
            "speed_hint": "停住再走，不要滑过去",
            "stop_points": [
              "竖画下端手顺应过来后的顿点（停住处）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:23:09",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖钩:钩",
          "segment": "钩",
          "action": "竖钩出钩：走到底端停住（一个停顿、有拍子感，笔锋随之聚拢），稍微一提，再把笔尖往左上方转上去——先转笔、手带着笔一起转，笔尖指向上方，然后压住笔毫的弹性把锋送出去，向左上方出一个小尖钩，方向是朝字内、呼应左半部分。",
          "measurable": {
            "direction": "下→出勾",
            "entry": null,
            "exit": "向左提出，钩出得快",
            "pressure_profile": "渐提后出锋",
            "speed_hint": "快而自然",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "00:04:45",
            "evidence_count": 38
          }
        }
      ]
    },
    {
      "id": "stroke:竖撇",
      "name": "竖撇",
      "font": "楷书",
      "category": "撇画类",
      "tags": [
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 3,
        "title": "第3节 基础笔画撇捺",
        "t": "00:01:05",
        "evidence_count": 10
      },
      "actions": [
        {
          "id": "stroke:竖撇:起笔",
          "segment": "起笔",
          "action": "竖撇起笔：逆入顿作点状，随即调锋略向上提；本节的竖撇是藏锋直接往上去藏，因为笔画前头饱满，还必须把笔锋往上提，笔尖在这一瞬间翻折下来再往下走，这样前头位置才有力度",
          "measurable": {
            "direction": "起笔向上顿（藏锋），随即折向下",
            "entry": "藏锋后笔尖轻绕小圈成小圆头",
            "exit": null,
            "pressure_profile": "顿（重）→微提→行笔",
            "speed_hint": null,
            "stop_points": [
              "起笔处顿作点状的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:01:05",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:竖撇:行笔",
          "segment": "行笔",
          "action": "行笔先做直线段：起头之后到中段保持较直的状态；行笔要提着走，感受线条的紧凑（老师称为「勒紧」），不能把毛笔拖在纸面上走，否则线条虚弱无力、浮在纸面；手上要使劲，但力度绝不能过大，过大就会僵硬呆滞",
          "measurable": {
            "direction": "上→右→下→左下",
            "entry": null,
            "exit": null,
            "pressure_profile": "行笔偏提（提着走），线条紧凑",
            "speed_hint": "慢而稳，不飘",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:09:59",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:竖撇:收笔",
          "segment": "收笔",
          "action": "收笔：从粗到细渐渐提笔，力送笔端送到位，把尖送出去；千万不要往外甩（老师说「一定要送到位不要甩出去」）",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "由粗到细渐提，力送笔端送出尖，不甩",
            "pressure_profile": "粗→细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:11:39",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖撇:转折",
          "segment": "转折",
          "action": "过半之后才开始向下弯曲、铺垫弧度，铺弧度的同时带出粗细变化；弧度最弯（最歪）的地方是最粗的地方，然后顺势往左下带出尖",
          "measurable": {
            "direction": "下→左下",
            "entry": null,
            "exit": null,
            "pressure_profile": "细→粗（铺弧度时同时变粗），到最弯处最粗",
            "speed_hint": null,
            "stop_points": [
              "弧度起始处（笔画过半之后）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 基础笔画撇捺",
            "t": "00:12:34",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:竖提",
      "name": "竖提",
      "font": "楷书",
      "category": "提画类",
      "tags": [
        "基础笔画",
        "B档",
        "竖提",
        "恨字"
      ],
      "certainty": "inferred",
      "source": {
        "part": 4,
        "title": "第4节 基础笔画点提",
        "t": "01:05:49",
        "evidence_count": 12
      },
      "actions": [
        {
          "id": "stroke:竖提:行笔",
          "segment": "行笔",
          "action": "竖提的竖要带一点向左回包的弧度，不是纯直线；竖的直度直接决定整字会不会歪斜，所以写竖时要先求直挺。",
          "measurable": {
            "direction": "竖直向下（略向左包）→右上挑出",
            "entry": null,
            "exit": null,
            "pressure_profile": "落笔顿→中段匀→提起渐细",
            "speed_hint": "匀速，压住笔锋再走",
            "stop_points": [
              "竖的起笔顿点",
              "转折圆转处",
              "提的出锋点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "01:05:49",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖提:收笔",
          "segment": "收笔",
          "action": "竖提的提要由粗到细送到位，收在字内；如果连笔写，是笔尖往左一提但仍停留在纸面上，再往下一切一顿，立柱后向右上提出去。",
          "measurable": {
            "direction": "右上",
            "entry": null,
            "exit": "立柱后向右上提，由粗到细送到位",
            "pressure_profile": "粗→细",
            "speed_hint": "渐行渐提",
            "stop_points": [
              "一顿处",
              "出锋终点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "01:10:29",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖提:转折",
          "segment": "转折",
          "action": "与撇折非常相像，只不过一个是竖一个是撇。起笔藏锋入、往右下顿，自然带出一点小小的圆转，再往左下方撑开小弧度；弧度不要做得太夸张，微挺就可以了",
          "measurable": {
            "direction": "上→右下→左下"
          },
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:14:16",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:竖提:整笔",
          "segment": "整笔",
          "action": "竖提只通过线条的左侧面表现「内掖」的体式，右侧面仍保持直挺；下笔顺势往左撇，换一个锋面落下铺开笔锋再往上走",
          "measurable": {
            "direction": "向下→向右上",
            "entry": "斜落铺开笔锋",
            "exit": "向上挑出",
            "pressure_profile": "细过渡→抵住压下→铺锋提起",
            "speed_hint": null,
            "stop_points": [
              "竖提转折处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 19,
            "title": "第19节 结构规律回抱",
            "t": "01:40:13",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:竖弯钩",
      "name": "竖弯钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "竖弯钩",
        "钩画类",
        "B档",
        "弯钩"
      ],
      "certainty": "inferred",
      "source": {
        "part": 8,
        "title": "第8节 基础笔画钩3",
        "t": "00:04:38",
        "evidence_count": 16
      },
      "actions": [
        {
          "id": "stroke:竖弯钩:起笔",
          "segment": "起笔",
          "action": "竖弯钩的起头最要紧的是「干净」：逆锋向上、落下就把形态和宽度定住，立住之后再站起来把笔锋铺开往下走。起笔处不能钝出一个大圆头，笔尖一定要藏住，藏锋的动作要果断。",
          "measurable": {
            "direction": "向下",
            "entry": "藏锋落下",
            "exit": null,
            "pressure_profile": "落笔饱满→行笔稍提→再渐压",
            "speed_hint": "先直挺一小段，再提速",
            "stop_points": [
              "起笔藏锋落点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:04:54",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:竖弯钩:行笔",
          "segment": "行笔",
          "action": "往下走这一段不加任何轻重按压，保持粗细平均；转过来之后一定要保证中锋线，往右继续铺开笔锋、慢慢压下。底边最低点的那条线要正、要稳；往上扬的姿态只能从后半段才开始。",
          "measurable": {
            "direction": "下→斜内→平向右",
            "entry": null,
            "exit": null,
            "pressure_profile": "轻→渐重",
            "speed_hint": "加速后到弯道即收",
            "stop_points": [
              "转弯道处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:05:22",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:竖弯钩:收笔",
          "segment": "收笔",
          "action": "小勾收尖时很直，由粗到细；不要挑着往外走，也不要直接往回去弹——这两个方向都太多余。收的时候靠上下提按的反作用力往上提。",
          "measurable": {
            "direction": "向上收尖（直线方向，不外挑、不回弹）",
            "entry": null,
            "exit": "小勾由粗到细收尖",
            "pressure_profile": "由粗到细",
            "speed_hint": "直接干脆",
            "stop_points": [
              "钩尖末端（收尖处）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:09:51",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖弯钩:转折",
          "segment": "转折",
          "action": "转弯用圆转：把笔尖往右下一顿，顺势做出一个小圆头，然后往下一转。转弯那一小段不能突然提细，整个过渡要匀；转的时候可以稍微有点细的感觉，但绝对不能细到一碰就断。",
          "measurable": {
            "direction": "上→下→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "转弯处略细（不可细到一碰就断）",
            "speed_hint": "转要稳，不要甩",
            "stop_points": [
              "向右下一顿的点（小圆头起转处）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:02:22",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:竖弯钩:钩",
          "segment": "钩",
          "action": "出钩：先用平铺压下去，顿住——停下来一定是有力度的；顺势压住之后往回弹，笔尖随之提起来，慢慢收拢，再往上换个方向，由粗到细送出一个小勾。钩是从笔画的内部出钩的，不是从笔画外边另起一个钩。",
          "measurable": {
            "direction": "向右上",
            "entry": null,
            "exit": "沿外侧出小勾",
            "pressure_profile": "压住→提",
            "speed_hint": "出钩要压住笔再提",
            "stop_points": [
              "出钩前压笔处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:06:48",
            "evidence_count": 16
          }
        },
        {
          "id": "stroke:竖弯钩:整笔",
          "segment": "整笔",
          "action": "竖弯钩全链（沿用前面几节立下的逆锋→落下→立笔调锋→铺锋）：逆锋上去→落下把宽度定住→立住并铺开笔锋往下走→转弯处往右下一顿做小圆头→转过来压厚→底边最低点顿住→往回弹提锋→慢慢收拢、上换方向→由粗到细送出小勾。",
          "measurable": {
            "direction": "逆锋向上→落下→向下→转平向右→向上出小勾",
            "entry": "藏锋落下定宽度",
            "exit": "由粗到细送出小勾（笔画内部出钩）",
            "pressure_profile": "落下压住→行笔粗细平均→转弯略细→转后压厚→顿住→提锋→由粗到细",
            "speed_hint": "落下之后不停笔，一气写到出钩",
            "stop_points": [
              "落下立笔处",
              "向右下一顿的小圆头处",
              "底边最低点的顿住处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:05:12",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:竖折",
      "name": "竖折",
      "font": "楷书",
      "category": "折画类",
      "tags": [
        "折笔",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 5,
        "title": "第5节 基础笔画折",
        "t": "00:24:46",
        "evidence_count": 9
      },
      "actions": [
        {
          "id": "stroke:竖折:起笔",
          "segment": "起笔",
          "action": "逆锋入纸落下并立住笔锋，随即收紧笔锋向右下转，进入竖的部分；起笔动作不要拖得太长。",
          "measurable": {
            "direction": "落下后由上向下（竖的方向）",
            "entry": "逆锋入纸后立锋收紧",
            "exit": null,
            "pressure_profile": "起头重",
            "speed_hint": "起笔动作不拖长，落下即转",
            "stop_points": [
              "起笔点：竖的上端着纸处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:20:42",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖折:行笔",
          "segment": "行笔",
          "action": "竖的部分先走一段平直的过渡，过了横以后再开始明显往内包；起头重、行笔时慢慢提起来变细，往内包时不能出现往下越走越粗的线条。",
          "measurable": {
            "direction": "向下 → 过渡后向内（向左）收",
            "entry": null,
            "exit": null,
            "pressure_profile": "起头重 → 行笔渐细 → 转弯处略有胖感但不再加粗",
            "speed_hint": "过渡段匀速，转弯后略放慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:25:48",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖折:收笔",
          "segment": "收笔",
          "action": "折出的横画收笔时慢慢压重，往下一顿再回锋；写法是先向上平走一小段，再慢慢铺开。",
          "measurable": {
            "direction": "向右",
            "entry": null,
            "exit": "顿笔回锋",
            "pressure_profile": "细 → 渐粗 → 顿住回锋",
            "speed_hint": "先细后铺，收笔稳住",
            "stop_points": [
              "收笔顿点：横画末端下顿处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:22:37",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:竖折:转折",
          "segment": "转折",
          "action": "竖到底提笔、落下、往内去转，转折处要顿住压实再走，边走边压；停住硬调笔尖会呲上去，转调锋的位置极易出侧锋，所以一定要压住。",
          "measurable": {
            "direction": "上→下→右",
            "entry": "提笔落下、顿住压实",
            "exit": "边走边压，铺锋向右",
            "pressure_profile": "顿住时最重，随后由粗到细",
            "speed_hint": "边走边压，不停笔",
            "stop_points": [
              "转折顿点：竖与横相接的折角处，需顿住压实"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 5,
            "title": "第5节 基础笔画折",
            "t": "00:34:02",
            "evidence_count": 10
          }
        }
      ]
    },
    {
      "id": "stroke:提",
      "name": "提",
      "font": "楷书",
      "category": "提画类",
      "tags": [
        "基础笔画",
        "提",
        "呼应"
      ],
      "certainty": "inferred",
      "source": {
        "part": 4,
        "title": "第4节 基础笔画点提",
        "t": "01:20:42",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:提:起笔",
          "segment": "起笔",
          "action": "提的起笔是切笔（竖提的竖）：落笔要稳，往右切一下，转折处略圆，往右一转后立住笔锋；起笔的藏锋要做清楚。",
          "measurable": {
            "direction": "起笔向右（右上）",
            "entry": "藏锋顿住后往右一带",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "顿住处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "01:21:28",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:提:行笔",
          "segment": "行笔",
          "action": "虫字底下的提画：底盘如果跟着上面的宽度走就显小，所以要往外撑一撑、往上去提，略微有一点倾斜的体式但带弧度；它和上部往右上倾斜的体式一路走，只是方向上略有相悖，于是横向出现外拓的体式。",
          "measurable": {
            "direction": "左下→右上（外撑、上提）",
            "exit": "带弧度"
          },
          "certainty": "verbatim",
          "source": {
            "part": 14,
            "title": "第14节 结构规律顶戴",
            "t": "00:14:17",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:提:收笔",
          "segment": "收笔",
          "action": "提的尖要向右上方送出去，由粗到细送到位，收在目标之内，不要甩出去写成虚尖。",
          "measurable": {
            "direction": "向右上，到位即止",
            "entry": null,
            "exit": "及时停住（顿住），不出尖、不拉长",
            "pressure_profile": "由重到轻，收处停住",
            "speed_hint": null,
            "stop_points": [
              "收笔的停住处（顿点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "01:10:57",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:提:转折",
          "segment": "转折",
          "action": "提的过渡段要有一次圆转，笔锋必须翻转过来之后才能走直线那一段；如果笔锋还没翻转就急着走，会跑出来或者压不住。圆转之后仍是侧锋的话，后面的提就挺不直、也不够饱满。",
          "measurable": {
            "direction": "竖直下→向右上的斜线",
            "entry": null,
            "exit": null,
            "pressure_profile": "顿→立柱→渐提渐细",
            "speed_hint": "等笔锋翻转过来再行笔",
            "stop_points": [
              "圆转（转折）处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "01:20:42",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:提:整笔",
          "segment": "整笔",
          "action": "提画整体是短促连贯的一笔：切笔落笔立住 → 圆转并等笔锋翻转 → 走直挺的一段 → 向右上送出尖锋。竖提可断可连，断开写就重新落笔立住再提。",
          "measurable": {
            "direction": "右上",
            "entry": "偏侧落笔",
            "exit": "向右上挑出",
            "pressure_profile": "偏胖→渐收",
            "speed_hint": null,
            "stop_points": [
              "提的起笔处",
              "提尖"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "01:05:21",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:弯钩",
      "name": "弯钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "笔画名",
        "钩画类",
        "钩",
        "弯钩",
        "马字",
        "B档",
        "翻转笔画",
        "换封面"
      ],
      "certainty": "inferred",
      "source": {
        "part": 6,
        "title": "第6节 基础笔画钩",
        "t": "00:00:58",
        "evidence_count": 20
      },
      "actions": [
        {
          "id": "stroke:弯钩:起笔",
          "segment": "起笔",
          "action": "弯钩的起笔顺承上一个笔画：先往左一带，再往右铺笔锋，因此起笔处会带一点藏锋；作为小横起笔时是「落下—压住顶住—往上去回」带弧度的写法，提起来是因为粗细由重到轻、节奏由慢到快。",
          "measurable": {
            "direction": "起笔往左一带→往右铺笔锋；小横带微微上扬的弧度",
            "entry": "顺承上一笔→往左一带→往右铺笔锋（带一点藏锋）",
            "exit": null,
            "pressure_profile": "落下压住→由重到轻提起来",
            "speed_hint": "由慢到快，提起来是笔画之间的衔接纽带",
            "stop_points": [
              "落下压住（顶住）处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "01:01:16",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:弯钩:行笔",
          "segment": "行笔",
          "action": "弯钩行笔：顺着往下走之后向左下转，转出弧度；转的时候不只转毛笔，还要手带着毛笔一起转，转过来的一段仍是中锋线条，随后把这一段压厚实一点、加粗不要过度，拖到下面再往左收。",
          "measurable": {
            "direction": "转折后顺时针方向画圆往内收",
            "entry": null,
            "exit": null,
            "pressure_profile": "顺笔渐提，由粗到细",
            "speed_hint": "走到手腕内侧处顺势提起",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "01:03:47",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:弯钩:收笔",
          "segment": "收笔",
          "action": "弯钩收笔：弧度要及时停住再往上去勾，小勾可以再收小一点，拖长了会显得飘逸、也会与旁边的撇画打架；出勾的位置受字内空间限制，不能随意地甩出去。",
          "measurable": {
            "direction": "弧度停住后向左上出尖",
            "entry": null,
            "exit": "小勾收尖，不宜过长",
            "pressure_profile": "由粗到细",
            "speed_hint": "及时停住，不要拖",
            "stop_points": [
              "弧度停住处（收势顿点）",
              "钩尖末端"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "01:00:36",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:弯钩:转折",
          "segment": "转折",
          "action": "马字折笔处的转折：笔尖本身先铺住，再提起来往右转、压住笔锋往下去走，包一个圆弧；转圆时不能只靠手指，手腕必须跟着一起走——手腕压死了用手指往内转，这个位置必然偏锋。",
          "measurable": {
            "direction": "右→下（圆转包弧）",
            "entry": null,
            "exit": null,
            "pressure_profile": "笔尖铺住→提起→转时压住笔锋",
            "speed_hint": "转圆时手腕随行，不能压死",
            "stop_points": [
              "折笔处提起来往右下一顿的顿点（小肩膀）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "00:28:37",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:弯钩:钩",
          "segment": "钩",
          "action": "弯钩出钩：走到弯钩底部停住，调锋后往左上方（平着向左）出一个小勾；钩的方向必须是往左延伸、把字的内侧包得更大，绝不能往内挑——往内挑会把这个面直接切断。",
          "measurable": {
            "direction": "向左偏上",
            "entry": null,
            "exit": "一顿→笔内调锋→向左挑出小尖",
            "pressure_profile": "顿住→回拉收拢→弹送出尖",
            "speed_hint": "靠手指速度弹出去，不甩",
            "stop_points": [
              "收笔处顿住、把笔尖往回拽的顿点",
              "小勾送出的出锋点（向左上）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 6,
            "title": "第6节 基础笔画钩",
            "t": "01:03:11",
            "evidence_count": 25
          }
        },
        {
          "id": "stroke:弯钩:整笔",
          "segment": "整笔",
          "action": "弯钩的弧度像画一个圈然后再往下去走；利用钩的小尖儿延长出去一点，严格画它的边线会略微斜一点，但很容易把这个斜度拉得过于长",
          "measurable": {},
          "certainty": "inferred",
          "source": {
            "part": 9,
            "title": "第9节 笔画总汇",
            "t": "00:15:24",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:卧钩",
      "name": "卧钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "钩",
        "卧钩",
        "心字底",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 7,
        "title": "第7节 基础笔画钩2",
        "t": "00:00:56",
        "evidence_count": 9
      },
      "actions": [
        {
          "id": "stroke:卧钩:起笔",
          "segment": "起笔",
          "action": "卧钩（心字底）起笔是直接露锋入笔（不藏锋），顺着钩势往下一段弧度走，把弧度先解决掉、把粗细顺过来，再平着走；起笔处不要犹豫，要一气铺开笔锋。",
          "measurable": {
            "direction": "入笔后向下走一段弧度（弯向右）",
            "entry": "直接露锋入笔",
            "exit": null,
            "pressure_profile": "入笔即铺开笔锋",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "01:10:30",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:卧钩:行笔",
          "segment": "行笔",
          "action": "卧钩行笔的粗细节奏：往下走一段弯向右的弧度，走到平处换方向再往右拉、同时加粗——平的地方是最粗的地方；然后慢慢收到尾部，仍是整段中锋的线条。像一艘船：平处最鼓，尾部收细。",
          "measurable": {
            "direction": "右上→右下→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "渐重，笔肚压",
            "speed_hint": null,
            "stop_points": [
              "弧度转平处（最粗点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "01:10:47",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:卧钩:转折",
          "segment": "转折",
          "action": "卧钩走到最边缘（船尾）时要稍微往上一翘，然后在这里回上去；落笔位置往下先走一段弧度，再用笔肚压到此处、正好停住，方向感是往内去包。",
          "measurable": {
            "direction": "走到最边缘稍往上一翘，随即回上去",
            "entry": null,
            "exit": null,
            "pressure_profile": "笔肚压到位，正好停住",
            "speed_hint": "到边缘停住再回",
            "stop_points": [
              "船尾处（最边缘）往上一翘的顿点",
              "笔肚压到位停住的位置"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "01:18:05",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:卧钩:钩",
          "segment": "钩",
          "action": "卧钩出钩：往左上方出这个勾，小尖的方向往内去包——不能往上随便甩出去，也不能往右走；方向是为了和下一笔产生联系，所以不要太随意。",
          "measurable": {
            "direction": "向左上方出钩，方向朝内（往内包）",
            "entry": null,
            "exit": "顿住→上调锋→笔尖向左带→自内侧顺时针出钩",
            "pressure_profile": "收细之后往内上起",
            "speed_hint": null,
            "stop_points": [
              "回上去的停住处",
              "向左上出钩的出锋点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "01:11:07",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:卧钩:整笔",
          "segment": "整笔",
          "action": "卧钩是本节最难的长笔画。分段讲法：①入笔弯一段，先把弧度解决、粗细顺过来；②由细到粗平着走；③往右上方抬，自然收细；④往内上起钩。写的时候这四段必须一气顺承，不能停一下再停一下。",
          "measurable": {
            "direction": "入笔弯段→平着走→右上抬（收细）→往内上起钩",
            "entry": "直接露锋入笔",
            "exit": "往左上出勾，方向朝内",
            "pressure_profile": "细→粗（平处最粗）→抬时收细",
            "speed_hint": "分段只是观察方法，书写要一气",
            "stop_points": [
              "平着走的最粗处",
              "船尾往上一翘处",
              "出钩的停住处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 7,
            "title": "第7节 基础笔画钩2",
            "t": "01:12:58",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:小短撇",
      "name": "小短撇",
      "font": "楷书",
      "category": "撇画类",
      "tags": [
        "A档-基础笔画",
        "撇画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 2,
        "title": "第2节 基础笔画撇捺",
        "t": "01:32:58",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:小短撇:起笔",
          "segment": "起笔",
          "action": "笔尖藏锋起笔，随即落下做一个顿笔动作，再提起来往左下方行笔。落笔要果断，不要犹豫。",
          "measurable": {
            "direction": "起笔原地落下顿笔后转左下",
            "entry": "藏锋起笔，笔尖藏住后落下",
            "exit": null,
            "pressure_profile": "轻入→落下顿笔加重→提起转轻",
            "speed_hint": "落笔果断，不犹豫",
            "stop_points": [
              "起笔落下后的顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "00:02:31",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:小短撇:行笔",
          "segment": "行笔",
          "action": "提笔后向左下方行笔，由粗到细；行笔中把笔的上侧面做饱满，慢慢送到尖。力度由重到轻，重处慢、轻处稍快。",
          "measurable": {
            "direction": "左下",
            "entry": null,
            "exit": null,
            "pressure_profile": "重→轻（由粗到细）",
            "speed_hint": "重处慢、轻处稍快",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "00:05:05",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:小短撇:收笔",
          "segment": "收笔",
          "action": "向左下把尖送出去，出锋要挺直有力；不能描着轻轻飘过去，否则尖部发虚、显得轻飘。",
          "measurable": {
            "direction": "左下",
            "exit": "向左下出锋，尖部挺直",
            "pressure_profile": "轻→渐收出尖",
            "speed_hint": "出锋送到尖",
            "stop_points": [
              "出锋前笔尖送到位处的停点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "00:05:52",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:小短撇:转折",
          "segment": "转折",
          "action": "落下之后把笔锋压住、先向下使劲压实（即下顿），再顺势往下走；顿笔太小会盖不住上侧面，多余的部分就会露出来。",
          "measurable": {
            "direction": "落下后原地向下压实",
            "entry": "顿笔后笔锋压住不放",
            "exit": null,
            "pressure_profile": "顿笔处为最重",
            "speed_hint": null,
            "stop_points": [
              "落下后的顿点（须压实，才能盖住上侧面）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "00:04:26",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:小短撇:整笔",
          "segment": "整笔",
          "action": "小短撇也叫平撇：笔画短、斜度小、方向趋于平，所以收笔即行笔、行笔即收笔；写的时候去找一种平的方向，不要写得太斜，甚至可以和横画比较。",
          "measurable": {
            "direction": "趋于平（左下但斜度小）",
            "entry": "藏锋起（藏锋动作不唯一，可往上提也可直接按下去）",
            "exit": "平向左下快速出锋",
            "pressure_profile": "由重到轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:32:58",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:小横",
      "name": "小横",
      "font": "楷书",
      "category": "横画类",
      "tags": [
        "小横",
        "左肩横",
        "颜勤礼碑"
      ],
      "certainty": "inferred",
      "source": {
        "part": 1,
        "title": "第1节 基础笔画横竖",
        "t": "00:40:01",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:小横:起笔",
          "segment": "起笔",
          "action": "用笔尖一点点逆入即可，动作很小，入笔方向（往左上还是平入）可以忽略不计",
          "measurable": {
            "direction": "左上或平入皆可，动作极小",
            "entry": "一点点逆入",
            "exit": null,
            "pressure_profile": "轻",
            "speed_hint": "动作很小",
            "stop_points": [
              "逆入落下的小顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:45:51",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:小横:行笔",
          "segment": "行笔",
          "action": "向右行笔时向右上倾斜，让线条上下左右饱满起来",
          "measurable": {
            "direction": "右，向上倾斜",
            "entry": null,
            "exit": null,
            "pressure_profile": "行笔渐加粗，整体饱满",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:44:24",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:小横:收笔",
          "segment": "收笔",
          "action": "收笔轻提后下按回锋；起笔与收笔的小动作全部用笔尖完成，不需要重压",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "轻提→下按→回锋",
            "pressure_profile": "轻，不用重压",
            "speed_hint": null,
            "stop_points": [
              "收笔下按处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:46:18",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:小横:整笔",
          "segment": "整笔",
          "action": "完整流程：一点点逆入→落下→向右行笔并渐渐加粗→收笔轻提→下压→回锋",
          "measurable": {
            "direction": "左→右，向上倾斜",
            "entry": "一点点逆入",
            "exit": "轻提→下压→回锋",
            "pressure_profile": "行笔渐粗→收笔轻提",
            "speed_hint": null,
            "stop_points": [
              "起笔逆入落下处",
              "收笔下压回锋处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:47:10",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:斜钩",
      "name": "斜钩",
      "font": "楷书",
      "category": "钩画类",
      "tags": [
        "斜钩",
        "钩画类",
        "圆笔",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 8,
        "title": "第8节 基础笔画钩3",
        "t": "00:19:50",
        "evidence_count": 14
      },
      "actions": [
        {
          "id": "stroke:斜钩:起笔",
          "segment": "起笔",
          "action": "斜钩起笔用圆笔：逆上去落下来，头要做圆一些、厚一点，再往下去走；不要出现漏风太严重的用笔动作。帖上另一种写法把撇画和斜钩的起笔都做圆。",
          "measurable": {
            "direction": "逆锋向上→落下（做圆、做厚）→向右下带",
            "entry": "圆笔起笔：落下即把头做圆做厚，笔锋不外露",
            "exit": null,
            "pressure_profile": "落下后压住（头要厚）",
            "speed_hint": "落下不犹豫，随即往右下方带出弧线",
            "stop_points": [
              "落下做圆头的压住处（起笔顿点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:21:29",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:斜钩:行笔",
          "segment": "行笔",
          "action": "斜钩行笔全程保持中锋线：先往内去凹陷，再往外去张开；它不是一个倒的笔画，是从正上方到右下角，体式高挑，后半段不要平着延长。下半段要有下沉的感觉，沉下去挤住中间那块空间，过了中段之后才往右拉开。上半段不粗不细、中段可以略细，靠提起笔来带。",
          "measurable": {
            "direction": "左上→右下→右上",
            "entry": null,
            "exit": null,
            "pressure_profile": "下段略粗→渐提",
            "speed_hint": "带上速度感往下去拉；这种长笔画一定要靠腕来用",
            "stop_points": [
              "弧度下沉开始处（下半段）",
              "下沉到底部即将转折处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:21:47",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:斜钩:收笔",
          "segment": "收笔",
          "action": "钩尖由粗到细、略圆；钩必须从笔画内部长出来，不能从外边往上出勾——那两种钩长得完全不一样。也不要把钩往右走得太大或往内凹得太大，否则会把这一块内白挤窄。",
          "measurable": {
            "direction": "向上收尖（不出笔画外部）",
            "entry": null,
            "exit": "由粗到细、略圆的钩尖",
            "pressure_profile": "由粗到细",
            "speed_hint": "往回一带即出勾",
            "stop_points": [
              "钩尖末端"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:30:10",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:斜钩:钩",
          "segment": "钩",
          "action": "斜钩出钩：走到收笔的位置先顿一下——停住就是顿笔，停住、顿住都要是有力度的；然后往回一收，落下之后笔尖提起来就是「往回弹」，靠的是笔尖的弹性；提笔之后一定要把锋聚好，往上（略偏右上）送出去。提得太早或太快就会写成过尖、过细的一根线。",
          "measurable": {
            "direction": "向上、略偏右上送出尖；从笔画内部出钩",
            "entry": null,
            "exit": "一顿→笔尖立起向上提→自内向上出钩",
            "pressure_profile": "顿住压住→提起聚锋→送出由粗到细",
            "speed_hint": "简省",
            "stop_points": [
              "收笔位置的顿笔停住处（出钩前顿点）",
              "聚锋后向上送出的起转点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:25:12",
            "evidence_count": 14
          }
        },
        {
          "id": "stroke:斜钩:整笔",
          "segment": "整笔",
          "action": "斜钩全链：逆锋上去→落下把头做圆做厚→保持中锋线一直带到底（弧度一定要顺势画）→底部顿笔停住→往回一收提锋→聚锋→向上送出尖。整条笔画不需要画圈之类的多余动作，因为勾画本身是顺势走的：在笔画内部往回提、再往上走就很连贯。",
          "measurable": {
            "direction": "逆锋向上→落下→右下（带弧度）→底部顿住→向上出尖",
            "entry": "圆笔藏锋落下",
            "exit": "由粗到细送出尖（笔画内部出钩）",
            "pressure_profile": "头厚→中段略细→底部顿住→提锋→由粗到细",
            "speed_hint": "一气写成，不要在底部绕圈",
            "stop_points": [
              "落下做圆头处",
              "底部顿笔处",
              "聚锋上送处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 8,
            "title": "第8节 基础笔画钩3",
            "t": "00:24:40",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:斜捺",
      "name": "斜捺",
      "font": "楷书",
      "category": "捺画类",
      "tags": [
        "A档-基础笔画",
        "捺画",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 2,
        "title": "第2节 基础笔画撇捺",
        "t": "01:09:45",
        "evidence_count": 8
      },
      "actions": [
        {
          "id": "stroke:斜捺:起笔",
          "segment": "起笔",
          "action": "起笔要有「勒紧」的感觉：提着气写，笔尖不能散、不能松散泄劲，然后逆入往右下走。起笔有两种：一种是承接上边撇画的连带式入笔（跟着上一笔直接入进来），一种是单写时直接逆入压下再翻折。",
          "measurable": {
            "direction": "右下",
            "entry": "反向逆入（藏锋）或 顺姿转下（露锋）",
            "exit": null,
            "pressure_profile": "起笔紧聚，笔锋不散",
            "speed_hint": null,
            "stop_points": [
              "起笔勒紧、笔尖顶住时的顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:16:15",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:斜捺:行笔",
          "segment": "行笔",
          "action": "行笔过程要把笔锋铺开、铺满，两边保证厚度，不要压得太重；力度由轻到重，渐行渐压。整个行笔要带弧度，弧度往下沉，不能写成一条僵直的直线。",
          "measurable": {
            "direction": "左上→右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "渐压粗",
            "speed_hint": null,
            "stop_points": [
              "打结处停顿",
              "捺角"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:16:56",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:斜捺:收笔",
          "segment": "收笔",
          "action": "到最重的位置稍驻笔，随即向右提锋出尖，一定要沿着上侧面出锋；笔锋要聚拢再出尖，捺脚要挺健饱满，不能虚。",
          "measurable": {
            "direction": "右",
            "exit": "沿上侧面提锋出尖",
            "pressure_profile": "最重处驻笔后提锋收尖",
            "speed_hint": "提锋出尖要快",
            "stop_points": [
              "最重位置的驻笔停顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:14:24",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:斜捺:转折",
          "segment": "转折",
          "action": "起笔转过来之后笔尖顺势往上去压，再往右下翻折；笔尖要压住、顶住再回来，然后顺势下行，这里是发力的点。翻折带出的弧度不能拖太长，过渡段（老师称「小脖子」）要短，一过渡完就马上去把笔锋铺开。",
          "measurable": {
            "direction": "右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "压笔后顶住，力度先聚后放",
            "speed_hint": "过渡段短促，不拖长",
            "stop_points": [
              "翻折顶住回来时的顿点（发力点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:19:30",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:斜捺:整笔",
          "segment": "整笔",
          "action": "捺画整体：勒紧起笔、蓄势下行、渐行渐压、聚锋出尖、捺脚饱满挺健；写完要能感觉到一点痛快的意思。单写时逆入压下来再翻折，与隶书「锤头波」一类笔法相似，动作方向基本相同，只是通过粗细和力度变化形成不同形态。",
          "measurable": {
            "direction": "右下→右",
            "entry": "勒紧起笔／单写时逆入压下再翻折",
            "exit": "沿上侧面出锋出尖",
            "pressure_profile": "由轻到重→最重处驻笔→提锋收尖",
            "speed_hint": "起笔慢，行笔渐行渐压，收笔提锋",
            "stop_points": [
              "最重处的驻笔停顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:14:36",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:斜撇",
      "name": "斜撇",
      "font": "楷书",
      "category": "撇画类",
      "tags": [
        "A档-基础笔画",
        "撇画",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 2,
        "title": "第2节 基础笔画撇捺",
        "t": "01:32:12",
        "evidence_count": 10
      },
      "actions": [
        {
          "id": "stroke:斜撇:起笔",
          "segment": "起笔",
          "action": "逆入，落下顿笔，笔尖呈点状（讲义原文作「逆入顿作点状」）；落下之后调锋略向上提，续住笔势，再准备向左下方行笔。",
          "measurable": {
            "direction": "向左下",
            "entry": "露锋直入 或 藏锋压下",
            "exit": null,
            "pressure_profile": "顿笔处最重，随后提起",
            "speed_hint": null,
            "stop_points": [
              "逆入落下后的顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:32:39",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:斜撇:行笔",
          "segment": "行笔",
          "action": "笔锋向左下方行走，行笔要「塞进」（讲义原文作「行笔塞进」），靠纸面与毛笔之间的摩擦力行进，不要在纸面上漂浮着划过去；笔画保持挺直，不要带弧度。",
          "measurable": {
            "direction": "右上→左下",
            "entry": null,
            "exit": null,
            "pressure_profile": "粗→细，渐提",
            "speed_hint": "挺直靠速度控制，不是甩出去",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:33:32",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:斜撇:收笔",
          "segment": "收笔",
          "action": "收笔时把笔锋勒紧、提着去写，向左下慢慢送到尖，送到位后快速出锋；速度由快到慢，力度由重到轻。不要急匆匆地提起来，要在笔尖位置有意识地停一下把笔送到位，但停不能太久。",
          "measurable": {
            "direction": "左下",
            "exit": "勒紧撇，向左下快速出锋",
            "pressure_profile": "重→轻",
            "speed_hint": "由快到慢；收笔送到尖",
            "stop_points": [
              "收笔勒紧、笔尖送到位时的停点（不可久停）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:33:56",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:斜撇:转折",
          "segment": "转折",
          "action": "落下之后调锋略向上提，是在上侧面略微向上拱、续住笔势的提示；提起时力度不能松，笔锋要挺立在纸面上。",
          "measurable": {
            "direction": "上提续势后转左下",
            "entry": "调锋略向上提（讲义原文作「调峰略向上提以叙事」）",
            "exit": null,
            "pressure_profile": "提起后仍保持压力，笔锋不松",
            "speed_hint": null,
            "stop_points": [
              "调锋上提的起势点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "00:05:20",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:斜撇:整笔",
          "segment": "整笔",
          "action": "短撇与斜撇只是一个长度的对比。斜撇一般比较直挺；像「少」字里那种较厚重的撇，写起来要敢重按、练压笔，但不能写得太过臃肿。",
          "measurable": {
            "direction": "左下",
            "entry": "逆入后落下顿笔",
            "exit": "勒紧撇送出尖",
            "pressure_profile": "由重到轻；厚重的一类需重按压笔",
            "speed_hint": "重处慢、轻处快",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 基础笔画撇捺",
            "t": "01:32:12",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:悬针竖",
      "name": "悬针竖",
      "font": "楷书",
      "category": "竖画类",
      "tags": [
        "悬针竖",
        "颜勤礼碑",
        "B档"
      ],
      "certainty": "inferred",
      "source": {
        "part": 1,
        "title": "第1节 基础笔画横竖",
        "t": "00:57:19",
        "evidence_count": 7
      },
      "actions": [
        {
          "id": "stroke:悬针竖:起笔",
          "segment": "起笔",
          "action": "逆锋往左上方起笔，笔尖落下后稍聚笔锋，再压下铺开笔锋开始向下行笔",
          "measurable": {
            "direction": "左上方逆入→正下方",
            "entry": "逆锋入笔后落下、聚锋",
            "exit": null,
            "pressure_profile": "落下聚锋后压实铺开",
            "speed_hint": null,
            "stop_points": [
              "落下聚锋后再压下的顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:57:52",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:悬针竖:行笔",
          "segment": "行笔",
          "action": "向下果断行笔、铺开笔锋，笔画厚重；中段粗细变化不大，保持较均匀",
          "measurable": {
            "direction": "上→下",
            "entry": null,
            "exit": null,
            "pressure_profile": "压住→渐提",
            "speed_hint": "下行果断",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:59:29",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:悬针竖:收笔",
          "segment": "收笔",
          "action": "尾部约 1/3 处开始渐渐提笔，把笔锋送到位出锋；尖端不能太细，否则线条会显得瘦弱",
          "measurable": {
            "direction": "向下→出锋",
            "entry": null,
            "exit": "渐提笔出锋送到位",
            "pressure_profile": "厚重→尾部渐细",
            "speed_hint": null,
            "stop_points": [
              "下部 1/3 处的提笔起点",
              "出锋的尖端（送到位处）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:59:43",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:右点",
      "name": "右点",
      "font": "楷书",
      "category": "点画类",
      "tags": [
        "基础笔画"
      ],
      "certainty": "inferred",
      "source": {
        "part": 4,
        "title": "第4节 基础笔画点提",
        "t": "00:08:58",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:右点:起笔",
          "segment": "起笔",
          "action": "右点（斜落的点）严格意义上落笔是侧锋，立起来之后往下要顺势走中锋；落笔处抢住笔尖往右压下去。",
          "measurable": {
            "direction": "左上→右下",
            "entry": "侧锋落笔，抢住笔尖压下去",
            "exit": null,
            "pressure_profile": "落笔即压→立柱",
            "speed_hint": "果断",
            "stop_points": [
              "落笔处（抢住笔尖的顿点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:08:58",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:右点:行笔",
          "segment": "行笔",
          "action": "右点行笔要利用铺开的笔锋面往下压，往右下方走中锋线；点画用的是笔头那一段，不铺厚就容易写成侧锋。",
          "measurable": {
            "direction": "左上→右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "压开笔锋面→中锋行笔",
            "speed_hint": "落笔稍快，行笔不犹豫",
            "stop_points": [
              "行笔中段（笔锋面压实处）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:09:43",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:右点:收笔",
          "segment": "收笔",
          "action": "右点从中段最粗处提起出尖，顺过去稍微一藏锋再落下去；出尖不要太尖太细，要保留饱满。",
          "measurable": {
            "direction": "右下→右下收尖",
            "entry": null,
            "exit": "从最粗处提锋出尖，顺势藏锋",
            "pressure_profile": "中段最粗→渐细出尖",
            "speed_hint": "送出，不甩",
            "stop_points": [
              "出尖处（中段最粗点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 4,
            "title": "第4节 基础笔画点提",
            "t": "00:09:20",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:长横",
      "name": "长横",
      "font": "楷书",
      "category": "横画类",
      "tags": [
        "长横",
        "颜勤礼碑"
      ],
      "certainty": "inferred",
      "source": {
        "part": 1,
        "title": "第1节 基础笔画横竖",
        "t": "00:49:42",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:长横:起笔",
          "segment": "起笔",
          "action": "逆锋入笔、笔尖落下后立住调锋，再向右开始行笔；起笔不必特别粗",
          "measurable": {
            "direction": "左→右",
            "entry": "逆锋入笔后落下、立住调锋",
            "exit": null,
            "pressure_profile": "起笔适中，不必特别重",
            "speed_hint": null,
            "stop_points": [
              "立住笔锋开始行笔处"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:49:48",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:长横:行笔",
          "segment": "行笔",
          "action": "中段明显变细、挺起笔锋往前行；中段的过渡段必须靠速度提起来才写得细，慢则发抖",
          "measurable": {
            "direction": "左→右，中段略上挺",
            "entry": null,
            "exit": null,
            "pressure_profile": "起笔铺开→中段提笔变细",
            "speed_hint": "中段过渡要快一点；速度慢会抖",
            "stop_points": [
              "提笔变细的起点（中段）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:49:44",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:长横:收笔",
          "segment": "收笔",
          "action": "收笔时先往右上方轻提，再往右下压（下顿），顿住后回锋；提笔方向必须是右上，向上直提容易出现缺口",
          "measurable": {
            "direction": "向右上方提笔→向右下顿",
            "entry": null,
            "exit": "右上方轻提→右下顿→回锋",
            "pressure_profile": "行笔细→轻提→下顿",
            "speed_hint": "不要着急提",
            "stop_points": [
              "右下顿笔处（顿住后回锋的驻留点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:50:06",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:长横:转折",
          "segment": "转折",
          "action": "收笔只是换了一个方向、拐一个圈完成；下顿处要先立住（顶住、稍停）再回锋",
          "measurable": {
            "direction": "轻提后向下拐圈",
            "entry": null,
            "exit": "下顿立住后回锋",
            "pressure_profile": "下顿加重后回锋",
            "speed_hint": null,
            "stop_points": [
              "下顿立住的驻笔点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 1,
            "title": "第1节 基础笔画横竖",
            "t": "00:50:22",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:点",
      "name": "点",
      "font": "隶书",
      "category": "点画类",
      "tags": [
        "用笔",
        "隶书",
        "点画",
        "【解析】"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 13,
        "title": "第5节 情有独钟",
        "t": "00:30:07",
        "evidence_count": 12
      },
      "actions": [
        {
          "id": "stroke:点:起笔",
          "segment": "起笔",
          "action": "逆锋起笔。点画很小，整个动作全部用笔尖完成——写法和竖画的起笔一样，只是方向不同。",
          "measurable": {
            "entry": "逆锋起笔后圆转",
            "direction": "上→下",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:30:07",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:点:行笔",
          "segment": "行笔",
          "action": "用笔尖写，按下来；动作一定要做到位，不能把点压成一团。",
          "measurable": {
            "pressure_profile": "轻→重（慢慢压笔）",
            "direction": "向下",
            "entry": null,
            "exit": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:51:46",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:点:收笔",
          "segment": "收笔",
          "action": "把笔慢慢提起来。",
          "measurable": {
            "exit": "用笔尖稍微向上回个笔",
            "direction": null,
            "entry": null,
            "pressure_profile": null,
            "speed_hint": "慢慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:52:19",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:点:整笔",
          "segment": "整笔",
          "action": "整笔形态：点画很小，形态像小犄角/三角形；爱字草字头上的两个小点不要写太大。",
          "measurable": {
            "direction": "点画方向略带斜势"
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:58:58",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:点画",
      "name": "点画",
      "font": "隶书",
      "category": "点画类",
      "tags": [
        "隶书",
        "点画",
        "本节新授",
        "点画类"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:02:34",
        "evidence_count": 11
      },
      "actions": [
        {
          "id": "stroke:点画:起笔",
          "segment": "起笔",
          "action": "逆入起笔。",
          "measurable": {
            "direction": "左下 → 右上",
            "entry": "向左下轻压毛笔，笔尖圆转",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "动作要慢一点不要着急",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:46",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:点画:行笔",
          "segment": "行笔",
          "action": "用笔尖把点画送出去：底下是圆润的，上面出尖（像倒过来的小瓜子、像小冰淇淋）。",
          "measurable": {
            "direction": "右上",
            "entry": null,
            "exit": null,
            "pressure_profile": "下圆上尖",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:02:10",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:点画:收笔",
          "segment": "收笔",
          "action": "平出收笔，由重到轻。",
          "measurable": {
            "direction": "方向随字变化",
            "entry": null,
            "exit": "平出",
            "pressure_profile": "由重→轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:46",
            "evidence_count": 1
          }
        },
        {
          "id": "stroke:点画:整笔",
          "segment": "整笔",
          "action": "《曹全碑》的点画多变化、笔笔尽见而不含糊；用笔逆入平出、由重到轻；方向的变化非常丰富。",
          "measurable": {
            "direction": "右上／向下／向左（三个点各不相同）",
            "entry": "逆锋起笔，笔尖圆转",
            "exit": "向上出尖",
            "pressure_profile": "下圆上尖",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:41",
            "evidence_count": 10
          }
        }
      ]
    },
    {
      "id": "stroke:短横",
      "name": "短横",
      "font": "隶书",
      "category": "隶书横画类",
      "tags": [
        "隶书",
        "藏锋起笔",
        "短横",
        "圆头起笔",
        "【解析】",
        "横画",
        "字头"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 9,
        "title": "第1节 蚕头雁尾，弓弩直立",
        "t": "00:13:31",
        "evidence_count": 14
      },
      "actions": [
        {
          "id": "stroke:短横:起笔",
          "segment": "起笔",
          "action": "逆锋（藏锋）起笔：先用笔尖向左轻轻压一下毛笔，再圆转过来作调锋动作，把笔毛调到笔画正中间，然后中锋向右行笔。",
          "measurable": {
            "direction": "左→右",
            "entry": "在竖画内逆锋起笔（藏锋），起笔不犹豫",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "起笔不犹豫，一气接行笔",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:13:12",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:短横:行笔",
          "segment": "行笔",
          "action": "圆转过来后中锋向右慢慢书写，动作一定要慢，不能快。",
          "measurable": {
            "direction": "左→右，略下弧",
            "entry": null,
            "exit": null,
            "pressure_profile": "粗→细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:17:19",
            "evidence_count": 14
          }
        },
        {
          "id": "stroke:短横:收笔",
          "segment": "收笔",
          "action": "收尾时慢慢把笔尖提起来，自然形成一个小尖；这个尖不尖锐，是毛笔自然抬起来的形态。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "封口横画由粗到细收住，只略过竖画一点点",
            "pressure_profile": "粗→细",
            "speed_hint": null,
            "stop_points": [
              "封口交接处（不能出现十字交叉口）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:17:36",
            "evidence_count": 11
          }
        }
      ]
    },
    {
      "id": "stroke:短撇",
      "name": "短撇（小撇画）",
      "font": "隶书",
      "category": "撇画类",
      "tags": [
        "【解析】",
        "撇画",
        "隶书"
      ],
      "certainty": "corrected",
      "source": {
        "part": 14,
        "title": "第6节 头头是道",
        "t": "00:51:34",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:短撇:起笔",
          "segment": "起笔",
          "action": "小撇画（小竖撇）逆锋起笔后向左撇出去；这种小撇比同字里的竖画要小很多。",
          "measurable": {
            "direction": "向左撇出",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "corrected",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "00:51:34",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:短撇:行笔",
          "segment": "行笔",
          "action": "短撇由粗到细，带一个小弧度；这类小撇画很短（成字左边的撇是最短的一笔）。",
          "measurable": {
            "direction": "向左",
            "entry": null,
            "exit": null,
            "pressure_profile": "由粗到细",
            "speed_hint": "带小弧度",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "01:02:29",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:短撇:收笔",
          "segment": "收笔",
          "action": "这种小撇是尖收笔（不是圆收笔）：慢慢把笔尖送出去；尖收时一定要控制住毛笔，不能让笔飞出去。",
          "measurable": {
            "direction": "笔尖向左送出",
            "entry": null,
            "exit": "尖收笔（送到位、停住，不要飞出去）",
            "pressure_profile": "由粗到细至尖",
            "speed_hint": "送到位即停",
            "stop_points": []
          },
          "certainty": "corrected",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "01:00:58",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:短竖画",
      "name": "短竖画（向内收）",
      "font": "隶书",
      "category": "隶书竖画类",
      "tags": [
        "隶书",
        "向内收"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 9,
        "title": "第1节 蚕头雁尾，弓弩直立",
        "t": "01:03:39",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:短竖画:起笔",
          "segment": "起笔",
          "action": "字内向内收的小竖画起笔：先向上顶一下毛笔，再用笔尖把笔圆转过来，调整笔锋后按向内收的方向写；圆转一律用笔尖完成，不能把笔毛按得太深。",
          "measurable": {
            "direction": "先向上 → 圆转 → 向内收",
            "entry": "逆锋轻入，向上顶后用笔尖圆转",
            "exit": null,
            "pressure_profile": "轻 → 渐重（不可按得太深）",
            "speed_hint": "慢、温柔",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:59:52",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:短竖画:行笔",
          "segment": "行笔",
          "action": "沿内收方向行笔，由粗到细（由重到轻），不是竖直竖直的竖。",
          "measurable": {
            "direction": "向内收",
            "entry": null,
            "exit": null,
            "pressure_profile": "由粗到细（由重到轻）",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "01:02:15",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:短竖画:收笔",
          "segment": "收笔",
          "action": "慢慢把毛笔提起来，自然变细、出尖，笔画末端由重到轻。",
          "measurable": {
            "direction": "向内收",
            "entry": null,
            "exit": "自然提笔变细出尖",
            "pressure_profile": "渐提渐轻",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "01:02:36",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:横",
      "name": "横",
      "font": "隶书",
      "category": "横画类",
      "tags": [
        "楷书",
        "多宝塔",
        "横画",
        "隶书",
        "横画类",
        "短横",
        "复习",
        "用笔",
        "燕尾"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 20,
        "title": "第20节-中国书法基础知识-基本章法",
        "t": "00:01:53",
        "evidence_count": 16
      },
      "actions": [
        {
          "id": "stroke:横:起笔",
          "segment": "起笔",
          "action": "起笔时毛笔尖锋向左入笔，向右下切笔后调锋（字幕原词作「调整封面」），随即斜向右上方行笔。",
          "measurable": {
            "direction": "左→右",
            "entry": "逆锋起笔（笔尖左压后圆转调锋）",
            "exit": null,
            "pressure_profile": "起笔轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 20,
            "title": "第20节-中国书法基础知识-基本章法",
            "t": "00:02:03",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:横:行笔",
          "segment": "行笔",
          "action": "行笔斜向右上方；行笔中段保持毛笔中锋。",
          "measurable": {
            "direction": "左→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "中间渐细，末段渐顿变重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 20,
            "title": "第20节-中国书法基础知识-基本章法",
            "t": "00:02:07",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:横:收笔",
          "segment": "收笔",
          "action": "收笔处笔尖轻提随之向右下，按笔（该词未确证，疑为「顿笔」）之后向左上方顶住笔尖逆收，最后将笔尖调至中锋状态抬笔即可。",
          "measurable": {
            "direction": "轻提后向右下 → 向左上方顶住笔尖逆收",
            "entry": null,
            "exit": "轻压后从下边缘线把笔抽出/提起，非顿笔",
            "pressure_profile": "轻压，只有一点点变粗",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 20,
            "title": "第20节-中国书法基础知识-基本章法",
            "t": "00:02:07",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:横:整笔",
          "segment": "整笔",
          "action": "以楷书《多宝塔》中的横画为例，演示笔法和中锋的具体运用（起笔、行笔、收笔三段见本节其余条目）。",
          "measurable": {
            "direction": "横向",
            "entry": null,
            "exit": "轻燕尾：燕尾处不厚重但要写得出",
            "pressure_profile": "燕尾处轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 20,
            "title": "第20节-中国书法基础知识-基本章法",
            "t": "00:01:53",
            "evidence_count": 9
          }
        }
      ]
    },
    {
      "id": "stroke:横画",
      "name": "横画（篆书）",
      "font": "隶书",
      "category": "横画类",
      "tags": [
        "【解析】",
        "篆书",
        "横画",
        "横画类",
        "笔画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 1,
        "title": "第1节 千里之行始于足下",
        "t": "00:15:23",
        "evidence_count": 18
      },
      "actions": [
        {
          "id": "stroke:横画:起笔",
          "segment": "起笔",
          "action": "横画起笔：一上一下把笔毫错开铺开（笔尖由收拢到完全打开），落笔时先往上带一个小动作、再往下错开摊平，写成向上拱的小弧。起笔形状随写随定——圆头、方头都可以，但不要刻意做成圆的。",
          "measurable": {
            "direction": "自左向右",
            "entry": "切入顿住后把笔锋打开（打开后不再变）",
            "pressure_profile": "起笔顿住（稍重）",
            "stop_points": [
              "错笔打开后的落笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 千里之行始于足下",
            "t": "00:15:23",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:横画:行笔",
          "segment": "行笔",
          "action": "横画行笔：笔毫保持完全铺开、不再保持中锋；行笔过程中保持粗细均匀，不要越写越粗或越写越细；笔画整体微微向上拱成小弧线，不能写成完全平直的一条。",
          "measurable": {
            "direction": "横向，自左向右",
            "pressure_profile": "中间略有粗细变化",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 千里之行始于足下",
            "t": "00:19:02",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:横画:收笔",
          "segment": "收笔",
          "action": "横画收笔：三种方式——沿毛笔上边缘线收笔、沿下边缘线收笔、沿中间收笔（收出来类似悬针竖的感觉）；案例里也常见直接急停、停住不收。收出来的形状不必强求方圆，出来什么形状算什么形状，也不要刻意写成圆的。",
          "measurable": {
            "exit": "燕尾收笔，或随意收笔、枯笔直接抬笔",
            "pressure_profile": "收笔时逐渐抬笔、笔尖离纸",
            "stop_points": [
              "收笔处的上／下边缘线或中间线"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 千里之行始于足下",
            "t": "00:16:24",
            "evidence_count": 14
          }
        }
      ]
    },
    {
      "id": "stroke:横画（宝盖头）",
      "name": "横画（宝盖头）",
      "font": "隶书",
      "category": "隶书横画类",
      "tags": [
        "【解析】",
        "隶书",
        "横画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 16,
        "title": "第8节 头头是道3",
        "t": "00:11:31",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:横画（宝盖头）:行笔",
          "segment": "行笔",
          "action": "宝盖头的横画比较平直，行笔中有一个由粗到细的细节变化。",
          "measurable": {
            "direction": "左 → 右",
            "entry": null,
            "exit": null,
            "pressure_profile": "由粗到细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:11:31",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横画（宝盖头）:整笔",
          "segment": "整笔",
          "action": "守字宝盖头的横画更粗重、方向向左斜过来，仍保持平直；整个宝盖头要写得非常宽，像大檐宽檐帽那样盖住下面，不能缩成棒球帽。",
          "measurable": {
            "direction": "向左斜过来",
            "entry": null,
            "exit": null,
            "pressure_profile": "更粗重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:37:05",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:横画（雨字头）",
      "name": "横画（雨字头）",
      "font": "隶书",
      "category": null,
      "tags": [],
      "certainty": "verbatim",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:横画（雨字头）:起笔",
          "segment": "起笔",
          "action": "雨字头第一个横画逆锋起笔、由粗到细；下面那个横画的起笔与第一个横画的起笔基本在一条竖线上。",
          "measurable": {
            "direction": "左 → 右",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": "由粗到细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:45:59",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横画（雨字头）:行笔",
          "segment": "行笔",
          "action": "雨字头下面那个横画比较平直；横竖对比要写出来，折过来的竖画稍微向内收、带一点小弧度。",
          "measurable": {
            "direction": "左 → 右",
            "entry": null,
            "exit": null,
            "pressure_profile": "与竖画形成粗细对比",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:54:30",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:横折",
      "name": "横折",
      "font": "隶书",
      "category": "隶书折画类",
      "tags": [
        "隶书",
        "折画",
        "折画类",
        "横折",
        "复习",
        "用笔",
        "【解析】",
        "字头"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 11,
        "title": "第3节 宛转矩折",
        "t": "00:00:35",
        "evidence_count": 20
      },
      "actions": [
        {
          "id": "stroke:横折:起笔",
          "segment": "起笔",
          "action": "横折＝短横＋短竖。起笔按短横来：先向左逆锋起笔，再向右写短横（短横本身由粗到细），到横画收笔处把笔提起来。",
          "measurable": {
            "direction": "向右下带弧度",
            "entry": "逆锋起笔（短横同法）",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "00:11:08",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:横折:行笔",
          "segment": "行笔",
          "action": "折下来写的竖画要向内收，上边稍微留一点圆润的感觉。",
          "measurable": {
            "direction": "左→右",
            "entry": null,
            "exit": null,
            "pressure_profile": "粗→细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:16:36",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横折:收笔",
          "segment": "收笔",
          "action": "折过来的竖画向内收；横竖搭得非常紧实时，上面会露出一个小圆脑袋；也可以上下断开写（短横收笔把笔提起来，在不远处逆锋起笔写竖）。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "圆润转折，不作楷书式顿笔直写",
            "pressure_profile": null,
            "speed_hint": "圆润过渡",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:03:44",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:横折:转折",
          "segment": "转折",
          "action": "在横画收笔往下的那个位置向上逆锋起笔，再圆转下来向内收写小竖画；接在刚碰到横画的位置就转下来，整体外轮廓才是圆的。",
          "measurable": {
            "direction": "向下（转折后向内收）",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "与横画相接的转折处（要圆润）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "00:11:34",
            "evidence_count": 22
          }
        },
        {
          "id": "stroke:横折:整笔",
          "segment": "整笔",
          "action": "横折的外轮廓是圆润的（隶书），跟楷书横折「直接右上—切笔—向下」的方整形形成明显对比；短横部分略微向上弧，由粗到细，折过来的小竖画向内收。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": "横粗竖细对比明显",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "00:10:57",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:横折断开",
      "name": "横折·断开",
      "font": "隶书",
      "category": "隶书折画类",
      "tags": [
        "隶书",
        "折画",
        "断开"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 11,
        "title": "第3节 宛转矩折",
        "t": "00:01:00",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:横折断开:转折",
          "segment": "转折",
          "action": "短横写完收笔后，在收笔方向上往外空出一点点小距离，再在这个位置向上逆锋起笔、向下写短竖——上下不相连，这叫断开；白字的上端小圆头比中字小得多。",
          "measurable": {
            "direction": "短横向右收笔 → 往外空一点 → 向上逆锋起笔 → 向下",
            "entry": "在空出一点距离的位置向上逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "位置不要太高",
            "stop_points": [
              "短横收笔外侧的空档位置（老师用红点标出）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "00:43:32",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:横折实接",
      "name": "横折·实接",
      "font": "隶书",
      "category": "隶书折画类",
      "tags": [
        "隶书",
        "折画",
        "实接"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 11,
        "title": "第3节 宛转矩折",
        "t": "00:00:54",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:横折实接:转折",
          "segment": "转折",
          "action": "横画收笔时把毛笔提起来，在收笔的位置向上逆锋起笔、旋转向下写竖画：两笔完全挨在一起，横画上面还会露出一个小圆脑袋——这种搭接叫「实接」。",
          "measurable": {
            "direction": "向上逆锋起笔 → 旋转 → 向下",
            "entry": "在短横收笔的位置向上逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "动作要肯定，不能停顿",
            "stop_points": [
              "短横收笔处（露出小圆脑袋的位置）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "00:28:19",
            "evidence_count": 10
          }
        }
      ]
    },
    {
      "id": "stroke:横折虚接",
      "name": "横折·虚接",
      "font": "隶书",
      "category": "隶书折画类",
      "tags": [
        "隶书",
        "折画",
        "虚接"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 11,
        "title": "第3节 宛转矩折",
        "t": "00:00:43",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:横折虚接:转折",
          "segment": "转折",
          "action": "横画收笔后向上逆锋起笔、圆转下来向内收写小竖画，但两笔只是轻轻碰上（中间有极小的空隙），不挨实——这种搭接叫「虚接」。",
          "measurable": {
            "direction": "向上起笔 → 圆转 → 向下向内收",
            "entry": "逆锋向上起笔",
            "exit": "向内收的小竖画",
            "pressure_profile": null,
            "speed_hint": "轻轻碰上，动作要肯定",
            "stop_points": [
              "横画收笔处（轻轻碰上的位置）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "00:13:44",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:弧线",
      "name": "弧线",
      "font": "隶书",
      "category": "弧线类（括号形笔画）",
      "tags": [
        "【解析】",
        "篆书",
        "笔画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 4,
        "title": "第4节 积水成渊蛟龙生焉",
        "t": "01:11:17",
        "evidence_count": 7
      },
      "actions": [
        {
          "id": "stroke:弧线:起笔",
          "segment": "起笔",
          "action": "弧线起笔：切入顿住，随后走一条长弧线；写子字下部的弧线时要先往下、再往上顶一下，斜着走长弧线。",
          "measurable": {
            "direction": "由下往上",
            "entry": "停顿后抬笔成弧",
            "stop_points": [
              "起笔停顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 积水成渊蛟龙生焉",
            "t": "01:21:44",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:弧线:行笔",
          "segment": "行笔",
          "action": "弧线行笔：一条弧线走到底，可以一笔完成，也可以分两笔写（案例上有一处是后补的接口）；弧度可大可小。",
          "measurable": {
            "direction": "由下往上成弧",
            "pressure_profile": "基本中锋，可有轻微粗细变动"
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 积水成渊蛟龙生焉",
            "t": "01:11:50",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:弧线:收笔",
          "segment": "收笔",
          "action": "弧线收笔：多沿上边缘线收笔；起笔与收笔的形状刻意做出差别，切不可写成完全对称。",
          "measurable": {
            "exit": "沿上边缘线收笔"
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 积水成渊蛟龙生焉",
            "t": "01:17:16",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:两头粗横画",
      "name": "两头粗中间细横画",
      "font": "隶书",
      "category": "横画类",
      "tags": [
        "【解析】",
        "横画",
        "隶书"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 14,
        "title": "第6节 头头是道",
        "t": "00:31:08",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:两头粗横画:起笔",
          "segment": "起笔",
          "action": "方头起笔：先向左轻压毛笔，再竖过来压一下把这个形态描出来（竖压不能太大，大了就回不来），然后马上调整笔锋、把笔锋调正，继续向右写。",
          "measurable": {
            "direction": "向左轻压 → 向下竖压 → 调正笔锋 → 向右",
            "entry": "方头：向左轻压后竖过来压一下",
            "exit": null,
            "pressure_profile": "起笔压一下（轻），不能压太大",
            "speed_hint": "动作要快而准",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "00:38:10",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:两头粗横画:行笔",
          "segment": "行笔",
          "action": "中段慢慢变细，后段边写边压笔让线条变粗，形成两头粗、中间细的横画（子字中间这一横，老师称作「蝴蝶结」）。",
          "measurable": {
            "direction": "向右",
            "entry": null,
            "exit": null,
            "pressure_profile": "中段细 → 后段压笔变粗（两头粗中间细）",
            "speed_hint": "中段慢、后段压笔再收",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "00:38:58",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:两头粗横画:收笔",
          "segment": "收笔",
          "action": "收笔时把笔从下边（笔画底部）抽出来，笔尖从下面出来，自然形成两头粗、中间细的形态。",
          "measurable": {
            "direction": "从笔画下方向外抽出",
            "entry": null,
            "exit": "从下边把笔抽出来（笔尖从底下出来）",
            "pressure_profile": "压笔后逐渐抽离",
            "speed_hint": "慢慢抽离",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "00:39:09",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:捺",
      "name": "捺",
      "font": "隶书",
      "category": "捺画类",
      "tags": [
        "用笔",
        "隶书",
        "捺画",
        "【解析】",
        "字头"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 13,
        "title": "第5节 情有独钟",
        "t": "00:32:16",
        "evidence_count": 13
      },
      "actions": [
        {
          "id": "stroke:捺:起笔",
          "segment": "起笔",
          "action": "逆锋起笔。令字人字头的捺画起笔比撇画起笔低一点点，露一个小头。",
          "measurable": {
            "direction": "左上→右下",
            "entry": "在竖画右侧一点起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:32:16",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:捺:行笔",
          "segment": "行笔",
          "action": "向右行笔，慢慢顿笔；捺画比较直，撇高捺低有一个高度差；撇捺画之间粗细变化不明显。",
          "measurable": {
            "direction": "右下",
            "pressure_profile": "细→粗",
            "entry": null,
            "exit": null,
            "speed_hint": "慢慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:35:52",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:捺:收笔",
          "segment": "收笔",
          "action": "顿笔后慢慢出锋。",
          "measurable": {
            "exit": "慢慢顿笔出锋",
            "direction": null,
            "entry": null,
            "pressure_profile": "细→粗后出锋",
            "speed_hint": "慢慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:23:43",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:捺:整笔",
          "segment": "整笔",
          "action": "整笔形态：捺画比较直、比较长，撇高捺低有高度差，撇捺粗细基本一致。",
          "measurable": {
            "direction": "向右下",
            "pressure_profile": "基本一样粗细"
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "01:14:41",
            "evidence_count": 2
          }
        }
      ]
    },
    {
      "id": "stroke:捺画",
      "name": "捺画",
      "font": "隶书",
      "category": "捺画类",
      "tags": [
        "隶书",
        "一波三折",
        "捺画",
        "本节新授",
        "【解析】"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:01:44",
        "evidence_count": 20
      },
      "actions": [
        {
          "id": "stroke:捺画:起笔",
          "segment": "起笔",
          "action": "上端藏锋起笔。",
          "measurable": {
            "direction": "逆锋起笔 → 向下向右",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:00",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:捺画:行笔",
          "segment": "行笔",
          "action": "从左上方向右下方书写，是斜势笔画（斜捺与平捺两种）；有倾斜角度和波折的小弧度、弯曲变化；注意由轻到重的变化。",
          "measurable": {
            "direction": "向右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "边走边顿；是全字最粗的部分",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:01:48",
            "evidence_count": 23
          }
        },
        {
          "id": "stroke:捺画:收笔",
          "segment": "收笔",
          "action": "末端向右上方提笔出锋完成。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "顿笔后慢慢出锋",
            "pressure_profile": null,
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:03",
            "evidence_count": 15
          }
        }
      ]
    },
    {
      "id": "stroke:撇",
      "name": "撇",
      "font": "隶书",
      "category": "隶书撇画类",
      "tags": [
        "隶书",
        "撇画类",
        "小撇",
        "用笔",
        "燕尾",
        "撇画",
        "【解析】",
        "字头"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 12,
        "title": "第4节 群英荟萃",
        "t": "00:38:35",
        "evidence_count": 16
      },
      "actions": [
        {
          "id": "stroke:撇:起笔",
          "segment": "起笔",
          "action": "小撇不用藏锋，直接向下漏锋入笔。",
          "measurable": {
            "direction": "右→左下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "横画下方、竖画左侧一点（撇）",
              "横画下方、竖画右侧一点（捺）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:38:35",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:撇:行笔",
          "segment": "行笔",
          "action": "向左慢慢推出去，撇画非常小；不能超过该字第二个横画。",
          "measurable": {
            "direction": "左下（保持较平）",
            "entry": null,
            "exit": null,
            "pressure_profile": "细→粗（逐渐加力）",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:37:54",
            "evidence_count": 15
          }
        },
        {
          "id": "stroke:撇:收笔",
          "segment": "收笔",
          "action": "用笔尖向上回一下笔。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "用笔尖向上回个笔（回锋），收笔圆或斜过来",
            "pressure_profile": "保持变粗",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:38:43",
            "evidence_count": 14
          }
        },
        {
          "id": "stroke:撇:整笔",
          "segment": "整笔",
          "action": "整笔形态：收笔处像「反过来的一个燕尾」；撇画要带弧度、比捺画短；不能写成一个比较立的状态。",
          "measurable": {
            "exit": "反过来的燕尾",
            "pressure_profile": "由细到粗"
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:15:52",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:撇画",
      "name": "撇画",
      "font": "隶书",
      "category": "撇画类",
      "tags": [
        "隶书",
        "撇画",
        "本节重点"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:01:16",
        "evidence_count": 13
      },
      "actions": [
        {
          "id": "stroke:撇画:起笔",
          "segment": "起笔",
          "action": "藏锋逆锋切入。",
          "measurable": {
            "direction": "向上逆锋起笔 → 圆转 → 向下或向右下撇",
            "entry": "逆锋轻入",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:01:21",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:撇画:行笔",
          "segment": "行笔",
          "action": "先向上再向左下行笔；写的时候注意转折处的轻重变化。",
          "measurable": {
            "direction": "向下 → 过横中线 → 向左撇出",
            "entry": null,
            "exit": null,
            "pressure_profile": "拐弯处轻压毛笔",
            "speed_hint": null,
            "stop_points": [
              "横中线位置（撇出点）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:01:21",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:撇画:收笔",
          "segment": "收笔",
          "action": "笔画末端笔锋稍钝，向上收笔；收笔可出锋，也可回锋。",
          "measurable": {
            "direction": "向左撇出后向上收笔",
            "entry": null,
            "exit": "向上收笔（回笔）",
            "pressure_profile": "渐提",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:01:21",
            "evidence_count": 9
          }
        }
      ]
    },
    {
      "id": "stroke:撇折",
      "name": "撇折",
      "font": "隶书",
      "category": "隶书折画类",
      "tags": [
        "【解析】",
        "隶书",
        "折画",
        "楷隶之别"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 16,
        "title": "第8节 头头是道3",
        "t": "00:56:38",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:撇折:起笔",
          "segment": "起笔",
          "action": "云字下部的撇折：在上面这个横画大约 1/3 处逆锋起笔，向左下写撇，注意不要写得太长。",
          "measurable": {
            "direction": "向左下",
            "entry": "在横画 1/3 处逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:56:38",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:撇折:行笔",
          "segment": "行笔",
          "action": "撇的部分比较直，但有粗细变化；折回来的那一段有一点弧度，而且是右上斜上去的。",
          "measurable": {
            "direction": "撇：向左下；折回：向右上",
            "entry": null,
            "exit": null,
            "pressure_profile": "撇有粗细变化",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:59:55",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:撇折:转折",
          "segment": "转折",
          "action": "撇折折回来时要圆转过来、向右上斜上去并带一点弧度；绝不能直接顿笔就折上去，那是楷书写法，隶书要圆头。",
          "measurable": {
            "direction": "向左下 → 圆转 → 向右上",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:56:57",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:平撇",
      "name": "平撇",
      "font": "隶书",
      "category": "隶书撇画类",
      "tags": [
        "隶书",
        "撇画",
        "文字"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 10,
        "title": "第2节 点石成金，张弛有度",
        "t": "00:49:07",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:平撇:起笔",
          "segment": "起笔",
          "action": "文字的平撇起笔：逆锋起笔后作调锋（把笔尖提起来但不离开纸面），直接向左撇出去——没有月字小竖撇上面那一段竖，是比较平向的撇。",
          "measurable": {
            "direction": "调锋后直接向左平出",
            "entry": "逆锋起笔 + 调锋",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:49:12",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:平撇:行笔",
          "segment": "行笔",
          "action": "平撇行笔：向左平推出去，把毛笔送到位置，撇画是比较平的一个，不能撇成立着的形状。",
          "measurable": {
            "direction": "向左平推",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:52:25",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:平撇:收笔",
          "segment": "收笔",
          "action": "平撇收笔：撇出去以后向上用笔尖回一下笔，与后一个笔画形成笔势上的承接。",
          "measurable": {
            "direction": "向上回笔",
            "entry": null,
            "exit": "用笔尖向上回笔",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:52:25",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:竖",
      "name": "竖",
      "font": "隶书",
      "category": "竖画类",
      "tags": [
        "用笔",
        "隶书",
        "向内收",
        "竖画",
        "【解析】",
        "字头"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 13,
        "title": "第5节 情有独钟",
        "t": "00:11:41",
        "evidence_count": 11
      },
      "actions": [
        {
          "id": "stroke:竖:起笔",
          "segment": "起笔",
          "action": "逆锋起笔后按向内收的方向写。草字头里的两个竖画起笔就要写得厚、粗；土字头的竖画起笔后由粗到细。",
          "measurable": {
            "direction": "上→下，向内收",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:20:47",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:竖:行笔",
          "segment": "行笔",
          "action": "竖画由粗到细地向下写；土字头的竖画只露一点点小头，不能写长。",
          "measurable": {
            "direction": "向内收",
            "pressure_profile": "粗→细",
            "entry": null,
            "exit": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:53:58",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:竖:收笔",
          "segment": "收笔",
          "action": "收笔时用笔尖轻轻回一下就可以了。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "由粗到细收住",
            "pressure_profile": "粗→细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 14,
            "title": "第6节 头头是道",
            "t": "00:14:25",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖:转折",
          "segment": "转折",
          "action": "遇到横画就转过来向下书写，转折处要圆润，转过来以后仍是向内收。",
          "measurable": {
            "direction": "向下（转折后向内收）",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "转折处（要圆润）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第7节 头头是道2",
            "t": "00:26:00",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖:整笔",
          "segment": "整笔",
          "action": "整笔形态：草字头的两个竖画向内收，写得较胖较粗；土字头的竖画由粗到细、只露一点小头。",
          "measurable": {
            "direction": "向内收",
            "pressure_profile": "粗→细"
          },
          "certainty": "verbatim",
          "source": {
            "part": 13,
            "title": "第5节 情有独钟",
            "t": "00:11:41",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:竖点",
      "name": "竖点",
      "font": "隶书",
      "category": "隶书点画类",
      "tags": [
        "隶书",
        "点画",
        "竖点",
        "【解析】"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 10,
        "title": "第2节 点石成金，张弛有度",
        "t": "00:13:07",
        "evidence_count": 7
      },
      "actions": [
        {
          "id": "stroke:竖点:起笔",
          "segment": "起笔",
          "action": "竖点（上面那个小点）起笔与竖画起笔相同：逆入藏锋起笔，先向上有一个动作，用毛笔顶上去，再圆转过来向下。",
          "measurable": {
            "direction": "向上起笔 → 圆转 → 向下",
            "entry": "向上逆锋起笔，圆转过来",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "慢一点",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:12:44",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:竖点:行笔",
          "segment": "行笔",
          "action": "竖点行笔：圆转过来以后用笔尖把它写下来，中途不要压着笔不动，要有一个稍微提笔的动作。",
          "measurable": {
            "direction": "向下",
            "entry": null,
            "exit": null,
            "pressure_profile": "落笔后稍提笔",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:14:14",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:竖点:收笔",
          "segment": "收笔",
          "action": "竖点收笔要圆润：先向上顶一下毛笔再圆转下行，不能在落笔处压着不动，否则会像之前那样出现一个「大脑袋」。",
          "measurable": {
            "direction": "向下 → 慢慢提笔",
            "entry": null,
            "exit": "慢慢把笔尖提起来，出锋呈尖状",
            "pressure_profile": null,
            "speed_hint": "慢一点",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:14:09",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:竖点:整笔",
          "segment": "整笔",
          "action": "守字（字幕作「手／首／守」）宝盖头点画的变体：方向向左倒过来，比完字的长一点，像一个小短竖，底下带小回笔；整体更粗重。",
          "measurable": {
            "direction": "向左斜过来",
            "entry": "逆锋起笔",
            "exit": "底下带一个小回笔",
            "pressure_profile": "更粗重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:32:39",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:竖钩",
      "name": "竖钩",
      "font": "隶书",
      "category": "钩画类",
      "tags": [
        "隶书",
        "钩画类",
        "竖钩",
        "曹全碑特征",
        "用笔",
        "【解析】"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:03:05",
        "evidence_count": 15
      },
      "actions": [
        {
          "id": "stroke:竖钩:起笔",
          "segment": "起笔",
          "action": "藏锋逆锋切入。",
          "measurable": {
            "direction": "先向右下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:03:05",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:竖钩:行笔",
          "segment": "行笔",
          "action": "先竖，再后向左送。",
          "measurable": {
            "direction": "右下 → 向下（很短）→ 向左推出去（较长）",
            "entry": null,
            "exit": null,
            "pressure_profile": "转弯处最粗",
            "speed_hint": "圆润转弯，慢慢弯转过来",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:03:11",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:竖钩:收笔",
          "segment": "收笔",
          "action": "收笔处回锋，或出锋。",
          "measurable": {
            "direction": "向左",
            "entry": null,
            "exit": "回锋或出锋",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:03:11",
            "evidence_count": 1
          }
        },
        {
          "id": "stroke:竖钩:转折",
          "segment": "转折",
          "action": "竖画底部用笔尖圆转过来（像写撇画一样转），转弯处是整笔最重、最粗的地方。",
          "measurable": {
            "direction": "下 → 左",
            "entry": null,
            "exit": null,
            "pressure_profile": "转弯处最重",
            "speed_hint": "慢",
            "stop_points": [
              "转弯处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:17:10",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖钩:钩",
          "segment": "钩",
          "action": "像写撇画一样，用无名指把毛笔慢慢向左送出去，慢慢出一个小尖儿；不能直接挑出去，也不能飞扫出去。",
          "measurable": {
            "direction": "向左推出后回锋",
            "entry": null,
            "exit": "用笔尖回一下锋，收笔圆润（去掉碑上残破的尖儿）",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:13:07",
            "evidence_count": 12
          }
        }
      ]
    },
    {
      "id": "stroke:竖钩（守字）",
      "name": "竖钩（守字）",
      "font": "隶书",
      "category": "隶书钩画类",
      "tags": [
        "【解析】",
        "隶书",
        "钩画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 16,
        "title": "第8节 头头是道3",
        "t": "00:38:02",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:竖钩（守字）:起笔",
          "segment": "起笔",
          "action": "守字下面的竖钩：先写一个小短横，然后逆锋起笔。",
          "measurable": {
            "direction": "向下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:38:02",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:竖钩（守字）:收笔",
          "segment": "收笔",
          "action": "圆转过来之后用笔尖直接写出圆润的弧度，慢慢把笔提起来，全程不压笔；形态像雨伞把。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "用笔尖直接推出圆润的弧度，慢慢提笔",
            "pressure_profile": "不压笔",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:39:33",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖钩（守字）:转折",
          "segment": "转折",
          "action": "竖钩在转折处圆转过来之后直接提笔，不压笔，所以会边写越写越细——跟之前学的竖钩（在转折处压笔）稍有不同。",
          "measurable": {
            "direction": "向下 → 圆转",
            "entry": null,
            "exit": null,
            "pressure_profile": "不压笔，边写越写越细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:38:53",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:竖画",
      "name": "竖画",
      "font": "隶书",
      "category": "竖画类",
      "tags": [
        "隶书",
        "弓弩直立",
        "【解析】",
        "篆书",
        "竖画",
        "笔画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:00:48",
        "evidence_count": 30
      },
      "actions": [
        {
          "id": "stroke:竖画:起笔",
          "segment": "起笔",
          "action": "起笔逆锋切入。",
          "measurable": {
            "direction": "向上圆转后向下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:00:52",
            "evidence_count": 17
          }
        },
        {
          "id": "stroke:竖画:行笔",
          "segment": "行笔",
          "action": "铺毫下行，下行过程中用力要均匀。",
          "measurable": {
            "direction": "向下，略向左偏",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:00:56",
            "evidence_count": 21
          }
        },
        {
          "id": "stroke:竖画:收笔",
          "segment": "收笔",
          "action": "收笔时笔锋慢提，在空中虚势；收锋线的形态呈锥状。",
          "measurable": {
            "direction": "上→下",
            "entry": null,
            "exit": "提笔后用笔尖回锋一下出尖",
            "pressure_profile": "渐提渐轻",
            "speed_hint": "慢",
            "stop_points": [
              "笔尖回锋处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:01:01",
            "evidence_count": 16
          }
        }
      ]
    },
    {
      "id": "stroke:竖提",
      "name": "竖提",
      "font": "隶书",
      "category": "隶书提画类",
      "tags": [
        "隶书",
        "提画类",
        "竖提"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 12,
        "title": "第4节 群英荟萃",
        "t": "01:18:57",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:竖提:起笔",
          "segment": "起笔",
          "action": "先逆锋起笔写一个小竖画，向下轻轻压毛笔，笔画自然由细变粗。",
          "measurable": {
            "direction": "上 → 下",
            "entry": "逆锋起笔，向下轻压毛笔",
            "exit": null,
            "pressure_profile": "由细到粗",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:20:25",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖提:行笔",
          "segment": "行笔",
          "action": "竖画由细到粗（慢慢把毛笔按下去就变粗），竖画略微向左斜。",
          "measurable": {
            "direction": "上 → 下",
            "entry": null,
            "exit": null,
            "pressure_profile": "由细到粗",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:20:43",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖提:收笔",
          "segment": "收笔",
          "action": "圆转过来后向右上把毛笔送出去出锋，出尖的地方离横中线非常近。",
          "measurable": {
            "direction": "右上",
            "entry": null,
            "exit": "向右上送出去出尖",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "出尖处（近横中线）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:19:54",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖提:转折",
          "segment": "转折",
          "action": "在竖画收笔的位置向左轻压毛笔，然后逆锋起笔、圆转过来；动作要缓慢，不能一下子猛按再猛抬。",
          "measurable": {
            "direction": "下 → 左 → 右上",
            "entry": "向左轻压毛笔后逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "缓慢一点",
            "stop_points": [
              "竖画收笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:19:19",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:竖弯",
      "name": "竖弯（字幕作「竖环」）",
      "font": "隶书",
      "category": "竖类",
      "tags": [
        "竖类",
        "篆书"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 2,
        "title": "第2节 不积跬步无以至千里",
        "t": "00:16:11",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:竖弯:起笔",
          "segment": "起笔",
          "action": "竖弯起笔：斜着把笔锋打开（斜切切入），使笔毫铺开，笔画起始处最粗。",
          "measurable": {
            "entry": "斜切切入，斜着把笔锋打开，笔毫铺开",
            "pressure_profile": "起笔处最粗"
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "00:56:52",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖弯:行笔",
          "segment": "行笔",
          "action": "竖弯行笔：自上而下带轻微弧度地写出，笔画由粗到细；弧度一定要写出来，不能拉成一条直线。",
          "measurable": {
            "direction": "自上而下，带轻微弧度",
            "pressure_profile": "由粗到细（起笔粗、向下渐细）"
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "00:18:06",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖弯:收笔",
          "segment": "收笔",
          "action": "竖弯收笔：沿上边缘线收笔，下缘形成一道斜切口；最后慢慢抬笔，把尖儿抬出来（写的时候很快，不必完全照搬这个斜度）。",
          "measurable": {
            "exit": "沿上边缘线收笔，下缘呈斜切口",
            "pressure_profile": "收笔时笔尖逐渐提起",
            "stop_points": [
              "收笔处的上边缘线"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "00:42:33",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖弯:转折",
          "segment": "转折",
          "action": "竖弯的转折处：写到弯处先停顿一下再接第二笔，接口明显、略不对称即可，不必收得完全均匀；要能感觉到这个拐点的位置。",
          "measurable": {
            "direction": "转折处由竖直方向转向横向",
            "exit": "停顿后接第二笔",
            "stop_points": [
              "转折停顿处（接口）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "00:12:18",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:竖弯钩",
      "name": "竖弯钩",
      "font": "隶书",
      "category": "隶书钩画类",
      "tags": [
        "隶书",
        "钩画类",
        "竖弯钩",
        "轻燕尾",
        "【解析】",
        "钩画",
        "呼应"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 12,
        "title": "第4节 群英荟萃",
        "t": "00:39:12",
        "evidence_count": 8
      },
      "actions": [
        {
          "id": "stroke:竖弯钩:起笔",
          "segment": "起笔",
          "action": "先逆锋起笔写一个小竖画。",
          "measurable": {
            "direction": "起笔后向右下",
            "entry": "逆锋起笔，先写一个小竖",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:40:07",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖弯钩:行笔",
          "segment": "行笔",
          "action": "向右下写捺画，边走边顿，和写捺画的写法一样。",
          "measurable": {
            "direction": "向右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "边走边顿",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:41:20",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖弯钩:收笔",
          "segment": "收笔",
          "action": "顿笔后慢慢出锋，收尾微微向右上扬；这种燕尾的顿笔不如捺画明显，叫轻燕尾。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "顿笔后慢慢出锋",
            "pressure_profile": null,
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:39:26",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:竖弯钩:转折",
          "segment": "转折",
          "action": "写完小竖画后把毛笔慢慢提起来——这个提笔过程就是调锋，毛笔不离开纸面，方便向右下转过去。",
          "measurable": {
            "direction": "下 → 右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "提笔变细",
            "speed_hint": "慢",
            "stop_points": [
              "竖画收笔处提笔调锋"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:43:30",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:竖折",
      "name": "竖折",
      "font": "隶书",
      "category": "折类",
      "tags": [
        "折类",
        "篆书",
        "隶书",
        "折画",
        "折画类",
        "竖折",
        "复习"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 2,
        "title": "第2节 不积跬步无以至千里",
        "t": "00:12:10",
        "evidence_count": 11
      },
      "actions": [
        {
          "id": "stroke:竖折:起笔",
          "segment": "起笔",
          "action": "竖折的竖画起笔：逆锋起笔，向下写竖（隶书竖画一般带一点点弧度）。",
          "measurable": {
            "direction": "上 → 下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "01:05:32",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖折:行笔",
          "segment": "行笔",
          "action": "竖折写法：先写竖，顿住后在折角处稍微停顿一下，再折出第二笔（横）；接口明显、可以略不对称。",
          "measurable": {
            "direction": "先竖后横",
            "exit": "折角停顿后接横",
            "stop_points": [
              "折角处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "00:12:18",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖折:收笔",
          "segment": "收笔",
          "action": "竖折收笔：折出的横按横画的要求写（微微上拱），最后把口收住；收尾方圆皆可。",
          "measurable": {
            "exit": "方圆皆可，不强求圆润"
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "00:12:10",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖折:转折",
          "segment": "转折",
          "action": "竖折的横画在竖画之内起笔：竖写完后，在竖画中间的位置逆锋起笔，向右写横画；这样横画的下缘与竖画外轮廓连成一个圆润的形状。",
          "measurable": {
            "direction": "下 → 右",
            "entry": "在竖画内逆锋起笔",
            "exit": null,
            "pressure_profile": "圆转处逐渐压笔",
            "speed_hint": null,
            "stop_points": [
              "竖画内起横处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "01:05:46",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:竖折:整笔",
          "segment": "整笔",
          "action": "隶书的竖折和横折都是搭接写出来的、都分两笔写（口诀「逢折必断」）；一笔连着写会出现楷书那种方正感，隶书的造型就没了。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "分两笔写",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 11,
            "title": "第3节 宛转矩折",
            "t": "01:08:26",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:提画",
      "name": "提画",
      "font": "隶书",
      "category": "提画类",
      "tags": [
        "隶书"
      ],
      "certainty": "inferred",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:02:54",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:提画:行笔",
          "segment": "行笔",
          "action": "从左下向右上挑起，用力由重而轻。",
          "measurable": {
            "direction": "左下→右上",
            "entry": null,
            "exit": null,
            "pressure_profile": "由重→轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:59",
            "evidence_count": 1
          }
        },
        {
          "id": "stroke:提画:整笔",
          "segment": "整笔",
          "action": "提画是从左下向右上挑起的笔画，用力由重而轻；倾斜度略有不同。（字幕同句作「立贯始终」，未确证，保留原词。）",
          "measurable": {
            "direction": "左下→右上",
            "entry": null,
            "exit": null,
            "pressure_profile": "由重→轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:59",
            "evidence_count": 2
          }
        }
      ]
    },
    {
      "id": "stroke:卧钩",
      "name": "卧钩",
      "font": "隶书",
      "category": "隶书钩画类",
      "tags": [
        "隶书",
        "钩画类",
        "卧钩"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 12,
        "title": "第4节 群英荟萃",
        "t": "00:57:19",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:卧钩:起笔",
          "segment": "起笔",
          "action": "在左斜线下方一点点逆锋起笔（起笔动作与其它笔画一致，只是方向不同）。",
          "measurable": {
            "direction": "右下",
            "entry": "逆锋起笔（左斜线下）",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "左斜线下起笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:57:38",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:卧钩:行笔",
          "segment": "行笔",
          "action": "中间有一段变细，斜着向中心点的位置写下来；到中心点后边走边顿，慢慢变平。",
          "measurable": {
            "direction": "右下 → 转平",
            "entry": null,
            "exit": null,
            "pressure_profile": "中段变细，后段变粗",
            "speed_hint": null,
            "stop_points": [
              "中心点处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:57:47",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:卧钩:收笔",
          "segment": "收笔",
          "action": "慢慢把毛笔平着送出去（出锋），收尾落在横中线下方；整体平缓，但不能写成横。",
          "measurable": {
            "direction": "平向右",
            "entry": null,
            "exit": "平向出锋",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "横中线下收尾处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "00:58:03",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:向内收的竖画",
      "name": "向内收的竖画",
      "font": "隶书",
      "category": "隶书竖画类",
      "tags": [
        "【解析】",
        "隶书",
        "竖画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 16,
        "title": "第8节 头头是道3",
        "t": "00:46:10",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:向内收的竖画:起笔",
          "segment": "起笔",
          "action": "竖画逆锋起笔，向内收着写。",
          "measurable": {
            "direction": "向内收",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:46:10",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:向内收的竖画:行笔",
          "segment": "行笔",
          "action": "两竖略向内斜、带一点小弧度；九字头两边的竖画也必须向内收。",
          "measurable": {
            "direction": "略向内斜",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:45:09",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:小短横（雨字头四点）",
      "name": "小短横（雨字头四点）",
      "font": "隶书",
      "category": "隶书点画类",
      "tags": [
        "【解析】",
        "隶书",
        "点画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 16,
        "title": "第8节 头头是道3",
        "t": "00:51:28",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:小短横（雨字头四点）:起笔",
          "segment": "起笔",
          "action": "雨字头四个点画用笔尖写，不要用力压毛笔；起笔要有藏锋的动作，越是细小的笔画越要做精致一点。",
          "measurable": {
            "direction": null,
            "entry": "用笔尖起笔，藏锋",
            "exit": null,
            "pressure_profile": "轻，不要压笔",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:51:32",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:小短横（雨字头四点）:行笔",
          "segment": "行笔",
          "action": "四个点画写得很随意，粗细、长短各不相同，不必把下面几笔照着上面一笔比量成一样。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": "粗细不一",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:51:54",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:小短横（雨字头四点）:整笔",
          "segment": "整笔",
          "action": "四个点画要控制好长短：与左边小竖画之间留出小距离，上下的间距也要匀称，空间排布整体要整。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "与左边小竖画之间留出小距离"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:55:13",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:小撇画",
      "name": "小撇画",
      "font": "隶书",
      "category": "隶书撇画类",
      "tags": [
        "【解析】",
        "隶书",
        "撇画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 16,
        "title": "第8节 头头是道3",
        "t": "00:14:35",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:小撇画:起笔",
          "segment": "起笔",
          "action": "小撇画在短横画大约 1/3 的位置起笔。",
          "measurable": {
            "direction": null,
            "entry": "在短横画 1/3 处起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:14:35",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:小撇画:行笔",
          "segment": "行笔",
          "action": "小撇画比较直，由细到粗地写。",
          "measurable": {
            "direction": "向左下",
            "entry": null,
            "exit": null,
            "pressure_profile": "由细到粗",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 16,
            "title": "第8节 头头是道3",
            "t": "00:03:55",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:小竖撇",
      "name": "小竖撇",
      "font": "隶书",
      "category": "隶书撇画类",
      "tags": [
        "隶书",
        "撇画",
        "月字"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 10,
        "title": "第2节 点石成金，张弛有度",
        "t": "00:30:24",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:小竖撇:起笔",
          "segment": "起笔",
          "action": "月字的小竖撇起笔：逆锋起笔，先向上走，这小竖撇上边一节的写法跟竖画完全一样。",
          "measurable": {
            "direction": "先向上（竖的一段）",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:30:39",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:小竖撇:行笔",
          "segment": "行笔",
          "action": "小竖撇中段有一个调锋动作：把笔稍微提起来，笔画出现一点变细；然后才向左撇出去，否则笔毛会散。撇出时用手指（无名指顶住毛笔）把毛笔向左平推送出去，是平向的撇，不是斜撇。",
          "measurable": {
            "direction": "竖向一段 → 向左平推",
            "entry": null,
            "exit": null,
            "pressure_profile": "中段稍微变细（提笔调锋）",
            "speed_hint": null,
            "stop_points": [
              "调锋处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:33:04",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:小竖撇:收笔",
          "segment": "收笔",
          "action": "小竖撇收笔：向左撇出去后停住，把笔稍微提起来用笔尖向上回一下笔。向上回是为了跟下一个横画有笔势承接——隶书笔画笔笔断、笔笔连。",
          "measurable": {
            "direction": "向左撇出后向上回笔",
            "entry": null,
            "exit": "停住后提笔，用笔尖向上回笔",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "撇出后的停笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:34:09",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:斜钩",
      "name": "斜钩",
      "font": "隶书",
      "category": "隶书钩画类",
      "tags": [
        "隶书",
        "钩画类",
        "斜钩"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 12,
        "title": "第4节 群英荟萃",
        "t": "01:13:54",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:斜钩:起笔",
          "segment": "起笔",
          "action": "逆锋起笔，往右下倾斜；写法跟之前学过的捺画基本一样。",
          "measurable": {
            "direction": "右下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:13:54",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:斜钩:行笔",
          "segment": "行笔",
          "action": "往右下写，中间有一段变细的过程。",
          "measurable": {
            "direction": "右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "中间变细",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:15:11",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:斜钩:收笔",
          "segment": "收笔",
          "action": "逐渐顿笔，再慢慢用笔尖出锋；顿笔的位置挨着左斜线。",
          "measurable": {
            "direction": "右下 → 出锋",
            "entry": null,
            "exit": "顿笔出锋",
            "pressure_profile": "顿后提笔",
            "speed_hint": null,
            "stop_points": [
              "顿笔处（挨左斜线）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 12,
            "title": "第4节 群英荟萃",
            "t": "01:15:14",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:右向点",
      "name": "右向点",
      "font": "隶书",
      "category": "隶书点画类",
      "tags": [
        "隶书",
        "点画",
        "右向点"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 10,
        "title": "第2节 点石成金，张弛有度",
        "t": "00:17:46",
        "evidence_count": 2
      },
      "actions": [
        {
          "id": "stroke:右向点:起笔",
          "segment": "起笔",
          "action": "右向点（六字右下那一点）起笔：同样逆锋起笔，只是方向上有一个小变化，圆转过来向右下写。",
          "measurable": {
            "direction": "圆转后向右下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:17:58",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:右向点:行笔",
          "segment": "行笔",
          "action": "右向点行笔：圆转过来向右下，边往下写边慢慢把笔提起来（渐行渐提），写出小尖。",
          "measurable": {
            "direction": "向右下",
            "entry": null,
            "exit": "出一个小尖",
            "pressure_profile": "渐行渐提",
            "speed_hint": "慢慢把笔提起来",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:18:12",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:长横",
      "name": "长横",
      "font": "隶书",
      "category": "横画类",
      "tags": [
        "隶书",
        "蚕头燕尾",
        "一波三折",
        "复习",
        "长横"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:00:29",
        "evidence_count": 17
      },
      "actions": [
        {
          "id": "stroke:长横:起笔",
          "segment": "起笔",
          "action": "起笔向左下逆锋切入，然后向行笔方向过渡。",
          "measurable": {
            "direction": "左→右",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:00:37",
            "evidence_count": 22
          }
        },
        {
          "id": "stroke:长横:行笔",
          "segment": "行笔",
          "action": "行笔过程中轻重变化较为明显，整体呈现两头重、中间轻的形态。",
          "measurable": {
            "direction": "左→右（写平时保持平直，不作右上斜）",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:00:37",
            "evidence_count": 27
          }
        },
        {
          "id": "stroke:长横:收笔",
          "segment": "收笔",
          "action": "渐顿下来作「原地右下顿」（边走边按、逐渐变重），再慢慢把笔锋抬起来平出成尖；顿笔最重处与起笔处在同一条水平线上。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "慢慢顿笔出锋",
            "pressure_profile": null,
            "speed_hint": "慢慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:27:33",
            "evidence_count": 25
          }
        },
        {
          "id": "stroke:长横:整笔",
          "segment": "整笔",
          "action": "整体形态具备蚕头燕尾、一波三折的特点，是《曹全碑》中最具代表性的笔画。",
          "measurable": {
            "direction": "左→右",
            "entry": "逆锋，向左下",
            "exit": null,
            "pressure_profile": "两头重→中间轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:00:29",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:长横低头起笔",
      "name": "长横·梯形低头起笔",
      "font": "隶书",
      "category": "隶书横画类",
      "tags": [
        "隶书",
        "梯形低头起笔"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 9,
        "title": "第1节 蚕头雁尾，弓弩直立",
        "t": "00:48:05",
        "evidence_count": 2
      },
      "actions": [
        {
          "id": "stroke:长横低头起笔:起笔",
          "segment": "起笔",
          "action": "梯形低头起笔（王字的长横）：先向左轻压毛笔，再向左前方压一下笔，转过来调锋后向右写，形成「低头」的感觉；起笔向左前方倾斜。",
          "measurable": {
            "direction": "先向左 → 向左前方压笔 → 转锋向右",
            "entry": "逆锋轻入后向左前方压笔",
            "exit": null,
            "pressure_profile": "轻压 → 压笔成梯形 → 匀称",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:48:42",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:长横方头起笔",
      "name": "长横·方头起笔",
      "font": "隶书",
      "category": "隶书横画类",
      "tags": [
        "隶书",
        "方头起笔"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 9,
        "title": "第1节 蚕头雁尾，弓弩直立",
        "t": "00:31:31",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:长横方头起笔:起笔",
          "segment": "起笔",
          "action": "方头起笔（三字长横）：先向左轻压毛笔，把毛笔竖过来向下压笔，把方头的形态描绘出来，然后向下压住、把笔锋调过来，中锋运笔向右写，最后顿笔出锋。",
          "measurable": {
            "direction": "先向左 → 向下竖压（成方头）→ 调锋向右",
            "entry": "逆锋轻入后向左轻压，把笔竖过来向下压出方头",
            "exit": null,
            "pressure_profile": "轻压 → 向下压笔 → 调锋后中锋匀称",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:31:31",
            "evidence_count": 9
          }
        }
      ]
    },
    {
      "id": "stroke:长横圆头起笔",
      "name": "长横·圆头起笔",
      "font": "隶书",
      "category": "隶书横画类",
      "tags": [
        "隶书",
        "圆头起笔"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 9,
        "title": "第1节 蚕头雁尾，弓弩直立",
        "t": "00:39:58",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:长横圆头起笔:起笔",
          "segment": "起笔",
          "action": "圆头起笔（干字的长横）：与短横起笔动作相同，先向左轻压毛笔，圆转过来向右写，中间有变细过程，最后顿笔出锋。",
          "measurable": {
            "direction": "先向左 → 圆转 → 向右",
            "entry": "逆锋轻入，向左轻压后圆转",
            "exit": null,
            "pressure_profile": "渐提变细",
            "speed_hint": "慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第1节 蚕头雁尾，弓弩直立",
            "t": "00:39:58",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:折",
      "name": "折（折角）",
      "font": "隶书",
      "category": "转折类",
      "tags": [
        "转折",
        "篆书",
        "【解析】"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 2,
        "title": "第2节 不积跬步无以至千里",
        "t": "01:00:54",
        "evidence_count": 8
      },
      "actions": [
        {
          "id": "stroke:折:起笔",
          "segment": "起笔",
          "action": "折的起笔：先往下顿住（切笔），把笔顿好再往下写。",
          "measurable": {
            "direction": "自上而下",
            "entry": "顿住切入",
            "pressure_profile": "顿笔处稍重"
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 积水成渊蛟龙生焉",
            "t": "00:13:15",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:折:收笔",
          "segment": "收笔",
          "action": "折画收笔同样可以做成燕尾；老师示范时折后往上收，收笔形状与起笔形状不必相同。",
          "measurable": {
            "exit": "燕尾收笔"
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 积水成渊蛟龙生焉",
            "t": "00:23:28",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:折:转折",
          "segment": "转折",
          "action": "折：第一笔写完先停顿（顿住）再折出第二笔；折角处方向骤变，可写得方硬、稍作转折，接口处允许留一点破口，不必对称。",
          "measurable": {
            "direction": "自上而下走弧线后折转，转折处稍向上",
            "exit": "折后打开笔继续写",
            "stop_points": [
              "折角停顿处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "01:00:54",
            "evidence_count": 9
          }
        }
      ]
    },
    {
      "id": "stroke:折画",
      "name": "折画",
      "font": "隶书",
      "category": "折画类",
      "tags": [
        "隶书"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 15,
        "title": "第15节 中国书法基础知识-基本用笔特点",
        "t": "00:02:11",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:折画:转折",
          "segment": "转折",
          "action": "横折是由横转向竖的方折笔画：先写横，然后顺势向下转折；写这类折画时手腕翻转要活，衔接宜自然。",
          "measurable": {
            "direction": "先向右写横，再顺势向下转折",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:20",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:折画:整笔",
          "segment": "整笔",
          "action": "折画婉转曲折，种类很多，本节展示的是横折。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 15,
            "title": "第15节 中国书法基础知识-基本用笔特点",
            "t": "00:02:11",
            "evidence_count": 1
          }
        }
      ]
    },
    {
      "id": "stroke:转",
      "name": "转（圆转）",
      "font": "隶书",
      "category": "转折类",
      "tags": [
        "转折",
        "篆书"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 2,
        "title": "第2节 不积跬步无以至千里",
        "t": "01:01:02",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:转:转折",
          "segment": "转折",
          "action": "转：不提笔、不换面，笔尖保持铺开直接写下去，由一向的笔势圆弧过渡到另一方向，中间没有停顿点。",
          "measurable": {
            "direction": "由一向的笔势转入另一方向，圆弧过渡",
            "exit": "不提笔、不换面，直接抹入下一方向",
            "pressure_profile": "笔尖保持铺开（打开）状态"
          },
          "certainty": "verbatim",
          "source": {
            "part": 2,
            "title": "第2节 不积跬步无以至千里",
            "t": "01:01:02",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:左向点",
      "name": "左向点",
      "font": "隶书",
      "category": "隶书点画类",
      "tags": [
        "隶书",
        "点画",
        "左向点"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 10,
        "title": "第2节 点石成金，张弛有度",
        "t": "00:14:37",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:左向点:起笔",
          "segment": "起笔",
          "action": "左向点（六字左下那一点）起笔：逆锋起笔，先向右上有一个动作，再圆转回来向左下撇出去。",
          "measurable": {
            "direction": "先向右上 → 圆转 → 向左下",
            "entry": "逆锋起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:14:48",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:左向点:行笔",
          "segment": "行笔",
          "action": "左向点行笔：逆锋起笔后圆转过来向下，出尖；出尖的小动作不要快，否则笔会飞出去。",
          "measurable": {
            "direction": "向左下",
            "entry": null,
            "exit": "出小尖",
            "pressure_profile": "渐行渐提",
            "speed_hint": "出尖动作不要太快",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:15:24",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:左向点:收笔",
          "segment": "收笔",
          "action": "左向点收笔：把毛笔向下、向左下送到位再收，出一个小尖；这个尖看着不扎人，仍是圆润的状态。提笔要渐进，不能一下子把笔提起来。",
          "measurable": {
            "direction": "向左下",
            "entry": null,
            "exit": "送到位后出小尖（圆润不扎人）",
            "pressure_profile": "逐渐提笔渐细出尖",
            "speed_hint": "送到位，慢慢提笔",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 10,
            "title": "第2节 点石成金，张弛有度",
            "t": "00:15:44",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:垂露竖",
      "name": "垂露竖",
      "font": "硬笔",
      "category": "竖画类",
      "tags": [
        "硬笔",
        "竖画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 3,
        "title": "第3节 竖画训练",
        "t": "00:03:05",
        "evidence_count": 9
      },
      "actions": [
        {
          "id": "stroke:垂露竖:起笔",
          "segment": "起笔",
          "action": "笔尖落纸先向右下做一个顿笔，顿完不能直接往右下方走，要向左上回锋回到笔画中间，再从中间竖直下行。顿笔只要轻轻一顿，顿大了会出现大疙瘩。",
          "measurable": {
            "direction": "向右下顿笔，向左上回锋后向下行笔",
            "entry": "右下顿笔，顿后向左上回锋到中间",
            "exit": null,
            "pressure_profile": "轻顿（顿笔过大就会出现大疙瘩）",
            "speed_hint": "顿笔后回锋再下行，动作放慢",
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:03:25",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:垂露竖:行笔",
          "segment": "行笔",
          "action": "找准方向后放心大胆地往下行笔，整个行笔必须一气呵成、自然顺畅，不能断断续续、也不能犹豫。写斜了、写方了都没关系，初学阶段先把行笔完整地展现出来，不要既想写正又想展现姿态。",
          "measurable": {
            "direction": "竖向下行",
            "entry": null,
            "exit": null,
            "pressure_profile": "匀速下行，不忽轻忽重",
            "speed_hint": "一气呵成、顺畅；可以慢一点走，但不能犹豫",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:07:29",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:垂露竖:收笔",
          "segment": "收笔",
          "action": "行笔下来之后先往左走，再转右，转右之后稍顿，回锋收笔。收完的形态像一滴露水垂在笔画下端，所以叫垂露竖。",
          "measurable": {
            "direction": "下行后先向左，再转右",
            "entry": null,
            "exit": "回锋收笔（收处圆润饱满如垂露）",
            "pressure_profile": "转右后稍顿，再回锋",
            "speed_hint": null,
            "stop_points": [
              "收笔处的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:10:42",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:点",
      "name": "点",
      "font": "硬笔",
      "category": null,
      "tags": [],
      "certainty": "verbatim",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:点:起笔",
          "segment": "起笔",
          "action": "点画由轻到重地落笔，稍微写长一点",
          "measurable": {
            "direction": null,
            "entry": "笔尖由轻落纸，转为重按",
            "exit": null,
            "pressure_profile": "轻→重（点的起笔要见动作）",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第9节 左偏旁及书写规律1",
            "t": "00:05:11",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:点:收笔",
          "segment": "收笔",
          "action": "点要「进去」写，回锋收笔，收住",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔",
            "pressure_profile": "重收（收笔处见厚度）",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第9节 左偏旁及书写规律1",
            "t": "00:04:24",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:短横",
      "name": "短横",
      "font": "硬笔",
      "category": "横画类",
      "tags": [
        "硬笔",
        "横画"
      ],
      "certainty": "inferred",
      "source": {
        "part": 2,
        "title": "第2节 横画练习",
        "t": "00:21:36",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:短横:起笔",
          "segment": "起笔",
          "action": "起笔动作与长横完全一样，同样先做一个右下顿笔，再转向右上。短横与长横的差别不在起笔，而在长度与弧度。",
          "measurable": {
            "direction": "右下顿笔后向右上行笔",
            "entry": "右下顿笔",
            "exit": null,
            "pressure_profile": "起笔重（顿笔）",
            "speed_hint": "起笔顿住再转笔",
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:21:36",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:短横:行笔",
          "segment": "行笔",
          "action": "同样往右上倾斜并带弧度，但短横的弧度展现很少、线条干脆。短横与长横同时出现在一个字里时，短横要写得略直一点，否则两笔都带弧度会显得「肉」。",
          "measurable": {
            "direction": "左→右上斜（较缓）",
            "entry": null,
            "exit": null,
            "pressure_profile": "重起→轻行→重收",
            "speed_hint": "行笔干脆、短促",
            "stop_points": [
              "起笔顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:21:41",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:短横:收笔",
          "segment": "收笔",
          "action": "收笔动作与长横完全相同：末端右下顿笔，再回锋收笔。三种横的收笔都一样。",
          "measurable": {
            "direction": "行笔末端向右下顿笔，笔尖回勾",
            "entry": null,
            "exit": "回锋收笔",
            "pressure_profile": "收笔重",
            "speed_hint": "顿笔后回勾再提起",
            "stop_points": [
              "行笔末端的右下顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:22:21",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:短撇",
      "name": "短撇",
      "font": "硬笔",
      "category": "撇画类",
      "tags": [
        "硬笔",
        "撇画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 4,
        "title": "第4节 撇画与提画训练",
        "t": "00:16:37",
        "evidence_count": 10
      },
      "actions": [
        {
          "id": "stroke:短撇:起笔",
          "segment": "起笔",
          "action": "右下顿笔起笔（与长撇一致）。",
          "measurable": {
            "direction": "顿笔后转向左下",
            "entry": "右下顿笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:16:47",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:短撇:行笔",
          "segment": "行笔",
          "action": "行笔过程中要把笔锋转换过来往左走，走左下行笔；整体形态略微呈现直。",
          "measurable": {
            "direction": "由右下转向左下",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:17:17",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:反点",
      "name": "反点",
      "font": "硬笔",
      "category": "点画类",
      "tags": [
        "硬笔",
        "点画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 4,
        "title": "第4节 撇画与提画训练",
        "t": "00:51:59",
        "evidence_count": 10
      },
      "actions": [
        {
          "id": "stroke:反点:起笔",
          "segment": "起笔",
          "action": "轻起笔进去，行笔由轻到重。",
          "measurable": {
            "direction": null,
            "entry": "轻起笔",
            "exit": null,
            "pressure_profile": "轻→重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "01:00:34",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:反点:收笔",
          "segment": "收笔",
          "action": "回锋收笔，收笔要重一点，笔头自然留出一个小钝头；作用是把整个字的重心拽回来、把中线以下补厚。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔，重收笔，留小钝头",
            "pressure_profile": "由轻到重，收笔最重",
            "speed_hint": "沉得住，坠下来",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:51:59",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:反捺",
      "name": "反捺",
      "font": "硬笔",
      "category": "捺画类",
      "tags": [
        "硬笔",
        "捺画",
        "反捺"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 5,
        "title": "第5节 捺画训练",
        "t": "00:02:26",
        "evidence_count": 7
      },
      "actions": [
        {
          "id": "stroke:反捺:起笔",
          "segment": "起笔",
          "action": "反捺凌空入笔（露锋落笔），落笔后笔尖回锋；它其实就是把右点加长而已。",
          "measurable": {
            "direction": null,
            "entry": "凌空入笔，笔尖回锋",
            "exit": null,
            "pressure_profile": "由轻到重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:23:49",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:反捺:行笔",
          "segment": "行笔",
          "action": "行笔是由轻到重的渐重过程：凌空入笔之后逐渐加重，到最后最重的地方还要再往回锋一点，让笔画重合。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": "由轻到重（渐重），末段最重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:24:50",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:反捺:收笔",
          "segment": "收笔",
          "action": "收笔一定要重：回锋收笔，笔画重叠之后厚度才更加牢固，才能压得住整个字。收笔轻了，字就跟着放飞、不沉稳；字往右上倾斜时，下面全靠这个反捺压回来。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔，笔画重叠加厚",
            "pressure_profile": "末段最重（收笔一定要重）",
            "speed_hint": null,
            "stop_points": [
              "收笔处（最重处）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:25:07",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:横",
      "name": "横",
      "font": "硬笔",
      "category": null,
      "tags": [],
      "certainty": "verbatim",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:横:起笔",
          "segment": "起笔",
          "action": "横画顿笔起笔",
          "measurable": {
            "direction": "向右",
            "entry": "顿笔",
            "exit": null,
            "pressure_profile": "顿笔起",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第9节 左偏旁及书写规律1",
            "t": "00:44:32",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:横:收笔",
          "segment": "收笔",
          "action": "横画轻收，最后一笔要压重一点、把它整个字拉回来",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "轻收／回锋收笔",
            "pressure_profile": "顿笔起→轻收；末笔要重",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第9节 左偏旁及书写规律1",
            "t": "00:44:32",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:横钩",
      "name": "横钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "钩画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 7,
        "title": "第7节 勾画练习1",
        "t": "00:26:41",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:横钩:起笔",
          "segment": "起笔",
          "action": "横钩（字幕作「横沟」）起笔是顿笔，横画要带右上倾斜的弧度。",
          "measurable": {
            "direction": "右上倾斜",
            "entry": "顿笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔处的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:26:53",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:横钩:行笔",
          "segment": "行笔",
          "action": "沿着弧度往上走，弧度必须展现出来，这是横钩最难的地方。",
          "measurable": {
            "direction": "右上倾斜，沿弧度上行",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "弧度要顺，不要写成直线",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:26:53",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:横钩:收笔",
          "segment": "收笔",
          "action": "收笔处一定要把这个字拉回来（拉正）。笔画要有韧性，不能一蹲就下来。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "收笔处往回拉，把字拉正",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:28:42",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横钩:转折",
          "segment": "转折",
          "action": "到顶后往下压、按一下笔，是个蓄力的过程；笔停住了再出钩。这个「突然停住出钩」就是横钩的笔法动作。",
          "measurable": {
            "direction": "右上弧度到顶后转下按笔",
            "entry": null,
            "exit": null,
            "pressure_profile": "到顶处下压按笔蓄力",
            "speed_hint": null,
            "stop_points": [
              "按笔蓄力停住的那一点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:28:11",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:横钩:钩",
          "segment": "钩",
          "action": "停住之后往回走，从这里出钩——这才是横钩的写法；笔尖下探、回锋从中间出钩不是横钩。",
          "measurable": {
            "direction": "停住后往回走再出钩",
            "entry": null,
            "exit": "回锋收笔后往回出钩",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:27:06",
            "evidence_count": 3
          }
        }
      ]
    },
    {
      "id": "stroke:横斜钩",
      "name": "横斜钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "笔画",
        "勾画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 8,
        "title": "第8节 勾画训练2",
        "t": "00:02:25",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:横斜钩:起笔",
          "segment": "起笔",
          "action": "顿笔起笔，起笔后的横要右上倾斜。",
          "measurable": {
            "entry": "顿笔",
            "direction": "右上倾斜"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:12:23",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横斜钩:行笔",
          "segment": "行笔",
          "action": "行笔过程一定要挺拔，像拉开的弓、劲儿一直在里面；挺不住笔画就软了。要灌住气、一口气写下来，一气呵成，中途不能改、不能变形、不能拉长腿。",
          "measurable": {
            "direction": "右上倾斜后折下",
            "pressure_profile": "行笔饱满不塌",
            "speed_hint": "一气呵成"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:12:18",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:横斜钩:收笔",
          "segment": "收笔",
          "action": "收笔落点要伸展出去再出勾，这样笔画才有头有尾；横顿笔折下来就出钩会变成头重脚轻。",
          "measurable": {
            "exit": "伸展出去再出勾",
            "pressure_profile": "收笔不轻"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:16:40",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横斜钩:转折",
          "segment": "转折",
          "action": "顿笔后按笔折下来，转折处要挺住、不能软。",
          "measurable": {
            "direction": "由右上转为折下"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:12:23",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:横斜钩:钩",
          "segment": "钩",
          "action": "出勾要挺、有力度，并控制好出勾的间距；撇点要有空间，往里收一点再出来，字才能挺住。",
          "measurable": {
            "exit": "挺拔出勾",
            "stop_points": [
              "出勾处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:12:33",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横斜钩:整笔",
          "segment": "整笔",
          "action": "横斜钩的三个要点：角度因字而异（飞字角度大、风字角度小）、行笔过程要挺拔一气呵成、收笔落点要伸展出去再出勾。",
          "measurable": null,
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:13:42",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:横折",
      "name": "横折",
      "font": "硬笔",
      "category": "折画类",
      "tags": [
        "硬笔",
        "折画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 6,
        "title": "第6节 折画训练",
        "t": "00:00:51",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:横折:起笔",
          "segment": "起笔",
          "action": "横折起笔：先顿笔，再往右上走；横要有倾斜度，还要带弧度。",
          "measurable": {
            "direction": "左→右（右上倾斜）",
            "entry": "顿笔起笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "横的起笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:03:02",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:横折:行笔",
          "segment": "行笔",
          "action": "横折折下来后的行笔：直还是弧要提前打算——下部没有笔画（如口）可以写得稍直，里面填了笔画就带一点弧度。",
          "measurable": {
            "direction": "左下",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:07:31",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:横折:收笔",
          "segment": "收笔",
          "action": "横折收笔：竖走完回锋收笔。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:03:33",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:横折:转折",
          "segment": "转折",
          "action": "横折转折：三步关键词——先右下顿，再回到中间，然后向左下行笔；顿笔不宜过长，对比动作不要过大。",
          "measurable": {
            "direction": "右上 → 右下顿 → 回中 → 左下",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "转折处的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:06:29",
            "evidence_count": 8
          }
        },
        {
          "id": "stroke:横折:整笔",
          "segment": "整笔",
          "action": "横折整笔：横的倾斜度与弧度都要出来；转折的顿笔不宜过长、对比动作不要过大；写字要心平气和，不能带着情绪写。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "心平气和，节奏平稳",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:08:35",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:撇折",
      "name": "撇折",
      "font": "硬笔",
      "category": "折画类",
      "tags": [
        "硬笔",
        "折画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 6,
        "title": "第6节 折画训练",
        "t": "00:01:24",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:撇折:起笔",
          "segment": "起笔",
          "action": "撇折起笔：顿笔撇下来，撇本身是由重到轻。",
          "measurable": {
            "direction": "上→左下",
            "entry": "顿笔起笔",
            "exit": null,
            "pressure_profile": "重→轻",
            "speed_hint": null,
            "stop_points": [
              "撇的起笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:17:18",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:撇折:行笔",
          "segment": "行笔",
          "action": "撇折整笔的按笔速度和厚度要由重到轻、再到重、再到轻。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": "重→轻→重→轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:20:02",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:撇折:收笔",
          "segment": "收笔",
          "action": "撇折收笔：撇的收笔处一定要轻、要提一下再顿笔；两个厚重笔画重叠会突出厚度，成一个小墨团。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "提笔（由重到轻）后顿笔",
            "pressure_profile": "由重到轻",
            "speed_hint": null,
            "stop_points": [
              "撇的收笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:21:44",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:撇折:转折",
          "segment": "转折",
          "action": "撇折转折：撇下来之后顿笔，再折上去（提笔）；折上去的地方有一个重新起笔的动作，老师建议拆成两个笔画来写。",
          "measurable": {
            "direction": "左下 → 顿笔 → 提笔右上",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "折上去处的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:01:24",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:撇折:整笔",
          "segment": "整笔",
          "action": "撇折整笔：建议拆成两个笔画书写（撇 + 折，撇由重到轻再提笔走）；撇折后面通常接点、横、竖等笔画，轻重结合字才美观。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": "重→轻→重→轻",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:01:41",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:平捺",
      "name": "平捺",
      "font": "硬笔",
      "category": "捺画类",
      "tags": [
        "硬笔",
        "捺画",
        "走之旁"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 5,
        "title": "第5节 捺画训练",
        "t": "00:02:13",
        "evidence_count": 7
      },
      "actions": [
        {
          "id": "stroke:平捺:起笔",
          "segment": "起笔",
          "action": "起笔要有「右上的弧度」，平捺的弧度展现比长捺高得多，而且一定要展现出来；但弧度必须控制——一旦过头，前半部分全是弧度，整个走之旁就跑偏了，剩下的笔画全在替这个弧度做准备。",
          "measurable": {
            "direction": "起笔向右上方走一个较高的弧度",
            "entry": "落笔后向右上走弧度（弧度要比长捺高）",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "弧度不能写过头",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:17:00",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:平捺:行笔",
          "segment": "行笔",
          "action": "行笔与长捺一样，要注意弧度；从起笔的弧度往下转过来之后就要进入弯度，呈「包容」姿态——上边无论写成什么样，下边都要接住，不要给后面的笔画留下非常大的压力。",
          "measurable": {
            "direction": "从右上弧度转为下行弯度",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:17:18",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:平捺:收笔",
          "segment": "收笔",
          "action": "出捺角要随着弧度往上走，不能随着弧度直接往下走——很多同学写不出来，是因为不敢往上走，脑子里认为平捺就该直接下去。捺角出在准备往右上走的地方。有收有放：收回来一点即可，太放肆会把字整个撑开。",
          "measurable": {
            "direction": "随弧度上行时出捺角",
            "entry": null,
            "exit": "出捺角后回一点，不要放肆外扩",
            "pressure_profile": "捺角处加力显厚重",
            "speed_hint": null,
            "stop_points": [
              "捺角（准备往右上走的位置）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:21:12",
            "evidence_count": 11
          }
        }
      ]
    },
    {
      "id": "stroke:竖",
      "name": "竖",
      "font": "硬笔",
      "category": null,
      "tags": [],
      "certainty": "verbatim",
      "source": {
        "part": null,
        "title": null,
        "t": null,
        "evidence_count": 0
      },
      "actions": [
        {
          "id": "stroke:竖:收笔",
          "segment": "收笔",
          "action": "偏旁里的竖要用垂露竖，收笔时重一点、回锋收笔，笔杆要挺",
          "measurable": {
            "direction": "竖向下行",
            "entry": null,
            "exit": "收笔点落在字的最低处",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "收笔点（最低处）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 9,
            "title": "第9节 左偏旁及书写规律1",
            "t": "00:24:39",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:竖钩",
      "name": "竖钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "钩画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 7,
        "title": "第7节 勾画练习1",
        "t": "00:01:23",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:竖钩:起笔",
          "segment": "起笔",
          "action": "顿笔起笔，竖身要挺直（挺直腰）再往下走，不能一上来就带弯。",
          "measurable": {
            "direction": "竖向下行",
            "entry": "顿笔后稍向内收，再下行",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "顿笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:04:05",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖钩:行笔",
          "segment": "行笔",
          "action": "竖直下行，行笔的后半段要保持挺直。这是全笔画最难的一段：越往下半径越小，手腕活动余地越来越小，直挺挺地下来会觉得吃力。",
          "measurable": {
            "direction": "竖直向下",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "写直挺挺地下来手感吃力，可略借一点弯度让手有活动空间",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:08:09",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖钩:收笔",
          "segment": "收笔",
          "action": "出钩处要压住笔、顶下去再出钩，绝不能提笔。笔画重合处才是重笔，厚度才稳得住；下部写重写稳，字才压得住。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔后压笔出钩",
            "pressure_profile": "出钩处保持重笔，不可提笔变薄",
            "speed_hint": null,
            "stop_points": [
              "出钩处按笔的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:13:37",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖钩:转折",
          "segment": "转折",
          "action": "竖下来之后先「左带」一个很小的小弧度（往左下带一点），转变运笔方向，再转回来出钩。不能直接下来就甩出钩。",
          "measurable": {
            "direction": "竖直向下 → 往左下带一点 → 转回出钩",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "左带的小弧度处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:01:34",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:竖钩:钩",
          "segment": "钩",
          "action": "左带之后回到笔画中间，从这个笔画的中间出钩。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "下端向外出钩",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:11:46",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖钩:整笔",
          "segment": "整笔",
          "action": "三点合成一个竖钩：行笔后半段保持挺直 → 左带一个小弧度 → 压笔从中间出钩。",
          "measurable": {
            "direction": "竖直向下，左带后向左上出钩",
            "entry": "顿笔",
            "exit": "回锋收笔后压笔出钩",
            "pressure_profile": "起笔稍重 → 行笔平稳 → 出钩处保持重笔",
            "speed_hint": null,
            "stop_points": [
              "起笔顿点",
              "左带小弧度",
              "出钩顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:07:16",
            "evidence_count": 2
          }
        }
      ]
    },
    {
      "id": "stroke:竖撇",
      "name": "竖撇",
      "font": "硬笔",
      "category": "撇画类",
      "tags": [
        "硬笔",
        "撇画",
        "川字"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 3,
        "title": "第3节 竖画训练",
        "t": "00:31:54",
        "evidence_count": 21
      },
      "actions": [
        {
          "id": "stroke:竖撇:起笔",
          "segment": "起笔",
          "action": "川字里的竖撇先顿笔起笔，起笔之后就要开始往左走。",
          "measurable": {
            "direction": "顿笔后先向下略偏左",
            "entry": "右下顿笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:31:54",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖撇:行笔",
          "segment": "行笔",
          "action": "竖撇的行笔要含蓄：既想往左走，又稍微竖一点，半推半就地把它撇出去。不能一下直直撇出去，那样整个字就会歪了斜了。",
          "measurable": {
            "direction": "先略向左下，到 2/3 处转出去撇",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:35:43",
            "evidence_count": 15
          }
        },
        {
          "id": "stroke:竖撇:收笔",
          "segment": "收笔",
          "action": "收笔与长撇一样是出锋，但要缓慢出锋，保证笔画厚度扎实、稳固；绝不能甩笔。",
          "measurable": {
            "direction": "向左下收尖",
            "entry": null,
            "exit": "出锋（缓慢出锋）",
            "pressure_profile": "出锋前保持厚度",
            "speed_hint": "缓慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:13:26",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖撇:转折",
          "segment": "转折",
          "action": "到笔画 2/3 处由竖转撇：先往左走（略偏左），再转出去撇出去，这一段必须连贯，不能写成两截。",
          "measurable": {
            "direction": "由下行转为向左下撇出",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "笔画 2/3 处的转折点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:10:12",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:竖弯钩",
      "name": "竖弯钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "笔画",
        "勾画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 8,
        "title": "第8节 勾画训练2",
        "t": "00:02:00",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:竖弯钩:起笔",
          "segment": "起笔",
          "action": "顿笔起笔，起笔要重——老师把竖弯钩的写法概括为「重起重收」，笔画相接时可以不重、直接接进去，分开写就一定要重起。",
          "measurable": {
            "entry": "顿笔重入",
            "direction": "向下",
            "pressure_profile": "起笔重"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:05:54",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖弯钩:行笔",
          "segment": "行笔",
          "action": "竖本身要挺直但要带点弧度，行笔时把弧度展现出来；写成直来直去就会出现一点小波折，转折处失去生气。",
          "measurable": {
            "direction": "下行后转右上",
            "pressure_profile": "带弧度，不提不压",
            "entry": null,
            "exit": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:06:22",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:竖弯钩:转折",
          "segment": "转折",
          "action": "拐过来往右上走，先打出一个右上的趋势；转折处同样要有弧度，这样笔画才流畅。",
          "measurable": {
            "direction": "由下转向右上",
            "exit": "带弧度地拐向右上"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:05:57",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖弯钩:钩",
          "segment": "钩",
          "action": "在即将往右上行笔的时候再出勾（圆弧要回得去，回得去之后再往右上走，然后出勾），不要一拐下来就直接甩勾；出勾一定要重。",
          "measurable": {
            "exit": "顺势转右上出钩",
            "pressure_profile": null,
            "direction": null,
            "entry": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:04:56",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:竖弯钩:整笔",
          "segment": "整笔",
          "action": "竖弯钩的三个要点：两处弧度都要展现、何时出勾、重起重收。笔画相接时「重起」可忽略，分开写必须重起。",
          "measurable": null,
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:06:17",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:竖折",
      "name": "竖折",
      "font": "硬笔",
      "category": "折画类",
      "tags": [
        "硬笔",
        "折画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 6,
        "title": "第6节 折画训练",
        "t": "00:01:16",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:竖折:起笔",
          "segment": "起笔",
          "action": "竖折起笔：顿笔下来落笔起行，笔画要干净利落，有重有轻。",
          "measurable": {
            "direction": "上→下",
            "entry": "顿笔起笔",
            "exit": null,
            "pressure_profile": "有重有轻",
            "speed_hint": null,
            "stop_points": [
              "竖的起笔处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:10:24",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖折:行笔",
          "segment": "行笔",
          "action": "竖折行笔：分解开是一个收笔加一个起笔，连起来时轻轻一顿、两画相重合，衔接处显得稍重。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": "衔接处稍重",
            "speed_hint": null,
            "stop_points": [
              "两画重合处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:15:03",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:竖折:收笔",
          "segment": "收笔",
          "action": "竖折收笔：横画写完回锋收笔。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:12:51",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:竖折:转折",
          "segment": "转折",
          "action": "竖折转折：折向左下时要有明显的转笔动作（老师用毛笔的话术叫转锋），不能直接写下去。",
          "measurable": {
            "direction": "上 → 下 → 左下",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "转折处的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:11:01",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:竖折:整笔",
          "segment": "整笔",
          "action": "竖折整笔：两画都要有弧度；折的角度有两种（外对比下来往里收、往里边），要按字形来定，不能随心所欲。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 6,
            "title": "第6节 折画训练",
            "t": "00:15:28",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:竖折折钩",
      "name": "竖折折钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "笔画",
        "勾画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 8,
        "title": "第8节 勾画训练2",
        "t": "00:01:46",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:竖折折钩:起笔",
          "segment": "起笔",
          "action": "顿笔起笔，竖要直挺（稍微直一点）。",
          "measurable": {
            "entry": "顿笔",
            "direction": "向下（直挺）"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:18:52",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:竖折折钩:行笔",
          "segment": "行笔",
          "action": "竖直挺下来 → 折过来右上倾斜一点 → 再来一个顿笔，准备包容。",
          "measurable": {
            "direction": "向下 → 右上倾斜"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:18:58",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:竖折折钩:转折",
          "segment": "转折",
          "action": "折钩的「折」要展现出来：下来走横、上去再顿一下笔，多做一个笔势动作，再出个勾就更好看；直来直去会显得扎人、刺人。",
          "measurable": {
            "direction": "折向右下",
            "stop_points": [
              "折处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:26:36",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:竖折折钩:钩",
          "segment": "钩",
          "action": "钩要有弧度：按笔包下来、稍微往外扩一点，形成包容，字才更稳固；钩要厚重，出勾时重笔出勾，不能随手一摔就过去。",
          "measurable": {
            "exit": "按笔包下来、往外扩一点后出勾",
            "pressure_profile": "出勾重（厚重）"
          },
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:21:24",
            "evidence_count": 13
          }
        },
        {
          "id": "stroke:竖折折钩:整笔",
          "segment": "整笔",
          "action": "竖折折钩的五个注意要点：勾的弧度、横的斜度、竖的倾斜度因字而异、折钩的「折」要展现、钩要厚重。",
          "measurable": null,
          "certainty": "verbatim",
          "source": {
            "part": 8,
            "title": "第8节 勾画训练2",
            "t": "00:22:18",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:提",
      "name": "提",
      "font": "硬笔",
      "category": "提画类",
      "tags": [
        "硬笔",
        "提画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 4,
        "title": "第4节 撇画与提画训练",
        "t": "00:00:53",
        "evidence_count": 14
      },
      "actions": [
        {
          "id": "stroke:提:起笔",
          "segment": "起笔",
          "action": "右下顿笔起笔，随即把行笔方向由右下转到右上——与短撇正好相反（短撇由右到左，提由右到右上）。",
          "measurable": {
            "direction": null,
            "entry": "顿笔（笔尖停一下再提）",
            "exit": null,
            "pressure_profile": "重起",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:22:38",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:提:行笔",
          "segment": "行笔",
          "action": "行笔过程很直，不需要带弧度；行笔速度要略快一点，用快速的起笔和快速的提笔把线条的流畅感展现出来。",
          "measurable": {
            "direction": "向左上（向右上）提起",
            "entry": null,
            "exit": null,
            "pressure_profile": "重起→轻收",
            "speed_hint": "提起要快，收笔要稍快",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:21:04",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:提:收笔",
          "segment": "收笔",
          "action": "提笔出去，由重到轻非常明显；不要太急着甩出去。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "轻收",
            "pressure_profile": "重起轻收",
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:21:38",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:弯钩",
      "name": "弯钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "钩画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 7,
        "title": "第7节 勾画练习1",
        "t": "00:01:56",
        "evidence_count": 5
      },
      "actions": [
        {
          "id": "stroke:弯钩:起笔",
          "segment": "起笔",
          "action": "顿笔起笔，轻轻一顿就顺着顿笔的方向转下来。",
          "measurable": {
            "direction": "顿笔后转下来",
            "entry": "顿笔（轻轻一顿，顺着顿笔就下来）",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔处的顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:16:19",
            "evidence_count": 2
          }
        },
        {
          "id": "stroke:弯钩:行笔",
          "segment": "行笔",
          "action": "把行笔过程展现到一个弯度上。竖钩讲求挺势，弯钩讲求弯势；行笔要一气呵成、圆润饱满，中途不能停顿顿笔。",
          "measurable": {
            "direction": "下行转弯",
            "entry": null,
            "exit": null,
            "pressure_profile": "行笔不停顿、不忽轻忽重，保持圆润饱满",
            "speed_hint": "一气呵成，行笔过程中不能出现停顿",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:01:56",
            "evidence_count": 9
          }
        },
        {
          "id": "stroke:弯钩:收笔",
          "segment": "收笔",
          "action": "起收一致：起笔点一定要对准收笔点（钩尖），对不齐弯钩就会歪。找法是先顿笔下来走，对齐了再出勾。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "回锋收笔，钩尖与起笔点上下对齐",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔点与收笔点（钩尖）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:18:14",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:弯钩:转折",
          "segment": "转折",
          "action": "转折处要圆转，弧度要一直圆过来，不能写成死角；弯度要弯得有风度，骨要直。",
          "measurable": {
            "direction": "下行转折成弧",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:03:27",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:弯钩:钩",
          "segment": "钩",
          "action": "对齐之后再出钩，出钩要顺畅，不要考虑太多。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "对齐后回锋出钩",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:19:59",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:弯钩:整笔",
          "segment": "整笔",
          "action": "弯钩要同时做到三点：行笔顺畅无迟疑、笔画圆润饱满、起收一致。三点做到了，钩就跑不了。",
          "measurable": {
            "direction": "顿笔转下来，弯势下行",
            "entry": "顿笔",
            "exit": "回锋收笔，钩尖与起笔点对齐",
            "pressure_profile": "全程不停顿，圆润饱满",
            "speed_hint": "一气呵成，行进过程中不迟疑",
            "stop_points": [
              "起笔顿点",
              "钩尖（与起笔点对齐）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:21:04",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:卧钩",
      "name": "卧钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "钩画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 7,
        "title": "第7节 勾画练习1",
        "t": "00:31:51",
        "evidence_count": 4
      },
      "actions": [
        {
          "id": "stroke:卧钩:起笔",
          "segment": "起笔",
          "action": "卧钩（字幕作「work沟」）用轻起笔，不用顿笔；轻起笔转下来。",
          "measurable": {
            "direction": "轻起笔后转下来",
            "entry": "轻起笔（不用顿笔）",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:31:51",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:卧钩:行笔",
          "segment": "行笔",
          "action": "转下来之后要往右上走，弧度大约占 1/4 个圆。有了往右上的趋势再出钩，钩的圆润度才会更高。",
          "measurable": {
            "direction": "下行后转往右上",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:03:23",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:卧钩:收笔",
          "segment": "收笔",
          "action": "出完钩提起来即可，但全过程要缓慢、沉稳，笔不能甩。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "出钩后提笔",
            "pressure_profile": null,
            "speed_hint": "缓慢地出去、慢慢地走，沉住气",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:35:45",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:卧钩:转折",
          "segment": "转折",
          "action": "在「马上要往右上走」的那段弧度上转折，转上去之后再出钩；不能下来就出钩。",
          "measurable": {
            "direction": "弧度上行至右上",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "即将往右上走的弧度转折处"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:33:00",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:卧钩:钩",
          "segment": "钩",
          "action": "出钩方向是回来之后往左上收，与行笔方向正好相反。不能往外扩、往外放，那叫放飞自我。",
          "measurable": {
            "direction": "回来之后往左上收",
            "entry": null,
            "exit": "回锋收笔后往左上收出钩",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:33:38",
            "evidence_count": 4
          }
        }
      ]
    },
    {
      "id": "stroke:斜钩",
      "name": "斜钩",
      "font": "硬笔",
      "category": "钩画类",
      "tags": [
        "硬笔",
        "钩画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 7,
        "title": "第7节 勾画练习1",
        "t": "00:02:01",
        "evidence_count": 3
      },
      "actions": [
        {
          "id": "stroke:斜钩:起笔",
          "segment": "起笔",
          "action": "斜钩又叫割钩。起笔要顿笔，右下顿笔后再向右下行笔；这个角度不好掌握。",
          "measurable": {
            "direction": "右下顿笔后向右下行笔",
            "entry": "右下顿笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:02:01",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:斜钩:行笔",
          "segment": "行笔",
          "action": "顺着斜线走，弧度要饱满。不能出现波折或中途的小折——波折是因为手拉不过去；去掉小折，让笔画完整地写下来。",
          "measurable": {
            "direction": "向右下，顺着斜线",
            "entry": null,
            "exit": null,
            "pressure_profile": "弧度饱满，不忽轻忽重",
            "speed_hint": "顺着斜线连续写完，不中断、不折断",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:21:26",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:斜钩:收笔",
          "segment": "收笔",
          "action": "钩的高度要挺拔，不能写矮写胖。矮胖的第一印象就不好；出钩方向是最重要的一点。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "向上出钩，钩部高度挺拔",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:24:18",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:斜钩:转折",
          "segment": "转折",
          "action": "转变行笔方向时不能迟钝、犹豫，笔画必须流畅。",
          "measurable": {
            "direction": "右下斜势转向出钩方向",
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": "方向转变处不迟疑、不停顿",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:22:01",
            "evidence_count": 4
          }
        },
        {
          "id": "stroke:斜钩:钩",
          "segment": "钩",
          "action": "勾要往上出：可以笔直向上出，也可以往里收，但必须形成锐角。绝不能出钝角——一出一个钝角，整个字就散开了。",
          "measurable": {
            "direction": "向上出钩（或向内收）",
            "entry": null,
            "exit": "向上出钩成锐角，不可出钝角",
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 7,
            "title": "第7节 勾画练习1",
            "t": "00:23:01",
            "evidence_count": 9
          }
        }
      ]
    },
    {
      "id": "stroke:悬针竖",
      "name": "悬针竖",
      "font": "硬笔",
      "category": "竖画类",
      "tags": [
        "楷书",
        "竖画",
        "用笔",
        "硬笔"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 1,
        "title": "第1节 基础知识与点画训练",
        "t": "00:02:10",
        "evidence_count": 10
      },
      "actions": [
        {
          "id": "stroke:悬针竖:起笔",
          "segment": "起笔",
          "action": "起笔与垂露竖完全一致：右下顿笔，顿后向左上回锋，回来之后从中间往下行笔。",
          "measurable": {
            "direction": "向右下顿笔，向左上回锋后向下行笔",
            "entry": "右下顿笔，顿后向左上回锋回来",
            "exit": null,
            "pressure_profile": "轻顿",
            "speed_hint": null,
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:16:11",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:悬针竖:行笔",
          "segment": "行笔",
          "action": "中锋行笔。字幕以图中楷书悬针竖的用笔方法为例，指出其第三步即为中锋行笔；起笔与收笔步骤字幕没有念出，本节不补。",
          "measurable": {
            "direction": "竖向下行",
            "entry": null,
            "exit": null,
            "pressure_profile": "由重到轻，渐轻（不能太突然，要有过程）",
            "speed_hint": "顺带下来、很快；如果笔画轻得发飘就说明行笔速度慢了",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 1,
            "title": "第1节 基础知识与点画训练",
            "t": "00:02:06",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:悬针竖:收笔",
          "segment": "收笔",
          "action": "收笔出尖：提笔出去，快速提笔才能出尖；提笔容易飘（往左飘或往右飘），必须稳住。切忌甩笔——不管笔画正不正就为出尖把笔甩出去，那样笔画会不正、字会歪掉。由重到轻也不能太突然，必须有过程。",
          "measurable": {
            "direction": "向下出尖",
            "entry": null,
            "exit": "提笔出尖（快速提笔）",
            "pressure_profile": "由重到轻直至出尖",
            "speed_hint": "比平常书写稍快；速度太慢出不了尖",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 3,
            "title": "第3节 竖画训练",
            "t": "00:18:37",
            "evidence_count": 8
          }
        }
      ]
    },
    {
      "id": "stroke:长横",
      "name": "长横",
      "font": "硬笔",
      "category": "横画类",
      "tags": [
        "硬笔",
        "横画"
      ],
      "certainty": "inferred",
      "source": {
        "part": 2,
        "title": "第2节 横画练习",
        "t": "00:02:34",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:长横:起笔",
          "segment": "起笔",
          "action": "笔尖落纸先做一个向右下的顿笔，把这个顿笔动作完整做出来，再顺着笔势转向右上开始行笔。顿笔是书写动作而不是一个独立笔画，所以要轻、要完整，不能一说顿笔就使大劲。",
          "measurable": {
            "direction": "起笔处向右下顿笔，随后向右上行笔",
            "entry": "右下顿笔（轻顿）",
            "exit": null,
            "pressure_profile": "顿笔处略重，随即转笔；顿笔不能过重",
            "speed_hint": "起笔先顿住再转笔，动作放慢",
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:02:21",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:长横:行笔",
          "segment": "行笔",
          "action": "从顿笔处顺势往右上走，写出斜度；斜度之上再带出弧度，两者结合笔画才饱满。弧度要顺笔势自然带出，不是刻意改变用笔方向，也不必靠提笔。",
          "measurable": {
            "direction": "左→右上斜，与水平线形成夹角",
            "entry": null,
            "exit": null,
            "pressure_profile": "重起→轻行→重收",
            "speed_hint": "行笔加快会自然带出提笔；一直按着笔走会显得顿挫",
            "stop_points": [
              "起笔顿点即行笔起点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:02:34",
            "evidence_count": 11
          }
        },
        {
          "id": "stroke:长横:收笔",
          "segment": "收笔",
          "action": "行笔到末端先做一个右下顿笔，再把笔尖回锋往回收；笔尖一收回来就顺势提起。顿笔加回锋是为了把右上斜出去的笔画拉正、把字拽回来。",
          "measurable": {
            "direction": "行笔末端向右下顿笔，笔尖随即向左回勾",
            "entry": null,
            "exit": "回锋收笔（回勾一下后空提）",
            "pressure_profile": "收笔重",
            "speed_hint": "顿笔后回勾要稳，再慢慢提起",
            "stop_points": [
              "行笔末端的右下顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:02:36",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:长捺",
      "name": "长捺",
      "font": "硬笔",
      "category": "捺画类",
      "tags": [
        "硬笔",
        "捺画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 5,
        "title": "第5节 捺画训练",
        "t": "00:01:07",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:长捺:起笔",
          "segment": "起笔",
          "action": "落笔之后先往右上方走一个小弧度——这是起笔处必须展现的笔势动作。弧度要轻微但明显，不能写大，也不能只在纸面形态上假装有弧度。但要分场合：捺基本都是跟别的笔画直接接上出的（笔画相接时），这时起笔可以略微忽略——硬做顿笔起笔反而不好看；把捺单独拿出来写时，仍然要有弧度，只是这个弧度与正常捺画的弧度方向相反。",
          "measurable": {
            "direction": "起笔处向右上方走一个小弧",
            "entry": "落笔后向右上走小弧度（不是直着往下走）",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:02:07",
            "evidence_count": 12
          }
        },
        {
          "id": "stroke:长捺:行笔",
          "segment": "行笔",
          "action": "行笔要直弯结合：先在直的基础上略微加一点弧度；弧度展现之后，行笔的 2/3 处做一个稍微直一点的动作，到 1/3 处再把弧度展现出来。写得太直字会非常呆板，写得太弯字会非常软；直弯结合才叫刚柔并济，笔画有骨头有肉。",
          "measurable": {
            "direction": "斜向右下",
            "entry": null,
            "exit": null,
            "pressure_profile": "中后段渐重，到捺角处最重",
            "speed_hint": null,
            "stop_points": [
              "捺角（行笔 1/3 处）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:09:45",
            "evidence_count": 10
          }
        },
        {
          "id": "stroke:长捺:收笔",
          "segment": "收笔",
          "action": "收笔处出捺角——这是第二个笔势发力点，要慢慢地做出来：轻轻起笔、抬笔出锋，不是甩笔。捺角处要使点劲、显厚重，显出整个字的厚重感和力度感。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": "捺角处轻轻起笔抬笔出锋（不是甩笔）",
            "pressure_profile": "到捺角处最重，出锋后渐收",
            "speed_hint": "慢慢地做出这个动作",
            "stop_points": [
              "捺角（第二个发力点）"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:11:55",
            "evidence_count": 7
          }
        },
        {
          "id": "stroke:长捺:整笔",
          "segment": "整笔",
          "action": "整笔起、行、收三步都要有：起笔、收笔各是一个发力点，两个发力点都要能感觉到。如果一笔直直写下来，毫无疑问会发软。",
          "measurable": {
            "direction": null,
            "entry": null,
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": [
              "起笔发力点",
              "捺角发力点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 5,
            "title": "第5节 捺画训练",
            "t": "00:13:14",
            "evidence_count": 6
          }
        }
      ]
    },
    {
      "id": "stroke:长撇",
      "name": "长撇",
      "font": "硬笔",
      "category": "撇画类",
      "tags": [
        "硬笔",
        "撇画"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 4,
        "title": "第4节 撇画与提画训练",
        "t": "00:00:37",
        "evidence_count": 15
      },
      "actions": [
        {
          "id": "stroke:长撇:起笔",
          "segment": "起笔",
          "action": "右下顿笔起笔，顿得轻巧一点即可，然后直接往左下撇出去。",
          "measurable": {
            "direction": "顿笔后向左下撇出",
            "entry": "右下顿笔（轻轻一顿）",
            "exit": null,
            "pressure_profile": "起笔处稍重，随即渐轻",
            "speed_hint": null,
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:03:25",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:长撇:行笔",
          "segment": "行笔",
          "action": "由重到轻、渐轻渐细地撇出，弧度要顺笔势自然出来；找准方向后赶紧行笔，不要考虑太多。",
          "measurable": {
            "direction": "右上向左下",
            "entry": null,
            "exit": null,
            "pressure_profile": "重→轻，渐轻渐细",
            "speed_hint": "找准方向后迅速行笔，不要犹豫",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:06:09",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:长撇:收笔",
          "segment": "收笔",
          "action": "找准方向后缓慢提笔出锋；随着提笔，笔画的轻细程度自然展现出来。",
          "measurable": {
            "direction": "向左下收尖",
            "entry": null,
            "exit": "出锋（缓慢提笔）",
            "pressure_profile": "轻→更轻",
            "speed_hint": "提笔要缓慢",
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:07:20",
            "evidence_count": 6
          }
        },
        {
          "id": "stroke:长撇:整笔",
          "segment": "整笔",
          "action": "起笔顿笔 → 行笔渐轻渐细 → 收笔缓慢出锋，三个部分连成一条由重到轻的线；弧度不能写直。",
          "measurable": {
            "direction": "右上向左下",
            "entry": "右下顿笔",
            "exit": "缓慢提笔出锋",
            "pressure_profile": "由重到轻",
            "speed_hint": null,
            "stop_points": [
              "起笔处的右下顿点"
            ]
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:08:15",
            "evidence_count": 5
          }
        }
      ]
    },
    {
      "id": "stroke:直撇",
      "name": "直撇",
      "font": "硬笔",
      "category": "撇画类",
      "tags": [
        "硬笔",
        "撇画",
        "【老师强调】"
      ],
      "certainty": "verbatim",
      "source": {
        "part": 4,
        "title": "第4节 撇画与提画训练",
        "t": "00:16:53",
        "evidence_count": 11
      },
      "actions": [
        {
          "id": "stroke:直撇:行笔",
          "segment": "行笔",
          "action": "顿笔后直直撇出，笔势挺拔、略微直；弧度不要刻意弯，一弯下去字就不好看。",
          "measurable": {
            "direction": "由右上向左下，略直",
            "entry": "右下顿笔",
            "exit": null,
            "pressure_profile": null,
            "speed_hint": null,
            "stop_points": []
          },
          "certainty": "verbatim",
          "source": {
            "part": 4,
            "title": "第4节 撇画与提画训练",
            "t": "00:16:53",
            "evidence_count": 7
          }
        }
      ]
    },
    {
      "id": "stroke:左肩横",
      "name": "左肩横",
      "font": "硬笔",
      "category": "横画类",
      "tags": [
        "硬笔",
        "横画"
      ],
      "certainty": "inferred",
      "source": {
        "part": 2,
        "title": "第2节 横画练习",
        "t": "00:26:14",
        "evidence_count": 6
      },
      "actions": [
        {
          "id": "stroke:左肩横:起笔",
          "segment": "起笔",
          "action": "笔尖凌空入纸，触纸后轻轻往右上带出一个小尖头就直接走，不做右下顿笔。这是左肩横与长横、短横唯一的区别点。",
          "measurable": {
            "direction": "落纸后直接向右上",
            "entry": "凌空入笔，轻起笔（笔尖触纸即往右上带）",
            "exit": null,
            "pressure_profile": "最轻",
            "speed_hint": "触纸即走，不要停顿顿笔",
            "stop_points": [
              "起笔处的小尖头（无顿点）"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:26:14",
            "evidence_count": 5
          }
        },
        {
          "id": "stroke:左肩横:行笔",
          "segment": "行笔",
          "action": "行笔过程与短横一样，往右上走；因为起笔轻，笔尖带出去的是一个小尖头，形态轻巧干脆。",
          "measurable": {
            "direction": "左→右上",
            "entry": null,
            "exit": null,
            "pressure_profile": "轻",
            "speed_hint": "与短横一致",
            "stop_points": []
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:29:29",
            "evidence_count": 3
          }
        },
        {
          "id": "stroke:左肩横:收笔",
          "segment": "收笔",
          "action": "收笔过程与长横、短横完全一样：末端右下顿笔，再回锋收笔。",
          "measurable": {
            "direction": "行笔末端向右下顿笔，笔尖回勾",
            "entry": null,
            "exit": "回锋收笔",
            "pressure_profile": "收笔重",
            "speed_hint": "顿笔后回勾再提起",
            "stop_points": [
              "行笔末端的右下顿点"
            ]
          },
          "certainty": "inferred",
          "source": {
            "part": 2,
            "title": "第2节 横画练习",
            "t": "00:29:34",
            "evidence_count": 3
          }
        }
      ]
    }
  ]
};
