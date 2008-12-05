#/bin/sh
#set -x

Help() {
   echo
   echo "Usage: "
   echo "   $0 \"/home/percussion/Rhythmyx/\""
   echo
}
test -d $1
echo $?
if [ $# -ne 1 ]
then
   echo
   echo "You must specify a valid Rhythmyx root directory"
   Help
   exit 1
elif [ "$1" = "-help" ]
then
   Help
   exit 2
elif [ -d $1 ]
then
   export RHYTHMYX_HOME=$1
   export JAVA_HOME=$RHYTHMYX_HOME/JRE/
   export ANT_HOME=$RHYTHMYX_HOME/Patch/InstallToolkit/
   echo RHYTHMYX_HOME=$RHYTHMYX_HOME
   echo JAVA_HOME=$JAVA_HOME
   echo ANT_HOME=$ANT_HOME
   $ANT_HOME/bin/ant -f deploy.xml
   exit 0
else
  echo "$1 is not a Directory"
  Help
  exit 3
fi

