package com.planet_ink.coffee_mud.interfaces;

public interface MsgListener
{
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental myHost, CMMsg msg);

	/** this method is used to tell the system whether
	 * a PENDING message may take place
	 */
	public boolean okMessage(Environmental myHost, CMMsg msg);

}
