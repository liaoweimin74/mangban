# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\mangban-e2e.spec.ts >> 用户管理 >> 用户列表显示 - 有数据且单元格非空
- Location: e2e\mangban-e2e.spec.ts:184:3

# Error details

```
TimeoutError: page.waitForURL: Timeout 10000ms exceeded.
=========================== logs ===========================
waiting for navigation to "**/dashboard" until "load"
============================================================
```

# Page snapshot

```yaml
- generic [ref=e4]:
  - generic [ref=e5]:
    - generic [ref=e6]: MB
    - heading "盲板管理系统" [level=1] [ref=e8]
    - paragraph [ref=e9]: 石化工厂 · 安全作业管理平台
  - generic [ref=e10]:
    - generic [ref=e11]:
      - generic [ref=e12]: 用户名
      - textbox "用户名" [ref=e16]:
        - /placeholder: 请输入用户名
        - text: admin
    - generic [ref=e17]:
      - generic [ref=e18]: 密码
      - textbox "密码" [ref=e22]:
        - /placeholder: 请输入密码
        - text: admin123
    - button "登 录" [ref=e30] [cursor=pointer]
  - paragraph [ref=e32]: 安全第一 · 规范作业
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test';
  2   | 
  3   | const BASE = 'http://127.0.0.1:5173';
  4   | 
  5   | /** Helper: 登录 */
  6   | async function loginAsAdmin(page) {
  7   |   await page.goto(`${BASE}/login`, { waitUntil: 'domcontentloaded' });
  8   |   if (page.url().includes('dashboard')) return;
  9   |   await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 5000 });
  10  |   await page.evaluate(() => {
  11  |     const un = document.querySelector('input[placeholder="请输入用户名"]') as HTMLInputElement;
  12  |     un.focus(); un.value = ''; document.execCommand('insertText', false, 'admin');
  13  |     un.dispatchEvent(new Event('input', { bubbles: true }));
  14  |     const pw = document.querySelector('input[placeholder="请输入密码"]') as HTMLInputElement;
  15  |     pw.focus(); pw.value = ''; document.execCommand('insertText', false, 'admin123');
  16  |     pw.dispatchEvent(new Event('input', { bubbles: true }));
  17  |   });
  18  |   await page.click('button');
> 19  |   await page.waitForURL('**/dashboard', { timeout: 10000 });
      |              ^ TimeoutError: page.waitForURL: Timeout 10000ms exceeded.
  20  | }
  21  | 
  22  | /** Helper: 导航到侧边栏子菜单 */
  23  | async function navigateTo(page, menuText: string) {
  24  |   await page.evaluate(() => {
  25  |     const items = document.querySelectorAll('.el-menu-item,.el-sub-menu__title');
  26  |     for (let i = 0; i < items.length; i++) {
  27  |       if (items[i].textContent?.includes('系统管理')) { (items[i] as HTMLElement).click(); break; }
  28  |     }
  29  |   });
  30  |   await page.waitForTimeout(500);
  31  |   await page.evaluate((text) => {
  32  |     const items = document.querySelectorAll('.el-menu-item');
  33  |     for (let i = 0; i < items.length; i++) {
  34  |       if (items[i].textContent?.trim() === text) { (items[i] as HTMLElement).click(); return; }
  35  |     }
  36  |   }, menuText);
  37  | }
  38  | 
  39  | /** Helper: 获取表格单元格文本列表 */
  40  | async function getTableRows(page): Promise<string[][]> {
  41  |   return page.evaluate(() => {
  42  |     const rows = document.querySelectorAll('.el-table__body-wrapper tr.el-table__row');
  43  |     return Array.from(rows).map(tr =>
  44  |       Array.from(tr.querySelectorAll('td .cell')).map(td => td.textContent?.trim() || '')
  45  |     );
  46  |   });
  47  | }
  48  | 
  49  | /** Helper: 点击按钮 by 文本 */
  50  | async function clickButton(page, text: string) {
  51  |   await page.evaluate((t) => {
  52  |     const btns = document.querySelectorAll('button');
  53  |     for (let i = 0; i < btns.length; i++) {
  54  |       if (btns[i].textContent?.includes(t)) { (btns[i] as HTMLElement).click(); return; }
  55  |     }
  56  |   }, text);
  57  | }
  58  | 
  59  | /** Helper: 填写 el-dialog 内 input by label */
  60  | async function fillDialogInputs(page, values: Record<string, string>) {
  61  |   await page.evaluate((vals) => {
  62  |     const dialog = document.querySelector('.el-dialog');
  63  |     if (!dialog) return;
  64  |     const labels = dialog.querySelectorAll('.el-form-item__label');
  65  |     labels.forEach((label) => {
  66  |       const text = label.textContent?.trim() || '';
  67  |       if (vals[text] !== undefined) {
  68  |         const formItem = label.closest('.el-form-item');
  69  |         const input = formItem?.querySelector('.el-input__inner') as HTMLInputElement;
  70  |         if (input) { input.value = vals[text]; input.dispatchEvent(new Event('input', { bubbles: true })); }
  71  |       }
  72  |     });
  73  |   }, values);
  74  | }
  75  | 
  76  | // ==================== 认证模块 ====================
  77  | test.describe('认证模块', () => {
  78  |   test('登录成功跳转首页', async ({ page }) => {
  79  |     await loginAsAdmin(page);
  80  |     await expect(page).toHaveURL(/dashboard/);
  81  |     await expect(page.locator('.el-menu').first()).toBeVisible();
  82  |   });
  83  | 
  84  |   test('错误密码登录失败', async ({ page }) => {
  85  |     await page.goto(`${BASE}/login`);
  86  |     await page.evaluate(() => {
  87  |       const un = document.querySelector('input[placeholder="请输入用户名"]') as HTMLInputElement;
  88  |       un.focus(); un.value = ''; document.execCommand('insertText', false, 'admin');
  89  |       un.dispatchEvent(new Event('input', { bubbles: true }));
  90  |       const pw = document.querySelector('input[placeholder="请输入密码"]') as HTMLInputElement;
  91  |       pw.focus(); pw.value = ''; document.execCommand('insertText', false, 'wrong');
  92  |       pw.dispatchEvent(new Event('input', { bubbles: true }));
  93  |     });
  94  |     await page.click('button');
  95  |     await page.waitForTimeout(2000);
  96  |     await expect(page).toHaveURL(/login/);
  97  |   });
  98  | });
  99  | 
  100 | // ==================== 角色管理模块 ====================
  101 | test.describe('角色管理', () => {
  102 |   test.beforeEach(async ({ page }) => {
  103 |     await loginAsAdmin(page);
  104 |     await navigateTo(page, '角色管理');
  105 |     await page.waitForURL('**/system/role');
  106 |     await page.waitForTimeout(1000);
  107 |   });
  108 | 
  109 |   test('角色列表显示 - 有数据且单元格非空', async ({ page }) => {
  110 |     const rows = await getTableRows(page);
  111 |     expect(rows.length).toBeGreaterThan(0);
  112 |     const firstRow = rows[0];
  113 |     expect(firstRow[0]).toBeTruthy(); // 角色名称
  114 |     expect(firstRow[1]).toBeTruthy(); // 角色编码
  115 |   });
  116 | 
  117 |   test('创建角色', async ({ page }) => {
  118 |     const roleName = `TEST_ROLE_${Date.now()}`;
  119 |     const roleCode = `TEST_${Date.now()}`;
```