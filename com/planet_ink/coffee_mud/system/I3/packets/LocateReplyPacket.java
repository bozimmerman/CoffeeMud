package com.planet_ink.coffee_mud.system.I3.packets;
import com.planet_ink.coffee_mud.system.I3.server.Server;

import java.util.Vector;

public class LocateReplyPacket extends Packet {
    public String located_mud_name;
    public String located_visible_name;
    public int    idle_time;
    public String status;

    public LocateReplyPacket(Vector v) throws InvalidPacketException {
        super(v);
        try {
            type = Packet.LOCATE_REPLY;
            located_mud_name = (String)v.elementAt(6);
            located_visible_name = (String)v.elementAt(7);
			try {
            idle_time = ((Integer)v.elementAt(8)).intValue();
			}
			catch( ClassCastException e ) {
				idle_time=-1;
			}
			try {
            status = (String)v.elementAt(9);
			}
			catch( ClassCastException e ) {
				status="unknown";
			}
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

    public LocateReplyPacket(String to_whom, String mud, String who, int idl, String stat) {
        super();
        type = Packet.LOCATE_REPLY;
        target_mud = mud;
        target_name = to_whom;
        located_mud_name = Server.getMudName();
        located_visible_name = who;
        idle_time = idl;
        status = stat;
    }

    public void send() throws InvalidPacketException {
        if( target_name == null || located_mud_name == null ||
            located_visible_name == null || status == null ) {
            throw new InvalidPacketException();
        }
        super.send();
    }

    public String toString() {
        return "({\"locate-reply\",5,\"" + Server.getMudName() +
               "\",0,\"" + target_mud + "\",\"" + target_name +
               "\",\"" + located_mud_name + "\",\"" +
               located_visible_name + "\"," + idle_time + ",\"" +
               status + "\",})";
    }
}
