package com.planet_ink.coffee_mud.application;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.database.*;
import com.planet_ink.coffee_mud.core.http.*;
import com.planet_ink.coffee_mud.core.threads.*;
import com.planet_ink.coffee_mud.core.smtp.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.intermud.imc2.*;
import com.planet_ink.coffee_mud.core.intermud.server.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.PrintWriter; // for writing to sockets
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.sql.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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

public class MUD extends Thread implements MudHost
{
	public static final float HOST_VERSION_MAJOR=(float)5.2;
	public static final long  HOST_VERSION_MINOR=3;
	
	public static boolean keepDown=true;
	public static String execExternalCommand=null;
	public static SaveThread saveThread=null;
	public static UtiliThread utiliThread=null;
	public static Server imserver=null;
	public static IMC2Driver imc2server=null;
	public static Vector webServers=new Vector();
	public static SMTPserver smtpServerThread=null;
	public static Vector mudThreads=new Vector();
	public static DVector accessed=new DVector(2);
	public static Vector autoblocked=new Vector();
	public static Vector databases=new Vector();

	public static boolean serverIsRunning = false;
	public static boolean isOK = false;

	public boolean acceptConnections=false;
	public String host="MyHost";
	public int port=5555;
    public final static String[] STATE_STRING={"waiting","accepting","allowing"};
    public int state=0;
	ServerSocket servsock=null;

	public MUD()
	{
		super("MUD-MainServer");
	}

	public static void fatalStartupError(Thread t, int type)
	{
		String str=null;
		switch(type)
		{
		case 1:
			str="ERROR: initHost() will not run without properties. Exiting.";
			break;
		case 2:
			str="Map is empty?! Exiting.";
			break;
		case 3:
			str="Database init failed. Exiting.";
			break;
		case 4:
			str="Fatal exception. Exiting.";
			break;
		case 5:
			str="MUD Server did not start. Exiting.";
			break;
		default:
			str="Fatal error loading classes.  Make sure you start up coffeemud from the directory containing the class files.";
			break;
		}
		Log.errOut("MUD",str);
		CMLib.killThread(t,100,1);
	}


	private static boolean initHost(Thread t, CMProps page)
	{

		if (!isOK)
		{
			CMLib.killThread(t,100,1);
			return false;
		}

		if ((page == null) || (!page.loaded))
		{
			fatalStartupError(t,1);
			return false;
		}

		while (!serverIsRunning && isOK)
		{
		}
		if (!isOK)
		{
			fatalStartupError(t,5);
			return false;
		}

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: connecting to database");
		
		CMProps.loadCommonINISettings(page);
		
		Vector compress=CMParms.parseCommas(page.getStr("COMPRESS").toUpperCase(),true);
		CMProps.setBoolVar(CMProps.SYSTEMB_ITEMDCOMPRESS,compress.contains("ITEMDESC"));
		CMProps.setBoolVar(CMProps.SYSTEMB_MOBCOMPRESS,compress.contains("GENMOBS"));
		CMProps.setBoolVar(CMProps.SYSTEMB_ROOMDCOMPRESS,compress.contains("ROOMDESC"));
		CMProps.setBoolVar(CMProps.SYSTEMB_MOBDCOMPRESS,compress.contains("MOBDESC"));
		Resources.setCompression(compress.contains("RESOURCES"));
		Vector nocache=CMParms.parseCommas(page.getStr("NOCACHE").toUpperCase(),true);
		CMProps.setBoolVar(CMProps.SYSTEMB_MOBNOCACHE,nocache.contains("GENMOBS"));
		CMProps.setBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE,nocache.contains("ROOMDESC"));
		
		DBConnector currentDBconnector=new DBConnector(page.getStr("DBCLASS"),page.getStr("DBSERVICE"),page.getStr("DBUSER"),page.getStr("DBPASS"),page.getInt("DBCONNECTIONS"),page.getBoolean("DBREUSE"),true);
		currentDBconnector.reconnect();
        CMLib.registerLibrary(new DBInterface(currentDBconnector));
		
		DBConnection DBTEST=currentDBconnector.DBFetch();
		if(DBTEST!=null) currentDBconnector.DBDone(DBTEST);
		if((currentDBconnector.amIOk())&&(CMLib.database().isConnected()))
		{
			Log.sysOut("MUD","Database connection successful.");
			databases.addElement(currentDBconnector);
		}
		else
		{
			String DBerrors=currentDBconnector.errorStatus().toString();
			Log.errOut("MUD","Fatal database error: "+DBerrors);
			System.exit(-1);
		}
        
		if(page.getStr("RUNWEBSERVERS").equalsIgnoreCase("true"))
		{
			HTTPserver webServerThread = new HTTPserver((MudHost)mudThreads.firstElement(),"pub",0);
			webServerThread.start();
			webServers.addElement(webServerThread);
			int numToDo=webServerThread.totalPorts();
			while((--numToDo)>0)
			{
				webServerThread = new HTTPserver((MudHost)mudThreads.firstElement(),"pub",numToDo);
				webServerThread.start();
				webServers.addElement(webServerThread);
			}
			webServerThread = new HTTPserver((MudHost)mudThreads.firstElement(),"admin",0);
			webServerThread.start();
			webServers.addElement(webServerThread);
			numToDo=webServerThread.totalPorts();
			while((--numToDo)>0)
			{
				webServerThread = new HTTPserver((MudHost)mudThreads.firstElement(),"admin",numToDo);
				webServerThread.start();
				webServers.addElement(webServerThread);
			}
			CMLib.registerLibrary(new ProcessHTTPrequest(null,(webServers.size()>0)?(HTTPserver)webServers.firstElement():null,null,true));
		}
		else
            CMLib.registerLibrary(new ProcessHTTPrequest(null,null,null,true));

		
		if(page.getStr("RUNSMTPSERVER").equalsIgnoreCase("true"))
		{
			smtpServerThread = new SMTPserver((MudHost)mudThreads.firstElement());
			smtpServerThread.start();
			CMLib.threads().startTickDown(smtpServerThread,Tickable.TICKID_EMAIL,60);
		}

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading base classes");
		if(!CMClass.loadClasses(page))
		{
			fatalStartupError(t,0);
			return false;
		}
        
        CMClass.globalClock().initializeINIClock(page);
        CMLib.factions().reloadFactions(CMProps.getVar(CMProps.SYSTEM_PREFACTIONS));
		CMSecurity.setSysOp(page.getStr("SYSOPMASK")); // requires all classes be loaded
		CMSecurity.parseGroups(page);

		int numChannelsLoaded=CMLib.channels().loadChannels(page.getStr("CHANNELS"),
													  page.getStr("ICHANNELS"),
													  page.getStr("IMC2CHANNELS"));
        int numJournalsLoaded=CMLib.journals().loadCommandJournals(page.getStr("COMMANDJOURNALS"));
		Log.sysOut("MUD","Channels loaded   : "+(numChannelsLoaded+numJournalsLoaded));

        CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading socials");
		CMLib.socials().clearAllSocials();
		if(CMLib.socials().num()==0)
			Log.errOut("MUD","WARNING: Unable to load socials from socials.txt!");
		else
			Log.sysOut("MUD","Socials loaded    : "+CMLib.socials().num());

		CMLib.database().DBReadAllClans();
		Log.sysOut("MUD","Clans loaded      : "+CMLib.clans().size());
		
		CMLib.threads().startTickDown(CMLib.factions(),Tickable.TICKID_MOB,10);

		Log.sysOut("MUD","Loading map...");
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading rooms....");
		CMLib.database().DBReadAllRooms(null);
		for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: filling map ("+A.Name()+")");
			A.fillInAreaRooms();
		}
		Log.sysOut("MUD","Mapped rooms      : "+CMLib.map().numRooms()+" in "+CMLib.map().numAreas()+" areas");
		CMLib.map().initStartRooms(page);
		CMLib.map().initDeathRooms(page);
		CMLib.map().initBodyRooms(page);
		

		if(!CMLib.map().roomIDs().hasMoreElements())
		{
			Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
			String id="START";
			Area newArea=CMLib.database().DBCreateArea("New Area","StdArea");
			Room room=CMClass.getLocale("StdRoom");
			room.setRoomID(id);
			room.setArea(newArea);
			room.setDisplayText("New Room");
			room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.  If your character is not an Archon, pick up the book you see here and read it immediately!");
			CMLib.database().DBCreateRoom(room,"StdRoom");
			Item I=CMClass.getMiscMagic("ManualArchon");
			room.addItem(I);
			CMLib.database().DBUpdateItems(room);
		}

		CMLib.database().DBReadQuests((MudHost)mudThreads.firstElement());
		if(CMLib.quests().numQuests()>0)
			Log.sysOut("MUD","Quests loaded     : "+CMLib.quests().numQuests());

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: readying for connections.");
		try
		{
			saveThread=new SaveThread();
			saveThread.start();

			utiliThread=new UtiliThread();
			utiliThread.start();

			Log.sysOut("MUD","Utility threads started");
		}
		catch (Throwable th)
		{
			Log.sysOut("MUD","CoffeeMud Server initHost() failed");
			fatalStartupError(t,4);
			return false;
		}

		try
		{
			if(page.getBoolean("RUNI3SERVER"))
			{
				String playstate=page.getStr("I3STATE");
				if((playstate==null)||(playstate.length()==0))
					playstate="Open to the public";
				IMudInterface imud=new IMudInterface(CMProps.getVar(CMProps.SYSTEM_MUDNAME),
													 "CoffeeMud v"+CMProps.getVar(CMProps.SYSTEM_MUDVER),
													 ((MUD)mudThreads.firstElement()).getPort(),
													 playstate,
													 CMLib.channels().iChannelsArray());
				imserver=new Server();
				int i3port=page.getInt("I3PORT");
				if(i3port==0) i3port=27766;
				Server.start(CMProps.getVar(CMProps.SYSTEM_MUDNAME),i3port,imud);
			}
		}
		catch(Exception e)
		{
			if(imserver!=null) Server.shutdown();
			imserver=null;
		}

		
		try
		{
			if(page.getBoolean("RUNIMC2CLIENT"))
			{
				imc2server=new IMC2Driver();
				if(!imc2server.imc_startup(false,
										page.getStr("IMC2LOGIN").trim(),
										CMProps.getVar(CMProps.SYSTEM_MUDNAME),
										page.getStr("IMC2MYEMAIL").trim(),
										page.getStr("IMC2MYWEB").trim(),
										page.getStr("IMC2HUBNAME").trim(),
										page.getInt("IMC2HUBPORT"),
										page.getStr("IMC2PASS1").trim(),
										page.getStr("IMC2PASS2").trim(),
										imc2server.buildChannelMap(page.getStr("IMC2CHANNELS").trim())))
				{
					Log.errOut("MUD","IMC2 Failed to start!");
					imc2server=null;
				}
				else
				{
					CMLib.intermud().registerIMC2(imc2server);
					imc2server.start();
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut("IMC2",e.getMessage());
		}

		for(int i=0;i<mudThreads.size();i++)
			((MUD)mudThreads.elementAt(i)).acceptConnections=true;
		Log.sysOut("MUD","Initialization complete.");
		CMProps.setBoolVar(CMProps.SYSTEMB_MUDSTARTED,true);
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"OK");
		return true;
	}


	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		serverIsRunning = false;

		if (!isOK)	return;

		InetAddress bindAddr = null;

		if (CMProps.getIntVar(CMProps.SYSTEMI_MUDBACKLOG) > 0)
			q_len = CMProps.getIntVar(CMProps.SYSTEMI_MUDBACKLOG);

		if (CMProps.getVar(CMProps.SYSTEM_MUDBINDADDRESS).length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(CMProps.getVar(CMProps.SYSTEM_MUDBINDADDRESS));
			}
			catch (UnknownHostException e)
			{
				Log.errOut("MUD","ERROR: MUD Server could not bind to address " + CMProps.getVar(CMProps.SYSTEM_MUDBINDADDRESS));
				bindAddr = null;
			}
		}

		try
		{
			servsock=new ServerSocket(port, q_len, bindAddr);

			Log.sysOut("MUD","MUD Server started on port: "+port);
			if (bindAddr != null)
				Log.sysOut("MUD","MUD Server bound to: "+bindAddr.toString());
			serverIsRunning = true;

			while(true)
			{
                state=0;
                if(servsock==null) break;
				sock=servsock.accept();
				sock.setSoLinger(true,3);
                state=1;

				if (acceptConnections)
				{
					String address="unknown";
					try{address=sock.getInetAddress().getHostAddress().trim();}catch(Exception e){}
					Log.sysOut("MUD","Got a connection from "+address+" on port "+port);
                    int proceed=0;
                    if(CMSecurity.isBanned(address))
                        proceed=1;
					int numAtThisAddress=0;
					long ConnectionWindow=(180*1000);
					long LastConnectionDelay=(5*60*1000);
					boolean anyAtThisAddress=false;
					int maxAtThisAddress=6;
                    if(!CMSecurity.isDisabled("CONNSPAMBLOCK"))
                    {
    					try{
    						for(int a=accessed.size()-1;a>=0;a--)
    						{
    							if((((Long)accessed.elementAt(a,2)).longValue()+LastConnectionDelay)<System.currentTimeMillis())
    								accessed.removeElementAt(a);
    							else
    							if(((String)accessed.elementAt(a,1)).trim().equalsIgnoreCase(address))
    							{
    								anyAtThisAddress=true;
    								if((((Long)accessed.elementAt(a,2)).longValue()+ConnectionWindow)>System.currentTimeMillis())
    									numAtThisAddress++;
    							}
    						}
    						if(autoblocked.contains(address.toUpperCase()))
    						{
    							if(!anyAtThisAddress)
    								autoblocked.remove(address.toUpperCase());
    							else
    								proceed=2;
    						}
    						else
    						if(numAtThisAddress>=maxAtThisAddress)
    						{
    							autoblocked.addElement(address.toUpperCase());
    							proceed=2;
    						}
    					}catch(java.lang.ArrayIndexOutOfBoundsException e){}
    
    					accessed.addElement(address,new Long(System.currentTimeMillis()));
                    }
                    
					if(proceed!=0)
					{
						Log.sysOut("MUD","Blocking a connection from "+address+" on port "+port);
						PrintWriter out = new PrintWriter(sock.getOutputStream());
						out.println("\n\rOFFLINE: Blocked\n\r");
						out.flush();
						if(proceed==2)
							out.println("\n\rYour address has been blocked temporarily due to excessive invalid connections.  Please try back in "+(Math.round(LastConnectionDelay/60000))+" minutes, and not before.\n\r\n\r");
						else
							out.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
						out.flush();
                        try{Thread.sleep(250);}catch(Exception e){}
						out.close();
						sock = null;
					}
					else
					{
                        state=2;
						StringBuffer introText=Resources.getFileResource("text/intro.txt",true);
                        Session S=(Session)CMClass.getCommon("DefaultSession");
                        S.initializeSession(sock, introText != null ? introText.toString() : null);
						S.start();
						CMLib.sessions().addElement(S);
						sock = null;
					}
				}
				else
                if((CMLib.database()!=null)&&(CMLib.database().isConnected()))
				{
					StringBuffer rejectText=Resources.getFileResource("text/offline.txt",true);
					PrintWriter out = new PrintWriter(sock.getOutputStream());
					out.println("\n\rOFFLINE: " + CMProps.getVar(CMProps.SYSTEM_MUDSTATUS)+"\n\r");
					out.println(rejectText);
					out.flush();
                    try{Thread.sleep(1000);}catch(Exception e){}
					out.close();
					sock = null;
				}
                else
                {
                    sock.close();
                    sock = null;
                }
			}
		}
		catch(Throwable t)
		{
		    if((!(t instanceof java.net.SocketException))
		    ||(t.getMessage()==null)
		    ||(t.getMessage().toLowerCase().indexOf("socket closed")<0))
		    {
				Log.errOut("MUD",t);
		    }

			if (!serverIsRunning)
				isOK = false;
		}

		Log.sysOut("MUD","CoffeeMud Server cleaning up.");

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(IOException e)
		{
		}

		Log.sysOut("MUD","CoffeeMud Server on port "+port+" stopped!");
	}
    public String getStatus()
    {
        if(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN))
            return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
            return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);
        return STATE_STRING[state];
    }

	public void shutdown(Session S, boolean keepItDown, String externalCommand)
	{
		globalShutdown(S,keepItDown,externalCommand);
		interrupt(); // kill the damn archon thread.
	}

	public static void defaultShutdown()
	{
		globalShutdown(null,true,null);
	}
	public static void globalShutdown(Session S, boolean keepItDown, String externalCommand)
	{
		CMProps.setBoolVar(CMProps.SYSTEMB_MUDSTARTED,false);
		CMProps.setBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN,true);
        CMLib.threads().suspendAll();
		if(S!=null)S.print("Closing MUD listeners to new connections...");
		for(int i=0;i<mudThreads.size();i++)
			((MUD)mudThreads.elementAt(i)).acceptConnections=false;
		Log.sysOut("MUD","New Connections are now closed");
		if(S!=null)S.println("Done.");
		
		if((!CMSecurity.isSaveFlag("NOPLAYERS"))
		&&(saveThread!=null))
		{
			if(S!=null)S.print("Saving players...");
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Saving players...");
            if(CMLib.sessions()!=null)
                for(int s=0;s<CMLib.sessions().size();s++)
                {
                    Session S2=CMLib.sessions().elementAt(s);
                    if((S2!=null)&&(S2.mob()!=null)&&(S2.mob().playerStats()!=null))
                        S2.mob().playerStats().setLastDateTime(System.currentTimeMillis());
                }
			saveThread.savePlayers();
            CMLib.coffeeTables().update();
			if(S!=null)S.println("done");
			Log.sysOut("MUD","All users saved.");
		}
        if(S!=null)S.print("Saving stats...");
        CMLib.coffeeTables().update();
        if(S!=null)S.println("done");
        Log.sysOut("MUD","Stats saved.");
		
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut("MUD","Notifying all objects of shutdown...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_SHUTDOWN,null);
		Vector roomSet=new Vector();
		try
		{
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
			{
			    Area A=(Area)e.nextElement();
			    A.toggleMobility(false);
			}
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				R.send(mob,msg);
				roomSet.addElement(R);
			}
	    }catch(NoSuchElementException e){}
		if(S!=null)S.println("done");
		if((saveThread==null)||(utiliThread==null))
		{
			CMProps.setBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN,false);
            CMLib.threads().resumeAll();
		    return;
		}

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Quests");
		CMLib.quests().shutdown();

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Save Thread");
		saveThread.shutdown();
		CMLib.killThread(saveThread,100,1);
		saveThread=null;
		
		if(S!=null)S.println("Save thread stopped");
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Utility Thread");
		utiliThread.shutdown();
		CMLib.killThread(utiliThread,100,1);
		utiliThread=null;
		if(S!=null)S.println("Utility thread stopped");
		Log.sysOut("MUD","Utility/Save Threads stopped.");
		
		if(CMSecurity.isSaveFlag("ROOMMOBS")
		||CMSecurity.isSaveFlag("ROOMITEMS")
		||CMSecurity.isSaveFlag("ROOMSHOPS"))
		{
			if(S!=null)S.print("Saving room data...");
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Rejuving the dead");
			CMLib.threads().tickAllTickers(null);
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Map Update");
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				((Area)e.nextElement()).toggleMobility(false);
			int roomCounter=0;
			Room R=null;
			for(Enumeration e=roomSet.elements();e.hasMoreElements();)
			{
			    if(((++roomCounter)%200)==0)
			    {
			        if(S!=null) S.print(".");
					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Map Update ("+roomCounter+")");
			    }
			    R=(Room)e.nextElement();
			    if(R.roomID().length()>0)
			    	R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
			}
			if(S!=null)S.println("done");
			Log.sysOut("MUD","Map data saved.");
		    
		}

		if(imserver!=null)
		{
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...I3Server");
			Server.shutdown();
			imserver=null;
			if(S!=null)S.println("I3Server stopped");
			Log.sysOut("MUD","I3Server stopped");
		}

		if(imc2server!=null)
		{
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...IMC2Server");
			imc2server.shutdown();
			imc2server=null;
			if(S!=null)S.println("IMC2Server stopped");
			Log.sysOut("MUD","IMC2Server stopped");
		}
		
		if(S!=null)S.print("Stopping player Sessions...");
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Stopping sessions");
		while(CMLib.sessions().size()>0)
		{
			Session S2=CMLib.sessions().elementAt(0);
			if((S!=null)&&(S2==S))
				CMLib.sessions().removeElementAt(0);
			else
			{
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Stopping session "+S2.getAddress());
				S2.logoff();
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Done stopping session "+S2.getAddress());
			}
			if(S!=null)S.print(".");
		}
		if(S!=null)S.println("All users logged off");
        try{Thread.sleep(3000);}catch(Exception e){/* give sessions a few seconds to inform the map */}
		Log.sysOut("MUD","All users logged off.");

		if(smtpServerThread!=null)
		{
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...smtp server");
			smtpServerThread.shutdown(S);
			smtpServerThread = null;
			Log.sysOut("MUD","SMTP Server stopped.");
			if(S!=null)S.println("SMTP Server stopped");
		}
		
		if(S!=null)S.print("Stopping all threads...");
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...shutting down service engine");
		CMLib.threads().shutdownAll();
		if(S!=null)S.println("done");
		Log.sysOut("MUD","Map Threads Stopped.");

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...closing db connections");
		for(int d=0;d<databases.size();d++)
			((DBConnector)databases.elementAt(d)).killConnections();
		if(S!=null)S.println("Database connections closed");
		Log.sysOut("MUD","Database connections closed.");

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Clearing socials, clans, channels");
		CMLib.socials().clearAllSocials();
		CMLib.clans().shutdownClans();
        CMLib.channels().unloadChannels();
        CMLib.journals().unloadCommandJournals();
        CMLib.polls().unload();

		CMLib.help().unloadHelpFile(null);

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading classes");
		CMClass.unload();
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading map");
		CMLib.map().unLoad();
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading resources");
		Resources.clearResources();
		Log.sysOut("MUD","Resources Cleared.");
		if(S!=null)S.println("All resources unloaded");


		for(int i=0;i<webServers.size();i++)
		{
			HTTPserver webServerThread=(HTTPserver)webServers.elementAt(i);
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down web server "+webServerThread.getName()+"...");
			webServerThread.shutdown(S);
			Log.sysOut("MUD","Web server "+webServerThread.getName()+" stopped.");
			if(S!=null)S.println("Web server "+webServerThread.getName()+" stopped");
		}
		webServers.clear();
		
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading macros");
		Scripts.clear();
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut("MUD","CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
        CMLib.threads().resumeAll();
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.logoff();
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		keepDown=keepItDown;
		execExternalCommand=externalCommand;
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutdown: you are the special lucky chosen one!");
		for(int m=mudThreads.size()-1;m>=0;m--)
			CMLib.killThread((MUD)mudThreads.elementAt(m),100,1);
		if(!keepItDown)
			CMProps.setBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN,false);
	}


	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				servsock = null;
			}
			catch(IOException e)
			{
			}
		}
		super.interrupt();
	}

	public static int activeCount(ThreadGroup tGroup)
	{
		int realAC=0;
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive())
				realAC++;
		}
		return realAC;
	}

	public static int killCount(ThreadGroup tGroup, Thread thisOne)
	{
		int killed=0;

		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != thisOne))
			{
				CMLib.killThread(tArray[i],500,1);
				killed++;
			}
		}
		return killed;
	}

	public static void threadList(ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive())
			{
			    if(tArray[i] instanceof Session)
			    {
			        Session S=(Session)tArray[i];
					Log.sysOut("MUD", "-->Thread: Session status "+S.getStatus()+"-"+CMParms.combine(S.previousCMD(),0) + "\n\r");
			    }
			    else
			    if(tArray[i] instanceof Tickable)
			    {
			        Tickable T=(Tickable)tArray[i];
					Log.sysOut("MUD", "-->Thread: "+T.ID()+"-"+T.name()+"-"+T.getTickStatus() + "\n\r");
			    }
			    else
                if((tArray[i] instanceof Tick)
                &&(((Tick)tArray[i]).lastClient!=null)
                &&(((Tick)tArray[i]).lastClient.clientObject!=null))
                    Log.sysOut("MUD", "-->Thread: "+tArray[i].getName()+" "+((Tick)tArray[i]).lastClient.clientObject.ID()+"-"+((Tick)tArray[i]).lastClient.clientObject.name()+"-"+((Tick)tArray[i]).lastClient.clientObject.getTickStatus() + "\n\r");
                else
					Log.sysOut("MUD", "-->Thread: "+tArray[i].getName() + "\n\r");
			}
		}
	}

	public String getHost()
	{
		return host;
	}
	public int getPort()
	{
		return port;
	}

	public static void main(String a[])
	{
		CMProps.setBoolVar(CMProps.SYSTEMB_MUDSTARTED,false);
		CMProps.setVar(CMProps.SYSTEM_MUDVER,HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
		DBConnector currentDBconnector=new DBConnector();
        CMLib.registerLibrary(new DBInterface(currentDBconnector));
        CMLib.registerLibrary(new ServiceEngine());
        CMLib.registerLibrary(new IMudClient());
		CMLib.registerLibrary(new ProcessHTTPrequest(null,null,null,true));
        CMProps page=null;
		
		String nameID="";
		String iniFile="coffeemud.ini";
		if(a.length>0)
		{
			for(int i=0;i<a.length;i++)
				nameID+=" "+a[i];
			nameID=nameID.trim();
			Vector V=CMParms.paramParse(nameID);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
				{
					iniFile=s.substring(5);
					V.removeElementAt(v);
					v--;
				}
			}
			nameID=CMParms.combine(V,0);
		}
		if(nameID.length()==0) nameID="Unnamed CoffeeMud";
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDNAME,nameID);
		try
		{
			while(true)
			{
				page=CMProps.loadPropPage("//"+iniFile);
				if ((page==null)||(!page.loaded))
				{
					Log.startLogFiles("mud",1);
					Log.setLogOutput("BOTH","BOTH","BOTH","BOTH","BOTH");
					Log.errOut("MUD","ERROR: Unable to read ini file: '"+iniFile+"'.");
					System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
					CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"A terminal error has occured!");
					System.exit(-1);
				}
				
				isOK = true;
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting");
				CMProps.setVar(CMProps.SYSTEM_INIPATH,iniFile,false);
				CMProps.setVar(CMProps.SYSTEM_MUDBINDADDRESS,page.getStr("BIND"));
				CMProps.setIntVar(CMProps.SYSTEMI_MUDBACKLOG,page.getInt("BACKLOG"));
				Log.startLogFiles("mud",page.getInt("NUMLOGS"));
				Log.setLogOutput(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("WRNMSGS"),page.getStr("DBGMSGS"),page.getStr("HLPMSGS"));

				System.out.println();
				Log.sysOut("MUD","CoffeeMud v"+CMProps.getVar(CMProps.SYSTEM_MUDVER));
				Log.sysOut("MUD","(C) 2000-2006 Bo Zimmerman");
				Log.sysOut("MUD","http://coffeemud.zimmers.net");

				Scripts.setLocale(page.getStr("LANGUAGE"),page.getStr("COUNTRY"));
				if(MUD.isOK)
				{
					mudThreads=new Vector();
					String ports=page.getProperty("PORT");
					int pdex=ports.indexOf(",");
					while(pdex>0)
					{
						MUD mud=new MUD();
						mud.acceptConnections=false;
						mud.port=CMath.s_int(ports.substring(0,pdex));
						ports=ports.substring(pdex+1);
						mud.start();
						mudThreads.addElement(mud);
						pdex=ports.indexOf(",");
					}
					MUD mud=new MUD();
					mud.acceptConnections=false;
					mud.port=CMath.s_int(ports);
					mud.start();
					mudThreads.addElement(mud);
				}

				StringBuffer str=new StringBuffer("");
				for(int m=0;m<mudThreads.size();m++)
				{
					MudHost mud=(MudHost)mudThreads.elementAt(m);
					str.append(" "+mud.getPort());
				}
				CMProps.setVar(CMProps.SYSTEM_MUDPORTS,str.toString());
				
		        Runtime.getRuntime().addShutdownHook(new Thread() {
		            public void run() {
		            	if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN))
		            		MUD.globalShutdown(null,true,null);
		            }
		        });
		        
				if(initHost(Thread.currentThread(),page))
					((MUD)mudThreads.firstElement()).join();

				System.gc();
                try{Thread.sleep(1000);}catch(Exception e){}
				System.runFinalization();
                try{Thread.sleep(1000);}catch(Exception e){}
                
				if(activeCount(Thread.currentThread().getThreadGroup())>1)
				{
					try{ Thread.sleep(1000);}catch(Exception e){}
					killCount(Thread.currentThread().getThreadGroup(),Thread.currentThread());
					if(activeCount(Thread.currentThread().getThreadGroup())>1)
					{
						Log.sysOut("MUD","WARNING: " + activeCount(Thread.currentThread().getThreadGroup()) +" other thread(s) are still active!");
						threadList(Thread.currentThread().getThreadGroup());
					}
				}
				if(keepDown)
				   break;
				if(execExternalCommand!=null)
				{
					//Runtime r=Runtime.getRuntime();
					//Process p=r.exec(external);
					Log.sysOut("Attempted to execute '"+execExternalCommand+"'.");
					execExternalCommand=null;
					break;
				}
			}
		}
		catch(InterruptedException e)
		{
			Log.errOut("MUD",e);
		}
	}
}
