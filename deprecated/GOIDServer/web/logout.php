<?php 
define( '_topLevelPage', 1 );
require_once 'include/common.php';
setcookie($cookieName, "", time() - 60*60, "/");
header('Location:/login');
?>