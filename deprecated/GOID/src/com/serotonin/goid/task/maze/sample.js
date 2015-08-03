//This program picks a wall (right or left)
//and follows it forever.
//Because the maze is contiguous,
//we'll touch each goal eventually.

//Define our persistant variables once
if (typeof(init) === undefined) {
	init = true;
	var turnDirection = 0;
}

//To start, we don't know which wall to hug
if (!turnDirection) {
	actuators.forwardMovement = 1;
	if (senses.leftEye) {
		turnDirection = -1; //Follow the left wall
	} else if (senses.rightEye) {
		turnDirection = 1; //Follow the right wall
	}

} else {
	//If we're at a dead-end
	if (senses.probe < 10) {
		actuators.turn = -0.5*turnDirection;
		actuators.forwardMovement = 0.2;

	//If we can't see the wall we're following (like if we need to turrn the corner)
	} else if ((turnDirection == -1 && !senses.leftEye) || (turnDirection == 1 && !senses.rightEye)) {
		actuators.turn = 0.5*turnDirection;

	//If we're clear to go
	} else if (!senses.blocked) {
		actuators.forwardMovement = 1;

	//If we're jammed
	} else {
		actuators.turn = -0.3*turnDirection;
	}
}