package com.planet_ink.coffee_mud.system.I3.packets;

public class NameServer {
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