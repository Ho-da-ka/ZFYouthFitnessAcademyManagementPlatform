CREATE TABLE IF NOT EXISTS coaches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coach_code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    gender VARCHAR(16) NOT NULL,
    phone VARCHAR(32) NULL,
    specialty VARCHAR(255) NULL,
    status VARCHAR(16) NOT NULL,
    remarks VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_coaches_coach_code (coach_code),
    UNIQUE KEY uk_coaches_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    gender VARCHAR(16) NOT NULL,
    birth_date DATE NOT NULL,
    guardian_name VARCHAR(64) NULL,
    guardian_phone VARCHAR(32) NULL,
    status VARCHAR(16) NOT NULL,
    remarks VARCHAR(255) NULL,
    goal_focus VARCHAR(255) NULL,
    training_tags VARCHAR(255) NULL,
    risk_notes VARCHAR(255) NULL,
    goal_start_date DATE NULL,
    goal_end_date DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_students_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    coach_id BIGINT NULL,
    student_id BIGINT NULL,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_accounts_username (username),
    UNIQUE KEY uk_user_accounts_coach_id (coach_id),
    UNIQUE KEY uk_user_accounts_student_id (student_id),
    KEY idx_user_accounts_role (role),
    CONSTRAINT fk_user_accounts_coach FOREIGN KEY (coach_id) REFERENCES coaches(id),
    CONSTRAINT fk_user_accounts_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    course_type VARCHAR(64) NOT NULL,
    coach_name VARCHAR(64) NOT NULL,
    venue VARCHAR(128) NOT NULL,
    start_time DATETIME NOT NULL,
    duration_minutes INT NOT NULL,
    max_capacity INT NULL,
    course_date DATE NULL,
    class_start_time TIME NULL,
    class_end_time TIME NULL,
    status VARCHAR(16) NOT NULL,
    description VARCHAR(255) NULL,
    training_theme VARCHAR(100) NULL,
    target_age_range VARCHAR(32) NULL,
    target_goals VARCHAR(255) NULL,
    focus_points VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_courses_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_attendance_student_date (student_id, attendance_date),
    KEY idx_attendance_course_date (course_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fitness_test_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NULL,
    student_name_snapshot VARCHAR(64) NULL,
    test_date DATE NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    test_value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    comment VARCHAR(255) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_fitness_student_date (student_id, test_date),
    KEY idx_fitness_deleted (deleted),
    CONSTRAINT fk_fitness_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS training_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    training_date DATE NOT NULL,
    training_content VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL,
    intensity_level VARCHAR(32) NULL,
    performance_summary VARCHAR(255) NULL,
    highlight_note VARCHAR(255) NULL,
    improvement_note VARCHAR(255) NULL,
    parent_action VARCHAR(255) NULL,
    next_step_suggestion VARCHAR(255) NULL,
    coach_comment VARCHAR(500) NULL,
    ai_summary VARCHAR(500) NULL,
    parent_read_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_training_student_date (student_id, training_date),
    KEY idx_training_course_date (course_id, training_date),
    CONSTRAINT fk_training_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_training_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS parent_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_account_id BIGINT NOT NULL,
    display_name VARCHAR(64) NULL,
    phone VARCHAR(32) NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_parent_accounts_user_account_id (user_account_id),
    KEY idx_parent_accounts_phone (phone),
    CONSTRAINT fk_parent_accounts_user_account FOREIGN KEY (user_account_id) REFERENCES user_accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stage_evaluations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    cycle_name VARCHAR(100) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    attendance_rate DECIMAL(5,4) NOT NULL,
    training_summary VARCHAR(255) NOT NULL,
    fitness_summary VARCHAR(255) NOT NULL,
    coach_evaluation VARCHAR(500) NOT NULL,
    next_stage_plan VARCHAR(500) NOT NULL,
    ai_interpretation VARCHAR(1000) NULL,
    parent_report VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_stage_evaluations_student_period (student_id, period_start, period_end),
    CONSTRAINT fk_stage_evaluations_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS care_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    alert_type VARCHAR(32) NOT NULL,
    alert_title VARCHAR(100) NOT NULL,
    alert_content VARCHAR(500) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    triggered_at DATETIME NOT NULL,
    resolved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_care_alerts_student_status (student_id, status),
    KEY idx_care_alerts_triggered_at (triggered_at),
    CONSTRAINT fk_care_alerts_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS parent_student_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_account_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_parent_student_relation (parent_account_id, student_id),
    KEY idx_parent_student_student (student_id),
    CONSTRAINT fk_parent_student_relation_parent FOREIGN KEY (parent_account_id) REFERENCES parent_accounts(id),
    CONSTRAINT fk_parent_student_relation_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_account_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    booking_status VARCHAR(16) NOT NULL DEFAULT 'BOOKED',
    course_capacity INT NOT NULL DEFAULT 20,
    booking_remark VARCHAR(255) NULL,
    checkin_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    checkin_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_course_bookings_parent (parent_account_id),
    KEY idx_course_bookings_course_status (course_id, booking_status),
    KEY idx_course_bookings_student (student_id),
    CONSTRAINT fk_course_bookings_parent FOREIGN KEY (parent_account_id) REFERENCES parent_accounts(id),
    CONSTRAINT fk_course_bookings_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_course_bookings_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS in_app_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_account_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    msg_type VARCHAR(32) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_in_app_messages_parent (parent_account_id),
    KEY idx_in_app_messages_read (is_read),
    CONSTRAINT fk_in_app_messages_parent FOREIGN KEY (parent_account_id) REFERENCES parent_accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(128) NOT NULL,
    username VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_refresh_tokens_token (token),
    KEY idx_refresh_tokens_expires_at (expires_at),
    KEY idx_refresh_tokens_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS max_capacity INT NULL,
    ADD COLUMN IF NOT EXISTS course_date DATE NULL,
    ADD COLUMN IF NOT EXISTS class_start_time TIME NULL,
    ADD COLUMN IF NOT EXISTS class_end_time TIME NULL,
    ADD COLUMN IF NOT EXISTS training_theme VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS target_age_range VARCHAR(32) NULL,
    ADD COLUMN IF NOT EXISTS target_goals VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS focus_points VARCHAR(255) NULL;

ALTER TABLE students
    ADD COLUMN IF NOT EXISTS goal_focus VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS training_tags VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS risk_notes VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS goal_start_date DATE NULL,
    ADD COLUMN IF NOT EXISTS goal_end_date DATE NULL;

ALTER TABLE training_records
    ADD COLUMN IF NOT EXISTS highlight_note VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS improvement_note VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS parent_action VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS next_step_suggestion VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS coach_comment VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS ai_summary VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS parent_read_at DATETIME NULL;

ALTER TABLE stage_evaluations
    ADD COLUMN IF NOT EXISTS cycle_name VARCHAR(100) NOT NULL,
    ADD COLUMN IF NOT EXISTS period_start DATE NOT NULL,
    ADD COLUMN IF NOT EXISTS period_end DATE NOT NULL,
    ADD COLUMN IF NOT EXISTS attendance_rate DECIMAL(5,4) NOT NULL,
    ADD COLUMN IF NOT EXISTS training_summary VARCHAR(255) NOT NULL,
    ADD COLUMN IF NOT EXISTS fitness_summary VARCHAR(255) NOT NULL,
    ADD COLUMN IF NOT EXISTS coach_evaluation VARCHAR(500) NOT NULL,
    ADD COLUMN IF NOT EXISTS next_stage_plan VARCHAR(500) NOT NULL,
    ADD COLUMN IF NOT EXISTS ai_interpretation VARCHAR(1000) NULL,
    ADD COLUMN IF NOT EXISTS parent_report VARCHAR(1000) NULL;
