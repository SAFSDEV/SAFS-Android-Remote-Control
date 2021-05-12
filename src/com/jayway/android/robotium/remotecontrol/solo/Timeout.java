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

import java.util.concurrent.TimeoutException;

import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

/** 
 * This class is used to process the methods of Timeout at the robotium-remote-control side.<br>
 * This is only a wrapper class, the real implementation is in Solo class, refer to
 * <a href="/doc/com/jayway/android/robotium/remotecontrol/solo/Solo.html">Solo</a><br>
 * 
 * <p>
 * Usage:<br>
 * Timeout timeout = new Timeout(solo);
 * </p>
 * 
 * @author Lei Wang, SAS Institute, Inc
 * @since  Jun 18, 2013
 *
 */
public class Timeout {
	private Solo solo = null;
	
	public Timeout(Solo solo){
		this.solo = solo;
	}
	
	/**
	 * Sets the default timeout length of the waitFor methods. Its by default set to 20 000 milliseconds.  -- <b>Robotium 4.1+ required</b>.<br>
	 * @param milliseconds, int, the timeout of the waitFor methods, in milliseconds.
	 * @return true if the command executed successfully, false if it did not.  
	 */
	public boolean setLargeTimeout(int milliseconds) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.setLargeTimeout(milliseconds);
	}
	
	/**
	 * Sets the default timeout length of the get, is, set, assert, enter and click methods. Its by default set to 10 000 milliseconds.  -- <b>Robotium 4.1+ required</b>.<br>
	 * @param milliseconds, int, the timeout of the get, is, set, assert, enter and click methods, in milliseconds.
	 * @return true if the command executed successfully, false if it did not.  
	 */
	public boolean setSmallTimeout(int milliseconds) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.setSmallTimeout(milliseconds);
	}
	
	/**
	 * Gets the default timeout length of the waitFor methods.  -- <b>Robotium 4.1+ required</b>.<br>
	 * @return int, the timeout in milliseconds.
	 */
	public int getLargeTimeout() throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.getLargeTimeout();
	}
	
	/**
	 * Gets the default timeout length of the get, is, set, assert, enter and click methods.  -- <b>Robotium 4.1+ required</b>.<br>
	 * @return int, the timeout in milliseconds.  
	 */
	public int getSmallTimeout() throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.getSmallTimeout();
	}
}
