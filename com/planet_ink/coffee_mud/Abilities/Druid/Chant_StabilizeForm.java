package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2024-2025 Bo Zimmerman

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
public class Chant_StabilizeForm extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_StabilizeForm";
	}

	private final static String localizedName = CMLib.lang().L("Stabilize Form");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Stabilize Form)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PRESERVING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your form stabilization fades."));

		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(!mob.amDead())
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&&(msg.tool() instanceof Ability)
		&&(CMath.bset(((Ability)msg.tool()).flags(), Ability.FLAG_POLYMORPHING)))
		{
			if((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false))
			{
				mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> stablized form absorbs the @x1 from <T-NAME>!",msg.tool().name()));
				return false;
			}
			else
				msg.source().tell(L("Your form stabilization fails."));
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already has a stabilized form."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					L(auto?"A stabilization field envelopes <T-NAME>!":"^S<S-NAME> chant(s) for a stabilized form.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability eA = beneficialAffect(mob,target,asLevel,0);
				if(eA != null)
				{
					final Ability effA = target.fetchEffect("Chant_Reincarnation");
					if(effA != null)
						effA.setStat("TICKDOWN", "1");
					for(final Enumeration<Ability> a = target.effects();a.hasMoreElements();)
					{
						final Ability A = a.nextElement();
						if((A!=null)
						&&CMath.bset(A.flags(), Ability.FLAG_POLYMORPHING)
						&&(A!=effA)&&(A!=eA)
						&&(CMath.s_int(A.getStat("TICKDOWN"))>0)
						&&(A.canBeUninvoked())
						&&(!A.isNowAnAutoEffect()))
						{
							int tickBonus = 50;
							if(A instanceof StdAbility)
								tickBonus = ((StdAbility) A).getTickdownTime(mob, target, asLevel, 0);
							A.setStat("TICKDOWN", ""+(A.getStat("TICKDOWN")+tickBonus));
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s), but nothing happens."));

		return success;
	}
}
