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

import java.io.IOException;
import java.net.SocketException;
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
 * This library manages everything related to 
 * logging a player or account in, 
 * creating a character, 
 * the account menu, 
 * certain login procedures, 
 * the rules around player names, 
 * start rooms, death rooms, and morgue rooms.
 * @author Bo Zimmerman
 *
 */
public interface CharCreationLibrary extends CMLibrary
{
	/**
	 * Re-populates the base stats of the given CharStats
	 * object by resetting the values to minimum, and then
	 * adding as many of the given points to random stats
	 * until they are all gone.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @param baseCharStats the charstats object to populate
	 * @param pointsLeft the number of points above minimum to allocate
	 */
	public void reRollStats(CharStats baseCharStats, int pointsLeft);

	/**
	 * A blocking call that populates the given mob with their base
	 * CharStats according to character creation rules.  This method
	 * might return instantly, or it might send the given session
	 * through the stat selection process.  The bonusPoints are any
	 * over and above the standard allocation points.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @see CharCreationLibrary#reRollStats(CharStats, int)
	 * @see CharCreationLibrary#promptCharClass(int, MOB, Session)
	 * @see CharCreationLibrary#promptRace(int, MOB, Session)
	 * @see CharCreationLibrary#promptGender(int, MOB, Session)
	 * @param theme the theme code to use for stat allocation
	 * @param mob the mob who is getting the new char stats
	 * @param timeoutSecs number of seconds before prompt times out
	 * @param session the session which might help allocate the points
	 * @param bonusPoints any bonus points to allocate to stats
	 * @throws IOException any input errors that occur
	 */
	public void promptBaseCharStats(int theme, MOB mob, int timeoutSecs, Session session, int bonusPoints) throws IOException;

	/**
	 * A blocking call that populates the given mob with a character class
	 * according to character creation rules.  This method
	 * might return instantly, or it might send the given session
	 * through the class selection process.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see CharCreationLibrary#reRollStats(CharStats, int)
	 * @see CharCreationLibrary#promptBaseCharStats(int, MOB, int, Session, int)
	 * @see CharCreationLibrary#promptRace(int, MOB, Session)
	 * @see CharCreationLibrary#promptGender(int, MOB, Session)
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @param theme the theme code to use for class selection
	 * @param mob the mob who is getting the new char class
	 * @param session the session which might help allocate the points
	 * @return null or the char class selected
	 * @throws IOException any input errors that occur
	 */
	public CharClass promptCharClass(int theme, MOB mob, Session session) throws IOException;

	/**
	 * A blocking call that populates the given mob with a particular race
	 * according to character creation rules.  This method
	 * might return instantly, or it might send the given session
	 * through the race selection process.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see CharCreationLibrary#reRollStats(CharStats, int)
	 * @see CharCreationLibrary#promptBaseCharStats(int, MOB, int, Session, int)
	 * @see CharCreationLibrary#promptCharClass(int, MOB, Session)
	 * @see CharCreationLibrary#promptGender(int, MOB, Session)
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @param theme the theme code to use for race selection
	 * @param mob the mob who is getting the new race
	 * @param session the session which might help get the race
	 * @return the race selected by this process
	 * @throws IOException any input errors that occur
	 */
	public Race promptRace(int theme, MOB mob, Session session) throws IOException;

	/**
	 * A blocking call that populates the given mob with a particular gender
	 * according to character creation rules.  This method goes
	 * through the gender selection process.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see CharCreationLibrary#reRollStats(CharStats, int)
	 * @see CharCreationLibrary#promptBaseCharStats(int, MOB, int, Session, int)
	 * @see CharCreationLibrary#promptCharClass(int, MOB, Session)
	 * @see CharCreationLibrary#promptRace(int, MOB, Session)
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @param theme the theme code to use for gender selection
	 * @param mob the mob who is getting the new gender
	 * @param session the session which might help get the gender
	 * @return the gender selected by this process
	 * @throws IOException any input errors that occur
	 */
	public char promptGender(int theme, MOB mob, Session session) throws IOException;

	/**
	 * Returns the cost, in trains, for the given mob to gain a point in the
	 * given ability code stat number.   
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @param mob the mob who is trying to train
	 * @param abilityCode the ability code the mob wants to train
	 * @param quiet true to not give verbal errors
	 * @return negative number for error, or the number of trains required
	 */
	public int getTrainingCost(MOB mob, int abilityCode, boolean quiet);

	/**
	 * Returns whether the given mob can change to the given class (that is,
	 * to become level 0 in that class) in the given theme.  The mob is
	 * optional, at which point it is only checking base rules and theme.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @see CharCreationLibrary#classQualifies(MOB, int)
	 * @param mob null or the mob who wants to learn a new class
	 * @param thisClass the class that the mob wants to learn
	 * @param theme the theme defining which classes are available
	 * @return true if the mob can change to the class, false otherwise
	 */
	public boolean canChangeToThisClass(MOB mob, CharClass thisClass, int theme);

	/**
	 * Returns the list of all character classes that the given mob can change
	 * into, given their currrent state, and the given theme.  The mob is
	 * optional, at which point it is only checking base class rules and theme.
	 * @see CharCreationLibrary#canChangeToThisClass(MOB, CharClass, int)
	 * @param mob the mob who wants to change classes
	 * @param theme the theme to filter the classes by
	 * @return the list of classes that the mob may change to.
	 */
	public List<CharClass> classQualifies(MOB mob, int theme);

	/**
	 * Returns the list of the names of all the expired Accounts
	 * or Characters, depending on whether the account system
	 * is used or not.  This only matters when the expiration
	 * system is being used, such as on a pay system
	 * @return the list of the names of all the expired
	 */
	public List<String> getExpiredAcctOrCharsList();

	/**
	 * Returns whether the given race may be selected by a user.
	 * @param R the Race to check
	 * @return true if it's selectable, false otherwise
	 */
	public boolean isAvailableRace(Race R);

	/**
	 * Returns whether the given character class may be selected by a user.
	 * @param C the CharClass to check
	 * @return true if it's selectable, false otherwise
	 */
	public boolean isAvailableCharClass(CharClass C);

	/**
	 * Returns the list of all races that the given mob can choose
	 * into, given their currrent state, and the given theme.  The mob is
	 * optional, at which point it is only checking base race rules and theme.
	 * @param theme the theme to filter the races by
	 * @see CharCreationLibrary#canChangeToThisClass(MOB, CharClass, int)
	 * @return the list of races that the mob may choose.
	 */
	public List<Race> raceQualifies(int theme);

	/**
	 * Returns whether the given name is a valid,  legitimate, 
	 * unused, unbanned, non-bad name to use in coffeemud, for accounts
	 * or players. 
	 * @see CharCreationLibrary#isBadName(String)
	 * @see CharCreationLibrary#isOkName(String, boolean)
	 * @see CharCreationLibrary#newCharNameCheck(String, String, boolean)
	 * @see CharCreationLibrary#newAccountNameCheck(String, String)
	 * @param login the name to test
	 * @param spacesOk true if spaces in the name are ok, false otherwise
	 * @return true if the name is ok, false otherwise 
	 */
	public boolean isOkName(String login, boolean spacesOk);

	/**
	 * Returns only whether the given name has a bad word in it.
	 * @see CharCreationLibrary#isOkName(String, boolean)
	 * @see CharCreationLibrary#newCharNameCheck(String, String, boolean)
	 * @see CharCreationLibrary#newAccountNameCheck(String, String)
	 * @param login the name to test
	 * @return true if it has a bad name, false otherwise
	 */
	public boolean isBadName(String login);

	/**
	 * Checks whether a character with the given login name from the
	 * given ipAddress may be created at this time.
	 * @see CharCreationLibrary#isBadName(String)
	 * @see CharCreationLibrary#isOkName(String, boolean)
	 * @see CharCreationLibrary#newAccountNameCheck(String, String)
	 * @see CharCreationLibrary.NewCharNameCheckResult
	 * @param login the name to check
	 * @param ipAddress the ip address of the name checker
	 * @param skipAccountNameCheck true to ignore account name matches
	 * @return the results of the new character name check.
	 */
	public NewCharNameCheckResult newCharNameCheck(String login, String ipAddress, boolean skipAccountNameCheck);
	
	/**
	 * Checks whether an account with the given login name from the
	 * given ipAddress may be created at this time.
	 * @see CharCreationLibrary#isBadName(String)
	 * @see CharCreationLibrary#isOkName(String, boolean)
	 * @see CharCreationLibrary#newCharNameCheck(String, String, boolean)
	 * @param login the name to check
	 * @param ipAddress the ip address of the name checker
	 * @return the results of the new account name check.
	 */
	public NewCharNameCheckResult newAccountNameCheck(String login, String ipAddress);
	
	/**
	 * Resets the MXP, MSP and other session flags based on the mobs
	 * attributes.  Typically done at sign-on only.
	 * @see CharCreationLibrary#showTheNews(MOB)
	 * @param mob the mob whose session needs to match his 
	 */
	public void reloadTerminal(MOB mob);

	/**
	 * Resets the terminal to the given mobs specs, shows
	 * any available polls, the daily message, and runs
	 * the MOTD.
	 * @see CharCreationLibrary#reloadTerminal(MOB)
	 * @param mob the mob to show these things to
	 */
	public void showTheNews(MOB mob);
	
	/**
	 * If any of the given mobs friends are online, they are sent the
	 * given message.
	 * @param mob the mob whose friends need notifying
	 * @param message the message to send to the mobs friends.
	 */
	public void notifyFriends(MOB mob, String message);
	
	/**
	 * Attempts to send the given session through the character creation process,
	 * at the end of which a character with the given login as name will be
	 * in the database, ready to load.
	 * @see CharCreationLibrary.LoginResult
	 * @param login the name of the new character
	 * @param session the session of the character creating person
	 * @return the results of the effort
	 * @throws java.io.IOException an i/o error with the telnet session
	 */
	public LoginResult createCharacter(String login, Session session) throws java.io.IOException;
	
	/**
	 * Initialize the rules for determining the new character start/recall room 
	 * given the characteristics of the player.
	 * @see CharCreationLibrary#initDeathRooms(CMProps)
	 * @see CharCreationLibrary#initBodyRooms(CMProps)
	 * @see CharCreationLibrary#getDefaultStartRoom(MOB)
	 * @param page the properties containing info about the start rooms
	 */
	public void initStartRooms(CMProps page);
	
	/**
	 * Initialize the rules for determining the new character death room 
	 * given the characteristics of the player.
	 * @see CharCreationLibrary#initStartRooms(CMProps)
	 * @see CharCreationLibrary#initBodyRooms(CMProps)
	 * @see CharCreationLibrary#getDefaultDeathRoom(MOB)
	 * @param page the properties containing info about the death rooms
	 */
	public void initDeathRooms(CMProps page);
	
	/**
	 * Initialize the rules for determining the new character morgue room 
	 * given the characteristics of the player.
	 * @see CharCreationLibrary#initStartRooms(CMProps)
	 * @see CharCreationLibrary#initDeathRooms(CMProps)
	 * @see CharCreationLibrary#getDefaultBodyRoom(MOB)
	 * @param page the properties containing info about the morgue rooms
	 */
	public void initBodyRooms(CMProps page);
	
	/**
	 * Given the characteristics of the given mob, this method returns
	 * the appropriate start/recall room for the given mob.
	 * @see CharCreationLibrary#initStartRooms(CMProps)
	 * @see CharCreationLibrary#getDefaultDeathRoom(MOB)
	 * @see CharCreationLibrary#getDefaultBodyRoom(MOB)
	 * @param mob the mob who needs to know their start room
	 * @return the start room for the given mob
	 */
	public Room getDefaultStartRoom(MOB mob);
	
	/**
	 * Given the characteristics of the given mob, this method returns
	 * the appropriate death room for the given mob.
	 * @see CharCreationLibrary#initDeathRooms(CMProps)
	 * @see CharCreationLibrary#getDefaultStartRoom(MOB)
	 * @see CharCreationLibrary#getDefaultBodyRoom(MOB)
	 * @param mob the mob who needs to know their death room
	 * @return the death room for the given mob
	 */
	public Room getDefaultDeathRoom(MOB mob);
	
	/**
	 * Given the characteristics of the given mob, this method returns
	 * the appropriate morgue room for the given mob.
	 * @see CharCreationLibrary#initBodyRooms(CMProps)
	 * @see CharCreationLibrary#getDefaultStartRoom(MOB)
	 * @see CharCreationLibrary#getDefaultDeathRoom(MOB)
	 * @param mob the mob who needs to know their morgue room
	 * @return the morgue room for the given mob
	 */
	public Room getDefaultBodyRoom(MOB mob);
	
	/**
	 * Based on the rules of the system, this method returns the number of
	 * bonus stat points available to players to allocate, if the system
	 * lets them do such a thing.
	 * @param playerStats The player stats object for the player
	 * @param account the player account object for the player, if applicable
	 * @return the number of stat points available to allocate
	 */
	public int getTotalBonusStatPoints(PlayerStats playerStats, PlayerAccount account);
	
	/**
	 * Returns a random fantasy name with the range of syllables given.
	 * @param minSyllable the minimum number of syllables, at least 1
	 * @param maxSyllable the maximum number of syllables, at least minimum
	 * @return a random fansty name
	 */
	public String generateRandomName(int minSyllable, int maxSyllable);
	
	/**
	 * Completes the given session and mobs login by putting the mob into the given start room
	 * in the world, checking their email, and seeing if they are allowed in.
	 * @see CharCreationLibrary.LoginResult
	 * @param session the session trying to login
	 * @param mob the mob trying to log in
	 * @param startRoom the room they will appear in
	 * @param resetStats true to reset their state (hit points, etc) or false to keep as-was
	 * @return the LoginResult status of having tried to complete their login
	 * @throws IOException any I/O errors during the process
	 */
	public LoginResult finishLogin(final Session session, final MOB mob, final Room startRoom, final boolean resetStats) throws IOException;

	/**
	 * Does a connection spam check against the given address, returning true if all
	 * is well, and false if it needs blocking.
	 * @param address the address to check
	 * @return true to proceed, false to block
	 */
	public boolean performSpamConnectionCheck(final String address);

	/**
	 * Takes the given session and mobs login by putting the mob into the given start room
	 * in the world, checking their email, and seeing if they are allowed in.  It does the
	 * complete login
	 * @param session the session trying to login, with the mob to login
	 * @param wizi true if the player should login wizinvisible
	 * @return the LoginResult status of having tried to complete their login
	 * @throws IOException any I/O errors during the process
	 */
	public LoginResult completePlayerLogin(final Session session, boolean wizi) throws IOException;
	
	/**
	 * Creates a new Login Session for the given Session, which will start the login state machine process
	 * that will end eventually with either a disconnect or a character logged in.
	 * @param session the telnet session trying to login
	 * @return the new session object, giving all necessary access to the login state machine.
	 */
	public LoginSession createLoginSession(final Session session);
	
	/**
	 * This is the main login state machine transaction object.  It allows the telnet session getting
	 * thread and i/o time to repeatedly call into the LoginSession object it creates until the
	 * object reports that it is completely done.  
	 * A login session includes initial telnet negotiation, login prompts, the account menu, and
	 * all of character creation. 
	 * @author Bo Zimmerman
	 *
	 */
	public interface LoginSession
	{
		/**
		 * The login name received in the first prompt after connection.
		 * @return the login name previously received.
		 */
		public String login();
		
		/**
		 * Continues through the login state machine for the given session.
		 * What the session should do next depends on the result object
		 * sent back.
		 * @see CharCreationLibrary.LoginResult
		 * @param session the session trying to login
		 * @return the results of the latest state
		 * @throws java.io.IOException any I/O errors that occur
		 */
		public LoginResult loginSystem(Session session) throws java.io.IOException;
		
		/**
		 * Puts the session into a "logged out" state, which means either back
		 * to the initial prompt, or back to the account menu, depending.
		 */
		public void logoutLoginSession();
		
		/**
		 * Set to true whenever the loginsystem needs the session to basically
		 * start the whole state machine over by re-creating the LoginSession
		 * object and calling in again.  It is an "I give up!" flag from this
		 * session.
		 * @return true if its time for a new LoginSession
		 */
		public boolean reset();

		/**
		 * Returns true if the loginsystem needs the session to skip any input
		 * it was previously asked for and simply call right back into the
		 * state machine.  The is usually done with when one non-input state
		 * needs to force proceed to another non-input state.
		 * @return true to skip user input
		 */
		public boolean skipInputThisTime();
		
		/**
		 * This strange method calls back into the given session for input from
		 * the user, if any is available. In a stateless I/O system, usually there
		 * isn't, so null is returned.
		 * @param session the session to ask for input
		 * @return null if none yet available, or the fully ENTEREd user input.
		 * @throws SocketException a socket error that occurs
		 * @throws IOException some other I/O error that occurred
		 */
		public String acceptInput(Session session)  throws SocketException, IOException;
	}

	/**
	 * A response object from one of the login system methods, basically telling the caller
	 * something about the results of what was attempted.
	 * @author Bo Zimmerman
	 *
	 */
	public enum LoginResult
	{
		/** Nothing happened, try again */
		NO_LOGIN,
		/** Login completed, proceed */
		NORMAL_LOGIN,
		/** User input required before proceeding */
		INPUT_REQUIRED
	}

	/**
	 * A response object from one of the name checking methods, telling the caller some specifics
	 * about the attempt to create a new character by testing a new name.
	 * @author Bo Zimmerman
	 *
	 */
	public enum NewCharNameCheckResult
	{
		/** All is well, proceed */
		OK,
		/** Cannot create a character because new players aren't allowed */
		NO_NEW_PLAYERS,
		/** Cannot create a character because new logins aren't allowed */
		NO_NEW_LOGINS,
		/** Either the name was a bad one, or used before by someone else */
		BAD_USED_NAME,
		/** The limits on character creation have been reached for this player or ip*/
		CREATE_LIMIT_REACHED
	}
}
