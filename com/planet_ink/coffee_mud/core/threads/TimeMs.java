package com.planet_ink.coffee_mud.core.threads;
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
public class TimeMs
{
	private long time;

	public TimeMs(long t)
	{
		this.time=t;
	}

	public TimeMs()
	{
		this.time=System.currentTimeMillis();
	}

	public synchronized void setToNow()
	{
		this.time=System.currentTimeMillis();
	}

	public synchronized void setToLater(long amount)
	{
		this.time=System.currentTimeMillis()+amount;
	}

	public synchronized void set(long t)
	{
		this.time=t;
	}

	public synchronized long get()
	{
		return this.time;
	}

	public synchronized boolean isNowLaterThan()
	{
		return System.currentTimeMillis() > this.time;
	}

	public synchronized boolean isNowEarlierThan()
	{
		return System.currentTimeMillis() < this.time;
	}
}
