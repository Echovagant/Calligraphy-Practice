/**
 * 指针能力探测。
 *
 * 判定「真压感」不能靠「pressure 落在 (0,1) 开区间」——鼠标按下时规范规定
 * pressure = 0.5，正好落在开区间里。改用：**压力取值出现过不属于
 * {0, 0.5, 1} 的值 → 判定为真压感**（笔的力度是连续变化的）。
 *
 * 超时（sampleMs）内没有指针事件 → 判为 touch（纯触屏设备）。
 *
 * 不依赖 DOM 库：target 只需要有 addEventListener / removeEventListener，
 * 因此可以在单元测试里传一个手写的假事件目标。
 */

export const CAPABILITY_MODE = {
  PEN: 'pen',
  POINTER: 'pointer',
  TOUCH: 'touch'
};

/** 鼠标/无压感设备只会出现这三个压力值 */
const NON_REAL_PRESSURES = new Set([0, 0.5, 1]);

export function detectPointerCapability(target, { sampleMs = 250 } = {}) {
  return new Promise((resolve) => {
    if (!target || typeof target.addEventListener !== 'function') {
      resolve({ mode: CAPABILITY_MODE.TOUCH, hasPressure: false, pointerType: '' });
      return;
    }

    const pressures = new Set();
    let pointerType = '';
    let settled = false;
    let timer = null;

    const finish = () => {
      if (settled) return;
      settled = true;
      if (timer !== null) clearTimeout(timer);
      target.removeEventListener('pointerdown', onDown);
      target.removeEventListener('pointermove', onMove);

      const hasPressure = [...pressures].some((value) => !NON_REAL_PRESSURES.has(value));
      const mode =
        pointerType === 'pen'
          ? CAPABILITY_MODE.PEN
          : hasPressure
            ? CAPABILITY_MODE.POINTER
            : CAPABILITY_MODE.TOUCH;

      resolve({ mode, hasPressure, pointerType });
    };

    const collect = (event) => {
      if (pointerType === '') pointerType = event.pointerType || 'mouse';
      const pressure = Number(event.pressure);
      pressures.add(Number.isFinite(pressure) ? pressure : 0);
    };

    function onDown(event) {
      collect(event);
      // 以 pointerdown 为起点重新计时，留出采样笔压变化的时间
      if (timer !== null) clearTimeout(timer);
      timer = setTimeout(finish, sampleMs);
    }

    function onMove(event) {
      collect(event);
    }

    target.addEventListener('pointerdown', onDown);
    target.addEventListener('pointermove', onMove);
    timer = setTimeout(finish, sampleMs);
  });
}
