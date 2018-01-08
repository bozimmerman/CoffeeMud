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
public interface CMRunnable extends Runnable
{
	/**
	 * Returns the number of milliseconds this runnable
	 * has been running.
	 * @return the time in millis
	 */
	public long activeTimeMillis();
	/**
	 * Either the time this runnable did start running, or
	 * will start running, depending on whether it's scheduled
	 * @return the time in millis
	 */
	public long getStartTime();
	/**
	 * The group identifier for this runnable, which may be used
	 * for executor selection
	 * @return the identifier for this runnable
	 */
	public int getGroupID();
}
