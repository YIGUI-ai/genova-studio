# 外部数据验证进度（更新至 2026-11-26）

目的：为 GENOVA Studio 的 GENOVA、urGENOVA 与 mGENOVA 功能建立可追溯的独立验证材料。本页记录 CiviConnections 论文、Jiang et al. OSF 数据、Gleser et al. 1965 表格和 Dr. John Vespa 提供的 BFI-2 数据的实际进展，以及尚未完成的复现工作。每项结论仅适用于已经测试的设计、输入和计算步骤。

## 1. CiviConnections：已发表数值基准

来源：Karatoprak Ersen、Lee 与 Yarbrough (2026)，*Reliability of Difference Scores Obtained From Nested Data Within a Multivariate Generalizability Theory Framework*，DOI [10.1177/00131644261451746](https://doi.org/10.1177/00131644261451746)。本地存放于 `external_sources/civiconnections_epm_2026/`，包含主文 PDF、multivariate supplement、univariate supplement 和 `SOURCE_MANIFEST.md`（含来源及 SHA-256）。

已完成：

- 从公开 supplements 中整理 pretest、posttest、sum 和 difference 的已发表 G-study variance components，以及由此得到的 covariance、D-study components、error variances、G/Φ 和 S/N 数值。
- `src/org/genova/gui/MComponentDStudy.java` 提供从已发表 components 到 multivariate/univariate D-study 结果的计算入口；`SelfTest.java` 有 51 项针对该论文数值的检查，包括 `PI:G:s` 的正确 divisor `485.985733`。CASMA report 中的 `206.924` 不用作此处的基准。
- 最近完整运行 `python3 genova-gui/validate.py`：自测共 543 项通过，其中上述 CiviConnections 检查 51 项通过；同一套 543 项也已用冻结候选 ZIP 解压出的 bundled Java runtime 全部跑通。结果见 `validation/selftest.log`、`validation/packaged_zip_selftest.log` 和 `validation/results.json`。

验证边界：这是**已发表中间数值到 D-study 结果**的验证。公开材料未提供 CiviConnections 原始逐行 observations、urGENOVA control/input files 或文中引用的完整 `.in.out` 文件，因此尚不能验证原始数据导入到 ANOVA、variance components 的端到端链路。Rabia Karatoprak Ersen 已说明她不处于可分享原始数据的位置；向 Won-Chan Lee/CASMA 询问档案或数据保管者的邮件已起草，本记录不假定邮件已发送或文件已取得。

## 2. Jiang et al. 2024：OSF 原始数据的子设计验证

来源：Jiang et al. (2024)，*Customizing Bayesian multivariate generalizability theory to mixed-format tests*，DOI [10.3758/s13428-024-02472-7](https://doi.org/10.3758/s13428-024-02472-7)，公开项目 [OSF wud3x](https://osf.io/wud3x/)。下载原件位于项目根目录 `wud3x-osfstorage-archive/`；验证脚本只读这些文件，并核对两份 CSV 的 SHA-256。

数据包含 533 名 persons、300 道二元 MCQ、6 个 FRT task，以及每个 FRT task 的两名 rater。论文联合设计为 `P × [I_MCQ ∪ (R:I)_FRT]`，其中 FRT 的 rater 嵌套在 item/task 内。论文报告的是 joint Bayesian/Stan mixed-format 模型结果。

已完成两个确定性选择的完整、平衡子表：

| 子表 | 选择规则 | 观察数 | 检查结果 |
| --- | --- | ---: | --- |
| FRT task 1，`P × R` | `resp_FRT.csv` 中 `item == 1`；533 人 × 2 名 rater | 1,066 | 14 项通过 |
| MCQ items 1–9，`P × I` | `resp_MCQ.csv` 中 `i1` 至 `i9`；533 人 × 9 题 | 4,797 | 14 项通过 |

`validation/osf_jiang_2024/validate_subdesigns.py` 对每张子表独立计算双因素 ANOVA 的 3 个 SS、3 个 MS、3 个 variance components 和 5 个 D-study 值，再调用 `OsfSubdesignRunner.java`，让 GENOVA Studio 的表格适配器和未改动的 jGENOVA 引擎处理同一批分数。共 28 项比较通过，绝对容差 `0.00006`（适配 jGENOVA 输出精度）。运行结果见 `validation/osf_jiang_subdesigns.log` 与 `validation/results.json`。OSF CSV 的空标题行号列仅在临时测试输入中移除；原始下载文件未改动。

**后续复现注记：Jiang 数据的完整论文复现尚未做。** 目前通过的两张子表是经典 ANOVA/G-theory 的独立数值检查，不能宣称重现论文 Table 4 的 Bayesian posterior estimates，也不能宣称当前 mGENOVA pilot 已覆盖 `P × [I_MCQ ∪ (R:I)_FRT]`。MCQ 在上述子测试中按数值 0/1 处理；论文把它作为 binary outcome 建模。现有两个 mGENOVA pilot 的设计分别是 `P × I` 和 `P × (I:R)`，并且每次最多 5,000 条观察，不支持该论文完整联合设计。

## 3. Gleser et al. 1965：Table 3 原始评分到 GENOVA 的验证

用户提供的原始扫描件为项目根目录 `gleser1965.pdf`。已将其 Table 3（PDF 第 15 页，印刷第 409 页）的 12 人 × 6 题 × 2 位评定者共 144 个评分逐格转录到 `samples/gleser1965_table3.csv`。自测先核对 12 个行总分、12 个列总分和总和 300，再经表格导入与既有 GENOVA 引擎运行平衡 `P × I × J` G-study。论文的 7 项 ANOVA df、SS、MS 和 7 项方差分量均逐项检查；评定者负方差分量约 −0.014 未被截零。随机题目与评定者下的 G = 0.657，以及固定六道题下的 G = 0.768，也分别与 Studio 的 D-study 输出核对。此类别 76 项检查通过。

**原文内部差异：**论文把 item 方差分量印为 0.471，但按论文 Eq. 17 和 Table 3 的均方应为约 0.46717，Studio 得到后者。测试将推导值作为该项期望值，并在 `validation/gleser1965/README.md` 保存算式、来源哈希及验证边界。此例为**平衡单变量**设计，不能代替 urGENOVA 的 unbalanced 或 mGENOVA 的 multivariate 端到端验证。

## 4. Vispoel et al. BFI-2：mGENOVA P × I 端到端外部验证

Dr. John Vespa 于 2026-11-26 提供了 346 人的 BFI-2 Extraversion 数据、变量代码本，以及 Vispoel et al. (2023) *Applying Multivariate Generalizability Theory to Psychological Assessments* 的教学补充材料。数据有 24 个完整题目反应列：3 个分量表各 4 题，在两个 occasions 分别测量；无缺失，所有值均为 1–5。原文件保留在仓库外，只读使用。`validation/external_sources/vispoel_bfi2_2023/SOURCE_MANIFEST.md` 记录三个文件的 SHA-256；因尚未在此记录原始文件的再分发书面许可，仓库不包含 raw CSV、codebook 或 supplement。

已完成 Occasion 1 的 `P × I` 验证：346 persons × 3 fixed variables × 4 items = 4,152 observations，低于当前 mGENOVA pilot 的 5,000-observation 限制。验证脚本将 wide 数据仅在临时目录转换为 Studio 所需的四列长表，并调用生产版 `MpXiDesign` 和 `MpXiEngine`。同时用独立双因素 ANOVA 公式重新计算各分量表的 person、item、person × item 分量及 person 间协方差。

结果为 **47/47 PASS**：Studio 与独立计算在全精度下相符；以下 G-study 结果也与补充材料的 mGENOVA 表格在其三位小数精度下相符。

同一套 47 项随后用冻结 `v1.0.0` ZIP 解压出的 application JAR 和 bundled OpenJDK 25.0.2 直接执行，再次 **47/47 PASS**。冻结 ZIP SHA-256 为 `71e931521e6555e45d97f88691f84c2304bca27d6406b74325f0c758e5cce8ce`，其中 JAR SHA-256 为 `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`。发布件结果保存在 `validation/vispoel_bfi2_2023/packaged_v1.0.0_results.json`。

| 分量表 | person | item | person × item |
| --- | ---: | ---: | ---: |
| Assertiveness | 0.571813 | 0.037186 | 0.693547 |
| Energy Level | 0.411481 | 0.099619 | 0.796817 |
| Sociability | 0.808440 | 0.217898 | 0.823769 |

person 协方差分别为 Assertiveness–Energy `0.288987`、Assertiveness–Sociability `0.502597`、Energy–Sociability `0.414156`，对应补充材料的 `0.289`、`0.503`、`0.414`。四题 D-study 也由精确分量独立复算并逐项通过，包括 composite `G = 0.879012`、`Phi = 0.863008`。

补充材料后面的 lavaan ULS 表不是原始 mGENOVA 的精确 oracle，因而仅作上下文，不与 ANOVA 入口混为同一硬基准。例如该表将 Assertiveness Phi 报为 `0.757`，而精确 ANOVA 分量给出 `0.757874`，按三位小数为 `0.758`。验证记录保留此差异，不通过放宽容差掩盖它。

**验证边界：**这项结果验证的是 Occasion 1、完整数据、3 个固定变量、每变量 4 个嵌套题目的 `P × I` raw-data-to-result 路径。它不验证两 occasion 的 `P × I × O`、Studio 的 `P × (I:R)`、missing cells、其他估计量，或与原 mGENOVA 可执行文件的无限制等价。完整两 occasion 数据有 8,304 个 observation，而且 `P × I × O` 当前未实现；不能把这 47 项通过表述为两面设计已经支持或验证。

## 5. QCIT / Q-CCIIT：官方页面与数据获取结论

QCIT（Quality of Care for Infants and Toddlers）是 Mathematica 的官方量表；旧名称为 Q-CCIIT（Quality of Caregiver-Child Interactions for Infants and Toddlers）。官方页面是 [Mathematica QCIT](https://www.mathematica.org/solutions/qcit)。页面提供 Fact Sheet、Introduction、FAQ、Pricing 和 publications 等材料；评分表、用户指南和培训材料通过 QCIT Store 提供，属于量表使用资料，不是研究原始 dataset。

目前找到的可公开下载材料包括：

- [Q-CCIIT 2015 psychometric report](https://acf.gov/sites/default/files/documents/opre/measuring_the_quality_of_caregiver_child_interactions_for_infants_and.pdf)；
- [该报告附录](https://www.mathematica.org/-/media/publications/pdfs/earlychildhood/q-cciit_report_appen.pdf)；
- Mathematica 的 [QCIT FAQ](https://www.mathematica.org/solutions/qcit/-/media/d670422f68244fb79952a2d3ba6d97fd.ashx)。

这些文件包含已发表的描述统计、可靠性、G-study/D-study 结果和分析说明，但没有发现 2012 psychometric field test 的逐行 observation-level 原始评分、完整 codebook 或原始 urGENOVA `.in/.out` 文件的公开下载入口。报告明确说明该 G-study 设计不平衡，涉及 classroom、cycle、item 和 observer，并使用过专门处理不平衡设计的 Brennan 软件；因此原始数据会是验证 urGENOVA 的高价值材料，但不能从报告表格反推出完整 raw dataset。

Baby FACES 2018 的 QCIT/Q-CCIIT 变量由 [ICPSR 37666](https://www.childandfamilydataarchive.org/cfda/archives/cfda/studies/37666) 归档，但该研究数据被标为 restricted，不能把它当作 2012 Q-CCIIT psychometric field-test 数据，也不能直接下载用于当前验证。

数据请求对象应先联系 **QCIT@mathematica-mpr.com**，请求匿名 observation-level field-test data、codebook，以及如仍保存的 urGENOVA input/output 或分析说明；若 Mathematica 不再保管，应请其转介 ACF/OPRE 或数据档案管理员。请求邮件草稿已在本对话中准备，尚未发送。

## 6. 待做的复现与验证

1. **Jiang 原论文模型复现。** 先核对 OSF Stan/R 文件的依赖、版本、路径、参数化、先验及运行配置；再在隔离工作目录运行论文的 joint mixed-format 模型，对照论文 Table 4 的 posterior means、SD 和区间。当前归档中的 `runstan.R` 引用未随包出现的 `mergecodebymanual1025.stan`，`generate_stan_data.R` 引用未在该脚本定义的 `scoreDataCode_new` / `outcomeDataCode_new`；需先查明是否有遗漏的版本或必要的手工改名步骤。不要未经核对就替换原文件或把某个 Stan 版本视为论文最终版本。
2. **GENOVA Studio 完整设计支持。** 若决定实现 `P × [I_MCQ ∪ (R:I)_FRT]`，需先明确 Bayesian 二元 MCQ 模型与经典 mGENOVA 估计的目标量，再设计适配器、统计引擎和独立比较。不同估计方法的数值不应被要求逐位相等。
3. **CiviConnections 端到端复现。** 若取得获准分享的匿名逐行数据及 urGENOVA input/output，固定数据版本、设计映射和各阶段期望结果，再比较导入、ANOVA、components、D-study 和最终报告。现有公开论文数值仍可作为下游计算基准。
4. **扩大 OSF 子设计覆盖。** 可在现有独立计算脚本基础上检查 FRT 的其余 task、更多 MCQ 子表及完整平衡 FRT 的 `P × (R:I)` 结构；每项新增测试都需先确认其设计与程序入口一致。
5. **Vispoel 两面设计。** 若决定扩展 mGENOVA，应把 `P × I × O` 作为新的明确设计实现和验证，不要把 Occasion 2 简单拼入现有 `P × I`。需先确定 crossed/nested facet 语义、协方差结构、D-study 目标和 observation-limit 策略，再以补充材料第 2.2 节的 components 为外部基准。

## 7. 复跑方式与证据

从 `/Users/yigui/Documents/GGG` 执行：

```sh
python3 genova-gui/build.py
python3 genova-gui/validate.py
```

只运行 Jiang 子设计检查：

```sh
python3 genova-gui/validation/osf_jiang_2024/validate_subdesigns.py
```

只运行 Vispoel BFI-2 Occasion 1 `P × I` 外部验证：

```sh
python3 genova-gui/validation/vispoel_bfi2_2023/validate_bfi2_pxi.py \
  "/path/to/RE__[External]_Data_Request_for_Validating_mGENOVA_in_GENOVA_Studio" \
  --output genova-gui/validation/vispoel_bfi2_2023/results.json
```

`validate.py` 在项目根目录存在 `wud3x-osfstorage-archive/` 时运行 OSF 检查。冻结投稿候选的结果仍为：543 项常规自测在开发 JDK、打包目录的 bundled runtime，以及冻结候选 ZIP 解压出的 bundled runtime 上均全部通过（含 Gleser 76 项与 CiviConnections 51 项）；Jiang 子设计另有 28 项通过；原发行包的 95 个原件哈希检查通过。候选 ZIP SHA-256 为 `71e931521e6555e45d97f88691f84c2304bca27d6406b74325f0c758e5cce8ce`，其中 JAR SHA-256 为 `9883b68ddcf9211d2957d09cf300bc231fc130142a648e0186ceefecb4e85c6f`。运行平台为 Apple Silicon macOS 27.2.1，bundled OpenJDK 25.0.2。2026-11-26 新增的 Vispoel BFI-2 `P × I` 外部验证在当前开发 JAR 和冻结 ZIP 的 bundled runtime/JAR 上均为 47/47 PASS；该重跑不改变冻结 artifact 的字节或既有证据边界。完整记录见 `validation/BUNDLED_RUNTIME_RELEASE_VALIDATION_2026-11-18.md`、`validation/release_candidate_manifest.json`、`validation/vispoel_bfi2_2023/results.json` 和 `validation/vispoel_bfi2_2023/packaged_v1.0.0_results.json`；各验证目录的 README 记录具体数据和测试解释。此进度文件记录的是上述已执行结果，不代表未来代码或数据版本自动保持相同结论。
