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

/**
 * Encapsulates abstract logging or other reporting activity allowing for different 
 * outlets of test logging and reporting.
 * @author Carl Nagle, SAS Institute, Inc.
 */
public interface LogsInterface {

	/** Log or otherwise report a passed/success message */
	public void pass(String action, String message);
	/** Log or otherwise report a failure/error message */
	public void fail(String action, String message);
	/** Log or otherwise report a warning message */
	public void warn(String action, String message);
	/** Log or otherwise report a generic informative message */
	public void info(String message);
	/** Log or otherwise report a debug message */
	public void debug(String message);

	/** 
	 * enable or disable the logging or reporting of debug messages. 
	 * In many implementations, disabling debug logging can improve overall runtime performance. */
	public void enableDebug(boolean enabled);
	
	/** @return true if debug logging is enabled. false otherwise. */
	public boolean isDebugEnabled();
}
