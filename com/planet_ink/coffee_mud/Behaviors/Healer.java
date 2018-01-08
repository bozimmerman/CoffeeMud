package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Healer extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "Healer";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	protected static final List<Ability> healingVector=new Vector<Ability>();

	public Healer()
	{
		super();
		minTicks=10; maxTicks=20; chance=100;
		tickReset();
		if(healingVector.size()==0)
		{
			healingVector.add(CMClass.getAbility("Prayer_CureBlindness"));
			healingVector.add(CMClass.getAbility("Prayer_CureDisease"));
			healingVector.add(CMClass.getAbility("Prayer_CureLight"));
			healingVector.add(CMClass.getAbility("Prayer_RemoveCurse"));
			healingVector.add(CMClass.getAbility("Prayer_Bless"));
			healingVector.add(CMClass.getAbility("Prayer_Sanctuary"));
		}
	}

	@Override
	public String accountForYourself()
	{
		return "benevolent healing";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			final Room thisRoom=mob.location();
			if(thisRoom==null)
				return true;

			final double aChance=CMath.div(mob.curState().getMana(),mob.maxState().getMana());
			if((Math.random()>aChance)||(mob.curState().getMana()<50))
				return true;

			if(thisRoom.numPCInhabitants()>0)
			{
				final MOB target=thisRoom.fetchRandomInhabitant();
				MOB followMOB=target;
				if((target != null)&&(target.amFollowing()!=null))
					followMOB=target.amUltimatelyFollowing();
				if((target!=null)
				&&(target!=mob)
				&&(followMOB.getVictim()!=mob)
				&&(!followMOB.isMonster()))
				{
					final Ability tryThisOne=healingVector.get(CMLib.dice().roll(1,healingVector.size(),-1));
					Ability thisOne=mob.fetchAbility(tryThisOne.ID());
					if(thisOne==null)
					{
						thisOne=(Ability)tryThisOne.copyOf();
						thisOne.setSavable(false);
						mob.addAbility(thisOne);
					}
					thisOne.setProficiency(100);
					final Vector<String> V=new Vector<String>();
					if(!target.isMonster())
						V.addElement(target.name());
					thisOne.invoke(mob,V,target,false,0);
				}
			}
		}
		return true;
	}
}
