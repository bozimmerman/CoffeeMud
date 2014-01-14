package com.planet_ink.coffee_mud.application;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.smtp.SMTPserver;
import com.planet_ink.coffee_mud.core.intermud.IMudClient;
import com.planet_ink.coffee_mud.core.intermud.cm1.CM1Server;
import com.planet_ink.coffee_mud.core.intermud.i3.IMudInterface;
import com.planet_ink.coffee_mud.core.intermud.imc2.IMC2Driver;
import com.planet_ink.coffee_mud.core.intermud.i3.server.I3Server;
import com.planet_ink.miniweb.http.MIMEType;
import com.planet_ink.miniweb.interfaces.FileManager;
import com.planet_ink.miniweb.server.MiniWebServer;
import com.planet_ink.miniweb.util.MiniWebConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter; // for writing to sockets
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.sql.*;


/*
   Copyright 2000-2014 Bo Zimmerman

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
	private static final float	  HOST_VERSION_MAJOR	= (float)5.8;
	private static final long	  HOST_VERSION_MINOR	= 1;
	private static enum MudState {STARTING,WAITING,ACCEPTING,STOPPED}


	private volatile MudState state		 = MudState.STOPPED;
	private ServerSocket	  servsock	 = null;
	private boolean			  acceptConns= false;
	private String			  host		 = "MyHost";
	private int				  port		 = 5555;
	private final long		  startupTime= System.currentTimeMillis();
	private final ThreadGroup threadGroup;

	private static volatile int	 		grpid				= 0;
	private static boolean				bringDown			= false;
	private static String				execExternalCommand	= null;
	private static I3Server				i3server			= null;
	private static IMC2Driver			imc2server			= null;
	private static List<MiniWebServer>	webServers			= new Vector<MiniWebServer>();
	private static SMTPserver			smtpServerThread	= null;
	private static List<String> 		autoblocked			= new Vector<String>();
	private static List<DBConnector>	databases			= new Vector<DBConnector>();
	private static List<CM1Server>		cm1Servers			= new Vector<CM1Server>();
	private static List<Triad<String,Long,Integer>>
										accessed			= new LinkedList<Triad<String,Long,Integer>>();
	private static final ServiceEngine	serviceEngine		= new ServiceEngine();

	public MUD(String name)
	{
		super(name);
		threadGroup=Thread.currentThread().getThreadGroup();
	}

	@Override
	public void acceptConnection(Socket sock) throws SocketException, IOException 
	{
		setState(MudState.ACCEPTING);
		serviceEngine.executeRunnable(threadGroup.getName(),new ConnectionAcceptor(sock));
	}
	public ThreadGroup threadGroup() { return threadGroup; }
	
	private class ConnectionAcceptor implements CMRunnable
	{
		Socket sock;
		long startTime=0;
		public ConnectionAcceptor(Socket sock) throws SocketException, IOException
		{
			this.sock=sock;
			sock.setSoLinger(true,3);
		}
		public void run()
		{
			startTime=System.currentTimeMillis();
			try
			{
				if (acceptConns)
				{
					String address="unknown";
					try{address=sock.getInetAddress().getHostAddress().trim();}catch(Exception e){}
					int proceed=0;
					if(CMSecurity.isBanned(address))
						proceed=1;
					int numAtThisAddress=0;
					long LastConnectionDelay=(5*60*1000);
					boolean anyAtThisAddress=false;
					int maxAtThisAddress=6;
					if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONNSPAMBLOCK))
					{
						if(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_CONNS, address))
						{
							synchronized(accessed)
							{
								for(Iterator<Triad<String,Long,Integer>> i=accessed.iterator();i.hasNext();)
								{
									Triad<String,Long,Integer> triad=i.next();
									if((triad.second.longValue()+LastConnectionDelay)<System.currentTimeMillis())
										i.remove();
									else
									if(triad.first.trim().equalsIgnoreCase(address))
									{
										anyAtThisAddress=true;
										triad.second=Long.valueOf(System.currentTimeMillis());
										numAtThisAddress=triad.third.intValue()+1;
										triad.third=Integer.valueOf(numAtThisAddress);
									}
								}
								if(!anyAtThisAddress)
									accessed.add(new Triad<String,Long,Integer>(address,Long.valueOf(System.currentTimeMillis()),Integer.valueOf(1)));
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
								autoblocked.add(address.toUpperCase());
								proceed=2;
							}
						}
					}

					if(proceed!=0)
					{
						int abusiveCount=numAtThisAddress-maxAtThisAddress+1;
						long rounder=Math.round(Math.sqrt(abusiveCount));
						if(abusiveCount == (rounder*rounder))
							Log.sysOut(Thread.currentThread().getName(),"Blocking a connection from "+address +" ("+numAtThisAddress+")");
						try
						{
							PrintWriter out = new PrintWriter(sock.getOutputStream());
							out.println("\n\rOFFLINE: Blocked\n\r");
							out.flush();
							if(proceed==2)
								out.println("\n\rYour address has been blocked temporarily due to excessive invalid connections.  Please try back in " + (LastConnectionDelay/60000) + " minutes, and not before.\n\r\n\r");
							else
								out.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
							out.flush();
							try{Thread.sleep(250);}catch(Exception e){}
							out.close();
						}
						catch(IOException e)
						{
							// dont say anything, just eat it.
						}
						sock = null;
					}
					else
					{
						Log.sysOut(Thread.currentThread().getName(),"Connection from "+address);
						// also the intro page
						CMFile introDir=new CMFile(Resources.makeFileResourceName("text"),null,CMFile.FLAG_FORCEALLOW);
						String introFilename="text/intro.txt";
						if(introDir.isDirectory())
						{
							CMFile[] files=introDir.listFiles();
							Vector<String> choices=new Vector<String>();
							for(int f=0;f<files.length;f++)
								if(files[f].getName().toLowerCase().startsWith("intro")
								&&files[f].getName().toLowerCase().endsWith(".txt"))
									choices.addElement("text/"+files[f].getName());
							if(choices.size()>0) introFilename=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
						}
						StringBuffer introText=Resources.getFileResource(introFilename,true);
						try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
						Session S=(Session)CMClass.getCommon("DefaultSession");
						S.initializeSession(sock, threadGroup().getName(), introText != null ? introText.toString() : null);
						CMLib.sessions().add(S);
						sock = null;
					}
				}
				else
				if((CMLib.database()!=null)&&(CMLib.database().isConnected())&&(CMLib.encoder()!=null))
				{
					StringBuffer rejectText;
					
					try { rejectText = Resources.getFileResource("text/offline.txt",true);
					} catch(java.lang.NullPointerException npe) { rejectText=new StringBuffer("");}
					
					try
					{
						PrintWriter out = new PrintWriter(sock.getOutputStream());
						out.println("\n\rOFFLINE: " + CMProps.getVar(CMProps.Str.MUDSTATUS)+"\n\r");
						out.println(rejectText);
						out.flush();
						
						try{Thread.sleep(1000);}catch(Exception e){}
						out.close();
					}
					catch(IOException e)
					{
						// dont say anything, just eat it.
					}
					sock = null;
				}
				else
				{
					try{sock.close();}catch(Exception e){}
					sock = null;
				}
			}
			finally
			{
				startTime=0;
			}
		}
		public long activeTimeMillis() { return (startTime>0)?System.currentTimeMillis()-startTime:0;}
	}
	
	public String getLanguage() 
	{
		String lang = CMProps.instance().getStr("LANGUAGE").toUpperCase().trim();
		if(lang.length()==0) return "English";
		for(int i=0;i<LanguageLibrary.ISO_LANG_CODES.length;i++)
			if(lang.equals(LanguageLibrary.ISO_LANG_CODES[i][0]))
				return LanguageLibrary.ISO_LANG_CODES[i][1];
		return "English";
	}

	public void setState(MudState st)
	{
		if(st!=state)
			state=st;
	}
	
	public void run()
	{
		setState(MudState.STARTING);
		int q_len = 6;
		Socket sock=null;

		InetAddress bindAddr = null;

		if (CMProps.getIntVar(CMProps.Int.MUDBACKLOG) > 0)
			q_len = CMProps.getIntVar(CMProps.Int.MUDBACKLOG);

		if (CMProps.getVar(CMProps.Str.MUDBINDADDRESS).length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(CMProps.getVar(CMProps.Str.MUDBINDADDRESS));
			}
			catch (UnknownHostException e)
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: MUD Server could not bind to address " + CMProps.getVar(CMProps.Str.MUDBINDADDRESS));
			}
		}

		try
		{
			servsock=new ServerSocket(port, q_len, bindAddr);

			Log.sysOut(Thread.currentThread().getName(),"MUD Server started on port: "+port);
			if (bindAddr != null)
				Log.sysOut(Thread.currentThread().getName(),"MUD Server bound to: "+bindAddr.toString());
			CMLib.hosts().add(this);
			while(servsock!=null)
			{
				setState(MudState.WAITING);
				sock=servsock.accept();
				acceptConnection(sock);
			}
		}
		catch(Exception t)
		{
			if((!(t instanceof java.net.SocketException))
			||(t.getMessage()==null)
			||(t.getMessage().toLowerCase().indexOf("socket closed")<0))
			{
				Log.errOut(Thread.currentThread().getName(),t);
			}
		}

		Log.sysOut(Thread.currentThread().getName(),"Server cleaning up.");

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

		Log.sysOut(Thread.currentThread().getName(),"MUD on port "+port+" stopped!");
		setState(MudState.STOPPED);
		CMLib.hosts().remove(this);
	}
	
	public String getStatus()
	{
		if(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		return state.toString();
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
		CMProps.setBoolAllVar(CMProps.Bool.MUDSTARTED,false);
		CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN,true);
		serviceEngine.suspendAll();
		if(S!=null)S.print("Closing MUD listeners to new connections...");
		for(int i=0;i<CMLib.hosts().size();i++)
			CMLib.hosts().get(i).setAcceptConnections(false);
		Log.sysOut(Thread.currentThread().getName(),"New Connections are now closed");
		if(S!=null)S.println("Done.");

		if(!CMSecurity.isSaveFlag("NOPLAYERS"))
		{
			if(S!=null)S.print("Saving players...");
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Saving players...");
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.SESSIONS);e.hasMoreElements();)
			{
				SessionsList list=((SessionsList)e.nextElement());
				for(Session S2 : list.allIterable())
				{
					MOB M = S2.mob();
					if((M!=null)&&(M.playerStats()!=null))
					{
						M.playerStats().setLastDateTime(System.currentTimeMillis());
						// important! shutdown their affects!
						for(int a=M.numAllEffects()-1;a>=0;a--) // reverse enumeration
						{
							Ability A=M.fetchEffect(a);
							try {
								if((A!=null)&&(A.canBeUninvoked()))
									A.unInvoke();
								if((A!=null)&&(!A.isSavable()))
									M.delEffect(A);
							} catch(Exception ex) {Log.errOut(ex);}
						}
					}
				}
			}
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.PLAYERS);e.hasMoreElements();)
				((PlayerLibrary)e.nextElement()).savePlayers();
			if(S!=null)S.println("done");
			Log.sysOut(Thread.currentThread().getName(),"All users saved.");
		}
		if(S!=null)S.print("Saving stats...");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.STATS);e.hasMoreElements();)
			((StatisticsLibrary)e.nextElement()).update();
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Stats saved.");

		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut(Thread.currentThread().getName(),"Notifying all objects of shutdown...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_SHUTDOWN,null);
		Vector<Room> roomSet=new Vector<Room>();
		try
		{
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.MAP);e.hasMoreElements();)
			{
				WorldMap map=((WorldMap)e.nextElement());
				for(Enumeration<Area> a=map.areas();a.hasMoreElements();)
					a.nextElement().setAreaState(Area.State.STOPPED);
			}
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.MAP);e.hasMoreElements();)
			{
				WorldMap map=((WorldMap)e.nextElement());
				for(Enumeration<Room> r=map.rooms();r.hasMoreElements();)
				{
					Room R=r.nextElement();
					R.send(mob,msg);
					roomSet.addElement(R);
				}
			}
		}catch(NoSuchElementException e){}
		if(S!=null)S.println("done");
		final CMLib.Library[][] libraryShutdownLists={
				{CMLib.Library.QUEST,CMLib.Library.TECH,CMLib.Library.SESSIONS},
				{CMLib.Library.STATS,CMLib.Library.THREADS},
				{CMLib.Library.SOCIALS,CMLib.Library.CLANS,CMLib.Library.CHANNELS,CMLib.Library.JOURNALS,
					CMLib.Library.POLLS,CMLib.Library.HELP,CMLib.Library.CATALOG,CMLib.Library.MAP,
					CMLib.Library.PLAYERS
				}
		};
		
		for(CMLib.Library lib : libraryShutdownLists[0])
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down "+CMStrings.capitalizeAndLower(lib.name())+"...");
			for(Enumeration<CMLibrary> e=CMLib.libraries(lib);e.hasMoreElements();)
				e.nextElement().shutdown();
		}

		if(S!=null)S.println("Save thread stopped");

		if(CMSecurity.isSaveFlag("ROOMMOBS")
		||CMSecurity.isSaveFlag("ROOMITEMS")
		||CMSecurity.isSaveFlag("ROOMSHOPS"))
		{
			if(S!=null)S.print("Saving room data...");
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Rejuving the dead");
			serviceEngine.tickAllTickers(null);
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Map Update");
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.MAP);e.hasMoreElements();)
			{
				WorldMap map=((WorldMap)e.nextElement());
				for(Enumeration<Area> a=map.areas();a.hasMoreElements();)
					a.nextElement().setAreaState(Area.State.STOPPED);
			}
			int roomCounter=0;
			Room R=null;
			for(Enumeration<Room> e=roomSet.elements();e.hasMoreElements();)
			{
				if(((++roomCounter)%200)==0)
				{
					if(S!=null) S.print(".");
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Map Update ("+roomCounter+")");
				}
				R=e.nextElement();
				if(R.roomID().length()>0)
					R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
			}
			if(S!=null)S.println("done");
			Log.sysOut(Thread.currentThread().getName(),"Map data saved.");

		}

		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...CM1Servers");
		for(CM1Server cm1server : cm1Servers)
		{
			try
			{
				cm1server.shutdown();
			}
			finally
			{
				if(S!=null)S.println(cm1server.getName()+" stopped");
				Log.sysOut(Thread.currentThread().getName(),cm1server.getName()+" stopped");
			}
		}
		cm1Servers.clear();

		if(i3server!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...I3Server");
			I3Server.shutdown();
			i3server=null;
			if(S!=null)S.println("I3Server stopped");
			Log.sysOut(Thread.currentThread().getName(),"I3Server stopped");
		}

		if(imc2server!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...IMC2Server");
			imc2server.shutdown();
			imc2server=null;
			if(S!=null)S.println("IMC2Server stopped");
			Log.sysOut(Thread.currentThread().getName(),"IMC2Server stopped");
		}

		if(S!=null)S.print("Stopping player Sessions...");
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Stopping sessions");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.SESSIONS);e.hasMoreElements();)
		{
			SessionsList list=((SessionsList)e.nextElement());
			for(Session S2 : list.allIterable())
			{
				if((S!=null)&&(S2==S))
					list.remove(S2);
				else
				{
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Stopping session "+S2.getAddress());
					S2.stopSession(true,true,true);
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Done stopping session "+S2.getAddress());
				}
				if(S!=null)S.print(".");
			}
		}
		if(S!=null)S.println("All users logged off");
		try{Thread.sleep(3000);}catch(Exception e){/* give sessions a few seconds to inform the map */}
		Log.sysOut(Thread.currentThread().getName(),"All users logged off.");

		if(smtpServerThread!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...smtp server");
			smtpServerThread.shutdown(S);
			smtpServerThread = null;
			Log.sysOut(Thread.currentThread().getName(),"SMTP Server stopped.");
			if(S!=null)S.println("SMTP Server stopped");
		}

		if(S!=null)S.print("Stopping all threads...");
		for(CMLib.Library lib : libraryShutdownLists[1])
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down "+CMStrings.capitalizeAndLower(lib.name())+"...");
			for(Enumeration<CMLibrary> e=CMLib.libraries(lib);e.hasMoreElements();)
				e.nextElement().shutdown();
		}
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Map Threads Stopped.");
		
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down services...");
		for(CMLib.Library lib : libraryShutdownLists[2])
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down "+CMStrings.capitalizeAndLower(lib.name())+"...");
			for(Enumeration<CMLibrary> e=CMLib.libraries(lib);e.hasMoreElements();)
				e.nextElement().shutdown();
		}
		for(CMLib.Library lib : CMLib.Library.values())
		{
			boolean found=false;
			for(CMLib.Library[] prevSet : libraryShutdownLists)
				found=found||CMParms.contains(prevSet, lib);
			if(!found)
			{
				CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down "+CMStrings.capitalizeAndLower(lib.name())+"...");
				for(Enumeration<CMLibrary> e=CMLib.libraries(lib);e.hasMoreElements();)
					e.nextElement().shutdown();
			}
		}
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...unloading resources");
		Resources.shutdown();
		
		Log.sysOut(Thread.currentThread().getName(),"Resources Cleared.");
		if(S!=null)S.println("All resources unloaded");

		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...closing db connections");
		for(int d=0;d<databases.size();d++)
			databases.get(d).killConnections();
		if(S!=null)S.println("Database connections closed");
		Log.sysOut(Thread.currentThread().getName(),"Database connections closed.");

		for(int i=0;i<webServers.size();i++)
		{
			MiniWebServer webServerThread=webServers.get(i);
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down web server "+webServerThread.getName()+"...");
			webServerThread.close();
			Log.sysOut(Thread.currentThread().getName(),"Web server "+webServerThread.getName()+" stopped.");
			if(S!=null)S.println("Web server "+webServerThread.getName()+" stopped");
		}
		webServers.clear();

		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...unloading macros");
		CMLib.lang().clear();
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...unloading classes");
		CMClass.shutdown();
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
		bringDown=keepItDown;
		serviceEngine.resumeAll();
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.stopSession(true,true,false);
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		execExternalCommand=externalCommand;
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutdown: you are the special lucky chosen one!");
		for(int m=CMLib.hosts().size()-1;m>=0;m--)
			if(CMLib.hosts().get(m) instanceof Thread)
			{
				try{
					CMLib.killThread((Thread)CMLib.hosts().get(m),100,30);
				} catch(Exception t){}
			}
		CMLib.hosts().clear();
		if(!keepItDown)
			CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN,false);
	}


	private static void startIntermud3()
	{
		char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		CMProps page=CMProps.instance();
		try
		{
			if(page.getBoolean("RUNI3SERVER")&&(tCode==MAIN_HOST)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.I3)))
			{
				if(i3server!=null) I3Server.shutdown();
				i3server=null;
				String playstate=page.getStr("MUDSTATE");
				if((playstate==null)||(playstate.length()==0))
					playstate=page.getStr("I3STATE");
				if((playstate==null)||(!CMath.isInteger(playstate)))
					playstate="Development";
				else
				switch(CMath.s_int(playstate.trim()))
				{
				case 0: playstate = "MudLib Development"; break;
				case 1: playstate = "Restricted Access"; break;
				case 2: playstate = "Beta Testing"; break;
				case 3: playstate = "Open to the public"; break;
				default: playstate = "MudLib Development"; break;
				}
				IMudInterface imud=new IMudInterface(CMProps.getVar(CMProps.Str.MUDNAME),
													 "CoffeeMud v"+CMProps.getVar(CMProps.Str.MUDVER),
													 CMLib.mud(0).getPort(),
													 playstate,
													 CMLib.channels().getI3ChannelsList());
				i3server=new I3Server();
				int i3port=page.getInt("I3PORT");
				if(i3port==0) i3port=27766;
				I3Server.start(CMProps.getVar(CMProps.Str.MUDNAME),i3port,imud);
			}
		}
		catch(Exception e)
		{
			if(i3server!=null) I3Server.shutdown();
			i3server=null;
		}
	}
	
	private static void startCM1()
	{
		char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		CMProps page=CMProps.instance();
		CM1Server cm1server = null;
		try
		{
			String runcm1=page.getPrivateStr("RUNCM1SERVER");
			if((runcm1!=null)&&(runcm1.equalsIgnoreCase("TRUE")))
			{
				String iniFile = page.getStr("CM1CONFIG");
				for(CM1Server s : cm1Servers)
					if(s.getINIFilename().equalsIgnoreCase(iniFile))
					{
						s.shutdown();
						cm1Servers.remove(s);
					}
				cm1server=new CM1Server("CM1Server"+tCode,iniFile);
				cm1server.start();
				cm1Servers.add(cm1server);
			}
		}
		catch(Exception e)
		{
			if(cm1server!=null)
			{
				cm1server.shutdown();
				cm1Servers.remove(cm1server);
			}
		}
	}
	
	private static void startIntermud2()
	{
		char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		CMProps page=CMProps.instance();
		try
		{
			if(page.getBoolean("RUNIMC2CLIENT")&&(tCode==MAIN_HOST)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.IMC2)))
			{
				imc2server=new IMC2Driver();
				if(!imc2server.imc_startup(false,
										page.getStr("IMC2LOGIN").trim(),
										CMProps.getVar(CMProps.Str.MUDNAME),
										page.getStr("IMC2MYEMAIL").trim(),
										page.getStr("IMC2MYWEB").trim(),
										page.getStr("IMC2HUBNAME").trim(),
										page.getInt("IMC2HUBPORT"),
										page.getStr("IMC2PASS1").trim(),
										page.getStr("IMC2PASS2").trim(),
										CMLib.channels().getIMC2ChannelsList()))
				{
					Log.errOut(Thread.currentThread().getName(),"IMC2 Failed to start!");
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
			Log.errOut(e);
		}
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

	public static int activeThreadCount(ThreadGroup tGroup, boolean nonDaemonsOnly)
	{
		int realAC=0;
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != Thread.currentThread()) && ((!nonDaemonsOnly)||(!tArray[i].isDaemon())))
				realAC++;
		}
		return realAC;
	}

	private static int killCount(ThreadGroup tGroup, boolean nonDaemonsOnly)
	{
		int killed=0;

		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != Thread.currentThread()) && ((!nonDaemonsOnly)||(!tArray[i].isDaemon())))
			{
				CMLib.killThread(tArray[i],500,10);
				killed++;
			}
		}
		return killed;
	}

	private static void threadList(ThreadGroup tGroup, boolean nonDaemonsOnly)
	{
		if(tGroup==null)
			return;
		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != Thread.currentThread()) && ((!nonDaemonsOnly)||(!tArray[i].isDaemon())))
			{
				String summary;
				if(tArray[i] instanceof MudHost)
					summary=": "+CMClass.classID(tArray[i])+": "+((MudHost)tArray[i]).getStatus();
				else
				{
					final Runnable R=serviceEngine.findRunnableByThread(tArray[i]);
					if(R instanceof TickableGroup)
						summary=": "+((TickableGroup)R).getName()+": "+((TickableGroup)R).getStatus();
					else
					if(R instanceof Session)
					{
						final Session S=(Session)R;
						final MOB mob=S.mob();
						final String mobName=(mob==null)?"null":mob.Name();
						summary=": session "+mobName+": "+S.getStatus().toString()+": "+CMParms.combineWithQuotes(S.getPreviousCMD(),0);
					}
					else
					if(R instanceof CMRunnable)
						summary=": "+CMClass.classID(R)+": active for "+((CMRunnable)R).activeTimeMillis()+"ms";
					else
					if(CMClass.classID(R).length()>0)
						summary=": "+CMClass.classID(R);
					else
						summary="";
				}
				Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName() + summary+"\n\r");
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

	private static class HostGroup extends Thread
	{
		private String		  name=null;
		private String		  iniFile=null;
		private String		  logName=null;
		private char		  threadCode=MAIN_HOST;
		private boolean		  hostStarted=false;
		private boolean		  failedStart=false;
		//protected ThreadGroup threadGroup;
		
		public HostGroup(ThreadGroup G, String mudName, String iniFileName)
		{
			super(G,"HOST"+grpid);
			//threadGroup=G;
			synchronized("HostGroupInit".intern()) {
				logName="mud"+((grpid>0)?("."+grpid):"");
				grpid++;
				iniFile=iniFileName;
				name=mudName;
				setDaemon(true);
				threadCode=G.getName().charAt(0);
			}
		}
		
		public boolean isStarted() { return hostStarted; }
		public boolean failedToStart() { return failedStart; }

		public void fatalStartupError(Thread t, int type)
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
			Log.errOut(Thread.currentThread().getName(),str);
			bringDown=true;
			
			CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN,true);
			//CMLib.killThread(t,100,1);
		}

		protected boolean initHost()
		{
			final Thread t=Thread.currentThread();
			CMProps page=CMProps.instance();
			
			if ((page == null) || (!page.isLoaded()))
			{
				fatalStartupError(t,1);
				return false;
			}
			
			char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
			boolean checkPrivate=(tCode!=MAIN_HOST);
			
			List<String> compress=CMParms.parseCommas(page.getStr("COMPRESS").toUpperCase(),true);
			CMProps.setBoolVar(CMProps.Bool.ITEMDCOMPRESS,compress.contains("ITEMDESC"));
			CMProps.setBoolVar(CMProps.Bool.MOBCOMPRESS,compress.contains("GENMOBS"));
			CMProps.setBoolVar(CMProps.Bool.ROOMDCOMPRESS,compress.contains("ROOMDESC"));
			CMProps.setBoolVar(CMProps.Bool.MOBDCOMPRESS,compress.contains("MOBDESC"));
			Resources.setCompression(compress.contains("RESOURCES"));
			List<String> nocache=CMParms.parseCommas(page.getStr("NOCACHE").toUpperCase(),true);
			CMProps.setBoolVar(CMProps.Bool.MOBNOCACHE,nocache.contains("GENMOBS"));
			CMProps.setBoolVar(CMProps.Bool.ROOMDNOCACHE,nocache.contains("ROOMDESC"));
			CMProps.setBoolVar(CMProps.Bool.FILERESOURCENOCACHE, nocache.contains("FILERESOURCES"));
			CMProps.setBoolVar(CMProps.Bool.CATALOGNOCACHE, nocache.contains("CATALOG"));
			CMProps.setBoolVar(CMProps.Bool.MAPFINDSNOCACHE,nocache.contains("MAPFINDERS"));
			CMProps.setBoolVar(CMProps.Bool.ACCOUNTSNOCACHE,nocache.contains("ACCOUNTS"));
			CMProps.setBoolVar(CMProps.Bool.PLAYERSNOCACHE,nocache.contains("PLAYERS"));

			DBConnector currentDBconnector=null;
			String dbClass=page.getStr("DBCLASS");
			if(tCode!=MAIN_HOST)
			{
				DatabaseEngine baseEngine=(DatabaseEngine)CMLib.library(MAIN_HOST,CMLib.Library.DATABASE);
				while((!MUD.bringDown)
				&&((baseEngine==null)||(!baseEngine.isConnected()))) {
					try {Thread.sleep(500);}catch(Exception e){ break;}
					baseEngine=(DatabaseEngine)CMLib.library(MAIN_HOST,CMLib.Library.DATABASE);
				}
				if(MUD.bringDown) 
					return false;
				
				if(page.getPrivateStr("DBCLASS").length()==0)
				{
					CMLib.registerLibrary(baseEngine);
					dbClass="";
				}
			}
			if(dbClass.length()>0)
			{
				String dbService=page.getStr("DBSERVICE");
				String dbUser=page.getStr("DBUSER");
				String dbPass=page.getStr("DBPASS");
				int dbConns=page.getInt("DBCONNECTIONS");
				int dbPingIntMins=page.getInt("DBPINGINTERVALMINS");
				if(dbConns == 0)
				{
					Log.errOut(Thread.currentThread().getName(),"Fatal error: DBCONNECTIONS in INI file is "+dbConns);
					System.exit(-1);
				}
				boolean dbReuse=page.getBoolean("DBREUSE");
				boolean useQue=!CMSecurity.isDisabled(CMSecurity.DisFlag.DBERRORQUE);
				boolean useQueStart=!CMSecurity.isDisabled(CMSecurity.DisFlag.DBERRORQUESTART);
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: connecting to database");
				currentDBconnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbConns,dbPingIntMins,dbReuse,useQue,useQueStart);
				currentDBconnector.reconnect();
				CMLib.registerLibrary(new DBInterface(currentDBconnector,CMProps.getPrivateSubSet("DB.*")));

				DBConnection DBTEST=currentDBconnector.DBFetch();
				if(DBTEST!=null) currentDBconnector.DBDone(DBTEST);
				if((DBTEST!=null)&&(currentDBconnector.amIOk())&&(CMLib.database().isConnected()))
				{
					Log.sysOut(Thread.currentThread().getName(),"Connected to "+currentDBconnector.service());
					databases.add(currentDBconnector);
				}
				else
				{
					String DBerrors=currentDBconnector.errorStatus().toString();
					Log.errOut(Thread.currentThread().getName(),"Fatal database error: "+DBerrors);
					return false;
				}
			}
			else
			if(CMLib.database()==null)
			{
				Log.errOut(Thread.currentThread().getName(),"No registered database!");
				return false;
			}

			// test the database
			try {
				CMFile F = new CMFile("/test.the.database",null);
				if(F.exists())
					Log.sysOut(Thread.currentThread().getName(),"Test file found .. hmm.. that was unexpected.");
					
			} catch(Exception e) {
				Log.errOut(e);
				Log.errOut("Database error! Panic shutdown!");
				return false;
			}
			
			String webServersList=page.getPrivateStr("RUNWEBSERVERS");
			if(webServersList.equalsIgnoreCase("true"))
				webServersList="pub,admin";
			if((webServersList.length()>0)&&(!webServersList.equalsIgnoreCase("false")))
			{
				List<String> serverNames=CMParms.parseCommas(webServersList,true);
				for(int s=0;s<serverNames.size();s++)
				{
					String serverName=serverNames.get(s);
					try
					{
						StringBuffer commonProps=new CMFile("web/common.ini", null, CMFile.FLAG_LOGERRORS).text();
						StringBuffer finalProps=new CMFile("web/"+serverName+".ini", null, CMFile.FLAG_LOGERRORS).text();
						commonProps.append("\n").append(finalProps.toString());
						MiniWebConfig config=new MiniWebConfig();
						config.setFileManager(new CMFile.CMFileManager());
						MiniWebServer.initConfig(config, Log.instance(), new ByteArrayInputStream(commonProps.toString().getBytes()));
						if(CMSecurity.isDebugging(DbgFlag.HTTPREQ))
							config.setDebugFlag(page.getStr("DBGMSGS"));
						if(CMSecurity.isDebugging(DbgFlag.HTTPACCESS))
							config.setAccessLogFlag(page.getStr("ACCMSGS"));
						MiniWebServer webServer=new MiniWebServer(serverName+Thread.currentThread().getThreadGroup().getName().charAt(0),config);
						config.setMiniWebServer(webServer);
						webServer.start();
						webServers.add(webServer);
					}
					catch(Exception e)
					{
						Log.errOut("HTTP server "+serverName+"NOT started: "+e.getMessage());
					}
				}
			}

			if(page.getPrivateStr("RUNSMTPSERVER").equalsIgnoreCase("true"))
			{
				smtpServerThread = new SMTPserver(CMLib.mud(0));
				smtpServerThread.start();
				serviceEngine.startTickDown(Thread.currentThread().getThreadGroup(),smtpServerThread,Tickable.TICKID_EMAIL,CMProps.getTickMillis(),(int)CMProps.getTicksPerMinute() * 5);
			}

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading base classes");
			if(!CMClass.loadAllCoffeeMudClasses(page))
			{
				fatalStartupError(t,0);
				return false;
			}
			CMLib.lang().setLocale(CMLib.props().getStr("LANGUAGE"),CMLib.props().getStr("COUNTRY"));
			if((threadCode==MudHost.MAIN_HOST)||(CMLib.time()!=CMLib.library(MudHost.MAIN_HOST, CMLib.Library.TIME)))
				CMLib.time().globalClock().initializeINIClock(page);
			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("FACTIONS")))
				CMLib.factions().reloadFactions(CMProps.getVar(CMProps.Str.PREFACTIONS));
			
			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("CHANNELS"))||(checkPrivate&&CMProps.isPrivateToMe("JOURNALS")))
			{
				int numChannelsLoaded=0;
				int numJournalsLoaded=0;
				if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("CHANNELS")))
					numChannelsLoaded=CMLib.channels().loadChannels(page.getStr("CHANNELS"),
																	page.getStr("ICHANNELS"),
																	page.getStr("IMC2CHANNELS"));
				if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("JOURNALS")))
				{
					numJournalsLoaded=CMLib.journals().loadCommandJournals(page.getStr("COMMANDJOURNALS"));
					numJournalsLoaded+=CMLib.journals().loadForumJournals(page.getStr("FORUMJOURNALS"));
				}
				Log.sysOut(Thread.currentThread().getName(),"Channels loaded   : "+(numChannelsLoaded+numJournalsLoaded));
			}

			if((tCode==MAIN_HOST)||(page.getRawPrivateStr("SYSOPMASK")!=null)) // needs to be after journals, for journal flags
			{
				CMSecurity.setSysOp(page.getStr("SYSOPMASK")); // requires all classes be loaded
				CMSecurity.parseGroups(page);
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("SOCIALS")))
			{
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading socials");
				CMLib.socials().unloadSocials();
				if(CMLib.socials().numSocialSets()==0)
					Log.errOut(Thread.currentThread().getName(),"WARNING: Unable to load socials from socials.txt!");
				else
					Log.sysOut(Thread.currentThread().getName(),"Socials loaded    : "+CMLib.socials().numSocialSets());
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("CLANS")))
			{
				CMLib.database().DBReadAllClans();
				Log.sysOut(Thread.currentThread().getName(),"Clans loaded      : "+CMLib.clans().numClans());
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("FACTIONS")))
				serviceEngine.startTickDown(Thread.currentThread().getThreadGroup(),CMLib.factions(),Tickable.TICKID_MOB,CMProps.getTickMillis(),10);

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Starting CM1");
			startCM1();
			
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Starting I3");
			startIntermud3();

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Starting IMC2");
			startIntermud2();
			
			try{Thread.sleep(500);}catch(Exception e){}
			
			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("CATALOG")))
			{
				Log.sysOut(Thread.currentThread().getName(),"Loading catalog...");
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading catalog....");
				CMLib.database().DBReadCatalogs();
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("MAP")))
			{
				Log.sysOut(Thread.currentThread().getName(),"Loading map...");
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading rooms....");
				CMLib.database().DBReadAllRooms(null);
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: preparing map....");
				Log.sysOut(Thread.currentThread().getName(),"Preparing map...");
				CMLib.database().DBReadArtifacts();
				for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					Area A=a.nextElement();
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: filling map ("+A.Name()+")");
					A.fillInAreaRooms();
				}
				Log.sysOut(Thread.currentThread().getName(),"Mapped rooms      : "+CMLib.map().numRooms()+" in "+CMLib.map().numAreas()+" areas");
		
				if(!CMLib.map().roomIDs().hasMoreElements())
				{
					Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
					String id="START";//New Area#0";
					Area newArea=CMClass.getAreaType("StdArea");
					newArea.setName("New Area");
					CMLib.map().addArea(newArea);
					CMLib.database().DBCreateArea(newArea);
					Room room=CMClass.getLocale("StdRoom");
					room.setRoomID(id);
					room.setArea(newArea);
					room.setDisplayText("New Room");
					room.setDescription("Brand new database room! You need to change this text with the MODIFY ROOM command.  If your character is not an Archon, pick up the book you see here and read it immediately!");
					CMLib.database().DBCreateRoom(room);
					Item I=CMClass.getMiscMagic("ManualArchon");
					room.addItem(I);
					CMLib.database().DBUpdateItems(room);
				}
				
				CMLib.login().initStartRooms(page);
				CMLib.login().initDeathRooms(page);
				CMLib.login().initBodyRooms(page);
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe("QUESTS")))
			{
				CMLib.database().DBReadQuests(CMLib.mud(0));
				if(CMLib.quests().numQuests()>0)
					Log.sysOut(Thread.currentThread().getName(),"Quests loaded     : "+CMLib.quests().numQuests());
			}
			
			if(tCode!=MAIN_HOST)
			{
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Waiting for HOST0");
				while((!MUD.bringDown)
				&&(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
				&&(!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
					try{Thread.sleep(500);}catch(Exception e){ break;}
				if((MUD.bringDown)
				||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
				||(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
					return false;
			}
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: readying for connections.");
			try
			{
				CMLib.activateLibraries();
				Log.sysOut(Thread.currentThread().getName(),"Services and utilities started");
			}
			catch (Throwable th)
			{
				Log.errOut(Thread.currentThread().getName(),"CoffeeMud Server initHost() failed");
				Log.errOut(Thread.currentThread().getName(),th);
				fatalStartupError(t,4);
				return false;
			}

			
			StringBuffer str=new StringBuffer("");
			for(int m=0;m<CMLib.hosts().size();m++)
			{
				MudHost mud=CMLib.hosts().get(m);
				str.append(" "+mud.getPort());
			}
			CMProps.setVar(CMProps.Str.MUDPORTS,str.toString());
			CMProps.setBoolAllVar(CMProps.Bool.MUDSTARTED,true);
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"OK");
			Log.sysOut(Thread.currentThread().getName(),"Host#"+threadCode+" initializated.");
			return true;
		}

		public void run()
		{
			CMLib.initialize(); // initialize the lib
			CMClass.initialize(); // initialize the classes
			Log.shareWith(MudHost.MAIN_HOST);
			Resources.shareWith(MudHost.MAIN_HOST);
			
			// wait for ini to be loaded, and for other matters
			if(threadCode!=MAIN_HOST) {
				while((CMLib.library(MAIN_HOST,CMLib.Library.INTERMUD)==null)&&(!MUD.bringDown)) {
					try {Thread.sleep(500);}catch(Exception e){ break;}
				}
				if(MUD.bringDown)
					return;
			}
			CMProps page=CMProps.loadPropPage("//"+iniFile);
			if ((page==null)||(!page.isLoaded()))
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
				System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"A terminal error has occured!");
				return;
			}
			page.resetSystemVars();
			CMProps.setBoolAllVar(CMProps.Bool.MUDSTARTED,false);
			serviceEngine.activate();
			
			if(threadCode!=MAIN_HOST)
			{
				if(CMath.isInteger(page.getPrivateStr("NUMLOGS")))
				{
					Log.newInstance();
					Log.instance().configureLogFile(logName,page.getInt("NUMLOGS"));
					Log.instance().configureLog(Log.Type.info, page.getStr("SYSMSGS"));
					Log.instance().configureLog(Log.Type.error, page.getStr("ERRMSGS"));
					Log.instance().configureLog(Log.Type.warning, page.getStr("WRNMSGS"));
					Log.instance().configureLog(Log.Type.debug, page.getStr("DBGMSGS"));
					Log.instance().configureLog(Log.Type.help, page.getStr("HLPMSGS"));
					Log.instance().configureLog(Log.Type.kills, page.getStr("KILMSGS"));
					Log.instance().configureLog(Log.Type.combat, page.getStr("CBTMSGS"));
					Log.instance().configureLog(Log.Type.access, page.getStr("ACCMSGS"));
				}
				if(page.getRawPrivateStr("SYSOPMASK")!=null)
					page.resetSecurityVars();
				else
					CMSecurity.instance().markShared();
			}
			
			if(page.getStr("DISABLE").trim().length()>0)
				Log.sysOut(Thread.currentThread().getName(),"Disabled subsystems: "+page.getStr("DISABLE"));
			if(page.getStr("DEBUG").trim().length()>0)
			{
				Log.sysOut(Thread.currentThread().getName(),"Debugging messages: "+page.getStr("DEBUG"));
				if(!Log.debugChannelOn())
					Log.errOut(Thread.currentThread().getName(),"Debug logging is disabled! Check your DBGMSGS flag!");
			}
			
			DBConnector currentDBconnector=new DBConnector();
			CMLib.registerLibrary(new DBInterface(currentDBconnector,CMProps.getPrivateSubSet("DB.*")));
			CMProps.setVar(CMProps.Str.MUDVER,HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
			
			// an arbitrary dividing line. After threadCode 0 
			if(threadCode==MAIN_HOST) {
				CMLib.registerLibrary(serviceEngine);
				CMLib.registerLibrary(new IMudClient());
			} else {
				CMLib.registerLibrary(CMLib.library(MAIN_HOST,CMLib.Library.THREADS));
				CMLib.registerLibrary(CMLib.library(MAIN_HOST,CMLib.Library.INTERMUD));
			}
			CMProps.setVar(CMProps.Str.INIPATH,iniFile,false);
			CMProps.setUpLowVar(CMProps.Str.MUDNAME,name.replace('\'','`'));
			try
			{
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting");
				CMProps.setVar(CMProps.Str.MUDBINDADDRESS,page.getStr("BIND"));
				CMProps.setIntVar(CMProps.Int.MUDBACKLOG,page.getInt("BACKLOG"));

				LinkedList<MUD> hostMuds=new LinkedList<MUD>();
				String ports=page.getProperty("PORT");
				int pdex=ports.indexOf(',');
				while(pdex>0)
				{
					MUD mud=new MUD("MUD@"+ports.substring(0,pdex));
					mud.setState(MudState.STARTING);
					mud.acceptConns=false;
					mud.port=CMath.s_int(ports.substring(0,pdex));
					ports=ports.substring(pdex+1);
					hostMuds.add(mud);
					mud.start();
					pdex=ports.indexOf(',');
				}
				MUD mud=new MUD("MUD@"+ports);
				mud.setState(MudState.STARTING);
				mud.acceptConns=false;
				mud.port=CMath.s_int(ports);
				hostMuds.add(mud);
				mud.start();
				

				if(hostMuds.size()==0)
				{
					Log.errOut("HOST#"+this.threadCode+" could not start any listeners.");
					return;
				}
				
				boolean oneStarted=false;
				long timeout=System.currentTimeMillis()+60000;
				while((!oneStarted) && (System.currentTimeMillis()<timeout))
				{
					int numStopped=0;
					for(MUD m : hostMuds)
						if(m.state==MudState.STOPPED)
							numStopped++;
						else
						if(m.state!=MudState.STARTING)
							oneStarted=true;
					if(numStopped==hostMuds.size())
					{
						Log.errOut("HOST#"+this.threadCode+" could not start any listeners.");
						failedStart=true;
						return;
					}
					try { Thread.sleep(100); }catch(Exception e){}
				}
				if(!oneStarted)
				{
					Log.errOut("HOST#"+this.threadCode+" could not start any listeners.");
					failedStart=true;
					return;
				}

				if(initHost())
				{
					Thread joinable=null;
					for(int i=0;i<CMLib.hosts().size();i++)
						if(CMLib.hosts().get(i) instanceof Thread)
						{
							joinable=(Thread)CMLib.hosts().get(i);
							break;
						}
					if(joinable!=null)
					{
						hostStarted=true;
						joinable.join();
					}
					else
						failedStart=true;
				}
				else
				{
					failedStart=true;
				}
			}
			catch(InterruptedException e)
			{
				Log.errOut(Thread.currentThread().getName(),e);
			}
		}
	}

	public List<Runnable> getOverdueThreads()
	{
		Vector<Runnable> V=new Vector<Runnable>();
		for(int w=0;w<webServers.size();w++)
			V.addAll(webServers.get(w).getOverdueThreads());
		return V;
	}

	public static void main(String a[])
	{
		String nameID="";
		Thread.currentThread().setName("MUD");
		Vector<String> iniFiles=new Vector<String>();
		if(a.length>0)
		{
			for(int i=0;i<a.length;i++)
				nameID+=" "+a[i];
			nameID=nameID.trim();
			List<String> V=CMParms.paramParse(nameID);
			for(int v=0;v<V.size();v++)
			{
				String s=V.get(v);
				if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
				{
					iniFiles.addElement(s.substring(5));
					V.remove(v);
					v--;
				}
			}
			nameID=CMParms.combine(V,0);
		}
		CMLib.initialize(); // initialize this threads libs
		
		if(iniFiles.size()==0) iniFiles.addElement("coffeemud.ini");
		if((nameID.length()==0)||(nameID.equalsIgnoreCase( "CoffeeMud" ))||nameID.equalsIgnoreCase("Your Muds Name"))
		{
			nameID="Unnamed_CoffeeMUD#";
			long idNumber=new Random(System.currentTimeMillis()).nextLong();
			try
			{
				idNumber=0;
				for(Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();e.hasMoreElements();)
				{
					NetworkInterface n=e.nextElement();
					idNumber^=n.getDisplayName().hashCode();
					try
					{
						Method m=n.getClass().getMethod("getHardwareAddress");
						Object o=m.invoke(n);
						if(o instanceof byte[])
						{
							for(int i=0;i<((byte[])o).length;i++)
								idNumber^=((byte[])o)[0] << (i*8);
						}
					}catch(Exception e1){}
				}
			}catch(Exception e1){}
			if(idNumber<0) idNumber=idNumber*-1;
			nameID=nameID+idNumber;
			System.err.println("*** Please give your mud a unique name in mud.bat or mudUNIX.sh!! ***");
		}
		else
		if(nameID.equalsIgnoreCase( "TheRealCoffeeMudCopyright2000-2014ByBoZimmerman" ))
			nameID="CoffeeMud";
		String iniFile=iniFiles.firstElement();
		CMProps page=CMProps.loadPropPage("//"+iniFile);
		if ((page==null)||(!page.isLoaded()))
		{
			Log.instance().configureLogFile("mud",1);
			Log.instance().configureLog(Log.Type.info, "BOTH");
			Log.instance().configureLog(Log.Type.error, "BOTH");
			Log.instance().configureLog(Log.Type.warning, "BOTH");
			Log.instance().configureLog(Log.Type.debug, "BOTH");
			Log.instance().configureLog(Log.Type.help, "BOTH");
			Log.instance().configureLog(Log.Type.kills, "BOTH");
			Log.instance().configureLog(Log.Type.combat, "BOTH");
			Log.instance().configureLog(Log.Type.access, "BOTH");
			Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
			System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"A terminal error has occured!");
			System.exit(-1);
			return;
		}
		Log.shareWith(MudHost.MAIN_HOST);
		Log.instance().configureLogFile("mud",page.getInt("NUMLOGS"));
		Log.instance().configureLog(Log.Type.info, page.getStr("SYSMSGS"));
		Log.instance().configureLog(Log.Type.error, page.getStr("ERRMSGS"));
		Log.instance().configureLog(Log.Type.warning, page.getStr("WRNMSGS"));
		Log.instance().configureLog(Log.Type.debug, page.getStr("DBGMSGS"));
		Log.instance().configureLog(Log.Type.help, page.getStr("HLPMSGS"));
		Log.instance().configureLog(Log.Type.kills, page.getStr("KILMSGS"));
		Log.instance().configureLog(Log.Type.combat, page.getStr("CBTMSGS"));
		Log.instance().configureLog(Log.Type.access, page.getStr("ACCMSGS"));

		
		Thread shutdownHook=new Thread("ShutdownHook") {
			public void run() {
				if(!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
					MUD.globalShutdown(null,true,null);
			}
		};

		while(!bringDown)
		{
			System.out.println();
			grpid=0;
			Log.sysOut(Thread.currentThread().getName(),"CoffeeMud v"+HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
			Log.sysOut(Thread.currentThread().getName(),"(C) 2000-2014 Bo Zimmerman");
			Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
			CMLib.hosts().clear();
			final LinkedList<HostGroup> myGroups=new LinkedList<HostGroup>();
			HostGroup mainGroup=null;
			for(int i=0;i<iniFiles.size();i++)
			{
				iniFile=iniFiles.elementAt(i);
				ThreadGroup G=new ThreadGroup(i+"-MUD");
				HostGroup H=new HostGroup(G,nameID,iniFile);
				if(mainGroup==null)
					mainGroup=H;
				myGroups.add(H);
				H.start();
			}
			if(mainGroup==null)
			{
				Log.errOut("CoffeeMud failed to start.");
				MUD.bringDown=true;
				CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN, true);
			}
			else
			{
				long timeout=System.currentTimeMillis()+1800000; /// 30 mins
				int numPending=1;
				while((numPending>0)&&(System.currentTimeMillis()<timeout))
				{
					numPending=0;
					for(HostGroup g : myGroups)
						if(!g.failedToStart() && !g.isStarted())
							numPending++;
					if(mainGroup.failedToStart())
						break;
					try { Thread.sleep(100); } catch(Exception e) {}
				}
				if(mainGroup.failedToStart())
				{
					Log.errOut("CoffeeMud failed to start.");
					MUD.bringDown=true;
					CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN, true);
				}
				else
				{
					Runtime.getRuntime().addShutdownHook(shutdownHook);
					for(int i=0;i<CMLib.hosts().size();i++)
						CMLib.hosts().get(i).setAcceptConnections(true);
					Log.sysOut(Thread.currentThread().getName(),"Initialization complete.");
					try{mainGroup.join();}catch(Exception e){e.printStackTrace(); Log.errOut(Thread.currentThread().getName(),e); }
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
			}
			
			System.gc();
			try{Thread.sleep(1000);}catch(Exception e){}
			System.runFinalization();
			try{Thread.sleep(1000);}catch(Exception e){}

			if(activeThreadCount(Thread.currentThread().getThreadGroup(),true)>1)
			{
				try{ Thread.sleep(1000);}catch(Exception e){}
				killCount(Thread.currentThread().getThreadGroup(),true);
				try{ Thread.sleep(1000);}catch(Exception e){}
				if(activeThreadCount(Thread.currentThread().getThreadGroup(),true)>1)
				{
					Log.sysOut(Thread.currentThread().getName(),"WARNING: " 
						+ activeThreadCount(Thread.currentThread().getThreadGroup(),true) 
						+" other thread(s) are still active!");
					threadList(Thread.currentThread().getThreadGroup(),true);
				}
			}
			if(!bringDown)
			{
				if(execExternalCommand!=null)
				{
					//Runtime r=Runtime.getRuntime();
					//Process p=r.exec(external);
					Log.sysOut("Attempted to execute '"+execExternalCommand+"'.");
					execExternalCommand=null;
					bringDown=true;
				}
			}
		}
	}

	public void setAcceptConnections(boolean truefalse){ acceptConns=truefalse;}
	public boolean isAcceptingConnections(){ return acceptConns;}
	public long getUptimeSecs() { return (System.currentTimeMillis()-startupTime)/1000;}

	public String executeCommand(String cmd)
		throws Exception
	{
		Vector<String> V=CMParms.parse(cmd);
		if(V.size()==0) throw new CMException("Unknown command!");
		String word=V.firstElement();
		if(word.equalsIgnoreCase("START")&&(V.size()>1))
		{
			String what=V.elementAt(1);
			if(what.equalsIgnoreCase("I3"))
			{
				startIntermud3();
				return "Done";
			}
			else
			if(what.equalsIgnoreCase("IMC2"))
			{
				startIntermud2();
				return "Done";
			}
		}
		throw new CMException("Unknown command: "+word);
	}
}
