#!/bin/sh

#Overlay-Mac.sh

. ./sharedFunctions.sh
. ./sharedVariablesMac.sh

#This script supply an inter-active way to help user install RobotiumRC.
#User can decide to install or not, and the installation directory.

#12 JUL, 2013  Carl Nagle

if ( test $# = 0 ) ; then
  echo " "
  echo "Usage: Overlay-Mac.sh AndroidSDKPath"
  SDKDirectory="$ANDROID_HOME"
  echo " "
  varlen=$(getStringLength $SDKDirectory)
  if (test $? -eq 0 ); then
    if ( test $varlen = 0 ); then 
      echo "User will need to manually edit APK local.properties..."
      SDKDirectory="<path-to-android-sdk>"
    fi
  else
    echo "ANDROID_HOME not set in this environment."
    echo "User will need to manually edit APK 'local.properties' files"
    SDKDirectory="<path-to-android-sdk>"
  fi
else
  SDKDirectory=$1
fi

echo "Using '${SDKDirectory}' as path to Android SDK."
echo "If this path is incorrect, the user will need to"
echo "manually edit the APK 'local.properties' files."

jdk=$(getJavaVersion)
jdkMajor=$(getJavaMajorVersion)
jdkMinor=$(getJavaMinorVersion)

echo " "
echo "Your java version is $jdk, major is $jdkMajor, minor is $jdkMinor."
echo " "
echo "Do you want to overlay RobotiumRC [Y|N], default is Y?"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  installSAFS=0
else
  installSAFS=1
  echo "The default RobotiumRC installation directory is $SAFSDirectory, is that where it is installed? [Y|N], default is Y?"
  read tmp

  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    echo "Input the directory where RobotiumRC is installed:"
    read tmp
    if ( test ! -z $tmp )
    then
      SAFSDirectory=$(removeLastPathSepCharFromString $tmp)
    fi
  fi
  echo "RobotiumRC will be overlayed on directory: $SAFSDirectory"
fi

if ( test $installSAFS = 1 ) 
then
    echo ""
    echo "Do you want to see details during installation [Y|N], default is Y?"
    read tmp
    if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
    then
      VERBOSE=0
    fi

    cmdline="java -jar robotiumrcinstall.jar -robotiumrc $SAFSDirectory -overlay RobotiumRCOverlay.ZIP"

    if ( test $VERBOSE = 1 )
    then
      cmdline="$cmdline -v"
    fi

    echo "Executing: $cmdline"

    $cmdline
    
    while [ ! -d $SAFSDirectory ]
      do
      printf "."
    done
    
    chmod a+x $SAFSDirectory/*.sh
   ./modifyProjectFiles.sh $SAFSDirectory $SDKDirectory 
   
   cp  -f $SAFSDirectory/SoloRemoteControl/libs/robotium-remotecontrol.jar $SAFSDirectory/SoloRemoteControl/libs/robotium-remotecontrol.jar.backup
   rm  -f $SAFSDirectory/SoloRemoteControl/libs/robotium-remotecontrol.jar 
fi
