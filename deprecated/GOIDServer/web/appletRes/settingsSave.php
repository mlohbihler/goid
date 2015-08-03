<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';

$userId = $_POST["userId"];
$splitLocation = $_POST["splitLocation"];
$outputSplitLocation = $_POST["outputSplitLocation"];
$frameInfo = stripslashes($_POST["frameInfo"]);

if (empty($frameInfo))
    executeStatement(null, "update users set splitLocation=?, outputSplitLocation=? where id=?", 'iis',
            $splitLocation, $outputSplitLocation, $userId);
else
    executeStatement(null, "update users set splitLocation=?, outputSplitLocation=?, frameInfo=? where id=?", 'iiss',
            $splitLocation, $outputSplitLocation, $frameInfo, $userId);
?>
