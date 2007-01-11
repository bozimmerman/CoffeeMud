package com.planet_ink.coffee_mud.Commands.interfaces;
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
import java.util.Vector;

/* 
   Copyright 2000-2007 Bo Zimmerman

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
 * A Command is a thing entered on the command line by players.  It
 * performs some function, right?
 */
public interface Command extends CMObject
{
	/**
	 * Returns the set of command words, with the most public one first,
	 * that are entered by the user to initiate this command.
	 * @return the set of command words that the user enters
	 */
	public String[] getAccessWords();
	/**
	 * Returns the number of actions required to completely
	 * activate this command. A value of 0.0 means perform 
	 * instantly.  This method only applies when the user
	 * is not in combat.
	 * @see Command#combatActionsCost()
	 * @return the number of player free actions required to do this
	 */
	public double actionsCost();
	/**
	 * Returns the number of actions required to completely
	 * activate this command. A value of 0.0 means perform 
	 * instantly.  This method only applies when the user
	 * is fighting in combat.
	 * @see Command#actionsCost()
	 * @return the number of player free actions required to do this
	 */
    public double combatActionsCost();
    /**
     * Whether the a group leader or charmer can order their followers
     * to do this command.
     * @return whether this command can be ordered.
     */
	public boolean canBeOrdered();
	/**
	 * Whether this command is available to the given player
	 * @param mob the player mob who might not even know about this command
	 * @return true if the command is available, and false if it is unknown
	 */
	public boolean securityCheck(MOB mob);
	/**
	 * This method actually performs the command, when the given parsed
	 * set of command-line words.  The commands list is almost always the
	 * set of strings, starting with the access word that triggered the
	 * command.  Some commands have custom APIs however, that allow almost
	 * anything to be in the commands list, or even for the commands to be null.
	 * This method is not allowed to be called until the player or mob has 
	 * satisfied the actionsCost requirements and the securityCheck
	 * @see Command#actionsCost()
	 * @see Command#securityCheck(MOB)
	 * @param mob the mob or player issueing the command
	 * @param commands usually the command words and parameters; a set of strings
	 * @return whether the command was successfully executed.  Is almost meaningless.
	 * @throws java.io.IOException usually means the player has dropped carrier
	 */
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException;
	/**
	 * This method is only called when the mob invoking this command
	 * does not have enough actions to complete it immediately.  The
	 * method is called when the command is entered, and every second
	 * afterwards until the invoker has enough actions to complete it.
	 * At completion time, execute is called.
	 * @see Command#execute(MOB, Vector)
	 * @param mob the player or mob invoking the command
	 * @param commands the parameters entered for the command (including the trigger word)
	 * @param secondsElapsed 0 at first, and increments every second
	 * @param actionsRemaining number of free actions the player is defficient.
	 * @return whether the command should be allowed to go forward. false cancels altogether.
	 */
    public boolean preExecute(MOB mob, Vector commands, int secondsElapsed, double actionsRemaining)
        throws java.io.IOException;
}
