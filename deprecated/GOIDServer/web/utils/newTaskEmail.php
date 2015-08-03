<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';
if ($_GET["t"] != $utils_password)
    die("Missing password");

$taskId = $_GET["taskId"];

$conn = createConn();

$taskInfo = getTaskInfo($conn, $taskId, null);
if ($taskInfo == null) {
    $conn->close();
    die("Invalid taskId");
}

$userData = query($conn, "select email from users where notifyOnNewTask");
$count = 0;
foreach ($userData as $user) {
    $url = "http://". $_SERVER['HTTP_HOST'] ."/play/". $taskId;
    $msg = "A new GoiD task is ready for you to play. <a href='". $url ."'>". $taskInfo["name"] ."</a>";
    sendMail($user["email"], "A new GoiD task has been released", $msg);
    $count++;
}

$conn->close();
?>
Done. <?php echo $count; ?> emails sent.