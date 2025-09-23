package com.planet_ink.coffee_mud.Abilities.Misc;
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
   Copyright 2018-2025 Bo Zimmerman

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
public class Adorable extends StdAbility
{
	@Override
	public String ID()
	{
		return "Adorable";
	}

	private final static String	localizedName	= CMLib.lang().L("Adorable");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Adorable)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(msg.target()==affected)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(msg.target() instanceof MOB))
		{
			final MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().location()==target.location())
			&&(msg.source().getVictim()!=target)
			&&(CMLib.flags().canBeSeenBy(msg.target(), msg.source()))
			&&(msg.source().phyStats().level() < (target.phyStats().level()-CMProps.getIntVar(CMProps.Int.EXPRATE)+super.getXLEVELLevel(target)))
			&&(!CMSecurity.isAllowed(msg.source(), target.location(), CMSecurity.SecFlag.KILLDEAD))
			&&(!CMLib.law().isLegalOfficerHere(msg.source()))
			&&(proficiencyCheck(target, 0, false))
			&&(!CMLib.law().isLegalJudgeHere(msg.source())))
			{
				msg.source().tell(L("@x1 is just too adorable to do that to.",target.name(msg.source())));
				if(target.getVictim()==msg.source())
				{
					target.makePeace(true);
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats charStats)
	{
		charStats.setStat(CharStats.STAT_CHARISMA, charStats.getStat(CharStats.STAT_CHARISMA)+2+(super.getXLEVELLevel(affectedMob)/2));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already adorable."), commands);
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("<T-NAME> <T-IS-ARE> adorable!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> can't seem to feel adorable right now."));

		// return whether it worked
		return success;
	}
}
