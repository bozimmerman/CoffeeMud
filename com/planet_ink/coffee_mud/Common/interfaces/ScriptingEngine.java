package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.Scriptable;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
 * The interface for the main CoffeeMud scripting engine, which implements
 * a scripting engine descended from the old mud codebases of the 90's
 * usually called MOBPROG.  Its main features include easy to understand
 * event-oriented triggers, making all mud commands implicit scripting
 * commands.  It also includes methods for embedding javascript.
 *
 * @see com.planet_ink.coffee_mud.Behaviors.Scriptable
 */
public interface ScriptingEngine extends CMCommon, Tickable, MsgListener
{
	/**
	 * Executes a script in response to an event
	 * The scripts are formatted as a 2 dimensional DVector
	 * with the first row being the trigger information.  Each
	 * row consists of the String command, and a parsed String[]
	 * array as dimension 2.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.ScriptableResponse
	 * @param scripted the object that is scripted
	 * @param source the source of the event
	 * @param target the target of the event
	 * @param monster a mob representation of the scripted object
	 * @param primaryItem an item involved in the event
	 * @param secondaryItem a second item involved in the event
	 * @param script 2 dimensional DVector, the script to execute
	 * @param msg a string message associated with the event
	 * @param tmp miscellaneous local variables
	 * @return N/A
	 */
	public String execute(PhysicalAgent scripted,
						  MOB source,
						  Environmental target,
						  MOB monster,
						  Item primaryItem,
						  Item secondaryItem,
						  DVector script,
						  String msg,
						  Object[] tmp);

	/**
	 * Uses this scripting engines variable parsing system to replace
	 * any script variables $XXXX with their script determined values.
	 * This is a powerful mechanism for getting at the script functions
	 * in order to access stat data about specific objects, do math, etc.
	 *
	 * @param source the source of the event
	 * @param target the target of the event
	 * @param scripted the object that is scripted
	 * @param monster a mob representation of the scripted object
	 * @param primaryItem an item involved in the event
	 * @param secondaryItem a second item involved in the event
	 * @param msg a string message associated with the event
	 * @param tmp miscellaneous local variables
	 * @param varifyable the string to parse
	 * @return N/A
	 */
	public String varify(MOB source,
						 Environmental target,
						 PhysicalAgent scripted,
						 MOB monster,
						 Item primaryItem,
						 Item secondaryItem,
						 String msg,
						 Object[] tmp,
						 String varifyable);
	/**
	 * Forces any queued event responses to be immediately
	 * executed.
	 */
	public void dequeResponses();

	/**
	 * Creates a mob from the Tickable object sent, possibly saving it
	 * locally to this object for use later.  If the object is a mob,
	 * it returns the mob.  Otherwise, it makes a fake one.
	 * @param ticking the scripted object to make a fake mob out of
	 * @return a mob from a tickable
	 */
	public MOB getMakeMOB(Tickable ticking);

	/**
	 * Receives a string for evaluation by the eval function, and stores
	 * it as the first element in the given 2 dimensional string array.
	 * @param evaluable the eval expression
	 * @return EVAL the 1 dimensional array to hold the compiled eval
	 * @throws ScriptParseException a parse error
	 */
	public String[] parseEval(String evaluable) throws ScriptParseException;

	/**
	 * Evaluates a scripting function.  Is called by the execute command
	 * to resolve IF, WHILE, and similar expressions that utilize the MOBPROG
	 * functions.  The expressions are passed in as a String array stored
	 * in a single string array entry (for replacement) in element 0.
	 * @param scripted the object that is scripted
	 * @param source the source of the event
	 * @param target the target of the event
	 * @param monster a mob representation of the scripted object
	 * @param primaryItem an item involved in the event
	 * @param secondaryItem a second item involved in the event
	 * @param msg a string message associated with the event
	 * @param tmp miscellaneous local variables
	 * @param eval the pre-parsed expression
	 * @param startEval while line to start evaluating on.
	 * @return true if the expression is true, false otherwise.
	 */
	public boolean eval(PhysicalAgent scripted,
						MOB source,
						Environmental target,
						MOB monster,
						Item primaryItem,
						Item secondaryItem,
						String msg,
						Object[] tmp,
						String[][] eval,
						int startEval);

	/**
	 * Evaluates one of the boolean functions as a string
	 * variable expression, which gives different and
	 * informative results.  See the Green Table in the
	 * Scripting Guide.
	 * @param scripted the object that is scripted
	 * @param source the source of the event
	 * @param target the target of the event
	 * @param monster a mob representation of the scripted object
	 * @param primaryItem an item involved in the event
	 * @param secondaryItem a second item involved in the event
	 * @param msg a string message associated with the event
	 * @param tmp miscellaneous local variables
	 * @param evaluable the function expression
	 * @return the results of the function expression
	 */
	public String functify(PhysicalAgent scripted,
							MOB source,
							Environmental target,
							MOB monster,
							Item primaryItem,
							Item secondaryItem,
							String msg,
							Object[] tmp,
							String evaluable);
	/**
	 * Calling this method forces this script to look for a trigger
	 * dealing with the end of a quest (QUEST_TIME_PROG -1).
	 * @param hostObj the scripted object
	 * @param mob a mob representation of the host object
	 * @param quest the name of the quest being ended
	 * @return true if a quest ending trigger was found and run
	 */
	public boolean endQuest(PhysicalAgent hostObj, MOB mob, String quest);

	/**
	 * Returns the script or load command(s).
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setScript(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#externalFiles()
	 * @return the script or load command(s)
	 */
	public String getScript();

	/**
	 * Returns the hey used to cache the script or load commands in here.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getScript()
	 * @return the key to the script or load command(s)
	 */
	public String getScriptResourceKey();

	/**
	 * Sets the script or load command(s).
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getScript()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#externalFiles()
	 * @param newParms the script or load command(s)
	 */
	public void setScript(String newParms);

	/**
	 * If the script is a load command, this will return the
	 * list of loaded script files referenced by the load command
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getScript()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setScript(String)
	 * @return a list of loaded script files.
	 */
	public List<String> externalFiles();

	/**
	 * If this script is associated with a particular quest, this
	 * method is called to register that quest name.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#defaultQuestName()
	 * @param questName the quest associated with this script
	 */
	public void registerDefaultQuest(String questName);

	/**
	 * If this script is associated with a particular quest, this
	 * method is called to return that quest name.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#registerDefaultQuest(String)
	 * @return the quest associated with this script, if any
	 */
	public String defaultQuestName();

	/**
	 * Sets the scope of any variables defined within the script.  Although the scope
	 * is somewhat modified if this script is quest-bound, it is usually honored.
	 * Valid scopes include: "" for global, "*" for local, or a shared named scope.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVarScope()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVar(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getLocalVarXML()
	 * @param scope the scope of variables
	 */
	public void setVarScope(String scope);

	/**
	 * Returns the scope of any variables defined within the script.  Although the scope
	 * is somewhat modified if this script is quest-bound, it is usually honored.
	 * Valid scopes include: "" for global, "*" for local, or a shared named scope.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVarScope(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVar(String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setLocalVarXML(String)
	 * @return the scope of variables
	 */
	public String getVarScope();

	/**
	 * If the variable scope of this script is local, this will return all the variables
	 * and values defined as an xml document for easy storage.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVarScope(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVar(String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setLocalVarXML(String)
	 * @return the local variable values as xml
	 */
	public String getLocalVarXML();

	/**
	 * If the variable scope of this script is local, this will set all the variables
	 * and values defined from a passed in xml document.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVarScope()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVar(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getLocalVarXML()
	 * @param xml the local variable values as xml
	 */
	public void setLocalVarXML(String xml);

	/**
	 * Returns whether this script is a temporary attributed of the scripted object,
	 * or a permanent on that should be saved with the object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setSavable(boolean)
	 * @return whether this script is a saveable attribute of the scripted object
	 */
	public boolean isSavable();

	/**
	 * Sets whether this script is a temporary attributed of the scripted object,
	 * or a permanent on that should be saved with the object.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#isSavable()
	 * @param truefalse true if this script is a saveable attribute of the scripted object
	 */
	public void setSavable(boolean truefalse);

	/**
	 * Returns the value of one of the internal variables, determined by the scope
	 * of the script, the context of the variable, and the name of the variable.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVarScope(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVarScope()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVar(String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#isVar(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getLocalVarXML()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setLocalVarXML(String)
	 * @param context the context of the variable, usually a mob or object name
	 * @param variable the name of the variable
	 * @return the value of the variable
	 */
	public String getVar(String context, String variable);

	/**
	 * Returns whether an internal variables, determined by the scope
	 * of the script, the context of the variable, and the name of the variable, is defined.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVarScope(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVarScope()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVar(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVar(String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getLocalVarXML()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setLocalVarXML(String)
	 * @param context the context of the variable, usually a mob or object name
	 * @param variable the name of the variable
	 * @return true if the variable has been set in the past, false otherwise
	 */
	public boolean isVar(String context, String variable);

	/**
	 * Sets the value of one of the internal variables, determined by the scope
	 * of the script, the context of the variable, and the name of the variable.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setVarScope(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVarScope()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getVar(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#isVar(String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#getLocalVarXML()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine#setLocalVarXML(String)
	 * @param context the context of the variable, usually a mob or object name
	 * @param variable the name of the variable
	 * @param value the value of the variable
	 */
	public void setVar(String context, String variable, String value);

	/**
	 * An object that holds the information about an event until it is
	 * time to execute its associated script.
	 * @author Bo Zimmermanimmerman
	 */
	public static class ScriptableResponse
	{
		private int tickDelay=0;
		/** (host) the object being scripted */
		public PhysicalAgent h=null;
		/** (source) the source of the event */
		public MOB s=null;
		/** (target) the target of the event */
		public Environmental t=null;
		/** (host mob) a mob representation of the host (h)*/
		public MOB m=null;
		/** (primary item) an item associated with this event */
		public Item pi=null;
		/** (second item) a second item associated with this event */
		public Item si=null;
		/** (script) the actual script to execute for this event */
		public DVector scr;
		/** a string associated with this event */
		public String message=null;

		/**
		 * Create an event response object
		 * @param host the object being scripted
		 * @param source the source of the event
		 * @param target the target of the event
		 * @param monster a mob representation of the host
		 * @param primaryItem an item associated with this event
		 * @param secondaryItem a second item associated with this event
		 * @param script the actual script to execute for this event
		 * @param ticks how many ticks to wait before executing the script
		 * @param msg a string associated with this event
		 */
		public ScriptableResponse(PhysicalAgent host,
								  MOB source,
								  Environmental target,
								  MOB monster,
								  Item primaryItem,
								  Item secondaryItem,
								  DVector script,
								  int ticks,
								  String msg)
		{
			h=host;
			s=source;
			t=target;
			m=monster;
			pi=primaryItem;
			si=secondaryItem;
			scr=script;
			tickDelay=ticks;
			message=msg;
		}

		/**
		 * Decrements the internal tick counter and returns true if
		 * the tick counter has dropped to or below 0
		 * @return true if its time to execute
		 */
		public boolean checkTimeToExecute() { return ((--tickDelay)<=0); }
	}

	/** The number of local variables associated with an execution of a script */
	public static final int SPECIAL_NUM_OBJECTS=12;
	/** The index into the local variables array for a random pc */
	public static final int SPECIAL_RANDPC=10;
	/** The index into the local variables array for a random pc or mob  */
	public static final int SPECIAL_RANDANYONE=11;
	/** The index into the local variables array for a random items shop price from shophas  */
	public static final int SPECIAL_9SHOPHASPRICE=9;

	/** String list of all valid trigger keywords */
	public static final String[] progs=
	{
		"GREET_PROG", //1
		"ALL_GREET_PROG", //2
		"SPEECH_PROG", //3
		"GIVE_PROG", //4
		"RAND_PROG", //5
		"ONCE_PROG", //6
		"FIGHT_PROG", //7
		"ENTRY_PROG", //8
		"EXIT_PROG", //9
		"DEATH_PROG", //10
		"HITPRCNT_PROG", //11
		"MASK_PROG", //12
		"QUEST_TIME_PROG", // 13
		"TIME_PROG", // 14
		"DAY_PROG", // 15
		"DELAY_PROG", // 16
		"FUNCTION_PROG", // 17
		"ACT_PROG", // 18
		"BRIBE_PROG", // 19
		"GET_PROG", // 20
		"PUT_PROG", // 21
		"DROP_PROG", // 22
		"WEAR_PROG", // 23
		"REMOVE_PROG", // 24
		"CONSUME_PROG", // 25
		"DAMAGE_PROG", // 26
		"BUY_PROG", // 27
		"SELL_PROG", // 28
		"LOGIN_PROG", // 29
		"LOGOFF_PROG", // 30
		"REGMASK_PROG", // 31
		"LEVEL_PROG", // 32
		"CHANNEL_PROG", // 33
		"OPEN_PROG", // 34
		"CLOSE_PROG", // 35
		"LOCK_PROG", // 36
		"UNLOCK_PROG", // 37
		"SOCIAL_PROG", // 38
		"LOOK_PROG", // 39
		"LLOOK_PROG", // 40
		"EXECMSG_PROG", // 41
		"CNCLMSG_PROG", // 42
		"IMASK_PROG", // 43
		"KILL_PROG", //44
		"ARRIVE_PROG" //45
	};

	/** String list of all valid mobprog functions for logical expressions or string functions */
	public static final String[] funcs=
	{
		"RAND", //1
		"HAS", //2
		"WORN", //3
		"ISNPC", //4
		"ISPC", //5
		"ISGOOD", //6
		"ISNAME", //7
		"ISEVIL", //8
		"ISNEUTRAL", //9
		"ISFIGHT", //10
		"ISIMMORT", //11
		"ISCHARMED", //12
		"STAT", //13
		"AFFECTED", //14
		"ISFOLLOW", //15
		"HITPRCNT", //16
		"INROOM", //17
		"SEX", //18
		"POSITION", //19
		"LEVEL", //20
		"CLASS", //21
		"BASECLASS", //22
		"RACE", //23
		"RACECAT", //24
		"GOLDAMT", //25
		"OBJTYPE", // 26
		"VAR", // 27
		"QUESTWINNER", //28
		"QUESTMOB", // 29
		"QUESTOBJ", // 30
		"ISQUESTMOBALIVE", // 31
		"NUMMOBSINAREA", // 32
		"NUMMOBS", // 33
		"NUMRACESINAREA", // 34
		"NUMRACES", // 35
		"ISHERE", // 36
		"INLOCALE", // 37
		"ISTIME", // 38
		"ISDAY", // 39
		"NUMBER", // 40
		"EVAL", // 41
		"RANDNUM", // 42
		"ROOMMOB", // 43
		"ROOMITEM", // 44
		"NUMMOBSROOM", // 45
		"NUMITEMSROOM", // 46
		"MOBITEM", // 47
		"NUMITEMSMOB", // 48
		"HASTATTOO", // 49
		"ISSEASON", // 50
		"ISWEATHER", // 51
		"GSTAT", // 52
		"INCONTAINER", //53
		"ISALIVE", // 54
		"ISPKILL", // 55
		"NAME", // 56
		"ISMOON", // 57
		"ISABLE", // 58
		"ISOPEN", // 59
		"ISLOCKED", // 60
		"STRIN", // 61
		"CALLFUNC", // 62
		"NUMPCSROOM", // 63
		"DEITY", // 64
		"CLAN", // 65
		"CLANRANK", // 66
		"HASTITLE", // 67
		"CLANDATA", // 68
		"ISBEHAVE", // 69
		"IPADDRESS", // 70
		"RAND0NUM", // 71
		"FACTION", //72
		"ISSERVANT", // 73
		"HASNUM", // 74
		"CURRENCY", // 75
		"VALUE", // 76
		"EXPLORED", // 77
		"EXP", // 78
		"NUMPCSAREA", // 79
		"QUESTPOINTS", // 80
		"TRAINS", // 81
		"PRACS", // 82
		"QVAR", // 83
		"MATH", // 84
		"ISLIKE", //85
		"STRCONTAINS", //86
		"ISBIRTHDAY", //87
		"MOOD", //88
		"ISRECALL", //89
		"INAREA", //90
		"DATETIME", //91
		"ISODD", // 92
		"QUESTSCRIPTED", //93
		"QUESTROOM", // 94
		"ISSPEAKING", // 95
		"ISCONTENT", // 96
		"WORNON", // 97
		"CLANQUALIFIES", // 98
		"HASACCTATTOO", // 99
		"SHOPITEM", // 100
		"NUMITEMSSHOP", // 101
		"SHOPHAS" // 102
	};

	/** String list of all valid mobprog commands */
	public static final String[] methods=
	{
		"MPASOUND", //1
		"MPECHO", //2
		"MPSLAY", //3
		"MPJUNK", //4
		"MPMLOAD", //5
		"MPOLOAD", //6
		"MPECHOAT", //7
		"MPECHOAROUND", //8
		"MPCAST", //9
		"MPKILL", //10
		"MPEXP", //11
		"MPPURGE", //12
		"MPUNAFFECT", //13
		"MPGOTO", //14
		"MPAT", //15
		"MPSET", //16
		"MPTRANSFER", //17
		"MPFORCE", //18
		"IF", //19
		"MPSETVAR", //20
		"MPENDQUEST",//21
		"MPQUESTWIN", //22
		"MPSTARTQUEST", //23
		"MPCALLFUNC", // 24
		"MPBEACON", // 25
		"MPALARM", // 26
		"MPWHILE", // 27
		"MPDAMAGE", // 28
		"MPTRACKTO", // 29
		"MPAFFECT", // 30
		"MPBEHAVE", // 31
		"MPUNBEHAVE",  //32
		"MPTATTOO", // 33
		"BREAK", // 34
		"MPGSET", // 35
		"MPSAVEVAR", // 36
		"MPENABLE", // 37
		"MPDISABLE", // 38
		"MPLOADVAR", // 39
		"MPM2I2M", // 40
		"MPOLOADROOM", // 41
		"MPHIDE", // 42
		"MPUNHIDE", // 43
		"MPOPEN", // 44
		"MPCLOSE", // 45
		"MPLOCK", // 46
		"MPUNLOCK", // 47
		"RETURN", // 48
		"MPTITLE", // 49
		"BREAK", // 50
		"MPSETCLANDATA", // 51
		"MPPLAYERCLASS", // 52
		"MPWALKTO", // 53
		"MPFACTION", //54
		"MPNOTRIGGER", // 55
		"MPSTOP", // 56
		"<SCRIPT>", // 57
		"MPRESET", // 58
		"MPQUESTPOINTS", // 59
		"MPTRAINS", // 60
		"MPPRACS", // 61
		"FOR", // 62
		"MPARGSET", // 63
		"MPLOADQUESTOBJ", // 64
		"MPQSET", // 65
		"MPLOG", // 66
		"MPCHANNEL", // 67
		"MPUNLOADSCRIPT", //68
		"MPSTEPQUEST", //69
		"SWITCH", //70
		"MPREJUV", //71
		"MPSCRIPT", //72
		"MPSETINTERNAL", // 73
		"MPPROMPT", // 74
		"MPCONFIRM", // 75
		"MPCHOOSE", // 76
		"MPMONEY", // 77
		"MPHEAL", // 78
		"MPPOSSESS", // 79
		"MPSPEAK", // 80
		"MPSETCLAN", // 81
		"MPRLOAD", // 82
		"MPACCTATTOO", // 83
		"MPOLOADSHOP", //84
		"MPMLOADSHOP", //85
	};

	/** a list of some some extra stat codes for mobs*/
	public final static String[] GSTATCODES_ADDITIONAL={"DEITY","CLAN","CLANROLE","STINK"};
	/** index and equate for stat code for mob: deity*/
	public final static int GSTATADD_DEITY=0;
	/** index and equate for stat code for mob: clan */
	public final static int GSTATADD_CLAN=1;
	/** index and equate for stat code for mob: clan role */
	public final static int GSTATADD_CLANROLE=2;
	/** index and equate for stat code for mob: hygeine */
	public final static int GSTATADD_STINK=3;

	/** a list of the different parts of a time clock */
	public final static String[] DATETIME_ARGS={"HOUR","TIME","DAY","DATE","MONTH","YEAR"};

	/** List of evaluation signs ==, !=, &gt;, etc.*/
	public final static String[] SIGNS={"==",">=",">","<","<=","=>","=<","!="};

	/** Index and equate for == */
	public final static int SIGN_EQUL=0;
	/** Index and equate for &gt;= */
	public final static int SIGN_GTEQ=1;
	/** Index and equate for &gt; */
	public final static int SIGN_GRAT=2;
	/** Index and equate for &lt; */
	public final static int SIGN_LEST=3;
	/** Index and equate for &lt;= */
	public final static int SIGN_LTEQ=4;
	/** Index and equate for =&gt; */
	public final static int SIGN_EQGT=5;
	/** Index and equate for =&lt; */
	public final static int SIGN_EQLT=6;
	/** Index and equate for != */
	public final static int SIGN_NTEQ=7;

	/** a list of logical connectors (and, or, etc)*/
	public final static String[] CONNECTORS={"AND","OR","NOT","ANDNOT","ORNOT"};
	/** index and equate for logical connector AND*/
	public final static int CONNECTOR_AND=0;
	/** index and equate for logical connector OR */
	public final static int CONNECTOR_OR=1;
	/** index and equate for logical connector NOT */
	public final static int CONNECTOR_NOT=2;
	/** index and equate for logical connector ANDNOT */
	public final static int CONNECTOR_ANDNOT=3;
	/** index and equate for logical connector ORNOT */
	public final static int CONNECTOR_ORNOT=4;
	/** A table to describe what happens when connectors are found sequentially (and and not or not and and, etc) */
	public final static int[][] CONNECTOR_MAP=
	{
		{CONNECTOR_AND,CONNECTOR_OR,CONNECTOR_ANDNOT,CONNECTOR_AND,CONNECTOR_ORNOT}, //and
		{CONNECTOR_OR,CONNECTOR_OR,CONNECTOR_ORNOT,CONNECTOR_ORNOT,CONNECTOR_ORNOT}, //or
		{CONNECTOR_ANDNOT,CONNECTOR_ORNOT,CONNECTOR_AND,CONNECTOR_AND,CONNECTOR_OR}, //not
		{CONNECTOR_ANDNOT,CONNECTOR_ORNOT,CONNECTOR_AND,CONNECTOR_AND,CONNECTOR_ORNOT}, //andnot
		{CONNECTOR_ORNOT,CONNECTOR_ORNOT,CONNECTOR_OR,CONNECTOR_ORNOT,CONNECTOR_ORNOT}, //ornot
	};
}
