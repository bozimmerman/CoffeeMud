package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2023-2025 Bo Zimmerman

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
public class Fighter_FierceMount extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_FierceMount";
	}

	private final static String localizedName = CMLib.lang().L("Fierce Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedDisplayText = CMLib.lang().L("(Fierce Mount)");

	@Override
	public String displayText()
	{
		return (canBeUninvoked() ? localizedDisplayText : "");
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return !super.isNowAnAutoEffect();
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(canBeUninvoked()
		&& (affected instanceof Rideable)
		&&(((Rideable)affected).numRiders()>0))
		{
			final int level;
			if(invoker() != null)
				level=2+(adjustedLevel(invoker(),0)/10);
			else
			if(affected instanceof MOB)
				level=2+(adjustedLevel((MOB)affected,0)/10);
			else
				level=10;
			final double damBonus=CMath.mul(CMath.div(proficiency(),100.0),level);
			final double attBonus=CMath.mul(CMath.div(proficiency(),100.0),2*level);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(attBonus));
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(damBonus));
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB))
		{
			final MOB M = (MOB)affected;
			if((M.riding() instanceof MOB)
			&&(invoker()==M)
			&&(!canBeUninvoked()))
			{
				if(M.isInCombat())
				{
					final MOB rideM = (MOB)M.riding();
					if(CMLib.dice().rollPercentage()==1)
						super.helpProficiency((MOB)affected, 0);
					if(rideM.fetchEffect(ID())==null)
					{
						final Ability fierceA = CMClass.getAbility(ID());
						fierceA.setInvoker(M);
						rideM.addEffect(fierceA);
						fierceA.makeLongLasting();
						rideM.recoverPhyStats();
					}
				}
			}
			else
			if((M instanceof Rideable)
			&&(canBeUninvoked())
			&&(invoker()!=null))
			{
				final Rideable rideMeM = (Rideable)M;
				if((rideMeM.numRiders()==0)
				||(!invoker().isInCombat())
				||(!rideMeM.amRiding(invoker())))
				{
					unInvoke();
					return false;
				}
			}
		}
		return true;
	}
}
