<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$submit = $_POST['submit'];
$email = $_POST['email'];

if (!empty($submit)) {
    $conn = createConn();
    
    $userId = queryValue($conn, "select id from users where email=?", 's', $email);
    
    if (empty($userId))
        $message = "That email address was not found";
    else {
        // Create a new password.
        $password = generateToken(6);
        $hash = hashPassword(generatePasswordSalt(), $password);
        executeStatement($conn, "update users set password=? where id=?", 'ss', $hash, $userId);
        
        // Send an email with the new password
        $mailMsg = "Someone - hopefully you - requested that your GoiD password be changed. Here it is: ". $password;
        sendMail($email, 'Your new GoiD password', $mailMsg);
        
        $message = "A new password has been generated and emailed to you";
    }
    
    $conn->close();
}

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;line-height:26px;">
    Enter your email address. If found, your password will be changed to something random and emailed to you.
    <form action="" method="post">
      Email: <input type="text" name="email" class="longInput" value="<?php echo $email; ?>"/><br/>
      <input type="submit" name="submit" onclick="return dbclickCheck(this);" value="Submit"/>
    </form>
    <?php if (!empty($message)) { ?>
      <span class="error"><?php echo $message; ?></span>
    <?php } ?>
  </div>
</div>

<?php include 'include/footer.php'; ?>