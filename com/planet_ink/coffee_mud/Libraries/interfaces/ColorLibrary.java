package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/*
   Copyright 2005-2015 Bo Zimmerman

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
public interface ColorLibrary extends CMLibrary
{
	public enum Color
	{
		WHITE("\033[1;37m","<FONT COLOR=WHITE",'w'),
		LIGHTGREEN("\033[1;32m","<FONT COLOR=LIGHTGREEN",'g'),
		LIGHTBLUE("\033[1;34m","<FONT COLOR=BLUE",'b'),
		LIGHTRED("\033[1;31m","<FONT COLOR=RED",'r'),
		YELLOW("\033[1;33m","<FONT COLOR=YELLOW",'y'),
		LIGHTCYAN("\033[1;36m","<FONT COLOR=CYAN",'c'),
		LIGHTPURPLE("\033[1;35m","<FONT COLOR=VIOLET",'p'),
		GREY("\033[0;37m","<FONT COLOR=LIGHTGREY",'W'),
		GREEN("\033[0;32m","<FONT COLOR=GREEN",'G'),
		BLUE("\033[0;34m","<FONT COLOR=#000099",'B'),
		RED("\033[0;31m","<FONT COLOR=#993300",'R'),
		BROWN("\033[0;33m","<FONT COLOR=#999966",'Y'),
		CYAN("\033[0;36m","<FONT COLOR=DARKCYAN",'C'),
		PURPLE("\033[0;35m","<FONT COLOR=PURPLE",'P'),
		DARKGREY("\033[1;30m","<FONT COLOR=GRAY",'k'),
		BLACK("\033[0;30m","<FONT COLOR=BLACK",'K'),
		NONE("\033[0;0m","</I></U></BLINK></B></FONT"),
		BOLD("\033[1m","<B"),
		UNDERLINE("\033[4m","<U"),
		BLINK("\033[5m","<BLINK"),
		ITALICS("\033[6m","<I"),
		BGWHITE("\033[47m"," style=\"background-color: white\""),
		BGGREEN("\033[42m"," style=\"background-color: green\""),
		BGBLUE("\033[44m"," style=\"background-color: #000099\""),
		BGRED("\033[41m"," style=\"background-color: #993300\""),
		BGYELLOW("\033[43m"," style=\"background-color: #999966\""),
		BGCYAN("\033[46m"," style=\"background-color: darkcyan\""),
		BGPURPLE("\033[45m"," style=\"background-color: purple\""),
		BGBLACK("\033[40m"," style=\"background-color: black\""),
		BGDEFAULT("\033[49m"," style=\"background-color: white\""),
		;
		
		private final String	ansiCode;
		private final String	htmlTag;
		private final char		codeLetter;
		private final boolean	isBasicColor;
		private final boolean	isExtendedColor;
		
		private Color(String ansiCode, String htmlTag, char codeLetter)
		{
			this.ansiCode = ansiCode;
			this.codeLetter = codeLetter;
			this.htmlTag = htmlTag;
			isBasicColor = ((this.codeLetter != 'K') && (this.codeLetter != '\0'));
			isExtendedColor = (this.codeLetter != '\0');
		}
		
		private Color(String ansiCode, String htmlTag)
		{
			this(ansiCode, htmlTag, '\0');
		}
		
		public final boolean isBasicColor()
		{
			return isBasicColor;
		}
		
		public final boolean isExtendedColor()
		{
			return isExtendedColor;
		}
		
		public final String getHtmlTag()
		{
			return htmlTag;
		}
		
		public final String getANSICode()
		{
			return ansiCode;
		}
		
		public final char getCodeChar()
		{
			return codeLetter;
		}
		
		public final String getCodeString()
		{
			return name();
		}
		
	}

	public static final Color[] COLOR_CODELETTERSINCARDINALORDER=
	{
		Color.DARKGREY,
		Color.LIGHTRED,
		Color.LIGHTGREEN,
		Color.YELLOW,
		Color.LIGHTBLUE,
		Color.LIGHTPURPLE,
		Color.LIGHTCYAN,
		Color.WHITE,
		null,
		null
	};
	
	public static final Map<String,String> MAP_ANSICOLOR_TO_ANSIBGCOLOR=new SHashtable<String,String>(new Object[][]{
		{   Color.WHITE.getANSICode(), Color.BGWHITE.getANSICode()},
		{   Color.LIGHTGREEN.getANSICode(), Color.BGGREEN.getANSICode()},
		{   Color.LIGHTBLUE.getANSICode(), Color.BGBLUE.getANSICode()},
		{   Color.LIGHTRED.getANSICode(), Color.BGRED.getANSICode()},
		{   Color.YELLOW.getANSICode(), Color.BGYELLOW.getANSICode()},
		{   Color.LIGHTCYAN.getANSICode(), Color.BGCYAN.getANSICode()},
		{   Color.LIGHTPURPLE.getANSICode(), Color.BGPURPLE.getANSICode()},
		{   Color.GREY.getANSICode(), Color.BGWHITE.getANSICode()},
		{   Color.GREEN.getANSICode(), Color.BGGREEN.getANSICode()},
		{   Color.BLUE.getANSICode(), Color.BGBLUE.getANSICode()},
		{   Color.RED.getANSICode(), Color.BGRED.getANSICode()},
		{   Color.BROWN.getANSICode(), Color.BGYELLOW.getANSICode()},
		{   Color.CYAN.getANSICode(), Color.BGCYAN.getANSICode()},
		{   Color.PURPLE.getANSICode(), Color.BGPURPLE.getANSICode()},
		{   Color.DARKGREY.getANSICode(), Color.BGDEFAULT.getANSICode()},
		{   Color.BLACK.getANSICode(), Color.BGBLACK.getANSICode()}
	});

	//remaining=aijlnoszAJV
	public enum SpecialColor
	{
		YOU_FIGHT('f'),
		FIGHT_YOU('e'),
		FIGHT('F'),
		SPELL('S'),
		EMOTE('E'),
		TALK('T'),
		TELL('t'),
		CHANNEL('Q'),
		CHANNELFORE('q'),
		IMPORTANT1('x'),
		IMPORTANT2('X'),
		IMPORTANT3('Z'),
		ROOMTITLE('O'),
		ROOMDESC('L'),
		DIRECTION('D'),
		DOORDESC('d'),
		ITEM('I'),
		MOB('M'),
		HITPOINTS('h'),
		MANA('m'),
		MOVES('v'),
		NORMAL('N'),
		HIGHLIGHT('H'),
		UNEXPDIRECTION('U'),
		UNEXPDOORDESC('u'),
		WEATHER('J')
		;
		
		private final char		code;
		private final String	underStr;
		
		private SpecialColor(char escapeCode)
		{
			this.code = escapeCode;
			this.underStr = name().replace('_', '-');
		}
		
		public final char getCodeChar()
		{
			return code;
		}
		
		public final String getCodeString()
		{
			return underStr;
		}
	}

	public static final char COLORCODE_BACKGROUND='~';
	public static final char COLORCODE_FANSI256='#';
	public static final char COLORCODE_BANSI256='|';

	public static final String COLOR_FR0G3B5="\033[38;5;"+(16+(0*36)+(3*6)+5)+"m";
	public static final String COLOR_BR0G3B5="\033[48;5;"+(16+(0*36)+(3*6)+5)+"m";

	public interface ColorState
	{
		public char foregroundCode();
		public char backgroundCode();
	}
	
	public void clearLookups();
	public int translateSingleCMCodeToANSIOffSet(String code);
	public String translateCMCodeToANSI(String code);
	public String translateANSItoCMCode(String code);
	public String mixHTMLCodes(String code1, String code2);
	public String mixColorCodes(String code1, String code2);
	public CMMsg fixSourceFightColor(CMMsg msg);
	public String[] standardHTMLlookups();
	public String[] standardColorLookups();
	public ColorState valueOf(final char fg, final char bg);
	public ColorState getNormalColor();
}
