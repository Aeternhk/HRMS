# HRMS - 人力资源管理系统

面向生产制造企业的人力资源管理系统，涵盖员工全生命周期管理、考勤薪资核算、数据决策分析、员工互动投票等核心业务功能。

## 双端架构

| 端 | 面向用户 | 定位 |
|----|---------|------|
| PC管理端 | HR专员、人事主管、部门管理者 | 完整的人力资源管理控制台 |
| 微信小程序端 | 全体普通员工 | 移动自助服务平台（打卡/查询/互动） |

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| PC前端 | Vue 3 + TDesign Vue Next + TypeScript + Pinia + ECharts | 3.x / 2.x / 5.x |
| 构建工具 | Vite | 5.x |
| 后端 | Spring Boot + MyBatis-Plus + JWT | 2.7.x / 3.5.x |
| 数据库 | MySQL 8.0 (InnoDB / utf8mb4) | 8.0 |
| 小程序 | 微信原生 + TDesign MiniProgram | - |

## 项目结构

```
HRMS/
├── frontend/              # PC管理端（Vue3 + TDesign + TypeScript）
│   └── src/
│       ├── api/modules/   # API模块化（auth/employee/salary/vote等10+模块）
│       ├── pages/         # 页面（17个路由页面）
│       ├── layouts/       # 布局（侧边栏+顶栏+主内容区）
│       ├── router/        # 路由配置
│       ├── store/         # Pinia状态管理
│       └── utils/         # 工具函数
│
├── miniprogram/           # 微信小程序端（原生 + TDesign MiniProgram）
│   └── pages/
│       ├── login/         # 登录
│       ├── index/         # 首页（待办+考勤+公告）
│       ├── clock/         # 多时段打卡
│       ├── vote/          # 投票（列表/详情/结果）
│       ├── notice/        # 公告
│       └── profile/       # 个人中心（资料/工资条/申请）
│
├── backend/               # 后端服务（Spring Boot + MyBatis-Plus）
│   └── src/main/java/com/hrms/
│       ├── controller/    # 控制器层（11个Controller + 小程序Controller）
│       ├── mapper/        # 数据访问层
│       ├── entity/        # 实体类（16+张表）
│       ├── common/        # 通用类（Result统一响应）
│       ├── config/        # 配置类（CORS/MyBatis/JWT/Security）
│       └── security/      # 认证工具
│
└── sql/                   # SQL脚本（初始化/迁移/测试数据）
```

## 核心功能

### PC管理端
- **数据看板** — 在职人数、入离职趋势、部门分布、考勤异常等可视化
- **员工管理** — 完整CRUD、批量Excel导入导出、工号自动生成、状态流转
- **部门管理** — 树形结构、人数统计、部门编码
- **考勤管理** — 多时段打卡（上午/下午/加班）、迟到早退判定、请假/加班审批
- **薪资管理** — 完整收支项目、自动计算净工资、批量发放
- **投票管理** — 单选/多选/评分/排序四种类型、防刷票、结果导出
- **公告通知** — 发布/撤回、状态管理
- **系统管理** — 角色/用户/日志/字典/招聘

### 微信小程序端
- **打卡** — 多时段大按钮打卡、实时状态回显
- **投票** — 列表浏览、参与投票、查看结果
- **我的** — 个人资料、出勤统计、工资条、请假/加班申请

## 角色权限

| 角色 | 职责范围 |
|------|----------|
| 超级管理员 | 系统配置、用户管理、全部模块权限 |
| 人事主管 | 员工管理、考勤审批、薪资发放、报表查看、投票管理 |
| 部门主管 | 本部门人员查看、本部门考勤审批 |
| 普通员工 | 个人信息查看、考勤打卡、参与投票 |

> RBAC权限模型，`login_type` 区分终端权限：1=仅小程序、2=仅PC、3=双端

## 快速开始

### 环境要求
- JDK 8+
- MySQL 8.0
- Node.js 16+
- 微信开发者工具（小程序端）

### 后端启动
```bash
cd backend
# 配置 src/main/resources/application.yml 中的数据库连接
mvn spring-boot:run
```

### PC前端启动
```bash
cd frontend
npm install
npm run dev
```

### 小程序端
使用微信开发者工具打开 `miniprogram/` 目录，配置 AppID 后即可预览。

### 数据库初始化
```bash
mysql -u root -p < sql/init_database.sql
# 多时段打卡迁移（按需）
mysql -u root -p < sql/migration_multi_shift_20260409.sql
```

## API规范

- 统一响应格式：`{ "code": 200, "message": "success", "data": {...} }`
- PC端接口前缀：`/api/*`
- 小程序端接口前缀：`/mini/*`
- 认证方式：JWT Token（Bearer Header）

## 开发约定

- 后端 Java 8 环境，**禁止使用 `var` 关键字**
- 前端使用 TDesign 作用域插槽 `#colKey`，禁止 `h()` 渲染函数
- API 调用使用 `src/api/modules/` 下的模块化封装，禁止硬编码 mock
- 软删除字段 `deleted`，查询条件需兼容 NULL 值
- 考勤日期字段使用 `DATE()` 函数包装查询
