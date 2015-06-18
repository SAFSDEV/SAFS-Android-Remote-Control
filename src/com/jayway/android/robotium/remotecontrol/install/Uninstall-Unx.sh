#!/bin/sh

#Uninstall-Unx.sh

. ./sharedFunctions.sh
. ./sharedVariablesUnx.sh

#This script supply an inter-active way to help user uninstall RobotiumRC.
#User can decide to uninstall or not.

#17 AUG, 2011  DharmeshPatel
#25 JUN, 2012  Carl Nagle

#==========  Prepare to uninstall RobotiumRC         =======================================
echo "Do you want to uninstall RobotiumRC [Y|N]?  (Default is Y)"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  uninstallSAFS=0
else
  uninstallSAFS=1
  echo "Is your RobotiumRC installation directory $SAFSDirectory? If NOT, Input your RobotiumRC installation directory. If YES, just type Enter."
  read tmp

  if ( test ! -z $tmp ) && ( test -d $tmp )
  then
      SAFSDirectory=$tmp
  fi
  #echo "Your RobotiumRC installation directory: $SAFSDirectory"
fi
#========================================================================================

#===========  Ask user if he wants to see installation details   ========
if ( test $uninstallSAFS = 1 )
then
  echo "Do you want to see details during Uninstallation [Y|N]?  (Default is Y)"
  read tmp
  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    VERBOSE=0
  fi

#===========  Create the uninstallation command  ==================================
  cmdline="java -jar robotiumrcinstall.jar -removerobotiumrc $SAFSDirectory"

  if ( test $VERBOSE = 1 )
  then
    cmdline="$cmdline -v"
  fi

  echo "Executing: $cmdline"

#==========  run uninstallation command  ==================================
  $cmdline
#========================================================================
fi
