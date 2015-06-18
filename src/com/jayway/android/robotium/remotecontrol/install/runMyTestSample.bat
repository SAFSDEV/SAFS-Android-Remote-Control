
set MYTEST=com.jayway.android.robotium.remotecontrol.MyTest
set AUT=%ROBOTIUMRC_HOME%\SampleSpinner\SpinnerActivity-debug.apk
set MESSENGER=%ROBOTIUMRC_HOME%\SAFSTCPMessenger\bin\SAFSTCPMessenger-debug.apk
set RUNNER=%ROBOTIUMRC_HOME%\SAFSTestRunner\bin\SAFSTestRunner-debug.apk
set INSTRUMENT=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner

set RCLIBS=%ROBOTIUMRC_HOME%\SoloRemoteControl\libs
set CLASSPATH=%RCLIBS%\safs-remotecontrol.jar;%ANDROID_HOME%\tools\lib\ddmlib.jar
set AVD=SprintEvo

java -cp "%CLASSPATH%" %MYTEST% aut=%AUT% messenger=%MESSENGER% runner=%RUNNER% instrument=%INSTRUMENT% avd=%AVD%
