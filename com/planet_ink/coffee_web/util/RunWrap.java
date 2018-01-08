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
 * A runnable wrapper to be used with CWThreadExecutor that tracks
 * its own active running time.
 * @author Bo Zimmerman
 *
 */
public class RunWrap
{
	private final Runnable runnable;
	private final Thread   thread;
	private final long 	   startTime;
	
	public RunWrap(Runnable runnable, Thread thread)
	{
		this.runnable=runnable;
		this.thread=thread;
		startTime=System.currentTimeMillis();
	}
	
	public Runnable getRunnable()
	{
		return runnable;
	}
	
	public Thread getThread()
	{
		return thread;
	}
	
	/**
	 * Returns the number of milliseconds this runnable
	 * has been running.
	 * @return the time in millis
	 */
	public long activeTimeMillis()
	{
		return System.currentTimeMillis()-startTime;
	}
}
