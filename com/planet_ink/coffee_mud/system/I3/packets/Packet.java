package com.planet_ink.coffee_mud.system.I3.packets;

import com.planet_ink.coffee_mud.system.I3.server.Server;
import java.util.Vector;

public class Packet {
	/*
		Transmissions are LPC arrays with a predefined set of six initial elements: 
	({ type, ttl, originator mudname, originator username, target mudname, target username, ... }). 
	*/
    final static public int CHAN_MESSAGE = 1;
    final static public int CHAN_EMOTE   = 2;
    final static public int CHAN_TARGET  = 3;
    final static public int WHO_REQUEST  = 4;
    final static public int WHO_REPLY    = 5;
    final static public int TELL         = 6;
    final static public int LOCATE_QUERY = 7;
    final static public int LOCATE_REPLY = 8;

    public String sender_mud = null;
    public String sender_name = null;
    public String target_mud = null;
    public String target_name = null;
    public int    type = 0;

    public Packet() {
        super();
        sender_mud = Server.getMudName();
    }

    public Packet(Vector v) {
        super();
        {
            Object ob;

            ob = v.elementAt(2);
            if( ob instanceof String ) {
                sender_mud = (String)ob;
            }
            ob = v.elementAt(3);
            if( ob instanceof String ) {
                sender_name = (String)ob;
            }
            ob = v.elementAt(4);
            if( ob instanceof String ) {
                target_mud = (String)ob;
            }
            ob = v.elementAt(5);
            if( ob instanceof String ) {
                target_name = (String)ob;
            }
        }
    }

    public String convertString(String str) {
        StringBuffer b = new StringBuffer(str);
        int i = 0;

        while( i < b.length() ) {
            char c = b.charAt(i);

            if( c != '\\' && c != '"' ) {
                i++;
            }
            else {
                b.insert(i, '\\');
                i += 2;
            }
        }
        return new String(b);
    }

    public void send() throws InvalidPacketException {
        if( type == 0 ) {
            throw new InvalidPacketException();
        }
        Intermud.sendPacket(this);
    }
}
