package com.planet_ink.coffee_mud.i3.packets;

public class I3Exception extends Exception {
    public I3Exception() {
        this("Unidentified exception.");
    }

    public I3Exception(String str) {
        super(str);
    }
}