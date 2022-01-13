package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2003-2022 Bo Zimmerman

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
public class Chant_PredictPhase extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_PredictPhase";
	}

	private final static String localizedName = CMLib.lang().L("Predict Phase");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_MOONSUMMONING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s) and gaze(s) toward the sky.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(R.getArea().getTimeObj().getMoonPhase(R).getDesc());

				for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
				{
					final Ability eA=a.nextElement();
					if((eA!=null)
					&&((eA.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONSUMMONING))
						mob.tell(L("This place is under the effect of @x1.",eA.name()));
				}
				final int exper = super.getXLEVELLevel(mob);
				if(exper > 0)
				{
					final CharState cs = (CharState)CMClass.getCommon("DefaultCharState");
					cs.setAllValues(0);
					final CharStats ce = (CharStats)CMClass.getCommon("DefaultCharStats");
					ce.setAllValues(0);
					final PhyStats ps = (PhyStats)CMClass.getCommon("DefaultPhyStats");
					ps.setAllValues(0);
					mob.baseCharStats().getCurrentClass().affectCharState(mob, cs);
					mob.baseCharStats().getCurrentClass().affectCharStats(mob, ce);
					mob.baseCharStats().getCurrentClass().affectPhyStats(mob, ps);
					final List<String> report = new ArrayList<String>();
					if(cs.getMovement()>0)
						report.add(L("movement +@x1",""+cs.getMovement()));
					if((exper>=2)&&(cs.getHitPoints()>0))
						report.add(L("hit points +@x1",""+cs.getHitPoints()));
					if((exper>=3)&&(cs.getMana()>0))
						report.add(L("mana +@x1",""+cs.getMana()));
					if((exper>=4)&&(ps.attackAdjustment()>0))
						report.add(L("attack +@x1",""+ps.attackAdjustment()));
					if((exper>=5)&&(ps.armor()<0))
						report.add(L("armor +@x1",""+(-1*ps.armor())));
					if((exper>=6)&&(cs.getMovement()<0))
						report.add(L("movement @x1",""+cs.getMovement()));
					if((exper>=7)&&(cs.getHitPoints()<0))
						report.add(L("hit points @x1",""+cs.getHitPoints()));
					if((exper>=8)&&(cs.getMana()<0))
						report.add(L("mana @x1",""+cs.getMana()));
					if((exper>=9)&&(ps.attackAdjustment()<0))
						report.add(L("attack @x1",""+ps.attackAdjustment()));
					if((exper>=10)&&(ps.armor()>0))
						report.add(L("armor @x1",""+(-1*ps.armor())));
					if(exper>=5)
					{
						final List<String> subReport = new ArrayList<String>();
						for(final int stat : CharStats.CODES.ALLCODES())
						{
							if(ce.getStat(stat)>0)
								subReport.add(CharStats.CODES.NAME(stat).toLowerCase()+" +"+ce.getStat(stat));
							else
								subReport.add(CharStats.CODES.NAME(stat).toLowerCase()+" "+ce.getStat(stat));
						}
						while(subReport.size()>(exper-4))
							subReport.remove(CMLib.dice().roll(1, subReport.size(), -1));
						report.addAll(subReport);
					}
					if(report.size()>0)
						mob.tell(L("The moon is having the following sway over you: @x1",CMLib.english().toEnglishStringList(report)));
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) and gaze(s) toward the sky, but the magic fizzles."));

		return success;
	}
}
