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
public class Skill_Attack2 extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Attack2";
	}

	private final static String localizedName = CMLib.lang().L("Second Attack");

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
		return Ability.ACODE_SKILL | Ability.DOMAIN_MARTIALLORE;
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

	protected int attackToNerf()
	{
		return 2;
	}

	protected int roundToNerf()
	{
		return 1;
	}

	protected double nerfAmount()
	{
		return .8;
	}

	protected double numberOfFullAttacks()
	{
		return 1.0;
	}

	protected int				attacksSinceNerfing	= 0;
	protected int				roundOfNerfing		= 1;
	protected volatile boolean	freeToNerf			= false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if((affected instanceof MOB)&&(((MOB)affected).isInCombat()))
			affectableStats.setSpeed(affectableStats.speed()+(numberOfFullAttacks()*(proficiency()/100.0)));
		if((freeToNerf)&& (affectableStats.attackAdjustment()>0))
			affectableStats.setAttackAdjustment((int)Math.round(affectableStats.attackAdjustment() * nerfAmount()));
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(mob.isInCombat())
		&&(!mob.amDead())
		&&(msg.target() instanceof MOB))
		{
			attacksSinceNerfing++;
			freeToNerf=((attacksSinceNerfing==attackToNerf()) && (roundToNerf()==roundOfNerfing));
			if(freeToNerf)
			{
				if(CMLib.dice().rollPercentage()>97)
					helpProficiency(mob, 0);
				mob.recoverPhyStats();
				//chargen combat fighter iterations=100 skiplevels=4 export=output6.txt 1 91
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		roundOfNerfing++;
		if(roundOfNerfing>roundToNerf())
		{
			roundOfNerfing=1;
			attacksSinceNerfing=0;
			freeToNerf=false;
		}
		return true;
	}
}
