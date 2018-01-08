package com.planet_ink.coffee_web.util;

/*
   Copyright 2002-2018 Bo Zimmerman

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
 * A basic thread for all request management.  It's main purpose
 * is simply to carry the configuration for the server around
 * @author Bo Zimmerman
 *
 */
public class CWThread extends Thread
{
	private final CWConfig config;
	
	public CWThread(CWConfig config, Runnable r, String name)
	{
		super(r, name);
		this.config=config;
	}
	
	public CWThread(CWConfig config, String name)
	{
		super(name);
		this.config=config;
	}
	
	public CWConfig getConfig()
	{
		return config;
	}
	
	public String toString()
	{
		final StringBuilder dump = new StringBuilder("");
		final java.lang.StackTraceElement[] s=getStackTrace();
		for (final StackTraceElement element : s)
			dump.append(element.getClassName()+": "+element.getMethodName()+"("+element.getFileName()+": "+element.getLineNumber()+") | ");
		return dump.toString();
	}
}
