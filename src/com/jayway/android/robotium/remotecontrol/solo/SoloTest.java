/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package com.jayway.android.robotium.remotecontrol.solo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.android.auto.lib.DUtilities;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

/**
 * This is used to test Remote Solo Implementation.<br>
 * The tested Activity is "com.android.example.spinner.SpinnerActivity"<br>
 * If testing with another Activity, we suggest to subclass this program.<br>
 * <p>
 * 
 * <b>Prerequisite:</b>
 * <pre>
 * 1. Set the android sdk dir (use one of 2 ways):
 * 
 *    1.1 set environment ANDROID_HOME
 *    1.2 launch application with JVM property as: -Dandroid-home="D:\\your\\android-sdk-dir"
 *    
 * 2. (Optional)Set the ant sdk dir if you need rebuild the test-runner-apk (use one of 2 ways):
 * 
 *    2.1 set environment ANT_HOME
 *    2.2 launch application with JVM property as: -Dant-home="D:\\your\\ant-sdk-dir"
 *       
 * 3. Command-line args to set the APK of AUT, SAFSMessenger and TestRunner to be installed:
 *    
 *    aut=D:\\Eclipse\\workspace\\ApiDemos\\bin\\ApiDemos.apk  (sets installAUT=true)
 *    messenger=d:\\buildFilePath\\SAFSTCPMessenger-debug.apk  (sets installMessenger=true)
 *    runner=d:\\buildFilePath\\RobotiumTestRunner-debug.apk   (sets installRunner=true)
 *      (or runnersource=d:\\testRunnerSourcePath like below:)
 *    runnersource=d:\\Eclipse\\workspace\\android\\SAFSEngine (sets rebuildRunner=true) 
 *    
 *    OR, to bypass individual installs and use what is already installed:
 *    
 *    -noaut
 *    -nomessenger
 *    -norunner
 *    
 *    
 * 4. Command-line arg to set the instrument:
 * 
 *    instrument=com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner
 * 
 * 5. Command-line arg to set the avd name to launch, or the device/emulator serial number to look for:
 *    
 *    avd="avdName" or avd="devOrEmuSerial"
 *    
 * 6. Command-line arg to set the persistence of launched emulator:
 *    
 *    persistavd="True" or persistavd="False" (default=false)
 *    
 * 7. Command-line arg to set to resign AUT automatically:
 *    
 *    resignjar=C:\\safs\\lib\\re-sign.jar
 *    
 * 8. Command-line arg to set to remove installed APKs automatically after test finish:
 *    
 *    -removeinstalledapk
 * </pre>
 *   
 * <p><pre>  
 * Run as:
 * 
 *   java com.jayway.android.robotium.remotecontrol.solo.SoloTest aut=d:\\buildFilePath\\ApiDemos.apk messenger=d:\\buildFilePath\\SAFSTCPMessenger-debug.apk runner=d:\\buildFilePath\\RobotiumTestRunner-debug.apk instrument=com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner
 *   
 *   or, rebuild testrunner and use it
 *   java -Dant-home="C:\\pathTo\\ant-sdk" com.jayway.android.robotium.remotecontrol.solo.SoloTest aut=d:\\buildFilePath\\ApiDemos.apk messenger=d:\\buildFilePath\\SAFSTCPMessenger-debug.apk runnersource=d:\\testRunnerSourcePath\\ instrument=com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner
 *   
 *   or, assuming everything is already installed:
 *   
 *   java -Dandroid-home="C:\\pathTo\\android-sdk" com.jayway.android.robotium.remotecontrol.solo.SoloTest -noaut -nomessenger -norunner instrument=com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner
 *   
 *   
 * </pre>
 * @author Lei Wang, SAS Institute, Inc.
 * @since
 * <br>(LeiWang)	Mar 09, 2012	Add a field robotiumUtils.
 * <br>(LeiWang)	Jun 21, 2012	Add a field robotiumTimeout.
 * <br>(LeiWang)	AUG 16, 2013	Add option to permit remove installed apks automatically after test finish.
 */
public class SoloTest{
	public Solo solo = null;
	public RobotiumUtils robotiumUtils = null;
	public Timeout robotiumTimeout = null;
	
	private boolean enableProtocolDebug = true;
	private boolean enableRunnerDebug = true;
	
	private boolean installAUT = true;
	private boolean installMessenger = true;
	private boolean installRunner = true;
	
	/**
	 * Rebuilding testrunner will depend on {@link #autApk} and {@link #testRunnerSourceDir}
	 * 
	 */
	private boolean rebuildRunner = false;
	
	/**
	 * resignJar is the path to JAR file used to resign<br>
	 * it will be modified by the value pass in as 'resignjar=' argument<br>
	 */
	private String resignJar = null;
	
	/**
	 * autApk is the path to AUT's apk<br>
	 * it is set to {@link #DEFAULT_AUT_APK} by default<br>
	 * it will be modified by the value pass in as 'aut=' argument<br>
	 */
	private String autApk = DEFAULT_AUT_APK;
	/**
	 * messengerApk is the path to messenger's apk<br>
	 * it is set to {@link #DEFAULT_MESSENGER_APK} by default<br>
	 * it will be modified by the value pass in as 'messenger=' argument<br>
	 */
	private String messengerApk = DEFAULT_MESSENGER_APK;
	/**
	 * messengerApk is the path to testrunner's apk<br>
	 * it is set to {@link #DEFAULT_TESTRUNNER_APK} by default<br>
	 * it will be modified by the value pass in as 'runner=' argument<br>
	 */
	private String testRunnerApk = DEFAULT_TESTRUNNER_APK;
	
	/**
	 * testRunnerSourceDir is the path where the testrunner's source locates<br>
	 * it is set to {@link #DEFAULT_TESTRUNNER_SOURCE_DIR} by default<br>
	 * it will be modified by the value pass in as 'runnersource=' argument<br>
	 */
	private String testRunnerSourceDir = DEFAULT_TESTRUNNER_SOURCE_DIR;
	
	/**
	 * messengerApk is the path to messenger's apk<br>
	 * it is set to {@link #DEFAULT_INSTRUMENT_ARG} by default<br>
	 * it will be modified by the value pass in as 'instrument=' argument<br>
	 */
	private String instrumentArg = DEFAULT_INSTRUMENT_ARG;
	
	/**
	 * avdSerialNo is device or emulator's serial number, it is "" by default<br>
	 * it will be modified by the value pass in as 'avd=' argument<br>
	 * 
	 * If there is no device/emulator is attached/launched, we will try to<br>
	 * launch the emulator with this serial number.<br>
	 * If there is one device/emulator is attached/launched, we will ignore<br>
	 * this serial number.<br>
	 * If there are multiple devices/emulators are attached/launched, we<br>
	 * use this serial number to locate the device/emulator.<br>
	 *  
	 */
	protected String avdSerialNo = "";

	/**
	 * If this is true, we will keep the launched emulator running even after the<br>
	 * test has finished.<br>
	 * The default value is false.<br>
	 * it will be modified by the value pass in as 'persistavd=' argument<br>
	 * 
	 */
	protected boolean persistEmulators = false;

	protected boolean weLaunchedEmulator = false;
	
	/** flag defaults to true to unlock the screen of any emulator we wish to connect to. */
	protected boolean unlockEmulatorScreen = true;
	
	/**
	 * This field will store the activity UID of you launch AUT.<br>
	 * It will be set in method {@link #initialize()}<br>
	 * 
	 * It will be used in method {@link #goBackToViewUID(String)} to prevent infinite loop.<br>
	 */
	protected String mainActivityUID = null;

	/**
	 * True to enable LogsInterface debug logging, false otherwise.
	 * @see #isDebugEnabled()
	 * @see #enableDebug(boolean)
	 */
	protected boolean debugEnabled = false;	
	
	protected LogsInterface log = null;
	
	/** true if we received the 'aut' command-line argument, or setAUTApk() was called. */
	public boolean argAUTpassed = false;
	/** true if we received the 'messenger' command-line argument, or setMessengerApk() was called. */
	public boolean argMESSENGERpassed = false;
	/** true if we received the 'runner' command-line argument, or setTestRunnerApk() was called. */
	public boolean argRUNNERpassed = false;
	/** true if we received the 'instrument' command-line argument, or setInstrumentArg() was called. */
	public boolean argINSTRUMENTpassed = false;
	/** true if we received the 'resignjar' command-line argument, or setResignJar() was called. */
	public boolean argRESIGNJARpassed = false;
	/** true if we received the 'runnersource' command-line argument, or setTestRunnerSourceDir() was called. */
	public boolean argRUNNERSOURCEpassed = false;
	/**
	 * default is false. 
	 * true if we received the '-removeinstalledapk' command-line argument, or setRemoveinstalledapk(true) was called. 
	 * If this is true, all installed apk including 'aut', 'runner' and 'messenger' will be removed.*/
	public boolean removeinstalledapk = false;
	
	/** 
	 * Modify {@link #DEFAULT_AUT_APK} according to your system.<br>
	 * Modify {@link #DEFAULT_MESSENGER_APK} according to your system.<br>
	 * Modify {@link #DEFAULT_TESTRUNNER_APK} according to your system.<br>
	 * Modify {@link #DEFAULT_TESTRUNNER_SOURCE_DIR} according to your system.<br>
	 * Modify {@link #DEFAULT_INSTRUMENT_ARG} according to your system.<br>
	 */
	public static final String DEFAULT_AUT_APK = "D:\\Eclipse\\workspace\\ApiDemos\\bin\\ApiDemos.apk";
	public static final String DEFAULT_MESSENGER_APK = "D:\\Eclipse\\workspace\\android\\SAFSMessenger\\bin\\SAFSTCPMessenger-debug.apk";
	public static final String DEFAULT_TESTRUNNER_APK = "D:\\Eclipse\\workspace\\android\\SAFSEngine\\bin\\RobotiumTestRunner-debug.apk";
	public static final String DEFAULT_TESTRUNNER_SOURCE_DIR = "D:\\Eclipse\\workspace\\android\\SAFSEngine";
	public static final String DEFAULT_INSTRUMENT_ARG = "com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner";
	
	public static final String ARG_KEY_RESIGN_JAR = "resignjar";
	public static final String ARG_KEY_AUT_APK = "aut";
	public static final String ARG_KEY_MESSENGER_APK = "messenger";
	public static final String ARG_KEY_TESTRUNNER_APK = "runner";
	public static final String ARG_KEY_TESTRUNNER_SOURCE = "runnersource";
	public static final String ARG_KEY_INSTRUMENT_ARG = "instrument";
	public static final String ARG_KEY_AVD 			  = "avd";
	public static final String ARG_KEY_PERSIST_AVD	  = "persistavd";

	public static final String ARG_KEY_NO_MESSENGER = "-nomessenger";
	public static final String ARG_KEY_NO_RUNNER = "-norunner";
	public static final String ARG_KEY_NO_AUT = "-noaut";
	
	public static final String ARG_KEY_REMOVE_INSTALLED_APK = "-removeinstalledapk";
	
	public SoloTest(){
		solo = new Solo();
		//By default, we disable the debug message of Protocol/Runner so that the console
		//show only the test log message.
		setProtocolDebug(false);
		setRunnerDebug(false);
	}
	
	public SoloTest(String messengerApk, String testRunnerApk, String instrumentArg){
		this();
		this.messengerApk = messengerApk;
		this.testRunnerApk = testRunnerApk;
		this.instrumentArg = instrumentArg;
	}
	
	/**
	 * @param args	Array of Strings: "aut=xxx", "messenger=xxx", "runner=xxx", "runnersource=xxx", "instrument=xxx", "resignjar=xxx"
	 */
	public SoloTest(String[] args){
		this();
		//Get 'apk path' and 'instrument' from program's parameters
		//and set them to soloTest object.
		String temp = "";
		String[] tempArray = null;
		for(int i=0;i<args.length;i++){
			temp=args[i];
			tempArray = temp.split("=");
			if(tempArray!=null && tempArray.length==2){
				//use temp to contain the parameter key
				temp = tempArray[0].trim();
				if(ARG_KEY_AUT_APK.equalsIgnoreCase(temp)){
					setAUTApk(tempArray[1]); 
				}else if(ARG_KEY_RESIGN_JAR.equalsIgnoreCase(temp)){
					setResignJar(tempArray[1]); 
				}else if(ARG_KEY_MESSENGER_APK.equalsIgnoreCase(temp)){
					setMessengerApk(tempArray[1]); 
				}else if(ARG_KEY_TESTRUNNER_APK.equalsIgnoreCase(temp)){
					setTestRunnerApk(tempArray[1]);
				}else if(ARG_KEY_INSTRUMENT_ARG.equalsIgnoreCase(temp)){
					setInstrumentArg(tempArray[1]); 						
				}else if(ARG_KEY_TESTRUNNER_SOURCE.equalsIgnoreCase(temp)){
					setTestRunnerSourceDir(tempArray[1]);
				}else if(ARG_KEY_AVD.equalsIgnoreCase(temp)){
					avdSerialNo = tempArray[1];
				}else if(ARG_KEY_PERSIST_AVD.equalsIgnoreCase(temp)){
					try{ persistEmulators = Boolean.parseBoolean(tempArray[1]);} catch(Exception e){}
				}
			}else{
				if(tempArray.length == 1){
					if(ARG_KEY_NO_AUT.equalsIgnoreCase(temp)){
						installAUT = false; 
					}else if(ARG_KEY_NO_MESSENGER.equalsIgnoreCase(temp)){
						installMessenger = false; 
					}else if(ARG_KEY_NO_RUNNER.equalsIgnoreCase(temp)){
						installRunner = false;
					}else if(ARG_KEY_REMOVE_INSTALLED_APK.equalsIgnoreCase(temp)){
						removeinstalledapk = true;
					}
				}
			}
		}
	}
	
	public String getTestRunnerSourceDir() {
		return testRunnerSourceDir;
	}

	public void setTestRunnerSourceDir(String testRunnerSourceDir) {
		this.testRunnerSourceDir = testRunnerSourceDir;
		if(testRunnerSourceDir!=null) {
			this.argRUNNERSOURCEpassed = true;
			this.setRebuildRunner(true);
		}
	}

	public boolean isRemoveinstalledapk() {
		return removeinstalledapk;
	}

	public void setRemoveinstalledapk(boolean removeinstalledapk) {
		this.removeinstalledapk = removeinstalledapk;
	}

	public String getResignJar() {
		return resignJar;
	}

	public void setResignJar(String resignJar) {
		this.resignJar = resignJar;
		if(resignJar!=null) this.argRESIGNJARpassed = true;
	}

	public String getAUTApk() {
		return autApk;
	}

	public void setAUTApk(String autApk) {
		this.autApk = autApk;
		if(autApk!=null) this.argAUTpassed = true;
	}

	public String getMessengerApk() {
		return messengerApk;
	}

	public void setMessengerApk(String messengerApk) {
		this.messengerApk = messengerApk;
		if(messengerApk!=null) this.argMESSENGERpassed = true;
	}

	public String getTestRunnerApk() {
		return testRunnerApk;
	}

	public void setTestRunnerApk(String testRunnerApk) {
		this.testRunnerApk = testRunnerApk;
		if(testRunnerApk!=null) this.argRUNNERpassed = true;
	}

	public String getInstrumentArg() {
		return instrumentArg;
	}

	public void setInstrumentArg(String instrumentArg) {
		this.instrumentArg = instrumentArg;
		if(instrumentArg!=null) this.argINSTRUMENTpassed = true;
	}
	
	/**
	 * Set the LogsInterface, or replace it with null.
	 */
	public void setLogsInterface(LogsInterface ilog){
		log = ilog;
	}
	
	/**
	 * Subclasses can override to initialize their own LogsInterface.
	 * @return LogsInterface or null if not set
	 * @see #prepareLogsInterface()
	 */
	public LogsInterface getLogsInterface(){
		return log;
	}
	
	/**
	 * Turn on or off the protocol's debug message
	 * @param enable
	 */
	public void setProtocolDebug(boolean enableProtocolDebug){
		this.enableProtocolDebug = enableProtocolDebug;
	}
	/**
	 * Turn on or off the runner's debug message
	 * @param enable
	 */
	public void setRunnerDebug(boolean enableRunnerDebug){
		this.enableRunnerDebug = enableRunnerDebug;
	}
		
	public void setInstallAUT(boolean installAUT) {
		this.installAUT = installAUT;
	}

	public void setInstallMessenger(boolean installMessenger) {
		this.installMessenger = installMessenger;
	}

	public void setInstallRunner(boolean installRunner) {
		this.installRunner = installRunner;
	}

	/**
	 * Rebuilding testrunner will depend on {@link #autApk} and {@link #testRunnerSourceDir}
	 * 
	 * @param rebuildRunner
	 */
	public void setRebuildRunner(boolean rebuildRunner) {
		this.rebuildRunner = rebuildRunner;
	}

	/**
	 * Called during preparation().
	 * Default implementation initializes a LogsImpl LogsInterface 
	 * logging to the User's Working Directory if a valid LogsInterface is not returned by getLogsInterface().
	 * @see #getLogsInterface()
	 * @see #setLogsInterface(LogsInterface)
	 * @see #preparation()
	 * @throws IOException
	 */
	protected void prepareLogsInterface() throws IOException{
		if(getLogsInterface() == null) setLogsInterface(new LogsImpl());
	}
	
	/**
	 * A template method.<br>
	 * 
	 * @see #prepareLogsInterface()
	 * @see #prepareDevice()
	 * @see #preparation()
	 * @see #initialize()
	 * @see #test()
	 * @see #terminate()
	 */
	final public void process(){
		String action = ".process()";
		if(!preparation()){
			log.fail(action, "Preparation error");
			//stop the emulator if we have launched it.
			if(!stopEmulator()){
				log.warn(action, "We fail to stop the emulator launched by us.");
			}
			return;
		}
		try{
			if(initialize()){
				log.info("Begin Test.");
				test();
				log.info("End Test.");
			}			
		}catch(Throwable e){
			log.fail(action, "Met Exception "+e.getClass().getSimpleName()+":"+e.getMessage());
		}finally{
			//Whether the 'remote engine' has been started or not
			//we will call the method terminate() to stop the local controller runner.
			if(!terminate()){
				log.warn(action, "Termination of Solo fail!");
			}			
		}		
	}
	
	/**
	 * Install the apk of SAFSTCPMessenger and RobotiumTestRunner<br>
	 * Forward the tcp port from on-computer-2411 to on-device-2410<br>
	 * 
	 * @return	True if the preparation is finished successfully.
	 * @see #prepareLogsInterface()
	 * @see #prepareDevice()
	 */
	final protected boolean preparation(){
		String action = ".preparation()";
		try{
			prepareLogsInterface();
			if(!prepareDevice()){
				throw new RuntimeException("Can't detect connected device/emulator!");
			}
			
			// 1. Install apks
			if (installAUT) {
				if(getResignJar()!=null){
					log.debug("RESIGNING " + autApk);
					DUtilities.resignAUTApk(resignJar, autApk);
				}
				log.debug("INSTALLING " + autApk);
				DUtilities.installReplaceAPK(autApk);
			} else {
				log.debug("BYPASSING INSTALLATION of target AUT...");
			}
	
			if (installMessenger) {
				log.debug("INSTALLING " + messengerApk);
				DUtilities.installReplaceAPK(messengerApk);
			} else {
				log.debug("BYPASSING INSTALLATION of SAFS Messenger...");
			}
	
			// Before installing the TestRunner apk, we may repackage it.
			if (installRunner) {
				if (rebuildRunner) {
					log.debug("REBUILDING " + testRunnerApk);
					testRunnerApk = DUtilities.rebuildTestRunnerApk(testRunnerSourceDir, autApk, testRunnerApk, instrumentArg, true);
					if (testRunnerApk==null) {
						throw new RuntimeException(action + " Fail to repackage the TestRunner apk!");
					}
				}
	
				log.debug("INSTALLING " + testRunnerApk);
				DUtilities.installReplaceAPK(testRunnerApk);
			} else {
				log.debug("BYPASSING INSTALLATION of Instrumentation Test Runner...");
			}
	
			// 2. Launch the InstrumentationTestRunner
			if(!DUtilities.launchTestInstrumentation(instrumentArg)){
				throw new RuntimeException("Fail to launch instrument '"+instrumentArg+"'");
			}
			// 3. Forward tcp port is needed? how to know the way of connection, by
			// USB? by WIFI?
			boolean portForwarding = true;
			solo.setPortForwarding(portForwarding);
				
			log.debug("Prepare for test successfully.");

		}catch(RuntimeException e){
			log.fail(action, "During preparation, met RuntimeException="+e.getMessage());
			return false;
		}catch(IOException e){
			log.fail(action, "During preparation, met IOException="+e.getMessage());
			return false;
		}		
		return true;
	}
	
	public boolean prepareDevice(){
		// see if any devices is attached
		boolean havedevice = false;
		
		List<String> devices = null;
		try{
			devices = DUtilities.getAttachedDevices();
			log.info("Detected "+ devices.size() +" device/emulators attached.");
			if(devices.size() == 0){
				DUtilities.DEFAULT_EMULATOR_AVD = avdSerialNo;
				if((DUtilities.DEFAULT_EMULATOR_AVD != null) && (DUtilities.DEFAULT_EMULATOR_AVD.length()> 0)){
					//DUtilities.killADBServer();
					//try{Thread.sleep(5000);}catch(Exception x){}
					log.info("Attempting to launch EMULATOR_AVD: "+ DUtilities.DEFAULT_EMULATOR_AVD);
					if (! DUtilities.launchEmulatorAVD(DUtilities.DEFAULT_EMULATOR_AVD)){
						String msg = "Unsuccessful launching EMULATOR_AVD: "+DUtilities.DEFAULT_EMULATOR_AVD +", or TIMEOUT was reached.";
						log.debug(msg);
						return false;							
					}else{
						weLaunchedEmulator = true;
						log.info("Emulator launch appears to be successful...");
						havedevice = true;
						if(unlockEmulatorScreen) {
							String stat = DUtilities.unlockDeviceScreen()? " ":" NOT ";
							log.info("Emulator screen was"+ stat +"successfully unlocked!");
						}						
					}
				}else{
					String msg = "No Devices found and no EMULATOR_AVD specified in configuration file.";
					log.debug(msg);
					return false;							
				}				
			}else if(devices.size() > 1){
				// if multiple device attached then user DeviceSerial to target device
				DUtilities.DEFAULT_DEVICE_SERIAL = avdSerialNo;
				if(DUtilities.DEFAULT_DEVICE_SERIAL.length() > 0){
					boolean matched = false;
					int d = 0;
					String lcserial = DUtilities.DEFAULT_DEVICE_SERIAL.toLowerCase();
					String lcdevice = null;
					for(;(d < devices.size())&&(!matched);d++){
						lcdevice = ((String)devices.get(d)).toLowerCase();
						log.info("Attempting match device '"+ lcdevice +"' with default '"+ lcserial +"'");
						matched = lcdevice.startsWith(lcserial);
					}
					// if DeviceSerial does not match one of multiple then abort
					if(matched){
						havedevice = true;
						DUtilities.USE_DEVICE_SERIAL = " -s "+ DUtilities.DEFAULT_DEVICE_SERIAL +" ";
					}else{
						String msg = "Requested Device '"+ DUtilities.DEFAULT_DEVICE_SERIAL +"' was not found.";
						log.debug(msg);
						return false;							
					}
				}else{
					// if no DeviceSerial present then use first device
					String device = null;
					String tdev = (String)devices.get(0);
					if(tdev.endsWith("device")){
						device = tdev.substring(0, tdev.length() -6).trim();
					}else if(tdev.endsWith("emulator")){// not known to be used
						device = tdev.substring(0, tdev.length() -8).trim();
					}else{
						String msg = "Unknown Device Listing Format: "+ tdev;
						log.debug(msg);
						return false;							
					}
					havedevice = true;
					DUtilities.USE_DEVICE_SERIAL = " -s "+ device +" ";						
				}
			}else{
				// if one device, we don't need to specify -s DEVICE_SERIAL
				// DUtilities.USE_DEVICE_SERIAL should already be empty ("");
				havedevice = true;
			}
			
		}catch(Exception x){
			log.debug("Aborting due to "+x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}			
		
		return havedevice;
	}
	
	/**
	 * Initialize the Solo object and launch the main application.<br>
	 * You will not modify this method in the sub-class, normally<br>
	 * 
	 * @return true if the initialization is successful.
	 */
	final protected boolean initialize(){
		boolean success = false;
		String action = ".initialize()";
		try {
			solo.initialize();
			robotiumUtils = new RobotiumUtils(solo);
			robotiumTimeout = new Timeout(solo);
			
			//We can enable/disable the debug message of Protocol or Runner
			solo.turnProtocolDebug(enableProtocolDebug);
			solo.turnRunnerDebug(enableRunnerDebug);
			
			//Start the main Activity
			success = solo.startMainLauncher();
			//Set the mainActivityUID
			mainActivityUID = solo.getCurrentActivity();
			log.debug("mainActivityUID="+mainActivityUID);
			
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ShutdownInvocationException e) {
			e.printStackTrace();
		}
		
		if(success){
			log.debug("Launch application successfully.");
		}else{
			log.fail(action, "Fail to launch application.");
		}
		return success; 
	}
	
	/**
	 * Terminate the remote engine.<br>
	 * Terminate the local controller runner.<br>
	 * Terminate the emulator if we have started it.<br>
	 * 
	 * You will not modify this method in the sub-class.<br>
	 * 
	 * @return true if the initialization is successful.
	 */
	final protected boolean terminate(){
		boolean success = false;
		String action = ".terminate()";
		
		robotiumUtils = null;
		robotiumTimeout = null;
		
		try {			
			if(!solo.shutdownRemote()){
				log.warn(action, "Fail to shutdown remote service.");
			}
			//Even if we fail to shutdown remote service, we will shutdown RemoteControlRunner
			solo.shutdown();
			
			if(removeinstalledapk) removeInstalledAPK();
			
			if(!stopEmulator()){
				log.warn(action, "We fail to stop the emulator launched by us.");
			}
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(success){
			log.debug("terminate successfully.");
		}else{
			log.warn(action, "Fail to terminate.");
		}
		return success;
	}
	
	final void removeInstalledAPK(){
		String action = ".removeInstalledAPK()";
		try{
			DUtilities.uninstallAPK(autApk, true);
		}catch(Exception e){
			log.warn(action, e.getMessage());
		}
		try{
			DUtilities.uninstallAPK(messengerApk, true);
		}catch(Exception e){
			log.warn(action, e.getMessage());
		}
		try{
			DUtilities.uninstallAPK(testRunnerApk, true);
		}catch(Exception e){
			log.warn(action, e.getMessage());
		}
	}
	
	/**
	 * Stop the emulator launched by us only if we have launched it and<br>
	 * we don't want to persist it.<br>
	 * 
	 * @return boolean, true if the emulator is stopped successfully or we don't need to stop it.
	 */
	final protected boolean stopEmulator(){
		boolean stopped = true;
		
		if(weLaunchedEmulator){
			//Then we will shutdown any emulator lauched by us.
			if(!persistEmulators) {
				log.info(" checking for launched emulators...");
				stopped = DUtilities.shutdownLaunchedEmulators(weLaunchedEmulator);		  	
			}else{
				log.info(" attempting to PERSIST any launched emulators...");
			}
		}else{
			log.info("we didn't start any emulators.");
		}
		
		return stopped;
	}
	
	/**
	 * Use solo to test.<br>
	 * You will extend this method in the sub-class, normally<br>
	 * 
	 * In the RobotiumTestRunner project, there is a file AndroidManifest.xml:<br>
	 * There is a tag <instrumentation> like following:
	 * 
	 * <instrumentation android:name="com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner"
	 *                  android:targetPackage="com.android.example.spinner"
	 *                  android:label="General-Purpose Robotium Test Runner"/>
	 *                  
	 * If you want to test another application, you should modify the property "android:targetPackage"<br>
	 * For example, android:targetPackage="your.test.application.package"<br>
	 * 
	 * And you need to override this method to do the test work.<br>
	 */
	protected void test(){
		//Begin the testing
		String action = ".test()";
		try {
			
			String activityID = solo.getCurrentActivity();
			Properties props = solo._last_remote_result;
			String activityName = props.getProperty(Message.PARAM_NAME);
			String activityClass = props.getProperty(Message.PARAM_CLASS);

			log.info("CurrentActivity   UID: "+ activityID);
			log.info("CurrentActivity Class: "+ activityClass);				
			log.info("CurrentActivity  Name: "+ activityName);
			
			//DEBUG: Verifying the Name we return is the same one used by waitForActivity
			if(solo.waitForActivity(activityName, 1000)){
				log.pass(action, "'"+activityName+"' was found in timeout period.");
			}else{
				log.warn(action, "*** '"+activityName+"' was NOT FOUND in timeout period. ***");
			}
			// NEGATIVE TEST
			if(solo.waitForActivity("BoguActivity", 1000)){
				log.warn(action, "*** BogusActivity was reported as found but is not a valid Activity! ***");
			}else{
				log.pass(action, "BogusActivity was not found and was not expected to be found.");
			}

			String layoutUID = solo.getView("android.widget.LinearLayout", 0);			
			log.info("Layout UID= "+layoutUID);
			
			String listUID = solo.getView("android.widget.ListView", 0);
			log.info("list UID= "+listUID);
			
			solo.config_setScreenshotFileType(Message.FILETYPE_JPEG);
			BufferedImage image = solo.takeScreenshot("ActivityScreenshot_JPEG");
			if(image != null) {
				log.info("Screenshot image  width:"+ image.getWidth());
				log.info("Screenshot image height:"+ image.getHeight());
				log.info("Screenshot stored at: "+ solo._last_remote_result.getProperty(Message.PARAM_NAME+"FILE"));
			}
			else log.info("Screenshot returned as NULL!");

			solo.config_setScreenshotFileType(Message.FILETYPE_PNG);
			image = solo.takeScreenshot("ActivityScreenshot_PNG");
			if(image != null) {
				log.info("Screenshot image  width:"+ image.getWidth());
				log.info("Screenshot image height:"+ image.getHeight());
				log.info("Screenshot stored at: "+ solo._last_remote_result.getProperty(Message.PARAM_NAME+"FILE"));
			}
			else log.info("Screenshot returned as NULL!");
			
			// SHUTDOWN all Activities.  Done Testing.
			if(solo.finishOpenedActivities()){
				log.info("Application finished/shutdown without error.");				
			}else{
				log.warn(action, "Application finished/shutdown with error.");
			}

		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ShutdownInvocationException e) {
			e.printStackTrace();
		} catch (RemoteSoloException e) {
			e.printStackTrace();
		} 
	}

	/** use log.debug LogsInterface directly. */
	@Deprecated
	public void debug(String message) {
		log.debug(getClass().getSimpleName()+" DEBUG: "+message);
	}
	/** use log.info LogsInterface directly. */
	@Deprecated
	public void info(String message) {
		log.info(getClass().getSimpleName()+" INFO: "+message);
	}
	/** use log.warn LogsInterface instead. */
	@Deprecated
	public void warn(String message) {
		log.warn(getClass().getSimpleName(), "WARN: "+message);
	}
	/** use log.pass LogsInterface instead. */
	@Deprecated
	public void pass(String message) {
		log.pass(getClass().getSimpleName(), "PASS: "+message);		
	}
	/** use log.fail LogsInterface instead. */
	@Deprecated
	public void fail( String message) {
		log.fail(getClass().getSimpleName(),"FAIL: "+message);
	}
	/** use log.fail LogsInterface instead. */
	@Deprecated
	public void error( String message) {
		log.fail(getClass().getSimpleName(),"ERROR: "+message);
	}
	
	/**
	 * @param viewUID
	 * @throws Exception
	 */
	protected void goBackToViewUID(String viewUID) throws Exception{
		int loopLimit = 10;
		int looptime = 0;
		
		do{
			if(solo.waitForViewUID(viewUID, 50, true)){
				log.debug("Back to view "+viewUID);
				return;
			}else{
				//solo.getCurrentActivity() will never return null? There is always an activity.
				String currentActivity = solo.getCurrentActivity();
				log.debug("Current Activity is "+currentActivity);
				if(currentActivity==null || currentActivity.equals(mainActivityUID)){
					log.debug("Exit the main activity!!!");
					break;
				}else{
					log.debug("Trying go back...");
				}
			}
			looptime++;
		}while((looptime<loopLimit) && solo.goBack());
		
		log.debug("Can not go back to view "+viewUID);
	}
	
	protected void pause(int millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void scrollToTop(){
		//scroll to the top of the list
		try {
			while(solo.scrollUp()){
				log.debug("Scrolling up......");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void scrollToBottoum(){
		//scroll to the bottom of the list
		try {
			while(solo.scrollDown()){
				log.debug("Scrolling down......");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Wrap the text with regex prefix and suffix ".*"<br>
	 * When you call some method like {@link Solo#clickOnText(String)}, the parameter<br>
	 * can be regex string to match more, you can call this method to wrap the parameter.<br>
	 * 
	 * @param text	String
	 * @return		String, .*text.*
	 */
	protected String wrapRegex(String text){
		if(text==null) return text;
		return ".*"+text+".*";
	}
	
	/**
	 * @param args	Array of String passed from command line:
	 *   messenger=xxx  
	 *   runner=xxx 
	 *   runnersource=xxx  
	 *   instrument=xxx 
	 */
	public static void main(String[] args){
		
		SoloTest soloTest = new SoloTest(args);
//		soloTest.installAUT = false;
//		soloTest.installMessenger = false;
//		soloTest.installRunner = false;
		//You can turn on the debug log to see the 'debug message' from protocol or runner
//		soloTest.setProtocolDebug(true);
//		soloTest.setRunnerDebug(true);
//		soloTest.setRebuildRunner(true);
		
		soloTest.process();
	}
}
