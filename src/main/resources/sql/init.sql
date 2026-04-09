CREATE DATABASE IF NOT EXISTS whdz DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE whdz;

CREATE TABLE IF NOT EXISTS triage_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_name VARCHAR(100) NOT NULL,
    patient_age INT,
    patient_gender TINYINT DEFAULT 0,
    symptoms TEXT NOT NULL,
    symptom_duration VARCHAR(200),
    recommended_department VARCHAR(100),
    urgency_level TINYINT,
    ai_advice TEXT,
    status TINYINT DEFAULT 0,
    deleted TINYINT DEFAULT 0 NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
