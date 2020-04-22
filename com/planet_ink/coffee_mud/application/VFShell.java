package com.planet_ink.coffee_mud.application;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.coffee_mud.Commands.Shell;
import com.planet_ink.coffee_mud.Common.FakeSession;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionFilter;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Libraries.Clans;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMFile;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.Resources;
import com.planet_ink.coffee_mud.core.database.DBConnection;
import com.planet_ink.coffee_mud.core.database.DBConnector;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.MudHost;
import com.planet_ink.coffee_mud.core.interfaces.Physical;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;
/*
Copyright 2007-2020 Bo Zimmerman

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
public class VFShell
{
	public static void main(final String[] args)
	{
		final ThreadGroup g=new ThreadGroup("0");
		final Thread t=new Thread(g,new Runnable()
		{
			@Override
			public void run()
			{
				CMProps page=null;

				CMLib.initialize(); // forces this thread to HAVE a library
				Log.instance().configureLogFile(null,1);
				Log.instance().configureLog(Log.Type.info, "ON");
				Log.instance().configureLog(Log.Type.error, "ON");
				Log.instance().configureLog(Log.Type.warning, "ON");
				Log.instance().configureLog(Log.Type.debug, "ON");
				Log.instance().configureLog(Log.Type.help, "ON");
				Log.instance().configureLog(Log.Type.kills, "ON");
				Log.instance().configureLog(Log.Type.combat, "ON");
				Log.instance().configureLog(Log.Type.access, "ON");
				String nameID="";
				String iniFile="coffeemud.ini";
				if(args.length>0)
				{
					for (final String element : args)
						nameID+=" "+element;
					nameID=nameID.trim();
					final List<String> V=CMParms.cleanParameterList(nameID);
					for(int v=0;v<V.size();v++)
					{
						final String s=V.get(v);
						if(s.toUpperCase().startsWith("BOOT=")&&(s.length()>5))
						{
							iniFile=s.substring(5);
							V.remove(v);
							v--;
						}
					}
				}
				page=CMProps.loadPropPage(iniFile);
				if ((page==null)||(!page.isLoaded()))
				{
					System.out.println("ERROR: Unable to read ini file: '"+iniFile+"'.");
					System.exit(-1);
					return;
				}

				DBConnector currentDBconnector=null;
				final String dbClass=page.getStr("DBCLASS");
				if(dbClass.length()>0)
				{
					final String dbService=page.getStr("DBSERVICE");
					final String dbUser=page.getStr("DBUSER");
					final String dbPass=page.getStr("DBPASS");
					final int dbConns=page.getInt("DBCONNECTIONS");
					final int dbPingIntMins=page.getInt("DBPINGINTERVALMINS");
					if(dbConns == 0)
					{
						Log.errOut(Thread.currentThread().getName(),"Fatal error: DBCONNECTIONS in INI file is "+dbConns);
						System.exit(-1);
					}
					final boolean dbReuse=page.getBoolean("DBREUSE");
					final boolean dbTransact=page.getBoolean("DBTRANSACT");
					final Map<String,String> dbParms=CMParms.parseEQParms(page.getStr("DBPARMS"),CMParms.spaceDelimiter,false);
					final boolean useQue=!CMSecurity.isDisabled(CMSecurity.DisFlag.DBERRORQUE);
					final boolean useQueStart=!CMSecurity.isDisabled(CMSecurity.DisFlag.DBERRORQUESTART);
					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: connecting to database");
					currentDBconnector=new DBConnector(dbClass,dbService,dbUser,dbPass,dbParms,dbConns,dbPingIntMins,dbReuse,dbTransact,useQue,useQueStart);
					currentDBconnector.reconnect();
					CMLib.registerLibrary(new DBInterface(currentDBconnector,CMProps.getPrivateSubSet("DB.*")));

					final DBConnection DBTEST=currentDBconnector.DBFetch();
					if(DBTEST!=null)
						currentDBconnector.DBDone(DBTEST);
					if((DBTEST!=null)&&(currentDBconnector.amIOk())&&(CMLib.database().isConnected()))
					{
						Log.sysOut(Thread.currentThread().getName(),"Connected to "+currentDBconnector.service());
					}
					else
					{
						final String DBerrors=currentDBconnector.errorStatus().toString();
						Log.errOut(Thread.currentThread().getName(),"Fatal database error: "+DBerrors);
						return;
					}
				}
				if((CMLib.database()==null)||(currentDBconnector==null))
				{
					Log.errOut(Thread.currentThread().getName(),"No registered database!");
					System.exit(-1);
					return;
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
					System.exit(-1);
				}

				CMClass.initialize();
				Resources.initialize();
				CMSecurity.instance();
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.AutoTitles());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMJournals());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.BeanCounter());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CharCreation());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.Clans());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMAbleMap());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMAbleParms());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMCatalog());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMChannels());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMColor());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMMap());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CoffeeMaker());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.MUDPercolator());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.EnglishParser());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CoffeeUtensils());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMEncoder());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMGenEditor());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CoffeeFilter());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CMLister());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.Dice());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.DirtyLanguage());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.Sense());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.XMLManager());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.MUDZapper());
				CMLib.registerLibrary(new com.planet_ink.coffee_mud.Libraries.CoffeeTime());
				CMClass.addClass(CMObjectType.COMMON, new com.planet_ink.coffee_mud.Common.DefaultCharState());
				CMClass.addClass(CMObjectType.COMMON, new com.planet_ink.coffee_mud.Common.DefaultCharStats());
				CMClass.addClass(CMObjectType.COMMON, new com.planet_ink.coffee_mud.Common.DefaultPhyStats());

				final MOB fakeMob = new StdMOB();

				final Session session = new Session()
				{
					protected OutputStream bout=System.out;
					protected MOB mob = fakeMob;

					@Override
					public String ID()
					{
						return "FakeSession";
					}

					@Override
					public String name()
					{
						return ID();
					}

					@Override
					public CMObject newInstance()
					{
						try
						{
							return getClass().newInstance();
						}
						catch (final Exception e)
						{
							return new FakeSession();
						}
					}

					@Override
					public CMObject copyOf()
					{
						try
						{
							return (CMObject) this.clone();
						}
						catch (final Exception e)
						{
							return newInstance();
						}
					}

					@Override
					public int compareTo(final CMObject o)
					{
						return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
					}

					@Override
					public void initializeSession(final Socket s, final String groupName, final String introTextStr)
					{
					}

					@Override
					public String getGroupName()
					{
						return Thread.currentThread().getThreadGroup().getName();
					}

					@Override
					public void setGroupName(final String group)
					{
					}

					@Override
					public long getStartTime()
					{
						return 0;
					}

					@Override
					public int getGroupID()
					{
						return Thread.currentThread().getThreadGroup().getName().charAt(0);
					}

					@Override
					public boolean isLockedUpWriting()
					{
						return false;
					}

					@Override
					public void initializeClass()
					{
					}

					@Override
					public void run()
					{
					}

					@Override
					public boolean isRunning()
					{
						return false;
					}

					@Override
					public boolean isPendingLogin(final String otherLoginName)
					{
						return false;
					}

					@Override
					public void logout(final boolean b1)
					{
					}

					@Override
					public String getTerminalType()
					{
						return "Fake";
					}

					@Override
					public void negotiateTelnetMode(final int code)
					{
					}

					@Override
					public boolean isAllowedMxp(final String tag)
					{
						return false;
					}

					@Override
					public boolean isFake()
					{
						return false;
					}

					@Override
					public void setIdleTimers()
					{
					}

					@Override
					public boolean sendGMCPEvent(final String eventName, final String json)
					{
						return false;
					}

					@Override
					public void setFakeInput(final String input)
					{
					}

					@Override
					public boolean isAllowedMcp(final String packageName, final float version)
					{
						return false;
					}

					@Override
					public boolean sendMcpCommand(final String packageCommand, final String parms)
					{
						return false;
					}

					@Override
					public String[] getColorCodes()
					{
						return new String[255];
					}

					@Override
					public boolean addSessionFilter(final SessionFilter filter)
					{
						return false;
					}

					@Override
					public void onlyPrint(final String msg, final boolean noCache)
					{
						if (bout != null)
						{
							synchronized (bout)
							{
								try
								{
									bout.write(CMStrings.removeColors(msg).replaceAll("\n\r", "\n").getBytes());
								}
								catch (final Exception e)
								{
									Log.errOut("FakeSession", e);
								}
							}
						}
					}

					@Override
					public void onlyPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void rawOut(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void rawPrintln(final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void rawPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void safeRawPrintln(final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void safeRawPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void stdPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void stdPrint(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void stdPrintln(final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void stdPrintln(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void rawCharsOut(final char[] c)
					{
						onlyPrint(new String(c), false);
					}

					@Override
					public void print(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void promptPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void print(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void println(final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void println(final Physical Source, final Environmental Target, final Environmental Tool, final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void wraplessPrintln(final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void wraplessPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void colorOnlyPrintln(final String msg, final boolean noCache)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void colorOnlyPrint(final String msg, final boolean noCache)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void colorOnlyPrintln(final String msg)
					{
						onlyPrint(msg + "\n", false);
					}

					@Override
					public void colorOnlyPrint(final String msg)
					{
						onlyPrint(msg, false);
					}

					@Override
					public void setPromptFlag(final boolean truefalse)
					{
					}

					@Override
					public char hotkey(final long maxWait)
					{
						return ' ';
					}

					@Override
					public String prompt(final String Message, final String Default)
					{
						onlyPrint(Message, false);
						final String msg = readlineContinue();
						if (msg.length() == 0)
							return Default;
						return msg;
					}

					@Override
					public void prompt(final InputCallback callBack)
					{
						callBack.showPrompt();
						callBack.setInput(readlineContinue());
						callBack.callBack();
					}

					@Override
					public String prompt(final String Message, final String Default, final long maxTime)
					{
						return prompt(Message, Default);
					}

					@Override
					public String prompt(final String Message)
					{
						return prompt(Message, "");
					}

					@Override
					public String prompt(final String Message, final long maxTime)
					{
						return prompt(Message, "");
					}

					@Override
					public boolean confirm(final String Message, String Default)
					{
						if (Default.toUpperCase().startsWith("T"))
							Default = "Y";
						final String YN = choose(Message, "YN", Default, 0);
						return (YN.equals("Y")) ? true : false;
					}

					@Override
					public boolean confirm(final String Message, final String Default, final long maxTime)
					{
						return confirm(Message, Default, 0);
					}

					@Override
					public String choose(final String Message, final String Choices, final String Default)
					{
						onlyPrint(Message, false);
						final String msg = readlineContinue();
						if (msg.length() == 0)
							return Default;
						if (Choices.toUpperCase().indexOf(msg.toUpperCase().trim()) >= 0)
							return msg.toUpperCase().trim();
						return Default;
					}

					@Override
					public String choose(final String Message, final String Choices, final String Default, final long maxTime, final List<String> paramsOut) throws IOException
					{
						return choose(Message, Choices, Default);
					}

					@Override
					public String choose(final String Message, final String Choices, final String Default, final long maxTime)
					{
						return choose(Message, Choices, Default);
					}

					@Override
					public String blockingIn(final long timeoutMillis, final boolean filter)
					{
						return readlineContinue();
					}

					@Override
					public String readlineContinue()
					{
						try
						{
							return new BufferedReader(new InputStreamReader(System.in)).readLine();
						}
						catch (final Exception e)
						{
							System.exit(-1);
							return "";
						}
					}

					@Override
					public void setBeingSnoopedBy(final Session session, final boolean onOff)
					{
					}

					@Override
					public boolean isBeingSnoopedBy(final Session S)
					{
						return S == this;
					}

					@Override
					public int snoopSuspension(final int x)
					{
						return 0;
					}

					@Override
					public void stopSession(final boolean t1, final boolean t2, final boolean t3)
					{
					}

					@Override
					public boolean isStopped()
					{
						return false;
					}

					@Override
					public boolean isAfk()
					{
						return false;
					}

					@Override
					public void setAfkFlag(final boolean truefalse)
					{
					}

					@Override
					public String getAfkMessage()
					{
						return "";
					}

					@Override
					public void setAFKMessage(final String str)
					{
					}

					@Override
					public List<String> getPreviousCMD()
					{
						return new Vector<String>();
					}

					@Override
					public MOB mob()
					{
						return mob;
					}

					@Override
					public void setMob(final MOB newmob)
					{
						mob = newmob;
					}

					@Override
					public void setAccount(final PlayerAccount account)
					{
					}

					@Override
					public void setCurrentColor(final ColorState newcolor)
					{
					}

					@Override
					public void pushMarkedColor(final ColorState newcolor)
					{
					}

					@Override
					public void setLastColor(final ColorState newColor)
					{
					}

					@Override
					public ColorState getCurrentColor()
					{
						return CMLib.color().getNormalColor();
					}

					@Override
					public ColorState getLastColor()
					{
						return CMLib.color().getNormalColor();
					}

					@Override
					public ColorState popMarkedColor()
					{
						return CMLib.color().getNormalColor();
					}

					@Override
					public int getWrap()
					{
						return 80;
					}

					@Override
					public String getAddress()
					{
						return "";
					}

					@Override
					public SessionStatus getStatus()
					{
						return SessionStatus.MAINLOOP;
					}

					@Override
					public void setStatus(final SessionStatus newStatus)
					{
					}

					@Override
					public boolean isWaitingForInput()
					{
						return false;
					}

					@Override
					public long getTotalMillis()
					{
						return 0;
					}

					@Override
					public long getTotalTicks()
					{
						return 0;
					}

					@Override
					public long getIdleMillis()
					{
						return 0;
					}

					@Override
					public long getMillisOnline()
					{
						return 0;
					}

					@Override
					public long getLastPKFight()
					{
						return 0;
					}

					@Override
					public void setLastPKFight()
					{
					}

					@Override
					public long getLastNPCFight()
					{
						return 0;
					}

					@Override
					public void setLastNPCFight()
					{
					}

					@Override
					public long getInputLoopTime()
					{
						return 0;
					}

					@Override
					public void setInputLoopTime()
					{
					}

					@Override
					public List<String> getLastMsgs()
					{
						return new Vector<String>();
					}

					@Override
					public void setServerTelnetMode(final int telnetCode, final boolean onOff)
					{
					}

					@Override
					public boolean getServerTelnetMode(final int telnetCode)
					{
						return false;
					}

					@Override
					public void setClientTelnetMode(final int telnetCode, final boolean onOff)
					{
					}

					@Override
					public boolean getClientTelnetMode(final int telnetCode)
					{
						return false;
					}

					@Override
					public void changeTelnetMode(final int telnetCode, final boolean onOff)
					{
					}

					@Override
					public void initTelnetMode(final int mobbitmap)
					{
					}

					@Override
					public int getSaveStatIndex()
					{
						return 0;
					}

					@Override
					public String getStat(final String code)
					{
						return null;
					}

					@Override
					public String[] getStatCodes()
					{
						return new String[0];
					}

					@Override
					public boolean isStat(final String code)
					{
						return false;
					}

					@Override
					public void setStat(final String code, final String val)
					{
					}

					@Override
					public long activeTimeMillis()
					{
						return 0;
					}

					@Override
					public boolean autoLogin(final String name, final String password)
					{
						return false;
					}
				};
				fakeMob.setSession(session);
				fakeMob.setSoulMate(fakeMob);
				fakeMob.setAttribute(MOB.Attrib.SYSOPMSGS, true);
				final Shell shell = new Shell();
				String command="";
				String pwd="";
				System.out.println("CoffeeMud VFShell started. Use 'exit' to quit.");
				System.out.println("");
				while((command!=null)&&(!command.equalsIgnoreCase("exit")))
				{
					try
					{
						pwd=(String)shell.executeInternal(fakeMob, 0, ".");
						System.out.print(pwd+"> ");
						command=new BufferedReader(new InputStreamReader(System.in)).readLine();
						if((command!=null)&&(command.trim().length()>0)&&(!command.trim().toLowerCase().startsWith("exit")))
							shell.execute(fakeMob, CMParms.parse("SHELL "+command), 0);
					}
					catch (final Exception e)
					{
						e.printStackTrace();
						break;
					}
				}

				currentDBconnector.killConnections();
			}
		});
		try
		{
			t.start();
			t.join();
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
