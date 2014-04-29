package com.planet_ink.miniweb.util;

/*
Copyright 2002-2011 Bo Zimmerman

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
public class MWThread extends Thread
{
	private final MiniWebConfig config;

	public MWThread(MiniWebConfig config, Runnable r, String name)
	{
		super(r, name);
		this.config=config;
	}

	public MWThread(MiniWebConfig config, String name)
	{
		super(name);
		this.config=config;
	}

	public MiniWebConfig getConfig()
	{
		return config;
	}
}
