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
import com.planet_ink.coffee_mud.i3.server.*;
import com.planet_ink.coffee_mud.Commands.*;
import com.planet_ink.coffee_mud.Commands.base.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.web.*;


public class MUD extends Thread implements Host
{
	public static final float HOST_VERSION_MAJOR=(float)4.1;
	public static final long  HOST_VERSION_MINOR=1;

	public static String nameID="My Mud";
	public static boolean keepDown=true;
	public static String execExternalCommand=null;
	public static SaveThread saveThread=null;
	public static UtiliThread utiliThread=null;
	public static INI page=null;
	public static INI webCommon=null;
	public static Server imserver=null;
	public static HTTPserver webServerThread=null;
	public static HTTPserver adminServerThread=null;
	public static Vector mudThreads=new Vector();
	private static String offlineReason="UNKNOWN";

	public static boolean serverIsRunning = false;
	public static boolean isOK = false;
	public final static String ServerVersionString = "CoffeeMUD-MainServer/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;

	public boolean acceptConnections=false;
	public int port=5555;
	ServerSocket servsock=null;

	public MUD()
	{
		super("MUD-MainServer");
	}

	private static boolean loadPropPage(String iniFile)
	{
		if (page==null || !page.loaded)
		{
			page=new INI(iniFile);
			if(!page.loaded)
				return false;
		}
		return true;
	}

	private static boolean loadWebCommonPropPage()
	{
		if (webCommon==null || !webCommon.loaded)
		{
			webCommon=new INI("web" + File.separatorChar + "common.ini");
			if(!webCommon.loaded)
				return false;
		}
		return true;
	}

	public Properties getCommonPropPage()
	{
		return webCommon;
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


    // returns "(unknown/not set)" if property not set
    private static String getCheckedProp(Properties sysprop,String id)
    {
        try
        {
            String s = (String)sysprop.get(id);
            return (s == null)?"(unknown/not set)":s;
        }
        catch (SecurityException e)
        {
            return "(security exception: " + e.getMessage() + ")";
        }
    }

    private static void logSystemCfg()
    {
        // GET SYSTEM PROPERTIES
        ///////////////////////////////////
        Properties sysprop = null;
        try
        {
            sysprop = System.getProperties();
        }
        catch (SecurityException e)
        {
            System.err.println("Security exception - cannot get system properties: " + e.getMessage());
            return;
        }

        // PRINT SYSTEM PROPERTIES
        ///////////////////////////////////
        Log.sysOut("User",getCheckedProp(sysprop,"user.name"));
        Log.sysOut("OS",getCheckedProp(sysprop,"os.name")
                    + " " + getCheckedProp(sysprop,"os.version")
                    + "/" + getCheckedProp(sysprop,"os.arch")
                    + " (" + getCheckedProp(sysprop,"sun.cpu.endian") + " endian)");

        Log.sysOut("Java", "version " + getCheckedProp(sysprop,"java.version"));
        Log.sysOut("Java", getCheckedProp(sysprop,"java.runtime.name"));
        Log.sysOut("Java", getCheckedProp(sysprop,"java.vendor") );

        Log.sysOut("Java VM", getCheckedProp(sysprop,"java.vm.name")
                            + " " + getCheckedProp(sysprop,"java.vm.version"));
        Log.sysOut("Java VM", getCheckedProp(sysprop,"java.vm.vendor") );

        // It's easier to use Locale.getDefault() and TimeZone.getDefault() than
        //  system properties user.region, user.timezone and user.language
        Log.sysOut("Locale", Locale.getDefault().getDisplayName());
        Log.sysOut("Timezone", TimeZone.getDefault().getDisplayName(true,TimeZone.LONG) );
    }


	private static boolean initHost(Thread t)
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

		offlineReason=new String("Booting: connecting to database");
		CommonStrings.setVar(CommonStrings.SYSTEM_BADNAMES,page.getStr("BADNAMES"));
		CommonStrings.setVar(CommonStrings.SYSTEM_MULTICLASS,page.getStr("CLASSSYSTEM"));
		CommonStrings.setVar(CommonStrings.SYSTEM_PKILL,page.getStr("PLAYERKILL"));
		CommonStrings.setVar(CommonStrings.SYSTEM_PLAYERDEATH,page.getStr("PLAYERDEATH"));
		CommonStrings.setVar(CommonStrings.SYSTEM_PLAYERFLEE,page.getStr("FLEE"));
		CommonStrings.setVar(CommonStrings.SYSTEM_SHOWDAMAGE,page.getStr("SHOWDAMAGE"));
		CommonStrings.setVar(CommonStrings.SYSTEM_EMAILREQ,page.getStr("EMAILREQ"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC0,page.getStr("ESCAPE0"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC1,page.getStr("ESCAPE1"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC2,page.getStr("ESCAPE2"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC3,page.getStr("ESCAPE3"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC4,page.getStr("ESCAPE4"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC5,page.getStr("ESCAPE5"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC6,page.getStr("ESCAPE6"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC7,page.getStr("ESCAPE7"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC8,page.getStr("ESCAPE8"));
		CommonStrings.setVar(CommonStrings.SYSTEM_ESC9,page.getStr("ESCAPE9"));
		CommonStrings.setVar(CommonStrings.SYSTEM_MSPPATH,page.getStr("SOUNDPATH"));
		CommonStrings.setVar(CommonStrings.SYSTEM_CLANVOTED,page.getStr("CLANVOTED"));
		CommonStrings.setVar(CommonStrings.SYSTEM_CLANVOTEO,page.getStr("CLANVOTEO"));
		CommonStrings.setVar(CommonStrings.SYSTEM_CLANVOTER,page.getStr("CLANVOTER"));
		CommonStrings.setVar(CommonStrings.SYSTEM_AUTOPURGE,page.getStr("AUTOPURGE"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_PAGEBREAK,page.getStr("PAGEBREAK"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_CLANENCHCOST,page.getStr("CLANENCHCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF,page.getStr("FOLLOWLEVELDIFF"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_EXPRATE,page.getStr("EXPRATE"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_SKYSIZE,page.getStr("SKYSIZE"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MAXSTAT,page.getStr("MAXSTATS"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANACOST,page.getStr("MANACOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_EDITORTYPE,0);
		if(page.getStr("EDITORTYPE").equalsIgnoreCase("WIZARD")) CommonStrings.setIntVar(CommonStrings.SYSTEMI_EDITORTYPE,1);
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MINCLANMEMBERS,page.getStr("MINCLANMEMBERS"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_CLANCOST,page.getStr("CLANCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_DAYSCLANDEATH,page.getStr("DAYSCLANDEATH"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MINCLANLEVEL,page.getStr("MINCLANLEVEL"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST,page.getStr("SKILLPRACCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST,page.getStr("SKILLTRAINCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST,page.getStr("COMMONPRACCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST,page.getStr("COMMONTRAINCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_LANGPRACCOST,page.getStr("LANGPRACCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_LANGTRAINCOST,page.getStr("LANGTRAINCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_LASTPLAYERLEVEL,page.getStr("LASTPLAYERLEVEL"));
		Vector compress=Util.parseCommas(page.getStr("COMPRESS").toUpperCase());
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS,compress.contains("ITEMDESC"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS,compress.contains("GENMOBS"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ROOMDCOMPRESS,compress.contains("ROOMDESC"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS,compress.contains("MOBDESC"));
		Resources.setCompression(compress.contains("RESOURCES"));

		DBConnector.connect(page.getStr("DBCLASS"),page.getStr("DBSERVICE"),page.getStr("DBUSER"),page.getStr("DBPASS"),page.getInt("DBCONNECTIONS"),true);
		String DBerrors=DBConnector.errorStatus().toString();
		if(DBerrors.length()==0)
			Log.sysOut("MUD","Database connection successful.");
		else
		{
			Log.errOut("MUD","Fatal database error: "+DBerrors);
			System.exit(-1);
		}


		if (page.getStr("RUNWEBSERVERS").equalsIgnoreCase("true"))
		{
			if (loadWebCommonPropPage())
			{
				webServerThread = new HTTPserver((Host)mudThreads.firstElement(),"pub");
				webServerThread.start();
				adminServerThread = new HTTPserver((Host)mudThreads.firstElement(),"admin");
				adminServerThread.start();
				if(!HTTPserver.loadWebMacros())
					Log.errOut("MUD","Unable to loadWebMacros");
			}
			else
				Log.errOut("MUD","Unable to start web server - loadWebCommonPropPage() failed");
		}

		ExternalPlay.setPlayer(ExternalCommands.getInstance(), new ExternalSystems(), new IMudClient());


		offlineReason="Booting: loading base classes";
		if(!CMClass.loadClasses(page))
		{
			fatalStartupError(t,0);
			return false;
		}

		int numChannelsLoaded=Channels.loadChannels(page.getStr("CHANNELS"),page.getStr("ICHANNELS"),CommandSet.getInstance());
		CommandProcessor.myHost=(Host)mudThreads.firstElement();
		Log.sysOut("MUD","Channels loaded   : "+numChannelsLoaded);

		offlineReason="Booting: loading socials";
		Socials.load("resources"+File.separatorChar+"socials.txt");
		if(!Socials.isLoaded())
			Log.errOut("MUD","WARNING: Unable to load socials from socials.txt!");
		else
			Log.sysOut("MUD","Socials loaded    : "+Socials.num());

		ClanLoader.DBRead((Host)mudThreads.firstElement());
		Log.sysOut("MUD","Clans loaded      : "+Clans.size());

		Log.sysOut("MUD","Loading map...");
		offlineReason="Booting: loading rooms (0% completed).";
		RoomLoader.DBRead((Host)mudThreads.firstElement());
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			offlineReason="Booting: filling map ("+A.Name()+")";
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
			Area newArea=ExternalPlay.DBCreateArea("New Area","StdArea");
			Room room=CMClass.getLocale("StdRoom");
			room.setArea(newArea);
			room.setRoomID(id);
			room.setDisplayText("New Room");
			room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.  If your character is not an Archon, pick up the book you see here and read it immediately!");
			RoomLoader.DBCreate(room,"StdRoom");
			Item I=CMClass.getMiscMagic("ManualArchon");
			room.addItem(I);
			CMMap.addRoom(room);
			ExternalPlay.DBUpdateItems(room);
		}

		ExternalPlay.DBReadQuests((Host)mudThreads.firstElement());
		if(Quests.numQuests()>0)
			Log.sysOut("MUD","Quests loaded     : "+Quests.numQuests());

		offlineReason="Booting: readying for connections.";
		try
		{
			CommandProcessor.commandSet.loadAbilities(CMClass.abilities());

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
				IMudInterface imud=new IMudInterface(nameID,
													 getGlobalVer(),
													 ((MUD)mudThreads.firstElement()).getPort(),
													 playstate,
													 Channels.iChannelsArray());
				imserver=new Server();
				int i3port=page.getInt("I3PORT");
				if(i3port==0) i3port=27766;
				imserver.start(nameID,i3port,imud);
			}
		}
		catch(Exception e)
		{
			Log.errOut("MUD",e);
		}


		for(int i=0;i<mudThreads.size();i++)
			((MUD)mudThreads.elementAt(i)).acceptConnections=true;
		Log.sysOut("MUD","Initialization complete.");
		ExternalPlay.setSystemStarted(nameID);
		offlineReason="UNKNOWN";
		return true;
	}


	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		serverIsRunning = false;

		if (!isOK)	return;

		if ((page == null) || (!page.loaded))
		{
			Log.errOut("MUD","ERROR: Host thread will not run with no properties.");
			return;
		}

		InetAddress bindAddr = null;

		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		if (page.getStr("BIND") != null && page.getStr("BIND").length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(page.getStr("BIND"));
			}
			catch (UnknownHostException e)
			{
				Log.errOut("MUD","ERROR: MUD Server could not bind to address " + page.getStr("BIND"));
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
				sock=servsock.accept();

				if (acceptConnections)
				{
					String address="unknown";
					try{address=sock.getInetAddress().getHostAddress();}catch(Exception e){}
					Log.sysOut("MUD","Got a connection from "+address);
					// now see if they are banned!
					Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
					boolean ok=true;
					if((banned!=null)&&(banned.size()>0))
					for(int b=0;b<banned.size();b++)
					{
						String str=(String)banned.elementAt(b);
						if(str.length()>0)
						{
							if(str.equals("*")||((str.indexOf("*")<0))&&(str.equals(address))) ok= false;
							else
							if(str.startsWith("*")&&str.endsWith("*")&&(address.indexOf(str.substring(1,str.length()-1))>=0)) ok= false;
							else
							if(str.startsWith("*")&&(address.endsWith(str.substring(1)))) ok= false;
							else
							if(str.endsWith("*")&&(address.startsWith(str.substring(0,str.length()-1)))) ok= false;
						}
						if(!ok) break;
					}
					if(!ok)
					{
						Log.sysOut("MUD","Blocking a connection from "+address);
						PrintWriter out = new PrintWriter(sock.getOutputStream());
						out.println("\n\rOFFLINE: Blocked\n\r");
						out.flush();
						out.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
						out.flush();
						out.close();
						sock = null;
					}
					else
					{
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
					String address="unknown";
					try{address=sock.getInetAddress().getHostAddress();}catch(Exception e){}
					Log.sysOut("MUD","Rejecting a connection from "+address);
					StringBuffer rejectText=Resources.getFileResource("text"+File.separatorChar+"offline.txt");
					PrintWriter out = new PrintWriter(sock.getOutputStream());
					out.println("\n\rOFFLINE: " + offlineReason+"\n\r");
					out.flush();
					out.println(rejectText);
					out.flush();
					out.close();
					sock = null;
				}
			}
		}
		catch(Throwable t)
		{
			if((t!=null)&&(t instanceof Exception))
				Log.errOut("MUD",((Exception)t).getMessage());

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
		if((saveThread==null)||(utiliThread==null)) return;

		offlineReason="Shutting down" + (keepItDown? "..." : " and restarting...");
		for(int i=0;i<mudThreads.size();i++)
			((MUD)mudThreads.elementAt(i)).acceptConnections=false;
		Log.sysOut("MUD","Host will now reject new connections.");
		if(S!=null)S.println("Host will now reject new connections.");

		offlineReason="Shutting down...Quests";
		Quests.shutdown();

		offlineReason="Shutting down...Saving players...";
		saveThread.savePlayers();
		if(S!=null)S.println("All users saved.");
		offlineReason="Shutting down...Save Thread";
		saveThread.shutdown();
		saveThread.interrupt();
		saveThread=null;
		if(S!=null)S.println("Save thread stopped.");
		offlineReason="Shutting down...Utility Thread";
		utiliThread.shutdown();
		utiliThread.interrupt();
		utiliThread=null;
		if(S!=null)S.println("Utility thread stopped.");
		

		offlineReason="Shutting down...IMServer";
		if(imserver!=null)
		{
			imserver.shutdown();
			imserver=null;
			if(S!=null)S.println("IMServer stopped.");
		}

		offlineReason="Shutting down...Stopping sessions";
		while(Sessions.size()>0)
		{
			Session S2=Sessions.elementAt(0);
			if((S!=null)&&(S2==S))
				Sessions.removeElementAt(0);
			else
			{
				offlineReason="Shutting down...Stopping session "+S2.getAddress();
				S2.logoff();
				offlineReason="Shutting down...Done stopping session "+S2.getAddress();
			}
		}
		if(S!=null)S.println("All users logged off.");

		offlineReason="Shutting down...shutting down service engine";
		ServiceEngine.shutdownAll();
		if(S!=null)S.println("All threads stopped.");

		offlineReason="Shutting down...closing db connections";
		DBConnector.killConnections();
		Log.sysOut("MUD","All users saved.");
		if(S!=null)S.println("Database connections closed.");

		offlineReason="Shutting down...Clearing socials, clans, channels";
		Socials.clearAllSocials();
		Clans.shutdownClans();
		Channels.unloadChannels();

		Help.unloadHelpFile(null);

		offlineReason="Shutting down...unloading classes";
		CMClass.unload();
		offlineReason="Shutting down...unloading map";
		CMMap.unLoad();
		page=null;
		offlineReason="Shutting down...unloading resources";
		Resources.clearResources();
		webCommon=null;
		if(webServerThread!=null)
		{
			offlineReason="Shutting down...pub webserver";
			webServerThread.shutdown(S);
			webServerThread = null;
		}
		if(adminServerThread!=null)
		{
			offlineReason="Shutting down...admin webserver";
			adminServerThread.shutdown(S);
			adminServerThread = null;
		}
		offlineReason="Shutting down...unloading macros";
		HTTPserver.unloadWebMacros();
		Scripts.clear();
		offlineReason="Shutting down" + (keepItDown? "..." : " and restarting...");

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut("MUD","CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.logoff();
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		keepDown=keepItDown;
		execExternalCommand=externalCommand;
		offlineReason="Shutdown: you are the special lucky chosen one!";
		for(int m=mudThreads.size()-1;m>=0;m--)
			((MUD)mudThreads.elementAt(m)).interrupt();
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

	public static void threadList(ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive())
				Log.sysOut("MUD", "-->Thread: "+tArray[i].getName() + "\n\r");
		}
	}

	public static String getGlobalVer()
	{
		return "CoffeeMud v"+HOST_VERSION_MAJOR+"."+HOST_VERSION_MINOR;
	}
	public String getVer()
	{
		return getGlobalVer();
	}

	public boolean isGameRunning()
	{
		return acceptConnections;
	}

	public int getPort()
	{
		return port;
	}
	public String getPortStr()
	{
		StringBuffer str=new StringBuffer("");
		for(int m=0;m<mudThreads.size();m++)
		{
			Host mud=(Host)mudThreads.elementAt(m);
			str.append(" "+mud.getPort());
		}
		return str.toString().trim();
	}

	public String ServerVersionString()
	{
		return ServerVersionString;
	}

	public String gameStatusStr()
	{
		if (acceptConnections)
			return "OK";
		else
			return offlineReason;
	}

	public void setGameStatusStr(String str)
	{
		offlineReason=str;
	}

	public static void main(String a[]) throws IOException
	{
		Log.startLogFiles();

		nameID="";
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
		try
		{
			while(true)
			{
				if (!loadPropPage(iniFile))
				{
					Log.errOut("MUD","ERROR: Unable to read ini file.");
					System.out.println("MUD/ERROR: Unable to read ini file.");
					offlineReason="A terminal error has occured!";
				}
				else
				{
					isOK = true;
					offlineReason="Booting";
				}
				if(page!=null)
					Log.Initialize(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("DBGMSGS"));

				System.out.println();
				Log.sysOut("MUD",getGlobalVer());
				Log.sysOut("MUD","(C) 2000-2003 Bo Zimmerman");
				Log.sysOut("MUD","www.zimmers.net/home/mud.html");

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

				if(initHost(Thread.currentThread()))
					((MUD)mudThreads.firstElement()).join();

				System.gc();
				System.runFinalization();

				if(activeCount(Thread.currentThread().getThreadGroup())>1)
				{
					Log.sysOut("MUD","WARNING: " + activeCount(Thread.currentThread().getThreadGroup()) +" other thread(s) are still active!");
					threadList(Thread.currentThread().getThreadGroup());
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
