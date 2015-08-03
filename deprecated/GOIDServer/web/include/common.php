<?php
// no direct access
defined('_topLevelPage') or die('Restricted access');
require_once 'config.php';

//
/// Definitions
//
$decode_taskLevels = array(1 => "easy", 2 => "some effort", 3 => "tricky", 4 => "hard", 5 => "fired");
$cookieName = "goidId";


//
/// Common code
//
// If a cookie is set, get the user information from the database.
$userData = getUserData(getUserId());


//
/// Common functions
//
function generateToken($length) {
    $chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    $result = "";
    for ($i=0; $i<$length; $i++) {
        $rand = rand(0, strlen($chars) - 1);
        $result .= substr($chars, $rand, 1);
    }
    return $result;
}

function getTaskInfo($conn, $taskId, $userId) {
    $sql = "select t.name, t.className, t.level, t.authorName, t.authorLink, ut.script
            from tasks t
              left join userTasks ut on t.id = ut.taskId and ut.userId=?
            where t.id=?";
    return queryRow($conn, $sql, 'ss', $userId, $taskId);
}

function sendMail($to, $subject, $body) {
    global $smtp_lib;
    
    if ($smtp_lib == "Swift") {
        global $smtp_from, $smtp_host, $smtp_port, $smtp_auth, $smtp_user, $smtp_pass;
        
        //require_once '../swift/swift_required.php';
        require_once 'swift/swift_required.php';
        
        $message = Swift_Message::newInstance();
        $message->setTo($to);
        $message->setSubject($subject);
        $message->setFrom($smtp_from);
        $message->setBody($body, "text/html");
        $message->setBody($body, "text/plain");
        
        $transport = Swift_SmtpTransport::newInstance($smtp_host, $smtp_port);
        if ($smtp_auth) {
            $transport->setUsername($smtp_user);
            $transport->setPassword($smtp_pass);
        }
        
        $mailer = Swift_Mailer::newInstance($transport);
        $result = $mailer->send($message, $failures);
        
        if (!result) {
            echo("<p>" . $failures . "</p>");
            return false;
        }
    }
    else if ($smtp_lib == "Mail") {
        global $smtp_from, $smtp_host, $smtp_port, $smtp_auth, $smtp_user, $smtp_pass;
    
        require_once 'System.php';
        require_once "Mail.php";
    
        $headers = array('From' => $smtp_from, 'To' => $to, 'Subject' => $subject, 'Content-type' => 'text/html');
        $smtp = Mail::factory('smtp', array('host' => $smtp_host, 'port' => $smtp_port, 'auth' => $smtp_auth,
                'username' => $smtp_user, 'password' => $smtp_pass));
        $mail = $smtp->send($to, $headers, $body);
        
        if (PEAR::isError($mail)) {
            echo("<p>" . $mail->getMessage() . "</p>");
            return false;
        }
    }
    else {
        $headers = "From: " . $smtp_from . "\r\nContent-type: text/html";
        mail($to, $subject, $body, $headers);
    }
    return true;
}


//
/// Database functions
//
function createConn() {
    global $db_host, $db_user, $db_pass, $db_schema, $db_port;
    
    $conn = new mysqli($db_host, $db_user, $db_pass, $db_schema, $db_port);
    
    if (mysqli_connect_errno()) {
        printf("Connect failed: %s\n", mysqli_connect_error());
        exit();
    }
    
    return $conn;
}

function queryValue(/*connection*/$conn, /*the statement*/$sql, /*string of parameters*/$paramTypes = null /*parameters*/) {
    $args = func_get_args();
    $result = call_user_func_array("query", $args);
    if (count($result) == 0)
        return null;
    return current($result[0]);
}

function queryRow(/*connection*/$conn, /*the statement*/$sql, /*string of parameters*/$paramTypes = null /*parameters*/) {
    $args = func_get_args();
    $result = call_user_func_array("query", $args);
    return $result[0];
}

function query(/*connection*/$conn, /*the statement*/$sql, /*string of parameters*/$paramTypes = null /*parameters*/) {
    $newConn = false;
    if ($conn === null) {
        $conn = createConn();
        $newConn = true;
    }
    
    $result = array();
    if ($stmt = $conn->prepare($sql)) {
        if (strlen($paramTypes) > 0) {
            $args = func_get_args();
            call_user_func_array(array($stmt, "bind_param"), array_splice($args, 2));
        }
        $stmt->execute();
        if (!empty($stmt->error))
            die("Insert failed: ". $stmt->error);
        
        $meta = $stmt->result_metadata();
        while ($field = $meta->fetch_field())
            $params[] = &$row[$field->name];
        
        call_user_func_array(array($stmt, "bind_result"), $params);
        
        while ($stmt->fetch()) {
            foreach ($row as $key => $val)
                $c[$key] = $val;
            $result[] = $c;
        }
        $stmt->close();
    }
    else
        die("What happened? ". $conn->error);
    
    if ($newConn)
        $conn->close();
        
    return $result;
}

function executeStatement(/*connection*/$conn, /*the statement*/$sql, /*string of parameters*/$paramTypes = null /*parameters*/) {
    $newConn = false;
    if ($conn === null) {
        $conn = createConn();
        $newConn = true;
    }
    
    if ($stmt = $conn->prepare($sql)) {
        if (strlen($paramTypes) > 0) {
            $args = func_get_args();
            call_user_func_array(array($stmt, "bind_param"), array_splice($args, 2));
        }
        $stmt->execute();
        
        $rows = $stmt->affected_rows;
        if (!empty($stmt->error))
            die("Execute failed: ". $stmt->error);
        $stmt->close();
    }
    else
        die("What happened? ". $conn->error);
    
    if ($newConn)
        $conn->close();
    
    return $rows;
}


//
/// User management functions
//
function getUserId() {
    global $cookieName;
    return $_COOKIE[$cookieName];
}

function setUserCookie($userId) {
    global $cookieName;
    setcookie($cookieName, $userId, time() + 60*60*24*365*10, "/"); // 10 years or so
}

function getUserData(/* string */$userId, $conn = null) {
    if (empty($userId))
       return null;
    
    $sql = "select id, username, email, password, verificationToken, emailVerified, splitLocation, outputSplitLocation, 
              frameInfo, notifyOnNewTask, notifyOnBeatenTopRank, goidRank
            from users where id=?";
    return queryRow($conn, $sql, "s", $userId);
}

function sendVerificationEmail($userId, $verificationToken, $email) {
    $url = "http://". $_SERVER['HTTP_HOST'] ."/verify?g=". $userId ."&t=". $verificationToken;
    $mailMsg = 'Click this link to complete your GoiD registration<br/><br/>';
    $mailMsg .= '<a href="'. $url .'">'. $url .'</a>';
    //mail($email, 'Verify your GoiD registration', $mailMsg, $headers);
    sendMail($email, 'Verify your GoiD registration', $mailMsg);
}

function generatePasswordSalt() {
    return substr(str_pad(dechex(mt_rand()), 8, '0', STR_PAD_LEFT), -8);
}

function hashPassword($salt, $password) {
    return $salt . hash('whirlpool', $salt . $password);
}

function passwordMatches($password, $hash) {
    $salt = substr($hash, 0, 8);
    return $hash == hashPassword($salt, $password);
}

function validatePassword($password) {
    if (empty($password))
       return "Password is required";
    if (strlen($password) < 4)
       return "Password cannot be less than 4 characters";
    if (strlen($password) > 20)
       return "Password cannot be more than 20 characters";
    return null;
}


//
/// Ranking calculations
//
function updateTaskRankings($conn, $taskId) {
    $querySql = "select ut.userId, ut.score, ut.rank, u.email
                 from userTasks ut
                   join users u on ut.userId = u.id and u.notifyOnBeatenTopRank
                 where taskId=?
                   and completed
                 order by score desc";    
    $data = query($conn, $querySql, "s", $taskId);
    
    $updateSql = "update userTasks set rank=? where userId=? and taskId=?";
    $rowIndex = 1;
    $currentRank = 0;
    $lastScore = null;
    $rankChanges = array();
    
    foreach ($data as $row) {
        if ($lastScore != $row["score"]) {
            $currentRank = $rowIndex;
            $lastScore = $row["score"];
        }
        
        if ($row["rank"] != $currentRank) {
            // The user's rank changed
            executeStatement($conn, $updateSql, "iss", $currentRank, $row["userId"], $taskId);
            
            if ($row["rank"] < $currentRank && $row["email"] != null) {
                // The user's rank went down and they wish to be notified.
                $rankChanges[] = array(
                        "userId" => $row["userId"],
                        "email" => $row["email"],
                        "oldRank" => $row["rank"],
                        "newRank" => $currentRank,
                );
            }
        }
        
        $rowIndex++;
    }
    
    return $rankChanges;
}

function updateGoidRankings($conn) {
    // Overall GoiD ranks may now have changed, so recalculate. Turn the task ranks into goid scores.
    $sql = "select ut.userId, avg(ut.rank) / count(1) as score, u.goidRank
            from userTasks ut
              join users u on u.id=ut.userId
            where completed
            group by userId
            order by score";
    $scores = query($conn, $sql);

    // Determine goid ranks from the goid scores.
    $updateSql = "update users set goidRank=? where id=?";
    $rowIndex = 1;
    $currentRank = 0;
    $lastScore = null;
    foreach ($scores as $row) {
        $score = floatval($row["score"]);
        if ($lastScore != $score) {
            $currentRank = $rowIndex;
            $lastScore = $score;
        }
        
        if ($row["goidRank"] != $currentRank)
            // The user's goid rank changed
            executeStatement($conn, $updateSql, "is", $currentRank, $row["userId"]);
            
        $rowIndex++;
    }
}
?>