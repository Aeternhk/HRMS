-- ============================================================
-- HRMS 小程序投票功能 - 测试数据
-- 生成两个测试投票：一个进行中，一个已完成
-- 执行前请确保 vote_subject / vote_option / vote_record 表已存在
-- ============================================================

-- 清理旧测试数据（可选，谨慎执行）
-- DELETE FROM vote_record WHERE subject_id IN (SELECT id FROM vote_subject WHERE title LIKE '测试%');
-- DELETE FROM vote_option WHERE subject_id IN (SELECT id FROM vote_subject WHERE title LIKE '测试%');
-- DELETE FROM vote_subject WHERE title LIKE '测试%';

-- ==================== 投票1：进行中的投票（单选） ====================
INSERT INTO vote_subject (
    title, description, vote_type, max_selections, anonymous,
    start_time, end_time, target_type, target_ids, status,
    creator_id, create_time, update_time, deleted
) VALUES (
    '2026年第一季度员工活动投票',
    '为了丰富大家的业余生活，公司计划在近期组织一次团建活动。请从以下选项中选出你最期待的活动形式，我们将根据投票结果安排具体行程。',
    1,   -- 单选
    1,
    0,   -- 不匿名
    '2026-04-01 09:00:00',
    '2026-05-01 18:00:00',  -- 截止时间在未来 → 进行中
    NULL, NULL,
    1,   -- 状态：启用
    1,   -- 创建者ID（管理员）
    NOW(),
    NOW(),
    0
);

SET @vote_ongoing_id = LAST_INSERT_ID();

-- 进行中投票的选项
INSERT INTO vote_option (subject_id, content, sort, create_time) VALUES
(@vote_ongoing_id, '户外拓展训练（爬山+团队游戏）', 1, NOW()),
(@vote_ongoing_id, '农家乐一日游（采摘+野炊+KTV）', 2, NOW()),
(@vote_ongoing_id, '城市寻宝挑战赛', 3, NOW()),
(@vote_ongoing_id, '温泉度假村休闲游', 4, NOW());

SET @opt_1a = (SELECT id FROM vote_option WHERE subject_id = @vote_ongoing_id AND sort = 1);
SET @opt_1b = (SELECT id FROM vote_option WHERE subject_id = @vote_ongoing_id AND sort = 2);
SET @opt_1c = (SELECT id FROM vote_option WHERE subject_id = @vote_ongoing_id AND sort = 3);
SET @opt_1d = (SELECT id FROM vote_option WHERE subject_id = @vote_ongoing_id AND sort = 4);

-- 模拟一些已投票记录（假设用户ID 2~8 是小程序用户）
INSERT INTO vote_record (subject_id, voter_id, option_ids, score, vote_time) VALUES
(@vote_ongoing_id, 2, @opt_1b, NULL, '2026-04-03 10:30:00'),   -- 选了农家乐
(@vote_ongoing_id, 3, @opt_1a, NULL, '2026-04-03 11:15:00'),   -- 选了户外拓展
(@vote_ongoing_id, 4, @opt_1b, NULL, '2026-04-04 09:20:00'),   -- 选了农家乐
(@vote_ongoing_id, 5, @opt_1c, NULL, '2026-04-04 14:00:00'),   -- 选了城市寻宝
(@vote_ongoing_id, 6, @opt_1a, NULL, '2026-04-05 08:45:00');   -- 选了户外拓展


-- ==================== 投票2：已完成的投票（多选） ====================
INSERT INTO vote_subject (
    title, description, vote_type, max_selections, anonymous,
    start_time, end_time, target_type, target_ids, status,
    creator_id, create_time, update_time, deleted
) VALUES (
    '公司食堂菜品满意度调查',
    '感谢大家对食堂工作的支持！本次调查旨在了解员工对当前菜品的满意程度，以便我们持续改进餐饮服务质量。请选择你喜欢的菜品类型（可多选）。',
    2,   -- 多选
    3,   -- 最多选3项
    0,   -- 不匿名
    '2026-03-01 08:00:00',
    '2026-03-31 23:59:00',  -- 截止时间在过去 → 已结束
    NULL, NULL,
    2,   -- 状态：已关闭
    1,
    '2026-02-28 10:00:00',
    '2026-04-01 09:00:00',
    0
);

SET @vote_ended_id = LAST_INSERT_ID();

-- 已完成投票的选项
INSERT INTO vote_option (subject_id, content, sort, create_time) VALUES
(@vote_ended_id, '川湘菜系列', 1, '2026-02-28 10:00:00'),
(@vote_ended_id, '粤菜/清淡口味', 2, '2026-02-28 10:00:00'),
(@vote_ended_id, '北方面食主食', 3, '2026-02-28 10:00:00'),
(@vote_ended_id, '西式简餐沙拉', 4, '2026-02-28 10:00:00'),
(@vote_ended_id, '清真风味窗口', 5, '2026-02-28 10:00:00');

SET @opt_2a = (SELECT id FROM vote_option WHERE subject_id = @vote_ended_id AND sort = 1);
SET @opt_2b = (SELECT id FROM vote_option WHERE subject_id = @vote_ended_id AND sort = 2);
SET @opt_2c = (SELECT id FROM vote_option WHERE subject_id = @vote_ended_id AND sort = 3);
SET @opt_2d = (SELECT id FROM vote_option WHERE subject_id = @vote_ended_id AND sort = 4);
SET @opt_2e = (SELECT id FROM vote_option WHERE subject_id = @vote_ended_id AND sort = 5);

-- 模拟大量已完成投票记录
INSERT INTO vote_record (subject_id, voter_id, option_ids, score, vote_time) VALUES
(@vote_ended_id, 2, CONCAT(@opt_2a, ',', @opt_2b), NULL, '2026-03-05 12:00:00'),
(@vote_ended_id, 3, CONCAT(@opt_2b, ',', @opt_2d), NULL, '2026-03-05 13:20:00'),
(@vote_ended_id, 4, CONCAT(@opt_2a, ',', @opt_2c, ',', @opt_2e), NULL, '2026-03-06 11:40:00'),
(@vote_ended_id, 5, CONCAT(@opt_2b, ',', @opt_2e), NULL, '2026-03-06 14:10:00'),
(@vote_ended_id, 6, CONCAT(@opt_2a, ',', @opt_2b, ',', @opt_2c), NULL, '2026-03-07 09:30:00'),
(@vote_ended_id, 7, CONCAT(@opt_2d, ',', @opt_2e), NULL, '2026-03-07 12:50:00'),
(@vote_ended_id, 8, CONCAT(@opt_2a, ',', @opt_2d), NULL, '2026-03-08 10:15:00');


-- ============================================================
-- 验证数据
-- ============================================================
-- SELECT vs.id, vs.title, vs.vote_type, vs.status,
--        vs.start_time, vs.end_time,
--        (SELECT COUNT(*) FROM vote_option vo WHERE vo.subject_id = vs.id) AS option_count,
--        (SELECT COUNT(*) FROM vote_record vr WHERE vr.subject_id = vs.id) AS vote_count
-- FROM vote_subject vs
-- WHERE vs.deleted = 0
-- ORDER BY vs.create_time DESC;
