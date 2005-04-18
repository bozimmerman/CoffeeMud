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
public class BlackBear extends StdMOB
{
	public String ID(){return "BlackBear";}
	public BlackBear()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Black Bear";
		setDescription("A bear, husky with black fur.");
		setDisplayText("A black bear ambles around.");
		Factions.setAlignment(this,Faction.ALIGN_NEUTRAL);
		setMoney(0);
		baseEnvStats.setWeight(20 + Math.abs(randomizer.nextInt() % 45));
		setWimpHitPoint(2);

		baseEnvStats.setWeight(250 + Math.abs(randomizer.nextInt() % 55));


		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseCharStats().setStat(CharStats.STRENGTH,16);
		baseCharStats().setStat(CharStats.DEXTERITY,18);
		baseCharStats().setMyRace(CMClass.getRace("Bear"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(3);
		baseEnvStats().setArmor(70);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
