:: Install.bat
:: 
:: Sets the appropriate environment variables and runs the ANt Deploy for the PSO Toolkit
::
::  Author : MPM 09/10/2008

@ECHO OFF

IF "%1" == "" GOTO NO-DIRECTORY

ECHO.

SET RHYTHMYX_HOME=%1
SET JAVA_HOME=%RHYTHMYX_HOME%\JRE
SET ANT_HOME=%RHYTHMYX_HOME%\Patch\InstallToolkit
%ANT_HOME%\bin\ant -f deploy.xml

GOTO END

:NO-DIRECTORY
ECHO.
ECHO Please Specify Rhythmyx Root Directory
ECHO.

:END