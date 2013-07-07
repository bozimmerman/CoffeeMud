package com.planet_ink.coffee_mud.core.threads;
/*
Portions Copyright 2002 Jeff Kamenek
Portions Copyright 2002-2013 Bo Zimmerman

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
public class Timeout
{
	private final long interval;
	private long nextTimeout;
	
	public Timeout(long interval)
	{
		super();
		this.interval=interval;
		reset();
	}
	
	public synchronized void reset()
	{
		this.nextTimeout=System.currentTimeMillis()+interval;
	}
	
	public synchronized void forceTimeout()
	{
		this.nextTimeout=Long.MIN_VALUE;
	}
	
	public synchronized boolean isTimedOut()
	{
		return System.currentTimeMillis() > this.nextTimeout;
	}
}
