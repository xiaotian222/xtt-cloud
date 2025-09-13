-- Platform 服务测试数据 - 部门用户数据
-- 为每个部门添加10个测试用户

-- 1. 插入用户数据
INSERT INTO sys_user (username, password, nickname, email, phone, status, created_at, updated_at) VALUES
-- 总公司用户 (10个)
('ceo_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'CEO助理', 'ceo001@xtt.com', '13800138010', 1, NOW(), NOW()),
('hr_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'HR总监', 'hr001@xtt.com', '13800138011', 1, NOW(), NOW()),
('admin_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '行政主管', 'admin001@xtt.com', '13800138012', 1, NOW(), NOW()),
('legal_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '法务专员', 'legal001@xtt.com', '13800138013', 1, NOW(), NOW()),
('strategy_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '战略规划', 'strategy001@xtt.com', '13800138014', 1, NOW(), NOW()),
('board_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '董事会秘书', 'board001@xtt.com', '13800138015', 1, NOW(), NOW()),
('compliance_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '合规专员', 'compliance001@xtt.com', '13800138016', 1, NOW(), NOW()),
('risk_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '风险管理', 'risk001@xtt.com', '13800138017', 1, NOW(), NOW()),
('governance_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '公司治理', 'governance001@xtt.com', '13800138018', 1, NOW(), NOW()),
('executive_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '执行助理', 'executive001@xtt.com', '13800138019', 1, NOW(), NOW()),

-- 技术部用户 (10个)
('tech_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '技术总监', 'tech001@xtt.com', '13800138020', 1, NOW(), NOW()),
('tech_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '架构师', 'tech002@xtt.com', '13800138021', 1, NOW(), NOW()),
('tech_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '技术经理', 'tech003@xtt.com', '13800138022', 1, NOW(), NOW()),
('tech_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '高级工程师', 'tech004@xtt.com', '13800138023', 1, NOW(), NOW()),
('tech_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '运维工程师', 'tech005@xtt.com', '13800138024', 1, NOW(), NOW()),
('tech_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '安全工程师', 'tech006@xtt.com', '13800138025', 1, NOW(), NOW()),
('tech_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '技术顾问', 'tech007@xtt.com', '13800138026', 1, NOW(), NOW()),
('tech_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '技术专员', 'tech008@xtt.com', '13800138027', 1, NOW(), NOW()),
('tech_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '技术助理', 'tech009@xtt.com', '13800138028', 1, NOW(), NOW()),
('tech_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '技术实习生', 'tech010@xtt.com', '13800138029', 1, NOW(), NOW()),

-- 市场部用户 (10个)
('market_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场总监', 'market001@xtt.com', '13800138030', 1, NOW(), NOW()),
('market_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场经理', 'market002@xtt.com', '13800138031', 1, NOW(), NOW()),
('market_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '品牌经理', 'market003@xtt.com', '13800138032', 1, NOW(), NOW()),
('market_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '产品经理', 'market004@xtt.com', '13800138033', 1, NOW(), NOW()),
('market_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场专员', 'market005@xtt.com', '13800138034', 1, NOW(), NOW()),
('market_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场分析师', 'market006@xtt.com', '13800138035', 1, NOW(), NOW()),
('market_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场策划', 'market007@xtt.com', '13800138036', 1, NOW(), NOW()),
('market_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场调研', 'market008@xtt.com', '13800138037', 1, NOW(), NOW()),
('market_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场助理', 'market009@xtt.com', '13800138038', 1, NOW(), NOW()),
('market_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '市场实习生', 'market010@xtt.com', '13800138039', 1, NOW(), NOW()),

-- 财务部用户 (10个)
('finance_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '财务总监', 'finance001@xtt.com', '13800138040', 1, NOW(), NOW()),
('finance_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '财务经理', 'finance002@xtt.com', '13800138041', 1, NOW(), NOW()),
('finance_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '会计主管', 'finance003@xtt.com', '13800138042', 1, NOW(), NOW()),
('finance_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '出纳', 'finance004@xtt.com', '13800138043', 1, NOW(), NOW()),
('finance_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '成本会计', 'finance005@xtt.com', '13800138044', 1, NOW(), NOW()),
('finance_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '税务专员', 'finance006@xtt.com', '13800138045', 1, NOW(), NOW()),
('finance_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '审计专员', 'finance007@xtt.com', '13800138046', 1, NOW(), NOW()),
('finance_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '预算专员', 'finance008@xtt.com', '13800138047', 1, NOW(), NOW()),
('finance_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '财务助理', 'finance009@xtt.com', '13800138048', 1, NOW(), NOW()),
('finance_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '财务实习生', 'finance010@xtt.com', '13800138049', 1, NOW(), NOW()),

-- 开发组用户 (10个)
('dev_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '开发组长', 'dev001@xtt.com', '13800138050', 1, NOW(), NOW()),
('dev_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '高级开发', 'dev002@xtt.com', '13800138051', 1, NOW(), NOW()),
('dev_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Java开发', 'dev003@xtt.com', '13800138052', 1, NOW(), NOW()),
('dev_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '前端开发', 'dev004@xtt.com', '13800138053', 1, NOW(), NOW()),
('dev_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '后端开发', 'dev005@xtt.com', '13800138054', 1, NOW(), NOW()),
('dev_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '全栈开发', 'dev006@xtt.com', '13800138055', 1, NOW(), NOW()),
('dev_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '移动开发', 'dev007@xtt.com', '13800138056', 1, NOW(), NOW()),
('dev_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '数据库开发', 'dev008@xtt.com', '13800138057', 1, NOW(), NOW()),
('dev_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '开发工程师', 'dev009@xtt.com', '13800138058', 1, NOW(), NOW()),
('dev_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '开发实习生', 'dev010@xtt.com', '13800138059', 1, NOW(), NOW()),

-- 测试组用户 (10个)
('test_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '测试组长', 'test001@xtt.com', '13800138060', 1, NOW(), NOW()),
('test_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '高级测试', 'test002@xtt.com', '13800138061', 1, NOW(), NOW()),
('test_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '自动化测试', 'test003@xtt.com', '13800138062', 1, NOW(), NOW()),
('test_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '性能测试', 'test004@xtt.com', '13800138063', 1, NOW(), NOW()),
('test_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '功能测试', 'test005@xtt.com', '13800138064', 1, NOW(), NOW()),
('test_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '接口测试', 'test006@xtt.com', '13800138065', 1, NOW(), NOW()),
('test_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '安全测试', 'test007@xtt.com', '13800138066', 1, NOW(), NOW()),
('test_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '测试工程师', 'test008@xtt.com', '13800138067', 1, NOW(), NOW()),
('test_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '测试专员', 'test009@xtt.com', '13800138068', 1, NOW(), NOW()),
('test_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '测试实习生', 'test010@xtt.com', '13800138069', 1, NOW(), NOW()),

-- 销售组用户 (10个)
('sales_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售总监', 'sales001@xtt.com', '13800138070', 1, NOW(), NOW()),
('sales_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售经理', 'sales002@xtt.com', '13800138071', 1, NOW(), NOW()),
('sales_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '高级销售', 'sales003@xtt.com', '13800138072', 1, NOW(), NOW()),
('sales_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售代表', 'sales004@xtt.com', '13800138073', 1, NOW(), NOW()),
('sales_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '客户经理', 'sales005@xtt.com', '13800138074', 1, NOW(), NOW()),
('sales_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '渠道经理', 'sales006@xtt.com', '13800138075', 1, NOW(), NOW()),
('sales_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售专员', 'sales007@xtt.com', '13800138076', 1, NOW(), NOW()),
('sales_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售助理', 'sales008@xtt.com', '13800138077', 1, NOW(), NOW()),
('sales_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售顾问', 'sales009@xtt.com', '13800138078', 1, NOW(), NOW()),
('sales_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '销售实习生', 'sales010@xtt.com', '13800138079', 1, NOW(), NOW()),

-- 推广组用户 (10个)
('promo_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '推广总监', 'promo001@xtt.com', '13800138080', 1, NOW(), NOW()),
('promo_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '推广经理', 'promo002@xtt.com', '13800138081', 1, NOW(), NOW()),
('promo_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '数字营销', 'promo003@xtt.com', '13800138082', 1, NOW(), NOW()),
('promo_004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '内容运营', 'promo004@xtt.com', '13800138083', 1, NOW(), NOW()),
('promo_005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '社交媒体', 'promo005@xtt.com', '13800138084', 1, NOW(), NOW()),
('promo_006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'SEO专员', 'promo006@xtt.com', '13800138085', 1, NOW(), NOW()),
('promo_007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'SEM专员', 'promo007@xtt.com', '13800138086', 1, NOW(), NOW()),
('promo_008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '活动策划', 'promo008@xtt.com', '13800138087', 1, NOW(), NOW()),
('promo_009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '推广专员', 'promo009@xtt.com', '13800138088', 1, NOW(), NOW()),
('promo_010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '推广实习生', 'promo010@xtt.com', '13800138089', 1, NOW(), NOW());

-- 2. 插入用户角色关系
INSERT INTO sys_user_role (user_id, role_id) VALUES
-- 总公司用户角色分配 (用户ID 6-15)
(6, 2), (7, 2), (8, 2), (9, 3), (10, 3), (11, 3), (12, 3), (13, 3), (14, 3), (15, 3),

-- 技术部用户角色分配 (用户ID 16-25)
(16, 2), (17, 2), (18, 2), (19, 3), (20, 3), (21, 3), (22, 3), (23, 3), (24, 3), (25, 3),

-- 市场部用户角色分配 (用户ID 26-35)
(26, 2), (27, 2), (28, 2), (29, 3), (30, 3), (31, 3), (32, 3), (33, 3), (34, 3), (35, 3),

-- 财务部用户角色分配 (用户ID 36-45)
(36, 2), (37, 2), (38, 2), (39, 3), (40, 3), (41, 3), (42, 3), (43, 3), (44, 3), (45, 3),

-- 开发组用户角色分配 (用户ID 46-55)
(46, 2), (47, 2), (48, 3), (49, 3), (50, 3), (51, 3), (52, 3), (53, 3), (54, 3), (55, 3),

-- 测试组用户角色分配 (用户ID 56-65)
(56, 2), (57, 2), (58, 3), (59, 3), (60, 3), (61, 3), (62, 3), (63, 3), (64, 3), (65, 3),

-- 销售组用户角色分配 (用户ID 66-75)
(66, 2), (67, 2), (68, 2), (69, 3), (70, 3), (71, 3), (72, 3), (73, 3), (74, 3), (75, 3),

-- 推广组用户角色分配 (用户ID 76-85)
(76, 2), (77, 2), (78, 2), (79, 3), (80, 3), (81, 3), (82, 3), (83, 3), (84, 3), (85, 3);

-- 3. 插入用户部门关系
INSERT INTO sys_user_dept (user_id, dept_id) VALUES
-- 总公司用户部门关系 (用户ID 6-15 -> 部门ID 1)
(6, 1), (7, 1), (8, 1), (9, 1), (10, 1), (11, 1), (12, 1), (13, 1), (14, 1), (15, 1),

-- 技术部用户部门关系 (用户ID 16-25 -> 部门ID 2)
(16, 2), (17, 2), (18, 2), (19, 2), (20, 2), (21, 2), (22, 2), (23, 2), (24, 2), (25, 2),

-- 市场部用户部门关系 (用户ID 26-35 -> 部门ID 3)
(26, 3), (27, 3), (28, 3), (29, 3), (30, 3), (31, 3), (32, 3), (33, 3), (34, 3), (35, 3),

-- 财务部用户部门关系 (用户ID 36-45 -> 部门ID 4)
(36, 4), (37, 4), (38, 4), (39, 4), (40, 4), (41, 4), (42, 4), (43, 4), (44, 4), (45, 4),

-- 开发组用户部门关系 (用户ID 46-55 -> 部门ID 5)
(46, 5), (47, 5), (48, 5), (49, 5), (50, 5), (51, 5), (52, 5), (53, 5), (54, 5), (55, 5),

-- 测试组用户部门关系 (用户ID 56-65 -> 部门ID 6)
(56, 6), (57, 6), (58, 6), (59, 6), (60, 6), (61, 6), (62, 6), (63, 6), (64, 6), (65, 6),

-- 销售组用户部门关系 (用户ID 66-75 -> 部门ID 7)
(66, 7), (67, 7), (68, 7), (69, 7), (70, 7), (71, 7), (72, 7), (73, 7), (74, 7), (75, 7),

-- 推广组用户部门关系 (用户ID 76-85 -> 部门ID 8)
(76, 8), (77, 8), (78, 8), (79, 8), (80, 8), (81, 8), (82, 8), (83, 8), (84, 8), (85, 8);

-- 4. 插入审计日志数据
INSERT INTO sys_audit_log (username, action, resource, method, ip, result, message) VALUES
-- 总公司用户操作日志
('ceo_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('hr_001', '创建用户', '/api/platform/users', 'POST', '127.0.0.1', 'SUCCESS', '创建新用户'),
('admin_001', '更新用户', '/api/platform/users/10', 'PUT', '127.0.0.1', 'SUCCESS', '更新用户信息'),
('legal_001', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('strategy_001', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),

-- 技术部用户操作日志
('tech_001', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('tech_002', '创建角色', '/api/platform/roles', 'POST', '127.0.0.1', 'SUCCESS', '创建新角色'),
('tech_003', '更新权限', '/api/platform/permissions/5', 'PUT', '127.0.0.1', 'SUCCESS', '更新权限信息'),
('tech_004', '查看应用列表', '/api/platform/apps', 'GET', '127.0.0.1', 'SUCCESS', '查询应用列表'),
('tech_005', '查看菜单列表', '/api/platform/menus', 'GET', '127.0.0.1', 'SUCCESS', '查询菜单列表'),

-- 市场部用户操作日志
('market_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('market_002', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('market_003', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),
('market_004', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('market_005', '查看应用列表', '/api/platform/apps', 'GET', '127.0.0.1', 'SUCCESS', '查询应用列表'),

-- 财务部用户操作日志
('finance_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('finance_002', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('finance_003', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),
('finance_004', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('finance_005', '查看审计日志', '/api/platform/audit-logs', 'GET', '127.0.0.1', 'SUCCESS', '查询审计日志'),

-- 开发组用户操作日志
('dev_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('dev_002', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('dev_003', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),
('dev_004', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('dev_005', '查看应用列表', '/api/platform/apps', 'GET', '127.0.0.1', 'SUCCESS', '查询应用列表'),

-- 测试组用户操作日志
('test_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('test_002', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('test_003', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),
('test_004', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('test_005', '查看审计日志', '/api/platform/audit-logs', 'GET', '127.0.0.1', 'SUCCESS', '查询审计日志'),

-- 销售组用户操作日志
('sales_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('sales_002', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('sales_003', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),
('sales_004', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('sales_005', '查看应用列表', '/api/platform/apps', 'GET', '127.0.0.1', 'SUCCESS', '查询应用列表'),

-- 推广组用户操作日志
('promo_001', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('promo_002', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('promo_003', '查看角色列表', '/api/platform/roles', 'GET', '127.0.0.1', 'SUCCESS', '查询角色列表'),
('promo_004', '查看权限列表', '/api/platform/permissions', 'GET', '127.0.0.1', 'SUCCESS', '查询权限列表'),
('promo_005', '查看菜单列表', '/api/platform/menus', 'GET', '127.0.0.1', 'SUCCESS', '查询菜单列表');
