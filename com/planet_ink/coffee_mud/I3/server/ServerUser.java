/**
 * imaginary.server.ServerUser
 * Copyright (C) 1996 George Reese
 * The interface prescribing behaviour for a mudlib
 * user connection object.
 */

package com.planet_ink.coffee_mud.i3.server;


/**
 * The ServerUser interface prescribes behaviours which
 * must be defined by any user connection used with
 * the Imaginary JavaMud Server.  Specifically, it
 * requires that the user connection be able to handle
 * user input somehow.<BR>
 * Created: 27 September 1996<BR>
 * Last modified: 27 Septembet 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public interface ServerUser extends ServerObject {
    /**
     * This method is triggered by the server when the
     * user first connects.
     */
    public abstract void connect();

    /**
     * The server calls this method every server cycle.  The
     * mudlib implementation is expected to be queueing up user
     * input as it gets it (as opposed to processing it immediately
     * as it comes across the net) for synchronicity's sake.
     * The mudlib implementation therefore should use this method
     * to pull a command off the queue and process it.
     */
    public abstract void processInput();

    /**
     * The server calls this method just after creating an instance
     * of the mudlib user connection object that implements this
     * interface.  Normally, the socket would be passed as an
     * argument to the constructor.  Because the Server class does
     * not know the name of the user connection implementation class
     * at compile time, it has to use the Class.forName.newInstance()
     * construct, which means the default constructor must be used.
     * This method thus allows the server to pass the mudlib
     * implementation the socket to use for communication with the
     * client.
     * @exception java.io.IOException thrown if a problem creating I/O streams occurs
     * @param s the socket connected to the user's machine
     * @see java.lang.Class#forName
     * @see java.lang.Class#newInstance
     */
    public abstract void setSocket(java.net.Socket s) throws java.io.IOException;
}