# 随包分发的字体

本目录下的字体文件随本项目一同分发，均采用 **SIL Open Font License, Version 1.1**（OFL-1.1）授权。
OFL-1.1 允许自由使用、嵌入、修改与再分发（含商业用途），
**唯一硬性要求是：分发时必须附带版权声明与许可证全文** —— 因此每个字体的许可证都在本目录里。

| 字体 | 用途 | 族名 | 许可证 |
|---|---|---|---|
| 霞鹜文楷 LXGW WenKai | 楷书档 | `CP-WenKai` | [LICENSE-lxgw-wenkai.txt](LICENSE-lxgw-wenkai.txt) |
| 龙藏体 Long Cang | 行书档 | `CP-LongCang` | [LICENSE-long-cang.txt](LICENSE-long-cang.txt) |
| 柳建毛草 Liu Jian Mao Cao | 草书档 | `CP-MaoCao` | [LICENSE-liu-jian-mao-cao.txt](LICENSE-liu-jian-mao-cao.txt) |

## 字形来源与版权声明

- 霞鹜文楷：Copyright 2021-2023 LXGW；字形基于 The Klee Project Authors 的 Klee（Copyright 2020 The Klee Project Authors）
- 龙藏体：Copyright 2018 The LongCang Project Authors
- 柳建毛草：Copyright 2018 The Liu Jian Mao Cao Project Authors

各字体的完整声明见上面链接的许可证文件首段。

## 子集化说明

这三个字体已用 fontTools 子集化到本应用实际用到的字符集
（汉字笔顺数据覆盖的 9680 字 + ASCII + 常用标点）。
子集化只裁掉用不到的字符，不改动任何字形数据。

分片字体（龙藏体、柳建毛草）保留了原始的 `unicode-range` 划分，
浏览器按需下载对应分片，因此 `@font-face` 里每条规则都带 `unicode-range`。

## 修改字体的限制

按 OFL-1.1 第 3 条：若基于这些字体制作衍生字体，未经原版权方书面授权，
字体名称中不得使用其保留名称（Reserved Font Names）。
本目录下的文件均为原样分发，未做任何字形修改。
