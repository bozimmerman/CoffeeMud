/**
 * imaginary.server.ServerSecurityException
 * Copyright (c) 1996 George Reese
 * An exception for attempts to violate server security
 */

package com.planet_ink.coffee_mud.i3.server;


/**
 * This exception gets thrown by the server when some
 * class tries to perform an operation it should not
 * be allowed to perform.
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public class ServerSecurityException extends Exception {
    /**
     * Constructs a new security excetption with a generic
     * message.
     */
    public ServerSecurityException() {
        this("A general security exception occurred.");
    }

    /**
     * Constructs a new security exception with the
     * specified error message,
     * @param err the error message
     */
    public ServerSecurityException(String err) {
        super(err);
    }
}
