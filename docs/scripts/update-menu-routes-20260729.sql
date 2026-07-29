-- 菜单表路由路径更新：/process/ → /blindplate/
-- 执行前请确认菜单表名
UPDATE menu SET route_path = REPLACE(route_path, '/process/', '/blindplate/')
WHERE route_path LIKE '/process/%';

-- 验证：应返回 0 条记录
SELECT * FROM menu WHERE route_path LIKE '/process/%';

-- 验证：应返回已更新的菜单记录
SELECT * FROM menu WHERE route_path LIKE '/blindplate/%';