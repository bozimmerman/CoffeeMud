/**
 * imaginary.net.i3.Intermud
 * Copyright (c) 1996 George Reese
 * This source code may not be modified, copied,
 * redistributed, or used in any fashion without the
 * express written consent of George Reese.
 *
 * This is the TCP/IP interface to version 3 of the
 * Intermud network.
 */
package com.planet_ink.coffee_mud.i3.packets;
import com.planet_ink.coffee_mud.i3.packets.InvalidPacketException;
import com.planet_ink.coffee_mud.i3.net.Interactive;
import com.planet_ink.coffee_mud.i3.persist.*;
import com.planet_ink.coffee_mud.utils.*;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The Intermud class is the central focus of incoming
 * and outgoing Intermud 3 packets.  It creates the link
 * to the I3 router, handles reconnection, and routing
 * of packets to the mudlib.  The mudlib is responsible
 * for providing two specific objects to interface with
 * this object:
 * an implementation of imaginary.net.i3.ImudServices
 * an implementation of imaginary.persist.PersistentPeer
 * To start up the Intermud connection, call the class
 * method setup().
 * The class itself creates an instance of itself and
 * serves as a way to interface to the rest of the mudlib.
 * When the mudlib needs to send a packet, it sends it
 * through a class method which then routes it to the
 * proper instance of Intermud.
 * @author George Reese
 * @version 1.0
 * @see imaginary.net.i3.ImudServices
 * @see imaginary.persist.PersistentPeer
 */
public class Intermud implements Runnable, Persistent, Serializable {
    static private Intermud thread = null;

    /**
     * Sends a packet to the router.  The packet must
     * be a valid subclass of imaginary.net.i3.Packet.
     * This method will then route the packet to the
     * currently running Intermud instance.
     * @param p an instance of a subclass of imaginary.i3.net.Packet
     * @see imaginary.net.i3.Packet
     */
    static public void sendPacket(Packet p) {
		if(!isConnected()) return;
        thread.send(p);
    }

    /**
     * Creates the initial link to an I3 router.
     * It will handle subsequent reconnections as needed
     * for as long as the mud process is running.
     * @param imud an instance of the mudlib implementation of imaginary.net.i3.ImudServices
     * @param peer and instance of the mudlib implementation of imaginary.net.i3.IntermudPeer
     * @see imaginary.net.i3.ImudServices
     * @see imaginary.persist.PersistentPeer
     */
    static public void setup(ImudServices imud, PersistentPeer peer) {
        if( thread != null ) {
            return;
        }
        thread = new Intermud(imud, peer);
    }

    /**
     * Translates a user entered mud name into the mud's
     * canonical name.
     * @param mud the user entered mud name
     * @return the specified mud's canonical name
     */
    static public String translateName(String mud) {
		if(!isConnected()) return "";
        return thread.getMudNameFor(mud);
    }

    /**
     * Returns a String representing the local channel
     * name for the specified remote channel by
     * calling the ImudServices implementation of
     * getLocalChannel().
     * @param c the remote channel name
     * @return the local channel name for the specified remote channel name
     * @see imaginary.net.i3.ImudServices#getLocalChannel
     */
    static public String getLocalChannel(String c ) {
		if(!isConnected()) return "";
        return thread.intermud.getLocalChannel(c);
    }

    /**
     * Returns a String representing the remote channel
     * name for the specified local channel by
     * calling the ImudServices implementation of
     * getRemoteChannel().
     * @param c the local channel name
     * @return the remote channel name for the specified local channel name
     * @see imaginary.net.i3.ImudServices#getRemoteChannel
     */
    static public String getRemoteChannel(String c) {
		if(!isConnected()) return "";
        return thread.intermud.getRemoteChannel(c);
    }

    /**
     * Determines whether or not the specified mud is up.
     * You may pass user entered mud names, as this method
     * will take the time to convert to a canonical name.
     * @param mud the name of the mud being checked
     * @return true if the mud is currently up, false otherwise
     */
    static public boolean isUp(String mud) {
		if(!isConnected()) return false;
        Mud m = thread.getMud(mud);

        if( m == null ) {
            return false;
        }
        else {
            return (m.state == -1);
        }
    }

    private boolean             connected;
    private Socket              connection;
    private Thread              input_thread;
    private ImudServices        intermud;
    private int                 modified;
    private DataOutputStream    output;
    private PersistentPeer      peer;
    private SaveThread          save_thread;
	public boolean				shutdown=false;
    public DataInputStream input;
    public int                 attempts;
    public Hashtable           banned;
    public ChannelList         channels;
    public MudList             muds;
    public Vector              name_servers;
    public int                 password;

    private Intermud(ImudServices imud, PersistentPeer p) {
        super();
        intermud = imud;
        peer = p;
        peer.setPersistent(this);
        connected = false;
        password = -1;
        attempts = 0;
        input_thread = null;
        channels = new ChannelList(-1);
        muds = new MudList(-1);
        banned = new Hashtable();
        name_servers = new Vector();
        name_servers.addElement(new NameServer("us-1.i3.intermud.org", 9000, "*gjs"));
        modified = Persistent.UNMODIFIED;
        try {
            restore();
        }
        catch( PersistenceException e ) {
            password = -1;
            channels = new ChannelList(-1);
            muds = new MudList(-1);
			Log.errOut("Intermud",e);
        }
        save_thread = new SaveThread(this);
        save_thread.start();
        connect();
    }

    // Handles an incoming channel list packet
    private synchronized void channelList(Vector packet) {
        Hashtable list = (Hashtable)packet.elementAt(7);
        Enumeration keys = list.keys();

        synchronized( channels ) {
            channels.setChannelListId(((Integer)packet.elementAt(6)).intValue());
            while( keys.hasMoreElements() ) {
                Channel c = new Channel();
                Object ob;

                c.channel = (String)keys.nextElement();
                ob = list.get(c.channel);
                if( ob instanceof Integer ) {
                    removeChannel(c);
                }
                else {
                    Vector info = (Vector)ob;

                    c.owner = (String)info.elementAt(0);
                    c.type = ((Integer)info.elementAt(1)).intValue();
                    addChannel(c);
                }
            }
        }
        modified = Persistent.MODIFIED;
    }

	public static NameServer getNameServer()
	{
		if(thread==null) return null;
		if(thread.name_servers==null) return null;
		if(thread.name_servers.size()==0) return null;
        return (NameServer)thread.name_servers.elementAt(0);
	}

    private synchronized void connect() {
		if(shutdown) return;
        attempts++;
        try {
            NameServer n = (NameServer)name_servers.elementAt(0);

            connection = new Socket(n.ip, n.port);
            output = new DataOutputStream(connection.getOutputStream());
            send("({\"startup-req-3\",5,\"" + intermud.getMudName() + "\",0,\"" +
                 n.name + "\",0," + password +
                 "," + muds.getMudListId() + "," + channels.getChannelListId() + "," + intermud.getMudPort() +
                 ",0,0,\""+intermud.getMudVersion()+"\",\""+intermud.getMudVersion()+"\",\""+intermud.getMudVersion()+"\",\"CoffeeMud\"," +
                 "\""+intermud.getMudState()+"\",\"bo@zimmers.net\",([" +
                 "\"who\":1,\"finger\":1,\"channel\":1,\"tell\":1,\"locate\":1,]),([]),})");
			/* for my amusement
			({"startup-req-3",5,"TESTMUD",0,"TESTMUD",0,*gjs,0,0,27766,0,0,","3","3",
			"CoffeeMud","OK","bo@zimmers.net",(["who":1,]),([]),})
			({"channel-listen",5,"diku_chat",0,"TESTMUD",0,"diku_chat",1,})
			*/
            connected = true;
            input_thread = new Thread(this);
            input_thread.setName("Intermud");
            input_thread.start();
            {
                Enumeration e = intermud.getChannels();

                while( e.hasMoreElements() ) {
                    String chan = (String)e.nextElement();

					send("({\"channel-listen\",5,\"" + intermud.getMudName() + "\",0,\"" +
                         n.name + "\",0,\"" + chan + "\",1,})");
                }
            }
        }
        catch( Exception e ) {
            try { Thread.sleep(attempts * 100); }
            catch( InterruptedException ignore )
			{
				if(shutdown)
				{
					Log.sysOut("Intermud","Shutdown!");
					return;
				}
			}
            connect();
        }
    }

    // Handles an incoming error packet
    private synchronized void error(Vector packet) {
        Object target = packet.elementAt(5);
        String msg = (String)packet.elementAt(7);

        if( target instanceof Integer ) {
            I3Exception e;

            e = new I3Exception(msg);
			if(e.getMessage()!=null)
				Log.errOut("InterMud","276-"+e.getMessage());
        }
        else {
        }
    }

    private synchronized void mudlist(Vector packet) {
        Hashtable list;
        Enumeration keys;

        synchronized( muds ) {
            muds.setMudListId(((Integer)packet.elementAt(6)).intValue());
            list = (Hashtable)packet.elementAt(7);
            keys = list.keys();
            while( keys.hasMoreElements() ) {
                Mud mud = new Mud();
                Object info;

                mud.mud_name = (String)keys.nextElement();
                info = list.get(mud.mud_name);
                if( info instanceof Integer ) {
                    removeMud(mud);
                }
                else {
                    Vector v = (Vector)info;
					int total=0;
					for(int vi=0;vi<v.size();vi++)
						if(v.elementAt(vi) instanceof String)
							total+=((String)v.elementAt(vi)).length();
					if(total<1024)
					{
						mud.state = ((Integer)v.elementAt(0)).intValue();
						mud.address = (String)v.elementAt(1);
						mud.player_port = ((Integer)v.elementAt(2)).intValue();
						mud.tcp_port = ((Integer)v.elementAt(3)).intValue();
						mud.udp_port = ((Integer)v.elementAt(4)).intValue();
						mud.mudlib = (String)v.elementAt(5);
						mud.base_mudlib = (String)v.elementAt(6);
						mud.driver = (String)v.elementAt(7);
						mud.mud_type = (String)v.elementAt(8);
						mud.status = (String)v.elementAt(9);
						mud.admin_email = (String)v.elementAt(10);
						addMud(mud);
					}
                }
            }
        }
    }
             // Hashtable services = (Hashtable)v.elementAt(11);
             // Hashtable other_info = (Hashtable)v.elementAt(12);

    public void restore() throws PersistenceException {
        if( modified != Persistent.UNMODIFIED ) {
            throw new PersistenceException("Restoring over changed data.");
        }
        peer.restore();
        modified = Persistent.UNMODIFIED;
    }

	public static boolean isConnected()
	{
		if(thread==null) return false;
		return thread.connected;
	}

    public void run() {

        try {
            input = new DataInputStream(connection.getInputStream());
        }
        catch( java.io.IOException e ) {
            input = null;
            connected = false;
        }
        while( connected ) {
            Vector data;

            try { Thread.sleep(100); }
            catch( InterruptedException e )
			{
				if(shutdown)
				{
					Log.sysOut("Intermud","Shutdown!!");
					return;
				}
			}
            // Read a packet from the router
            {
                String str;

                try {
                    int len = input.readInt();
					if(len>65536)
					{
						int skipped=0;
						while(skipped<len)
							skipped+=input.skipBytes(len);
						Log.errOut("Got illegal packet: "+skipped+"/"+len+" bytes.");
						continue;
					}
                    byte[] tmp = new byte[len];

                    input.readFully(tmp);
					str=new String(tmp);
                }
                catch( java.io.IOException e ) {
                    data = null;
                    str = null;
                    connected = false;
                    try { Thread.sleep(1200); }
					catch (InterruptedException ee)
					{
						if(shutdown)
						{
							Log.sysOut("Intermud","Shutdown!!!");
							return;
						}
					}
                    connect();
					if(e.getMessage()!=null)
						Log.errOut("InterMud","384-"+e.getMessage());
                    return;
                }
                try {
                    data = (Vector)LPCData.getLPCData(str);
                }
                catch( I3Exception e ) {
					if(e.getMessage()!=null)
						Log.errOut("InterMud","389-"+e.getMessage());
                    continue;
                }
            }
            // Figure out the packet type and send it to the mudlib
            {
                String type = (String)data.elementAt(0);

                if( type.equals("channel-m") || type.equals("channel-e") || type.equals("channel-t") ) {
                    try {
                        ChannelPacket p = new ChannelPacket(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","0-"+e.getMessage());
                    }
                }
                else if( type.equals("chan-who-req") ) {
                    try {
                        ChannelWhoRequest p = new ChannelWhoRequest(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","1-"+e.getMessage());
                    }
                }
                else if( type.equals("channel-add") ) {
                    try {
                        ChannelAdd p = new ChannelAdd(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","2-"+e.getMessage());
                    }
                }
                else if( type.equals("channel-remove") ) {
                    try {
                        ChannelDelete p = new ChannelDelete(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","3-"+e.getMessage());
                    }
                }
                else if( type.equals("channel-listen") ) {
                    try {
                        ChannelListen p = new ChannelListen(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","4-"+e.getMessage());
                    }
                }
                else if( type.equals("chan-who-reply") ) {
                    try {
                        ChannelWhoReply p = new ChannelWhoReply(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","5-"+e.getMessage());
                    }
                }
                else if( type.equals("chanlist-reply") ) {
                    channelList(data);
                }
                else if( type.equals("locate-reply") ) {
                    try {
                        LocateReplyPacket p = new LocateReplyPacket(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","6-"+e.getMessage());
                    }
                }
                else if( type.equals("locate-req") ) {
                    try {
                        LocateQueryPacket p = new LocateQueryPacket(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","7-"+e.getMessage());
                    }
                }
                else if( type.equals("mudlist") ) {
                    mudlist(data);
                }
                else if( type.equals("startup-reply") ) {
                    startupReply(data);
                }
                else if( type.equals("tell") ) {
                    try {
                        TellPacket p = new TellPacket(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","8-"+e.getMessage());
                    }
                }
                else if( type.equals("who-req") ) {
                    try {
                        WhoPacket p = new WhoPacket(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","9-"+e.getMessage());
                    }
				}
                else if( type.equals("who-reply") ) {
                    try {
                        WhoPacket p = new WhoPacket(data);

                        intermud.receive(p);
                    }
                    catch( InvalidPacketException e ) {
						Log.errOut("Intermud","10-"+e.getMessage());
                    }
                }
                else if( type.equals("error") ) {
                    error(data);
                }
                else if( type.equals("ucache-update") ) {
                    // i have NO idea what to do here
				}
                else {
                    Log.errOut("Intermud","Other packet: " + type);
                }
            }
        }
    }

    public void save() throws PersistenceException {
        if( modified == Persistent.UNMODIFIED ) {
            return;
        }
        peer.save();
        modified = Persistent.UNMODIFIED;
    }

    /**
     * Sends any valid subclass of Packet to the router.
     * @param p the packet to send
     */
    public void send(Packet p) {
        send(p.toString());
    }

    // Send a formatted mud mode packet to the router
    private void send(String str) {
        int x = str.length();

        try {
            output.writeInt(x);
            output.writeBytes(str);
        }
        catch( java.io.IOException e ) {
			if(e.getMessage()!=null)
				Log.errOut("InterMud","557-"+e.getMessage());
        }
    }

    // Handle a startup reply packet
    private synchronized void startupReply(Vector packet) {
        Vector router_list = (Vector)packet.elementAt(6);

        if( router_list != null ) {
            Vector router = (Vector)router_list.elementAt(0);
            NameServer name_server = (NameServer)name_servers.elementAt(0);

            if( !name_server.name.equals(router.elementAt(0)) ) {
                // create new name server and connect
                return;
            }
        }
        password = ((Integer)packet.elementAt(7)).intValue();
        modified = Persistent.MODIFIED;
    }

    /**
     * Shuts down the connection to the router without
     * reconnecting.
     * @see java.lang.Runnable#stop
     */
    public void stop()
	{
		connected = false;
		shutdown=true;
		try { if(input!=null) input.close();}catch(Exception e){}
		try { if(connection!=null) connection.close();}catch(Exception e){}
		try { if(input_thread!=null) input_thread.interrupt(); }catch(Exception e){}
		input_thread=null;
		if(save_thread!=null)
		{
			try { save_thread.close();}catch(Exception e){}
			try { save_thread.interrupt();}catch(Exception e){}
		}
		save_thread=null;
        try { save(); }
        catch( PersistenceException e ) { }
		shutdown=false;
    }



    /**
     * Adds a channel to the channel list.
     * This does not subscribe the mud to that channel.
     * In order to subscribe, the channel needs to be
     * added to the ImudServices implementation's getChannels()
     * method.
     * @param c the channel to add to the list of known channels
     * @see imaginary.net.i3.ImudServices#getChannels
     */
    public void addChannel(Channel c) {
        channels.addChannel(c);
    }

    /**
     * Removes a channel from the channel list.
     * @param c the channel to remove
     */
    public void removeChannel(Channel c) {
        channels.removeChannel(c);
    }

    /**
     * @return the list of currently known channels
     */
    public ChannelList getChannelList() {
        return channels;
    }

    /**
     * Sets the channel list to a new channel list.
     * @param list the new channel list
     */
    public void setChannelList(ChannelList list) {
        channels = list;
    }

    /**
     * Adds a mud to the list of known muds.
     * @param m the mud to add
     */
    public void addMud(Mud m) {
        muds.addMud(m);
        modified = Persistent.MODIFIED;
    }

    private Mud getMud(String mud_name) {
        return muds.getMud(getMudNameFor(mud_name));
    }

    /**
     * Removed a mud from the list of known muds.
     * @param m the mud to remove
     */
    public void removeMud(Mud m) {
        muds.removeMud(m);
        modified = Persistent.MODIFIED;
    }

    /**
     * @return the list of known muds
     */
    public MudList getMudList() {
        return muds;
    }

    /**
     * @return the list of known muds
     */
    public static MudList getAllMudsList() {
		if(!isConnected()) return new MudList(-1);
        return thread.muds;
    }
    /**
     * @return the list of known muds
     */
    public static ChannelList getAllChannelList() {
		if(!isConnected()) return new ChannelList();
        return thread.channels;
    }
    /**
     * Sets the list of known muds to the specified list.
     * @param list the new list of muds
     */
    public void setMudList(MudList list) {
        muds = list;
    }

    private String getMudNameFor(String mud)
	{
        Enumeration list = muds.getMuds().keys();
        mud = mud.toLowerCase().replace('.', ' ');
        while( list.hasMoreElements() ) {
            String str = (String)list.nextElement();

            if( mud.equalsIgnoreCase(str) ) {
                return str;
            }
        }
        list = muds.getMuds().keys();
        while( list.hasMoreElements() ) {
            String str = (String)list.nextElement();
            if( EnglishParser.containsString(str,mud) ) {
                return str;
            }
        }
        return mud;
    }

    /**
     * @return the I3 password for this mud
     */
     public int getPassword() {
        return password;
     }

    /**
     * Sets the Intermud 3 password.
     * @param pass the new password
     */
    public void setPassword(int pass) {
        password = pass;
    }

	public static void shutdown()
	{
		if(thread!=null)
			thread.stop();
		thread=null;
	}

}

class SaveThread extends Thread {
    private Intermud intermud;
	private boolean closed=false;

    public SaveThread(Intermud imud) {
        super("Intermud save");
		setName("Intermud save");
        intermud = imud;
    }

	public void close()
	{ closed=true; }

    public void run() {
        while( !closed ) {
            try {
                Thread.sleep(120000);
                if(!closed) intermud.save();
            }
			catch (InterruptedException e)
			{
				Log.sysOut("Intermud","Save Thread Shutdown!");
				return;
			}
            catch( PersistenceException e ) {

            }
        }
    }
}
