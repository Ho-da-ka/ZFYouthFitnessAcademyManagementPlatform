SET NAMES utf8mb4;

INSERT INTO coaches (coach_code, name, gender, phone, specialty, status, remarks)
VALUES
    ('DEMO-C001', '张晨', 'MALE', '13826050001', '爆发力与跳跃专项', 'ACTIVE', '演示数据：主带少年爆发力班'),
    ('DEMO-C002', '李瑶', 'FEMALE', '13826050002', '协调敏捷与体态控制', 'ACTIVE', '演示数据：主带敏捷基础班'),
    ('DEMO-C003', '陈浩', 'MALE', '13826050003', '耐力训练与课堂管理', 'ACTIVE', '演示数据：周末耐力课程'),
    ('DEMO-C004', '王欣', 'FEMALE', '13826050004', '核心稳定与柔韧恢复', 'ACTIVE', '演示数据：小组训练与恢复课')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    gender = VALUES(gender),
    phone = VALUES(phone),
    specialty = VALUES(specialty),
    status = VALUES(status),
    remarks = VALUES(remarks);

INSERT INTO students (student_no, name, gender, birth_date, guardian_name, guardian_phone, status, remarks, goal_focus, training_tags, risk_notes, goal_start_date, goal_end_date)
VALUES
    ('DEMO-S001', '王小明', 'MALE', '2014-03-12', '王女士', '13926050001', 'ACTIVE', '演示数据：爆发力重点跟进', '爆发力提升', '跳跃,核心稳定,下肢力量', '落地膝内扣需关注', '2026-05-01', '2026-07-31'),
    ('DEMO-S002', '刘子涵', 'FEMALE', '2015-08-24', '刘先生', '13926050002', 'ACTIVE', '演示数据：出勤波动', '耐力与节奏控制', '耐力,跑姿,呼吸节奏', '近期出勤不稳定', '2026-05-01', '2026-07-31'),
    ('DEMO-S003', '陈一诺', 'FEMALE', '2013-11-05', '陈女士', '13926050003', 'ACTIVE', '演示数据：协调能力好', '协调敏捷提升', '敏捷梯,反应,平衡', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S004', '赵宇航', 'MALE', '2014-06-19', '赵先生', '13926050004', 'ACTIVE', '演示数据：核心稳定不足', '核心稳定强化', '核心,髋稳定,支撑', '平板支撑后程塌腰', '2026-05-01', '2026-07-31'),
    ('DEMO-S005', '周语桐', 'FEMALE', '2016-01-28', '周女士', '13926050005', 'ACTIVE', '演示数据：柔韧恢复', '柔韧与体态改善', '柔韧,体态,恢复', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S006', '黄俊杰', 'MALE', '2013-09-14', '黄先生', '13926050006', 'ACTIVE', '演示数据：耐力提升明显', '耐力提升', '耐力,间歇跑,心肺', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S007', '林可欣', 'FEMALE', '2015-04-09', '林女士', '13926050007', 'ACTIVE', '演示数据：课堂投入高', '反应速度提升', '反应,协调,游戏化训练', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S008', '何沐阳', 'MALE', '2014-12-30', '何女士', '13926050008', 'ACTIVE', '演示数据：跳跃专项', '跳跃能力提升', '跳跃,爆发力,摆臂', '起跳摆臂不同步', '2026-05-01', '2026-07-31'),
    ('DEMO-S009', '孙嘉乐', 'MALE', '2016-07-18', '孙先生', '13926050009', 'ACTIVE', '演示数据：新生适应期', '基础体能建立', '基础动作,规则意识,趣味体能', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S010', '唐雨菲', 'FEMALE', '2015-10-11', '唐女士', '13926050010', 'ACTIVE', '演示数据：阶段评估完整', '柔韧与协调', '柔韧,协调,核心', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S011', '马思远', 'MALE', '2013-05-22', '马先生', '13926050011', 'ACTIVE', '演示数据：高阶训练', '综合能力巩固', '速度,耐力,核心', '', '2026-05-01', '2026-07-31'),
    ('DEMO-S012', '许安然', 'FEMALE', '2016-02-03', '许女士', '13926050012', 'ACTIVE', '演示数据：低龄启蒙', '动作习惯养成', '平衡,协调,柔韧', '', '2026-05-01', '2026-07-31')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    gender = VALUES(gender),
    birth_date = VALUES(birth_date),
    guardian_name = VALUES(guardian_name),
    guardian_phone = VALUES(guardian_phone),
    status = VALUES(status),
    remarks = VALUES(remarks),
    goal_focus = VALUES(goal_focus),
    training_tags = VALUES(training_tags),
    risk_notes = VALUES(risk_notes),
    goal_start_date = VALUES(goal_start_date),
    goal_end_date = VALUES(goal_end_date);

INSERT INTO user_accounts (username, password_hash, role, status, coach_id)
SELECT CONCAT('coach_', LOWER(coach_code)),
       (SELECT password_hash FROM user_accounts WHERE username = 'coach' LIMIT 1),
       'COACH',
       status,
       id
FROM coaches
WHERE coach_code LIKE 'DEMO-C%'
ON DUPLICATE KEY UPDATE
    role = VALUES(role),
    status = VALUES(status),
    coach_id = VALUES(coach_id);

INSERT INTO user_accounts (username, password_hash, role, status, student_id)
SELECT CONCAT('student_', LOWER(student_no)),
       (SELECT password_hash FROM user_accounts WHERE username = 'student' LIMIT 1),
       'STUDENT',
       status,
       id
FROM students
WHERE student_no LIKE 'DEMO-S%'
ON DUPLICATE KEY UPDATE
    role = VALUES(role),
    status = VALUES(status),
    student_id = VALUES(student_id);

INSERT INTO user_accounts (username, password_hash, role, status)
SELECT guardian_phone,
       (SELECT password_hash FROM user_accounts WHERE username = 'parent' LIMIT 1),
       'PARENT',
       'ACTIVE'
FROM students
WHERE student_no LIKE 'DEMO-S%'
ON DUPLICATE KEY UPDATE
    role = VALUES(role),
    status = VALUES(status);

INSERT INTO parent_accounts (user_account_id, display_name, phone, status)
SELECT ua.id, s.guardian_name, s.guardian_phone, 'ACTIVE'
FROM students s
JOIN user_accounts ua ON ua.username = s.guardian_phone
WHERE s.student_no LIKE 'DEMO-S%'
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    status = VALUES(status);

INSERT INTO parent_student_relations (parent_account_id, student_id, binding_type)
SELECT pa.id, s.id, 'AUTO'
FROM students s
JOIN parent_accounts pa ON pa.phone = s.guardian_phone
WHERE s.student_no LIKE 'DEMO-S%'
ON DUPLICATE KEY UPDATE
    binding_type = VALUES(binding_type);

INSERT INTO courses (course_code, name, course_type, coach_name, venue, start_time, duration_minutes, max_capacity, course_date, class_start_time, class_end_time, status, description, training_theme, target_age_range, target_goals, focus_points)
VALUES
    ('DEMO-EXP-0501', '少年爆发力A班', '专项提升', '张晨', '一号训练馆', '2026-05-01 17:30:00', 60, 16, '2026-05-01', '17:30:00', '18:30:00', 'COMPLETED', '跳跃与下肢爆发力专项', '立定跳远与加速启动', '10-14岁', '提升起跳速度、摆臂协调与落地稳定', '起跳角度,髋膝踝协同,落地缓冲'),
    ('DEMO-COORD-0502', '协调敏捷基础班', '基础训练', '李瑶', '二号训练馆', '2026-05-02 16:30:00', 60, 14, '2026-05-02', '16:30:00', '17:30:00', 'COMPLETED', '敏捷梯与反应游戏', '协调敏捷与平衡控制', '8-12岁', '提升反应速度、脚步节奏和身体控制', '敏捷梯,侧向移动,单脚平衡'),
    ('DEMO-END-0503', '周末耐力提升课', '耐力训练', '陈浩', '户外操场', '2026-05-03 09:30:00', 75, 20, '2026-05-03', '09:30:00', '10:45:00', 'ONGOING', '间歇跑与节奏呼吸', '耐力与节奏控制', '10-15岁', '提升持续运动能力与配速意识', '呼吸节奏,步频控制,恢复心率'),
    ('DEMO-CORE-0505', '核心稳定小组课', '小组精品', '王欣', '三号训练馆', '2026-05-05 18:00:00', 60, 10, '2026-05-05', '18:00:00', '19:00:00', 'PLANNED', '核心支撑与髋稳定', '核心稳定与动作质量', '9-14岁', '改善支撑质量与跑跳动作稳定', '平板支撑,臀桥,侧桥'),
    ('DEMO-FLEX-0507', '柔韧恢复课', '恢复训练', '王欣', '康复训练区', '2026-05-07 17:00:00', 50, 12, '2026-05-07', '17:00:00', '17:50:00', 'PLANNED', '柔韧性与运动后恢复', '柔韧恢复与体态改善', '8-13岁', '改善肩髋灵活性与体态控制', '动态拉伸,髋关节活动,体前屈'),
    ('DEMO-PARENT-0508', '亲子体能体验课', '亲子体验', '李瑶', '一号训练馆', '2026-05-08 19:00:00', 60, 18, '2026-05-08', '19:00:00', '20:00:00', 'PLANNED', '亲子协作与趣味体能挑战', '亲子协作体能', '6-10岁', '提升亲子参与度与基础运动兴趣', '协作游戏,基础跳跃,平衡挑战')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    course_type = VALUES(course_type),
    coach_name = VALUES(coach_name),
    venue = VALUES(venue),
    start_time = VALUES(start_time),
    duration_minutes = VALUES(duration_minutes),
    max_capacity = VALUES(max_capacity),
    course_date = VALUES(course_date),
    class_start_time = VALUES(class_start_time),
    class_end_time = VALUES(class_end_time),
    status = VALUES(status),
    description = VALUES(description),
    training_theme = VALUES(training_theme),
    target_age_range = VALUES(target_age_range),
    target_goals = VALUES(target_goals),
    focus_points = VALUES(focus_points);

INSERT INTO attendance_records (student_id, course_id, attendance_date, status, note)
SELECT s.id, c.id, c.course_date, 'PRESENT', '演示数据：正常签到'
FROM students s JOIN courses c
WHERE s.student_no IN ('DEMO-S001','DEMO-S003','DEMO-S004','DEMO-S008')
  AND c.course_code = 'DEMO-EXP-0501'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records a WHERE a.student_id = s.id AND a.course_id = c.id AND a.attendance_date = c.course_date
  );

INSERT INTO attendance_records (student_id, course_id, attendance_date, status, note)
SELECT s.id, c.id, c.course_date, 'PRESENT', '演示数据：协调课出勤'
FROM students s JOIN courses c
WHERE s.student_no IN ('DEMO-S003','DEMO-S005','DEMO-S007','DEMO-S010','DEMO-S012')
  AND c.course_code = 'DEMO-COORD-0502'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records a WHERE a.student_id = s.id AND a.course_id = c.id AND a.attendance_date = c.course_date
  );

INSERT INTO attendance_records (student_id, course_id, attendance_date, status, note)
SELECT s.id, c.id, c.course_date,
       CASE s.student_no WHEN 'DEMO-S002' THEN 'ABSENT' WHEN 'DEMO-S009' THEN 'LEAVE' ELSE 'PRESENT' END,
       CASE s.student_no WHEN 'DEMO-S002' THEN '演示数据：未到课，需联系家长' WHEN 'DEMO-S009' THEN '演示数据：请假' ELSE '演示数据：耐力课出勤' END
FROM students s JOIN courses c
WHERE s.student_no IN ('DEMO-S002','DEMO-S006','DEMO-S009','DEMO-S011')
  AND c.course_code = 'DEMO-END-0503'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records a WHERE a.student_id = s.id AND a.course_id = c.id AND a.attendance_date = c.course_date
  );

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-03', '立定跳远、加速启动、落地缓冲', 60, 'MEDIUM', '起跳速度和摆臂节奏明显改善', '连续三次立定跳远成绩提升，起跳更果断', '落地时膝盖仍偶有内扣', '家中练习深蹲落地缓冲，每组8次', '更新下一阶段爆发力训练计划，加入单脚落地稳定', '适合下节课加入弹力带髋外展激活', '基于训练记录生成：爆发力提升明显，建议继续强化落地控制'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S001' AND c.course_code = 'DEMO-EXP-0501'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-03' AND t.training_content = '立定跳远、加速启动、落地缓冲');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-03', '间歇跑、节奏呼吸、恢复拉伸', 75, 'MEDIUM', '前半段配速稳定，后半段注意力下降', '前两组间歇跑完成质量好', '第三组后呼吸节奏乱，出勤也需持续跟进', '记录睡眠和课前饮水情况', '先稳定出勤，再逐步增加间歇跑组数', '建议家长协助确认缺勤原因', '基于训练记录生成：耐力基础可继续建立，先处理出勤波动'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S002' AND c.course_code = 'DEMO-END-0503'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-03' AND t.training_content = '间歇跑、节奏呼吸、恢复拉伸');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-02', '敏捷梯、反应球、单脚平衡', 60, 'LOW', '脚步节奏清晰，反应速度较好', '敏捷梯连续动作几乎无失误', '单脚闭眼平衡时间偏短', '每天做2组单脚站立练习', '增加动态平衡与方向变化训练', '课堂投入高，可作为小组示范', '基于训练记录生成：协调敏捷表现突出，可提高动态平衡难度'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S003' AND c.course_code = 'DEMO-COORD-0502'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-02' AND t.training_content = '敏捷梯、反应球、单脚平衡');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-01', '核心支撑、臀桥、侧向移动', 60, 'MEDIUM', '支撑动作理解较快，但后程稳定性不足', '臀桥动作完成度高', '平板支撑30秒后出现塌腰', '睡前完成两组死虫动作', '下次加入低强度核心循环', '注意控制动作质量，不追求次数', '基于训练记录生成：核心稳定需持续强化'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S004' AND c.course_code = 'DEMO-EXP-0501'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-01' AND t.training_content = '核心支撑、臀桥、侧向移动');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-02', '动态拉伸、髋关节活动、体前屈', 50, 'LOW', '柔韧性进步，动作完成更放松', '坐位体前屈动作紧张感减少', '肩部活动度仍需打开', '课后保持5分钟拉伸', '加入肩胸打开和猫牛式练习', '适合恢复课继续跟进', '基于训练记录生成：柔韧恢复方向正确'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S005' AND c.course_code = 'DEMO-COORD-0502'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-02' AND t.training_content = '动态拉伸、髋关节活动、体前屈');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-03', '800米节奏跑、折返跑、放松跑', 75, 'HIGH', '节奏跑完成稳定，恢复速度快', '最后一组仍能保持步频', '跑姿上身略前倾', '家中做靠墙站姿调整', '继续做配速训练，加入跑姿纠正', '耐力课表现稳定', '基于训练记录生成：耐力提升明显'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S006' AND c.course_code = 'DEMO-END-0503'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-03' AND t.training_content = '800米节奏跑、折返跑、放松跑');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-02', '反应游戏、跨步协调、平衡挑战', 60, 'LOW', '参与度高，反应任务完成快', '反应球接球成功率高', '侧向跨步时身体重心偏高', '亲子小游戏练习快速转身', '加入低重心侧移训练', '可提升动作控制难度', '基于训练记录生成：反应速度好，重心控制需加强'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S007' AND c.course_code = 'DEMO-COORD-0502'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-02' AND t.training_content = '反应游戏、跨步协调、平衡挑战');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-01', '摆臂起跳、连续跳、落地稳定', 60, 'MEDIUM', '跳跃积极性高，连续跳节奏有改善', '摆臂幅度比上周更充分', '起跳和摆臂仍有半拍延迟', '镜前练习摆臂节奏', '继续拆分起跳与摆臂配合', '下次可拍视频复盘', '基于训练记录生成：跳跃专项进步中'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S008' AND c.course_code = 'DEMO-EXP-0501'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-01' AND t.training_content = '摆臂起跳、连续跳、落地稳定');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-03', '基础跳跃、平衡木、趣味接力', 60, 'LOW', '新生适应良好，能跟随课堂规则', '平衡木通过率高，课堂情绪稳定', '接力环节容易提前启动', '家长可做口令等待小游戏', '继续强化规则意识和启动控制', '低龄启蒙阶段以稳定参与为主', '基于训练记录生成：新生适应良好，继续建立规则意识'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S009' AND c.course_code = 'DEMO-END-0503'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-03' AND t.training_content = '基础跳跃、平衡木、趣味接力');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-02', '柔韧拉伸、反应步伐、核心小游戏', 60, 'LOW', '柔韧动作完成轻松，协调反应稳定', '坐位体前屈和动态拉伸表现好', '核心小游戏中支撑时间偏短', '每天练习20秒平板支撑', '增加短时多组核心支撑', '适合加入核心稳定小组课', '基于训练记录生成：柔韧优势明显，核心支撑可加强'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S010' AND c.course_code = 'DEMO-COORD-0502'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-02' AND t.training_content = '柔韧拉伸、反应步伐、核心小游戏');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-03', '节奏跑、折返启动、核心收尾', 75, 'HIGH', '综合能力较均衡，能带动小组节奏', '折返启动速度快，节奏跑稳定', '核心收尾动作略急', '训练后做充分拉伸放松', '提高动作质量要求，减少抢节奏', '高阶学员可安排挑战任务', '基于训练记录生成：综合能力稳定，可进入高阶挑战'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S011' AND c.course_code = 'DEMO-END-0503'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-03' AND t.training_content = '节奏跑、折返启动、核心收尾');

INSERT INTO training_records (student_id, course_id, training_date, training_content, duration_minutes, intensity_level, performance_summary, highlight_note, improvement_note, parent_action, next_step_suggestion, coach_comment, ai_summary)
SELECT s.id, c.id, '2026-05-02', '平衡挑战、柔韧拉伸、亲子协作游戏', 60, 'LOW', '低龄启蒙参与度好，平衡任务完成稳定', '单脚平衡时间比上周延长', '协作游戏中注意力容易分散', '家中做一分钟平衡小游戏', '继续用游戏化方式强化专注', '适合亲子体验课继续巩固', '基于训练记录生成：平衡能力提升，专注度需游戏化引导'
FROM students s JOIN courses c
WHERE s.student_no = 'DEMO-S012' AND c.course_code = 'DEMO-COORD-0502'
  AND NOT EXISTS (SELECT 1 FROM training_records t WHERE t.student_id = s.id AND t.course_id = c.id AND t.training_date = '2026-05-02' AND t.training_content = '平衡挑战、柔韧拉伸、亲子协作游戏');

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-03', '立定跳远', 190, 'cm', '较上次提升，起跳更果断' FROM students s
WHERE student_no = 'DEMO-S001'
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-03' AND f.item_name = '立定跳远' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-03', '800米跑', 245, '秒', '后半程配速下降' FROM students s
WHERE student_no = 'DEMO-S002'
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-03' AND f.item_name = '800米跑' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-02', '敏捷T测试', 11.8, '秒', '方向切换速度较好' FROM students s
WHERE student_no = 'DEMO-S003'
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-02' AND f.item_name = '敏捷T测试' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-01', '平板支撑', 42, '秒', '后程核心稳定不足' FROM students s
WHERE student_no = 'DEMO-S004'
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-01' AND f.item_name = '平板支撑' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-02', '坐位体前屈', 12.5, 'cm', '柔韧性持续改善' FROM students s
WHERE student_no IN ('DEMO-S005','DEMO-S010','DEMO-S012')
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-02' AND f.item_name = '坐位体前屈' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-03', '800米跑', 226, '秒', '耐力表现稳定' FROM students s
WHERE student_no IN ('DEMO-S006','DEMO-S011')
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-03' AND f.item_name = '800米跑' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-02', '反应接球', 18, '次', '反应速度较好' FROM students s
WHERE student_no = 'DEMO-S007'
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-02' AND f.item_name = '反应接球' AND f.deleted = 0);

INSERT INTO fitness_test_records (student_id, student_name_snapshot, test_date, item_name, test_value, unit, comment)
SELECT id, name, '2026-05-01', '连续跳', 24, '次', '摆臂节奏需要继续统一' FROM students s
WHERE student_no = 'DEMO-S008'
  AND NOT EXISTS (SELECT 1 FROM fitness_test_records f WHERE f.student_id = s.id AND f.test_date = '2026-05-01' AND f.item_name = '连续跳' AND f.deleted = 0);

INSERT INTO stage_evaluations (student_id, cycle_name, period_start, period_end, attendance_rate, training_summary, fitness_summary, coach_evaluation, next_stage_plan, ai_interpretation, parent_report)
SELECT s.id, '2026年5月上旬成长评估', '2026-05-01', '2026-05-10', 0.9000, '完成爆发力与跳跃专项训练，课堂专注度较好', '立定跳远达到190cm，爆发力提升明显', '起跳速度提升，落地稳定仍需关注', '加入单脚落地稳定与核心控制训练', 'AI解读：爆发力提升明显，建议下一阶段兼顾落地稳定与髋膝控制。', '王小明本阶段爆发力进步明显，后续将继续提升落地稳定性，请家长配合完成低强度落地缓冲练习。'
FROM students s
WHERE s.student_no = 'DEMO-S001'
  AND NOT EXISTS (SELECT 1 FROM stage_evaluations e WHERE e.student_id = s.id AND e.cycle_name = '2026年5月上旬成长评估');

INSERT INTO stage_evaluations (student_id, cycle_name, period_start, period_end, attendance_rate, training_summary, fitness_summary, coach_evaluation, next_stage_plan, ai_interpretation, parent_report)
SELECT s.id, '2026年5月上旬成长评估', '2026-05-01', '2026-05-10', 0.6500, '耐力训练完成度受出勤影响', '800米跑后半程配速下降', '建议先稳定出勤，再提升训练量', '联系家长确认缺勤原因，逐步恢复间歇跑', 'AI解读：出勤波动影响耐力训练连续性，优先处理到课稳定性。', '刘子涵本阶段耐力基础仍在建立中，建议先保证连续到课，再逐步提高训练负荷。'
FROM students s
WHERE s.student_no = 'DEMO-S002'
  AND NOT EXISTS (SELECT 1 FROM stage_evaluations e WHERE e.student_id = s.id AND e.cycle_name = '2026年5月上旬成长评估');

INSERT INTO stage_evaluations (student_id, cycle_name, period_start, period_end, attendance_rate, training_summary, fitness_summary, coach_evaluation, next_stage_plan, ai_interpretation, parent_report)
SELECT s.id, '2026年5月上旬成长评估', '2026-05-01', '2026-05-10', 1.0000, '协调敏捷课程表现稳定', '敏捷T测试完成质量好', '脚步节奏清晰，动态平衡可继续挑战', '提高动态平衡和方向切换难度', 'AI解读：协调敏捷表现突出，可进入更复杂的方向变化训练。', '陈一诺协调能力表现优秀，下一阶段会增加方向变化和动态平衡挑战。'
FROM students s
WHERE s.student_no = 'DEMO-S003'
  AND NOT EXISTS (SELECT 1 FROM stage_evaluations e WHERE e.student_id = s.id AND e.cycle_name = '2026年5月上旬成长评估');

INSERT INTO stage_evaluations (student_id, cycle_name, period_start, period_end, attendance_rate, training_summary, fitness_summary, coach_evaluation, next_stage_plan, ai_interpretation, parent_report)
SELECT s.id, '2026年5月上旬成长评估', '2026-05-01', '2026-05-10', 1.0000, '耐力课完成稳定，节奏跑质量高', '800米跑表现稳定，恢复速度快', '耐力基础较好，可提高跑姿要求', '加入跑姿纠正和高阶配速任务', 'AI解读：耐力表现稳定，下一阶段适合提升跑姿质量与配速意识。', '黄俊杰耐力表现稳定，后续会加入跑姿优化和高阶配速任务。'
FROM students s
WHERE s.student_no = 'DEMO-S006'
  AND NOT EXISTS (SELECT 1 FROM stage_evaluations e WHERE e.student_id = s.id AND e.cycle_name = '2026年5月上旬成长评估');

INSERT INTO stage_evaluations (student_id, cycle_name, period_start, period_end, attendance_rate, training_summary, fitness_summary, coach_evaluation, next_stage_plan, ai_interpretation, parent_report)
SELECT s.id, '2026年5月上旬成长评估', '2026-05-01', '2026-05-10', 1.0000, '柔韧与协调训练完成度高', '坐位体前屈表现较好', '柔韧优势明显，核心支撑需补强', '加入短时多组核心支撑训练', 'AI解读：柔韧表现好，核心支撑是下一阶段关键补强点。', '唐雨菲柔韧表现优秀，下一阶段会增加核心支撑训练，帮助动作更稳定。'
FROM students s
WHERE s.student_no = 'DEMO-S010'
  AND NOT EXISTS (SELECT 1 FROM stage_evaluations e WHERE e.student_id = s.id AND e.cycle_name = '2026年5月上旬成长评估');

INSERT INTO stage_evaluations (student_id, cycle_name, period_start, period_end, attendance_rate, training_summary, fitness_summary, coach_evaluation, next_stage_plan, ai_interpretation, parent_report)
SELECT s.id, '2026年5月上旬成长评估', '2026-05-01', '2026-05-10', 0.9000, '低龄启蒙参与稳定，平衡任务进步', '单脚平衡时间提升', '游戏化训练效果好，注意力仍需引导', '继续亲子游戏化练习与专注训练', 'AI解读：平衡能力提升，建议用游戏化任务延长专注时间。', '许安然平衡能力有进步，建议家中用小游戏继续巩固专注和动作控制。'
FROM students s
WHERE s.student_no = 'DEMO-S012'
  AND NOT EXISTS (SELECT 1 FROM stage_evaluations e WHERE e.student_id = s.id AND e.cycle_name = '2026年5月上旬成长评估');

INSERT INTO course_bookings (parent_account_id, student_id, course_id, booking_status, course_capacity, booking_remark, checkin_status, checkin_time)
SELECT pa.id, s.id, c.id, 'BOOKED', c.max_capacity, '演示数据：家长端课程预约', 'PENDING', NULL
FROM students s
JOIN parent_accounts pa ON pa.phone = s.guardian_phone
JOIN courses c
WHERE s.student_no IN ('DEMO-S001','DEMO-S003','DEMO-S004','DEMO-S006','DEMO-S010','DEMO-S012')
  AND c.course_code = 'DEMO-CORE-0505'
  AND NOT EXISTS (SELECT 1 FROM course_bookings b WHERE b.parent_account_id = pa.id AND b.student_id = s.id AND b.course_id = c.id);

INSERT INTO course_bookings (parent_account_id, student_id, course_id, booking_status, course_capacity, booking_remark, checkin_status, checkin_time)
SELECT pa.id, s.id, c.id, 'BOOKED', c.max_capacity, '演示数据：亲子体验课预约', 'PENDING', NULL
FROM students s
JOIN parent_accounts pa ON pa.phone = s.guardian_phone
JOIN courses c
WHERE s.student_no IN ('DEMO-S007','DEMO-S009','DEMO-S012')
  AND c.course_code = 'DEMO-PARENT-0508'
  AND NOT EXISTS (SELECT 1 FROM course_bookings b WHERE b.parent_account_id = pa.id AND b.student_id = s.id AND b.course_id = c.id);

INSERT INTO in_app_messages (parent_account_id, title, content, msg_type, is_read)
SELECT pa.id, '训练报告已生成', CONCAT(s.name, '的阶段成长报告已生成，可在成长总览查看。'), 'GROWTH_REPORT', 0
FROM students s
JOIN parent_accounts pa ON pa.phone = s.guardian_phone
WHERE s.student_no IN ('DEMO-S001','DEMO-S002','DEMO-S003')
  AND NOT EXISTS (SELECT 1 FROM in_app_messages m WHERE m.parent_account_id = pa.id AND m.title = '训练报告已生成' AND m.content = CONCAT(s.name, '的阶段成长报告已生成，可在成长总览查看。'));

INSERT INTO in_app_messages (parent_account_id, title, content, msg_type, is_read)
SELECT pa.id, '课程预约成功', CONCAT(s.name, '已预约核心稳定小组课，请按时到场。'), 'BOOKING', 0
FROM students s
JOIN parent_accounts pa ON pa.phone = s.guardian_phone
WHERE s.student_no IN ('DEMO-S001','DEMO-S004','DEMO-S006')
  AND NOT EXISTS (SELECT 1 FROM in_app_messages m WHERE m.parent_account_id = pa.id AND m.title = '课程预约成功' AND m.content = CONCAT(s.name, '已预约核心稳定小组课，请按时到场。'));

INSERT INTO care_alerts (student_id, alert_type, alert_title, alert_content, status, triggered_at)
SELECT s.id, 'ATTENDANCE', '出勤连续性提醒', '本月出勤存在波动，建议联系家长确认近期安排。', 'OPEN', '2026-05-03 12:00:00'
FROM students s
WHERE s.student_no = 'DEMO-S002'
  AND NOT EXISTS (SELECT 1 FROM care_alerts a WHERE a.student_id = s.id AND a.alert_title = '出勤连续性提醒' AND a.status = 'OPEN');

INSERT INTO care_alerts (student_id, alert_type, alert_title, alert_content, status, triggered_at)
SELECT s.id, 'TRAINING', '落地稳定性提醒', '跳跃能力提升明显，但落地膝内扣仍需跟进。', 'OPEN', '2026-05-03 12:30:00'
FROM students s
WHERE s.student_no = 'DEMO-S001'
  AND NOT EXISTS (SELECT 1 FROM care_alerts a WHERE a.student_id = s.id AND a.alert_title = '落地稳定性提醒' AND a.status = 'OPEN');
