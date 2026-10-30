import { ref } from 'vue';

/**
 * 专注模式：练习页把全站顶栏与页脚收起来，画布独占整屏。
 *
 * 用模块级 ref 而不是 store —— 这是纯界面状态，跟练习数据无关，
 * 而且练习页与 App.vue 要共享同一份开关。
 */
const immersive = ref(false);

export function useImmersiveMode() {
  return { immersive };
}
