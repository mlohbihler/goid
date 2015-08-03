<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$conn = createConn();

// Handle the resending of the verification email.
$task = $_GET['task'];
$message = null;
if ($task == "resend") {
    sendVerificationEmail($userData["id"], $userData["verificationToken"], $userData["email"]);
    $message = "Verification email was resent";
}

// Handle the updating of profile data.
$update = $_POST['update'];
$notifyOnNewTask = filter_var($_POST['notifyOnNewTask'], FILTER_VALIDATE_BOOLEAN);
$notifyOnBeatenTopRank = filter_var($_POST['notifyOnBeatenTopRank'], FILTER_VALIDATE_BOOLEAN);

if (!empty($update)) {
    executeStatement($conn, "update users set notifyOnNewTask=?, notifyOnBeatenTopRank=? where id=?", 'iis',
           $notifyOnNewTask, $notifyOnBeatenTopRank, $userData["id"]);
    $message = "Updates have been saved.";
    
    // Reload the user information.
    $userData = getUserData($userData["id"]);
}

// Handle the changing of the password
$passwordMessage = null;
$changePassword = $_POST["changePassword"];
$newPassword = $_POST["newPassword"];

if (!empty($changePassword)) {
    // Validate the password.
    $passwordMessage = validatePassword($newPassword);
    if (empty($passwordMessage)) {
        $hash = hashPassword(generatePasswordSalt(), $newPassword);
        executeStatement($conn, "update users set password=? where id=?", 'ss', $hash, $userData["id"]);
        $passwordMessage = "You password has been changed.";
    }
}

// Set the state of the checkboxes
$notifyOnNewTaskChecked = "";
$notifyOnBeatenTopRankChecked = "";
if ($userData["notifyOnNewTask"])
    $notifyOnNewTaskChecked = ' checked="checked"';
if ($userData["notifyOnBeatenTopRank"])
    $notifyOnBeatenTopRankChecked = ' checked="checked"';

// Get stats data
if ($userData["emailVerified"]) {
    $sql = "select 'taskTotal' as id, count(1) as cnt from tasks
              union
            select 'taskCount', count(1) from userTasks where userId=?
              union
            select 'taskComplete', count(1) from userTasks where userId=? and completed
              union
            select 'bestRank', min(rank) from userTasks where userId=?
              union
            select 'userCount', count(1) from users
            ";
    $data = query($conn, $sql, "sss", $userData["id"], $userData["id"], $userData["id"]);
    
    // Normalize the data
    $stats = array();
    foreach ($data as $stat)
        $stats[$stat["id"]] = $stat["cnt"];
}

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <?php if (empty($userData)) {?>
      Not logged in.
    <?php } else if (!$userData["emailVerified"]) { ?>
      Your email address has not yet been verified. <a href="/profile?task=resend">Resend my verification email.</a>
      <?php if (!empty($message)) { ?>
        <br/><br/><span class="error"><?php echo $message; ?></span>
      <?php } ?>
    <?php } else { ?>
      <h3>Stats</h3>
      <dl class="stats">
        <dt>GoiD rank</dt>
        <dd><?php
          if ($userData["goidRank"] == null)
              echo "Unranked. <a href='/tasks'>Go play something.</a>";
          else
              echo "#". $userData["goidRank"] ." of ". $stats["userCount"] ." players";
        ?></dd>
        <dt>Best task rank</dt>
        <dd><?php echo ($stats["bestRank"] == null ? "-" : $stats["bestRank"]) ?></dd>
        <dt>Completed tasks</dt>
        <dd><?php echo $stats["taskComplete"] ?></dd>
        <dt>Incomplete tasks</dt>
        <dd><?php echo $stats["taskCount"] - $stats["taskComplete"] ?></dd>
        <dt>Unattempted tasks</dt>
        <dd><?php echo $stats["taskTotal"] - $stats["taskCount"] ?></dd>
      </dl>
    
      <h3>Notifications</h3>
      <form action="" method="post">
        Email: <?php echo $userData["email"]; ?><br/>
        <input type="checkbox" name="notifyOnNewTask" id="notifyOnNewTask"<?php echo $notifyOnNewTaskChecked; ?> value="true"/>
                <label for="notifyOnNewTask">Notify me when a new task is released</label><br/>
        <input type="checkbox" name="notifyOnBeatenTopRank" id="notifyOnBeatenTopRank"<?php echo $notifyOnBeatenTopRankChecked; ?> value="true"/>
                <label for="notifyOnBeatenTopRank">Notify me when if someone beats my top rank</label><br/>
        <input type="submit" name="update" onclick="return dbclickCheck(this);" value="Update"/>
      </form>
      <?php if (!empty($message)) { ?>
        <span class="error"><?php echo $message; ?></span>
      <?php } ?>
      
      <h3>Password</h3>
      <form action="" method="post">
        To change your password, enter the new one.<br/>
        New password: <input type="password" name="newPassword"/><br/>
        <input type="submit" name="changePassword" onclick="return dbclickCheck(this);" value="Change password"/>
      </form>
      <?php if (!empty($passwordMessage)) { ?>
        <span class="error"><?php echo $passwordMessage; ?></span>
      <?php } ?>
    <?php } ?>
  </div>
</div>

<?php include 'include/footer.php'; ?>


