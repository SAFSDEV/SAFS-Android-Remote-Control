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

import java.util.Properties;
import com.jayway.android.robotium.remotecontrol.solo.Message;
import com.jayway.android.robotium.remotecontrol.solo.SoloTest;

public class MyTest extends SoloTest{

  public MyTest(){ super(); }
  public MyTest(String[] args){ super(args); }
  public MyTest(String messengerApk, String testRunnerApk, String instrumentArg){
	  super(messengerApk, testRunnerApk, instrumentArg);
  }

  public static void main(String[] args){
	  SoloTest soloTest = new MyTest(args);	  
	  soloTest.process();
  }

	protected void test(){
		String action = "MyTest";
	    try{
		  String activityID = solo.getCurrentActivity();
		  Properties props = solo._last_remote_result;
		  String activityName = props.getProperty(Message.PARAM_NAME);
		  String activityClass = props.getProperty(Message.PARAM_CLASS);

		  log.pass(action, "CurrentActivity   UID: "+ activityID);
		  log.pass(action, "CurrentActivity Class: "+ activityClass);
		  log.pass(action, "CurrentActivity  Name: "+ activityName);

	    }catch(Throwable e){
		    e.printStackTrace();
	    }
	}
}

