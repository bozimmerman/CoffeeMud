package com.planet_ink.coffee_web.http;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletManager;
import com.planet_ink.coffee_web.util.CWConfig;
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
 * Manages a relatively static set of servlet classes 
 * and the root contexts needed to access them.
 * 
 * @author Bo Zimmerman
 *
 */
public class ServletManager implements SimpleServletManager
{
	private final Map<String,Class<? extends SimpleServlet>> 		  servlets; 	// map of registered servlets by context
	private final Map<Class<? extends SimpleServlet>, RequestStats> servletStats; // stats about each servlet
	private final Map<Class<? extends SimpleServlet>, Boolean> 		  servletInit; // whether a servlets been initialized
	
	public ServletManager(CWConfig config)
	{
		servlets = new Hashtable<String,Class<? extends SimpleServlet>>();
		servletStats = new Hashtable<Class<? extends SimpleServlet>, RequestStats>();
		servletInit = new Hashtable<Class<? extends SimpleServlet>, Boolean>();
		
		for(final String context : config.getServlets().keySet())
		{
			String className=config.getServlets().get(context);
			if(className.indexOf('.')<0)
				className="com.planet_ink.coffee_web.servlets."+className;
			try
			{
				@SuppressWarnings("unchecked")
				final
				Class<? extends SimpleServlet> servletClass=(Class<? extends SimpleServlet>) Class.forName(className);
				registerServlet(context, servletClass);
			}
			catch (final ClassNotFoundException e)
			{
				config.getLogger().severe("Servlet Manager can't load "+className);
			}
		}
	}
	
	
	/**
	 * Internal method to register a servlets existence, and its context.
	 * This will go away when a config file is permitted
	 * @param context the uri context the servlet responds to
	 * @param servletClass the class of the servlet
	 */
	@Override
	public void registerServlet(String context, Class<? extends SimpleServlet> servletClass)
	{
		servlets.put(context, servletClass);
		servletStats.put(servletClass, new RequestStats());
	}
	
	/**
	 * For anyone externally interested, will return the list of servlet classes
	 * that are registered
	 * @return the list of servlet classes
	 */
	@Override
	public Collection<Class<? extends SimpleServlet>> getServlets()
	{
		return servlets.values();
	}

	/**
	 * Returns a servlet (if any) that handles the given uri context.
	 * if none is found, NULL is returned.
	 * @param rootContext the uri context
	 * @return the servlet class, if any, or null
	 */
	@Override
	public Class<? extends SimpleServlet> findServlet(String rootContext)
	{
		final Class<? extends SimpleServlet> c=servlets.get(rootContext);
		if(c == null)
			return null;
		if(servletInit.containsKey(c))
			return c;
		synchronized(servletInit)
		{
			if(servletInit.containsKey(c))
				return c;
			SimpleServlet servlet;
			try
			{
				servlet = c.newInstance();
				servlet.init();
			}
			catch (final Exception e){}
			servletInit.put(c, Boolean.TRUE);
		}
		return c;
	}

	/**
	 * Returns a servlet statistics object for the given servlet class
	 * or null if none exists
	 * @param servletClass the servlet class managed by this web server
	 * @return the servlet stats object
	 */
	@Override
	public RequestStats getServletStats(Class<? extends SimpleServlet> servletClass)
	{
		return servletStats.get(servletClass);
	}
}
