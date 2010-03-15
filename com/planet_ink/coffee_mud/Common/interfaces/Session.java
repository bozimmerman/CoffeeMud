package com.planet_ink.coffee_mud.Common.interfaces;
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
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
/* 
   Copyright 2000-2010 Bo Zimmerman

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
 * A Session object is the key interface between the internet user 
 * and their player MOB.  In fact, the presence of an attached session
 * object to a MOB is the only difference between an NPC MOB and a player MOB.
 * This object handles input, output, and related processes.
 */
@SuppressWarnings("unchecked")
public interface Session extends CMCommon
{
    
    /**
     * Negotiates various telnet options (or attempts to), and 
     * prints the introTextStr to the user.
     * @param s the socket the user connected from
     * @param introTextStr introductory text string (Hello!)
     */
    public void initializeSession(Socket s, String introTextStr);
    
    /**
     * There is no interface for Thread, so since DefaultSession
     * implements thread, and this fact needs to be externatized,
     * the thread start method is hereby externalized.  Not 
     * required for most sessions, only for those acting as Threads.
     */
    public void start();
    
    /**
     * Returns a list of telnet coded strings indexed by coffeemud
     * color code.  May be from the standard list, or read from 
     * player records for a customized list.
     * @return telnet coded color strings.
     */
	public String[] clookup();
    
    /**
     * Low level text output method.
     * Implements such features as snoops, spam-stacking, page
     * breaks, and line caching
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#out(char[])
     * @param msg the string to send to the user
     * @param noCache true to disable line caching, false otherwise
     */
	public void onlyPrint(String msg, boolean noCache);
    
    /**
     * Low level text output method.
     * Implements such features as snoops, spam-stacking.
     * No page breaking, and Always line caching
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void onlyPrint(String msg);
    
    /**
     * Lowest level user-output method.  Does nothing
     * but send the string to the user, period.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#out(char[])
     * @param msg the string to send to the user
     */
	
    public void rawOut(String msg);
    
    /**
     * Low level line-output method.  Sets the
     * prompt flag after write, and inserts
     * additional pre-linefeed if currently at
     * the prompt.  Adds post linefeed of course.
     * Does not do a page break.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrintln(String, int)
     * @param msg the string to send to the user
     */
	public void rawPrintln(String msg);
    
    /**
     * Low level line-output method.  Sets the
     * prompt flag after write, and inserts
     * additional pre-linefeed if currently at
     * the prompt.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrint(String, int)
     * @param msg the string to send to the user
     */
	public void rawPrint(String msg);
    
    /**
     * Higher-level line output method.  Does full
     * filtering of special characters and codes.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrint(String, int)
     * @param msg the string to send to the user
     */
	public void stdPrint(String msg);
    
    /**
     * Higher-level line output method.  Does full
     * filtering of special characters and codes
     * using given variable values.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrint(String, int)
     * @param Source variable for special code parsing: Source
     * @param Target variable for special code parsing: Target
     * @param Tool variable for special code parsing: Tool
     * @param msg the string to send to the user
     */
	public void stdPrint(Environmental Source,
						 Environmental Target,
						 Environmental Tool,
						 String msg);
    
    /**
     * Higher-level line output method.  Does full
     * filtering of special characters and codes.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrintln(String, int)
     * @param msg the string to send to the user
     */
	public void stdPrintln(String msg);
    
    /**
     * Higher-level line output method.  Does full
     * filtering of special characters and codes
     * using given variable values.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrintln(String, int)
     * @param Source variable for special code parsing: Source
     * @param Target variable for special code parsing: Target
     * @param Tool variable for special code parsing: Tool
     * @param msg the string to send to the user
     */
	public void stdPrintln(Environmental Source,
						   Environmental Target,
						   Environmental Tool,
						   String msg);
    
    /**
     * Lowest level user-output method.  Does nothing
     * but send the string to the user, period.
     * @param c string (as char array) to send out to the user
     */
	public void out(char[] c);
    
    /**
     * Checks whether this session is currently over its 
     * time limit trying to write data to its socket. 
     * For some reason this happens, and this method
     * detects it.
     * @return true if something bas happened, false otherwise
     */
	public boolean isLockedUpWriting();
    
	/**
	 * Returns true if this session is merely a placeholder,
	 * and does not represent an actual user connection.  
	 * Will ensure that the system continues to treat the
	 * user as a mob.
	 * @return true if this session is not a connection
	 */
	public boolean isFake();
	
    /**
     * Medium-level text output method.  Does full
     * filtering of special characters and codes.
     * Does not manage the prompt.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void print(String msg);
    
    /**
     * Medium-level text output method.  Does full
     * filtering of special characters and codes
     * using given variable values.
     * Does not manage the prompt.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param Source variable for special code parsing: Source
     * @param Target variable for special code parsing: Target
     * @param Tool variable for special code parsing: Tool
     * @param msg the string to send to the user
     */
	public void print(Environmental Source,
					  Environmental Target,
					  Environmental Tool,
					  String msg);
    
    /**
     * Medium-level text output method.  Does full
     * filtering of special characters and codes.
     * Does not manage the prompt.
     * Adds a linefeed at the end though.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void println(String msg);
    
    /**
     * Medium-level text output method.  Does full
     * filtering of special characters and codes
     * using given variable values.
     * Does not manage the prompt.
     * Adds a linefeed at the end though.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param Source variable for special code parsing: Source
     * @param Target variable for special code parsing: Target
     * @param Tool variable for special code parsing: Tool
     * @param msg the string to send to the user
     */
	public void println(Environmental Source,
						Environmental Target,
						Environmental Tool,
						String msg);
    
    /**
     * Notifies this session to output the users prompt
     * again once it has reached a suitable lapse in
     * text output.
     * @param truefalse true to send another prompt, false otherwise
     */
	public void setPromptFlag(boolean truefalse);
    
    /**
     * Medium-level text output method.  Does full
     * filtering of special characters and codes.
     * DOES manage the prompt, but turns OFF word wrap!
     * Adds a linefeed at the end.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void wraplessPrintln(String msg);
    
    /**
     * Medium-level text output method.  Does full
     * filtering of special characters and codes.
     * DOES manage the prompt, but turns OFF word wrap!
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void wraplessPrint(String msg);
    
    /**
     * Lower-Medium-level text output method.  Does only the
     * parsing of color codes, no word wrapping, no codes.
     * Adds a linefeed at the end.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     * @param noCache true to disable line caching, false otherwise
     */
	public void colorOnlyPrintln(String msg, boolean noCache);
    
    /**
     * Lower-Medium-level text output method.  Does only the
     * parsing of color codes, no word wrapping, no codes.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     * @param noCache true to disable line caching, false otherwise
     */
	public void colorOnlyPrint(String msg, boolean noCache);
    
    /**
     * Lower-Medium-level text output method.  Does only the
     * parsing of color codes, no word wrapping, no codes.
     * Adds a linefeed at the end.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void colorOnlyPrintln(String msg);
    
    /**
     * Lower-Medium-level text output method.  Does only the
     * parsing of color codes, no word wrapping, no codes.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
     * @param msg the string to send to the user
     */
	public void colorOnlyPrint(String msg);
    
    /**
     * Waits the given milliseconds for a key to be pressed, after which
     * it returns that key, or \0 if nothing pressed.
     * @param maxWait the maximum milliseconds to wait
     * @return the key pressed, or \0
     */
    public char hotkey(long maxWait);
    
    /**
     * Prompts the user to enter a string, and then returns what
     * the enter.  Does not time out, but may throw an exception
     * on disconnnect.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, long)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, String, long)
     * @param Message the prompt message to display to the user
     * @param Default the default response if the user just hits enter
     * @return the string entered by the user, or the Default
     * @throws IOException a disconnect
     */
	public String prompt(String Message, String Default)
		throws IOException;
    
    /**
     * Prompts the user to enter a string, and then returns what
     * the enter.  Possibly times out, and may throw an exception
     * on disconnnect or time out.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, long)
     * @param Message the prompt message to display to the user
     * @param Default the default response if the user just hits enter
     * @param maxTime max number of milliseconds to wait before timing out
     * @return the string entered by the user, or the Default
     * @throws IOException a disconnect or time out
     */
	public String prompt(String Message, String Default, long maxTime)
		throws IOException;
    
    /**
     * Prompts the user to enter a string, and then returns what
     * the enter.  Does not time out, but may throw an exception
     * on disconnnect.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, long)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, String, long)
     * @param Message the prompt message to display to the user
     * @return the string entered by the user
     * @throws IOException a disconnect
     */
	public String prompt(String Message)
		throws IOException;
    
    /**
     * Prompts the user to enter a string, and then returns what
     * the enter.  Possibly times out, and may throw an exception
     * on disconnnect or time out.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#prompt(String, String, long)
     * @param Message the prompt message to display to the user
     * @param maxTime max number of milliseconds to wait before timing out
     * @return the string entered by the user
     * @throws IOException a disconnect or time out
     */
	public String prompt(String Message, long maxTime)
		throws IOException;
    
    /**
     * Prompts the user to enter Y or N, and returns what they
     * enter.  Will not time out, but may throw an exception on
     * disconnect.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#confirm(String, String, long)
     * @param Message the prompt message to display to the user
     * @param Default the default response if the user just hits enter
     * @return true if they entered Y, false otherwise
     * @throws IOException a disconnect
     */
	public boolean confirm(String Message, String Default)
	    throws IOException;
    
    /**
     * Prompts the user to enter Y or N, and returns what they
     * enter. Possibly times out, and may throw an exception
     * on disconnnect or time out.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#confirm(String, String)
     * @param Message the prompt message to display to the user
     * @param Default the default response if the user just hits enter
     * @param maxTime max number of milliseconds to wait before timing out
     * @return true if they entered Y, false otherwise
     * @throws IOException a disconnect or time out
     */
	public boolean confirm(String Message, String Default, long maxTime)
	    throws IOException;
    
    /**
     * Prompts the user to enter one character responses from a set of
     * valid choices.  Repeats the prompt if the user does not enter
     * a valid choice.  ENTER is a valid choice for Default. Does not time out, 
     * but may throw an exception on disconnnect.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#choose(String, String, String, long)
     * @param Message the prompt message to display to the user
     * @param Choices a list of uppercase characters that may be entered
     * @param Default the default response if the user just hits enter
     * @return the character entered from the choices
     * @throws IOException a disconnect
     */
	public String choose(String Message, String Choices, String Default)
	    throws IOException;
    
    /**
     * Prompts the user to enter one character responses from a set of
     * valid choices.  Repeats the prompt if the user does not enter
     * a valid choice.  ENTER is a valid choice for Default.   May time out, 
     * and may throw an exception on disconnnect.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#choose(String, String, String)
     * @param Message the prompt message to display to the user
     * @param Choices a list of uppercase characters that may be entered
     * @param Default the default response if the user just hits enter
     * @param maxTime max number of milliseconds to wait before timing out
     * @return the character entered from the choices
     * @throws IOException a disconnect or time out
     */
	public String choose(String Message, String Choices, String Default, long maxTime)
	    throws IOException;
    
    /**
     * Notifies this session that the given session is snooping it.  
     * This session will manage said snooping.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#startBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#stopBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#amBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
     * @param S the session to snoop on me.
     */
	public void startBeingSnoopedBy(Session S);
    
    /**
     * Notifies this session that the given session is no longer snooping it.
     * This session will remove said snooping.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#startBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#stopBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#amBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
     * @param S the session to stop snooping on me.
     */
	public void stopBeingSnoopedBy(Session S);
    
    /**
     * Checks to see if the given session is snooping on this one.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#startBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#stopBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#amBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
     * @param S the session to check for a snoop on me.
     * @return true if the given session is snooping on me, false otherwise
     */
	public boolean amBeingSnoopedBy(Session S);
    
    /**
     * Increments or decrements the snoop suspension counter
     * by the given amount.  Only when the counter is 0 does
     * snooping of this session actually occur.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#startBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#stopBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#amBeingSnoopedBy(Session)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
     * @param change the amount to change the snoop suspension counter by
     * @return the current value of the snoop suspension counter after the change
     */
	public int snoopSuspension(int change);
    
    /**
     * Force the current player to logoff.
     * @param removeMOB true to remove the mob from the game
     * @param dropSession true to force closed sockets, and removed session
     * @param killThread true to force a thread death, and false to be more lenient
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#killFlag()
     */
	public void kill(boolean removeMOB, boolean dropSession, boolean killThread);
    
    /**
     * Returns whether this session is done, or slated to be done.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#logoff(boolean, boolean, boolean)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#killFlag()
     * @return true if this session needs to go, false otherwise
     */
	public boolean killFlag();
    
	/**
	 * Allows the user to select a different character, taking them back to the login
	 * prompt, or to the account character listing screen, whichever is appropriate.
     * @param removeMOB true to remove the mob from the game
	 */
	public void logout(boolean removeMOB);
	
    /**
     * Returns whether this mob/session is currently Away From Keyboard
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAfkFlag(boolean)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAFKMessage(String)
     * @return true if they are AFK, false otherwise
     */
	public boolean afkFlag();
    
    /**
     * Sets whether this mob/session is currently Away From Keyboard
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#afkFlag()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#afkMessage()
     * @param truefalse true if they are AFK, false otherwise
     */
	public void setAfkFlag(boolean truefalse);
    
    /**
     * Returns the reason given by the user that they are AFK.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAfkFlag(boolean)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAFKMessage(String)
     * @return  the reason given by the user that they are AFK.
     */
    public String afkMessage();
    
    /**
     * Returns the reason given by the user that they are AFK.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAfkFlag(boolean)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#afkMessage()
     * @param str the reason given by the user that they are AFK.
     */
    public void setAFKMessage(String str);
    
    /**
     * Blocks the current thread until the user attached to this session
     * hits ENTER, returning the characters they enter.  Completely filtered input.
     * @return the string entered by the user
     * @throws IOException any exception generated during input
     */
	public String blockingIn()
		throws IOException;
    
    /**
     * Blocks for a short amount of time, returning an input
     * string only if the user happens to have hit enter. 
     * @return a string entered by the user
     * @throws IOException exceptions thrown, typically a timeout
     */
	public String readlineContinue()
		throws IOException, SocketException;
    
    /**
     * Returns a pre-parsed, pre-filtered Vector of strings
     * representing the last command entered by the user 
     * through this session.
     * @return a vector of strings
     */
	public Vector previousCMD();
    
    /**
     * Returns the player MOB attached to this session object.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setMob(MOB)
     * @return  the player MOB attached to this session object.
     */
	public MOB mob();
    
    /**
     * Sets the player MOB attached to this session object.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#mob()
     * @param newmob the player MOB attached to this session object.
     */
	public void setMob(MOB newmob);
    
    /**
     * Sets the player acount attached to this session object.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#mob()
     * @param newmob the player account attached to this session object.
     */
	public void setAccount(PlayerAccount account);
	
    /**
     * Converts a character after the ^ sign (usually a color code)
     * into an appropriate telnet escape sequence string for output.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getColor(char)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#currentColor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#lastColor()
     * @param c the ^ character code
     * @return telnet escape sequence string for output
     */
	public String makeEscape(int c);
    
    /**
     * Returns the given color code, unless it is one that translates
     * to another, such as ?
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#makeEscape(int)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#currentColor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#lastColor()
     * @param c the color code
     * @return the color code again
     */
	public int getColor(char c);
    
    /**
     * Returns the current color code.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getColor(char)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#makeEscape(int)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#lastColor()
     * @return the current color code.
     */
	public int currentColor();
    
    /**
     * Returns the previous current color code.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getColor(char)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#currentColor()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#makeEscape(int)
     * @return the previous current color code.
     */
    public int lastColor();
    
    /**
     * Gets the column number for engine word-wrapping. 
     * 0 Means disabled.
     * 
     * @return the wrap column
     */
	public int getWrap();
    
    /**
     * Gets the current telnet clients ip address.
     * 
     * @return the ip address
     */
	public String getAddress();
    
    /**
     * Gets the tick/thread status of this session object.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#STATUS_LOGIN
     * @return the tick status
     */
	public int getStatus();
    
    /**
     * Gets the total milliseconds consumed by this session objects thread.
     * 
     * @return the total milliseconds consumed
     */
	public long getTotalMillis();
    
    /**
     * Gets the total number of ticks consumed by this session object thread.
     * 
     * @return the total ticks consumed
     */
	public long getTotalTicks();
    
    /**
     * Gets the number of milliseconds since a user entry was registered by this session
     * 
     * @return the idle milliseconds passed
     */
	public long getIdleMillis();
    
    /**
     * Gets the milliseconds elapsed since this user came online.
     * 
     * @return the milliseconds online
     */
    public long getMillisOnline();
    
    /**
     * Gets the milliseconds timestamp since this user last registered a pk fight.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setLastPKFight()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setLastNPCFight()
     * @return the last pk fight timestamp
     */
    public long getLastPKFight();
    
    /**
     * Sets now as the milliseconds timestamp since this user last registered a pk fight.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getLastPKFight()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getLastNPCFight()
     */
    public void setLastPKFight();
    
    /**
     * Gets the milliseconds timestamp since this user last registered a npc fight.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setLastPKFight()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setLastNPCFight()
     * @return the last npc fight timestamp
     */
    public long getLastNPCFight();
    
    /**
     * Sets now as the milliseconds timestamp since this user last registered a npc fight.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getLastPKFight()
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getLastNPCFight()
     */
    public void setLastNPCFight();
    
    /**
     * Returns the last time in milliseconds that this session began its input loop.
     * Is typically only held up by executing a user command, so it is an accurate
     * gauge of a locked up user command.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#updateLoopTime()
     * 
     * @return the last time in milliseconds that this session began its input loop
     */
    public long lastLoopTime();
    
    /**
     * Sets the last time in milliseconds that this session began its input loop.
     * Is typically only held up by executing a user command, so it is an accurate
     * gauge of a locked up user command.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#lastLoopTime()
     */
	public void updateLoopTime();
	
    /**
     * Returns a Vector of the last several message strings received by this user.
     * All are already previously filtered and parsed and ready to display.
     * 
     * @return a vector of string message strings
     */
    public Vector getLastMsgs();
    
    /**
     * Gets the terminal type the user has
     * @return the terminal type
     */
    public String getTerminalType();
    
    /**
     * Sets a server telnet mode flag.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#serverTelnetMode(int)
     * @param telnetCode the telnet code
     * @param onOff true to turn on, false to turn off the flag
     */
    public void setServerTelnetMode(int telnetCode, boolean onOff);
    
    /**
     * Gets a server telnet mode flag.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setServerTelnetMode(int, boolean)
     * @param telnetCode the telnet code
     * 
     * @return true, if server telnet mode is on, false otherwise
     */
    public boolean serverTelnetMode(int telnetCode);
    
    /**
     * Sets a client telnet mode flag.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#clientTelnetMode(int)
     * 
     * @param telnetCode the telnet code
     * @param onOff true to turn on, false to turn off the flag
     */
    public void setClientTelnetMode(int telnetCode, boolean onOff);
    
    /**
     * Gets a client telnet mode flag.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setClientTelnetMode(int, boolean)
     * 
     * @param telnetCode the telnet code
     * 
     * @return true, if client telnet mode is on, false otherwise
     */
    public boolean clientTelnetMode(int telnetCode);
    
    /**
     * Change telnet mode by sending the appropriate command to the clients client.
     * A response received later will trigger mode changed.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setClientTelnetMode(int, boolean)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setServerTelnetMode(int, boolean)
     * @param telnetCode the telnet code
     * @param onOff true to turn on, false to turn off the flag
     */
    public void changeTelnetMode(int telnetCode, boolean onOff);
    
    /**
     * Change telnet mode by negotiating the command to the clients client.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setClientTelnetMode(int, boolean)
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setServerTelnetMode(int, boolean)
     * @param telnetCode the telnet code
     */
    public void negotiateTelnetMode(int telnetCode);
    
    /**
     * Initializes a telnet mode between this session and the connected client by negotiating
     * certain fundamental flags, like ANSI, MXP, and MSP.  It will use a bitmap of MOB flags
     * passed in as a guide.
     * 
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#getBitmap()
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#ATT_MXP
     * @see com.planet_ink.coffee_mud.Common.interfaces.Session#changeTelnetMode(int, boolean)
     * @param mobbitmap the mobbitmap the bitmap of mob flags to use as a guide in negotiation
     */
    public void initTelnetMode(int mobbitmap);
    
    /** TELNET CODE: transmit binary */
    public static final int TELNET_BINARY=0;
    /** TELNET CODE: echo */
    public static final int TELNET_ECHO=1;
    /** TELNET CODE: echo */
    public static final int TELNET_LOGOUT=18;
    /** TELNET CODE: supress go ahead*/
    public static final int TELNET_SUPRESS_GO_AHEAD=3;
    /** TELNET CODE: sending terminal type*/
    public static final int TELNET_TERMTYPE=24;
    /** TELNET CODE: Negotiate About Window Size.*/
    public static final int TELNET_NAWS=31;
    /** TELNET CODE: Remote Flow Control.*/
    public static final int TELNET_TOGGLE_FLOW_CONTROL=33;
    /** TELNET CODE: Linemode*/
    public static final int TELNET_LINEMODE=34;
    /** TELNET CODE: MSSP Server Status protocol*/
    public static final int TELNET_MSSP=70;
    /** TELNET CODE: text compression, protocol 1*/
    public static final int TELNET_COMPRESS=85;
    /** TELNET CODE: text compression, protocol 2*/
    public static final int TELNET_COMPRESS2=86;
    /** TELNET CODE: MSP SOund protocol*/
    public static final int TELNET_MSP=90;
    /** TELNET CODE: MXP Extended protocol*/
	public static final int TELNET_MXP=91;
    /** TELNET CODE: End of subnegotiation parameters*/
	public static final int TELNET_SE=240;
    /** TELNET CODE: Are You There*/
    public static final int TELNET_AYT=246;
    /** TELNET CODE: Erase character*/
    public static final int TELNET_EC=247;
    /** TELNET CODE: Indicates that what follows is subnegotiation of the indicated option*/
    public static final int TELNET_SB=250;
    /** TELNET CODE: Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option*/
	public static final int TELNET_WILL=251;
    /** TELNET CODE: Indicates the refusal to perform, or continue performing, the indicated option*/
	public static final int TELNET_WONT=252;
    /** TELNET CODE: Indicates the request that the other party perform, or confirmation that you are expecting the other party to perform, the indicated option*/
	public static final int TELNET_DO=253;
    /** TELNET CODE: 253 doubles as fake ansi telnet code*/
    public static final int TELNET_ANSI=253;
    /** TELNET CODE: Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.*/
	public static final int TELNET_DONT=254;
    /** TELNET CODE: IAC*/
	public static final int TELNET_IAC=255;
    /** Array String-friendly descriptions of the various telnet codes.  Indexed by code id 0-255*/
    public static final String[] TELNET_DESCS=
    { 
        "BINARY","ECHO","2","SUPRESS GO AHEAD","4","5","6","7","8","9", //0-9
        "10","11","12","13","14","15","16","17","LOGOUT","19", //10-19
        "20","21","22","23","TERMTYPE","25","26","27","28","29", //20-29
        "30","NAWS","32","FLOWCONTROL","LINEMODE","35","36","37","38","39", //30-39
        "40","41","42","43","44","45","46","47","48","49", //40-49
        "50","51","52","53","54","55","56","57","58","59", //50-59
        "60","61","62","63","64","65","66","67","68","69", //60-69
        "MSSP","71","72","73","74","75","76","77","78","79", //70-79
        "","","","","","COMPRESS","COMPRESS2","","","", //80-89
        "MSP","MXP","","","","","","","","", //90-99
        "","","","","","","","","","", //100-109
        "","","","","","","","","","", //110-119
        "","","","","","","","","","", //120-129
        "","","","","","","","","","", //130-139
        "","","","","","","","","","", //140-149
        "","","","","","","","","","", //150-159
        "","","","","","","","","","", //160-169
        "","","","","","","","","","", //170-179
        "","","","","","","","","","", //180-189
        "","","","","","","","","","", //190-199
        "","","","","","","","","","", //200-209
        "","","","","","","","","","", //210-219
        "","","","","","","","","","", //220-229
        "","","","","","","","","","", //230-239
        "SE","","","","","","AYT","EC","","", //240-249
        "SB","","","ANSI","",""              //250-255
    };
    
    /** for REPLY command, constant for maximum number of saved strings */
    public static final int MAX_PREVMSGS=100;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODE=1;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODEMASK_EDIT=1;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODEMASK_TRAPSIG=2;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_MODEMASK_ACK=4;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC=3;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_DEFAULT=3;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_VALUE=2;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_CANTCHANGE=1;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_NOSUPPORT=0;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_ACK=128;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_XON=15;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_XOFF=16;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_EOF=8;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_SUSP=9;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_BRK=2;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_IP=3;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_AO=4;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_AYT=5;
    /** Some sort of TELNET protocol constant related to LINEMODE -- I've no idea what it does */
    public static final int TELNET_LINEMODE_SLC_EOR=6;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_OK=0;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGIN=1;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_ACCOUNTMENU=2;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGIN1=3;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGIN2=4;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT=5;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT1=6;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT2=7;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT3=8;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT4=9;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT5=10;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT6=11;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT7=12;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT8=13;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT9=14;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT10=15;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT11=16;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUT12=17;
    /** Status value constant possibly returned by getStatus method */
	public static final int STATUS_LOGOUTFINAL=18;
    /** Status strings indexed by the various STATUS_ constants.  Descriptive strings */
	public static final String[] STATUS_STR={"OPEN","LOGIN-S","ACCOUNTMENU","LOGIN-1","LOGIN-2",
											"LOGOUT-S","LOGOUT-1","LOGOUT-2","LOGOUT-3",
											"LOGOUT-4","LOGOUT-5","LOGOUT-6","LOGOUT-7",
											"LOGOUT-8","LOGOUT-9","LOGOUT-10","LOGOUT-11",
											"LOGOUT-12","CLOSED"};
}
