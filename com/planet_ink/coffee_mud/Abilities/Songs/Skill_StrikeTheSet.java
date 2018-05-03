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
   Copyright 2014-2018 Bo Zimmerman

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

public class Skill_StrikeTheSet extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_StrikeTheSet";
	}

	private final static String localizedName = CMLib.lang().L("Strike The Set");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
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

	private static final String[] triggerStrings =I(new String[] {"STRIKETHESET","STRIKESET"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_THEATRE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		if((invoker()==null)
		||(invoker().location()!=CMLib.map().roomLocation(affected))
		||(!CMLib.flags().isInTheGame(invoker(),true)))
			unInvoke();
		else
		{
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_UNLOCATABLE);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, int tickID)
	{
		if((invoker()==null)
		||(invoker().location()!=CMLib.map().roomLocation(affected))
		||(!CMLib.flags().isInTheGame(invoker(),true)))
		{
			unInvoke();
		}
		return super.tick(ticking, tickID);
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final Room R=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,R,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> strike(s) the set."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				for(Enumeration<Item> i=R.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					msg.setTarget(I);
					if((I!=null)&&(I.container()==null))
					{
						if(I.okMessage(I, msg))
						{
							I.executeMsg(I, msg);
							beneficialAffect(mob,I,asLevel,Ability.TICKS_ALMOST_FOREVER);
						}
					}
				}
				beneficialAffect(mob,R,asLevel,Ability.TICKS_ALMOST_FOREVER);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> wave(s) <S-HIS-HER> arms around, confusing <S-HIM-HERSELF>."));

		return success;
	}

}
