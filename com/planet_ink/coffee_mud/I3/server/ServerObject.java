/**
 * imaginary.server.ServerObject
 * Copyright (c) 1996 George Reese
 * An interface for mudlib object's to implement
 */

package com.planet_ink.coffee_mud.i3.server;


/**
 * The ServerObject interface prescribes methods which
 * must be defined in any object in the mudlib.
 * Specifically, it wants objects to flag when they
 * are destructed so that the server will stop keeping
 * track of them, and also so that other objects in
 * the mudlib can stop keeping track of them.  It is
 * important to remember with Java that destructing an
 * object does not get rid of all references to it.  You
 * need to get rid of all references to it in order to
 * delete it from memory.  A good mudlib thus will always
 * check if an object is destructed before using it in
 * code and it will remove references to destructed objects.
 * For example:
 * <PRE>
 * MudObject some_object;
 *
 * public void doSomething() {
 *   if( some_object.getDestructed() ) {
 *     some_object = null;
 *     return;
 *   }
 *   some_object.getSomethingDoneTo();
 * }
 * </PRE>
 * For people used to LPC, the above code would be roughly
 * the same as doing:
 * <PRE>
 * object some_object;
 *
 * void do_something() {
 *   if( !some_object ) {
 *     return;
 *   }
 *   some_object->get_something_done_to();
 * }
 * </PRE>
 * Created: 27 September 1996<BR>
 * Last modified: 27 September 1996
 * @author George Reese
 * @version 1.0
 */
public interface ServerObject {
    /**
     * This method should make it such that getDestructed()
     * returns true and the object is no longer valid for
     * the system.
     * @see #getDestructed
     */
    public abstract void destruct();

    /**
     * Triggered every server cycle by the server to see if this
     * object has an event to process.  If this object has an
     * event to process, this method should then trigger the
     * processing of that event.
     */
    public void processEvent();

    /**
     * Signifies that thsi object has been marked for destruction
     * and that all references to this object should be nulled
     * out as soon as they notice.
     * @return true if the object is marked for destruction, false otherwise
     */
    public abstract boolean getDestructed();

    /**
     * This method is used to find the unique object id for this
     * object.  The object id is assigned by the server through
     * the setObjectId() method.  An implementation should thus
     * keep track of this object id, not allow anything else to
     * modify it, and return it through this getObjectId() method.
     * @return the object id as assigned by the server
     * @see #setObjectId
     */
     public abstract String getObjectId();

     /**
      * This method should be used to set the object's unique
      * object id which the object should be keeping track of.
      * Make sure that this can only get set once.  The proper
      * code for this method probably should be something like:
      * <PRE>
      * public final void setObjectId(String id) {
      *   if( object_id != null ) {
      *     return;
      *   }
      *   object_id = id;
      * }
      * </PRE>
      * @param id the unique value to which the id is being set
      * @see #getObjectId
      */
     public abstract void setObjectId(String id);
}
