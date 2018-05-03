package com.planet_ink.coffee_web.http;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.ServletSessionManager;
import com.planet_ink.coffee_web.interfaces.SimpleServletSession;
import com.planet_ink.coffee_web.util.CWConfig;

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
 * This class manages servlet session objects
 * for servlets as well.
 * 
 * @author Bo Zimmerman
 *
 */
public class SessionManager implements ServletSessionManager
{
	private final Map<String,SimpleServletSession>	sessions;		// map of ids to sessions
	private final CWConfig						config;
	
	/**
	 * Construct a session manager
	 * @param config the web server config
	 */
	public SessionManager(CWConfig config)
	{
		sessions = new Hashtable<String, SimpleServletSession>();  
		this.config=config;
	}
	
	/**
	 * Internal method to find an existing session based on the request data.
	 * @param sessionID the id of the session
	 */
	@Override
	public SimpleServletSession findSession(String sessionID)
	{
		return sessions.get(sessionID);
	}

	/**
	 * Internal method to find an existing session based on the request data.
	 * If the session does not exist, it will be created and returned
	 * @param sessionID the id of the session
	 */
	@Override
	public SimpleServletSession findOrCreateSession(String sessionID)
	{
		SimpleServletSession session = sessions.get(sessionID);
		if(session != null) return session;
		session = new ServletSession(sessionID);
		synchronized(sessions)
		{
			sessions.put(sessionID, session);
			return session;
		}
	}
	
	/**
	 * A maintence method forcing the manager to examine all sessions
	 * for any that have timed out and remove them, if so.
	 */
	@Override
	public void cleanUpSessions()
	{
		synchronized(sessions)
		{
			final long currentTime=System.currentTimeMillis();
			final long idleExpireTime=currentTime - config.getSessionMaxIdleMs();
			final Date ageExpireTime=new Date(currentTime - config.getSessionMaxAgeMs());
			for(final Iterator<String> s=sessions.keySet().iterator();s.hasNext();)
			{
				final String sessionID=s.next();
				final SimpleServletSession session=sessions.get(sessionID);
				if((session.getSessionLastTouchTime() < idleExpireTime)
				||(session.getSessionStart().before(ageExpireTime)))
				{
					s.remove();
				}
			}
		}
	}

	/**
	 * For generating a new servlet session and returning its ID
	 * @param request the current request to base the new session on
	 * @return the new servlet session obj
	 */
	@Override
	public SimpleServletSession createSession(HTTPRequest request)
	{
		String sessionID = request.getClientAddress().hashCode()+""+System.currentTimeMillis() + "" + System.nanoTime();
		try
		{
			while(sessions.containsKey(sessionID))
			{
				Thread.sleep(1);
				sessionID = request.getClientAddress().hashCode()+""+System.currentTimeMillis() + "" + System.nanoTime();
			}
		}catch(final Exception e)
		{
			config.getLogger().throwing("", "", e);
		}
		final SimpleServletSession newSession = new ServletSession(sessionID);
		synchronized(sessions)
		{
			sessions.put(sessionID, newSession);
			return newSession;
		}
	}
}
