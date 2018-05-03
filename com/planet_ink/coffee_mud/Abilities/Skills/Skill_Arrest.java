package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Skill_Arrest extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Arrest";
	}

	private final static String localizedName = CMLib.lang().L("Arrest");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"ARREST"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL;
	}

	public static List<LegalWarrant> getWarrantsOf(MOB target, Area legalA)
	{
		LegalBehavior B=null;
		if(legalA!=null)
			B=CMLib.law().getLegalBehavior(legalA);
		List<LegalWarrant> warrants=new Vector<LegalWarrant>();
		if(B!=null)
		{
			warrants=B.getWarrantsOf(legalA,target);
			for(int i=warrants.size()-1;i>=0;i--)
			{
				final LegalWarrant W=warrants.get(i);
				if(W.crime().equalsIgnoreCase("pardoned"))
					warrants.remove(i);
			}
		}
		return warrants;
	}

	public void makePeace(Room R, MOB mob, MOB target)
	{
		if(R==null)
			return;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB inhab=R.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
				if(inhab.getVictim()==mob)
					inhab.makePeace(true);
				if(inhab.getVictim()==target)
					inhab.makePeace(true);
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if((mob==target)&&(!auto))
		{
			mob.tell(L("You can not arrest yourself."));
			return false;
		}

		if(Skill_Arrest.getWarrantsOf(target, CMLib.law().getLegalObject(mob.location().getArea())).size()==0)
		{
			mob.tell(L("@x1 has no warrants out here.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!auto)
		{
			if(mob.baseWeight()<(target.baseWeight()-450))
			{
				mob.tell(L("@x1 is way to big for you!",target.name(mob)));
				return false;
			}
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		levelDiff-=(abilityCode()*mob.charStats().getStat(CharStats.STAT_STRENGTH));

		// now see if it worked
		final boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_STRENGTH)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto);
		if(success)
		{
			Ability A=CMClass.getAbility("Skill_ArrestingSap");
			if(A!=null)
			{
				A.setProficiency(100);
				A.setAbilityCode(10);
				A.invoke(mob,target,true,0);
			}
			if(CMLib.flags().isSleeping(target))
			{
				makePeace(mob.location(),mob,target);
				A=target.fetchEffect("Skill_ArrestingSap");
				if(A!=null)
					A.unInvoke();
				A=CMClass.getAbility("Skill_HandCuff");
				if(A!=null)
					A.invoke(mob,target,true,0);
				makePeace(mob.location(),mob,target);
				mob.tell(L("You'll have to PULL @x1 to the judge now before he gets out of the cuffs.",target.charStats().himher()));
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> rear(s) back and attempt(s) to knock <T-NAMESELF> out, but fail(s)."));

		// return whether it worked
		return success;
	}
}
