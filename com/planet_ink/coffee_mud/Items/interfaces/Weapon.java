package com.planet_ink.coffee_mud.Items.interfaces;
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
 * An item that can be used to do damage to another mob.
 * That's what a weapon is.
 * They are classified by "damage type", which speaks generally
 * to the way the weapon hurts a mob, and a "weapon classification",
 * which speaks to the way in which the weapon is used, or the skills
 * required.
 * @author Bo Zimmerman
 */
public interface Weapon extends Item
{
	/** One of the weapon type, denotes natural weapon damage */
	public final static int TYPE_NATURAL=0;
	/** One of the weapon type, denotes slashing weapon damage */
	public final static int TYPE_SLASHING=1;
	/** One of the weapon type, denotes poking weapon damage */
	public final static int TYPE_PIERCING=2;
	/** One of the weapon type, denotes blunt weapon damage */
	public final static int TYPE_BASHING=3;
	/** One of the weapon type, denotes fire weapon damage */
	public final static int TYPE_BURNING=4;
	/** One of the weapon type, denotes explosive weapon damage */
	public final static int TYPE_BURSTING=5;
	/** One of the weapon type, denotes shooting weapon damage */
	public final static int TYPE_SHOOT=6;
	/** One of the weapon type, denotes cold weapon damage */
	public final static int TYPE_FROSTING=7;
	/** One of the weapon type, denotes poisony weapon damage */
	public final static int TYPE_GASSING=8;
	/** One of the weapon type, denotes acid weapon damage */
	public final static int TYPE_MELTING=9;
	/** One of the weapon type, denotes electric weapon damage */
	public final static int TYPE_STRIKING=10;
	/** One of the weapon type, denotes light weapon damage */
	public final static int TYPE_LASERING=11;
	/** One of the weapon type, denotes sonic weapon damage */
	public final static int TYPE_SONICING=12;

	/**
	 * Description code words for the weapon/damage types, indexed
	 * by the weapon/damage type values.
	 */
	public final static String[] TYPE_DESCS=
	{
		"NATURAL",
		"SLASHING",
		"PIERCING",
		"BASHING",
		"BURNING",
		"BURSTING",
		"SHOOTING",
		"FROSTING",
		"GASSING",
		"MELTING",
		"STRIKING",
		"LASERING",
		"SONICING"
	};

	/** One of the weapon classification, denotes an axe swinging type weapon */
	public final static int CLASS_AXE=0;
	/** One of the weapon classification, denotes a blunt type weapon */
	public final static int CLASS_BLUNT=1;
	/** One of the weapon classification, denotes an edged type weapon */
	public final static int CLASS_EDGED=2;
	/** One of the weapon classification, denotes a flailed type weapon */
	public final static int CLASS_FLAILED=3;
	/** One of the weapon classification, denotes a hammer swinging type weapon */
	public final static int CLASS_HAMMER=4;
	/** One of the weapon classification, denotes a natural type weapon */
	public final static int CLASS_NATURAL=5;
	/** One of the weapon classification, denotes a polearm type weapon */
	public final static int CLASS_POLEARM=6;
	/** One of the weapon classification, denotes a ranged type weapon */
	public final static int CLASS_RANGED=7;
	/** One of the weapon classification, denotes a sword type weapon */
	public final static int CLASS_SWORD=8;
	/** One of the weapon classification, denotes a dagger type weapon */
	public final static int CLASS_DAGGER=9;
	/** One of the weapon classification, denotes a staff type weapon */
	public final static int CLASS_STAFF=10;
	/** One of the weapon classification, denotes a thrown type weapon */
	public final static int CLASS_THROWN=11;
	
	/**
	 * Description code words for the weapon classifications, indexed
	 * by the weapon classification code values.
	 */
	public final static String[] CLASS_DESCS=
	{
		"AXE",
		"BLUNT",
		"EDGED",
		"FLAILED",
		"HAMMER",
		"KARATE",
		"POLEARM",
		"RANGED",
		"SWORD",
		"DAGGER",
		"STAFF",
		"THROWN"
	};

	/**
	 * Gets the type of damage this weapon does, from the weapon damage
	 * type list of codes.
	 * @see Weapon#TYPE_DESCS
	 * @return get weapon damage type code
	 */
	public int weaponDamageType();

	/**
	 * Sets the type of damage this weapon does, from the weapon damage
	 * type list of codes.
	 * @see Weapon#TYPE_DESCS
	 * @param newType get weapon damage type code
	 */
	public void setWeaponDamageType(int newType);
	
	/**
	 * Gets the general classification of this weapon, denoting how the
	 * weapon is used and the skills required.  The code comes from the
	 * weapon class list of codes.
	 * @see Weapon#CLASS_DESCS
	 * @return the weapon classification code
	 */
	public int weaponClassification();
	
	/**
	 * Sets the general classification of this weapon, denoting how the
	 * weapon is used and the skills required.  The code comes from the
	 * weapon class list of codes.
	 * @see Weapon#CLASS_DESCS
	 * @param newClassification the weapon classification code
	 */
	public void setWeaponClassification(int newClassification);

	/**
	 * Sets the minimum and maximum range of this weapon. 
	 * Combined with the size of the room, and who attacks first,
	 * this can set the distance between two combatants.
	 * @see Environmental#maxRange()
	 * @see Environmental#minRange()
	 * @param min the minimum range 0=melee
	 * @param max the maximum range 0=melee
	 */
	public void setRanges(int min, int max);

	/**
	 * Gets the string that would be shown if someone gets a hit with
	 * this weapon. 
	 * @see Weapon#missString()
	 * @param damageAmount the amount of damage done with this weapon
	 * @return the message string to show someone hit with this weapon
	 */
	public String hitString(int damageAmount);

	/**
	 * Gets the string that would be shown if someone misses with
	 * this weapon. 
	 * @see Weapon#hitString(int)
	 * @return the message string to show someone misses with this weapon
	 */
	public String missString();
}
