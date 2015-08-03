<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$taskId = $_REQUEST['taskId'];
if (empty($taskId))
    die("Invalid task id");

$taskInfo = getTaskInfo(null, $taskId, null);
if (empty($taskInfo))
    die("Invalid task");

$shortTaskInclude = 'content/desc/short_'. $taskId .'.html';

$goidVer = 26;
$taskVer = 19;
$seroVer = 12;

$title = 'Play GoiD!';
include 'include/header.php';
?>

<div class="textContent">
  <div style="float:right;"><a href="/home"><img src="/images/logo_small_alpha.png" border="0"/></a></div>
  
  <h1><?php echo $taskInfo["name"]; ?></h1>
  <p>Level: <span class="taskLevel"><?php echo $decode_taskLevels[$taskInfo["level"]]; ?></span></p>
  <p><?php include $shortTaskInclude; ?></p>
  <p><a href="/play/<?php echo $taskId ?>">Back to task description</a></p>
</div>
<br/>

<a name="applet"></a>
<script type="text/javascript" src="http://www.java.com/js/deployJava.js"></script>
<script type="text/javascript">
  var attributes = {
          scriptable: "false",
          mayscript: "false",
          cache_archive: "/appletRes/goid.jar,/appletRes/task.jar,/appletRes/seroUtils.jar",
          cache_version: "0.0.0.<?php echo $goidVer; ?>, 0.0.0.<?php echo $taskVer; ?>, 0.0.0.<?php echo $seroVer; ?>",
          archive: "/appletRes/commons-httpclient-3.0.1.jar,/appletRes/commons-logging-1.1.1.jar,/appletRes/commons-codec-1.3.jar",
          code: "com.serotonin.goid.applet.AppletMain",
          width: "100%",
          height: "600"
  };
  var parameters = {
          codebase_lookup: "true",
          image: "/images/agent_play_goid.png",
          boxbgcolor: 'white',
          boxborder: 'true',
          centerimage: 'true',
          <?php if (!empty($userData)) { ?>
            userId: "<?php echo $userData["id"]; ?>",
            splitLocation: "<?php echo $userData["splitLocation"]; ?>",
            outputSplitLocation: "<?php echo $userData["outputSplitLocation"]; ?>",
          <?php } ?>
          taskId: "<?php echo $taskId; ?>",
          taskClass: "<?php echo $taskInfo["className"]; ?>"
  };
  var version = '1.6';
  deployJava.runApplet(attributes, parameters, version);
</script>

<?php include 'include/footer.php'; ?>