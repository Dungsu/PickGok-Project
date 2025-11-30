-- *******************************************
-- PickGok Project Database Initialization
-- *******************************************

-- 1. 스키마 생성 및 초기화
DROP DATABASE IF EXISTS pick_gok;
CREATE DATABASE pick_gok
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pick_gok;

-- 2. USERS 테이블 (회원 정보)
CREATE TABLE users (
    userid VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    preferred_genres VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. ADMINS 테이블 (관리자 정보)
CREATE TABLE admins (
    adminid VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (adminid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- [초기 데이터] 관리자 계정 생성 (ID: admin / PW: 1234)
INSERT INTO admins (adminid, password, name) VALUES ('admin', '1234', 'Master Admin');

-- 4. TRACKS 테이블 (음악 메타데이터)
CREATE TABLE tracks (
    track_id INT NOT NULL,
    title VARCHAR(255) DEFAULT NULL,
    artist VARCHAR(255) DEFAULT NULL,
    genre VARCHAR(100) DEFAULT 'Unknown',
    duration INT DEFAULT 0,
    file_path VARCHAR(500) DEFAULT NULL,
    play_count BIGINT DEFAULT 0,
    PRIMARY KEY (track_id),
    KEY idx_title (title),
    KEY idx_play_count (play_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. LIKES 테이블 (좋아요 보관함)
CREATE TABLE likes (
    like_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    track_id INT NOT NULL,
    liked_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (like_id),
    UNIQUE KEY unique_like (user_id, track_id),
    KEY track_id (track_id),
    CONSTRAINT likes_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (userid) ON DELETE CASCADE,
    CONSTRAINT likes_ibfk_2 FOREIGN KEY (track_id) REFERENCES tracks (track_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. USER_HISTORY 테이블 (행동 로그)
CREATE TABLE user_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    track_id INT NOT NULL,
    action_type ENUM('LIKE', 'UNLIKE', 'SKIP') NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (history_id),
    KEY user_id (user_id),
    KEY track_id (track_id),
    CONSTRAINT user_history_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (userid) ON DELETE CASCADE,
    CONSTRAINT user_history_ibfk_2 FOREIGN KEY (track_id) REFERENCES tracks (track_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. RECOMMENDATIONS 테이블 (추천 이력 - 선택 사항)
CREATE TABLE recommendations (
    rec_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    track_id INT NOT NULL,
    rec_type ENUM('SIMILAR', 'DAILY') NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rec_id),
    KEY user_id (user_id),
    KEY track_id (track_id),
    CONSTRAINT recommendations_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (userid) ON DELETE CASCADE,
    CONSTRAINT recommendations_ibfk_2 FOREIGN KEY (track_id) REFERENCES tracks (track_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;