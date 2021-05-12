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

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

/** 
 * This class is used to process the methods of RobotiumUtils from the robotium-remote-control side.<br>
 * This is only a wrapper class, the real implementation is in Solo class, refer to
 * <a href="/doc/com/jayway/android/robotium/remotecontrol/solo/Solo.html">Solo</a><br>
 * 
 * <p>
 * Usage:<br>
 * RobotiumUtils utils = new RobotiumUtils(solo);
 * </p>
 * 
 * @author Lei Wang, SAS Institute, Inc
 * @since  May 17, 2013
 *
 */
public class RobotiumUtils {
	private Solo solo = null;
	
	public RobotiumUtils(Solo solo){
		this.solo = solo;
	}
	/**
	 * Filters Views based on the given class type.
	 * 
	 * @param className, String, the class to filter
	 * @param viewUIDList, LIst, the list of UID for views to filter from
	 * @return a List of UID for filtered views
	 */
	public List filterViews(String className,  List<String>viewUIDList) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.filterViews(className, viewUIDList);
	}
	
	/**
	 * Filters a collection of Views and returns a list that contains only Views
	 * with text that matches a specified regular expression.
	 * 
	 * @param viewUIDList, List The collection of UID for views to scan.
	 * @param regex The text pattern to search for.
	 * @return A list of UID for views whose text matches the given regex.
	 */	
	public List filterViewsByText(List<String>viewUIDList, String regex) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		try{
			Pattern.compile(regex);
		}catch(PatternSyntaxException e){
			throw new IllegalThreadStateException("'"+regex+"' is not a valid regular expression. "+e.getClass().getSimpleName()+":"+e.getMessage());
		}
		return solo.filterViewsByText(viewUIDList, regex);
	}
	
	/**
	 * Filters all Views not within the given set.
	 *
	 * @param classNameList, List, contains 'full qualified class name' for all classes that are OK to pass the filter
	 * @param viewList, List, the list of UID for views to filter from
	 * @return a List of UID for filtered views
	 */	
	public List filterViewsToSet(List<String>classNameList, List<String>viewUIDList) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.filterViewsToSet(classNameList, viewUIDList);
	}

	/**
	 * Checks if a View matches a certain string and returns the amount of total matches.
	 * 
	 * @param regex, String, the regex to match
	 * @param textViewUID, String, the UID for view to check
	 * @param matchedViewUIDList, Set, set of UID for views that have matched
	 * @return number of total matches
	 */
	public int getNumberOfMatches(String regex, String textViewUID, Set<String>matchedViewUIDList) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.getNumberOfMatches(regex, textViewUID, matchedViewUIDList);
	}
	
	/**
	 * Removes invisible Views.
	 * 
	 * @param viewUIDList an Iterable with UID for Views that are being checked for invisible Views.
	 * @return a List of UID for no invisible Views.
	 */
	public List removeInvisibleViews(List<String>viewUIDList) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		return solo.removeInvisibleViews(viewUIDList);
	}
	
	/**
	 * Orders Views by their location on-screen.
	 * 
	 * @param viewUIDList, List, a list of UID for the views to sort.
	 */
	public void sortViewsByLocationOnScreen(List<String>viewUIDList) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		solo.sortViewsByLocationOnScreen(viewUIDList);
	}
	
	/**
	 * Orders Views by their location on-screen.
	 * 
	 * @param viewUIDList, List, a list of UID for the views to sort.
	 * @param yAxisFirst Whether the y-axis should be compared before the x-axis.
	 */
	public void sortViewsByLocationOnScreen(List<String>viewUIDList, boolean yAxisFirst) throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		solo.sortViewsByLocationOnScreen(viewUIDList, yAxisFirst);
	}
}
