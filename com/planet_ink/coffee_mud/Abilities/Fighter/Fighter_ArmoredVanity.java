package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Fighter_ArmoredVanity extends FighterSkill implements Runnable
{
	@Override
	public String ID()
	{
		return "Fighter_ArmoredVanity";
	}

	private final static String localizedName = CMLib.lang().L("Armored Vanity");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
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
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}

	protected volatile int bonus = -1;
	protected volatile boolean enabled = false;

	protected void calculateBonus(final MOB mob)
	{
		double bonus = 0;
		if(mob==null)
			return;
		int max = 1 + super.getXLEVELLevel(mob);
		for(final Enumeration<Item> i = mob.items();i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if((I instanceof Armor)
			&& (I.amBeingWornProperly())
			&& (!I.amWearingAt(Item.WORN_FLOATING_NEARBY))
			&& (I.basePhyStats().armor()>1)
			&& (CMLib.itemBuilder().calculateBaseValue(I)*3<I.baseGoldValue())
			&& ((--max)>=0))
				bonus += CMath.div(I.basePhyStats().armor(),2.0);
		}
		if((bonus > 0)&&(proficiency()<100))
			this.bonus = (int)Math.round(bonus * CMath.div(proficiency(), 100.0));
		else
			this.bonus = (int)bonus;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((!(affected instanceof MOB))||(!enabled))
			return;
		if(bonus <= 0)
			calculateBonus((MOB)affected);
		if(bonus > 0)
			affectableStats.setArmor(affectableStats.armor()-bonus);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(!(ticking instanceof MOB))
			return true;
		final MOB mob=(MOB)ticking;
		if(!mob.isInCombat())
		{
			if(enabled)
			{
				enabled = false;
				mob.recoverPhyStats();
			}
			return true;
		}
		if((CMLib.flags().canBeSeenBy(mob.getVictim(),mob))
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(mob,0,false)))
		{
			if(!enabled)
			{
				enabled = true;
				mob.recoverPhyStats();
			}
			if((bonus>0)&&(CMLib.dice().rollPercentage()==1))
				helpProficiency(mob, 0);
		}
		else
		{
			if(enabled)
			{
				enabled = false;
				mob.recoverPhyStats();
			}
		}
		return true;
	}

	@Override
	public void run()
	{
		this.bonus = -1;
		// if you really need to recover in here, it's ok
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if((msg.source()==affected)
		&&(msg.target() instanceof Item))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_REMOVE:
				if(msg.trailerRunnables()==null)
					msg.addTrailerRunnable(this);
				break;
			}
		}
	}
}
