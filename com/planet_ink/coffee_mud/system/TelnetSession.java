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
	private int pageBreak=-1;
	private long lastOutput=0;
	private Vector cmdQ=new Vector();
	private Vector snoops=new Vector();
	private final static String hexStr="0123456789ABCDEF";
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

	public long lastStart=System.currentTimeMillis();
	public long lastStop=System.currentTimeMillis();
	public long lastLoopTop=System.currentTimeMillis();
	public long milliTotal=0;
	public long tickTotal=0;
	public long lastKeystroke=0;

	private int termID = 0;	//1 = ANSI, 2 = SOUND/MUSIC
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
	private final static int HIMHERSELF=8;
	private final static int HISHERSELF=9;
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
			tagTable.put("-HIM-HERSELF",new Integer(HIMHERSELF));
			tagTable.put("-HIS-HERSELF",new Integer(HISHERSELF));
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
	public void startSnooping(Session S)
	{
		if(!snoops.contains(S))
			snoops.addElement(S);
	}
	public void stopSnooping(Session S)
	{
		while(snoops.contains(S))
			snoops.removeElement(S);
	}
	public boolean amSnooping(Session S)
	{
		return(snoops.contains(S));
	}

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
		if((cmds.size()>0)&&(((String)cmds.elementAt(0)).trim().startsWith("!")))
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

	public synchronized void onlyPrint(String msg)
	{
		if((out==null)||(msg==null)) return;
		
		if(snoops.size()>0)
			for(int s=0;s<snoops.size();s++)
				((Session)snoops.elementAt(s)).onlyPrint(msg);
		
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
		if(pageBreak<0)
			pageBreak=CommonStrings.getIntVar(CommonStrings.SYSTEMI_PAGEBREAK);
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
	
	public void rawPrint(String msg)
	{ if(msg==null)return; 
	  onlyPrint((needPrompt?"":"\n\r")+msg);
	  needPrompt=true;
	}

	public void print(String msg)
	{ onlyPrint(filter(mob,mob,null,msg,false)); }
	
	public void rawPrintln(String msg)
	{ if(msg==null)return; rawPrint(msg+"\n\r");}

	public void stdPrint(String msg)
	{ rawPrint(filter(mob,mob,null,msg,false)); }

	public void print(Environmental src, Environmental trg, Environmental tol, String msg)
	{ onlyPrint((filter(src,trg,tol,msg,false)));}

	public void stdPrint(Environmental src, Environmental trg, Environmental tol, String msg)
	{ rawPrint(filter(src,trg,trg,msg,false)); }

	public void println(String msg)
	{ if(msg==null)return; print(msg+"\n\r");}

	public void unfilteredPrintln(String msg)
	{ if(msg==null)return; 
	  onlyPrint(filter(mob,mob,null,msg,true)+"\n\r");
	  needPrompt=true;
	}

	public void unfilteredPrint(String msg)
	{ onlyPrint(filter(mob,mob,null,msg,true));
	  needPrompt=true;
	}

	public void colorOnlyPrintln(String msg)
	{ if(msg==null)return; 
	  onlyPrint(colorOnlyFilter(msg)+"\n\r");
	  needPrompt=true;
	}

	public void colorOnlyPrint(String msg)
	{ onlyPrint(colorOnlyFilter(msg));
	  needPrompt=true;
	}

	public void stdPrintln(String msg)
	{ if(msg==null)return; 
	  rawPrint(filter(mob,mob,null,msg,false)+"\n\r"); 
	}

	public void println(Environmental src, Environmental trg, Environmental tol, String msg)
	{ if(msg==null)return; 
	  onlyPrint(filter(src,trg,tol,msg,false)+"\n\r");
	}

	public void stdPrintln(Environmental src,Environmental trg, Environmental tol, String msg)
	{ if(msg==null)return; 
	  rawPrint(filter(src,trg,tol,msg,false)+"\n\r"); 
	}

	public void setPromptFlag(boolean truefalse)
	{
		needPrompt=truefalse;
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

	public final int getColor(char c)
	{
		int i;

		// warning do not nest!
		if (c == '?') return lastColor;
		if (((int)c)>255) return -1;
		return (int)c;
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
					clookup[(int)sub.charAt(0)]=sub.substring(1);
					x=changes.indexOf("#");
				}
				for(int i=0;i<clookup.length;i++)
				{
					String s=clookup[i];
					if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
						clookup[i]=clookup[(int)s.charAt(1)];
				}
			}
		}
		return clookup;
	}

	public final String makeEscape(int c)
	{
		if((c<='9')&&(c>='0'))
		{
			if((termID&2)==2)
				return CommonStrings.getVar(CommonStrings.SYSTEM_ESC0+(((int)c)-((int)'0')));
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

		if ((currentColor != ((int)'N'))&&(((termID&1)==1))) 
			buf.append(makeEscape((int)'N'));

		return buf.toString();

	}
	
	public String filter(Environmental source,
						 Environmental target,
						 Environmental tool,
						 String msg,
						 boolean wrapOnly)
	{
		if(mob==null) return msg;
		if(msg==null) return null;
		
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
				case (char)13:
					{
						if(((loop<buf.length()-1)&&(((int)buf.charAt(loop+1))!=10))
						&&((loop>0)&&(((int)buf.charAt(loop-1))!=10)))
							buf.insert(loop+1,(char)10);
						len=loop+78;
						lastSpace=loop;
					}
					break;
				case (char)10:
					{
						len=loop+78;
						lastSpace=loop;
					}
					break;
				case '`':
					buf.setCharAt(loop,'\'');
					break;
				case '!':
					if((loop<buf.length()-10)
					&&(buf.charAt(loop+1)=='!')
					&&((buf.substring(loop+2,loop+7).equalsIgnoreCase("sound"))
					   ||(buf.substring(loop+2,loop+7).equalsIgnoreCase("music"))))
					{
						int x=buf.indexOf("(",loop+7);
						int y=buf.indexOf(")",loop+7);
						if((x>=0)&&(y>=x))
						{
							if(((termID&2)==2)
							&&((source==null)
							   ||(source==mob)
							   ||(Sense.canBeHeardBy(source,mob))))
							{
								loop=y;
								len=len+(y-loop)+1;
							}
							else
							{
								buf.delete(loop,y+1);
								loop--;
							}
						}
					}
					break;
				case '&':
					if(loop<buf.length()-3)
					{
						if(buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
							buf.replace(loop,loop+3,"<");
						else
						if(buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))
							buf.replace(loop,loop+3,">");
					}
					break;
				case '%':
					if(loop<buf.length()-2)
					{
						int dig1=hexStr.indexOf(buf.charAt(loop+1));
						int dig2=hexStr.indexOf(buf.charAt(loop+2));
						if((dig1>=0)&&(dig2>=0))
						{
							buf.setCharAt(loop,(char)((dig1*16)+dig2));
							buf.deleteCharAt(loop+1);
							if(((int)buf.charAt(loop))==13)
								buf.setCharAt(loop+1,(char)10);
							else
								buf.deleteCharAt(loop+1);
						}
					}
					break;
				case '(':
					if((!wrapOnly)&&(loop<(buf.length()-1)))
					{
						char c2=Character.toUpperCase(buf.charAt(loop+1));
						if(((loop<buf.length()-2)&&(buf.charAt(loop+2)==')')&&(c2=='S'))
						||((loop<buf.length()-3)&&(buf.charAt(loop+3)==')')&&(Character.toUpperCase(buf.charAt(loop+2))=='S')&&((c2=='Y')||(c2=='E'))))
						{
							String lastWord="";
							if(lastSp>lastSpace)
							{
								lastWord=Util.removeColors(buf.substring(lastSpace,lastSp)).trim().toUpperCase();
								while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(0))))
									  lastWord=lastWord.substring(1);
								while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(lastWord.length()-1))))
									  lastWord=lastWord.substring(0,lastWord.length()-1);
							}
							else
							{
								for(int i=(lastSpace-1);((i>=0)&&(!Character.isLetterOrDigit(buf.charAt(i))));i--)
									lastWord=buf.charAt(i)+lastWord;
								lastWord=Util.removeColors(lastWord).trim().toUpperCase();
							}

							int lastParen=(c2=='S')?loop+2:loop+3;
							if((lastWord.equals("A")||lastWord.equals("YOU")||lastWord.equals("1")||doSagain))
							{
								if(c2=='Y')
									buf.replace(loop,lastParen+1,Util.sameCase("y",buf.charAt(loop+1)));
								else
									buf.delete(loop,lastParen+1);
								doSagain=true;
								loop--;
							}
							else
							{
								if(c2=='Y')
									buf.replace(loop,lastParen+1,Util.sameCase("ies",buf.charAt(loop+1)));
								else
								{
									buf.deleteCharAt(lastParen);
									buf.deleteCharAt(loop);
								}
							}
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
							buf.setCharAt(loop,(char)13);
							if((loop>=buf.length()-2)||((loop<buf.length()-2)&&(((int)buf.charAt(loop+2))!=10)))
								buf.setCharAt(loop+1,(char)10);
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
						for(;(ldex<loop+14)&&(ldex<buf.length());ldex++)
							if(buf.charAt(ldex)=='>')
								break;
							else
								cmd.append(Character.toUpperCase(buf.charAt(ldex)));
						if((ldex<buf.length())&&(buf.charAt(ldex)=='>')&&(cmd.length()>4))
						{
							Environmental regarding=null;
							switch(Character.toUpperCase(cmd.charAt(0)))
							{
							case 'S': regarding=source; break;
							case 'T': regarding=target; break;
							case 'O': regarding=tool; break;
							}
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
									if(((source==target)||(target==null))&&(mob==regarding))
										replacement="yourself";
									else
									if(mob==regarding)
										replacement="you";
									else
									if((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))
										replacement=((regarding instanceof MOB)?"someone":"something");
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
							case HIMHERSELF:
								{
									if(regarding==null)
										replacement="themself";
									else
									if(mob==regarding)
										replacement="yourself";
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().himher()+"self";
									else
										replacement="itself";

								}
								break;
							case HISHERSELF:
								{
									if(regarding==null)
										replacement="themself";
									else
									if(mob==regarding)
										replacement="yourself";
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().hisher()+"self";
									else
										replacement="itself";
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
								loop--;
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
								if((colorID<((int)'0'))||(colorID>((int)'9')))
								{
									loop+=csl-1;	// already processed 1 char
									len+=csl;		// does not count for any length
								}
								else
								{
									loop--;
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
			   &&(((int)buf.charAt(loop))!=13)
			   &&(((int)buf.charAt(loop))!=10))
			{
				if(buf.charAt(lastSp+1)==' ')
				{
					buf.setCharAt(lastSp,(char)13);
					buf.setCharAt(lastSp+1,(char)10);
				}
				else
				{
					buf.setCharAt(lastSp,(char)13);
					buf.insert(lastSp,(char)10);
				}
				loop=lastSp+2;
			}
			len=loop+78;
		}

		if((firstAlpha>=0)&&(firstAlpha<buf.length()))
			buf.setCharAt(firstAlpha,Character.toUpperCase(buf.charAt(firstAlpha)));
		if ((currentColor != ((int)'N'))&&((termID&1)==1))
			buf.append(makeEscape((int)'N'));

		/* fabulous debug code
		for(int i=0;i<buf.length();i+=25)
		{
			for(int x=0;x<25;x++)
			{
				if((i+x)<buf.length())
				{
					char c=buf.charAt(i+x);
					if((c!='\r')&&(c!='\n'))
						System.out.print(c);
					else
						System.out.print("?");
				}
			}
			System.out.print(" ");
			for(int x=0;x<25;x++)
			{
				if((i+x)<buf.length())
				{
					int c=(int)buf.charAt(i+x);
					int a=c/16;
					int b=c%16;
					System.out.print(("0123456789ABCDEF").charAt(a));
					System.out.print(("0123456789ABCDEF").charAt(b));
				}
			}
			System.out.print(" \n");
		}
		//*/
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
				// don't let them enter telnet codes, except IAC, which is handled...
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
				if(c==255) handleIAC();
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
				case 'z': {      if((mob().location()!=null)&&(mob().isASysOp(mob().location())))
								  buf.append(mob().location().getArea().name());
							  c++; break; }
				case 'R': {   if((mob().location()!=null)&&(mob().isASysOp(mob().location())))
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
				case 'B': { buf.append("\n\r"); c++; break;}
				case 'd': {	  MOB victim=mob().getVictim();
							  if((mob().isInCombat())&&(victim!=null))
								  buf.append(""+mob().rangeToTarget());
							  c++; break; }
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
				MOB newMob=(MOB)CMClass.getMOB("StdMOB");
				newMob.setSession(this);
				mob=newMob;
				status=Session.STATUS_LOGIN;
				if(ExternalPlay.login(newMob))
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
						String input=readlineContinue();
						if(input!=null)
						{
							lastKeystroke=System.currentTimeMillis();
							setAfkFlag(false);
							enque(0,Util.parse(input));
						}
						if(mob==null) break;
						
						if((spamStack>0)&&((lastOutput-System.currentTimeMillis())>100))
							onlyPrint("");
						
						if((!afkFlag())&&(getIdleMillis()>=600000))
							setAfkFlag(true);

						if((((MOB)mob).lastTickedDateTime()>lastStop)
						||(!mob.isInCombat()))
						{
							CMDS=deque();
							if(CMDS!=null)
							{
								waiting=false;
								if(CMDS.size()>0)
								{
									setPreviousCmd(CMDS);
									if(snoops.size()>0)
										for(int s=0;s<snoops.size();s++)
											((Session)snoops.elementAt(s)).rawPrintln(Util.combine(CMDS,0));
									milliTotal+=(lastStop-lastStart);
									tickTotal++;

									lastStart=System.currentTimeMillis();
									ExternalPlay.doCommand(mob,CMDS);
									lastStop=System.currentTimeMillis();
								}
								needPrompt=true;
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
		catch(InterruptedException x)
		{
			Log.sysOut("Session","Interrupted!");
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

		Sessions.removeElement(this);

		status=Session.STATUS_LOGOUT4;
		if(mob!=null)
		{
			ExternalPlay.channel("WIZINFO","",mob.Name()+" has logged out.",true);
			// the player quit message!
			if(mob.location()!=null)
			{
				FullMsg msg=new FullMsg(mob,null,Affect.MSG_QUIT,null);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					if((follower!=null)&&(follower.location()!=mob.location()))
						follower.affect(follower,msg);
				}
				if(mob.location()!=null)
					mob.location().send(mob,msg);
			}
			if(mob.playerStats()!=null)
				mob.playerStats().setLastDateTime(System.currentTimeMillis());
			Log.sysOut("Session","logout: "+mob.Name());
			mob.removeFromGame();
			mob.setSession(null);
			mob=null;
		}
		status=Session.STATUS_LOGOUT5;
		killFlag=true;
		waiting=false;
		needPrompt=false;

		closeSocks();
		//finally
		//{
		//}
		status=Session.STATUS_LOGOUTFINAL;
	}
}