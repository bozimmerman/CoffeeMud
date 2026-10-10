package com.planet_ink.coffee_web.http;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;

import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletManager;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_web.util.RequestStats;

/*
   Copyright 2012-2026 Bo Zimmerman

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
 * Manages a relatively static set of servlets
 * and the root contexts needed to access them.
 *
 * @author Bo Zimmerman
 *
 */
public class ServletManager implements SimpleServletManager
{
	private final Map<String, SimpleServlet>				servlets;		// map of registered servlets by context
	private final Map<String, Class<?>>						servletClasses;	// map of registered servlets by context
	private final Map<SimpleServlet, RequestStats>			servletStats;	// stats about each servlet
	private final Map<SimpleServlet, Map<String, Object>>	servletAttribs;	// attributes for each servlet
	private final Map<SimpleServlet, Map<String, String>>	servletInis;	// ini entries for each servlet
	private final CWConfig config;

	public ServletManager(final CWConfig config)
	{
		this.config = config;
		servlets = new Hashtable<String,SimpleServlet>();
		servletStats = new Hashtable<SimpleServlet, RequestStats>();
		servletClasses = new Hashtable<String, Class<?>>();
		servletAttribs = new Hashtable<SimpleServlet, Map<String, Object>>();
		servletInis = new Hashtable<SimpleServlet, Map<String, String>>();

		for(final String context : config.getServletClasses().keySet())
		{
			String className=config.getServletClasses().get(context);
			if(className.indexOf('.')<0)
				className="com.planet_ink.coffee_web.servlets."+className;
			try
			{
				@SuppressWarnings("unchecked")
				final Class<? extends SimpleServlet> servletClass = (Class<? extends SimpleServlet>)Class.forName(className);
				servletClasses.put(context, servletClass);
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
	 * @param servlet the of the servlet
	 */
	@Override
	public void registerServlet(final String context, final SimpleServlet servlet)
	{
		servlets.put(context, servlet);
		servletStats.put(servlet, new RequestStats());
		try
		{
			servlet.init();
		}
		catch(final Exception e)
		{
			config.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * For anyone externally interested, will return the list of servlets
	 * that are registered
	 * @return the list of servlets
	 */
	@Override
	public Collection<SimpleServlet> getServlets()
	{
		return servlets.values();
	}

	/**
	 * For anyone externally interested, will return the list of servlet
	 * paths that are registered
	 * @return the list of servlet paths
	 */
	@Override
	public Collection<String> getServletPaths()
	{
		return servletClasses.keySet();
	}

	/**
	 * Returns a servlet (if any) that handles the given uri context.
	 * if none is found, NULL is returned.
	 * @param rootContext the uri context
	 * @return the servlet, if any, or null
	 */
	@Override
	public SimpleServlet findServlet(final String rootContext)
	{
		SimpleServlet c=servlets.get(rootContext);
		if(c != null)
			return c;
		synchronized(servlets)
		{
			c=servlets.get(rootContext);
			if(c != null)
				return c;
			final Class<?> ssClass = this.servletClasses.get(rootContext);
			if(ssClass == null)
				return null;
			try
			{
				final Object o = ssClass.getDeclaredConstructor().newInstance();
				if(o instanceof SimpleServlet)
					c = (SimpleServlet)o;
				else
					c = null;
				if(c == null)
				{
					config.getLogger().log(Level.SEVERE, "No valid servlet found for "+rootContext);
					this.servletClasses.remove(rootContext);
				}
				else
					registerServlet(rootContext, c);
			}
			catch (final Exception e)
			{
				config.getLogger().log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return c;
	}

	/**
	 * Returns a servlet statistics object for the given servlet
	 * or null if none exists
	 * @param servlet the servlet managed by this web server
	 * @return the servlet stats object
	 */
	@Override
	public RequestStats getServletStats(final SimpleServlet servlet)
	{
		return servletStats.get(servlet);
	}


	/**
	 * Returns the context variables for the given servlet, which persist
	 * across requests, but not across server boots.
	 *
	 * @param rootContext the servlet context
	 * @return the variables map
	 */
	@Override
	public Map<String, Object> getServletContextVariables(final String rootContext)
	{
		final SimpleServlet servlet = this.findServlet(rootContext);
		if(servlet == null)
			return new Hashtable<String, Object>();
		if(!this.servletAttribs.containsKey(servlet))
			this.servletAttribs.put(servlet, new Hashtable<String, Object>());
		return this.servletAttribs.get(servlet);
	}


	@Override
	public Map<String, String> getServletInitVariables(final String rootContext)
	{
		final SimpleServlet servlet = this.findServlet(rootContext);
		if(servlet == null)
			return new Hashtable<String, String>();
		if(!this.servletInis.containsKey(servlet))
			this.servletInis.put(servlet, new Hashtable<String, String>());
		return this.servletInis.get(servlet);
	}
}
