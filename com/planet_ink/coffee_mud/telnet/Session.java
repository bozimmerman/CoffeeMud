package com.planet_ink.coffee_mud.telnet;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class Session extends Thread 
{
	public Socket sock;
	public BufferedReader in;
	public PrintWriter out;
	public MOB mob;
	public boolean killFlag=false;
	public boolean needPrompt=false;
	public StringBuffer input=new StringBuffer("");
	boolean waiting=false;
	public static final int SOTIMEOUT=300;
	public Vector previousCmd=new Vector();
	public Vector ondeckCmd=null;
	
	
	private Vector cmdQ=new Vector();

	public Session(Socket s, BufferedReader newin, PrintWriter newout)
	{
		sock=s;
		in=newin;
		out=newout;
		try
		{
			if(sock!=null)
				sock.setSoTimeout(SOTIMEOUT);
		}
		catch(SocketException e)
		{
		}
	}
	
	public Vector deque()
	{
		Vector returnable=null;
		
		if(cmdQ.size()>0) 
		synchronized(cmdQ)
		{
			Input thisInput=(Input)cmdQ.elementAt(0);
			if(thisInput.tickDown==0)
			{
				cmdQ.removeElementAt(0);
				returnable=thisInput.commandEntered;
			}
			else
				thisInput.tickDown--;
		}
		return returnable;
	}

	public void setPreviousCmd(Vector cmds)
	{
		if(cmds==null) return;
		
		if((cmds.size()>0)&&(((String)cmds.elementAt(0)).trim().equals("!")))
			return;
			
		previousCmd.removeAllElements();
		for(int i=0;i<cmds.size();i++)
			previousCmd.addElement(((String)cmds.elementAt(i)).toString());
	}
	
	public void enque(int tickDown, Vector commands)
	{
		if(commands==null) 
			return;
		synchronized(cmdQ)
		{
			Input newQ=new Input();
			newQ.tickDown=tickDown;
			newQ.commandEntered=commands;
			cmdQ.addElement(newQ);
		}
	}
	
	private void errorOut(Exception t)
	{
		Log.errOut("Session",t);
		MUD.allSessions.removeElement(this);
		killFlag=true;
	}
	
	public void rawPrintln(String msg)
	{
		if(!needPrompt)
			out.print("\n\r");
		if((out==null)||(msg==null)) return;
		out.print(msg+"\n\r");
		out.flush();
		needPrompt=true;
	}

	public void rawPrint(String msg)
	{
		if(!needPrompt)
			out.print("\n\r");
		if((out==null)||(msg==null)) return;
		out.print(msg);
		out.flush();
		needPrompt=true;
	}

	public void print(String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(filter(mob,mob,msg));
		out.flush();
	}

	
	public void stdPrint(String msg)
	{
		if((out==null)||(msg==null)) return;
		
		if(!needPrompt)
			out.print("\n\r");
		print(msg);
		needPrompt=true;
	}

	
	public void print(Environmental Source, 
					  Environmental Target,
					  String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(filter(Source,Target,msg));
		out.flush();
	}
	
	public void stdPrint(Environmental Source, 
					  Environmental Target,
					  String msg)
	{
		if((out==null)||(msg==null)) return;
		if(!needPrompt)
			out.print("\n\r");
		print(Source,Target,msg);
		needPrompt=true;
		
	}
	
	public void print(String msg, 
					  int Length, 
					  String msgEnd)
	{
		if((out==null)||(msg==null)) return;
		while(msg.length()<Length)
			msg=msg+" ";
		msg=msg+msgEnd;
		out.print(filter(mob,mob,msg));
		out.flush();
	}
	
	public void stdPrint(String msg,
						 int Length,
						 String msgEnd)
	{
		if((out==null)||(msg==null)) return;
		if(!needPrompt)
			out.print("\n\r");
		print(msg,Length,msgEnd);
		needPrompt=true;
		
	}
	
	public void println(String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(filter(mob,mob,msg)+"\n\r");
		out.flush();
	}
	
	public void stdPrintln(String msg)
	{
		if((out==null)||(msg==null)) return;
		if(!needPrompt)
			out.print("\n\r");
		println(msg);
		needPrompt=true;
	}
	
	public void println(Environmental Source, 
						Environmental Target, 
						String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(filter(Source,Target,msg)+"\n\r");
		out.flush();
	}
	
	public void stdPrintln(Environmental Source,
						   Environmental Target,
						   String msg)
	{
		if((out==null)||(msg==null)) return;
		if(!needPrompt)
			out.print("\n\r");
		println(Source,Target,msg);
		needPrompt=true;
	}
	
	public String prompt(String Message, String Default)
	{
		String Msg=prompt(Message).trim();
		if(Msg.equals("")) return Default;
		else return Msg;
	}

	public String prompt(String Message)
	{
		print(Message);
		String input=blockingIn();
		if(input==null) return "";
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

	public String filter(Environmental source, 
						 Environmental target, 
						 String msg)
	{
		if(mob==null) return msg;
		
		if(msg.length()==0) return msg;
		
		boolean doSagain=false;
		StringBuffer buf=new StringBuffer(msg);
		
		int len=78;
		int loop=0;
		int lastSpace=0;
		while(buf.length()>loop)
		{
			int lastSp=-1;
			boolean breakPlease=false;
			while((!breakPlease)&&(loop<len)&&(buf.length()>loop))
			{
				switch(buf.charAt(loop))
				{
				case ' ':
					{
						if(lastSp>lastSpace)
							lastSpace=lastSp;
						lastSp=loop;
					}
					break;
				case '\n':
					{
						if((loop<buf.length()-1)&&(buf.charAt(loop+1)!='\r'))
							buf.insert(loop+1,'\r');
						len=loop+78;
						lastSpace=loop;
					}
					break;
				case '\r':
					{
						len=loop+78;
						lastSpace=loop;
					}
					break;
				case '`':
					buf.setCharAt(loop,'\'');
					break;
				case '(':
					if(((loop<buf.length()-2)&&(buf.charAt(loop+2)==')')&&(Character.toUpperCase(buf.charAt(loop+1))=='S'))
					||((loop<buf.length()-3)&&(buf.charAt(loop+3)==')')&&(Character.toUpperCase(buf.charAt(loop+1))=='E')&&(Character.toUpperCase(buf.charAt(loop+2))=='S')))
					{
						int lastParen=loop+2;
						if(Character.toUpperCase(buf.charAt(loop+1))=='E')
							lastParen++;
						
						String lastWord=null;
						if(lastSp>lastSpace)
							lastWord=buf.substring(lastSpace,lastSp).trim().toUpperCase();
						else
						{
							lastWord="";
							for(int i=(lastSpace-1);((i>=0)&&(buf.charAt(i)!=' '));i--)
								lastWord=buf.charAt(i)+lastWord;
						}
						
						if((lastWord!=null)&&(lastWord.endsWith("A")||lastWord.endsWith("YOU")||lastWord.endsWith(" 1")||doSagain))
						{
							buf.delete(loop,lastParen+1);
							doSagain=true;
							loop--;
						}
						else
						{
							buf.deleteCharAt(lastParen);
							buf.deleteCharAt(loop);
						}
							
					}
					break;
				case '\\':
					if(loop<buf.length()-1)
					{
						if(buf.charAt(loop+1)=='n')
						{
							buf.setCharAt(loop,'\n');
							if((loop>=buf.length()-2)||((loop<buf.length()-2)&&(buf.charAt(loop+2)!='\r')))
								buf.setCharAt(loop+1,'\r');
							else
							if(loop<buf.length()-2)
								buf.deleteCharAt(loop+1);
						}
						else
						if(buf.charAt(loop+1)=='`')
						{
							buf.setCharAt(loop,'\'');
							buf.deleteCharAt(loop+1);
						}
					}
					break;
				case '<':
					{
						int endDex=loop;
						StringBuffer cmd=new StringBuffer("");
						int ldex=loop+1;
						for(;(ldex<loop+11)&&(ldex<buf.length());ldex++)
							if(buf.charAt(ldex)=='>')
								break;
							else
								cmd.append(Character.toUpperCase(buf.charAt(ldex)));
						if((buf.charAt(ldex)=='>')&&(cmd.length()>4))
						{
							Environmental regarding=source;
							if(Character.toUpperCase(cmd.charAt(0))=='T')
								regarding=target;
							String replacement=null;
							if((cmd.length()==6)&&(cmd.charAt(2)=='N'))
							{
								if(regarding==null)
									replacement="";
								else
								if(mob==regarding)
									replacement="you";
								else
								if((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))
									replacement=((regarding instanceof MOB)?"someone":"something");
								else
									replacement=regarding.name();
							}
							else
							if((cmd.length()==9)&&(cmd.charAt(4)=='S'))
							{
								if(regarding==null)
									replacement="";
								else
								if(mob==regarding)
									replacement="your";
								else
								if(regarding instanceof MOB)
									replacement=((MOB)regarding).charStats().hisher();
								else
									replacement="its";
									
							}
							else
							if((cmd.length()==9)&&(cmd.charAt(4)=='M'))
							{
								if(regarding==null)
									replacement="";
								else
								if(mob==regarding)
									replacement="you";
								else
								if(regarding instanceof MOB)
									replacement=((MOB)regarding).charStats().himher();
								else
									replacement="it";
								
							}
							else
							if((cmd.length()==8)&&(cmd.charAt(4)=='-'))
							{
								if(regarding==null)
									replacement="";
								else
								if(mob==regarding)
									replacement="you";
								else
								if(regarding instanceof MOB)
									replacement=((MOB)regarding).charStats().heshe();
								else
									replacement="its";
							}
							if(replacement!=null)
							{
								buf.delete(loop,ldex+1);
								buf.insert(loop,replacement.toCharArray());
							}
						}
					}
					break;
				default:
					break;
					
				}
				loop++;
			}
			if((len<buf.length())&&(loop!=lastSp)&&(lastSp>=0))
			{
				buf.setCharAt(lastSp,'\n');
				buf.insert(lastSp,'\r');
				loop=lastSp+2;
			}
			len=loop+78;
		}
		if(Character.toUpperCase(buf.charAt(0))!=buf.charAt(0))
			buf.setCharAt(0,Character.toUpperCase(buf.charAt(0)));
		return buf.toString();
	}
	
	
	private String preFilter(StringBuffer input)
	{
		if(input==null) return null;
		
		int x=0;
		while(x<input.length())
		{
			char c=input.charAt(x);
			if(c=='\'')
			{
				input.setCharAt(x,'\\');
				input.insert(x+1,'`');
				x++;
			}
			else
			if(c==8)
			{
				String newStr=input.toString();
				if(x==0)
					input=new StringBuffer(newStr.substring(x+1));
				else
				{
					input=new StringBuffer(newStr.substring(0,x-1)+newStr.substring(x+1));
					x--;
				}
				x--;
			}
			x++;
		}
		return input.toString();
	}
	
	public String blockingIn()
	{
		if((in==null)||(out==null)) return "";
		input=new StringBuffer("");
		try
		{
			while(!killFlag)
			{
				try
				{
					int c=in.read();
					if(c==13)
						break;
					else
					if((c>0)&&(c!=10))
						input.append((char)c);
				}
				catch(InterruptedIOException e)
				{
				}
			}
		}
		catch(SocketException s)
		{
			return null;
		}
		catch(IOException ioe)
		{
			Log.errOut("Session",ioe);
			return null;
		}
		String inStr=preFilter(input);
		input=new StringBuffer("");
		return inStr;
	}
	
	public String readlineContinue()
		throws IOException
	{
		
		if((in==null)||(out==null)) return "";
		
		while(!killFlag)
		{
			try
			{
				int c=in.read();
				if(c==10)
					c=-1;
				else
				if(c==13)
					break;
				if(c>0)
					input.append((char)c);
			}
			catch(InterruptedIOException e)
			{
				return null;
			}
		}
		
		String inStr=preFilter(input);
		input=new StringBuffer("");
		return inStr;
	}
	
	public boolean confirm(String Message, String Default)
	throws IOException
	{
		String YN=choose(Message,"YN",Default);
		if(YN.equals("Y"))
			return true;
		return false;
	}
	
	public String choose(String Message, String Choices, String Default)
	throws IOException
	{
		String YN="";
		while((YN.equals(""))||(Choices.indexOf(YN)<0)&&(!killFlag))
		{
			print(Message);
			YN=blockingIn();
			if(YN==null){ return Default.toUpperCase(); }
			YN=YN.trim().toUpperCase();
			if(YN.equals("")){ return Default.toUpperCase(); }
			if(YN.length()>1) YN=YN.substring(0,1);
		}
		return YN;
	}
	
	public void run() 
	{
		try
		{
			boolean done=false;
			long tries=5;
			while((!killFlag)&&((--tries)>0))
			{
				MOB newMob=new StdMOB();
				newMob.setSession(this);
				mob=newMob;
				if(FrontDoor.login(newMob))
				{
					if((!killFlag)&&(mob!=null))
						Log.sysOut("Session","login: "+mob.name());
					needPrompt=true;
					Vector CMDS=null;
					while((!killFlag)&&(mob!=null))
					{
						waiting=true;
						String input=readlineContinue();
						if(input!=null)
							CMDS=CommandProcessor.parse(input);
						else
							CMDS=ondeckCmd;
					
						if(CMDS!=null)
						{
							waiting=false;
							if(CMDS.size()>0)
							{
								if((CMDS==ondeckCmd)||(!mob.isInCombat()))
								{
									if(CMDS==ondeckCmd)
										ondeckCmd=null;
									setPreviousCmd(CMDS);
									CommandProcessor.doCommand(mob,CMDS);
								}
								else
								if((cmdQ.size()==0)&&(ondeckCmd==null))
									ondeckCmd=CMDS;
								else
									enque(1,CMDS);
							}
							needPrompt=true;
						}
						if((needPrompt)&&(waiting))
						{
							print("\n\r<"+mob.curState().getHitPoints()+"hp "+mob.curState().getMana()+"m "+mob.curState().getMovement()+"mv>");
							if((input==null)&&(this.input!=null)&&(this.input.length()>0))
								this.rawPrint(this.input.toString());
							needPrompt=false;
						}
					}
					if(mob!=null)
						Log.sysOut("Session","logout: "+mob.name());
				}
				else
				{
					mob=null;
					newMob.setSession(null);
				}
			}
		} 
		catch(SocketException e) 
		{
			if(e.getMessage()==null)
				errorOut(e);
			else
			if((e.getMessage().indexOf("reset by peer")<0)&&(e.getMessage().indexOf("socket closed")<0)&&(e.getMessage().indexOf("tream closed")<0))
			{
				errorOut(e);
			}
		}
		catch(Exception t) 
		{
			if(t.getMessage()==null)
				errorOut(t);
			else
			if((t.getMessage().indexOf("reset by peer")<0)&&(t.getMessage().indexOf("socket closed")<0)&&(t.getMessage().indexOf("tream closed")<0))
			{
				errorOut(t);
			}
		}
		
		MUD.allSessions.removeElement(this);
		
		if(mob!=null) 
		{
			mob.destroy();
			mob.setSession(null);
			mob=null;
		}
		killFlag=true;
		waiting=false;
		needPrompt=false;
		
		try
		{
			if(in!=null)
				in.close();
			if(out!=null)
				out.close();
			if(sock!=null)
				sock.close();
			in=null;
			out=null;
			sock=null;
		
		} 
		catch(IOException e) 
		{
		}
	}
}