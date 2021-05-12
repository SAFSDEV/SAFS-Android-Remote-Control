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
package com.jayway.android.robotium.remotecontrol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.jayway.android.robotium.remotecontrol.solo.Solo;

/**
 * This class is used to transport a collection of objects between MobileDevice/Emulator and RemoteControl.<br>
 * 
 * For example:<br> 
 * From release4.1+, Robotium begins to provide some APIs who use android.graphics.PointF as<br>
 * parameter. Some APIs need more than one PointF parameter, to transport multiple PointF as <br>
 * an object thru our TCP protocol in the same time, this class is created.<br>
 * Example of usage:<br>
 * At controller side, user can simply call {@link ObjectCollection()} to create an instance, and add some<br>
 * instances of PointF and send this object thru the wire.<br>
 * At the device side, we receive that object 'ObjectCollection' and we call {@link ObjectCollection#getObjectList()} <br>
 * to get a list of Android's PointF objects, which can be used by Robotium's APIs.<br><br>
 * 
 * @author Lei Wang, SAS Institute, Inc
 * @param <T>
 * @since  Jun 21, 2013
 *
 * @see Solo#rotateLarge(PointF, PointF)
 * @see Solo#getScreenshotSequence(int, int)
 */
public class ObjectCollection<T> implements Serializable{
	private static final long serialVersionUID = 8014238078288066150L;

	private List<T> objectList = new ArrayList<T>();
	
	public List<T> getObjectList(){
		return objectList;
	}
	
	public void addToObjectList(T p){
		objectList.add(p);
	}

	public int getSize(){
		return objectList.size();
	}
	
	public T getObject(int i){
		return objectList.get(i);
	}
}
