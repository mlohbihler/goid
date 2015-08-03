<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$login = $_POST['login'];
$username = $_POST['username'];
$password = $_POST['password'];

if (!empty($login)) {
    // Process the login information.
    $result = queryRow(null, "select id, password from users where username=?", 's', $username);
    
    if (empty($result["id"]) || !passwordMatches($password, $result["password"]))
        $message = "No user was found with those credentials";
    else {
        setUserCookie($result["id"]);
        header('Location:/profile');
        die();
    }
}

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <form action="" method="post">
      Username: <input type="text" name="username" value="<?php echo $username; ?>"/><br/>
      Password: <input type="password" name="password" value="<?php echo $password; ?>"/><br/>
      <input type="submit" name="login" onclick="return dbclickCheck(this);" value="Login"/>
    </form>
    <?php if (!empty($message)) { ?>
      <span class="error"><?php echo $message; ?></span>
    <?php } ?>
  </div>
  <br/>
  <a href="lostpwd">Lost your password?</a>
</div>

<?php include 'include/footer.php'; ?>