package com.planet_ink.coffee_web.util;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
 * A custom thread factory whose only virtue is that it gives nice names to the
 * threads to make them easier to track.
 * @author Bo Zimmerman
 *
 */
public class CWThreadFactory implements ThreadFactory  
{
	private String 						serverName;
	private final AtomicInteger 		counter		= new AtomicInteger();
	private final LinkedList<Thread> 	active 		= new LinkedList<Thread>();
	private final CWConfig			config;
	
	public CWThreadFactory(String serverName, CWConfig config)
	{
		this.serverName=serverName;
		this.config=config;
	}
	public void setServerName(String newName)
	{
		this.serverName=newName;
	}
	@Override
	public Thread newThread(Runnable r) 
	{
		final Thread t = new CWThread(config, r,"cweb-"+serverName+"#"+counter.addAndGet(1));
		active.add(t);
		return t;
	}
	public Collection<Thread> getThreads() 
	{ 
		return active;
	}
}
