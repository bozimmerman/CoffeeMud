package com.planet_ink.coffee_mud.interfaces;

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
