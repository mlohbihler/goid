Senses act as the input to a script, presented as an object with properties that provide the current value for the 
particular sense. The agents in different tasks have different senses. For example, to determine which direction your
agent is current facing you would use the following (the value is typically in radians):
<pre>var direction = senses.orientation;</pre>