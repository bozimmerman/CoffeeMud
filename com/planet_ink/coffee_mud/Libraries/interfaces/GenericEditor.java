package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
   Copyright 2008-2025 Bo Zimmerman

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
 * The command line user interface for almost all On Line Editor functions
 * goes through this library.  Functions are available for Modifiable
 * interface edits, general menu edits, as well as abstract object
 * editor menu interfaces.
 *
 * @author Bo Zimmerman
 *
 */
public interface GenericEditor extends CMLibrary
{
	/**
	 * An interface for implementing a custom evaluator
	 * for prompts requiring lists of things.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface CMEval
	{
		/**
		 * Evaluate the given value entry against a list
		 * of choices, throwing an exception on problems.
		 *
		 * @param val the user entry
		 * @param choices the choices
		 * @param emptyOK true if "" is an ok value
		 * @return the adjusted value
		 * @throws CMException any errors
		 */
		public Object eval(Object val, Object[] choices, boolean emptyOK)
				throws CMException;
	}

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Area.
	 *
	 * @param mob the player doing the editing
	 * @param myArea the area being edited
	 * @param alsoUpdateAreas set to put other area objects changed by this one (parents/children)
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyArea(MOB mob, Area myArea, Set<Area> alsoUpdateAreas, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Room.
	 *
	 * @param mob the player doing the editing
	 * @param R the room being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @return the new room, if its type was changed
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public Room modifyRoom(MOB mob, Room R, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Player Account.
	 *
	 * @param mob the player doing the editing
	 * @param A the account being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyAccount(MOB mob, PlayerAccount A, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows modifying the modest editable parameters of
	 * a standard item
	 *
	 * @param mob the player doing the editing
	 * @param I the item to edit
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyStdItem(MOB mob, Item I, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows modifying the modest editable parameters of
	 * a standard mob
	 *
	 * @param mob the player doing the editing
	 * @param M the mob to edit
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyStdMob(MOB mob, MOB M, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of a set of material components required to
	 * use a skill or cast a spell.
	 *
	 * @param mob the player doing the editing
	 * @param skillID the ability ID to modify components for
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyComponents(MOB mob, String skillID, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Clan.
	 *
	 * @param mob the player doing the editing
	 * @param C the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyClan(MOB mob, Clan C, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Ability (non Lang/Common)
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenAbility(MOB mob, Ability me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Language.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenLanguage(MOB mob, Language me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Manufacturer.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyManufacturer(MOB mob, Manufacturer me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Crafting Skill.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenCraftSkill(MOB mob, Ability me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Wrighting Skill.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenWrightSkill(final MOB mob, final Ability me, int showFlag) throws IOException;


	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Command.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenCommand(final MOB mob, final Modifiable me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Trap.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenTrap(final MOB mob, final Trap me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Gathering Skill.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenGatheringSkill(final MOB mob, final Ability me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Char Class.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenClass(MOB mob, CharClass me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Exit.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenExit(MOB mob, Exit me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Generic Race.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGenRace(MOB mob, Race me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Player Character.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyPlayer(MOB mob, MOB me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows the editing of an existing Clan Government.
	 *
	 * @param mob the player doing the editing
	 * @param me the object being edited
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void modifyGovernment(MOB mob, ClanGovernment me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Modifies the All Qualify rule for the given Ability.  These are
	 * the rules that auto-apply certain abilities to All classes as
	 * an abstract group, or to Each class as an individual skill.
	 *
	 * @param mob the player doing the editing
	 * @param eachOrAll true for Each class, false for All classes
	 * @param me the Ability to find the rule for
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @return the new AbilityMapping object containing the criteria.
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public AbilityMapper.AbilityMapping modifyAllQualifyEntry(MOB mob, String eachOrAll, Ability me, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Generates an edited Planar Rule string after allowing the user
	 * to add/edit various rules to define the Plane of Existence.
	 *
	 * @param mob the player doing the editing
	 * @param planeName the name of the Plane of Existence
	 * @param planeSet the existing variable definitions for the plane
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @return the edited Planar Rule string (key=value pairs)
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String modifyPlane(final MOB mob, final String planeName, final Map<String,String> planeSet, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * The general command line editor entry point will direct the
	 * user to the specific editor menu most appropriate to the given
	 * object, whether room, exit, area, item, or mob.
	 *
	 * @param mob the player doing the editing
	 * @param E the object to edit
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genMiscSet(MOB mob, Environmental E, int showFlag) throws IOException;

	/**
	 * Changes the given rooms type into that of the given new room.
	 * Copies all the things, and destroys the old room.  If the old
	 * room is savable, the DB is also updated.
	 *
	 * @param R the old room with all the stuff in it
	 * @param newRoom the new room type
	 * @return the new room
	 */
	public Room changeRoomType(Room R, Room newRoom);

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows populating/modifying a list containing either Ability
	 * or Behavior objects, and, optionally, their parameters.
	 *
	 * @see GenericEditor#spellsOrBehaviors(MOB, List, int, int, boolean)
	 *
	 * @param mob the player doing the editing
	 * @param V the list of ability/behavior objects
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param inParms true to allow parms to be edited, false otherwise
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void spellsOrBehavs(MOB mob, List<CMObject> V, int showNumber, int showFlag, boolean inParms) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows populating/modifying a list containing either Ability
	 * or Behavior objects, and, optionally, their parameters.
	 *
	 * @see GenericEditor#spellsOrBehavs(MOB, List, int, int, boolean)
	 *
	 * @param mob the player doing the editing
	 * @param V the list of ability/behavior objects
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param inParms true to allow parms to be edited, false otherwise
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void spellsOrBehaviors(MOB mob, List<CMObject> V, int showNumber, int showFlag, boolean inParms) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows changing an armors given worn location bitmap, as well
	 * as its and/or distinction.
	 *
	 * @param mob the player doing the editing
	 * @param oldWornLocation the old/new worn location bitmap
	 * @param logicalAnd true for All locations, false for Any loc
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void wornLocation(MOB mob, long[] oldWornLocation, boolean[] logicalAnd, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows changing an Armor's given clothing layer number, as well
	 * as it's layering attribute value.
	 *
	 * @param mob the player doing the editing
	 * @param layerAtt the existing/new layer attributes
	 * @param clothingLayer the existing/new clothing layer
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void wornLayer(MOB mob, short[] layerAtt, short[] clothingLayer, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows changing the given objects phyStat "ability" value,
	 * either as a Magic Level or Tech Level.
	 *
	 * @param mob the player doing the editing
	 * @param P the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genAbility(MOB mob, Physical P, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows changing the given objects set of affects.
	 *
	 * @param mob the player doing the editing
	 * @param P the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genAffects(MOB mob, Physical P, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * Allows changing the given objects set of behaviors.
	 *
	 * @param mob the player doing the editing
	 * @param P the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genBehaviors(MOB mob, PhysicalAgent P, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user for a new description value using the mud's
	 * standard menu interface, and changes the given objects description.
	 *
	 * @param mob the player doing the editing
	 * @param E the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genDescription(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user for a new display text using the mud's
	 * standard menu interface, and changes the given objects display text.
	 *
	 * @param mob the player doing the editing
	 * @param E the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genDisplayText(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user for a new name value using the mud's
	 * standard menu interface, and changes the given objects name.
	 *
	 * @param mob the player doing the editing
	 * @param E the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genName(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;

	/**
	 * Provides an command line editor menu for a CoffeeMud object.
	 * If the given object is generic, an appropriate generic
	 * editor menu is given, and showNumber is ignored.  If the
	 * object is not generic, a single string editor for the
	 * misc text is used, and showNumber is respected.
	 *
	 * @param mob the player doing the editing
	 * @param E the object to edit
	 * @param showNumber the item number of this menu entry, possibly ignored
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void genMiscText(MOB mob, Environmental E, int showNumber, int showFlag) throws IOException;

	/**
	 * Prompts the given user for a boolean value using the mud's
	 * standard menu interface.  This returns the users choice,
	 * giving a Toggle? prompt.
	 *
	 * @param mob the player doing the toggling
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @return the value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public boolean promptToggle(MOB mob, int showNumber, int showFlag, String fieldDisp) throws IOException;

	/**
	 * Prompts the given user for a boolean value using the mud's
	 * standard menu interface.  A previous boolean value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;

	/**
	 * Prompts the given user for a boolean value using the mud's
	 * standard menu interface.  A previous boolean value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param help null, or a help message to respond to on ?
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public boolean prompt(MOB mob, boolean oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;

	/**
	 * Prompts the given user for a double value using the mud's
	 * standard menu interface.  A previous double value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;

	/**
	 * Prompts the given user for a double value using the mud's
	 * standard menu interface.  A previous double value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param help null, or a help message to respond to on ?
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public double prompt(MOB mob, double oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;

	/**
	 * Prompts the given user for a int value using the mud's
	 * standard menu interface.  A previous int value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;

	/**
	 * Prompts the given user for a int value using the mud's
	 * standard menu interface.  A previous int value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param help null, or a help message to respond to on ?
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public int prompt(MOB mob, int oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;

	/**
	 * Prompts the given user for a long value using the mud's
	 * standard menu interface.  A previous long value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;

	/**
	 * Prompts the given user for a long value using the mud's
	 * standard menu interface.  A previous long value is
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param help null, or a help message to respond to on ?
	 * @return the new value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public long prompt(MOB mob, long oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * This function accepts from the user and generates a
	 * bitmap based on named bits (first value of each choices pair
	 * is a bit value, and second is the name).
	 * The returned and given old value are bitmap numbers.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the bitmap
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param choices list of bit pair choices: str(int)/str pairs
	 * @return the new int value generated from options entered by the user from the choices
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public int promptMulti(MOB mob, int oldVal, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * This accepts a previous string key value, and returns
	 * the same.  The key value is chosen from a list of choice
	 * pairs where the first entry is the key value, and the second
	 * a display value.  Only one selection is permitted.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous key value
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param choices set of choices as pair(key value, display value)
	 * @return the new key value selected by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String promptChoice(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * This accepts a string of delimited key values, and returns
	 * the same.  Each key value is chosen from a list of choice
	 * pairs where the first entry is the key value, and the second
	 * a display value.
	 * Obviously this means multiple selections from the list of
	 * choices is permitted.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous values as a delimited string
	 * @param delimiter the delimiter of the choices in the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param choices set of choices as pair(key value, display value)
	 * @param nullOK true if selecting nothing is OK, false otherwise
	 * @return the new delimited values selected by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String promptMultiSelectList(MOB mob, String oldVal, String delimiter, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices, boolean nullOK) throws IOException;

	/**
	 * Prompts the given user using the mud's standard menu interface.
	 * This function accepts EITHER a string from a set of choices
	 * (first value of each choices pair applies) OR generating a
	 * bitmap based on named bits (first value of each choices pair
	 * is a bit value, and second is the name).  In the second case,
	 * the returned and given old value are numbers cast as strings.
	 * Seriously, this should be two unrelated functions, but it made
	 * the QuestManager CL editor a lot smaller.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string, or str(int)
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param choices list of unique choices, or str(int)/str pairs
	 * @return the new string or str(int) value entered by the user from the choices
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String promptMultiOrExtra(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, PairList<String,String> choices) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @param rawPrint true to show old values unfiltered, false for normal view
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK, boolean rawPrint) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @param help null, or a help message to respond to on ?
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK, String help) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param help null, or a help message to respond to on ?
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, String help) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @param rawPrint true to show old values unfiltered, false for normal view
	 * @param help null, or a help message to respond to on ?
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob, String oldVal, int showNumber, int showFlag, String fieldDisp, boolean emptyOK, boolean rawPrint, String help) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * It supports picking strings from options and other features.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @param rawPrint true to show old values unfiltered, false for normal view
	 * @param help null, or a help message to respond to on ?
	 * @param eval null, or an {@link GenericEditor.CMEval } object
	 * @param choices null, or the set of valid choices, any objects that have toString()
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob,
						String oldVal,
						int showNumber,
						int showFlag,
						String fieldDisp,
						boolean emptyOK,
						boolean rawPrint,
						String help,
						CMEval eval,
						Object[] choices) throws IOException;

	/**
	 * Prompts the given user for a string using the mud's
	 * standard menu interface.  A previous string value can
	 * be provided for editing purposes.
	 *
	 * It supports picking strings from options, max chars,
	 * and other features.
	 *
	 * @param mob the player doing the editing
	 * @param oldVal the previous value of the string
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @param rawPrint true to show old values unfiltered, false for normal view
	 * @param maxChars 0, or a maximum number of chars for the string
	 * @param help null, or a help message to respond to on ?
	 * @param eval null, or an {@link GenericEditor.CMEval } object
	 * @param choices null, or the set of valid choices, any objects that have toString()
	 * @return the new string value entered by the user
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public String prompt(MOB mob,
						String oldVal,
						int showNumber,
						int showFlag,
						String fieldDisp,
						boolean emptyOK,
						boolean rawPrint,
						int maxChars,
						String help,
						CMEval eval,
						Object[] choices) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface String stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatStr(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface String stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param emptyOK true if "" is acceptable, false otherwise
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatStr(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField, boolean emptyOK) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Integer stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatInt(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Integer stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatInt(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Boolean stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatBool(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Boolean stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatBool(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Double stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatDouble(MOB mob, Modifiable E, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface Double stat value.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatDouble(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface stat value chosen from
	 * a set of choices.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param choices the set of valid choices, any object that has toString()
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatChoices(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField, Object[] choices) throws IOException;

	/**
	 * Prompts the given mob for a Modifiable interface stat value that consists of a
	 * comma delimited list of values from a set of choices.
	 * Sets the Modifiable stat value appropriately.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable
	 * @param mob the player doing the editing
	 * @param E the Modifiable object to modify
	 * @param help null, or a help message to respond to on ?
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisp the prompt display string
	 * @param statField the official Modifiable stat field id
	 * @param choices the set of valid choices, any object that has toString()
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public void promptStatCommaChoices(MOB mob, Modifiable E, String help, int showNumber, int showFlag, String fieldDisp, String statField, Object[] choices) throws IOException;

	/**
	 * Prompts the given mob for one or more choices from a given enum values
	 * array.  Returns all the choices in a collection.  Uses the standard
	 * menu interface.
	 *
	 * @param mob the player doing the editing
	 * @param flags the current existing set of selected enum values
	 * @param values the full set of all enum values
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisplayStr the prompt display string
	 * @return the new user value
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	public Collection<? extends Object> promptEnumChoices(final MOB mob, final Collection<? extends Object> flags, final Object[] values, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException;

	/**
	 * Prompts the given mob for one or more choices from a given enum values
	 * array.  Returns the choice from the collection.  Uses the standard
	 * menu interface.
	 *
	 * @param mob the player doing the editing
	 * @param val current value
	 * @param cs the values to choose from
	 * @param showNumber the item number of this menu entry
	 * @param showFlag 0 to only show prompt and value, -999 to always edit, or the showNumber to edit
	 * @param fieldDisplayStr the prompt display string
	 * @return the new user value
	 * @throws IOException any i/o errors that occur (socket reset errors usually)
	 */
	@SuppressWarnings("rawtypes")
	public Enum<? extends Enum> promptEnumChoice(final MOB mob, final Enum<? extends Enum> val, final Enum<? extends Enum>[] cs, final int showNumber, final int showFlag, final String fieldDisplayStr) throws IOException;
}
