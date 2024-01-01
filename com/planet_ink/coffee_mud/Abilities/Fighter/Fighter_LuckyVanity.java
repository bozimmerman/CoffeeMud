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
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_LuckyVanity extends FighterSkill implements Runnable
{
	@Override
	public String ID()
	{
		return "Fighter_LuckyVanity";
	}

	private final static String localizedName = CMLib.lang().L("Lucky Vanity");

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

	protected volatile double gearBonus = -1;
	protected volatile double bonus = -1;

	protected void calculateBonus(final MOB mob)
	{
		double bonus = 0;
		if(mob==null)
			return;
		final Room R = mob.location();
		if((R==null)||(R.numInhabitants()<((mob.riding() instanceof MOB)?3:2)))
		{
			this.bonus=0;
			return;
		}
		boolean seen = false;
		final CMFlagLibrary flagLib = CMLib.flags();
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M = m.nextElement();
			if((M != mob)
			&&(M != mob.riding())
			&&(flagLib.canBeSeenBy(mob, M)))
			{
				seen=true;
				break;
			}
		}
		if(!seen)
		{
			this.bonus=0;
			return;
		}
		if(this.gearBonus>=0)
		{
			this.bonus = this.gearBonus;
			return;
		}
		int max = 1 + super.getXLEVELLevel(mob);
		for(final Enumeration<Item> i = mob.items();i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if((I instanceof Armor)
			&&(I.rawWornCode() != 0)
			&& (I.amBeingWornProperly())
			&& (!I.amWearingAt(Item.WORN_FLOATING_NEARBY))
			&& (CMLib.itemBuilder().calculateBaseValue(I)*3<I.baseGoldValue())
			&& ((--max)>=0))
				bonus += 5;
		}
		if((bonus > 0)&&(proficiency()<100))
			this.bonus = bonus * CMath.div(proficiency(), 100.0);
		else
			this.bonus = bonus;
		this.gearBonus = bonus;
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats charStats)
	{
		if(bonus < 0)
			calculateBonus(affectedMob);
		if(bonus > 0)
		{
			charStats.setStat(CharStats.STAT_SAVE_MIND, charStats.getStat(CharStats.STAT_SAVE_MIND)+(int)bonus);
			charStats.setStat(CharStats.STAT_SAVE_POISON, charStats.getStat(CharStats.STAT_SAVE_POISON)+(int)bonus);
			charStats.setStat(CharStats.STAT_SAVE_PARALYSIS, charStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+(int)bonus);
			charStats.setStat(CharStats.STAT_SAVE_DISEASE, charStats.getStat(CharStats.STAT_SAVE_DISEASE)+(int)bonus);
			charStats.setStat(CharStats.STAT_SAVE_TRAPS, charStats.getStat(CharStats.STAT_SAVE_TRAPS)+(int)bonus);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(!(ticking instanceof MOB))
			return true;
		final MOB mob=(MOB)ticking;
		if((bonus>0)&&(CMLib.dice().rollPercentage()==1))
			helpProficiency(mob, 0);
		return true;
	}

	@Override
	public void run()
	{
		this.bonus = -1;
		final MOB mob = (affecting() instanceof MOB)?(MOB)affecting():null;
		if(mob != null)
			mob.recoverPhyStats();
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
			{
				this.gearBonus = -1;
				if(msg.trailerRunnables()==null)
					msg.addTrailerRunnable(this);
				break;
			}
			}
		}
		else
		if((msg.target() instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER))
		{
			if(msg.trailerRunnables()==null)
				msg.addTrailerRunnable(this);
		}
	}
}
