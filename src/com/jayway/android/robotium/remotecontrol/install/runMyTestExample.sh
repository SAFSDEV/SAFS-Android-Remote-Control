#!/bin/sh

#runMyTestExample.sh
#02 JUN 2014 Carl Nagle

SDKDir=$ANDROID_HOME
if ( test -z $SDKDir )
then 
    echo " "
    echo "ANDROID_HOME is not set in this environment."
    echo " "
    return 1
else
    if (!(test -d $SDKDir) )
    then
        echo " "
        echo "Specified ANDROID_HOME directory does NOT exist."
        echo " "
        return 1        
    fi
fi
echo "Using ANDROID_HOME as '${SDKDir}' "

RCDir=$ROBOTIUMRC_HOME
if ( test -z $RCDir )
then
    echo " "
    echo "ROBOTIUMRC_HOME is not set in this environment."
    echo " " 
    return 1
else
    if (!(test -d $RCDir) )
    then
        echo " "
        echo "Specified ROBOTIUMRC_HOME directory does NOT exist."
        echo " "
        return 1
   fi
fi
echo "Using ROBOTIUMRC_HOME as '${RCDir}' "

RCLIBS=$RCDir/SoloRemoteControl/libs
CLASSPATH=$RCLIBS/safs-remotecontrol.jar:$SDKDir/tools/lib/ddmlib.jar
AUT=$RCDir/SampleSpinner/bin/SpinnerActivity-debug.apk
MESSENGER=$RCDir/SAFSTCPMessenger/bin/SAFSTCPMessenger-debug.apk
RUNNER=$RCDir/SAFSTestRunner/bin/SAFSTestRunner-debug.apk
INSTRUMENT=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
AVD=SprintEvo
MYTEST=com.jayway.android.robotium.remotecontrol.MyTest

java -cp $CLASSPATH $MYTEST aut=$AUT messenger=$MESSENGER runner=$RUNNER instrument=$INSTRUMENT avd=$AVD
