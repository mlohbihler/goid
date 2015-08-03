<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';
$sql = "select splitLocation, outputSplitLocation, frameInfo from users where id=?";
$result = queryRow(null, $sql, 's', $_POST["userId"]);
if (!empty($result))
    echo $result["splitLocation"] ."|". $result["outputSplitLocation"] ."|". $result["frameInfo"];
?>
