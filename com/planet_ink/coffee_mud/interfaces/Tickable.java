package com.planet_ink.coffee_mud.interfaces;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public interface Tickable
{
	public String ID();
	public String name();
	public long getTickStatus();
	public static long STATUS_NOT=0;
	public static long STATUS_START=1;
	public static long STATUS_CLASS=2;
	public static long STATUS_RACE=3;
	public static long STATUS_FIGHT=4;
	public static long STATUS_WEATHER=5;
	public static long STATUS_DEAD=6;
	public static long STATUS_ALIVE=7;
	public static long STATUS_OTHER=98;
	public static long STATUS_END=99;
	public static long STATUS_BEHAVIOR=512;
	public static long STATUS_AFFECT=1024;

	/**
	 * this method allows any environmental object
	 * to behave according to a timed response.  by
	 * default, it will never be called unless the
	 * object uses the ServiceEngine to setup service.
	 * The tickID allows granularity with the type
	 * of service being requested.
	 */
	public boolean tick(Tickable ticking, int tickID);
}
