<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$sql = "select t.id, t.name, t.level, t.authorName, t.authorLink, ut.players, ut2.userId, ut2.rank
        from tasks t
          left join (select taskId, count(*) as players from userTasks group by taskId) ut on t.id=ut.taskId
          left join userTasks ut2 on t.id=ut2.taskId and ut2.userId=?
        order by priority";
$tasks = query(null, $sql, 's', $userData["id"]);

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <h1>Tasks</h1>
    <?php foreach ($tasks as $task) { ?>
      <table style="float:right;width:250px;" class="taskInfo">
        <tr>
          <td>Author</td>
          <td>
            <?php
              if ($task["authorLink"] == null)
                  echo $task["authorName"];
              else
                  echo "<a href='". $task["authorLink"] ."'>". $task["authorName"] ."</a>";
            ?>
          </td>
        </tr>
        <tr>
          <td><span class="def">Level</span></td>
          <td><span class="taskLevel"><?php echo $decode_taskLevels[$task["level"]]; ?></span></td>
        </tr>
        <tr>
          <td><span class="def">Players</span></td>
          <td><?php echo $task["players"] == null ? "0" : $task["players"]; ?></td>
        </tr>
        <?php if ($userData != null) { ?>
          <tr>
            <td>Your <span class="def">rank</span></td>
            <td>
              <?php
                if ($task["userId"] == null)
                    echo "Not attempted";
                elseif ($task["rank"] == null)
                    echo "Incomplete";
                else
                    echo $task["rank"];
              ?>
            </td>
          </tr>
        <?php } ?>
      </table>
      <a href="/play/<?php echo $task["id"]; ?>"><b><?php echo $task["name"]; ?></b></a>
      <p style="margin-left:40px;">
        <?php
          $include =  "content/desc/short_". $task["id"] .".html";
          include $include; 
        ?>
      </p>
      <div style="clear:both;"></div>
    <?php } ?>
  </div>
</div>

<?php include 'include/footer.php'; ?>