package com.planet_ink.coffee_mud.i3.packets;

public class InvalidPacketException extends I3Exception {
    public InvalidPacketException() {
        super("Invalid packet.");
    }
}