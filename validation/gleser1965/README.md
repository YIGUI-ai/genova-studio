# Gleser, Cronbach & Rajaratnam (1965), Table 3

## Source and transcription

- Original scan: `/Users/yigui/Documents/GGG/gleser1965.pdf`, PDF page 15 (printed page 409), Table 3. Original PDF SHA-256: `e5c2eb5240992ac436f0a9b8843622194d0456ac2a60708ae9a246236cb3eef6`.
- Long-format transcription: `samples/gleser1965_table3.csv` with `person,item,judge,score`; 12 persons × 6 items × 2 judges = 144 observations.
- Controls from the printed table: person totals `20, 22, 27, 22, 41, 13, 17, 14, 26, 25, 29, 44`; item/judge column totals `29, 30, 33, 24, 9, 9, 35, 29, 29, 32, 21, 20`; grand total `300`. `SelfTest.java` asserts every total before running the engine.

## Numeric oracle

The `Gleser Cronbach Rajaratnam 1965 Table 3` self-test runs the CSV through `DataTable`, `Design.prepare`, and the existing GENOVA engine as a balanced `P × I × J` design. It checks all seven ANOVA effects (df, SS, MS), all seven variance components, and the two published D-study G coefficients. The source reports G = 0.657 when items and judges are random and G = 0.768 when the six items are fixed. The test runs both engine configurations and independently recomputes the two ratios from the paper's rounded variance components.

The source prints the item variance component as **0.471**. Its own Table 3 mean squares and Eq. 17 on PDF page 14 imply `(12.9333 − 1.4758 − 0.8667 + 0.6212) / 24 = 0.46717`; the engine gives approximately **0.46717**. This discrepancy exceeds rounding error, so the self-test uses the value derived from the published mean squares and explicitly records the printed 0.471 as inconsistent. It does not widen the tolerance to make the discrepancy disappear. The negative judge component (approximately −0.014) is asserted to remain negative.

Run from `/Users/yigui/Documents/GGG`:

```sh
python3 genova-gui/build.py
python3 genova-gui/validate.py
```

Latest run: 543 self-test checks passed, including 76 in this category. See `validation/selftest.log` and `validation/results.json`.

This is a **balanced, univariate** published example. It verifies the raw-data-to-GENOVA path for this design; it does not validate unbalanced urGENOVA or multivariate mGENOVA estimators.
