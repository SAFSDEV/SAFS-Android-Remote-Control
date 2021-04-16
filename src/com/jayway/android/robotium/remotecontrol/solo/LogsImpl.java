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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;

/**
 * Default FileWriter version of a Robotium RemoteControl LogsInterface
 * @author CarlNagle
 */
public class LogsImpl implements LogsInterface {

	java.nio.file.Path logpath = null;
	BufferedWriter writer = null;
	boolean debugEnabled = false;
	
	/**
	 * Initialize a BufferedWriter with "RemoteControl.log" output at the User's current working directory.
	 * @throws IOException from initialize() if Path is not a valid absolute Path.
	 * @see #initialize()
	 */
	public LogsImpl()throws IOException{
		super();
		logpath = FileSystems.getDefault().getPath(System.getProperty("user.dir")+ File.separator+"RemoteControl.log");
		initialize();
	}

	/**
	 * Initialize a BufferedWriter with the file Path specified.
	 * @param apath
	 * @throws IOException from initialize() if Path is not a valid absolute Path.
	 * @see #initialize()
	 */
	public LogsImpl(java.nio.file.Path apath)throws IOException{
		super();
		logpath = apath;
		initialize();
	}
	
	/**
	 * Internal use.  Initialize the LogsInterface passed to the constructors.
	 * @throws IOException if Path is not a valid absolute Path that can be written.
	 */
	protected void initialize() throws IOException{
		if(logpath == null) throw new NoSuchFileException("null");
		if(!logpath.isAbsolute()) throw new NoSuchFileException("Incomplete LogsInterface path: "+ logpath.toFile().getPath());
		writer = java.nio.file.Files.newBufferedWriter(logpath, StandardCharsets.UTF_8);
	}
	
	@Override
	public void finalize()throws Throwable{
		try{ writer.flush(); }catch(Exception x){}
		try{ writer.close(); }catch(Exception x){}
		writer = null;
		logpath = null;
	}

	@Override
	public void pass(String action, String message) {
		try{
			writer.write("PASS : "+ action +": "+ message);
			writer.newLine();
			writer.flush();
		}catch(Exception x){}
	}

	@Override
	public void fail(String action, String message) {
		try{
			writer.write("FAIL : "+ action +": "+ message);
			writer.newLine();
			writer.flush();
		}catch(Exception x){}
	}

	@Override
	public void warn(String action, String message) {
		try{
			writer.write("WARN : "+ action +": "+ message);
			writer.newLine();
			writer.flush();
		}catch(Exception x){}
	}

	@Override
	public void info(String message) {
		try{
			writer.write(" INFO: "+ message);
			writer.newLine();
			writer.flush();
		}catch(Exception x){}
	}

	@Override
	public void debug(String message) {
		try{
			if(debugEnabled){
				writer.write("DEBUG: "+ message);
				writer.newLine();
				writer.flush();
			}
		}catch(Exception x){}
	}

	@Override
	public void enableDebug(boolean enabled) {
		debugEnabled = enabled;
	}

	@Override
	public boolean isDebugEnabled() {
		return debugEnabled;
	}
	
}
