package com.planet_ink.coffee_mud.interfaces;

public interface MsgListener
{
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental myHost, Affect affect);

	/** this method is used to tell the system whether
	 * a PENDING affect may take place
	 */
	public boolean okAffect(Environmental myHost, Affect affect);

}
