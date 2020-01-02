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
   Copyright 2005-2020 Bo Zimmerman

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

	protected String[]		clookup		= null;
	protected String[]		htlookup	= null;
	protected Color256[]	color256s	= null;

	protected final Map<Short, Color> color256to16map =  new HashMap<Short, Color>();

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
		public boolean equals(final Object cs)
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

	/**
	 * Special mapping object for 256 color ansi system.
	 *
	 * @author BZ
	 */
	private static class Color256Impl implements Color256
	{
		private final short number;
		private final String name1;
		private final String name2;
		private Color non256color;
		private final String htmlCode;
		private final short expertiseNum;
		private final short cm6Code;
		private final String cmChars;

		public Color256Impl(final short number,  final String name1, final String name2, final Color non256color,
							final String htmlCode, final short expertiseNum, final short cm6Code, final String cmChars)
		{
			this.number=number;
			this.name1=name1;
			this.name2=name2;
			this.non256color=non256color;
			this.htmlCode=htmlCode;
			this.expertiseNum=expertiseNum;
			this.cm6Code=cm6Code;
			this.cmChars=cmChars;
		}

		/**
		 * @param non256color the non256color to set
		 */
		@Override
		public void setNon256color(final Color non256color)
		{
			this.non256color = non256color;
		}

		/**
		 * @return the number
		 */
		@Override
		public short getNumber()
		{
			return number;
		}

		/**
		 * @return the name1
		 */
		@Override
		public String getName1()
		{
			return name1;
		}

		/**
		 * @return the name2
		 */
		@Override
		public String getName2()
		{
			return name2;
		}

		/**
		 * @return the non256color
		 */
		@Override
		public Color getNon256color()
		{
			return non256color;
		}

		/**
		 * @return the htmlCode
		 */
		@Override
		public String getHtmlCode()
		{
			return htmlCode;
		}

		/**
		 * @return the expertiseNum
		 */
		@Override
		public short getExpertiseNum()
		{
			return expertiseNum;
		}

		/**
		 * @return the cm6Code
		 */
		@Override
		public short getCm6Code()
		{
			return cm6Code;
		}

		/**
		 * @return the cmChars
		 */
		@Override
		public String getCmChars()
		{
			return cmChars;
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

	protected int translateSingleCMCodeToANSIOffSet(final String code)
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
	public String[] fixPlayerColorDefs(final String colorDefs)
	{
		final String[] clookup=CMLib.color().standardColorLookups().clone();
		final List<String> changesList = CMParms.parseAny(colorDefs, '#', true);
		for(final String change : changesList)
		{
			int subChar;
			String subColor;
			if(change.startsWith("(") && (change.indexOf(')')>0))
			{
				final int x=change.indexOf(')');
				subChar=CMath.s_int(change.substring(1,x));
				subColor = change.substring(x+1);
			}
			else
			{
				subChar=change.charAt(0);
				subColor=change.substring(1);
			}
			if((subColor.length()>4)
			&&(subColor.charAt(2)=='|')
			&&(subColor.charAt(3)=='^'))
				subColor=subColor.substring(3)+"^~"+subColor.substring(1, 2);
			clookup[subChar]=subColor;
		}
		for(int i=0;i<clookup.length;i++)
		{
			final String s=clookup[i];
			if((s!=null)
			&&(s.startsWith("^"))
			&&(s.length()==2))
				clookup[i]=clookup[s.charAt(1)];
		}
		return clookup;
	}

	@Override
	public String translateCMCodeToANSI(final String code)
	{
		if((code==null)||(code.length()==0))
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
	public String translateANSItoCMCode(final String code)
	{
		if(code == null)
			return "";
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
	public String mixHTMLCodes(final String code1, final String code2)
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
	public String mixColorCodes(final String code1, String code2)
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
	public CMMsg fixSourceFightColor(final CMMsg msg)
	{
		if(msg.sourceMessage()!=null)
			msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(),"^F","^f"));
		if(msg.targetMessage()!=null)
			msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(),"^F","^e"));
		return msg;
	}

	protected final SpecialColor findCodeColor(final String name)
	{
		if(this.nameMap.size()==0)
		{
			for(final SpecialColor code : SpecialColor.values())
			{
				nameMap.put(code.getCodeString(), code);
			}
		}
		if(this.nameMap.containsKey(name))
			return this.nameMap.get(name);
		return (SpecialColor)CMath.s_valueOf(SpecialColor.class, name);
	}

	protected final char findCodeChar(final String name)
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
			for(final Color C : Color.values())
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
					final char codeChar=this.findCodeChar(key.toUpperCase());
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
							for(final Color C : Color.values())
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
		Resources.removeResource("SYSTEM_COLOR_INFO: "+true);
		Resources.removeResource("SYSTEM_COLOR_INFO: "+false);
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

			for(final Color C : Color.values())
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
					final char codeChar=this.findCodeChar(key.toUpperCase());
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
								for(final Color C : Color.values())
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

	@Override
	public String getColorInfo(final boolean doAll256)
	{
		StringBuffer buf = (StringBuffer)Resources.getResource("SYSTEM_COLOR_INFO: "+doAll256);
		if(buf == null)
		{
			buf = new StringBuffer("");
			int longestName = 0;
			int secondLongestName = 0;
			for(final Color256 C : color256s)
			{
				if(C!=null)
				{
					if(C.getName1().length()>longestName)
						longestName=C.getName1().length();
					if(C.getName2().length()>secondLongestName)
						secondLongestName=C.getName2().length();
				}
			}
			final int colSize = (longestName + secondLongestName + 2 + 6);
			final int max_cols = 78 / colSize;
			int col=1;
			for(final Color256 C : color256s)
			{
				if((C==null)
				||((!doAll256) && (C.getCm6Code() >=0))
				||((C.getCm6Code()<0)&&(C.getNon256color()==Color.BLACK)))
					continue;
				buf.append("^N").append(CMStrings.padRight("^"+C.getCmChars(), 6))
					.append(C.getCmChars())
					.append(CMStrings.padRight(C.getName1(), longestName))
					.append("^N: ").append(C.getCmChars())
					.append(CMStrings.padRight(C.getName2(), secondLongestName))
					.append("^N");
				if((++col) >= max_cols)
				{
					col=1;
					buf.append("\n\r");
				}
			}
			Resources.submitResource("SYSTEM_COLOR_INFO: "+doAll256, buf);
		}
		return buf.toString();
	}

	protected void generateRecipes()
	{
		final StringBuilder str=new StringBuilder("");
		final Set<String> namesUsed = new HashSet<String>();
		final List<Color256> allColors = new XVector<Color256>(color256s);
		Collections.sort(allColors, new Comparator<Color256>()
		{
			@Override
			public int compare(final Color256 o1, final Color256 o2)
			{
				int level1 = 1;
				if(o1.getExpertiseNum() > 0)
				{
					final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition("TUNING"+o1.getExpertiseNum());
					if(def.getMinimumLevel()>0)
						level1=def.getMinimumLevel();
				}
				int level2 = 1;
				if(o2.getExpertiseNum() > 0)
				{
					final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition("TUNING"+o2.getExpertiseNum());
					if(def.getMinimumLevel()>0)
						level2=def.getMinimumLevel();
				}
				if(level1==level2)
					return 0;
				if(level1>level2)
					return 1;
				return -1;
			}

		});
		for(final Color256 c : allColors)
		{
			if((c.getName1().indexOf("black")>=0)
			||(c.getName2().indexOf("black")>=0))
				continue;
			int level = 1;
			if(c.getExpertiseNum() > 0)
			{
				final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition("TUNING"+c.getExpertiseNum());
				if(def.getMinimumLevel()>0)
					level=def.getMinimumLevel();
			}
			if(namesUsed.contains(c.getName1()))
			{
				System.out.println("Re-used: "+c.getName1());
				continue;
			}
			final String misc = (c.getCmChars().indexOf('#')>0)?"ANSI256=TRUE":"";
			str.append(c.getName1()).append("\t")
			   .append(level).append("\t")
			   .append(9+level).append("\t")
			   .append(c.getCmChars()).append(c.getName1()).append("^?\t")
			   .append("").append("\t") // application mask
			   .append(c.getExpertiseNum()).append("\t")  // expertise
			   .append(misc).append("\n\r");
			namesUsed.add(c.getName1());
			if(namesUsed.contains(c.getName2()))
				continue;
			if(!c.getName1().equals(c.getName2()))
			{
				str.append(c.getName2()).append("\t")
				   .append(level+10).append("\t")
				   .append(10+level).append("\t")
				   .append(c.getCmChars()).append(c.getName2()).append("^?\t")
				   .append("").append("\t") // application mask
				   .append(c.getExpertiseNum()).append("\t")  // expertise
				   .append(misc).append("\n\r");
			}
		}
		final CMFile F1=new CMFile("///resources/skills/dyeing.txt",null);
		F1.saveText(str.toString());
		final CMFile F2=new CMFile("///resources/skills/lacquering.txt",null);
		F2.saveText(str.toString());
	}

	@Override
	public boolean activate()
	{
		final List<Color256> list=new ArrayList<Color256>();
		final CMFile F=new CMFile(Resources.buildResourcePath("skills/colors.txt"),null);
		if(F.exists())
		{
			color256to16map.clear();
			final List<String> lines=Resources.getFileLineVector(F.text());
			final Map<Short,Short> color16map=new TreeMap<Short,Short>();
			final Map<Short,Color256> straightMap=new TreeMap<Short,Color256>();
			for(final String line : lines)
			{
				if(line.trim().startsWith("#"))
					continue;
				final String[] bits = line.split("\t");
				if(bits.length>=8)
				{
					Color baseColor = null;
					short cm6code = -1;
					if(bits[7].length()>0)
					{
						if(CMath.isNumber(bits[7].substring(0, 1)))
						{
							int num=bits[7].charAt(0)-'0';
							if((num>=0)&&(num<=5))
							{
								int buildNum=(num*36);
								num=bits[7].charAt(1)-'0';
								if((num>=0)&&(num<=5))
								{
									buildNum+=(num*6);
									num=bits[7].charAt(2)-'0';
									if((num>=0)&&(num<=5))
										cm6code=(short)((buildNum + num) + 16);
								}
							}
						}
						else
							baseColor = (ColorLibrary.Color)CMath.s_valueOf(ColorLibrary.Color.class, bits[7].toUpperCase().trim());
					}
					String cmChars;
					if(cm6code < 0)
					{
						if(baseColor != null)
							cmChars = "^"+baseColor.getCodeChar();
						else
						{
							Log.errOut("Error in skills/colors.txt: "+line);
							continue;
						}
					}
					else
						cmChars = "^#"+bits[7];
					final Color256Impl newColor = new Color256Impl(
						CMath.s_short(bits[0]),  bits[1], bits[2], baseColor,
						bits[5], CMath.s_short(bits[6]), cm6code, cmChars
					);
					if(bits[4].length()>0)
						color16map.put(Short.valueOf(newColor.number), Short.valueOf(CMath.s_short(bits[4])));
					list.add(newColor);
					straightMap.put(Short.valueOf(newColor.number), newColor);
				}
			}
			for(final Short s : color16map.keySet())
			{
				final Short color256to16 = color16map.get(s);
				final Color256 color256 = straightMap.get(s);
				if(color256 != null)
				{
					final Color256 color16 = straightMap.get(color256to16);
					if((color16 != null)
					&&(color16.getNon256color() != null))
					{
						color256.setNon256color(color16.getNon256color());
						color256to16map.put(Short.valueOf(color256.getCm6Code()), color16.getNon256color());
					}
					else
						Log.errOut("Unable to map color.dat number "+color256.getNumber()+" to "+color256to16);
				}
			}
			Collections.sort(list, new Comparator<Color256>()
			{
				@Override
				public int compare(final Color256 o1, final Color256 o2)
				{
					return Integer.valueOf(o1.getNumber()).compareTo(Integer.valueOf(o2.getNumber()));
				}
			});
			color256s=list.toArray(new Color256[0]);
		}
		return true;
	}

	@Override
	public Color getANSI16Equivalent(final short color256Code)
	{
		return color256to16map.get(Short.valueOf(color256Code));
	}

	@Override
	public Enumeration<Color256> getColors256()
	{
		return new IteratorEnumeration<Color256>(Arrays.asList(this.color256s).iterator());
	}
}
