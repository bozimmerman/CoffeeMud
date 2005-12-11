package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class CoffeeFilter
{
	private CoffeeFilter(){};
	private final static String hexStr="0123456789ABCDEF";
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
    private final static int SIRMADAM=10;
    private final static int ISARE2=11;
    private final static int NAMENOART=12;
    
	private static Hashtable tagTable=null;
	
	public static Hashtable getTagTable()
	{
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
            tagTable.put("-SIRMADAM",new Integer(SIRMADAM));
            tagTable.put("IS-ARE",new Integer(ISARE2));
            tagTable.put("-NAMENOART",new Integer(NAMENOART));
		}
		return tagTable;
	}
	
    
	public static String simpleOutFilter(String msg)
	{
		if(msg==null) return null;
		StringBuffer buf=new StringBuffer(msg);
		for(int i=0;i<buf.length();i++)
		{
			switch(buf.charAt(i))
			{
			case '`':
				buf.setCharAt(i,'\'');
				break;
			case '\\':
				if(i<buf.length()-1)
				{
					switch(buf.charAt(i+1))
					{
					case 'n':
					case 'r':
						{
						buf.setCharAt(i,(char)13);
						if((i>=buf.length()-2)||((i<buf.length()-2)&&((buf.charAt(i+2))!=10)))
							buf.setCharAt(i+1,(char)10);
						else
						if(i<buf.length()-2)
							buf.deleteCharAt(i+1);
						}
						break;
					case '\'':
					case '`':
						{
						buf.setCharAt(i,'\'');
						buf.deleteCharAt(i+1);
						}
						break;
					}
				}
				break;
			}
		}
		return buf.toString();
	}
	
    
	// no word-wrapping, text filtering or ('\','n') -> '\n' translations
	// (it's not a member of the interface either so probably shouldn't be public)
	public static String colorOnlyFilter(String msg, Session S)
	{
		if(msg==null) return null;

		if(msg.length()==0) return msg;
		StringBuffer buf=new StringBuffer(msg);

		int loop=0;

		while(buf.length()>loop)
		{
			switch(buf.charAt(loop))
			{
			case '>':
			    if(S.clientTelnetMode(Session.TELNET_MXP))
			    {
					buf.delete(loop,loop+1);
					buf.insert(loop,"&gt;".toCharArray());
					loop+=3;
			    }
			    break;
			case '"':
			    if(S.clientTelnetMode(Session.TELNET_MXP))
			    {
					buf.delete(loop,loop+1);
					buf.insert(loop,"&quot;".toCharArray());
					loop+=5;
			    }
			    break;
			case '&':
			    if(S.clientTelnetMode(Session.TELNET_MXP))
			    {
					if((!buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
					&&(buf.substring(loop,loop+3).equalsIgnoreCase("gt;")))
					{
						buf.delete(loop,loop+1);
						buf.insert(loop,"&amp;".toCharArray());
						loop+=4;
					}
			    }
			    else
				if(loop<buf.length()-3)
				{
					if(buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
						buf.replace(loop,loop+3,"<");
					else
					if(buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))
						buf.replace(loop,loop+3,">");
				}
				break;
				case '<':
				    if(S.clientTelnetMode(Session.TELNET_MXP))
					{
						buf.delete(loop,loop+1);
						buf.insert(loop,"&lt;".toCharArray());
						loop+=3;
					}
				    break;
				case '^':
				{
					if((loop<buf.length()-1)&&(S!=null))
					{
						int colorID = S.getColor( buf.charAt(loop+1) );
						if (colorID != -1)
						{
							String colorEscStr = S.makeEscape(colorID);
							int csl=0;
							if (colorEscStr != null)
							{
								csl = colorEscStr.length();
								if (csl > 0)
									buf.replace(loop,loop+2 ,colorEscStr);
							}
							if (csl == 0)
							{
								// remove the color code
								buf.deleteCharAt(loop);
								buf.deleteCharAt(loop);
								loop-=1;
							}
							else
							if((colorID<('0'))||(colorID>('9')))
							{
							    // begin MXP tags
							    if(buf.charAt(loop)=='<')
							    {
									int tagStart=loop;
							        while(loop<(buf.length()-1))
							        {
							            if((buf.charAt(loop)!='^')||(buf.charAt(loop+1)!='>'))
							            {
								            loop++;
								            if(loop>=(buf.length()-1))
								            {
								                loop=tagStart;
								                break;
								            }
							            }
							            else
							            if(!S.clientTelnetMode(Session.TELNET_MXP))
								        {
							                buf.delete(tagStart,loop+2);
								            loop=tagStart-1;
								            break;
								        }
							            else
							            {
							                loop--;
							                break;
							            }
							        }
							    }
							    else
							    {
									loop+=csl-1;	// already processed 1 char
							    }
							}
							else
							{
								loop--;
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

		if ((S!=null)&&(S.currentColor() != ('N'))&&(S.clientTelnetMode(Session.TELNET_ANSI)))
			buf.append(S.makeEscape('N'));
		return buf.toString();
	}
    
    
    private static String getLastWord(StringBuffer buf, int lastSp, int lastSpace)
    {
        String lastWord="";
        if(lastSp>lastSpace)
        {
            lastWord=Util.removeColors(buf.substring(lastSpace,lastSp)).trim().toUpperCase();
            while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(0))))
                  lastWord=lastWord.substring(1);
            while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(lastWord.length()-1))))
                  lastWord=lastWord.substring(0,lastWord.length()-1);
            for(int i=lastWord.length()-1;i>=0;i--)
                if(!Character.isLetterOrDigit(lastWord.charAt(i)))
                { lastWord=lastWord.substring(i+1); break;}
        }
        else
        {
            for(int i=(lastSpace-1);((i>=0)&&(!Character.isLetterOrDigit(buf.charAt(i))));i--)
                lastWord=buf.charAt(i)+lastWord;
            lastWord=Util.removeColors(lastWord).trim().toUpperCase();
        }
        return lastWord;
    }
    
	public static String fullOutFilter(Session S,
									   MOB mob,
									   Environmental source,
									   Environmental target,
									   Environmental tool,
									   String msg,
									   boolean wrapOnly)
	{
		if(mob==null) return msg;
		if(msg==null) return null;

		if(msg.length()==0) return msg;
		boolean doSagain=false;
        boolean firstSdone=false;
		StringBuffer buf=new StringBuffer(msg);

		int wrap=(S!=null)?S.getWrap():78;
		int len=(wrap>0)?wrap:Integer.MAX_VALUE;
		int loop=0;
		int lastSpace=0;
		int firstAlpha=-1;

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
						if(((loop<buf.length()-1)&&((buf.charAt(loop+1))!=10))
						&&((loop>0)&&((buf.charAt(loop-1))!=10)))
							buf.insert(loop+1,(char)10);
						if(wrap>0) len=loop+wrap;
						lastSpace=loop;
					}
					break;
				case (char)10:
					{
						if(wrap>0) len=loop+wrap;
						lastSpace=loop;
					}
					break;
				case '`':
					buf.setCharAt(loop,'\'');
					break;
				case '!':
					if((loop<buf.length()-10)
					&&(S!=null)
					&&(buf.charAt(loop+1)=='!')
					&&((buf.substring(loop+2,loop+7).equalsIgnoreCase("sound"))
					   ||(buf.substring(loop+2,loop+7).equalsIgnoreCase("music"))))
					{
						int x=buf.indexOf("(",loop+7);
						int y=buf.indexOf(")",loop+7);
						if((x>=0)&&(y>=x))
						{
							if((S.clientTelnetMode(Session.TELNET_MSP))
							&&((source==null)
							   ||(source==mob)
							   ||(Sense.canBeHeardBy(source,mob))))
							{
								loop=y;
								if(wrap>0)
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
				case '>':
				    if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
				    {
						buf.delete(loop,loop+1);
						buf.insert(loop,"&gt;".toCharArray());
						loop+=3;
				    }
				    break;
				case '"':
				    if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
				    {
						buf.delete(loop,loop+1);
						buf.insert(loop,"&quot;".toCharArray());
						loop+=5;
				    }
				    break;
				case '&':
				    if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
				    {
						if((!buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
						&&(!buf.substring(loop,loop+3).equalsIgnoreCase("gt;")))
						{
							buf.delete(loop,loop+1);
							buf.insert(loop,"&amp;".toCharArray());
							loop+=4;
						}
						else
						    loop+=3;
				    }
				    else
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
							if((buf.charAt(loop))==13)
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
                            String lastWord=getLastWord(buf,lastSp,lastSpace);
							int lastParen=(c2=='S')?loop+2:loop+3;
							if(lastWord.equals("A")
                            ||lastWord.equals("YOU")
                            ||lastWord.equals("1")
                            ||doSagain)
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
                            firstSdone=true;
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
							if((loop>=buf.length()-2)||((loop<buf.length()-2)&&((buf.charAt(loop+2))!=10)))
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
					if((!wrapOnly)&&((loop+1)<buf.length()))
					{
						// supported here <?-HIS-HER>, <?-HIM-HER>, <?-NAME>,
						// <?-NAMESELF>, <?-HE-SHE>, <?-IS-ARE>, <?-HAS-HAVE>
						//int endDex=loop;
						StringBuffer cmd=new StringBuffer("");
						int ldex=loop+1;
						char lc=' ';
						for(;(ldex<buf.length())&&(cmd!=null);ldex++)
						{
						    lc=buf.charAt(ldex);
						    if(lc=='>') 
						        break;
						    switch(lc)
						    {
						    	case '<':
						    	case '\n':
						    	case '\r':
						    	    cmd=null;
						    	    break;
						    	default:
									cmd.append(Character.toUpperCase(lc));
							    	break;
						    }
						}
						if((cmd!=null)&&(ldex<buf.length())&&(buf.charAt(ldex)=='>')&&(cmd.length()>1)&&(cmd.length()<14))
						{
							Environmental regarding=null;
							switch(cmd.charAt(0))
							{
							case 'S': regarding=source; break;
							case 'T': regarding=target; break;
							case 'O': regarding=tool; break;
							}
							String replacement=null;
							Integer I=(Integer)getTagTable().get(cmd.substring(1));
							if(I==null)
							{
							    if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
							    {
									buf.delete(loop,loop+1);
									buf.insert(loop,"&lt;".toCharArray());
							    }
							}
							else
							switch(I.intValue())
							{
							case NAME:
								{
									if(regarding==null)
										replacement="";
									else
									if(mob==regarding)
                                    {
										replacement="you";
                                        if(!firstSdone) doSagain=true;
                                    }
									else
									if(((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))&&(regarding.Name().trim().length()>0))
										replacement=((regarding instanceof MOB)?"someone":"something");
									else
										replacement=regarding.name();
								}
								break;
                            case NAMENOART:
                                {
                                    if(regarding==null)
                                        replacement="";
                                    else
                                    if(mob==regarding)
                                    {
                                        replacement="you";
                                        if(!firstSdone) doSagain=true;
                                    }
                                    else
                                    if(((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))&&(regarding.Name().trim().length()>0))
                                        replacement=((regarding instanceof MOB)?"someone":"something");
                                    else
                                        replacement=CMLib.english().cleanArticles(regarding.name());
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
                                    {
                                        replacement="you";
                                        if(!firstSdone) doSagain=true;
                                    }
									else
									if(((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))&&(regarding.Name().trim().length()>0))
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
									if(((!Sense.canSee(mob))||(!Sense.canBeSeenBy(regarding,mob)))&&(regarding.Name().trim().length()>0))
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
                                    {
                                        replacement="you";
                                        if(!firstSdone) doSagain=true;
                                    }
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
                                    {
                                        replacement="you";
                                        if(!firstSdone) doSagain=true;
                                    }
									else
									if(regarding instanceof MOB)
										replacement=((MOB)regarding).charStats().heshe();
									else
										replacement="its";
								}
								break;
                            case SIRMADAM:
                                {
                                    if(regarding==null)
                                        replacement="";
                                    else
                                    if(regarding instanceof MOB)
                                        replacement=((MOB)regarding).charStats().sirmadam();
                                    else
                                        replacement="sir";
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
                            case ISARE2:
                                {
                                    String lastWord=getLastWord(buf,lastSp,lastSpace);
                                    if((lastWord.equals("A")||lastWord.equals("YOU")||lastWord.equals("1")||doSagain))
                                        replacement="is";
                                    else
                                        replacement="are";
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
						else
					    if((S!=null)&&(S.clientTelnetMode(Session.TELNET_MXP)))
						{
							buf.delete(loop,loop+1);
							buf.insert(loop,"&lt;".toCharArray());
							loop+=3;
						}
					}
					break;
					case '^':
					{
						if((loop<buf.length()-1)&&(S!=null))
						{
							int colorID = S.getColor( buf.charAt(loop+1) );
							if (colorID != -1)
							{
								String colorEscStr = S.makeEscape(colorID);
								int csl=0;
								if (colorEscStr != null)
								{
									csl = colorEscStr.length();
									if (csl > 0)
										buf.replace(loop,loop+2 ,colorEscStr);
								}
								if (csl == 0)
								{
									// remove the color code
									buf.deleteCharAt(loop);
									buf.deleteCharAt(loop);
									loop-=1;
								}
								else
								if((colorID<('0'))||(colorID>('9')))
								{
								    // begin MXP tags
								    if(buf.charAt(loop)=='<')
								    {
										int tagStart=loop;
								        while(loop<(buf.length()-1))
								        {
								            if((buf.charAt(loop)!='^')||(buf.charAt(loop+1)!='>'))
								            {
									            loop++;
									            if(loop>=(buf.length()-1))
									            {
									                loop=tagStart;
									                break;
									            }
								            }
								            else
								            if((S==null)||(!S.clientTelnetMode(Session.TELNET_MXP)))
									        {
								                buf.delete(tagStart,loop+2);
									            loop=tagStart-1;
									            break;
									        }
								            else
								            {
								                if(wrap>0)len+=(loop-tagStart);
								                loop--;
								                break;
								            }
								        }
								    }
								    else
								    {
										loop+=csl-1;	// already processed 1 char
										if(wrap>0)len+=csl;		// does not count for any length
								    }
								}
								else
								{
									loop--;
									if(wrap>0)len+=csl;		// does not count for any length
								}
							}
						}
						break;
					}
				default:
					if((firstAlpha < 0)&&(Character.isLetter(buf.charAt(loop))))
						firstAlpha = loop;
					break;
				}
				loop++;
			}


			if((len<buf.length())
			&&(loop!=lastSp)
			&&(lastSp>=0)
			&&(loop>=0)
			&&(loop<buf.length())
			&&(buf.charAt(loop)!=13)
			&&(buf.charAt(loop)!=10))
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
			if(wrap>0)len=loop+wrap;
		}

		if(firstAlpha<0) firstAlpha=0;
		if(firstAlpha<buf.length())
			buf.setCharAt(firstAlpha,Character.toUpperCase(buf.charAt(firstAlpha)));
		if ((S!=null)&&(S.currentColor() != ('N'))&&(S.clientTelnetMode(Session.TELNET_ANSI)))
			buf.append(S.makeEscape('N'));

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


	public static StringBuffer simpleInFilter(StringBuffer input, boolean allowMXP)
	{
		if(input==null) return null;

		int x=0;
		while(x<input.length())
		{
			char c=input.charAt(x);
			if(c=='\'')
				input.setCharAt(x,'`');
			else
			if((c=='^')&&(x<(input.length()-1))&&(!allowMXP))
			{
			    switch(input.charAt(x+1))
			    {
			    case '<':
			    case '>':
			    case '&':
			        input.deleteCharAt(x);
			        break;
			    }
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
		return new StringBuffer(input.toString());
	}

	public static String fullInFilter(String input, boolean allowMXP)
	{
		if(input==null) return null;
		StringBuffer buf=new StringBuffer(input);
		for(int i=0;i<buf.length();i++)
		{
			switch(buf.charAt(i))
			{
			case (char)10:
				buf.setCharAt(i,'r');
				buf.insert(i,'\\');
				break;
			case (char)13:
				buf.setCharAt(i,'n');
				buf.insert(i,'\\');
				break;
			}
		}
		return simpleInFilter(buf,allowMXP).toString();
	}
	
	public static String safetyFilter(String s)
	{
		StringBuffer s1=new StringBuffer(s);
		
		int x=-1;
		while((++x)<s1.length())
		{
			if(s1.charAt(x)=='\r')
			{
				s1.deleteCharAt(x);
				x--;
			}
			else
			if(s1.charAt(x)=='\n')
			{
				s1.setCharAt(x,'\\');
				s1.insert(x+1,'n');
				x++;
			}
			else
			if(s1.charAt(x)=='\'')
				s1.setCharAt(x,'`');
		}
		return s1.toString();
	}
}
