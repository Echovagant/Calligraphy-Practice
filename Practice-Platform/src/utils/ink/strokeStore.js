/**
 * 笔画栈（撤销 / 重做）+ 序列化。
 *
 * 存的是**骨架**（中心线采样点 + 每点宽度），不是轮廓多边形——
 * 轮廓是渲染时从骨架派生的，撤销 / 重绘 / 缩放都不必处理 path 数据。
 *
 * **一笔之内不可撤销** —— 与毛笔语义一致（写坏这一笔就是废了，重写）。
 *
 * 纯状态，不依赖 DOM。
 */

export const STROKE_STORE_VERSION = 1;

function isValidStroke(stroke) {
  return Boolean(stroke) && Array.isArray(stroke.points) && stroke.points.length > 0;
}

export function createStrokeStore({ strokes = [], redoStack = [] } = {}) {
  let list = strokes.filter(isValidStroke);
  let redo = redoStack.filter(isValidStroke);

  return {
    get strokes() {
      return list;
    },
    get redoStack() {
      return redo;
    },
    get canUndo() {
      return list.length > 0;
    },
    get canRedo() {
      return redo.length > 0;
    },
    get size() {
      return list.length;
    },

    /** 落一笔。空笔画不入栈；落笔后清空重做栈。 */
    push(stroke) {
      if (!isValidStroke(stroke)) return false;
      list.push(stroke);
      redo = [];
      return true;
    },

    /** 撤销一笔，返回被撤销的笔画；没有可撤销时返回 null */
    undo() {
      if (list.length === 0) return null;
      const stroke = list.pop();
      redo.push(stroke);
      return stroke;
    },

    /** 重做，返回被恢复的笔画；没有可重做时返回 null */
    redo() {
      if (redo.length === 0) return null;
      const stroke = redo.pop();
      list.push(stroke);
      return stroke;
    },

    clear() {
      list = [];
      redo = [];
    },

    /** 序列化骨架（体积小，够重建整幅画） */
    serialize() {
      return { version: STROKE_STORE_VERSION, strokes: list };
    },

    /** 从序列化数据恢复；数据损坏时保持现状并返回 false */
    restore(payload) {
      let data = payload;
      if (typeof payload === 'string') {
        try {
          data = JSON.parse(payload);
        } catch {
          return false;
        }
      }
      if (!data || !Array.isArray(data.strokes)) return false;
      list = data.strokes.filter(isValidStroke);
      redo = [];
      return true;
    }
  };
}
