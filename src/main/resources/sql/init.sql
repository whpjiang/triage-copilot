CREATE TABLE IF NOT EXISTS disease_master (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    disease_code VARCHAR(64) NOT NULL UNIQUE,
    disease_name VARCHAR(255) NOT NULL,
    aliases_json TEXT,
    symptom_keywords TEXT,
    gender_rule VARCHAR(32) DEFAULT 'all',
    age_min INT,
    age_max INT,
    age_group VARCHAR(32),
    urgency_level VARCHAR(32) DEFAULT 'medium',
    review_status VARCHAR(32) DEFAULT 'approved',
    deleted TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disease_alias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    disease_code VARCHAR(64) NOT NULL,
    alias_name VARCHAR(255) NOT NULL,
    alias_type VARCHAR(64),
    source VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medical_capability_catalog (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    capability_code VARCHAR(64) NOT NULL UNIQUE,
    capability_name VARCHAR(255) NOT NULL,
    capability_type VARCHAR(64) NOT NULL,
    parent_code VARCHAR(64),
    standard_dept_code VARCHAR(64),
    aliases_json TEXT,
    gender_rule VARCHAR(32) DEFAULT 'all',
    age_min INT,
    age_max INT,
    crowd_tags_json TEXT,
    pathway_tags_json TEXT,
    active_status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disease_capability_rel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    disease_code VARCHAR(64) NOT NULL,
    capability_code VARCHAR(64) NOT NULL,
    rel_type VARCHAR(64),
    priority_score DECIMAL(8,2) DEFAULT 1.00,
    crowd_constraint VARCHAR(255),
    note VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hospital (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hospital_code VARCHAR(64) NOT NULL UNIQUE,
    hospital_name VARCHAR(255) NOT NULL,
    city VARCHAR(64),
    active_status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hospital_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hospital_id BIGINT NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    parent_department_name VARCHAR(255),
    department_intro TEXT,
    service_scope TEXT,
    active_status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    gender_rule VARCHAR(32) DEFAULT 'all',
    age_min INT,
    age_max INT,
    crowd_tags_json TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS department_capability_rel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    capability_code VARCHAR(64) NOT NULL,
    support_level VARCHAR(32) DEFAULT 'PRIMARY',
    weight DECIMAL(8,2) DEFAULT 1.00,
    source VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS import_job_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dataset_type VARCHAR(64) NOT NULL,
    file_name VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    success_count INT DEFAULT 0,
    failure_count INT DEFAULT 0,
    review_count INT DEFAULT 0,
    message VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS import_failure_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    row_number INT,
    raw_content TEXT,
    error_message VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS import_review_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    dataset_type VARCHAR(64),
    item_key VARCHAR(128),
    issue_type VARCHAR(128),
    raw_content TEXT,
    suggestion VARCHAR(500),
    resolved TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELETE FROM disease_capability_rel;
DELETE FROM department_capability_rel;
DELETE FROM disease_alias;
DELETE FROM disease_master;
DELETE FROM medical_capability_catalog;
DELETE FROM hospital_department;
DELETE FROM hospital;

INSERT INTO disease_master (disease_code, disease_name, aliases_json, symptom_keywords, gender_rule, age_min, age_max, age_group, urgency_level, review_status, deleted) VALUES
('acute_upper_respiratory_infection', '急性上呼吸道感染', '["感冒","上呼吸道感染","儿童咳嗽"]', '["发热","咳嗽","咽痛","流涕"]', 'all', 0, 80, 'all', 'medium', 'approved', 0),
('geriatric_cognitive_decline', '老年认知功能下降', '["记忆下降","老人健忘","老年记忆下降"]', '["记忆下降","反应变慢","定向力差"]', 'all', 60, 120, 'elderly', 'medium', 'approved', 0),
('pelvic_inflammatory_disease', '盆腔炎', '["女性下腹痛","盆腔感染"]', '["下腹痛","白带异常","发热"]', 'female_only', 14, 60, 'adult', 'medium', 'approved', 0),
('benign_prostatic_hyperplasia', '良性前列腺增生', '["前列腺增生","男性排尿异常"]', '["尿频","尿急","排尿困难","夜尿增多"]', 'male_only', 40, 120, 'adult', 'medium', 'approved', 0),
('lumbar_disc_herniation', '腰椎间盘突出', '["腰腿痛","腰痛伴下肢麻木"]', '["腰痛","下肢麻木","放射痛","腿痛"]', 'all', 16, 80, 'adult', 'medium', 'approved', 0),
('post_transplant_followup', '器官移植术后随访', '["移植术后复查","移植术后异常复查"]', '["移植术后","复查","排异","免疫抑制剂"]', 'all', 0, 120, 'all', 'high', 'approved', 0);

INSERT INTO disease_alias (disease_code, alias_name, alias_type, source) VALUES
('acute_upper_respiratory_infection', '儿童咳嗽', 'symptom_alias', 'seed'),
('geriatric_cognitive_decline', '老年记忆下降', 'symptom_alias', 'seed'),
('pelvic_inflammatory_disease', '女性下腹痛', 'symptom_alias', 'seed'),
('benign_prostatic_hyperplasia', '男性排尿异常', 'symptom_alias', 'seed'),
('lumbar_disc_herniation', '腰腿痛', 'symptom_alias', 'seed'),
('post_transplant_followup', '移植术后异常复查', 'symptom_alias', 'seed');

INSERT INTO medical_capability_catalog (capability_code, capability_name, capability_type, parent_code, standard_dept_code, aliases_json, gender_rule, age_min, age_max, crowd_tags_json, pathway_tags_json, active_status) VALUES
('cap_pediatrics', '儿科', 'STANDARD_DEPT', NULL, 'PED', '["儿科","儿童发热"]', 'all', 0, 14, '["child"]', '[]', 1),
('cap_pediatric_fever_clinic', '儿童发热门诊', 'SPECIAL_PATHWAY', 'cap_pediatrics', 'PED', '["儿童发热门诊","儿科发热"]', 'all', 0, 14, '["child"]', '["child_fever_pathway"]', 1),
('cap_geriatrics', '老年病科', 'SPECIAL_POPULATION', NULL, 'GER', '["老年病科","老年综合评估"]', 'all', 60, 120, '["elderly"]', '[]', 1),
('cap_memory_clinic', '记忆障碍门诊', 'SUBSPECIALTY', 'cap_geriatrics', 'GER', '["记忆门诊","认知门诊"]', 'all', 60, 120, '["elderly"]', '["elderly_multisymptom_pathway"]', 1),
('cap_gynecology', '妇科', 'STANDARD_DEPT', NULL, 'GYN', '["妇科"]', 'female_only', 14, 60, '[]', '[]', 1),
('cap_andrology', '男科', 'STANDARD_DEPT', NULL, 'AND', '["男科"]', 'male_only', 18, 120, '[]', '[]', 1),
('cap_male_urinary_clinic', '男性排尿异常门诊', 'SUBSPECIALTY', 'cap_andrology', 'AND', '["排尿异常门诊","前列腺专病"]', 'male_only', 18, 120, '[]', '["male_urinary_pathway"]', 1),
('cap_spine_surgery', '脊柱外科', 'SUBSPECIALTY', 'cap_orthopedics', 'ORT', '["脊柱外科","腰椎专病"]', 'all', 16, 80, '[]', '["spine_pathway"]', 1),
('cap_spine_pain_clinic', '脊柱疼痛专病门诊', 'SUBSPECIALTY', 'cap_spine_surgery', 'ORT', '["腰腿痛门诊","脊柱疼痛门诊"]', 'all', 16, 80, '[]', '["spine_pathway"]', 1),
('cap_transplant_followup', '器官移植随访', 'SPECIAL_PATHWAY', NULL, 'TRP', '["移植随访","移植门诊"]', 'all', 0, 120, '["transplant_followup"]', '["transplant_followup"]', 1);

INSERT INTO disease_capability_rel (disease_code, capability_code, rel_type, priority_score, crowd_constraint, note) VALUES
('acute_upper_respiratory_infection', 'cap_pediatrics', 'PRIMARY', 1.20, 'child', '儿童发热与咳嗽优先儿科'),
('acute_upper_respiratory_infection', 'cap_pediatric_fever_clinic', 'SECONDARY', 1.35, 'child', '儿童发热可进一步召回专病路径'),
('geriatric_cognitive_decline', 'cap_geriatrics', 'PRIMARY', 1.30, 'elderly', '老年记忆下降优先老年病科路径'),
('geriatric_cognitive_decline', 'cap_memory_clinic', 'SECONDARY', 1.45, 'elderly', '老年认知下降可细化到记忆障碍门诊'),
('pelvic_inflammatory_disease', 'cap_gynecology', 'PRIMARY', 1.20, 'female', '女性下腹痛优先妇科'),
('benign_prostatic_hyperplasia', 'cap_andrology', 'PRIMARY', 1.20, 'male', '男性排尿异常优先男科'),
('benign_prostatic_hyperplasia', 'cap_male_urinary_clinic', 'SECONDARY', 1.40, 'male', '排尿异常可细化到男性排尿异常门诊'),
('lumbar_disc_herniation', 'cap_spine_surgery', 'PRIMARY', 1.40, NULL, '腰腿痛场景优先脊柱外科'),
('lumbar_disc_herniation', 'cap_spine_pain_clinic', 'SECONDARY', 1.50, NULL, '腰腿痛可细化到脊柱疼痛专病门诊'),
('post_transplant_followup', 'cap_transplant_followup', 'PRIMARY', 1.60, 'transplant_followup', '移植术后复查走特殊路径');

INSERT INTO hospital (id, hospital_code, hospital_name, city, active_status, deleted) VALUES
(1, 'shanghai_first_people_hospital', '上海市第一人民医院', '上海', 1, 0),
(2, 'shanghai_union_medical_center', '上海联合医学中心', '上海', 1, 0);

INSERT INTO hospital_department (id, hospital_id, department_name, parent_department_name, department_intro, service_scope, active_status, deleted, gender_rule, age_min, age_max, crowd_tags_json) VALUES
(1, 1, '儿科门诊', '儿科', '儿童常见病诊疗', '发热、咳嗽、上呼吸道症状', 1, 0, 'all', 0, 14, '["child"]'),
(7, 1, '儿童发热门诊', '儿科', '儿童发热快速评估门诊', '儿童发热、急性呼吸道症状', 1, 0, 'all', 0, 14, '["child"]'),
(2, 1, '老年病科', '内科', '老年综合评估门诊', '认知下降、慢病共管、多症状老年患者', 1, 0, 'all', 60, 120, '["elderly"]'),
(8, 1, '记忆障碍门诊', '老年病科', '老年认知与记忆专病门诊', '记忆下降、认知障碍、反应变慢', 1, 0, 'all', 60, 120, '["elderly"]'),
(3, 1, '妇科门诊', '妇产科', '女性专科门诊', '女性下腹痛、盆腔炎、月经异常', 1, 0, 'female_only', 14, 60, '[]'),
(4, 1, '男科门诊', '泌尿外科', '男性专科门诊', '排尿异常、前列腺问题', 1, 0, 'male_only', 18, 120, '[]'),
(9, 1, '男性排尿异常门诊', '男科', '男性排尿问题专病门诊', '尿频、尿急、夜尿增多、前列腺相关问题', 1, 0, 'male_only', 18, 120, '[]'),
(5, 1, '脊柱外科门诊', '骨科', '脊柱专病门诊', '腰椎间盘突出、腰腿痛、下肢麻木', 1, 0, 'all', 16, 80, '[]'),
(10, 1, '脊柱疼痛专病门诊', '脊柱外科', '腰腿痛专病门诊', '腰腿痛、坐骨神经痛、慢性腰痛', 1, 0, 'all', 16, 80, '[]'),
(6, 2, '器官移植随访门诊', '移植医学中心', '移植术后专病路径', '术后复查、排异监测、免疫抑制剂调整', 1, 0, 'all', 0, 120, '["transplant_followup"]');

INSERT INTO department_capability_rel (department_id, capability_code, support_level, weight, source) VALUES
(1, 'cap_pediatrics', 'PRIMARY', 1.00, 'seed'),
(7, 'cap_pediatric_fever_clinic', 'PRIMARY', 1.20, 'seed'),
(2, 'cap_geriatrics', 'PRIMARY', 1.00, 'seed'),
(8, 'cap_memory_clinic', 'PRIMARY', 1.25, 'seed'),
(3, 'cap_gynecology', 'PRIMARY', 1.00, 'seed'),
(4, 'cap_andrology', 'PRIMARY', 1.00, 'seed'),
(9, 'cap_male_urinary_clinic', 'PRIMARY', 1.20, 'seed'),
(5, 'cap_spine_surgery', 'PRIMARY', 1.10, 'seed'),
(10, 'cap_spine_pain_clinic', 'PRIMARY', 1.25, 'seed'),
(6, 'cap_transplant_followup', 'PRIMARY', 1.20, 'seed');
