/**
 * imaginary.net.i3.TellPacket
 * Copyright (c) 1996 George Reese
 * The I3 tell packet
 */

package com.planet_ink.coffee_mud.system.I3.packets;
import com.planet_ink.coffee_mud.system.I3.net.Interactive;
import com.planet_ink.coffee_mud.system.I3.server.Server;

import java.util.Vector;

/**
 * This extension of the Packet class handles incoming and
 * outgoing intermud tells.  To use it in a services object,
 * simply grab the data out of its public members.  To use
 * it to send a tell, set all of its public data members
 * and call the send() method.<BR>
 * Created: 22 Spetember 1996<BR>
 * Last modified: 29 September 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public class TellPacket extends Packet {
	
    /**
     * The display name for the person sending the tell.
     */
    public String sender_visible_name = null;
    /**
     * The actual message being sent.
     */
    public String message = null;

	public TellPacket()
	{
		super();
        type = Packet.TELL;
	}
	
    /**
     * Constructs a tell package based on an I3 mud mode vector.
     * @exception imaginary.net.i3.InvalidPacketException thrown if the incoming packet is bad
     * @param v the I3 mud mode vector containing the incoming tell
     */
    public TellPacket(Vector v) throws InvalidPacketException {
        super(v);
        try {
            type = Packet.TELL;
            sender_visible_name = (String)v.elementAt(6);
            message = (String)v.elementAt(7);
        }
        catch( ClassCastException e ) {
            throw new InvalidPacketException();
        }
    }

    /**
     * Constructs an outgoing tell.
     * @param u the interactive sending the tell
     * @param who the person whom they are sending the tell to
     * @param mud the mud the target is on
     * @param msg the message being sent
     */
    public TellPacket(Interactive u, String who, String mud, String msg) {
        super();
        type = Packet.TELL;
        sender_name = u.getKeyName();
        target_mud = mud;
        target_name = who;
        sender_visible_name = u.getDisplayName();
        message = msg;
    }

    /**
     * Sends a properly constructed outgoing tell to its target.
     * @exception imaginary.net.i3.InvalidPacketException thrown if this packet was not properly constructed
     * @see imaginary.net.i3.Packet#send
     */
    public void send() throws InvalidPacketException {
        if( message == null || sender_visible_name == null ) {
            throw new InvalidPacketException();
        }
        message = convertString(message);
        super.send();
    }

    /**
     * This method is used by the I3 system to turn the packet
     * into a mud mode string.  To see the proper format for
     * an I3 tell, see the <A HREF="http://www.imaginary.com/intermud/intermud3.html">
     * Intermud 3</A> documentation.
     * @return the mud mode string for this packet
     */
    public String toString() {
        return "({\"tell\",5,\"" + Server.getMudName() +
               "\",\"" + sender_name + "\",\"" + target_mud +
               "\",\"" + target_name + "\",\"" +
               sender_visible_name + "\",\"" + message + "\",})";
    }
}
