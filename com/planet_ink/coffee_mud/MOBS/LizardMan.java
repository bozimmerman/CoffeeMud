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
public class LizardMan extends StdMOB
{
	public String ID(){return "LizardMan";}
	public LizardMan()
	{
		super();
		Username="a Lizard Man";
		setDescription("a 6 foot tall reptilian humanoid.");
		setDisplayText("A mean looking Lizard Man stands here.");
		Factions.setAlignment(this,Faction.ALIGN_EVIL);
		setMoney(20);
		baseEnvStats.setWeight(225);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,6);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setStat(CharStats.STRENGTH,18);

		baseCharStats().setMyRace(CMClass.getRace("LizardMan"));
		baseEnvStats().setAbility(0);
		baseEnvStats().setDamage(6);
		baseEnvStats().setSpeed(3);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
