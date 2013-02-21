package com.planet_ink.miniweb.interfaces;

import java.util.Collection;

import com.planet_ink.miniweb.http.MIMEType;
import com.planet_ink.miniweb.util.MWRequestStats;

/*
Copyright 2012-2013 Bo Zimmerman

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
 * Interface for an http response converter manager, based on the mime type 
 * of the file returned.
 * 
 * @author Bo Zimmerman
 *
 */
public interface MimeConverterManager
{
	/**
	 * Internal method to register a servlets existence, and its context.
	 * This will go away when a config file is permitted
	 * @param context the uri context the servlet responds to
	 * @param converterClass the class of the converter
	 */
	public void registerConverter(MIMEType mime, Class<? extends HTTPOutputConverter> converterClass);
	
	/**
	 * For anyone externally interested, will return the list of converter classes
	 * that are registered
	 * @return the list of converter classes
	 */
	public Collection<Class<? extends HTTPOutputConverter>> getConverters();

	/**
	 * Returns a converter (if any) that handles the given mime type.
	 * if none is found, NULL is returned.
	 * @param mime the mime type
	 * @return the servlet class, if any, or null
	 */
	public Class<? extends HTTPOutputConverter> findConverter(MIMEType mime);

	/**
	 * Returns a statistics object for the given converter class
	 * or null if none exists
	 * @param converterClass the converter class managed by this web server
	 * @return the converter stats object
	 */
	public MWRequestStats getConverterStats(Class<? extends HTTPOutputConverter> converterClass);
}
