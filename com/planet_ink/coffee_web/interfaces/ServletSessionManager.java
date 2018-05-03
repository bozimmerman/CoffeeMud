package com.planet_ink.coffee_web.interfaces;

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
 * Interface for session managers for the web server.  Maintains a cache
 * of sessionID-&gt;session object map, and includes a method for periodic
 * timeout/cleanup which should be called from time to time for memory
 * purposes.
 * @author Bo Zimmerman
 */
public interface ServletSessionManager
{
	/**
	 * Internal method to find an existing session based on the request data.
	 * @param sessionID the id of the session
	 */
	public SimpleServletSession findSession(String sessionID);
	
	/**
	 * Internal method to find an existing session based on the request data.
	 * If the session does not exist, it will be created and returned
	 * @param sessionID the id of the session
	 */
	public SimpleServletSession findOrCreateSession(String sessionID);
	
	/**
	 * For generating a new servlet session and returning its ID
	 * @param request the current request to base the new session on
	 * @return the new servlet session obj
	 */
	public SimpleServletSession createSession(HTTPRequest request);

	/**
	 * A maintence method forcing the manager to examine all sessions
	 * for any that have timed out and remove them, if so.
	 */
	public void cleanUpSessions();
}
