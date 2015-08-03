<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

// Get the verification token and user id from the request
$userId = $_GET['g'];
$token = $_GET['t'];
$message = null;

if (empty($userId) || empty($token))
    $message = "Missing verification data";
else {
    $conn = createConn();

    // Update the data in the database
    $rows = executeStatement($conn, "update users set emailVerified=true where id=? and verificationToken=?", 'ss',
            $userId, $token);
    if ($rows != 1)
        $message = "Couldn't match verification data";
    else {
        // Reload the user information.
        setUserCookie($userId);
        $userData = getUserData($userId, $conn);
    }
    
    $conn->close();
}

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <?php if (empty($message)) { ?>
      Hurray! Your email address has been verified. Welcome to the GoiD family 
      <a href="/profile"><?php echo $userData["username"]; ?></a>.
    <?php } else { ?>
      There was a problem with the information that was provided. Email address verification failed.<br/>
      <br/>
      <?php echo $message; ?> 
    <?php } ?>
  </div>
</div>

<?php include 'include/footer.php'; ?>