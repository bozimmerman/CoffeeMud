package com.planet_ink.coffee_mud.application;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.CMSecurity.ConnectState;
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
import com.planet_ink.coffee_mud.core.intermud.i3.CoffeeMudI3Bridge;
import com.planet_ink.coffee_mud.core.intermud.imc2.IMC2Driver;
import com.planet_ink.coffee_mud.core.intermud.i3.server.I3Server;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_web.util.CWThread;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter; // for writing to sockets
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.net.*;
import java.util.*;
import java.sql.*;

/*
   Copyright 2000-2024 Bo Zimmerman

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
	private static final String	  HOST_VERSION	= "5.10.4.0";

	private static enum MudState
	{
		STARTING,
		WAITING,
		ACCEPTING,
		STOPPED
	}

	private volatile MudState state		 = MudState.STOPPED;
	private ServerSocket	  servsock	 = null;
	private boolean			  acceptConns= false;
	private int				  port		 = 5555;
	private final long		  startupTime= System.currentTimeMillis();
	private final ThreadGroup threadGroup;

	private static volatile int	 		grpid				= 0;
	private static boolean				bringDown			= false;
	private static String				execExternalCommand	= null;
	private static I3Server				i3server			= null;
	private static IMC2Driver			imc2server			= null;
	private static List<WebServer>		webServers			= new Vector<WebServer>();
	private static SMTPserver			smtpServerThread	= null;
	private static List<DBConnector>	databases			= new Vector<DBConnector>();
	private static List<CM1Server>		cm1Servers			= new Vector<CM1Server>();
	private static final ServiceEngine	serviceEngine		= new ServiceEngine();
	private static AtomicBoolean 		bootSync			= new AtomicBoolean(false);
	private static Map<String,String>	clArgs				= new Hashtable<String,String>();

	private static final long 			SHUTDOWN_TIMEOUT 	= 5 * 60 * 1000;

	public MUD(final String name)
	{
		super(name);
		threadGroup=Thread.currentThread().getThreadGroup();
	}

	@Override
	public void acceptConnection(final Socket sock) throws SocketException, IOException
	{
		sock.setKeepAlive(true);
		setState(MudState.ACCEPTING);
		final ConnectionAcceptor acceptor = new ConnectionAcceptor(sock, Thread.currentThread().getName());
		serviceEngine.executeRunnable(threadGroup.getName(),acceptor);
	}

	@Override
	public ThreadGroup threadGroup()
	{
		return threadGroup;
	}

	private static boolean checkedSleep(final long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(final Exception e)
		{
			return false;
		}
		return true;
	}

	private class ConnectionAcceptor implements CMRunnable
	{
		Socket sock;
		long startTime=0;
		String name = null;

		public ConnectionAcceptor(final Socket sock, final String name) throws SocketException, IOException
		{
			this.sock=sock;
			this.name = name;
			sock.setSoLinger(true,3);
		}

		@Override
		public long getStartTime()
		{
			return startTime;
		}

		protected String name()
		{
			if(this.name == null)
				return Thread.currentThread().getName();
			return this.name;
		}

		@Override
		public int getGroupID()
		{
			return Thread.currentThread().getThreadGroup().getName().charAt(0);
		}

		@Override
		public void run()
		{
			startTime=System.currentTimeMillis();
			try
			{
				if (acceptConns)
				{
					final String address = CMSecurity.getSocketAddress(sock);
					final int[] numAtAddress = new int[] {0};
					final ConnectState proceed = CMSecurity.getConnectState(sock, numAtAddress);
					if(proceed != ConnectState.NORMAL)
					{
						final int abusiveCount=numAtAddress[0]-(int)CMSecurity.CONN_MAX_PER_ADDR+1;
						final long rounder=Math.round(Math.sqrt(abusiveCount));
						if(abusiveCount == (rounder*rounder))
							Log.sysOut(name(),"Blocking a connection from "+address +" ("+numAtAddress[0]+")");
						try
						{
							final PrintWriter out = new PrintWriter(sock.getOutputStream());
							StringBuffer introText;
							if(proceed==ConnectState.BLOCKED)
							{
								introText=new StringBuffer(Resources.getFileResource(Resources.makeFileResourceName("text/connblocked.txt"),true));
								introText=CMStrings.replaceAll(introText, "@mins@", ""+(CMSecurity.CONN_LAST_DELAY_MS/60000L));
							}
							else
							{
								introText=Resources.getFileResource(Resources.makeFileResourceName("text/blocked.txt"),true);
							}
							try
							{
								introText = CMLib.webMacroFilter().virtualPageFilter(introText);
							}
							catch (final Exception ex)
							{
							}
							out.print(introText.toString());
							out.flush();
							checkedSleep(250);
							out.close();
						}
						catch(final IOException e)
						{
							// dont say anything, just eat it.
						}
						sock = null;
					}
					else
					{
						Log.sysOut(name(),"Connection from "+address);
						// also the intro page
						final CMFile introDir=new CMFile(Resources.makeFileResourceName("text"),null,CMFile.FLAG_FORCEALLOW);
						String introFilename="text/intro.txt";
						if(introDir.isDirectory())
						{
							final CMFile[] files=introDir.listFiles();
							final List<String> choices=new ArrayList<String>();
							for (final CMFile file : files)
							{
								if(file.getName().toLowerCase().startsWith("intro")
								&&file.getName().toLowerCase().endsWith(".txt"))
									choices.add("text/"+file.getName());
							}
							if(choices.size()>0)
								introFilename=choices.get(CMLib.dice().roll(1,choices.size(),-1));
						}
						StringBuffer introText=Resources.getFileResource(introFilename,true);
						try
						{
							introText = CMLib.webMacroFilter().virtualPageFilter(introText);
						}
						catch (final Exception ex)
						{
						}
						final Session S=(Session)CMClass.getCommon("DefaultSession");
						S.initializeSession(sock, threadGroup().getName(), introText != null ? introText.toString() : null);
						CMLib.sessions().add(S);
						sock = null;
					}
				}
				else
				if((CMLib.database()!=null)
				&&(CMLib.database().isConnected())
				&&(CMLib.encoder()!=null))
				{
					StringBuffer rejectText;
					try
					{
						rejectText = Resources.getFileResource("text/offline.txt",true);
					}
					catch(final java.lang.NullPointerException npe)
					{
						rejectText=new StringBuffer("");
					}
					try
					{
						final PrintWriter out = new PrintWriter(sock.getOutputStream());
						out.println("\n\rOFFLINE: " + CMProps.getVar(CMProps.Str.MUDSTATUS)+"\n\r");
						out.println(rejectText);
						out.flush();
						checkedSleep(1000);
						out.close();
					}
					catch(final IOException e)
					{
						// dont say anything, just eat it.
					}
					sock = null;
				}
				else
				{
					try
					{
						sock.close();
					}
					catch (final Exception e)
					{
					}
					sock = null;
				}
			}
			finally
			{
				startTime=0;
			}
		}

		@Override
		public long activeTimeMillis()
		{
			return (startTime > 0) ? System.currentTimeMillis() - startTime : 0;
		}
	}

	@Override
	public String getLanguage()
	{
		final String lang = CMProps.instance().getStr("LANGUAGE").toUpperCase().trim();
		if(lang.length()==0)
			return "English";
		for (final Pair<String,String> element : CMProps.getListFileStringPairsList(CMProps.ListFile.ISO_LANG_CODES))
		{
			if(lang.equals(element.first))
				return element.second;
		}
		return "English";
	}

	public void setState(final MudState st)
	{
		if(st!=state)
			state=st;
	}

	@Override
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
			catch (final UnknownHostException e)
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
			String oldPorts = CMProps.getVar(CMProps.Str.LOCALMUDPORTS);
			if(oldPorts == null)
				oldPorts = "";
			CMProps.setVar(CMProps.Str.LOCALMUDPORTS,(oldPorts+" "+this.getPort()).trim());
			while(servsock!=null)
			{
				setState(MudState.WAITING);
				sock=servsock.accept();
				acceptConnection(sock);
			}
		}
		catch(final Exception t)
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
		catch(final IOException e)
		{
		}

		Log.sysOut(Thread.currentThread().getName(),"MUD on port "+port+" stopped!");
		setState(MudState.STOPPED);
		CMLib.hosts().remove(this);
	}

	@Override
	public String getStatus()
	{
		if(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		return state.toString();
	}

	@Override
	public void shutdown(final Session S, final boolean keepItDown, final String externalCommand)
	{
		globalShutdown(S,keepItDown,externalCommand);
		interrupt(); // kill the damn archon thread.
	}

	public static void defaultShutdown()
	{
		globalShutdown(null,true,null);
	}

	private static void shutdownMemReport(final String blockName)
	{
		try
		{
			Object obj = new Object();
			final WeakReference<Object> ref = new WeakReference<Object>(obj);
			obj = null;
			System.gc();
			System.runFinalization();
			while(ref.get() != null)
			{
				System.gc();
			}
			checkedSleep(3000);
		}
		catch (final Exception e)
		{
		}
		final long free=Runtime.getRuntime().freeMemory()/1024;
		final long total=Runtime.getRuntime().totalMemory()/1024;
		Log.debugOut("Memory: "+blockName+": "+(total-free)+"/"+total);
	}

	protected static void shutdownLibrarySet(final Library lib, final Session S, final AtomicLong shutdownStateTime, final boolean debugMem)
	{
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down "+CMStrings.capitalizeAndLower(lib.name())+"...");
		for(final Enumeration<CMLibrary> e=CMLib.libraries(lib);e.hasMoreElements();)
		{
			try
			{
				shutdownStateTime.set(System.currentTimeMillis()+(5*SHUTDOWN_TIMEOUT));
				final CMLibrary library=e.nextElement();
				if(S!=null)
					S.print(library.name()+"...");
				library.shutdown();
				if(S!=null)
					S.println("shut down.");
				if(debugMem)
					shutdownMemReport(library.ID());
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
		}
	}

	public static void globalShutdown(final Session S, final boolean keepItDown, final String externalCommand)
	{
		CMProps.setBoolAllVar(CMProps.Bool.MUDSTARTED,false);
		CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN,true);
		bootSync.set(false);
		final AtomicLong shutdownStateTime = new AtomicLong(System.currentTimeMillis());
		final Thread currentShutdownThread=Thread.currentThread();
		final Thread shutdownWatchThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(shutdownStateTime.get()!=0)
				{
					final long ellapsed=System.currentTimeMillis()-shutdownStateTime.get();
					if(ellapsed > SHUTDOWN_TIMEOUT)
					{
						if(externalCommand!=null)
							MUD.execExternalRestart(externalCommand);
						Log.errOut("** Shutdown timeout. **");
						final StringBuilder lines=new StringBuilder("");
						lines.append("\n\r^HThread: ^N"+currentShutdownThread.getName()+"\n\r");
						final java.lang.StackTraceElement[] s=currentShutdownThread.getStackTrace();
						for (final StackTraceElement element : s)
							lines.append("\n   "+element.getClassName()+": "+element.getMethodName()+"("+element.getFileName()+": "+element.getLineNumber()+")");
						Log.errOut(lines.toString());
						break;
					}
					CMLib.s_sleep(10 * 1000);
				}
			}
		});
		shutdownWatchThread.start();

		final boolean debugMem = CMSecurity.isDebugging(CMSecurity.DbgFlag.SHUTDOWN);
		if(debugMem) shutdownMemReport("BaseLine");
		serviceEngine.suspendAll(null);
		if(S!=null)
			S.print(CMLib.lang().L("Closing MUD listeners to new connections..."));
		for(int i=0;i<CMLib.hosts().size();i++)
			CMLib.hosts().get(i).setAcceptConnections(false);
		Log.sysOut(Thread.currentThread().getName(),"New Connections are now closed");
		if(S!=null)
			S.println(CMLib.lang().L("Done."));

		if(!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPLAYERS))
		{
			if(S!=null)
				S.print(CMLib.lang().L("Saving players..."));
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Saving players...");
			for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.SESSIONS);e.hasMoreElements();)
			{
				final SessionsList list=((SessionsList)e.nextElement());
				for(final Session S2 : list.allIterable())
				{
					final MOB M = S2.mob();
					if((M!=null)&&(M.playerStats()!=null))
					{
						shutdownStateTime.set(System.currentTimeMillis());
						M.playerStats().setLastDateTime(System.currentTimeMillis());
						// important! shutdown their affects!
						for(int a=M.numAllEffects()-1;a>=0;a--) // reverse enumeration
						{
							try
							{
								final Ability A=M.fetchEffect(a);
								if((A!=null)&&(A.canBeUninvoked()))
									A.unInvoke();
								if((A!=null)&&(!A.isSavable()))
									M.delEffect(A);
							}
							catch (final Throwable ex)
							{
								Log.errOut(ex);
							}
						}
					}
				}
			}
			try
			{
				for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.PLAYERS);e.hasMoreElements();)
				{
					try
					{
						final PlayerLibrary lib = (PlayerLibrary)e.nextElement();
						shutdownStateTime.set(System.currentTimeMillis()+(lib.numPlayers()*(60 * 1000)));
						lib.savePlayers();
					}
					catch (final Throwable ex)
					{
						Log.errOut(ex);
					}
				}
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			if(S!=null)
				S.println(CMLib.lang().L("done"));
			Log.sysOut(Thread.currentThread().getName(),"All users saved.");
		}
		shutdownStateTime.set(System.currentTimeMillis());
		if(S!=null)
			S.print(CMLib.lang().L("Saving stats..."));
		try
		{
			for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.STATS);e.hasMoreElements();)
				((StatisticsLibrary)e.nextElement()).update();
		}
		catch (final Throwable ex)
		{
			Log.errOut(ex);
		}
		if(S!=null)
			S.println(CMLib.lang().L("done"));
		Log.sysOut(Thread.currentThread().getName(),"Stats saved.");
		if(debugMem)
			shutdownMemReport("Saves");

		shutdownStateTime.set(System.currentTimeMillis());
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut(Thread.currentThread().getName(),"Notifying all objects of shutdown...");
		if(S!=null)
			S.print(CMLib.lang().L("Notifying all objects of shutdown..."));
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Notifying Objects");
		final MOB mob=(S!=null)?S.mob():CMClass.getFactoryMOB();
		final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_SHUTDOWN,null);
		final List<Room> roomSet=new ArrayList<Room>();
		try
		{
			shutdownStateTime.set(System.currentTimeMillis());
			for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.MAP);e.hasMoreElements();)
			{
				final WorldMap map=((WorldMap)e.nextElement());
				for(final Enumeration<Area> a=map.areas();a.hasMoreElements();)
					a.nextElement().setAreaState(Area.State.STOPPED);
			}
			shutdownStateTime.set(System.currentTimeMillis());
			for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.MAP);e.hasMoreElements();)
			{
				final WorldMap map=((WorldMap)e.nextElement());
				for(final Enumeration<Room> r=map.rooms();r.hasMoreElements();)
				{
					// single threadding is important here.
					final Room R=r.nextElement();
					try
					{
						R.send(mob,msg);
					}
					catch (final Throwable ex)
					{
						Log.errOut(ex);
					}
					roomSet.add(R);
					shutdownStateTime.set(System.currentTimeMillis());
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		if(S!=null)
			S.println(CMLib.lang().L("done"));
		if(debugMem) shutdownMemReport("Notifications");
		final CMLib.Library[][] libraryShutdownLists=
		{
			{CMLib.Library.QUEST,CMLib.Library.TECH,CMLib.Library.SESSIONS},
			{CMLib.Library.STATS,CMLib.Library.THREADS},
			{CMLib.Library.SOCIALS,CMLib.Library.CLANS,CMLib.Library.CHANNELS,CMLib.Library.JOURNALS,
				CMLib.Library.POLLS,CMLib.Library.HELP,CMLib.Library.CATALOG,CMLib.Library.MAP,
				CMLib.Library.PLAYERS
			}
		};

		shutdownStateTime.set(System.currentTimeMillis());
		for(final CMLib.Library lib : libraryShutdownLists[0])
			shutdownLibrarySet(lib, S, shutdownStateTime, debugMem);
		if(S!=null)
			S.println(CMLib.lang().L("Save thread stopped")); // from sessions

		if(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMMOBS)
		||CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMITEMS)
		||CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMSHOPS))
		{
			if(S!=null)
				S.print(CMLib.lang().L("Saving room data..."));
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Rejuving the dead");
			serviceEngine.tickAllTickers(null);
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Map Update");
			for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.MAP);e.hasMoreElements();)
			{
				final WorldMap map=((WorldMap)e.nextElement());
				for(final Enumeration<Area> a=map.areas();a.hasMoreElements();)
					a.nextElement().setAreaState(Area.State.STOPPED);
			}
			shutdownStateTime.set(System.currentTimeMillis());
			int roomCounter=0;
			Room R=null;
			for(final Iterator<Room> e=roomSet.iterator();e.hasNext();)
			{
				if(((++roomCounter)%200)==0)
				{
					if(S!=null)
						S.print(".");
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Map Update ("+roomCounter+")");
				}
				R=e.next();
				try
				{
					if(R.roomID().length()>0)
						R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
				}
				catch (final Throwable ex)
				{
					Log.errOut(ex);
				}
				shutdownStateTime.set(System.currentTimeMillis());
			}
			if(S!=null)
				S.println(CMLib.lang().L("done"));
			Log.sysOut(Thread.currentThread().getName(),"Map data saved.");
		}

		shutdownStateTime.set(System.currentTimeMillis());
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...CM1Servers");
		for(final CM1Server cm1server : cm1Servers)
		{
			try
			{
				cm1server.shutdown();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			finally
			{
				if(S!=null)
					S.println(CMLib.lang().L("@x1 stopped",cm1server.getName()));
				if(debugMem) shutdownMemReport("CM1Server");
			}
		}
		cm1Servers.clear();

		shutdownStateTime.set(System.currentTimeMillis());
		if(i3server!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...I3Server");
			try
			{
				I3Server.shutdown();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			i3server=null;
			if(S!=null)
				S.println(CMLib.lang().L("I3Server stopped"));
			Log.sysOut(Thread.currentThread().getName(),"I3Server stopped");
			if(debugMem) shutdownMemReport("I3Server");
		}

		shutdownStateTime.set(System.currentTimeMillis());
		if(imc2server!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...IMC2Server");
			try
			{
				imc2server.shutdown();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			imc2server=null;
			if(S!=null)
				S.println(CMLib.lang().L("IMC2Server stopped"));
			Log.sysOut(Thread.currentThread().getName(),"IMC2Server stopped");
			if(debugMem) shutdownMemReport("IMC2Server");
		}

		shutdownStateTime.set(System.currentTimeMillis());
		if(S!=null)
			S.print(CMLib.lang().L("Stopping player Sessions..."));
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Stopping sessions");
		for(final Enumeration<CMLibrary> e=CMLib.libraries(CMLib.Library.SESSIONS);e.hasMoreElements();)
		{
			final SessionsList list=((SessionsList)e.nextElement());
			for(final Session S2 : list.allIterable())
			{
				if((S!=null)&&(S2==S))
				{
					if(mob!=null)
						mob.removeFromGame(true, false);
					list.remove(S2);
				}
				else
				{
					shutdownStateTime.set(System.currentTimeMillis());
					S2.stopSession(true,true,false);
				}
				if(S!=null)
					S.print(".");
			}
		}
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...Done stopping sessions");
		shutdownStateTime.set(System.currentTimeMillis());
		if(S!=null)
			S.println(CMLib.lang().L("All users logged off"));
		checkedSleep(3000);
		/* give sessions a few seconds to inform the map */
		Log.sysOut(Thread.currentThread().getName(),"All users logged off.");
		if(debugMem) shutdownMemReport("Sessions");

		if(smtpServerThread!=null)
		{
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...smtp server");
			smtpServerThread.shutdown();
			smtpServerThread = null;
			Log.sysOut(Thread.currentThread().getName(),"SMTP Server stopped.");
			if(S!=null)
				S.println(CMLib.lang().L("SMTP Server stopped"));
			if(debugMem) shutdownMemReport("SMTP Server");
		}

		shutdownStateTime.set(System.currentTimeMillis());
		for(final CMLib.Library lib : libraryShutdownLists[1])
			shutdownLibrarySet(lib, S, shutdownStateTime, debugMem);

		if(debugMem) shutdownMemReport("Map Threads");
		Log.sysOut(Thread.currentThread().getName(),"Map Threads Stopped.");

		shutdownStateTime.set(System.currentTimeMillis());
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down services...");
		for(final CMLib.Library lib : libraryShutdownLists[2])
			shutdownLibrarySet(lib, S, shutdownStateTime, debugMem);
		shutdownStateTime.set(System.currentTimeMillis());
		for(final CMLib.Library lib : CMLib.Library.values())
		{
			boolean found=false;
			for(final CMLib.Library[] prevSet : libraryShutdownLists)
				found=found||CMParms.contains(prevSet, lib);
			if(!found)
				shutdownLibrarySet(lib, S, shutdownStateTime, debugMem);
		}
		shutdownStateTime.set(System.currentTimeMillis());
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...unloading resources");
		Resources.shutdown();

		Log.sysOut(Thread.currentThread().getName(),"Resources Cleared.");
		if(S!=null)
			S.println(CMLib.lang().L("All resources unloaded"));
		if(debugMem) shutdownMemReport("Resources");

		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...closing db connections");
		for(int d=0;d<databases.size();d++)
			databases.get(d).killConnections();
		if(S!=null)
			S.println(CMLib.lang().L("Database connections closed"));
		Log.sysOut(Thread.currentThread().getName(),"Database connections closed.");
		if(debugMem) shutdownMemReport("Database Connections");

		shutdownStateTime.set(System.currentTimeMillis());
		for(int i=0;i<webServers.size();i++)
		{
			final WebServer webServerThread=webServers.get(i);
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down web server "+webServerThread.getName()+"...");
			try
			{
				shutdownStateTime.set(System.currentTimeMillis());
				webServerThread.close();
			}
			catch (final Throwable ex)
			{
				Log.errOut(ex);
			}
			Log.sysOut(Thread.currentThread().getName(),"Web server "+webServerThread.getName()+" stopped.");
			if(S!=null)
				S.println(CMLib.lang().L("Web server @x1 stopped",webServerThread.getName()));
			if(debugMem) shutdownMemReport("Web Server "+webServerThread.getName());
		}
		webServers.clear();

		shutdownStateTime.set(System.currentTimeMillis());
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...unloading macros");
		CMLib.lang().clear();
		if(debugMem) shutdownMemReport("Macros");
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...unloading classes");
		try
		{
			CMClass.shutdown();
		}
		catch (final Throwable ex)
		{
			Log.errOut(ex);
		}
		if(debugMem) shutdownMemReport("Java Classes");
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));

		checkedSleep(500);
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud shutdown complete.");
		if(S!=null)
			S.println(CMLib.lang().L("CoffeeMud shutdown complete."));
		bringDown=keepItDown;
		serviceEngine.resumeAll();
		if(!keepItDown)
		{
			if(S!=null)
				S.println(CMLib.lang().L("Restarting..."));
		}
		shutdownStateTime.set(0);
		shutdownWatchThread.interrupt();
		checkedSleep(500);
		System.gc();
		System.runFinalization();
		checkedSleep(500);
		if(debugMem) shutdownMemReport("Complete");

		execExternalCommand=externalCommand;
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutdown: you are the special lucky chosen one!");
		for(int m=CMLib.hosts().size()-1;m>=0;m--)
		{
			if(CMLib.hosts().get(m) instanceof Thread)
			{
				try
				{
					CMLib.killThread((Thread)CMLib.hosts().get(m),100,30);
				}
				catch (final Throwable t)
				{
				}
			}
		}
		shutdownStateTime.set(0);
		CMLib.hosts().clear();
		CMSecurity.unloadAll();
		if(!keepItDown)
			CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN,false);
		Log.debugOut("Final Used memory = "+(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	}

	private static boolean startWebServer(final CMProps page, final String serverName)
	{
		try
		{
			final StringBuffer commonProps=new CMFile("web/common.ini", null, CMFile.FLAG_LOGERRORS).text();
			final StringBuffer finalProps=new CMFile("web/"+serverName+".ini", null, CMFile.FLAG_LOGERRORS).text();
			commonProps.append("\n").append(finalProps.toString());
			final CWConfig config=new CWConfig();
			config.setFileManager(new CMFile.CMFileManager());
			WebServer.initConfig(config, Log.instance(), new ByteArrayInputStream(commonProps.toString().getBytes()));
			if(CMSecurity.isDebugging(DbgFlag.HTTPREQ))
				config.setDebugFlag(page.getStr("DBGMSGS"));
			if(CMSecurity.isDebugging(DbgFlag.HTTPACCESS))
				config.setAccessLogFlag(page.getStr("ACCMSGS"));
			final WebServer webServer=new WebServer(serverName+Thread.currentThread().getThreadGroup().getName().charAt(0),config);
			config.setCoffeeWebServer(webServer);
			webServer.start();
			webServers.add(webServer);
			return true;
		}
		catch(final Exception e)
		{
			Log.errOut("HTTP server "+serverName+" NOT started: "+e.getMessage());
			return false;
		}
	}

	private static boolean stopWebServer(final String serverName)
	{
		try
		{
			final String oldStatus = CMProps.getVar(CMProps.Str.MUDSTATUS);
			for(int i=0;i<webServers.size();i++)
			{
				final WebServer webServerThread=webServers.get(i);
				if((webServerThread.getName().length() < 1)||(webServerThread.getName().indexOf('-')<0))
					continue;
				final String name = webServerThread.getName().substring(webServerThread.getName().indexOf('-')+1,webServerThread.getName().length()-1);
				if(name.equals(serverName))
				{
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down web server "+webServerThread.getName()+"...");
					webServerThread.close();
					Log.sysOut(Thread.currentThread().getName(),"Web server "+webServerThread.getName()+" stopped.");
					webServers.remove(webServerThread);
					CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,oldStatus);
					return true;
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("HTTP server "+serverName+" NOT stopped: "+e.getMessage());
		}
		return false;
	}

	private static void startIntermud3(final int smtpPort)
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
		try
		{
			if(page.getBoolean("RUNI3SERVER")
			&&(tCode==MAIN_HOST)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.I3)))
			{
				if(i3server!=null)
					I3Server.shutdown();
				i3server=null;
				String playstate=page.getStr("MUDSTATE");
				if((playstate==null) || (playstate.length()==0))
					playstate=page.getStr("I3STATE");
				if((playstate==null) || (!CMath.isInteger(playstate)))
					playstate="Development";
				else
				switch(CMath.s_int(playstate.trim()))
				{
				case 0:
					playstate = "MudLib Development";
					break;
				case 1:
					playstate = "Restricted Access";
					break;
				case 2:
					playstate = "Beta Testing";
					break;
				case 3:
					playstate = "Open to the public";
					break;
				default:
					playstate = "MudLib Development";
					break;
				}
				final CoffeeMudI3Bridge imud=new CoffeeMudI3Bridge(CMProps.getVar(CMProps.Str.MUDNAME),
														 "CoffeeMud v"+CMProps.getVar(CMProps.Str.MUDVER),
														 CMLib.mud(0).getPort(),
														 playstate,
														 CMLib.channels().getI3ChannelsList());
				i3server=new I3Server();
				int i3port=page.getInt("I3PORT");
				if(i3port==0)
					i3port=27766;
				final String routersList = page.getStr("I3ROUTERS");
				final List<String> routersSepV = CMParms.parseCommas(routersList, true);
				if(routersSepV.size()>0)
				{
					final String mudName = CMProps.getVar(CMProps.Str.MUDNAME);
					final String adminEmail = CMProps.getVar(CMProps.Str.ADMINEMAIL);
					final String[] routersArray = routersSepV.toArray(new String[0]);
					I3Server.start(mudName,i3port,imud,routersArray,adminEmail,smtpPort);
				}
			}
		}
		catch(final Exception e)
		{
			if(i3server!=null)
				I3Server.shutdown();
			i3server=null;
		}
	}

	private static boolean stopCM1()
	{
		try
		{
			for(final CM1Server s : cm1Servers)
			{
				s.shutdown();
				cm1Servers.remove(s);
			}
			return true;
		}
		catch(final Exception e)
		{
			return false;
		}
	}

	private static boolean startCM1()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
		CM1Server cm1server = null;
		try
		{
			final String runcm1=page.getPrivateStr("RUNCM1SERVER");
			if((runcm1!=null)&&(runcm1.equalsIgnoreCase("TRUE")))
			{
				final String iniFile = page.getStr("CM1CONFIG");
				for(final CM1Server s : cm1Servers)
				{
					if(s.getINIFilename().equalsIgnoreCase(iniFile))
					{
						s.shutdown();
						cm1Servers.remove(s);
					}
				}
				cm1server=new CM1Server("CM1Server"+tCode,iniFile);
				cm1server.start();
				cm1Servers.add(cm1server);
			}
			return true;
		}
		catch(final Exception e)
		{
			if(cm1server!=null)
			{
				cm1server.shutdown();
				cm1Servers.remove(cm1server);
			}
			return false;
		}
	}

	private static void startIntermud2()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMProps page=CMProps.instance();
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
		catch(final Exception e)
		{
			Log.errOut(e);
		}
	}

	@Override
	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				servsock = null;
			}
			catch(final IOException e)
			{
			}
		}
		super.interrupt();
	}

	public static int activeThreadCount(final ThreadGroup tGroup, final boolean nonDaemonsOnly)
	{
		int realAC=0;
		final int ac = tGroup.activeCount();
		final Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if((tArray[i] != null)
			&& tArray[i].isAlive()
			&&(tArray[i] != Thread.currentThread())
			&&((!nonDaemonsOnly)||(!tArray[i].isDaemon())))
				realAC++;
		}
		return realAC;
	}

	private static int killCount(final ThreadGroup tGroup, final boolean nonDaemonsOnly)
	{
		int killed=0;

		final int ac = tGroup.activeCount();
		final Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if ((tArray[i] != null)
			&& tArray[i].isAlive()
			&&(tArray[i] != Thread.currentThread())
			&&((!nonDaemonsOnly)||(!tArray[i].isDaemon())))
			{
				CMLib.killThread(tArray[i],500,10);
				killed++;
			}
		}
		return killed;
	}

	private static void threadList(final ThreadGroup tGroup, final boolean nonDaemonsOnly)
	{
		if(tGroup==null)
			return;
		final int ac = tGroup.activeCount();
		final Thread tArray[] = new Thread [ac+1];
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
						summary=": session "+mobName+": "+S.getStatus().toString()+": "+CMParms.combineQuoted(S.getPreviousCMD(),0);
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

	@Override
	public String getHost()
	{
		return CMProps.getVar(Str.MUDDOMAIN);
	}

	@Override
	public int getPort()
	{
		return port;
	}

	@Override
	public String geWebHostUrl()
	{
		WebServer server=null;
		for(final WebServer serv : webServers)
		{
			if(!CMath.s_bool(serv.getConfig().getMiscProp("ADMIN")))
			{
				server=serv;
				break;
			}
		}
		if((server == null) && (webServers.size()>0))
			server = webServers.get(0);
		if(server == null)
			return "/";
		final int[] ports = server.getConfig().getHttpListenPorts();
		if((ports == null)||(ports.length==0))
			return "/";
		if(CMParms.contains(ports, 80))
			return "http://" + CMProps.getVar(Str.MUDDOMAIN)+"/";
		return "http://" + CMProps.getVar(Str.MUDDOMAIN)+":"+ports[0]+"/";
	}

	private static class HostGroup extends Thread
	{
		private String	name		= null;
		private String	iniFile		= null;
		private String	logName		= null;
		private char	threadCode	= MAIN_HOST;
		private boolean	hostStarted	= false;
		private boolean	failedStart	= false;
		//protected ThreadGroup threadGroup;

		public HostGroup(final ThreadGroup G, final String mudName, final String iniFileName)
		{
			super(G,"HOST"+grpid);
			//threadGroup=G;
			synchronized(CMClass.getSync("HostGroupInit"))
			{
				logName="mud"+((grpid>0)?("."+grpid):"");
				grpid++;
				iniFile=iniFileName;
				name=mudName;
				setDaemon(true);
				threadCode=G.getName().charAt(0);
			}
		}

		public boolean isStarted()
		{
			return hostStarted;
		}

		public boolean failedToStart()
		{
			return failedStart;
		}

		public void fatalStartupError(final Thread t, final int type)
		{
			String errorInternal=null;
			switch(type)
			{
			case 1:
				errorInternal="ERROR: initHost() will not run without properties. Exiting.";
				break;
			case 2:
				errorInternal="Map is empty?! Exiting.";
				break;
			case 3:
				errorInternal="Database init failed. Exiting.";
				break;
			case 4:
				errorInternal="Fatal exception. Exiting.";
				break;
			case 5:
				errorInternal="MUD Server did not start. Exiting.";
				break;
			default:
				errorInternal="Fatal error loading classes.  Make sure you start up coffeemud from the directory containing the class files.";
				break;
			}
			Log.errOut(Thread.currentThread().getName(),errorInternal);
			bringDown=true;

			CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN,true);
			//CMLib.killThread(t,100,1);
		}

		protected boolean initHost()
		{
			final Thread t=Thread.currentThread();
			final CMProps page=CMProps.instance();

			if ((page == null) || (!page.isLoaded()))
			{
				fatalStartupError(t,1);
				return false;
			}

			final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
			final boolean checkPrivate=(tCode!=MAIN_HOST);

			final List<String> compress=CMParms.parseCommas(page.getStr("COMPRESS").toUpperCase(),true);
			CMProps.setBoolVar(CMProps.Bool.ITEMDCOMPRESS,compress.contains("ITEMDESC"));
			CMProps.setBoolVar(CMProps.Bool.MOBCOMPRESS,compress.contains("GENMOBS"));
			CMProps.setBoolVar(CMProps.Bool.ROOMDCOMPRESS,compress.contains("ROOMDESC"));
			CMProps.setBoolVar(CMProps.Bool.MOBDCOMPRESS,compress.contains("MOBDESC"));
			Resources.setCompression(compress.contains("RESOURCES"));
			final List<String> nocache=CMParms.parseCommas(page.getStr("NOCACHE").toUpperCase(),true);
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
				&&((baseEngine==null)||(!baseEngine.isConnected())))
				{
					if(!checkedSleep(500))
						break;
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
				final String dbService=page.getStr("DBSERVICE");
				final String dbUser=page.getStr("DBUSER");
				final String dbPass=page.getStr("DBPASS");
				final int dbConns=page.getInt("DBCONNECTIONS");
				final int dbPingIntMins=page.getInt("DBPINGINTERVALMINS");
				final Map<String,String> dbParms=CMParms.parseEQParms(page.getStr("DBPARMS"),CMParms.spaceDelimiter,false);
				if(dbConns == 0)
				{
					Log.errOut(Thread.currentThread().getName(),"Fatal error: DBCONNECTIONS in INI file is "+dbConns);
					System.exit(-1);
				}
				final boolean dbReuse=page.getBoolean("DBREUSE");
				final boolean dbTransact=page.getBoolean("DBTRANSACT");
				final boolean useQue=!CMSecurity.isDisabled(CMSecurity.DisFlag.DBERRORQUE);
				final boolean useQueStart=!CMSecurity.isDisabled(CMSecurity.DisFlag.DBERRORQUESTART);
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: connecting to database");
				currentDBconnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbParms,dbConns,dbPingIntMins,dbReuse,dbTransact,useQue,useQueStart);
				currentDBconnector.reconnect();
				CMLib.registerLibrary(new DBInterface(currentDBconnector,CMProps.getPrivateSubSet("DB.*")));

				final DBConnection DBTEST=currentDBconnector.DBFetchTest();
				if(DBTEST!=null)
					currentDBconnector.DBDone(DBTEST);
				if((DBTEST!=null)&&(currentDBconnector.amIOk())&&(CMLib.database().isConnected()))
				{
					Log.sysOut(Thread.currentThread().getName(),"Connected to "+currentDBconnector.service());
					databases.add(currentDBconnector);
				}
				else
				{
					final String DBerrors=currentDBconnector.errorStatus().toString();
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
			try
			{
				final CMFile F = new CMFile("/test.the.database",null);
				if(F.exists())
					Log.sysOut(Thread.currentThread().getName(),"Test file found .. hmm.. that was unexpected.");

			}
			catch(final Exception e)
			{
				Log.errOut(e);
				Log.errOut("Database error! Panic shutdown!");
				return false;
			}

			String webServersList=page.getPrivateStr("RUNWEBSERVERS");
			if(webServersList.equalsIgnoreCase("true"))
				webServersList="pub,admin";
			if((webServersList.length()>0)&&(!webServersList.equalsIgnoreCase("false")))
			{
				final List<String> serverNames=CMParms.parseCommas(webServersList,true);
				for(int s=0;s<serverNames.size();s++)
				{
					final String serverName=serverNames.get(s);
					startWebServer(page, serverName);
				}
			}

			smtpServerThread = new SMTPserver(CMLib.mud(0)); // initializes variables, even if it's not used
			if(page.getPrivateStr("RUNSMTPSERVER").equalsIgnoreCase("true"))
			{
				smtpServerThread.start();
				serviceEngine.startTickDown(Thread.currentThread().getThreadGroup(),smtpServerThread,Tickable.TICKID_EMAIL,CMProps.getTickMillis(),(int)CMProps.getTicksPerMinute() * 5);
			}
			else
			{
				smtpServerThread = null;
				CMProps.setBoolAllVar(CMProps.Bool.EMAILFORWARDING,false);
			}

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading base classes");
			// wait for baseC
			while((tCode!=MudHost.MAIN_HOST)
			&&(!bootSync.get())
			&&(CMLib.s_sleep(500)))
			{}
			if(!CMClass.loadAllCoffeeMudClasses(page))
			{
				fatalStartupError(t,0);
				return false;
			}
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading factions");
			if((tCode==MAIN_HOST)
			||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.FACTIONS.name())))
				CMLib.factions().reloadFactions(CMProps.getVar(CMProps.Str.PREFACTIONS));

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: initializing classes");
			CMClass.instance().intializeClasses();

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading expertises");
			if((tCode==MudHost.MAIN_HOST)||(CMProps.isPrivateToMe(CMLib.Library.EXPERTISES.name())))
			{
				CMLib.expertises().recompileExpertises();
				Log.sysOut(Thread.currentThread().getName(),"Expertises defined: "+CMLib.expertises().numExpertises());
			}

			CMLib.lang().setLocale(CMLib.props().getStr("LANGUAGE"),CMLib.props().getStr("COUNTRY"));
			if((threadCode==MudHost.MAIN_HOST)
			||(CMLib.time()!=CMLib.library(MudHost.MAIN_HOST, CMLib.Library.TIME)))
				CMLib.time().globalClock().initializeINIClock(page);
			else
			{
				CMProps.setIntVar(CMProps.Int.TICKSPERMUDDAY,""+((CMProps.getMillisPerMudHour()*CMLib.time().globalClock().getHoursInDay()/CMProps.getTickMillis())));
				CMProps.setIntVar(CMProps.Int.TICKSPERMUDMONTH,""+((CMProps.getMillisPerMudHour()*CMLib.time().globalClock().getHoursInDay()*CMLib.time().globalClock().getDaysInMonth()/CMProps.getTickMillis())));
			}

			if(tCode==MudHost.MAIN_HOST)
				bootSync.set(true);

			if((tCode==MAIN_HOST)
			||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.CHANNELS.name()))
			||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.JOURNALS.name())))
			{
				int numChannelsLoaded=0;
				int numJournalsLoaded=0;
				if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.CHANNELS.name())))
				{
					final String normalChannels=page.getStr("CHANNELS");
					final String i3Channels=page.getBoolean("RUNI3SERVER") ? page.getStr("ICHANNELS") : "";
					final String imc2Channels=page.getBoolean("RUNIMC2CLIENT") ? page.getStr("IMC2CHANNELS") : "";
					numChannelsLoaded=CMLib.channels().loadChannels(normalChannels, i3Channels, imc2Channels);
				}
				if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.JOURNALS.name())))
				{
					numJournalsLoaded=CMLib.journals().loadCommandJournals(page.getStr("COMMANDJOURNALS"));
					numJournalsLoaded+=CMLib.journals().loadForumJournals(page.getStr("FORUMJOURNALS"));
				}
				Log.sysOut(Thread.currentThread().getName(),"Channels loaded   : "+(numChannelsLoaded+numJournalsLoaded));
			}

			if((tCode==MAIN_HOST)
			||(page.getRawPrivateStr("SYSOPMASK")!=null)
			||(checkPrivate&&CMProps.isPrivateToMe("SECURITY"))
			) // needs to be after journals, for journal flags
			{
				CMSecurity.setSysOp(page.getStr("SYSOPMASK")); // requires all classes be loaded
				CMSecurity.parseGroups(page);
			}
			else
				CMSecurity.shareWith(MAIN_HOST);

			if((tCode==MAIN_HOST)
			||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.SOCIALS.name())))
			{
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading socials");
				CMLib.socials().unloadSocials();
				final List<String> list = CMLib.socials().getSocialsBaseList();
				if(list.size()==0)
					Log.errOut(Thread.currentThread().getName(),"WARNING: Unable to load socials from socials.txt!");
				else
					Log.sysOut(Thread.currentThread().getName(),"Socials loaded    : "+list.size());
			}

			final Map<String,Clan> clanPostLoads=new TreeMap<String,Clan>();
			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.CLANS.name())))
			{
				final List<Clan> clans = CMLib.database().DBReadAllClans();
				for(final Clan C : clans)
				{
					CMLib.clans().addClan(C);
					clanPostLoads.put(C.clanID(), C);
				}
				Log.sysOut(Thread.currentThread().getName(),"Clans loaded      : "+CMLib.clans().numClans());
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.FACTIONS.name())))
				serviceEngine.startTickDown(Thread.currentThread().getThreadGroup(),CMLib.factions(),Tickable.TICKID_MOB,CMProps.getTickMillis(),10);

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Starting CM1");
			startCM1();

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Starting I3");
			startIntermud3((smtpServerThread==null)?-1:smtpServerThread.getSMTPPort());

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Starting IMC2");
			startIntermud2();

			checkedSleep(500);

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.CATALOG.name())))
			{
				Log.sysOut(Thread.currentThread().getName(),"Loading catalog...");
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading catalog....");
				CMLib.database().DBReadCatalogs();
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.MAP.name())))
			{
				Log.sysOut(Thread.currentThread().getName(),"Loading map...");
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading rooms....");
				CMLib.database().DBReadAllRooms(null);
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: loading space....");
				CMLib.database().DBReadSpace();
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: preparing map....");
				Log.sysOut(Thread.currentThread().getName(),"Preparing map...");
				CMLib.database().DBReadArtifacts();
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: filling map ("+A.Name()+")");
					A.fillInAreaRooms();
				}
				{
					final MOB mob=CMClass.getFactoryMOB();
					final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_STARTUP,null);
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Sending room startup.");
					for(final Enumeration<Room> a=CMLib.map().rooms();a.hasMoreElements();)
					{
						final Room R=a.nextElement();
						if(R!=null)
						{
							msg.setTarget(R);
							R.send(mob, msg);
						}
					}
					mob.destroy();
				}

				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Map Load Complete.");
				Log.sysOut(Thread.currentThread().getName(),"Mapped rooms      : "+CMLib.map().numRooms()+" in "+CMLib.map().numAreas()+" areas");
				if(CMLib.space().numSpaceObjects()>0)
					Log.sysOut(Thread.currentThread().getName(),"Space objects     : "+CMLib.space().numSpaceObjects());

				if(!CMLib.map().roomIDs().hasMoreElements())
				{
					Log.sysOut("NO MAPPED ROOM?!  I'll make ya one!");
					CMLib.time().globalClock().setYear(1000);
					final String id="START";//New Area#0";
					final Area newArea=CMClass.getAreaType("StdArea");
					newArea.setName(CMLib.lang().L("New Area"));
					CMLib.map().addArea(newArea);
					CMLib.database().DBCreateArea(newArea);
					final Room room=CMClass.getLocale("StdRoom");
					room.setRoomID(id);
					room.setArea(newArea);
					room.setDisplayText(CMLib.lang().L("New Room"));
					room.setDescription(CMLib.lang().L("Brand new database room! You need to change this text with the MODIFY ROOM command.  If your character is not an Archon, pick up the book you see here and read it immediately!"));
					CMLib.map().registerWorldObjectLoaded(newArea, null, newArea);
					CMLib.database().DBCreateRoom(room);
					final Item I=CMClass.getMiscMagic("ManualArchon");
					room.addItem(I);
					CMLib.database().DBUpdateItems(room);
				}
			}

			if(clanPostLoads.size()>0)
			{
				final int num = CMLib.database().DBReadClanItems(clanPostLoads);
				Log.sysOut(Thread.currentThread().getName(),"Clan owned items  : "+num);
			}

			if((tCode==MAIN_HOST)||(checkPrivate&&CMProps.isPrivateToMe(CMLib.Library.QUEST.name())))
			{
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Loading Quests");
				CMLib.quests().shutdown();
				for(final Quest Q : CMLib.database().DBReadQuests())
					CMLib.quests().addQuest(Q);
				if(CMLib.quests().numQuests()>0)
					Log.sysOut(Thread.currentThread().getName(),"Quests loaded     : "+CMLib.quests().numQuests());
			}

			if(tCode!=MAIN_HOST)
			{
				CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: Waiting for HOST0");
				while((!MUD.bringDown)
				&&(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
				&&(!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
				{
					if(!checkedSleep(500))
						break;
				}
				if((MUD.bringDown)
				||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
				||(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
					return false;

			}

			if((tCode==MAIN_HOST)
			||(checkPrivate
				&& CMProps.isPrivateToMe(CMLib.Library.LOGIN.name())))
			{
				CMLib.login().initStartRooms(page);
				CMLib.login().initDeathRooms(page);
				CMLib.login().initBodyRooms(page);
			}

			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: readying for connections.");
			try
			{
				// start all clocks
				final TimeClock gC = CMLib.time().globalClock();
				gC.tick(null, -1);
				for(final Enumeration<Area> a = CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					final TimeClock C = A.getTimeObj();
					if(C != gC)
						C.tick(null, -1);
				}
				CMLib.activateLibraries();
				Log.sysOut(Thread.currentThread().getName(),"Services and utilities started");
			}
			catch (final Throwable th)
			{
				Log.errOut(Thread.currentThread().getName(),"CoffeeMud Server initHost() failed");
				Log.errOut(Thread.currentThread().getName(),th);
				fatalStartupError(t,4);
				return false;
			}

			final StringBuffer str=new StringBuffer("");
			for(int m=0;m<CMLib.hosts().size();m++)
			{
				final MudHost mud=CMLib.hosts().get(m);
				str.append(" "+mud.getPort());
			}
			CMProps.setVar(CMProps.Str.ALLMUDPORTS,str.toString());
			CMProps.setBoolAllVar(CMProps.Bool.MUDSTARTED,true);
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Online");
			Log.sysOut(Thread.currentThread().getName(),"Host#"+threadCode+" initializated.");
			return true;
		}

		@Override
		public void run()
		{
			CMLib.initialize(); // initialize the lib
			CMClass.initialize(); // initialize the classes
			Log.shareWith(MudHost.MAIN_HOST);
			Resources.shareWith(MudHost.MAIN_HOST);

			// wait for ini to be loaded, and for other matters
			if(threadCode!=MAIN_HOST)
			{
				while((CMLib.library(MAIN_HOST,CMLib.Library.INTERMUD)==null)&&(!MUD.bringDown))
				{
					if(!checkedSleep(500))
						break;
				}
				if(MUD.bringDown)
					return;
			}
			final CMProps page=CMProps.loadPropPage("//"+iniFile);
			page.putAll(clArgs);
			if ((page==null)||(!page.isLoaded()))
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
				System.err.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
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
					for(final Log.Type logType : Log.Type.values())
						Log.instance().configureLog(logType, page.getStr(logType.getLogCode()));
				}
				if((page.getRawPrivateStr("SYSOPMASK")!=null)
				||(page.getRawPrivateStr("DISABLE")!=null)
				||(page.getRawPrivateStr("ENABLE")!=null)
				||(page.getRawPrivateStr("DEBUG")!=null)
				||(page.getRawPrivateStr("SAVE")!=null))
				{
					//page.resetSecurityVars();
				}
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

			final DBConnector currentDBconnector=new DBConnector();
			CMLib.registerLibrary(new DBInterface(currentDBconnector,CMProps.getPrivateSubSet("DB.*")));
			CMProps.setVar(CMProps.Str.MUDVER, HOST_VERSION);

			// an arbitrary dividing line. After threadCode 0
			if(threadCode==MAIN_HOST)
			{
				CMLib.registerLibrary(serviceEngine);
				CMLib.registerLibrary(new IMudClient());
			}
			else
			{
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

				final LinkedList<MUD> hostMuds=new LinkedList<MUD>();
				String ports=page.getProperty("PORT");
				if((ports==null)||(ports.length()==0))
				{
					Log.errOut("HOST#"+this.threadCode+" could not start any listeners.");
					failedStart=true;
					return;
				}
				int pdex=ports.indexOf(',');
				while(pdex>0)
				{
					final MUD mud=new MUD("MUD@"+ports.substring(0,pdex));
					mud.setState(MudState.STARTING);
					mud.acceptConns=false;
					mud.port=CMath.s_int(ports.substring(0,pdex));
					ports=ports.substring(pdex+1);
					hostMuds.add(mud);
					mud.start();
					pdex=ports.indexOf(',');
				}
				final MUD mud=new MUD("MUD@"+ports);
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
				final long timeout=System.currentTimeMillis()+60000;
				while((!oneStarted) && (System.currentTimeMillis()<timeout))
				{
					int numStopped=0;
					for(final MUD m : hostMuds)
					{
						if(m.state==MudState.STOPPED)
							numStopped++;
						else
						if(m.state!=MudState.STARTING)
							oneStarted=true;
					}
					if(numStopped==hostMuds.size())
					{
						Log.errOut("HOST#"+this.threadCode+" could not start any listeners.");
						failedStart=true;
						return;
					}
					checkedSleep(100);
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
					{
						if(CMLib.hosts().get(i) instanceof Thread)
						{
							joinable=(Thread)CMLib.hosts().get(i);
							break;
						}
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
			catch(final InterruptedException e)
			{
				Log.errOut(Thread.currentThread().getName(),e);
			}
		}
	}

	@Override
	public List<Runnable> getOverdueThreads()
	{
		final Vector<Runnable> V=new Vector<Runnable>();
		for(int w=0;w<webServers.size();w++)
			V.addAll(webServers.get(w).getOverdueThreads());
		return V;
	}

	public static void main(final String a[])
	{
		String nameID="";
		Thread.currentThread().setName("MUD");
		final Vector<String> iniFiles=new Vector<String>();
		if(a.length>0)
		{
			final Map<String,String[]> hargs=CMParms.parseCommandLineArgs(a);
			if(hargs.containsKey(""))
				nameID = hargs.remove("")[0];
			final String[] boots = hargs.remove("BOOT");
			if(boots != null)
			{
				for(final String bootIni : boots)
					iniFiles.add(bootIni);
			}
			final Map<String,String> fargs = new Hashtable<String,String>();
			for(final String key : hargs.keySet())
				fargs.put(key, hargs.get(key)[hargs.get(key).length-1]);
			clArgs = new ReadOnlyMap<String,String>(fargs);
		}
		CMLib.initialize(); // initialize this threads libs

		if(iniFiles.size()==0)
			iniFiles.addElement("coffeemud.ini");
		String iniFile=iniFiles.firstElement();
		final CMProps page=CMProps.loadPropPage("//"+iniFile);
		page.putAll(clArgs);
		if((nameID.length()==0)||(nameID.equalsIgnoreCase( "CoffeeMud" ))||nameID.equalsIgnoreCase("Your Muds Name"))
		{
			long idNumber=new Random(System.currentTimeMillis()).nextLong();
			try
			{
				idNumber=0;
				for(final Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();e.hasMoreElements();)
				{
					final NetworkInterface n=e.nextElement();
					idNumber^=n.getDisplayName().hashCode();
					try
					{
						final Method m=n.getClass().getMethod("getHardwareAddress");
						final Object o=m.invoke(n);
						if(o instanceof byte[])
						{
							for(int i=0;i<((byte[])o).length;i++)
								idNumber^=((byte[])o)[0] << (i*8);
						}
					}
					catch (final Exception e1)
					{
					}
				}
			}
			catch (final Exception e1)
			{
			}
			if(idNumber<0)
				idNumber=idNumber*-1;
			nameID=nameID+idNumber;
			if((page != null)
			&& page.containsKey("MUD_NAME")
			&& (page.getStr("MUD_NAME") != null)
			&& (page.getStr("MUD_NAME").toString().trim().length()>0))
			{
				nameID = page.getStr("MUD_NAME").toString().trim();
				nameID = CMStrings.replaceAll(nameID, "\"", "`");
			}
			else
				System.err.println("*** Please give your mud a unique name in mud.bat or mudUNIX.sh!! ***");
		}
		else
		if(nameID.equalsIgnoreCase( "TheRealCoffeeMudCopyright2000-2024ByBoZimmerman" ))
			nameID="CoffeeMud";
		if ((page==null)||(!page.isLoaded()))
		{
			Log.instance().configureLogFile("mud",1);
			for(final Log.Type logType : Log.Type.values())
				Log.instance().configureLog(logType, "BOTH");
			Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
			System.err.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
			CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"A terminal error has occured!");
			System.exit(-1);
			return;
		}
		Log.shareWith(MudHost.MAIN_HOST);
		Log.instance().configureLogFile("mud",page.getInt("NUMLOGS"));
		for(final Log.Type logType : Log.Type.values())
			Log.instance().configureLog(logType, page.getStr(logType.getLogCode()));

		final Thread shutdownHook=new Thread("ShutdownHook")
		{
			@Override
			public void run()
			{
				if(!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
				{
					ServiceEngine.panicDumpAllThreads();
					MUD.globalShutdown(null,true,null);
				}
			}
		};

		while(!bringDown)
		{
			final PrintStream eolStream = System.out;
			eolStream.println();
			grpid=0;
			Log.sysOut(Thread.currentThread().getName(),"CoffeeMud v"+HOST_VERSION);
			Log.sysOut(Thread.currentThread().getName(),"(C) 2000-2024 Bo Zimmerman");
			Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
			CMLib.hosts().clear();
			final LinkedList<HostGroup> myGroups=new LinkedList<HostGroup>();
			HostGroup mainHostGroup=null;
			ThreadGroup mainThreadGroup = null;
			for(int i=0;i<iniFiles.size();i++)
			{
				iniFile=iniFiles.elementAt(i);
				final ThreadGroup G=new ThreadGroup(i+"-MUD");
				final HostGroup H=new HostGroup(G,nameID,iniFile);
				if((mainHostGroup==null)||(mainThreadGroup == null))
				{
					mainHostGroup=H;
					mainThreadGroup=G;
				}
				myGroups.add(H);
				H.start();
			}
			if(mainHostGroup==null)
			{
				Log.errOut("CoffeeMud failed to start.");
				MUD.bringDown=true;
				CMProps.setBoolAllVar(CMProps.Bool.MUDSHUTTINGDOWN, true);
			}
			else
			{
				final long timeout=System.currentTimeMillis()+1800000; /// 30 mins
				int numPending=1;
				while((numPending>0)&&(System.currentTimeMillis()<timeout))
				{
					numPending=0;
					for(final HostGroup g : myGroups)
					{
						if(!g.failedToStart() && !g.isStarted())
							numPending++;
					}
					if(mainHostGroup.failedToStart())
						break;
					checkedSleep(100);
				}
				if(mainHostGroup.failedToStart())
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
					try
					{
						mainHostGroup.join();
					}
					catch(final Exception e)
					{
						e.printStackTrace(); Log.errOut(Thread.currentThread().getName(),e);
					}
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
			}

			System.gc();
			checkedSleep(1000);
			System.runFinalization();
			checkedSleep(1000);

			if(activeThreadCount(Thread.currentThread().getThreadGroup(),true)>1)
			{
				checkedSleep(1000);
				killCount(Thread.currentThread().getThreadGroup(),true);
				checkedSleep(1000);
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
					execExternalRestart(execExternalCommand);
					execExternalCommand=null;
					bringDown=true;
				}
			}
		}
	}

	public static void execExternalRestart(final String command)
	{
		final Runtime r=Runtime.getRuntime();
		try
		{
			if((command==null) || (command.equalsIgnoreCase("hard")))
			{
				if(new File("./restart.sh").exists())
				{
					r.exec("sh restart.sh".split(" "));
					Log.sysOut("Attempted to execute 'restart.sh' in "+new File(".").getCanonicalPath());
				}
				else
				if(new File(".\\restart.bat").exists())
				{
					r.exec(new String[] {"cmd.exe","/c","start","","restart.bat"});
					Log.sysOut("Attempted to execute 'restart.bat' in "+new File(".").getCanonicalPath());
				}
			}
			else
			if(System.getProperty("os.name").toLowerCase().indexOf("windows")>=0)
			{
				r.exec(new String[] {"cmd.exe","/c","start","",command});
				Log.sysOut("Attempted to execute '"+command+"' in "+new File(".").getCanonicalPath());
			}
			else
			{
				r.exec(new String[] {"sh",command});
				Log.sysOut("Attempted to execute '"+command+"' in "+new File(".").getCanonicalPath());
			}
		}
		catch (final IOException e)
		{
			Log.errOut(e);
		}
	}

	@Override
	public void setAcceptConnections(final boolean truefalse)
	{
		acceptConns=truefalse;
	}

	@Override
	public boolean isAcceptingConnections()
	{
		return acceptConns;
	}

	@Override
	public long getUptimeSecs()
	{
		return (System.currentTimeMillis()-startupTime)/1000;
	}

	@Override
	public String executeCommand(final String cmd) throws Exception
	{
		final List<String> V=CMParms.parse(cmd);
		if(V.size()==0)
			throw new CMException("Unknown command!");
		final String word=V.get(0);
		if(word.equalsIgnoreCase("GET")&&(V.size()>1))
		{
			final String what=V.get(1);
			if(what.equalsIgnoreCase("CM1SERVER")&&(V.size()>2))
			{
				final String what2=V.get(2);
				if(cm1Servers.size()==0)
					return "Failure";
				final CM1Server svr=cm1Servers.get(0);
				if(what2.equalsIgnoreCase("PORT"))
					return ""+svr.getPort();
				else
				if(what2.equalsIgnoreCase("NAME"))
					return ""+svr.getName();
				return "Failure";
			}
			else
				return "Failure";
		}
		else
		if(word.equalsIgnoreCase("TICK")&&(V.size()>1))
		{
			final String what=V.get(1);
			if(what.equalsIgnoreCase("SMTP"))
			{
				if((smtpServerThread != null)
				&&(smtpServerThread.tick(smtpServerThread, Tickable.TICKID_EMAIL)))
					return "Done";
				else
					return "Failure";
			}
			else
				return "Failure";
		}
		else
		if(word.equalsIgnoreCase("START")&&(V.size()>1))
		{
			final String what=V.get(1);
			if(what.equalsIgnoreCase("I3"))
			{
				startIntermud3((smtpServerThread==null)?-1:smtpServerThread.getSMTPPort());
				return "Done";
			}
			else
			if(what.equalsIgnoreCase("IMC2"))
			{
				startIntermud2();
				return "Done";
			}
			else
			if(what.equalsIgnoreCase("WEB"))
			{
				if(V.size()<3)
					return "Need Server Name";
				if(startWebServer(CMProps.instance(),V.get(2)))
					return "Done";
				else
					return "Failure";
			}
			else
			if(what.equalsIgnoreCase("CM1"))
			{
				if(V.size()<3)
					return "Need Server Name";
				if(startCM1())
					return "Done";
				else
					return "Failure";
			}
			else
			if(what.equalsIgnoreCase("SMTP"))
			{
				if(smtpServerThread==null)
				{
					smtpServerThread = new SMTPserver(CMLib.mud(0)); // initializes variables, even if it's not used
					smtpServerThread.start();
					serviceEngine.startTickDown(Thread.currentThread().getThreadGroup(),smtpServerThread,Tickable.TICKID_EMAIL,CMProps.getTickMillis(),(int)CMProps.getTicksPerMinute() * 5);
					return "Done";
				}
				return "Failure";
			}
		}
		else
		if(word.equalsIgnoreCase("STOP")&&(V.size()>1))
		{
			final String what=V.get(1);
			if(what.equalsIgnoreCase("WEB"))
			{
				if(V.size()<3)
					return "Need Server Name";
				if(stopWebServer(V.get(2)))
					return "Done";
				else
					return "Failure";
			}
			else
			if(what.equalsIgnoreCase("I3"))
			{
				if(i3server!=null)
					I3Server.shutdown();
				i3server=null;
				return "Done";
			}
			else
			if(what.equalsIgnoreCase("IMC2"))
			{
				if(imc2server!=null)
					imc2server.shutdown();
				return "Done";
			}
			else
			if(what.equalsIgnoreCase("CM1"))
			{
				if(stopCM1())
					return "Done";
				else
					return "Failure";
			}
			else
			if(what.equalsIgnoreCase("SMTP"))
			{
				if(smtpServerThread!=null)
				{
					smtpServerThread.shutdown();
					serviceEngine.unTickAll(smtpServerThread);
					smtpServerThread = null;
					return "Done";
				}
				return "Failure";
			}
		}
		else
		if(word.equalsIgnoreCase("WEBSERVER")&&(V.size()>2))
		{
			final String var = V.get(2);
			WebServer server=null;
			for(final WebServer serv : webServers)
			{
				if(serv.getName().equalsIgnoreCase(V.get(1)))
				{
					server=serv;
					break;
				}
				else
				if(CMath.s_bool(serv.getConfig().getMiscProp("ADMIN")))
				{
					if(V.get(1).equalsIgnoreCase("ADMIN"))
					{
						server=serv;
						break;
					}
				}
				else
				{
					if(V.get(1).equalsIgnoreCase("PUB"))
					{
						server=serv;
						break;
					}
				}
			}
			if(server==null)
				throw new CMException("Unknown server: "+var);
			if(var.equalsIgnoreCase("PORT"))
			{
				final int[] ports = server.getConfig().getHttpListenPorts();
				if(ports.length==0)
					return "";
				return Integer.toString(ports[0]);
			}
			else
			if((V.size()>3)&&(var.equalsIgnoreCase("DEBUG")))
				server.getConfig().setDebugFlag(V.get(3));
			else
			if((V.size()>3)&&(var.equalsIgnoreCase("ACCESS")))
				server.getConfig().setDebugFlag(V.get(3));
			else
				throw new CMException("Unknown variable: "+var);
			return "";
		}
		throw new CMException("Unknown command: "+word);
	}
}
