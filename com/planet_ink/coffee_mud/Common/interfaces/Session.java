package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
/*
   Copyright 2001-2018 Bo Zimmerman

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
public interface Session extends CMCommon, Modifiable, CMRunnable
{

	/**
	 * Negotiates various telnet options (or attempts to), and
	 * prints the introTextStr to the user.
	 * @param s the socket the user connected from
	 * @param groupName the name of the thread group the session goes to
	 * @param introTextStr introductory text string (Hello!)
	 */
	public void initializeSession(Socket s, String groupName, String introTextStr);

	/**
	 * Returns the group name to which this session belongs
	 * @return the thread group name
	 */
	public String getGroupName();

	/**
	 * Sets the group name to which this session belongs
	 * @param group the thread group name
	 */
	public void setGroupName(String group);

	/**
	 * Returns a list of standard/foreground telnet coded strings
	 * indexed by coffeemud color code.  May be from the standard list,
	 * or read from player records for a customized list.
	 * @return telnet coded color strings.
	 */
	public String[] getColorCodes();

	/**
	 * Low level text output method.
	 * Implements such features as snoops, spam-stacking, page
	 * breaks, and line caching
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawCharsOut(char[])
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
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawCharsOut(char[])
	 * @param msg the string to send to the user
	 */

	public void rawOut(String msg);

	/**
	 * Low level line-output method.  Sets the
	 * prompt flag after write, and inserts
	 * additional pre-linefeed if currently at
	 * the prompt.  Adds post linefeed of course.
	 * Does not do a page break.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrintln(String)
	 * @param msg the string to send to the user
	 */
	public void rawPrintln(String msg);

	/**
	 * Low level line-output method.  Sets the
	 * prompt flag after write, and inserts
	 * additional pre-linefeed if currently at
	 * the prompt.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrint(String)
	 * @param msg the string to send to the user
	 */
	public void rawPrint(String msg);

	/**
	 * Low level line-output method.  Sets the
	 * prompt flag after write, and inserts
	 * additional pre-linefeed if currently at
	 * the prompt.  Adds post linefeed of course.
	 * Does not do a page break, or color, but does
	 * protect mxp clients.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#safeRawPrint(String)
	 * @param msg the string to send to the user
	 */
	public void safeRawPrintln(String msg);

	/**
	 * Low level line-output method.  Sets the
	 * prompt flag after write, and inserts
	 * additional pre-linefeed if currently at
	 * the prompt.  Does not do color, but does
	 * protect mxp clients.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#safeRawPrintln(String)
	 * @param msg the string to send to the user
	 */
	public void safeRawPrint(String msg);

	/**
	 * Higher-level line output method.  Does full
	 * filtering of special characters and codes.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrint(String)
	 * @param msg the string to send to the user
	 */
	public void stdPrint(String msg);

	/**
	 * Higher-level line output method.  Does full
	 * filtering of special characters and codes
	 * using given variable values.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrint(String)
	 * @param Source variable for special code parsing: Source
	 * @param Target variable for special code parsing: Target
	 * @param Tool variable for special code parsing: Tool
	 * @param msg the string to send to the user
	 */
	public void stdPrint(Physical Source,
						 Environmental Target,
						 Environmental Tool,
						 String msg);

	/**
	 * Higher-level line output method.  Does full
	 * filtering of special characters and codes.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrintln(String)
	 * @param msg the string to send to the user
	 */
	public void stdPrintln(String msg);

	/**
	 * Higher-level line output method.  Does full
	 * filtering of special characters and codes
	 * using given variable values.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#rawPrintln(String)
	 * @param Source variable for special code parsing: Source
	 * @param Target variable for special code parsing: Target
	 * @param Tool variable for special code parsing: Tool
	 * @param msg the string to send to the user
	 */
	public void stdPrintln(Physical Source,
						   Environmental Target,
						   Environmental Tool,
						   String msg);

	/**
	 * Lowest level user-output method.  Does nothing
	 * but send the string to the user, period.
	 * @param c string (as char array) to send out to the user
	 */
	public void rawCharsOut(char[] c);

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
	 * Returns whether this session is engaged in a login/account
	 * menu transaction.  If a loginsession is passed it, it will
	 * return true only if the two sessions are engaging the
	 * same login name.
	 * @param otherLoginName another login name potentially match
	 * @return true if it is pending, false otherwise
	 */
	public boolean isPendingLogin(final String otherLoginName);

	/**
	 * If the session is in a non-logged in state (or even if it
	 * is), this will automatically login the user with the given
	 * name and password, and switch the session to the main loop.
	 * Sending null for name/password will reset the login session.
	 * @param name the username
	 * @param password the password
	 * @return true if the login succeeded
	 */
	public boolean autoLogin(String name, String password);
	
	/**
	 * Medium-level text output method.  Does full
	 * filtering of special characters and codes.
	 * Does not manage the prompt, and should NOT be used for prompts.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#promptPrint(String)
	 * @param msg the string to send to the user
	 */
	public void print(String msg);

	/**
	 * Medium-level text output method.  Does full
	 * filtering of special characters and codes.
	 * Does not manage the prompt. Should be used for prompts!
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#onlyPrint(String, boolean)
	 * @param msg the string to send to the user
	 */
	public void promptPrint(String msg);

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
	public void print(Physical Source,
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
	public void println(Physical Source,
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
	 * Puts the session into an input state, returning immediately.  The
	 * given callback will be made when the ENTER key has been hit, and
	 * the session will go back into a prompt state.
	 * This is basically for non-blocking input.
	 * @see InputCallback
	 * @param callBack the callback to modify and make when done
	 */
	public void prompt(InputCallback callBack);

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
	public String prompt(final String Message, long maxTime)
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
	public boolean confirm(final String Message, final String Default)
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
	public boolean confirm(final String Message, final String Default, long maxTime)
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
	public String choose(final String Message, final String Choices, final String Default)
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
	 * @param maxTime max number of milliseconds to wait before timing out
	 * @param paramsOut an empty list to put any extra crap added to the end of the choice.
	 * @return the character entered from the choices
	 * @throws IOException a disconnect
	 */
	public String choose(final String Message, final String Choices, final String Default, long maxTime, List<String> paramsOut)
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
	public String choose(final String Message, final String Choices, final String Default, long maxTime)
		throws IOException;

	/**
	 * Notifies this session that the given session is snooping it.
	 * This session will manage said snooping.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setBeingSnoopedBy(Session, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#isBeingSnoopedBy(Session)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
	 * @param session the session to snoop on me.
	 * @param onOff true to turn on snooping, false otherwise
	 */
	public void setBeingSnoopedBy(Session session, boolean onOff);

	/**
	 * Checks to see if the given session is snooping on this one.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setBeingSnoopedBy(Session, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#isBeingSnoopedBy(Session)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
	 * @param S the session to check for a snoop on me.
	 * @return true if the given session is snooping on me, false otherwise
	 */
	public boolean isBeingSnoopedBy(Session S);

	/**
	 * Increments or decrements the snoop suspension counter
	 * by the given amount.  Only when the counter is 0 does
	 * snooping of this session actually occur.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setBeingSnoopedBy(Session, boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#isBeingSnoopedBy(Session)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#snoopSuspension(int)
	 * @param change the amount to change the snoop suspension counter by
	 * @return the current value of the snoop suspension counter after the change
	 */
	public int snoopSuspension(int change);

	/**
	 * Force the current player to logoff, end the session, and/or kill the thread.
	 * @param removeMOB true to remove the mob from the game
	 * @param dropSession true to force closed sockets, and removed session
	 * @param killThread true to force a thread death, and false to be more lenient
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#isStopped()
	 */
	public void stopSession(boolean removeMOB, boolean dropSession, boolean killThread);

	/**
	 * Returns whether this session is done, or slated to be done.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#logout(boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#isStopped()
	 * @return true if this session needs to go, false otherwise
	 */
	public boolean isStopped();

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
	public boolean isAfk();

	/**
	 * Sets whether this mob/session is currently Away From Keyboard
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#isAfk()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getAfkMessage()
	 * @param truefalse true if they are AFK, false otherwise
	 */
	public void setAfkFlag(boolean truefalse);

	/**
	 * Returns the reason given by the user that they are AFK.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAfkFlag(boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAFKMessage(String)
	 * @return  the reason given by the user that they are AFK.
	 */
	public String getAfkMessage();

	/**
	 * Returns the reason given by the user that they are AFK.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setAfkFlag(boolean)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getAfkMessage()
	 * @param str the reason given by the user that they are AFK.
	 */
	public void setAFKMessage(String str);

	/**
	 * Blocks the current thread until the user attached to this session
	 * hits ENTER, returning the characters they enter.  Completely filtered input.
	 * @param timeoutMillis milliseconds before InterruptedIOException thrown
	 * @param filter true to allow normal coffeemud input filtering
	 * @return the string entered by the user
	 * @throws IOException any exception generated during input
	 */
	public String blockingIn(long timeoutMillis, boolean filter)
		throws IOException;

	/**
	 * Blocks for a short amount of time, returning an input
	 * string only if the user happens to have hit enter.
	 * @return a string entered by the user
	 * @throws IOException exceptions thrown, typically a timeout
	 * @throws SocketException exceptions thrown, typically socket error
	 */
	public String readlineContinue()
		throws IOException, SocketException;

	/**
	 * Returns a pre-parsed, pre-filtered list of strings
	 * representing the last command entered by the user
	 * through this session.
	 * @return a list of strings
	 */
	public List<String> getPreviousCMD();

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
	 * @param account the player account attached to this session object.
	 */
	public void setAccount(PlayerAccount account);

	/**
	 * Returns the current color code.
	 * @return the current color code.
	 */
	public ColorState getCurrentColor();

	/**
	 * Sets the current color code.
	 * @param newcolor the color to change it to
	 */
	public void setCurrentColor(final ColorState newcolor);

	/**
	 * Returns the previous current color code.
	 * @return the previous current color code.
	 */
	public ColorState getLastColor();

	/**
	 * Returns the previous current color code.
	 * @param newColor the color to change it to
	 */
	public void setLastColor(final ColorState newColor);

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
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus
	 * @return the tick status
	 */
	public SessionStatus getStatus();

	/**
	 * Sets the tick/thread status of this session object.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus
	 * @param newStatus the tick status
	 */
	public void setStatus(SessionStatus newStatus);

	/**
	 * Returns whether this session is waiting for input
	 *
	 * @return true if it is, false otherwise
	 */
	public boolean isWaitingForInput();

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
	 * @see Session#setIdleTimers()
	 * @return the idle milliseconds passed
	 */
	public long getIdleMillis();

	/**
	 * Resets the internal idle timers.
	 * @see Session#getIdleMillis()
	 */
	public void setIdleTimers();

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
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#setInputLoopTime()
	 *
	 * @return the last time in milliseconds that this session began its input loop
	 */
	public long getInputLoopTime();

	/**
	 * Sets the last time in milliseconds that this session began its input loop.
	 * Is typically only held up by executing a user command, so it is an accurate
	 * gauge of a locked up user command.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getInputLoopTime()
	 */
	public void setInputLoopTime();

	/**
	 * Whether this session is currently actively interacting with the user in
	 * some way -- whether this session currently has "thread time".
	 * @return true if its active and running, false otherwise
	 */
	public boolean isRunning();

	/**
	 * Returns a list of the last several message strings received by this user.
	 * All are already previously filtered and parsed and ready to display.
	 *
	 * @return a list of string message strings
	 */
	public List<String> getLastMsgs();

	/**
	 * Gets the terminal type the user has
	 * @return the terminal type
	 */
	public String getTerminalType();

	/**
	 * Sets a server telnet mode flag.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getServerTelnetMode(int)
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
	public boolean getServerTelnetMode(int telnetCode);

	/**
	 * Sets a client telnet mode flag.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#TELNET_ANSI
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getClientTelnetMode(int)
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
	public boolean getClientTelnetMode(int telnetCode);

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
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#getAttributesBitmap()
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#changeTelnetMode(int, boolean)
	 * @param mobbitmap the mobbitmap the bitmap of mob flags to use as a guide in negotiation
	 */
	public void initTelnetMode(int mobbitmap);

	/**
	 * Returns true if the given max tag will be accepted by the client.
	 * @param tag the tag to check
	 * @return true if allowed, false otherwise
	 */
	public boolean isAllowedMxp(String tag);

	/**
	 * Returns true if this client supports the given package at the
	 * given version.
	 * @param packageName the package to check
	 * @param version the version of the package to check
	 * @return true if there's support, false otherwise
	 */
	public boolean isAllowedMcp(String packageName, float version);
	
	/**
	 * Sends a properly formatted MCP command to the session, if it or its package is supported.
	 * @param packageCommand the fully formed command
	 * @param parms the variable parameters, already well formed
	 * @return true if it was sent, false otherwise
	 */
	public boolean sendMcpCommand(String packageCommand, String parms);
	
	/**
	 * Potentially sends the GMCP event of the given name with the given json
	 * doc.
	 * @param eventName the event name, like comm.channel
	 * @param json the json doc, like {"blah":"BLAH"}
	 */
	public void sendGMCPEvent(final String eventName, final String json);

	/**
	 * Send this session fake input as if the user had typed it in.
	 * @param input text to send.
	 */
	public void setFakeInput(String input);

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
	/** TELNET CODE: MSDP protocol*/
	public static final int TELNET_MSDP=69;
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
	/** TELNET CODE: AARD protocol*/
	public static final int TELNET_AARD=102;
	/** TELNET CODE: End of subnegotiation parameters*/
	public static final int TELNET_SE=240;
	/** TELNET CODE: Are You There*/
	public static final int TELNET_AYT=246;
	/** TELNET CODE: Erase character*/
	public static final int TELNET_EC=247;
	/** TELNET CODE: ATCP protocol*/
	public static final int TELNET_ATCP=200;
	/** TELNET CODE: GMCP protocol*/
	public static final int TELNET_GMCP=201;
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
	/** TELNET CODE: Indicates that the other party can go ahead and transmit -- I'm done.*/
	public static final int TELNET_GA=249;
	/** TELNET CODE: Indicates that there is nothing to do?*/
	public static final int TELNET_NOP=241;
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
		"60","61","62","63","64","65","66","67","68","MSDP", //60-69
		"MSSP","71","72","73","74","75","76","77","78","79", //70-79
		"","","","","","COMPRESS","COMPRESS2","","","", //80-89
		"MSP","MXP","","","","","","","","", //90-99
		"","","AARD","","","","","","","", //100-109
		"","","","","","","","","","", //110-119
		"","","","","","","","","","", //120-129
		"","","","","","","","","","", //130-139
		"","","","","","","","","","", //140-149
		"","","","","","","","","","", //150-159
		"","","","","","","","","","", //160-169
		"","","","","","","","","","", //170-179
		"","","","","","","","","","", //180-189
		"","","","","","","","","","", //190-199
		"ATCP","GMCP","","","","","","","","", //200-209
		"","","","","","","","","","", //210-219
		"","","","","","","","","","", //220-229
		"","","","","","","","","","", //230-239
		"SE","NOP","","","","","AYT","EC","","GA", //240-249
		"SB","","","ANSI","","" 			 //250-255
	};

	/* The charset recognized by MSDP is unknown, so assume std ascii */
	public final static String MSDP_CHARSET = "US-ASCII";

	/** How to start a GMCP subnegotiation */
	public final static byte[] TELNETBYTES_GMCP_HEAD= new byte[]{(byte)Session.TELNET_IAC,(byte)Session.TELNET_SB,(byte)Session.TELNET_GMCP};
	/** How to end a telnet subnegotiation */
	public final static byte[] TELNETBYTES_END_SB	= new byte[]{(byte)Session.TELNET_IAC,(byte)Session.TELNET_SE};
	/** Go ahead bytes */
	public final static byte[] TELNETGABYTES		= {(byte)TELNET_IAC,(byte)TELNET_GA};

	/**
	 * The internal class to managing asynchronous user input.
	 * This class supports three types of input: open text (PROMPT),
	 * one-letter options (CHOOSE), and Y/N (CONFIRM).
	 * @author Bo Zimmerman
	 *
	 */
	public static abstract class InputCallback
	{
		/**
		 * The threa different types of user input processing
		 * supported by the abstract InputCallback class
		 * @author Bo Zimmerman
		 *
		 */
		public static enum Type { CONFIRM, PROMPT, CHOOSE, WAIT }

		private final Type			type;
		private final String		defaultInput;
		private final String		choicesStr;
		protected final long		timeoutMs;
		protected volatile long		timeout;
		protected volatile String	input		= "";
		protected volatile boolean	confirmed	= false;
		protected volatile boolean	waiting		= true;
		protected volatile boolean	noTrim		= false;

		/**
		 * Full constructor.  Receives the Type of processing, a default input for
		 * when the user just hits ENTER, a list of one-character choices (if CHOOSE Type
		 * is used) and an amount of time, in ms, for the user to be given before timeout
		 * and the timedOut() method is called.
		 * @param type the type of processing
		 * @param defaultInput default input value
		 * @param choicesStr list of one-character options (if CHOOSE Type)
		 * @param timeoutMs time, in ms, before the user is kicked
		 */
		public InputCallback(Type type, String defaultInput, String choicesStr, long timeoutMs)
		{
			this.type=type;
			if((choicesStr!=null)&&(choicesStr.trim().length()>0))
				this.choicesStr=choicesStr.toUpperCase().trim();
			else
			switch(type)
			{
				case CONFIRM:
					this.choicesStr = "YN";
					break;
				case CHOOSE:
					this.choicesStr = "YN";
					break;
				default:
					this.choicesStr = "";
			}
			this.timeoutMs=timeoutMs;
			if(this.timeoutMs<=0)
				this.timeout=0;
			else
				this.timeout=System.currentTimeMillis()+timeoutMs;
			this.waiting=true;
			this.defaultInput=defaultInput;
		}

		/**
		 * Constructor.  Receives the Type of processing, a default input for
		 * when the user just hits ENTER,  and an amount of time, in ms, for
		 * the user to be given before timeout and the timedOut() method is called.
		 * @param type the type of processing
		 * @param defaultInput default input value
		 * @param timeoutMs time, in ms, before the user is kicked
		 */
		public InputCallback(Type type, String defaultInput, long timeoutMs)
		{
			this(type, defaultInput, "", timeoutMs);
		}

		/**
		 * Constructor.  Receives the Type of processing, and an amount of time, in ms,
		 * for the user to be given before timeout and the timedOut() method is called.
		 * @param type the type of processing
		 * @param timeoutMs time, in ms, before the user is kicked
		 */
		public InputCallback(Type type, long timeoutMs)
		{
			this(type, "", timeoutMs);
		}

		/**
		 * Constructor.  Receives the Type of processing.
		 * @param type the type of processing
		 */
		public InputCallback(Type type)
		{
			this(type, 0);
		}

		/**
		 * Constructor.  Receives the Type of processing, a default input for
		 * when the user just hits ENTER.
		 * @param type the type of processing
		 * @param defaultInput default input value
		 */
		public InputCallback(Type type, String defaultInput)
		{
			this(type, "", 0);
		}

		/**
		 * Returns true if a timeout was given, and this class has been
		 * active longer than that amount of time.
		 * @return true if timed out, false otherwise.
		 */
		public boolean isTimedOut()
		{
			final boolean isTimedOut=(timeout > 0) && (System.currentTimeMillis() > timeout);
			if(isTimedOut)
				waiting=false;
			return isTimedOut;
		}

		/**
		 * Forces user-input into this class, potentially changing its user
		 * input waiting state.  If the input is invalid for CONFIRM or CHOOSE,
		 * then this will call ShowPrompt and go back to waiting. Otherwise,
		 * waiting is set to false and it becomes very likely that callBack()
		 * will be called.
		 * @param input the user input to force
		 */
		public void setInput(String input)
		{
			this.input=input;
			switch(type)
			{
			case PROMPT:
			{
				if((input.length()>0)&&(input.charAt(input.length()-1)=='\\'))
					this.input=input.substring(0,input.length()-1);
				else
					this.input=input;
				if((this.input.trim().length()==0)&&(!noTrim))
					this.input=defaultInput;
				waiting=false;
				break;
			}
			case WAIT:
				waiting=true;
				return;
			case CONFIRM:
				if(input.trim().toUpperCase().startsWith("T"))
					input="Y";
				//$FALL-THROUGH$
			case CHOOSE:
					this.input=input.toUpperCase().trim();
				if(this.input.length()==0)
					this.input=defaultInput.toUpperCase();
				if(this.input.length()>0)
				{
					this.input=this.input.substring(0,1);
					if((choicesStr.length()==0)
					||(choicesStr.indexOf(this.input)>=0))
					{
						waiting=false;
						if(type==Type.CONFIRM)
							this.confirmed=this.input.equals("Y");
					}
					else
					{
						showPrompt();
					}
				}
				break;
			}
			waiting=false;
		}

		/**
		 * Returns true if this class is currently waiting
		 * for user input.
		 * @return true if waiting, false if waiting is over.
		 */
		public boolean waitForInput()
		{
			return waiting;
		}

		/**
		 * This method allows reuse of a given InputCallback.
		 * It will re-start the timeout period, and flag
		 * the callback for requiring more input.
		 * @return this
		 */
		public InputCallback reset()
		{
			input="";
			if(timeoutMs>0)
				timeout=System.currentTimeMillis()+timeoutMs;
			waiting=true;
			confirmed=false;
			return this;
		}

		/**
		 * This method is called by InputCallback before user
		 * input is requested.  If a CHOOSE or CONFIRM type is
		 * used, and the user enters something unrecognized, then
		 * this method is called again before more input.
		 */
		public abstract void showPrompt();

		/**
		 * This method is call by InputCallback if a timeout
		 * value greater than 0 is given and that amount of time has
		 * been exceeded.
		 */
		public abstract void timedOut();

		/**
		 * This method is called if the user hits ENTER, and
		 * their input data is valid (one of the choices for
		 * CHOOSE or CONFIRM), or anything else for PROMPT.
		 * If the user has entered nothing, and a default
		 * value has been given, the default value is returned.
		 * The user entry is available to this method in
		 * this.input variable.
		 */
		public abstract void callBack();
	}

	public abstract class TickingCallback extends InputCallback
	{
		protected volatile int			counter			= 0;
		protected final StringBuilder	collectedInput	= new StringBuilder("");

		/**
		 * Only constructor is the one to tell out often to call back.
		 * @param tickerMs the time is ms between timeouts
		 */
		public TickingCallback(long tickerMs)
		{
			super(Type.PROMPT,tickerMs);
		}
		/**
		 * TimeOutCallback has no prompt
		 */
		@Override
		public void showPrompt()
		{
		}

		@Override
		public void callBack()
		{
		}

		@Override
		public void setInput(String input)
		{
			super.setInput(input);
			collectedInput.append(this.input).append("\n\r");
			input="";
			waiting=true;
		}

		@Override
		public void timedOut()
		{
			timeout=System.currentTimeMillis()+timeoutMs;
			waiting=true;
			if(!tick(counter++))
			{
				timeout=System.currentTimeMillis()-1;
				waiting=false;
			}
		}

		/**
		 * This method id called every ticker ms with an incremented
		 * counter.  This is also where you control the process.
		 * @param counter the counter, higher than the last time
		 * @return true to keep ticking, false to finally stop.
		 */
		public abstract boolean tick(int counter);
	}

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

	/** For MSDP protocol, denotes variable start*/
	public static final char MSDP_VAR			= 1;
	/** For MSDP protocol, denotes value start*/
	public static final char MSDP_VAL			= 2;
	/** For MSDP protocol, denotes table open*/
	public static final char MSDP_TABLE_OPEN	= 3;
	/** For MSDP protocol, denotes table done*/
	public static final char MSDP_TABLE_CLOSE	= 4;
	/** For MSDP protocol, denotes array start*/
	public static final char MSDP_ARRAY_OPEN	= 5;
	/** For MSDP protocol, denotes array done*/
	public static final char MSDP_ARRAY_CLOSE	= 6;

	/**
	 * The status of the session, from opening handshake, to final goodbyes
	 * @author Bo Zimmerman
	 */
	public static enum SessionStatus
	{
		HANDSHAKE_OPEN, HANDSHAKE_MCCP, HANDSHAKE_MXP, HANDSHAKE_MXPPAUSE, HANDSHAKE_DONE,
		LOGIN, LOGIN2, ACCOUNT_MENU, CHARCREATE, IDLE, MAINLOOP,
		LOGOUT, LOGOUT1, LOGOUT2, LOGOUT3, LOGOUT4, LOGOUT5, LOGOUT6, LOGOUT7, LOGOUT8,
		LOGOUT9, LOGOUT10, LOGOUT11, LOGOUT12, LOGOUTFINAL
	}
}
