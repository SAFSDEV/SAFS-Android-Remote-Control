#!/bin/sh

#Setup-Mac.sh

. ./install/sharedFunctions.sh
. ./install/sharedVariablesMac.sh

#This script supply an inter-active way to help user install RobotiumRC.
#User can decide to install or not, and the installation directory.

#25 JUN, 2012  Carl Nagle
#28 AUG, 2013  Carl Nagle
#14 MAR, 2014  Carl Nagle

if ( test $# = 0 ) ; then
  echo " "
  echo "Usage: Setup-Mac.sh AndroidSDKPath [ApacheAntPath]"
  SDKDirectory="$ANDROID_HOME"
  if ( test -z "$SDKDirectory" )
  then
    echo " "
    echo "ANDROID_HOME is not set in this environment."
    echo "You will need to manually edit APK 'local.properties' files."
    SDKDirectory=$SDKUNKNOWN
  fi
else
  SDKDirectory=$1
  if( test ! -d $SDKDirectory )
  then
      echo " "
      echo "ANDROID_HOME is not set to a directory in this environment."
      echo "You will need to manually edit APK 'local.properties' files."
      SDKDirectory=$SDKUNKNOWN
  fi
fi

echo " "
echo "Using '${SDKDirectory}' as path to Android SDK."
echo " "
echo "If this path is incorrect, the user will need to"
echo "manually edit the APK 'local.properties' files."

jdk=$(getJavaVersion)
jdkMajor=$(getJavaMajorVersion)
jdkMinor=$(getJavaMinorVersion)

echo " "
echo "Your java version is $jdk"
echo " "
echo "Do you want to install RobotiumRC?" 
echo "[Y|N], default is Y?"
read tmp
if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
then
  installSAFS=0
else
  installSAFS=1

  RCDir="$ROBOTIUMRC_HOME"
  echo " "
  if ( test -n "$RCDir" )
  then
     if ( test -d $RCDir )
     then
         echo "Found existing ROBOTIUMRC_HOME path: '${RCDir}'"
         SAFSDirectory=$RCDir
     else
         echo "Existing ROBOTIUMRC_HOME path is NOT a directory."
     fi
  else
     echo "Did not find an existing ROBOTIUMRC_HOME in your profile."
  fi

  echo " "
  echo "Do you accept the RobotiumRC installation directory: '${SAFSDirectory}'?"
  echo "[Y|N], default is Y?"
  read tmp

  if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
  then
    echo " "
    echo "Input the directory where you want to install RobotiumRC:"
    read tmp
    if ( test ! -z $tmp )
    then
      SAFSDirectory=$(removeLastPathSepCharFromString $tmp)
    fi
  fi
  echo " "
  echo "RobotiumRC will be installed to directory: '${SAFSDirectory}'"
fi

if ( test $installSAFS = 1 ) 
then
    cmdline="java -jar robotiumrcinstall.jar -robotiumrc $SAFSDirectory"
    
    cmdline="$cmdline -testrunner $testRunner"
    
    if ( test -d $SDKDirectory )
    then
        cmdline="$cmdline -androidhome $SDKDirectory"

        if ( test $# = 2 ) 
        then
            ANTDir=$2
        fi
        if ( test -z "$ANTDir" ) || ( test ! -d $ANTDir ) 
        then
            ANTDir="$ANT_HOME"
        fi

        echo " "
        if ( test -z "$ANTDir" ) || ( test ! -d $ANTDir )
        then
            echo "Did not find ANT_HOME properly set in your profile." 
	    echo "Skipping automatic APK builds..."
        else
	    echo "Using '${ANTDir}' as path to Android SDK."
	    echo " "
	    echo "Do you want to build the SAFSTestRunner and SAFSTCPMessenger automatically?"
	    echo "[Y|N], default is Y?"
	    read tmp
	    if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
	    then
	        BUILDAPKS=0
	    fi
	    if ( test $BUILDAPKS = 1 )
	    then
	        cmdline="$cmdline -dobuild -anthome $ANTDir"
	    fi
	    echo " "
	    echo "Do you want to automatically re-sign the sample SpinnerActivity app?"
	    echo "[Y|N], default is Y?"
	    read tmp
	    if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
	    then
	        RESIGN=0
	    fi
	    if ( test $RESIGN = 1 ); then
	        cmdline="$cmdline -resignjar $SAFSDirectory/SoloRemoteControl/libs/re-sign.jar"
                cmdline="$cmdline -originalapk $SAFSDirectory/SampleSpinner/bin/SpinnerActivity.apk"
	        cmdline="$cmdline -resignedapk $SAFSDirectory/SampleSpinner/bin/SpinnerActivity-debug.apk"
	    fi
        fi
    fi

    echo " "
    echo "Do you want to see verbose details during installation?"
    echo "[Y|N], default is Y?"
    read tmp
    if ( test ! -z $tmp )  && (( test $tmp = "N" ) || ( test $tmp = "n" ))
    then
      VERBOSE=0
    fi

    if ( test $VERBOSE = 1 )
    then
      cmdline="$cmdline -v"
    fi

    echo "Executing: $cmdline"

    $cmdline

fi
