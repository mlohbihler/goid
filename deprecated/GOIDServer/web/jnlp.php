<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$taskId = $_GET['taskId'];
if (empty($taskId))
    die("Missing task id");

// Use the goidId parameter if it exists to determine the user. The cookie that was sent did not necessarily come 
// from the browser. In particular, JWS will get the IE cookie and send that even if the user is using FF at the 
// moment.
$userData = getUserData($_GET['goidId']);

$taskInfo = getTaskInfo(null, $taskId, $userData["id"]);
if (empty($taskInfo))
    die("Invalid task id: "+ $taskId);

header("Content-type: application/x-java-jnlp-file");
header("Cache-Control: no-cache, must-revalidate"); // HTTP/1.1
header("Expires: Sat, 26 Jul 1997 05:00:00 GMT"); // Date in the past

echo '<?xml version="1.0" encoding="utf-8"?>';
?>
<jnlp spec="1.0+" codebase="http://<?php echo $_SERVER['HTTP_HOST']; ?>" href="http://<?php echo $_SERVER['HTTP_HOST']; ?>/<?php echo $taskId; ?>_<?php echo $userData["id"]; ?>.jnlp">
  <information>
    <title>The Game Of Intelligent Design (<?php echo $taskId; ?>)</title>
    <vendor>Serotonin Software Technologies, Inc.</vendor>
    <homepage href="/home"/>
    <description>The Game Of Intelligent Design (<?php echo $taskId; ?>)</description>
    <description kind="short"><?php echo $taskInfo["name"]; ?></description>
    <icon href="/images/logo_icon.gif"/>
    <icon kind="splash" href="/images/splash.gif"/>
    <offline-allowed/>
  </information>
  <resources>
    <j2se version="1.6"/>
    <jar href="/appletRes/goid.jar"/>
    <jar href="/appletRes/task.jar"/>
    <jar href="/appletRes/seroUtils.jar"/>
    <jar href="/appletRes/commons-httpclient-3.0.1.jar"/>
    <jar href="/appletRes/commons-logging-1.1.1.jar"/>
    <jar href="/appletRes/commons-codec-1.3.jar"/>
  </resources>
  <application-desc main-class="com.serotonin.goid.applet.JnlpMain">
    <argument>x<?php echo $userData["id"]; ?></argument>
    <argument>x<?php echo $taskId; ?></argument>
    <argument>x<?php echo $taskInfo["className"]; ?></argument>
  </application-desc>
</jnlp>