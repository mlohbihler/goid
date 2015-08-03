<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';
if ($_GET["t"] != $utils_password)
    die("Missing password");

$time = time();
$conn = createConn();

$taskIds = query($conn, "select id from tasks");
foreach ($taskIds as $taskIdRow)
    updateTaskRankings($conn, $taskIdRow["id"]);

updateGoidRankings($conn);
        
$conn->close();
?>
Done