package com.planet_ink.coffee_mud.system;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class TelnetSession extends Thread implements Session
{
	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	private MOB mob;
	private boolean killFlag=false;
	private boolean needPrompt=false;
	private StringBuffer input=new StringBuffer("");
	private boolean waiting=false;
	private static final int SOTIMEOUT=300;
	private Vector previousCmd=new Vector();

	private Calendar lastCMDDateTime=Calendar.getInstance();
	private Vector cmdQ=new Vector();

	private boolean lastWasCR=false;

	public static String[] clookup=null;
	private int termID = 0;	//0 = NOANSI, 1 = ANSI
	public int currentColor=(int)'N';
	public int lastColor=-1;
	private final static int HISHER=0;
	private final static int HIMHER=1;
	private final static int NAME=2;
	private final static int NAMESELF=3;
	private final static int HESHE=4;
	private final static int ISARE=5;
	private final static int HASHAVE=6;
	private final static int YOUPOSS=7;
	private static Hashtable tagTable=null;

	private static int sessionCounter=0;
	public TelnetSession(Socket s, String introTextStr)
	{
		super("TelnetSession."+sessionCounter);
		++sessionCounter;
		if(tagTable==null)
		{
			tagTable=new Hashtable();
			tagTable.put("-HIS-HER",new Integer(HISHER));
			tagTable.put("-HIM-HER",new Integer(HIMHER));
			tagTable.put("-NAME",new Integer(NAME));
			tagTable.put("-NAMESELF",new Integer(NAMESELF));
			tagTable.put("-HE-SHE",new Integer(HESHE));
			tagTable.put("-IS-ARE",new Integer(ISARE));
			tagTable.put("-HAS-HAVE",new Integer(HASHAVE));
			tagTable.put("-YOUPOSS",new Integer(YOUPOSS));
		}
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

	public MOB mob(){return mob;}
	public void setMob(MOB newmob)
	{ mob=newmob;}
	public boolean killFlag(){return killFlag;}
	public void setKillFlag(boolean truefalse){killFlag=truefalse;}
	public Vector previousCMD(){return previousCmd;}

	public Vector deque()
	{
		Vector returnable=null;

		synchronized(cmdQ)
		{
			if(cmdQ.size()>0)
			{
				Vector topCMD=(Vector)cmdQ.elementAt(0);
				cmdQ.removeElement(topCMD);
				int topTick=0;
				if(topCMD.size()>0)
				{
					topTick=((Integer)topCMD.elementAt(0)).intValue();
					topCMD.removeElementAt(0);
				}
				if(topTick==0)
					returnable=topCMD;
				else
				{
					topTick--;
					topCMD.insertElementAt(new Integer(topTick),0);
					cmdQ.insertElementAt(topCMD,0);
				}
			}
		}
		return returnable;
	}

	public void setPreviousCmd(Vector cmds)
	{
		if(cmds==null) return;
		if(cmds.size()==0) return;
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
			
			if((commands.size()>0)&&(commands.elementAt(0) instanceof Integer))
				commands.removeElementAt(0);
			commands.insertElementAt(new Integer(tickDown),0);
			cmdQ.addElement(commands);
		}
	}

	private void errorOut(Exception t)
	{
		Log.errOut("Session",t);
		Sessions.removeElement(this);
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
		out.print(filter(mob,mob,msg,false));
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
		out.print(filter(Source,Target,msg,false));
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
		out.print(filter(mob,mob,msg, false));
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
		out.print(filter(mob,mob,msg,false)+"\n\r");
		out.flush();
	}

	public void unfilteredPrintln(String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(filter(mob,mob,msg,true)+"\n\r");
		out.flush();
	}

	public void unfilteredPrint(String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(filter(mob,mob,msg,true));
		out.flush();
	}

	public void colorOnlyPrintln(String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(colorOnlyFilter(msg)+"\n\r");
		out.flush();
	}

	public void colorOnlyPrint(String msg)
	{
		if((out==null)||(msg==null)) return;
		out.print(colorOnlyFilter(msg));
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
		out.print(filter(Source,Target,msg,false)+"\n\r");
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
		throws IOException
	{
		String Msg=prompt(Message).trim();
		if(Msg.equals("")) return Default;
		else return Msg;
	}

	public String prompt(String Message)
		throws IOException
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

	public final int getColor(char c)
	{
		int i;
		
		// warning do not nest!
		if (c == '?') return lastColor;
		if (((int)c)>255) return -1;
		return (int)c;
	}
	
	public final String makeEscape(int c)
	{
		if (termID == 1 && c != -1)
		{
			if(clookup==null)
			{
				clookup=new String[256];
				// N B H - normal bold highlight
				clookup[(int)'N']="\033[0;37m";
				clookup[(int)'B']="\033[1;37m";
				clookup[(int)'H']="\033[1;36m";
				// F S - NOT IN USE! fight spell
				clookup[(int)'F']="\033[1;31m";
				clookup[(int)'S']="\033[1;34m";
				// E T Q - NOT IN USE! emote talk channeltalk
				clookup[(int)'E']="\033[1;35m";
				clookup[(int)'T']="\033[1;32m";
				clookup[(int)'Q']="\033[0;36;44m";
				// X Y Z - important messages
				clookup[(int)'X']="\033[1;36;44m";
				clookup[(int)'Y']="\033[1;33;44m";
				clookup[(int)'Z']="\033[1;33;41m";
				//  R L D d - roomtitle roomdesc(look) Direction door
				clookup[(int)'R']="\033[1;32m";
				clookup[(int)'L']="\033[0;32m";
				clookup[(int)'D']="\033[1;36;44m";
				clookup[(int)'d']="\033[0;32m";
				// I M - item, mob
				clookup[(int)'I']="\033[0;36m";
				clookup[(int)'M']="\033[0;33m";
				// h m v - prompt colors
				clookup[(int)'h']="\033[1;32m";
				clookup[(int)'m']="\033[1;36m";
				clookup[(int)'v']="\033[1;34m";
				// fixed system colors
				clookup[(int)'w']="\033[1;37m";
				clookup[(int)'g']="\033[1;32m";
				clookup[(int)'b']="\033[1;34m";
				clookup[(int)'r']="\033[1;31m";
				clookup[(int)'y']="\033[1;33m";
				clookup[(int)'c']="\033[1;36m";
				clookup[(int)'p']="\033[1;35m";
			}
			if (c != currentColor)
			{
				lastColor = currentColor;
				currentColor = c;
				return clookup[c];
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

		if ((currentColor != ((int)'N'))&&(termID>0)) buf.append(makeEscape((int)'N'));
		
		return buf.toString();

	}	
	
	public String filter(Environmental source,
						 Environmental target,
						 String msg,
						 boolean wrapOnly)
	{
		if(mob==null) return msg;

		if(msg.length()==0) return msg;
		boolean doSagain=false;
		StringBuffer buf=new StringBuffer(msg);

		int len=78;
		int loop=0;
		int lastSpace=0;
		int firstAlpha=0;
		
		while(buf.length()>loop)
		{
			int lastSp=-1;
			while((loop<len)&&(buf.length()>loop))
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
				case '%':
					if(loop<buf.length()-2)
					{
						int dig1=("0123456789ABCDEF").indexOf(buf.charAt(loop+1));
						int dig2=("0123456789ABCDEF").indexOf(buf.charAt(loop+2));
						if((dig1>=0)&&(dig2>=0))
						{
							buf.setCharAt(loop,(char)((dig1*16)+dig2));
							buf.deleteCharAt(loop+1);
							if(buf.charAt(loop)==13)
								buf.setCharAt(loop+1,(char)10);
							else
								buf.deleteCharAt(loop+1);
						}
					}
					break;
				case '(':
					if(!wrapOnly)
					if(((loop<buf.length()-2)&&(buf.charAt(loop+2)==')')&&(Character.toUpperCase(buf.charAt(loop+1))=='S'))
					||((loop<buf.length()-3)&&(buf.charAt(loop+3)==')')&&(Character.toUpperCase(buf.charAt(loop+1))=='E')&&(Character.toUpperCase(buf.charAt(loop+2))=='S')))
					{
						int lastParen=loop+2;
						if(Character.toUpperCase(buf.charAt(loop+1))=='E')
							lastParen++;

						String lastWord="";
						if(lastSp>lastSpace)
						{
							lastWord=buf.substring(lastSpace,lastSp).trim();
							while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(0))))
								  lastWord=lastWord.substring(1);
							while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(lastWord.length()-1))))
								  lastWord=lastWord.substring(0,lastWord.length()-1);
						}
						else
						{
							for(int i=(lastSpace-1);((i>=0)&&(!Character.isLetterOrDigit(buf.charAt(i))));i--)
								lastWord=buf.charAt(i)+lastWord;
						}
						if((lastWord.equalsIgnoreCase("A")||lastWord.equalsIgnoreCase("YOU")||lastWord.equals("1")||doSagain))
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
						switch(buf.charAt(loop+1))
						{
						case 'n':
						case 'r':
							{
							buf.setCharAt(loop,'\n');
							if((loop>=buf.length()-2)||((loop<buf.length()-2)&&(buf.charAt(loop+2)!='\r')))
								buf.setCharAt(loop+1,'\r');
							else
							if(loop<buf.length()-2)
								buf.deleteCharAt(loop+1);
							}
							break;
						case '\'':
						case '`':
							{
							buf.setCharAt(loop,'\'');
							buf.deleteCharAt(loop+1);
							}
							break;
						}
					}
					break;
				case '<':
					if(!wrapOnly)
					{
						// supported here <?-HIS-HER>, <?-HIM-HER>, <?-NAME>,
						// <?-NAMESELF>, <?-HE-SHE>, <?-IS-ARE>, <?-HAS-HAVE>
						//int endDex=loop;
						StringBuffer cmd=new StringBuffer("");
						int ldex=loop+1;
						for(;(ldex<loop+11)&&(ldex<buf.length());ldex++)
							if(buf.charAt(ldex)=='>')
								break;
							else
								cmd.append(Character.toUpperCase(buf.charAt(ldex)));
						if((ldex<buf.length())&&(buf.charAt(ldex)=='>')&&(cmd.length()>4))
						{
							Environmental regarding=source;
							if(Character.toUpperCase(cmd.charAt(0))=='T')
								regarding=target;
							String replacement=null;
							Integer I=(Integer)tagTable.get(cmd.substring(1));
							if(I!=null)
							switch(I.intValue())
							{
							case NAME:
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
								break;
							case NAMESELF:
								{
									if(regarding==null)
										replacement="";
									else
									if((source==target)&&(mob==regarding))
										replacement="yourself";
									else
									if((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))
										replacement=((regarding instanceof MOB)?"someone":"something");
									else
									if(mob==regarding)
										replacement="you";
									else
									if(source==target)
										replacement=((regarding instanceof MOB)?(((MOB)regarding).charStats().himher()+"self"):"itself");
									else
										replacement=regarding.name();
								}
								break;
							case YOUPOSS:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="your";
									else
									if((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))
										replacement=((regarding instanceof MOB)?"someone's":"something's");
									else
										replacement=regarding.name()+"'s";
								}
								break;
							case HISHER:
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
								break;
							case HIMHER:
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
								break;
							case HESHE:
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
								break;
							case ISARE:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="are";
									else
									if(regarding instanceof MOB)
										replacement="is";
								}
								break;
							case HASHAVE:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
										replacement="have";
									else
									if(regarding instanceof MOB)
										replacement="has";
								}
								break;
							}
							if(replacement!=null)
							{
								buf.delete(loop,ldex+1);
								buf.insert(loop,replacement.toCharArray());
							}
						}
					}
					break;
					case '^':
					{
						if (loop<buf.length()-1)
						{
							int colorID = getColor( buf.charAt(loop+1) );
							if (colorID != -1)
							{
								String colorEscStr = makeEscape(colorID);
								int csl=0;
								if (colorEscStr != null)
								{
									csl = colorEscStr.length();
									if (csl > 0)
										buf.replace(loop,loop+2 ,colorEscStr);
								}
								if (loop == 0)
									firstAlpha = csl;
								if (csl == 0)
								{
									// remove the color code
									buf.deleteCharAt(loop);
									buf.deleteCharAt(loop);
									loop-=1;
								}
								else
								{
									loop+=csl-1;	// already processed 1 char
									len+=csl;		// does not count for any length
								}
							}
						}
						break;
					}
				default:
					break;

				}
				loop++;
			}

			
			if((len<buf.length())
			   &&(loop!=lastSp)
			   &&(lastSp>=0)
			   &&(buf.charAt(loop)!='\n')
			   &&(buf.charAt(loop)!='\r'))
			{
				if(buf.charAt(lastSp+1)==' ')
				{
					buf.setCharAt(lastSp,'\r');
					buf.setCharAt(lastSp+1,'\n');
				}
				else
				{
					buf.setCharAt(lastSp,'\n');
					buf.insert(lastSp,'\r');
				}
				loop=lastSp+2;
			}
			len=loop+78;
		}

		buf.setCharAt(firstAlpha,Character.toUpperCase(buf.charAt(firstAlpha)));
		if ((currentColor != ((int)'N'))&&(termID>0)) buf.append(makeEscape((int)'N'));
		
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
				input.setCharAt(x,'`');
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



	private boolean appendInput(int c)
	{
		boolean rv = false;
		switch (c)
		{

			case 10:
			{
				c=-1;
				if (!lastWasCR)
					rv = true;
//				lastWasCR = false;
				break;
			}
			case 13:
			{
//				out.print("\n\r");
				c=-1;
				rv = true;
				lastWasCR = true;
				break;
			}
			case 26:
			{
				// don't let them enter ANSI escape sequences...
				c = -1;
				break;
			}
			default:
			{
				lastWasCR = false;
				break;
			}
		}

		if(c>0)
			input.append((char)c);
		return rv;
	}

	public String blockingIn()
		throws IOException
	{
		if((in==null)||(out==null)) return "";
		input=new StringBuffer("");
		while(!killFlag)
		{
			try
			{
				int c=in.read();
				if(c<0)
					throw new IOException("reset by peer");
				else
				if (appendInput(c))
					break;
			}
			catch(InterruptedIOException e)
			{
			}
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
				if(c<0)
					throw new IOException("Connection reset by peer.");
				else
				if (appendInput(c))
					break;
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

	public void logoff()
	{
		killFlag=true;
		this.interrupt();
		try{Thread.sleep(1000);}catch(Exception i){}
	}

	public void setTermID(int tid)
	{
		if (tid != 0)
			termID = 1;
		else
			termID = 0;
	}

	public int getTermID()
	{
		return termID;
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
	
	public void run()
	{
		try
		{
			long tries=5;
			while((!killFlag)&&((--tries)>0))
			{
				MOB newMob=(MOB)CMClass.getMOB("StdMOB");
				newMob.setSession(this);
				mob=newMob;
				if(ExternalPlay.login(newMob))
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
							enque(0,Util.parse(input));
						if(mob==null) break;
						if((((MOB)mob).lastTickedDateTime().after(lastCMDDateTime))||(!mob.isInCombat()))
						{
							CMDS=deque();
							if(CMDS!=null)
							{
								waiting=false;
								if(CMDS.size()>0)
								{
									setPreviousCmd(CMDS);
									ExternalPlay.doCommand(mob,CMDS);
									lastCMDDateTime=Calendar.getInstance();
								}
								needPrompt=true;
							}
						}
						if((needPrompt)&&(waiting))
						{
							print("\n\r^N<^h"+mob.curState().getHitPoints()+"hp^N ^m"+mob.curState().getMana()+"m^N ^v"+mob.curState().getMovement()+"mv^N>");
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
		catch(InterruptedException x)
		{
			Log.sysOut("Session","Interrupted!");
		}
		catch(SocketException e)
		{
			if(e.getMessage()==null)
				errorOut(e);
			else
			if((e.getMessage().indexOf("reset by peer")<0)
			&&(e.getMessage().indexOf("socket closed")<0)
			&&(e.getMessage().indexOf("timed out")<0)
			&&(e.getMessage().indexOf("tream closed")<0))
			{
				errorOut(e);
			}
		}
		catch(Exception t)
		{
			if(t.getMessage()==null)
				errorOut(t);
			else
			if((t.getMessage().indexOf("reset by peer")<0)
			 &&(t.getMessage().indexOf("socket closed")<0)
			 &&(t.getMessage().indexOf("timed out")<0)
			 &&(t.getMessage().indexOf("tream closed")<0))
			{
				errorOut(t);
			}
		}

		Sessions.removeElement(this);

		if(mob!=null)
		{
			mob.destroy();
			mob.setSession(null);
			mob=null;
		}
		killFlag=true;
		waiting=false;
		needPrompt=false;

		//try
		//{
			// might be source of lockup.
			//if(in!=null)
			//	in.close();
			//if(out!=null)
			//	out.close();
			//if(sock!=null)
			//	sock.close();
			in=null;
			out=null;
			sock=null;

		//}
		//catch(IOException e)
		//{
		//}
		//finally
		//{
		//}
	}
}