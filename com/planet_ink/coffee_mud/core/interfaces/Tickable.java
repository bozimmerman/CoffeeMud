package com.planet_ink.coffee_mud.core.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
 * This interface is implemented by any object which wishes to get periodic thread time from
 * the threads engine.  Almost all CoffeeMud objects implement this interface
 * @author Bo Zimmerman
 *
 */
public interface Tickable extends CMObject
{
	/**
	 * The nice displayable name of this instance of this object
	 * @return the displayable name of this object instance
	 */
	@Override
	public String name();
	/**
	 * A coded status for this object during the period where
	 * its tick method is being called. The statis is defined,
	 * at least in part, by constants in this interface STATUS_*.
	 * STATUS_NOT should be returned when the objects tick method
	 * is not currently in execution.  It should never return
	 * STATUS_NOT when the objects tick method is in execution.
	 * @see Tickable#tick(Tickable, int)
	 * @return the numeric status of this object
	 */
	public int getTickStatus();

	/**
	 * this is the method which is called periodically by the threading engine.  How often it
	 * is called depends on the parameters passed to the threadding engine when it is submitted
	 * for thread access.  Typically the period is once per TIME_TICK period, but that is
	 * determined when the object is submitted to the thread engine.
	 * @see Tickable
	 * @see com.planet_ink.coffee_mud.core.threads.ServiceEngine
	 * @see TickableGroup
	 * @param ticking a reference to this Tickable object
	 * @param tickID the TICKID_ constant describing this periodic call, as defined in Tickable
	 * @return true always, unless this object no longer wishes to ever tick again, in which case false
	 */
	public boolean tick(Tickable ticking, int tickID);

	/** modifies a tickID to designate a longer wait before declaring dead.*/
	public final static int TICKID_LONGERMASK=256;
	/** modifies a tickID to designate that this object should tick by itself.*/
	public final static int TICKID_SOLITARYMASK=65536;

	/** the most common tickid, representing the tick of a mob*/
	public final static int TICKID_MOB=0;
	/** the tickid representing the tick of a behavior on an item*/
	public final static int TICKID_ITEM_BEHAVIOR=1;
	/** the tickid representing the tick that automatically closes open doors*/
	public final static int TICKID_EXIT_REOPEN=2;
	/** the tickid representing the decay tick on a corpse*/
	public final static int TICKID_DEADBODY_DECAY=3;
	/** the tickid representing the going out of a light*/
	public final static int TICKID_LIGHT_FLICKERS=4;
	/** the tickid representing the resetting of a trap*/
	public final static int TICKID_TRAP_RESET=5;
	/** the tickid representing the destruction of a trap*/
	public final static int TICKID_TRAP_DESTRUCTION=6;
	/** the tickid representing the returning of an innkey to the front desk*/
	public final static int TICKID_ITEM_BOUNCEBACK=7;
	/** the tickid representing the tick of a behavior on a room*/
	public final static int TICKID_ROOM_BEHAVIOR=8;
	/** the tickid representing the standard tick of an area*/
	public final static int TICKID_AREA=9;
	/** the tickid representing the resetting of an item that rejuvenates*/
	public final static int TICKID_ROOM_ITEM_REJUV=10;
	/** the tickid representing the ticking of a behavior on an exit*/
	public final static int TICKID_EXIT_BEHAVIOR=11;
	/** the tickid representing the ticking of a spell on a non-mob object*/
	public final static int TICKID_SPELL_AFFECT=12;
	/** the tickid representing the ticking of a quest script*/
	public final static int TICKID_QUEST=13;
	/** the tickid representing the ticking of a clan object*/
	public final static int TICKID_CLAN=14;
	/** the tickid representing the ticking of a clan item*/
	public final static int TICKID_CLANITEM=15;
	/** the tickid representing the ticking of the smtp server*/
	public final static int TICKID_EMAIL=TICKID_SOLITARYMASK|16;
	/** the tickid representing the impending stop of a service*/
	public final static int TICKID_READYTOSTOP=17;
	/** the tickid for a live auction tick*/
	public final static int TICKID_LIVEAUCTION=18;
	/** the tickid for a slow auction service tick*/
	public final static int TICKID_TIMEAUCTION=19;
	/** the tickid for a miscellaneous tickable service*/
	public final static int TICKID_MISCELLANEOUS=20;
	/** the tickid for a miscellaneous tickable service*/
	public final static int TICKID_PROPERTY_SPECIAL=21;
	/** the tickid for an electrical proactive action service*/
	public final static int TICKID_ELECTRONICS=22;
	/** the tickid for a system support task*/
	public final static int TICKID_SUPPORT=23;
	/** the tickid for an special combat service*/
	public final static int TICKID_SPECIALMANEUVER=24;
	/** the tickid for an special combat service*/
	public final static int TICKID_SPECIALCOMBAT=25;
	/** the tickid for a beam weapon in motion*/
	public final static int TICKID_BEAMWEAPON=26;

	/** a tick status constant representing  the state of waiting for tick access */
	public static int STATUS_NOT=0;
	/** a tick status constant representing the state of just starting its tick access */
	public static int STATUS_START=1;
	/** a tick status representing CLASS part  of its tick access  */
	public static int STATUS_CLASS=2;
	/** a tick status representing RACE part  of its tick access  */
	public static int STATUS_RACE=3;
	/** a tick status representing COMBAT part  of its tick access  */
	public static int STATUS_FIGHT=4;
	/** a tick status representing WEATHER part  of its tick access  */
	public static int STATUS_WEATHER=5;
	/** a tick status representing DEAD part  of its tick access  */
	public static int STATUS_DEAD=6;
	/** a tick status representing ALIVE part  of its tick access  */
	public static int STATUS_ALIVE=7;
	/** a tick status representing OTHER part  of its tick access  */
	public static int STATUS_REBIRTH=8;
	/** a tick status representing ALIVE part  of its tick access  */
	public static int STATUS_OTHER=98;
	/** a tick status representing the end of its tick access  */
	public static int STATUS_END=99;
	/** a tick status MASK representing the tick access of an associated behavior */
	public static int STATUS_BEHAVIOR=512;
	/** a tick status MASK representing the tick access of an associated effect */
	public static int STATUS_AFFECT=1024;
	/** a tick status MASK representing the tick access of an associated script */
	public static int STATUS_SCRIPT=2048;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC=4096;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC2=8192;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC3=16384;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC4=32768;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC5=65536;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC6=131072;
	/** a tick status MASK  whose meaning is class dependent */
	public static int STATUS_MISC7=131072*2;
}
