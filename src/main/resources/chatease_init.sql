
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. Table: user_info
-- =======================================================
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`
(
    `user_id`            varchar(32) NOT NULL COMMENT 'User ID (Expanded)',
    `email`              varchar(50) NOT NULL COMMENT 'Email',
    `nick_name`          varchar(20)  DEFAULT NULL COMMENT 'Nickname',
    `join_type`          tinyint(1) DEFAULT NULL COMMENT '0: Direct join, 1: Join after approval',
    `sex`                tinyint(1) DEFAULT NULL COMMENT 'Gender 0: Female, 1: Male',
    `password`           varchar(60)  DEFAULT NULL COMMENT 'Password',
    `personal_signature` varchar(50)  DEFAULT NULL COMMENT 'Profile signature',
    `status`             tinyint(1) DEFAULT 1 COMMENT 'Status 1:Active 0:Disabled',
    `create_time`        datetime     DEFAULT NULL COMMENT 'Creation time',
    `last_login_time`    datetime     DEFAULT NULL COMMENT 'Last login time',
    `area_name`          varchar(50)  DEFAULT NULL COMMENT 'Area name',
    `area_code`          varchar(50)  DEFAULT NULL COMMENT 'Area code',
    `last_off_time`      bigint(13) DEFAULT NULL COMMENT 'Last offline time',
    `avatar`             varchar(255) DEFAULT NULL COMMENT 'User Avatar',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `idx_key_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User information';

-- =======================================================
-- 2. Table: group_info
-- =======================================================
DROP TABLE IF EXISTS `group_info`;
CREATE TABLE `group_info`
(
    `group_id`       varchar(32) NOT NULL COMMENT 'Group ID (Expanded)',
    `group_name`     varchar(20)  DEFAULT NULL COMMENT 'Group name',
    `group_owner_id` varchar(32)  DEFAULT NULL COMMENT 'Group owner ID',
    `group_avatar`   varchar(255) DEFAULT NULL COMMENT 'Group avatar',
    `create_time`    datetime     DEFAULT NULL COMMENT 'Creation time',
    `group_notice`   varchar(500) DEFAULT NULL COMMENT 'Group announcement',
    `join_type`      tinyint      DEFAULT NULL COMMENT '0: Join directly 1: Join after admin approval',
    `status`         tinyint      DEFAULT 1 COMMENT 'Status 1: Active 0: Disbanded',
    PRIMARY KEY (`group_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- =======================================================
-- 3. Table: user_contact
-- =======================================================
DROP TABLE IF EXISTS `user_contact`;
CREATE TABLE `user_contact`
(
    `user_id`          varchar(32) NOT NULL COMMENT 'User ID',
    `contact_id`       varchar(32) NOT NULL COMMENT 'Contact ID or Group ID',
    `contact_type`     tinyint  DEFAULT 0 COMMENT 'Contact type: 0-Friend, 1-Group',
    `create_time`      datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `status`           tinyint  DEFAULT 0 COMMENT 'Status: 0-Not friend, 1-Friend, 2-Deleted, 3-Blocked',
    `last_update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    PRIMARY KEY (`user_id`, `contact_id`) USING BTREE,
    KEY                `idx_contact_id` (`contact_id`) USING BTREE,
    KEY                `idx_user_status` (`user_id`, `status`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Contact list' ROW_FORMAT = DYNAMIC;

-- =======================================================
-- 4. Table: user_apply
-- =======================================================
DROP TABLE IF EXISTS `user_apply`;
CREATE TABLE `user_apply`
(
    `apply_id`        int(11) NOT NULL AUTO_INCREMENT COMMENT 'Auto increment ID',
    `apply_user_id`   varchar(32) NOT NULL COMMENT 'Applicant user ID',
    `receive_user_id` varchar(32) NOT NULL COMMENT 'Receiver user ID',
    `contact_type`    tinyint(1) NOT NULL COMMENT 'Contact type: 0-Friend, 1-Group',
    `contact_id`      varchar(32)  DEFAULT NULL COMMENT 'Contact or group ID',
    `last_apply_time` bigint(20) DEFAULT NULL COMMENT 'Last application time',
    `status`          tinyint(1) DEFAULT NULL COMMENT 'Status: 0-Pending, 1-Accepted, 2-Rejected, 3-Blocked',
    `apply_info`      varchar(100) DEFAULT NULL COMMENT 'Application information',
    PRIMARY KEY (`apply_id`) USING BTREE,
    KEY               `idx_apply_user` (`apply_user_id`) USING BTREE,
    KEY               `idx_receive_user` (`receive_user_id`) USING BTREE,
    KEY               `idx_contact` (`contact_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User application table' ROW_FORMAT = DYNAMIC;

-- =======================================================
-- 5. Table: admin_info
-- =======================================================
DROP TABLE IF EXISTS `admin_info`;
CREATE TABLE `admin_info`
(
    `admin_id`        int(11) NOT NULL AUTO_INCREMENT COMMENT 'Admin ID',
    `username`        varchar(30) NOT NULL COMMENT 'Login username',
    `password`        varchar(60) NOT NULL COMMENT 'Encrypted password',
    `role`            varchar(20) DEFAULT 'admin' COMMENT 'Role: super_admin, editor',
    `create_time`     datetime    DEFAULT CURRENT_TIMESTAMP,
    `last_login_time` datetime    DEFAULT NULL,
    PRIMARY KEY (`admin_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Administrator table';

-- =======================================================
-- 6. Table: sys_setting
-- =======================================================
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`
(
    `setting_code`  varchar(50) NOT NULL COMMENT 'Config Key',
    `setting_value` varchar(1000) DEFAULT NULL COMMENT 'Config Value',
    `description`   varchar(200)  DEFAULT NULL COMMENT 'Description of the setting',
    PRIMARY KEY (`setting_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Global Settings';

-- =======================================================
-- 7. Table: sys_broadcast
-- =======================================================
DROP TABLE IF EXISTS `sys_broadcast`;
CREATE TABLE `sys_broadcast`
(
    `broadcast_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto increment ID',
    `sender_id`    varchar(32)   NOT NULL COMMENT 'Usually the ROBOT_UID',
    `content`      varchar(1000) NOT NULL COMMENT 'Message content',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `message_type` tinyint(4) DEFAULT 0 COMMENT 'Message Type: 0-Text, 1-Image, 4-Video',
    `file_path`    varchar(255) DEFAULT NULL COMMENT 'File URL (if applicable)',
    PRIMARY KEY (`broadcast_id`),
    KEY            `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Broadcast Messages';

-- =======================================================
-- 8. Table: user_robot_relation
-- =======================================================
DROP TABLE IF EXISTS `user_robot_relation`;
CREATE TABLE `user_robot_relation`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `user_id`        varchar(32) NOT NULL COMMENT 'User ID',
    `robot_id`       varchar(50) NOT NULL COMMENT 'Robot ID',
    `status`         tinyint(1) DEFAULT 1 COMMENT 'Status: 1-Normal, 0-Hidden/Removed, 2-Blocked',
    `create_time`    datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `last_read_time` bigint(20) DEFAULT NULL COMMENT 'Last read timestamp (for unread count)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_robot` (`user_id`, `robot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Robot Relationship Table';

-- =======================================================
-- 9. Table: app_version
-- =======================================================
DROP TABLE IF EXISTS `app_version`;
CREATE TABLE `app_version`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `version_number`  varchar(50)  NOT NULL COMMENT 'Version number, e.g., 1.0.0',
    `update_content`  text COMMENT 'Update content description',
    `download_url`    varchar(255) NOT NULL COMMENT 'Download URL for the installer package',
    `file_size`       bigint(20) DEFAULT 0 COMMENT 'File size in bytes',
    `file_md5`        varchar(32) DEFAULT NULL COMMENT 'File MD5 checksum',
    `is_force_update` tinyint(1) DEFAULT 0 COMMENT 'Force update: 0-No, 1-Yes',
    `status`          tinyint(1) DEFAULT 1 COMMENT 'Status: 1-Published, 0-Revoked',
    `create_time`     datetime    DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='App Version Management';

-- =======================================================
-- 10. Table: chat_message
-- =======================================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`
(
    `message_id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Message ID (Auto increment)',
    `session_id`          varchar(32) NOT NULL COMMENT 'Session ID (e.g., UID1_UID2 for private, GID for group)',
    `send_user_id`        varchar(32) NOT NULL COMMENT 'Sender ID',
    `send_user_nick_name` varchar(20)   DEFAULT NULL COMMENT 'Redundant: Sender nickname',
    `send_user_avatar`    varchar(255)  DEFAULT NULL COMMENT 'Redundant: Sender avatar',
    `contact_id`          varchar(32) NOT NULL COMMENT 'Receiver ID (User ID or Group ID)',
    `contact_type`        tinyint(1) NOT NULL COMMENT '0: Personal chat, 1: Group chat',
    `content`             varchar(1000) DEFAULT NULL COMMENT 'Message content',
    `message_type`        tinyint(4) DEFAULT 0 COMMENT '0: Text, 1: Image, 2: File, 3: Audio, 4: Video, 5: System',
    `file_size`           bigint(20) DEFAULT NULL COMMENT 'File size in bytes (if applicable)',
    `file_name`           varchar(200)  DEFAULT NULL COMMENT 'Original file name (if applicable)',
    `file_path`           varchar(255)  DEFAULT NULL COMMENT 'File path/URL (if applicable)',
    `status`              tinyint(1) DEFAULT 1 COMMENT '0: Sending, 1: Sent, 2: Read, 3: Recall',
    `send_time`           bigint(20) DEFAULT NULL COMMENT 'Send timestamp',
    PRIMARY KEY (`message_id`),
    KEY                   `idx_session_id` (`session_id`),
    KEY                   `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chat message history';

-- =======================================================
-- 11. Table: chat_session
-- =======================================================
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`
(
    `user_id`           varchar(32) NOT NULL COMMENT 'Owner of this session',
    `contact_id`        varchar(32) NOT NULL COMMENT 'Contact ID (Friend ID or Group ID)',
    `session_id`        varchar(32) NOT NULL COMMENT 'Associated Session ID in chat_message',
    `contact_name`      varchar(20)  DEFAULT NULL COMMENT 'Redundant: Contact name',
    `contact_avatar`    varchar(255) DEFAULT NULL COMMENT 'Redundant: Contact avatar',
    `last_message`      varchar(500) DEFAULT NULL COMMENT 'Snapshot of the last message',
    `last_receive_time` bigint(20) DEFAULT NULL COMMENT 'Time of the last message',
    `unread_count`      int(11) DEFAULT 0 COMMENT 'Number of unread messages',
    `contact_type`      tinyint(1) DEFAULT 0 COMMENT '0: Personal, 1: Group',
    PRIMARY KEY (`user_id`, `contact_id`),
    KEY                 `idx_user_time` (`user_id`, `last_receive_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User chat sessions list';


-- =======================================================
-- Data Initialization
-- =======================================================

-- 1. Init Robot Config
INSERT INTO `sys_setting` (`setting_code`, `setting_value`, `description`)
VALUES ('ROBOT_UID', 'UID_ROBOT_001', 'Fixed Robot ID'),
       ('ROBOT_NICKNAME', 'ChatEase Helper', 'Nickname displayed for the robot'),
       ('ROBOT_AVATAR', 'https://cdn-icons-png.flaticon.com/512/4712/4712035.png', 'Robot Avatar URL'),
       ('ROBOT_WELCOME', 'Hi! Welcome to ChatEase, I am your intelligent assistant.',
        'Welcome message sent upon registration') ON DUPLICATE KEY
UPDATE `setting_value` =
VALUES (`setting_value`);

-- 2. Init Super Admin
-- Default User: admin
-- Password Hash: Please replace with your generated real hash
INSERT INTO `admin_info` (`username`, `password`, `role`, `create_time`)
VALUES ('admin', '.....', 'super_admin', NOW()) ON DUPLICATE KEY
UPDATE `role` =
VALUES (`role`);

-- 3. Init First System Broadcast (For new users)
INSERT INTO `sys_broadcast` (`sender_id`, `content`, `message_type`, `create_time`)
VALUES ('UID_ROBOT_001', 'Hi! Welcome to ChatEase. Connect naturally.', 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;