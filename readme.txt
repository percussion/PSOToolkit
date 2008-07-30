This is the PSOToolkit for Rhythmyx 6.x  Routines will be added here
as we find uses for them.  

THIS VERSION REQUIRES RHYTHMYX 6.5 OR LATER 


If you are using 6.0 or 6.1  see the Rhino and 6.1 branches in the PSO Code Library.  

To deploy the toolkit, unzip the distribution into a new subdirectory. 

You must have Java 1.5 and Apache Ant properly intstalled. The RHYTHMYX_HOME
environment variable must point at your Rhythmyx 6.5 installation.  

Manually remove (and backup) any previous PSOToolkit6 JARS from the 
/Rhythmyx/AppServer/Server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib folder
PSCToolkit5.jar may be left in place if your installation uses it. 

Type the command: 

ant -f deploy.xml 

This version now installs the JavaDoc for the toolkit into the server's "Docs"
directory. It can be accessed from the server at: 

http://<server>:<port>/Rhythmyx/Docs/Rhythmyx/PSOToolkit/index.html 

If you use 6.5.1 or later and have the patch installer loaded in Linux, add
these lines to your .profile  

export RHYTHMYX_HOME=$HOME/Rhythmyx  ##or where ever it is installed
export JAVA_HOME=$RHYTHMYX_HOME/JRE/
export ANT_HOME=$RHYTHMYX_HOME/Patch/InstallToolkit/

you can then run Ant: 

$ANT_HOME/bin/ant -f deploy.xml 




******************************
To use the Workflow Dispatcher
******************************
- Install the Dispatcher by running ant -f deploy.xml
- Restart the server
- Log into the Content Explorer. 
- Go to the Workflow tab
- Select the Workflow that will use the dispatcher (note the workflow ID)
- Select the State that will contain the transition with the dispatcher
- Select the Transition to apply the dispatcher to (note the transition ID)
- Save the Transition

In the file system:
- Go to rxconfig/Workflow and open dispatcher.properties
- Create a new line for each transition that uses the dispatcher:
	Sample: 5|4:psoSampleWFAction,sys_TouchParentItems
	WFID|TRID:wfAction, wfAction