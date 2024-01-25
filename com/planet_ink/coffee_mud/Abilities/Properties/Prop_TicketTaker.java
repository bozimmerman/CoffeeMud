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
   Copyright 2003-2024 Bo Zimmerman

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

	protected double	cost		= 0.0;
	protected String	ticketName	= null;

	protected final LimitedTreeSet<String> paid		= new LimitedTreeSet<String>(1000,25,false);

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		cost=0.0;
		ticketName = null;
		if(CMath.isNumber(text))
			cost = Math.abs(CMath.s_double(text));
		else
		if(text.trim().length()>0)
			ticketName = text.trim();
	}

	protected boolean isTicketTakingScenario(final MOB mob2, final Rideable rideableR)
	{
		final Physical host = affected;
		if(host instanceof Rider)
		{
			final Rider hostM=(Rider)host;
			if(rideableR==hostM)
				return true;
			if(hostM.riding()==null)
			{
				if(isOnBoard(host, rideableR))
					return true;
				return false;
			}
			if(hostM.riding()==rideableR)
				return true;
			if((rideableR.riding()==hostM.riding()))
				return true;
			if((((Rider)hostM.riding()).riding()==rideableR))
				return true;
		}
		if(host instanceof Rideable)
			return host==rideableR;
		return false;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
	}

	protected boolean isPrivilegedWith(final MOB mob, final Rideable R)
	{
		if((R instanceof Boardable)
		&&(R instanceof PrivateProperty)
		&&(CMLib.law().doesHavePrivilegesWith(mob, (PrivateProperty)R)))
			return true;
		return false;
	}

	protected boolean isOnBoard(final Environmental host, final Rideable R)
	{
		if(host instanceof MOB)
		{
			final MOB M=(MOB)host;
			final Room locR = M.location();
			if(locR != null)
			{
				if((locR.getArea() instanceof Boardable)
				&&(((Boardable)locR.getArea()).getBoardableItem() == R))
					return true;
				if((R instanceof Boardable)
				&&(((Boardable)R).getBoardableItem().owner()==locR)
				&&(R instanceof PrivateProperty)
				&&(((PrivateProperty)R).getOwnerName().length()==0))
					return true;
			}
		}
		return false;
	}

	protected Rideable getRideable(final Environmental target, final Environmental exit)
	{
		if(target instanceof Rideable)
			return (Rideable)target;
		if((target instanceof Room)
		&&(CMLib.map().roomLocation(affected) == target))
		{
			final Room R = (Room)target;
			if((R.getArea() instanceof Boardable)
			&&(exit instanceof PrepositionExit))
				return (Rideable)((Boardable)R.getArea()).getBoardableItem();
		}
		return null;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		final MOB mob=msg.source();
		if((msg.target()!=null)
		&&(myHost!=mob)
		&&(!mob.isMonster()))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_SIT:
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_SLEEP:
			{
				final Rideable ride = getRideable(msg.target(), msg.tool());
				if((ride != null)
				&& (isTicketTakingScenario(msg.source(),ride))
				&& (!paid.contains(msg.source().Name())))
				{
					paid.add(msg.source().Name());
					//final Room hostR = CMLib.map().roomLocation(affected);
					if(cost > 0)
					{
						String currency=CMLib.beanCounter().getCurrency(affected);
						if(currency.length()==0)
							currency=CMLib.beanCounter().getCurrency(mob);
						if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)>=cost)
						{
							final String costStr=CMLib.beanCounter().nameCurrencyShort(currency, cost);
							mob.location().show(mob,myHost,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> give(s) @x1 to <T-NAME>.",costStr));
							CMLib.beanCounter().subtractMoney(mob,currency,cost);
						}
					}
					else
					if(ticketName != null)
					{
						final Item I = getTicket(msg.source());
						if(I != null)
						{
							mob.location().show(mob,myHost,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> give(s) @x1 to <T-NAME>.",I.name(mob)));
							I.destroy();
						}
					}
				}
				break;
			}
		}
		}
	}

	protected Item getTicket(final MOB mob)
	{
		final Item I = mob.findItem(null, ticketName);
		if((I != null)
		&&(I.amWearingAt(Item.WORN_HELD)||I.amWearingAt(Item.IN_INVENTORY))
		&&(CMLib.flags().isDroppable(I))
		&&(CMLib.utensils().canBePlayerDestroyed(mob, I, true, true)))
			return I;
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		final MOB mob=msg.source();
		if((msg.target()!=null)
		&&(myHost!=mob)
		&&(!mob.isMonster())
		&&(msg.target() instanceof Rideable))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_SIT:
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_SLEEP:
			{
				if(isTicketTakingScenario(msg.source(),(Rideable)msg.target()))
				{
					if(cost > 0)
					{
						String currency=CMLib.beanCounter().getCurrency(affected);
						if(currency.length()==0)
							currency=CMLib.beanCounter().getCurrency(mob);
						if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<cost)
						{
							final String costStr=CMLib.beanCounter().nameCurrencyLong(currency,cost);
							if((myHost instanceof MOB)
							&&(((MOB)myHost).location() == mob.location()))
								CMLib.commands().postSay((MOB)myHost,mob,L("You'll need @x1 to board.",costStr),false,false);
							else
							if(myHost instanceof MOB)
								mob.tell(L("@x1 says 'You'll need @x2 to board.'",myHost.name(),costStr));
							else
								mob.tell(L("You'll need @x1 to board.",costStr));
							return false;
						}
					}
					else
					if(ticketName != null)
					{
						final Item I = getTicket(msg.source());
						if(I == null)
						{
							if((myHost instanceof MOB)
							&&(((MOB)myHost).location() == mob.location()))
								CMLib.commands().postSay((MOB)myHost,mob,L("You'll need @x1 to board.",ticketName),false,false);
							else
							if(myHost instanceof MOB)
								mob.tell(L("@x1 says 'You'll need @x2 to board.'",myHost.name(),ticketName));
							else
								mob.tell(L("You'll need @x1 to board.",ticketName));
						}
					}
				}
				break;
			}
			default:
				break;
			}
		}
		return true;
	}
}
