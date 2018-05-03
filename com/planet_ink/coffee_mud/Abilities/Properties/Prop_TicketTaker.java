package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class Prop_TicketTaker extends Property
{
	@Override
	public String ID()
	{
		return "Prop_TicketTaker";
	}

	@Override
	public String name()
	{
		return "Ticket Taker";
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS;
	}

	@Override
	public String accountForYourself()
	{
		return "one who acts as a ticket taker";
	}

	protected double cost()
	{
		int amount=CMath.s_int(text());
		if(amount==0)
			amount=10;
		return amount;
	}

	protected boolean isMine(Environmental host, Rideable R)
	{
		if(host instanceof Rider)
		{
			final Rider mob=(Rider)host;
			if(R==mob)
				return true;
			if(mob.riding()==null)
				return false;
			if(mob.riding()==R)
				return true;
			if((((Rider)R).riding()==mob.riding()))
				return true;
			if((((Rider)mob.riding()).riding()==R))
				return true;
		}
		else
		if(host instanceof Rideable)
			return host==R;
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((myHost instanceof Rider)&&(((Rider)myHost).riding()!=null))
		   ||(myHost instanceof Rideable))
		{
			final MOB mob=msg.source();
			if((msg.target()!=null)
			&&(myHost!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(myHost,(Rideable)msg.target())))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_MOUNT:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				{
					String currency=CMLib.beanCounter().getCurrency(affected);
					if(currency.length()==0)
						currency=CMLib.beanCounter().getCurrency(mob);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)>=cost())
					{
						final String costStr=CMLib.beanCounter().nameCurrencyShort(currency,cost());
						mob.location().show(mob,myHost,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> give(s) @x1 to <T-NAME>.",costStr));
						CMLib.beanCounter().subtractMoney(mob,currency,cost());
					}
				}
				break;
				}
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(((myHost instanceof Rider)&&(((Rider)myHost).riding()!=null))
		   ||(myHost instanceof Rideable))
		{
			final MOB mob=msg.source();
			if((msg.target()!=null)
			&&(myHost!=mob)
			&&(!mob.isMonster())
			&&(msg.target() instanceof Rideable)
			&&(isMine(myHost,(Rideable)msg.target())))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_MOUNT:
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				{
					String currency=CMLib.beanCounter().getCurrency(affected);
					if(currency.length()==0)
						currency=CMLib.beanCounter().getCurrency(mob);
					if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<cost())
					{
						final String costStr=CMLib.beanCounter().nameCurrencyLong(currency,cost());
						if(myHost instanceof MOB)
							CMLib.commands().postSay((MOB)myHost,mob,L("You'll need @x1 to board.",costStr),false,false);
						else
							mob.tell(L("You'll need @x1 to board.",costStr));
						return false;
					}
					break;
				}
				default:
					break;
				}
			}
		}
		return true;
	}
}
