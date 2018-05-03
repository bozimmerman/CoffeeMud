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
/**
 * The color library does just what it says, provides access to methods
 * to translate colors from words, to ansi codes, to html tags, and
 * combine them all in ways appropriate for their use.
 * 
 * @author Bo Zimmerman
 *
 */
public interface ColorLibrary extends CMLibrary
{
	/**
	 * These are the straight color codes and character codes
	 * such as blink adn so forth.  Includes back and foreground,
	 * and lots of codes for each one, including ansi codes,
	 * html tags, ^ codes, and so forth.
	 * 
	 * @author Bo Zimmerman
	 */
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
		
		/**
		 * True if its a basic 16 color, but also non-black.
		 * @return its a basic 16 color, but also non-black.
		 */
		public final boolean isBasicColor()
		{
			return isBasicColor;
		}
		
		/**
		 * True if its a basic 16 color, including black.
		 * @return its a basic 16 color, including black.
		 */
		public final boolean isExtendedColor()
		{
			return isExtendedColor;
		}

		/**
		 * Returns the HTML tag for this color code
		 * @return the HTML tag for this color code
		 */
		public final String getHtmlTag()
		{
			return htmlTag;
		}

		/**
		 * Returns the ANSI escape codes for this color.
		 * @return the ANSI escape codes for this color.
		 */
		public final String getANSICode()
		{
			return ansiCode;
		}

		/**
		 * Returns the ^ char code, or 0 if its not a basic color
		 * @return the ^ char code, or 0 if its not a basic color
		 */
		public final char getCodeChar()
		{
			return codeLetter;
		}
		
		/**
		 * Returns the name, but with - instead of _
		 * @return the name, but with - instead of _
		 */
		public final String getCodeString()
		{
			return name();
		}
		
	}

	/**
	 * The Basic 8 Colors, in their numeric ansi code order.
	 */
	public static final Color[] COLORS_INCARDINALORDER=
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
	
	/**
	 * A mapping of the escape codes for the basic 16 ansi 
	 * colors to the basic 16 background ansi color codes
	 */
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
		{
			Color.BLACK.getANSICode(), Color.BGBLACK.getANSICode()
		}
	});

	//remaining=aijlnoszAJV
	/**
	 * The special color codes are named color codes which are
	 * usually renameable by the user to other basic colors.
	 * 
	 * @author Bo Zimmerman
	 *
	 */
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
		
		private final char		cchar;
		private final String	underStr;
		private final String	escapeCode;
		
		private SpecialColor(char escapeChar)
		{
			this.cchar = escapeChar;
			this.underStr = name().replace('_', '-');
			this.escapeCode = "^"+cchar;
		}
		
		/**
		 * Returns the ^ color code
		 * @return the ^ color code
		 */
		public final char getCodeChar()
		{
			return cchar;
		}
		
		/**
		 * Returns the FULL ^ color code
		 * @return the FULL ^ color code
		 */
		public final String getEscapeCode()
		{
			return escapeCode;
		}
		
		/**
		 * Returns the name with _ replaced by -
		 * @return the name with _ replaced by -
		 */
		public final String getCodeString()
		{
			return underStr;
		}
	}

	/**
	 * Color code prefix to designate background color (^ for foreground)
	 */
	public static final char COLORCODE_BACKGROUND='~';
	
	/**
	 * Color code prefix to designate foreground ansi color
	 */
	public static final char COLORCODE_FANSI256='#';
	
	/**
	 * Color code prefix to designate background ansi color
	 */
	public static final char COLORCODE_BANSI256='|';

	/**
	 * A color state is a class saved for users so the system knows
	 * what the current color situation is, which allows it to go
	 * back when it changes.
	 * @author Bo Zimmerman
	 *
	 */
	public interface ColorState
	{
		/**
		 * Returns the foreground basic 16 color code
		 * @return the foreground basic 16 color code
		 */
		public char foregroundCode();
		
		/**
		 * Returns the background basic 16 color code
		 * @return the background basic 16 color code
		 */
		public char backgroundCode();
	}

	/**
	 * Clears the color code lookup tables so that the next
	 * translations will come from the properties.
	 */
	public void clearLookups();

	/**
	 * Translates a basic 16 or special color code, anything
	 * that starts with ^ to the ansi escape sequence.
	 * @see ColorLibrary#translateANSItoCMCode(String)
	 * @param code the ^ code
	 * @return the ansi escape sequence
	 */
	public String translateCMCodeToANSI(String code);

	/**
	 * Translates an ansi escape sequence to a 
	 * basic 16 or special color code, anything
	 * that starts with ^ to the .
	 * @see ColorLibrary#translateCMCodeToANSI(String)
	 * @param code ansi escape sequence
	 * @return the ^ code
	 */
	public String translateANSItoCMCode(String code);

	/**
	 * Given a foreground and background set of html tags,
	 * this method generates a single useable html tag
	 * from the two.
	 * @see ColorLibrary#mixColorCodes(String, String)
	 * @param code1 the first html tag
	 * @param code2 the other html tag, or null
	 * @return the combined html tag
	 */
	public String mixHTMLCodes(String code1, String code2);

	/**
	 * Given a foreground and background set of ansi escape codes,
	 * this method generates a single useable ansi escape code
	 * from the two.
	 * @see ColorLibrary#mixHTMLCodes(String, String)
	 * @param code1 the first ansi escape code
	 * @param code2 the other ansi escape code, or null
	 * @return the combined ansi escape code
	 */
	public String mixColorCodes(String code1, String code2);

	/**
	 * Does nothing more impressive than adding the color codes
	 * for combat to the source and target messages if possible.
	 * @param msg the message to colorize
	 * @return the same CMMsg sent, returned.
	 */
	public CMMsg fixSourceFightColor(CMMsg msg);

	/**
	 * Returns the standard html tag lookup table, which
	 * maps an array indexed by the ^ color codes to an html
	 * tag.
	 * @see ColorLibrary#standardColorLookups()
	 * @return the html tag lookup table
	 */
	public String[] standardHTMLlookups();
	
	/**
	 * Returns the standard ansi escape color lookup table, which
	 * maps an array indexed by the ^ color codes to an ansi escape 
	 * color
	 * @see ColorLibrary#standardHTMLlookups()
	 * @return the ansi escape color lookup table
	 */
	public String[] standardColorLookups();
	
	/**
	 * Generates a new ColorState object from the given 
	 * foreground and background basic 16 ^ color codes.
	 * @see ColorLibrary.ColorState
	 * @param fg the foreground color code char
	 * @param bg the background color code char
	 * @return the new ColorState object
	 */
	public ColorState valueOf(final char fg, final char bg);
	
	/**
	 * A ColorState object that represents the basic normal
	 * color, typically grey with no background.
	 * @see ColorLibrary.ColorState
	 * @return the normal color ColorState
	 */
	public ColorState getNormalColor();
}
