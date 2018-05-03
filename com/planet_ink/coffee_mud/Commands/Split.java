package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Split extends StdCommand
{
	public Split(){}

	private final String[] access=I(new String[]{"SPLIT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(L("Split what, how much?"));
			return false;
		}
		final String itemID=CMParms.combine(commands,1);
		final long numGold=CMLib.english().numPossibleGold(mob,itemID);
		if(numGold>0)
		{
			final String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
			final double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);

			int num=0;
			final Set<MOB> H=mob.getGroupMembers(new SHashSet<MOB>());

			for (final MOB recipientM : H)
			{
				if((!recipientM.isMonster())
				&&(recipientM!=mob)
				&&(recipientM.location()==mob.location())
				&&(mob.location().isInhabitant(recipientM)))
					num++;
				else
				{
					H.remove(recipientM);
				}
			}
			if(num==0)
			{
				mob.tell(L("No one appears to be eligible to receive any of your money."));
				return false;
			}

			double totalAbsoluteValue=CMath.mul(numGold,denom);
			totalAbsoluteValue=CMath.div(totalAbsoluteValue,num+1);
			if((totalAbsoluteValue*num)>CMLib.beanCounter().getTotalAbsoluteValue(mob,currency))
			{
				mob.tell(L("You don't have that much @x1.",CMLib.beanCounter().getDenominationName(currency,denom)));
				return false;
			}
			final List<Coins> V=CMLib.beanCounter().makeAllCurrency(currency,totalAbsoluteValue);
			CMLib.beanCounter().subtractMoney(mob,totalAbsoluteValue*num);
			for (final Object element : H)
			{
				final MOB recipient=(MOB)element;
				for(int v=0;v<V.size();v++)
				{
					Coins C=V.get(v);
					C=(Coins)C.copyOf();
					mob.addItem(C);
					final CMMsg newMsg=CMClass.getMsg(mob,recipient,C,CMMsg.MSG_GIVE,L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF>."));
					if(mob.location().okMessage(mob,newMsg))
						mob.location().send(mob,newMsg);
					C.putCoinsBack();
				}
			}
		}
		else
		if((commands.size()>2)&&(CMath.isInteger(commands.get(commands.size()-1))))
		{
			final int howMuch=CMath.s_int(commands.get(commands.size()-1));
			if(howMuch<=0)
			{
				mob.tell(L("Split what, how much?"));
				return false;
			}
			commands.remove(commands.size()-1);
			final Vector<String> v=CMParms.parse("GET "+howMuch+" FROM \""+CMParms.combine(commands,1)+"\"");
			final Command c=CMClass.getCommand("Get");
			return c.execute(mob, v, metaFlags);
		}
		else
		{
			mob.tell(L("Split what, how much?"));
			return false;
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
