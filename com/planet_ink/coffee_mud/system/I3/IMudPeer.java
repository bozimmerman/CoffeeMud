package com.planet_ink.coffee_mud.system.I3;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.system.I3.persist.PersistentPeer;
import com.planet_ink.coffee_mud.system.I3.persist.Persistent;
import com.planet_ink.coffee_mud.system.I3.persist.PersistenceException;

public class IMudPeer implements PersistentPeer
{
    /**
     * Gets data about this peer from storage and gives it
     * back to the object for which this peer exists.
     * @exception imaginary.persist.PersistenceException if an error occurs during restore
     */
	public void restore() throws PersistenceException
	{
	}

    /**
     * Triggers a save of its peer.  Implementing classes
     * should do whatever it takes to save the object in
     * this method.
     * @exception imaginary.persist.PersistenceException if a problem occurs in saving
     */
    public void save() throws PersistenceException
	{
	}

    /**
     * Assigns a persistent object to this peer for
     * persistence operations.
     * @param ob the implementation of imaginary.persist.Persistent that this is a peer for
     * @see imaginary.persist.Persistent
     */
    public void setPersistent(Persistent ob)
	{
	}
		

    /**
     * An implementation uses this to tell its Persistent
     * that it is in the middle of restoring.
     * @return true if a restore operation is in progress
     */
    public boolean isRestoring()
	{return false;}
}
