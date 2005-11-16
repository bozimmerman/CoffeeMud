package com.planet_ink.coffee_mud.application;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.system.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.i3.*;
import com.planet_ink.coffee_mud.i3.server.Server;
import com.planet_ink.coffee_mud.i3.imc2.IMC2Driver;
import com.planet_ink.coffee_mud.web.*;
import com.planet_ink.coffee_mud.web.espresso.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public static final float HOST_VERSION_MAJOR=(float)4.7;
	public static final long  HOST_VERSION_MINOR=11;
	
	public static boolean keepDown=true;
	public static String execExternalCommand=null;
	public static SaveThread saveThread=null;
	public static UtiliThread utiliThread=null;
	public static Server imserver=null;
	public static IMC2Driver imc2server=null;
	public static HTTPserver webServerThread=null;
	public static HTTPserver adminServerThread=null;
    public static EspressoServer espserver=null;
	public static SMTPserver smtpServerThread=null;
	public static Vector mudThreads=new Vector();
	public static DVector accessed=new DVector(2);
	public static Vector autoblocked=new Vector();

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
		t.interrupt();
	}


	private static boolean initHost(Thread t, INI page)
	{

		if (!isOK)
		{
			t.interrupt();
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

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: connecting to database");
		
		CommonStrings.loadCommonINISettings(page);
		
		Vector compress=Util.parseCommas(page.getStr("COMPRESS").toUpperCase(),true);
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS,compress.contains("ITEMDESC"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS,compress.contains("GENMOBS"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ROOMDCOMPRESS,compress.contains("ROOMDESC"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS,compress.contains("MOBDESC"));
		Resources.setCompression(compress.contains("RESOURCES"));
		Vector nocache=Util.parseCommas(page.getStr("NOCACHE").toUpperCase(),true);
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBNOCACHE,nocache.contains("GENMOBS"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ROOMDNOCACHE,nocache.contains("ROOMDESC"));
		
		
		DBConnector.connect(page.getStr("DBCLASS"),page.getStr("DBSERVICE"),page.getStr("DBUSER"),page.getStr("DBPASS"),page.getInt("DBCONNECTIONS"),true);
		String DBerrors=DBConnector.errorStatus().toString();
		if((DBerrors.length()==0)||(DBerrors.startsWith("OK!")))
			Log.sysOut("MUD","Database connection successful.");
		else
		{
			Log.errOut("MUD","Fatal database error: "+DBerrors);
			System.exit(-1);
		}


		if(page.getStr("RUNWEBSERVERS").equalsIgnoreCase("true"))
		{
			webServerThread = new HTTPserver((MudHost)mudThreads.firstElement(),"pub");
			webServerThread.start();
			adminServerThread = new HTTPserver((MudHost)mudThreads.firstElement(),"admin");
			adminServerThread.start();
			CMClass.registerExternalHTTP(new ProcessHTTPrequest(null,(adminServerThread!=null)?adminServerThread:(webServerThread!=null)?webServerThread:null,null,true));
		}
		else
			CMClass.registerExternalHTTP(new ProcessHTTPrequest(null,null,null,true));

		
		if(page.getStr("RUNSMTPSERVER").equalsIgnoreCase("true"))
		{
			smtpServerThread = new SMTPserver((MudHost)mudThreads.firstElement());
			smtpServerThread.start();
			CMClass.ThreadEngine().startTickDown(smtpServerThread,MudHost.TICK_EMAIL,60);
		}

        if(page.getBoolean("RUNESPRESSOSERVER"))
        {
			try
			{
			    int espport=page.getInt("ESPRESSOPORT");
			    if(espport==0) espport=27755;
			    espserver=new EspressoServer((MudHost)mudThreads.firstElement(),espport);
			    espserver.start();
			    EspressoServer.loadEspressoCommands();
			}
			catch(Exception e)
			{
			    Log.errOut("MUD",e);
			}
        }
		
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: loading base classes");
		if(!CMClass.loadClasses(page))
		{
			fatalStartupError(t,0);
			return false;
		}
		CMSecurity.setSysOp(page.getStr("SYSOPMASK")); // requires all classes be loaded
		CMSecurity.parseGroups(page);

		int numChannelsLoaded=ChannelSet.loadChannels(page.getStr("CHANNELS"),
													  page.getStr("ICHANNELS"),
													  page.getStr("IMC2CHANNELS"));
        int numJournalsLoaded=ChannelSet.loadCommandJournals(page.getStr("COMMANDJOURNALS"));
		Log.sysOut("MUD","Channels loaded   : "+(numChannelsLoaded+numJournalsLoaded));

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: loading socials");
		Socials.load("resources"+File.separatorChar+"socials.txt");
		if(!Socials.isLoaded())
			Log.errOut("MUD","WARNING: Unable to load socials from socials.txt!");
		else
			Log.sysOut("MUD","Socials loaded    : "+Socials.num());

		ClanLoader.DBRead();
		Log.sysOut("MUD","Clans loaded      : "+Clans.size());
		
		Factions FS=new Factions();
		CMClass.ThreadEngine().startTickDown(FS,MudHost.TICK_MOB,10);

		Log.sysOut("MUD","Loading map...");
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: loading rooms....");
		RoomLoader.DBRead();
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: filling map ("+A.Name()+")");
			A.fillInAreaRooms();
		}
		Log.sysOut("MUD","Mapped rooms      : "+CMMap.numRooms()+" in "+CMMap.numAreas()+" areas");
		CMMap.initStartRooms(page);
		CMMap.initDeathRooms(page);
		CMMap.initBodyRooms(page);
		

		if(CMMap.numRooms()==0)
		{
			Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
			String id="START";
			Area newArea=CMClass.DBEngine().DBCreateArea("New Area","StdArea");
			Room room=CMClass.getLocale("StdRoom");
			room.setArea(newArea);
			room.setRoomID(id);
			room.setDisplayText("New Room");
			room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.  If your character is not an Archon, pick up the book you see here and read it immediately!");
			RoomLoader.DBCreate(room,"StdRoom");
			Item I=CMClass.getMiscMagic("ManualArchon");
			room.addItem(I);
			CMMap.addRoom(room);
			CMClass.DBEngine().DBUpdateItems(room);
		}

		CMClass.DBEngine().DBReadQuests((MudHost)mudThreads.firstElement());
		if(Quests.numQuests()>0)
			Log.sysOut("MUD","Quests loaded     : "+Quests.numQuests());

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: readying for connections.");
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
				IMudInterface imud=new IMudInterface(CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME),
													 "CoffeeMud v"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDVER),
													 ((MUD)mudThreads.firstElement()).getPort(),
													 playstate,
													 ChannelSet.iChannelsArray());
				imserver=new Server();
				int i3port=page.getInt("I3PORT");
				if(i3port==0) i3port=27766;
				Server.start(CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME),i3port,imud);
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
										CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME),
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
					CMClass.I3Interface().registerIMC2(imc2server);
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
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSTARTED,true);
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"OK");
		return true;
	}


	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		serverIsRunning = false;

		if (!isOK)	return;

		InetAddress bindAddr = null;

		if (CommonStrings.getIntVar(CommonStrings.SYSTEMI_MUDBACKLOG) > 0)
			q_len = CommonStrings.getIntVar(CommonStrings.SYSTEMI_MUDBACKLOG);

		if (CommonStrings.getVar(CommonStrings.SYSTEM_MUDBINDADDRESS).length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(CommonStrings.getVar(CommonStrings.SYSTEM_MUDBINDADDRESS));
			}
			catch (UnknownHostException e)
			{
				Log.errOut("MUD","ERROR: MUD Server could not bind to address " + CommonStrings.getVar(CommonStrings.SYSTEM_MUDBINDADDRESS));
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
						StringBuffer introText=Resources.getFileResource("text"+File.separatorChar+"intro.txt");
						TelnetSession S=new TelnetSession(sock,
							introText != null ? introText.toString() : null);
						S.start();
						Sessions.addElement(S);
						sock = null;
					}
				}
				else
				{
					StringBuffer rejectText=Resources.getFileResource("text"+File.separatorChar+"offline.txt");
					PrintWriter out = new PrintWriter(sock.getOutputStream());
					out.println("\n\rOFFLINE: " + CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS)+"\n\r");
					out.flush();
					out.println(rejectText);
					out.flush();
                    try{Thread.sleep(100);}catch(Exception e){}
					out.close();
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
        if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSHUTTINGDOWN))
            return CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS);
        if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
            return CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS);
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
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSTARTED,false);
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSHUTTINGDOWN,true);
        CMClass.ThreadEngine().suspendAll();
		if(S!=null)S.print("Closing MUD listeners to new connections...");
		for(int i=0;i<mudThreads.size();i++)
			((MUD)mudThreads.elementAt(i)).acceptConnections=false;
		Log.sysOut("MUD","New Connections are now closed");
		if(S!=null)S.println("Done.");
		
		if((!CMSecurity.isSaveFlag("NOPLAYERS"))
		&&(saveThread!=null))
		{
			if(S!=null)S.print("Saving players...");
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Saving players...");
			saveThread.savePlayers();
            CoffeeTables.update();
			if(S!=null)S.println("done");
			Log.sysOut("MUD","All users saved.");
		}
        if(S!=null)S.print("Saving stats...");
        CoffeeTables.update();
        if(S!=null)S.println("done");
        Log.sysOut("MUD","Stats saved.");
		
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut("MUD","Notifying all objects...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_SHUTDOWN,null);
		try
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				R.send(mob,msg);
			}
	    }catch(NoSuchElementException e){}
		if(S!=null)S.println("done");
		if((saveThread==null)||(utiliThread==null))
		{
			CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSHUTTINGDOWN,false);
            CMClass.ThreadEngine().resumeAll();
		    return;
		}

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Quests");
		Quests.shutdown();

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Save Thread");
		saveThread.shutdown();
		saveThread.interrupt();
		saveThread=null;
		
		if(S!=null)S.println("Save thread stopped");
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Utility Thread");
		utiliThread.shutdown();
		utiliThread.interrupt();
		utiliThread=null;
		if(S!=null)S.println("Utility thread stopped");
		Log.sysOut("MUD","Utility/Save Threads stopped.");
		
		if(CMSecurity.isSaveFlag("ROOMMOBS")
		||CMSecurity.isSaveFlag("ROOMITEMS")
		||CMSecurity.isSaveFlag("ROOMSHOPS"))
		{
			if(S!=null)S.print("Saving room data...");
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Map Update");
			int roomCounter=0;
			for(Enumeration e=CMMap.areas();e.hasMoreElements();)
			{
			    Area A=(Area)e.nextElement();
			    A.toggleMobility(false);
			}
			if(CMSecurity.isSaveFlag("ROOMSHOPS")&&(!CMSecurity.isSaveFlag("ROOMMOBS")))
			{
			    try
			    {
					for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
					{
					    Room R=(Room)e.nextElement();
					    for(int m=0;m<R.numInhabitants();m++)
					    {
					        MOB M=R.fetchInhabitant(m);
					        if((M instanceof ShopKeeper)
					        &&(M.isEligibleMonster())
					        &&(M.getStartRoom()!=R)
					        &&(M.getStartRoom()!=null))
					            M.getStartRoom().bringMobHere(M,false);
					    }
					}
			    }catch(NoSuchElementException e){}
			}
			else
			if(CMSecurity.isSaveFlag("ROOMMOBS"))
			{
			    try
			    {
					for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
					{
					    Room R=(Room)e.nextElement();
					    if(R.roomID().length()==0)
					    for(int m=0;m<R.numInhabitants();m++)
					    {
					        MOB M=R.fetchInhabitant(m);
					        if((M!=null)
					        &&(M.isEligibleMonster())
					        &&(M.getStartRoom()!=R)
					        &&(M.getStartRoom()!=null)
					        &&(M.getStartRoom().roomID().length()>0))
					            M.getStartRoom().bringMobHere(M,false);
					    }
					}
			    }catch(NoSuchElementException e){}
			}
            Vector shopmobs=new Vector();
            Vector bodies=new Vector();
			for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
			{
			    if(((++roomCounter)%200)==0)
			    {
			        if(S!=null) S.print(".");
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Map Update ("+roomCounter+")");
			    }
			    Room R=(Room)e.nextElement();
			    if(R.roomID().length()>0)
			    {
			        if(CMSecurity.isSaveFlag("ROOMMOBS"))
			        {
			            for(int m=0;m<R.numInhabitants();m++)
			            {
			                MOB M=R.fetchInhabitant(m);
			                if((M!=null)&&(M.isEligibleMonster()))
			                    M.setStartRoom(R);
			            }
			            CMClass.DBEngine().DBUpdateMOBs(R);
			        }
			        else
			        if(CMSecurity.isSaveFlag("ROOMSHOPS"))
			        {
			            shopmobs.clear();
			            for(int m=0;m<R.numInhabitants();m++)
			            {
			                MOB M=R.fetchInhabitant(m);
			                if((M!=null)
			                &&(M.isEligibleMonster())
			                &&(M instanceof ShopKeeper)
			                &&(M.getStartRoom()==R))
			                    shopmobs.addElement(M);
			            }
			            if(shopmobs.size()>0)
				            CMClass.DBEngine().DBUpdateTheseMOBs(R,shopmobs);
			        }
			        if(CMSecurity.isSaveFlag("ROOMITEMS"))
			        {
			            bodies.clear();
				        for(int i=0;i<R.numItems();i++)
				        {
				            Item I=R.fetchItem(i);
				            if(I instanceof DeadBody)
				                bodies.addElement(I);
				        }
				        for(int i=0;i<bodies.size();i++)
				            ((Item)bodies.elementAt(i)).destroy();
			            CMClass.DBEngine().DBUpdateItems(R);
			        }
			    }
			}
			if(S!=null)S.println("done");
			Log.sysOut("MUD","Map data saved.");
		    
		}

		if(imserver!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...I3Server");
			Server.shutdown();
			imserver=null;
			if(S!=null)S.println("I3Server stopped");
			Log.sysOut("MUD","I3Server stopped");
		}

		if(imc2server!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...IMC2Server");
			imc2server.shutdown();
			imc2server=null;
			if(S!=null)S.println("IMC2Server stopped");
			Log.sysOut("MUD","IMC2Server stopped");
		}
		
		if(S!=null)S.print("Stopping player sessions...");
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Stopping sessions");
		while(Sessions.size()>0)
		{
			Session S2=Sessions.elementAt(0);
			if((S!=null)&&(S2==S))
				Sessions.removeElementAt(0);
			else
			{
				CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Stopping session "+S2.getAddress());
				S2.logoff();
				CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Done stopping session "+S2.getAddress());
			}
			if(S!=null)S.print(".");
		}
		if(S!=null)S.println("All users logged off");
		Log.sysOut("MUD","All users logged off.");

		if(smtpServerThread!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...smtp server");
			smtpServerThread.shutdown(S);
			smtpServerThread = null;
			Log.sysOut("MUD","SMTP Server stopped.");
			if(S!=null)S.println("SMTP Server stopped");
		}
		
		if(S!=null)S.print("Stopping all threads...");
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...shutting down service engine");
		CMClass.ThreadEngine().shutdownAll();
		if(S!=null)S.println("done");
		Log.sysOut("MUD","Map Threads Stopped.");

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...closing db connections");
		DBConnector.killConnections();
		if(S!=null)S.println("Database connections closed");
		Log.sysOut("MUD","Database connections closed.");

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Clearing socials, clans, channels");
		Socials.clearAllSocials();
		Clans.shutdownClans();
		ChannelSet.unloadChannelsAndCommandJournals();
        Polls.unload();

		MUDHelp.unloadHelpFile(null);

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...unloading classes");
		CMClass.unload();
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...unloading map");
		CMMap.unLoad();
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...unloading resources");
		Resources.clearResources();
		Log.sysOut("MUD","Resources Cleared.");
		if(S!=null)S.println("All resources unloaded");


		if(webServerThread!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...pub webserver");
			webServerThread.shutdown(S);
			webServerThread = null;
			Log.sysOut("MUD","Public Web Server stopped.");
			if(S!=null)S.println("Public Web Server stopped");
		}
		if(adminServerThread!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...admin webserver");
			adminServerThread.shutdown(S);
			adminServerThread = null;
			Log.sysOut("MUD","Admin Web Server stopped.");
			if(S!=null)S.println("Admin Web Server stopped");
		}
		
		if(espserver!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...espresso server");
			espserver.shutdown(S);
			espserver = null;
			Log.sysOut("MUD","Espresso Server stopped.");
			if(S!=null)S.println("Espresso Server stopped");
		}
		
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...unloading macros");
		Scripts.clear();
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut("MUD","CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
        CMClass.ThreadEngine().resumeAll();
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.logoff();
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		keepDown=keepItDown;
		execExternalCommand=externalCommand;
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutdown: you are the special lucky chosen one!");
		for(int m=mudThreads.size()-1;m>=0;m--)
			((MUD)mudThreads.elementAt(m)).interrupt();
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSHUTTINGDOWN,false);
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
				tArray[i].interrupt();
				try{Thread.sleep(500);}catch(Exception e){}
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
					Log.sysOut("MUD", "-->Thread: Session status "+S.getStatus()+"-"+Util.combine(S.previousCMD(),0) + "\n\r");
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
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSTARTED,false);
		CommonStrings.setVar(CommonStrings.SYSTEM_MUDVER,HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
		CMClass.registerEngines(new DBInterface(),new ServiceEngine());
		CMClass.registerI3Interface(new IMudClient());
		CMClass.registerExternalHTTP(new ProcessHTTPrequest(null,null,null,true));
		INI page=null;
		
		String nameID="";
		String iniFile="coffeemud.ini";
		if(a.length>0)
		{
			for(int i=0;i<a.length;i++)
				nameID+=" "+a[i];
			nameID=nameID.trim();
			Vector V=Util.paramParse(nameID);
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
			nameID=Util.combine(V,0);
		}
		if(nameID.length()==0) nameID="Unnamed CoffeeMud";
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDNAME,nameID);
		try
		{
			while(true)
			{
				page=INI.loadPropPage(iniFile);
				if ((page==null)||(!page.loaded))
				{
					Log.startLogFiles(1);
					Log.errOut("MUD","ERROR: Unable to read ini file: '"+iniFile+"'.");
					System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"A terminal error has occured!");
					System.exit(-1);
				}
				
				isOK = true;
				CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting");
				CommonStrings.setVar(CommonStrings.SYSTEM_INIPATH,iniFile,false);
				CommonStrings.setVar(CommonStrings.SYSTEM_MUDBINDADDRESS,page.getStr("BIND"));
				CommonStrings.setIntVar(CommonStrings.SYSTEMI_MUDBACKLOG,page.getInt("BACKLOG"));
				Log.startLogFiles(page.getInt("NUMLOGS"));
				Log.Initialize(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("DBGMSGS"));

				System.out.println();
				Log.sysOut("MUD","CoffeeMud v"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDVER));
				Log.sysOut("MUD","(C) 2000-2005 Bo Zimmerman");
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
						mud.port=Util.s_int(ports.substring(0,pdex));
						ports=ports.substring(pdex+1);
						mud.start();
						mudThreads.addElement(mud);
						pdex=ports.indexOf(",");
					}
					MUD mud=new MUD();
					mud.acceptConnections=false;
					mud.port=Util.s_int(ports);
					mud.start();
					mudThreads.addElement(mud);
				}

				StringBuffer str=new StringBuffer("");
				for(int m=0;m<mudThreads.size();m++)
				{
					MudHost mud=(MudHost)mudThreads.elementAt(m);
					str.append(" "+mud.getPort());
				}
				CommonStrings.setVar(CommonStrings.SYSTEM_MUDPORTS,str.toString());
				
				if(initHost(Thread.currentThread(),page))
					((MUD)mudThreads.firstElement()).join();

				System.gc();
				System.runFinalization();

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
