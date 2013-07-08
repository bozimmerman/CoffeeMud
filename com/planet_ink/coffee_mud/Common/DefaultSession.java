package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
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
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.*;
import java.net.*;
import java.nio.charset.Charset;

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
@SuppressWarnings({"unchecked","rawtypes"})
public class DefaultSession implements Session
{
	protected static final int	   SOTIMEOUT		= 300;
	protected static final int	   PINGTIMEOUT  	= 30000;
	protected static final int	   MSDPPINGINTERVAL	= 1000;
	protected static final byte[]  TELNETGABYTES	= {(byte)TELNET_IAC,(byte)TELNET_GA};
	protected static final char[]  PINGCHARS		= {TELNET_IAC,TELNET_NOP};
	private final HashSet		   telnetSupportSet = new HashSet();
	private static final HashSet   mxpSupportSet	= new HashSet();
	private static final Hashtable mxpVersionInfo   = new Hashtable();
	private static final String	   TIMEOUT_MSG		= "Timed Out.";
	
	
	private volatile Thread  runThread 			 = null;
	private volatile Thread	 writeThread 		 = null;
	protected volatile int   status 			 = 0;
	protected int   		 snoopSuspensionStack= 0;
	protected final Socket[] sock				 = new Socket[1];
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
	protected StringBuffer   preliminaryInput	 = new StringBuffer("");
	protected StringBuffer   fakeInput			 = null;
	protected boolean   	 waiting			 = false;
	protected List<String>   previousCmd		 = new Vector<String>();
	protected String[]  	 clookup			 = null;
	protected String		 lastColorStr		 = "";
	protected String		 lastStr			 = null;
	protected int   		 spamStack			 = 0;
	protected List  		 snoops				 = new Vector();
	protected List<String>   prevMsgs			 = new Vector<String>();
	protected StringBuffer   curPrevMsg			 = null;
	protected boolean   	 lastWasCR			 = false;
	protected boolean   	 lastWasLF			 = false;
	protected boolean   	 suspendCommandLine	 = false;
	protected boolean[] 	 serverTelnetCodes	 = new boolean[256];
	protected boolean[] 	 clientTelnetCodes	 = new boolean[256];
	protected String		 terminalType		 = "UNKNOWN";
	protected int   		 terminalWidth		 = -1;
	protected int   		 terminalHeight		 = -1;
	protected long  		 writeStartTime		 = 0;
	protected boolean   	 bNextByteIs255		 = false;
	protected boolean   	 connectionComplete	 = false;
	protected ReentrantLock  writeLock 			 = new ReentrantLock(true);
	protected LoginSession	 loginSession 		 = null;
	
	protected final Map<Object, Object> msdpReportables = new TreeMap<Object,Object>();

	protected ColorState	 currentColor		 = ColorLibrary.COLORSTATE_NORMAL;
	protected ColorState	 lastColor			 = ColorLibrary.COLORSTATE_NORMAL;
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
	protected long			 promptLastShown	 = 0;
	protected volatile long  lastWriteTime		 = System.currentTimeMillis();
	protected boolean   	 debugOutput		 = false;
	protected boolean   	 debugInput			 = false;
	protected StringBuffer   debugInputBuf		 = new StringBuffer("");
	protected volatile InputCallback inputCallback=null;

	public String ID(){return "DefaultSession";}
	public String name() { return ID();}
	public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new DefaultSession();}}
	public void initializeClass(){}
	public boolean isFake() { return false;}
	public CMObject copyOf(){ try{ Object O=this.clone(); return (CMObject)O;}catch(Exception e){return newInstance();} }
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public char threadGroupChar = '\0';

	public DefaultSession()
	{
		threadGroupChar=Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	public void initializeSession(Socket s, String introTextStr)
	{
		sock[0]=s;
		try
		{
			debugOutput = CMSecurity.isDebugging(CMSecurity.DbgFlag.BINOUT);
			debugInput = CMSecurity.isDebugging(CMSecurity.DbgFlag.BININ);
			if(debugInput)
				CMLib.threads().startTickDown(new Tickable(){
					@Override public String ID() { return "SessionTicker";}
					@Override public CMObject newInstance() { return null; }
					@Override public CMObject copyOf() { return null; }
					@Override public void initializeClass() {}
					@Override public int compareTo(CMObject o) { return 0;}
					@Override public String name() { return ID(); }
					@Override public long getTickStatus() { return 0; }
					@Override public boolean tick(Tickable ticking, int tickID) {
						if(debugInputBuf.length()>0)
						{
							Log.sysOut("INPUT: '"+debugInputBuf.toString()+"'");
							debugInputBuf.setLength(0);
						}
						return !killFlag;
					} }, 0, 100, 1);

			sock[0].setSoTimeout(SOTIMEOUT);
			rawout=sock[0].getOutputStream();
			rawin=sock[0].getInputStream();
			
			setServerTelnetMode(TELNET_ANSI,true);
			setClientTelnetMode(TELNET_ANSI,true);
			setClientTelnetMode(TELNET_TERMTYPE,true);
			negotiateTelnetMode(rawout,TELNET_TERMTYPE);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MCCP))
				changeTelnetMode(rawout,TELNET_COMPRESS2,true);

			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP))
				changeTelnetMode(rawout,TELNET_MXP,true);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP))
				changeTelnetMode(rawout,TELNET_MSP,true);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSDP))
				changeTelnetMode(rawout,TELNET_MSDP,true);
			//changeTelnetMode(rawout,TELNET_SUPRESS_GO_AHEAD,true);
			changeTelnetMode(rawout,TELNET_NAWS,true);
			//changeTelnetMode(rawout,TELNET_BINARY,true);
			preliminaryRead(250);
			if((!terminalType.equalsIgnoreCase("ANSI"))&&(clientTelnetMode(TELNET_ECHO)))
				changeTelnetModeBackwards(rawout,TELNET_ECHO,false);
			rawout.flush();
			preliminaryRead(250);

			Charset charSet=Charset.forName(CMProps.getVar(CMProps.Str.CHARSETINPUT));
			inMaxBytesPerChar=(int)Math.round(Math.ceil(charSet.newEncoder().maxBytesPerChar()));
			charWriter=new SesInputStream(inMaxBytesPerChar);
			in=new BufferedReader(new InputStreamReader(charWriter,charSet));
			out=new PrintWriter(new OutputStreamWriter(rawout,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));

			preliminaryRead(250);
			if(clientTelnetMode(TELNET_COMPRESS2))
			{
				out.flush();
				rawout.flush();
				preliminaryRead(250);
				out.flush();
				rawout.flush();
				preliminaryRead(250);
				negotiateTelnetMode(rawout,TELNET_COMPRESS2);
				out.flush();
				rawout.flush();
				preliminaryRead(250);
				ZOutputStream zOut=new ZOutputStream(rawout, JZlib.Z_DEFAULT_COMPRESSION);
				rawout=zOut;
				zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
				out = new PrintWriter(new OutputStreamWriter(zOut,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
				try{Thread.sleep(50);}catch(Exception e){}
			}
			else
			{
				if(out==null) return;
				out.flush();
				rawout.flush();
				preliminaryRead(500);
			}
			if(clientTelnetMode(Session.TELNET_MXP))
			{
				rawOut("\n\033[6z\n\033[6z<SUPPORT IMAGE IMAGE.URL>\n");
				if(out==null) return;
				out.flush();
				rawout.flush();
				preliminaryRead(1000);
			}
			preliminaryRead(500);
			if(introTextStr!=null)
				print(introTextStr);
			if((clientTelnetMode(Session.TELNET_MXP))
			&&((mxpSupportSet.contains("+IMAGE.URL"))
				||((mxpSupportSet.contains("+IMAGE"))&&(!mxpSupportSet.contains("-IMAGE.URL")))))
			{
				// also the intro page
				String[] paths=CMProps.mxpImagePath("intro.jpg");
				if(paths[0].length()>0)
				{
					CMFile introDir=new CMFile("/web/pub/images/mxp",null,false,true);
					String introFilename=paths[1];
					if(introDir.isDirectory())
					{
						CMFile[] files=introDir.listFiles();
						Vector choices=new Vector();
						for(int f=0;f<files.length;f++)
							if(files[f].getName().toLowerCase().startsWith("intro")
							&&files[f].getName().toLowerCase().endsWith(".jpg"))
								choices.addElement(files[f].getName());
						if(choices.size()>0) introFilename=(String)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
					}
					println("\n\r\n\r\n\r^<IMAGE '"+introFilename+"' URL='"+paths[0]+"' H=400 W=400^>\n\r\n\r");
				}
			}
			preliminaryRead(100);
			connectionComplete=true;
			status=Session.STATUS_LOGIN;
		}
		catch(Exception e)
		{
			if(e.getMessage()==null)
				Log.errOut(e);
			else
				Log.errOut(e.getMessage());
		}
		if(preliminaryInput.length()>0)
			fakeInput=preliminaryInput;
		preliminaryInput=null;
	}

	private void preliminaryRead(long timeToWait)
	{
		try{
			long time=System.currentTimeMillis();
			while((System.currentTimeMillis()-time)<timeToWait)
			{
				String s=blockingIn(timeToWait);
				if((s!=null)&&(s.length()>0))
					preliminaryInput.append(s+"\n");
			}
		}catch(Exception e){}
	}

	private void negotiateTelnetMode(OutputStream out, int optionCode)
	throws IOException
	{
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
			Log.debugOut("Sent sub-option: "+Session.TELNET_DESCS[optionCode]);
		if(optionCode==TELNET_TERMTYPE)
		{
			byte[] stream={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)optionCode,(byte)1,(byte)TELNET_IAC,(byte)TELNET_SE};
			rawBytesOut(out, stream);
		}
		else
		{
			byte[] stream={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)optionCode,(byte)TELNET_IAC,(byte)TELNET_SE};
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
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_TERMTYPE));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_BINARY));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_ECHO));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_LOGOUT));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_TERMTYPE));
			telnetSupportSet.add(Integer.valueOf(Session.TELNET_NAWS));
			//telnetSupportSet.add(Integer.valueOf(Session.TELNET_COMPRESS2));
			//telnetSupportSet.add(Integer.valueOf(Session.TELNET_LINEMODE));
		}
		return telnetSupportSet.contains(Integer.valueOf(telnetCode));
	}

	public void setServerTelnetMode(int telnetCode, boolean onOff)
	{ serverTelnetCodes[telnetCode]=onOff; }
	public boolean serverTelnetMode(int telnetCode)
	{ return serverTelnetCodes[telnetCode]; }
	public void setClientTelnetMode(int telnetCode, boolean onOff)
	{ clientTelnetCodes[telnetCode]=onOff; }
	public boolean clientTelnetMode(int telnetCode)
	{ return clientTelnetCodes[telnetCode]; }
	private void changeTelnetMode(OutputStream out, int telnetCode, boolean onOff) throws IOException
	{
		byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_WILL:(byte)TELNET_WONT,(byte)telnetCode};
		rawBytesOut(out, command);
		out.flush();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Sent: "+(onOff?"Will":"Won't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}
	// this is stupid, but a printwriter can not be cast as an outputstream, so this dup was necessary
	public void changeTelnetMode(int telnetCode, boolean onOff)
	{
		try
		{
			byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_WILL:(byte)TELNET_WONT,(byte)telnetCode};
			out.flush();
			rawBytesOut(rawout, command);
			rawout.flush();
		}
		catch(Exception e){}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Sent: "+(onOff?"Will":"Won't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}
	public void changeTelnetModeBackwards(int telnetCode, boolean onOff) throws IOException
	{
		byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_DO:(byte)TELNET_DONT,(byte)telnetCode};
		out.flush();
		rawBytesOut(rawout, command);
		rawout.flush();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Back-Sent: "+(onOff?"Do":"Don't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}
	public void changeTelnetModeBackwards(OutputStream out, int telnetCode, boolean onOff) throws IOException
	{
		byte[] command={(byte)TELNET_IAC,onOff?(byte)TELNET_DO:(byte)TELNET_DONT,(byte)telnetCode};
		rawBytesOut(out, command);
		out.flush();
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Back-Sent: "+(onOff?"Do":"Don't")+" "+Session.TELNET_DESCS[telnetCode]);
		setServerTelnetMode(telnetCode,onOff);
	}
	public void negotiateTelnetMode(int telnetCode)
	{
		try
		{
			out.flush();
			if(telnetCode==TELNET_TERMTYPE)
			{
				byte[] command={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)telnetCode,(byte)1,(byte)TELNET_IAC,(byte)TELNET_SE};
				rawBytesOut(rawout, command);
			}
			else
			{
				byte[] command={(byte)TELNET_IAC,(byte)TELNET_SB,(byte)telnetCode,(byte)TELNET_IAC,(byte)TELNET_SE};
				rawBytesOut(rawout, command);
			}
			rawout.flush();
		}
		catch(Exception e){}
		if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Negotiate-Sent: "+Session.TELNET_DESCS[telnetCode]);
	}

	public void initTelnetMode(int mobbitmap)
	{
		setServerTelnetMode(TELNET_ANSI,CMath.bset(mobbitmap,MOB.ATT_ANSI));
		setClientTelnetMode(TELNET_ANSI,CMath.bset(mobbitmap,MOB.ATT_ANSI));
		boolean changedSomething=false;
		boolean mxpSet=(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP))&&CMath.bset(mobbitmap,MOB.ATT_MXP);
		if(mxpSet!=clientTelnetMode(TELNET_MXP))
		{ changeTelnetMode(TELNET_MXP,!clientTelnetMode(TELNET_MXP)); changedSomething=true;}
		boolean mspSet=(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP))&&CMath.bset(mobbitmap,MOB.ATT_SOUND);
		if(mspSet!=clientTelnetMode(TELNET_MSP))
		{ changeTelnetMode(TELNET_MSP,!clientTelnetMode(TELNET_MSP)); changedSomething=true;}
		try{if(changedSomething) blockingIn(500);}catch(Exception e){}
	}

	public ColorState currentColor(final ColorState newColor)
	{
		if(newColor!=null)
			currentColor=newColor;
		return currentColor;
	}
	
	public ColorState lastColor(final ColorState newColor)
	{
		if(newColor!=null)
			lastColor=newColor;
		return lastColor;
	}
	public long getTotalMillis(){ return milliTotal;}
	public long getIdleMillis(){ return System.currentTimeMillis()-lastKeystroke;}
	public long getTotalTicks(){ return tickTotal;}
	public long getMillisOnline(){ return System.currentTimeMillis()-onlineTime;}

	public long lastLoopTime(){ return lastLoopTop;}
	public void updateLoopTime(){ lastLoopTop=System.currentTimeMillis();}
	public long getLastPKFight(){return lastPKFight;}
	public void setLastPKFight(){lastPKFight=System.currentTimeMillis();}
	public long getLastNPCFight(){return lastNPCFight;}
	public void setLastNPCFight(){lastNPCFight=System.currentTimeMillis();}
	public List<String> getLastMsgs(){return new XVector(prevMsgs);}

	public String getTerminalType(){ return terminalType;}
	public MOB mob(){return mob;}
	public void setMob(MOB newmob)
	{ 
		mob=newmob;
	}
	public void setAccount(PlayerAccount account)
	{
		acct=account;
	}
	public int getWrap()
	{
		if(terminalWidth>5) return terminalWidth;
		return ((mob!=null)&&(mob.playerStats()!=null))?mob.playerStats().getWrap():78;
	}
	public int getPageBreak()
	{
		if(((mob!=null)&&(mob.playerStats()!=null)))
		{
			final int pageBreak=mob.playerStats().getPageBreak();
			if(pageBreak <= 0) return pageBreak;
			if(terminalHeight>3) return terminalHeight;
			return pageBreak;
		}
		return -1;
	}
	public boolean isStopped()
	{
		return killFlag;
	}
	public void setKillFlag(boolean truefalse)
	{ 
		killFlag=truefalse;
	}
	public List<String> previousCMD(){return previousCmd;}
	public void startBeingSnoopedBy(Session S)
	{
		if(!snoops.contains(S))
			snoops.add(S);
	}
	public void stopBeingSnoopedBy(Session S)
	{
		while(snoops.contains(S))
			snoops.remove(S);
	}
	public boolean amBeingSnoopedBy(Session S)
	{
		if(S==null) return snoops.size()==0;
		return(snoops.contains(S));
	}
	public synchronized int snoopSuspension(int change){
		snoopSuspensionStack+=change;
		return snoopSuspensionStack;
	}

	private int metaFlags() {
		return ((snoops.size()>0)?Command.METAFLAG_SNOOPED:0)
			   |(((mob!=null)&&(mob.soulMate()!=null))?Command.METAFLAG_POSSESSED:0);
	}

	public void setPreviousCmd(List cmds)
	{
		if(cmds==null) return;
		if(cmds.size()==0) return;
		if((cmds.size()>0)&&(((String)cmds.get(0)).trim().startsWith("!")))
			return;

		previousCmd.clear();
		for(int i=0;i<cmds.size();i++)
			previousCmd.add(((String)cmds.get(i)));
	}

	public boolean afkFlag(){return afkFlag;}
	public void setAfkFlag(boolean truefalse)
	{
		if(afkFlag==truefalse) return;
		afkFlag=truefalse;
		if(afkFlag)
			println("\n\rYou are now listed as AFK.");
		else
		{
			afkMessage=null;
			println("\n\rYou are no longer AFK.");
		}
	}
	public String afkMessage()
	{
		if(mob==null) return "";
		if((afkMessage==null)||(CMStrings.removeColors(afkMessage).trim().length()==0))
			return mob.name()+" is AFK at the moment.";
		return afkMessage;
	}
	public void setAFKMessage(String str){afkMessage=str;}

	protected void errorOut(Exception t)
	{
		Log.errOut(t);
		CMLib.sessions().remove(this);
		setKillFlag(true);
	}

	protected long getWriteStartTime(){return writeStartTime;}
	public boolean isLockedUpWriting(){
		long time=writeStartTime;
		if(time==0) return false;
		return ((System.currentTimeMillis()-time)>10000);
	}
	
	public final void rawBytesOut(final OutputStream out, final byte[] bytes) throws IOException
	{
		try
		{
			if(debugOutput && Log.debugChannelOn())
			{
				StringBuilder str=new StringBuilder("OUTPUT: '");
				for(byte c : bytes)
					str.append(c).append(" ");
				Log.debugOut( str.toString()+"'");
			}
			out.write(bytes);
		}
		finally
		{
			lastWriteTime=System.currentTimeMillis();
		}
	}

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
					if(debugOutput && Log.debugChannelOn())
					{
						StringBuilder str=new StringBuilder("OUTPUT: '");
						for(char c : chars)
							str.append(c);
						Log.debugOut( str.toString()+"'");
					}
					out.write(chars);
					if(out.checkError())
						stopSession(true,true,true);
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
		catch(Exception ioe){ setKillFlag(true);}
	}
	
	public void rawCharsOut(String c)
	{
		if(c!=null) 
			rawCharsOut(c.toCharArray());
	}
	public void rawCharsOut(char c)
	{
		char[] cs={c}; 
		rawCharsOut(cs);
	}
	public void snoopSupportPrint(final String msg, final boolean noCache)
	{
		try{
			if((snoops.size()>0)&&(snoopSuspensionStack<=0))
			{
				String msgColored;
				String preFix=CMLib.coffeeFilter().colorOnlyFilter("^Z"+((mob==null)?"?":mob.Name())+":^N ",this);
				final int crCheck=msg.indexOf('\n');
				if((crCheck>=0)&&(crCheck<msg.length()-2))
				{
					StringBuffer buf=new StringBuffer(msg);
					for(int i=buf.length()-1;i>=0;i--)
						if((buf.charAt(i)=='\n')&&(i<buf.length()-2)&&(buf.charAt(i+1)=='\r'))
							buf.insert(i+2, preFix);
					msgColored=buf.toString();
				}
				else
					msgColored=msg;
				for(int s=0;s<snoops.size();s++)
					((Session)snoops.get(s)).onlyPrint(preFix+msgColored,noCache);
			}
		}catch(IndexOutOfBoundsException x){}
		
	}
	
	public void onlyPrint(String msg)
	{
		onlyPrint(msg,false);
	}
	public void onlyPrint(String msg, boolean noCache)
	{
		if((out==null)||(msg==null)) 
			return;
		try
		{
			snoopSupportPrint(msg,noCache);
			String newMsg=CMLib.lang().finalTranslation(msg);
			if(newMsg!=null) msg=newMsg;

			if(msg.endsWith("\n\r")
			&&(msg.equals(lastStr))
			&&(msg.length()>2)
			&&(msg.indexOf("\n")==(msg.length()-2)))
			{ spamStack++; return; }
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
				int pageBreak=getPageBreak();
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
							try{ 
								String s=blockingIn(-1); 
								if(s!=null)
								{
									s=s.toLowerCase();
									if(s.startsWith("qu")||s.startsWith("ex")||s.equals("x"))
										return;
								}
							}catch(Exception e){return;}
						}
					}
				}
			}

			// handle line cache --
			if(!noCache)
			for(int i=0;i<msg.length();i++)
			{
				if(curPrevMsg==null) curPrevMsg=new StringBuffer("");
				if(msg.charAt(i)=='\r') continue;
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
		catch(java.lang.NullPointerException e){}
	}

	public void rawOut(String msg)
	{
		rawCharsOut(msg);
	}
	public void rawPrint(String msg)
	{
		if(msg==null)
			return;
		onlyPrint((needPrompt?"":"\n\r")+msg,false);
		needPrompt=true;
	}

	public void print(String msg)
	{
		onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false),false);
	}
	
	public void promptPrint(String msg)
	{
		print(msg);
		if((!clientTelnetMode(TELNET_SUPRESS_GO_AHEAD)) && (!killFlag))
			try { rawBytesOut(rawout, TELNETGABYTES); } catch(Exception e){}
	}

	public void rawPrintln(String msg)
	{
		if(msg!=null)
			rawPrint(msg+"\n\r");
	}

	public void stdPrint(String msg)
	{
		rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false)); 
	}

	public void print(Physical src, Environmental trg, Environmental tol, String msg)
	{
		onlyPrint((CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)),false);
	}

	public void stdPrint(Physical src, Environmental trg, Environmental tol, String msg)
	{
		rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,trg,msg,false)); 
	}

	public void println(String msg)
	{
		if(msg!=null)
			print(msg+"\n\r");
	}

	public void wraplessPrintln(String msg)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,true)+"\n\r",false);
		needPrompt=true;
	}

	public void wraplessPrint(String msg)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,true),false);
		needPrompt=true;
	}

	public void colorOnlyPrintln(String msg)
	{
		colorOnlyPrint(msg,false);
	}
	public void colorOnlyPrintln(String msg, boolean noCache)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().colorOnlyFilter(msg,this)+"\n\r",noCache);
		needPrompt=true;
	}

	public void colorOnlyPrint(String msg)
	{
		colorOnlyPrint(msg,false);
	}
	public void colorOnlyPrint(String msg, boolean noCache)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().colorOnlyFilter(msg,this),noCache);
		needPrompt=true;
	}

	public void stdPrintln(String msg)
	{
		if(msg!=null)
			rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false)+"\n\r");
	}

	public void println(Physical src, Environmental trg, Environmental tol, String msg)
	{
		if(msg!=null)
			onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)+"\n\r",false);
	}

	public void stdPrintln(Physical src,Environmental trg, Environmental tol, String msg)
	{
		if(msg!=null)
			rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)+"\n\r");
	}

	public void setPromptFlag(boolean truefalse)
	{
		needPrompt=truefalse;
	}

	public String prompt(String Message, String Default, long maxTime)
		throws IOException
	{
		String Msg=prompt(Message,maxTime).trim();
		if(Msg.equals(""))
			return Default;
		return Msg;
	}

	public String prompt(String Message, String Default)
		throws IOException
	{
		String Msg=prompt(Message,-1).trim();
		if(Msg.equals(""))
			return Default;
		return Msg;
	}

	public void prompt(InputCallback callBack)
	{
		if(callBack!=null)
		{
			callBack.showPrompt();
			try {	
				if(!clientTelnetMode(TELNET_SUPRESS_GO_AHEAD)) 
					rawBytesOut(rawout, TELNETGABYTES); 
			} catch (IOException e) { }
		}
		this.inputCallback=callBack;
		this.status=this.status|STATUSMASK_WAITING_FOR_INPUT;
	}

	public String prompt(String Message, long maxTime) 
			throws IOException
	{
		promptPrint(Message);
		String input=blockingIn(maxTime);
		if(input==null) return "";
		if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
			return input.substring(0,input.length()-1);
		return input;
	}

	public String prompt(String Message)
		throws IOException
	{
		promptPrint(Message);
		String input=blockingIn(-1);
		if(input==null) return "";
		if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
			return input.substring(0,input.length()-1);
		return input;
	}

	public String[] clookup()
	{
		if(clookup==null)
			clookup=CMLib.color().standardColorLookups();

		if(mob()==null) return clookup;
		PlayerStats pstats=mob().playerStats();
		if((mob.soulMate()!=null)&&(mob.soulMate().playerStats()!=null))
			pstats=mob.soulMate().playerStats();
		if(pstats==null) return clookup;

		if(!pstats.getColorStr().equals(lastColorStr))
		{
			if(pstats.getColorStr().length()==0)
				clookup=CMLib.color().standardColorLookups();
			else
			{
				String changes=pstats.getColorStr();
				lastColorStr=changes;
				clookup=CMLib.color().standardColorLookups().clone();
				int x=changes.indexOf('#');
				while(x>0)
				{
					String sub=changes.substring(0,x);
					changes=changes.substring(x+1);
					clookup[sub.charAt(0)]=CMLib.color().translateCMCodeToANSI(sub.substring(1));
					x=changes.indexOf('#');
				}
				for(int i=0;i<clookup.length;i++)
				{
					String s=clookup[i];
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
					if(terminalType.equalsIgnoreCase("ANSI"))
						changeTelnetMode(rawout,TELNET_ECHO,true);
					else
					if(terminalType.toLowerCase().startsWith("mushclient")&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
						negotiateTelnetMode(rawout,TELNET_MXP);
				}
				else
				if (suboptionData[0] == 1) // Request for data.
				{/* No idea how to handle this, ignore it for now.*/}
			}
			break;
		case TELNET_NAWS:
			if (dataSize == 4)  // It should always be 4.
			{
				terminalWidth = ((suboptionData[0] << 8) | suboptionData[1])-2;
				terminalHeight = (suboptionData[2] << 8) | suboptionData[3];
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("For suboption "+Session.TELNET_DESCS[optionCode]+", got: "+terminalWidth+"x"+terminalHeight);
			}
			break;
		case TELNET_MSDP:
			{
				byte[] resp=CMLib.utensils().processMsdp(this, suboptionData, dataSize, this.msdpReportables);
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("For suboption "+Session.TELNET_DESCS[optionCode]+", got "+dataSize+" bytes, sent "+((resp==null)?0:resp.length));
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
		if((in==null)||(out==null)) return;
		int c=readByte();
		if((char)c!='[') return;

		boolean quote=false;
		StringBuffer escapeStr=new StringBuffer("");
		while(((c=readByte())!=-1)
		&&(!killFlag)
		&&((quote)||(!Character.isLetter((char)c))))
		{
			escapeStr.append((char)c);
			if(c=='"') quote=!quote;
		}
		if(c==-1) return;
		escapeStr.append((char)c);
		String esc=escapeStr.toString().trim();
		// at the moment, we only handle mxp escapes
		// everything else is effectively EATEN
		if(!esc.endsWith("z")) return;
		esc=esc.substring(0,esc.length()-1);
		if(!CMath.isNumber(esc)) return;
		int escNum=CMath.s_int(esc);
		// only LINE-based mxp escape sequences are respected
		if(escNum>3) return;
		sock[0].setSoTimeout(30000);
		StringBuffer line=new StringBuffer("");
		while(((c=readByte())!=-1)&&(!killFlag))
		{
			if(c=='\n') break;
			line.append((char)c);
		}
		sock[0].setSoTimeout(SOTIMEOUT);
		String l=line.toString().toUpperCase().trim();
		// now we have the line, so parse out tags -- only tags matter!
		while(l.length()>0)
		{
			int tagStart=l.indexOf('<');
			if(tagStart<0) return;
			int tagEnd=l.indexOf('>');
			if(tagEnd<0) return;
			String tag=l.substring(tagStart+1,tagEnd).trim();
			l=l.substring(tagEnd+1).trim();
			// now we have a tag, and its parameters (space delimited)
			Vector parts=CMParms.parseSpaces(tag,true);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Got secure MXP tag: "+tag);
			if(parts.size()>1)
			{
				tag=(String)parts.firstElement();
				if(tag.equals("VERSION"))
				{
					for(int p=1;p<parts.size();p++)
					{
						String pp=(String)parts.elementAt(p);
						int x=pp.indexOf('=');
						if(x<0) continue;
						mxpVersionInfo.remove(pp.substring(0,x).trim());
						mxpVersionInfo.put(pp.substring(0,x).trim(),pp.substring(x+1).trim());
					}
				}
				else
				if(tag.equals("SUPPORTS"))
					for(int p=1;p<parts.size();p++)
						mxpSupportSet.add(parts.elementAt(p));
				else
				if(tag.equals("SHUTDOWN"))
				{
					MOB M=CMLib.players().getLoadPlayer((String)parts.elementAt(1));
					if((M!=null)
					&&(M.playerStats().matchesPassword((String)parts.elementAt(2)))
					&&(CMSecurity.isASysOp(M)))
					{
						boolean keepDown=parts.size()>3?CMath.s_bool((String)parts.elementAt(3)):true;
						String externalCmd=(parts.size()>4)?CMParms.combine(parts,4):null;
						Vector cmd=new XVector("SHUTDOWN","NOPROMPT");
						if(!keepDown)
						{
							cmd.add("RESTART");
							if((externalCmd!=null)&&(externalCmd.length()>0))
								cmd.add(externalCmd);
						}
						Command C=CMClass.getCommand("Shutdown");
						l="";
						setKillFlag(true);
						rawCharsOut(out,"\n\n\033[1z<Executing Shutdown...\n\n".toCharArray());
						M.setSession(this);
						if(C!=null) C.execute(M,cmd,0);
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
		int c=readByte();
		if(c>255)c=c&0xff;

		switch(c)
		{
		case TELNET_IAC:
			bNextByteIs255=true;
			break;
		case TELNET_SB:
		{
			char[] subOptionData = new char[1024];
			int subOptionCode = readByte();
			int numBytes = 0;
			int last = 0;
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Reading sub-option "+subOptionCode);
			while(((last = readByte()) != -1)
			&&(numBytes<subOptionData.length)
			&&(!killFlag))
			{
				if (last == TELNET_IAC)
				{
					last = readByte();
					if (last == TELNET_IAC)
						subOptionData[numBytes++] = TELNET_IAC;
					else
					if (last == TELNET_SE)
						break;
				}
				else
					subOptionData[numBytes++] = (char)last;
			}
			handleSubOption(subOptionCode, subOptionData, numBytes);
			break;
		}
		case TELNET_DO:
		{
			int last=readByte();
			setClientTelnetMode(last,true);
			if((terminalType.equalsIgnoreCase("zmud")||terminalType.equalsIgnoreCase("cmud"))&&(last==Session.TELNET_ECHO))
				setClientTelnetMode(Session.TELNET_ECHO,false);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Got DO "+Session.TELNET_DESCS[last]);
			if((last==TELNET_COMPRESS2)&&(serverTelnetMode(last)))
			{
				setClientTelnetMode(last,true);
				if(connectionComplete)
				{
					negotiateTelnetMode(rawout,TELNET_COMPRESS2);
					out.flush();
					ZOutputStream zOut=new ZOutputStream(rawout, JZlib.Z_DEFAULT_COMPRESSION);
					zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
					out = new PrintWriter(new OutputStreamWriter(zOut,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
					try{Thread.sleep(250);}catch(Exception e){}
				}
			}
			else
			if(!mightSupportTelnetMode(last))
				changeTelnetMode(last,false);
			else
			if(!serverTelnetMode(last))
				changeTelnetMode(last,true);
			if(serverTelnetCodes[TELNET_LOGOUT])
				setKillFlag(true);
			break;
		}
		case TELNET_DONT:
		{
			int last=readByte();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Got DONT "+Session.TELNET_DESCS[last]);
			setClientTelnetMode(last,false);
			if((last==TELNET_COMPRESS2)&&(serverTelnetMode(last)))
			{
				setClientTelnetMode(last,false);
				out = new PrintWriter(new OutputStreamWriter(rawout,CMProps.getVar(CMProps.Str.CHARSETOUTPUT)));
			}
			if((mightSupportTelnetMode(last)&&(serverTelnetMode(last))))
				changeTelnetMode(last,false);
			break;
		}
		case TELNET_WILL:
		{
			int last=readByte();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Got WILL "+Session.TELNET_DESCS[last]);
			setClientTelnetMode(last,true);
			if((terminalType.equalsIgnoreCase("zmud")||terminalType.equalsIgnoreCase("cmud"))&&(last==Session.TELNET_ECHO))
				setClientTelnetMode(Session.TELNET_ECHO,false);
			if(!mightSupportTelnetMode(last))
				changeTelnetModeBackwards(last,false);
			else
			if(!serverTelnetMode(last))
				changeTelnetModeBackwards(last,true);
			if(serverTelnetCodes[TELNET_LOGOUT])
				setKillFlag(true);
			break;
		}
		case TELNET_WONT:
		{
			int last=readByte();
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET)) Log.debugOut("Got WONT "+Session.TELNET_DESCS[last]);
			setClientTelnetMode(last,false);
			if((mightSupportTelnetMode(last))&&(serverTelnetMode(last)))
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
			int read = rawin.read();
			if(read==-1)
				throw new java.io.InterruptedIOException(".");
			if(debugInput && Log.debugChannelOn())
				debugInputBuf.append(read).append(" ");
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
				char c=fakeInput.charAt(0);
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
		while((in!=null) && !in.ready() && !killFlag && (rawin!=null) &&(rawin.available()>0) && (--maxBytes>=0))
		{
			try{
				return in.read();
			} 
			catch(java.io.InterruptedIOException e)
			{
				b = readByte();
				charWriter.write(b);
			}
		}
		if(in==null)
			throw new java.io.InterruptedIOException();
		return in.read();
	}
	
	public char hotkey(long maxWait)
	{
		if((in==null)||(out==null)) 
			return '\0';
		input=new StringBuffer("");
		long start=System.currentTimeMillis();
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
		catch(java.io.IOException e) { }
		return '\0';
	}

	public int nonBlockingIn(boolean appendInputFlag)
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
						if (clientTelnetMode(TELNET_ECHO))
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
						if (clientTelnetMode(TELNET_ECHO))
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
					if(appendInputFlag) input.append((char)c);
					if (clientTelnetMode(TELNET_ECHO))
						rawCharsOut((char)c);
					if(!appendInputFlag) return c;
				}
				if(rv) return 0;
			}
		}
		catch(InterruptedIOException e)
		{
			return -1;
		}
		return 1;
	}

	public String blockingIn(long maxTime)
		throws IOException
	{
		if((in==null)||(out==null)) return "";
		input=new StringBuffer("");
		final long start=System.currentTimeMillis();
		final long timeoutTime= (maxTime<=0) ? Long.MAX_VALUE : (start + maxTime);
		long nextPingAtTime=start + PINGTIMEOUT;
		try
		{
			suspendCommandLine=true;
			long now;
			long lastC;
			while((!killFlag)
			&&((now=System.currentTimeMillis())<timeoutTime))
			{
				if((lastC=nonBlockingIn(true))==0)
					break;
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
			suspendCommandLine=false;
			if(System.currentTimeMillis()>=timeoutTime)
				throw new java.io.InterruptedIOException(TIMEOUT_MSG);

			StringBuffer inStr=CMLib.coffeeFilter().simpleInFilter(input,CMSecurity.isAllowed(mob,(mob!=null)?mob.location():null,CMSecurity.SecFlag.MXPTAGS));
			input=new StringBuffer("");
			if(inStr==null) 
				return null;
			final String str=inStr.toString();
			snoopSupportPrint(str+"\n\r",true);
			return str;
		}
		finally
		{
			suspendCommandLine=false;
		}
	}

	public String blockingIn()
		throws IOException
	{
		return blockingIn(-1);
	}

	public String readlineContinue()
		throws IOException, SocketException
	{

		if((in==null)||(out==null)) 
			return "";
		int code=-1;
		while(!killFlag)
		{
			synchronized(sock)
			{
				if(sock[0].isClosed() || (!sock[0].isConnected()))
				{
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

		StringBuffer inStr=CMLib.coffeeFilter().simpleInFilter(input,CMSecurity.isAllowed(mob,(mob!=null)?mob.location():null,CMSecurity.SecFlag.MXPTAGS));
		input=new StringBuffer("");
		if(inStr==null) 
			return null;
		final String str=inStr.toString();
		snoopSupportPrint(str+"\n\r",true);
		return str;
	}

	public boolean confirm(final String Message, String Default, long maxTime)
	throws IOException
	{
		if(Default.toUpperCase().startsWith("T")) Default="Y";
		String YN=choose(Message,"YN",Default,maxTime);
		if(YN.equals("Y"))
			return true;
		return false;
	}
	public boolean confirm(final String Message, String Default)
	throws IOException
	{
		if(Default.toUpperCase().startsWith("T")) Default="Y";
		String YN=choose(Message,"YN",Default,-1);
		if(YN.equals("Y"))
			return true;
		return false;
	}

	public String choose(final String Message, final String Choices, String Default)
	throws IOException
	{ 
		return choose(Message,Choices,Default,-1,null);
	}

	public String choose(final String Message, final String Choices, final String Default, long maxTime)
	throws IOException
	{
		return choose(Message,Choices,Default,maxTime,null);
	}
	
	public String choose(final String Message, final String Choices, final String Default, long maxTime, List<String> paramsOut)
	throws IOException
	{
		String YN="";
		String rest=null;
		while((YN.equals(""))||(Choices.indexOf(YN)<0)&&(!killFlag))
		{
			promptPrint(Message);
			YN=blockingIn(maxTime);
			if(YN==null){ return Default.toUpperCase(); }
			YN=YN.trim();
			if(YN.equals("")){ return Default.toUpperCase(); }
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
			paramsOut.addAll(CMParms.paramParse(rest));
		return YN;
	}

	
	public void stopSession(boolean removeMOB, boolean dropSession, boolean killThread)
	{
		setKillFlag(true);
		status=Session.STATUS_LOGOUT5;
		if(removeMOB)
		{
			removeMOBFromGame(false);
		}
		if(dropSession) 
		{
			preLogout(mob);
			logoutFinal();
		}
		Thread killThisThread=null;
		synchronized(this)
		{
			if(killThread)
			{
				if(runThread==Thread.currentThread())
					setKillFlag(true);
				else
				if(runThread!=null)
					killThisThread=runThread;
			}
		}
		if(killThisThread!=null)
			killThisThread.interrupt();
		killThisThread=writeThread;
		if(killThisThread!=null)
			killThisThread.interrupt();
	}

	public void showPrompt()
	{
		promptLastShown=System.currentTimeMillis();
		MOB mob=mob();
		if(mob==null) return;
		if(mob.playerStats()==null) return;
		StringBuffer buf=new StringBuffer("");
		if(clientTelnetMode(Session.TELNET_MXP))
			buf.append("^<!EN Hp '"+mob().curState().getHitPoints()
					  +"'^>^<!EN MaxHp '"+mob().maxState().getHitPoints()
					  +"'^>^<!EN Mana '"+mob().curState().getMana()
					  +"'^>^<!EN MaxMana '"+mob().maxState().getMana()
					  +"'^>^<!EN Move '"+mob().curState().getMovement()
					  +"'^>^<!EN MaxMove '"+mob().maxState().getMovement()
					  +"'^>^<!EN Exp '"+mob().getExperience()
					  +"'^>^<!EN ExpNeed '"+mob().getExpNeededLevel()
					  +"'^>^\n\r\n\r");
		buf.append(CMLib.utensils().builtPrompt(mob));
		promptPrint("^<Prompt^>"+buf.toString()+"^</Prompt^>^.^N");
	}

	protected void closeSocks(String finalMsg)
	{
		if(sock[0]!=null)
		{
			synchronized(sock)
			{
				if(sock[0]!=null)
				{
					try
					{
						Log.sysOut("Disconnect: "+finalMsg+getAddress()+" ("+CMLib.time().date2SmartEllapsedTime(getMillisOnline(),true)+")");
						status=Session.STATUS_LOGOUT7;
						sock[0].shutdownInput();
						status=Session.STATUS_LOGOUT8;
						if(out!=null)
						{
							try{
								if(!out.checkError())
								{
									out.write(PINGCHARS);
									out.checkError();
								}
							} catch(Exception t){}
							out.close();
						}
						status=Session.STATUS_LOGOUT9;
						sock[0].shutdownOutput();
						status=Session.STATUS_LOGOUT10;
						sock[0].close();
						status=Session.STATUS_LOGOUT11;
					}
					catch(IOException e)
					{
					}
					finally
					{
						rawin=null;
						in=null;
						out=null;
						sock[0]=null;
					}
				}
			}
		}
	}

	public String getAddress()
	{
		try
		{
			return sock[0].getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
			return "Unknown (Excpt "+e.getMessage() + ")";
		}
	}

	private void preLogout(MOB M)
	{
		if(M==null) 
			return;
		boolean inTheGame=CMLib.flags().isInTheGame(M,true);
		
		while((getLastPKFight()>0)
		&&((System.currentTimeMillis()-getLastPKFight())<(2*60*1000))
		&&(mob!=null))
		{ try{Thread.sleep(1000);}catch(Exception e){}}
		String name=M.Name();
		if(name.trim().length()==0) name="Unknown";
		if((M.isInCombat())&&(M.location()!=null))
		{
			CMLib.commands().postFlee(mob,"NOWHERE");
			M.makePeace();
		}
		if(!CMLib.flags().isCloaked(M))
		{
			List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOGOFFS);
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),M.clans(),name+" has logged out",true);
		}
		CMLib.login().notifyFriends(M,"^X"+M.Name()+" has logged off.^.^?");
			
		// the player quit message!
		CMLib.threads().executeRunnable(new LoginLogoutThread(M,CMMsg.MSG_QUIT));
		if(M.playerStats()!=null)
			M.playerStats().setLastDateTime(System.currentTimeMillis());
		Log.sysOut("Logout: "+name+" ("+CMLib.time().date2SmartEllapsedTime(System.currentTimeMillis()-userLoginTime,true)+")");
		if(inTheGame)
			CMLib.database().DBUpdateFollowers(M);
	}
	
	private void removeMOBFromGame(boolean killSession)
	{
		MOB M=mob;
		if(M!=null)
		{
			boolean inTheGame=CMLib.flags().isInTheGame(M,true);
			PlayerStats pstats=M.playerStats();
			if(pstats!=null) {
				pstats.setLastDateTime(System.currentTimeMillis());
			}
			if(inTheGame)
				CMLib.database().DBUpdateFollowers(M);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LOGOUTS))
				M.removeFromGame(true,killSession);
		}
	}
	public int getStatus()
	{
		return status;
	}
	public void logout(boolean removeMOB)
	{
		if((mob==null)||(mob.playerStats()==null))
			stopSession(false,false,false);
		else
		{
			preLogout(mob);
			if(removeMOB)
				removeMOBFromGame(false);
			mob.setSession(null);
			mob=null;
		}
	}

	public boolean isRunning() 
	{
		return runThread!=null;
	}
	
	public boolean isPendingLogin(final CharCreationLibrary.LoginSession loginObj)
	{
		switch(status&STATUSMASK_ALL)
		{
			case Session.STATUS_LOGIN:
			case Session.STATUS_ACCOUNTMENU:
			case Session.STATUS_LOGIN1:
			case Session.STATUS_LOGIN2:
				break;
			default:
				return false;
		}
		if(loginObj==null)
			return true;
		if(loginSession==null)
			return false;
		final String otherLogin=loginObj.login;
		final String myLogin=loginSession.login;
		if((otherLogin==null)||(myLogin==null))
			return false;
		return otherLogin.equalsIgnoreCase(myLogin);
	}
	
	
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
		if(clientTelnetMode(TELNET_MSDP)&&(activeMillis>=nextMsdpPing))
		{
			nextMsdpPing=activeMillis+MSDPPINGINTERVAL;
			byte[] msdpPingBuf=CMLib.utensils().pingMsdp(this, msdpReportables);
			if(msdpPingBuf!=null)
			{
				try { rawBytesOut(rawout, msdpPingBuf);}catch(IOException e){}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.TELNET))
					Log.debugOut("MSDP Reported: "+msdpPingBuf.length+" bytes");
			}
		}
		
		try
		{
			if(killFlag) 
				status=Session.STATUS_LOGOUT;
			if((status&STATUSMASK_WAITING_FOR_INPUT)==STATUSMASK_WAITING_FOR_INPUT)
			{
				if(this.inputCallback!=null)
				{
					try
					{
						String input=readlineContinue();
						if(input != null)
						{
							this.inputCallback.setInput(input);
							if(!this.inputCallback.waitForInput())
							{
								final InputCallback callBack=this.inputCallback;
								CMLib.threads().executeRunnable(new Runnable(){
									public void run(){  callBack.callBack(); }
								});
							}
						}
						else
						if(this.inputCallback.isTimedOut())
						{
							this.inputCallback.timedOut();
						}
					}
					catch(Exception e)
					{
						
					}
				}
				if((this.inputCallback==null)||(!this.inputCallback.waitForInput()))
				{
					this.inputCallback=null;
					status=status&Session.STATUSMASK_ALL;
				}
			}
			else
			switch(status&STATUSMASK_ALL)
			{
			case Session.STATUS_IDLE:
				break;
			case Session.STATUS_MAINLOOP:
				mainLoop();
				break;
			case Session.STATUS_LOGIN:
			case Session.STATUS_ACCOUNTMENU:
			case Session.STATUS_LOGIN1:
			case Session.STATUS_LOGIN2:
				loginSystem();
				break;
			case Session.STATUS_LOGOUT:
			case Session.STATUS_LOGOUT1:
			case Session.STATUS_LOGOUT2:
			case Session.STATUS_LOGOUT3:
			case Session.STATUS_LOGOUT4:
			case Session.STATUS_LOGOUT5:
			case Session.STATUS_LOGOUT6:
			case Session.STATUS_LOGOUT7:
			case Session.STATUS_LOGOUT8:
			case Session.STATUS_LOGOUT9:
			case Session.STATUS_LOGOUT10:
			case Session.STATUS_LOGOUT11:
			case Session.STATUS_LOGOUT12:
			case Session.STATUS_LOGOUTFINAL:
			{
				this.inputCallback=null;
				preLogout(mob);
				logoutFinal();
				break;
			}
			}
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
	
	public void loginSystem()
	{
		try
		{
			if(loginSession==null)
				loginSession=new CharCreationLibrary.LoginSession();
			else
			{
				
				loginSession.lastInput=readlineContinue();
				if(loginSession.lastInput==null)
					return;
			}
			if(!killFlag)
			{
				status=Session.STATUS_LOGIN;
				mob=null;
				CharCreationLibrary.LoginResult loginResult=null;
				if(acct==null)
				{
					loginResult=CMLib.login().loginSystem(this,loginSession);
					if(loginResult==CharCreationLibrary.LoginResult.INPUT_REQUIRED)
					{
						status=Session.STATUS_LOGIN;
						return;
					}
				}
				if((acct!=null)||(loginResult==LoginResult.ACCOUNT_LOGIN)||(loginResult==LoginResult.ACCOUNT_CREATED))
				{
					try
					{
						if(acct!=null)
						{
							StringBuilder loginMsg=new StringBuilder("");
							loginMsg.append(getAddress()).append(" "+terminalType)
							.append((clientTelnetMode(Session.TELNET_COMPRESS)||clientTelnetMode(Session.TELNET_COMPRESS2))?" CMP":"")
							.append(clientTelnetMode(Session.TELNET_MSDP)?" MSDP":"")
							.append(", account login: "+acct.accountName());
							Log.sysOut(loginMsg.toString());
						}
						status=Session.STATUS_ACCOUNTMENU;
						loginResult=CMLib.login().doAccountMenu(acct,this,(loginResult==LoginResult.ACCOUNT_CREATED));
					}
					finally
					{
						status=Session.STATUS_LOGIN;
					}
				}
				if(loginResult != LoginResult.NO_LOGIN)
				{
					status=Session.STATUS_LOGIN2;
					loginSession=null;
					if((mob!=null)&&(mob.playerStats()!=null))
						acct=mob.playerStats().getAccount();
					if((!killFlag)&&((mob!=null)))
					{
						userLoginTime=System.currentTimeMillis();
						StringBuilder loginMsg=new StringBuilder("");
						loginMsg.append(getAddress()).append(" "+terminalType)
						.append(((CMath.bset(mob.getBitmap(),MOB.ATT_MXP)&&clientTelnetMode(Session.TELNET_MXP)))?" MXP":"")
						.append(clientTelnetMode(Session.TELNET_MSDP)?" MSDP":"")
						.append((clientTelnetMode(Session.TELNET_COMPRESS)||clientTelnetMode(Session.TELNET_COMPRESS2))?" CMP":"")
						.append(((CMath.bset(mob.getBitmap(),MOB.ATT_ANSI)&&clientTelnetMode(Session.TELNET_ANSI)))?" ANSI":"")
						.append(", character login: "+mob.Name());
						Log.sysOut(loginMsg.toString());
						if(loginResult != CharCreationLibrary.LoginResult.NO_LOGIN)
						{
							CMMsg msg = CMClass.getMsg(mob,null,CMMsg.MSG_LOGIN,null);
							if(!CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_LOGIN,msg))
								setKillFlag(true);
							else
								CMLib.commands().monitorGlobalMessage(mob.location(), msg);
						}
					}
					needPrompt=true;
					if((!killFlag)&&(mob!=null))
					{
						status=Session.STATUS_MAINLOOP;
						return;
					}
				}
				else
				{
					mob=null;
				}
				status=Session.STATUS_LOGIN;
				return;
			}
			else
			{
				loginSession=null;
			}
			status=Session.STATUS_LOGOUT;
		}
		catch(SocketException e)
		{
			synchronized(sock) {
				if(!Log.isMaskedErrMsg(e.getMessage())&&((!killFlag)||((sock[0]!=null)&&sock[0].isConnected())))
					errorOut(e);
			}
			status=Session.STATUS_LOGOUT;
			preLogout(mob);
			status=Session.STATUS_LOGOUT1;
		}
		catch(Exception t)
		{
			synchronized(sock) {
				if(!Log.isMaskedErrMsg(t.getMessage())
				&&((!killFlag)
					||(sock[0]!=null&&sock[0].isConnected())))
					errorOut(t);
			}
			status=Session.STATUS_LOGOUT;
			preLogout(mob);
			status=Session.STATUS_LOGOUT1;
		}
	}

	public void logoutFinal()
	{
		try
		{
			final MOB M=mob();
			final String finalMsg;
			if(M!=null)
				finalMsg=M.Name()+": ";
			else
			if(acct!=null)
				finalMsg=acct.accountName()+": ";
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
				catch(Exception e)
				{
					Log.errOut(e);
				}
				finally
				{
				}
			}
			
			status=Session.STATUS_LOGOUT4;
			setKillFlag(true);
			waiting=false;
			needPrompt=false;
			acct=null;
			snoops.clear();
			
			closeSocks(finalMsg);
			status=Session.STATUS_LOGOUT5;
		}
		finally
		{
			CMLib.sessions().remove(this);
			status=Session.STATUS_LOGOUTFINAL;
		}
	}
	
	public void mainLoop()
	{
		try
		{
			lastLoopTop=System.currentTimeMillis();
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
				MOB mob=mob();
				if((CMDS.size()>0)&&(mob!=null))
				{
					waiting=false;
					String firstWord=CMDS.get(0);
					PlayerStats pstats=mob.playerStats();
					String alias=(pstats!=null)?pstats.getAlias(firstWord):"";
					Vector ALL_CMDS=new Vector();
					boolean echoOn=false;
					if(alias.length()>0)
					{
						CMDS.remove(0);
						Vector<String> all_stuff=CMParms.parseSquiggleDelimited(alias,true);
						for(String stuff : all_stuff)
						{
							List THIS_CMDS=new XVector(CMDS);
							ALL_CMDS.addElement(THIS_CMDS);
							Vector preCommands=CMParms.parse(stuff);
							for(int v=preCommands.size()-1;v>=0;v--)
								THIS_CMDS.add(0,preCommands.elementAt(v));
						}
						echoOn=true;
					}
					else
						ALL_CMDS.addElement(CMDS);
					for(int v=0;v<ALL_CMDS.size();v++)
					{
						CMDS=(List)ALL_CMDS.elementAt(v);
						setPreviousCmd(CMDS);
						milliTotal+=(lastStop-lastStart);
	
						lastStart=System.currentTimeMillis();
						if(echoOn) rawPrintln(CMParms.combineWithQuotes(CMDS,0));
						List<List<String>> MORE_CMDS=CMLib.lang().preCommandParser(CMDS);
						for(int m=0;m<MORE_CMDS.size();m++)
							mob.enqueCommand(MORE_CMDS.get(m),metaFlags(),0);
						lastStop=System.currentTimeMillis();
					}
				}
				needPrompt=true;
			}
			if(mob==null)
			{
				status=Session.STATUS_LOGOUT;
				preLogout(mob);
				status=Session.STATUS_ACCOUNTMENU;
				return;
			}
			while((!killFlag)&&(mob!=null)&&(mob.dequeCommand()))
				{}
	
			if(((System.currentTimeMillis()-lastBlahCheck)>=60000)
			&&(mob()!=null))
			{
				lastBlahCheck=System.currentTimeMillis();
				Vector<String> V=CMParms.parse(CMProps.getVar(CMProps.Str.IDLETIMERS));
				if((V.size()>0)
				&&(!CMSecurity.isAllowed(mob(),mob().location(),CMSecurity.SecFlag.IDLEOK))
				&&(CMath.s_int(V.firstElement())>0))
				{
					int minsIdle=(int)(getIdleMillis()/60000);
					if(minsIdle>=CMath.s_int(V.firstElement()))
					{
						println("\n\r^ZYou are being logged out!^?");
						setKillFlag(true);
					}
					else
					if(minsIdle>=CMath.s_int(V.lastElement()))
					{
						int remain=CMath.s_int(V.firstElement())-minsIdle;
						println(mob(),null,null,"\n\r^ZIf you don't do something, you will be logged out in "+remain+" minute(s)!^?");
					}
				}
	
				if(!afkFlag())
				{
					if(getIdleMillis()>=600000)
						setAfkFlag(true);
				}
				else
				if((getIdleMillis()>=10800000)&&(!isStopped()))
				{
					if((!CMLib.flags().isSleeping(mob))
					&&(mob().fetchEffect("Disease_Blahs")==null)
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						Ability A=CMClass.getAbility("Disease_Blahs");
						if(A!=null) A.invoke(mob,mob,true,0);
					}
					else
					if((CMLib.flags().isSleeping(mob))
					&&(mob().fetchEffect("Disease_Narcolepsy")==null)
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						Ability A=CMClass.getAbility("Disease_Narcolepsy");
						if(A!=null) A.invoke(mob,mob,true,0);
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
		catch(SocketException e)
		{
			synchronized(sock) {
				if(!Log.isMaskedErrMsg(e.getMessage())&&((!killFlag)||((sock[0]!=null)&&sock[0].isConnected())))
					errorOut(e);
			}
			status=Session.STATUS_LOGOUT;
			preLogout(mob);
			status=Session.STATUS_LOGOUT1;
		}
		catch(Exception t)
		{
			synchronized(sock) {
				if((!Log.isMaskedErrMsg(t.getMessage()))
				&&((!killFlag)
					||(sock[0]!=null&&sock[0].isConnected())))
					errorOut(t);
			}
			status=Session.STATUS_LOGOUT;
			preLogout(mob);
			status=Session.STATUS_LOGOUT1;
		}
	}
	
	public long activeTimeMillis() 
	{
		if(activeMillis==0) return 0;
		return System.currentTimeMillis()-activeMillis;
	}
	
	public static class LoginLogoutThread implements CMRunnable, Tickable
	{
		public String name(){return (theMOB==null)?"Dead LLThread":"LLThread for "+theMOB.Name();}
		public boolean tick(Tickable ticking, int tickID){return false;}
		public String ID(){return name();}
		public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new LoginLogoutThread();}}
		public void initializeClass(){}
		public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
		public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
		public long getTickStatus(){return 0;}
		private MOB theMOB=null;
		private int msgCode=-1;
		private HashSet skipRooms=new HashSet();
		private long activeMillis=0;
		private LoginLogoutThread(){}
		public LoginLogoutThread(MOB mob, int msgC)
		{
			theMOB=mob;
			msgCode=msgC;
		}

		public void initialize()
		{
			Set<MOB> group=theMOB.getGroupMembers(new HashSet<MOB>());
			skipRooms.clear();
			for(Iterator i=group.iterator();i.hasNext();)
			{
				MOB M=(MOB)i.next();
				if((M.location()!=null)&&(!skipRooms.contains(M.location())))
					skipRooms.add(M.location());
			}
			if((!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
			&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			{
				CMMsg msg=CMClass.getMsg(theMOB,null,msgCode,null);
				Room R=theMOB.location();
				if(R!=null) skipRooms.remove(R);
				try{
					if((R!=null)&&(theMOB.location()!=null))
						R.send(theMOB,msg);
					for(Iterator i=skipRooms.iterator();i.hasNext();)
					{
						R=(Room)i.next();
						if(theMOB.location()!=null)
							R.sendOthers(theMOB,msg);
					}
					if(R!=null) skipRooms.add(R);
				}catch(Exception e){}
			}
		}

		public void run()
		{
			activeMillis=System.currentTimeMillis();
			if((!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
			&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			{
				CMMsg msg=CMClass.getMsg(theMOB,null,msgCode,null);
				Room R=null;
				try{
					for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						R=(Room)e.nextElement();
						if((!skipRooms.contains(R))&&(theMOB.location()!=null))
							R.sendOthers(theMOB,msg);
					}
				}catch(Exception e){}
				theMOB=null;
			}
		}
		public long activeTimeMillis() {
			return (activeMillis>0)?System.currentTimeMillis()-activeMillis:0;
		}
	}

	private static enum SESS_STAT_CODES {PREVCMD,ISAFK,AFKMESSAGE,ADDRESS,IDLETIME,
										 LASTMSG,LASTNPCFIGHT,LASTPKFIGHT,TERMTYPE,
										 TOTALMILLIS,TOTALTICKS,WRAP,LASTLOOPTIME}
	public int getSaveStatIndex() { return SESS_STAT_CODES.values().length;}
	public String[] getStatCodes() { return CMParms.toStringArray(SESS_STAT_CODES.values());}
	public boolean isStat(String code) { return getStatIndex(code)!=null;}
	private SESS_STAT_CODES getStatIndex(String code) { return (SESS_STAT_CODES)CMath.s_valueOf(SESS_STAT_CODES.values(),code); }
	public String getStat(String code) 
	{
		final SESS_STAT_CODES stat = getStatIndex(code);
		if(stat==null){ return "";}
		switch(stat)
		{
		case PREVCMD: return CMParms.combineWithQuotes(previousCMD(),0);
		case ISAFK: return ""+afkFlag();
		case AFKMESSAGE: return afkMessage();
		case ADDRESS: return getAddress();
		case IDLETIME: return CMLib.time().date2String(System.currentTimeMillis()-getIdleMillis());
		case LASTMSG: return CMParms.combineWithQuotes(getLastMsgs(),0);
		case LASTNPCFIGHT: return CMLib.time().date2String(getLastNPCFight());
		case LASTPKFIGHT: return CMLib.time().date2String(getLastPKFight());
		case TERMTYPE: return getTerminalType();
		case TOTALMILLIS: return CMLib.time().date2String(System.currentTimeMillis()-getTotalMillis());
		case TOTALTICKS: return ""+getTotalTicks();
		case WRAP: return ""+getWrap();
		case LASTLOOPTIME: return CMLib.time().date2String(lastLoopTime());
		default: Log.errOut("Session","getStat:Unhandled:"+stat.toString()); break;
		}
		return null;
	}
	public void setStat(String code, String val) 
	{
		final SESS_STAT_CODES stat = getStatIndex(code);
		if(stat==null){ return;}
		switch(stat)
		{
		case PREVCMD: previousCmd=CMParms.parse(val); break;
		case ISAFK: afkFlag=CMath.s_bool(val); break;
		case AFKMESSAGE: afkMessage=val; break;
		case ADDRESS: return;
		case IDLETIME: lastKeystroke=CMLib.time().string2Millis(val); break;
		case LASTMSG: prevMsgs=CMParms.parse(val); break;
		case LASTNPCFIGHT: lastNPCFight=CMLib.time().string2Millis(val); break;
		case LASTPKFIGHT: lastPKFight=CMLib.time().string2Millis(val); break;
		case TERMTYPE: terminalType=val; break;
		case TOTALMILLIS: milliTotal = System.currentTimeMillis() - CMLib.time().string2Millis(val); break;
		case TOTALTICKS: tickTotal= CMath.s_int(val); break;
		case WRAP: if((mob!=null)&&(mob.playerStats()!=null)) mob.playerStats().setWrap(CMath.s_int(val)); break;
		case LASTLOOPTIME: lastLoopTop=CMLib.time().string2Millis(val); break;
		default: Log.errOut("Session","setStat:Unhandled:"+stat.toString()); break;
		}
	}
	
	private static class SesInputStream extends InputStream
	{
		private int[] bytes;
		private int start=0;
		private int end=0;
		protected SesInputStream(int maxBytesPerChar)
		{
			bytes=new int[maxBytesPerChar+1];
		}
		public int read() throws IOException
		{
			if(start==end)
				throw new java.io.InterruptedIOException();
			int b=bytes[start];
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
