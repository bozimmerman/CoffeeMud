/**
 * IMC2 version 0.10 - an inter-mud communications protocol
 * Copyright (C) 1996 & 1997 Oliver Jowett <oliver@randomly.org>
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

package com.planet_ink.coffee_mud.i3.imc2;

import java.util.*;
import java.io.*;
import java.net.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

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

    Date imc_boot = new Date();
    long imc_now,imc_sequencenumber,imc_prev_sequencenumber;
    public hubinfo this_imcmud = new hubinfo();
    public siteinfo imc_siteinfo = new siteinfo();
    imc_statistics imc_stats = new imc_statistics();
    int imc_active; /* Connection state */

    long HeartBeat = 0; // elapsed heartbeats

    Hashtable muds = new Hashtable();
    Hashtable channels = new Hashtable();
    public Hashtable chanhist = new Hashtable();
    Hashtable replies = new Hashtable();
    
    public Hashtable chan_conf = new Hashtable();
	public Hashtable chan_mask = new Hashtable();

    DataInputStream in;
    DataOutputStream out;

    public String imc_name = "";
    public short imc_log_on;

    /* refresh timeout */
    final static int  IMC_TIMEOUT = 650;

    final static int CHAN_OPEN     = 1;
    final static int CHAN_CLOSED   = 2;
    final static int CHAN_PRIVATE  = 3;
    final static int CHAN_COPEN    = 4;
    final static int CHAN_CPRIVATE = 5;

    /* max length of any mud name */
    final static int IMC_MNAME_LENGTH = 20;

    /* max length of any player name */
    final static int IMC_PNAME_LENGTH = 40;

    /* max length of any player@mud name */
    final static int IMC_NAME_LENGTH = (IMC_MNAME_LENGTH + IMC_PNAME_LENGTH + 1);

    /* activation states */
    final static int IA_NONE = 0;
    final static int IA_CONFIG1 = 1;
    final static int IA_CONFIG2 = 2;
    final static int IA_UP = 3;

    /* connection states */
    final static int IMC_CLOSED = 0; /* No active connection */
    final static int IMC_CONNECTING = 1; /* Contacting hub */
    final static int IMC_WAIT1 = 2; /* Waiting for hub verification */
    final static int IMC_CONNECTED = 3; /* Fully connected */

    final static int IMC_VERSION = 2;
    final static String IMC_VERSIONID  = "IMC2 4.00 for JavaMUD";

    final void tracef(int level, String s)
    {
        // this is the log function
        // it should be replaced with the mud driver log function
        // if other muds are using this class
        Log.errOut("IMC2",s);
    }

    final void send_to_player(String name, String text)
    {
		if(name.equalsIgnoreCase("all"))
		{
			for(int s=0;s<Sessions.size();s++)
				Sessions.elementAt(s).println(text);
		}
		else
		{
			MOB M=CMMap.getPlayer(name);
			if(M!=null) M.tell(text);
		}
    }
	
	final public String[][] buildChannelMap(String s)
	{
		
		Vector V=Util.parseCommas(s,true);
		Vector finalV=new Vector();
		for(int v=0;v<V.size();v++)
		{
			String s2=(String)V.elementAt(v);
			Vector V2=Util.parse(s2);
			String[] bit=new String[3];
			bit[0]="";
			bit[1]="";
			bit[2]="";
			if(V2.size()<1) continue;
			bit[0]=(String)V2.elementAt(0);
			if(V2.size()==1)
				bit[2]=(String)V2.elementAt(0);
			else
			{
				bit[2]=(String)V2.lastElement();
				if(V2.size()>2)
					bit[1]=Util.combine(V2,1,V.size()-1);
			}
			
			finalV.addElement(bit);
		}
		String[][] finalS=new String[finalV.size()][3];
		for(int i=0;i<finalV.size();i++)
			finalS[i]=(String[])finalV.elementAt(i);
		return finalS;
	}
	final public String[][] buildChannelMap()
	{
		String[][] map=new String[chan_conf.size()][3];
		int dex=0;
		for(Enumeration e=chan_conf.keys();e.hasMoreElements();)
		{
			map[dex][0]=(String)e.nextElement();
			map[dex][1]=(String)chan_mask.get(map[dex][0]);
			map[dex][2]=(String)chan_conf.get(map[dex][0]);
		}
		return map;
	}

	final public static String[] explodeNicely(String s)
	{
	      return explodeNicely(s, " ");
	}

	final public static String[] explodeNicely(String s, String separator)
	{
	    StringTokenizer st = new StringTokenizer(s, separator);

	    int n = st.countTokens();
	    if(n==0)
	    {
	        String array[] = new String[1];
	        array[0] = s;
	        return array;
	    }

	    String array[]  = new String[n];

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
       this_imcmud.outbuf = line+"\n\r";
    }

    /* connect to hub */
    final boolean imc_connect_to() {
        int desc;
        String buf;
        int r;

        if (imc_active == IA_NONE) {
            tracef(0, "IMC is not active");
            return false;
        }

        tracef(8, "Connecting to " + this_imcmud.hubname);

        try {
            sa = new Socket(this_imcmud.host, this_imcmud.port);
        } catch (Exception e) {
            tracef(0, "Error connecting to " + this_imcmud.host + ":" +
                          this_imcmud.port);
            return false;

        }
        if (sa == null) {
            tracef(0, "Error connecting to " + this_imcmud.host + ":" +
                          this_imcmud.port);
            return false;
        }

        try {
            in = new DataInputStream(sa.getInputStream());
            out = new DataOutputStream(sa.getOutputStream());
        } catch(Exception e) { }

        this_imcmud.state = IMC_CONNECTING;
        this_imcmud.insize = 1024;
        this_imcmud.outsize = 1024;

        buf = "PW " + imc_name + " " + this_imcmud.clientpw + " version=" +
            IMC_VERSION;
        do_imcsend(buf);

        imc_write_to_socket(out);
        imc_read_from_socket(in);

        return true;
    }


    final public void ev_keepalive(Object param)
    {
        imc_send_keepalive();
        imc_register_call_out(150, "ev_keepalive", null);
    }

    public void ev_request_keepalive(Object param)
    {
        imc_request_keepalive();
        //imc_register_call_out(25, "ev_keepalive", null);
    }

    /* start up IMC */
    final boolean imc_startup_network() {
        if (imc_active != IA_CONFIG2) {
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

    final public void ev_imc_firstrefresh(Object param)
    {
        PACKET out = new PACKET();

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
									  String[][] channelMap)
    {
       if( imc_active != IA_NONE )
       {
          tracef(0,  "imc_startup: called with imc_active = "+imc_active );
          return false;
       }

       imc_now = new Date().getTime()/1000;                  /* start our clock */
       imc_boot = new Date();

       imc_sequencenumber = imc_now;

        for(int i=0;i<channelMap.length;i++)
		{
			chan_mask.put(channelMap[i][0],channelMap[i][1]);
			chan_conf.put(channelMap[i][0],channelMap[i][2]);
		}
		
		imc_name=loginName;
		imc_log_on=1; // logging?
		this_imcmud.autoconnect=true; 
		imc_siteinfo.name=CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME);
		imc_siteinfo.host=host;
		imc_siteinfo.port=Util.s_int((String)Util.parse(CommonStrings.getVar(CommonStrings.SYSTEM_MUDPORTS)).elementAt(0));
		imc_siteinfo.email=email;
		imc_siteinfo.base="CoffeeMud v"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDVER);
		imc_siteinfo.details="Custom Java-based Mud";
		imc_siteinfo.www=web;
		this_imcmud.hubname=hub;
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
               return true;
            imc_active = IA_NONE;
       }
       return false;
    }

    final String escape2(String data)
    {
        data = Util.replaceAll(data, "\"", "\\\"");
        return data;
    }

    final String normal2(String data)
    {
//        data = Util.replaceAll(data, "\\\"", "\"");

        data = Util.replaceAll(data, "^r", "&BOLD&RED");
        data = Util.replaceAll(data, "^R", "&RED");
        data = Util.replaceAll(data, "^y", "&BOLD&YELLOW");
        data = Util.replaceAll(data, "^Y", "&YELLOW");
        data = Util.replaceAll(data, "^g", "&BOLD&GREEN");
        data = Util.replaceAll(data, "^G", "&GREEN");
        data = Util.replaceAll(data, "^b", "&BOLD&BLUE");
        data = Util.replaceAll(data, "^B", "&BLUE");
        data = Util.replaceAll(data, "^p", "&BOLD&MAGENTA");
        data = Util.replaceAll(data, "^P", "&MAGENTA");
        data = Util.replaceAll(data, "^w", "&BOLD&BLACK");
        data = Util.replaceAll(data, "^W", "&BLACK");
        data = Util.replaceAll(data, "^c", "&BOLD&CYAN");
        data = Util.replaceAll(data, "^C", "&CYAN");
        data = Util.replaceAll(data, "^W", "&OFF");
        data = Util.replaceAll(data, "~w", "&WHITE");

        return data;
    }

    /* printkeys: print key-value pairs, escaping values */
    final String printkeys(PACKET data) {
        String buf;
        String temp;
        int len = 0;
        int i;

        buf = "";

        for (i = 0; i < PACKET.IMC_MAX_KEYS; i++) {
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

    final PACKET interpret2(String argument )
    {
        String seq;
        PACKET out = new PACKET();

        imc_initdata(out);

        StringTokenizer st = new StringTokenizer(argument, " ");
        if(st.countTokens() < 5)
        {
            tracef(0, "interpret: bad packet received, discarding");
            tracef(0, "interpret: argument was '"+argument+"'");
            imc_stats.sequence_drops++;
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

        try {

        while((keys.length() > 0) && !keys.equals(" "))
        {
            String key = "";
            String val = "";
            int kpos = keys.indexOf("=");
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
                            String tmp2 = keys.substring(p-1, p+1);
                            if(!tmp2.equals("\\\""))
                                found = true;
                        }
                        else
                            found = true;
                    }

                    val = keys.substring(1, p);
                    keys = keys.substring(p+2, keys.length());
                }
                else
                {
                    if(!keys.equals(" ")) {
                        int npos = keys.indexOf(" ");
                        if (npos > -1) {
                            val = keys.substring(0, npos);
                            if(npos+2 > keys.length())
                                keys = "";
                            else
                                keys = keys.substring(npos + 1, keys.length());
                        }
                        else {
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

        } catch (Exception e) {
                Log.errOut("IMC2Driver", "interpret2: exception: "+e.toString());
        }

        out.i.sequence = new Long(seq).longValue();
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
        IMC_CHANNEL c;

        if(channels.get(name)!=null)
                return (IMC_CHANNEL) channels.get(name);
        return null;
    }


    /* return 'mud' from 'player@mud' */
    final String imc_mudof(String fullname )
    {
       String buf;
       String where;

       int pos = fullname.indexOf("@");
       if(pos > -1)
           buf = fullname.substring(pos+1, fullname.length());
        else
            buf = "*";

       return buf;
    }

    /* return d from a!b!c!d */
    final String imc_lastinpath(String path)
    {
        int pos = path.lastIndexOf("!");
        if(pos < 0)
            return path;

        return path.substring(pos+1, path.length());
    }

    /* return 'player' from 'player@mud' */
    final String imc_playerof(String fullname )
    {
       String buf;
       String where;

       int pos = fullname.indexOf("@");
       if(pos > -1)
           buf = fullname.substring(0, pos);
        else
            buf = "*";

       return buf;
    }

    /* add "key=value" to "p" */
    final void imc_addkey(PACKET p, String key, String value) {

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

        for (int i = 0; i < PACKET.IMC_MAX_KEYS; i++) {
            if (!p.key[i].equals("") && (key.equalsIgnoreCase(p.key[i]))) {
                p.key[i] = "";
                p.value[i] = "";
                break;
            }
        }
        if (value=="")
            return;

        for (int i = 0; i < PACKET.IMC_MAX_KEYS; i++) {
            if (p.key[i] == "") {
                p.key[i] = key;
                if((value.indexOf(" ") > -1))
                    value = "\""+value+"\"";
                p.value[i] = value;
                return;
            }
        }
    }

    /* add "key=value" for an integer value */
    final void imc_addkeyi(PACKET p, String key, int value) {
        String temp;
        temp = String.valueOf(value);
        imc_addkey(p, key, temp);
    }

    /* clear all keys in "p" */
    final void imc_initdata(PACKET p) {
        int i;

        for (i = 0; i < PACKET.IMC_MAX_KEYS; i++) {
            p.key[i] = "";
            p.value[i] = "";
        }
    }

    /* convert back from 'd' to 'p' */
    final void setdata(PACKET p, imc_char_data d) {
        imc_initdata(p);

        if (d == null) {
            p.from = "*";
            imc_addkeyi(p, "level", -1);
            return;
        }

        p.from = d.name;

        if (d.wizi > 0)
            imc_addkeyi(p, "wizi", d.wizi);
//        imc_addkeyi(p, "level", d.level);
    }

    /* send a who-request to a remote mud */
    final void imc_send_who(imc_char_data from, String to, String type) {
        PACKET out = new PACKET();

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

    final void imc_send( PACKET p)
    {
        if (imc_active < IA_UP) {
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

//        p.i.path = this_imcmud.hubname;

        do_send_packet(p);
    }



    final void imc_loop() {
        return;
    }


//
    // list of all active call_out's in the game
    //
    static LinkedList call_outs = new LinkedList();

    public void imc_register_call_out(int hbeat, String function_name, Object param) {
        Vector call_out = new Vector();
        call_out.add(function_name);
        call_out.add(new Long(HeartBeat + hbeat));
        call_out.add(param);

        call_outs.add(call_out);
    }

    public void imc_process_call_outs() {
        if (call_outs.size() < 1) {
            return;
        }

        for (int i = 0; i < call_outs.size(); i++) {
            Vector call_out = (Vector) call_outs.get(i);
            String fun = (String) call_out.elementAt(0);
            long hbeat = ( (Long) call_out.elementAt(1)).longValue();
            Object param = call_out.elementAt(2);

            if (hbeat == HeartBeat) {
                Object o = this;
                if (o != null) {
                    Class cl = o.getClass();
                    java.lang.reflect.Method funcs[] = cl.getMethods();
                    if (funcs.length > 1) {
                        for (int k = 0; k < funcs.length; k++) {
                            String m_name = (String) funcs[k].getName();
                            if (m_name.equals(fun)) {
                                try {
                                    funcs[k].invoke(o, new Object[] {param});
                                }
                                catch (Exception e) {
                                    tracef(0,
                                           "imc: call_out failed with error: "
                                           + e.toString());

                                }
                            }
                        }
                    }
                }
                call_outs.remove(i);
                i--;
            }
        }
    }

	final public static StringBuffer replaceSB(StringBuffer inStr, String str1, String str2)
	{
	    boolean found = true;  
	      
	    int last = -1;
	      
	    while(found)
	    {
	        found = false;
	        int ndx = inStr.toString().indexOf(str1);
	        if(ndx > -1)
	        {
	            if(last != ndx) {
	                try {
	                  if(str1.length() == 1)
	                      inStr.deleteCharAt(ndx);
	                  else
	                      inStr.delete(ndx, ndx+str1.length());

	                  inStr.insert(ndx, str2);
	                  last = ndx;

	                  found = true;
	                } catch (Exception e) {
	                      System.out.println(e.toString());
	                      System.out.println("str1 = '"+str1+"', str2 = '"+str2+"'");
	                      System.out.println("ndx = '"+ndx+"'");
	                      System.out.println("inStr = '"+inStr.toString()+"'");
	                }
	            }
	        }
	    }

	    return inStr;
	}
	  
	final public static String toIMCColours(String str, boolean ansi)
	{
	    // Char macros
	    StringBuffer res = new StringBuffer(str);
	    // ANSI color macros
	    res = replaceSB(res, "&YELLOW&BOLD","^y");
	    res = replaceSB(res, "&RED&BOLD", "^r");
	    res = replaceSB(res, "&BLUE&BOLD", "^b");
	    res = replaceSB(res, "&GREEN&BOLD", "^g");
	    res = replaceSB(res, "&MAGENTA&BOLD", "^p");
	    res = replaceSB(res, "&WHITE&BOLD", "^w");
	    res = replaceSB(res, "&CYAN&BOLD", "^c");
	    res = replaceSB(res, "&OFF&BOLD", "^w");

	    res = replaceSB(res, "&YELLOW","^Y");
	    res = replaceSB(res, "&RED", "^R");
	    res = replaceSB(res, "&BLUE", "^B");
	    res = replaceSB(res, "&GREEN", "^G");
	    res = replaceSB(res, "&MAGENTA", "^P");
	    res = replaceSB(res, "&WHITE", "^W");
	    res = replaceSB(res, "&CYAN", "^C");

	    res = replaceSB(res, "&OFF", "^w");

	    return res.toString();
	}


   final String update_wholist()
    {
		StringBuffer str=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session ses=(Session)Sessions.elementAt(s);
			MOB smob=ses.mob();
			if((smob!=null)&&(smob.soulMate()!=null))
				smob=smob.soulMate();
			if((!ses.killFlag())&&(smob!=null)
			&&(!smob.amDead())
			&&(smob.location()!=null)
			&&(Sense.isSeen(smob)))
			{
				str.append(Util.padRight(smob.name(),20)+Util.padRight(smob.charStats().displayClassLevel(smob,true),10)+"\r\n");
				//whoV2.addElement(smob.name());
				//whoV2.addElement(new Integer((int)(ses.getIdleMillis()/1000)));
				//whoV2.addElement(smob.charStats().displayClassLevel(smob,true));
			}
		}
		if(str.length()==0) return "Nobody!";
		return str.toString();
    }

    final String who_help()
    {
        return "\\n\\r"+
            "Available imcminfo types:\\n\\r"+
            "help   - this screen\\n\\r"+
            "who    - who is online\\n\\r"+
            "istats - IMC statistics\\n\\r";
    }

    final void imc_recv_who(imc_char_data from, String sender, String type)
    {
        PACKET out = new PACKET();

        imc_initdata(out);
        out.from = from.name;
        imc_addkeyi(out, "level", 3);

        out.to = sender;
        out.type = "who-reply";

        if(type.equals("who"))
            imc_addkey(out, "text", update_wholist());
        else
        if(type.equals("istats"))
            imc_addkey(out, "text", do_imcstats());
        else
        if(type.equals("help"))
            imc_addkey(out, "text", who_help());

        imc_send(out);
    }

    final boolean query_online(String name)
    {
		MOB M=CMMap.getPlayer(name);
		if(M==null) return false;
		return Sense.isInTheGame(M);
    }

    final void imc_recv_whois(imc_char_data from, String sender, int level)
    {
        PACKET out = new PACKET();

        imc_initdata(out);
        out.from = from.name;
        imc_addkeyi(out, "level", 3);

        out.to = sender;
        out.type = "whois-reply";

        imc_addkey(out, "text", "imcpfind "+from.name+"@"+imc_name+" is "+
                       (query_online(from.name) ? "online":"offline")+ ".");

        imc_send(out);
    }


    final void imc_recv_ping(imc_char_data from, String path, String sender)
    {
        PACKET out = new PACKET();

        imc_initdata(out);
        out.from = from.name;

        out.to = sender;
        out.type = "ping-reply";

        imc_addkey(out, "path", path);

        imc_send(out);
    }


    final void imc_recv_tell(imc_char_data d, String from, String text)
    {
        send_to_player(d.name, from+" imcptells you '&BSAY"+text+"'&ESAY");
    }

    final void imc_recv_chat(imc_char_data d, String from, String channel, String text)
    {
        StringTokenizer st = new StringTokenizer(channel, ":");
        if (st.countTokens() > 1) {
            channel = st.nextToken();
            channel = st.nextToken();
        }

        String chatText = from + " &BLUE[&YELLOW&BOLD<&GREEN" + channel +
                   "&YELLOW&BOLD>&BLUE]" +
                   "&OFF " + text + "&OFF";

        send_to_player("all", chatText);

        LinkedList l = (LinkedList) chanhist.get(channel);
        if(l == null)
            l = new LinkedList();

        l.add(chatText);
        chanhist.put(channel, l);
    }


    final void imc_recv_is_alive(imc_char_data d, String from, String path,
                           String version, String netname)
    {
        String mudname = imc_mudof(from);
        REMOTEINFO rinfo = new REMOTEINFO();
        rinfo.name = mudname;
        rinfo.network = netname;
        rinfo.version = version;

        int pos = path.indexOf("!");
        if(pos > -1)
        {
            String hubs = path.substring(pos+1, path.length());
            int pos2 = hubs.indexOf("!");
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
        text = Util.replaceAll(text, "\\n\\r", "\n\r");
        String lines[] = explodeNicely(text, "\\n\\r");

        send_to_player(d.name, "\n\r"+text+"&OFF");
    }

    final void imc_recv_ping_reply(imc_char_data d, String from, String path)
    {
       String route[] = explodeNicely(path, "!");

       StringBuffer text = new StringBuffer("Traceroute information for "+from+"\n\r");

       text.append("&CYANSend path:&OFF   ");
       for(int i = 0; i < route.length; i++)
       {
           if (i > 0)
               text.append("->");
           text.append(route[i]);
       }
       text.append("\n\r");
       text.append("&CYANReturn path:&OFF ");
       for(int i= route.length-1; i >= 0; i--)
       {
           if (i < route.length-1)
               text.append("->");
           text.append(route[i]);
       }

       send_to_player(d.name, text.toString());
   }


    final String read_channel_name(String _hubname)
    {
        String local_name = (String) chan_conf.get(_hubname);
        if(local_name == null) local_name = "(PRIVATE)";
        
        return local_name;
    }

    final void imc_recv_update(String from, String chan, String owner,
                         String operators, String policy, String invited,
                         String excluded )
    {
        IMC_CHANNEL c;
        String mud;

        mud = imc_mudof(from);

        /* forged? */
        if (chan.indexOf(":")<-1 || !mud.equalsIgnoreCase(imc_mudof(chan)))
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
          if( !p.key[i].equals("") && p.key[i].equalsIgnoreCase(key))
          {
              if(p.value[i].startsWith("\""))
                  return p.value[i].substring(1, p.value[i].length()-1);
              else
                  return p.value[i];
          }

       return def;
    }

    /* identical to imc_getkey, except get the integer value of the key */
    final int imc_getkeyi( PACKET p, String key, int def )
    {
       int i;

       for( i = 0; i < PACKET.IMC_MAX_KEYS; i++ )
          if( !p.key[i].equals("") && p.key[i].equalsIgnoreCase(key) )
          {
              return Util.s_int(p.value[i]);
          }

       return def;
    }


    final public Hashtable query_channels() {
        return channels;
    }

    final public Hashtable query_muds() {
        return muds;
    }

    final public void exec_commands(PACKET p) {
        if (p == null)
            return;
        String cmd = p.type;
        imc_char_data d = new imc_char_data();

        d.name = p.i.to;
        d.wizi = imc_getkeyi(p, "wizi", 0);
        d.level = imc_getkeyi(p, "level", 0);
        d.invis = 0;

        String to_mud = imc_mudof(d.name);
        if (!to_mud.equalsIgnoreCase(imc_name) && !to_mud.equals("*")) {
            tracef(8, "Message was not sent to this mud.");
            return;
        }

        d.name = imc_playerof(d.name);
        tracef(8, "Message sent to " + d.name);

        if (p.type.equals("who")) {
            tracef(8, "Who request received from " + p.i.from);
            imc_recv_who(d, p.i.from, imc_getkey(p, "type", "who"));
        }

        if (p.type.equals("whois")) {
            tracef(8, "Whois request received from " + p.i.from);
            imc_recv_whois(d, p.i.from, imc_getkeyi(p, "level", 0));
        }

        if (p.type.equals("ping")) {
            tracef(8, "Ping reply received from " + p.i.from);
            imc_recv_ping(d, p.i.path, p.i.from);
        }

        if (p.type.equals("tell")) {
            tracef(8, "Tell received from " + p.i.from);
            imc_recv_tell(d, p.i.from, imc_getkey(p, "text", ""));
        }

        if (p.type.equals("keepalive-request")) {
            tracef(8, "Keepalive request received from " + p.i.from);
            imc_send_keepalive();
        }

        if (p.type.equals("who-reply")) {
            tracef(8, "Who reply received from " + p.i.from);
            imc_recv_who_reply(d, imc_getkey(p, "text", ""));
        }

        if (p.type.equals("ping-reply")) {
            tracef(8, "Ping-reply reply received from " + p.i.from);
            imc_recv_ping_reply(d, p.i.from, imc_getkey(p, "path", ""));
        }

        if (p.type.equals("is-alive")) {
            tracef(8, "is-alive received from " + p.i.from);
            imc_recv_is_alive(d, p.i.from, p.i.path,
                              imc_getkey(p, "versionid", "Unknown"),
                              imc_getkey(p, "networkname", "None"));
        }

        if (p.type.equals("ice-msg-b")) {
            tracef(8, "Chat received from " + p.i.from);
            imc_recv_chat(d, p.i.from, imc_getkey(p, "channel", "ICHAT"),
                          imc_getkey(p, "text", ""));
        }

        if (p.type.equals("ice-update")) {
            tracef(8, "Ice update reply from " + p.i.from);
            imc_recv_update(p.from, imc_getkey(p, "channel", ""),
                            imc_getkey(p, "owner", ""),
                            imc_getkey(p, "operators", ""),
                            imc_getkey(p, "policy", ""),
                            imc_getkey(p, "invited", ""),
                            imc_getkey(p, "excluded", ""));

        }
    }

    final void check_password(String s) {
        StringTokenizer st = new StringTokenizer(s, " ");
        if (st.countTokens() < 3) {
            tracef(0, "Password not found in Hub reply.");
            return;
        }

        String c = st.nextToken();
        String hubName = st.nextToken();
        String pwd = st.nextToken();

        if (pwd.equals(this_imcmud.serverpw))
            tracef(8, "Password OK.");
        else
            tracef(0, "Password incorrect.");
    }

    final public void imc_read_from_socket(DataInputStream in) {
        try {
            if (in.available() > 0) {
                String s = in.readLine();
                if (s == null)
                    return;

                if (s.length() > 0) {
                    tracef(8, "imc: received '" + s + "'");
                    if (s.startsWith("PW "))
                        check_password(s);
                    else {
                        PACKET p = interpret2(s);
                        exec_commands(p);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.errOut("IMC2Driver", "read socket error: " + e.toString());
        }
    }

    final public void imc_write_to_socket(DataOutputStream out) {
        try {
            if (this_imcmud.outbuf.equals(""))
                return;

            tracef(8, "imc: sending '" + this_imcmud.outbuf + "'");
            out.write(this_imcmud.outbuf.getBytes());
            this_imcmud.outbuf = "";
        }
        catch (Exception e) {
            tracef(0, "write socket error: " + e.toString());
            imc_active = IA_NONE;
            tracef(0, "Waiting 20 seconds and try to reconnect.");
            try {
                sleep(20000);
            }
            catch (Exception ex) {}
            this.imc_startup(true,
							 imc_name,
							 imc_siteinfo.host,
							 imc_siteinfo.email,
							 imc_siteinfo.www,
							 this_imcmud.hubname,
							 this_imcmud.port,
							 this_imcmud.clientpw,
							 this_imcmud.serverpw,
							 buildChannelMap());
        }
    }

    /* send a keepalive to everyone */
    final public void imc_send_keepalive() {
        PACKET out = new PACKET();

        if (imc_active < IA_UP)
            return;

        imc_initdata(out);
        out.type = "is-alive";
        out.from = "*";
        out.to = "*@*";
        imc_addkey(out, "versionid", IMC_VERSIONID);
        imc_addkey(out, "networkname", this_imcmud.network);
        if (imc_siteinfo.flags != null && imc_siteinfo.flags != "")
            imc_addkey(out, "flags", imc_siteinfo.flags);

        imc_send(out);
    }

    final public LinkedList imc_query_mudlist() {
        LinkedList imclist = new LinkedList();
        imclist.add("Active muds on IMC:");
        imclist.add(
            "&CYANName              &BOLDIMC Version                              " +
            "&OFF&GREENNetwork     &BOLDHub&OFF\n\r\n\r");

        Enumeration e = muds.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            REMOTEINFO r = (REMOTEINFO) muds.get(key);
            if (r != null) {
                imclist.add("&CYAN" + Util.padLeft(key, 17) + " " +
                            "&BOLD" + Util.padLeft(r.version, 40) +
                            " " +
                            "&OFF&GREEN" + Util.padLeft(r.network, 11) +
                            " " +
                            "&BOLD&GREEN" + r.hub + "&OFF");
            }
        }

        return imclist;
    }

    final public LinkedList imc_query_chanlist() {
        LinkedList imclist = new LinkedList();
        imclist.add("&CYANName                   &BOLDLocal name      " +
                    "&OFF&GREENOwner              &BOLDLevel    " +
                    "Policy&OFF\n\r\n\r");

        Enumeration e = channels.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            IMC_CHANNEL r = (IMC_CHANNEL) channels.get(key);
            if (r != null) {
                String policy = "final public";
                if (r.policy == CHAN_PRIVATE)
                    policy = "(private)";
                else
                if (r.policy == CHAN_COPEN)
                    policy = "open";
                else
                if (r.policy == this.CHAN_CPRIVATE)
                    policy = "(cprivate)";

                imclist.add("&CYAN" + Util.padLeft(key, 22) + " " +
                            "&BOLD" + Util.padLeft(r.local_name, 15) +
                            " " +
                            "&OFF&GREEN" + Util.padLeft(r.owner, 18) +
                            " " +
                            "&OFF&GREEN" + Util.padLeft(""+r.level, 8) +
                            " " +
                            "&BOLD&GREEN" + policy + "&OFF");
            }
        }

        return imclist;
    }

    /* send a keepalive to everyone */
    final public void imc_request_keepalive() {
        PACKET out = new PACKET();

        if (imc_active < IA_UP)
            return;

        imc_initdata(out);
        out.type = "keepalive-request";
        out.from = "*";
        out.to = "*@*";
        imc_addkey(out, "versionid", IMC_VERSIONID);
        if (imc_siteinfo.flags != null && imc_siteinfo.flags != "")
            imc_addkey(out, "flags", imc_siteinfo.flags);

        imc_send(out);
    }

    final class call_out
        extends Thread {

        IMC2Driver imc_client;
        int seq = 0;

        public call_out(IMC2Driver _imc_client) {
            imc_client = _imc_client;
        }

        final public void run() {
            if (imc_client == null)
                return;
            while (true) {
//              tracef(1, "call_out: process call outs");
                imc_client.imc_process_call_outs();
                imc_client.imc_write_to_socket(out);
                try {
                    sleep(100);
                    seq++;
                    if (seq % 10 == 0) {
                        imc_client.imc_sequencenumber++;
                        seq = 0;
                    }
                }
                catch (Exception e) {}

            }
        }
    }

    final class call_in
        extends Thread {

        IMC2Driver imc_client;

        public call_in(IMC2Driver _imc_client) {
            imc_client = _imc_client;
        }

        final public void run() {
            if (imc_client == null)
                return;
            while (true) {
//              tracef(1, "call_out: process call outs");
                imc_client.imc_read_from_socket(in);
                try {
                    sleep(100);
                }
                catch (Exception e) {}

            }
        }

    }

    /* send a ping with a given timestamp */
    final public void imc_send_ping(String name, String to, int time_s, int time_u) {
        PACKET out = new PACKET();

        if (imc_active < IA_UP)
            return;

        imc_initdata(out);
        out.type = "ping";
        out.from = name;
        out.to = "*@" + to;
        imc_addkeyi(out, "time-s", time_s);
        imc_addkeyi(out, "time-us", time_u);

        imc_send(out);
        out = null;
    }

    final public void run_imcpinfo(String name, String mudname, String who, int level,
                             int invis) {
        imc_char_data test = new imc_char_data();
        test.name = name;
        test.level = level;
        test.invis = invis;
        imc_send_who(test, "@" + mudname, "finger " + who);
    }

    final public void run_imcminfo(String name, String mudname, String type,
                             int level, int invis) {
        imc_char_data test = new imc_char_data();
        test.name = name;
        test.level = level;
        test.invis = invis;
        imc_send_who(test, "@" + mudname, type);
    }

    final public void imc_send_who(String name, String mudname, int level, int invis) {
        imc_char_data test = new imc_char_data();
        test.name = name;
        test.level = level;
        test.invis = invis;
        imc_send_who(test, "@" + mudname, "who");
    }

    final public String imc_send_tell(String from, String to, String text, int level,
                                int invis) {
        imc_char_data chr = new imc_char_data();
        chr.name = from;
        chr.level = level;
        chr.invis = invis;
        return imc_send_tell(chr, to, text, 1);
    }

    final public String imc_send_reply(String from, String text, int level, int invis) {
        imc_char_data chr = new imc_char_data();
        chr.name = from;
        chr.level = level;
        chr.invis = invis;
        return imc_send_reply(chr, text);
    }

    final public String imc_send_chat(String from, String to, String text, int level,
                                int emote) {
        imc_char_data chr = new imc_char_data();
        chr.name = from;
        chr.level = level;
        return imc_send_chat(chr, to, text, level, emote);
    }

    /* send a tell to a remote player */
    final String imc_send_tell(imc_char_data from, String to, String argument,
                         int isreply) {
        PACKET out = new PACKET();

        if (imc_active < IA_UP)
            return "IMC is not active.";

        if (imc_mudof(to).equals("*"))
            return "You cannot send tell to everyone!";
                /* don't let them do this */

        setdata(out, from);

        out.to = to;
        out.type = "tell";
        imc_addkeyi(out, "level", 3);
        imc_addkey(out, "text", argument);

        imc_send(out);

        replies.put(from.name, to);

        String chatText = "You imcptell " + to + " '&BOLD&CYAN" + argument +
            "&OFF'.";
        return chatText;
    }

    /* send a reply to a remote player */
    final String imc_send_reply(imc_char_data from, String argument) {
        PACKET out = new PACKET();

        if (imc_active < IA_UP)
            return "IMC is not active.";

        setdata(out, from);
        String to = (String) replies.get(from.name);
        if (to == null)
            return "Noone to reply to.";

        out.to = to;
        out.type = "tell";
        imc_addkeyi(out, "level", 3);
        imc_addkey(out, "text", argument);

        imc_send(out);

        String chatText = "You imcptell " + to + " '&BOLD&CYAN" + argument +
            "&OFF'.";
        return chatText;
    }

    /* send a tell to a remote player */
    final String imc_send_chat(imc_char_data from, String to, String argument,
                         int isreply, int emote) {
        PACKET out = new PACKET();

        if (imc_active < IA_UP)
            return "IMC is not running.";

        setdata(out, from);

//      imc_addkey(p, "level", 3);

        out.to = "*@*";
        out.type = "ice-msg-b";
        imc_addkey(out, "channel", to);
        imc_addkey(out, "text", argument);
        imc_addkeyi(out, "emote", emote);

        /*      if (isreply > 0)
                  imc_addkeyi(out, "isreply", isreply);*/

        imc_send(out);

        String chan = to;
        int pos = to.indexOf(":");
        if (pos > -1)
            chan = to.substring(pos + 1, to.length());

        String text = argument;
        if (argument.startsWith(", ") || emote == 1)
            text = from.name + "@" + this.imc_name;

        String chatText = "&CYAN&BOLD" + from.name + "@" + this.imc_name +
            " &OFF&BLUE[&OFF&YELLOW&BOLD<&GREEN" + chan +
            "&YELLOW&BOLD>&BLUE]" +
            "&OFF " + text + "&OFF";

        LinkedList l = (LinkedList) chanhist.get(chan);
        if (l == null)
            l = new LinkedList();

        l.add(chatText);
        chanhist.put(chan, l);

        return chatText;

    }

    final public String run_imcpsettings(String from, String cmd, int level) {
        StringBuffer s = new StringBuffer();

        String params[] = explodeNicely(cmd);

        if (params.length < 2) {
            s.append("&GREENSyntax: &CYANimcpsettings show   - &CYAN&BOLDlists IMC network functions and IMC channels\n\r" +
                     "&OFF&CYAN        imcpsettings subscribe +channel - &OFF&CYAN&BOLDcreates a subscription to a channel\n\r" +
                     "&OFF&CYAN        imcpsettings subscribe -channel - &OFF&CYAN&BOLDdeletes a subscription to a channel\n\r" +
                     "&OFF&CYAN        imcpsettings blacklist +mud - &OFF&CYAN&BOLDadds a mud to the blacklist\n\r" +
                     "&OFF&CYAN        imcpsettings blacklist -mud - &OFF&CYAN&BOLDremoves a mud from the blacklist\n\r" +
                     "&OFF&CYAN        imcpsettings blacklist +person@mud - &OFF&CYAN&BOLDadds a player to the blacklist\n\r" +
                     "&OFF&CYAN        imcpsettings blacklist -person@mud - &OFF&CYAN&BOLDremoves a player from the blacklist\n\r" +
                     "&OFF&CYAN        imcpsettings function +nfunction  - &OFF&CYAN&BOLDswitches on a network function\n\r" +
                     "&OFF&CYAN        imcpsettings function -nfunction  - &OFF&CYAN&BOLDswitches off a network function\n\r");
        }

        return s.toString();
    }

    final public void run() {
        imc_read_from_socket(in);
        HeartBeat = 0;

        call_in c_thread2 = new call_in(this);
        c_thread2.start();

        call_out c_thread = new call_out(this);
        c_thread.start();

        while (true) {
            HeartBeat++;
            try {
                sleep(2000);
            }
            catch (Exception e) {}
        }
    }

}