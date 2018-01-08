package com.planet_ink.coffee_web.interfaces;

import java.util.Date;

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
 * A pojo interface for servlet sessions
 * @author Bo Zimmerman
 *
 */
public interface SimpleServletSession
{
	/**
	 * Returns special defined string "user".
	 * By default, this string is empty ""
	 * @return a session string called "user"
	 */
	public String getUser();
	/**
	 * Sets the special defined string "user"
	 * @param user the string called "user"
	 */
	public void setUser(String user);
	/**
	 * Returns the session id as defined by the
	 * session manager managing this object
	 * @return the session id
	 */
	public String getSessionId();
	/**
	 * Returns the date object corresponding to when
	 * this session was created.
	 * @return date object
	 */
	public Date getSessionStart();
	/**
	 * Returns the time, in milliseconds, when this session 
	 * was last "touched" by the client
	 * @return time in millis when last touched
	 */
	public long getSessionLastTouchTime();
	/**
	 * Returns an arbitrary, session-defined object stored in this
	 * session.
	 * @param name the name of the object
	 * @return the object stored, or null if not found
	 */
	public Object getSessionObject(String name);
	/**
	 * Sets  an arbitrary, session-defined object stored in this
	 * session.  Sending a value of null will delete the object.
	 * @param name any olde name for an object
	 * @param obj the object to store under this name, or null to delete
	 */
	public void setSessionObject(String name, Object obj);
	/**
	 * Marks this session as having been access by the client at this time
	 */
	public void touch();
}
