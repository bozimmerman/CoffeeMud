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
import java.util.*;
/* 
   Copyright 2000-2014 Bo Zimmerman

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
public interface CharCreationLibrary extends CMLibrary
{
	public void reRollStats(MOB mob, CharStats C, int pointsLeft);
	public void promptPlayerStats(int theme, MOB mob, Session session, int bonusPoints) throws IOException;
	public CharClass promptCharClass(int theme, MOB mob, Session session) throws IOException;
	public int getTrainingCost(MOB mob, int abilityCode, boolean quiet);
	public boolean canChangeToThisClass(MOB mob, CharClass thisClass, int theme);
	// mob is optional
	public List<CharClass> classQualifies(MOB mob, int theme);
	// mob is optional
	public void moveSessionToCorrectThreadGroup(final Session session, int theme);
	public List<String> getExpiredList();
	public List<Race> raceQualifies(MOB mob, int theme);
	public boolean isOkName(String login, boolean spacesOk);
	public boolean isBadName(String login);
	public void reloadTerminal(MOB mob);
	public void showTheNews(MOB mob);
	public void notifyFriends(MOB mob, String message);
	public LoginResult createCharacter(PlayerAccount acct, String login, Session session) throws java.io.IOException;
	public LoginResult loginSystem(Session session, LoginSession loginObj) throws java.io.IOException;
	public NewCharNameCheckResult newCharNameCheck(String login, String ipAddress, boolean checkPlayerName);
	public NewCharNameCheckResult newAccountNameCheck(String login, String ipAddress);
	public void pageRooms(CMProps page, Map<String, String> table, String start);
	public void initStartRooms(CMProps page);
	public void initDeathRooms(CMProps page);
	public void initBodyRooms(CMProps page);
	public Room getDefaultStartRoom(MOB mob);
	public Room getDefaultDeathRoom(MOB mob);
	public Room getDefaultBodyRoom(MOB mob);
	public String generateRandomName(int minSyllable, int maxSyllable);
	
	public enum LoginState {LOGIN_START, LOGIN_NAME, LOGIN_ACCTCHAR_PWORD, LOGIN_PASS_START, LOGIN_NEWACCOUNT_CONFIRM, LOGIN_NEWCHAR_CONFIRM, 
							LOGIN_PASS_RECEIVED, LOGIN_EMAIL_PASSWORD, LOGIN_ACCTCONV_CONFIRM,
							ACCTMENU_COMMAND, ACCTMENU_PROMPT, ACCTMENU_CONFIRMCOMMAND, ACCTMENU_ADDTOCOMMAND,
							ACCTMENU_SHOWMENU, ACCTMENU_SHOWCHARS, ACCTMENU_START,
							ACCTCREATE_START, ACCTCREATE_ANSICONFIRM, ACCTCREATE_PASSWORDED,
							ACCTCREATE_EMAILSTART, ACCTCREATE_EMAILPROMPT, ACCTCREATE_EMAILENTERED, ACCTCREATE_EMAILCONFIRMED, 
							CHARCR_EMAILCONFIRMED, CHARCR_EMAILPROMPT, CHARCR_EMAILENTERED, CHARCR_EMAILSTART, CHARCR_EMAILDONE, 
							CHARCR_PASSWORDDONE, CHARCR_START, CHARCR_ANSIDONE, CHARCR_ANSICONFIRMED, CHARCR_THEMEDONE, 
							CHARCR_THEMEPICKED, CHARCR_THEMESTART, CHARCR_GENDERSTART, CHARCR_GENDERDONE,
							CHARCR_RACEDONE, CHARCR_RACESTART, CHARCR_RACEENTERED, CHARCR_RACECONFIRMED, 
							CHARCR_STATDONE, CHARCR_STATSTART, CHARCR_STATCONFIRM, CHARCR_STATPICK, CHARCR_STATPICKADD,
							CHARCR_CLASSSTART, CHARCR_CLASSDONE, CHARCR_CLASSPICKED, CHARCR_CLASSCONFIRM, 
							CHARCR_FACTIONNEXT, CHARCR_FACTIONDONE, CHARCR_FACTIONPICK, 
							CHARCR_FINISH
							}
	
	public static class LoginSession
	{
		public boolean 		 wizi	   = false;
		public boolean		 reset	   = false;
		public boolean		 skipInput = false;
		public LoginState 	 state	   = LoginState.LOGIN_START;
		public String 		 login	   = null;
		public PlayerAccount acct 	   = null;
		public String 		 lastInput = null;
		public String		 savedInput= null;
		public String 		 password  = null;
		public int			 attempt   = 0;
		public MOB			 mob	   = null;
		public int			 theme	   = -1;
		public int			 statPoints= 0;
		public CharStats	 baseStats = null;
		public int			 index	   = 0;
		public PlayerLibrary.ThinnerPlayer player = null;
	}
	
	public enum NewCharNameCheckResult { OK, NO_NEW_PLAYERS, NO_NEW_LOGINS, BAD_USED_NAME, CREATE_LIMIT_REACHED }
	
	public final static String[] DEFAULT_BADNAMES = new String[]{"LIST","DELETE","QUIT","NEW","HERE","YOU","SHIT","FUCK","CUNT","FAGGOT","ASSHOLE","NIGGER","ARSEHOLE","PUSSY","COCK","SLUT","BITCH","DAMN","CRAP","GOD","JESUS","CHRIST","NOBODY","SOMEBODY","MESSIAH","ADMIN","SYSOP"};
	
	public enum LoginResult
	{
		NO_LOGIN, NORMAL_LOGIN, INPUT_REQUIRED
	}
}
