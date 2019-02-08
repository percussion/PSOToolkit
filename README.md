## Overview
This is the PSOToolkit for Percussion CMS.   

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

Note: If this error is encountered:

Error: Could not find or load main class org.apache.tools.ant.launch.Launcher

then the installer is using the system Java version as opposed to the Percussion CMS Java installation.  This can be remedied by uninstalling Java from the system and running the install.sh script once more.



## Building from Source

> git clone https://github.com/percussion/PSOToolkit.git

> mvn clean install