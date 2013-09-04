http://www.percussion.com

This is the PSOToolkit for Rhythmyx 7.0 Routines will be added here
as we find uses for them.  

THIS VERSION REQUIRES RHYTHMYX 7.0.1 OR LATER 


If you are using 6.5.2 or 6.7 see the other branches in the PSO Code Library.  

	NOTE: Please remove prior versions of the PSO Toolkit by removing prior versions of the jar from 
	the /Rhythmyx/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib directory.  

	The PSCToolkit5.jar may be left in place if your installation uses it. 
	
To deploy the toolkit, unzip the distribution into a new directory.
   
    NOTE: This version now unzips into the PSOToolkit7.0 subdirectory. 

Installation: 

>Install.bat c:\Rhythmyx
>sh install.sh ~/Rhythmyx

Where the argument is the home directory where Rhythmyx is installed. 

Otherwise, to manually install, you must have Java 1.6 and Apache Ant properly installed. 
The RHYTHMYX_HOME environment variable must point at your Rhythmyx 7.0.1 installation.  



Type the command: 

ant -f deploy.xml 

This version now installs the JavaDoc for the toolkit into the server's "Docs"
directory. It can be accessed from the server at: 

http://<server>:<port>/Rhythmyx/Docs/Rhythmyx/PSOToolkit/index.html 

To use the patch installer to install on Linux, add these lines to your .profile  

export RHYTHMYX_HOME=$HOME/Rhythmyx  ##or where ever it is installed
export JAVA_HOME=$RHYTHMYX_HOME/JRE/
export ANT_HOME=$RHYTHMYX_HOME/Patch/InstallToolkit/

you can then run Ant: 

$ANT_HOME/bin/ant -f deploy.xml 
