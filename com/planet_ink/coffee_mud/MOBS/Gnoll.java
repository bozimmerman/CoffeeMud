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
public class Gnoll extends StdMOB
{
	public String ID(){return "Gnoll";}
	public Gnoll()
	{
		super();
		Username="a Gnoll";
		setDescription("a 7 foot tall creature with a body resembling a large human and the head of a hyena.");
		setDisplayText("A nasty Gnoll stands here.");
		setAlignment(0);
		setMoney(20);
		baseEnvStats.setWeight(300);
		setWimpHitPoint(0);

		Weapon h=CMClass.getWeapon("MorningStar");
		Random randomizer = new Random(System.currentTimeMillis());
		int percentage = randomizer.nextInt() % 100;
		if((percentage & 1) == 0)
		{
		   h = CMClass.getWeapon("Longsword");
		}
		if(h!=null)
		{
			h.wearAt(Item.WIELD);
			addInventory(h);
		}

		baseCharStats().setStat(CharStats.INTELLIGENCE,6);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setStat(CharStats.STRENGTH,22);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
