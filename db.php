<?php
// ═══════════════════════════════════════════════════════════
//  db.php — اتصال قاعدة البيانات
// ═══════════════════════════════════════════════════════════
require_once __DIR__ . '/config.php';

function getDB(): PDO {
    static $pdo = null;
    if ($pdo === null) {
        $dsn = "mysql:host=" . DB_HOST
             . ";port="      . DB_PORT
             . ";dbname="    . DB_NAME
             . ";charset=utf8mb4";
        $pdo = new PDO($dsn, DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ]);
    }
    return $pdo;
}

function getClientIP(): string {
    foreach (['HTTP_CF_CONNECTING_IP','HTTP_X_FORWARDED_FOR','REMOTE_ADDR'] as $k) {
        if (!empty($_SERVER[$k])) {
            return trim(explode(',', $_SERVER[$k])[0]);
        }
    }
    return '0.0.0.0';
}

function checkRateLimit(string $ip): bool {
    $db  = getDB();
    $sql = "SELECT COUNT(*) FROM verify_log
            WHERE ip = ? AND at > DATE_SUB(NOW(), INTERVAL 1 MINUTE)";
    $st  = $db->prepare($sql);
    $st->execute([$ip]);
    return (int)$st->fetchColumn() < RATE_LIMIT;
}
