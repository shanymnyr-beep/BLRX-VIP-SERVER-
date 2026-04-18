<?php
// ═══════════════════════════════════════════════════════════
//  verify.php — API التحقق من المفاتيح
//  POST {"key":"XXXX","deviceId":"XXXX"}
// ═══════════════════════════════════════════════════════════
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200); exit;
}
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit(json_encode(['success'=>false,'code'=>'METHOD_NOT_ALLOWED']));
}

require_once __DIR__ . '/db.php';

$raw      = file_get_contents('php://input');
$data     = json_decode($raw, true);
$key      = trim((string)($data['key']      ?? ''));
$deviceId = trim((string)($data['deviceId'] ?? ''));

if ($key === '' || $deviceId === '') {
    exit(json_encode(['success'=>false,'code'=>'INVALID_KEY']));
}

$ip = getClientIP();
if (!checkRateLimit($ip)) {
    try {
        getDB()->prepare("INSERT INTO verify_log (key_code,device_id,result,ip) VALUES (?,?,?,?)")
               ->execute([$key,$deviceId,'RATE_LIMIT',$ip]);
    } catch(Exception $e){}
    exit(json_encode(['success'=>false,'code'=>'RATE_LIMIT']));
}

try { $db = getDB(); }
catch(Exception $e) {
    exit(json_encode(['success'=>false,'code'=>'SERVER_ERROR']));
}

$st = $db->prepare("SELECT * FROM `keys` WHERE key_code = ? LIMIT 1");
$st->execute([$key]);
$row = $st->fetch();

$result = 'INVALID_KEY';
$ok     = false;
$plan   = '';
$days   = 0;

if ($row) {
    if ((int)$row['revoked'] === 1) {
        $result = 'INVALID_KEY';
    } elseif (strtotime($row['expires_at']) < time()) {
        $result = 'EXPIRED_KEY';
    } elseif ((int)$row['used'] === 1 && !empty($row['device_id'])) {
        if ($row['device_id'] === $deviceId) {
            $ok = true; $result = 'OK';
            $plan = $row['plan'];
            $days = max(0,(int)ceil((strtotime($row['expires_at'])-time())/86400));
        } else {
            $result = 'WRONG_DEVICE';
        }
    } else {
        $db->prepare("UPDATE `keys` SET used=1,device_id=?,activated_at=NOW() WHERE id=?")
           ->execute([$deviceId,$row['id']]);
        $ok = true; $result = 'OK';
        $plan = $row['plan'];
        $days = max(0,(int)ceil((strtotime($row['expires_at'])-time())/86400));
    }
}

try {
    $db->prepare("INSERT INTO verify_log (key_code,device_id,result,ip) VALUES (?,?,?,?)")
       ->execute([$key,$deviceId,$result,$ip]);
} catch(Exception $e){}

// ✅ plan و daysLeft في الجذر — يتوافق مع C++ parser
if ($ok) {
    echo json_encode([
        'success'  => true,
        'code'     => 'OK',
        'plan'     => $plan,
        'daysLeft' => $days,
    ]);
} else {
    echo json_encode(['success'=>false,'code'=>$result]);
}
