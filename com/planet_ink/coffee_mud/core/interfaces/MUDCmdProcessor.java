package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;
import java.util.List;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;
import com.planet_ink.coffee_mud.Common.interfaces.Tattoo;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/*
   Copyright 2015-2018 Bo Zimmerman

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
*
* Something that is capable of taking user-entered commands, parsing
* them, and then executing them.
* @author Bo Zimmerman
*
*/
public interface MUDCmdProcessor
{

	/** constant mask for the metaflags parameter for execute and preexecute, means being mpforced*/
	public static final int METAFLAG_MPFORCED=1;
	/** constant mask for the metaflags parameter for execute and preexecute, means being ordered*/
	public static final int METAFLAG_ORDER=2;
	/** constant mask for the metaflags parameter for execute and preexecute, means being possessed*/
	public static final int METAFLAG_POSSESSED=4;
	/** constant mask for the metaflags parameter for execute and preexecute, means being snooped*/
	public static final int METAFLAG_SNOOPED=8;
	/** constant mask for the metaflags parameter for execute and preexecute, means being forced with AS*/
	public static final int METAFLAG_AS=16;
	/** constant mask for the metaflags parameter for execute and preexecute, means being forced with spells*/
	public static final int METAFLAG_FORCED=32;
	/** constant mask for the metaflags parameter for execute and preexecute, means being a meta-command*/
	public static final int METAFLAG_ASMESSAGE=64;
	/** constant mask for the metaflags parameter for execute and preexecute, means always after prev command*/
	public static final int METAFLAG_INORDER=128;
	
	/**
	 * Parses the given command string tokens to determine what kind of command is
	 * to be executed, and depending on how many actions the player has remaining
	 * this tick, either executes the command or puts it on the que for automatic
	 * execution once sufficient actions are available.
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @see MUDCmdProcessor#actions()
	 * @param commands the parsed command string tokens
	 * @param metaFlags meta-command flags to send to the command, if any
	 * @param actionCost either 0 to let the action object decide, or an override cost
	 */
	public void enqueCommand(List<String> commands, int metaFlags, double actionCost);
	
	/**
	 * Parses the given commands string tokens to determine what kind of commands are
	 * to be executed, and depending on how many actions the player has remaining
	 * this tick, either executes the commands or puts them on the que for automatic
	 * execution once sufficient actions are available.
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @see MUDCmdProcessor#actions()
	 * @param commands the parsed commands string tokens
	 * @param metaFlags meta-command flags to send to the command, if any
	 */
	public void enqueCommands(List<List<String>> commands, int metaFlags);
	
	/**
	 * Parses the given command string tokens to determine what kind of command is
	 * to be executed, and depending on how many actions the player has remaining
	 * this tick, either executes the command or puts it on the que for automatic
	 * execution once sufficient actions are available.
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @see MUDCmdProcessor#actions()
	 * @param commands the parsed command string tokens
	 * @param metaFlags meta-command flags to send to the command, if any
	 * @param actionCost either 0 to let the action object decide, or an override cost
	 */
	public void prequeCommand(List<String> commands, int metaFlags, double actionCost);
	
	/**
	 * Parses the given command string tokens to determine what kind of command is
	 * to be executed, and depending on how many actions the player has remaining
	 * this tick, either executes the command or puts it on the que for automatic
	 * execution once sufficient actions are available.
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @param commands the parsed command string tokens
	 * @param metaFlags meta-command flags to send to the command, if any
	 */
	public void prequeCommands(List<List<String>> commands, int metaFlags);

	/**
	 * If this processor has enough action points to perform the top command on the que,
	 * then this method will execute that action.
	 * It returns whether there are still more actions on the que.
	 * @see MUDCmdProcessor#enqueCommand(List, int, double)
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @see MUDCmdProcessor#commandQueSize()
	 * @see MUDCmdProcessor#actions()
	 * @return true if there are more actions on the que, false otherwise
	 */
	public boolean dequeCommand();
	
	/**
	 * Cancels and empties the command que of this processor.
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#enqueCommand(List, int, double)
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @see MUDCmdProcessor#commandQueSize()
	 * @see MUDCmdProcessor#actions()
	 */
	public void clearCommandQueue();
	
	/**
	 * Returns the number of commands on this processors que
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#doCommand(List, int)
	 * @see MUDCmdProcessor#enqueCommand(List, int, double)
	 * @see MUDCmdProcessor#actions()
	 * @return the number of commands on this processors que
	 */
	public int commandQueSize();
	
	/**
	 * Forces this processor to parse the given command string tokens,
	 * determine a command to execute, and then execute it, 
	 * regardless of whether there are any action points
	 * remaining.
	 * @see MUDCmdProcessor#dequeCommand()
	 * @see MUDCmdProcessor#clearCommandQueue()
	 * @see MUDCmdProcessor#commandQueSize()
	 * @see MUDCmdProcessor#enqueCommand(List, int, double)
	 * @see MUDCmdProcessor#actions()
	 * @param commands the command string tokens
	 * @param metaFlags any meta-command flags
	 */
	public void doCommand(List<String> commands, int metaFlags);
	
	/**
	 * Returns the number of action points that this processor
	 * has to spend towards performing any commands, skills,
	 * or whatever needs action points to be spent on them.
	 * They are usually replenished once per tick.
	 * @see MUDCmdProcessor#enqueCommand(List, int, double)
	 * @see MUDCmdProcessor#setActions(double)
	 * @return the number of action points
	 */
	public double actions();
	
	/**
	 * Sets the number of action points that this processor
	 * has to spend towards performing any commands, skills,
	 * or whatever needs action points to be spent on them.
	 * They are usually replenished once per tick.
	 * @see MUDCmdProcessor#enqueCommand(List, int, double)
	 * @see MUDCmdProcessor#actions()
	 * @param remain the number of action points
	 */
	public void setActions(double remain);
}
