-- ============================================================
-- 修复成绩组成项：清理重复数据 + 加索引
-- 不再使用 ADD UNIQUE KEY（容易因 metadata lock 卡住），
-- 改为普通 INDEX + DELETE+INSERT 种子模式。
-- ============================================================

-- 1. 查看清理前数量和重复情况
SELECT '=== 清理前 ===' AS step;
SELECT COUNT(*) AS total_rows FROM tb_grade_component;

-- 2. 创建临时表保存要保留的 ID
DROP TEMPORARY TABLE IF EXISTS tmp_keep;
CREATE TEMPORARY TABLE tmp_keep AS
SELECT MIN(id) AS id FROM tb_grade_component GROUP BY course_id, name;

-- 3. 删除重复行
DELETE FROM tb_grade_component WHERE id NOT IN (SELECT id FROM tmp_keep);
DROP TEMPORARY TABLE IF EXISTS tmp_keep;

-- 4. 查看清理后数量
SELECT '=== 清理后 ===' AS step;
SELECT COUNT(*) AS total_rows FROM tb_grade_component;
SELECT course_id, name, COUNT(*) AS cnt
FROM tb_grade_component GROUP BY course_id, name HAVING cnt > 1;

-- 5. 最终验证
SELECT '=== 完成 ===' AS step;
SELECT COUNT(*) AS final_rows FROM tb_grade_component;
