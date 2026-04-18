-- ═══════════════════════════════════════════════════════════
--  install.sql — إنشاء جداول قاعدة البيانات
--  شغّله مرة واحدة فقط من Railway MySQL Query
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS `keys` (
    `id`           INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    `key_code`     VARCHAR(64)     NOT NULL UNIQUE,
    `plan`         VARCHAR(32)     NOT NULL DEFAULT 'VIP',
    `days`         INT             NOT NULL DEFAULT 7,
    `used`         TINYINT(1)      NOT NULL DEFAULT 0,
    `device_id`    VARCHAR(128)             DEFAULT NULL,
    `revoked`      TINYINT(1)      NOT NULL DEFAULT 0,
    `revoked_at`   DATETIME                 DEFAULT NULL,
    `expires_at`   DATETIME        NOT NULL,
    `activated_at` DATETIME                 DEFAULT NULL,
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_key_code` (`key_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `verify_log` (
    `id`         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `key_code`   VARCHAR(64)  NOT NULL,
    `device_id`  VARCHAR(128)          DEFAULT NULL,
    `result`     VARCHAR(32)  NOT NULL,
    `ip`         VARCHAR(45)  NOT NULL,
    `at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_ip_at` (`ip`, `at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
