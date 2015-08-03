<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';

$userId = $_POST["userId"];
$taskId = $_POST["taskId"];
$script = stripslashes($_POST["script"]);

$sql = "insert into userTasks (userId, taskId, script, completed, lastUpdated) values (?,?,?,false,?)
        on duplicate key update script=?, lastUpdated=?";
$time = time();
$rows = executeStatement(null, $sql, 'sssisi', $userId, $taskId, $script, $time, $script, $time);
if ($rows < 1)
    echo "Invalid affected rows: ". $rows .", user=". $userId .", task=". $taskId;
?>