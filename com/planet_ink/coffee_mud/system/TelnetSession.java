package com.planet_ink.coffee_mud.system;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class TelnetSession extends Thread implements Session
{
	private int status=0;
	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	private MOB mob;
	private boolean killFlag=false;
	private boolean needPrompt=false;
	private boolean afkFlag=false;
	private StringBuffer input=new StringBuffer("");
	private boolean waiting=false;
	private static final int SOTIMEOUT=300;
	private Vector previousCmd=new Vector();
	private String[] clookup=null;
	private String lastColorStr="";
	private String lastStr=null;
	private int spamStack=0;
	private long lastOutput=0;
	private Vector snoops=new Vector();
	private final static String[] maskErrMsgs={
			"reset by peer",
			"socket closed",
			"connection abort",
			"connection reset",
			"jvm_recv",
			"timed out",
			"stream closed"
	};

	private boolean lastWasCR=false;
	private boolean lastWasLF=false;
	private boolean suspendCommandLine=false;

	public long lastStart=System.currentTimeMillis();
	public long lastStop=System.currentTimeMillis();
	public long lastLoopTop=System.currentTimeMillis();
	public long lastBlahCheck=0;
	public long milliTotal=0;
	public long tickTotal=0;
	public long lastKeystroke=0;

	private int termID = 0;	//1 = ANSI, 2 = SOUND/MUSIC, 4=MSX, 8=XML, 16=MCP 
	public int currentColor='N';
	public int lastColor=-1;
	private static int sessionCounter=0;
	public TelnetSession(Socket s, String introTextStr)
	{
		super("TelnetSession."+sessionCounter);
		++sessionCounter;
		sock=s;
		try
		{
			out = new PrintWriter(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			if(introTextStr!=null)
			{
				out.println(introTextStr);
				out.flush();
			}

			if(sock!=null)
				sock.setSoTimeout(SOTIMEOUT);
		}
		catch(SocketException e)
		{
		}
		catch(IOException e)
		{
		}
	}

	public int currentColor(){return currentColor;}
	public long getTotalMillis(){ return milliTotal;}
	public long getIdleMillis(){ return System.currentTimeMillis()-lastKeystroke;}
	public long getTotalTicks(){ return tickTotal;}

	public long lastLoopTime(){ return lastLoopTop;}

	public MOB mob(){return mob;}
	public void setMob(MOB newmob)
	{ mob=newmob;}
	public boolean killFlag(){return killFlag;}
	public void setKillFlag(boolean truefalse){killFlag=truefalse;}
	public Vector previousCMD(){return previousCmd;}
	public void startBeingSnoopedBy(Session S)
	{
		if(!snoops.contains(S))
			snoops.addElement(S);
	}
	public void stopBeingSnoopedBy(Session S)
	{
		while(snoops.contains(S))
			snoops.removeElement(S);
	}
	public boolean amBeingSnoopedBy(Session S)
	{
		if(S==null) return snoops.size()==0;
		return(snoops.contains(S));
	}

	public void setPreviousCmd(Vector cmds)
	{
		if(cmds==null) return;
		if(cmds.size()==0) return;
		if((cmds.size()>0)&&(((String)cmds.elementAt(0)).trim().startsWith("!")))
			return;

		previousCmd.removeAllElements();
		for(int i=0;i<cmds.size();i++)
			previousCmd.addElement(((String)cmds.elementAt(i)).toString());
	}

	public boolean afkFlag(){return afkFlag;}
	public void setAfkFlag(boolean truefalse)
	{
		if(afkFlag==truefalse) return;
		afkFlag=truefalse;
		if(afkFlag)
			println("\n\rYou are now listed as AFK.");
		else
			println("\n\rYou are no longer AFK.");
	}

	private void errorOut(Exception t)
	{
		Log.errOut("Session",t);
		Sessions.removeElement(this);
		killFlag=true;
	}

	public void onlyPrint(String msg){onlyPrint(msg,-1);}
	public synchronized void onlyPrint(String msg, int pageBreak)
	{
		if((out==null)||(msg==null)) return;
		try
		{
			if(snoops.size()>0)
				for(int s=0;s<snoops.size();s++)
					((Session)snoops.elementAt(s)).onlyPrint(msg,0);

			lastOutput=System.currentTimeMillis();

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
				out.print(lastStr);
				out.flush();
			}

			spamStack=0;
			if(msg.startsWith("\n\r")&&(msg.length()>2))
				lastStr=msg.substring(2);
			else
				lastStr=msg;
			
			if(pageBreak<0)	pageBreak=CommonStrings.getIntVar(CommonStrings.SYSTEMI_PAGEBREAK);
			if(pageBreak>0)
			{
				int lines=0;
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
							out.print(msg.substring(0,i));
							msg=msg.substring(i+1);
							out.flush();
							out.print("<pause - enter>");
							out.flush();
							try{
								blockingIn();
							}catch(Exception e){return;}
						}
					}
				}
			}
			out.print(msg);
			out.flush();
		}
		catch(java.lang.NullPointerException e){}
	}

	public void rawPrint(String msg){rawPrint(msg,-1);}
	public void rawPrint(String msg, int pageBreak)
	{ if(msg==null)return;
	  onlyPrint((needPrompt?"":"\n\r")+msg,pageBreak);
	  needPrompt=true;
	}

	public void print(String msg)
	{ onlyPrint(CoffeeFilter.fullOutFilter(this,mob,mob,mob,null,msg,false),-1); }

	public void rawPrintln(String msg){rawPrintln(msg,-1);}
	public void rawPrintln(String msg, int pageBreak)
	{ if(msg==null)return; rawPrint(msg+"\n\r",pageBreak);}

	public void stdPrint(String msg)
	{ rawPrint(CoffeeFilter.fullOutFilter(this,mob,mob,mob,null,msg,false)); }

	public void print(Environmental src, Environmental trg, Environmental tol, String msg)
	{ onlyPrint((CoffeeFilter.fullOutFilter(this,mob,src,trg,tol,msg,false)),-1);}

	public void stdPrint(Environmental src, Environmental trg, Environmental tol, String msg)
	{ rawPrint(CoffeeFilter.fullOutFilter(this,mob,src,trg,trg,msg,false)); }

	public void println(String msg)
	{ if(msg==null)return; print(msg+"\n\r");}

	public void unfilteredPrintln(String msg)
	{ if(msg==null)return;
	  onlyPrint(CoffeeFilter.fullOutFilter(this,mob,mob,mob,null,msg,true)+"\n\r",-1);
	  needPrompt=true;
	}

	public void unfilteredPrint(String msg)
	{ onlyPrint(CoffeeFilter.fullOutFilter(this,mob,mob,mob,null,msg,true),-1);
	  needPrompt=true;
	}

	public void colorOnlyPrintln(String msg)
	{ colorOnlyPrint(msg,-1);}
	public void colorOnlyPrintln(String msg, int pageBreak)
	{ if(msg==null)return;
	  onlyPrint(colorOnlyFilter(msg)+"\n\r",pageBreak);
	  needPrompt=true;
	}

	public void colorOnlyPrint(String msg)
	{ colorOnlyPrint(msg,-1);}
	public void colorOnlyPrint(String msg, int pageBreak)
	{ onlyPrint(colorOnlyFilter(msg),pageBreak);
	  needPrompt=true;
	}

	public void stdPrintln(String msg)
	{ if(msg==null)return;
	  rawPrint(CoffeeFilter.fullOutFilter(this,mob,mob,mob,null,msg,false)+"\n\r");
	}

	public void println(Environmental src, Environmental trg, Environmental tol, String msg)
	{ if(msg==null)return;
	  onlyPrint(CoffeeFilter.fullOutFilter(this,mob,src,trg,tol,msg,false)+"\n\r",-1);
	}

	public void stdPrintln(Environmental src,Environmental trg, Environmental tol, String msg)
	{ if(msg==null)return;
	  rawPrint(CoffeeFilter.fullOutFilter(this,mob,src,trg,tol,msg,false)+"\n\r");
	}

	public void setPromptFlag(boolean truefalse)
	{
		needPrompt=truefalse;
	}

	public String prompt(String Message, String Default, long maxTime)
		throws IOException
	{
		String Msg=prompt(Message,maxTime).trim();
		if(Msg.equals("")) return Default;
		else return Msg;
	}

	public String prompt(String Message, String Default)
		throws IOException
	{
		String Msg=prompt(Message,-1).trim();
		if(Msg.equals("")) return Default;
		else return Msg;
	}

	public String prompt(String Message, long maxTime)
		throws IOException
	{
		print(Message);
		String input=blockingIn(maxTime);
		if(input==null) return "";
		if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
			return input.substring(0,input.length()-1);
		else
			return input;
	}

	public String prompt(String Message)
		throws IOException
	{
		print(Message);
		String input=blockingIn(-1);
		if(input==null) return "";
		if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
			return input.substring(0,input.length()-1);
		else
			return input;
	}

	public void cmdExit(MOB mob, Vector commands)
		throws Exception
	{
		if (confirm("\n\rQuit -- are you sure (y/N)?","N"))
		{
			killFlag=true;
		}
	}

	public int getColor(char c)
	{
		// warning do not nest!
		if (c == '?') return lastColor;
		if (c>255) return -1;
		return c;
	}

	public String[] clookup(){
		if(clookup==null)
			clookup=CommonStrings.standardColorLookups();

		if(mob()==null) return clookup;
		PlayerStats pstats=mob().playerStats();
		if(pstats==null) return clookup;

		if(!pstats.getColorStr().equals(lastColorStr))
		{
			if(pstats.getColorStr().length()==0)
				clookup=CommonStrings.standardColorLookups();
			else
			{
				String changes=pstats.getColorStr();
				lastColorStr=changes;
				clookup=(String[])CommonStrings.standardColorLookups().clone();
				int x=changes.indexOf("#");
				while(x>0)
				{
					String sub=changes.substring(0,x);
					changes=changes.substring(x+1);
					clookup[sub.charAt(0)]=sub.substring(1);
					x=changes.indexOf("#");
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

	public String makeEscape(int c)
	{
		if((c<='9')&&(c>='0'))
		{
			if((termID&2)==2)
				return CommonStrings.getVar(CommonStrings.SYSTEM_ESC0+(c-('0')));
			else
				return "";
		}
		else
		if (((termID&1)==1) && (c != -1))
		{
			if ((c != currentColor)||(c=='^'))
			{
				if(c !='.')
				{
					lastColor = currentColor;
					currentColor = c;
				}
				return clookup()[c];
			}
		}
		else
		{
			lastColor = currentColor;
			currentColor = 0;
		}
		return null;
	}

	// no word-wrapping, text filtering or ('\','n') -> '\n' translations
	// (it's not a member of the interface either so probably shouldn't be public)
	public String colorOnlyFilter(String msg)
	{
		if(mob==null) return msg;
		if(msg==null) return null;

		if(msg.length()==0) return msg;

		StringBuffer buf=new StringBuffer(msg);

		// all we're looking for is carat character
		while (buf.toString().indexOf('^') != -1)
		{
			int loop = buf.toString().indexOf('^');
			if (loop<buf.length()-1)
			{
				int colorID = getColor( buf.charAt(loop+1) );
				if (colorID != -1)
				{
					String colorEscStr = makeEscape(colorID);
					int csl = 0;
					if (colorEscStr != null)
					{
						csl = colorEscStr.length();
						// it would be NICE if java let us replace with an
						//  empty string (not null), but it don't seem to likes it
						if (csl > 0)
							buf.replace(loop,loop+2 ,colorEscStr);
					}

					if (csl == 0)
					{
						// remove the color code
						buf.deleteCharAt(loop);
						buf.deleteCharAt(loop);
					}
				}
			}
		}

		if ((currentColor != ('N'))&&(((termID&1)==1)))
			buf.append(makeEscape('N'));

		return buf.toString();

	}

	private boolean appendInput(int c)
	{
		boolean rv = false;
		switch (c)
		{
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
				break;
			}
			case 26:
			{
				lastWasCR = false;
				lastWasLF = false;
				// don't let them enter ANSI escape sequences...
				c = -1;
				break;
			}
			case 241:
			case 242:
			case 243:
			case 244:
			case 245:
			case 246:
			case 247:
			case 248:
			case 249:
			case 250:
			case 251:
			case 252:
			case 253:
			case 254:
			case 255:
			{
				lastWasCR = false;
				lastWasLF = false;
				// don't let them enter telnet codes, except IAC, which is handled...
				c = -1;
				break;
			}
			default:
			{
				lastWasCR = false;
				lastWasLF = false;
				break;
			}
		}

		if(c>0)
		{
			lastKeystroke=System.currentTimeMillis();
			input.append((char)c);
		}
		return rv;
	}

	public void handleIAC()
		throws IOException, InterruptedIOException
	{
		if((in==null)||(out==null))
			return;
		int c=in.read();
		int opt=0;
		switch(c)
		{
		case 251:
		case 252:
		case 253:
		case 254:
			opt=in.read();
			break;
		default:
			return;
		}
		char[] wont={255,252,(char)opt};
		char[] dont={255,254,(char)opt};
		switch(c)
		{
		case 251:
		case 252:
			out.write(dont);
			break;
		case 253:
		case 254:
			out.write(wont);
			break;
		default:
			return;
		}
	}

	public String blockingIn(long maxTime)
		throws IOException
	{
		if((in==null)||(out==null)) return "";
		input=new StringBuffer("");
		long start=System.currentTimeMillis();
		try
		{
			suspendCommandLine=true;
			while((!killFlag)
			&&((maxTime<=0)||((System.currentTimeMillis()-start)<maxTime)))
			{
				try
				{
					int c=in.read();
					if(c<0)
						throw new IOException("reset by peer");
					else
					if(c==255) handleIAC();
					else
					if (appendInput(c))
						break;
				}
				catch(InterruptedIOException e)
				{
				}
			}
			suspendCommandLine=false;
			if((maxTime>0)&&((System.currentTimeMillis()-start)>=maxTime))
				throw new java.io.InterruptedIOException("Timed Out.");
		
			StringBuffer inStr=CoffeeFilter.simpleInFilter(input);
			input=new StringBuffer("");
			if(inStr==null) return null;
			return inStr.toString();
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

		if((in==null)||(out==null)) return "";
		while(!killFlag)
		{
			try
			{
				int c=in.read();
				if(c<0)
					throw new IOException("Connection reset by peer.");
				else
				if(c==255) handleIAC();
				else
				if (appendInput(c))
					break;
			}
			catch(InterruptedIOException e)
			{
				return null;
			}
		}

		StringBuffer inStr=CoffeeFilter.simpleInFilter(input);
		input=new StringBuffer("");
		if(inStr==null) return null;
		return inStr.toString();
	}

	public boolean confirm(String Message, String Default, long maxTime)
	throws IOException
	{
		String YN=choose(Message,"YN",Default,maxTime);
		if(YN.equals("Y"))
			return true;
		return false;
	}
	public boolean confirm(String Message, String Default)
	throws IOException
	{
		String YN=choose(Message,"YN",Default,-1);
		if(YN.equals("Y"))
			return true;
		return false;
	}

	public String choose(String Message, String Choices, String Default)
	throws IOException
	{ return choose(Message,Choices,Default,-1);}
	
	public String choose(String Message, String Choices, String Default, long maxTime)
	throws IOException
	{
		String YN="";
		while((YN.equals(""))||(Choices.indexOf(YN)<0)&&(!killFlag))
		{
			print(Message);
			YN=blockingIn(maxTime);
			if(YN==null){ return Default.toUpperCase(); }
			YN=YN.trim().toUpperCase();
			if(YN.equals("")){ return Default.toUpperCase(); }
			if(YN.length()>1) YN=YN.substring(0,1);
		}
		return YN;
	}

	public void logoff()
	{
		killFlag=true;
		this.interrupt();
		try{Thread.sleep(1000);}catch(Exception i){}
	}

	private static final int WIZINV=  EnvStats.IS_INVISIBLE
									 |EnvStats.IS_NOT_SEEN
									 |EnvStats.IS_HIDDEN
									 |EnvStats.IS_SNEAKING
									 |EnvStats.IS_FLYING
									 |EnvStats.IS_CLIMBING
									 |EnvStats.IS_SWIMMING;
	public void showPrompt()
	{
		if(mob()==null) return;
		if(mob().playerStats()==null) return;
		StringBuffer buf=new StringBuffer("\n\r");
		String prompt=mob().playerStats().getPrompt();
		int c=0;
		while(c<prompt.length())
			if((prompt.charAt(c)=='%')&&(c<(prompt.length()-1)))
			{
				switch(prompt.charAt(++c))
				{
				case 'h': { buf.append(mob().curState().getHitPoints()); c++; break;}
				case 'H': { buf.append(mob().maxState().getHitPoints()); c++; break;}
				case 'm': { buf.append(mob().curState().getMana()); c++; break;}
				case 'M': { buf.append(mob().maxState().getMana()); c++; break;}
				case 'v': { buf.append(mob().curState().getMovement()); c++; break;}
				case 'V': { buf.append(mob().maxState().getMovement()); c++; break;}
				case 'x': { buf.append(mob().getExperience()); c++; break;}
				case 'X': {
							  if(mob().getExpNeededLevel()==Integer.MAX_VALUE)
								buf.append("N/A");
							  else
								buf.append(mob().getExpNeededLevel());
							  c++; break;
						  }
				case 'g': { buf.append(mob().getMoney()); c++; break;}
				case 'a': { buf.append(mob().getAlignment()); c++; break;}
				case 'A': { buf.append(CommonStrings.alignmentStr(mob().getAlignment())); c++; break;}
				case 'w': { buf.append(mob().envStats().weight()); c++; break;}
				case 'W': { buf.append(mob().maxCarry()); c++; break;}
				case 'r': {   if(mob().location()!=null)
								  buf.append(mob().location().displayText());
							  c++; break; }
				case 'z': {      if((mob().location()!=null)&&(CMSecurity.isAllowed(mob(),mob().location(),"SYSMSGS")))
								  buf.append(mob().location().getArea().name());
							  c++; break; }
				case 'R': {   if((mob().location()!=null)&&CMSecurity.isAllowed(mob(),mob().location(),"SYSMSGS"))
								  buf.append(mob().location().roomID());
							  c++; break; }
				case 'e': {	  MOB victim=mob().getVictim();
							  if((mob().isInCombat())&&(victim!=null)&&(Sense.canBeSeenBy(victim,mob)))
								  buf.append(victim.name());
							  c++; break; }
				case 'E': {	  MOB victim=mob().getVictim();
							  if((mob().isInCombat())&&(victim!=null)&&(Sense.canBeSeenBy(victim,mob)))
								  buf.append(victim.charStats().getMyRace().healthText(victim)+"\n\r");
							  c++; break; }
				case 'I': {   if(Util.bset(mob().envStats().disposition(),WIZINV))
								  buf.append("WizInv");
							  else
							  if(!Sense.isSeen(mob()))
								  buf.append("Undetectable");
							  else
							  if(Sense.isInvisible(mob())&&Sense.isHidden(mob()))
								  buf.append("Hidden/Invisible");
							  else
							  if(Sense.isInvisible(mob()))
								  buf.append("Invisible");
							  else
							  if(Sense.isHidden(mob()))
								  buf.append("Hidden");
							  c++; break;}
				case 'B': { buf.append("\n\r"); c++; break;}
				case 'd': {	  MOB victim=mob().getVictim();
							  if((mob().isInCombat())&&(victim!=null))
								  buf.append(""+mob().rangeToTarget());
							  c++; break; }
				case 't': {	  if(mob().location()!=null)
								  buf.append(Util.capitalize(TimeClock.TOD_DESC[mob().location().getArea().getTimeObj().getTODCode()].toLowerCase()));
							  c++; break;
						  }
				case 'T': {	  if(mob().location()!=null)
								  buf.append(mob().location().getArea().getTimeObj().getTimeOfDay());
							  c++; break;
						  }
				case '@': {	  if(mob().location()!=null)
								  buf.append(mob().location().getArea().getClimateObj().weatherDescription(mob().location()));
							  c++; break;
						  }
				case 'K':
				case 'k': { MOB tank=mob();
							if((tank.getVictim()!=null)
							&&(tank.getVictim().getVictim()!=null)
							&&(tank.getVictim().getVictim()!=mob()))
								tank=tank.getVictim().getVictim();
							if(((c+1)<prompt.length())&&(tank!=null))
								switch(prompt.charAt(c+1))
								{
									case 'h': { buf.append(tank.curState().getHitPoints()); c++; break;}
									case 'H': { buf.append(tank.maxState().getHitPoints()); c++; break;}
									case 'm': { buf.append(tank.curState().getMana()); c++; break;}
									case 'M': { buf.append(tank.maxState().getMana()); c++; break;}
									case 'v': { buf.append(tank.curState().getMovement()); c++; break;}
									case 'V': { buf.append(tank.maxState().getMovement()); c++; break;}
								}
							c++;
							break;
						  }
				default:{ buf.append("%"+prompt.charAt(c)); c++; break;}
				}
			}
			else
				buf.append(prompt.charAt(c++));
		print(buf.toString()+"^N");
	}

	public void setTermID(int tid)
	{
		termID = tid;
	}

	private void closeSocks()
	{
		try
		{
			if(sock!=null)
			{
				status=Session.STATUS_LOGOUT6;
				if(out!=null)
					out.flush();
				status=Session.STATUS_LOGOUT7;
				sock.shutdownInput();
				status=Session.STATUS_LOGOUT8;
				sock.shutdownOutput();
				status=Session.STATUS_LOGOUT9;
				if(out!=null)
					out.close();
				status=Session.STATUS_LOGOUT10;
				sock.close();
				status=Session.STATUS_LOGOUT11;
			}
			in=null;
			out=null;
			sock=null;

		}
		catch(IOException e)
		{
		}
	}

	public int getTermID()
	{
		return termID;
	}

	private boolean isMaskedErrMsg(String str)
	{
		if(str==null) return false;
		str=str.toLowerCase();
		for(int i=0;i<maskErrMsgs.length;i++)
			if(str.indexOf(maskErrMsgs[i])>=0)
				return true;
		return false;
	}

	public String getAddress()
	{
		try
		{
			return sock.getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
			return "Unknown (Excpt "+e.getMessage() + ")";
		}
	}

	public int getStatus(){return status;}

	public void run()
	{
		status=Session.STATUS_LOGIN;
		try
		{
			long tries=5;
			while((!killFlag)&&((--tries)>0))
			{
				MOB newMob=CMClass.getMOB("StdMOB");
				newMob.setSession(this);
				mob=newMob;
				status=Session.STATUS_LOGIN;
				Command C=CMClass.getCommand("FrontLogin");
				String input=null;
				if((C!=null)&&(C.execute(mob,null)))
				{
					status=Session.STATUS_LOGIN2;
					if((!killFlag)&&(mob!=null))
						Log.sysOut("Session","login: "+mob.Name());
					needPrompt=true;
					Vector CMDS=null;
					while((!killFlag)&&(mob!=null))
					{
						status=Session.STATUS_OK;
						lastLoopTop=System.currentTimeMillis();
						waiting=true;
						if(suspendCommandLine)
						{
							input=null;
							try{Thread.sleep(100);}catch(Exception e){}
						}
						else
							input=readlineContinue();
						if(input!=null)
						{
							lastKeystroke=System.currentTimeMillis();
							setAfkFlag(false);
							CMDS=Util.parse(input);
							if(CMDS.size()>0)
							{
								waiting=false;
								setPreviousCmd(CMDS);
								milliTotal+=(lastStop-lastStart);
								
								if(snoops.size()>0)
									for(int s=0;s<snoops.size();s++)
										((Session)snoops.elementAt(s)).rawPrintln(input);
								
								lastStart=System.currentTimeMillis();
								mob.enqueCommand(CMDS,0);
								lastStop=System.currentTimeMillis();
							}
							needPrompt=true;
						}
						if(mob==null) break;

						if((spamStack>0)&&((lastOutput-System.currentTimeMillis())>100))
							onlyPrint("",0);

						if(!afkFlag())
						{
							if(getIdleMillis()>=600000)
								setAfkFlag(true);
						}
						else
						if((getIdleMillis()>=10800000)
						   &&(mob()!=null)
						   &&((System.currentTimeMillis()-lastBlahCheck)>=60000))
						{
							lastBlahCheck=System.currentTimeMillis();
							if((!Sense.isSleeping(mob))
							&&(mob().fetchEffect("Disease_Blahs")==null))
							{
								Ability A=CMClass.getAbility("Disease_Blahs");
								if(A!=null) A.invoke(mob,mob,true,0);
							}
							else
							if((Sense.isSleeping(mob))
							&&(mob().fetchEffect("Disease_Narcolepsy")==null))
							{
								Ability A=CMClass.getAbility("Disease_Narcolepsy");
								if(A!=null) A.invoke(mob,mob,true,0);
							}
						}

						if((needPrompt)&&(waiting))
						{
							showPrompt();
							if((input==null)&&(input!=null)&&(input.length()>0))
								rawPrint(input.toString());
							needPrompt=false;
						}
					}
					status=Session.STATUS_LOGOUT2;
				}
				else
				{
					mob=null;
					newMob.setSession(null);
				}
				status=Session.STATUS_LOGOUT;
			}
			status=Session.STATUS_LOGOUT3;
		}
		catch(SocketException e)
		{
			if(!isMaskedErrMsg(e.getMessage()))
				errorOut(e);
		}
		catch(Exception t)
		{
			if(!isMaskedErrMsg(t.getMessage()))
				errorOut(t);
		}
		status=Session.STATUS_LOGOUT3;

		if(mob!=null)
		{
			String name=mob.Name();
			if(name.trim().length()==0) name="Unknown";
			CommonMsgs.channel("WIZINFO","",name+" has logged out.",true);
			// the player quit message!
			if(mob.location()!=null)
			{
				FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_QUIT,null);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					if((follower!=null)&&(follower.location()!=mob.location()))
						follower.executeMsg(follower,msg);
				}
				if(mob.location()!=null)
					mob.location().send(mob,msg);
			}
			if(mob.playerStats()!=null)
				mob.playerStats().setLastDateTime(System.currentTimeMillis());
			Log.sysOut("Session","logout: "+name);
			mob.removeFromGame();
			mob.setSession(null);
			mob=null;
		}
		
		status=Session.STATUS_LOGOUT4;
		killFlag=true;
		waiting=false;
		needPrompt=false;
		snoops.clear();
		
		closeSocks();
		
		
		status=Session.STATUS_LOGOUT5;
		Sessions.removeElement(this);

		//finally
		//{
		//}
		status=Session.STATUS_LOGOUTFINAL;
	}
}
