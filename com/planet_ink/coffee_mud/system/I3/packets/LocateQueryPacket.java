package com.planet_ink.coffee_mud.system.I3.packets;

import com.planet_ink.coffee_mud.system.I3.server.Server;
import java.util.Vector;

public class LocateQueryPacket extends Packet {
    public String user_name;

    public LocateQueryPacket(Vector v) throws InvalidPacketException {
        super(v);
        try {
            type = Packet.LOCATE_QUERY;
            user_name = (String)v.elementAt(6);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

    public LocateQueryPacket(String nom, String who) {
        super();
        type = Packet.LOCATE_QUERY;
        sender_name = nom;
        user_name = who;
    }

    public void send() throws InvalidPacketException {
        if( sender_name == null || user_name == null ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
        return "({\"locate-req\",5,\"" + Server.getMudName() +
               "\", \"" + sender_name + "\",0,0,\"" +
               user_name + "\",})";
    }
}
