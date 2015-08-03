<?php 
defined('_topLevelPage') or die('Restricted access');

$host = 'gameofid.com';
if ($_SERVER['HTTP_HOST'] == 'gameofid.com') {
    $host = 'gameofintelligentdesign.com';
}

?>
<div style="float:left;">
  <a href="/home"><img src="images/logo_small.png" border="0"/></a>
</div>
<div class="nav">
  <a href="http://<?php echo $host; ?>/home">Home</a> |
  <a href="/tasks">Tasks</a> |
  <a href="/help">Help</a> |
  <a href="http://blog.serotoninsoftware.com/">Blog</a><br/>
  <?php if ($userData["email"] == null) { ?>
    <a href="/register">Register</a> |
    <a href="/login">Login</a>
  <?php } else { ?>
    <a href="/profile"><?php echo $userData["username"]; ?></a>
    <?php if (!$userData["emailVerified"]) echo "(unverified)"; ?>
    (<a href="/login">not you?</a>) |
    <a href="/logout">Logout</a>
  <?php } ?>
  <br/>
  <br/>
  <br/>
  <br/>
  Think you're smart?<br/>
  <b>Play GoiD</b>
</div>