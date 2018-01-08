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
   Copyright 2011-2018 Bo Zimmerman

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

public class Skill_Satire extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Satire";
	}

	private final static String localizedName = CMLib.lang().L("Satire");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"SATIRE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;
	}

	public void criminalFail(LegalBehavior B, Area A2, MOB mob, MOB witness)
	{
		final String crime="disrespect for the law";
		final String desc="Everyone should respect the law.";
		final String crimeLocs="";
		final String crimeFlags="!witness";
		final String sentence=Law.PUNISHMENT_DESCS[0];
		B.addWarrant(A2,mob,witness,crimeLocs,crimeFlags,crime,sentence,desc);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(mob==target)
		{
			mob.tell(L("Mock whom?!"));
			return false;
		}
		LegalBehavior B=null;
		Area A2=null;
		final Vector<MOB> forgivables=new Vector<MOB>();
		final Room room=mob.location();
		if(room==null)
		{
			return false;
		}
		B=CMLib.law().getLegalBehavior(room);
		A2=CMLib.law().getLegalObject(room);
		if((B==null)||((!B.isAnyOfficer(A2, target))&&(!B.isJudge(A2, target))))
		{
			mob.tell(mob,target,null,L("<T-NAME> is not an officer here."));
			return false;
		}
		final Set<MOB> group = mob.getGroupMembers(new HashSet<MOB>());
		for(final MOB M : group)
		{
			if((M.location()==room)
			&&(M!=mob)
			&& B.hasWarrant(A2,M))
			{
				forgivables.add(M);
			}
		}

		if(!CMLib.flags().canBeHeardSpeakingBy(mob, target))
		{
			mob.tell(mob,target,null,L("<T-NAME> can't hear you."));
			return false;
		}

		if(forgivables.size()==0)
		{
			mob.tell(L("Noone you know is wanted for anything here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;

		final boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(!success)
		{
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to mock <T-NAME>, but <S-IS-ARE> not funny."));
			if(CMLib.dice().rollPercentage()>mob.charStats().getStat(CharStats.STAT_CHARISMA))
				criminalFail(B,A2,mob,target);
			return false;
		}
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_JUSTICE,L("<S-NAME> mock(s) <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(final MOB M : forgivables)
			{
				if(B.aquit(A2, M, null))
				{
					room.show(M, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME>, smiling, forget(s) <S-YOUPOSS> crime."));
					return false;
				}
			}
			if((msg.value()>0)||(CMLib.dice().rollPercentage()<(25-mob.charStats().getStat(CharStats.STAT_CHARISMA))))
			{
				criminalFail(B,A2,mob,target);
			}
		}
		return success;
	}

}
