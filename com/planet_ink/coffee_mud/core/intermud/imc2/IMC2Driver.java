package com.planet_ink.coffee_mud.core.intermud.imc2;

import com.planet_ink.coffee_mud.Commands.interfaces.Command;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.*;

/**
 * IMC2 version 0.10 - an inter-mud communications protocol
 * Copyright (C) 1996 - 1997 Oliver Jowet: oliver@randomly.org
 *
 * IMC2 Gold versions 1.00 though 2.00 are developed by MudWorld.
 * Copyright (C) 1999 - 2002 Haslage Net Electronics (Anthony R. Haslage)
 *
 * IMC2 MUD-Net version 3.10 is developed by Alsherok and Crimson Oracles
 * Copyright (C) 2002 Roger Libiez ( Samson )
 * Additional code Copyright (C) 2002 Orion Elder
 * Registered with the United States Copyright Office
 * TX 5-555-584
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see the file COPYING); if not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Ported to Java by Istvan David (u_davis@users.sourceforge.net)
 *
 */

@SuppressWarnings({"unchecked","rawtypes"})
public final class IMC2Driver extends Thread {

	Socket sa;

	/* a player on IMC */
	final class imc_char_data {
		String name = ""; /* name of character */
		int invis; /* invisible to IMC? */
		int level; /* trust level */
		int wizi; /* wizi level */
	}
	static final int PERM_NONE = 0;
	static final int PERM_PLAYER = 1;
	static final int PERM_IMMORTAL = 2;
	static final int PERM_ADMIN = 3;
	boolean shutdown=false;
	boolean isShutdown=false;

	Date imc_boot = new Date();
	long imc_now,imc_sequencenumber,imc_prev_sequencenumber;
	public hubinfo this_imcmud = new hubinfo();
	public siteinfo imc_siteinfo = new siteinfo();
	imc_statistics imc_stats = new imc_statistics();
	public int imc_active; /* Connection state */
	long HeartBeat = 0; // elapsed heartbeats
	call_out c_thread=null;
	call_in c_thread2=null;

	Hashtable<String,REMOTEINFO> muds = new Hashtable();
	Hashtable channels = new Hashtable();
	public Hashtable chanhist = new Hashtable();
	Hashtable replies = new Hashtable();

	public Hashtable chan_conf = new Hashtable();
	public Hashtable chan_mask = new Hashtable();

	BufferedReader in;
	DataOutputStream out;

	public String imc_name = "";
	public short imc_log_on;

	private long reconnectBackoffTime = 20000;

	/* refresh timeout */
	final static int  IMC_TIMEOUT = 650;

	public final static int CHAN_OPEN     = 1;
	public final static int CHAN_CLOSED   = 2;
	public final static int CHAN_PRIVATE  = 3;
	public final static int CHAN_COPEN    = 4;
	public final static int CHAN_CPRIVATE = 5;

	/* max length of any mud name */
	final static int IMC_MNAME_LENGTH = 20;

	/* max length of any player name */
	final static int IMC_PNAME_LENGTH = 40;

	/* max length of any player@mud name */
	final static int IMC_NAME_LENGTH = (IMC_MNAME_LENGTH + IMC_PNAME_LENGTH + 1);

	/* activation states */
	public final static int IA_NONE = 0;
	public final static int IA_CONFIG1 = 1;
	public final static int IA_CONFIG2 = 2;
	public final static int IA_UP = 3;

	/* connection states */
	final static int IMC_CLOSED = 0; /* No active connection */
	final static int IMC_CONNECTING = 1; /* Contacting hub */
	final static int IMC_WAIT1 = 2; /* Waiting for hub verification */
	final static int IMC_CONNECTED = 3; /* Fully connected */

	final static int IMC_VERSION = 2;
	final static String IMC_VERSIONID  = "IMC2 4.00 for CoffeeMud";

	final void tracef(int level, String s)
	{
		if((level<1)||(CMSecurity.isDebugging(CMSecurity.DbgFlag.IMC2)))
			Log.debugOut("IMC2",level+"/"+s);
	}

	final void send_to_player(String name, String text)
	{
		if(name.equalsIgnoreCase("all"))
		{
			for(final Session S : CMLib.sessions().localOnlineIterable())
				S.println(text);
		}
		else
		{
			final MOB M=CMLib.players().getPlayer(name);
			if(M!=null)
				M.tell(text);
		}
	}

	public void ev_request_keepalive(Object param)
	{
		imc_request_keepalive();
		//imc_register_call_out(25, "ev_keepalive", null);
	}

	final public List<CMChannel> rebuildChannelMap()
	{
		final List<CMChannel> map=new Vector<CMChannel>(chan_conf.size());
		for(final Enumeration e=chan_conf.keys();e.hasMoreElements();)
		{
			String name=(String)e.nextElement();
			String mask=(String)chan_mask.get(name);
			String imc2Name=(String)chan_conf.get(name);
			final CMChannel chan=CMLib.channels().createNewChannel(name, "", imc2Name, mask, new HashSet<ChannelFlag>(),"","");
			map.add(chan);
		}
		return map;
	}

	final public static String[] explodeNicely(String s)
	{
		  return explodeNicely(s, " ");
	}

	public void shutdown()
	{
		shutdown=true;
		try
		{
		if(c_thread!=null)
			c_thread.shutdown();
		}
		catch(final Exception e)
		{
		}
		try
		{
		if(c_thread2!=null)
			c_thread2.shutdown();
		}
		catch(final Exception e)
		{
		}
		CMLib.killThread(this,500,1);

		int i=0;
		while (((++i)<60)&&((!c_thread.isShutdown)||(!c_thread2.isShutdown)||(!isShutdown)))
			try { sleep(1000);    }
			catch (final Exception ex)
			{
			}
	}

	final public static String[] explodeNicely(String s, String separator)
	{
		final StringTokenizer st = new StringTokenizer(s, separator);

		final int n = st.countTokens();
		if(n==0)
		{
			final String array[] = new String[1];
			array[0] = s;
			return array;
		}

		final String array[]  = new String[n];

		int i = 0;
		while(st.hasMoreTokens())
		{
			array[i] = st.nextToken();
				i++;
		}

		return array;
	}

	/* put a line onto descriptors output buffer */
	final void do_imcsend( String line )
	{
		if(line.endsWith("\n\r"))
			this_imcmud.outbuf = line;
		else
			this_imcmud.outbuf = line+"\n\r";
	}

	/* connect to hub */
	final boolean imc_connect_to()
	{
		String buf;

		if (imc_active == IA_NONE)
		{
			tracef(0, "IMC is not active");
			return false;
		}

		tracef(8, "Connecting to " + this_imcmud.hubname);

		try
		{
			setName("IMC2Client:"+this_imcmud.host+"@"+this_imcmud.port);
			sa = new Socket(this_imcmud.host, this_imcmud.port);
		}
		catch (final Exception e)
		{
			tracef(0, "Error connecting to " + this_imcmud.host + ":" +
						  this_imcmud.port);
			return false;

		}
		if (sa == null)
		{
			tracef(0, "Error connecting to " + this_imcmud.host + ":" +
						  this_imcmud.port);
			return false;
		}

		try
		{
			sa.setSoTimeout(60000);
			in = new BufferedReader(new InputStreamReader(sa.getInputStream()));
			out = new DataOutputStream(sa.getOutputStream());
		}
		catch(final Exception e)
		{
		}

		this_imcmud.state = IMC_CONNECTING;
		this_imcmud.insize = 1024;
		this_imcmud.outsize = 1024;

		buf = "PW " + imc_name.trim() + " " + this_imcmud.clientpw + " version=" +
			IMC_VERSION+" autosetup "+this_imcmud.serverpw;
		do_imcsend(buf);

		imc_write_to_socket(out);
		imc_read_from_socket(in);
		reconnectBackoffTime=20000;
		return true;
	}

	/* start up IMC */
	final boolean imc_startup_network()
	{
		if (imc_active != IA_CONFIG2)
		{
			tracef(0, "imc_startup_network: called with imc_active == "+
						  imc_active);
			return false;
		}

		tracef(8, "IMC2 Network Initializing");

		imc_active = IA_UP;
		imc_stats.start = imc_now;
		imc_stats.rx_pkts = 0;
		imc_stats.tx_pkts = 0;
		imc_stats.rx_bytes = 0;
		imc_stats.tx_bytes = 0;
		imc_stats.sequence_drops = 0;

		/* Connect to Hub */
		if (!imc_connect_to())
			return false;

		imc_register_call_out(5, "ev_keepalive", null);

		imc_register_call_out(6, "ev_request_keepalive", null);

		return true;
	}

	final public void ev_imc_firstrefresh()
	{
		final PACKET out = new PACKET();
		if (this_imcmud == null || imc_active < IA_UP)
			return;

		out.from = "*";
		out.to = "IMC@*";
		out.type = "ice-refresh";
		imc_initdata(out);
		imc_addkey(out, "channel", "*");
		imc_send(out);
	}

	final public boolean imc_startup( boolean force,
									  String loginName,
									  String host,
									  String email,
									  String web,
									  String hub,
									  int port,
									  String passclient,
									  String passsrvr,
									  List<CMChannel> imc2Channels)
	{
	   if( imc_active != IA_NONE )
	   {
		  tracef(0,  "imc_startup: called with imc_active = "+imc_active );
		  return false;
	   }
	   imc_now = new Date().getTime()/1000; 				 /* start our clock */
	   imc_boot = new Date();

	   imc_sequencenumber = imc_now;

		for(final CMChannel chan : imc2Channels)
		{
			chan_mask.put(chan.name(),chan.mask());
			chan_conf.put(chan.name(),chan.imc2Name());
		}

		imc_name=loginName;
		imc_log_on=1; // logging?
		this_imcmud.autoconnect=true;
		imc_siteinfo.name=CMProps.getVar(CMProps.Str.MUDNAME);
		imc_siteinfo.host=host;
		imc_siteinfo.port=CMath.s_int(CMParms.parse(CMProps.getVar(CMProps.Str.MUDPORTS)).elementAt(0));
		imc_siteinfo.email=email;
		imc_siteinfo.base="CoffeeMud v"+CMProps.getVar(CMProps.Str.MUDVER);
		imc_siteinfo.details="Custom Java-based Mud";
		imc_siteinfo.www=web;
		this_imcmud.hubname=hub;
		this_imcmud.host=hub;
		this_imcmud.port=port;
		this_imcmud.clientpw=passclient;
		this_imcmud.serverpw=passsrvr;
		if( !this_imcmud.autoconnect && !force )
		{
			tracef(8,  "IMC2 data loaded. Autoconnect not set. "+
						 "IMC will need to be connected manually." );
			return false;
		}

		imc_active = !imc_name.equals("") ? IA_CONFIG2 : IA_CONFIG1;

		if( imc_active == IA_CONFIG2 && ( this_imcmud.autoconnect || force ) )
		{
			if( imc_startup_network() )
			{
				ev_imc_firstrefresh();
				return true;
			}
			imc_active = IA_NONE;
		}
		return false;
	}

	final String normal2(String data)
	{
		data = CMStrings.replaceAll(data, "\\\"", "\"");
		final StringBuffer str=new StringBuffer(data);
		for(int i=0;i<str.length()-1;i++)
		{
			if(str.charAt(i)=='~')
			switch(str.charAt(i+1))
			{
				case 'R':
				case 'r':
				case 'y':
				case 'Y':
				case 'g':
				case 'G':
				case 'b':
				case 'B':
				case 'w':
				case 'W':
				case 'c':
				case 'C': str.setCharAt(i,'^'); break;
				case 'm': str.setCharAt(i,'^'); str.setCharAt(i+1,'p'); break;
				case 'M': str.setCharAt(i,'^'); str.setCharAt(i+1,'P'); break;
				case 'p': str.setCharAt(i,'^'); str.setCharAt(i+1,'p'); break;
				case 'P': str.setCharAt(i,'^'); str.setCharAt(i+1,'P'); break;
				case 'z': str.setCharAt(i,'^'); str.setCharAt(i+1,'R'); break;
				case 'x':
				case 'u':
				case 'v':
				case 'i':
				case '$':
				case 's':
				case 'Z':
				case 'X':
				case 'D': str.setCharAt(i,'^'); str.setCharAt(i+1,'W'); break;
				case 'd': str.setCharAt(i,'^'); str.setCharAt(i+1,'w'); break;
				case '!': str.setCharAt(i,'^'); str.setCharAt(i+1,'.'); break;
				case 'L': str.setCharAt(i,'^'); str.setCharAt(i+1,'!'); break;
			}
			else
			if(str.charAt(i)=='^')
			switch(str.charAt(i+1))
			{
			case 'O': str.setCharAt(i+1,'y'); break;
			}
			else
			if(str.charAt(i)=='&')
			switch(str.charAt(i+1))
			{
			case 'D': str.setCharAt(i,'^'); str.setCharAt(i+1,'!'); break;
			}
		}
		return str.toString();
	}

	/* printkeys: print key-value pairs, escaping values */
	final String printkeys(PACKET data)
	{
		String buf;
		int i;

		buf = "";

		for (i = 0; i < PACKET.IMC_MAX_KEYS; i++)
		{
			if (!data.key[i].equals("") )
			{

				buf = buf + data.key[i] + "=";
				if (data.value[i].equals(""))
					buf = buf + "\"" + data.value[i] + "\"";
				else
					buf = buf + data.value[i];
				buf = buf + " ";
			}
		}
		return buf;
	}

	final public String do_imcstats()
	{
		return "IMC Statistics:\\n\\r\\n"+
			"Received packets:    "+imc_stats.rx_pkts+"\\n\\r"+
			"Received bytes:      "+imc_stats.rx_bytes+"\\n\\r"+
			"Transmitted packets: "+imc_stats.tx_pkts+"\\n\\r"+
			"Transmitted bytes:   "+imc_stats.tx_bytes+"\\n\\r"+
			"Packets dropped:     "+imc_stats.sequence_drops+"\\n\\n\\r"+
			"Last IMC Boot:       "+imc_boot.toString()+"\\n\\r";
	}
	final public String do_imcinfo()
	{
		final String host=CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase();
		return "IMC Statistics:\\n\\r\\n"+
			"Site Name           :"+imc_siteinfo.name+"\\n\\r"+
			"Site Host           :"+((host.length()>0)?host:sa.getLocalAddress().getHostName())+"\\n\\r"+
			"Site Port           :"+imc_siteinfo.port+"\\n\\r"+
			((imc_siteinfo.www.length()>0)?"Web Address         :"+imc_siteinfo.www+"\\n\\r":"")+
			((imc_siteinfo.www.length()>0)?"Email               :"+imc_siteinfo.email+"\\n\\r":"")+
			"Codebase            :"+imc_siteinfo.base+"\\n\\r"+
			"IMC Version         :"+IMC_VERSIONID+"\\n\\r";
	}

	final PACKET interpret2(String argument )
	{
		String seq;
		final PACKET out = new PACKET();

		imc_initdata(out);

		final StringTokenizer st = new StringTokenizer(argument, " ");
		if(st.countTokens() < 5)
		{
			if(st.countTokens() == 4)
			{
				final String tmp = st.nextToken();
				if(tmp.equalsIgnoreCase("autosetup"))
				{
					// who cares what the server says.
					/*
					String serverName = st.nextToken();
					if(st.nextToken().equalsIgnoreCase("accept"))
					{
						String networkName = st.nextToken();
					}
					*/
				}
			}
			else
			{
				tracef(0, "interpret: bad packet received, discarding");
				tracef(0, "interpret: argument was '"+argument+"'");
				imc_stats.sequence_drops++;
			}
			return null;
		}

		if(imc_stats.max_pkt < argument.length())
			imc_stats.max_pkt = argument.length();

		imc_stats.rx_bytes += argument.length();
		imc_stats.rx_pkts++;

		int pos = 0;
		String tmp = st.nextToken();
		out.i.from = tmp; pos+=tmp.length();
		tmp = st.nextToken();
		seq = tmp; pos+=tmp.length();
		tmp = st.nextToken();
		out.i.path = tmp; pos+=tmp.length();
		tmp = st.nextToken();
		out.type = tmp; pos+=tmp.length();
		tmp = st.nextToken();
		out.i.to = tmp; pos+=tmp.length();

		String keys = argument.substring(pos+5, argument.length());

		try
		{

		while((keys.length() > 0) && !keys.equals(" "))
		{
			String key = "";
			String val = "";
			final int kpos = keys.indexOf('=');
			if(kpos > -1)
			{
				key = keys.substring(0, kpos);
				keys = keys.substring(kpos+1, keys.length());
				if(keys.startsWith("\""))
				{
					int p = 0;
					boolean found = false;
					while(!found)
					{
						p = keys.indexOf("\"", p+1);
						if(p > -1)
						{
							final String tmp2 = keys.substring(p-1, p+1);
							if(!tmp2.equals("\\\""))
								found = true;
						}
						else
						{
							p=keys.length();
							found = true;
						}
					}
					val = keys.substring(1, p);
					if(p>=keys.length())
						keys="";
					else
					if(p+2>=keys.length())
						keys = keys.substring(p+1, keys.length());
					else
						keys = keys.substring(p+2, keys.length());
				}
				else
				{
					if(!keys.equals(" "))
					{
						final int npos = keys.indexOf(' ');
						if (npos > -1)
						{
							val = keys.substring(0, npos);
							if(npos+2 > keys.length())
								keys = "";
							else
								keys = keys.substring(npos + 1, keys.length());
						}
						else
						{
							val = keys;
							keys = "";
						}
					}
					else
						keys = "";
				}
			}

			val = normal2(val);
			imc_addkey(out, key, val);
		}

		}
		catch (final StringIndexOutOfBoundsException x)
		{
			Log.errOut("IMC2Driver", "Stringerror: "+keys);
			Log.errOut("IMC2Driver", x);
		}
		catch (final Exception e)
		{
			Log.errOut("IMC2Driver", e);
		}

		out.i.sequence = Long.valueOf(seq).longValue();
		return out;
	}

	final String generate2( PACKET p )
	{
	   String temp;
	   String newpath;

	   if(p.i.path.equals(""))
		  newpath = imc_name;
	   else
		  newpath = imc_name+"!"+p.i.path;

	   temp = p.i.from+" "+p.i.sequence+" "+newpath+" "+p.type+" "+p.i.to+" "+printkeys( p );

	   imc_stats.tx_bytes += temp.length();
	   imc_stats.tx_pkts++;

	   return temp;
	}

	final void do_send_packet( PACKET p )
	{
		String output;

		output = generate2(p);
		do_imcsend(output);
	}

	final IMC_CHANNEL imc_findchannel( String name )
	{
		if(channels.get(name)!=null)
			return (IMC_CHANNEL) channels.get(name);
		return null;
	}

	/* return 'mud' from 'player@mud' */
	final String imc_mudof(String fullname )
	{
	   String buf;

	   final int pos = fullname.indexOf('@');
	   if(pos > -1)
		   buf = fullname.substring(pos+1);
		else
			buf = "*";

	   return buf;
	}

	/* return d from a!b!c!d */
	final String imc_lastinpath(String path)
	{
		final int pos = path.lastIndexOf('!');
		if(pos < 0)
			return path;

		return path.substring(pos+1, path.length());
	}

	/* return 'player' from 'player@mud' */
	final String imc_playerof(String fullname )
	{
	   String buf;

	   final int pos = fullname.indexOf('@');
	   if(pos > -1)
		   buf = fullname.substring(0, pos);
		else
			buf = "*";

	   return buf;
	}

	/* add "key=value" to "p" */
	final void imc_addkey(PACKET p, String key, String value)
	{

		for(int i = 0; i < value.length(); i++)
		{
			if(value.substring(i, i+1).equals("\""))
			{
				String tmp = value.substring(0, i);
				tmp = tmp.concat("\\\"").concat(value.substring(i+1, value.length()));
				value = tmp;
				i++;
			}
		}

		for (int i = 0; i < PACKET.IMC_MAX_KEYS; i++)
		{
			if (!p.key[i].equals("") && (key.equalsIgnoreCase(p.key[i])))
			{
				p.key[i] = "";
				p.value[i] = "";
				break;
			}
		}
		if (value=="")
			return;

		for (int i = 0; i < PACKET.IMC_MAX_KEYS; i++)
		{
			if (p.key[i] == "")
			{
				p.key[i] = key;
				if((value.indexOf(' ') > -1))
					value = "\""+value+"\"";
				p.value[i] = value;
				return;
			}
		}
	}

	/* add "key=value" for an integer value */
	final void imc_addkeyi(PACKET p, String key, int value)
	{
		String temp;
		temp = String.valueOf(value);
		imc_addkey(p, key, temp);
	}

	/* clear all keys in "p" */
	final void imc_initdata(PACKET p)
	{
		int i;

		for (i = 0; i < PACKET.IMC_MAX_KEYS; i++)
		{
			p.key[i] = "";
			p.value[i] = "";
		}
	}

	/* convert back from 'd' to 'p' */
	final void setdata(PACKET p, imc_char_data d)
	{
		imc_initdata(p);

		if (d == null)
		{
			p.from = "*";
			imc_addkeyi(p, "level", -1);
			return;
		}

		p.from = d.name;

		if (d.wizi > 0)
			imc_addkeyi(p, "wizi", d.wizi);
//  	  imc_addkeyi(p, "level", d.level);
	}

	/* send a who-request to a remote mud */
	final void imc_send_who(imc_char_data from, String to, String type)
	{
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return;

		if (imc_mudof(to).equals("*"))
			return; /* don't let them do this */

		setdata(out, from);

		out.to = "*@" + imc_mudof(to);
		out.type = "who";

		imc_addkey(out, "type", type);

		imc_send(out);
	}

	/* send a who-request to a remote mud */
   public final void imc_send_whois(String from, String to, int level)
   {
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return;

		//if (imc_mudof(to).equals("*"))
		//    return; /* don't let them do this */

		imc_initdata(out);

		out.from = from;

		out.to = to+"@*";
		out.type = "whois";

		imc_addkeyi(out, "level", level);

		imc_send(out);
	}

	final void imc_send( PACKET p)
	{
		if (imc_active < IA_UP)
		{
			tracef(0, "imc_send when not active!");
			return;
		}

		/* initialize packet fields that the caller shouldn't/doesn't set */

		p.i.stamp = 0;
		p.i.path = "";

		p.i.sequence = imc_sequencenumber;
		if(p.i.sequence == imc_prev_sequencenumber)
		{
			p.i.sequence++;
			imc_sequencenumber++;
		}

		imc_prev_sequencenumber = imc_sequencenumber;

		p.i.to = p.to;
		p.i.from = p.from;
		p.i.from = p.i.from.concat("@");
		p.i.from = p.i.from.concat(imc_name);

//  	p.i.path = this_imcmud.hubname;

		do_send_packet(p);
	}

	final void imc_loop()
	{
		return;
	}

//
	// list of all active call_out's in the game
	//
	static LinkedList call_outs = new LinkedList();

	public void imc_register_call_out(int hbeat, String function_name, Object param)
	{
		final Vector call_out = new Vector();
		call_out.add(function_name);
		call_out.add(Long.valueOf(HeartBeat + hbeat));
		call_out.add(param);

		call_outs.add(call_out);
	}

	public void imc_process_call_outs()
	{
		if (call_outs.size() < 1)
		{
			return;
		}

		for (int i = 0; i < call_outs.size(); i++)
		{
			final Vector call_out = (Vector) call_outs.get(i);
			final String fun = (String) call_out.elementAt(0);
			final long hbeat = ( (Long) call_out.elementAt(1)).longValue();
			final Object param = call_out.elementAt(2);

			if (hbeat == HeartBeat)
			{
				final Object o = this;
				final Class<?> cl = o.getClass();
				final java.lang.reflect.Method funcs[] = cl.getMethods();
				if (funcs.length > 1)
				{
					for (final Method func : funcs)
					{
						final String m_name = func.getName();
						if (m_name.equals(fun))
						{
							try
							{
								func.invoke(o, new Object[] {param});
							}
							catch (final Exception e)
							{
								tracef(0,
									   "imc: call_out failed with error: "
									   + e.toString());
							 }
						}
					}
				}
				call_outs.remove(i);
				i--;
			}
		}
	}

	final public static String toIMCColours(String res)
	{
		// ANSI color macros
		res = CMStrings.replaceAll(res, "\"", "\\\"");
		final StringBuffer str=new StringBuffer(res);
		for(int i=0;i<str.length()-1;i++)
		{
			if(str.charAt(i)=='`')
				str.setCharAt(i,'\'');
			else
			if(str.charAt(i)=='^')
			switch(str.charAt(i+1))
			{
				case ColorLibrary.COLORCODE_BACKGROUND:
				  if(i<str.length()-2)
					  str.delete(i,i+3);
				  break;
				case ColorLibrary.COLORCODE_FANSI256:
				case ColorLibrary.COLORCODE_BANSI256:
				  if(i<str.length()-4)
					  str.delete(i,i+5);
				  break;
				case 'R':
				case 'r':
				case 'y':
				case 'Y':
				case 'g':
				case 'G':
				case 'b':
				case 'B':
				case 'w':
				case 'W':
				case 'c':
				case 'C': str.setCharAt(i,'~'); break;
				case 'm': str.setCharAt(i,'~'); str.setCharAt(i+1,'p'); break;
				case 'M': str.setCharAt(i,'~'); str.setCharAt(i+1,'P'); break;
				case 'p': str.setCharAt(i,'~'); str.setCharAt(i+1,'m'); break;
				case 'P': str.setCharAt(i,'~'); str.setCharAt(i+1,'M'); break;
				case '?':
				case '.': str.setCharAt(i,'~'); str.setCharAt(i+1,'!'); break;
				case '!': str.setCharAt(i,'~'); str.setCharAt(i+1,'L'); break;
				case '*': str.setCharAt(i,'~'); str.setCharAt(i+1,'$'); break;
				case '/': str.setCharAt(i,'~'); str.setCharAt(i+1,'s'); break;
				case '_': str.setCharAt(i,'~'); str.setCharAt(i+1,'u'); break;
				case '^': str.setCharAt(i,'^'); str.deleteCharAt(i+1); break;
				default:
					str.setCharAt(i,'~'); str.setCharAt(i+1,'c');
					break;
			}
		}
		return str.toString();
	}

	final String update_wholist()
	{
		final StringBuffer str=new StringBuffer("Players on "+CMProps.getVar(CMProps.Str.MUDNAME)+":\\n\\r");
		final Command C=CMClass.getCommand("Who");
		if(C!=null)
		{
			String msg=null;
			try
			{
				msg=(String)C.executeInternal(null,0);
			}
			catch(final Exception e)
			{
			}
			if(msg!=null)
			{
				msg=CMStrings.replaceAll(msg,"\n\r","\\n\\r");
				return str.toString()+msg;
			}
		}
		return "Unavailable";
	}

	final String who_help()
	{
		return "\\n\\r"+
			"Available imcminfo types:\\n\\r"+
			"help   - this screen\\n\\r"+
			"who    - who is online\\n\\r"+
			"info   - mud information\\n\\r"+
			"who    - who is online\\n\\r";
	}

	final void imc_recv_who(imc_char_data from, String sender, String type)
	{
		final PACKET out = new PACKET();

		imc_initdata(out);
		out.from = from.name;
		imc_addkeyi(out, "level", 3);

		out.to = sender;
		out.type = "who-reply";

		if(type.equals("who"))
			imc_addkey(out, "text", toIMCColours(update_wholist()));
		else
		if(type.equals("istats"))
			imc_addkey(out, "text", toIMCColours(do_imcstats()));
		else
		if(type.equals("info"))
			imc_addkey(out, "text", toIMCColours(do_imcinfo()));
		else
		if(type.equals("help"))
			imc_addkey(out, "text", toIMCColours(who_help()));

		imc_send(out);
	}

	final void imc_recv_whois(imc_char_data from, String sender, int level)
	{
		final PACKET out = new PACKET();

		imc_initdata(out);
		out.from = from.name;
		imc_addkeyi(out, "level", 3);

		out.to = sender;
		out.type = "whois-reply";

		final MOB M=CMLib.players().getPlayer(from.name);
		if(M==null)
			return;
		imc_addkey(out, "text", "imcpfind "+from.name+"@"+imc_name
						+" is "+(CMLib.flags().isInTheGame(M,false) ? "online":"offline")+ ".");

		imc_send(out);
	}

	final void imc_recv_ping(imc_char_data from, String path, String sender)
	{
		final PACKET out = new PACKET();

		imc_initdata(out);
		out.from = from.name;

		out.to = sender;
		out.type = "ping-reply";

		imc_addkey(out, "path", path);

		imc_send(out);
	}

	final void imc_recv_tell(imc_char_data d, String from, String text)
	{
		final MOB mob=CMClass.getFactoryMOB();
		mob.setName(from);
		mob.setLocation(CMClass.getLocale("StdRoom"));
		final MOB smob=CMLib.sessions().findPlayerOnline(d.name,true);
		if(smob!=null)
			CMLib.commands().postSay(mob,smob,text,true,true);
		final Room R=mob.location();
		mob.destroy();
		if(R!=null)
			R.destroy();
	}

	public REMOTEINFO getIMC2Mud(String named)
	{
		final Hashtable l=query_muds();
		if(l.containsKey(named))
			return (REMOTEINFO)l.get(named);
		for(final Enumeration e=l.elements();e.hasMoreElements();)
		{
			final REMOTEINFO m=(REMOTEINFO)e.nextElement();
			if(m.name.equalsIgnoreCase(named))
				return m;
		}
		return null;
	}

	final void imc_recv_chat(imc_char_data d, String from, String channel, String text, int emote)
	{
		final StringTokenizer st = new StringTokenizer(channel, ":");
		if (st.countTokens() > 1)
		{
			channel = st.nextToken();
			channel = st.nextToken();
		}
		else
		if(st.countTokens()> 0)
			channel=st.nextToken();

		String channelName=channel;
		CMMsg msg=null;
		if(from.toUpperCase().endsWith(imc_name.toUpperCase()))
			return;
		if(channelName.length()==0)
			return;
		channelName=read_channel_name(channelName);
		if(channelName.length()==0)
			return;
		final int channelInt=CMLib.channels().getChannelIndex(channelName);
		if(channelInt<0)
			return;
		final String channelColor="^Q";
		final MOB mob=CMClass.getFactoryMOB();
		mob.setName(from);
		mob.setLocation(CMClass.getLocale("StdRoom"));
		String str=channelColor+"^<CHANNEL \""+channelName+"\"^>"+mob.name()+" "+channelName+"(S) '"+text+"'^</CHANNEL^>^N^.";
		if(emote>0)
		{
			if(emote==1)
				str=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+from+" "+text+"^</CHANNEL^>^N^.";
			else
				str=channelColor+"^<CHANNEL \""+channelName+"\"^>["+channelName+"] "+text+"^</CHANNEL^>^N^.";
		}

		msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelInt),str);

		CMLib.channels().channelQueUp(channelInt,(CMMsg)msg.copyOf());
		for(final Session S : CMLib.sessions().localOnlineIterable())
			if((CMLib.channels().mayReadThisChannel(mob,false,S,channelInt))
			&&(S.mob().location()!=null)
			&&(S.mob().location().okMessage(S.mob(),msg)))
				S.mob().executeMsg(S.mob(),msg);

		LinkedList l = (LinkedList) chanhist.get(channel);
		if(l == null)
			l = new LinkedList();

		l.add(str);
		chanhist.put(channel, l);
		final Room R=mob.location();
		mob.destroy();
		if(R!=null)
			R.destroy();
	}

	final void imc_recv_is_alive(imc_char_data d, String from, String path,
						   String version, String netname)
	{
		final String mudname = imc_mudof(from);
		final REMOTEINFO rinfo = new REMOTEINFO();
		rinfo.name = mudname;
		rinfo.network = netname;
		rinfo.version = version;

		final int pos = path.indexOf('!');
		if(pos > -1)
		{
			final String hubs = path.substring(pos+1, path.length());
			final int pos2 = hubs.indexOf('!');
			if(pos2 > -1)
				rinfo.hub = hubs.substring(0, pos2);
			else
				rinfo.hub = hubs;
		}
		else
			rinfo.hub = "None";

		muds.put(mudname, rinfo);
	}

	final void imc_recv_who_reply(imc_char_data d, String text)
	{
		text = CMStrings.replaceAll(text, "\\n\\r", "\n\r");
		send_to_player(d.name, "\n\r"+text+"^.^?");
	}

	final void imc_recv_whois_reply(imc_char_data d, String to, String text)
	{
		text = CMStrings.replaceAll(text, "\\n\\r", "\n\r");
		send_to_player(d.name, "\n\r"+to+": "+text+"^.^?");
	}

	final void imc_recv_ping_reply(imc_char_data d, String from, String path)
	{
	   final String route[] = explodeNicely(path, "!");

	   final StringBuffer text = new StringBuffer("Traceroute information for "+from+"\n\r");

	   text.append("^cSend path:^?   ");
	   for(int i = 0; i < route.length; i++)
	   {
		   if (i > 0)
			   text.append("->");
		   text.append(route[i]);
	   }
	   text.append("\n\r");
	   text.append("^cReturn path:^? ");
	   for(int i= route.length-1; i >= 0; i--)
	   {
		   if (i < route.length-1)
			   text.append("->");
		   text.append(route[i]);
	   }

	   if(CMSecurity.isDebugging(DbgFlag.IMC2))
		   Log.debugOut("IMC2Driver",text.toString());
	   //send_to_player(d.name, text.toString());
   }

	final String read_channel_name(String channame)
	{
		if(channame==null)
			return "";
		channame=channame.toUpperCase();
		for(final Enumeration e=chan_conf.keys();e.hasMoreElements();)
		{
			final String key=(String)e.nextElement();
			final String val=((String)chan_conf.get(key)).toUpperCase();
			if(val.equals(channame))
				return key;
			if(val.endsWith(":"+channame))
				return key;
			if(channame.endsWith(":"+val))
				return key;
		}
		return "";
	}

	public IMC_CHANNEL getAnIMC2Channel(String name)
	{
		name=name.toUpperCase();
		for(final Enumeration e=channels.elements();e.hasMoreElements();)
		{
			final IMC_CHANNEL c=(IMC_CHANNEL)e.nextElement();
			if(read_channel_name(c.name).equalsIgnoreCase(name))
				return c;
		}
		return null;
	}

	final void imc_recv_update(String from, String chan, String owner,
						 String operators, String policy, String invited,
						 String excluded )
	{
		IMC_CHANNEL c;
		String mud;

		mud = imc_mudof(from);

		/* forged? */
		if (chan.indexOf(':')<-1 || !mud.equalsIgnoreCase(imc_mudof(chan)))
			return;

		c = new IMC_CHANNEL();
		c.name = chan;
		c.owner = owner;
		c.operators = operators;
		c.invited = invited;
		c.excluded = excluded;
		c.local_name = this.read_channel_name(chan);
		c.perm_level = PERM_ADMIN;
		c.refreshed = true;

		if (policy.equalsIgnoreCase("open"))
			c.policy = CHAN_OPEN;
		else if (policy.equalsIgnoreCase("closed"))
			c.policy = CHAN_CLOSED;
		else if (policy.equalsIgnoreCase("copen"))
			c.policy = CHAN_COPEN;
		else if (policy.equalsIgnoreCase("cprivate"))
			c.policy = CHAN_CPRIVATE;
		else
			c.policy = CHAN_PRIVATE;

		channels.put(c.name, c);
   }

	/* get the value of "key" from "p"; if it isn't present, return "def" */
	final String imc_getkey( PACKET p, String key, String def )
	{
	   int i;

	   for( i = 0; i < PACKET.IMC_MAX_KEYS; i++ )
	{
		  if( !p.key[i].equals("") && p.key[i].equalsIgnoreCase(key))
		  {
			  if(p.value[i].startsWith("\""))
				  return p.value[i].substring(1, p.value[i].length()-1);
			  return p.value[i];
		  }

	}
	   return def;
	}

	/* identical to imc_getkey, except get the integer value of the key */
	final int imc_getkeyi( PACKET p, String key, int def )
	{
	   int i;

	   for( i = 0; i < PACKET.IMC_MAX_KEYS; i++ )
	{
		  if( !p.key[i].equals("") && p.key[i].equalsIgnoreCase(key) )
		  {
			  return CMath.s_int(p.value[i]);
		  }

	}
	   return def;
	}

	final public Hashtable query_channels()
	{
		return channels;
	}

	final public Hashtable query_muds()
	{
		return muds;
	}

	final public void exec_commands(PACKET p)
	{
		if (p == null)
			return;
		final imc_char_data d = new imc_char_data();

		d.name = p.i.to;
		d.wizi = imc_getkeyi(p, "wizi", 0);
		d.level = imc_getkeyi(p, "level", 0);
		d.invis = 0;

		final String to_mud = imc_mudof(d.name);
		if (!to_mud.equalsIgnoreCase(imc_name) && !to_mud.equals("*"))
		{
			tracef(8, "Message was for "+to_mud+", not "+imc_name+" -- rejecting!");
			return;
		}

		d.name = imc_playerof(d.name);
		tracef(8, "Received message was sent to " + d.name+", "+p.type+", "+CMParms.toListString(p.value));

		if (p.type.equals("who"))
		{
			tracef(8, "Who request received from " + p.i.from);
			imc_recv_who(d, p.i.from, imc_getkey(p, "type", "who"));
		}

		if (p.type.equals("whois"))
		{
			tracef(8, "Whois request received from " + p.i.from);
			imc_recv_whois(d, p.i.from, imc_getkeyi(p, "level", 0));
		}

		if (p.type.equals("ping"))
		{
			tracef(8, "Ping reply received from " + p.i.from);
			imc_recv_ping(d, p.i.path, p.i.from);
		}

		if (p.type.equals("tell"))
		{
			tracef(8, "Tell received from " + p.i.from);
			imc_recv_tell(d, p.i.from, imc_getkey(p, "text", ""));
		}

		if (p.type.equals("keepalive-request"))
		{
			tracef(8, "Keepalive request received from " + p.i.from);
			imc_send_isalive(p.i.from);
		}

		if (p.type.equals("who-reply"))
		{
			tracef(8, "Who reply received from " + p.i.from);
			imc_recv_who_reply(d, imc_getkey(p, "text", ""));
		}

		if (p.type.equals("whois-reply"))
		{
			tracef(8, "Who-Is reply received from " + p.i.from);
			imc_recv_whois_reply(d, p.i.from, imc_getkey(p, "text", ""));
		}

		if (p.type.equals("ping-reply"))
		{
			tracef(8, "Ping-reply reply received from " + p.i.from);
			imc_recv_ping_reply(d, p.i.from, imc_getkey(p, "path", ""));
		}

		if (p.type.equals("is-alive"))
		{
			tracef(8, "is-alive received from " + p.i.from);
			imc_recv_is_alive(d, p.i.from, p.i.path,
							  imc_getkey(p, "versionid", "Unknown"),
							  imc_getkey(p, "networkname", "None"));
		}

		if (p.type.equals("ice-msg-b"))
		{
			tracef(8, "Chat received from " + p.i.from);
			imc_recv_chat(d, p.i.from, imc_getkey(p, "channel", "ICHAT"),
						  imc_getkey(p, "text", ""),CMath.s_int(imc_getkey(p,"emote","0")));
		}

		if (p.type.equals("ice-update"))
		{
			tracef(8, "Ice update reply from " + p.i.from);
			imc_recv_update(p.from, imc_getkey(p, "channel", ""),
							imc_getkey(p, "owner", ""),
							imc_getkey(p, "operators", ""),
							imc_getkey(p, "policy", ""),
							imc_getkey(p, "invited", ""),
							imc_getkey(p, "excluded", ""));

		}
	}

	final boolean check_password(String s)
	{
		final StringTokenizer st = new StringTokenizer(s, " ");
		if (st.countTokens() < 3)
		{
			tracef(0, "Password not found in Hub reply.");
			return false;
		}

		st.nextToken();
		st.nextToken();
		final String pwd = st.nextToken();

		if (pwd.equals(this_imcmud.serverpw))
		{
			tracef(8, "Password OK.");
			return true;
		}
		tracef(0, "Password incorrect.");
		return false;
	}

	final public void imc_read_from_socket(BufferedReader in)
	{
		try
		{
			if ((in!=null)&&(in.ready()))
			{
				final String s = in.readLine();
				if (s == null) return;

				if (s.length() > 0)
				{
					tracef(8, "imc: received '" + s + "'");
					if (s.startsWith("PW "))
						check_password(s);
					else
					{
						final PACKET p = interpret2(s);
						if(p!=null)
							exec_commands(p);
					}
				}
			}
		}
		catch (final Exception e)
		{
			final String errMsg=e.getMessage()==null?e.toString():e.getMessage();
			if((errMsg==null)
			||(errMsg.toUpperCase().indexOf("TIMED OUT")<0))
				Log.errOut("IMC2Driver", "read: "+errMsg);
			if((errMsg!=null)
			&&(!shutdown)
			&&(errMsg.toUpperCase().indexOf("CONNECTION")>=0))
			{
				imc_active = IA_NONE;
				tracef(1, "Waiting "+(reconnectBackoffTime/1000)+" seconds and try to reconnect.");
				try
				{
					sleep(reconnectBackoffTime);
					reconnectBackoffTime=reconnectBackoffTime*2;
				}
				catch (final Exception ex)
				{
				}
				this.imc_startup(true,
								 imc_name,
								 imc_siteinfo.host,
								 imc_siteinfo.email,
								 imc_siteinfo.www,
								 this_imcmud.hubname,
								 this_imcmud.port,
								 this_imcmud.clientpw,
								 this_imcmud.serverpw,
								 rebuildChannelMap());
			}
		}
	}

	final public void imc_write_to_socket(DataOutputStream out)
	{
		try
		{
			if (this_imcmud.outbuf.equals(""))
				return;

			tracef(8, "imc: sending '" + this_imcmud.outbuf + "'");
			out.write(this_imcmud.outbuf.getBytes());
			this_imcmud.outbuf = "";
		}
		catch (final Exception e)
		{
			tracef(1, "write socket error: " + e.toString());
			imc_active = IA_NONE;
			tracef(1, "Waiting "+(reconnectBackoffTime/1000)+" seconds and try to reconnect.");
			try
			{
				sleep(reconnectBackoffTime);
				reconnectBackoffTime=reconnectBackoffTime*2;
			}
			catch (final Exception ex)
			{
			}
			this.imc_startup(true,
							 imc_name,
							 imc_siteinfo.host,
							 imc_siteinfo.email,
							 imc_siteinfo.www,
							 this_imcmud.hubname,
							 this_imcmud.port,
							 this_imcmud.clientpw,
							 this_imcmud.serverpw,
							 rebuildChannelMap());
		}
	}

	/* send a keepalive to everyone */
	final public void imc_send_isalive(String reqFrom)
	{
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return;

		imc_initdata(out);
		out.type = "is-alive";
		out.from = "*";
		if(reqFrom.endsWith("*"))
		{
			Log.errOut("IMC2 SPAM DETECTED: "+reqFrom+"!");
		}
		out.to = reqFrom;
		imc_addkey(out, "versionid", IMC_VERSIONID);
		imc_addkey(out, "networkname", this_imcmud.network);
		if (imc_siteinfo.flags != null && !"".equals(imc_siteinfo.flags))
			imc_addkey(out, "flags", imc_siteinfo.flags);
		imc_send(out);
	}

	/* send a keepalive to everyone */
	final public void imc_request_keepalive()
	{
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return;

		imc_initdata(out);
		out.type = "keepalive-request";
		out.from = "*";
		out.to = "*@*";
		imc_addkey(out, "versionid", IMC_VERSIONID);
		if (imc_siteinfo.flags != null && !"".equals(imc_siteinfo.flags))
			imc_addkey(out, "flags", imc_siteinfo.flags);
		imc_send(out);
	}

	final class call_out extends Thread
	{

		IMC2Driver imc_client;
		boolean shutdown=false;
		public boolean isShutdown=false;
		int seq = 0;
		long lastPingReceived=System.currentTimeMillis();

		public call_out(IMC2Driver _imc_client)
		{
			super("IMC2-call_out");
			setName("IMC2-call_out");
			imc_client = _imc_client;
			setDaemon(true);
		}

		public void shutdown()
		{
			shutdown=true;
			interrupt();
		}

		@Override
		final public void run()
		{
			if (imc_client == null)
				return;
			long timeSinceLastPing=System.currentTimeMillis();
			while (!shutdown)
			{
				if(CMSecurity.isDisabled(DisFlag.IMC2))
				{
					CMLib.s_sleep(100);
					continue;
				}
				if((System.currentTimeMillis()-timeSinceLastPing) > (30 * 60 * 1000))
				{
					imc_client.imc_send_ping("Server01");
					timeSinceLastPing=System.currentTimeMillis();
					// everything is taken care of from here.  detect disconnect, reconnect -- everything.
				}
//  			tracef(1, "call_out: process call outs");
				imc_client.imc_process_call_outs();
				imc_client.imc_write_to_socket(out);
				try
				{
					sleep(100);
					seq++;
					if (seq % 10 == 0)
					{
						imc_client.imc_sequencenumber++;
						seq = 0;
					}
				}
				catch (final Exception e)
				{
				}

			}
			isShutdown=true;
		}
	}

	final class call_in extends Thread {

		IMC2Driver imc_client;
		boolean shutdown=false;
		public boolean isShutdown=false;

		public call_in(IMC2Driver _imc_client)
		{
			super("IMC2-call_in");
			setName("IMC2-call_in");
			imc_client = _imc_client;
			setDaemon(true);
		}

		public void shutdown()
		{
			shutdown=true;
			interrupt();
		}

		@Override
		final public void run()
		{
			if (imc_client == null)
				return;
			while (!shutdown)
			{
				if(!CMSecurity.isDisabled(DisFlag.IMC2))
				{
//	  			tracef(1, "call_out: process call outs");
					imc_client.imc_read_from_socket(in);
				}
				try
				{
					sleep(100);
				}
				catch (final Exception e)
				{
				}
			}
			isShutdown=true;
		}

	}

	/* send a ping with a given timestamp */
	final public void imc_send_ping(String to)
	{
		PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return;

		imc_initdata(out);
		out.type = "ping";
		out.from = "*";
		out.to = "*@" + to;
		imc_send(out);
		out = null;
	}

	final public void run_imcpinfo(String name, String mudname, String who, int level,
							 int invis)
							 {
		final imc_char_data test = new imc_char_data();
		test.name = name;
		test.level = level;
		test.invis = invis;
		imc_send_who(test, "@" + mudname, "finger " + who);
	}

	final public void run_imcminfo(String name, String mudname, String type,
							 int level, int invis)
							 {
		final imc_char_data test = new imc_char_data();
		test.name = name;
		test.level = level;
		test.invis = invis;
		imc_send_who(test, "@" + mudname, type);
	}

	final public void imc_send_who(String name, String mudname, String type, int level, int invis)
	{
		final imc_char_data test = new imc_char_data();
		test.name = name;
		test.level = level;
		test.invis = invis;
		imc_send_who(test, "@" + mudname, type);
	}

	final public String imc_send_tell(String from, String to, String text, int level,
								int invis)
								{
		final imc_char_data chr = new imc_char_data();
		chr.name = from;
		chr.level = level;
		chr.invis = invis;
		return imc_send_tell(chr, to, text, 1);
	}

	final public String imc_send_reply(String from, String text, int level, int invis)
	{
		final imc_char_data chr = new imc_char_data();
		chr.name = from;
		chr.level = level;
		chr.invis = invis;
		return imc_send_reply(chr, text);
	}

	final public String imc_send_chat(String from, String to, String text, int level,
								int emote)
								{
		final imc_char_data chr = new imc_char_data();
		chr.name = from;
		chr.level = level;
		return imc_send_chat(chr, to, text, level, emote);
	}

	/* send a tell to a remote player */
	final String imc_send_tell(imc_char_data from, String to, String argument,
						 int isreply)
						 {
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return "IMC is not active.";

		if (imc_mudof(to).equals("*"))
		{
			return "You cannot send tell to everyone!";
				/* don't let them do this */
		}

		setdata(out, from);

		out.to = to;
		out.type = "tell";
		imc_addkeyi(out, "level", 3);
		imc_addkey(out, "text", toIMCColours(argument));

		imc_send(out);

		replies.put(from.name, to);

		final String chatText = "You tell " + to + " '^c" + argument +
			"'^?.";
		return chatText;
	}

	/* send a reply to a remote player */
	final String imc_send_reply(imc_char_data from, String argument)
	{
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return "IMC is not active.";

		setdata(out, from);
		final String to = (String) replies.get(from.name);
		if (to == null)
			return "Noone to reply to.";

		out.to = to;
		out.type = "tell";
		imc_addkeyi(out, "level", 3);
		imc_addkey(out, "text", toIMCColours(argument));

		imc_send(out);

		final String chatText = "You tell " + to + " ^c'" + argument +
			"'^?.";
		return chatText;
	}

	/* send a tell to a remote player */
	final String imc_send_chat(imc_char_data from,
								String to,
								String argument,
								int isreply,
								int emote)
								{
		final PACKET out = new PACKET();

		if (imc_active < IA_UP)
			return "IMC is not running.";

		setdata(out, from);

//  	imc_addkey(p, "level", 3);

		out.to = "*@*";
		out.type = "ice-msg-b";
		imc_addkey(out, "channel", to);
		imc_addkey(out, "text", toIMCColours(argument));
		imc_addkeyi(out, "emote", emote);

		/*  	if (isreply > 0)
				  imc_addkeyi(out, "isreply", isreply);*/

		imc_send(out);

		String chan = to;
		final int pos = to.indexOf(':');
		if (pos > -1)
			chan = to.substring(pos + 1, to.length());

		String text = argument;
		if (argument.startsWith(", ") || emote == 1)
			text = from.name + "@" + this.imc_name;

		final String chatText = from.name + "@" + this.imc_name +
			" [" + chan +
			"] " +
			" " + text + "^?";

		LinkedList l = (LinkedList) chanhist.get(chan);
		if (l == null)
			l = new LinkedList();

		l.add(chatText);
		chanhist.put(chan, l);

		return chatText;

	}

	@Override
	final public void run()
	{
		imc_read_from_socket(in);
		HeartBeat = 0;

		if(c_thread2==null)
		{
			c_thread2 = new call_in(this);
			c_thread2.start();
		}
		if(c_thread==null)
		{
			c_thread = new call_out(this);
			c_thread.start();
		}
		Log.sysOut("IMC2","(c) 1996-2002/Java port by Istvan David");
		Log.sysOut("IMC2","Client connected to "+this_imcmud.host.trim());

		while (!shutdown)
		{
			HeartBeat++;
			try
			{
				sleep(2000);
			}
			catch (final Exception e)
			{
			}
			isShutdown=true;
		}
	}

}
