<?php
// ═══════════════════════════════════════════════════════════
//  config.php — Railway MySQL
//  يقرأ بيانات DB تلقائياً من Environment Variables
// ═══════════════════════════════════════════════════════════
define('DB_HOST', getenv('MYSQLHOST')     ?: 'localhost');
define('DB_NAME', getenv('MYSQLDATABASE') ?: 'railway');
define('DB_USER', getenv('MYSQLUSER')     ?: 'root');
define('DB_PASS', getenv('MYSQLPASSWORD') ?: '');
define('DB_PORT', getenv('MYSQLPORT')     ?: '3306');

define('RATE_LIMIT', 20);

date_default_timezone_set('Asia/Riyadh');
