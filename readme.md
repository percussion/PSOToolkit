== Overview
This is the PSOToolkit for Rhythmyx 7.2 

Routines will be added here as we find uses for them.  

THIS VERSION REQUIRES RHYTHMYX 7.0.1 OR LATER 


If you are using 6.5.2 or 6.7 select the branch that matches your product version when checking out the code:

* rel-cms71
* rel-cms67
* svn/652

NOTE: Please remove prior versions of the PSO Toolkit from your Rhythmyx installation by removing prior versions of the jar from 
the /Rhythmyx/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib directory.  

The PSCToolkit5.jar may be left in place if your installation uses it. 

To deploy the toolkit, you must first build the project from source, or download a packaged distribution.

=== Download  

* 7.03 - 7.2 : http//cdn.percussion.com/downloads/open/psotoolkit/PSOToolkit7.x.zip
* 6.7 - http://cdn.percussion.com/downloads/open/psotoolkit/PSOToolkit6.7.zip
   

=== Installation  

Unzip the distribution into a new directory.

>Install.bat c:\Rhythmyx
>sh install.sh ~/Rhythmyx

Where the argument is the home directory where Rhythmyx is installed. 


=== Manual Install
To manually install, you must have the Java 1.6 JDK with a JAVA_HOME environment variable,
and Apache Ant installed with an ANT_HOME environment variable set. 

The RHYTHMYX_HOME environment variable must point at your Rhythmyx installation directory.  

For Example:

To use the patch installer to install on Linux, add these lines to your .profile  

export RHYTHMYX_HOME=$HOME/Rhythmyx  ##or where ever it is installed
export JAVA_HOME=$RHYTHMYX_HOME/JRE/
export ANT_HOME=$RHYTHMYX_HOME/Patch/InstallToolkit/

you can then run Ant: 

$ANT_HOME/bin/ant -f deploy.xml 

=== Documentation
This version now installs the JavaDoc for the toolkit into the server's "Docs"
directory. It can be accessed from the server at: 

http://<server>:<port>/Rhythmyx/Docs/Rhythmyx/PSOToolkit/index.html 
