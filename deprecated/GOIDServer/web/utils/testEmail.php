<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';
if ($_GET["t"] != $utils_password)
    die("Missing password");

$msg = "This is a test email from GoiD";
sendMail("ml@serotoninsoftware.com", "GoiD test email", $msg);
?>
Email sent.