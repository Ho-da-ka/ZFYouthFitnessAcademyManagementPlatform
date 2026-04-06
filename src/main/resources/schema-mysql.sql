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
    status VARCHAR(16) NOT NULL,
    description VARCHAR(255) NULL,
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
    coach_comment VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_training_student_date (student_id, training_date),
    KEY idx_training_course_date (course_id, training_date),
    CONSTRAINT fk_training_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_training_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
