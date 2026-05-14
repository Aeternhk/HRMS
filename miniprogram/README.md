# HRMS 微信小程序

## 项目简介

HRMS 人力资源管理系统的小程序端，面向普通员工提供便捷的移动端自助服务。

## 功能模块

### 1. 登录模块
- 工号/手机号 + 密码登录
- 记住登录状态（7天）
- Token 自动刷新

### 2. 首页
- 快捷打卡入口
- 今日考勤状态展示
- 月度考勤统计
- 待办提醒

### 3. 打卡模块
- 上班打卡（GPS定位）
- 下班打卡
- 打卡记录查询
- 月度考勤统计

### 4. 投票模块
- 进行中投票列表
- 已结束投票列表
- 参与投票（单选/多选/评分）
- 投票结果查看

### 5. 个人中心
- 个人信息查看
- 工资条查询
- 请假/加班申请
- 修改密码
- 退出登录

## 技术栈

- **框架**: 微信小程序原生开发
- **UI组件**: TDesign 微信小程序组件库
- **状态管理**: 原生
- **网络请求**: 微信小程序 wx.request

## 项目结构

```
miniprogram/
├── pages/                    # 页面目录
│   ├── login/               # 登录页
│   ├── index/               # 首页
│   ├── clock/               # 打卡模块
│   │   └── record/          # 打卡记录
│   ├── vote/                # 投票模块
│   │   ├── detail/          # 投票详情
│   │   └── result/          # 投票结果
│   └── profile/             # 个人中心
│       ├── info/            # 个人信息
│       ├── salary/          # 工资条
│       ├── apply/           # 申请页面
│       └── settings/        # 设置
├── components/              # 公共组件
├── utils/                   # 工具函数
│   ├── request.js          # 网络请求
│   └── util.js             # 通用工具
├── images/                  # 图片资源
├── app.js                   # 小程序入口
├── app.json                 # 全局配置
├── app.wxss                 # 全局样式
├── sitemap.json            # 站点地图
└── project.config.json     # 项目配置
```

## 后端接口

小程序对接后端 Spring Boot 项目，使用以下接口：

| 接口 | 说明 |
|------|------|
| POST /api/mini/login | 登录 |
| POST /api/mini/clockIn | 打卡 |
| GET /api/mini/attendance/today | 今日考勤 |
| GET /api/mini/attendance/record | 打卡记录 |
| GET /api/mini/vote/list | 投票列表 |
| POST /api/mini/vote/submit | 提交投票 |
| GET /api/mini/user/profile | 个人信息 |
| GET /api/mini/salary/latest | 最新工资条 |
| POST /api/mini/leave/apply | 请假申请 |
| POST /api/mini/overtime/apply | 加班申请 |

## 开发说明

1. 使用微信开发者工具打开 `miniprogram` 目录
2. **构建 npm**：点击菜单「工具」→「构建 npm」
3. 勾选 **「将 JS 编译成 ES5」**
4. 修改 `app.js` 中的 `baseUrl` 为实际后端地址
5. 需要后端实现对应的接口

## 注意事项

- **必须在微信开发者工具中构建 npm**，否则 TDesign 组件无法正常使用
- 打卡功能初期使用模拟定位
- 工资条显示脱敏处理
- 仅支持账号密码登录（不支持微信一键登录）
- 无需消息推送功能
