package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2022-2025 Bo Zimmerman

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
public class Fighter_ForearmBlock extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_ForearmBlock";
	}

	private final static String	localizedName	= CMLib.lang().L("Forearm Block");

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
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_EVASIVE;
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

	protected volatile int	triesThisRound = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			triesThisRound=0;
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(CMLib.flags().isAliveAwakeMobile(mob,true))
		&&(msg.source().rangeToTarget()==0)
		&&(CMLib.flags().canBeSeenBy(mob, msg.source()))
		&&(!CMLib.flags().isBoundOrHeld(mob))
		&&(mob.charStats().getBodyPart(Race.BODY_ARM)>0)
		&&((msg.tool()==null)
			||((msg.tool() instanceof Weapon)&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
		&&(msg.source().getVictim()==mob))
		{
			final CMMsg msg2=CMClass.getMsg(mob,msg.source(),this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> block(s) the attack by <T-NAME>!"));
			if(((++triesThisRound)<(mob.phyStats().speed()+CMath.div(super.getXLEVELLevel(mob),3.0)))
			&&(proficiencyCheck(null,mob.charStats().getStat(CharStats.STAT_DEXTERITY)-93+(getXLEVELLevel(mob)),false))
			&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg2);
				helpProficiency(mob, 0);
				return false;
			}
		}
		return true;
	}
}
