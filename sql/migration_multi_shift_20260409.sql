-- =============================================
-- 考勤表多时段改造 (2026-04-09)
-- 支持一天多次打卡：上午、下午、加班
-- =============================================

-- 1. 新增时段字段
ALTER TABLE `hr_attendance`
  ADD COLUMN `shift_type` VARCHAR(20) DEFAULT 'MORNING' COMMENT '打卡时段: MORNING=上午 AFTERNOON=下午 OVERTIME=加班'
  AFTER `attendance_date`;

-- 2. 删除旧唯一键（每人每天一条）
ALTER TABLE `hr_attendance` DROP INDEX `uk_user_date`;

-- 3. 创建新联合唯一键（每人每天每时段一条）
ALTER TABLE `hr_attendance`
  ADD UNIQUE KEY `uk_user_date_shift` (`user_id`, `attendance_date`, `shift_type`);

-- 4. 更新现有数据：默认设为 MORNING
UPDATE `hr_attendance` SET `shift_type` = 'MORNING' WHERE `shift_type` IS NULL;
