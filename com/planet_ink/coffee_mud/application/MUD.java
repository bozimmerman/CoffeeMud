package com.planet_ink.coffee_mud.application;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import com.planet_ink.coffee_mud.system.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.commands.*;

public class MUD extends Thread implements Host
{
	public SaveThread saveThread=null;
	public INI page=null;
	public boolean keepDown=true;
	public String execExternalCommand=null;

	public static final float HOST_VERSION_MAJOR=(float)3.0;
	public static final float HOST_VERSION_MINOR=(float)0.1;
	
	private boolean acceptConnections=false;
	private String offlineReason=new String("UNKNOWN");
	public boolean isOK = false;
	
	public ServerSocket servsock=null;


	public MUD()
	{
		super("MUD-host");

		isOK = false;
		
		if (!loadPropPage())
		{
			Log.errOut("MUD","ERROR: Unable to read ini file.");
			offlineReason=new String("A terminal error has occured!");
		}
		else
		{
			isOK = true;
			offlineReason=new String("Booting");
		}
		acceptConnections = false;
	}	
	
	private boolean loadPropPage()
	{
		if (page==null || !page.loaded)
		{
			page=new INI("coffeemud.ini");
			if(!page.loaded)
				return false;
		}
		return true;
	}

	public void fatalStartupError(int type)
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
			//...
		default:
			str="Fatal error loading classes.  Make sure you start up coffeemud from the directory containing the class files.";
			break;
		}
		Log.errOut("MUD",str);
		this.interrupt();
	}
	

	private boolean initHost()
	{
		if (!isOK)
		{
			this.interrupt();
			return false;
		}

		if ((page == null) || (!page.loaded))
		{
			fatalStartupError(1);
			return false;
		}
		
		Log.Initialize(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("DBGMSGS"));
		System.out.println();
		Log.sysOut("MUD",getVer());
		Log.sysOut("MUD","(C) 2000-2002 Bo Zimmerman");
		Log.sysOut("MUD","www.zimmers.net/home/mud.html");
		Log.sysOut("MUD","Starting...\n\r");
		
		DBConnector.DBConfirmDeletions=page.getBoolean("DBCONFIRMDELETIONS");
		offlineReason=new String("Booting: connecting to database");
		DBConnector.connect(page.getStr("DBCLASS"),page.getStr("DBSERVICE"),page.getStr("DBUSER"),page.getStr("DBPASS"),page.getInt("DBCONNECTIONS"),true);
		String DBerrors=DBConnector.errorStatus().toString();
		if(DBerrors.length()==0)
			Log.sysOut("MUD","Database connection successful.");
		else
		{
			Log.errOut("MUD","Fatal database error: "+DBerrors);
			System.exit(-1);
			//fatalStartupError(3);
			//return false;
		}
		if(DBConnector.DBConfirmDeletions)
			Log.sysOut("MUD","DB Deletions will be confirmed.");
		
		CommandProcessor commandProcessor=new CommandProcessor();
		ExternalPlay.setPlayer(new ExternalCommands(commandProcessor), new ExternalSystems());

		if(!CMClass.loadClasses(page))
		{
			fatalStartupError(0);
			return false;
		}

		int numChannelsLoaded=commandProcessor.channels.loadChannels(page.getStr("CHANNELS"),commandProcessor.commandSet);
		commandProcessor.myHost=this;
		Log.sysOut("MUD","Channels loaded   : "+numChannelsLoaded);

		commandProcessor.socials.load("resources"+File.separatorChar+"socials.txt");
		if(!commandProcessor.socials.loaded)
			Log.errOut("MUD","WARNING: Unable to load socials from socials.txt!");
		else
			Log.sysOut("MUD","Socials loaded    : "+commandProcessor.socials.num());

		Log.sysOut("MUD","Loading map...");
		offlineReason=new String("Booting: loading rooms (this can take a while).");
		RoomLoader.DBRead(CMMap.AREAS,CMMap.map);
		Log.sysOut("MUD","Mapped rooms      : "+CMMap.map.size()+" in "+CMMap.AREAS.size()+" areas");
		if(CMMap.map.size()==0)
		{
			Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
			String id=page.getStr("START");
			if(id.length()==0) id="START";
			Room room=CMClass.getLocale("StdRoom");
			room.setID(id);
			room.setDisplayText("New Room");
			room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.  If your character is not an Archon, pick up the book you see here and read it immediately!");
			RoomLoader.DBCreate(room,"CoffeeMud");
			Item I=CMClass.getMiscMagic("ManualArchon");
			room.addItem(I);
			CMMap.map.addElement(room);
		}

		offlineReason=new String("Booting: readying for connections.");
		try
		{
			CMMap.setStartRoom(page.getStr("START"));

			commandProcessor.commandSet.loadAbilities(CMClass.abilities);

			saveThread=new SaveThread();
			saveThread.start();
			Log.sysOut("MUD","Save thread started");
		}
		catch (Throwable t)
		{
			Log.sysOut("MUD","CoffeeMud Server initHost() failed");
			fatalStartupError(4);
			return false;
		}

		acceptConnections = true;
		Log.sysOut("MUD","Initialization complete. Port: "+page.getInt("PORT"));
		offlineReason=new String("UNKNOWN");
		return true;
	}
	
	
	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		boolean serverOK = false;

		if (!isOK)	return;
		if ((page == null) || (!page.loaded))
		{
			Log.errOut("MUD","ERROR: Host thread will not run with no properties.");
			return;
		}
		
		try
		{
			servsock=new ServerSocket(page.getInt("PORT"), q_len);

			//jef
			serverOK = true;

			while(true)
			{
				sock=servsock.accept();
				
				if (acceptConnections)
				{
					Log.sysOut("MUD","Got a connection.");
					StringBuffer introText=Resources.getFileResource("intro.txt");
					TelnetSession S=new TelnetSession(sock,
						introText != null ? introText.toString() : null);
					S.start();
					Sessions.addElement(S);
					// jef: whoops...
					sock = null;
				}
				else
				{
					Log.sysOut("MUD","Rejecting a connection.");
					StringBuffer rejectText=Resources.getFileResource("offline.txt");
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
			if ((servsock == null) && (serverOK))
			{
				if((t!=null)&&(t instanceof Exception)&&((t.getMessage()==null)||(t.getMessage().indexOf("closed")<0)))
					Log.errOut("MUD",((Exception)t));
			}

			if (!serverOK)
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

		Log.sysOut("MUD","CoffeeMud Server thread stopped!");
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


	public void shutdown(Session S, boolean keepItDown, String externalCommand)
	{
		if(saveThread==null) return;

		offlineReason=new String("Shutting down" + (keepItDown? "..." : " and restarting...") );
		acceptConnections = false;
		Log.sysOut("MUD","Host will now reject new connections.");
		S.println("Host will now reject new connections.");

		saveThread.shutdown();
		saveThread.interrupt();
		saveThread=null;
		S.println("Save thread stopped.");

		for(int s=0;s<Sessions.size();s++)
		{
			Session session=Sessions.elementAt(s);
			if(session.mob()!=null)
			{
				MOBloader.DBUpdate(session.mob());
				MOBloader.DBUpdateFollowers(session.mob());
			}
		}
		Log.sysOut("MUD","All users saved.");
		S.println("All users saved.");

		while(Sessions.size()>0)
		{
			Session S2=Sessions.elementAt(0);
			if(S2==S)
				Sessions.removeElementAt(0);
			else
				S2.logoff();
		}
		S.println("All users logged off.");

		ServiceEngine.shutdownAll();
		S.println("All threads stopped.");

		DBConnector.killConnections();
		Log.sysOut("MUD","All users saved.");
		S.println("Database connections closed.");

		CMClass.unload();
		CMMap.unLoad();
		page=null;
		CMClass.unload();
		Resources.clearResources();
		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut("MUD","CoffeeMud shutdown complete.");
		S.println("CoffeeMud shutdown complete.");
		if(!keepItDown)
			S.println("Restarting...");
		S.logoff();
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		this.keepDown=keepItDown;
		this.execExternalCommand=externalCommand;
		offlineReason=new String("Shutdown: you are the special lucky chosen one!");
		this.interrupt();
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
	
	public String getVer()
	{
		return "CoffeeMud v"+HOST_VERSION_MAJOR+"."+HOST_VERSION_MINOR;
	}
	
	public static void main(String a[]) throws IOException
	{
		Log.startLogFiles();
		
		try
		{
			while(true)
			{
				MUD mud=new MUD();
				mud.start();
				mud.initHost();
				mud.join();
				System.gc();
				System.runFinalization();
				boolean keepDown=mud.keepDown;
				String external=mud.execExternalCommand;
				mud=null;
				System.gc();
				System.runFinalization();
				if(Thread.activeCount()>1)
				{
					Log.sysOut("MUD","WARNING: " + (Thread.activeCount()-1) +" other thread(s) are still active!");
					threadList(Thread.currentThread().getThreadGroup());
				}
				if(keepDown)
					break;
				if(external!=null)
				{
					//Runtime r=Runtime.getRuntime();
					//Process p=r.exec(external);
					Log.sysOut("Attempted to execute '"+external+"'.");
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
