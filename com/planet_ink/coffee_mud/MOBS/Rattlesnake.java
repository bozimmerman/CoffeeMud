package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Rattlesnake extends StdMOB
{
	public String ID(){return "Rattlesnake";}
	public Rattlesnake()
	{
		super();
		Username="a rattlesnake";
		setDescription("A fearsome creature with long fangs and an effective warning for the unwary.");
		setDisplayText("A rattlesnake shakes his tale at you furiously");
		setAlignment(500);
		setMoney(0);

		addBehavior(CMClass.getBehavior("CombatAbilities"));
		addAbility(CMClass.getAbility("Poison"));

		baseEnvStats().setDamage(4);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(10);

		baseCharStats().setMyRace(CMClass.getRace("Snake"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
