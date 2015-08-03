// This script uses approximations and learning mechanisms over classical kinetics.

if (typeof(initialized) == "undefined") {
    // Define some variables that will be used repeatedly. The 
    // first time the script is run "initialized" will be undefined,
    // so this will only be run once.
    initialized = true;
    experienceMoveToTarget = new ExperienceMoveToTarget();
    proprioceptiveMoveToTarget = new ProprioceptiveMoveToTarget(1);
}

experienceMoveToTarget.execute();

function ExperienceMoveToTarget() {
    // Maintain a mapping of known Cartesian coordinates to arm angles. This map gets updated as necessary.
    this.coordsToAngles = new SpatialMap(10);
    
    this.execute = function() {
        // Add current values to experience as necessary
        var wristX = senses.wristLocation.getX();
        var wristY = senses.wristLocation.getY();
        if (this.coordsToAngles.getNearest(wristX, wristY) == null)
            this.coordsToAngles.put(wristX, wristY, senses.shoulderAngle, senses.elbowAngle);
        
        // Check our accumulated experience to see if we know how to get where we want to go.
        var targetPointX = senses.targetLocation.getX();
        var targetPointY = senses.targetLocation.getY();
        var position = this.coordsToAngles.getNearest(targetPointX, targetPointY);
        if (position != null) {
            // Yay, we know the angles we need to get to the target.
            proprioceptiveMoveToTarget.execute(position.v1, position.v2);
            return;
        }
        
        // Use the default method.
        defaultMoveToTarget();
    };
};

// This class uses the given target angles to apply torques to the joints using a simple function. The speed factor
// determines how quickly the target angles should be approached. The higher the speed factor, the slower the approach.
function ProprioceptiveMoveToTarget(speedFactor) {
    this.speedFactor = speedFactor;
    
    this.execute = function(shoulderTarget, elbowTarget) {
        actuators.shoulderTorque = (shoulderTarget - senses.shoulderAngle) / this.speedFactor - senses.shoulderMomentum;
        actuators.elbowTorque = (elbowTarget - senses.elbowAngle) / this.speedFactor - senses.elbowMomentum;
        
        var dist = senses.wristToTarget.distance(0, 0);
        if (dist < 15)
            // If we're really close, guide in with the default movement.
            defaultMoveToTarget();
    }
}

// This function provides a default mechanism of moving the arm. It is inefficient and sometimes unpredictable,
// but it at least serves to help explore the movement space such that better mechanisms can be used in the future.
function defaultMoveToTarget() {
    var elbowPolar = Utils.polarCoords(senses.elbowToTarget);
    
    // Try to point the forearm toward the target by minimizing the error in its current angle.
    var forearmAngleError = Utils.minimumDifference(elbowPolar.getAngle(), senses.realForearmAngle);
    forearmAngleError = Utils.constrain(forearmAngleError, 0.3);
    actuators.elbowTorque = forearmAngleError - senses.elbowMomentum;
    
    // Move the shoulder joint so that the elbow is the forearm's distance from the target. The above code will direct
    // the forearm to point at the target, so this should be sufficient.
    var shoulderPolar = Utils.polarCoords(senses.targetLocation);
    if (senses.shoulderAngle < Utils.normalizeAngle(shoulderPolar.getAngle()))
        // The shoulder angle is on the wrong side of the target, so just increase the angle until it isn't.
        actuators.shoulderTorque = 0.1;
    else {
        // The shoulder angle is on the correct side, so adjust it until it is the correct distance from the target.
        var adj = Utils.constrain((150 - elbowPolar.getRadius()) / 150, 0.3);
        actuators.shoulderTorque = adj - senses.shoulderMomentum;
    }
}

// A class for maintaining a map from cartesian to polar coordinates.
function SpatialMap(defaultRadius) {
    this.radiusSq = defaultRadius * defaultRadius;
    this.mappings = [];
    
    this.put = function(x, y, shoulder, elbow) {
        var index = this.mappings.length;
        this.mappings[index] = { from: new Tuple(x,y), to: new Tuple(shoulder, elbow) };
    };
    
    this.getNearest = function(x, y) {
        var ds = -1;
        var nearest = null;
        var dist;
        for (var i=0; i<this.mappings.length; i++) {
            dist = this.mappings[i]["from"].distanceSq(x, y);
            if ((dist < ds || nearest == null) && dist <= this.radiusSq) {
                ds = dist;
                nearest = this.mappings[i]["to"];
            }
        }
        return nearest;
    };
}

// A simple class to store coordinates, either cartesian or polar.
function Tuple(v1, v2) {
    this.v1 = v1;
    this.v2 = v2;
    
    this.distanceSq = function(v1, v2) {
        return (this.v1 - v1) * (this.v1 - v1) + (this.v2 - v2) * (this.v2 - v2);
    }
}
