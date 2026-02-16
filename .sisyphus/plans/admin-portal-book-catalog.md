# Admin Portal 书目编目功能完善

## TL;DR

> **Quick Summary**: 为 admin-portal 构建完整的书目管理界面，包括 AdminLayout 布局框架、书目列表/新增/编辑/删除功能，以及退出登录功能。
> 
> **Deliverables**:
> - AdminLayout 组件（侧边栏 + Header + 内容区）
> - BookListView 书目列表页面（分页、搜索、操作按钮）
> - BookFormView 书目表单页面（新增/编辑共用）
> - 退出登录按钮及功能
> - API 层完善（createBook, updateBook, deleteBook）
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Task 1 (Layout) → Task 2 (API) → Task 3 (List) → Task 4 (Form) → Task 5 (Delete) → Task 6 (Logout)

---

## Context

### Original Request
完善 admin-portal 书目编目、退出登录等功能

### Interview Summary
**Key Discussions**:
- 需要全部6项功能：书目列表、详情/编辑、新增、删除、退出登录、整体布局

**Research Findings**:
- 后端 API 已存在：GET/POST/PUT/DELETE `/api/admin/books`
- Book 实体：id, title, author, edition, status, volumes (嵌套)
- 前端已有：LoginView.vue 登录流程、user.ts store、request.ts 拦截器
- api/book.ts 部分实现（listBooks, getBook），缺少 create/update/delete

### Metis Review
**Identified Gaps** (addressed):
- 分页/搜索参数：后端已支持 page, pageSize, keyword, status 参数
- 表单验证规则：使用 Element Plus FormRules，必填 title/author
- 错误处理：复用现有 request.ts 的 ElMessage 模式
- 删除确认：使用 ElMessageBox.confirm

---

## Work Objectives

### Core Objective
构建完整的书目管理后台，让管理员可以查看、创建、编辑、删除书籍，并能安全退出系统

### Concrete Deliverables
- `src/layouts/AdminLayout.vue` - 后台布局框架
- `src/views/book/BookListView.vue` - 书目列表页
- `src/views/book/BookFormView.vue` - 书目表单页（新增/编辑）
- `src/api/book.ts` - 补全 CRUD API 函数
- 路由配置更新
- Header 退出按钮

### Definition of Done
- [ ] 登录后跳转到 AdminLayout 包裹的主页面
- [ ] 侧边栏显示"书目管理"导航项
- [ ] 书目列表支持分页、关键词搜索
- [ ] 可新增书籍并保存成功
- [ ] 可编辑现有书籍并保存成功
- [ ] 可删除书籍（带确认弹窗）
- [ ] 点击退出按钮清除 token 并跳转登录页

### Must Have
- 遵循现有宋朝古风审美风格
- 复用现有 Element Plus 组件
- 使用 Pinia store 管理状态
- 复用 request.ts 拦截器（自动携带 token）

### Must NOT Have (Guardrails)
- 不做用户管理功能
- 不做 Dashboard/统计分析
- 不做 Excel 导入导出
- 不做批量操作
- 不做图片上传
- 不做审计日志
- 不引入新的 UI 库
- 不修改后端 API

---

## Verification Strategy

> **UNIVERSAL RULE: ZERO HUMAN INTERVENTION**
>
> ALL verification by agent using Playwright or curl.

### Test Decision
- **Infrastructure exists**: NO (no test config in admin-portal)
- **Automated tests**: None
- **Framework**: none

### Agent-Executed QA Scenarios (MANDATORY — ALL tasks)

每个任务的验收通过 Playwright 自动化验证，具体场景在各 TODO 中详述。

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: AdminLayout 布局组件
└── Task 2: API 层完善

Wave 2 (After Wave 1):
├── Task 3: BookListView 列表页
├── Task 6: 退出登录功能 (Header 内)

Wave 3 (After Wave 2):
├── Task 4: BookFormView 表单页
└── Task 5: 删除功能 (集成到列表页)

Critical Path: Task 1 → Task 3 → Task 4
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 3, 4, 5, 6 | 2 |
| 2 | None | 3, 4, 5 | 1 |
| 3 | 1, 2 | 4, 5 | 6 |
| 4 | 3 | None | 5 |
| 5 | 3 | None | 4 |
| 6 | 1 | None | 3 |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents |
|------|-------|-------------------|
| 1 | 1, 2 | category="visual-engineering" + category="quick" |
| 2 | 3, 6 | category="visual-engineering" |
| 3 | 4, 5 | category="visual-engineering" |

---

## TODOs

- [ ] 1. AdminLayout 布局组件

  **What to do**:
  - 创建 `src/layouts/AdminLayout.vue`
  - 使用 Element Plus el-container/el-aside/el-header/el-main
  - 侧边栏宽度 220px，包含 el-menu 导航
  - Header 高度 60px，右侧预留退出按钮位置
  - 内容区使用 `<router-view />` 渲染子路由
  - 更新 router/index.ts：将 home 和 book 路由包裹在 AdminLayout 下
  - 导航项：书目管理 (/books)
  - 采用现有宋朝古风配色 (#2c3e50 深墨背景, #cfb078 泥金高亮)

  **Must NOT do**:
  - 不做 Dashboard 页面
  - 不做用户管理导航
  - 不做可折叠侧边栏

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 涉及 Vue 组件和 UI 布局设计
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: 布局设计和 Element Plus 组件使用

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Tasks 3, 4, 5, 6
  - **Blocked By**: None

  **References**:
  - `admin-portal/src/views/LoginView.vue:105-158` - 宋朝古风 CSS 样式参考（配色、字体、阴影）
  - `admin-portal/src/router/index.ts` - 现有路由配置结构
  - `admin-portal/src/App.vue` - 根组件结构
  - Element Plus Layout: https://element-plus.org/en-US/component/container.html
  - Element Plus Menu: https://element-plus.org/en-US/component/menu.html

  **Acceptance Criteria**:

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Layout renders with sidebar and header
    Tool: Playwright (playwright skill)
    Preconditions: Dev server running on localhost:5173, user logged in (token in localStorage)
    Steps:
      1. Set localStorage.setItem('token', 'test-token')
      2. Navigate to: http://localhost:5173/
      3. Wait for: .el-aside visible (timeout: 5s)
      4. Assert: .el-aside width is 220px
      5. Assert: .el-header visible
      6. Assert: .el-main contains router-view content
      7. Screenshot: .sisyphus/evidence/task-1-layout-structure.png
    Expected Result: Layout shows sidebar, header, and main content area
    Evidence: .sisyphus/evidence/task-1-layout-structure.png

  Scenario: Sidebar navigation works
    Tool: Playwright (playwright skill)
    Preconditions: User logged in, on home page
    Steps:
      1. Navigate to: http://localhost:5173/
      2. Wait for: .el-menu visible
      3. Assert: Menu contains "书目管理" text
      4. Click: Menu item with text "书目管理"
      5. Wait for: URL contains /books (timeout: 3s)
      6. Screenshot: .sisyphus/evidence/task-1-nav-click.png
    Expected Result: Clicking nav item navigates to books page
    Evidence: .sisyphus/evidence/task-1-nav-click.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add AdminLayout with sidebar and header`
  - Files: `src/layouts/AdminLayout.vue`, `src/router/index.ts`

---

- [ ] 2. API 层完善 (book.ts)

  **What to do**:
  - 在 `src/api/book.ts` 添加缺失的 API 函数:
    - `createBook(data: Partial<Book>)` → POST /api/admin/books
    - `updateBook(id: number, data: Partial<Book>)` → PUT /api/admin/books/:id
    - `deleteBook(id: number)` → DELETE /api/admin/books/:id
  - 确保类型定义与后端 Book 实体匹配
  - 复用现有 request 实例

  **Must NOT do**:
  - 不修改 request.ts 拦截器逻辑
  - 不添加新的 HTTP 库

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 API 函数添加，单文件修改
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Tasks 3, 4, 5
  - **Blocked By**: None

  **References**:
  - `admin-portal/src/api/book.ts` - 现有 API 定义（listBooks, getBook）
  - `admin-portal/src/utils/request.ts` - axios 实例和拦截器
  - `content-service/src/main/java/com/literature/content/controller/BookController.java` - 后端 API 端点定义
  - `content-service/src/main/java/com/literature/content/entity/Book.java` - Book 实体字段

  **Acceptance Criteria**:

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: TypeScript compiles without errors
    Tool: Bash
    Preconditions: Node modules installed
    Steps:
      1. cd admin-portal && npx vue-tsc --noEmit
      2. Assert: exit code is 0
      3. Assert: no type errors in output
    Expected Result: TypeScript compilation succeeds
    Evidence: Command output captured

  Scenario: API functions are exported correctly
    Tool: Bash
    Preconditions: None
    Steps:
      1. grep -E "export function (createBook|updateBook|deleteBook)" admin-portal/src/api/book.ts
      2. Assert: All 3 functions found
    Expected Result: createBook, updateBook, deleteBook exported
    Evidence: grep output
  ```

  **Commit**: YES
  - Message: `feat(api): add createBook, updateBook, deleteBook functions`
  - Files: `src/api/book.ts`

---

- [ ] 3. BookListView 书目列表页

  **What to do**:
  - 创建 `src/views/book/BookListView.vue`
  - 使用 el-table 显示书目列表，列：书名、作者、版本、状态、操作
  - 顶部搜索栏：关键词输入框 + 搜索按钮 + 新增按钮
  - 分页组件：el-pagination，默认每页 10 条
  - 操作列：编辑按钮、删除按钮
  - 调用 listBooks API 获取数据
  - 添加路由 `/books` 到 router/index.ts

  **Must NOT do**:
  - 不做高级筛选（日期范围、多选状态）
  - 不做表格列排序
  - 不做批量选择

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 涉及复杂 UI 组件和交互逻辑
  - **Skills**: [`frontend-ui-ux`, `playwright`]
    - `frontend-ui-ux`: Element Plus Table/Pagination 组件使用
    - `playwright`: QA 验证

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 6)
  - **Blocks**: Tasks 4, 5
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `admin-portal/src/api/book.ts` - listBooks API 函数
  - `admin-portal/src/views/LoginView.vue:105-220` - CSS 风格参考
  - `content-service/src/main/java/com/literature/content/controller/BookController.java:36-55` - 后端分页参数 (page, pageSize, keyword, status)
  - Element Plus Table: https://element-plus.org/en-US/component/table.html
  - Element Plus Pagination: https://element-plus.org/en-US/component/pagination.html

  **Acceptance Criteria**:

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Book list displays with pagination
    Tool: Playwright (playwright skill)
    Preconditions: Dev server running, user logged in, backend running with test data
    Steps:
      1. Navigate to: http://localhost:5173/books
      2. Wait for: .el-table visible (timeout: 10s)
      3. Assert: Table has columns: 书名, 作者, 版本, 状态, 操作
      4. Assert: .el-pagination visible
      5. Screenshot: .sisyphus/evidence/task-3-book-list.png
    Expected Result: Book list table renders with pagination
    Evidence: .sisyphus/evidence/task-3-book-list.png

  Scenario: Search filters book list
    Tool: Playwright (playwright skill)
    Preconditions: Books exist in database
    Steps:
      1. Navigate to: http://localhost:5173/books
      2. Wait for: .el-table visible
      3. Fill: input[placeholder*="搜索"] → "论语"
      4. Click: button containing "搜索"
      5. Wait for: table data refresh (network idle)
      6. Assert: Table rows filtered (or empty if no match)
      7. Screenshot: .sisyphus/evidence/task-3-search.png
    Expected Result: Search filters results
    Evidence: .sisyphus/evidence/task-3-search.png

  Scenario: Empty state when no books
    Tool: Playwright (playwright skill)
    Preconditions: Database has no books (or filtered to empty)
    Steps:
      1. Navigate to: http://localhost:5173/books
      2. Fill: search input → "不存在的书名xyz"
      3. Click: search button
      4. Wait for: network idle
      5. Assert: Empty state message visible OR table shows 0 rows
      6. Screenshot: .sisyphus/evidence/task-3-empty.png
    Expected Result: Empty state displayed appropriately
    Evidence: .sisyphus/evidence/task-3-empty.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add BookListView with search and pagination`
  - Files: `src/views/book/BookListView.vue`, `src/router/index.ts`

---

- [ ] 4. BookFormView 书目表单页

  **What to do**:
  - 创建 `src/views/book/BookFormView.vue`
  - 共用新增/编辑场景，通过路由参数 id 区分
  - 表单字段：书名(必填)、作者(必填)、版本、状态(下拉选择)
  - 使用 el-form + el-form-item + FormRules 验证
  - 编辑模式：根据 id 调用 getBook API 预填数据
  - 保存按钮：调用 createBook 或 updateBook API
  - 取消按钮：返回列表页
  - 添加路由：`/books/new` 和 `/books/:id/edit`

  **Must NOT do**:
  - 不做 volumes 嵌套编辑（仅展示 count）
  - 不做图片上传
  - 不做富文本编辑器

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 涉及表单组件、验证逻辑、路由参数处理
  - **Skills**: [`frontend-ui-ux`, `playwright`]
    - `frontend-ui-ux`: Element Plus Form 组件和验证
    - `playwright`: QA 验证

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 5)
  - **Blocks**: None
  - **Blocked By**: Task 3

  **References**:
  - `admin-portal/src/views/LoginView.vue:12-51` - el-form 使用示例
  - `admin-portal/src/api/book.ts` - createBook, updateBook, getBook API
  - `content-service/src/main/java/com/literature/content/entity/Book.java` - 字段定义 (title, author, edition, status)
  - Element Plus Form: https://element-plus.org/en-US/component/form.html
  - Element Plus Select: https://element-plus.org/en-US/component/select.html

  **Acceptance Criteria**:

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Create new book successfully
    Tool: Playwright (playwright skill)
    Preconditions: Dev server and backend running, user logged in
    Steps:
      1. Navigate to: http://localhost:5173/books/new
      2. Wait for: form visible
      3. Fill: input[name="title"] or first input → "测试书籍"
      4. Fill: input[name="author"] or second input → "测试作者"
      5. Fill: input[name="edition"] → "初版"
      6. Select: status dropdown → "draft" or first option
      7. Click: submit/save button
      8. Wait for: navigation to /books OR success message
      9. Assert: success message visible (ElMessage)
      10. Screenshot: .sisyphus/evidence/task-4-create-success.png
    Expected Result: Book created and redirected to list
    Evidence: .sisyphus/evidence/task-4-create-success.png

  Scenario: Edit existing book
    Tool: Playwright (playwright skill)
    Preconditions: Book with id=1 exists
    Steps:
      1. Navigate to: http://localhost:5173/books/1/edit
      2. Wait for: form visible with pre-filled data
      3. Assert: title input has value (not empty)
      4. Clear and Fill: title input → "修改后的书名"
      5. Click: save button
      6. Wait for: success message
      7. Screenshot: .sisyphus/evidence/task-4-edit-success.png
    Expected Result: Book updated successfully
    Evidence: .sisyphus/evidence/task-4-edit-success.png

  Scenario: Form validation shows errors
    Tool: Playwright (playwright skill)
    Preconditions: On create form
    Steps:
      1. Navigate to: http://localhost:5173/books/new
      2. Leave all required fields empty
      3. Click: save button
      4. Assert: validation error messages visible
      5. Screenshot: .sisyphus/evidence/task-4-validation.png
    Expected Result: Validation errors shown for required fields
    Evidence: .sisyphus/evidence/task-4-validation.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add BookFormView for create and edit`
  - Files: `src/views/book/BookFormView.vue`, `src/router/index.ts`

---

- [ ] 5. 删除书目功能

  **What to do**:
  - 在 BookListView 的操作列添加删除按钮
  - 点击删除显示 ElMessageBox.confirm 确认弹窗
  - 确认后调用 deleteBook API
  - 成功后刷新列表
  - 失败显示错误提示

  **Must NOT do**:
  - 不做批量删除
  - 不做软删除（后端决定）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 在现有组件上添加简单功能
  - **Skills**: [`playwright`]
    - `playwright`: QA 验证删除流程

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 4)
  - **Blocks**: None
  - **Blocked By**: Task 3

  **References**:
  - `admin-portal/src/views/book/BookListView.vue` - 在此文件添加删除功能
  - `admin-portal/src/api/book.ts:deleteBook` - 删除 API
  - Element Plus MessageBox: https://element-plus.org/en-US/component/message-box.html

  **Acceptance Criteria**:

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Delete book with confirmation
    Tool: Playwright (playwright skill)
    Preconditions: Book exists in list
    Steps:
      1. Navigate to: http://localhost:5173/books
      2. Wait for: table visible with at least 1 row
      3. Note: row count before delete
      4. Click: delete button on first row
      5. Wait for: confirmation dialog visible
      6. Assert: dialog contains book title or "确认删除"
      7. Click: confirm button in dialog
      8. Wait for: success message AND table refresh
      9. Assert: row count decreased OR book removed
      10. Screenshot: .sisyphus/evidence/task-5-delete-success.png
    Expected Result: Book deleted after confirmation
    Evidence: .sisyphus/evidence/task-5-delete-success.png

  Scenario: Cancel delete keeps book
    Tool: Playwright (playwright skill)
    Preconditions: Book exists
    Steps:
      1. Navigate to: http://localhost:5173/books
      2. Note: row count
      3. Click: delete button
      4. Wait for: confirmation dialog
      5. Click: cancel button
      6. Assert: dialog closed
      7. Assert: row count unchanged
      8. Screenshot: .sisyphus/evidence/task-5-delete-cancel.png
    Expected Result: Book not deleted when cancelled
    Evidence: .sisyphus/evidence/task-5-delete-cancel.png
  ```

  **Commit**: YES (groups with Task 3 if done together)
  - Message: `feat(admin): add delete book with confirmation`
  - Files: `src/views/book/BookListView.vue`

---

- [ ] 6. 退出登录功能

  **What to do**:
  - 在 AdminLayout Header 右侧添加退出按钮
  - 显示当前用户名（如果有）+ 退出图标/文字
  - 点击调用 userStore.logoutAction()
  - 跳转到 /login 页面
  - 确保清除 localStorage 中的 token

  **Must NOT do**:
  - 不做用户头像
  - 不做下拉菜单（个人设置等）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 UI 添加和事件绑定
  - **Skills**: [`playwright`]
    - `playwright`: QA 验证退出流程

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 3)
  - **Blocks**: None
  - **Blocked By**: Task 1

  **References**:
  - `admin-portal/src/layouts/AdminLayout.vue` - 在 Header 中添加退出按钮
  - `admin-portal/src/stores/user.ts:30-33` - logoutAction 函数
  - `admin-portal/src/router/index.ts:27-34` - 路由守卫（验证退出后跳转）

  **Acceptance Criteria**:

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Logout clears token and redirects
    Tool: Playwright (playwright skill)
    Preconditions: User logged in with token in localStorage
    Steps:
      1. Set localStorage.token = 'valid-token'
      2. Navigate to: http://localhost:5173/
      3. Wait for: AdminLayout visible
      4. Assert: logout button visible in header
      5. Click: logout button
      6. Wait for: navigation to /login (timeout: 5s)
      7. Evaluate: localStorage.getItem('token')
      8. Assert: token is null or empty
      9. Screenshot: .sisyphus/evidence/task-6-logout.png
    Expected Result: Token cleared and redirected to login
    Evidence: .sisyphus/evidence/task-6-logout.png

  Scenario: Cannot access admin page after logout
    Tool: Playwright (playwright skill)
    Preconditions: Token cleared
    Steps:
      1. Clear localStorage
      2. Navigate to: http://localhost:5173/books
      3. Wait for: redirect to /login
      4. Assert: URL is /login
      5. Screenshot: .sisyphus/evidence/task-6-guard.png
    Expected Result: Redirected to login (route guard works)
    Evidence: .sisyphus/evidence/task-6-guard.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add logout button in header`
  - Files: `src/layouts/AdminLayout.vue`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(admin): add AdminLayout with sidebar and header` | AdminLayout.vue, router/index.ts | Playwright |
| 2 | `feat(api): add createBook, updateBook, deleteBook functions` | api/book.ts | vue-tsc |
| 3 | `feat(admin): add BookListView with search and pagination` | BookListView.vue, router/index.ts | Playwright |
| 4 | `feat(admin): add BookFormView for create and edit` | BookFormView.vue, router/index.ts | Playwright |
| 5 | `feat(admin): add delete book with confirmation` | BookListView.vue | Playwright |
| 6 | `feat(admin): add logout button in header` | AdminLayout.vue | Playwright |

---

## Success Criteria

### Verification Commands
```bash
cd admin-portal && npm run build  # Expected: Build successful
cd admin-portal && npx vue-tsc --noEmit  # Expected: No type errors
```

### Final Checklist
- [ ] AdminLayout 正确渲染侧边栏和 Header
- [ ] 书目列表显示分页和搜索功能
- [ ] 可成功创建新书目
- [ ] 可成功编辑现有书目
- [ ] 删除带确认弹窗且生效
- [ ] 退出清除 token 并跳转登录页
- [ ] 所有页面符合宋朝古风审美
- [ ] 无 TypeScript 编译错误
