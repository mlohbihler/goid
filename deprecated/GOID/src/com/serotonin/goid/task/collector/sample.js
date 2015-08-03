if (typeof(initialized) == "undefined") {
    // Define some variables that will be used repeatedly. The 
    // first time the script is run "initialized" will be undefined,
    // so this will only be run once.
    initialized = true;
    displacementTracking = new DisplacementTracking();
    postTargetCollection = new PostTargetCollection();
    obstacleAvoidance = new ObstacleAvoidance();
    randomSearching = new RandomSearching();
}

// Remember where we are, relatively speaking.
displacementTracking.execute();

// Check if there is a target we can collect.
if (!collectTarget()) {
    // If not, check if we recently collected a target.
    if (!postTargetCollection.execute()) {
        // If not, check if we need to avoid an obstacle.
        if (!obstacleAvoidance.execute())
            // If not, search around randomly.
            randomSearching.execute();
    }
}

// This function will detect a target in the visual field and move towards it. 
function collectTarget() {
    // Find the nearest target.
    var target = null;
    for (var i = 0; i<senses.targets.length; i++) {
        if (target == null || target.getRadius() > senses.targets[i].getRadius())
            target = senses.targets[i];
    }
    
    // If there are no targets, exit.
    if (target == null)
        return false;
    
    // Gradually turn towards the target.
    actuators.turn = Utils.normalizeAngle(target.getAngle()) / 10;
    
    if (target.getRadius() > 10)
        // If the target is still a ways away, head in full speed.
        actuators.move = 1;
    else if (target.getRadius() > 5)
        // If the target is close, slow down a bit.
        actuators.move = 0.5;
    else
        // If the target is very close, slow even more. This ensures that
        // we won't miss the target if we aren't heading right at it (in
        // case the angle reading was off).
        actuators.move = 0.25;
    
    return true;
}

// This function works on the assumption that if we collected a target, chances
// are there are more nearby.
function PostTargetCollection() {
    // The maximum number of turns to make to look for other targets.
    this.maxLookAroundCounter = 10;
    // The number of turns left to make
    this.lookAroundCounter = 0;
    // The amount of turn to execute each time.
    this.turnAmount = 0;

    this.execute = function() {
        if (senses.targetCollected) {
            // This is only true the turn immediately following the collection of
            // a target. So, use this event to initialize the variables.
            this.lookAroundCounter = this.maxLookAroundCounter;
            // Keep turning in the direction that we last turned.
            this.turnAmount = senses.lastTurnAmount > 0 ? 1 : -1;
        }

        if (this.lookAroundCounter > 0) {
            // This is executed until the look around counter runs out.
            if (senses.targets.length == 0) {
                // If we still didn't find anything, keep turning.
                actuators.turn = this.turnAmount;
                actuators.move = 0.5;
                this.lookAroundCounter--;

                return true;
            }

            // If we made it this far, it means we found a target.
            if (this.lookAroundCounter <= 2)
                // We only just found the target. So, prevent such close calls in 
                // the future by increasing the maximum look around. Hey, this is
                // really a form of learning. Cool.
                this.maxLookAroundCounter += 3;

            // Cancel this behaviour.
            this.lookAroundCounter = 0;
        }

        return false;
    };
}

// Keep out of the way of walls.
function ObstacleAvoidance() {
    // Whether or not we are in escape mode.
    this.escape = false;
    // The amount to turn if we're in escape mode.
    this.escapeTurn = 0;
    
    this.execute = function() {
        if (!this.escape) {
            // We're not in excape mode.
            if (senses.obstacles.length > 0) {
                // Something is in our way. Avoid the first obstacle we see.
                var polar = senses.obstacles[0];
                var angle = Utils.normalizeAngle(polar.getAngle());
                if (polar.getRadius() < 10) {
                    // We're too close. Go into escape mode.
                    this.escape = true;
                    if (angle < 0.01)
                        this.escapeTurn = -1;
                    else
                        this.escapeTurn = 1;
                }
                else {
                    // Turn away from the obstacle.
                    if (angle > -0.01)
                        actuators.turn = -1 / polar.getRadius();
                    else
                        actuators.turn = 1 / polar.getRadius();
                }
                
                // Full steam ahead.
                actuators.move = 1;
                
                return true;
            }
        }
        else {
            // Keep turning in the same direction and move at a slower speed until 
            // we no longer see any obstacles.
            if (senses.obstacles.length == 0)
                this.escape = false;
            else {
                actuators.turn = this.escapeTurn;
                actuators.move = 0.5;
            }
            return true;
        }
        
        return false;
    };
}

// Turn randomly while moving around.
function RandomSearching() {
    // The amount of time we'll turn in this direction.
    this.countdown = 0;
    // The amount to turn.
    this.turnAmount = -1;
    
    this.execute = function() {
        if (this.countdown <= 0) {
            // Re-initialize the variables.
            this.countdown = Utils.nextInt(100) + 50;
            this.turnAmount = (Math.random() - 0.5) * 0.03 * (this.turnAmount < 0 ? 1 : -1);
        }

        actuators.turn = this.turnAmount;
        actuators.move = 1;

        this.countdown--;
    };
};

// This class can keep track of where we are relative to where we started.
function DisplacementTracking() {
    var xSum;
    var ySum;
    
    this.execute = function() {
        // Turn the last move amount and the current orientation into cartesian coordinates.
        var carte = Utils.polarCoords(senses.lastMoveAmount, senses.orientation).toCartesian();
        // Add the cartesian displacements to the sums.
        xSum += carte.getX();
        ySum += carte.getY();
    }
    
    // This is currently not used.
    this.reset = function() {
        this.xSum = 0;
        this.ySum = 0;
    }
}
