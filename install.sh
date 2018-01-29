#/bin/sh
#set -x

Help() {
   echo
   echo "Usage: "
   echo "   $0 \"/home/percussion/PercussionCMS/\""
   echo
}
test -d $1
echo $?
if [ $# -ne 1 ]
then
   echo
   echo "You must specify a valid Percussion CMS root directory"
   Help
   exit 1
elif [ "$1" = "-help" ]
then
   Help
   exit 2
elif [ -d $1 ]
then
   export PERCUSSION_HOME=$1
   export JAVA_HOME=$PERCUSSION_HOME/JRE/
   export ANT_HOME=$PERCUSSION_HOME/Patch/InstallToolkit/
   echo PERCUSSION_HOME=$PERCUSSION_HOME
   echo JAVA_HOME=$JAVA_HOME
   echo ANT_HOME=$ANT_HOME
   export DIR_NAME=`dirname $0`
   
   $ANT_HOME/bin/ant -f $DIR_NAME/deploy.xml
   exit 0
else
  echo "$1 is not a Directory"
  Help
  exit 3
fi
