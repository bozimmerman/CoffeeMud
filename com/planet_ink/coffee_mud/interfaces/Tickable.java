package com.planet_ink.coffee_mud.interfaces;

public interface Tickable
{
	public String ID();
	public String name();

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
