/**
 * imaginary.net.InvalidNameException
 * Copyright (c) 1996 George Reese
 * An exception thrown for invalid user names.
 */

package com.planet_ink.coffee_mud.i3.net;

/**
 * This class is thrown whenever an attempt to create
 * a bad user name is made.<BR>
 * Created: 28 September 1996<BR>
 * Last modified: 28 September 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public class InvalidNameException extends Exception {
    /**
     * Constructs a new invalid name exception with
     * the specified reason.
     * @param reason the reason for the exception
     */
    public InvalidNameException(String reason) {
        super(reason);
    }
}