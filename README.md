# goid
Souce and stuff for the Game of Intelligent Design

Overview
--------
GoID (or the Game of Intelligent Design) is an exploration of artificial general intelligence (AGI) concepts. 
Originally it even was a game, but its design didn't foster a community the way that it needed to. It used to be a set 
of tasks, and you would write an agent (in Javascript) to perform each. After the agent finished the task it would be 
scored on criteria intrinsic to the specific task. (The code for the original game will be added to this repo for 
historical interest.)

Now, GoID is a single task that will get more and more complex over time. The agents that people write will of course 
have to become more complex as well. You might consider this a form of agent evolution. This is where the software 
gets its name: players are the intelligent designers of the agents that, with hope and a diaper load of luck, will 
become the basis of AGI. At the very least we might expect to shed some light on how nervous systems work, how data is 
represented in brains, or any of the other countless AGI puzzles that have yet to be solved.

Core concepts
-------------
The study of artificial intelligence started in the 1950s. Over 60 years later we still don't have any of the 
technologies that people back then didn't seem to think were a big deal. Sure, computers can play chess and Jeopardy!, 
but, as one researcher pointed out, they don't know to come in from the rain. People will say that there is artificial 
intelligence at work everywhere, from driving cars to writing hotel reviews to finding me movies to watch. But these 
are all examples of "narrow intelligence", or fancy software that is very good at doing one very specific task, but is 
incapable of anything else. Humans, the only example of general intelligence in existence that we know of, are capable 
of much, much more. Except, it seems, to be able to build another general intelligence.

This, however, is not for lack of trying. Over those 60 years there have been all manner of attempts to create 
intelligence, from symbolic processing to artificial neural networks (ANNs) to fuzzy logic to predictive analytics to 
creating detailed simulations of neural physiology to the hundreds of other approaches, most of which i haven't heard 
of or understand. And it's not like the people who tried these things were not qualified or anything; we're talking 
about some very big-brained people. So what's been going wrong? Well, i don't know the answer to that question either, 
but i suggest it doesn't matter. (Ok, in some cases i know what is going wrong, but whatevs, it still doesn't matter.) 
What we need is an entirely new approach. If what we want is something like a human intelligence, we need to create in 
a way that is similar to how it was created in the first place: evolution.

Ok, yes, this has been tried too - many times - but small changes in the approach can have important consequences, and 
i believe that what i am proposing is truly different. Also, if it's clear that there is a problem with the approach, 
it can be adjusted.

The core concept is that of agent situation: the agent has to live in a simulation that is as much like our world as 
is practical. To start, i will propose a simulation that will be disappointingly simple for some, but we have to start 
somewhere, and the intention is for it to become more complex over time.

Other important concepts help define the approach. One is temporal resolution: the agent lives in real time, and 
receives sensory input at the millisecond level (somewhere around every 10 or 20 milliseconds, which nicely matches 
the frame rate of simulation viewers, and is in fact similar to the firing rate of neurons).

A more advanced concept is biologically-inspired movement. You don't "turn left", "turn right", "move one block 
forward"... You apply a force to a joint over time like a muscle would. I call this more advanced because force-based 
movement (as opposed to inverse kinematics) is maddeningly difficult to get working well. (See 
https://www.youtube.com/watch?v=K5DcK-ImFxc for an attempt to create an artificial inchworm.) This will not be in the 
original simulation, but will be eventually because it's killing me to understand it.

Details
-------
Specifically, there is an Environment, in which an Agent lives. (Later, multiple agents.) The agent's body is part of 
the environment; the part that is really the "agent" is what controls it, which we can call the Brain. At its 
simplest, the brain receives, say every 10ms, a set of values from the environment, which we'll call its Sensory 
Input. It can do with this input whatever it likes: process it, store it, ignore it... While it does this, it sets 
values in its Actuators, or outputs, which typically will apply a force to a joint that causes the agent's body to 
move at the end of the cycle. The agent body is subject to the environment just as real-world living things would be. 
If the agent's brain does take actions to do things like eat or hide, it will eventually end up dying, which is what 
we're trying to avoid.

Over time we make the environment more and more complext, which forces the brains to do the same. What we want is to 
gradually build up a primitive brain structure that, hopefully, will answer some basic questions about intelligent 
brains, such as how memories are likely stored, how brains represent and process data internally, how motor planning 
works, maybe what the basic function of a cortical column is. These are lofty goals, but even if the project ends up 
only giving good ideas to smart people, it has a good chance of getting the world that much closer to AGI than it was 
before. And that, my friends, is the point.
