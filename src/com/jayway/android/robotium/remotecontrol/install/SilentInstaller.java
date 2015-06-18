package com.jayway.android.robotium.remotecontrol.install;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import org.safs.android.auto.lib.AndroidTools;
import org.safs.android.auto.lib.AntTool;
import org.safs.android.auto.lib.Process2;
import org.safs.android.auto.lib.Console;
import org.safs.install.ProgressIndicator;
import org.safs.natives.NativeWrapper;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.text.FileUtilities;

/*************************************************************************************************************************
 * RobotiumRC installer designed to extract the contents of ZIP files into parameter-specified locations.
 * <p>
 * The app can handle new installs, and overlaying updates to existing installs, or performing a crude 
 * "uninstall" by recursively removing files and subfolders from the specified install location.
 * <p>
 * The default ZIP file sought for RobotiumRC installs is: RobotiumRCInstall.ZIP
 * <p>
 * The default ZIP file sought for RobotiumRC overlays is: RobotiumRCOverlay.ZIP
 * <p>
 * The default install location for RobotiumRC is:
 * <p><ul><pre>
 * Windows: C:\RobotiumRC<br>
 *   Linux: /usr/local/robotiumrc<br>
 *     Mac: /Library/robotiumrc
 * </pre></ul>
 * <p>
 * The app can remove and install, install and overlay, or remove and install and overly in a 
 * single invocation if all the parameters are properly specified.  If no parameters whatsoever are 
 * specified then the app will attempt an install to the default location.
 * <p>
 * @author Carl Nagle  JUN 25, 2012 Original Release<br>
 * 		   Lei Wang  JUL 02, 2013 Modify to build Runner/Messenger automatically.<br>
 * 		   Lei Wang  JUL 03, 2013 Add ProgressIndicator to show the progress.<br>
 *         Carl Nagle  JUL 11, 2013 Add -resignjar, -originalapk, and -resignedapk options to re-sign the sample apk<br>
 *         Lei Wang  JUL 25, 2013 Move some codes to org.safs.text.FileUtilities<br>
 *                              Move ProgressIndicator to package org.safs.install<br>
 *         Lei Wang  NOV 01, 2013 Add "-testrunner" option to decide what is wrote to local.properties of TCPMessenger<br>
 *                              Modify method modifyPropertiesFile(): add more comments to .properties file.
 *         Carl Nagle  JUL 24, 2014 RobotiumTestRunner is no longer built all the time.
 */
public class SilentInstaller {
	/** "RobotiumRCInstall.ZIP" */
	static final String ZIP_INSTALL_FILE    = "RobotiumRCInstall.ZIP";
	/** "RobotiumRCOverlay.ZIP" */
	static final String ZIP_OVERLAY_FILE    = "RobotiumRCOverlay.ZIP";

	/** "C:\RobotiumRC" **/
	public static final String DEFAULT_WIN_SAFS_DIR = "C:\\RobotiumRC";
	/** "/usr/local/robotiumrc" **/
	public static final String DEFAULT_UNX_SAFS_DIR = "/usr/local/robotiumrc";
	/** "/Library/robotiumrc" **/
	public static final String DEFAULT_MAC_SAFS_DIR = "/Library/robotiumrc";

	public static final String ROBOTIUM_TEST_RUNNER = "RobotiumTestRunner";
	public static final String SAFS_TEST_RUNNER 	= "SAFSTestRunner";

	/** "-removerobotiumrc" **/
	public static final String OPTION_REMOVE_SAFS  = "-removerobotiumrc";
	/** "-silent" **/
	public static final String OPTION_SILENT  = "-silent";	
	/** "-robotiumrc" **/
	public static final String OPTION_ALTSAFS = "-robotiumrc";
	/** "-androidhome" **/
	public static final String OPTION_ANDROID_HOME = "-androidhome";
	/** "-anthome" **/
	public static final String OPTION_ANT_HOME = "-anthome";
	/** "-dobuild" **/
	public static final String OPTION_BUILD_RUNNER_MESSENGER = "-dobuild";
	/** "-resignjar" **/
	public static final String OPTION_RESIGN_JAR = "-resignjar";
	/** "-originalapk" **/
	public static final String OPTION_ORIGINAL_APK = "-originalapk";
	/** "-resignedapk" **/
	public static final String OPTION_RESIGNED_APK = "-resignedapk";
	/** "-overlay" **/
	public static final String OPTION_OVERLAY = "-overlay";
	/** "-v" **/
	public static final String OPTION_VERBOSE = "-v";
	/** "-testrunner" **/
	public static final String OPTION_TESTRUNNER = "-testrunner";
	
	static boolean overlaysafs = false;
	static boolean installsafs = true;
	static boolean removesafs = false;
	static boolean verbose = false;
	static boolean dobuild = false;
	static String  installedsafsdir = DEFAULT_WIN_SAFS_DIR;
	static String  safsdir = "";
	static String  overlayfile = "";
	static String  safs_silent  = "-silent";
	static boolean installsilent = true;	
	static String  androidHome = "";
	static String  antHome = "";
	static String  resignJAR = "";
	static String  originalAPK = "";
	static String  resignedAPK = "";
	static boolean doResign = false;
	static String  testRunner = SAFS_TEST_RUNNER;
	
	static AntTool anttool = AntTool.instance();
	static AndroidTools androidtool = AndroidTools.get();
	/** progressbar is a swing panel to show the progress of installation.*/
	static ProgressIndicator progresser = new ProgressIndicator();

	static{
		System.out.println ("Installing on '"+ Console.OS_NAME+"' System");

		if(Console.isWindowsOS()){
			safsdir = DEFAULT_WIN_SAFS_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
		}else if(Console.isUnixOS()){
			safsdir = DEFAULT_UNX_SAFS_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
		}else if(Console.isMacOS()){
			safsdir = DEFAULT_MAC_SAFS_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
		}else{
			System.err.println("Operating System '"+Console.OS_NAME+"' not yet supported!");
		}
	}
	
	static void printArgs(String[] args){
		System.out.println( args.length + " arguments provided...");
		for(int i = 0; i < args.length; i++){
			System.out.println(args[i]);
		}
		System.out.println ("");
		System.out.println ("UnInstall  RobotiumRC: "+ removesafs);
		System.out.println ("Installing RobotiumRC: "+ installsafs);
		System.out.println ("Overlaying RobotiumRC: "+ overlaysafs);
		System.out.println ("RobotiumRC InstallDir: "+ safsdir);
	}

	static void parseArgs(String[] args){
		int argc = args.length;
		boolean explicit = false;
		
		for(int i = 0; i < argc; i++){
			String arg = args[i].toLowerCase();

			if (arg.equals(OPTION_SILENT)) { installsilent = true; continue; }
			
			if (arg.equals(OPTION_REMOVE_SAFS)) {
				if (i < argc -1){
					installedsafsdir = args[++i];
					installedsafsdir = installedsafsdir.trim();
				}
				removesafs = true;
				if (!explicit) installsafs = false;
				continue;
			}
			if (arg.equals(OPTION_OVERLAY)) {
				if (i < argc -1) {
					overlayfile = args[++i];
					overlaysafs = true;
					if (!explicit) installsafs = false;
				}
				continue; 
			}
			
			if (arg.equals(OPTION_ALTSAFS)) { 
				if (i < argc -1) {
					safsdir = args[++i];
					explicit = true;
					installsafs = true;
				}
				continue;
			}
			
			if (arg.equals(OPTION_ANT_HOME)) { 
				if (i < argc -1) {
					antHome = args[++i];
				}
				continue;
			}
			
			if (arg.equals(OPTION_ANDROID_HOME)) { 
				if (i < argc -1) {
					androidHome = args[++i];
				}
				continue;
			}
			
			if (arg.equals(OPTION_BUILD_RUNNER_MESSENGER)) { 
				dobuild = true;
				continue;
			}
			
			if (arg.equals(OPTION_RESIGN_JAR)) { 
				if (i < argc -1){
					resignJAR = args[++i];
					resignJAR = resignJAR.trim();
					if(originalAPK.length()> 4 && resignedAPK.length() > 4) doResign = true;
				}
				continue;
			}
			
			if (arg.equals(OPTION_ORIGINAL_APK)) { 
				if (i < argc -1){
					originalAPK = args[++i];
					originalAPK = originalAPK.trim();
					if(resignJAR.length()> 12 && resignedAPK.length() > 4) doResign = true;
				}
				continue;
			}
			
			if (arg.equals(OPTION_RESIGNED_APK)) { 
				if (i < argc -1){
					resignedAPK = args[++i];
					resignedAPK = resignedAPK.trim();
					if(resignJAR.length()> 12 && originalAPK.length() > 4) doResign = true;
				}
				continue;
			}
			
			if (arg.equals(OPTION_TESTRUNNER)) { 
				if (i < argc -1){
					testRunner = args[++i];
					testRunner = testRunner.trim();
				}
				continue;
			}

			if(arg.equals(OPTION_VERBOSE)){
				verbose = true;
				continue;
			}
		}
	}


	/**
	 * Perform RobotiumRC install.
	 * The RobotiumRC install is currently always silent (GUI-less).<br>
	 * If -altdir parameter was specified then the install will attempt to use any 
	 * provided user-specified directory for the install.
	 * <p>
     * Any user-specified directories must exist; or, we must not be denied the ability to 
     * create them and write/copy files to them.
	 **/
	static int doSAFSInstall() throws IOException, FileNotFoundException{
		int     status        = -1;
		
		progresser.setProgressMessage("RobotiumRC InstallDir: "+ safsdir);
		progresser.setProgressMessage("Start RobotiumRC Installation......");
		
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();
		
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified install path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
			if(! root.mkdirs()){
			    throw new FileNotFoundException(
			    "Specified install path could not be created.");
			}			
		}
		
		progresser.setProgressMessage("Unzip "+ZIP_INSTALL_FILE+" ......");
		FileUtilities.unzipFile(ZIP_INSTALL_FILE, root, verbose);

		if(Console.isUnixOS()||Console.isMacOS()){
			// if unix or mac chmod -R a+x *.sh
			// if unix or mac chmod -R a+x *.apk
			// if unix or mac chmod -R a+w *
			progresser.setProgressMessage("Recursively modifying write permissions for all users at "+root.getAbsolutePath());
			modUnixRecursiveFolderAllWritePermissions(root);
		}
		
		progresser.setProgress(dobuild? 20:75);
		modifyPropertiesFile(root);

		if(!overlaysafs){
			if(dobuild)	buildMessengerRunner();
			
			progresser.setProgress(doResign? 60:90);
	
			if(doResign) resignOriginalAPK();
			
			progresser.setProgress(100);
			progresser.setProgressMessage("Completed RobotiumRC Installation.");
		}else{
			progresser.setProgress(dobuild? 50:75);
			progresser.setProgressMessage("Completed initial RobotiumRC Installation.");
		}
		status = 0;
		return status;
	}
	
	static final String TCP_MESSENGER_FOLDER = "SAFSTCPMessenger";
	static final String ROBOTIUM_RUNNER_FOLDER = "RobotiumTestRunner"; // no longer delivered with RC for 5.x
	static final String SAFS_RUNNER_FOLDER = "SAFSTestRunner";
	static final String SAMPLE_SPINNER_FOLDER = "SampleSpinner";
	
	static int DEFAULTMESSENGERSDK = 10;//target=Google Inc.:Google APIs:10
	static int DEFAULTRUNNERSDK = 15;//target=Google Inc.:Google APIs:15
	
	/**
	 * This method will modify the file local.properties of SamplSpinner, SAFSTestRunner and SAFSTCPMessenger<br>
	 * It requires that android sdk has been installed.<br>
	 * It will need androidHome to modify the property file. So please call main() with parameter "-androidhome androidHome", or<br> 
	 * an environment "ANDROID_HOME"/"ANDROID_SDK" should be set to the "Android sdk home directory".<br>
	 * @param root, File, the root directory where the robotium rc has been installed.
	 * @throws FileNotFoundException
	 */
	static void modifyPropertiesFile(File root) throws FileNotFoundException{
		progresser.setProgressMessage("Modifying the local.properties for Messenger and Runner.");
		//Prepare the androidHome, it is needed to set the local.properties
		try{
			androidtool.setToolHome(androidHome);
		}catch(IllegalStateException ise){
			try{
				androidHome = androidtool.getToolHome();
			}catch(IllegalStateException e){
				System.err.println("Warning: Fail to initialize Android Tool due to '"+e.getMessage()+"'");
				return;
			}
		}		
		
		//Try to override the property 'target' of project.properties
		int messengerTargetLevel = -1;
		int runnerTargetLevel = -1;
		List<Integer> levels = androidtool.getInstalledSKDLevel();
		for(Integer level: levels){
			if(messengerTargetLevel<0 && level>=DEFAULTMESSENGERSDK ) messengerTargetLevel = level;
			if(runnerTargetLevel<0 && level>=DEFAULTRUNNERSDK ) runnerTargetLevel = level;
		}
		
		//automationLibsFolder will be wrote to local.properties of TCPMessenger
		//The first one will be used, the other one will be wrote as comment.
		String[] automationLibsFolder = new String[1];
		
		//For Runner: RobotiumTestRunner, SAFSTestRunner
		File directory = null;
        
		//SAFSTestRunner
		directory = new CaseInsensitiveFile(root, SAFS_RUNNER_FOLDER).toFile();
		if(!directory.exists() || !directory.isDirectory()){
		    throw new FileNotFoundException( SAFS_RUNNER_FOLDER+" doesn't exist or is not a directory.");
		}
		modifyPropertiesFile(directory, androidHome, runnerTargetLevel, DEFAULTRUNNERSDK, null);
		automationLibsFolder[0] = new CaseInsensitiveFile(directory, "libs").getAbsolutePath();
		
		//For Messenger:
		directory = new CaseInsensitiveFile(root, TCP_MESSENGER_FOLDER).toFile();
		if(!directory.exists() || !directory.isDirectory()){
		    throw new FileNotFoundException( TCP_MESSENGER_FOLDER+" doesn't exist or is not a directory.");
		}
		modifyPropertiesFile(directory, androidHome, messengerTargetLevel, DEFAULTMESSENGERSDK, automationLibsFolder);		

		//For SampleSpinner:
		directory = new CaseInsensitiveFile(root, SAMPLE_SPINNER_FOLDER).toFile();
		if(!directory.exists() || !directory.isDirectory()){
		    throw new FileNotFoundException( SAMPLE_SPINNER_FOLDER+" doesn't exist or is not a directory.");
		}
		modifyPropertiesFile(directory, androidHome, messengerTargetLevel, DEFAULTMESSENGERSDK, null);		
	}
	
	/**
	 * 
	 * @param directory, File, the Runner or Messenger's project folder.
	 * @param androidHome, String, the android sdk home, to be wrote to local.properties
	 * @param targetLevel, int, the real target level to be set for 'target=Google Inc.:Google APIs:?'
	 * @param defaultSdkLevel, int, {@value #DEFAULTMESSENGERSDK} for messenger; {@value #DEFAULTRUNNERSDK} for Runner.
	 * @param automationLibsFolder, String[], an array of the libs folder of the Runner,
	 *                                      the first one will be used, other will be put into properties file as comment
	 *                                      Only used when modifying Messenger's property file.
	 *                                      null when modifying Runner's property file.
	 * @throws FileNotFoundException
	 * @see {@link #modifyPropertiesFile(File)}
	 * @see {@link #addEscapeToFilepath(String)}
	 */
	static void modifyPropertiesFile(File directory,
									 String androidHome,
			                         int targetLevel,
			                         int defaultSdkLevel,
			                         String[] automationLibsFolder/*optional*/)throws FileNotFoundException{
		String localPropertiesComment = "\n#'sdk.dir' is location of the android SDK. This is only used by Ant";
		String targetComment = "\n#'target' overrides that of project.properties";//+Level
		String targetPrefix = "Google Inc.:Google APIs:";//+Level
		String runnerLibraryFolderComment = 
				"\n#'safs.droid.automation.libs' indicates the library folder of target TestRunner.\n" +
				"#During build of TCPMessenger, the safstcpmessenger.jar will be generated and put into this folder\n" +
				"#Depends on the Runner that you will use, you can choose one of the following setting";
		boolean addAutomationLibsProperty = (automationLibsFolder!=null && automationLibsFolder.length>0);
		
		File localPropertiesFile =  new CaseInsensitiveFile(directory, "local.properties").toFile();
		//sdk.dir=<android-sdk-path> will be set for both runner and messenger
		//target=Google Inc.:Google APIs:? will be set for both runner and messenger when 'targetLevel>defaultSdkLevel'
		//safs.droid.automation.libs=<Runer_libs_dir> will be set only for messenger
		if(!localPropertiesFile.exists()){
			PrintWriter wr = null;
			try {
				wr = new PrintWriter(new FileWriter(localPropertiesFile));
				wr.println(localPropertiesComment);
				wr.println("sdk.dir="+addEscapeToFilepath(androidHome));
				if(addAutomationLibsProperty){
					wr.println(runnerLibraryFolderComment);
					for(int i=0;i<automationLibsFolder.length;i++){
						wr.println((i==0? "":"#")+"safs.droid.automation.libs="+addEscapeToFilepath(automationLibsFolder[i]));
					}
				}
				wr.println(targetComment);
				wr.println("target="+targetPrefix+targetLevel);
				wr.flush();
			} catch (IOException e) {
				System.err.println("Warning: fail to write file '"+localPropertiesFile.getAbsolutePath()+"' Exception="+e.getClass()+":"+e.getMessage());
			}finally{
				if(wr!=null) wr.close();
			}
		}else{
			System.out.println(localPropertiesFile.getAbsolutePath()+" exists.");
			try {
				Properties props = new Properties();
				props.load(new FileInputStream(localPropertiesFile));
				//As Properties can only store one comment each time, if we want to
				//store one comment for one item, we have to create new Properties object
				//but we must remove the item from the loaded Properties, otherwise there will be 2 items
				props.remove("sdk.dir");
				props.remove("target");
				if(addAutomationLibsProperty) props.remove("safs.droid.automation.libs");
				FileOutputStream fos = new FileOutputStream(localPropertiesFile);

				
				props.setProperty("sdk.dir", androidHome);
				props.store(fos,localPropertiesComment);
				
				props = new Properties();
				if(addAutomationLibsProperty){
					props.setProperty("safs.droid.automation.libs", automationLibsFolder[0]);
					for(int i=1;i<automationLibsFolder.length;i++){
						runnerLibraryFolderComment += "\n#safs.droid.automation.libs="+automationLibsFolder[i];
					}
					props.store(fos,runnerLibraryFolderComment);
				}
				
				props = new Properties();
				props.setProperty("target", targetPrefix+targetLevel);
				props.store(fos,targetComment);
				
				fos.close();
				
			} catch (IOException e) {
				System.err.println("Warning: fail to read/write file '"+localPropertiesFile.getAbsolutePath()+"' Exception="+e.getClass()+":"+e.getMessage());
			}
		}
	}
	
	/**
	 * To build a project, Ant requires that the file path separator "\" must be escaped as "\\" in the .properties file.<br>
	 * Before writing a file path to the properties file, call this method to get an escaped string.<br>
	 * 
	 * @param filepath, String, the file path to be modified
	 * @return String, The escaped file path
	 * @see #modifyPropertiesFile(File, String, int, int, String)
	 */
	static String addEscapeToFilepath(String filepath){
		if(Console.isWindowsOS()){
			return filepath.replace("\\", "\\\\");
		}
//		else if(Console.isUnixOS()){
//			
//		}
		else{
			//TODO Do we need to modify androidhome for other OS???
			//this depends the format of file "local.properties"
			return filepath;
		}
	}
	
	/**
	 * This method will attempt to re-sign {@link #originalAPK} with the {@link #resignJAR} 
	 * and write it to {@link #resignedAPK}.<br>
	 * @throws FileNotFoundException if the re-sign JAR file or the originalAPK file are not valid, 
	 * or if the output directory for the resignedAPK is not valid.
	 */
	static void resignOriginalAPK() throws FileNotFoundException{
		progresser.setProgressMessage("Attempting to re-sign '"+ originalAPK +"'");
		File jarfile = new CaseInsensitiveFile(resignJAR).toFile();
		if(!jarfile.isFile()) throw new FileNotFoundException("Invalid RESIGN JAR path: "+ resignJAR);
		File inapk = new CaseInsensitiveFile(originalAPK).toFile();
		if(!inapk.isFile()) throw new FileNotFoundException("Invalid RESIGN input APK: "+ originalAPK);
		File outapk = new CaseInsensitiveFile(resignedAPK).toFile();
		if(!outapk.getParentFile().isDirectory()) throw new FileNotFoundException("Invalid RESIGN output directory: "+ outapk.getParentFile().getPath());
		try{
			java.net.URLClassLoader l = new java.net.URLClassLoader(new java.net.URL[]{new java.net.URL("file:"+ jarfile.getAbsolutePath())}, ClassLoader.getSystemClassLoader());
			Class<?> c = l.loadClass("de.troido.resigner.main.Main");
			Method[] methods = c.getDeclaredMethods();
			Method main = null;
			for(int i=0;i<methods.length;i++){
				Method method = methods[i];
				if(method.getName().equals("main")) main = method;
			}
			main.invoke(c, new Object[]{new String[]{inapk.getAbsolutePath(),outapk.getAbsolutePath()}});
			progresser.setProgressMessage("Success re-signing '"+ outapk.getAbsolutePath() +"'");
		}catch(Exception x){
			progresser.setProgressMessage("ERROR encountered re-signing '"+ outapk.getAbsolutePath() +"'");
			System.err.println("Warning: Fail to re-sign APK due to '"+x.getMessage()+"'");
		}
	}
	
	/**
	 * This method will build RobotiumTestRunner, SAFSTestRunner and SAFSTCPMessenger<br>
	 * It will use Ant Tool to build. So please call main() with parameter "-anthome antHome", or<br> 
	 * an environment "ANT_HOME" should be set to the "Ant tool home directory".<br>
	 * @throws FileNotFoundException
	 * @see {@link #buildAPK(String, boolean)}
	 */
	static void buildMessengerRunner() throws FileNotFoundException{
		progresser.setProgressMessage("Try to build Messenger and Runner.");
		//Initialize the AntTool's home
		try{
			anttool.setToolHome(antHome);
		}catch(IllegalStateException ise){
			try{
				antHome = anttool.getToolHome();
			}catch(IllegalStateException e){
				System.err.println("Warning: Fail to initialize Ant Tool due to '"+e.getMessage()+"'");
				return;
			}
		}
		
		//Messenger must be built firstly:
		//Build messenger will generate a safstcpmessenger.jar and copy it to RobotiumTestRunner's 'libs' folder
		//safstcpmessenger.jar is needed during the build of Runner.
		//But it doesn't put that jar to the SAFSRunner's 'libs' folder!!!
		File messenger = new File(safsdir, TCP_MESSENGER_FOLDER);
		if(messenger.exists()){
			progresser.setProgressMessage("Building "+TCP_MESSENGER_FOLDER+" ... ");
			if(!buildAPK(messenger.getAbsolutePath(), true)){
				System.err.println("Warning: Fail to build Messenger '"+messenger.getAbsolutePath()+"'");
			}
			progresser.setProgress(50);
		}else{
			throw new FileNotFoundException(messenger.getAbsolutePath()+" is not found.");
		}
		
		File runner = new File(safsdir, ROBOTIUM_RUNNER_FOLDER);
		if(runner.exists()){
			progresser.setProgressMessage("Building "+ROBOTIUM_RUNNER_FOLDER+" ... ");
			if(!buildAPK(runner.getAbsolutePath(), true)){
				System.err.println("Warning: Fail to build Robotium Runner '"+runner.getAbsolutePath()+"'");
			}
			progresser.setProgress(70);
		}
		//else{ // RobotiumTestRunner is no longer delivered with Robotium 5.x support
		//	throw new FileNotFoundException(runner.getAbsolutePath()+" is not found.");
		//}
		
		File safsrunner = new File(safsdir, SAFS_RUNNER_FOLDER);
		if(safsrunner.exists()){
			if(runner.exists()){ //RobotiumTestRunner may no longer be delivered with RC
				//Before building the SAFSRunner, we need to copy the safstcpmessenger.jar from 
				//RobotiumTestRunner/libs to SAFSTestRunner/libs
				String jarfile = "libs"+File.separator+"safstcpmessenger.jar";
				String src = runner.getAbsolutePath();
				String dest = safsrunner.getAbsolutePath();
				if(!src.endsWith(File.separator)) src+=File.separator+jarfile; else src+=jarfile;
				if(!dest.endsWith(File.separator)) dest+=File.separator+jarfile; else dest+=jarfile;
				
				try {
					FileUtilities.copyFile(new FileInputStream(new File(src)), new FileOutputStream(new File(dest)));
				} catch (IOException e) {
					System.err.println("Warning: Fail to copy '"+src+"' to '"+dest+"'");
				}
			}
			progresser.setProgressMessage("Building "+SAFS_RUNNER_FOLDER+" ... ");
			if(!buildAPK(safsrunner.getAbsolutePath(), true)){
				System.err.println("Warning: Fail to build SAFS Runner '"+safsrunner.getAbsolutePath()+"'");
			}
			progresser.setProgress(90);
		}else{ 
			throw new FileNotFoundException(safsrunner.getAbsolutePath()+" is not found.");
		}
	}
	
	/**
	 * chmod -R a+w on the provided folder.<br>
	 * Only valid on Unix/Mac systems.
	 * @param afolder
	 * @return
	 */
	static boolean modUnixRecursiveFolderAllWritePermissions(File afolder){
		if(afolder==null) {
			System.err.println("Invalid/Null folder provided for modUnixRecursiveFolder!");
			return false;
		}
		if(!afolder.isDirectory()) {
			System.err.println("Folder provided for modUnixRecursiveFolder is NOT a directory!");
			return false;
		}
		boolean success = false;
		Process2 process = null;		
		try {
			Process proc = Runtime.getRuntime().exec("chmod -R a+w "+afolder.getAbsolutePath());
			process = (new Process2(proc)).discardStderr().discardStdout().waitForSuccess();
			success = true;
		} catch (Exception e) {
			System.err.println("During modUnixRecursiveFolder, met Exception="+e.getMessage());
		}finally{
			try{ process.destroy(); process = null;}catch(Exception x){}
		}		
		return success;
	}
	
	/**
	 * @param appDirString, String, the directory where the application locates
	 * @param debug, boolean, to build a debug or release version
	 * @return boolean, true if the build succeed
	 * @see #buildMessengerRunner()
	 */
	static boolean buildAPK(String appDirString, boolean debug){
		boolean buildSuccess = false;
		
		//Try to build the apk
		String[] allArgs =  new String[1];
		allArgs[0] = debug ? "debug" : "release"; 
		Process2 process = null;
		
		try {
			File workingDir = new File(appDirString);
			process = anttool.ant(workingDir, allArgs).discardStderr().discardStdout().waitForSuccess();
			buildSuccess = true;
			
		} catch (Exception e) {
			System.err.println("During build apk, met Exception="+e.getMessage());
		}finally{
			try{ process.destroy(); process = null;}catch(Exception x){}
		}
		
		return buildSuccess;
	}
	
	/**
	 * Perform RobotiumRC Overlay.
	 * The RobotiumRC overlay is currently always silent (GUI-less).<br>
	 * If -overlay parameter was specified then the install will attempt to use the 
	 * provided Snapshot ZIP file for the overlay.
	 * <p>
     * The overlay ZIP file must exist.  An overlay can be applied immediately 
     * following a RobotiumRC Install during the same invocation of this installer.  
     * Thus, you can run an initial install and then do an updated overlay on top of 
     * that as part of one invocation.
	 **/
	static int doSAFSOverlay() throws IOException, FileNotFoundException{
		
		int     status        = -1;
		progresser.setProgressMessage("RobotiumRC Install Dir : "+ safsdir);
		progresser.setProgressMessage ("RobotiumRC Overlay File: "+ overlayfile);
		
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified install path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
		    throw new FileNotFoundException(
		    "RobotiumRC Installation must already exist for an OVERLAY to be applied!");
		}
		
		progresser.setProgressMessage("Unzip "+overlayfile+" ......");
		FileUtilities.unzipFile(overlayfile, root, verbose);
		
		progresser.setProgress(dobuild? 20:100);
		//TODO Need to modify the local.properties after the overlay???
//		modifyPropertiesFile(root);

		if(dobuild) buildMessengerRunner();
		progresser.setProgressMessage("Completed RobotiumRC Overlay.");
		
		progresser.setProgress(doResign? 60:90);

		if(doResign) resignOriginalAPK();
		
		progresser.setProgress(100);
		progresser.setProgressMessage("Completed RobotiumRC Overlay.");
		
		status = 0;
		return status;
	}	
	
	static int doSAFSUnInstall(){
	    progresser.setProgressMessage("Robotium UnInstall: "+ installedsafsdir);
		progresser.setProgressMessage("This may take a few moments...");

		int status = FileUtilities.deleteDirectoryRecursively(installedsafsdir, verbose);
		progresser.setProgressMessage("Completed RobotiumRC UnInstallation.");
		return status;
	}
	
    /**
    * This SilentInstaller provides no GUI, but will accept some configuration parameters.
    * Any user-specified directories must exist; or, we must not be denied the ability to 
    * create them and write/copy files to them.
    * <p>
    * 
    * @param args[] The following parameters or arguments can be specified:<br>
    * <ul>
    *   <li>-silent<br>
    *     Currently, the install for RobotiumRC is always silent (GUI-less).
    *     <p>
    *   <li>-robotiumrc "robotium install directory"<br>
    *     Install RobotiumRC files to the user specified directory 
    *     instead of the default install directory.
    *     <p>
    *     You *must* provide this parameter with the desired install directory--even if 
    *     default--if the invocation is trying to combine operations like remove then install, 
    *     or install then overlay, or remove then install then overlay.
    *     <p>
    *   <li>-overlay RobotiumRCSnapshot.ZIP<br>
    *     Overlay RobotiumRC files over an existing RobotiumRC installation.<br>
    *     The overlay can occur immediately following a new installation 
    *     during the same class invocation.
    *   <li>-removerobotiumrc installedrobotiumrc<br>
    *     Remove the installed RobotiumRC.
    *     installedrobotiumrc indicates the directory where RobotiumRC is installed, 
    *     like "C:\RobotiumRC\" or "/usr/local/robotiumrc"
    *   <li>-androidhome androidHome<br>
    *     It indicates 'the home directory to android sdk'
    *     It is used to modify local.properties of Runner and Messenger.
    *     Only {@link #installsafs} is true, this parameter will take effect.
    *   <li>-anthome antHome<br>
    *     It indicates 'the home directory to ant sdk'
    *     It is used to build Runner and Messenger.
    *     Only both {@link #installsafs}/{@link #overlaysafs} and {@link #dobuild} are true, this parameter will take effect.
    *   <li>-dobuild<br>
    *     If this parameter is present, program will try to build the TestRunner and Messenger.
    *     Only {@link #installsafs} or {@link #overlaysafs}, this parameter will take effect.
    *   <li>-resignjar "&lt;pathTo/re-sign.jar"<br>
    *     If this parameter is present, along with valid settings for -originalapk and -resignedapk,
    *     the installer will attempt to re-sign the -originalapk with the installed SDK credentials 
    *     and write the new apk out to the -resignedapk path.
    *     Only with {@link #installsafs} will these parameters take effect.
    *   <li>-originalapk "&lt;pathTo/SpinnerActivity.apk"<br>
    *     If this parameter is present, along with valid settings for -resignjar and -resignedapk,
    *     the installer will attempt to re-sign the -originalapk with the installed SDK credentials  
    *     and write the new apk out to the -resignedapk path.
    *     Only with {@link #installsafs} will these parameters take effect.
    *   <li>-resignedapk "&lt;pathTo/SpinnerActivity-debug.apk"<br>
    *     If this parameter is present, along with valid settings for -resignjar and -originalapk,
    *     the installer will attempt to re-sign the -originalapk with the installed SDK credentials  
    *     and write the new apk out to the -resignedapk path.
    *     Only with {@link #installsafs} will these parameters take effect.
    *   <li>-v<br>
    *     Verbose to see the detail installation or un-installation information
    *   <li>-testrunner "RobotiumTestRunner or SAFSTestRunner"<br>
    *     It indicates the test runner to be used.
    *     If this is not present, the default test runner is RobotiumTestRunner.
    *     This setting will affect the content of local.properties of TCPMessenger project.
    * </ul>
    * 
    **/
	public static void main(String[] args) {
		parseArgs(args);
//		printArgs(args);
		
		int status = -1;		
		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	progresser.createAndShowGUI();
            }
        });
		
		try{
			if (removesafs ){ status = doSAFSUnInstall(); }
   	        if (installsafs){ status = doSAFSInstall(); }
	        if (overlaysafs){ status = doSAFSOverlay(); }
		}catch(FileNotFoundException fnf){
			System.out.println(" ");
			System.out.println("Installation Error:");
			System.out.println(fnf.getMessage());
			fnf.printStackTrace();
			status = -1;
		}catch(IOException io){
			System.out.println(" ");
			System.out.println("File Extraction Error:");
			System.out.println(io.getMessage());
			io.printStackTrace();
			status = -2;
		}finally{
			progresser.close();
		}
		
	    System.exit(status);
	}

}