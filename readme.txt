## Overview
This is the PSOToolkit for Percussion CMS.   
Routines will be added here as we find uses for them.  


## Download 
* 7.3  [PSOToolkit7.3.zip](http://cdn.percussion.com/downloads/open/psotoolkit/PSOToolkit7.3.zip) 
* 7.03 - 7.2  [PSOToolkit7.x.zip](http://cdn.percussion.com/downloads/open/psotoolkit/PSOToolkit7.x.zip)
* 6.7        [PSOToolkit6.7.zip](http://cdn.percussion.com/downloads/open/psotoolkit/PSOToolkit6.7.zip)
* Latest Nightly Snapshot - [Nightly Snapshot](http://cdn.percussion.com/downloads/open/psotoolkit/PSOToolkit7.x-SNAPSHOT.zip)


If you are using 6.5.2 or 6.7 select the branch that matches your product version when checking out the code:
* rel-cms73
* rel-cms71
* rel-cms67
* svn/652

**NOTE:** Please remove prior versions of the PSO Toolkit from your Percussion CMS installation by removing prior versions of the jar from 
the `/PercussionCMS/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/lib` directory.  

The PSCToolkit5.jar may be left in place if your installation uses it. 



## Installation  
To deploy the toolkit, you must first build the project from source, or download a packaged distribution.
Unzip the distribution into a new directory.

To install on Windows run the following command from the unzipped directory.  Replacing c:\PercussionCMS with the path to your CM System installation.
>Install.bat c:\PercussionCMS

To install on Linux or Solaris systems, run the following command from the unzipped directory, replacing ~/PercussionCMS with the path to your CM System installation. 
>chmod +x install.sh   
>dos2unix install.sh #temporary workaround for packaging bug.   
>sh install.sh ~/PercussionCMS   

Restart the Percussion CMS instance.


## Manual Install
To manually install, you must have the Java 1.6 JDK with a JAVA_HOME environment variable,
and Apache Ant installed with an ANT_HOME environment variable set. 

The PERCUSSION_HOME environment variable must point at your Percussion CMS installation directory.  

For Example:

To use the patch installer to install on Linux, add these lines to your .profile  

> export PERCUSSION_HOME=$HOME/PercussionCMS  ##or where ever it is installed   
> export JAVA_HOME=$PERCUSSION_HOME/JRE/   
> export ANT_HOME=$PERCUSSION_HOME/Patch/InstallToolkit/   

you can then run Ant: 

> $ANT_HOME/bin/ant -f deploy.xml 

## Documentation
This version now installs the JavaDoc for the toolkit into the server's "Docs"
directory. It can be accessed from the server at: 

http://<server>:<port>/Rhythmyx/Docs/PercussionCMS/PSOToolkit/index.html 

## Building from Source

> git clone https://github.com/percussion/PSOToolkit.git

### Configure Ivy
The Toolkit uses [Apache Ivy](http://ant.apache.org/ivy/) for dependency management.  In addition to requiring that JAVA_HOME, ANT_HOME, and PERCUSSION_HOME environment variables are configured, the Ivy dependencies also need configured in your Ant profile.  

Download Apache Ivy with dependencies and copy the Ivy jar from the Ivy distribution AND the jars in the lib folder of the Ivy distribution to:

> $HOME/.ant/lib

### Building
The build script provides several targets.  To build the Toolkit distribution, use the "dist" target from the directory that you cloned the repository to:

> ant ivy-retrieve, dist 

