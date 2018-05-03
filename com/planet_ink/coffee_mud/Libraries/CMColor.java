package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
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
public class CMColor extends StdLibrary implements ColorLibrary
{
	@Override
	public String ID()
	{
		return "CMColor";
	}

	public String[] clookup=null;
	public String[] htlookup=null;

	private final static Map<Integer,ColorState> cache=new SHashtable<Integer,ColorState>();
	
	private final Map<String, SpecialColor> nameMap = new Hashtable<String, SpecialColor>();

	private static class ColorStateImpl implements ColorState
	{
		public final char foregroundCode;
		public final char backgroundCode;

		@Override
		public char foregroundCode()
		{
			return foregroundCode;
		}
		
		@Override
		public char backgroundCode()
		{
			return backgroundCode;
		}

		public ColorStateImpl(final char fg, final char bg)
		{
			foregroundCode=fg;
			backgroundCode=bg;
		}

		@Override
		public boolean equals(Object cs)
		{
			if(!(cs instanceof ColorState))
				return false;
			return (((ColorState)cs).foregroundCode() == foregroundCode)
				&& (((ColorState)cs).backgroundCode() == backgroundCode);
		}

		@Override
		public int hashCode()
		{
			return (backgroundCode * 65536) + foregroundCode;
		}

	}
	
	private static final ColorState COLORSTATE_NORMAL=new ColorStateImpl('N','.');
	
	@Override
	public final ColorState getNormalColor()
	{
		return COLORSTATE_NORMAL;
	}

	@Override
	public final ColorState valueOf(final char fg, final char bg)
	{
		final Integer keyI=Integer.valueOf((bg * 65536) + fg);
		if(cache.containsKey(keyI))
			return cache.get(keyI);
		final ColorState newColorState = new ColorStateImpl(fg,bg);
		cache.put(keyI,newColorState);
		return newColorState;
	}
	
	protected int translateSingleCMCodeToANSIOffSet(String code)
	{
		if(code.length()==0)
			return -1;
		if(!code.startsWith("^"))
			return -1;
		int i=code.length()-1;
		while(i>=0)
		{
			if(Character.isLetter(code.charAt(i)))
				return "krgybpcw".indexOf(Character.toLowerCase(code.charAt(i)));
			else
				i++;
		}
		return 3;
	}

	public String translateCMCodeToFGNumber(String code)
	{
		if(code.length()==0)
			return code;
		if(!code.startsWith("^"))
			return code;
		final int background=code.indexOf('|');
		if(background>0)
			code=code.substring(0,background);
		int bold=0;
		for(int i=0;i<code.length();i++)
		{
			if(Character.isLowerCase(code.charAt(i)))
				bold=1;
		}
		return bold+";"+(30+translateSingleCMCodeToANSIOffSet(code))+"m";
	}

	@Override
	public String translateCMCodeToANSI(String code)
	{
		if(code.length()==0)
			return code;
		if(!code.startsWith("^"))
			return code;
		final int background=code.indexOf('|');
		int bold=0;
		for(int i=0;i<code.length();i++)
		{
			if(Character.isLowerCase(code.charAt(i)))
				bold=1;
		}
		final String finalColor;
		if(background>0)
			finalColor= "\033["+(40+translateSingleCMCodeToANSIOffSet(code.substring(0,background)))+";"+bold+";"+(30+translateSingleCMCodeToANSIOffSet(code.substring(background+1)))+"m";
		else
			finalColor = "\033["+bold+";"+(30+translateSingleCMCodeToANSIOffSet(code))+"m";
		return finalColor;
	}

	@Override
	public String translateANSItoCMCode(String code)
	{
		if(code.length()==0)
			return code;
		if(code.indexOf('^')==0)
			return code;
		if(code.indexOf('|')>0)
			return code;
		String code1=null;
		String code2=null;
		final boolean bold=(code.indexOf(";1;")>0)||(code.indexOf("[1;")>0);
		for(int i=0;i<COLORS_INCARDINALORDER.length;i++)
		{
			if((code1==null)&&(code.indexOf(""+(40+i))>0))
				code1="^"+Character.toUpperCase(COLORS_INCARDINALORDER[i].getCodeChar());
			if((code2==null)&&(code.indexOf(""+(30+i))>0))
				code2="^"+(bold?COLORS_INCARDINALORDER[i].getCodeChar():Character.toUpperCase(COLORS_INCARDINALORDER[i].getCodeChar()));
		}
		if((code1!=null)&&(code2!=null))
			return code1+"|"+code2;
		else
		if((code1==null)&&(code2!=null))
			return code2;
		else
		if((code1!=null)&&(code2==null))
			return code1;
		else
			return "^W";
	}

	@Override
	public String mixHTMLCodes(String code1, String code2)
	{
		String html=null;
		if((code1==null)||(code1.length()==0))
			html=code2;
		else
		if((code2==null)||(code2.length()==0))
			html=code1;
		else
		if(code1.startsWith(" ")&&(code2.startsWith("<FONT")))
			html=code2+code1;
		else
		if(code2.startsWith(" ")&&(code1.startsWith("<FONT")))
			html=code1+code2;
		else
		if(code1.startsWith("<")&&(code2.startsWith("<")))
			html=code1+">"+code2;
		else
		if(!code1.startsWith("<"))
			html=code2;
		else
			html=code1;
		if(html.startsWith(" "))
			return "<FONT"+html;
		return html;
	}

	@Override
	public String mixColorCodes(String code1, String code2)
	{
		if((code1==null)||(code1.length()==0))
			return code2;
		if((code2==null)||(code2.length()==0))
			return code1;
		if(code1.charAt(code1.length()-1)!=code2.charAt(code2.length()-1))
			return code1+code2;
		if(code2.startsWith("\033["))
			code2=code2.substring("\033[".length());
		return code1.substring(0,code1.length()-1)+";"+code2;
	}

	@Override
	public CMMsg fixSourceFightColor(CMMsg msg)
	{
		if(msg.sourceMessage()!=null)
			msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(),"^F","^f"));
		if(msg.targetMessage()!=null)
			msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(),"^F","^e"));
		return msg;
	}

	protected final SpecialColor findCodeColor(String name)
	{
		if(this.nameMap.size()==0)
		{
			for(SpecialColor code : SpecialColor.values())
			{
				nameMap.put(code.getCodeString(), code);
			}
		}
		if(this.nameMap.containsKey(name))
			return this.nameMap.get(name);
		return (SpecialColor)CMath.s_valueOf(SpecialColor.class, name);
	}
	
	protected final char findCodeChar(String name)
	{
		final SpecialColor code = findCodeColor(name);
		if(code != null)
			return code.getCodeChar();
		return ' ';
	}
	
	@Override
	public String[] standardHTMLlookups()
	{
		if(htlookup==null)
		{
			htlookup=new String[256];

			htlookup['!']=Color.BOLD.getHtmlTag();		// bold
			htlookup['_']=Color.UNDERLINE.getHtmlTag(); // underline
			htlookup['*']=Color.BLINK.getHtmlTag();		// blink
			htlookup['/']=Color.ITALICS.getHtmlTag();	// italics
			htlookup['.']=Color.NONE.getHtmlTag();   	// reset
			htlookup['^']="^";  						// ansi escape
			htlookup['<']="<";  						// mxp escape
			htlookup['"']="\""; 						// mxp escape
			htlookup['>']=">";  						// mxp escape
			htlookup['&']="&";  						// mxp escape
			for(Color C : Color.values())
			{
				if(C.isBasicColor())
					htlookup[C.getCodeChar()] = C.getHtmlTag();
			}

			// default color settings:
			htlookup[SpecialColor.HIGHLIGHT.getCodeChar()]=Color.LIGHTCYAN.getHtmlTag();
			htlookup[SpecialColor.YOU_FIGHT.getCodeChar()]=Color.LIGHTPURPLE.getHtmlTag();
			htlookup[SpecialColor.FIGHT_YOU.getCodeChar()]=Color.LIGHTRED.getHtmlTag();
			htlookup[SpecialColor.FIGHT.getCodeChar()]=Color.RED.getHtmlTag();
			htlookup[SpecialColor.SPELL.getCodeChar()]=Color.YELLOW.getHtmlTag();
			htlookup[SpecialColor.EMOTE.getCodeChar()]=Color.LIGHTPURPLE.getHtmlTag();
			htlookup[SpecialColor.WEATHER.getCodeChar()]=Color.WHITE.getHtmlTag();
			htlookup[SpecialColor.TALK.getCodeChar()]=Color.LIGHTBLUE.getHtmlTag();
			htlookup[SpecialColor.TELL.getCodeChar()]=Color.CYAN.getHtmlTag();
			htlookup[SpecialColor.CHANNEL.getCodeChar()]=mixHTMLCodes(Color.LIGHTCYAN.getHtmlTag(),Color.BGBLUE.getHtmlTag());
			htlookup[SpecialColor.CHANNELFORE.getCodeChar()]=Color.LIGHTCYAN.getHtmlTag();
			htlookup[SpecialColor.IMPORTANT1.getCodeChar()]=mixHTMLCodes(Color.LIGHTCYAN.getHtmlTag(),Color.BGBLUE.getHtmlTag());
			htlookup[SpecialColor.IMPORTANT2.getCodeChar()]=mixHTMLCodes(Color.YELLOW.getHtmlTag(),Color.BGBLUE.getHtmlTag());
			htlookup[SpecialColor.IMPORTANT3.getCodeChar()]=mixHTMLCodes(Color.YELLOW.getHtmlTag(),Color.BGRED.getHtmlTag());
			htlookup[SpecialColor.ROOMTITLE.getCodeChar()]=Color.LIGHTCYAN.getHtmlTag();
			htlookup[SpecialColor.ROOMDESC.getCodeChar()]=Color.WHITE.getHtmlTag();
			htlookup[SpecialColor.DIRECTION.getCodeChar()]=mixHTMLCodes(Color.LIGHTCYAN.getHtmlTag(),Color.BGBLUE.getHtmlTag());
			htlookup[SpecialColor.DOORDESC.getCodeChar()]=Color.LIGHTBLUE.getHtmlTag();
			htlookup[SpecialColor.ITEM.getCodeChar()]=Color.LIGHTGREEN.getHtmlTag();
			htlookup[SpecialColor.MOB.getCodeChar()]=Color.LIGHTPURPLE.getHtmlTag();
			htlookup[SpecialColor.HITPOINTS.getCodeChar()]=Color.LIGHTCYAN.getHtmlTag();
			htlookup[SpecialColor.MANA.getCodeChar()]=Color.LIGHTCYAN.getHtmlTag();
			htlookup[SpecialColor.MOVES.getCodeChar()]=Color.LIGHTCYAN.getHtmlTag();
			htlookup[SpecialColor.UNEXPDIRECTION.getCodeChar()]=mixHTMLCodes(Color.CYAN.getHtmlTag(),Color.BGBLUE.getHtmlTag());
			htlookup[SpecialColor.UNEXPDOORDESC.getCodeChar()]=Color.LIGHTBLUE.getHtmlTag();
			final List<String> schemeSettings=CMParms.parseCommas(CMProps.getVar(CMProps.Str.COLORSCHEME),true);
			for(int i=0;i<schemeSettings.size();i++)
			{
				final String s=schemeSettings.get(i);
				int x=s.indexOf('=');
				if(x>0)
				{
					final String key=s.substring(0,x).trim();
					String value=s.substring(x+1).trim();
					char codeChar=this.findCodeChar(key.toUpperCase());
					if(codeChar!=' ')
					{
						String newVal=null;
						String addColor=null;
						String addCode=null;
						while(value.length()>0)
						{
							x=value.indexOf('+');
							if(x<0)
							{
								addColor=value;
								value="";
							}
							else
							{
								addColor=value.substring(0,x).trim();
								value=value.substring(x+1).trim();
							}
							addCode=null;
							for(Color C : Color.values())
							{
								if(C.name().equalsIgnoreCase(addColor))
								{
									addCode = C.getHtmlTag();
									break;
								}
							}
							if(addCode!=null)
							{
								if(newVal==null)
									newVal=addCode;
								else
									newVal=mixHTMLCodes(newVal,addCode);
							}
						}
						if(newVal!=null)
							htlookup[codeChar]=newVal;
					}
				}
			}

			for(int i=0;i<htlookup.length;i++)
			{
				final String s=htlookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					htlookup[i]=htlookup[s.charAt(1)];
			}
			htlookup[SpecialColor.NORMAL.getCodeChar()]=Color.NONE.getHtmlTag();
		}
		return htlookup;
	}

	@Override
	public void clearLookups()
	{
		clookup = null;
	}

	@Override
	public String[] standardColorLookups()
	{
		if(clookup==null)
		{
			clookup=new String[256];
			clookup['!']=Color.BOLD.getANSICode();		// bold
			clookup['_']=Color.UNDERLINE.getANSICode(); // underline
			clookup['*']=Color.BLINK.getANSICode();   	// blink
			clookup['/']=Color.ITALICS.getANSICode(); 	// italics
			clookup['.']=Color.NONE.getANSICode();		// reset
			clookup['^']="^";   						// ansi escape
			clookup['<']="<";   						// mxp escape
			clookup['"']="\"";  						// mxp escape
			clookup['>']=">";   						// mxp escape
			clookup['&']="&";   						// mxp escape
			clookup[COLORCODE_BACKGROUND]=null;			  // ** special background color code
			clookup[COLORCODE_FANSI256]=null;  			  // ** special foreground 256 color code
			clookup[COLORCODE_BANSI256]=null;  			  // ** special background 256 color code
			
			for(Color C : Color.values())
			{
				if(C.isBasicColor())
					clookup[C.getCodeChar()]=C.getANSICode();
			}

			// default color settings:
			clookup[SpecialColor.NORMAL.getCodeChar()] = Color.GREY.getANSICode();
			clookup[SpecialColor.HIGHLIGHT.getCodeChar()] = Color.LIGHTCYAN.getANSICode();
			clookup[SpecialColor.YOU_FIGHT.getCodeChar()] = Color.LIGHTPURPLE.getANSICode();
			clookup[SpecialColor.FIGHT_YOU.getCodeChar()] = Color.LIGHTRED.getANSICode();
			clookup[SpecialColor.FIGHT.getCodeChar()] = Color.RED.getANSICode();
			clookup[SpecialColor.SPELL.getCodeChar()] = Color.YELLOW.getANSICode();
			clookup[SpecialColor.EMOTE.getCodeChar()] = Color.LIGHTPURPLE.getANSICode();
			clookup[SpecialColor.WEATHER.getCodeChar()] = Color.WHITE.getANSICode();
			clookup[SpecialColor.TALK.getCodeChar()] = Color.LIGHTBLUE.getANSICode();
			clookup[SpecialColor.TELL.getCodeChar()] = Color.CYAN.getANSICode();
			clookup[SpecialColor.CHANNEL.getCodeChar()] = mixColorCodes(Color.LIGHTCYAN.getANSICode(), Color.BGBLUE.getANSICode());
			clookup[SpecialColor.CHANNELFORE.getCodeChar()] = Color.LIGHTCYAN.getANSICode();
			clookup[SpecialColor.IMPORTANT1.getCodeChar()] = mixColorCodes(Color.LIGHTCYAN.getANSICode(), Color.BGBLUE.getANSICode());
			clookup[SpecialColor.IMPORTANT2.getCodeChar()] = mixColorCodes(Color.YELLOW.getANSICode(), Color.BGBLUE.getANSICode());
			clookup[SpecialColor.IMPORTANT3.getCodeChar()] = mixColorCodes(Color.YELLOW.getANSICode(), Color.BGRED.getANSICode());
			clookup[SpecialColor.ROOMTITLE.getCodeChar()] = Color.LIGHTCYAN.getANSICode();
			clookup[SpecialColor.ROOMDESC.getCodeChar()] = Color.WHITE.getANSICode();
			clookup[SpecialColor.DIRECTION.getCodeChar()] = mixColorCodes(Color.LIGHTCYAN.getANSICode(), Color.BGBLUE.getANSICode());
			clookup[SpecialColor.DOORDESC.getCodeChar()] = Color.LIGHTBLUE.getANSICode();
			clookup[SpecialColor.ITEM.getCodeChar()] = Color.LIGHTGREEN.getANSICode();
			clookup[SpecialColor.MOB.getCodeChar()] = Color.LIGHTPURPLE.getANSICode();
			clookup[SpecialColor.HITPOINTS.getCodeChar()] = Color.LIGHTCYAN.getANSICode();
			clookup[SpecialColor.MANA.getCodeChar()] = Color.LIGHTCYAN.getANSICode();
			clookup[SpecialColor.MOVES.getCodeChar()] = Color.LIGHTCYAN.getANSICode();
			clookup[SpecialColor.UNEXPDIRECTION.getCodeChar()] = mixColorCodes(Color.CYAN.getANSICode(), Color.BGBLUE.getANSICode());
			clookup[SpecialColor.UNEXPDOORDESC.getCodeChar()] = Color.LIGHTBLUE.getANSICode();
			final List<String> schemeSettings=CMParms.parseCommas(CMProps.getVar(CMProps.Str.COLORSCHEME),true);
			for(int i=0;i<schemeSettings.size();i++)
			{
				final String s=schemeSettings.get(i);
				int x=s.indexOf('=');
				if(x>0)
				{
					final String key=s.substring(0,x).trim();
					String value=s.substring(x+1).trim();
					char codeChar=this.findCodeChar(key.toUpperCase());
					if(codeChar!=' ')
					{
						String newVal=null;
						String addColor=null;
						String addCode=null;
						while(value.length()>0)
						{
							x=value.indexOf('+');
							if(x<0)
							{
								addColor=value;
								value="";
							}
							else
							{
								addColor=value.substring(0,x).trim();
								value=value.substring(x+1).trim();
							}
							if(addColor!=null)
							{
								addCode=null;
								for(Color C : Color.values())
								{
									if(C.name().equalsIgnoreCase(addColor))
									{
										addCode = C.getANSICode();
										break;
									}
								}
								if(addCode!=null)
								{
									if(newVal==null)
										newVal=addCode;
									else
										newVal=mixColorCodes(newVal,addCode);
								}
							}
						}
						if(newVal!=null)
							clookup[codeChar]=newVal;
					}
				}
			}

			for(int i=0;i<clookup.length;i++)
			{
				final String s=clookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					clookup[i]=clookup[s.charAt(1)];
			}
		}
		return clookup;
	}
}
