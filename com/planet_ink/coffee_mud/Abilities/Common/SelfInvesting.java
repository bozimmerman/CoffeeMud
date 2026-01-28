package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Skills.StdSkill;
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
   Copyright 2026-2026 Bo Zimmerman

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
public class SelfInvesting extends StdSkill
{
	@Override
	public String ID()
	{
		return "SelfInvesting";
	}

	private final static String localizedName = CMLib.lang().L("Self Investing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "SELFINVEST", "SELFINVESTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ECONOMIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
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
	public long flags()
	{
		return super.flags() | Ability.FLAG_NOORDERING;
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
	public int usageType()
	{
		return USAGE_MANA;
	}

	private volatile long lastInvestment = 0;
	private volatile double bankedValue = 0.0;

	@Override
	public void setMiscText(final String newMiscText)
	{
		lastInvestment = CMParms.getParmLong(newMiscText, "LASTUSE", 0);
		bankedValue = CMParms.getParmDouble(newMiscText, "BANKED", 0);
	}

	@Override
	public String text()
	{
		final StringBuilder str = new StringBuilder("");
		if(lastInvestment > 0)
			str.append("LASTUSE=").append(lastInvestment).append(" ");
		if(bankedValue > 0)
			str.append("BANKED=").append(bankedValue).append(" ");
		return str.toString();
	}

	protected SelfInvesting getVarHolder(final MOB mob)
	{
		if(mob == null)
			return this;
		final SelfInvesting A = (SelfInvesting)mob.fetchAbility(ID());
		if(A == null)
			return this;
		return A;
	}

	protected long getLastInvestmentDay(final MOB mob)
	{
		return getVarHolder(mob).lastInvestment;
	}

	protected void setLastInvestmentDay(final MOB mob, final long day)
	{
		getVarHolder(mob).lastInvestment = day;
	}

	protected double getBankedValue(final MOB mob)
	{
		return getVarHolder(mob).bankedValue;
	}

	protected void setBankedValue(final MOB mob, final double value)
	{
		getVarHolder(mob).bankedValue = value;
	}

	/**
	 * Gets the current MUD day as a long value for comparison.
	 */
	protected long getCurrentMudDay(final MOB mob)
	{
		final TimeClock clock = CMLib.time().homeClock(mob);
		if(clock == null)
			return 0;
		return (((clock.getYear() * clock.getMonthsInYear()) + clock.getMonth()) * clock.getDaysInMonth()) + clock.getDayOfMonth();
	}

	/**
	 * Gets the maximum bank value (XP needed to gain next level).
	 */
	protected int getMaxBankValue(final MOB mob)
	{
		if(mob == null)
			return 0;
		final int neededXP = mob.getExpNeededLevel();
		if(neededXP == Integer.MAX_VALUE)
			return 5000; // Default for max level characters
		return CMLib.leveler().getLevelExperienceJustThisLevel(mob, mob.phyStats().level());
	}

	/**
	 * Gets the qualifying class level for this ability.
	 */
	protected int getQualifyingLevel(final MOB mob, final int asLevel)
	{
		return super.adjustedLevel(mob, asLevel) + super.getXLEVELLevel(mob);
	}

	/**
	 * Gets the maximum XP that can be transferred per use.
	 */
	protected int getMaxXPPerUse(final MOB mob, final int asLevel)
	{
		return 100 * getQualifyingLevel(mob, asLevel);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);

		if((msg.source() == affected)
		&& (msg.targetMinor() == CMMsg.TYP_SELL)
		&& (msg.tool() instanceof Item)
		&& (affected instanceof MOB))
		{
			final MOB mob = (MOB)affected;
			final Item soldItem = (Item)msg.tool();
			final int itemValue = soldItem.value();
			if(itemValue > 0)
			{
				final int maxBank = getMaxBankValue(mob);
				double currentBank = getBankedValue(mob);
				currentBank += itemValue;
				if(currentBank > maxBank)
					currentBank = maxBank;
				setBankedValue(mob, currentBank);
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isPlayer())
		{
			mob.tell(L("Only players can use this skill."));
			return false;
		}

		final double bankValue = this.getBankedValue(mob);
		final int maxXPPerUse = getMaxXPPerUse(mob, asLevel);
		final int maxBank = getMaxBankValue(mob);
		final long currentMudDay = getCurrentMudDay(mob);
		final long lastUseDay = this.getLastInvestmentDay(mob);

		if(commands.size() == 0)
		{
			final StringBuilder str = new StringBuilder();
			str.append(L("^HYour Self-Investing Bank:^N\n\r"));
			str.append(L("  Available funds: ^W@x1^N worth of sales\n\r", "" + bankValue));
			str.append(L("  Maximum bank: ^W@x1^N\n\r", "" + maxBank));
			str.append(L("  Maximum XP per transfer: ^W@x1^N\n\r", "" + maxXPPerUse));
			str.append(L("  Transfer rate: 1 gold = 1 XP\n\r"));
			if(lastUseDay >= currentMudDay)
				str.append(L("  ^rYou have already invested today. Wait until the next day.^N\n\r"));
			else
				str.append(L("  ^gYou may invest today.^N\n\r"));
			str.append(L("\n\rUsage: SELFINVEST <amount> to convert gold to experience."));
			mob.tell(str.toString());
			return true;
		}

		if(lastUseDay >= currentMudDay)
		{
			mob.tell(L("You have already invested in yourself today. You must wait until the next day."));
			return false;
		}

		final String amountStr = CMParms.combine(commands, 0);
		int amount = 0;
		if(amountStr.equalsIgnoreCase("all") || amountStr.equalsIgnoreCase("max"))
		{
			amount = (int)Math.min(bankValue, maxXPPerUse);
		}
		else
		if(CMath.isInteger(amountStr))
		{
			amount = CMath.s_int(amountStr);
		}
		else
		{
			mob.tell(L("'@x1' is not a valid amount. Use SELFINVEST <number> or SELFINVEST ALL.", amountStr));
			return false;
		}

		if(amount <= 0)
		{
			mob.tell(L("You must specify a positive amount to invest."));
			return false;
		}

		if(amount > bankValue)
		{
			mob.tell(L("You only have @x1 gold worth of sales available in your self-investment bank.", "" + bankValue));
			return false;
		}

		if(amount > maxXPPerUse)
		{
			mob.tell(L("You can only invest up to @x1 experience at a time.", "" + maxXPPerUse));
			amount = maxXPPerUse;
		}

		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		final boolean success = super.proficiencyCheck(mob, 0, auto);
		if(success)
		{
			final CMMsg msg = CMClass.getMsg(mob, null, this,
					CMMsg.MSG_NOISYMOVEMENT,
					L("<S-NAME> invest(s) <S-HIS-HER> sales earnings into personal experience."));
			if(mob.location().okMessage(mob, msg))
			{
				mob.location().send(mob, msg);
				setBankedValue(mob, bankValue - amount);
				setLastInvestmentDay(mob, currentMudDay);
				CMLib.leveler().postExperience(mob, "ABILITY:" + ID(), null, null, amount, true);
				mob.tell(L("You have converted @x1 worth of sales into @x1 experience points.", "" + amount));
				mob.tell(L("Remaining bank balance: @x1.", "" + (bankValue - amount)));
			}
		}
		else
			super.beneficialVisualFizzle(mob, null, L("<S-NAME> fail(s) to invest in <S-HIM-HERSELF>."));

		return success;
	}
}
