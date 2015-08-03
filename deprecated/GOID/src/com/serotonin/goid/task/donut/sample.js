// Always move forward. Movement will not happen if an obstacle is in the way.
actuators.forwardMovement = 1;

if (senses.obstacles)
    // If there are any obstacles turn clockwise. Eventually we will be able
    // to move forward.
    actuators.turn = 0.005;
else
    // If there aren't any obstacles, turn counter-clockwise. Turn gently 
    // enough to ensure that we eventually find a wall that we can follow.
    actuators.turn = -0.005;
