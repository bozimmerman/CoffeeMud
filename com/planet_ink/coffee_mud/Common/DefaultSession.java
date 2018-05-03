package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginResult;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginSession;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.jcraft.jzlib.*;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.*;
import java.net.*;
import java.nio.charset.Charset;

/*
   Copyright 2005-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class DefaultSession implements Session
{
	protected static final int		SOTIMEOUT		= 300;
	protected static final int		PINGTIMEOUT  	= 30000;
	protected static final int		MSDPPINGINTERVAL= 1000;
	protected static final char[]	PINGCHARS		= {0};

	protected final Set<Integer>		telnetSupportSet= new HashSet<Integer>();
	protected final Set<String>			mxpSupportSet	= new HashSet<String>();
	protected final Map<String,String>	mxpVersionInfo  = new Hashtable<String,String>();
	protected final Map<Object, Object> msdpReportables = new TreeMap<Object,Object>();
	protected final Map<String, Double> gmcpSupports	= new TreeMap<String,Double>();
	protected final Map<String, Long> 	gmcpPings		= new TreeMap<String,Long>();
	
	protected final String[]			mcpKey			= new String[1];
	protected final Map<String,String>	mcpKeyPairs		= new TreeMap<String,String>();
	protected final boolean		 		mcpDisabled		= CMSecurity.isDisabled(DisFlag.MCP);
	protected final Map<String,float[]>	mcpSupported	= new TreeMap<String,float[]>();
	protected final AtomicBoolean 		sockObj 		= new AtomicBoolean(false); 
	
	private static final String		TIMEOUT_MSG		= "Timed Out.";

	private volatile Thread  runThread 			 = null;
	private volatile Thread	 writeThread 		 = null;
	protected String		 groupName			 = null;
	protected SessionStatus  status 			 = SessionStatus.HANDSHAKE_OPEN;
	protected long 			 lastStateChangeMs	 = System.currentTimeMillis();
	protected int   		 snoopSuspensionStack= 0;
	protected Socket 	 	 sock				 = null;
	protected SesInputStream charWriter;
	protected int			 inMaxBytesPerChar	 = 1;
	protected BufferedReader in;
	protected PrintWriter	 out;
	protected InputStream    rawin;
	protected OutputStream   rawout;
	protected MOB   		 mob;
	protected PlayerAccount  acct				 = null;
	protected boolean   	 killFlag			 = false;
	protected boolean   	 needPrompt			 = false;
	protected boolean   	 afkFlag			 = false;
	protected String		 afkMessage			 = null;
	protected StringBuffer   input				 = new StringBuffer("");
	protected StringBuffer   fakeInput			 = null;
	protected boolean   	 waiting			 = false;
	protected List<String>   previousCmd		 = new Vector<String>();
	protected String[]  	 clookup			 = null;
	protected String		 lastColorStr		 = "";
	protected String		 lastStr			 = null;
	protected int			 spamStack			 = 0;
	protected List<Session>	 snoops				 = new Vector<Session>();
	protected List<String>	 prevMsgs			 = new Vector<String>();
	protected StringBuffer	 curPrevMsg			 = null;
	protected boolean		 lastWasCR			 = false;
	protected boolean		 lastWasLF			 = false;
	protected boolean		 suspendCommandLine	 = false;
	protected boolean[] 	 serverTelnetCodes	 = new boolean[256];
	protected boolean[]		 clientTelnetCodes	 = new boolean[256];
	protected String		 terminalType		 = "UNKNOWN";
	protected int			 terminalWidth		 = -1;
	protected int			 terminalHeight		 = -1;
	protected long			 writeStartTime		 = 0;
	protected boolean		 bNextByteIs255		 = false;
	protected boolean		 connectionComplete	 = false;
	protected ReentrantLock  writeLock 			 = new ReentrantLock(true);
	protected LoginSession	 loginSession 		 = null;
	protected boolean[]		 loggingOutObj		 = new boolean[]{false};

	protected byte[]		 promptSuffix		 = new byte[0];
	protected ColorState	 currentColor		 = null;
	protected ColorState	 lastColor			 = null;
	protected long			 lastStart			 = System.currentTimeMillis();
	protected long			 lastStop			 = System.currentTimeMillis();
	protected long			 lastLoopTop		 = System.currentTimeMillis();
	protected long			 nextMsdpPing		 = System.currentTimeMillis();
	protected long			 userLoginTime		 = System.currentTimeMillis();
	protected long			 onlineTime			 = System.currentTimeMillis();
	protected long			 activeMillis		 = 0;
	protected long			 lastPKFight		 = 0;
	protected long			 lastNPCFight		 = 0;
	protected long			 lastBlahCheck		 = 0;
	protected long			 milliTotal			 = 0;
	protected long			 tickTotal			 = 0;
	protected long			 lastKeystroke		 = 0;
	protected long			 lastIACIn		 	 = System.currentTimeMillis();
	protected long			 promptLastShown	 = 0;
	protected char 			 threadGroupChar	 = '\0';
	protected volatile long  lastWriteTime		 = System.currentTimeMillis();
	protected boolean		 debugStrInput		 = true;
	protected boolean		 debugBinOutput		 = false;
	protected boolean		 debugBinInput		 = false;
	protected StringBuffer   debugBinInputBuf	 = new StringBuffer("");

	protected volatile InputCallback inputCallback  = null;

	@Override
	public String ID()
	{
		return "DefaultSession";
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
			return new DefaultSession();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public boolean isFake()
	{
		return false;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O = this.clone();
			return (CMObject) O;
		}
		catch (final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	public DefaultSession()
	{
		threadGroupChar=Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	@Override
	public void setStatus(SessionStatus newStatus)
	{
		synchronized(status)
		{
			if(status!=newStatus)
			{
				status=newStatus;
				lastStateChangeMs=System.currentTimeMillis();
			}
		}
	}

	@Override
	public long getStartTime()
	{
		return this.lastStart;
	}
	
	@Override
	public int getGroupID()
	{
		return this.threadGroupChar;
	}
	
	@Override 
	public boolean isAllowedMcp(String packageName, float version)
	{
		float[] chk = mcpSupported.get(packageName);
		if((chk == null)||(version < chk[0])||(version>chk[1])||(mcpKey[0] == null))
		{
			return false;
		}
		return true;
	}

	@Override 
	public boolean sendMcpCommand(String packageCommand, String parms)
	{
		if(mcpKey[0] != null)
		{
			rawPrintln("#$#"+packageCommand+" "+mcpKey[0]+" "+parms);
			return true;
		}
		return false;
	}
	
	@Override
	public void initializeSession(final Socket s, final String groupName, final String introTextStr)
	{
		this.groupName=groupName;
		sock=s;
		promptSuffix = CMProps.getPromptSuffix();
		currentColor = CMLib.color().getNormalColor();
		lastColor = CMLib.color().getNormalColor();
		try
		{
			setStatus(SessionStatus.HANDSHAKE_OPEN);
			debugStrInput = CMSecurity.isDebugging(CMSecurity.DbgFlag.INPUT);
			debugBinOutput = CMSecurity.isDebugging(CMSecurity.DbgFlag.BINOUT);
			debugBinInput = CMSecurity.isDebugging(CMSecurity.DbgFlag.BININ);
			if(debugBinInput)
			{
				CMLib.threads().startTickDown(new Tickable()
				{
					@Override
					public String ID()
					{
						return "SessionTicker";
					}

					@Override
					public CMObject newInstance()
					{
						return null;
					}

					@Override
					public CMObject copyOf()
					{
						return null;
					}

					@Override
					public void initializeClass()
					{
					}

					@Override
					public int compareTo(CMObject o)
					{
						return 0;
					}

					@Override
					public String name()
					{
						return ID();
					}

					@Override
					public int getTickStatus()
					{
						return 0;
					}

					@Override
					public boolean tick(Tickable ticking, int tickID)
					{
						if(debugBinInputBuf.length()>0)
						{
							Log.sysOut("BINPUT: "+(mob==null?"":mob.Name())+": '"+debugBinInputBuf.toString()+"'");
							debugBinInputBuf.setLength(0);
						}
						return !killFlag;
					} 
				}, 0, 100, 1);
			}

			if((sock == null)||(!sock.isConnected()))
			{
				rawout=new BufferedOutputStream(new ByteArrayOutputStream());
				rawin=new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
				in=new BufferedReader(new InputStreamReader(rawin));
				out=new PrintWriter(new OutputStreamWriter(rawout));
				return;
			}

			sock.setSoTimeout(SOTIMEOUT);
			rawout=new BufferedOutputStream(sock.getOutputStream());
			rawin=new BufferedInputStream(sock.getInputStream());
			if(!mcpDisabled)
			{
				rawBytesOut(rawout,("\n\r#$#mcp version: 2.1 to: 2.1\n\r").getBytes(CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
			}
			rawBytesOut(rawout,("\n\rConnecting to "+CMProps.getVar(CMProps.Str.MUDNAME)+"...\n\r").getBytes("US-ASCII"));
			rawout.flush();

			setServerTelnetMode(TELNET_ANSI,true);
			setClientTelnetMode(TELNET_ANSI,true);
			setClientTelnetMode(TELNET_TERMTYPE,true);
			changeTelnetMode(rawout,TELNET_TERMTYPE,true);
			negotiateTelnetMode(rawout,TELNET_TERMTYPE);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MCCP))
				changeTelnetMode(rawout,TELNET_COMPRESS2,true);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP))
				changeTelnetMode(rawout,TELNET_MXP,true);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.GMCP))
				changeTelnetMode(rawout,TELNET_GMCP,true);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP))
				changeTelnetMode(rawout,TELNET_MSP,true);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSDP))
				changeTelnetMode(rawout,TELNET_MSDP,true);
			//changeTelnetMode(rawout,TELNET_SUPRESS_GO_AHEAD,true);
			changeTelnetMode(rawout,TELNET_NAWS,true);
			//changeTelnetMode(rawout,TELNET_BINARY,true);
			if(mightSupportTelnetMode(TELNET_GA))
				rawBytesOut(rawout,TELNETGABYTES);
			rawout.flush();

			final Charset charSet=Charset.forName(CMProps.getVar(CMProps.Str.CHARSETINPUT));
			inMaxBytesPerChar=(int)Math.round(Math.ceil(charSet.newEncoder().maxBytesPerChar()));
			charWriter=new SesInputStream(inMaxBytesPerChar);
			in=new BufferedReader(new InputStreamReader(charWriter,charSet));
			out=new PrintWriter(new OutputStreamWriter(rawout,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));

			prompt(new TickingCallback(250)
			{
				private final long firstIACIn=lastIACIn;

				@Override 
				public boolean tick(int counter)
				{
					try
					{
						if(out!=null)
						{
							out.flush();
							rawout.flush();
						}
						else
						{
							killFlag=true;
							return false;
						}
						switch(status)
						{
						case HANDSHAKE_OPEN:
						{
							if((!terminalType.equals("ANSI"))&&(getClientTelnetMode(TELNET_ECHO)))
								changeTelnetModeBackwards(rawout,TELNET_ECHO,false);
							rawout.flush();
							setStatus(SessionStatus.HANDSHAKE_MCCP);
							break;
						}
						case HANDSHAKE_MCCP:
						{
							if(((lastIACIn>firstIACIn)&&((System.currentTimeMillis()-lastIACIn)>500))
							||((System.currentTimeMillis()-lastIACIn)>5000))
							{
								if(getClientTelnetMode(TELNET_COMPRESS2))
								{
									negotiateTelnetMode(rawout,TELNET_COMPRESS2);
									rawout.flush();
									if(getClientTelnetMode(TELNET_COMPRESS2))
									{
										final ZOutputStream zOut=new ZOutputStream(rawout, JZlib.Z_DEFAULT_COMPRESSION);
										rawout=zOut;
										zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
										out = new PrintWriter(new OutputStreamWriter(zOut,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
										if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
											Log.debugOut("MCCP compression started");
									}
								}
								setStatus(SessionStatus.HANDSHAKE_MXP);
							}
							break;
						}
						case HANDSHAKE_MXP:
						{
							if(!getClientTelnetMode(Session.TELNET_MXP))
								setStatus(SessionStatus.HANDSHAKE_DONE);
							else
							{
								rawOut("\n\033[6z\n\033[6z<SUPPORT IMAGE IMAGE.URL>\n");
								rawout.flush();
								rawOut("\n\033[6z\n\033[6z<SUPPORT>\n");
								rawout.flush();
								setStatus(SessionStatus.HANDSHAKE_MXPPAUSE);
							}
							break;
						}
						case HANDSHAKE_MXPPAUSE:
						{
							if(((System.currentTimeMillis()-lastStateChangeMs)>2000)
							||(mxpSupportSet.contains("+IMAGE.URL")||mxpSupportSet.contains("+IMAGE")||mxpSupportSet.contains("-IMAGE.URL")))
								setStatus(SessionStatus.HANDSHAKE_DONE);
							break;
						}
						case HANDSHAKE_DONE:
						{
							if(introTextStr!=null)
								print(introTextStr);
							if(out!=null)
							{
								out.flush();
								rawout.flush();
								if((getClientTelnetMode(Session.TELNET_MXP))
								&&((mxpSupportSet.contains("+IMAGE.URL"))
									||((mxpSupportSet.contains("+IMAGE"))&&(!mxpSupportSet.contains("-IMAGE.URL")))))
								{
									// also the intro page
									final String[] paths=CMLib.protocol().mxpImagePath("intro.jpg");
									if(paths[0].length()>0)
									{
										final CMFile introDir=new CMFile("/web/pub/images/mxp",null,CMFile.FLAG_FORCEALLOW);
										String introFilename=paths[1];
										if(introDir.isDirectory())
										{
											final CMFile[] files=introDir.listFiles();
											final Vector<String> choices=new Vector<String>();
											for (final CMFile file : files)
											{
												if(file.getName().toLowerCase().startsWith("intro")
												&&file.getName().toLowerCase().endsWith(".jpg"))
													choices.addElement(file.getName());
											}
											if(choices.size()>0)
												introFilename=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
										}
										println("\n\r\n\r\n\r^<IMAGE '"+introFilename+"' URL='"+paths[0]+"' H=400 W=400^>\n\r\n\r");
										if(out!=null)
										{
											out.flush();
											rawout.flush();
										}
									}
								}
							}
						}
							//$FALL-THROUGH$
						default:
							connectionComplete=true;
							status=SessionStatus.LOGIN;
							if(collectedInput.length()>0)
								fakeInput=new StringBuffer(collectedInput.toString());
							return false;
						}
						return (out!=null);
					}
					catch(final Exception e)
					{
						if(e.getMessage()==null)
							Log.errOut(e);
						else
							Log.errOut(e.getMessage());
					}
					connectionComplete=true;
					status=SessionStatus.LOGIN;
					return false;
				}
			});
		}
		catch(final Exception e)
		{
			if(e.getMessage()==null)
				Log.errOut(e);
			else
				Log.errOut(e.getMessage());
		}
	}

	protected void compress2Off() throws IOException
	{
		changeTelnetMode(rawout,TELNET_COMPRESS2,false);
		out.flush();
		rawout.flush();
		rawout=new BufferedOutputStream(sock.getOutputStream());
		out = new PrintWriter(new OutputStreamWriter(rawout,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
		CMLib.s_sleep(50);
		changeTelnetMode(rawout,TELNET_COMPRESS2,false);
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("MCCP compression stopped");
	}

	@Override
	public String getGroupName()
	{
		return groupName;
	}

	@Override
	public void setGroupName(String group)
	{
		groupName=group;
	}

	@Override
	public void setFakeInput(String input)
	{
		if(fakeInput!=null)
			fakeInput.append(input);
		else
			fakeInput=new StringBuffer(input);
	}

	private void negotiateTelnetMode(OutputStream out, int optionCode)
	throws IOException
	{
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Sent sub-option: "+Session.TELNET_DESCS[optionCode]);
		if(optionCode==TELNET_TERMTYPE)
		{
			final byte[] stream={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)optionCode,(byte)1,(byte)TELNET_IAC,(byte)TELNET_SE};
			rawBytesOut(out, stream);
		}
		else
		{
			final byte[] stream={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)optionCode,(byte)TELNET_IAC,(byte)TELNET_SE};
			rawBytesOut(out, stream);
		}
		out.flush();
	}

	private boolean mightSupportTelnetMode(int telnetCode)
	{
		if(telnetSupportSet.size()==0)
		{
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_MXP));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_MSP));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_MSDP));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_GMCP));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_TERMTYPE));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_BINARY));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_ECHO));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_LOGOUT));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_NAWS));
			//telnetSupportSet.add(Integer.valueOf(Session.TELNET_GA));
			//telnetSupportSet.add(Integer.valueOf(Session.TELNET_SUPRESS_GO_AHEAD));
			//telnetSupportSet.add(Integer.valueOf(Session.TELNET_COMPRESS2));
			//telnetSupportSet.add(Integer.valueOf(Session.TELNET_LINEMODE));
		}
		return telnetSupportSet.contains(Integer.valueOf(telnetCode));
	}

	@Override
	public void setServerTelnetMode(int telnetCode, boolean onOff)
	{ 
		serverTelnetCodes[telnetCode]=onOff; 
	}
	
	@Override
	public boolean getServerTelnetMode(int telnetCode)
	{ 
		return serverTelnetCodes[telnetCode]; 
	}
	
	@Override
	public void setClientTelnetMode(int telnetCode, boolean onOff)
	{ 
		clientTelnetCodes[telnetCode]=onOff; 
	}
	
	@Override
	public boolean getClientTelnetMode(int telnetCode)
	{ 
		return clientTelnetCodes[telnetCode]; 
	}

	private void changeTelnetMode(OutputStream out, int telnetCode, boolean onOff) throws IOException
	{
		final byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_WILL:(byte)TELNET_WONT,(byte)telnetCode};
		rawBytesOut(out, command);
		out.flush();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Sent: "+(onOff?"Will":"Won't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}

	@Override
	public boolean isAllowedMxp(final String tagString)
	{
		if(tagString.startsWith("^<"))
		{
			if((!getClientTelnetMode(TELNET_MXP))||(mxpSupportSet.size()==0))
				return false;
			int x=tagString.indexOf(' ');
			if(x<0) 
				x=tagString.indexOf('>');
			if((x>0)&&(this.mxpSupportSet.contains("-"+tagString.substring(2,x))))
				return false;
		}
		else
		if(tagString.startsWith("&"))
		{
			if(terminalType.equalsIgnoreCase("mushclient"))
				return true;
		}
		return true;
	}

	@Override
	public void sendGMCPEvent(final String eventName, final String json)
	{
		if((!getClientTelnetMode(TELNET_GMCP))||(gmcpSupports.size()==0))
			return;
		try
		{
			final String lowerEventName=eventName.toLowerCase().trim();
			final int x=lowerEventName.lastIndexOf('.');
			if((x<0)&&(!gmcpSupports.containsKey(lowerEventName)))
				return;
			if((!gmcpSupports.containsKey(lowerEventName)) && (!gmcpSupports.containsKey(lowerEventName.substring(0, x))))
				return;
			if(CMSecurity.isDebugging(DbgFlag.TELNET))
				Log.debugOut("GMCP Sent: "+(lowerEventName+" "+json));
			rawBytesOut(rawout,TELNETBYTES_GMCP_HEAD);
			rawBytesOut(rawout,(lowerEventName+" "+json).getBytes());
			rawBytesOut(rawout,TELNETBYTES_END_SB);
		}
		catch(final IOException e)
		{
			killFlag=true;
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
	}

	// this is stupid, but a printwriter can not be cast as an outputstream, so this dup was necessary
	@Override
	public void changeTelnetMode(int telnetCode, boolean onOff)
	{
		try
		{
			final byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_WILL:(byte)TELNET_WONT,(byte)telnetCode};
			out.flush();
			rawBytesOut(rawout, command);
			rawout.flush();
		}
		catch (final Exception e)
		{
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Sent: "+(onOff?"Will":"Won't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}
	
	public void changeTelnetModeBackwards(int telnetCode, boolean onOff) throws IOException
	{
		final byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_DO:(byte)TELNET_DONT,(byte)telnetCode};
		if(out!=null)
			out.flush();
		rawBytesOut(rawout, command);
		rawout.flush();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Back-Sent: "+(onOff?"Do":"Don't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}

	public void changeTelnetModeBackwards(OutputStream out, int telnetCode, boolean onOff) throws IOException
	{
		final byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_DO:(byte)TELNET_DONT,(byte)telnetCode};
		rawBytesOut(out, command);
		out.flush();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Back-Sent: "+(onOff?"Do":"Don't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}

	@Override
	public void negotiateTelnetMode(int telnetCode)
	{
		try
		{
			out.flush();
			if(telnetCode==TELNET_TERMTYPE)
			{
				final byte[] command={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)telnetCode,(byte)1,(byte)TELNET_IAC,(byte)TELNET_SE};
				rawBytesOut(rawout, command);
			}
			else
			{
				final byte[] command={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)telnetCode,(byte)TELNET_IAC,(byte)TELNET_SE};
				rawBytesOut(rawout, command);
			}
			rawout.flush();
		}
		catch (final Exception e)
		{
		}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Negotiate-Sent: "+Session.TELNET_DESCS[telnetCode]);
	}
	
	private void mushClientTurnOffMXPFix()
	{
		try
		{
			// i don't know why this works, but it does
			clearInputBuffer(500);
			Command C=CMClass.getCommand("MXP");
			C.execute(mob, new XVector<String>("MXP","QUIET"), 0);
			clearInputBuffer(500);
			C=CMClass.getCommand("NOMXP");
			C.execute(mob, new XVector<String>("NOMXP","QUIET"), 0);
		}
		catch(Exception e)
		{
		}
	}

	@Override
	public void initTelnetMode(int attributesBitmap)
	{
		setServerTelnetMode(TELNET_ANSI,CMath.bset(attributesBitmap,MOB.Attrib.ANSI.getBitCode()));
		setClientTelnetMode(TELNET_ANSI,CMath.bset(attributesBitmap,MOB.Attrib.ANSI.getBitCode()));
		boolean changedSomething=false;
		final boolean mxpSet=(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP))&&CMath.bset(attributesBitmap,MOB.Attrib.MXP.getBitCode());
		if(mxpSet!=getClientTelnetMode(TELNET_MXP))
		{ 
			changeTelnetMode(TELNET_MXP,!getClientTelnetMode(TELNET_MXP)); 
			changedSomething=true;
			if((!mxpSet)&&(getTerminalType()!=null)&&(getTerminalType().equals("mushclient")))
				mushClientTurnOffMXPFix();
		}
		final boolean mspSet=(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP))&&CMath.bset(attributesBitmap,MOB.Attrib.SOUND.getBitCode());
		if(mspSet!=getClientTelnetMode(TELNET_MSP))
		{ 
			changeTelnetMode(TELNET_MSP,!getClientTelnetMode(TELNET_MSP)); 
			changedSomething=true;
		}
		if(changedSomething) 
			clearInputBuffer(500);
	}

	@Override 
	public ColorState getCurrentColor() 
	{ 
		return currentColor; 
	}
	
	@Override
	public void setCurrentColor(final ColorState newColor)
	{
		if(newColor!=null)
			currentColor=newColor;
	}
	
	@Override 
	public ColorState getLastColor() 
	{ 
		return lastColor; 
	}
	
	@Override
	public void setLastColor(final ColorState newColor)
	{
		if(newColor!=null)
			lastColor=newColor;
	}

	@Override
	public long getTotalMillis()
	{
		return milliTotal;
	}

	@Override
	public long getIdleMillis()
	{
		return System.currentTimeMillis() - lastKeystroke;
	}

	@Override
	public long getTotalTicks()
	{
		return tickTotal;
	}

	@Override
	public long getMillisOnline()
	{
		return System.currentTimeMillis() - onlineTime;
	}

	@Override
	public long getInputLoopTime()
	{
		return lastLoopTop;
	}

	@Override
	public void setInputLoopTime()
	{
		lastLoopTop = System.currentTimeMillis();
	}

	@Override
	public long getLastPKFight()
	{
		return lastPKFight;
	}

	@Override
	public void setLastPKFight()
	{
		lastPKFight = System.currentTimeMillis();
	}

	@Override
	public long getLastNPCFight()
	{
		return lastNPCFight;
	}

	@Override
	public void setLastNPCFight()
	{
		lastNPCFight = System.currentTimeMillis();
	}

	@Override
	public List<String> getLastMsgs()
	{
		return new XVector<String>(prevMsgs);
	}

	@Override
	public String getTerminalType()
	{
		return terminalType;
	}

	@Override
	public MOB mob()
	{
		return mob;
	}

	@Override
	public void setMob(MOB newmob)
	{
		mob=newmob;
	}
	
	@Override
	public void setAccount(PlayerAccount account)
	{
		acct=account;
	}
	
	@Override
	public int getWrap()
	{
		if(terminalWidth>5)
			return terminalWidth;
		return ((mob!=null)&&(mob.playerStats()!=null))?mob.playerStats().getWrap():78;
	}

	public int getPageBreak()
	{
		if(((mob!=null)&&(mob.playerStats()!=null)))
		{
			final int pageBreak=mob.playerStats().getPageBreak();
			if(pageBreak <= 0)
				return pageBreak;
			if(terminalHeight>3)
				return terminalHeight;
			return pageBreak;
		}
		return -1;
	}

	@Override
	public boolean isStopped()
	{
		return killFlag;
	}

	public void setKillFlag(boolean truefalse)
	{
		killFlag=truefalse;
	}

	@Override
	public List<String> getPreviousCMD()
	{
		return previousCmd;
	}

	@Override
	public void setBeingSnoopedBy(Session session, boolean onOff)
	{
		if(onOff)
		{
			if(!snoops.contains(session))
				snoops.add(session);
		}
		else
		{
			while(snoops.contains(session))
				snoops.remove(session);
		}
	}

	@Override
	public boolean isBeingSnoopedBy(Session S)
	{
		if(S==null)
			return snoops.size()==0;
		return(snoops.contains(S));
	}

	@Override
	public synchronized int snoopSuspension(int change)
	{
		snoopSuspensionStack+=change;
		return snoopSuspensionStack;
	}

	private int metaFlags()
	{
		return ((snoops.size()>0)?MUDCmdProcessor.METAFLAG_SNOOPED:0)
			   |(((mob!=null)&&(mob.soulMate()!=null))?MUDCmdProcessor.METAFLAG_POSSESSED:0);
	}

	public void setPreviousCmd(List<String> cmds)
	{
		if(cmds==null)
			return;
		if(cmds.size()==0)
			return;
		if((cmds.size()>0)&&(cmds.get(0).trim().startsWith("!")))
			return;

		previousCmd.clear();
		for(int i=0;i<cmds.size();i++)
			previousCmd.add((cmds.get(i)));
	}

	@Override
	public boolean isAfk()
	{
		return afkFlag;
	}

	@Override
	public void setAfkFlag(boolean truefalse)
	{
		if(afkFlag==truefalse)
			return;
		afkFlag=truefalse;
		if(afkFlag)
			println("\n\rYou are now listed as AFK.");
		else
		{
			afkMessage=null;
			println("\n\rYou are no longer AFK.");
		}
	}

	@Override
	public String getAfkMessage()
	{
		if(mob==null)
			return "";
		if((afkMessage==null)||(CMStrings.removeColors(afkMessage).trim().length()==0))
			return mob.name()+" is AFK at the moment.";
		return afkMessage;
	}

	@Override
	public void setAFKMessage(String str)
	{
		afkMessage = str;
	}

	protected void errorOut(Exception t)
	{
		Log.errOut(t);
		CMLib.sessions().remove(this);
		setKillFlag(true);
	}

	protected long getWriteStartTime()
	{
		return writeStartTime;
	}

	@Override
	public boolean isLockedUpWriting()
	{
		final long time=writeStartTime;
		if(time==0)
			return false;
		return ((System.currentTimeMillis()-time)>10000);
	}

	public final void rawBytesOut(final OutputStream out, final byte[] bytes) throws IOException
	{
		try
		{
			if(debugBinOutput && Log.debugChannelOn())
			{
				final StringBuilder str=new StringBuilder("OUTPUT: '");
				for(final byte c : bytes)
					str.append(c).append(" ");
				Log.debugOut( str.toString()+"'");
			}
			if(this.out!=null)
				this.out.flush();
			out.write(bytes);
			out.flush();
		}
		catch(ArrayIndexOutOfBoundsException x)
		{
			Log.errOut("ZLibFail",x.getMessage());
			this.setKillFlag(true);
		}
		finally
		{
			lastWriteTime=System.currentTimeMillis();
		}
	}

	@Override
	public void rawCharsOut(char[] chars)
	{
		rawCharsOut(out,chars);
	}

	public void rawCharsOut(final PrintWriter out, char[] chars)
	{
		if((out==null)||(chars==null)||(chars.length==0))
			return;
		try
		{
			if(writeLock.tryLock(10000, TimeUnit.MILLISECONDS))
			{
				try
				{
					writeThread=Thread.currentThread();
					writeStartTime=System.currentTimeMillis();
					if(debugBinOutput && Log.debugChannelOn())
					{
						final StringBuilder str=new StringBuilder("OUTPUT: '");
						for(final char c : chars)
							str.append(c);
						Log.debugOut( str.toString()+"'");
					}
					out.write(chars);
					if(out.checkError() && (!killFlag))
						stopSession(true,true,false);
				}
				finally
				{
					writeThread=null;
					writeStartTime=0;
					lastWriteTime=System.currentTimeMillis();
					writeLock.unlock();
				}
			}
			else
			if(!killFlag)
			{
				final String name=(mob!=null)?mob.Name():getAddress();
				Log.errOut("DefaultSession","Kicked out "+name+" due to write-lock ("+out.getClass().getName()+".");
				stopSession(true,true,true);
				final Thread killThisThread=writeThread;
				if(killThisThread!=null)
					CMLib.killThread(killThisThread,500,1);
			}
		}
		catch (final Exception ioe)
		{
			stopSession(true,true,false);
			setKillFlag(true);
		}
	}

	public void rawCharsOut(String c)
	{
		if(c!=null)
			rawCharsOut(c.toCharArray());
	}

	public void rawCharsOut(char c)
	{
		final char[] cs={c};
		rawCharsOut(cs);
	}

	public void snoopSupportPrint(final String msg, final boolean noCache)
	{
		try
		{
			if((snoops.size()>0)&&(snoopSuspensionStack<=0))
			{
				String msgColored;
				final String preFix=CMLib.coffeeFilter().colorOnlyFilter("^Z"+((mob==null)?"?":mob.Name())+":^N ",this);
				final int crCheck=msg.indexOf('\n');
				if((crCheck>=0)&&(crCheck<msg.length()-2))
				{
					final StringBuffer buf=new StringBuffer(msg);
					for(int i=buf.length()-1;i>=0;i--)
					{
						if((buf.charAt(i)=='\n')&&(i<buf.length()-2)&&(buf.charAt(i+1)=='\r'))
							buf.insert(i+2, preFix);
					}
					msgColored=buf.toString();
				}
				else
					msgColored=msg;
				for(int s=0;s<snoops.size();s++)
					snoops.get(s).onlyPrint(preFix+msgColored,noCache);
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
	}

	@Override
	public void onlyPrint(String msg)
	{
		onlyPrint(msg,false);
	}

	@Override
	public void onlyPrint(String msg, boolean noCache)
	{
		if((out==null)||(msg==null))
			return;
		try
		{
			snoopSupportPrint(msg,noCache);
			final String newMsg=CMLib.lang().finalTranslation(msg);
			if(newMsg!=null)
				msg=newMsg;

			if(msg.endsWith("\n\r")
			&&(msg.equals(lastStr))
			&&(msg.length()>2)
			&&(msg.indexOf("\n")==(msg.length()-2)))
			{ 
				spamStack++; 
				return; 
			}
			else
			if(spamStack>0)
			{
				if(spamStack>1)
					lastStr=lastStr.substring(0,lastStr.length()-2)+"("+spamStack+")"+lastStr.substring(lastStr.length()-2);
				rawCharsOut(lastStr.toCharArray());
			}

			spamStack=0;
			if(msg.startsWith("\n\r")&&(msg.length()>2))
				lastStr=msg.substring(2);
			else
				lastStr=msg;

			if(runThread==Thread.currentThread())
			{
				final int pageBreak=getPageBreak();
				int lines=0;
				int last=0;
				if(pageBreak>0)
				for(int i=0;i<msg.length();i++)
				{
					if(msg.charAt(i)=='\n')
					{
						lines++;
						if(lines>=pageBreak)
						{
							lines=0;
							if((i<(msg.length()-1)&&(msg.charAt(i+1)=='\r')))
								i++;
							rawCharsOut(msg.substring(last,i+1).toCharArray());
							last=i+1;
							rawCharsOut("<pause - enter>".toCharArray());
							try
							{
								String s=blockingIn(-1, true);
								if(s!=null)
								{
									s=s.toLowerCase();
									if(s.startsWith("qu")||s.startsWith("ex")||s.equals("x"))
										return;
								}
							}
							catch (final Exception e)
							{
								return;
							}
						}
					}
				}
			}

			// handle line cache --
			if(!noCache)
			for(int i=0;i<msg.length();i++)
			{
				if(curPrevMsg==null)
					curPrevMsg=new StringBuffer("");
				if(msg.charAt(i)=='\r')
					continue;
				if(msg.charAt(i)=='\n')
				{
					if(curPrevMsg.toString().trim().length()>0)
					{
						synchronized(prevMsgs)
						{
							while(prevMsgs.size()>=MAX_PREVMSGS)
								prevMsgs.remove(0);
							prevMsgs.add(curPrevMsg.toString());
							curPrevMsg.setLength(0);
						}
					}
					continue;
				}
				curPrevMsg.append(msg.charAt(i));
			}
			rawCharsOut(msg.toCharArray());
		}
		catch (final java.lang.NullPointerException e)
		{
		}
	}

	@Override
	public void rawOut(String msg)
	{
		rawCharsOut(msg);
	}
	
	@Override
	public void rawPrint(String msg)
	{
		if(msg==null)
			return;
		onlyPrint((needPrompt?"":"\n\r")+msg,false);
		needPrompt=true;
	}

	@Override
	public void safeRawPrint(String msg)
	{
		if(msg==null)
			return;
		onlyPrint((needPrompt?"":"\n\r")+CMLib.coffeeFilter().mxpSafetyFilter(msg, this),false);
		needPrompt=true;
	}

	@Override
	public void print(String msg)
	{
		onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false),false);
	}

	@Override
	public void promptPrint(String msg)
	{
		print(msg);
		if(promptSuffix.length>0)
		{
			try
			{
				rawBytesOut(rawout, promptSuffix);
			}
			catch (IOException e)
			{
			}
		}
		final MOB mob=mob();
		if((!getClientTelnetMode(TELNET_SUPRESS_GO_AHEAD)) 
		&& (!killFlag)
		&& (mightSupportTelnetMode(TELNET_GA)
			||(mob==null)
			||(mob.isAttributeSet(MOB.Attrib.TELNET_GA))))
		{
			try 
			{ 
				rawBytesOut(rawout, TELNETGABYTES); 
			} 
			catch (final Exception e)
			{
			}
		}
	}

	@Override
	public void rawPrintln(String msg)
	{
		if(msg!=null)
			rawPrint(msg+"\n\r");
	}

	@Override
	public void safeRawPrintln(String msg)
	{
		if(msg!=null)
			safeRawPrint(msg+"\n\r");
	}

	@Override
	public void stdPrint(String msg)
	{
		rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false));
	}

	@Override
	public void print(Physical src, Environmental trg, Environmental tol, String msg)
	{
		onlyPrint((CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)),false);
	}

	@Override
	public void stdPrint(Physical src, Environmental trg, Environmental tol, String msg)
	{
		rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,trg,msg,false));
	}

	@Override
	public void println(String msg)
	{
		if(msg!=null)
			print(msg+"\n\r");
	}

	@Override
	public void wraplessPrintln(String msg)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,true)+"\n\r",false);
		needPrompt=true;
	}

	@Override
	public void wraplessPrint(String msg)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,true),false);
		needPrompt=true;
	}

	@Override
	public void colorOnlyPrintln(String msg)
	{
		colorOnlyPrint(msg+"\n\r",false);
	}

	@Override
	public void colorOnlyPrintln(String msg, boolean noCache)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().colorOnlyFilter(msg,this)+"\n\r",noCache);
		needPrompt=true;
	}

	@Override
	public void colorOnlyPrint(String msg)
	{
		colorOnlyPrint(msg,false);
	}

	@Override
	public void colorOnlyPrint(String msg, boolean noCache)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().colorOnlyFilter(msg,this),noCache);
		needPrompt=true;
	}

	@Override
	public void stdPrintln(String msg)
	{
		if(msg!=null)
			rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false)+"\n\r");
	}

	@Override
	public void println(Physical src, Environmental trg, Environmental tol, String msg)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)+"\n\r",false);
	}

	@Override
	public void stdPrintln(Physical src,Environmental trg, Environmental tol, String msg)
	{
		if(msg!=null)
			rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)+"\n\r");
	}

	@Override
	public void setPromptFlag(boolean truefalse)
	{
		needPrompt=truefalse;
	}

	@Override
	public String prompt(String Message, String Default, long maxTime)
		throws IOException
	{
		final String Msg=prompt(Message,maxTime).trim();
		if(Msg.equals(""))
			return Default;
		return Msg;
	}

	@Override
	public String prompt(String Message, String Default)
		throws IOException
	{
		final String Msg=prompt(Message,-1).trim();
		if(Msg.equals(""))
			return Default;
		return Msg;
	}

	@Override
	public void prompt(InputCallback callBack)
	{
		if(callBack!=null)
			callBack.showPrompt();
		if(this.inputCallback!=null)
			this.inputCallback.timedOut();
		this.inputCallback=callBack;
	}

	@Override
	public String prompt(String Message, long maxTime)
			throws IOException
	{
		promptPrint(Message);
		final String input=blockingIn(maxTime, true);
		if(input==null)
			return "";
		if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
			return input.substring(0,input.length()-1);
		return input;
	}

	@Override
	public String prompt(String Message)
		throws IOException
	{
		promptPrint(Message);
		final String input=blockingIn(-1, true);
		if(input==null)
			return "";
		if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
			return input.substring(0,input.length()-1);
		return input;
	}

	@Override
	public String[] getColorCodes()
	{
		if(clookup==null)
			clookup=CMLib.color().standardColorLookups();

		if(mob()==null)
			return clookup;
		PlayerStats pstats=mob().playerStats();
		if((mob.soulMate()!=null)&&(mob.soulMate().playerStats()!=null))
			pstats=mob.soulMate().playerStats();
		if(pstats==null)
			return clookup;

		if(!pstats.getColorStr().equals(lastColorStr))
		{
			lastColorStr=pstats.getColorStr();
			if(pstats.getColorStr().length()==0)
				clookup=CMLib.color().standardColorLookups();
			else
			{
				clookup=CMLib.color().standardColorLookups().clone();
				List<String> changesList = CMParms.parseAny(pstats.getColorStr(), '#', true);
				for(String change : changesList)
				{
					int subChar;
					String subColor;
					if(change.startsWith("(") && (change.indexOf(')')>0))
					{
						int x=change.indexOf(')');
						subChar=CMath.s_int(change.substring(1,x));
						subColor = change.substring(x+1);
					}
					else
					{
						subChar=change.charAt(0);
						subColor=change.substring(1);
					}
					clookup[subChar]=CMLib.color().translateCMCodeToANSI(subColor);
				}
				for(int i=0;i<clookup.length;i++)
				{
					final String s=clookup[i];
					if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
						clookup[i]=clookup[s.charAt(1)];
				}
			}
		}
		return clookup;
	}

	public void handleSubOption(int optionCode, char[] suboptionData, int dataSize)
		throws IOException
	{
		switch(optionCode)
		{
		case TELNET_TERMTYPE:
			if(dataSize >= 1)
			{
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("For suboption "+Session.TELNET_DESCS[optionCode]+", got code "+((int)suboptionData[0])+": "+new String(suboptionData, 1, dataSize - 1));
				if(suboptionData[0] == 0)
				{
					terminalType = new String(suboptionData, 1, dataSize - 1);
					if(terminalType.equalsIgnoreCase("ZMUD")
					||terminalType.equalsIgnoreCase("CMUD")
					||terminalType.equalsIgnoreCase("XTERM"))
					{
						if(mightSupportTelnetMode(TELNET_ECHO))
							telnetSupportSet.remove(Integer.valueOf(TELNET_ECHO));
						changeTelnetMode(rawout,TELNET_ECHO,false);
					}
					else
					if(terminalType.equals("ANSI"))
						changeTelnetMode(rawout,TELNET_ECHO,true);
					else
					if(terminalType.startsWith("GIVE-WINTIN.NET-A-CHANCE"))
					{
						rawOut("\n\r\n\r**** Your MUD Client is Broken! Please use another!!****\n\r\n\r");
						rawout.flush();
						CMLib.s_sleep(1000);
						rawout.close();
					}
					else
					if(terminalType.toLowerCase().startsWith("mushclient")&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
					{
						negotiateTelnetMode(rawout,TELNET_MXP);
						mxpSupportSet.remove("+IMAGE");
						mxpSupportSet.remove("+IMAGE.URL");
						mxpSupportSet.add("-IMAGE.URL");
					}
					else
					if(terminalType.toLowerCase().equals("simplemu"))
					{
						if(CMParms.indexOf(this.promptSuffix, (byte)'\n')<0)
						{
							promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length+1);
							promptSuffix[promptSuffix.length-1] = (byte)'\n';
						}
						if(CMParms.indexOf(this.promptSuffix, (byte)'\r')<0)
						{
							promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length+1);
							promptSuffix[promptSuffix.length-1] = (byte)'\r';
						}
						if((!this.mightSupportTelnetMode(Session.TELNET_GA))
						&&(CMParms.indexOf(this.promptSuffix, Session.TELNETGABYTES)<0))
						{
							final int pos = promptSuffix.length;
							promptSuffix = Arrays.copyOf(promptSuffix, promptSuffix.length + Session.TELNETGABYTES.length);
							System.arraycopy(Session.TELNETGABYTES, 0, promptSuffix, pos, Session.TELNETGABYTES.length);
						}
					}
				}
				else
				if (suboptionData[0] == 1) // Request for data.
				{/* No idea how to handle this, ignore it for now. */
				}
			}
			break;
		case TELNET_NAWS:
			if (dataSize == 4)  // It should always be 4.
			{
				terminalWidth = ((suboptionData[0] << 8) | suboptionData[1])-2;
				terminalHeight = (suboptionData[2] << 8) | suboptionData[3];
				if(terminalWidth > CMStrings.SPACES.length()) 
					terminalWidth=CMStrings.SPACES.length();
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("For suboption "+Session.TELNET_DESCS[optionCode]+", got: "+terminalWidth+"x"+terminalHeight);
			}
			break;
		case TELNET_MSDP:
			{
				final byte[] resp=CMLib.protocol().processMsdp(this, suboptionData, dataSize, this.msdpReportables);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("For suboption "+Session.TELNET_DESCS[optionCode]+", got "+dataSize+" bytes, sent "+((resp==null)?0:resp.length));
				if(resp!=null)
					rawBytesOut(rawout, resp);
			}
			break;
		case TELNET_GMCP:
			{
				final byte[] resp=CMLib.protocol().processGmcp(this, new String(suboptionData), this.gmcpSupports);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				{
					Log.debugOut("For suboption "+Session.TELNET_DESCS[optionCode]+", got "+dataSize+" bytes, sent "+((resp==null)?0:resp.length));
					Log.debugOut(new String(suboptionData));
				}
				if(resp!=null)
					rawBytesOut(rawout, resp);
			}
			break;
		default:
			// Ignore it.
			break;
		}
	}

	public void handleEscape() throws IOException, InterruptedIOException
	{
		if((in==null)||(out==null))
			return;
		int c=readByte();
		if((char)c!='[')
			return;

		boolean quote=false;
		final StringBuffer escapeStr=new StringBuffer("");
		while(((c=readByte())!=-1)
		&&(!killFlag)
		&&((quote)||(!Character.isLetter((char)c))))
		{
			escapeStr.append((char)c);
			if(c=='"')
				quote=!quote;
		}
		if(c==-1)
			return;
		escapeStr.append((char)c);
		String esc=escapeStr.toString().trim();
		// at the moment, we only handle mxp escapes
		// everything else is effectively EATEN
		if(!esc.endsWith("z"))
			return;
		esc=esc.substring(0,esc.length()-1);
		if(!CMath.isNumber(esc))
			return;
		final int escNum=CMath.s_int(esc);
		// only LINE-based mxp escape sequences are respected
		if(escNum>3)
			return;
		sock.setSoTimeout(30000);
		final StringBuffer line=new StringBuffer("");
		while(((c=readByte())!=-1)&&(!killFlag))
		{
			if(c=='\n')
				break;
			line.append((char)c);
		}
		sock.setSoTimeout(SOTIMEOUT);
		String l=line.toString().toUpperCase().trim();
		// now we have the line, so parse out tags -- only tags matter!
		while(l.length()>0)
		{
			final int tagStart=l.indexOf('<');
			if(tagStart<0)
				return;
			final int tagEnd=l.indexOf('>');
			if(tagEnd<0)
				return;
			String tag=l.substring(tagStart+1,tagEnd).trim();
			l=l.substring(tagEnd+1).trim();
			// now we have a tag, and its parameters (space delimited)
			final List<String> parts=CMParms.parseSpaces(tag,true);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				Log.debugOut("Got secure MXP tag: "+tag);
			if(parts.size()>1)
			{
				tag=parts.get(0);
				if(tag.equals("VERSION"))
				{
					for(int p=1;p<parts.size();p++)
					{
						final String pp=parts.get(p);
						final int x=pp.indexOf('=');
						if(x<0)
							continue;
						mxpVersionInfo.remove(pp.substring(0,x).trim());
						mxpVersionInfo.put(pp.substring(0,x).trim(),pp.substring(x+1).trim());
					}
				}
				else
				if(tag.equals("SUPPORTS"))
				{
					for(int p=1;p<parts.size();p++)
					{
						final String s=parts.get(p).toUpperCase();
						final int x=s.indexOf('.');
						if(s.startsWith("+"))
						{
							if((terminalType == null)
							||(!terminalType.toLowerCase().startsWith("mushclient"))
							||(s.indexOf("IMAGE")<0))
							{
								mxpSupportSet.add(s);
								if(x>0)
									mxpSupportSet.add(s.substring(0, x));
							}
						}
						else
						if(s.startsWith("-"))
						{
							mxpSupportSet.add(s);
							mxpSupportSet.remove("+"+s.substring(1));
						}
					}
				}
				else
				if(tag.equals("SHUTDOWN"))
				{
					final MOB M=CMLib.players().getLoadPlayer(parts.get(1));
					if((M!=null)
					&&(M.playerStats().matchesPassword(parts.get(2)))
					&&(CMSecurity.isASysOp(M)))
					{
						final boolean keepDown=parts.size()>3?CMath.s_bool(parts.get(3)):true;
						final String externalCmd=(parts.size()>4)?CMParms.combine(parts,4):null;
						final Vector<String> cmd=new XVector<String>("SHUTDOWN","NOPROMPT");
						if(!keepDown)
						{
							cmd.add("RESTART");
							if((externalCmd!=null)&&(externalCmd.length()>0))
								cmd.add(externalCmd);
						}
						final Command C=CMClass.getCommand("Shutdown");
						l="";
						setKillFlag(true);
						rawCharsOut(out,"\n\n\033[1z<Executing Shutdown...\n\n".toCharArray());
						M.setSession(this);
						if(C!=null)
							C.execute(M,cmd,0);
					}
				}
			}
		}
	}

	public void handleIAC()
		throws IOException, InterruptedIOException
	{
		if((in==null)||(out==null))
			return;
		lastIACIn=System.currentTimeMillis();
		int c=readByte();
		if(c>255)
			c=c&0xff;

		switch(c)
		{
		case TELNET_IAC:
			bNextByteIs255=true;
			break;
		case TELNET_SB:
		{
			CharArrayWriter subOptionStream=new CharArrayWriter();
			final int subOptionCode = readByte();
			int last = 0;
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				Log.debugOut("Reading sub-option "+subOptionCode);
			final long expire=System.currentTimeMillis() + 100;
			while((System.currentTimeMillis()<expire)
			&&(!killFlag))
			{
				try
				{
					last = readByte();
					if(last != -1)
					{
						if(subOptionStream.size()>1024*1024*5)
						{
							killFlag=true;
							return;
						}
						else
						if (last == TELNET_IAC)
						{
							last = readByte();
							if (last == TELNET_IAC)
								subOptionStream.write(TELNET_IAC);
							else
							if (last == TELNET_SE)
								break;
						}
						else
							subOptionStream.write((char)last);
					}
				}
				catch(IOException e)
				{
				}
			}
			final char[] subOptionData=subOptionStream.toCharArray();
			subOptionStream=null;
			handleSubOption(subOptionCode, subOptionData, subOptionData.length);
			break;
		}
		case TELNET_DO:
		{
			final int last=readByte();
			setClientTelnetMode(last,true);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				Log.debugOut("Got DO "+Session.TELNET_DESCS[last]);
			if((terminalType.equalsIgnoreCase("zmud")||terminalType.equalsIgnoreCase("cmud"))&&(last==Session.TELNET_ECHO))
				setClientTelnetMode(Session.TELNET_ECHO,false);
			if(last==TELNET_TERMTYPE)
				negotiateTelnetMode(rawout,TELNET_TERMTYPE);
			if((last==TELNET_COMPRESS2)&&(getServerTelnetMode(last)))
			{
				setClientTelnetMode(last,true);
				if(connectionComplete)
				{
					prompt(new TickingCallback(250)
					{
						@Override
						public boolean tick(int counter)
						{
							try
							{
								if((out==null)||(killFlag))
									return false;
								out.flush();
								rawout.flush();
								switch(counter)
								{
								case 3:
								{
									if(getClientTelnetMode(TELNET_COMPRESS2))
									{
										negotiateTelnetMode(rawout,TELNET_COMPRESS2);
										rawout.flush();
										final ZOutputStream zOut=new ZOutputStream(rawout, JZlib.Z_DEFAULT_COMPRESSION);
										rawout=zOut;
										zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
										out = new PrintWriter(new OutputStreamWriter(zOut,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
										if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
											Log.debugOut("MCCP compression started");
									}
									break;
								}
								case 10:
									return false;
								default:
									break;
								}
								return true;
							}
							catch(final IOException e)
							{
								if(e.getMessage()==null)
									Log.errOut(e);
								else
									Log.errOut(e.getMessage());
							}
							return false;
						}
					});
				}
			}
			else
			if(!mightSupportTelnetMode(last))
				changeTelnetMode(last,false);
			else
			if(!getServerTelnetMode(last))
				changeTelnetMode(last,true);
			if(serverTelnetCodes[TELNET_LOGOUT])
				setKillFlag(true);
			break;
		}
		case TELNET_DONT:
		{
			final int last=readByte();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				Log.debugOut("Got DONT "+Session.TELNET_DESCS[last]);
			setClientTelnetMode(last,false);
			if((last==TELNET_COMPRESS2)&&(getServerTelnetMode(last)))
			{
				setClientTelnetMode(last,false);
				rawout=new BufferedOutputStream(sock.getOutputStream());
				out = new PrintWriter(new OutputStreamWriter(rawout,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
			}
			if((mightSupportTelnetMode(last)&&(getServerTelnetMode(last))))
				changeTelnetMode(last,false);
			break;
		}
		case TELNET_WILL:
		{
			final int last=readByte();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				Log.debugOut("Got WILL "+Session.TELNET_DESCS[last]);
			setClientTelnetMode(last,true);
			if((terminalType.equalsIgnoreCase("zmud")||terminalType.equalsIgnoreCase("cmud"))&&(last==Session.TELNET_ECHO))
				setClientTelnetMode(Session.TELNET_ECHO,false);
			if(!mightSupportTelnetMode(last))
				changeTelnetModeBackwards(last,false);
			else
			if(!getServerTelnetMode(last))
				changeTelnetModeBackwards(last,true);
			if(serverTelnetCodes[TELNET_LOGOUT])
				setKillFlag(true);
			break;
		}
		case TELNET_WONT:
		{
			final int last=readByte();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
				Log.debugOut("Got WONT "+Session.TELNET_DESCS[last]);
			setClientTelnetMode(last,false);
			if((mightSupportTelnetMode(last))&&(getServerTelnetMode(last)))
				changeTelnetModeBackwards(last,false);
			break;
		}
		case TELNET_AYT:
			rawCharsOut(" \b");
			break;
		default:
			return;
		}
	}

	public int readByte() throws IOException
	{
		if(bNextByteIs255)
			return (byte)255;
		bNextByteIs255 = false;
		if(fakeInput!=null)
			throw new java.io.InterruptedIOException(".");
		if((rawin!=null) && (rawin.available()>0))
		{
			final int read = rawin.read();
			if(read==-1)
				throw new java.io.InterruptedIOException(".");
			if(debugBinInput && Log.debugChannelOn())
				debugBinInputBuf.append(read).append(" ");
			return read;
		}
		throw new java.io.InterruptedIOException(".");
	}

	public int readChar() throws IOException
	{
		if(bNextByteIs255)
			return 255;
		bNextByteIs255 = false;
		if(fakeInput!=null)
		{
			if(fakeInput.length()>0)
			{
				final char c=fakeInput.charAt(0);
				fakeInput.delete(0, 1);
				return c;
			}
			fakeInput=null;
		}
		int b = readByte();
		if(1==inMaxBytesPerChar)
			return b;
		if((b==TELNET_IAC)||((b&0xff)==TELNET_IAC)||(b=='\033')||(b==27)||(in==null))
			return b;
		charWriter.write(b);
		int maxBytes=inMaxBytesPerChar;
		while((in!=null) 
			&& !in.ready() 
			&& !killFlag 
			&& (rawin!=null) 
			&&(rawin.available()>0) 
			&& (--maxBytes>=0))
		{
			try
			{
				return in.read();
			}
			catch(final java.io.InterruptedIOException e)
			{
				b = readByte();
				charWriter.write(b);
			}
		}
		if(in==null)
			throw new java.io.InterruptedIOException();
		return in.read();
	}

	@Override
	public char hotkey(long maxWait)
	{
		if((in==null)||(out==null))
			return '\0';
		input=new StringBuffer("");
		final long start=System.currentTimeMillis();
		try
		{
			suspendCommandLine=true;
			char c='\0';
			while((!killFlag)
			&&((maxWait<=0)||((System.currentTimeMillis()-start)<maxWait)))
			{
				c=(char)nonBlockingIn(false);
				if((c==(char)0)||(c==(char)1)||(c==(char)-1))
					continue;
				return c;
			}
			suspendCommandLine=false;
			if((maxWait>0)&&((System.currentTimeMillis()-start)>=maxWait))
				throw new java.io.InterruptedIOException(TIMEOUT_MSG);
		}
		catch (final java.io.IOException e)
		{
		}
		return '\0';
	}

	protected int nonBlockingIn(boolean appendInputFlag)
	throws IOException
	{
		try
		{
			int c=readChar();
			if(c<0)
				throw new IOException("reset by peer");
			else
			if((c==TELNET_IAC)||((c&0xff)==TELNET_IAC))
				handleIAC();
			else
			if(c=='\033')
				handleEscape();
			else
			{
				boolean rv = false;
				switch (c)
				{
					case 0:
					{
						c=-1;
						lastWasCR = false;
						lastWasLF = false;
					}
					break;
					case 10:
					{
						c=-1;
						if(!lastWasCR)
						{
							lastWasLF = true;
							rv = true;
						}
						else
							lastWasLF = false;
						lastWasCR = false;
						if (getClientTelnetMode(TELNET_ECHO))
							rawCharsOut(""+(char)13+(char)10);  // CR
						break;
					}
					case 13:
					{
						c=-1;
						if(!lastWasLF)
						{
							lastWasCR = true;
							rv = true;
						}
						else
							lastWasCR = false;
						lastWasLF = false;
						if (getClientTelnetMode(TELNET_ECHO))
							rawCharsOut(""+(char)13+(char)10);  // CR
						break;
					}
					case 27:
					{
						lastWasCR = false;
						lastWasLF = false;
						// don't let them enter ANSI escape sequences...
						c = -1;
						break;
					}
					default:
					{
						if(((c>>8)&0xff)>241)
							c=-1;
						lastWasCR = false;
						lastWasLF = false;
						break;
					}
				}

				if(c>0)
				{
					lastKeystroke=System.currentTimeMillis();
					if(appendInputFlag)
						input.append((char)c);
					if (getClientTelnetMode(TELNET_ECHO))
						rawCharsOut((char)c);
					if(!appendInputFlag)
						return c;
				}
				if(rv)
					return 0;
			}
		}
		catch(final InterruptedIOException e)
		{
			return -1;
		}
		return 1;
	}

	protected void clearInputBuffer(long maxTime)
	{
		try
		{
			blockingIn(maxTime, true);
		}
		catch(Exception e)
		{
		}
	}
	
	@Override
	public String blockingIn(long maxTime, boolean filter)
		throws IOException
	{
		if((in==null)||(out==null))
			return "";
		this.input.setLength(0);
		final long start=System.currentTimeMillis();
		final long timeoutTime= (maxTime<=0) ? Long.MAX_VALUE : (start + maxTime);
		long nextPingAtTime=start + PINGTIMEOUT;
		try
		{
			suspendCommandLine=true;
			long now;
			long lastC;
			StringBuilder inStr = null;
			while((!killFlag)
			&&((now=System.currentTimeMillis())<timeoutTime))
			{
				if((lastC=nonBlockingIn(true))==0)
				{
					inStr=new StringBuilder(input);
					this.input.setLength(0);
					if(sessionMCPCheck(inStr))
						break;
				}
				else
				if(lastC == -1)
				{
					if(now > nextPingAtTime)
					{
						rawCharsOut(PINGCHARS);
						nextPingAtTime=now +PINGTIMEOUT;
					}
					CMLib.s_sleep(100); // if they entered nothing, make sure we dont do a busy poll
				}
			}
			if(inStr == null)
				inStr = new StringBuilder();
			suspendCommandLine=false;
			if(System.currentTimeMillis()>=timeoutTime)
				throw new java.io.InterruptedIOException(TIMEOUT_MSG);

			final String str;
			if(filter)
				str=CMLib.coffeeFilter().simpleInFilter(inStr,CMSecurity.isAllowed(mob,(mob!=null)?mob.location():null,CMSecurity.SecFlag.MXPTAGS));
			else
				str=inStr.toString();
			if(str==null)
				return null;
			snoopSupportPrint(str+"\n\r",true);
			if(debugStrInput)
				Log.sysOut("INPUT: "+(mob==null?"":mob.Name())+": '"+inStr.toString()+"'");
			return str;
		}
		finally
		{
			suspendCommandLine=false;
		}
	}

	public String blockingIn() throws IOException
	{
		return blockingIn(-1, true);
	}

	protected boolean sessionMCPCheck(final StringBuilder inStr)
	{
		if((inStr.length()>3)&&(inStr.substring(0, 2).equals("#$")))
		{
			if(inStr.substring(0, 3).equals("#$#"))
			{
				if(debugStrInput)
					Log.sysOut("INPUT: "+(mob==null?"":mob.Name())+": '"+inStr.toString()+"'");
				if(CMLib.protocol().mcp(this,inStr,mcpKey,mcpSupported,mcpKeyPairs))
					return false;
			}
			else
			if(inStr.substring(0, 3).equals("#$\""))
				inStr.delete(0, 3);
		}
		return true;
	}
	
	@Override
	public String readlineContinue() throws IOException, SocketException
	{
		if((in==null)||(out==null))
			return "";
		int code=-1;
		while(!killFlag)
		{
			synchronized(sock)
			{
				if(sock.isClosed() || (!sock.isConnected()))
				{
					if(mob()!=null)
						stopSession(true,CMLib.flags().isInTheGame(mob(),true),false);
					else
						setKillFlag(true);
					return null;
				}
			}
			code=nonBlockingIn(true);
			if(code==1)
				continue;
			if(code==0)
				break;
			if(code==-1)
				return null;
		}

		final StringBuilder inStr=new StringBuilder(input);
		input.setLength(0);
		if(!sessionMCPCheck(inStr))
			return null;
		final String str=CMLib.coffeeFilter().simpleInFilter(inStr,CMSecurity.isAllowed(mob,(mob!=null)?mob.location():null,CMSecurity.SecFlag.MXPTAGS));
		if(str==null)
			return null;
		snoopSupportPrint(str+"\n\r",true);
		if(debugStrInput)
			Log.sysOut("INPUT: "+(mob==null?"":mob.Name())+": '"+inStr.toString()+"'");
		return str;
	}

	@Override
	public boolean confirm(final String Message, String Default, long maxTime) throws IOException
	{
		if(Default.toUpperCase().startsWith("T"))
			Default="Y";
		final String YN=choose(Message,"YN",Default,maxTime);
		if(YN.equals("Y"))
			return true;
		return false;
	}

	@Override
	public boolean confirm(final String Message, String Default) throws IOException
	{
		if(Default.toUpperCase().startsWith("T"))
			Default="Y";
		final String YN=choose(Message,"YN",Default,-1);
		if(YN.equals("Y"))
			return true;
		return false;
	}

	@Override
	public String choose(final String Message, final String Choices, String Default) throws IOException
	{
		return choose(Message,Choices,Default,-1,null);
	}

	@Override
	public String choose(final String Message, final String Choices, final String Default, long maxTime) throws IOException
	{
		return choose(Message,Choices,Default,maxTime,null);
	}

	@Override
	public String choose(final String Message, final String Choices, final String Default, long maxTime, List<String> paramsOut) throws IOException
	{
		String YN="";
		String rest=null;
		while((YN.equals(""))||(Choices.indexOf(YN)<0)&&(!killFlag))
		{
			promptPrint(Message);
			YN=blockingIn(maxTime, true);
			if(YN==null)
			{
				return Default.toUpperCase();
			}
			YN=YN.trim();
			if(YN.equals(""))
			{
				return Default.toUpperCase();
			}
			if(YN.length()>1)
			{
				if(paramsOut!=null)
					rest=YN.substring(1).trim();
				YN=YN.substring(0,1).toUpperCase();
			}
			else
				YN=YN.toUpperCase();
		}
		if((rest!=null)&&(paramsOut!=null)&&(rest.length()>0))
			paramsOut.addAll(CMParms.cleanParameterList(rest));
		return YN;
	}

	@Override
	public void stopSession(boolean removeMOB, boolean dropSession, boolean killThread)
	{
		setKillFlag(true);
		setStatus(SessionStatus.LOGOUT5);
		if(dropSession)
		{
			preLogout(mob);
		}
		if(removeMOB)
		{
			removeMOBFromGame(false);
		}
		if(dropSession)
			logoutFinal();
		if(killThread)
		{
			// this is a really really bad idea, because any other sessions
			// on the same thread get killed also!
			Thread killThisThread=null;
			synchronized(this)
			{
				if(runThread==Thread.currentThread())
					setKillFlag(true);
				else
				if(runThread!=null)
					killThisThread=runThread;
			}
			if(killThisThread!=null)
				killThisThread.interrupt();
			killThisThread=writeThread;
			if(killThisThread!=null)
				killThisThread.interrupt();
		}
	}

	public void showPrompt()
	{
		promptLastShown=System.currentTimeMillis();
		final MOB mob=mob();
		if(mob==null)
			return;
		if(mob.playerStats()==null)
			return;
		final StringBuilder buf=new StringBuilder("");
		if(getClientTelnetMode(Session.TELNET_MXP))
		{
			buf.append("^<!ENTITY Hp '").append(mob.curState().getHitPoints())
				.append("'^>^<!ENTITY MaxHp '").append(mob.maxState().getHitPoints())
				.append("'^>^<!ENTITY Mana '").append(mob.curState().getMana())
				.append("'^>^<!ENTITY MaxMana '").append(mob.maxState().getMana())
				.append("'^>^<!ENTITY Move '").append(mob.curState().getMovement())
				.append("'^>^<!ENTITY MaxMove '").append(mob.maxState().getMovement())
				.append("'^>^<!ENTITY Exp '").append(mob.getExperience())
				.append("'^>^<!ENTITY ExpNeed '").append(mob.getExpNeededLevel())
				.append("'^>\n\r\n\r");
		}
		buf.append(CMLib.utensils().builtPrompt(mob));
		promptPrint("^<Prompt^>"+buf.toString()+"^</Prompt^>^.^N");
	}

	protected void closeSocks(String finalMsg)
	{
		if((sock!=null)&&(!sockObj.get()))
		{
			sockObj.set(true);
			try
			{
				Log.sysOut("Disconnect: "+finalMsg+getAddress()+" ("+CMLib.time().date2SmartEllapsedTime(getMillisOnline(),true)+")");
				setStatus(SessionStatus.LOGOUT7);
				sock.shutdownInput();
				setStatus(SessionStatus.LOGOUT8);
				if(out!=null)
				{
					try
					{
						if(!out.checkError())
						{
							out.write(PINGCHARS);
							out.checkError();
						}
					}
					catch (final Exception t)
					{
					}
					out.close();
				}
				setStatus(SessionStatus.LOGOUT9);
				sock.shutdownOutput();
				setStatus(SessionStatus.LOGOUT10);
				sock.close();
				setStatus(SessionStatus.LOGOUT11);
			}
			catch(final IOException e)
			{
			}
			catch (final java.lang.ArrayIndexOutOfBoundsException e)
			{
			}
			finally
			{
				rawin=null;
				in=null;
				out=null;
				sock=null;
				sockObj.set(false);
			}
		}
	}

	@Override
	public String getAddress()
	{
		try
		{
			return sock.getInetAddress().getHostAddress();
		}
		catch (final Exception e)
		{
			return "Unknown (Excpt "+e.getMessage() + ")";
		}
	}

	private void preLogout(MOB M)
	{
		if(M==null)
			return;
		synchronized(loggingOutObj)
		{
			if(loggingOutObj[0])
				return;
			try
			{
				loggingOutObj[0]=true;
				final boolean inTheGame=CMLib.flags().isInTheGame(M,true);
				if(inTheGame 
				&& (M.location()!=null)
				&&((!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
					||(CMLib.sessions().getCountAll()>1)))
				{
					List<Room> rooms=new ArrayList<Room>(1);
					rooms.add(M.location());
					for(MOB M2 : M.getGroupMembers(new HashSet<MOB>()))
					{
						if((M2.location()!=null)&&(!rooms.contains(M2.location())))
							rooms.add(M2.location());
					}
					for(Room R : rooms)
					{
						try
						{
							R.send(M, CMClass.getMsg(mob, CMMsg.MSG_QUIT, null));
						}
						catch (Throwable t)
						{ /* and eat it */
							Log.errOut(t.getMessage());
						}
					}
				}
		
				while((getLastPKFight()>0)
				&&((System.currentTimeMillis()-getLastPKFight())<(2*60*1000))
				&&(mob!=null))
					CMLib.s_sleep(1000);
				String name=M.Name();
				if(name.trim().length()==0)
					name="Unknown";
				if((M.isInCombat())&&(M.location()!=null))
				{
					CMLib.commands().postFlee(mob,"NOWHERE");
					M.makePeace(false);
				}
				if(!CMLib.flags().isCloaked(M))
				{
					final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOGOFFS);
					for(int i=0;i<channels.size();i++)
						CMLib.commands().postChannel(channels.get(i),M.clans(),L("@x1 has logged out",name),true);
				}
				CMLib.login().notifyFriends(M,L("^X@x1 has logged off.^.^?",M.Name()));
		
				// the player quit message!
				CMLib.threads().executeRunnable(groupName,new LoginLogoutThread(M,CMMsg.MSG_QUIT));
				if(M.playerStats()!=null)
					M.playerStats().setLastDateTime(System.currentTimeMillis());
				Log.sysOut("Logout: "+name+" ("+CMLib.time().date2SmartEllapsedTime(System.currentTimeMillis()-userLoginTime,true)+")");
				if(inTheGame)
					CMLib.database().DBUpdateFollowers(M);
			}
			finally
			{
				loggingOutObj[0]=false;
			}
		}
	}

	private void removeMOBFromGame(boolean killSession)
	{
		final MOB M=mob;
		if(M!=null)
		{
			final PlayerStats pStats=M.playerStats();
			final boolean inTheGame=CMLib.flags().isInTheGame(M,true);
			if(pStats!=null)
				pStats.setLastDateTime(System.currentTimeMillis());
			if(inTheGame)
				CMLib.database().DBUpdateFollowers(M);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LOGOUTS))
			{
				if(pStats!=null) // cant do this when logouts are suspended -- folks might get killed!
					CMLib.threads().suspendResumeRecurse(M, false, true);
				M.removeFromGame(true,killSession);
			}
		}
	}

	@Override
	public SessionStatus getStatus()
	{
		return status;
	}

	@Override
	public boolean isWaitingForInput()
	{
		return (inputCallback!=null);
	}

	@Override
	public void logout(boolean removeMOB)
	{
		final MOB M = this.mob;
		if((M==null)||(M.playerStats()==null))
			stopSession(false,false,false);
		else
		{
			preLogout(M);
			if(removeMOB)
				removeMOBFromGame(false);
			M.setSession(null);
			this.mob=null;
		}
	}

	@Override
	public boolean isRunning()
	{
		return runThread!=null;
	}

	@Override
	public boolean isPendingLogin(final String otherLoginName)
	{
		switch(status)
		{
			case LOGIN:
			case LOGIN2:
			case HANDSHAKE_OPEN:
			case HANDSHAKE_MCCP:
			case HANDSHAKE_MXP:
			case HANDSHAKE_MXPPAUSE:
			case HANDSHAKE_DONE:
				break;
			default:
				return false;
		}
		if(loginSession==null)
			return false;
		final String myLogin=loginSession.login();
		if((otherLoginName==null)||(myLogin==null))
			return false;
		return otherLoginName.equalsIgnoreCase(myLogin);
	}

	protected void doProtocolPings()
	{
		if(getClientTelnetMode(TELNET_MSDP))
		{
			final byte[] msdpPingBuf=CMLib.protocol().pingMsdp(this, msdpReportables);
			if(msdpPingBuf!=null)
			{
				try
				{
					rawBytesOut(rawout, msdpPingBuf);
				}
				catch (final IOException e)
				{
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("MSDP Reported: "+msdpPingBuf.length+" bytes");
			}
		}
		if(getClientTelnetMode(TELNET_GMCP))
		{
			final byte[] gmcpPingBuf=CMLib.protocol().pingGmcp(this, gmcpPings, gmcpSupports);
			if(gmcpPingBuf!=null)
			{
				try
				{
					rawBytesOut(rawout, gmcpPingBuf);
				}
				catch (final IOException e)
				{
				}
			}
		}
	}

	@Override
	public void run()
	{
		synchronized(this)
		{
			if(runThread!=null)
			{
				// one at a time, thank you.
				return;
			}
			runThread=Thread.currentThread();
		}

		activeMillis=System.currentTimeMillis();
		if((activeMillis>=nextMsdpPing)&&(connectionComplete))
		{
			nextMsdpPing=activeMillis+MSDPPINGINTERVAL;
			doProtocolPings();
		}

		try
		{
			if(killFlag)
				setStatus(SessionStatus.LOGOUT);
			final InputCallback callBack=this.inputCallback;
			if(callBack!=null)
			{
				try
				{
					final String input=readlineContinue();
					if(input != null)
					{
						callBack.setInput(input);
						if(!callBack.waitForInput())
						{
							CMLib.threads().executeRunnable(groupName,new Runnable()
							{
								@Override
								public void run()
								{
									try
									{
										callBack.callBack();
									}
									catch(final Throwable t)
									{
										Log.errOut(t);
									}
								}
							});
						}
					}
					else
					if(callBack.isTimedOut())
					{
						callBack.timedOut();
					}
				}
				catch(final Exception e)
				{

				}
				if(!callBack.waitForInput())
					inputCallback=null;
			}
			else
			switch(status)
			{
			case IDLE:
			case HANDSHAKE_OPEN:
			case HANDSHAKE_MCCP:
			case HANDSHAKE_MXP:
			case HANDSHAKE_MXPPAUSE:
			case HANDSHAKE_DONE:
				break;
			case MAINLOOP:
				mainLoop();
				break;
			case LOGIN:
			case ACCOUNT_MENU:
			case CHARCREATE:
			case LOGIN2:
				loginSystem();
				break;
			case LOGOUT:
			case LOGOUT1:
			case LOGOUT2:
			case LOGOUT3:
			case LOGOUT4:
			case LOGOUT5:
			case LOGOUT6:
			case LOGOUT7:
			case LOGOUT8:
			case LOGOUT9:
			case LOGOUT10:
			case LOGOUT11:
			case LOGOUT12:
			case LOGOUTFINAL:
			{
				inputCallback=null;
				preLogout(mob);
				logoutFinal();
				break;
			}
			}
		}
		catch(final Throwable t)
		{
			Log.errOut(t);
		}
		finally
		{
			synchronized(this)
			{
				runThread=null;
			}
			activeMillis=0;
		}
	}
	
	@Override
	public boolean autoLogin(String name, String password)
	{
		if(name==null)
		{
			if(password!=null)
				return false;
			this.loginSession=null;
			setStatus(SessionStatus.LOGIN);
			return true;
		}
		else
		if(password==null)
			return false;
		name = CMStrings.capitalizeAndLower(name);
		final MOB mob=CMLib.players().getLoadPlayer(name);
		if((mob==null)||(mob.playerStats()==null))
			return false;
		PlayerLibrary.ThinnerPlayer player=new PlayerLibrary.ThinnerPlayer();
		player.password=mob.playerStats().getPasswordStr();
		if(!player.matchesPassword(password))
			return false;
		try
		{
			setMob(mob);
			if(CMLib.login().completePlayerLogin(this,false) == CharCreationLibrary.LoginResult.NORMAL_LOGIN)
			{
				this.nonBlockingIn(false);
			}
			else
			{
				setMob(null);
				return false;
			}
		}
		catch(Exception e)
		{
		}
		return setLoggedInState(LoginResult.NORMAL_LOGIN);
	}
	
	protected boolean setLoggedInState(final LoginResult loginResult)
	{
		setStatus(SessionStatus.LOGIN2);
		if((mob!=null)&&(mob.playerStats()!=null))
			acct=mob.playerStats().getAccount();
		if((!killFlag)&&((mob!=null)))
		{
			if(mob.playerStats()!=null)
				CMLib.threads().suspendResumeRecurse(mob, false, false);
			userLoginTime=System.currentTimeMillis();
			final StringBuilder loginMsg=new StringBuilder("");
			loginMsg.append(getAddress()).append(" "+terminalType)
			.append(((mob.isAttributeSet(MOB.Attrib.MXP)&&getClientTelnetMode(Session.TELNET_MXP)))?" MXP":"")
			.append(getClientTelnetMode(Session.TELNET_MSDP)?" MSDP":"")
			.append(getClientTelnetMode(Session.TELNET_ATCP)?" ATCP":"")
			.append(getClientTelnetMode(Session.TELNET_GMCP)?" GMCP":"")
			.append((getClientTelnetMode(Session.TELNET_COMPRESS)||getClientTelnetMode(Session.TELNET_COMPRESS2))?" CMP":"")
			.append(((mob.isAttributeSet(MOB.Attrib.ANSI)&&getClientTelnetMode(Session.TELNET_ANSI)))?" ANSI":"")
			.append(", character login: "+mob.Name());
			Log.sysOut(loginMsg.toString());
			if(loginResult != CharCreationLibrary.LoginResult.NO_LOGIN)
			{
				final CMMsg msg = CMClass.getMsg(mob,null,CMMsg.MSG_LOGIN,null);
				if(!CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_LOGIN,msg))
					setKillFlag(true);
				else
					CMLib.commands().monitorGlobalMessage(mob.location(), msg);
			}
		}

		needPrompt=true;
		if((!killFlag)&&(mob!=null))
		{
			setStatus(SessionStatus.MAINLOOP);
			return true;
		}
		return false;
	}
	
	public void loginSystem()
	{
		try
		{
			if((loginSession==null)||(loginSession.reset()))
			{
				loginSession = CMLib.login().createLoginSession(this);
				setStatus(SessionStatus.LOGIN);
			}
			else
			if(!loginSession.skipInputThisTime())
			{
				String lastInput = loginSession.acceptInput(this);
				if(lastInput==null)
				{
					if((System.currentTimeMillis()-lastWriteTime)>PINGTIMEOUT)
						rawCharsOut(PINGCHARS);
					return;
				}
				if(!killFlag)
					setInputLoopTime();
			}
			if(loginSession == null)
				killFlag = true;
			else
			if(!killFlag)
			{
				final CharCreationLibrary.LoginResult loginResult=loginSession.loginSystem(this);
				switch(loginResult)
				{
				case INPUT_REQUIRED:
					return;
				case NO_LOGIN:
				{
					mob=null;
					setStatus(SessionStatus.LOGIN);
					return;
				}
				case NORMAL_LOGIN:
				{
					if(setLoggedInState(loginResult))
						return;
				}
				}
				setStatus(SessionStatus.LOGIN);
				return;
			}
			else
			{
				loginSession=null;
			}
			setStatus(SessionStatus.LOGOUT);
		}
		catch(final SocketException e)
		{
			synchronized(sock)
			{
				if(!Log.isMaskedErrMsg(e.getMessage())&&((!killFlag)||((sock!=null)&&sock.isConnected())))
					errorOut(e);
			}
			setStatus(SessionStatus.LOGOUT);
			preLogout(mob);
			setStatus(SessionStatus.LOGOUT1);
		}
		catch(final Exception t)
		{
			synchronized(sock)
			{
				if(!Log.isMaskedErrMsg(t.getMessage())
				&&((!killFlag)
					||(sock!=null&&sock.isConnected())))
					errorOut(t);
			}
			setStatus(SessionStatus.LOGOUT);
			preLogout(mob);
			setStatus(SessionStatus.LOGOUT1);
		}
	}

	public void logoutFinal()
	{
		try
		{
			final MOB M=mob();
			final PlayerAccount acct=this.acct;
			final String finalMsg;
			if(M!=null)
				finalMsg=M.Name()+": ";
			else
			if(acct!=null)
				finalMsg=acct.getAccountName()+": ";
			else
				finalMsg="";
			previousCmd.clear(); // will let system know you are back in login menu
			if(M!=null)
			{
				try
				{
					if(CMSecurity.isDisabled(CMSecurity.DisFlag.LOGOUTS))
					{
						M.setSession(null);
						if(M.location()==null)
							M.setLocation(M.getStartRoom());
						if(M.location()==null)
							M.setLocation(CMLib.map().getStartRoom(M));
						if(M.location()==null)
							M.setLocation(CMLib.map().getRandomRoom());
						CMLib.commands().postSleep(M);
						M.setSession(this);
						M.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SLEEPING);
						M.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SLEEPING);
					}
					else
					{
						M.removeFromGame(true,true);
						M.setSession(null);
						mob=null;
					}
				}
				catch(final Exception e)
				{
					Log.errOut(e);
				}
				finally
				{
				}
			}

			setStatus(SessionStatus.LOGOUT4);
			setKillFlag(true);
			waiting=false;
			needPrompt=false;
			this.acct=null;
			snoops.clear();

			closeSocks(finalMsg);
			setStatus(SessionStatus.LOGOUT5);
		}
		finally
		{
			CMLib.sessions().remove(this);
			setStatus(SessionStatus.LOGOUTFINAL);
		}
	}
	
	private String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	public void mainLoop()
	{
		try
		{
			setInputLoopTime();
			waiting=true;
			String input;
			if(suspendCommandLine)
			{
				input=null;
				return;
			}
			else
				input=readlineContinue();
			if(input==null)
			{
				if((System.currentTimeMillis()-lastWriteTime)>PINGTIMEOUT)
					rawCharsOut(PINGCHARS);
			}
			else
			{
				lastKeystroke=System.currentTimeMillis();
				if(input.trim().length()>0)
					prevMsgs.add(input);
				setAfkFlag(false);
				List<String> CMDS=CMParms.parse(input);
				final MOB mob=mob();
				if((CMDS.size()>0)&&(mob!=null))
				{
					waiting=false;
					final String firstWord=CMDS.get(0);
					final PlayerStats pStats=mob.playerStats();
					final String alias=(pStats!=null)?pStats.getAlias(firstWord):"";
					final List<List<String>> ALL_CMDS=new LinkedList<List<String>>();
					boolean echoOn=false;
					if(alias.length()>0)
					{
						CMDS.remove(0);
						final List<String> all_stuff=CMParms.parseSquiggleDelimited(alias,true);
						echoOn=true;
						if((all_stuff.size()>0)&&(all_stuff.get(0).toString().toLowerCase().startsWith("noecho")))
						{
							echoOn=false;
							all_stuff.set(0, all_stuff.get(0).toString().substring(6).trim());
						}
						for(final String stuff : all_stuff)
						{
							final List<String> THIS_CMDS=new XVector<String>(CMDS);
							ALL_CMDS.add(THIS_CMDS);
							final List<String> preCommands=CMParms.parse(stuff);
							for(int v=preCommands.size()-1;v>=0;v--)
								THIS_CMDS.add(0,preCommands.get(v));
						}
					}
					else
						ALL_CMDS.add(CMDS);
					final double curActions = mob.actions();
					mob.setActions(0.0);
					for(Iterator<List<String>> i=ALL_CMDS.iterator();i.hasNext();)
					{
						CMDS=i.next();
						setPreviousCmd(CMDS);
						milliTotal+=(lastStop-lastStart);

						lastStart=System.currentTimeMillis();
						if(echoOn)
							rawPrintln(CMParms.combineQuoted(CMDS,0));
						final List<List<String>> MORE_CMDS=CMLib.lang().preCommandParser(CMDS);
						for(int m=0;m<MORE_CMDS.size();m++)
							mob.enqueCommand(MORE_CMDS.get(m),metaFlags()|MUDCmdProcessor.METAFLAG_INORDER,0);
						lastStop=System.currentTimeMillis();
					}
					mob.setActions(curActions);
				}
				needPrompt=true;
			}
			if(mob==null)
			{
				if(loginSession != null)
					loginSession.logoutLoginSession();
				setStatus(SessionStatus.LOGIN);
				return;
			}
			while((!killFlag)&&(mob!=null)&&(mob.dequeCommand()))
			{
			}

			if(((System.currentTimeMillis()-lastBlahCheck)>=60000)
			&&(mob()!=null))
			{
				lastBlahCheck=System.currentTimeMillis();
				final Vector<String> V=CMParms.parse(CMProps.getVar(CMProps.Str.IDLETIMERS));
				if((V.size()>0)
				&&(!CMSecurity.isAllowed(mob(),mob().location(),CMSecurity.SecFlag.IDLEOK))
				&&(CMath.s_int(V.firstElement())>0))
				{
					final int minsIdle=(int)(getIdleMillis()/60000);
					if(minsIdle>=CMath.s_int(V.firstElement()))
					{
						println(CMLib.lang().L("\n\r^ZYou are being logged out!^?"));
						stopSession(true,true,false);
					}
					else
					if(minsIdle>=CMath.s_int(V.lastElement()))
					{
						final int remain=CMath.s_int(V.firstElement())-minsIdle;
						println(mob(),null,null,CMLib.lang().L("\n\r^ZIf you don't do something, you will be logged out in @x1 minute(s)!^?",""+remain));
					}
				}

				if(!isAfk())
				{
					if(getIdleMillis()>=600000)
						setAfkFlag(true);
				}
				else
				if((getIdleMillis()>=10800000)&&(!isStopped())&&CMLib.flags().isInTheGame(mob(), true))
				{
					if((!CMLib.flags().isSleeping(mob))
					&&(mob().fetchEffect("Disease_Blahs")==null)
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						final Ability A=CMClass.getAbility("Disease_Blahs");
						if((A!=null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							A.invoke(mob,mob,true,0);
					}
					else
					if((CMLib.flags().isSleeping(mob))
					&&(mob().fetchEffect("Disease_Narcolepsy")==null)
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						final Ability A=CMClass.getAbility("Disease_Narcolepsy");
						if((A!=null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							A.invoke(mob,mob,true,0);
					}
				}
			}
			if((needPrompt)&&(waiting))
			{
				if((mob!=null)&&(mob.isInCombat()))
				{
					if(((System.currentTimeMillis()-promptLastShown)>=CMProps.getTickMillis())
					||(input!=null))
					{
						showPrompt();
						needPrompt=false;
					}
				}
				else
				{
					showPrompt();
					needPrompt=false;
				}
			}
		}
		catch(final SocketException e)
		{
			synchronized(sock)
			{
				if(!Log.isMaskedErrMsg(e.getMessage())&&((!killFlag)||((sock!=null)&&sock.isConnected())))
					errorOut(e);
			}
			setStatus(SessionStatus.LOGOUT);
			preLogout(mob);
			setStatus(SessionStatus.LOGOUT1);
		}
		catch(final Exception t)
		{
			synchronized(sock)
			{
				if((!Log.isMaskedErrMsg(t.getMessage()))
				&&((!killFlag)
					||(sock!=null&&sock.isConnected())))
					errorOut(t);
			}
			setStatus(SessionStatus.LOGOUT);
			preLogout(mob);
			setStatus(SessionStatus.LOGOUT1);
		}
	}

	@Override
	public long activeTimeMillis()
	{
		if(activeMillis==0)
			return 0;
		return System.currentTimeMillis()-activeMillis;
	}

	public static class LoginLogoutThread implements CMRunnable, Tickable
	{
		private final long startTime=System.currentTimeMillis();

		@Override
		public String name()
		{
			return (theMOB == null) ? "Dead LLThread" : "LLThread for " + theMOB.Name();
		}

		@Override
		public boolean tick(Tickable ticking, int tickID)
		{
			return false;
		}

		@Override
		public String ID()
		{
			return name();
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
				return new LoginLogoutThread();
			}
		}

		@Override
		public void initializeClass()
		{
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
		public int compareTo(CMObject o)
		{
			return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
		}

		@Override
		public int getTickStatus()
		{
			return 0;
		}

		@Override
		public long getStartTime()
		{
			return startTime;
		}

		@Override
		public int getGroupID()
		{
			return '0';
		}
		
		private MOB				theMOB			= null;
		private int				msgCode			= -1;
		private final Set<Room>	skipRooms		= new HashSet<Room>();
		private long			activeMillis	= 0;

		private LoginLogoutThread()
		{
		}

		public LoginLogoutThread(MOB mob, int msgC)
		{
			theMOB=mob;
			msgCode=msgC;
		}

		public void initialize()
		{
			final Set<MOB> group=theMOB.getGroupMembers(new HashSet<MOB>());
			skipRooms.clear();
			for (final Object element : group)
			{
				final MOB M=(MOB)element;
				if((M.location()!=null)&&(!skipRooms.contains(M.location())))
					skipRooms.add(M.location());
			}
			if((!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
			&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			{
				final CMMsg msg=CMClass.getMsg(theMOB,null,msgCode,null);
				Room R=theMOB.location();
				if(R!=null)
					skipRooms.remove(R);
				try
				{
					if((R!=null)&&(theMOB.location()!=null))
						R.send(theMOB,msg);
					for(final Iterator i=skipRooms.iterator();i.hasNext();)
					{
						R=(Room)i.next();
						if(theMOB.location()!=null)
							R.sendOthers(theMOB,msg);
					}
					if(R!=null)
						skipRooms.add(R);
				}
				catch (final Exception e)
				{
				}
			}
		}

		@Override
		public void run()
		{
			activeMillis=System.currentTimeMillis();
			if((!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
			&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			{
				final CMMsg msg=CMClass.getMsg(theMOB,null,msgCode,null);
				Room R=null;
				try
				{
					for(final Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						R=(Room)e.nextElement();
						if((!skipRooms.contains(R))&&(theMOB.location()!=null))
							R.sendOthers(theMOB,msg);
					}
				}
				catch(final Exception e)
				{
				}
				theMOB=null;
			}
		}

		@Override
		public long activeTimeMillis()
		{
			return (activeMillis>0)?System.currentTimeMillis()-activeMillis:0;
		}
	}

	@Override
	public void setIdleTimers()
	{
		this.lastStr=""; // also resets spam counter
		this.spamStack=0;
		lastKeystroke=System.currentTimeMillis();
		lastWriteTime=System.currentTimeMillis();
	}

	private static enum SESS_STAT_CODES {PREVCMD,ISAFK,AFKMESSAGE,ADDRESS,IDLETIME,
										 LASTMSG,LASTNPCFIGHT,LASTPKFIGHT,TERMTYPE,
										 TOTALMILLIS,TOTALTICKS,WRAP,LASTLOOPTIME,
										 ROOMLOOK}

	@Override
	public int getSaveStatIndex()
	{
		return SESS_STAT_CODES.values().length;
	}

	@Override
	public String[] getStatCodes()
	{
		return CMParms.toStringArray(SESS_STAT_CODES.values());
	}

	@Override
	public boolean isStat(String code)
	{
		return getStatIndex(code) != null;
	}

	private SESS_STAT_CODES getStatIndex(String code)
	{
		return (SESS_STAT_CODES) CMath.s_valueOf(SESS_STAT_CODES.values(), code);
	}

	@Override
	public String getStat(String code)
	{
		final SESS_STAT_CODES stat = getStatIndex(code);
		if (stat == null)
		{
			return "";
		}
		switch (stat)
		{
		case PREVCMD:
			return CMParms.combineQuoted(getPreviousCMD(), 0);
		case ISAFK:
			return "" + isAfk();
		case AFKMESSAGE:
			return getAfkMessage();
		case ADDRESS:
			return getAddress();
		case IDLETIME:
			return CMLib.time().date2String(System.currentTimeMillis() - getIdleMillis());
		case LASTMSG:
			return CMParms.combineQuoted(getLastMsgs(), 0);
		case LASTNPCFIGHT:
			return CMLib.time().date2String(getLastNPCFight());
		case LASTPKFIGHT:
			return CMLib.time().date2String(getLastPKFight());
		case TERMTYPE:
			return getTerminalType();
		case TOTALMILLIS:
			return CMLib.time().date2String(System.currentTimeMillis() - getTotalMillis());
		case TOTALTICKS:
			return "" + getTotalTicks();
		case WRAP:
			return "" + getWrap();
		case LASTLOOPTIME:
			return CMLib.time().date2String(getInputLoopTime());
		case ROOMLOOK:
			break; // do nothing
		default:
			Log.errOut("Session", "getStat:Unhandled:" + stat.toString());
			break;
		}
		return null;
	}

	@Override
	public void setStat(String code, String val)
	{
		final SESS_STAT_CODES stat = getStatIndex(code);
		if (stat == null)
		{
			return;
		}
		switch (stat)
		{
		case PREVCMD:
			previousCmd = CMParms.parse(val);
			break;
		case ISAFK:
			afkFlag = CMath.s_bool(val);
			break;
		case AFKMESSAGE:
			afkMessage = val;
			break;
		case ADDRESS:
			return;
		case IDLETIME:
			lastKeystroke = CMLib.time().string2Millis(val);
			break;
		case LASTMSG:
			prevMsgs = CMParms.parse(val);
			break;
		case LASTNPCFIGHT:
			lastNPCFight = CMLib.time().string2Millis(val);
			break;
		case LASTPKFIGHT:
			lastPKFight = CMLib.time().string2Millis(val);
			break;
		case TERMTYPE:
			terminalType = val;
			break;
		case TOTALMILLIS:
			milliTotal = System.currentTimeMillis() - CMLib.time().string2Millis(val);
			break;
		case TOTALTICKS:
			tickTotal = CMath.s_int(val);
			break;
		case WRAP:
			if ((mob != null) && (mob.playerStats() != null))
				mob.playerStats().setWrap(CMath.s_int(val));
			break;
		case LASTLOOPTIME:
			lastLoopTop = CMLib.time().string2Millis(val);
			break;
		case ROOMLOOK:
			if(getClientTelnetMode(TELNET_GMCP))
			{
				final byte[] gmcpPingBuf=CMLib.protocol().invokeRoomChangeGmcp(this, gmcpPings, gmcpSupports);
				if(gmcpPingBuf!=null)
				{
					try
					{
						rawBytesOut(rawout, gmcpPingBuf);
					}
					catch (final IOException e)
					{
					}
				}
			}
			break;
		default:
			Log.errOut("Session", "setStat:Unhandled:" + stat.toString());
			break;
		}
	}

	private static class SesInputStream extends InputStream
	{
		private final int[] bytes;
		private int start=0;
		private int end=0;
		protected SesInputStream(int maxBytesPerChar)
		{
			bytes=new int[maxBytesPerChar+1];
		}

		@Override
		public int read() throws IOException
		{
			if(start==end)
				throw new java.io.InterruptedIOException();
			final int b=bytes[start];
			if(start==bytes.length-1)
				start=0;
			else
				start++;
			return b;
		}

		public void write(int b)
		{
			bytes[end]=b;
			if(end==bytes.length-1)
				end=0;
			else
				end++;
		}
	}
}
