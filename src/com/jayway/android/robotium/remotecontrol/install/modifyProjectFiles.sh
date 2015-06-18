#!/bin/sh

#modifyProjectFiles.sh

. ./sharedFunctions.sh

#After installation of RobotiumRemoteControl files, this script will be called
#as part of that installation.
#
#This script needs to be called with two parameters: 
#
#   RobotiumRCDirectory
#   AndroidSDKDirectory
#
#This script will modify the local.properties files for the SAFSTCPMessenger and
#RobotiumTestRunner projects.

# 26 JUN, 2012  Carl Nagle

LOCAL_PROPERTIES_FILE=local.properties
SAFSEnvScript=RobotiumRCEnv.sh

if ( test $# != 2 ) ; then
  echo "Usage: modifyProjectFiles.sh RobotiumRCPath AndroidSDKPath"
else
  RCDirectory=$1
  SDKDirectory=$2
  lastChar=$(getLastCharFromString $RCDirectory)
  if ( test $? -eq 0 ); then
    if ( test $lastChar != "/" );then
      RCDirectory=$RCDirectory"/"
    fi
  fi
  
  echo "ANDROID_HOME=$SDKDirectory"
  
  varlen=$(getStringLength $SDKDirectory)
  if ( test $? -eq 1 ); then
    echo "ANDROID_HOME not available. User will need to manually edit ${LOCAL_PROPERTIES_FILES}s"
    SDKDirectory="path-to-android-sdk"
  else
    if ( test $varlen -eq 0 ); then
      echo "ANDROID_HOME invalid. User will need to manually edit ${LOCAL_PROPERTIES_FILES}s"
      SDKDirectory="path-to-android-sdk"
    fi
  fi

  MessengerRoot=$RCDirectory"SAFSTCPMessenger/"
  RunnerRoot=$RCDirectory"RobotiumTestRunner/"
  SAFSRunnerRoot=$RCDirectory"SAFSTestRunner/"
  SampleRoot=$RCDirectory"SampleSpinner/"
  RunnerLibs=$RunnerRoot"libs"
  SoloRoot=$RCDirectory"SoloRemoteControl/"

#==========  Create SAFS Setup environment script  ===============================

cat > $RCDirectory$SAFSEnvScript <<EOF

#====  Define RobotiumRC' environment   ==============================================
ROBOTIUMRC=$RCDirectory
CLASSPATH=.:\$ROBOTIUMRC/libs:\$ROBOTIUMRC/libs/safssockets.jar:\$ROBOTIUMRC/libs/safsautoandroid.jar:\$ROBOTIUMRC/libs/robotium-remotcontrol.jar:\$ROBOTIUMRC/libs/robotium-serializable.jar:\$ROBOTIUMRC/libs/re-sign.jar:\$CLASSPATH
export ROBOTIUMRC CLASSPATH

echo "RobotiumRC environment is ready."
EOF
#==================================================================================


#==============================================================
#====   Create local.properties configuration files  ==========
#==============================================================

echo "Creating $MessengerRoot$LOCAL_PROPERTIES_FILE"

#==================================================================================
cat > $MessengerRoot$LOCAL_PROPERTIES_FILE <<EOF

# location of the SDK. This is only used by Ant
sdk.dir=$SDKDirectory
safs.droid.automation.libs=$RunnerLibs

EOF
#==================================================================================

echo "Creating $RunnerRoot$LOCAL_PROPERTIES_FILE"

#==================================================================================
cat > $RunnerRoot$LOCAL_PROPERTIES_FILE <<EOF

#==================================================================================

echo "Creating $SAFSRunnerRoot$LOCAL_PROPERTIES_FILE"

#==================================================================================
cat > $SAFSRunnerRoot$LOCAL_PROPERTIES_FILE <<EOF

# location of the SDK. This is only used by Ant
sdk.dir=$SDKDirectory

EOF
#==================================================================================

  chmod a+x $RCDirectory$SAFSEnvScript
  
  echo "Modifying file and directory permissions for write access..."

  chmod -R a+w $RunnerRoot
  chmod -R a+w $SAFSRunnerRoot
  chmod -R a+w $MessengerRoot
  chmod -R a+w $SoloRoot
  chmod -R a+w $SampleRoot
  
fi
