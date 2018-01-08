package com.planet_ink.coffee_web.interfaces;

import java.util.Collection;

import com.planet_ink.coffee_web.util.RequestStats;

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
 * Interface for a manager of active sessions, which are mapped by 
 * their root context.
 * @author Bo Zimmerman
 *
 */
public interface SimpleServletManager
{
	/**
	 * Internal method to register a servlets existence, and its context.
	 * This will go away when a config file is permitted
	 * @param context the uri context the servlet responds to
	 * @param servletClass the class of the servlet
	 */
	public void registerServlet(String context, Class<? extends SimpleServlet> servletClass);
	
	/**
	 * For anyone externally interested, will return the list of servlet classes
	 * that are registered
	 * @return the list of servlet classes
	 */
	public Collection<Class<? extends SimpleServlet>> getServlets();

	/**
	 * Returns a servlet (if any) that handles the given uri context.
	 * if none is found, NULL is returned.
	 * @param rootContext the uri context
	 * @return the servlet class, if any, or null
	 */
	public Class<? extends SimpleServlet> findServlet(String rootContext);

	/**
	 * Returns a servlet statistics object for the given servlet class
	 * or null if none exists
	 * @param servletClass the servlet class managed by this web server
	 * @return the servlet stats object
	 */
	public RequestStats getServletStats(Class<? extends SimpleServlet> servletClass);

}
