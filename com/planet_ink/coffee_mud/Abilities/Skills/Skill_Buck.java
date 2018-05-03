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
   Copyright 2016-2018 Bo Zimmerman

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

public class Skill_Buck extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Buck";
	}

	private final static String	localizedName	= CMLib.lang().L("Buck");

	@Override
	public String name()
	{
		return localizedName;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BUCK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob instanceof Rideable)
			&&(((Rideable)mob).numRiders()>0))
				return Ability.QUALITY_BENEFICIAL_SELF;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!(mob instanceof Rideable))
		||(((Rideable)mob).numRiders()==0))
		{
			mob.tell(L("No one and nothing is not mounted on you!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Rideable R=(Rideable)mob;
		int avgDex=0;
		for(int i=0;i<R.numRiders();i++)
		{
			Rider r=R.fetchRider(i);
			if(r instanceof MOB)
				avgDex+=((MOB)r).charStats().getStat(CharStats.STAT_DEXTERITY);
		}
		avgDex = avgDex / R.numRiders();
		
		int adj = ((mob.charStats().getStat(CharStats.STAT_STRENGTH)*2) - (avgDex*3)) + (2*getXLEVELLevel(mob));
		final boolean success=proficiencyCheck(mob,adj,auto);

		String str=null;
		if(success)
		{
			str=auto?L("<T-NAME> is bucked!"):L("<S-NAME> buck(s) <T-NAME> off <S-NAMESELF>!");
			final Room roomR=CMLib.map().roomLocation(mob);
			List<Rider> targets=new ArrayList<Rider>(R.numRiders());
			for(int r=0;r<R.numRiders();r++)
				targets.add(R.fetchRider(r));
			for(Rider target : targets)
			{
				if(target instanceof MOB)
				{
					final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.MASK_SOUND|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
					if(roomR.okMessage(mob,msg))
					{
						roomR.send(mob,msg);
						if(msg.value() <=0)
						{
							roomR.show(mob, target, CMMsg.MSG_OK_ACTION, str);
							target.setRiding(null);
						}
					}
				}
				else
				{
					roomR.show(mob, target, CMMsg.MSG_OK_ACTION, str);
					target.setRiding(null);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to buck off <S-HIS-HER> riders, but fails."));

		return success;
	}

}
