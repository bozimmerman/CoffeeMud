package com.planet_ink.coffee_mud.Libraries.intermud.i3;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.IntermudInterface.InterProto;
import com.planet_ink.coffee_mud.Libraries.intermud.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.entities.I3Mud;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.net.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.server.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.Serializable;

/*
   Copyright 2003-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * This is the CoffeeMud implementation of the ImudServices interface. It
 * provides all the mudlib specific functionality needed by the
 * Intermud 3 System.
 *
 * @author Bo Zimmerman
 *
 */
public class CoffeeMudI3Bridge implements ImudServices, Serializable
{
	public static final long serialVersionUID=0;

	public String			version		= "CoffeeMud vX.X";
	public String			name		= "CoffeeMud";
	public String			i3state		= "Development";
	public Room				universalR	= null;
	public int				mudPort		= 5555;
	public List<CMChannel>	channels	= new XVector<CMChannel>();
	public Map<String,Long>	inkeys		= new LimitedTreeMap<String,Long>(1200000,1000,true);
	public Map<String,Long>	outkeys		= new LimitedTreeMap<String,Long>(1200000,1000,true);

	private volatile long lastPacketReceivedTime = System.currentTimeMillis();

	String[][] i3ansi_conversion=
	{
		/*
		 * Conversion Format Below:
		 *
		 * { "&lt;MUD TRANSLATION&gt;", "PINKFISH", "ANSI TRANSLATION" }
		 *
		 * Foreground Standard Colors
		 */
		{ "^K", "%^BLACK%^",   "\033[0;0;30m" }, // Black
		{ "^R", "%^RED%^",     "\033[0;0;31m" }, // Dark Red
		{ "^G", "%^GREEN%^",   "\033[0;0;32m" }, // Dark Green
		{ "^Y", "%^ORANGE%^",  "\033[0;0;33m" }, // Orange/Brown
		{ "^B", "%^BLUE%^",    "\033[0;0;34m" }, // Dark Blue
		{ "^P", "%^MAGENTA%^", "\033[0;0;35m" }, // Purple/Magenta
		{ "^C", "%^CYAN%^",    "\033[0;0;36m" }, // Cyan
		{ "^W", "%^WHITE%^",   "\033[0;0;37m" }, // Grey

		/* Background colors */
		{ "", "%^B_BLACK%^",   "\033[40m" }, // Black
		{ "", "%^B_RED%^",     "\033[41m" }, // Red
		{ "", "%^B_GREEN%^",   "\033[42m" }, // Green
		{ "", "%^B_ORANGE%^",  "\033[43m" }, // Orange
		{ "", "%^B_YELLOW%^",  "\033[43m" }, // Yellow, which may as well be orange since ANSI doesn't do that
		{ "", "%^B_BLUE%^",    "\033[44m" }, // Blue
		{ "", "%^B_MAGENTA%^", "\033[45m" }, // Purple/Magenta
		{ "", "%^B_CYAN%^",    "\033[46m" }, // Cyan
		{ "", "%^B_WHITE%^",   "\033[47m" }, // White

		/* Text Affects */
		{ "^.", "%^RESET%^",	 "\033[0m" }, // Reset Text
		{ "^.", "%^RESET%^",	 "\033[0m" }, // Reset Text
		{ "^H", "%^BOLD%^", 	 "\033[1m" }, // Bolden Text(Brightens it)
		{ "^.", "%^EBOLD%^",	 "\033[0m" }, // Assumed to be a reset tag to stop bold
		{ "^_", "%^UNDERLINE%^", "\033[4m" }, // Underline Text
		{ "^*", "%^FLASH%^",	 "\033[5m" }, // Blink Text
		{ "^/", "%^ITALIC%^",    "\033[6m" }, // Italic Text
		{ "", "%^REVERSE%^",   "\033[7m" }, // Reverse Background and Foreground Colors

		/* Foreground extended colors */
		{ "^k", "%^BLACK%^%^BOLD%^",   "\033[0;1;30m" }, // Dark Grey
		{ "^r", "%^RED%^%^BOLD%^",     "\033[0;1;31m" }, // Red
		{ "^g", "%^GREEN%^%^BOLD%^",   "\033[0;1;32m" }, // Green
		{ "^y", "%^YELLOW%^",   	   "\033[0;1;33m" }, // Yellow
		{ "^b", "%^BLUE%^%^BOLD%^",    "\033[0;1;34m" }, // Blue
		{ "^p", "%^MAGENTA%^%^BOLD%^", "\033[0;1;35m" }, // Pink
		{ "^c", "%^CYAN%^%^BOLD%^",    "\033[0;1;36m" }, // Light Blue
		{ "^w", "%^WHITE%^%^BOLD%^",   "\033[0;1;37m" }, // White

		/* Blinking foreground standard color */
		{ "^K^*", "%^BLACK%^%^FLASH%^", 		  "\033[0;5;30m" }, // Black
		{ "^R^*", "%^RED%^%^FLASH%^",   		  "\033[0;5;31m" }, // Dark Red
		{ "^G^*", "%^GREEN%^%^FLASH%^", 		  "\033[0;5;32m" }, // Dark Green
		{ "^Y^*", "%^ORANGE%^%^FLASH%^",		  "\033[0;5;33m" }, // Orange/Brown
		{ "^B^*", "%^BLUE%^%^FLASH%^",  		  "\033[0;5;34m" }, // Dark Blue
		{ "^P^*", "%^MAGENTA%^%^FLASH%^",   	  "\033[0;5;35m" }, // Magenta/Purple
		{ "^C^*", "%^CYAN%^%^FLASH%^",  		  "\033[0;5;36m" }, // Cyan
		{ "^W^*", "%^WHITE%^%^FLASH%^", 		  "\033[0;5;37m" }, // Grey
		{ "^k^*", "%^BLACK%^%^BOLD%^%^FLASH%^",   "\033[1;5;30m" }, // Dark Grey
		{ "^r^*", "%^RED%^%^BOLD%^%^FLASH%^",     "\033[1;5;31m" }, // Red
		{ "^g^*", "%^GREEN%^%^BOLD%^%^FLASH%^",   "\033[1;5;32m" }, // Green
		{ "^y^*", "%^YELLOW%^%^FLASH%^",		  "\033[1;5;33m" }, // Yellow
		{ "^b^*", "%^BLUE%^%^BOLD%^%^FLASH%^",    "\033[1;5;34m" }, // Blue
		{ "^p^*", "%^MAGENTA%^%^BOLD%^%^FLASH%^", "\033[1;5;35m" }, // Pink
		{ "^c^*", "%^CYAN%^%^BOLD%^%^FLASH%^",    "\033[1;5;36m" }, // Light Blue
		{ "^w^*", "%^WHITE%^%^BOLD%^%^FLASH%^",   "\033[1;5;37m" }  // White
	};

	/**
	 * Constructor
	 *
	 * @param name the mud name to register as
	 * @param version the mud version to register as
	 * @param publicMudPort the public port the mud is running on
	 * @param channels the list of local channels to register with i3
	 * @param i3Port the local port to run the i3 server on
	 * @param routersList the list of routers to connect to
	 * @param adminEmail the admin email address for this mud
	 * @param smtpPort the smtp port to use for this mud
	 */
	public CoffeeMudI3Bridge(final String name,
							 final String version,
							 final int publicMudPort,
							 final List<CMChannel> channels,
							 final int i3Port,
							 final String[] routersList,
							 final String adminEmail,
							 final int smtpPort
							 )
	{
		if(name!=null)
			this.name=name;
		this.i3state=getPlayState();
		if(version!=null)
			this.version=version;
		if(channels!=null)
			this.channels=channels;
		else
		if(this.channels.size()==0)
		{
			final Map<InterProto,String> imudChans = new HashMap<InterProto,String>();
			imudChans.put(InterProto.I3,"diku_chat");
			this.channels.add(CMLib.channels().createNewChannel("I3CHAT", imudChans, "", new HashSet<ChannelFlag>(),"",""));
			imudChans.put(InterProto.I3,"diku_immortals");
			this.channels.add(CMLib.channels().createNewChannel("I3GOSSIP", imudChans, "", new HashSet<ChannelFlag>(),"",""));
			imudChans.put(InterProto.I3,"diku_code");
			this.channels.add(CMLib.channels().createNewChannel("GREET", imudChans, "", new HashSet<ChannelFlag>(),"",""));
		}
		this.mudPort=publicMudPort;
		try
		{
			I3Server.start(name,i3Port,this,routersList,adminEmail,smtpPort);
		}
		catch(final ServerSecurityException e)
		{}
	}

	/**
	 * Shuts down the I3 server
	 */
	public void shutdown()
	{
		try
		{
			I3Server.shutdown();
		}
		catch (final Throwable ex)
		{
			Log.errOut(ex);
		}
	}

	/**
	 * Returns whether or not the I3 server is currently online
	 * @return true if the I3 server is currently online
	 */
	public boolean isOnline()
	{
		return I3Client.isConnected() && (!CMSecurity.isDisabled(DisFlag.I3));
	}

	/**
	 * Returns the public availability state of this mud
	 * @return the public availability state of this mud
	 */
	private String getPlayState()
	{
		String playState=CMProps.instance().getStr("MUDSTATE");
		if((playState==null) || (playState.length()==0))
			playState=CMProps.instance().getStr("I3STATE");
		if((playState==null) || (!CMath.isInteger(playState)))
			playState=L("Development");
		else
		switch(CMath.s_int(playState.trim()))
		{
		case 0:
			playState = L("MudLib Development");
			break;
		case 1:
			playState = L("Restricted Access");
			break;
		case 2:
			playState = L("Beta Testing");
			break;
		case 3:
			playState = L("Open to the public");
			break;
		default:
			playState = L("MudLib Development");
			break;
		}
		return playState;
	}

	/**
	 * Localized static text translation
	 * @param str the string to translate
	 * @param xs the variables to insert into the translated string
	 * @return the translated string
	 */
	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
	}

	/**
	 * Sets the local channels map
	 * @param channels the map to set
	 */
	public void setChannelsMap(final List<CMChannel> channels)
	{
		this.channels=channels;
	}

	/**
	 * Given a mob name, this method tries to find a matching online mob.
	 * @param mobName the name of the mob to find
	 * @return the matching online mob, or null if not found
	 */
	protected MOB findSessMob(final String mobName)
	{
		return CMLib.sessions().findCharacterOnline(mobName, true);
	}

	/**
	 * Returns a universal room for use as a location for incoming I3 tells and
	 * the like.
	 *
	 * @return a universal room for use as a location for incoming I3 tells and
	 *         the like
	 */
	protected Room getUniversalRoom()
	{
		if(universalR==null)
		{
			universalR=CMClass.getLocale("StdRoom");
		}
		return universalR;
	}

	/**
	 * Fixes a color string coming in from I3 to use CoffeeMud's
	 * standard color string conventions.
	 *
	 * @param str the string to fix
	 * @return the fixed string
	 */
	public String fixColors(final String str)
	{
		final StringBuffer buf=new StringBuffer(str);
		int startedAt=-1;
		for(int i=0;i<buf.length();i++)
		{
			if(buf.charAt(i)=='%')
			{
				if(startedAt<0)
					startedAt=i;
				else
				if(((i+1)<buf.length())&&(buf.charAt(i+1)=='^'))
				{
					String found=null;
					final String code=buf.substring(startedAt,i+2);
					for (final String[] element : i3ansi_conversion)
					{
						if(code.equals(element[1]))
						{
							found=element[0];
							break;
						}
					}
					if(found!=null)
					{
						buf.replace(startedAt,i+2,found);
						i=startedAt+1;
					}
					startedAt=-1;
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Fixes a social string coming in from I3 to use CoffeeMud's
	 * standard social string conventions.
	 *
	 * @param str the string to fix
	 * @return the fixed string
	 */
	public String socialFixIn(String str)
	{

		str=CMStrings.replaceAll(str,"$N","<S-NAME>");
		str=CMStrings.replaceAll(str,"$n","<S-NAME>");
		str=CMStrings.replaceAll(str,"$T","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$t","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$O","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$o","<T-NAMESELF>");
		str=CMStrings.replaceAll(str,"$m","<S-HIM-HER>");
		str=CMStrings.replaceAll(str,"$M","<T-HIM-HER>");
		str=CMStrings.replaceAll(str,"$s","<S-HIS-HER>");
		str=CMStrings.replaceAll(str,"$S","<T-HIS-HER>");
		str=CMStrings.replaceAll(str,"$e","<S-HE-SHE>");
		str=CMStrings.replaceAll(str,"$E","<T-HE-SHE>");
		str=CMStrings.replaceAll(str,"`","\'");
		if(str.equals("$"))
			return "";
		return str.trim();
	}

	/**
	 * Sends a channel who request to a remote mud on the I3 network
	 *
	 * @param mob the mob requesting the who
	 * @param channel the channel to check
	 * @param mudName the remote mud to check
	 * @return true if the request was sent
	 */
	public boolean i3chanwho(final MOB mob, final String channel, String mudName)
	{
		if((mob==null)||(!isOnline()))
			return false;
		if(!I3Client.isAPossibleMUDName(mudName))
			return false;
		mudName=I3Client.translateName(mudName);
		if(!I3Client.isUp(mudName))
			return false;
		if(I3Client.getRemoteChannel(channel).length()==0)
			return false;
		final ChannelWhoRequest ck=new ChannelWhoRequest();
		ck.sender_name=mob.Name();
		ck.target_mud=mudName;
		ck.channel= I3Client.getRemoteChannel(channel);
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IntermudClient", e);
		}
		return true;
	}

	/**
	 * Adds a channel to the I3 network. The channel must be a local channel,
	 * meaning it is listed in the local channels list.
	 *
	 * @param mob the mob requesting the addition
	 * @param channel the channel to add
	 */
	public void i3channelAdd(final MOB mob, final String channel)
	{
		if((mob==null)||(!isOnline()))
			return;
		if((channel==null)||(channel.length()==0)||(I3Client.getLocalChannel(channel).length()==0))
		{
			mob.tell(L("You must specify an existing channel to add it to the i3 network."));
			return;
		}

		final ChannelAdd ck=new ChannelAdd();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel=channel;
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IntermudClient", e);
		}
	}

	/**
	 * Starts listening to a channel on the I3 network. If the channel is not a
	 * local channel (meaning it is not listed in the local channels list), then
	 * it will be added to the local channels list as an unofficial channel.
	 *
	 * @param mob the mob requesting to listen
	 * @param channel the channel to listen to
	 */
	public void i3channelListen(final MOB mob, final String channel)
	{
		if((mob==null)||(!isOnline()))
			return;
		if((channel==null)||(channel.length()==0))
		{
			mob.tell(L("You must specify a channel name listed in your INI file."));
			return;
		}
		if(I3Client.getLocalChannel(channel).length()==0)
		{
			if(I3Client.registerFakeChannel(channel).length()>0)
				mob.tell(L("Channel was not officially registered."));
			else
				mob.tell(L("Channel listen failed."));
		}
		final ChannelListen ck=new ChannelListen();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel= I3Client.getRemoteChannel(channel);
		ck.onoff=1;
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IntermudClient", e);
		}
	}

	/**
	 * Stops listening to a channel on the I3 network. If the channel is not a
	 * local channel (meaning it is not listed in the local channels list), then
	 * it will be removed from the local channels list.
	 *
	 * @param mob the mob requesting to stop listening
	 * @param channel the channel to stop listening to
	 */
	public void i3channelSilence(final MOB mob, final String channel)
	{
		if((mob==null)||(!isOnline()))
			return;
		if((channel==null)
		   ||(channel.length()==0)
		   ||(I3Client.getLocalChannel(channel).length()==0))
		{
			mob.tell(L("You must specify an actual channel name."));
			return;
		}
		if(I3Client.removeFakeChannel(channel).length()>0)
			mob.tell(L("Unofficial channel closed."));

		final ChannelListen ck=new ChannelListen();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel=channel;
		ck.onoff=0;
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IntermudClient", e);
		}
	}

	/**
	 * Removes a channel from the I3 network. The channel must be a local
	 * channel, meaning it is listed in the local channels list.
	 *
	 * @param mob the mob requesting the removal
	 * @param channel the channel to remove
	 */
	public void i3channelRemove(final MOB mob, final String channel)
	{
		if((mob==null)||(!isOnline()))
			return;
		if((channel==null)||(channel.length()==0)||(I3Client.getRemoteChannel(channel).length()==0))
		{
			mob.tell(L("You must specify a valid InterMud 3 channel name."));
			return;
		}
		final ChannelDelete ck=new ChannelDelete();
		ck.sender_name=mob.Name();
		ck.target_mud=I3Client.getNameServer().name;
		ck.channel=channel;
		ck.channel = I3Client.getRemoteChannel(ck.channel);
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IntermudClient", e);
		}
	}

	/**
	 * Returns true if the given channel name is a local I3 channel (meaning it
	 * is listed in the local channels list).
	 *
	 * @param channelName the channel name to check
	 * @return true if the given channel name is a local I3 channel
	 */
	public boolean isLocalI3channel(final String channelName)
	{
		if(!isOnline())
			return false;
		final String remote=I3Client.getRemoteChannel(channelName);
		if(remote.length()==0)
			return false;
		return true;
	}

	/**
	 * Returns true if the given channel name is a remote I3 channel (meaning it
	 * is not listed in the local channels list, but is known to I3).
	 *
	 * @param channelName the channel name to check
	 * @return true if the given channel name is a remote I3 channel
	 */
	public boolean isRemoteI3Channel(final String channelName)
	{
		if(!isOnline())
			return false;
		final String remote=I3Client.getLocalChannel(channelName);
		if(remote.length()==0)
			return false;
		return true;
	}

	/**
	 * Given a local channel name, returns the remote channel name. Example:
	 * getI3ChannelName("I3CHAT") might return "diku_chat"
	 *
	 * @param localChannelName the local channel name
	 * @return the remote channel name
	 */
	public String getI3ChannelName(final String localChannelName)
	{
		final String fixedChannel = I3Client.getRemoteChannel(localChannelName);
		if(((fixedChannel != null)&&(fixedChannel.length()>0)))
			return fixedChannel;
		else
			return localChannelName;
	}

	/**
	 * Given a string, tries to find an I3 mud with a matching name. The match
	 * is first attempted as an exact match, then as a case-insensitive exact
	 * match, then as a starts-with match, then as an ends-with match, then as a
	 * contains match, and finally as a partial match (if the given string
	 * contains spaces).
	 *
	 * @param parms the string to match against
	 * @return a list of matching I3 muds
	 */
	public List<I3Mud> i3MudFinder(final String parms)
	{
		final MudList list=I3Client.getAllMudsList();
		if(list==null)
			return null;
		final Map<String,I3Mud> l=list.getMuds();
		for(final I3Mud m : l.values())
		{
			if(m.mud_name.equals(parms))
				return new XVector<I3Mud>(m);
		}
		for(final I3Mud m : l.values())
		{
			if(m.mud_name.equalsIgnoreCase(parms))
				return new XVector<I3Mud>(m);
		}
		if(parms.startsWith("*")&&(!parms.endsWith("*")))
		{
			final List<I3Mud> muds=new XVector<I3Mud>();
			for(final I3Mud m : l.values())
			{
				if(m.mud_name.toLowerCase().endsWith(parms.toLowerCase()))
					muds.add(m);
			}
			return muds;
		}
		if(parms.endsWith("*")&&(!parms.startsWith("*")))
		{
			final List<I3Mud> muds=new XVector<I3Mud>();
			for(final I3Mud m : l.values())
			{
				if(m.mud_name.toLowerCase().startsWith(parms.toLowerCase()))
					muds.add(m);
			}
			return muds;
		}
		if(parms.endsWith("*")&&(parms.startsWith("*")))
		{
			final List<I3Mud> muds=new XVector<I3Mud>();
			for(final I3Mud m : l.values())
			{
				if(m.mud_name.toLowerCase().indexOf(parms.toLowerCase())>=0)
					muds.add(m);
			}
			return muds;
		}
		final List<I3Mud> muds=new XVector<I3Mud>();
		for(final I3Mud m : l.values())
		{
			if((m.state<0)&&(CMLib.english().containsString(m.mud_name,parms)))
				muds.add(m);
		}
		return muds;
	}

	/**
	 * Returns a list of all known I3 muds, sorted alphabetically by name, and
	 * only those that are currently online.
	 *
	 * @return a list of all known I3 muds
	 */
	public List<I3Mud> getSortedI3Muds()
	{
		final Vector<I3Mud> list = new Vector<I3Mud>();
		if(!isOnline())
			return list;
		final MudList mudList=I3Client.getAllMudsList();
		if(mudList!=null)
		{
			for(final I3Mud m : mudList.getMuds().values())
			{
				if(m.state<0)
				{
					boolean done=false;
					for(int v=0;v<list.size();v++)
					{
						final I3Mud m2=list.elementAt(v);
						if(m2.mud_name.toUpperCase().compareTo(m.mud_name.toUpperCase())>0)
						{
							list.insertElementAt(m,v);
							done=true;
							break;
						}
					}
					if(!done)
						list.addElement(m);
				}
			}
		}
		return list;
	}

	/**
	 * Converts a social message so that it will be properly interpreted by
	 * other I3 muds.
	 *
	 * @param str the string to convert
	 * @return the converted string
	 */
	public String socialI3FixOut(String str)
	{
		str=CMStrings.replaceAll(str,"<S-NAME>","$N");
		str=CMStrings.replaceAll(str,"<T-NAME>","$O");
		str=CMStrings.replaceAll(str,"<T-NAMESELF>","$O");
		str=CMStrings.replaceAll(str,"<S-HIM-HER>","$m");
		str=CMStrings.replaceAll(str,"<T-HIM-HER>","$M");
		str=CMStrings.replaceAll(str,"<S-HIS-HER>","$s");
		str=CMStrings.replaceAll(str,"<T-HIS-HER>","$S");
		str=CMStrings.replaceAll(str,"<S-HE-SHE>","$e");
		str=CMStrings.replaceAll(str,"<T-HE-SHE>","$E");
		str=CMStrings.replaceAll(str,"\'","`");
		if(str.equals(""))
			return "$";
		return str.trim();
	}

	/**
	 * Sends a message to the given I3 channel.
	 *
	 * @param mob the mob sending the message
	 * @param channelName the channel to send it to
	 * @param message the message to send
	 */
	public void sendI3ChannelMsg(final MOB mob, final String channelName, final String message)
	{
		final ChannelPacket ck;
		if((message.startsWith(":")||message.startsWith(","))
		&&(message.trim().length()>1))
		{
			String msgstr=message.substring(1);
			final Vector<String> V=CMParms.parse(msgstr);
			Social socialS=CMLib.socials().fetchSocial(V,true,false);
			if(socialS==null)
				socialS=CMLib.socials().fetchSocial(V,false,false);
			CMMsg msg=null;
			if((socialS!=null)
			&&(socialS.meetsCriteriaToUse(mob)))
			{
				msg=socialS.makeChannelMsg(mob,0,channelName,V,true);
				final int atDex = (msg.target()!=null) ? msg.target().name().indexOf('@') : -1;
				if(atDex>=0)
				{
					final int x=atDex;
					String mudName=msg.target().name().substring(x+1);
					final String tellName=msg.target().name().substring(0,x);
					if((mudName==null)||(mudName.length()==0))
					{
						mob.tell(L("You must specify a mud name."));
						return;
					}
					if((tellName==null)||(tellName.length()<1))
					{
						mob.tell(L("You must specify someone to emote to."));
						return;
					}
					if(!I3Client.isAPossibleMUDName(mudName))
					{
						mob.tell(L("'@x1' is an unknown mud.",mudName));
						return;
					}
					mudName=I3Client.translateName(mudName);
					if(!I3Client.isUp(mudName))
					{
						mob.tell(L("@x1 is not available.",mudName));
						return;
					}
					ck = new ChannelTargetEmote();
					ck.channel = getI3ChannelName(channelName);
					ck.sender_name=mob.Name();
					ck.sender_visible_name=mob.Name();
					ck.target_mud=mudName;
					ck.target_name=tellName;
					((ChannelTargetEmote)ck).target_visible_name=tellName;
					if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0))
						((ChannelTargetEmote)ck).message_target=socialI3FixOut(CMStrings.removeColors(msg.targetMessage()));
					if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
						((ChannelTargetEmote)ck).message=socialI3FixOut(CMStrings.removeColors(msg.othersMessage()));
				}
				else
				if(msg.target()!=null)
				{
					ck = new ChannelTargetEmote();
					ck.target_name=msg.target().name();
					ck.channel = getI3ChannelName(channelName);
					ck.sender_name=mob.Name();
					ck.sender_visible_name=mob.Name();
					((ChannelTargetEmote)ck).target_visible_name=msg.target().name();
					if((msg.targetMessage()!=null)&&(msg.targetMessage().length()>0))
						((ChannelTargetEmote)ck).message_target=socialI3FixOut(CMStrings.removeColors(msg.targetMessage()));
				}
				else
				{
					ck = new ChannelEmote();
					ck.channel = getI3ChannelName(channelName);
					ck.sender_name=mob.Name();
					ck.sender_visible_name=mob.Name();
				}
				if((msg.othersMessage()!=null)&&(msg.othersMessage().length()>0))
					ck.message=socialI3FixOut(CMStrings.removeColors(msg.othersMessage()));
				else
					ck.message=socialI3FixOut(CMStrings.removeColors(msg.sourceMessage()));
				ck.message = ck.convertString(ck.message);
			}
			else
			{
				ck = new ChannelEmote();
				ck.channel = this.getI3ChannelName(channelName);
				ck.sender_name=mob.Name();
				ck.sender_visible_name=mob.Name();
				if(msgstr.trim().startsWith("'")||msgstr.trim().startsWith("`"))
					msgstr=msgstr.trim();
				else
					msgstr=" "+msgstr.trim();
				ck.message=socialI3FixOut("<S-NAME>"+msgstr);
			}
		}
		else
		{
			ck = new ChannelMessage();
			ck.channel = getI3ChannelName(channelName);
			ck.sender_name=mob.Name();
			ck.sender_visible_name=mob.Name();
			ck.message=message;
		}
		try
		{
			ck.send();
		}
		catch (final Exception e)
		{
			Log.errOut("IntermudClient", e);
		}
	}


	/**
	 * Returns the last packet received time.
	 * @return the last packet received time
	 */
	@Override
	public long getLastPacketReceivedTime()
	{
		return lastPacketReceivedTime;
	}

	/**
	 * Sets the last packet received time to the current time.
	 */
	@Override
	public void resetLastPacketReceivedTime()
	{
		lastPacketReceivedTime=System.currentTimeMillis();
	}

	/**
	 * Returns the incoming keys timestamp map. This is used to prevent
	 * replay attacks.
	 * @return the incoming keys timestamp map
	 */
	@Override
	public Map<String,Long> getIncomingKeys()
	{
		return inkeys;
	}

	/**
	 * Returns a map of outgoing packet keys to timestamps. This is used to
	 * prevent replay attacks.
	 *
	 * @return a map of outgoing packet keys to timestamps
	 */
	@Override
	public Map<String,Long> getOutgoingKeys()
	{
		return outkeys;
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
	@Override
	public void receive(final Packet packet)
	{
		switch(packet.getType())
		{
		case CHANNEL_E:
		case CHANNEL_M:
		case CHANNEL_T:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelPacket ck=(ChannelPacket)packet;
				String channelName=ck.channel;
				channelName = I3Client.getLocalChannel(channelName);
				CMMsg msg=null;

				if((ck.sender_mud!=null)&&(ck.sender_mud.equalsIgnoreCase(getMudName())))
					return;
				if((ck.channel==null)||(ck.channel.length()==0))
					return;
				final String channelColor="^Q";
				final ChannelsLibrary channels = CMLib.channels();
				final int channelInt=channels.getChannelIndex(channelName);
				int channelCode=channelInt;
				if(channelInt < 0)
				{
					channelCode=47;
				}
				ck.message=fixColors(CMProps.applyINIFilter(ck.message,CMProps.Str.CHANNELFILTER));
				final MOB mob=CMClass.getFactoryMOB();
				mob.setName(ck.sender_name+"@"+ck.sender_mud);
				mob.setLocation(getUniversalRoom());
				MOB targetMOB=null;
				boolean killtargetmob=false;
				if(ck.type==Packet.PacketType.CHANNEL_T)
				{
					final ChannelTargetEmote ct = (ChannelTargetEmote)ck;
					if(ct.message_target != null)
						ct.message_target = fixColors(CMProps.applyINIFilter(ct.message_target,CMProps.Str.CHANNELFILTER));
					if((ct.target_mud!=null)
					&&(ct.target_mud.equals(getMudName()))
					&&(CMLib.players().isLoadedPlayer(ct.target_name)))
						targetMOB=CMLib.players().getPlayer(ct.target_name);
					if((ct.target_visible_name!=null)&&(targetMOB==null))
					{
						killtargetmob=true;
						targetMOB=CMClass.getFactoryMOB();
						targetMOB.setName(ct.target_visible_name+"@"+ck.target_mud);
						targetMOB.setLocation(getUniversalRoom());
					}
					String msgs=socialFixIn(ck.message);
					msgs=CMProps.applyINIFilter(msgs,CMProps.Str.EMOTEFILTER);
					String targmsgs=socialFixIn(ct.message_target);
					targmsgs=CMProps.applyINIFilter(targmsgs,CMProps.Str.EMOTEFILTER);
					final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+msgs+"^</CHANNEL^>^N^.";
					final String str2=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+targmsgs+"^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,targetMOB,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str2,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
				}
				else
				if(ck.type==Packet.PacketType.CHANNEL_E)
				{
					String msgs=socialFixIn(ck.message);
					msgs=CMProps.applyINIFilter(msgs,CMProps.Str.EMOTEFILTER);
					final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+msgs+"^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
				}
				else
				{
					final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>"+mob.name()+" "+channelName+"(S) '"+ck.message+"'^</CHANNEL^>^N^.";
					msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
				}
				CMLib.commands().monitorGlobalMessage(mob.location(), msg);
				try
				{
					if(channelInt>=0)
						channels.channelQueUp(channelInt, msg, 0);
				}
				catch(final Exception e)
				{
					Log.errOut(e);
				}
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB M=S.mob();
					final ChannelsLibrary myChanLib=CMLib.get(S)._channels();
					final int chanNum = (myChanLib == channels) ? channelInt : myChanLib.getChannelIndex(channelName);
					if((chanNum >= 0)
					&&(myChanLib.mayReadThisChannel(mob,false,S,chanNum))
					&&(M.location()!=null))
					{
						msg.setOthersCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+chanNum));
						if(M.location().okMessage(M,msg))
							M.executeMsg(M,msg);
					}
				}
				mob.destroy();
				if((targetMOB!=null)&&(killtargetmob))
					targetMOB.destroy();
			}
			break;
		case LOCATE_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final LocateQueryPacket lk=(LocateQueryPacket)packet;
				String stat="online";
				String name=CMStrings.capitalizeAndLower(lk.user_name);
				final MOB smob=findSessMob(lk.user_name);
				if(smob!=null)
				{
					if(CMLib.flags().isCloaked(smob))
						stat="exists, but not logged in";
				}
				else
				if(CMLib.players().getPlayer(lk.user_name)!=null)
					stat="exists, but not logged in";
				else
				if(CMLib.players().playerExists(lk.user_name))
					stat="exists, but is not online";
				else
					name=null;
				if(name!=null)
				{
					final LocateReplyPacket lpk=new LocateReplyPacket(lk.sender_name,lk.sender_mud,name,0,stat);
					try
					{
						lpk.send();
					}
					catch (final Exception e)
					{
						Log.errOut("IntermudClient", e);
					}
				}
			}
			break;
		case LOCATE_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final LocateReplyPacket lk=(LocateReplyPacket)packet;
				final MOB smob=findSessMob(lk.target_name);
				if(smob!=null)
					smob.tell(fixColors(lk.located_visible_name)+"@"+fixColors(lk.located_mud_name)+" ("+lk.idle_time+"): "+fixColors(lk.status));
			}
			break;
		case FINGER_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final FingerRequest lk=(FingerRequest)packet;
				Packet pkt;
				final MOB M=CMLib.players().getLoadPlayer(lk.target_name);
				if(M==null)
					pkt=new ErrorPacket(lk.sender_name,lk.sender_mud,"unk-user","User "+lk.target_name+" is not known here.","0");
				else
				{
					final FingerReply fpkt = new FingerReply(lk.sender_name,lk.sender_mud);
					pkt=fpkt;
					fpkt.e_mail="0";
					final Session sess=M.session();
					if((sess==null)||(!sess.isAfk()))
						fpkt.idle_time="-1";
					else
						fpkt.idle_time=Long.toString(sess.getIdleMillis()/1000);
					fpkt.ip_time="0"; // what IS this?
					fpkt.loginout_time=CMLib.time().date2String(M.playerStats().getLastDateTime());
					fpkt.real_name="0"; // don't even know this
					if(M.titledName().equals(M.name()))
						fpkt.title="An ordinary "+M.charStats().displayClassName();
					else
						fpkt.title=M.titledName();
					fpkt.visible_name=M.name();
					fpkt.extra=M.name()+" is a "+M.charStats().raceName()+" "+M.charStats().displayClassName();
				}
				try
				{
					pkt.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IntermudClient", e);
				}
			}
			break;
		case FINGER_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final FingerReply lk=(FingerReply)packet;
				final MOB smob=findSessMob(lk.target_name);
				if(smob!=null)
				{
					final StringBuilder response=new StringBuilder("");
					if((lk.visible_name.length()>0)&&(!lk.visible_name.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Name"),10)).append(": ^N").append(lk.visible_name).append("\n\r");
					if((lk.title.length()>0)&&(!lk.title.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Title"),10)).append(": ^N").append(lk.title).append("\n\r");
					if((lk.real_name.length()>0)&&(!lk.real_name.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Real Name"),10)).append(": ^N").append(lk.real_name).append("\n\r");
					if((lk.e_mail.length()>0)&&(!lk.e_mail.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Email"),10)).append(": ^N").append(lk.e_mail).append("\n\r");
					if((lk.loginout_time.length()>0)&&(!lk.loginout_time.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Logged"),10)).append(": ^N").append(lk.loginout_time).append("\n\r");
					if((lk.ip_time.length()>0)&&(!lk.ip_time.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("IP Time"),10)).append(": ^N").append(lk.ip_time).append("\n\r");
					if((lk.extra.length()>0)&&(!lk.extra.equals("0")))
						response.append("^H").append(CMStrings.padRight(L("Extra"),10)).append(": ^N").append(lk.extra).append("\n\r");
					smob.tell(response.toString());
				}
			}
			break;
		case AUTH_MUD_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final MudAuthRequest lk=(MudAuthRequest)packet;
				if(lk.sender_mud.equalsIgnoreCase(I3Server.getMudName()))
				{
					if(CMSecurity.isDebugging(DbgFlag.I3))
						Log.debugOut("I3","Received my own mud-auth.");
				}
				else
					Log.sysOut("I3","MUD "+lk.sender_mud+" wants to mud-auth.");
				inkeys.put(lk.sender_mud, Long.valueOf(new Random(System.currentTimeMillis()).nextInt(999999)));
				final MudAuthReply pkt = new MudAuthReply(lk.sender_mud, inkeys.get(lk.sender_mud).intValue());
				try
				{
					pkt.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IntermudClient", e);
				}
			}
			break;
		case OOB_REQ:
			{
				final OOBReq lk=(OOBReq)packet;
				if(lk.target_mud.equalsIgnoreCase(I3Server.getMudName()))
					inkeys.put(lk.sender_mud, Long.valueOf(-1));
				break;
			}
		case AUTH_MUD_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final MudAuthReply lk=(MudAuthReply)packet;
				if(lk.sender_mud.equalsIgnoreCase(I3Server.getMudName()))
				{
					if(CMSecurity.isDebugging(DbgFlag.I3))
						Log.debugOut("I3","I replied to my mud-auth.");
					lastPacketReceivedTime=System.currentTimeMillis();
				}
				else
				{
					outkeys.put(lk.sender_mud, Long.valueOf(lk.key));
					//TODO: if we have stuff to deliver, connect, and deliver it now
					Log.sysOut("I3","MUD "+lk.sender_mud+" replied to my mud-auth with key "+lk.key+".");
				}
			}
			break;
		case WHO_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final WhoReplyPacket wk=(WhoReplyPacket)packet;
				final MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					final StringBuffer buf=new StringBuffer("\n\rwhois@"+fixColors(wk.sender_mud)+":\n\r");
					final Vector<?> V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						final Vector<?> V2=(Vector<?>)V.elementAt(v);
						final String nom = fixColors(V2.elementAt(0).toString());
						final String idle=V2.elementAt(1).toString();
						final String xtra = fixColors(V2.elementAt(2).toString());
						buf.append("["+CMStrings.padRight(nom,20)+"] "+xtra+" ("+idle+")\n\r");
					}
					smob.session().wraplessPrintln(buf.toString());
					break;
				}
			}
			break;
		case CHAN_WHO_REPLY:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelWhoReply wk=(ChannelWhoReply)packet;
				final MOB smob=findSessMob(wk.target_name);
				if(smob!=null)
				{
					final StringBuffer buf=new StringBuffer("\n\rListening on "+wk.channel+"@"+fixColors(wk.sender_mud)+":\n\r");
					final Vector<?> V=wk.who;
					if(V.size()==0)
						buf.append("Nobody!");
					else
					for(int v=0;v<V.size();v++)
					{
						final String nom = fixColors((String)V.elementAt(v));
						buf.append("["+CMStrings.padRight(nom,20)+"]\n\r");
					}
					smob.session().wraplessPrintln(buf.toString());
					smob.session().setPromptFlag(true);
					break;
				}
			}
			break;
		case CHAN_WHO_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelWhoRequest wk=(ChannelWhoRequest)packet;
				final ChannelWhoReply wkr=new ChannelWhoReply();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				wkr.channel=wk.channel;
				final String locChannel = I3Client.getRemoteChannel(wk.channel);
				final int channelInt=CMLib.channels().getChannelIndex(locChannel);
				final Vector<String> whoV=new Vector<String>();
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB M=S.mob();
					if((CMLib.channels().mayReadThisChannel(M,false,S,channelInt))
					&&(M!=null)
					&&(!CMLib.flags().isCloaked(M)))
						whoV.addElement(M.name());
				}
				wkr.who=whoV;
				try
				{
					wkr.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IntermudClient", e);
				}
			}
			break;
		case CHAN_USER_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final ChannelUserRequest wk=(ChannelUserRequest)packet;
				final ChannelUserReply wkr=new ChannelUserReply();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				wkr.userRequested=wk.userToRequest;
				final MOB M=CMLib.players().getLoadPlayer(wk.userToRequest);
				if(M!=null)
				{
					wkr.userVisibleName = M.name();
					wkr.gender=(char)M.charStats().getStat(CharStats.STAT_GENDER);
					try
					{
						wkr.send();
					}
					catch (final Exception e)
					{
						Log.errOut("IntermudClient", e);
					}
				}
			}
			break;
		case WHO_REQ:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final WhoReqPacket wk=(WhoReqPacket)packet;
				final WhoReplyPacket wkr=new WhoReplyPacket();
				wkr.target_name=wk.sender_name;
				wkr.target_mud=wk.sender_mud;
				final Vector<Vector<Object>> whoV=new Vector<Vector<Object>>();
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					MOB smob=S.mob();
					if((smob!=null)&&(smob.soulMate()!=null))
						smob=smob.soulMate();
					if((!S.isStopped())&&(smob!=null)
					&&(!smob.amDead())
					&&(CMLib.flags().isInTheGame(smob,true))
					&&(!CMLib.flags().isCloaked(smob)))
					{
						final Vector<Object> whoV2=new Vector<Object>();
						whoV2.addElement(smob.name());
						whoV2.addElement(Integer.valueOf((int)(S.getIdleMillis()/1000)));
						whoV2.addElement(smob.charStats().displayClassLevel(smob,true));
						whoV.addElement(whoV2);
					}
				}
				wkr.who=whoV;
				try
				{
					wkr.send();
				}
				catch (final Exception e)
				{
					Log.errOut("IntermudClient", e);
				}
			}
			break;
		case TELL:
			{
				lastPacketReceivedTime=System.currentTimeMillis();
				final TellPacket tk=(TellPacket)packet;
				final MOB smob=findSessMob(tk.target_name);
				if(smob!=null)
				{
					MOB mob=null;
					final PlayerStats pstats=smob.playerStats();
					if(pstats!=null)
					{
						if((pstats.getReplyToMOB()!=null)&&(pstats.getReplyToMOB().Name().indexOf('@')>=0))
							mob=pstats.getReplyToMOB();
						else
							mob=CMClass.getFactoryMOB();
						pstats.setReplyTo(mob, PlayerStats.REPLY_TELL);
					}
					else
						mob=CMClass.getFactoryMOB();
					mob.setName(tk.sender_name+"@"+tk.sender_mud);
					mob.setLocation(getUniversalRoom());
					tk.message=fixColors(CMProps.applyINIFilter(tk.message,CMProps.Str.SAYFILTER));
					CMLib.commands().postSay(mob,smob,tk.message,true,true);
				}
			}
			break;
		default:
			Log.errOut("IMudInterface","Unknown type: "+packet.getType());
			break;
		}
	}

	/**
	 * @return an enumeration of channels this mud subscribes to
	 */
	@Override
	public java.util.Enumeration<String> getChannels()
	{
		final Vector<String> V=new Vector<String>();
		for(final CMChannel chan : channels)
			V.addElement(chan.i3name());
		return V.elements();
	}

	/**
	 * Register a fake channel
	 * @param chan the remote channel name
	 * @return the local channel name for the specified new local channel name
	 * @see com.planet_ink.coffee_mud.Libraries.intermud.i3.ImudServices#getLocalChannel
	 */
	@Override
	public boolean addChannel(final CMChannel chan)
	{
		if((getLocalChannel(chan.i3name()).length()==0)
		&&(getRemoteChannel(chan.name()).length()==0))
		{
			channels.add(chan);
			return true;
		}
		return false;
	}

	/**
	 * Remote a channel
	 * @param remoteChannelName the remote name
	 * @return true if remove succeeds
	 */
	@Override
	public boolean delChannel(final String remoteChannelName)
	{
		for(int i=0;i<channels.size();i++)
		{
			if(channels.get(i).i3name().equalsIgnoreCase(remoteChannelName))
			{
				channels.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Given a I3 channel name, this method should provide
	 * the local name for that channel.
	 * Example:
	 *
	 * if( str.equals("imud_code") ) return "intercre";
	 *
	 * @param str the remote name of the desired channel
	 * @return the local channel name for a remote channel
	 * @see #getRemoteChannel
	 */
	@Override
	public String getLocalChannel(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.i3name().equalsIgnoreCase(str))
				return chan.name();
		}
		return "";
	}

	/**
	 * Given a local channel name, this method should provide
	 * the local mask for that channel.
	 * Example:
	 *
	 * if( str.equals("ICODE") ) return "";
	 *
	 * @param str the local name of the desired channel
	 * @return the local channel mask for a remote channel
	 * @see #getLocalMask
	 */
	public String getLocalMask(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.name().equalsIgnoreCase(str))
				return chan.mask();
		}
		return "";
	}

	/**
	 * @return the name of this mud
	 */
	@Override
	public String getMudName()
	{
		return name;
	}

	/**
	 * @return the software name and version
	 */
	@Override
	public String getMudVersion()
	{
		return version;
	}

	/**
	 * @return the software name and version
	 */
	@Override
	public String getMudState()
	{
		return i3state;
	}

	/**
	 * @return the player mudPort for this mud
	 */
	@Override
	public int getMudPort()
	{
		return mudPort;
	}

	/**
	 * Given a remote channel name, returns the mask
	 * required.
	 * Example:
	 *
	 * if( str.equals("intercre") ) return "";
	 *
	 * @param str the remote name of the desired channel
	 * @return the remote mask of the specified local channel
	 */
	@Override
	public String getRemoteMask(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.i3name().equalsIgnoreCase(str))
				return chan.mask();
		}
		return "";
	}

	/**
	 * Given a local channel name, returns the remote
	 * channel name.
	 * Example:
	 *
	 * if( str.equals("intercre") ) return "imud_code";
	 *
	 * @param str the local name of the desired channel
	 * @return the remote name of the specified local channel
	 */
	@Override
	public String getRemoteChannel(final String str)
	{
		for(final CMChannel chan : channels)
		{
			if(chan.name().equalsIgnoreCase(str))
				return chan.i3name();
		}
		return "";
	}
}
