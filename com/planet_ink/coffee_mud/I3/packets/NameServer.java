package com.planet_ink.coffee_mud.i3.packets;
import java.io.Serializable;

public class NameServer implements Serializable {
    protected String ip;
    protected String name;
    protected int    port;

    public NameServer(String addr, int p, String nom) {
        super();
        ip = addr;
        port = p;
        name = nom;
    }
}