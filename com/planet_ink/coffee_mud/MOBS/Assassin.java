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
public class Assassin extends GenMob
{
	public String ID(){return "Assassin";}
	public Assassin()
	{
		super();
		Username="an assassin";
		setDescription("He`s all dressed in black, and has eyes as cold as ice.");
		setDisplayText("An assassin stands here.");
		Race R=CMClass.getRace("Human");
		if(R!=null)
		{
			baseCharStats().setMyRace(R);
			R.startRacing(this,false);
		}
		baseCharStats().setStat(CharStats.DEXTERITY,18);
		baseCharStats().setStat(CharStats.GENDER,'M');
		baseCharStats().setStat(CharStats.WISDOM,18);
		baseEnvStats().setSensesMask(baseEnvStats().disposition()|EnvStats.CAN_SEE_DARK);

		Ability A=CMClass.getAbility("Thief_Hide");
		if(A!=null)
		{
			A.setProfficiency(100);
			addAbility(A);
		}
		A=CMClass.getAbility("Thief_Sneak");
		if(A!=null)
		{
			A.setProfficiency(100);
			addAbility(A);
		}
		A=CMClass.getAbility("Thief_BackStab");
		if(A!=null)
		{
			A.setProfficiency(100);
			addAbility(A);
		}
		A=CMClass.getAbility("Thief_Assassinate");
		if(A!=null)
		{
			A.setProfficiency(100);
			addAbility(A);
		}
		Item I=CMClass.getWeapon("Longsword");
		if(I!=null)
		{
			addInventory(I);
			I.wearAt(Item.WIELD);
		}
		I=CMClass.getArmor("LeatherArmor");
		if(I!=null)
		{
			addInventory(I);
			I.wearIfPossible(this);
		}
		Weapon d=CMClass.getWeapon("Dagger");
		if(d!=null)
		{
			d.wearAt(Item.HELD);
			addInventory(d);
		}


		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

}
