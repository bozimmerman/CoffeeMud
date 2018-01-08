package com.planet_ink.coffee_mud.core.threads;

import com.planet_ink.coffee_mud.core.Log;

/*
Portions Copyright 2002 Jeff Kamenek
Portions Copyright 2002-2018 Bo Zimmerman

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
public class CMFactoryThread extends Thread
{
	private volatile Runnable runnable = null;

	public CMFactoryThread(ThreadGroup group, Runnable runnable, String name)
	{
		super(group,runnable,name);
		if(group==null)
			throw new java.lang.IllegalArgumentException();
		//this.runnable=runnable; the factory does not send a REAL runnable
	}

	/**
	 * Sets the runnable currently running
	 * if available
	 * @param runnable the runnable running
	 */
	public void setRunnable(Runnable runnable)
	{
		this.runnable=runnable;
	}

	/**
	 * Returns the runnable currently running
	 * if available
	 * @return the runnable running
	 */
	public Runnable getRunnable()
	{
		return runnable;
	}
}
