<?php
// ═══════════════════════════════════════════════════════════
//  admin/index.php  -  لوحة تحكم BLRX VIP
// ═══════════════════════════════════════════════════════════
session_start();
require_once('db.php');

// ─── كلمة السر الثابتة ─────────────────────────────────────
define('ADMIN_PASSWORD', 'BLRX/2010');

function isLoggedIn(): bool {
    return !empty($_SESSION['admin_id']);
}
function redirect(string $url): void {
    header("Location: $url"); exit;
}
function genKey(): string {
    $chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    $key   = '';
    for ($i = 0; $i < 10; $i++) {
        $key .= $chars[random_int(0, strlen($chars)-1)];
    }
    return $key;
}
function planLabel(int $days): string {
    return match(true) {
        $days <= 1  => '1day',
        $days <= 3  => '3days',
        $days <= 7  => '7days',
        $days <= 15 => '15days',
        default     => '30days',
    };
}
function e(mixed $v): string {
    return htmlspecialchars((string)$v, ENT_QUOTES|ENT_SUBSTITUTE, 'UTF-8');
}

$db  = getDB();
$msg = '';
$err = '';

// ────────────────────────────────────────────────────────────
//  معالجة POST
// ────────────────────────────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    // ── تسجيل الدخول ──────────────────────────────────────
    if ($action === 'login') {
        $p = $_POST['password'] ?? '';
        if ($p === ADMIN_PASSWORD) {
            $_SESSION['admin_id']   = 1;
            $_SESSION['admin_user'] = 'admin';
            redirect('index.php');
        } else {
            $err = 'كلمة المرور غلط';
        }
    }

    // ── تسجيل الخروج ──────────────────────────────────────
    if ($action === 'logout') {
        session_destroy();
        redirect('index.php');
    }

    if (isLoggedIn()) {

        // ── إنشاء مفاتيح ──────────────────────────────────
        if ($action === 'generate') {
            $qty  = max(1, min(200, (int)($_POST['qty']  ?? 1)));
            $days = (int)($_POST['days'] ?? 7);
            if (!in_array($days,[1,3,7,15,30])) $days=7;
            $plan = planLabel($days);
            $exp  = date('Y-m-d H:i:s', strtotime("+{$days} days"));

            $ins  = $db->prepare(
                "INSERT INTO `keys` (key_code,plan,days,expires_at) VALUES (?,?,?,?)"
            );
            $newKeys = [];
            for ($i = 0; $i < $qty; $i++) {
                $attempts = 0;
                do {
                    $kc = genKey();
                    $chk = $db->prepare("SELECT id FROM `keys` WHERE key_code=?");
                    $chk->execute([$kc]);
                    $attempts++;
                } while ($chk->fetch() && $attempts < 10);
                $ins->execute([$kc, $plan, $days, $exp]);
                $newKeys[] = $kc;
            }
            $_SESSION['generated_keys'] = $newKeys;
            redirect('index.php?tab=generate&done=1');
        }

        // ── إلغاء مفتاح ───────────────────────────────────
        if ($action === 'revoke') {
            $kc = trim($_POST['key_code'] ?? '');
            if ($kc !== '') {
                $db->prepare("UPDATE `keys` SET revoked=1, revoked_at=NOW() WHERE key_code=?")->execute([$kc]);
                $msg = "تم إلغاء المفتاح: $kc";
            }
            redirect('index.php?tab=keys&msg=' . urlencode($msg));
        }

        // ── حذف مفتاح ─────────────────────────────────────
        if ($action === 'delete') {
            $id = (int)($_POST['key_id'] ?? 0);
            $db->prepare("DELETE FROM `keys` WHERE id=?")->execute([$id]);
            redirect('index.php?tab=keys&msg=تم+الحذف');
        }

        // ── تغيير كلمة المرور ─────────────────────────────
        // (محذوف - كلمة السر ثابتة في الكود)
    }
}

// ────────────────────────────────────────────────────────────
//  بيانات للصفحة
// ────────────────────────────────────────────────────────────
$tab = $_GET['tab'] ?? 'stats';
if (isset($_GET['msg'])) $msg = $_GET['msg'];

$stats = [];
if (isLoggedIn()) {
    $stats['total']   = (int)$db->query("SELECT COUNT(*) FROM `keys`")->fetchColumn();
    $stats['used']    = (int)$db->query("SELECT COUNT(*) FROM `keys` WHERE used=1 AND revoked=0")->fetchColumn();
    $stats['free']    = (int)$db->query("SELECT COUNT(*) FROM `keys` WHERE used=0 AND revoked=0 AND expires_at>NOW()")->fetchColumn();
    $stats['expired'] = (int)$db->query("SELECT COUNT(*) FROM `keys` WHERE expires_at<=NOW() AND revoked=0")->fetchColumn();
    $stats['revoked'] = (int)$db->query("SELECT COUNT(*) FROM `keys` WHERE revoked=1")->fetchColumn();
    $stats['devices'] = (int)$db->query("SELECT COUNT(DISTINCT device_id) FROM `keys` WHERE device_id IS NOT NULL")->fetchColumn();
    $stats['active']  = (int)$db->query("SELECT COUNT(*) FROM `keys` WHERE used=1 AND revoked=0 AND expires_at>NOW()")->fetchColumn();
}

$allKeys = [];
if (isLoggedIn() && $tab === 'keys') {
    $filter = $_GET['filter'] ?? 'all';
    $search = trim($_GET['search'] ?? '');
    $sql = "SELECT * FROM `keys` WHERE 1=1";
    $params = [];
    if ($filter === 'used')    { $sql .= " AND used=1 AND revoked=0"; }
    if ($filter === 'free')    { $sql .= " AND used=0 AND revoked=0 AND expires_at>NOW()"; }
    if ($filter === 'expired') { $sql .= " AND expires_at<=NOW() AND revoked=0"; }
    if ($filter === 'revoked') { $sql .= " AND revoked=1"; }
    if ($search !== '')        { $sql .= " AND (key_code LIKE ? OR device_id LIKE ?)"; $params[]="%$search%"; $params[]="%$search%"; }
    $sql .= " ORDER BY id DESC LIMIT 200";
    $st = $db->prepare($sql);
    $st->execute($params);
    $allKeys = $st->fetchAll();
}

$generatedKeys = [];
if ($tab === 'generate' && isset($_GET['done']) && isset($_SESSION['generated_keys'])) {
    $generatedKeys = $_SESSION['generated_keys'];
    unset($_SESSION['generated_keys']);
}
?>
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>BLRX VIP - Admin Panel</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
:root{
    --bg:#0a0000;--bg2:#130303;--bg3:#1a0404;
    --red:#cc1111;--red2:#ff2222;--red3:#ff5555;
    --text:#f0e0e0;--text2:#aa7777;--border:#3a0808;
    --success:#1a6a1a;--warn:#6a5500;
}
body{background:var(--bg);color:var(--text);font-family:system-ui,sans-serif;min-height:100vh}
a{color:var(--red3);text-decoration:none}
::-webkit-scrollbar{width:6px;height:6px}
::-webkit-scrollbar-track{background:var(--bg2)}
::-webkit-scrollbar-thumb{background:var(--red);border-radius:3px}

.login-wrap{display:flex;align-items:center;justify-content:center;min-height:100vh;background:radial-gradient(ellipse at center,#1a0303 0%,#000 70%)}
.login-box{background:var(--bg2);border:1.5px solid var(--red);border-radius:16px;padding:40px 36px;width:340px;box-shadow:0 0 40px #aa000033}
.login-box h1{text-align:center;color:var(--red2);margin-bottom:8px;font-size:1.6rem;letter-spacing:2px}
.login-box p{text-align:center;color:var(--text2);margin-bottom:28px;font-size:.85rem}
.field{margin-bottom:16px}
.field label{display:block;color:var(--text2);font-size:.8rem;margin-bottom:6px}
.field input{width:100%;padding:10px 14px;background:#0d0202;border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:1rem;outline:none;transition:.2s}
.field input:focus{border-color:var(--red)}
.btn{display:inline-flex;align-items:center;justify-content:center;gap:6px;padding:10px 22px;border:none;border-radius:8px;font-size:.9rem;cursor:pointer;transition:.2s;font-weight:600}
.btn-red{background:var(--red);color:#fff}.btn-red:hover{background:var(--red2)}
.btn-dark{background:var(--bg3);color:var(--text);border:1px solid var(--border)}.btn-dark:hover{border-color:var(--red);color:var(--red3)}
.btn-sm{padding:5px 14px;font-size:.78rem;border-radius:6px}
.btn-danger{background:#4a0808;color:#ff8888;border:1px solid #7a1111}.btn-danger:hover{background:#6a0808}
.btn-warn{background:#4a3800;color:#ffcc44;border:1px solid #7a6200}.btn-warn:hover{background:#5a4400}
.full-btn{width:100%;margin-top:4px}
.err{background:#3a0808;border:1px solid #7a1111;color:#ff8888;padding:10px 14px;border-radius:8px;margin-bottom:16px;font-size:.85rem}
.suc{background:#0a2a0a;border:1px solid #1a5a1a;color:#66ee66;padding:10px 14px;border-radius:8px;margin-bottom:16px;font-size:.85rem}

.layout{display:flex;min-height:100vh}
.sidebar{width:220px;background:var(--bg2);border-left:1px solid var(--border);flex-shrink:0;display:flex;flex-direction:column}
.sidebar-logo{padding:24px 20px 16px;border-bottom:1px solid var(--border)}
.sidebar-logo h2{color:var(--red2);font-size:1.3rem;letter-spacing:2px}
.sidebar-logo p{color:var(--text2);font-size:.75rem;margin-top:4px}
.nav{flex:1;padding:12px 0}
.nav-item{display:flex;align-items:center;gap:10px;padding:11px 20px;color:var(--text2);cursor:pointer;transition:.15s;font-size:.88rem;text-decoration:none}
.nav-item:hover{background:var(--bg3);color:var(--text)}
.nav-item.active{background:var(--bg3);color:var(--red3);border-left:3px solid var(--red)}
.nav-item .ic{font-size:1.1rem;width:20px;text-align:center}
.sidebar-footer{padding:14px 20px;border-top:1px solid var(--border)}
.main{flex:1;overflow:auto;padding:28px 32px}
.page-title{font-size:1.4rem;color:var(--text);margin-bottom:24px;display:flex;align-items:center;gap:10px}

.stats-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:16px;margin-bottom:28px}
.stat-card{background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:20px 16px;text-align:center}
.stat-card .num{font-size:2rem;font-weight:700;color:var(--red2)}
.stat-card .lbl{color:var(--text2);font-size:.78rem;margin-top:4px}
.stat-card.green .num{color:#44cc44}
.stat-card.blue .num{color:#4488ff}
.stat-card.warn .num{color:#ffcc44}

.panel{background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:24px;margin-bottom:24px}
.panel h3{color:var(--red3);margin-bottom:18px;font-size:1rem;display:flex;align-items:center;gap:8px}

.form-row{display:flex;flex-wrap:wrap;gap:14px;align-items:flex-end}
.form-group{display:flex;flex-direction:column;gap:6px}
.form-group label{color:var(--text2);font-size:.78rem}
.form-group select,.form-group input[type=text],.form-group input[type=number],.form-group input[type=password]{
    padding:9px 14px;background:#0d0202;border:1px solid var(--border);border-radius:8px;
    color:var(--text);font-size:.9rem;outline:none;transition:.2s;min-width:120px
}
.form-group select:focus,.form-group input:focus{border-color:var(--red)}

.key-grid{display:flex;flex-wrap:wrap;gap:8px;margin-top:14px}
.key-badge{background:#0d0202;border:1px solid var(--border);border-radius:6px;padding:6px 12px;font-family:monospace;font-size:.9rem;color:#ff9999;letter-spacing:1px;cursor:pointer;transition:.15s}
.key-badge:hover{border-color:var(--red);color:#ffdddd}
.copy-all-bar{margin-top:14px;display:flex;gap:10px;align-items:center}
.copy-hint{color:var(--text2);font-size:.78rem}

.tbl-wrap{overflow-x:auto}
table{width:100%;border-collapse:collapse;font-size:.82rem}
thead th{background:var(--bg3);color:var(--text2);padding:10px 12px;text-align:right;font-weight:600;border-bottom:1px solid var(--border)}
tbody tr{border-bottom:1px solid #1a0404;transition:.15s}
tbody tr:hover{background:#120202}
tbody td{padding:9px 12px;color:var(--text)}
.badge{display:inline-block;padding:2px 9px;border-radius:20px;font-size:.72rem;font-weight:600}
.badge-used{background:#1a4a1a;color:#66ee66}
.badge-free{background:#1a1a4a;color:#6699ff}
.badge-exp{background:#3a2a00;color:#ffaa22}
.badge-rev{background:#3a0808;color:#ff6666}
.badge-active{background:#004a00;color:#88ff88}

.filter-bar{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:16px;align-items:center}
.filter-bar a{padding:5px 14px;border-radius:20px;border:1px solid var(--border);color:var(--text2);font-size:.78rem;transition:.15s}
.filter-bar a.active,.filter-bar a:hover{border-color:var(--red);color:var(--red3);background:var(--bg3)}
.search-bar{display:flex;gap:8px;margin-bottom:14px}
.search-bar input{flex:1;max-width:300px;padding:8px 14px;background:#0d0202;border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:.85rem;outline:none}
.search-bar input:focus{border-color:var(--red)}
.tg-float{position:fixed;bottom:28px;left:28px;width:52px;height:52px;border-radius:50%;background:var(--red);display:flex;align-items:center;justify-content:center;font-size:1.4rem;box-shadow:0 4px 20px #cc111166;transition:.2s;z-index:999}
.tg-float:hover{background:var(--red2);transform:scale(1.1)}
@media(max-width:700px){
    .layout{flex-direction:column}
    .sidebar{width:100%;flex-direction:row;flex-wrap:wrap}
    .main{padding:16px}
}
</style>
</head>
<body>

<?php if (!isLoggedIn()): ?>
<!-- ══════════════════ LOGIN PAGE ══════════════════ -->
<div class="login-wrap">
  <div class="login-box">
    <h1>🔐 BLRX VIP</h1>
    <p>Admin Panel - لوحة التحكم</p>
    <?php if ($err): ?><div class="err"><?=e($err)?></div><?php endif ?>
    <form method="POST">
      <input type="hidden" name="action" value="login">
      <div class="field">
        <label>كلمة المرور</label>
        <input type="password" name="password" autofocus autocomplete="current-password">
      </div>
      <button type="submit" class="btn btn-red full-btn">دخول</button>
    </form>
    <div style="text-align:center;margin-top:20px">
      <a href="https://t.me/SARKEXx48" target="_blank" style="color:var(--text2);font-size:.78rem">📱 @SARKEXx48</a>
    </div>
  </div>
</div>

<?php else: ?>
<!-- ══════════════════ ADMIN LAYOUT ══════════════════ -->
<div class="layout">

  <aside class="sidebar">
    <div class="sidebar-logo">
      <h2>BLRX VIP</h2>
      <p>مرحباً، <?=e($_SESSION['admin_user'])?></p>
    </div>
    <nav class="nav">
      <a href="?tab=stats"    class="nav-item <?=$tab==='stats'?'active':''?>"><span class="ic">📊</span> الإحصائيات</a>
      <a href="?tab=generate" class="nav-item <?=$tab==='generate'?'active':''?>"><span class="ic">🔑</span> إنشاء مفاتيح</a>
      <a href="?tab=keys"     class="nav-item <?=$tab==='keys'?'active':''?>"><span class="ic">📋</span> إدارة المفاتيح</a>
      <a href="?tab=revoke"   class="nav-item <?=$tab==='revoke'?'active':''?>"><span class="ic">🚫</span> إلغاء مفتاح</a>
    </nav>
    <div class="sidebar-footer">
      <form method="POST">
        <input type="hidden" name="action" value="logout">
        <button type="submit" class="btn btn-dark btn-sm">🚪 خروج</button>
      </form>
    </div>
  </aside>

  <main class="main">
    <?php if ($msg): ?><div class="suc"><?=e($msg)?></div><?php endif ?>
    <?php if ($err): ?><div class="err"><?=e($err)?></div><?php endif ?>

    <?php if ($tab === 'stats'): ?>
    <div class="page-title">📊 الإحصائيات</div>
    <div class="stats-grid">
      <div class="stat-card"><div class="num"><?=$stats['total']?></div><div class="lbl">إجمالي المفاتيح</div></div>
      <div class="stat-card green"><div class="num"><?=$stats['active']?></div><div class="lbl">مستخدمة (نشطة)</div></div>
      <div class="stat-card blue"><div class="num"><?=$stats['free']?></div><div class="lbl">غير مستخدمة</div></div>
      <div class="stat-card warn"><div class="num"><?=$stats['expired']?></div><div class="lbl">منتهية الصلاحية</div></div>
      <div class="stat-card"><div class="num"><?=$stats['revoked']?></div><div class="lbl">ملغاة</div></div>
      <div class="stat-card blue"><div class="num"><?=$stats['devices']?></div><div class="lbl">أجهزة مرتبطة</div></div>
    </div>
    <div class="panel">
      <h3>📈 نشاط اليوم</h3>
      <?php
        $today   = $db->query("SELECT COUNT(*) FROM verify_log WHERE DATE(at)=CURDATE()")->fetchColumn();
        $todayOk = $db->query("SELECT COUNT(*) FROM verify_log WHERE DATE(at)=CURDATE() AND result='OK'")->fetchColumn();
      ?>
      <p style="color:var(--text2);font-size:.9rem">طلبات التحقق اليوم: <strong style="color:var(--text)"><?=$today?></strong> &nbsp;|&nbsp; ناجح: <strong style="color:#66ee66"><?=$todayOk?></strong> &nbsp;|&nbsp; فاشل: <strong style="color:#ff6666"><?=$today-$todayOk?></strong></p>
    </div>

    <?php elseif ($tab === 'generate'): ?>
    <div class="page-title">🔑 إنشاء مفاتيح</div>
    <div class="panel">
      <h3>⚙️ إعدادات الإنشاء</h3>
      <form method="POST">
        <input type="hidden" name="action" value="generate">
        <div class="form-row">
          <div class="form-group">
            <label>مدة المفتاح</label>
            <select name="days">
              <option value="1">1 يوم</option>
              <option value="3">3 أيام</option>
              <option value="7" selected>7 أيام</option>
              <option value="15">15 يوم</option>
              <option value="30">30 يوم</option>
            </select>
          </div>
          <div class="form-group">
            <label>الكمية</label>
            <input type="number" name="qty" value="10" min="1" max="200" style="width:100px">
          </div>
          <div class="form-group" style="justify-content:flex-end">
            <button type="submit" class="btn btn-red">✨ إنشاء</button>
          </div>
        </div>
      </form>
    </div>

    <?php if (!empty($generatedKeys)): ?>
    <div class="panel">
      <h3>✅ المفاتيح المنشأة (<?=count($generatedKeys)?>)</h3>
      <div class="key-grid" id="keyGrid">
        <?php foreach ($generatedKeys as $k): ?>
        <div class="key-badge" onclick="copyOne('<?=e($k)?>')" title="اضغط للنسخ"><?=e($k)?></div>
        <?php endforeach ?>
      </div>
      <div class="copy-all-bar">
        <button class="btn btn-dark btn-sm" onclick="copyAll()">📋 نسخ الكل</button>
        <span class="copy-hint" id="copyHint"></span>
      </div>
    </div>
    <script>
    function copyOne(k){navigator.clipboard.writeText(k).then(()=>{document.getElementById('copyHint').textContent='تم نسخ: '+k;setTimeout(()=>document.getElementById('copyHint').textContent='',2000)})}
    function copyAll(){
      const keys=[...document.querySelectorAll('.key-badge')].map(el=>el.textContent.trim()).join('\n');
      navigator.clipboard.writeText(keys).then(()=>{document.getElementById('copyHint').textContent='تم نسخ '+document.querySelectorAll('.key-badge').length+' مفتاح ✓';setTimeout(()=>document.getElementById('copyHint').textContent='',3000)});
    }
    </script>
    <?php endif ?>

    <?php elseif ($tab === 'keys'): ?>
    <div class="page-title">📋 إدارة المفاتيح</div>
    <?php $f = $_GET['filter'] ?? 'all'; $s = $_GET['search'] ?? ''; ?>
    <div class="filter-bar">
      <a href="?tab=keys&filter=all<?=$s?"&search=".urlencode($s):''?>"     class="<?=$f==='all'?'active':''?>">الكل</a>
      <a href="?tab=keys&filter=free<?=$s?"&search=".urlencode($s):''?>"    class="<?=$f==='free'?'active':''?>">🟢 متاحة</a>
      <a href="?tab=keys&filter=used<?=$s?"&search=".urlencode($s):''?>"    class="<?=$f==='used'?'active':''?>">🔵 مستخدمة</a>
      <a href="?tab=keys&filter=expired<?=$s?"&search=".urlencode($s):''?>" class="<?=$f==='expired'?'active':''?>">🟡 منتهية</a>
      <a href="?tab=keys&filter=revoked<?=$s?"&search=".urlencode($s):''?>" class="<?=$f==='revoked'?'active':''?>">🔴 ملغاة</a>
    </div>
    <div class="search-bar">
      <form method="GET" style="display:contents">
        <input type="hidden" name="tab" value="keys">
        <input type="hidden" name="filter" value="<?=e($f)?>">
        <input type="text" name="search" value="<?=e($s)?>" placeholder="بحث بالمفتاح أو الجهاز...">
        <button type="submit" class="btn btn-dark btn-sm">🔍</button>
      </form>
    </div>
    <div class="panel" style="padding:0">
      <div class="tbl-wrap">
        <table>
          <thead>
            <tr>
              <th>المفتاح</th><th>الحالة</th><th>المدة</th><th>تاريخ الإنشاء</th><th>تاريخ الانتهاء</th><th>Device ID</th><th>إجراء</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($allKeys as $row): ?>
            <?php
              $now = time(); $exp = strtotime($row['expires_at']);
              if ($row['revoked'])       $badge = '<span class="badge badge-rev">ملغى</span>';
              elseif ($exp < $now)       $badge = '<span class="badge badge-exp">منتهي</span>';
              elseif ($row['used'])      $badge = '<span class="badge badge-active">نشط</span>';
              else                       $badge = '<span class="badge badge-free">متاح</span>';
            ?>
            <tr>
              <td><code style="color:#ff9999;letter-spacing:1px"><?=e($row['key_code'])?></code></td>
              <td><?=$badge?></td>
              <td><?=e($row['plan'])?></td>
              <td style="color:var(--text2)"><?=e(substr($row['created_at'],0,16))?></td>
              <td style="color:<?=$exp<$now?'#ff6633':'var(--text2)'?>"><?=e(substr($row['expires_at'],0,16))?></td>
              <td style="font-size:.75rem;color:var(--text2)"><?=$row['device_id']?e(substr($row['device_id'],0,18)).'…':'—'?></td>
              <td>
                <?php if (!$row['revoked']): ?>
                <form method="POST" style="display:inline" onsubmit="return confirm('إلغاء المفتاح؟')">
                  <input type="hidden" name="action" value="revoke">
                  <input type="hidden" name="key_code" value="<?=e($row['key_code'])?>">
                  <button type="submit" class="btn btn-warn btn-sm">🚫</button>
                </form>
                <?php endif ?>
                <form method="POST" style="display:inline" onsubmit="return confirm('حذف؟')">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="key_id" value="<?=(int)$row['id']?>">
                  <button type="submit" class="btn btn-danger btn-sm">🗑</button>
                </form>
              </td>
            </tr>
            <?php endforeach ?>
            <?php if (empty($allKeys)): ?>
            <tr><td colspan="7" style="text-align:center;padding:30px;color:var(--text2)">لا توجد مفاتيح</td></tr>
            <?php endif ?>
          </tbody>
        </table>
      </div>
    </div>

    <?php elseif ($tab === 'revoke'): ?>
    <div class="page-title">🚫 إلغاء مفتاح</div>
    <div class="panel">
      <h3>إلغاء مفتاح يدوياً</h3>
      <form method="POST">
        <input type="hidden" name="action" value="revoke">
        <div class="form-row">
          <div class="form-group" style="flex:1">
            <label>رمز المفتاح</label>
            <input type="text" name="key_code" placeholder="مثال: A9X4K7P2Q1" style="width:100%">
          </div>
          <div class="form-group" style="justify-content:flex-end">
            <button type="submit" class="btn btn-danger" onclick="return confirm('تأكيد الإلغاء؟')">🚫 إلغاء</button>
          </div>
        </div>
      </form>
    </div>
    <div class="panel">
      <h3>📋 المفاتيح النشطة</h3>
      <div class="tbl-wrap">
        <table>
          <thead><tr><th>المفتاح</th><th>المدة</th><th>الانتهاء</th><th>Device ID</th><th>إلغاء</th></tr></thead>
          <tbody>
          <?php
            $active = $db->query("SELECT * FROM `keys` WHERE used=1 AND revoked=0 AND expires_at>NOW() ORDER BY expires_at LIMIT 100")->fetchAll();
            foreach ($active as $row):
          ?>
          <tr>
            <td><code style="color:#ff9999"><?=e($row['key_code'])?></code></td>
            <td><?=e($row['plan'])?></td>
            <td style="color:var(--text2)"><?=e(substr($row['expires_at'],0,16))?></td>
            <td style="font-size:.75rem;color:var(--text2)"><?=$row['device_id']?e(substr($row['device_id'],0,20)).'…':'—'?></td>
            <td>
              <form method="POST" style="display:inline" onsubmit="return confirm('إلغاء؟')">
                <input type="hidden" name="action" value="revoke">
                <input type="hidden" name="key_code" value="<?=e($row['key_code'])?>">
                <button type="submit" class="btn btn-danger btn-sm">🚫 إلغاء</button>
              </form>
            </td>
          </tr>
          <?php endforeach ?>
          <?php if(empty($active)): ?><tr><td colspan="5" style="text-align:center;padding:20px;color:var(--text2)">لا توجد مفاتيح نشطة</td></tr><?php endif ?>
          </tbody>
        </table>
      </div>
    </div>
    <?php endif ?>

  </main>
</div>

<a href="https://t.me/SARKEXx48" target="_blank" class="tg-float" title="تيليجرام">✈</a>

<?php endif ?>
</body>
</html>