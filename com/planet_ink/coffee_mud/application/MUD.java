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
import com.planet_ink.coffee_mud.core.threads.Tick;
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
   Copyright 2000-2013 Bo Zimmerman

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
	private static final float	HOST_VERSION_MAJOR	= (float)5.8;
	private static final long	 HOST_VERSION_MINOR	= 0;
	private final static String[] STATE_STRING			= {"waiting","accepting","allowing"};

	private int				state		= 0;
	private ServerSocket	servsock	= null;
	private boolean			acceptConns	= false;
	private String			host		= "MyHost";
	private int				port		= 5555;
	private final long		startupTime = System.currentTimeMillis();

	private static boolean				serverIsRunning		= false;
	private static boolean				isOK 				= false;
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

	public MUD(String name)
	{
		super(name);
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
		Log.errOut(Thread.currentThread().getName(),str);
		bringDown=true;
		CMProps.setBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN,true);
		CMLib.killThread(t,100,1);
	}

	protected static boolean initHost(Thread t)
	{
		if (!isOK)
		{
			CMLib.killThread(t,100,1);
			return false;
		}

		CMProps page=CMProps.instance();
		
		if ((page == null) || (!page.isLoaded()))
		{
			fatalStartupError(t,1);
			return false;
		}
		
		char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		Vector<String> privacyV=new Vector<String>(1);
		if(tCode!=MAIN_HOST)
			privacyV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true);
		
		long startWait=System.currentTimeMillis();
		while (!serverIsRunning && isOK && ((System.currentTimeMillis() - startWait)< 90000))
		{ try{ Thread.sleep(500); }catch(Exception e){ isOK=false;} }
		
		if((!isOK)||(!serverIsRunning))
		{
			fatalStartupError(t,5);
			return false;
		}
		
		Vector<String> compress=CMParms.parseCommas(page.getStr("COMPRESS").toUpperCase(),true);
		CMProps.setBoolVar(CMProps.SYSTEMB_ITEMDCOMPRESS,compress.contains("ITEMDESC"));
		CMProps.setBoolVar(CMProps.SYSTEMB_MOBCOMPRESS,compress.contains("GENMOBS"));
		CMProps.setBoolVar(CMProps.SYSTEMB_ROOMDCOMPRESS,compress.contains("ROOMDESC"));
		CMProps.setBoolVar(CMProps.SYSTEMB_MOBDCOMPRESS,compress.contains("MOBDESC"));
		Resources.setCompression(compress.contains("RESOURCES"));
		Vector<String> nocache=CMParms.parseCommas(page.getStr("NOCACHE").toUpperCase(),true);
		CMProps.setBoolVar(CMProps.SYSTEMB_MOBNOCACHE,nocache.contains("GENMOBS"));
		CMProps.setBoolVar(CMProps.SYSTEMB_ROOMDNOCACHE,nocache.contains("ROOMDESC"));
		CMProps.setBoolVar(CMProps.SYSTEMB_FILERESOURCENOCACHE, nocache.contains("FILERESOURCES"));
		CMProps.setBoolVar(CMProps.SYSTEMB_CATALOGNOCACHE, nocache.contains("CATALOG"));
		CMProps.setBoolVar(CMProps.SYSTEMB_MAPFINDSNOCACHE,nocache.contains("MAPFINDERS"));

		DBConnector currentDBconnector=null;
		String dbClass=page.getStr("DBCLASS");
		if(tCode!=MAIN_HOST)
		{
			DatabaseEngine baseEngine=(DatabaseEngine)CMLib.library(MAIN_HOST,CMLib.LIBRARY_DATABASE);
			while((!MUD.bringDown)
			&&((baseEngine==null)||(!baseEngine.isConnected()))) {
				try {Thread.sleep(500);}catch(Exception e){ break;}
				baseEngine=(DatabaseEngine)CMLib.library(MAIN_HOST,CMLib.LIBRARY_DATABASE);
			}
			if(MUD.bringDown) return false;
			
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
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: connecting to database");
			currentDBconnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbConns,dbPingIntMins,dbReuse,useQue,useQueStart);
			currentDBconnector.reconnect();
			CMLib.registerLibrary(new DBInterface(currentDBconnector,CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true)));

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
				System.exit(-1);
			}
		}
		else
		if(CMLib.database()==null)
		{
			Log.errOut(Thread.currentThread().getName(),"No registered database!");
			System.exit(-1);
		}

		// test the database
		try {
			CMFile F = new CMFile("/test.the.database",null,false);
			if(F.exists())
				Log.sysOut(Thread.currentThread().getName(),"Test file found .. hmm.. that was unexpected.");
				
		} catch(Exception e) {
			Log.errOut(Thread.currentThread().getName(),e.getMessage());
			Log.errOut(Thread.currentThread().getName(),"Database error! Panic shutdown!");
			System.exit(-1);
		}
		
		String webServersList=page.getPrivateStr("RUNWEBSERVERS");
		if(webServersList.equalsIgnoreCase("true"))
			webServersList="pub,admin";
		if((webServersList.length()>0)&&(!webServersList.equalsIgnoreCase("false")))
		{
			Vector<String> serverNames=CMParms.parseCommas(webServersList,true);
			for(int s=0;s<serverNames.size();s++)
			{
				String serverName=serverNames.elementAt(s);
				try
				{
					StringBuffer commonProps=new CMFile("web/common.ini", null, true).text();
					StringBuffer finalProps=new CMFile("web/"+serverName+".ini", null, true).text();
					commonProps.append("\n").append(finalProps.toString());
					MiniWebConfig config=new MiniWebConfig();
					config.setFileManager(new FileManager(){
						@Override public char getFileSeparator() { 
							return '/';
						}
						@Override public File createFileFromPath(String localPath) {
							return new CMFile(localPath,null,false);
						}
						@Override public File createFileFromPath(File parent, String localPath) {
							return new CMFile(parent.getAbsolutePath()+'/'+localPath,null,false);
						}
						@Override public byte[] readFile(File file) throws IOException, FileNotFoundException {
							return ((CMFile)file).raw();
						}
						@Override public InputStream getFileStream(File file) throws IOException, FileNotFoundException {
							return ((CMFile)file).getRawStream();
						}
						@Override
						public RandomAccessFile getRandomAccessFile(File file) throws IOException, FileNotFoundException {
							return new RandomAccessFile(new File(((CMFile)file).getLocalPathAndName()),"r");
						}
						@Override
						public boolean supportsRandomAccess(File file) { 
							return ((CMFile)file).isLocalFile();
						}
					});
					MiniWebServer.initConfig(config, Log.instance(), new ByteArrayInputStream(commonProps.toString().getBytes()));
					if(CMSecurity.isDebugging(DbgFlag.HTTPREQ))
						config.setDebugFlag("BOTH");
					MiniWebServer webServer=new MiniWebServer(serverName,config);
					config.setMiniWebServer(webServer);
					webServer.start();
					webServers.add(webServer);
				}
				catch(Exception e)
				{
					Log.errOut("MUD","HTTP server "+serverName+"NOT started: "+e.getMessage());
				}
			}
		}

		if(page.getPrivateStr("RUNSMTPSERVER").equalsIgnoreCase("true"))
		{
			smtpServerThread = new SMTPserver(CMLib.mud(0));
			smtpServerThread.start();
			CMLib.threads().startTickDown(smtpServerThread,Tickable.TICKID_EMAIL,(int)CMProps.getTicksPerMinute() * 5);
		}

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading base classes");
		if(!CMClass.loadClasses(page))
		{
			fatalStartupError(t,0);
			return false;
		}
		CMLib.lang().setLocale(CMLib.props().getStr("LANGUAGE"),CMLib.props().getStr("COUNTRY"));
		CMLib.time().globalClock().initializeINIClock(page);
		if((tCode==MAIN_HOST)||(privacyV.contains("FACTIONS")))
			CMLib.factions().reloadFactions(CMProps.getVar(CMProps.SYSTEM_PREFACTIONS));
		
		if((tCode==MAIN_HOST)||(privacyV.contains("CHANNELS"))||(privacyV.contains("JOURNALS")))
		{
			int numChannelsLoaded=0;
			int numJournalsLoaded=0;
			if((tCode==MAIN_HOST)||(privacyV.contains("CHANNELS")))
				numChannelsLoaded=CMLib.channels().loadChannels(page.getStr("CHANNELS"),
																page.getStr("ICHANNELS"),
																page.getStr("IMC2CHANNELS"));
			if((tCode==MAIN_HOST)||(privacyV.contains("JOURNALS")))
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

		if((tCode==MAIN_HOST)||(privacyV.contains("SOCIALS")))
		{
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading socials");
			CMLib.socials().unloadSocials();
			if(CMLib.socials().numSocialSets()==0)
				Log.errOut(Thread.currentThread().getName(),"WARNING: Unable to load socials from socials.txt!");
			else
				Log.sysOut(Thread.currentThread().getName(),"Socials loaded    : "+CMLib.socials().numSocialSets());
		}

		if((tCode==MAIN_HOST)||(privacyV.contains("CLANS")))
		{
			CMLib.database().DBReadAllClans();
			Log.sysOut(Thread.currentThread().getName(),"Clans loaded      : "+CMLib.clans().numClans());
		}

		if((tCode==MAIN_HOST)||(privacyV.contains("FACTIONS")))
			CMLib.threads().startTickDown(CMLib.factions(),Tickable.TICKID_MOB,10);

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Starting CM1");
		startCM1();
		
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Starting I3");
		startIntermud3();

		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Starting IMC2");
		startIntermud2();
		
		try{Thread.sleep(500);}catch(Exception e){}
		
		if((tCode==MAIN_HOST)||(privacyV.contains("CATALOG")))
		{
			Log.sysOut(Thread.currentThread().getName(),"Loading catalog...");
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading catalog....");
			CMLib.database().DBReadCatalogs();
		}

		if((tCode==MAIN_HOST)||(privacyV.contains("MAP")))
		{
			Log.sysOut(Thread.currentThread().getName(),"Loading map...");
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: loading rooms....");
			CMLib.database().DBReadAllRooms(null);
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: preparing map....");
			Log.sysOut(Thread.currentThread().getName(),"Preparing map...");
			CMLib.database().DBReadArtifacts();
			for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=a.nextElement();
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: filling map ("+A.Name()+")");
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

		if((tCode==MAIN_HOST)||(privacyV.contains("QUESTS")))
		{
			CMLib.database().DBReadQuests(CMLib.mud(0));
			if(CMLib.quests().numQuests()>0)
				Log.sysOut(Thread.currentThread().getName(),"Quests loaded     : "+CMLib.quests().numQuests());
		}
		
		if(tCode!=MAIN_HOST)
		{
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Waiting for HOST0");
			while((!MUD.bringDown)
			&&(!CMProps.getBoolVar0(CMProps.SYSTEMB_MUDSTARTED))
			&&(!CMProps.getBoolVar0(CMProps.SYSTEMB_MUDSHUTTINGDOWN)))
				try{Thread.sleep(500);}catch(Exception e){ break;}
			if((MUD.bringDown)
			||(!CMProps.getBoolVar0(CMProps.SYSTEMB_MUDSTARTED))
			||(CMProps.getBoolVar0(CMProps.SYSTEMB_MUDSHUTTINGDOWN)))
				return false;
		}
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: readying for connections.");
		try
		{
			CMLib.activateLibraries();
			Log.sysOut(Thread.currentThread().getName(),"Utility threads started");
		}
		catch (Throwable th)
		{
			Log.errOut(Thread.currentThread().getName(),"CoffeeMud Server initHost() failed");
			Log.errOut(Thread.currentThread().getName(),th);
			fatalStartupError(t,4);
			return false;
		}

		
		for(int i=0;i<CMLib.hosts().size();i++)
			CMLib.hosts().get(i).setAcceptConnections(true);
		Log.sysOut(Thread.currentThread().getName(),"Initialization complete.");
		CMProps.setBoolVar(CMProps.SYSTEMB_MUDSTARTED,true);
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"OK");
		return true;
	}

	@Override
	public void acceptConnection(Socket sock) throws SocketException, IOException 
	{
		CMLib.threads().executeRunnable(new ConnectionAcceptor(sock));
	}
	
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
			state=1;
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
						state=2;
						// also the intro page
						CMFile introDir=new CMFile(Resources.makeFileResourceName("text"),null,false,true);
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
						S.initializeSession(sock, introText != null ? introText.toString() : null);
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
						out.println("\n\rOFFLINE: " + CMProps.getVar(CMProps.SYSTEM_MUDSTATUS)+"\n\r");
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
				Log.errOut(Thread.currentThread().getName(),"ERROR: MUD Server could not bind to address " + CMProps.getVar(CMProps.SYSTEM_MUDBINDADDRESS));
			}
		}

		try
		{
			servsock=new ServerSocket(port, q_len, bindAddr);

			Log.sysOut(Thread.currentThread().getName(),"MUD Server started on port: "+port);
			if (bindAddr != null)
				Log.sysOut(Thread.currentThread().getName(),"MUD Server bound to: "+bindAddr.toString());
			serverIsRunning = true;

			while(true)
			{
				state=0;
				if(servsock==null) break;
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

			if (!serverIsRunning)
				isOK = false;
		}

		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud Server cleaning up.");

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
		for(int i=0;i<CMLib.hosts().size();i++)
			CMLib.hosts().get(i).setAcceptConnections(false);
		Log.sysOut(Thread.currentThread().getName(),"New Connections are now closed");
		if(S!=null)S.println("Done.");

		if(!CMSecurity.isSaveFlag("NOPLAYERS"))
		{
			if(S!=null)S.print("Saving players...");
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Saving players...");
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_SESSIONS);e.hasMoreElements();)
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
							} catch(Exception ex) {Log.errOut("MUD",ex);}
						}
					}
				}
			}
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_PLAYERS);e.hasMoreElements();)
				((PlayerLibrary)e.nextElement()).savePlayers();
			if(S!=null)S.println("done");
			Log.sysOut(Thread.currentThread().getName(),"All users saved.");
		}
		if(S!=null)S.print("Saving stats...");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_STATS);e.hasMoreElements();)
			((StatisticsLibrary)e.nextElement()).update();
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Stats saved.");

		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));
		Log.sysOut(Thread.currentThread().getName(),"Notifying all objects of shutdown...");
		if(S!=null)S.print("Notifying all objects of shutdown...");
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Notifying Objects");
		MOB mob=null;
		if(S!=null) mob=S.mob();
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_SHUTDOWN,null);
		Vector<Room> roomSet=new Vector<Room>();
		try
		{
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_MAP);e.hasMoreElements();)
			{
				WorldMap map=((WorldMap)e.nextElement());
				for(Enumeration<Area> a=map.areas();a.hasMoreElements();)
					a.nextElement().setAreaState(Area.State.STOPPED);
			}
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_MAP);e.hasMoreElements();)
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
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Quests");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_QUEST);e.hasMoreElements();)
			e.nextElement().shutdown();


		if(S!=null)S.println("Save thread stopped");
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Session Thread");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_SESSIONS);e.hasMoreElements();)
			e.nextElement().shutdown();

		if(CMSecurity.isSaveFlag("ROOMMOBS")
		||CMSecurity.isSaveFlag("ROOMITEMS")
		||CMSecurity.isSaveFlag("ROOMSHOPS"))
		{
			if(S!=null)S.print("Saving room data...");
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Rejuving the dead");
			CMLib.threads().tickAllTickers(null);
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Map Update");
			for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_MAP);e.hasMoreElements();)
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
					CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Map Update ("+roomCounter+")");
				}
				R=e.nextElement();
				if(R.roomID().length()>0)
					R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
			}
			if(S!=null)S.println("done");
			Log.sysOut(Thread.currentThread().getName(),"Map data saved.");

		}

		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...CM1Servers");
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
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...I3Server");
			I3Server.shutdown();
			i3server=null;
			if(S!=null)S.println("I3Server stopped");
			Log.sysOut(Thread.currentThread().getName(),"I3Server stopped");
		}

		if(imc2server!=null)
		{
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...IMC2Server");
			imc2server.shutdown();
			imc2server=null;
			if(S!=null)S.println("IMC2Server stopped");
			Log.sysOut(Thread.currentThread().getName(),"IMC2Server stopped");
		}

		if(S!=null)S.print("Stopping player Sessions...");
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Stopping sessions");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_SESSIONS);e.hasMoreElements();)
		{
			SessionsList list=((SessionsList)e.nextElement());
			for(Session S2 : list.allIterable())
			{
				if((S!=null)&&(S2==S))
					list.remove(S2);
				else
				{
					CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Stopping session "+S2.getAddress());
					S2.stopSession(true,true,true);
					CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Done stopping session "+S2.getAddress());
				}
				if(S!=null)S.print(".");
			}
		}
		if(S!=null)S.println("All users logged off");
		try{Thread.sleep(3000);}catch(Exception e){/* give sessions a few seconds to inform the map */}
		Log.sysOut(Thread.currentThread().getName(),"All users logged off.");

		if(smtpServerThread!=null)
		{
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...smtp server");
			smtpServerThread.shutdown(S);
			smtpServerThread = null;
			Log.sysOut(Thread.currentThread().getName(),"SMTP Server stopped.");
			if(S!=null)S.println("SMTP Server stopped");
		}

		if(S!=null)S.print("Stopping all threads...");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_STATS);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_THREADS);e.hasMoreElements();)
		{
			CMLibrary lib=e.nextElement();
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...shutting down Service Engine: " + lib.ID());
			lib.shutdown();
		}
		if(S!=null)S.println("done");
		Log.sysOut(Thread.currentThread().getName(),"Map Threads Stopped.");
		
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...Clearing socials, clans, channels");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_SOCIALS);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_CLANS);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_CHANNELS);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_JOURNALS);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_POLLS);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_HELP);e.hasMoreElements();)
			e.nextElement().shutdown();

		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading classes");
		CMClass.shutdown();
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading map");
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_CATALOG);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_MAP);e.hasMoreElements();)
			e.nextElement().shutdown();
		for(Enumeration<CMLibrary> e=CMLib.libraries(CMLib.LIBRARY_PLAYERS);e.hasMoreElements();)
			e.nextElement().shutdown();
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading resources");
		Resources.clearResources();
		Log.sysOut(Thread.currentThread().getName(),"Resources Cleared.");
		if(S!=null)S.println("All resources unloaded");

		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...closing db connections");
		for(int d=0;d<databases.size();d++)
			databases.get(d).killConnections();
		if(S!=null)S.println("Database connections closed");
		Log.sysOut(Thread.currentThread().getName(),"Database connections closed.");

		for(int i=0;i<webServers.size();i++)
		{
			MiniWebServer webServerThread=webServers.get(i);
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down web server "+webServerThread.getName()+"...");
			webServerThread.close();
			Log.sysOut(Thread.currentThread().getName(),"Web server "+webServerThread.getName()+" stopped.");
			if(S!=null)S.println("Web server "+webServerThread.getName()+" stopped");
		}
		webServers.clear();

		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...unloading macros");
		CMLib.lang().clear();
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down" + (keepItDown? "..." : " and restarting..."));

		try{Thread.sleep(500);}catch(Exception i){}
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud shutdown complete.");
		if(S!=null)S.println("CoffeeMud shutdown complete.");
		bringDown=keepItDown;
		CMLib.threads().resumeAll();
		if(!keepItDown)
			if(S!=null)S.println("Restarting...");
		if(S!=null)S.stopSession(true,true,false);
		try{Thread.sleep(500);}catch(Exception i){}
		System.gc();
		System.runFinalization();
		try{Thread.sleep(500);}catch(Exception i){}

		execExternalCommand=externalCommand;
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutdown: you are the special lucky chosen one!");
		for(int m=CMLib.hosts().size()-1;m>=0;m--)
			if(CMLib.hosts().get(m) instanceof Thread)
			{
				try{
					CMLib.killThread((Thread)CMLib.hosts().get(m),100,30);
				} catch(Exception t){}
			}
		if(!keepItDown)
			CMProps.setBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN,false);
	}


	private static void startIntermud3()
	{
		char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		CMProps page=CMProps.instance();
		try
		{
			if(page.getBoolean("RUNI3SERVER")&&(tCode==MAIN_HOST))
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
				case 3: playstate = "Open for public"; break;
				default: playstate = "MudLib Development"; break;
				}
				IMudInterface imud=new IMudInterface(CMProps.getVar(CMProps.SYSTEM_MUDNAME),
													 "CoffeeMud v"+CMProps.getVar(CMProps.SYSTEM_MUDVER),
													 CMLib.mud(0).getPort(),
													 playstate,
													 CMLib.channels().getI3ChannelsList());
				i3server=new I3Server();
				int i3port=page.getInt("I3PORT");
				if(i3port==0) i3port=27766;
				I3Server.start(CMProps.getVar(CMProps.SYSTEM_MUDNAME),i3port,imud);
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
			if(page.getBoolean("RUNCM1SERVER"))
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
			if(page.getBoolean("RUNIMC2CLIENT")&&(tCode==MAIN_HOST))
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
			Log.errOut("IMC2",e.getMessage());
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

	public static int activeThreadCount(ThreadGroup tGroup)
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

	private static int killCount(ThreadGroup tGroup, Thread thisOne)
	{
		int killed=0;

		int ac = tGroup.activeCount();
		Thread tArray[] = new Thread [ac+1];
		tGroup.enumerate(tArray);
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null && tArray[i].isAlive() && (tArray[i] != thisOne))
			{
				CMLib.killThread(tArray[i],500,10);
				killed++;
			}
		}
		return killed;
	}

	private static void threadList(ThreadGroup tGroup)
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
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: Session status "+S.getStatus()+"-"+CMParms.combine(S.previousCMD(),0) + "\n\r");
				}
				else
				if(tArray[i] instanceof Tickable)
				{
					Tickable T=(Tickable)tArray[i];
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+T.ID()+"-"+T.name()+"-"+T.getTickStatus() + "\n\r");
				}
				else
				if((tArray[i] instanceof Tick)
				&&(((Tick)tArray[i]).lastClient!=null)
				&&(((Tick)tArray[i]).lastClient.clientObject!=null))
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName()+" "+((Tick)tArray[i]).lastClient.clientObject.ID()+"-"+((Tick)tArray[i]).lastClient.clientObject.name()+"-"+((Tick)tArray[i]).lastClient.clientObject.getTickStatus() + "\n\r");
				else
					Log.sysOut(Thread.currentThread().getName(), "-->Thread: "+tArray[i].getName() + "\n\r");
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
		private static int	 grpid=0;
		private String  	   name=null;
		private String  	   iniFile=null;
		private String  	   logName=null;
		private char		 threadCode=MAIN_HOST;
		
		public HostGroup(ThreadGroup G, String mudName, String iniFileName)
		{
			super(G,"HOST"+grpid);
			synchronized("HostGroupInit".intern()) {
				logName="mud"+((grpid>0)?("."+grpid):"");
				grpid++;
				iniFile=iniFileName;
				name=mudName;
				setDaemon(true);
				threadCode=G.getName().charAt(0);
			}
		}

		public void run()
		{
			new CMLib(); // initialize the lib
			new CMClass(); // initialize the classes
			Log.shareWith(MudHost.MAIN_HOST);
			
			// wait for ini to be loaded, and for other matters
			if(threadCode!=MAIN_HOST) {
				while((CMLib.library(MAIN_HOST,CMLib.LIBRARY_INTERMUD)==null)&&(!MUD.bringDown)) {
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
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"A terminal error has occured!");
				return;
			}
			page.resetSystemVars();
			CMProps.setBoolVar(CMProps.SYSTEMB_MUDSTARTED,false);
			
			if(threadCode!=MAIN_HOST)
			{
				if(CMath.isInteger(page.getPrivateStr("NUMLOGS")))
				{
					Log.newInstance();
					Log.instance().startLogFiles(logName,page.getInt("NUMLOGS"));
					Log.instance().setLogOutput(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("WRNMSGS"),page.getStr("DBGMSGS"),page.getStr("HLPMSGS"),page.getStr("KILMSGS"),page.getStr("CBTMSGS"));
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
			CMLib.registerLibrary(new DBInterface(currentDBconnector,CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_PRIVATERESOURCES).toUpperCase(),true)));
			CMProps.setVar(CMProps.SYSTEM_MUDVER,HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
			
			// an arbitrary dividing line. After threadCode 0 
			if(threadCode==MAIN_HOST) {
				CMLib.registerLibrary(new ServiceEngine());
				CMLib.registerLibrary(new IMudClient());
			} else {
				CMLib.registerLibrary(CMLib.library(MAIN_HOST,CMLib.LIBRARY_THREADS));
				CMLib.registerLibrary(CMLib.library(MAIN_HOST,CMLib.LIBRARY_INTERMUD));
			}
			CMProps.setVar(CMProps.SYSTEM_INIPATH,iniFile,false);
			CMProps.setUpLowVar(CMProps.SYSTEM_MUDNAME,name.replace('\'','`'));
			try
			{
				isOK = true;
				CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting");
				CMProps.setVar(CMProps.SYSTEM_MUDBINDADDRESS,page.getStr("BIND"));
				CMProps.setIntVar(CMProps.SYSTEMI_MUDBACKLOG,page.getInt("BACKLOG"));

				if(MUD.isOK)
				{
					String ports=page.getProperty("PORT");
					int pdex=ports.indexOf(',');
					while(pdex>0)
					{
						MUD mud=new MUD("MUD@"+ports.substring(0,pdex));
						mud.acceptConns=false;
						mud.port=CMath.s_int(ports.substring(0,pdex));
						ports=ports.substring(pdex+1);
						mud.start();
						CMLib.hosts().add(mud);
						pdex=ports.indexOf(',');
					}
					MUD mud=new MUD("MUD@"+ports);
					mud.acceptConns=false;
					mud.port=CMath.s_int(ports);
					mud.start();
					CMLib.hosts().add(mud);
				}

				StringBuffer str=new StringBuffer("");
				for(int m=0;m<CMLib.hosts().size();m++)
				{
					MudHost mud=CMLib.hosts().get(m);
					str.append(" "+mud.getPort());
				}
				CMProps.setVar(CMProps.SYSTEM_MUDPORTS,str.toString());

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN))
							MUD.globalShutdown(null,true,null);
					}
				});

				if(initHost(Thread.currentThread()))
				{
					Thread joinable=null;
					for(int i=0;i<CMLib.hosts().size();i++)
						if(CMLib.hosts().get(i) instanceof Thread)
						{
							joinable=(Thread)CMLib.hosts().get(i);
							break;
						}
					if(joinable!=null)
						joinable.join();
					else
						System.exit(-1);
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
		Vector<String> iniFiles=new Vector<String>();
		if(a.length>0)
		{
			for(int i=0;i<a.length;i++)
				nameID+=" "+a[i];
			nameID=nameID.trim();
			Vector<String> V=CMParms.paramParse(nameID);
			for(int v=0;v<V.size();v++)
			{
				String s=V.elementAt(v);
				if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
				{
					iniFiles.addElement(s.substring(5));
					V.removeElementAt(v);
					v--;
				}
			}
			nameID=CMParms.combine(V,0);
		}
		new CMLib(); // initialize this threads libs
		
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
		if(nameID.equalsIgnoreCase( "TheRealCoffeeMudCopyright2000-2013ByBoZimmerman" ))
			nameID="CoffeeMud";
		String iniFile=iniFiles.firstElement();
		CMProps page=CMProps.loadPropPage("//"+iniFile);
		if ((page==null)||(!page.isLoaded()))
		{
			Log.instance().startLogFiles("mud",1);
			Log.instance().setLogOutput("BOTH","BOTH","BOTH","BOTH","BOTH","BOTH","BOTH");
			Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
			System.out.println("MUD/ERROR: Unable to read ini file: '"+iniFile+"'.");
			CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"A terminal error has occured!");
			System.exit(-1);
			return;
		}
		Log.shareWith(MudHost.MAIN_HOST);
		Log.instance().startLogFiles("mud",page.getInt("NUMLOGS"));
		Log.instance().setLogOutput(page.getStr("SYSMSGS"),page.getStr("ERRMSGS"),page.getStr("WRNMSGS"),page.getStr("DBGMSGS"),page.getStr("HLPMSGS"),page.getStr("KILMSGS"),page.getStr("CBTMSGS"));
		while(!bringDown)
		{
			System.out.println();
			Log.sysOut(Thread.currentThread().getName(),"CoffeeMud v"+HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR);
			Log.sysOut(Thread.currentThread().getName(),"(C) 2000-2013 Bo Zimmerman");
			Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
			HostGroup joinable=null;
			CMLib.hosts().clear();
			for(int i=0;i<iniFiles.size();i++)
			{
				iniFile=iniFiles.elementAt(i);
				ThreadGroup G=new ThreadGroup(i+"-MUD");
				HostGroup H=new HostGroup(G,nameID,iniFile);
				H.start();
				if(joinable==null) joinable=H;
			}
			if(joinable!=null)
				try{joinable.join();}catch(Exception e){e.printStackTrace(); Log.errOut(Thread.currentThread().getName(),e); }
			System.gc();
			try{Thread.sleep(1000);}catch(Exception e){}
			System.runFinalization();
			try{Thread.sleep(1000);}catch(Exception e){}

			if(activeThreadCount(Thread.currentThread().getThreadGroup())>1)
			{
				try{ Thread.sleep(1000);}catch(Exception e){}
				killCount(Thread.currentThread().getThreadGroup(),Thread.currentThread());
				try{ Thread.sleep(1000);}catch(Exception e){}
				if(activeThreadCount(Thread.currentThread().getThreadGroup())>1)
				{
					Log.sysOut(Thread.currentThread().getName(),"WARNING: " + activeThreadCount(Thread.currentThread().getThreadGroup()) +" other thread(s) are still active!");
					threadList(Thread.currentThread().getThreadGroup());
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
