package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class Play_Ballad extends Play
{
	@Override
	public String ID()
	{
		return "Play_Ballad";
	}

	private final static String localizedName = CMLib.lang().L("Ballad");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected String songOf()
	{
		return CMLib.english().startWithAorAn(name());
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// the sex rules
		if(!(affected instanceof MOB))
			return;

		final MOB myChar=(MOB)affected;
		if(msg.target() instanceof MOB)
		{
			final MOB mate=(MOB)msg.target();
			if((msg.amISource(myChar))
			&&(msg.tool() instanceof Social)
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))
			&&(msg.sourceMinor()!=CMMsg.TYP_CHANNEL)
			&&(myChar.charStats().getStat(CharStats.STAT_GENDER)!=mate.charStats().getStat(CharStats.STAT_GENDER))
			&&((mate.charStats().getStat(CharStats.STAT_GENDER)==('M'))
			   ||(mate.charStats().getStat(CharStats.STAT_GENDER)==('F')))
			&&((myChar.charStats().getStat(CharStats.STAT_GENDER)==('M'))
			   ||(myChar.charStats().getStat(CharStats.STAT_GENDER)==('F')))
			&&(myChar.charStats().getMyRace().canBreedWith(mate.charStats().getMyRace()))
			&&(myChar.location()==mate.location())
			&&(myChar.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
			&&(mate.fetchWornItems(Wearable.WORN_LEGS|Wearable.WORN_WAIST,(short)-2048,(short)0).size()==0)
			&&((mate.charStats().getStat(CharStats.STAT_AGE)==0)
					||((mate.charStats().ageCategory()>Race.AGE_CHILD)
							&&(mate.charStats().ageCategory()<Race.AGE_OLD)))
			&&((myChar.charStats().getStat(CharStats.STAT_AGE)==0)
					||((myChar.charStats().ageCategory()>Race.AGE_CHILD)
							&&(myChar.charStats().ageCategory()<Race.AGE_OLD))))
			{
				MOB female=myChar;
				MOB male=mate;
				if((mate.charStats().getStat(CharStats.STAT_GENDER)==('F')))
				{
					female=mate;
					male=myChar;
				}
				final Ability A=CMClass.getAbility("Pregnancy");
				if((A!=null)
				&&(female.fetchAbility(A.ID())==null)
				&&(female.fetchEffect(A.ID())==null))
				{
					A.invoke(male,female,true,0);
					unInvoke();
				}
			}
		}
	}

	@Override
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.STAT_SAVE_MIND,stats.getStat(CharStats.STAT_SAVE_MIND)+adjustedLevel(invoker(),0));
	}

	@Override
	public void affectPhyStats(Physical mob, PhyStats stats)
	{
		super.affectPhyStats(mob,stats);
		if(invoker()!=null)
			stats.setAttackAdjustment(stats.attackAdjustment()
									 +invoker().charStats().getStat(CharStats.STAT_CHARISMA)
									 +adjustedLevel(invoker(),0));
	}
}
