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
public class Teacher extends StdMOB
{
	public String ID(){return "Teacher";}
	public Teacher()
	{
		super();
		Username="Cornelius, Knower of All Things";
		setDescription("He looks wise beyond his years.");
		setDisplayText("Cornelius is standing here contemplating your ignorance.");
		setAlignment(1000);
		setMoney(100);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(200);

		Behavior B=CMClass.getBehavior("MOBTeacher");
		if(B!=null) addBehavior(B);
		B=CMClass.getBehavior("MudChat");
		if(B!=null) addBehavior(B);
		B=CMClass.getBehavior("CombatAbilities");
		if(B!=null) addBehavior(B);

		baseCharStats().setStat(CharStats.INTELLIGENCE,25);
		baseCharStats().setStat(CharStats.WISDOM,25);
		baseCharStats().setStat(CharStats.CHARISMA,25);
		baseCharStats().setStat(CharStats.DEXTERITY,25);
		baseCharStats().setStat(CharStats.STRENGTH,25);
		baseCharStats().setStat(CharStats.CONSTITUTION,25);
		baseCharStats().setMyRace(CMClass.getRace("Human"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(10);
		baseEnvStats().setLevel(25);
		baseEnvStats().setArmor(-500);

		baseState.setHitPoints(4999);
		baseState.setMana(4999);
		baseState.setMovement(4999);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}




}
