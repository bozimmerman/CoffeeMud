package com.planet_ink.coffee_mud.system.I3;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.system.I3.packets.ImudServices;
import com.planet_ink.coffee_mud.system.I3.packets.Packet;

public class IMudInterface implements ImudServices
{
	
	public String version="CoffeeMud 3.0";
	public String name="CoffeeMud";
	public int port=4444;
	public String[][] channels={{"diku_chat","CHAT"},
									   {"diku_immortals","GOSSIP"},
									   {"diku_code","ANSWER"}};
														
	
	public IMudInterface (String Name, String Version, int Port, String[][] Channels)
	{
		if(Name!=null) name=Name;
		if(Version!=null) version=Version;
		if(Channels!=null) channels=Channels;
	}
	
	/**
     * Handles an incoming I3 packet asynchronously.
     * An implementation should make sure that asynchronously
     * processing the incoming packet will not have any
     * impact, otherwise you could end up with bizarre
     * behaviour like an intermud chat line appearing
     * in the middle of a room description.  If your
     * mudlib is not prepared to handle multiple threads,
     * just stack up incoming packets and pull them off
     * the stack during your main thread of execution.
     * @param packet the incoming packet
     */
	public void receive(Packet packet)
	{
		System.out.println("imi/"+packet.type);
	}

    /**
     * @return an enumeration of channels this mud subscribes to
     */
	public java.util.Enumeration getChannels()
	{
		Vector V=new Vector();
		for(int i=0;i<channels.length;i++)
			V.addElement(channels[i][0]);
		return V.elements();
	}

    /**
     * Given a I3 channel name, this method should provide
     * the local name for that channel.
     * Example:
     * <PRE>
     * if( str.equals("imud_code") ) return "intercre";
     * </PRE>
     * @param str the remote name of the desired channel
     * @return the local channel name for a remote channel
     * @see #getRemoteChannel
     */
    public String getLocalChannel(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][0].equalsIgnoreCase(str))
				return channels[i][1];
		return "";
	}

    /**
     * @return the name of this mud
     */
    public String getMudName(){
		return name;
	}

    /**
     * @return the software name and version
     */
    public String getMudVersion()
	{
		return version;
	}
	
    /**
     * @return the player port for this mud
     */
    public int getMudPort(){
		return port;
	}

    /**
     * Given a local channel name, returns the remote
     * channel name.
     * Example:
     * <PRE>
     * if( str.equals("intercre") ) return "imud_code";
     * </PRE>
     * @param str the local name of the desired channel
     * @return the remote name of the specified local channel
     */
    public String getRemoteChannel(String str){
		for(int i=0;i<channels.length;i++)
			if(channels[i][1].equalsIgnoreCase(str))
				return channels[i][0];
		return "";
	}
}
