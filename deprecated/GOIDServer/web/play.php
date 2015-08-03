<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$taskId = $_REQUEST['taskId'];
if (empty($taskId))
    die("Invalid task id");

$conn = createConn();
$taskInfo = getTaskInfo($conn, $taskId, null);
if (empty($taskInfo)) {
    $conn->close();
    die("Invalid task");
}

$shortTaskInclude = 'content/desc/short_'. $taskId .'.html';
$longTaskInclude = 'content/desc/long_'. $taskId .'.html';

// Get the stats
$playerCount = queryValue($conn, "select count(1) from userTasks where taskId=?", "s", $taskId);
if ($userData != null)
    $userRank = queryRow($conn, "select userId, rank from userTasks where taskId=? and userId=?", "ss",
            $taskId, $userData["id"]);

$sql = "select u.username, ut.rank, ut.score
        from userTasks ut
          join users u on u.id=ut.userId
        where ut.taskId=?
          and completed
        order by rank
        limit 10";
$topPlayers = query($conn, $sql, "s", $taskId);

$conn->close();

$title = 'Play GoiD!';
include 'include/header.php';
?>
<div class="textContent">
  <div style="float:right;padding-left:20px;">
    <a href="/home"><img src="/images/logo_small_alpha.png" border="0"/></a><br/>
    <p style="text-align:right;">
      <a href="/home">Home</a> |
      <a href="/tasks">Tasks</a> |
      <a href="/help">Help</a> |
      <a href="/blog/">Blog</a> |
      <?php if ($userData["email"] == null) { ?>
        <a href="/register">Register</a> |
        <a href="/login">Login</a>
      <?php } else { ?>
        <a href="/profile"><?php echo $userData["username"]; ?></a>
        <?php if (!$userData["emailVerified"]) echo "(unverified)"; ?>
        (<a href="/login">not you?</a>) |
        <a href="/logout">Logout</a>
      <?php } ?>
    </p>
  </div>
  
  <?php if ($playerCount > 0) { ?>
    <div style="clear:right;float:right;" class="taskStats">
      <h3>Stats</h3>
      <dl class="stats">
        <dt>Players</dt>
        <dd><?php echo $playerCount; ?></dd>
        
        <?php if ($userRank["userId"] != null) { ?>
          <dt>Your rank</dt>
          <dd><?php
            if ($userRank["rank"] == null)
                echo "Incomplete";
            else
                echo $userRank["rank"];
          ?></dd>
        <?php } ?>
      </dl>
      
      <?php if (!empty($topPlayers)) { ?>
        <h3>Top players</h3>
        <table>
          <?php foreach ($topPlayers as $tp) { ?>
            <tr>
              <th>#<?php echo $tp["rank"]; ?></th>
              <td><?php echo $tp["username"]; ?></td>
              <td>(<?php echo $tp["score"]; ?>)</td>
            </tr>
          <?php } ?>
        </table>
      <?php } ?>
    </div>
  <?php } ?>
  
  <h1><?php echo $taskInfo["name"]; ?></h1>
  <p>Level: <span class="taskLevel"><?php echo $decode_taskLevels[$taskInfo["level"]]; ?></span></p>
  <p><?php include $shortTaskInclude; ?></p>
  <p>
    For general GoiD concepts visit the <a href="/help">help</a> page. For more information on this task, read on.
  </p>
  <?php include $longTaskInclude; ?>
  <p>
    <b>Play now:</b>
    <script src="http://www.java.com/js/deployJava.js"></script>
    <script>
      deployJava.launchButtonPNG='/images/agent_small.png';
      var url = "http://<?php echo $_SERVER['HTTP_HOST']; ?>/<?php echo $taskId; ?>_<?php echo $userData["id"]; ?>.jnlp";
      deployJava.createWebStartLaunchButton(url, '1.6.0');
    </script>
    (or use the <a href="/applet/<?php echo $taskId; ?>">applet</a>)
  </p>
  <p>
    All tasks can be played either as applets or with Java Web Start. Applets are convenient, but the rendering in 
    Web Start is much better. Web start is recommended.
  </p>
</div>
<br/>

<?php include 'include/footer.php'; ?>