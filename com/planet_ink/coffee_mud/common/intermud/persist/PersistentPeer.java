/**
 * imaginary.persist.PersistentPeer
 * Copyright (c) 1996 George Reese
 * This source code may not be modified, copied,
 * redistributed, or used in any fashion without the
 * express written consent of George Reese.
 *
 * The PersistentPeer is an interface for defining
 * a particular kind of persistence for a specific
 * object.
 */

package com.planet_ink.coffee_mud.i3.persist;

/**
 * Any object which should persist over time should
 * have a PersistentPeer which handles saving it.
 * This allows a separate object to worry about
 * persistence issues, specifically whether the object
 * should save to a flatfile or database, and how that
 * saving occurs.
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public interface PersistentPeer {
    /**
     * Gets data about this peer from storage and gives it
     * back to the object for which this peer exists.
     * @exception imaginary.persist.PersistenceException if an error occurs during restore
     */
    public abstract void restore() throws PersistenceException;

    /**
     * Triggers a save of its peer.  Implementing classes
     * should do whatever it takes to save the object in
     * this method.
     * @exception imaginary.persist.PersistenceException if a problem occurs in saving
     */
    public abstract void save() throws PersistenceException;

    /**
     * Assigns a persistent object to this peer for
     * persistence operations.
     * @param ob the implementation of imaginary.persist.Persistent that this is a peer for
     * @see imaginary.persist.Persistent
     */
    public abstract void setPersistent(Persistent ob);

    /**
     * An implementation uses this to tell its Persistent
     * that it is in the middle of restoring.
     * @return true if a restore operation is in progress
     */
    public abstract boolean isRestoring();
}