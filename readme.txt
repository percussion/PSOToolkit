This is the PSOToolkit for Rhythmyx 6.6 Routines will be added here
as we find uses for them.  

THIS VERSION REQUIRES RHYTHMYX 6.6 OR LATER 


If you are using 6.0, 6.1  or 6.5.x, see the other branches in the PSO Code Library.  

	NOTE: The Rhythmyx 6.5 toolkit will continue to run on 6.6, but it will not install on 6.6.  
	If you have a 6.5 system with the toolkit installed, you may upgrade it without modifying the Toolkit.
	However, if you do re-install toolkit on an upgraded server, you MUST manually delete the PSOToolkit-6.5.jar
	from the /Rhythmyx/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib directory.  
	The PSCToolkit5.jar may be left in place if your installation uses it. 
	
To deploy the toolkit, unzip the distribution into a new directory.
   
    NOTE: This version now unzips into the PSOToolkit6.6 subdirectory. 

If you have the Rhythmyx Patch Toolkit installed, you can use the 
Install.bat or install.sh scripts: 

>Install.bat c:\Rhythmyx
>sh install.sh ~/Rhythmyx

Where the argument is the home directory where Rhythmyx is installed. 

Otherwise, to manually install, you must have Java 1.5 and Apache Ant properly installed. 
The RHYTHMYX_HOME environment variable must point at your Rhythmyx 6.5 installation.  



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

This version ships with a new Workflow Action dispatcher. For instructions
see the JavaDoc for IPSOWFActionService. 


