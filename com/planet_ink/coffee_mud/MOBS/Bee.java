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
public class Bee extends StdMOB
{
	public String ID(){return "Bee";}
	public Bee()
	{
		super();

		Username="a bee";
		setDescription("It\\`s a small buzzing insect with a nasty stinger on its butt.");
		setDisplayText("A bee buzzes around here.");
		Factions.setAlignment(this,Faction.ALIGN_NEUTRAL);
		setMoney(0);
		baseEnvStats.setWeight(1);
		setWimpHitPoint(2);

		addBehavior(CMClass.getBehavior("Follower"));
		addBehavior(CMClass.getBehavior("CombatAbilities"));
		baseEnvStats().setDamage(1);

		baseCharStats().setStat(CharStats.INTELLIGENCE,1);
		baseEnvStats().setDisposition(EnvStats.IS_FLYING);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(10);

		baseCharStats().setMyRace(CMClass.getRace("Insect"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));
		Ability A=CMClass.getAbility("Poison_BeeSting");
		if(A!=null) {
			A.setProfficiency(100);
			addAbility(A);
		}

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
