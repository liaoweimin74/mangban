# Verification Report

> 此檔案在 planning 階段建立，實際驗證需在 apply 完成後執行 `/opsx-verify`。
> 以下是 apply 完成後的驗證計劃與預期結果。

**Change**: `common-business-components`
**Verified at**: apply 完成後執行
**Verifier**: Sisyphus

---

## 1. Structural Validation (`openspec validate --all --json`)

- [ ] 全數 items `"valid": true`

**預期**：所有 artifacts 在生成時已符合 schema 結構。

---

## 2. Task Completion (`tasks.md`)

- [ ] 所有 `- [ ]` 已變為 `- [x]`

**預期**：apply 完成後全部 tasks 完成。

---

## 3. Delta Spec Sync State

| Capability | Sync 狀態 | 備註 |
|---|---|---|
| search-table | N/A | 新 capability，無既有 spec |
| form-builder | N/A | 新 capability，無既有 spec |
| reference-picker | N/A | 新 capability，無既有 spec |

---

## 4. Design / Specs Coherence Spot Check

| 抽樣項 | design 描述 | specs 對應 | 差距 |
|---|---|---|---|
| SearchTable fetchApi | 外部傳入，組件內部自動調用 | specs/search-table Requirement 6 定義 | 一致 |
| FormBuilder v-model | modelValue/update:modelValue | specs/form-builder Requirement 2 定義 | 一致 |
| ReferencePicker 選擇模式 | single/multiple | specs/reference-picker Requirement 4 定義 | 一致 |

**漂移警告**（非阻塞）：無

---

## 5. Implementation Signal

- [ ] Worktree 內無未 staged 的檔案
- [ ] 所有相關 commit 已推送

**Commit 範圍**：apply 完成後確認

---

## 6. Front-Door Routing Leak Detector（warning,非阻塞）

- [ ] 無檔案洩漏

**預期**：docs/superpowers/specs/ 不存在或為空（本次設計輸出在 change 目錄內）。

---

## 7. Deferred Manual Dogfood vs Automated Test Equivalence

plan.md 無 `[~]` 標記的 deferred task，本節空白。

---

## Overall Decision

- [ ] ✅ PASS — 可進入 finishing-a-development-branch 與 archive
- [ ] ⚠️ PASS WITH WARNINGS
- [ ] ❌ FAIL

**下一步**：執行 `/opsx-apply` 開始實現，完成後重跑 verify。