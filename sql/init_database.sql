-- =============================================
-- HRMS 人力资源管理系统 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 说明: 本脚本与 Java Entity 类 100% 对齐
--       执行前请确保数据库 hrms 已创建
-- =============================================

-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS `hrms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `hrms`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. sys_user 用户/员工表
--    对应 Entity: SysUser.java
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`        varchar(50)  NOT NULL                COMMENT '工号',
  `password`        varchar(255) NOT NULL DEFAULT ''     COMMENT '密码(BCrypt)',
  `role_id`         bigint       DEFAULT NULL            COMMENT '角色ID',
  `login_type`      int          DEFAULT 3               COMMENT '登录类型: 1=仅小程序 2=仅后台 3=两者皆可',
  `status`          int          DEFAULT 1               COMMENT '状态: 1=正常 0=禁用',
  `last_login_time` datetime     DEFAULT NULL           COMMENT '最后登录时间',
  `last_login_ip`   varchar(50)  DEFAULT NULL           COMMENT '最后登录IP',
  -- 基本信息字段
  `name`            varchar(50)  NOT NULL DEFAULT ''     COMMENT '姓名',
  `gender`          varchar(10)  DEFAULT NULL           COMMENT '性别',
  `nation`          varchar(20)  DEFAULT NULL           COMMENT '民族',
  `id_card`         varchar(18)  DEFAULT NULL           COMMENT '身份证号',
  `birthday`        date         DEFAULT NULL           COMMENT '出生日期',
  `marital_status`  varchar(20)  DEFAULT NULL           COMMENT '婚姻状况',
  -- 联系方式字段
  `phone`           varchar(20)  NOT NULL DEFAULT ''     COMMENT '手机号',
  `emergency_contact` varchar(50) DEFAULT NULL          COMMENT '紧急联系人',
  `emergency_phone` varchar(20)  DEFAULT NULL           COMMENT '紧急联系电话',
  `native_place`    varchar(100) DEFAULT NULL           COMMENT '籍贯',
  `address`         varchar(255) DEFAULT NULL           COMMENT '现住址',
  `email`           varchar(100) DEFAULT NULL           COMMENT '邮箱',
  -- 教育背景字段
  `education`       varchar(20)  DEFAULT NULL           COMMENT '学历',
  `graduate_school` varchar(100) DEFAULT NULL           COMMENT '毕业院校',
  `major`           varchar(100) DEFAULT NULL           COMMENT '所学专业',
  -- 工作信息字段
  `entry_date`      date         DEFAULT NULL           COMMENT '入职日期',
  `positive_date`   date         DEFAULT NULL           COMMENT '转正日期',
  `contract_start_date` date     DEFAULT NULL           COMMENT '合同开始日期',
  `contract_end_date`   date     DEFAULT NULL           COMMENT '合同结束日期',
  `department_id`   bigint       DEFAULT NULL           COMMENT '所属部门ID',
  `position`        varchar(50)  DEFAULT NULL           COMMENT '岗位',
  `rank`            varchar(20)  DEFAULT NULL           COMMENT '职级',
  `employee_status` int          DEFAULT 1               COMMENT '员工状态: 1=正式 2=试用 3=实习 4=离职 5=退休',
  `base_salary`     decimal(10,2) DEFAULT NULL          COMMENT '基本工资',
  `photo`           varchar(255) DEFAULT NULL           COMMENT '照片路径',
  `avatar`          varchar(255) DEFAULT NULL           COMMENT '头像路径',
  -- 审计字段
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         int          DEFAULT 0               COMMENT '逻辑删除: 0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户/员工表';

-- 初始用户数据 (密码均为 BCrypt("123456"))
-- admin: 超级管理员 | loginType=3(两端都可)
-- HR001: 人事主管   | loginType=3(两端都可)
-- EMP001~EMP005: 各部门员工
INSERT INTO `sys_user`
(`id`,`username`,`password`,`role_id`,`login_type`,`status`,
 `name`,`gender`,`phone`,`entry_date`,`department_id`,`position`,`rank`,`employee_status`,
 `create_time`,`update_time`,`deleted`) VALUES
(1, 'admin',  '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 1, 3, 1,
 '系统管理员', '男', '13800000001', '2020-01-01', 1, '系统管理员', 'P10', 1,
 NOW(), NOW(), 0),
(2, 'HR001',  '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 2, 3, 1,
 '李四',     '女', '13800000002', '2021-06-01', 8, '人事主管',   'P8', 1,
 NOW(), NOW(), 0),
(3, 'EMP001', '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 3, 3, 1,
 '赵六',     '女', '13800000003', '2021-09-15', 3, '品质工程师', 'P7', 1,
 NOW(), NOW(), 0),
(4, 'EMP002', '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 4, 3, 1,
 '王五',     '男', '13800000004', '2022-01-10',101, 'SMT操作员',  'P4', 1,
 NOW(), NOW(), 0),
(5, 'EMP003', '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 3, 3, 1,
 '钱七',     '男', '13800000005', '2019-05-20', 2, '生产经理',   'P8', 1,
 NOW(), NOW(), 0),
(6, 'EMP004', '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 4, 1, 1,
 '张三',     '男', '13800000006', '2020-03-15', 2, '生产主管',   'P6', 1,
 NOW(), NOW(), 0),
(7, 'EMP005', '$2a$10$yKnulAzPW1FfpxXrRFxsBO2fsyslQjw5R65pY1djqgEcW3kmlHs96', 4, 3, 1,
 '孙八',     '女', '13800000007', '2023-03-01', 4, '研发工程师', 'P6', 1,
 NOW(), NOW(), 0);


-- =============================================
-- 2. sys_role 角色表
--    对应 Entity: SysRole.java
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name`        varchar(50)  NOT NULL                COMMENT '角色名称',
  `code`        varchar(50)  NOT NULL                COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL           COMMENT '角色描述',
  `sort`        int          DEFAULT 0               COMMENT '排序',
  `status`      int          DEFAULT 1               COMMENT '状态: 1=启用 0=停用',
  `permissions` text         DEFAULT NULL           COMMENT '权限列表(逗号分隔)',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     int          DEFAULT 0               COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

INSERT INTO `sys_role`
(`id`,`name`,`code`,`description`,`sort`,`status`,`permissions`,`create_time`,`update_time`,`deleted`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员', 1, 1, '*',                       NOW(), NOW(), 0),
(2, '人事主管',   'HR_MANAGER',  '人事部门主管',   2, 1, 'employee:list,employee:create,employee:update,employee-delete,department:list,salary:list,attendance:*', NOW(), NOW(), 0),
(3, '部门主管',   'DEPT_MANAGER','各部门主管',     3, 1, 'employee:list,attendance:*,leave:*,overtime:*', NOW(), NOW(), 0),
(4, '普通员工',   'EMPLOYEE',    '普通员工',       4, 1, '',                        NOW(), NOW(), 0);


-- =============================================
-- 3. hr_department 部门表
--    对应 Entity: HrDepartment.java
-- =============================================
DROP TABLE IF EXISTS `hr_department`;
CREATE TABLE `hr_department` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name`        varchar(50)  NOT NULL                COMMENT '部门名称',
  `code`        varchar(50)  DEFAULT NULL           COMMENT '部门编码',
  `parent_id`   bigint       DEFAULT 0              COMMENT '父部门ID',
  `level`       int          DEFAULT 1               COMMENT '层级',
  `path`        varchar(255) DEFAULT NULL           COMMENT '层级路径',
  `leader_id`   bigint       DEFAULT NULL           COMMENT '部门负责人ID',
  `sort`        int          DEFAULT 0               COMMENT '排序',
  `status`      varchar(10)  DEFAULT '1'             COMMENT '状态: 1=正常 0=停用',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     int          DEFAULT 0               COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

INSERT INTO `hr_department`
(`id`,`name`,`code`,`parent_id`,`level`,`path`,`leader_id`,`sort`,`status`,`create_time`,`update_time`,`deleted`) VALUES
(1,  '总公司',   'ROOT',        0, 0, '1',       NULL, 0, '1', NOW(), NOW(), 0),
(2,  '生产部',   'PRODUCTION',  1, 1, '1,2',     5,    1, '1', NOW(), NOW(), 0),
(3,  '品质部',   'QUALITY',     1, 1, '1,3',     3,    2, '1', NOW(), NOW(), 0),
(4,  '研发部',   'RD',          1, 1, '1,4',     7,    3, '1', NOW(), NOW(), 0),
(5,  '采购部',   'PURCHASE',    1, 1, '1,5',     NULL, 4, '1', NOW(), NOW(), 0),
(6,  '仓储部',   'WAREHOUSE',   1, 1, '1,6',     NULL, 5, '1', NOW(), NOW(), 0),
(7,  '财务部',   'FINANCE',     1, 1, '1,7',     6,    6, '1', NOW(), NOW(), 0),
(8,  '人事部',   'HR',          1, 1, '1,8',     2,    7, '1', NOW(), NOW(), 0),
(9,  '行政部',   'ADMIN',       1, 1, '1,9',     NULL, 8, '1', NOW(), NOW(), 0),
(101,'SMT车间',  'SMT',         2, 2, '2,101',   3,    1, '1', NOW(), NOW(), 0),
(102,'贴合车间','LAMINATION',  2, 2, '2,102',   NULL, 2, '1', NOW(), NOW(), 0),
(103,'组装车间','ASSEMBLY',    2, 2, '2,103',   NULL, 3, '1', NOW(), NOW(), 0),
(104,'包装车间','PACKAGING',   2, 2, '2,104',   NULL, 4, '1', NOW(), NOW(), 0),
(201,'IQC',     'IQC',         3, 2, '3,201',   NULL, 1, '1', NOW(), NOW(), 0),
(202,'OQC',     'OQC',         3, 2, '3,202',   NULL, 2, '1', NOW(), NOW(), 0),
(203,'QE',      'QE',          3, 2, '3,203',   NULL, 3, '1', NOW(), NOW(), 0);


-- =============================================
-- 4. hr_attendance 考勤记录表（按Entity重写）
--    对应 Entity: HrAttendance.java
--    注意: 完全按照Entity字段定义，不再使用旧的上午/下午分开模式
-- =============================================
DROP TABLE IF EXISTS `hr_attendance`;
CREATE TABLE `hr_attendance` (
  `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT '考勤记录ID',
  `user_id`        bigint      NOT NULL                COMMENT '用户ID',
  `attendance_date` date        NOT NULL                COMMENT '考勤日期',
  `check_in_time`  datetime    DEFAULT NULL           COMMENT '签到时间',
  `check_out_time` datetime    DEFAULT NULL           COMMENT '签退时间',
  `check_in_status` varchar(20) DEFAULT NULL           COMMENT '签到状态: normal/late/early/missing',
  `check_out_status`varchar(20)DEFAULT NULL           COMMENT '签退状态: normal/early/overtime/missing',
  `work_hours`     decimal(5,2)DEFAULT NULL           COMMENT '工作时长(小时)',
  `result`         varchar(20) DEFAULT NULL           COMMENT '考勤结果: normal/absent/late/leave',
  `remark`         varchar(255)DEFAULT NULL           COMMENT '备注',
  `create_time`    datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        int         DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `attendance_date`),
  KEY `idx_attendance_date` (`attendance_date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤记录表';


-- =============================================
-- 5. hr_salary 薪酬记录表（按Entity重写）
--    对应 Entity: HrSalary.java
-- =============================================
DROP TABLE IF EXISTS `hr_salary`;
CREATE TABLE `hr_salary` (
  `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '薪酬ID',
  `user_id`            bigint       NOT NULL                COMMENT '用户ID',
  `salary_month`       varchar(7)   NOT NULL                COMMENT '薪酬月份 YYYY-MM',
  `base_salary`        decimal(10,2)DEFAULT 0.00            COMMENT '基本工资',
  `position_allowance` decimal(10,2)DEFAULT 0.00            COMMENT '岗位津贴',
  `performance`        decimal(10,2)DEFAULT 0.00            COMMENT '绩效奖金',
  `overtime_pay`       decimal(10,2)DEFAULT 0.00            COMMENT '加班费',
  `bonus`              decimal(10,2)DEFAULT 0.00            COMMENT '奖金',
  `meal_allowance`     decimal(10,2)DEFAULT 0.00            COMMENT '餐补',
  `transport_allowance`decimal(10,2)DEFAULT 0.00            COMMENT '交通补贴',
  `social_insurance`   decimal(10,2)DEFAULT 0.00            COMMENT '社保扣款',
  `housing_fund`       decimal(10,2)DEFAULT 0.00            COMMENT '公积金扣款',
  `other_deduction`    decimal(10,2)DEFAULT 0.00            COMMENT '其他扣款',
  `net_salary`         decimal(10,2)DEFAULT 0.00            COMMENT '实发工资',
  `status`             int          DEFAULT 0               COMMENT '发放状态: 0=未发放 1=已发放',
  `grant_date`         date         DEFAULT NULL           COMMENT '发放日期',
  `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_salary_month` (`salary_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪酬记录表';


-- =============================================
-- 6. hr_leave 请假申请表
--    对应 Entity: HrLeave.java
-- =============================================
DROP TABLE IF EXISTS `hr_leave`;
CREATE TABLE `hr_leave` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '请假ID',
  `user_id`       bigint       NOT NULL                COMMENT '用户ID',
  `leave_type`    varchar(20)  NOT NULL                COMMENT '请假类型',
  `start_date`    date         NOT NULL                COMMENT '开始日期',
  `end_date`      date         NOT NULL                COMMENT '结束日期',
  `days`          decimal(4,1) DEFAULT NULL            COMMENT '请假天数',
  `reason`        text         DEFAULT NULL           COMMENT '原因',
  `status`        int          DEFAULT 0               COMMENT '审批状态: 0=待审批 1=已通过 2=已拒绝',
  `approver_id`   bigint       DEFAULT NULL           COMMENT '审批人ID',
  `approve_time`  datetime     DEFAULT NULL           COMMENT '审批时间',
  `approve_remark`varchar(255) DEFAULT NULL           COMMENT '审批备注',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请假申请表';


-- =============================================
-- 7. hr_overtime 加班申请表
--    对应 Entity: HrOvertime.java
-- =============================================
DROP TABLE IF EXISTS `hr_overtime`;
CREATE TABLE `hr_overtime` (
  `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT '加班ID',
  `user_id`        bigint      NOT NULL                COMMENT '用户ID',
  `overtime_date`  date        NOT NULL                COMMENT '加班日期',
  `start_time`     datetime    NOT NULL                COMMENT '开始时间',
  `end_time`       datetime    NOT NULL                COMMENT '结束时间',
  `hours`          decimal(4,1)DEFAULT NULL            COMMENT '加班时长(小时)',
  `reason`         text        DEFAULT NULL           COMMENT '原因',
  `status`         int         DEFAULT 0               COMMENT '审批状态: 0=待审批 1=已通过 2=已拒绝',
  `approver_id`    bigint      DEFAULT NULL           COMMENT '审批人ID',
  `approve_remark` varchar(255)DEFAULT NULL           COMMENT '审批备注',
  `approve_time`   datetime    DEFAULT NULL           COMMENT '审批时间',
  `create_time`    datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        int         DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_overtime_date` (`overtime_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='加班申请表';


-- =============================================
-- 8. hr_contract 合同表
--    对应 Entity: HrContract.java
-- =============================================
DROP TABLE IF EXISTS `hr_contract`;
CREATE TABLE `hr_contract` (
  `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `user_id`        bigint      NOT NULL                COMMENT '用户ID',
  `contract_type`  varchar(20) DEFAULT NULL           COMMENT '合同类型',
  `contract_no`    varchar(50) DEFAULT NULL           COMMENT '合同编号',
  `start_date`     date        DEFAULT NULL           COMMENT '开始日期',
  `end_date`       date        DEFAULT NULL           COMMENT '结束日期',
  `contract_period`int         DEFAULT NULL           COMMENT '合同期限(月)',
  `sign_date`      date        DEFAULT NULL           COMMENT '签订日期',
  `contract_file`  varchar(255)DEFAULT NULL           COMMENT '合同文件路径',
  `status`         int         DEFAULT 1              COMMENT '状态: 1=有效 0=过期 2=解除',
  `remark`         varchar(255)DEFAULT NULL           COMMENT '备注',
  `create_time`    datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        int         DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='劳动合同表';


-- =============================================
-- 9. sys_notice 公告表
--    对应 Entity: SysNotice.java
-- =============================================
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`        varchar(200) NOT NULL                COMMENT '标题',
  `content`      text         DEFAULT NULL           COMMENT '内容',
  `type`         int          DEFAULT 1               COMMENT '类型: 1=普通 2=重要 3=紧急',
  `status`       int          DEFAULT 0               COMMENT '状态: 0=草稿 1=已发布',
  `creator_id`   bigint       DEFAULT NULL           COMMENT '创建者ID',
  `publish_time` datetime     DEFAULT NULL           COMMENT '发布时间',
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告通知表';

INSERT INTO `sys_notice` (`title`,`content`,`type`,`status`,`creator_id`,`create_time`,`update_time`,`deleted`) VALUES
('欢迎使用HRMS系统', '本系统为人力资源管理系统，请各位同事规范使用。', 1, 1, 1, NOW(), NOW(), 0);


-- =============================================
-- 10. sys_log 系统日志表
--     对应 Entity: SysLog.java
-- =============================================
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`     bigint       DEFAULT NULL           COMMENT '用户ID',
  `username`    varchar(50)  DEFAULT NULL           COMMENT '用户名',
  `operation`   varchar(100) DEFAULT NULL           COMMENT '操作描述',
  `method`      varchar(200) DEFAULT NULL           COMMENT '方法名',
  `params`      text         DEFAULT NULL           COMMENT '请求参数',
  `ip`          varchar(50)  DEFAULT NULL           COMMENT 'IP地址',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统操作日志表';


-- =============================================
-- 11. vote_subject 投票主题表
--     对应 Entity: VoteSubject.java
-- =============================================
DROP TABLE IF EXISTS `vote_subject`;
CREATE TABLE `vote_subject` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '投票主题ID',
  `title`         varchar(200) NOT NULL                COMMENT '标题',
  `description`   text         DEFAULT NULL           COMMENT '描述',
  `vote_type`     int          DEFAULT 1               COMMENT '投票类型: 1=单选 2=多选 3=评分',
  `max_selections`int          DEFAULT 1               COMMENT '最多选择数',
  `anonymous`     int          DEFAULT 0               COMMENT '是否匿名: 0=否 1=是',
  `start_time`    datetime     DEFAULT NULL           COMMENT '开始时间',
  `end_time`      datetime     DEFAULT NULL           COMMENT '结束时间',
  `target_type`   int          DEFAULT 1               COMMENT '参与对象: 1=全部 2=指定部门 3=指定人员',
  `target_ids`    varchar(500) DEFAULT NULL           COMMENT '指定对象IDs(逗号分隔)',
  `status`        int          DEFAULT 0               COMMENT '状态: 0=未开始 1=进行中 2=已结束',
  `creator_id`    bigint       DEFAULT NULL           COMMENT '创建者ID',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票主题表';


-- =============================================
-- 12. vote_option 投票选项表
--     对应 Entity: VoteOption.java
-- =============================================
DROP TABLE IF EXISTS `vote_option`;
CREATE TABLE `vote_option` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '选项ID',
  `subject_id`  bigint       NOT NULL                COMMENT '所属主题ID',
  `content`     varchar(500) NOT NULL                COMMENT '选项内容',
  `sort`        int          DEFAULT 0               COMMENT '排序',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票选项表';


-- =============================================
-- 13. vote_record 投票记录表
--     对应 Entity: VoteRecord.java
-- =============================================
DROP TABLE IF EXISTS `vote_record`;
CREATE TABLE `vote_record` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `subject_id` bigint       NOT NULL                COMMENT '投票主题ID',
  `voter_id`   bigint       NOT NULL                COMMENT '投票人ID',
  `option_ids` varchar(500) DEFAULT NULL           COMMENT '选中的选项IDs',
  `score`      int          DEFAULT NULL           COMMENT '评分(评分投票时使用)',
  `vote_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_voter_id` (`voter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票记录表';


-- =============================================
-- 14. hr_recruit_position 招聘职位表
--     对应 Entity: HrRecruitPosition.java
-- =============================================
DROP TABLE IF EXISTS `hr_recruit_position`;
CREATE TABLE `hr_recruit_position` (
  `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '职位ID',
  `title`          varchar(100) NOT NULL                COMMENT '职位名称',
  `department_id`  bigint       DEFAULT NULL           COMMENT '部门ID',
  `recruit_count`  int          DEFAULT 1               COMMENT '招聘人数',
  `job_type`       varchar(20)  DEFAULT NULL           COMMENT '全职/兼职/实习',
  `experience`     varchar(50)  DEFAULT NULL           COMMENT '经验要求',
  `education`      varchar(20)  DEFAULT NULL           COMMENT '学历要求',
  `salary_min`     decimal(10,2)DEFAULT NULL           COMMENT '最低薪资',
  `salary_max`     decimal(10,2)DEFAULT NULL           COMMENT '最高薪资',
  `location`       varchar(100) DEFAULT NULL           COMMENT '工作地点',
  `description`    text         DEFAULT NULL           COMMENT '职位描述',
  `requirements`   text         DEFAULT NULL           COMMENT '任职要求',
  `benefits`       text         DEFAULT NULL           COMMENT '福利待遇',
  `status`         int          DEFAULT 0              COMMENT '状态: 0=草稿 1=招聘中 2=已暂停 3=已结束',
  `publisher_id`   bigint       DEFAULT NULL           COMMENT '发布者ID',
  `publish_time`   datetime     DEFAULT NULL           COMMENT '发布时间',
  `deadline`       date         DEFAULT NULL           COMMENT '截止日期',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招聘职位表';


-- =============================================
-- 15. hr_recruit_candidate 招聘候选人表
--     对应 Entity: HrRecruitCandidate.java
-- =============================================
DROP TABLE IF EXISTS `hr_recruit_candidate`;
CREATE TABLE `hr_recruit_candidate` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '候选人ID',
  `position_id`      bigint       NOT NULL                COMMENT '应聘职位ID',
  `name`             varchar(50)  NOT NULL                COMMENT '姓名',
  `gender`           varchar(10)  DEFAULT NULL           COMMENT '性别',
  `phone`            varchar(20)  NOT NULL                COMMENT '手机号',
  `email`            varchar(100) DEFAULT NULL           COMMENT '邮箱',
  `age`              int          DEFAULT NULL           COMMENT '年龄',
  `education`        varchar(20)  DEFAULT NULL           COMMENT '学历',
  `school`           varchar(100) DEFAULT NULL           COMMENT '毕业院校',
  `major`            varchar(100) DEFAULT NULL           COMMENT '专业',
  `experience_years` int          DEFAULT NULL           COMMENT '工作年限',
  `current_company`  varchar(100) DEFAULT NULL           COMMENT '当前公司',
  `current_position` varchar(100) DEFAULT NULL           COMMENT '当前职位',
  `expected_salary`  decimal(10,2)DEFAULT NULL           COMMENT '期望薪资',
  `resume_file`      varchar(255) DEFAULT NULL           COMMENT '简历文件路径',
  `source`           varchar(20)  DEFAULT NULL           COMMENT '来源: website/referral/headhunter/campus',
  `self_intro`       text         DEFAULT NULL           COMMENT '自我介绍',
  `status`           int          DEFAULT 0              COMMENT '状态: 0=待筛选 1=初筛通过 2=面试中 3=已录用 4=已淘汰 5=已放弃',
  `remark`           varchar(500) DEFAULT NULL           COMMENT '备注',
  `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_position_id` (`position_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招聘候选人表';


-- =============================================
-- 16. hr_recruit_interview 招聘面试表
--     对应 Entity: HrRecruitInterview.java
-- =============================================
DROP TABLE IF EXISTS `hr_recruit_interview`;
CREATE TABLE `hr_recruit_interview` (
  `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  `candidate_id`     bigint      NOT NULL                COMMENT '候选人ID',
  `position_id`      bigint      NOT NULL                COMMENT '职位ID',
  `round`            int         DEFAULT 1               COMMENT '轮次: 1=初面 2=复试 3=终面',
  `interviewer_id`   bigint      DEFAULT NULL           COMMENT '面试官ID',
  `interview_type`   varchar(20) DEFAULT NULL           COMMENT '面试形式: onsite/video/phone',
  `interview_date`   datetime    DEFAULT NULL           COMMENT '面试时间',
  `duration_minutes` int         DEFAULT NULL           COMMENT '面试时长(分钟)',
  `location`         varchar(255)DEFAULT NULL           COMMENT '地点或链接',
  `score`            int         DEFAULT NULL           COMMENT '面试评分(0-100)',
  `result`           varchar(20) DEFAULT NULL           COMMENT '结果: pass/fail/pending/reschedule',
  `feedback`         text        DEFAULT NULL           COMMENT '反馈意见',
  `next_round`       int         DEFAULT NULL           COMMENT '是否进入下一轮: 1=是 0=否',
  `create_time`      datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          int         DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_candidate_id` (`candidate_id`),
  KEY `idx_position_id` (`position_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招聘面试记录表';


-- =============================================
-- 17. sys_dict_type 字典类型表
-- =============================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `name`        varchar(50)  NOT NULL                COMMENT '字典名称',
  `code`        varchar(50)  NOT NULL                COMMENT '字典编码',
  `description` varchar(255) DEFAULT NULL           COMMENT '描述',
  `status`      int          DEFAULT 1               COMMENT '状态: 1=启用 0=停用',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

INSERT INTO `sys_dict_type` (`name`,`code`,`description`,`status`,`deleted`) VALUES
('性别',        'gender',        '性别字典',     1, 0),
('民族',        'nation',        '民族字典',     1, 0),
('学历',        'education',     '学历字典',     1, 0),
('婚姻状况',    'marital_status','婚姻状况字典', 1, 0),
('员工状态',    'employee_status','员工状态字典', 1, 0),
('请假类型',    'leave_type',    '请假类型字典', 1, 0),
('合同类型',    'contract_type', '合同类型字典', 1, 0);


-- =============================================
-- 18. sys_dict_data 字典数据表
-- =============================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `dict_type_id`bigint      NOT NULL                COMMENT '字典类型ID',
  `label`       varchar(100)NOT NULL                COMMENT '标签',
  `value`       varchar(100)NOT NULL                COMMENT '值',
  `sort`        int         DEFAULT 0               COMMENT '排序',
  `status`      int         DEFAULT 1               COMMENT '状态: 1=启用 0=停用',
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     int         DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

INSERT INTO `sys_dict_data` (`dict_type_id`,`label`,`value`,`sort`,`status`,`deleted`) VALUES
-- 性别 (dict_type_id=1)
(1, '男',   '男',   1, 1, 0), (1, '女',   '女',   2, 1, 0),
-- 学历 (dict_type_id=3)
(3, '初中','初中', 1, 1, 0), (3, '高中','高中', 2, 1, 0),
(3, '中专','中专', 3, 1, 0), (3, '大专','大专', 4, 1, 0),
(3, '本科','本科', 5, 1, 0), (3, '硕士','硕士', 6, 1, 0),
(3, '博士','博士', 7, 1, 0),
-- 婚姻状况 (dict_type_id=4)
(4, '未婚','未婚', 1, 1, 0), (4, '已婚','已婚', 2, 1, 0),
(4, '离异','离异', 3, 1, 0), (4, '丧偶','丧偶', 4, 1, 0),
-- 员工状态 (dict_type_id=5)
(5, '正式','1', 1, 1, 0), (5, '试用','2', 2, 1, 0),
(5, '实习','3', 3, 1, 0), (5, '离职','4', 4, 1, 0), (5, '退休','5', 5, 1, 0),
-- 请假类型 (dict_type_id=6)
(6, '年假',  'annual',    1, 1, 0), (6, '病假',  'sick',     2, 1, 0),
(6, '事假',  'personal',  3, 1, 0), (6, '婚假',  'marriage', 4, 1, 0),
(6, '产假',  'maternity', 5, 1, 0), (6, '陪产假','paternity',6, 1, 0), (6, '丧假',  'bereavement',7, 1, 0),
-- 合同类型 (dict_type_id=7)
(7, '劳动合同','labor',     1, 1, 0), (7, '劳务合同','service', 2, 1, 0), (7, '实习协议','internship',3, 1, 0);

-- =============================================
-- 19. sys_menu 菜单权限表（保留，供前端路由使用）
-- =============================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id`   bigint       DEFAULT 0               COMMENT '父菜单ID',
  `name`        varchar(50)  NOT NULL                COMMENT '菜单名称',
  `path`        varchar(100) DEFAULT NULL           COMMENT '路由路径',
  `component`   varchar(100) DEFAULT NULL           COMMENT '组件路径',
  `permission`  varchar(100) DEFAULT NULL           COMMENT '权限标识',
  `type`        int          DEFAULT 1               COMMENT '类型: 1=菜单 2=按钮',
  `icon`        varchar(50)  DEFAULT NULL           COMMENT '图标',
  `sort`        int          DEFAULT 0               COMMENT '排序',
  `status`      int          DEFAULT 1               COMMENT '状态: 1=启用 0=停用',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     int          DEFAULT 0              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

INSERT INTO `sys_menu` (`parent_id`,`name`,`path`,`component`,`permission`,`type`,`icon`,`sort`,`status`,`deleted`) VALUES
(0, '系统管理', '/system', NULL,          NULL, 1, 'Setting', 100, 1, 0),
(1, '角色权限', '/system/role','system/RolePage','system:role', 1, 'UserManagement', 1, 1, 0),
(0, '员工管理', '/employee','employee/EmployeePage','employee:list', 1, 'People', 10, 1, 0),
(0, '部门管理', '/department','department/DepartmentPage','department:list', 1, 'Building', 20, 1, 0),
(0, '考勤管理', '/attendance',NULL,           NULL, 1, 'Calendar', 30, 1, 0),
(5, '考勤记录', '/attendance/record','attendance/RecordPage','attendance:record', 1, 'ClockInOut', 1, 1, 0),
(5, '请假申请', '/attendance/leave','attendance/LeavePage','attendance:leave',   1, 'PlaneLanding', 2, 1, 0),
(5, '加班申请', '/attendance/overtime','attendance/OvertimePage','attendance:overtime', 1, 'Timer', 3, 1, 0),
(0, '合同管理', '/contract','contract/ContractPage','contract:list', 1, 'FileText', 40, 1, 0),
(0, '薪酬管理', '/salary','salary/SalaryPage','salary:list', 1, 'Wallet', 50, 1, 0),
(0, '招聘管理', '/recruit', NULL,            NULL, 1, 'UserPlus', 55, 1, 0),
(11,'职位管理', '/recruit/position','recruit/PositionPage','recruit:position', 1, 'Briefcase', 1, 1, 0),
(11,'候选人管理','/recruit/candidate','recruit/CandidatePage','recruit:candidate', 1, 'Users', 2, 1, 0),
(11,'面试安排','/recruit/interview','recruit/InterviewPage','recruit:interview', 1, 'Video', 3, 1, 0),
(0, '投票管理', '/vote', NULL,              NULL, 1, 'Vote', 60, 1, 0),
(15,'投票主题', '/vote/subject','vote/VoteSubjectPage','vote:subject', 1, 'FileEdit', 1, 1, 0),
(0, '公告管理', '/notice','notice/NoticePage','notice:list', 1, 'BellRing', 70, 1, 0),
(0, '统计报表', '/report', NULL,             NULL, 1, 'ChartBar', 80, 1, 0);

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 初始化完成！
--
-- 默认账号:
--   管理员: admin / 123456  (loginType=3, 两端均可登录)
--   人事:   HR001 / 123456  (loginType=3, 两端均可登录)
--   员工:   EMP001~EMP005 / 123456
--   其中 EMP004 的 loginType=1 (仅小程序)
-- =============================================
