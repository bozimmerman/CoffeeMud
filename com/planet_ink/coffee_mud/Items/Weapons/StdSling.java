package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class StdSling extends StdWeapon
{
	public String ID(){	return "StdSling";}
	public StdSling()
	{
		super();
		setName("a sling");
		setDisplayText("a sling has been left here.");
		setDescription("It looks like it might shoot bullets!");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(8);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(2);
		setAmmunitionType("bullets");
		setAmmoCapacity(50);
		setAmmoRemaining(10);
		baseGoldValue=150;
		recoverEnvStats();
		minRange=1;
		maxRange=2;
		weaponType=Weapon.TYPE_BASHING;
		material=EnvResource.RESOURCE_LEATHER;
		weaponClassification=Weapon.CLASS_RANGED;
		setRawLogicalAnd(false);
	}


}
