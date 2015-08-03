<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$userCount = queryValue(null, "select count(*) from users");

include 'include/header.php';
?>
<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <br/>
    Welcome to...
    <h1>The Game of Intelligent Design</h1>
    <p>
      The Game of Intelligent Design is a new type of game built for big-brained people. Join the other
      <b><?php echo $userCount; ?></b> (and counting) registered users, and play now!
    </p>
    <p>
      It might not be pretty, but it's addictive. Find out how the quest for Artificial General Intelligence can be
      fun!
    </p>
    <p>
      <a href="/help">Break it down</a> &ndash; What it's all about<br/>
      <a href="/play/donut">Play it now</a> &ndash; Give it a try
    </p>
    
    <h2>News</h2>
    <p>
      Nov 30, 2009 - The release level of this game has been reduced to alpha. Some users have reported that the Java 
      version that is required by GoiD means they actually have to upgrade. (It's a big download, so i sympathize... 
      really i do.) Alternatives that are being researched include various Flash delivery platforms, Silverlight, and 
      ways to relax the Java version requirement, so stay tuned (i.e. register and you'll be sent an email with
      an update). In the meantime you can still play with the current version and everything will be fine once you get
      the correct version of Java installed.
    </p>
  </div>
</div>

<?php include 'include/footer.php'; ?>