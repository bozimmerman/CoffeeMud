package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Skeleton extends Undead
{
	public String ID(){return "Skeleton";}
	public Skeleton()
	{

		super();
		Username="a skeleton";
		setDescription("A walking pile of bones...");
		setDisplayText("a skeleton rattles as it walks.");
		setMoney(0);
		baseEnvStats.setWeight(30);

		Weapon sword=CMClass.getWeapon("Longsword");
		if(sword!=null)
		{
			sword.wearAt(Item.WIELD);
			addInventory(sword);
		}

		baseEnvStats().setDamage(5);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(70);
		baseEnvStats().setSpeed(1.0);

		baseCharStats().setMyRace(CMClass.getRace("Skeleton"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
