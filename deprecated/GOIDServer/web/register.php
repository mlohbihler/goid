<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

$register = $_POST['register'];
$username = $_POST['username'];
$email = $_POST['email'];
$password = $_POST['password'];
$notifyOnNewTask = filter_var($_POST['notifyOnNewTask'], FILTER_VALIDATE_BOOLEAN);
$notifyOnBeatenTopRank = filter_var($_POST['notifyOnBeatenTopRank'], FILTER_VALIDATE_BOOLEAN);

if (!empty($register)) {
    $messages = array();
    
    // Ensure the user isn't already registered.
    if ($userData["email"] != null)
        $messages[] = "You are already registered<br/>";
    else {
        $conn = createConn();
        
        // Validate the username
        $username = trim($username);
        $sanitizedUsername = filter_var($username, FILTER_SANITIZE_SPECIAL_CHARS);
        if (empty($sanitizedUsername))
            $messages[] = "Username is required";
        else if (strlen($sanitizedUsername) < 3)
            $messages[] = "Username cannot be less than 3 characters";
        else if (strlen($sanitizedUsername) > 20)
            $messages[] = "Username cannot be more than 20 characters";
        else {
            $count = queryValue($conn, "select count(*) from users where username=?", 's', $sanitizedUsername);
            if ($count != 0)
                $messages[] = "Username is already registered<br/>";
        }
        
        // Validate the email.
        $sanitizedEmail = filter_var($email, FILTER_SANITIZE_EMAIL);
        if (empty($sanitizedEmail))
            $messages[] = "Email is required";
        else if (!filter_var($sanitizedEmail, FILTER_VALIDATE_EMAIL))
            $messages[] = "Invalid email address";
        else if (strlen($sanitizedEmail) > 50)
            $messages[] = "Email address cannot be more than 50 characters";
        else {
            $count = queryValue($conn, "select count(*) from users where email=?", 's', $sanitizedEmail);
            if ($count != 0)
                $messages[] = "Email address is already registered<br/>";
        }
    
    // Validate the password
    $passwordMessage = validatePassword($password);
    if (!empty($passwordMessage))
            $messages[] = $passwordMessage;
        
        if (empty($messages)) {
            // Validation went ok. Continue...
            // Generate a verification token.
            $verificationToken = generateToken(5);
            
            // Hash the password
            $hash = hashPassword(generatePasswordSalt(), $password);
            
            // The insert statement
            $sql = "insert into users (id, username, email, password, verificationToken, emailVerified, splitLocation,
                        outputSplitLocation, creationDate, remoteAddr, notifyOnNewTask, notifyOnBeatenTopRank)
                      values (?,?,?,?,?,false,750,750,?,?,?,?)";
            if ($stmt = $conn->prepare($sql)) {
                $attempts = 5;
                
                // Do multiple attempts since there is a (vanishingly small) chance that the user id we generate could
                // be a duplicate.
                while (true) {
                    $userId = generateToken(20);
                    $stmt->bind_param('sssssisii', $userId, $sanitizedUsername, $sanitizedEmail, $hash,
                            $verificationToken, time(), $_SERVER['REMOTE_ADDR'], $notifyOnNewTask,
                            $notifyOnBeatenTopRank);
                    $stmt->execute();
                    $rows = $stmt->affected_rows;
                    if (!empty($stmt->error))
                        die("Insert failed: ". $stmt->error);
                    if ($stmt->affected_rows == 1)
                        break;
                    $attempts--;
                    if ($attempts == 0) {
                        $conn->close();
                        die("All attempts to create a new user failed");
                    }
                }
                $stmt->close();
            }
            else
                die("What happened? ". $conn->error);
            
            setUserCookie($userId);
           
            // Send an email with a random token with which to verify the address
            sendVerificationEmail($userId, $verificationToken, $email);
            
            $messages[] = "You have been sent a verification email. Click the link in it to complete your registration.
                    <b>If you think you didn't get your email, check your spam folder.</b>";
            
            // Reload the user information.
            $userData = getUserData($userId, $conn);
        }
        
        $conn->close();
    }
}
else {
    // Default the checkboxes to checked.
    $notifyOnNewTask = true;
    $notifyOnBeatenTopRank = true;
}

$notifyOnNewTaskChecked = "";
$notifyOnBeatenTopRankChecked = "";
if ($notifyOnNewTask)
    $notifyOnNewTaskChecked = ' checked="checked"';
if ($notifyOnBeatenTopRank)
    $notifyOnBeatenTopRankChecked = ' checked="checked"';

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <p>Registering is wise. Benefits include the following:</p>
    <ul>
      <li>Your task scripts get automatically saved</li>
      <li>Environment settings like the locations of divider bars get automatically saved</li>
      <li>Your rank is saved when you complete a task, and you can confirm that your brilliance exceeds everyone else's</li>
      <li>You can get notified when new tasks are released</li>
      <li>You can get notified in case someone beats your top rank (the horror!)</li>
    </ul>
    <br/>
    <div style="clear:both;line-height:26px;padding-left:50px;">
      <div style="float:left">
        <form action="" method="post">
          Username*: <input type="text" name="username" value="<?php echo $username; ?>"/><br/>
          Email*: <input type="text" name="email" class="longInput" value="<?php echo $email; ?>"/><br/>
          Password*: <input type="password" name="password" value="<?php echo $password; ?>"/><br/>
          <input type="checkbox" name="notifyOnNewTask" id="notifyOnNewTask"<?php echo $notifyOnNewTaskChecked; ?> value="true"/>
                  <label for="notifyOnNewTask">Notify me when a new task is released</label><br/>
          <input type="checkbox" name="notifyOnBeatenTopRank" id="notifyOnBeatenTopRank"<?php echo $notifyOnBeatenTopRankChecked; ?> value="true"/>
                  <label for="notifyOnBeatenTopRank">Notify me when if someone beats my top rank</label><br/>
          <input type="submit" name="register" value="Register" onclick="return dbclickCheck(this);"/>
        </form>
      </div>
      <?php if (!empty($messages)) { ?>
      <div style="float:left;padding-left:10px;width:250px;">
        <ul class="error">
          <?php 
              foreach ($messages as $msg)
                  echo '<li><span class="error">'. $msg .'</span></li>';
          ?>
        </ul>
      </div>
      <?php } ?>
    </div>
  </div>
</div>

<?php include 'include/footer.php'; ?>