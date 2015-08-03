<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';
echo queryValue(null, "select script from userTasks where userId=? and taskId=?", 'ss',
        $_POST["userId"], $_POST["taskId"]);
?>