// 本文件由字幕抽取产物聚合生成，请勿手改。
// 来源：软笔楷书全套教程（B站 BV1A9sbzTEUT）21 个分P 的字幕抽取，
// 每条都带出处（分P + 时间码），原话可在 .local/ink-knowledge/ 对应文件里回查。
// 生成脚本：.local/tools/aggregate-extraction.mjs

export { STROKE_DICTIONARY } from './strokeDictionary.js';
export { STRUCTURE_RULES } from './structureRules.js';
export { COMMON_ERRORS } from './commonErrors.js';
export { INK_TERMS } from './inkTerms.js';
export { WRITING_PRINCIPLES } from './writingPrinciples.js';
import { STROKE_DICTIONARY } from './strokeDictionary.js';

/**
 * 按笔画名查笔画条目。
 * 支持精确名、复合名的基名（「撇（斜撇、小短撇）」→ 撇）、以及包含匹配。
 */
export function findStroke(name) {
  if (!name) return null;
  const target = String(name).trim();
  return (
    STROKE_DICTIONARY.strokes.find((s) => s.name === target) ??
    STROKE_DICTIONARY.strokes.find((s) => s.name.split('（')[0].trim() === target) ??
    STROKE_DICTIONARY.strokes.find((s) => s.name.includes(target)) ??
    null
  );
}
