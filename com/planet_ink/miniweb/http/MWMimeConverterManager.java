package com.planet_ink.miniweb.http;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.miniweb.interfaces.HTTPOutputConverter;
import com.planet_ink.miniweb.interfaces.MimeConverterManager;
import com.planet_ink.miniweb.util.MiniWebConfig;
import com.planet_ink.miniweb.util.MWRequestStats;

/*
Copyright 2012-2014 Bo Zimmerman

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
public class MWMimeConverterManager implements MimeConverterManager
{
	private final Map<MIMEType,Class<? extends HTTPOutputConverter>> 	  	converters; 	// map of registered servlets by context
	private final Map<Class<? extends HTTPOutputConverter>, MWRequestStats> requestStats; // stats about each servlet

	/**
	 * Construct a mime config manager, loading the converters from the config given
	 * @param config
	 */
	public MWMimeConverterManager(MiniWebConfig config)
	{
		converters = new Hashtable<MIMEType,Class<? extends HTTPOutputConverter>>();
		requestStats = new Hashtable<Class<? extends HTTPOutputConverter>, MWRequestStats>();
		for(String mimeTypeName : config.getFileConverts().keySet())
		{
			MIMEType mimeType=MIMEType.valueOf(mimeTypeName);
			String className=config.getFileConverts().get(mimeTypeName);
			if(className.indexOf('.')<0)
				className="com.planet_ink.miniweb.converters."+className;
			try
			{
				@SuppressWarnings("unchecked")
				Class<? extends HTTPOutputConverter> converterClass=(Class<? extends HTTPOutputConverter>) Class.forName(className);
				registerConverter(mimeType, converterClass);
			}
			catch (ClassNotFoundException e)
			{
				config.getLogger().severe("Converter Manager can't load "+className);
			}
			catch (Exception e)
			{
				config.getLogger().severe("Converter Manager can't converter "+mimeTypeName);
			}
		}
	}


	/**
	 * Internal method to register a servlets existence, and its context.
	 * This will go away when a config file is permitted
	 * @param context the uri context the servlet responds to
	 * @param converterClass the class of the converter
	 */
	@Override
	public void registerConverter(MIMEType mime, Class<? extends HTTPOutputConverter> converterClass)
	{
		converters.put(mime, converterClass);
		requestStats.put(converterClass, new MWRequestStats());
	}

	/**
	 * For anyone externally interested, will return the list of converter classes
	 * that are registered
	 * @return the list of converter classes
	 */
	@Override
	public Collection<Class<? extends HTTPOutputConverter>> getConverters()
	{
		return converters.values();
	}

	/**
	 * Returns a converter (if any) that handles the given mime type.
	 * if none is found, NULL is returned.
	 * @param mime the mime type
	 * @return the servlet class, if any, or null
	 */
	@Override
	public Class<? extends HTTPOutputConverter> findConverter(MIMEType mime)
	{
		return converters.get(mime);
	}

	/**
	 * Returns a statistics object for the given converter class
	 * or null if none exists
	 * @param converterClass the converter class managed by this web server
	 * @return the converter stats object
	 */
	@Override
	public MWRequestStats getConverterStats(Class<? extends HTTPOutputConverter> converterClass)
	{
		return requestStats.get(converterClass);
	}
}
