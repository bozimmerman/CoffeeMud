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
import com.planet_ink.coffee_mud.web.*;
import com.planet_ink.coffee_mud.web.espresso.*;


public class MUD extends Thread implements MudHost
{
	public static final float HOST_VERSION_MAJOR=(float)4.2;
	public static final long  HOST_VERSION_MINOR=7;
	
	public static boolean keepDown=true;
	public static String execExternalCommand=null;
	public static SaveThread saveThread=null;
	public static UtiliThread utiliThread=null;
	public static INI page=null;
	public static Server imserver=null;
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

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: connecting to database");
		
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
		if(page.getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("LEVEL"))
			CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANACONSUMEAMT,-100);
		else
		if(page.getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("SPELLLEVEL"))
			CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANACONSUMEAMT,-200);
		else
			CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANACONSUMEAMT,Util.s_int(page.getStr("MANACONSUMEAMT").trim()));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANACONSUMETIME,page.getStr("MANACONSUMETIME"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_PAGEBREAK,page.getStr("PAGEBREAK"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MINMOVETIME,page.getStr("MINMOVETIME"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_CLANENCHCOST,page.getStr("CLANENCHCOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_FOLLOWLEVELDIFF,page.getStr("FOLLOWLEVELDIFF"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_EXPRATE,page.getStr("EXPRATE"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_SKYSIZE,page.getStr("SKYSIZE"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MAXSTAT,page.getStr("MAXSTATS"));
		if(page.getStr("BASEMAXSTAT").length()==0)
			CommonStrings.setIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT,18);
		else
			CommonStrings.setIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT,page.getStr("BASEMAXSTAT"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANACOST,page.getStr("MANACOST"));
		CommonStrings.setIntVar(CommonStrings.SYSTEMI_MANAMINCOST,page.getStr("MANAMINCOST"));
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
		Vector compress=Util.parseCommas(page.getStr("COMPRESS").toUpperCase(),true);
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS,compress.contains("ITEMDESC"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS,compress.contains("GENMOBS"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_ROOMDCOMPRESS,compress.contains("ROOMDESC"));
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MOBDCOMPRESS,compress.contains("MOBDESC"));
		Resources.setCompression(compress.contains("RESOURCES"));

		CommonStrings.setDisableVars(page.getStr("DISABLE"));
		if(page.getStr("DISABLE").trim().length()>0)
			Log.sysOut("MUD","Disabled subsystems: "+page.getStr("DISABLE"));
		CommonStrings.setDebugVars(page.getStr("DEBUG"));
		
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
			if(!HTTPserver.loadWebMacros())
				Log.errOut("MUD","Unable to loadWebMacros");
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
			    espserver.loadEspressoCommands();
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

		int numChannelsLoaded=ChannelSet.loadChannels(page.getStr("CHANNELS"),page.getStr("ICHANNELS"));
		Log.sysOut("MUD","Channels loaded   : "+numChannelsLoaded);

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting: loading socials");
		Socials.load("resources"+File.separatorChar+"socials.txt");
		if(!Socials.isLoaded())
			Log.errOut("MUD","WARNING: Unable to load socials from socials.txt!");
		else
			Log.sysOut("MUD","Socials loaded    : "+Socials.num());

		ClanLoader.DBRead();
		Log.sysOut("MUD","Clans loaded      : "+Clans.size());

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
				imserver.start(CommonStrings.getVar(CommonStrings.SYSTEM_MUDNAME),i3port,imud);
			}
		}
		catch(Exception e)
		{
			Log.errOut("MUD",e);
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
					try{address=sock.getInetAddress().getHostAddress().trim();}catch(Exception e){}
					Log.sysOut("MUD","Got a connection from "+address);
					// now see if they are banned!
					Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
					int proceed=0;
					if((banned!=null)&&(banned.size()>0))
					for(int b=0;b<banned.size();b++)
					{
						String str=(String)banned.elementAt(b);
						if(str.length()>0)
						{
							if(str.equals("*")||((str.indexOf("*")<0))&&(str.equals(address))) proceed=1;
							else
							if(str.startsWith("*")&&str.endsWith("*")&&(address.indexOf(str.substring(1,str.length()-1))>=0)) proceed=1;
							else
							if(str.startsWith("*")&&(address.endsWith(str.substring(1)))) proceed=1;
							else
							if(str.endsWith("*")&&(address.startsWith(str.substring(0,str.length()-1)))) proceed=1;
						}
						if(proceed!=0) break;
					}

					int numAtThisAddress=0;
					long ConnectionWindow=(180*1000);
					long LastConnectionDelay=(5*60*1000);
					boolean anyAtThisAddress=false;
					int maxAtThisAddress=6;
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
					if(proceed!=0)
					{
						Log.sysOut("MUD","Blocking a connection from "+address);
						PrintWriter out = new PrintWriter(sock.getOutputStream());
						out.println("\n\rOFFLINE: Blocked\n\r");
						out.flush();
						if(proceed==2)
							out.println("\n\rYour address has been blocked temporarily due to excessive invalid connections.  Please try back in "+((int)Math.round(LastConnectionDelay/60000))+" minutes, and not before.\n\r\n\r");
						else
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
					out.println("\n\rOFFLINE: " + CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS)+"\n\r");
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
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSTARTED,false);
		if(S!=null)S.print("Closing MUD listeners to new connections...");
		for(int i=0;i<mudThreads.size();i++)
			((MUD)mudThreads.elementAt(i)).acceptConnections=false;
		Log.sysOut("MUD","New Connections are now closed");
		if(S!=null)S.println("Done.");
		
		if(saveThread!=null)
		{
			if(S!=null)S.print("Saving players...");
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Saving players...");
			saveThread.savePlayers();
			if(S!=null)S.println("done");
			Log.sysOut("MUD","All users saved.");
		}
		
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut("MUD","Notifying all objects...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_SHUTDOWN,null);
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.send(mob,msg);
		}
		if(S!=null)S.println("done");
		if((saveThread==null)||(utiliThread==null)) return;

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


		if(imserver!=null)
		{
			CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...IMServer");
			imserver.shutdown();
			imserver=null;
			if(S!=null)S.println("IMServer stopped");
			Log.sysOut("MUD","IMServer stopped");
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
		ChannelSet.unloadChannels();

		MUDHelp.unloadHelpFile(null);

		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...unloading classes");
		CMClass.unload();
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down...unloading map");
		CMMap.unLoad();
		page=null;
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
		HTTPserver.unloadWebMacros();
		Scripts.clear();
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));

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
		CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Shutdown: you are the special lucky chosen one!");
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
				((Thread)tArray[i]).interrupt();
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
				Log.sysOut("MUD", "-->Thread: "+tArray[i].getName() + "\n\r");
		}
	}

	public int getPort()
	{
		return port;
	}

	public static void main(String a[]) throws IOException
	{
		CommonStrings.setBoolVar(CommonStrings.SYSTEMB_MUDSTARTED,false);
		CommonStrings.setVar(CommonStrings.SYSTEM_MUDVER,HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
		CMClass.registerEngines(new DBInterface(),new ServiceEngine());
		CMClass.registerI3Interface(new IMudClient());
		CMClass.registerExternalHTTP(new ProcessHTTPrequest(null,null,null,true));
		
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
				if (!loadPropPage(iniFile))
				{
					Log.startLogFiles(1);
					Log.errOut("MUD","ERROR: Unable to read ini file.");
					System.out.println("MUD/ERROR: Unable to read ini file.");
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"A terminal error has occured!");
				}
				else
				{
					isOK = true;
					CommonStrings.setUpLowVar(CommonStrings.SYSTEM_MUDSTATUS,"Booting");
				}
				if(page!=null)
				{
					Log.startLogFiles(page.getInt("NUMLOGS"));
					Log.Initialize(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("DBGMSGS"));
				}

				System.out.println();
				Log.sysOut("MUD","CoffeeMud v"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDVER));
				Log.sysOut("MUD","(C) 2000-2004 Bo Zimmerman");
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
				
				if(initHost(Thread.currentThread()))
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
