package com.planet_ink.coffee_web.http;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.coffee_web.interfaces.SimpleServletSession;

/*
   Copyright 2012-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * A pojo for a servlet session
 * Handles a "user" string, start time, idle time, and an arbitrary
 * collectin of objects for servlets to shove stuff into
 * @author Bo Zimmerman
 *
 */
public class ServletSession implements SimpleServletSession
{
	private final String			sessionID;
	private volatile String 		user			= "";
	private final Date				startTime;
	private final Map<String,Object>objects			= new Hashtable<String,Object>();
	private volatile long			lastTouchTime;
	
	public ServletSession(String sessionID)
	{
		this.sessionID=sessionID;
		this.startTime=new Date(System.currentTimeMillis());
		lastTouchTime=System.currentTimeMillis();
	}
	
	/**
	 * Returns special defined string "user".
	 * By default, this string is empty ""
	 * @return a session string called "user"
	 */
	@Override
	public String getUser()
	{
		return user;
	}
	/**
	 * Sets the special defined string "user"
	 * @param user the string called "user"
	 */
	@Override
	public void setUser(String user)
	{
		if(user != null)
		{
			this.user=user;
		}
	}
	/**
	 * Returns the session id as defined by the
	 * session manager managing this object
	 * @return the session id
	 */
	@Override
	public String getSessionId()
	{
		return sessionID;
	}
	/**
	 * Returns the date object corresponding to when
	 * this session was created.
	 * @return date object
	 */
	@Override
	public Date getSessionStart()
	{
		return startTime;
	}
	/**
	 * Returns the time, in milliseconds, when this session 
	 * was last "touched" by the client
	 * @return time in millis
	 */
	@Override
	public long getSessionLastTouchTime()
	{
		return lastTouchTime;
	}
	/**
	 * Returns an arbitrary, session-defined object stored in this
	 * session.
	 * @param name the name of the object
	 * @return the object stored, or null if not found
	 */
	@Override
	public Object getSessionObject(String name)
	{
		if(name == null)
		{
			return null;
		}
		return objects.get(name);
	}
	/**
	 * Sets  an arbitrary, session-defined object stored in this
	 * session.  Sending a value of null will delete the object.
	 * @param name any olde name for an object
	 * @param obj the object to store under this name, or null to delete
	 */
	@Override
	public void setSessionObject(String name, Object obj)
	{
		if(name == null)
		{
			return;
		}
		if(obj == null)
			objects.remove(name);
		else
			objects.put(name,  obj);
	}
	/**
	 * Marks this session as having been access by the client at this time
	 */
	@Override
	public void touch()
	{
		lastTouchTime=System.currentTimeMillis();
	}
}
