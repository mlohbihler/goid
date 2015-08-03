Actuators are how an agent influences its environment. The script can write to the "actuators" object in order to affect
changes. For example, to have your agent move forward and turn simply write the desired values to the appropriate 
actuator properties (as defined by the current task).
<pre>
actuators.move = 1;
actuators.turn = 0.1;
</pre>
Actuators will often have limits. For example, "move" values may be limited to between, say, -0.5 and 1. You may need 
to be careful about exceeding these limits; sometimes the value you specify may be counted against an agent's energy
consumption even though it ended up being truncated.