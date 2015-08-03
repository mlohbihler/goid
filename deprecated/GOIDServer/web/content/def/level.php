<?php 
define( '_topLevelPage', 1 );
require_once '../../include/common.php';
?>
Task levels are:
<dl class="levelDef">
  <dt><?php echo $decode_taskLevels[1]; ?></dt>
  <dd>Introductory task</dd>
  <dt><?php echo $decode_taskLevels[2]; ?></dt>
  <dd>Good for learning useful techniques</dd>
  <dt><?php echo $decode_taskLevels[3]; ?></dt>
  <dd>You'll need those useful techniques now</dd>
  <dt><?php echo $decode_taskLevels[4]; ?></dt>
  <dd>Be prepared to spend a lot of time</dd>
  <dt><?php echo $decode_taskLevels[5]; ?></dt>
  <dd>You'd better be financially independent and single, because this could take a long, long time.</dd>
</dl>