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
public class Minotaur extends StdMOB
{
	public String ID(){return "Minotaur";}
	public Minotaur()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a minotaur";
		setDescription("A tall humanoid with the head of a bull, and the body of a very muscular man.  It\\`s covered in red fur.");
		setDisplayText("A minotaur glares at you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(350 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,4 + Math.abs(randomizer.nextInt() % 5));
		baseCharStats().setStat(CharStats.STRENGTH,18);
		baseCharStats().setStat(CharStats.DEXTERITY,15);
		baseCharStats().setMyRace(CMClass.getRace("Minotaur"));
		baseCharStats().getMyRace().startRacing(this,false);

		Weapon mainWeapon=CMClass.getWeapon("BattleAxe");
		if(mainWeapon!=null)
		{
			mainWeapon.wearAt(Item.WIELD);
			this.addInventory(mainWeapon);
		}

		baseEnvStats().setDamage(12);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(60);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
