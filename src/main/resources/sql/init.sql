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
    district_name VARCHAR(64),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    hospital_level VARCHAR(32),
    is_emergency TINYINT DEFAULT 0,
    authority_score DECIMAL(10,2) DEFAULT 0,
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
    standard_dept_code VARCHAR(64),
    subspecialty_code VARCHAR(64),
    district_name VARCHAR(64),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    is_emergency TINYINT DEFAULT 0,
    national_key_score DECIMAL(10,2) DEFAULT 0,
    provincial_key_score DECIMAL(10,2) DEFAULT 0,
    city_key_score DECIMAL(10,2) DEFAULT 0,
    authority_score DECIMAL(10,2) DEFAULT 0,
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
    auto_mapped_count INT DEFAULT 0,
    message VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS import_failure_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    `row_number` INT,
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
    resolution_note VARCHAR(500),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctor_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hospital_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    doctor_name VARCHAR(128) NOT NULL,
    title VARCHAR(64),
    specialty_text TEXT,
    gender_rule VARCHAR(32) DEFAULT 'all',
    age_min INT,
    age_max INT,
    crowd_tags_json TEXT,
    authority_score DECIMAL(10,2) DEFAULT 0,
    academic_title_score DECIMAL(10,2) DEFAULT 0,
    is_expert TINYINT DEFAULT 0,
    campus_name VARCHAR(128),
    active_status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctor_capability_rel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    capability_code VARCHAR(64),
    weight DECIMAL(8,2) DEFAULT 0.30,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_recall_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    symptoms TEXT,
    gender VARCHAR(32),
    age INT,
    age_group VARCHAR(32),
    eligible_disease_count INT DEFAULT 0,
    rule_candidate_codes_json TEXT,
    suggested_codes_json TEXT,
    status VARCHAR(64),
    message VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS triage_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64),
    dialog_id VARCHAR(64),
    current_stage VARCHAR(32),
    ask_round INT DEFAULT 0,
    invalid_answer_count INT DEFAULT 0,
    city VARCHAR(64),
    area VARCHAR(64),
    nearby TINYINT DEFAULT 0,
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    patient_age INT,
    patient_gender VARCHAR(32),
    severity_level VARCHAR(32),
    route_type VARCHAR(32),
    status VARCHAR(32) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS triage_turn (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    turn_no INT NOT NULL,
    user_message TEXT,
    normalized_query TEXT,
    intent VARCHAR(64),
    stage VARCHAR(32),
    reply_text TEXT,
    raw_decision_json TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS triage_slot_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    symptoms_json TEXT,
    disease_name VARCHAR(255),
    target_hospital VARCHAR(255),
    target_department VARCHAR(255),
    target_doctor VARCHAR(255),
    missing_slots_json TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELETE FROM triage_turn;
DELETE FROM triage_slot_state;
DELETE FROM triage_session;
DELETE FROM ai_recall_audit_log;
DELETE FROM disease_capability_rel;
DELETE FROM department_capability_rel;
DELETE FROM disease_alias;
DELETE FROM disease_master;
DELETE FROM medical_capability_catalog;
DELETE FROM hospital_department;
DELETE FROM hospital;
DELETE FROM doctor_capability_rel;
DELETE FROM doctor_profile;
DELETE FROM import_failure_log;
DELETE FROM import_review_item;
DELETE FROM import_job_record;

INSERT INTO disease_master (disease_code, disease_name, aliases_json, symptom_keywords, gender_rule, age_min, age_max, age_group, urgency_level, review_status, deleted) VALUES
('acute_upper_respiratory_infection', 'Acute Upper Respiratory Infection', '["cold","upper respiratory infection","child cough"]', '["fever","cough","sore throat","runny nose"]', 'all', 0, 17, 'child', 'medium', 'approved', 0),
('geriatric_cognitive_decline', 'Geriatric Cognitive Decline', '["memory decline","elderly forgetfulness"]', '["memory decline","slow response","disorientation"]', 'all', 65, 120, 'elderly', 'medium', 'approved', 0),
('pelvic_inflammatory_disease', 'Pelvic Inflammatory Disease', '["lower abdominal pain in women","pid"]', '["lower abdominal pain","abnormal discharge","fever"]', 'female_only', 14, 60, 'adult', 'medium', 'approved', 0),
('benign_prostatic_hyperplasia', 'Benign Prostatic Hyperplasia', '["bph","male urinary symptoms"]', '["frequent urination","urgency","difficulty urinating","nocturia"]', 'male_only', 40, 120, 'adult', 'medium', 'approved', 0),
('lumbar_disc_herniation', 'Lumbar Disc Herniation', '["low back and leg pain","lumbar disc"]', '["low back pain","leg numbness","radiating pain","leg pain"]', 'all', 16, 80, 'adult', 'medium', 'approved', 0),
('post_transplant_followup', 'Post Transplant Followup', '["post transplant review","transplant followup"]', '["post transplant","review","rejection","immunosuppression"]', 'all', 0, 120, 'all', 'high', 'approved', 0);

INSERT INTO disease_alias (disease_code, alias_name, alias_type, source) VALUES
('acute_upper_respiratory_infection', 'child cough', 'symptom_alias', 'seed'),
('geriatric_cognitive_decline', 'memory decline', 'symptom_alias', 'seed'),
('pelvic_inflammatory_disease', 'lower abdominal pain in women', 'symptom_alias', 'seed'),
('benign_prostatic_hyperplasia', 'male urinary symptoms', 'symptom_alias', 'seed'),
('lumbar_disc_herniation', 'low back and leg pain', 'symptom_alias', 'seed'),
('post_transplant_followup', 'post transplant review', 'symptom_alias', 'seed');

INSERT INTO medical_capability_catalog (capability_code, capability_name, capability_type, parent_code, standard_dept_code, aliases_json, gender_rule, age_min, age_max, crowd_tags_json, pathway_tags_json, active_status) VALUES
('cap_pediatrics', 'Pediatrics', 'STANDARD_DEPT', NULL, 'PED', '["pediatrics","child fever"]', 'all', 0, 17, '["child","adolescent"]', '[]', 1),
('cap_pediatric_fever_clinic', 'Pediatric Fever Clinic', 'SPECIAL_PATHWAY', 'cap_pediatrics', 'PED', '["pediatric fever clinic","child fever"]', 'all', 0, 17, '["child","adolescent"]', '["child_fever_pathway"]', 1),
('cap_geriatrics', 'Geriatrics', 'SPECIAL_POPULATION', NULL, 'GER', '["geriatrics","elderly clinic"]', 'all', 65, 120, '["elderly"]', '[]', 1),
('cap_memory_clinic', 'Memory Clinic', 'SUBSPECIALTY', 'cap_geriatrics', 'GER', '["memory clinic","cognitive clinic"]', 'all', 65, 120, '["elderly"]', '["elderly_multisymptom_pathway"]', 1),
('cap_gynecology', 'Gynecology', 'STANDARD_DEPT', NULL, 'GYN', '["gynecology"]', 'female_only', 14, 60, '[]', '[]', 1),
('cap_andrology', 'Andrology', 'STANDARD_DEPT', NULL, 'AND', '["andrology"]', 'male_only', 18, 120, '[]', '[]', 1),
('cap_male_urinary_clinic', 'Male Urinary Clinic', 'SUBSPECIALTY', 'cap_andrology', 'AND', '["urinary clinic","prostate clinic"]', 'male_only', 18, 120, '[]', '["male_urinary_pathway"]', 1),
('cap_orthopedics', 'Orthopedics', 'STANDARD_DEPT', NULL, 'ORT', '["orthopedics"]', 'all', 12, 80, '["adolescent","adult"]', '[]', 1),
('cap_spine_surgery', 'Spine Surgery', 'SUBSPECIALTY', 'cap_orthopedics', 'ORT', '["spine surgery","lumbar clinic"]', 'all', 16, 80, '[]', '["spine_pathway"]', 1),
('cap_spine_pain_clinic', 'Spine Pain Clinic', 'SUBSPECIALTY', 'cap_spine_surgery', 'ORT', '["back pain clinic","spine pain clinic"]', 'all', 16, 80, '[]', '["spine_pathway"]', 1),
('cap_transplant_followup', 'Transplant Followup', 'SPECIAL_PATHWAY', NULL, 'TRP', '["transplant followup","transplant clinic"]', 'all', 0, 120, '["transplant_followup"]', '["transplant_followup"]', 1);

INSERT INTO disease_capability_rel (disease_code, capability_code, rel_type, priority_score, crowd_constraint, note) VALUES
('acute_upper_respiratory_infection', 'cap_pediatrics', 'PRIMARY', 1.20, 'child', 'Child fever and cough should prefer pediatrics'),
('acute_upper_respiratory_infection', 'cap_pediatric_fever_clinic', 'SECONDARY', 1.35, 'child', 'Child fever can be routed to fever clinic'),
('geriatric_cognitive_decline', 'cap_geriatrics', 'PRIMARY', 1.30, 'elderly', 'Elderly cognitive decline should prefer geriatrics'),
('geriatric_cognitive_decline', 'cap_memory_clinic', 'SECONDARY', 1.45, 'elderly', 'Elderly cognitive decline can be refined to memory clinic'),
('pelvic_inflammatory_disease', 'cap_gynecology', 'PRIMARY', 1.20, 'female', 'Female lower abdominal pain should prefer gynecology'),
('benign_prostatic_hyperplasia', 'cap_andrology', 'PRIMARY', 1.20, 'male', 'Male urinary symptoms should prefer andrology'),
('benign_prostatic_hyperplasia', 'cap_male_urinary_clinic', 'SECONDARY', 1.40, 'male', 'Urinary symptoms can be refined to male urinary clinic'),
('lumbar_disc_herniation', 'cap_spine_surgery', 'PRIMARY', 1.40, NULL, 'Back and leg pain should prefer spine surgery'),
('lumbar_disc_herniation', 'cap_spine_pain_clinic', 'SECONDARY', 1.50, NULL, 'Back and leg pain can be refined to spine pain clinic'),
('post_transplant_followup', 'cap_transplant_followup', 'PRIMARY', 1.60, 'transplant_followup', 'Post transplant followup should use transplant pathway');

INSERT INTO hospital (id, hospital_code, hospital_name, city, district_name, latitude, longitude, hospital_level, is_emergency, authority_score, active_status, deleted) VALUES
(1, 'example_general_hospital_a', 'Example General Hospital A', 'Wuhan', 'Jianghan', 30.602000, 114.274000, 'TERTIARY_A', 1, 82.00, 1, 0),
(2, 'example_specialty_center_b', 'Example Specialty Center B', 'Wuhan', 'Wuchang', 30.553000, 114.332000, 'TERTIARY_A', 1, 96.00, 1, 0);

INSERT INTO hospital_department (
    id, hospital_id, department_name, parent_department_name, department_intro, service_scope,
    active_status, deleted, gender_rule, age_min, age_max, crowd_tags_json,
    standard_dept_code, subspecialty_code, district_name, latitude, longitude, is_emergency,
    national_key_score, provincial_key_score, city_key_score, authority_score
) VALUES
(1, 1, 'Pediatrics Clinic', 'Pediatrics', 'Outpatient clinic for common pediatric diseases', 'fever,cough,upper respiratory symptoms', 1, 0, 'all', 0, 17, '["child","adolescent"]', 'PED', 'PED_GENERAL', 'Jianghan', 30.602000, 114.274000, 1, 0, 0, 18, 18),
(2, 1, 'Pediatric Fever Clinic', 'Pediatrics', 'Rapid assessment clinic for child fever', 'child fever,acute respiratory symptoms', 1, 0, 'all', 0, 17, '["child","adolescent"]', 'PED', 'PED_FEVER', 'Jianghan', 30.602100, 114.274300, 1, 0, 1, 16, 76),
(3, 1, 'Geriatrics Clinic', 'Internal Medicine', 'Comprehensive clinic for older adults', 'memory decline,chronic disease,multiple symptoms', 1, 0, 'all', 65, 120, '["elderly"]', 'GER', 'GER_GENERAL', 'Jianghan', 30.601500, 114.273500, 1, 0, 1, 20, 80),
(4, 1, 'Memory Clinic', 'Geriatrics', 'Specialized memory and cognition clinic', 'memory decline,cognitive impairment,slow response', 1, 0, 'all', 65, 120, '["elderly"]', 'GER', 'GER_MEMORY', 'Jianghan', 30.601800, 114.273900, 0, 0, 1, 25, 85),
(5, 1, 'Gynecology Clinic', 'Gynecology', 'Women health outpatient clinic', 'lower abdominal pain,pelvic inflammation,menstrual issues', 1, 0, 'female_only', 14, 60, '[]', 'GYN', 'GYN_GENERAL', 'Jianghan', 30.602300, 114.273800, 1, 0, 1, 18, 78),
(6, 1, 'Andrology Clinic', 'Urology', 'Male health outpatient clinic', 'urinary symptoms,prostate issues', 1, 0, 'male_only', 18, 120, '[]', 'AND', 'AND_GENERAL', 'Jianghan', 30.602600, 114.274100, 0, 0, 1, 18, 78),
(7, 1, 'Male Urinary Clinic', 'Andrology', 'Specialized male urinary clinic', 'frequent urination,urgency,nocturia,prostate issues', 1, 0, 'male_only', 18, 120, '[]', 'AND', 'AND_URINARY', 'Jianghan', 30.602700, 114.274500, 0, 0, 1, 22, 82),
(8, 1, 'Spine Surgery Clinic', 'Orthopedics', 'Specialized spine clinic', 'lumbar disc herniation,low back pain,leg numbness', 1, 0, 'all', 16, 80, '[]', 'ORT', 'ORT_SPINE_SURGERY', 'Jianghan', 30.603100, 114.274200, 1, 0, 1, 28, 88),
(9, 1, 'Spine Pain Clinic', 'Spine Surgery', 'Specialized back pain clinic', 'back pain,sciatica,chronic lumbar pain', 1, 0, 'all', 16, 80, '[]', 'ORT', 'ORT_SPINE_PAIN', 'Jianghan', 30.603200, 114.274400, 0, 0, 1, 26, 86),
(10, 2, 'Transplant Followup Clinic', 'Transplant Center', 'Followup pathway after transplant', 'postoperative review,rejection monitoring,immunosuppression management', 1, 0, 'all', 0, 120, '["transplant_followup"]', 'TRP', 'TRP_FOLLOWUP', 'Wuchang', 30.553200, 114.332200, 1, 1, 1, 20, 100);

INSERT INTO department_capability_rel (department_id, capability_code, support_level, weight, source) VALUES
(1, 'cap_pediatrics', 'PRIMARY', 1.00, 'seed-example'),
(2, 'cap_pediatric_fever_clinic', 'PRIMARY', 1.20, 'seed-example'),
(3, 'cap_geriatrics', 'PRIMARY', 1.00, 'seed-example'),
(4, 'cap_memory_clinic', 'PRIMARY', 1.25, 'seed-example'),
(5, 'cap_gynecology', 'PRIMARY', 1.00, 'seed-example'),
(6, 'cap_andrology', 'PRIMARY', 1.00, 'seed-example'),
(7, 'cap_male_urinary_clinic', 'PRIMARY', 1.20, 'seed-example'),
(8, 'cap_spine_surgery', 'PRIMARY', 1.10, 'seed-example'),
(8, 'cap_orthopedics', 'SECONDARY', 0.95, 'seed-example'),
(9, 'cap_spine_pain_clinic', 'PRIMARY', 1.25, 'seed-example'),
(10, 'cap_transplant_followup', 'PRIMARY', 1.20, 'seed-example');

INSERT INTO doctor_profile (
    id, hospital_id, department_id, doctor_name, title, specialty_text, gender_rule, age_min, age_max, crowd_tags_json,
    authority_score, academic_title_score, is_expert, campus_name, active_status
) VALUES
(1, 1, 2, 'Dr. Zhang', 'Associate Chief Physician', 'Child fever and respiratory infection', 'all', 0, 17, '["child","adolescent"]', 72.00, 12.00, 1, 'Main Campus', 1),
(2, 1, 4, 'Dr. Zhou', 'Chief Physician', 'Elderly cognition and memory decline', 'all', 65, 120, '["elderly"]', 86.00, 18.00, 1, 'Main Campus', 1),
(3, 1, 7, 'Dr. Chen', 'Associate Chief Physician', 'Male urinary symptoms and prostate disease', 'male_only', 18, 120, '[]', 74.00, 10.00, 0, 'Main Campus', 1),
(4, 1, 9, 'Dr. Li', 'Chief Physician', 'Low back pain and spine pain conditions', 'all', 16, 80, '[]', 88.00, 18.00, 1, 'Main Campus', 1),
(5, 2, 10, 'Dr. Wang', 'Chief Physician', 'Transplant followup and rejection management', 'all', 0, 120, '["transplant_followup"]', 96.00, 20.00, 1, 'Specialty Campus', 1);

INSERT INTO doctor_capability_rel (doctor_id, capability_code, weight) VALUES
(1, 'cap_pediatric_fever_clinic', 0.45),
(2, 'cap_memory_clinic', 0.45),
(3, 'cap_male_urinary_clinic', 0.45),
(4, 'cap_spine_pain_clinic', 0.45),
(5, 'cap_transplant_followup', 0.50);
