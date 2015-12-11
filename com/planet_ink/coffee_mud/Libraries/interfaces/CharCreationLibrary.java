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
   Copyright 2006-2015 Bo Zimmerman

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
	 * @param theme the theme code to use for stat allocation
	 * @param mob the mob who is getting the new char stats
	 * @param session the session which might help allocate the points
	 * @param bonusPoints any bonus points to allocate to stats
	 * @throws IOException any input errors that occur
	 */
	public void promptPlayerStats(int theme, MOB mob, Session session, int bonusPoints) throws IOException;
	
	/**
	 * A blocking call that populates the given mob with a character class
	 * according to character creation rules.  This method
	 * might return instantly, or it might send the given session
	 * through the class selection process.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharStats
	 * @see CharCreationLibrary#reRollStats(CharStats, int)
	 * @see CharCreationLibrary#promptPlayerStats(int, MOB, Session, int)
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area#THEME_FANTASY
	 * @param theme the theme code to use for class selection
	 * @param mob the mob who is getting the new char class
	 * @param session the session which might help allocate the points
	 * @throws IOException any input errors that occur
	 */
	public CharClass promptCharClass(int theme, MOB mob, Session session) throws IOException;
	
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
	 * Returns the list of all races that the given mob can choose
	 * into, given their currrent state, and the given theme.  The mob is
	 * optional, at which point it is only checking base race rules and theme.
	 * @see CharCreationLibrary#canChangeToThisClass(MOB, CharClass, int)
	 * @param mob the mob who wants to choose races
	 * @param theme the theme to filter the races by
	 * @return the list of races that the mob may choose.
	 */
	public List<Race> raceQualifies(MOB mob, int theme);
	
	/**
	 * Returns whether the given name is a valid,  legitimate, 
	 * unused, unbanned, non-bad name to use in coffeemud, for accounts
	 * or players. 
	 * @see CharCreationLibrary#isBadName(String)
	 * @param login the name to test
	 * @param spacesOk true if spaces in the name are ok, false otherwise
	 * @return true if the name is ok, false otherwise 
	 */
	public boolean isOkName(String login, boolean spacesOk);
	
	/**
	 * Returns only whether the given name has a bad word in it.
	 * @see CharCreationLibrary#isOkName(String, boolean)
	 * @param login the name to test
	 * @return true if it has a bad name, false otherwise
	 */
	public boolean isBadName(String login);
	
	/**
	 * Resets the MXP, MSP and other session flags based on the mobs
	 * attributes.  Typically done at sign-on only.
	 * @param mob the mob whose session needs to match his 
	 */
	public void reloadTerminal(MOB mob);
	public void showTheNews(MOB mob);
	public void notifyFriends(MOB mob, String message);
	public LoginResult createCharacter(PlayerAccount acct, String login, Session session) throws java.io.IOException;
	public NewCharNameCheckResult newCharNameCheck(String login, String ipAddress, boolean checkPlayerName);
	public NewCharNameCheckResult newAccountNameCheck(String login, String ipAddress);
	public void pageRooms(CMProps page, Map<String, String> table, String start);
	public void initStartRooms(CMProps page);
	public void initDeathRooms(CMProps page);
	public void initBodyRooms(CMProps page);
	public Room getDefaultStartRoom(MOB mob);
	public Room getDefaultDeathRoom(MOB mob);
	public Room getDefaultBodyRoom(MOB mob);
	public int getTotalBonusStatPoints();
	public String generateRandomName(int minSyllable, int maxSyllable);
	public LoginResult completeLogin(final Session session, final MOB mob, final Room startRoom, final boolean resetStats) throws IOException;
	public LoginSession createLoginSession(final Session session);
	
	public interface LoginSession
	{
		public String login();
		
		public LoginResult loginSystem(Session session) throws java.io.IOException;
		
		public void logoutLoginSession();
		
		public boolean reset();
		
		public boolean skipInputThisTime();
		
		public String acceptInput(Session session)  throws SocketException, IOException;
	}

	public enum LoginResult
	{
		NO_LOGIN,
		NORMAL_LOGIN,
		INPUT_REQUIRED
	}

	public enum NewCharNameCheckResult
	{
		OK,
		NO_NEW_PLAYERS,
		NO_NEW_LOGINS,
		BAD_USED_NAME,
		CREATE_LIMIT_REACHED
	}

	public enum LoginState
	{
		LOGIN_START,
		LOGIN_NAME,
		LOGIN_ACCTCHAR_PWORD,
		LOGIN_PASS_START,
		LOGIN_NEWACCOUNT_CONFIRM,
		LOGIN_NEWCHAR_CONFIRM,
		LOGIN_PASS_RECEIVED,
		LOGIN_EMAIL_PASSWORD,
		LOGIN_ACCTCONV_CONFIRM,
		ACCTMENU_COMMAND,
		ACCTMENU_PROMPT,
		ACCTMENU_CONFIRMCOMMAND,
		ACCTMENU_ADDTOCOMMAND,
		ACCTMENU_SHOWMENU,
		ACCTMENU_SHOWCHARS,
		ACCTMENU_START,
		ACCTCREATE_START,
		ACCTCREATE_ANSICONFIRM,
		ACCTCREATE_PASSWORDED,
		ACCTCREATE_EMAILSTART,
		ACCTCREATE_EMAILPROMPT,
		ACCTCREATE_EMAILENTERED,
		ACCTCREATE_EMAILCONFIRMED,
		CHARCR_EMAILCONFIRMED,
		CHARCR_EMAILPROMPT,
		CHARCR_EMAILENTERED,
		CHARCR_EMAILSTART,
		CHARCR_EMAILDONE,
		CHARCR_PASSWORDDONE,
		CHARCR_START,
		CHARCR_ANSIDONE,
		CHARCR_ANSICONFIRMED,
		CHARCR_THEMEDONE,
		CHARCR_THEMEPICKED,
		CHARCR_THEMESTART,
		CHARCR_GENDERSTART,
		CHARCR_GENDERDONE,
		CHARCR_RACEDONE,
		CHARCR_RACESTART,
		CHARCR_RACEENTERED,
		CHARCR_RACECONFIRMED,
		CHARCR_STATDONE,
		CHARCR_STATSTART,
		CHARCR_STATCONFIRM,
		CHARCR_STATPICK,
		CHARCR_STATPICKADD,
		CHARCR_CLASSSTART,
		CHARCR_CLASSDONE,
		CHARCR_CLASSPICKED,
		CHARCR_CLASSCONFIRM,
		CHARCR_FACTIONNEXT,
		CHARCR_FACTIONDONE,
		CHARCR_FACTIONPICK,
		CHARCR_FINISH
	}

	public final static String[] DEFAULT_BADNAMES = new String[]
	{
		"LIST","DELETE","QUIT","NEW","HERE","YOU","SHIT","FUCK","CUNT",
		"FAGGOT","ASSHOLE","NIGGER","ARSEHOLE","PUSSY", "COCK","SLUT",
		"BITCH","DAMN","CRAP","GOD","JESUS","CHRIST","NOBODY","SOMEBODY",
		"MESSIAH","ADMIN","SYSOP"
	};

}
