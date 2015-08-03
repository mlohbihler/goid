<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

include 'include/header.php';
?>

<div class="textContent">
  <?php include 'include/nav.php'; ?>
  
  <div style="clear:both;">
    <a href="#overview">Overview</a> |
    <a href="#details">Details</a> |
    <a href="#philosophy">Philosophy</a> |
    <a href="#taskAuthoring">Task authoring</a> |
    <a href="#about">About</a> |
    <a href="#comingSoon">Coming soon</a>
  </div>
  
  <div style="clear:both;">
    <a name="overview"></a><h1>Overview</h1>
    (Hover your mouse over <span class="def">definition</span>s.)
    <h3>How do I shoot?</h3>
    <p>
      There is (usually) no shooting. It's more like a strategy game, but not quite like that genre either. 
      You might call GoiD an 'advance strategy' game. If you like a good puzzle, you've come to the right place.
    </p>
    <p>
      It's like this... <b>You</b> don't play the game, you write a <b>script</b> that plays the game, and you sit back
      and watch your magnificent brilliance in action.
    </p>
    
    <h3>Hmm... some details please...</h3>
    <p>
      At its simplest, your <span class="def">script</span> reads information from the <span class="def">agent</span>'s 
      <span class="def">senses</span>, and then writes values to the agents' <span class="def">actuators</span>. What 
      specific senses and actuators are available depends upon the <span class="def">task</span>. Depending upon the 
      requirements of the task and the true extent of your genius your script can become arbitrarily large and complex 
      so as to include memory, conflicting desires, emotions, and such. Hey, why not just make it intelligent while 
      you're in there?
    </p>
    
    <h3>I'm still not sure i get it</h3>
    <p>
      An example may help. Take the 'donut' task. The goal is to have the agent perform a single lap around the island.
      It is able to sense the direction it is facing, whether there are any obstacles (walls) in its way, and whether 
      its last attempt to move was blocked (i.e. it hit a wall). It is able to actuate a turn, a forward movement, and 
      a backward movement. There are many ways in which the goal can be achieved, but a simple approach is to move 
      until a wall is reached, and then follow that wall around until the lap is completed. A sample (un-optimised) 
      script is provided in the task.
    </p>
    
    <a name="details"></a><h1>Details</h1>
    <h3>The interface</h3>
    <p>
      The user interface is divided into three parts: the environment viewer, the script panel, and the output panel. 
      The environment viewer allows you to see what is happening to your agent(s). You can use the mouse to pan around,
      and the magnification buttons (<img src="/images/magnifier_zoom_in.png"/> and 
      <img src="/images/magnifier_zoom_out.png"/>) to zoom in and out. Use <img src="/images/information.png"/> to 
      see a summary of your agent's current state.
    </p>
    <p>
      The script panel is where you edit your script. To get you started, each task has a sample script that is copied 
      to the script panel by pressing <img src="images/script_lightning.png"/>. Typical buttons are included to undo
      (<img src="/images/arrow_undo.png"/>), redo (<img src="images/arrow_redo.png"/>), and save 
      (<img src="/images/script_save.png"/>). <b>You must save your script to have the agent(s) use it!</b> If your 
      script saves values into its context, you can use <img src="/images/script_delete.png"/> to clear the context out.
    </p>
    <p>
      The output panel displays messages that your script writes. Just use
    </p>
    <pre>  console.out('my message');</pre>
    <p>
      Click <img src="/images/monitor_delete.png"/> to clear the output panel.
    </p>
    
    <h3>The process</h3>
    <p>
      A task is executed in discrete time steps. At each time step the agent is given its current state, and its
      script is executed allowing it to write values to its actuators. These output values are used to update the 
      agent and the <span class="def">environment</span>, which will likely result in different senses values at the 
      next time step in a feedback loop. 
    </p>
    <p>
      To execute a single time step press <img src="/images/control_end_blue.png"/>. To start a loop of execution
      press <img src="/images/control_play_blue.png"/>. To pause execution press 
      <img src="/images/control_pause_blue.png"/>. To stop execution and reset the environment back to the beginning
      press <img src="/images/control_start_blue.png"/>.
    </p>
    <p>
      While an execution loop is running, you can speed it up with the <img src="/images/lightning_add.png"/> button,
      or slow it down with <img src="/images/lightning_delete.png"/>. Note that the game runs in simulated time, not
      real time, so things like the speed of your computer will not affect your score.
    </p>
    
    <h3>The exam</h3>
    <p>
      When your script is ready, press <img src="images/bullet_go.png"/> to run it for real. In run mode you are not
      allowed to modify the script or agent context, because when your agent finishes the task your score is evaluated.
      To abort a run press <img src="/images/stop.png"/>.
    </p>
    
    <a name="philosophy"></a><h1>Philosophy</h1>
    <p class="quote">
      "As a general rule, biology tends to be conservative. It's rare that evolution 'invents' the same process several
      times." - Gero Miesenböck
    </p>
    
    <p>
      You'll find that for tasks that are similar enough you'll be copying functionality that worked well before into
      your new designs. The obvious parallel&ndash;albeit abstract&ndash;with evolution is genes. Sure, it's reaching
      but it's oddly satisfying at the same time.
    </p>
    
    <h3>What does this have to do with Intelligent Design?</h3>
    <p>
      Nothing. And everything.
    </p>
    
    <h3>Are you a Darwinist or a Creationist?</h3>
    <p>
      In the game? Yes.
    </p>
    
    <h3>Stop that</h3>
    <p>
      Obviously when you play the game you are the intelligent designer. But every time you make a modification to
      your script you evolve your agent's behaviour. There are multiple levels of evolutionary pressure going on here.
      First, you need your agent to solve the task; if it doesn't do that, you change your script and the old script
      becomes extinct. Second, when your rank isn't as high as you want it to be, you again evolve the script. Finally,
      for those tasks in which multiple users can have their scripts executing in a single competitive play environment 
      you need to adjust your script to account for the behaviour of other scripts. Neat or what?
    </p>
    
    <a name="taskAuthoring"></a><h1>Task authoring</h1>
    <p>
      If you are interested in authoring tasks for GoiD, contact the fellow mentioned below in the About section.
    </p>
    
    <a name="about"></a><h1>About</h1>
    <p>
      The Game of Intelligent Design is developed and maintained primarily by Matthew Lohbihler of Serotonin Software
      (ml at gameofid dot com). If you would like to <span class="def">contribute</span> to this project, 
      please please please please contact me.
    </p>
    
    <a name="comingSoon"></a><h1>Coming "Soon"</h1>
    <p>
      There are big plans for GoiD. Among the diaper load of task ideas there are things such as:
    </p>
    <ul>
      <li>
        Competitive play &ndash; tasks where users submit their scripts for a scheduled competitive execute, where
        multiple scripts run in a single environment at once.
      </li>
      <li>
        "Genetic" scripts &ndash; scripts that are arranged such that pieces can be swapped in and out so that agent
        behaviours can be randomly mutated.
      </li>
      <li>
        Cooperative scripts, where multiple users submit parts of a script, each of which vies for the agent's 
        "attention" based upon how well it chooses behaviours for a given context.
      </li>
      <li>
        External play APIs that would allow users to develop remotely executed behaviour code (just in case there is
        any AGI code out there that isn't written in Javascript).
      </li>
    </ul>
    <p>
      Sure, some of the above is a bit wishy-washy, but what is lacking in definition is more than made up for in 
      enthusiasm. Needless to say, two things are clear: 1) GoiD's future will be interesting, and 2) some help might 
      be needed (see About above).
    </p>
  </div>
</div>

<?php include 'include/footer.php'; ?>