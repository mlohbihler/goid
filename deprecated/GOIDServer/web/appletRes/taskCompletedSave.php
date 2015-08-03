<?php
define( '_topLevelPage', 1 );
require_once '../include/common.php';

$userId = $_POST["userId"];
$taskId = $_POST["taskId"];
$score = $_POST["score"];
$resultDetails = $_POST["resultDetails"];
//$userId = "ygJ3UTByxFrrHqdi5wod";
//$userId = "qsOg9agZHWsgN6S9Evth";
//$taskId = "collector";
//$score = -2000;
//$resultDetails = "testing";

$time = time();
$conn = createConn();

// Find the current score if it exists.
$oldData = queryRow($conn, "select score, rank from userTasks where userId=? and taskId=?", "ss", $userId, $taskId);

$scoreChanged = false;
$newScore = $score;
if ($oldData === null) {
    // Insert the new record.
    $sql = "insert into userTasks (userId, taskId, completed, score, resultDetails, lastCompletion, lastUpdated) 
            values (?,?,true,?,?,?,?)";
    $rows = executeStatement($conn, $sql, 'ssisii', $userId, $taskId, $score, $resultDetails, $time, $time);
    if ($rows != 1)
        die("Affected rows did not equal 1: ". $rows);
    $scoreChanged = true;
}
// This works even if the old data score is null.
else if ($score > $oldData["score"]) {
    // Beat the old score, so update.
    $sql = "update userTasks set completed=true, score=?, resultDetails=?, lastCompletion=?, lastUpdated=?
            where userId=? and taskId=?";
    $rows = executeStatement($conn, $sql, 'isiiss', $score, $resultDetails, $time, $time, $userId, $taskId);
    if ($rows != 1)
        die("Affected rows did not equal 1: ". $rows);
    $scoreChanged = true;
}
else
    // The user's existing score is better.
    $newScore = $oldData["score"];

// Determine the rank.
$sql = "select count(*) from (select distinct score from userTasks where taskId=? and score>?) a";
$rank = queryValue($conn, $sql, 'si', $taskId, $newScore);
$rank++;

$ties = queryValue($conn, "select count(*) from userTasks where taskId=? and score=?", 'si', $taskId, $newScore);
$ties--;

// Reply to the client.
echo "rank=". $rank .", changed=". ($scoreChanged ? "true" : "false") .", oldScore=". $oldData["score"] .", score=".
        $score .", ties=". $ties;

// If the score changed, relative ranks may have also changed, so recalculate.
if ($scoreChanged) {
    $rankChanges = updateTaskRankings($conn, $taskId);
    
    if (count($rankChanges)) {
        // Send out notices to the interested users that they've been slapped down.
        $taskName = queryValue($conn, "select name from tasks where id=?", "s", $taskId);
        
        foreach ($rankChanges as $rankChange) {
            // We don't need to send the notice to the user in question since we already echoed as message back.
            if ($rankChange["userId"] != $userId) {
                $msg = "Your rank on the task <b>". $taskName ."</b> has changed from ". $rankChange["oldRank"];
                $msg .= " to ". $rankChange["newRank"] .".<br/><br/>";
                
                $url = "http://". $_SERVER['HTTP_HOST'] ."/play/". $taskId;
                $msg .= '<a href="'. $url .'">'. $url .'</a>';
                
                sendMail($rankChange["email"], "GoiD rank change notification", $msg);
            }
        }
    }
    
    updateGoidRankings($conn);
}
        
$conn->close();
?>