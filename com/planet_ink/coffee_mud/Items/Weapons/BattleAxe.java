package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class BattleAxe extends Sword
{
	public String ID(){	return "BattleAxe";}
	public BattleAxe()
	{
		super();

		setName("a battle axe");
		setDisplayText("a heavy battle axe sits here");
		setDescription("It has a stout pole, about 4 feet in length with a trumpet shaped blade.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(15);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseGoldValue=35;
		wornLogicalAnd=true;
		material=EnvResource.RESOURCE_STEEL;
		properWornBitmap=Item.HELD|Item.WIELD;
		recoverEnvStats();
		weaponType=Weapon.TYPE_SLASHING;
		weaponClassification=Weapon.CLASS_AXE;
	}


}
