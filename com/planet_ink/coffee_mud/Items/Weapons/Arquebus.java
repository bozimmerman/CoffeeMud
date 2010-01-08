package com.planet_ink.coffee_mud.Items.Weapons;
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


/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Arquebus extends StdWeapon
{
	public String ID(){	return "Arquebus";}
	public Arquebus()
	{
		super();

		setName("an arquebus");
		setDisplayText("an arquebus is on the ground.");
		setDescription("It\\`s got a metal barrel and wooden stock.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(15);

		baseEnvStats().setAttackAdjustment(-1);
		baseEnvStats().setDamage(10);

		setAmmunitionType("bullets");
		setAmmoCapacity(1);
		setAmmoRemaining(1);
		minRange=0;
		maxRange=5;
		baseGoldValue=500;
		recoverEnvStats();
		wornLogicalAnd=true;
		material=RawMaterial.RESOURCE_IRON;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		weaponClassification=Weapon.CLASS_RANGED;
		weaponType=Weapon.TYPE_PIERCING;
	}



//	protected boolean isBackfire()
//	{
//
//	}

}
