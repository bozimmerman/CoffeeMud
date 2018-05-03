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
public class Prop_Retainable extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Retainable";
	}

	@Override
	public String name()
	{
		return "Ability to set Price/Retainability of a pet.";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	protected Room lastRoom=null;

	protected long payPeriodLengthInMilliseconds=0;
	protected int payPeriodLengthInMudDays=0;
	protected int payAmountPerPayPeriod=0;
	protected long lastPayDayTimestamp=0;
	protected long lastMoveIn=0;

	@Override
	public String accountForYourself()
	{
		return "Retainable";
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		int x=text.indexOf(';');
		if(x<0)
		{
			payAmountPerPayPeriod=CMath.s_int(text);
			lastPayDayTimestamp=0;
			payPeriodLengthInMudDays=0;
		}
		else
		{
			payAmountPerPayPeriod=CMath.s_int(text.substring(0,x));
			text=text.substring(x+1);
			x=text.indexOf(';');
			if(x<0)
			{
				payPeriodLengthInMudDays=CMath.s_int(text);
				lastPayDayTimestamp=0;
			}
			else
			{
				payPeriodLengthInMudDays=CMath.s_int(text.substring(0,x));
				lastPayDayTimestamp=CMath.s_long(text.substring(x+1));
			}
		}
	}

	public void quit(MOB mob, String msg)
	{
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				if((lastMoveIn>0)&&((System.currentTimeMillis()-lastMoveIn)>4))
				{
					lastMoveIn=0;
					if(mob.location()==lastRoom)
					{
						if((mob.amFollowing()!=null)&&(mob.location().isInhabitant(mob.amFollowing())))
							CMLib.commands().postSay(mob,mob.amFollowing(),L("Is this my new permanent post?  If so, order me to NOFOLLOW and I'll stay here."),false,false);
						else
						if(mob.location().numPCInhabitants()>0)
							CMLib.commands().postSay(mob,mob.amFollowing(),L("I guess this is my new permanent posting?"),false,false);
					}
				}
				if(payPeriodLengthInMudDays>0)
				{
					if(lastPayDayTimestamp==0)
					{
						lastPayDayTimestamp=System.currentTimeMillis();
						miscText=payAmountPerPayPeriod+";"+payPeriodLengthInMudDays+";"+lastPayDayTimestamp;
					}
					if(payPeriodLengthInMilliseconds<=0)
						payPeriodLengthInMilliseconds=((long)payPeriodLengthInMudDays)
										*((long)CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY))*CMProps.getTickMillis();
					if((System.currentTimeMillis()>(lastPayDayTimestamp+payPeriodLengthInMilliseconds))
					&&(CMLib.flags().isInTheGame(mob,true)))
					{
						lastPayDayTimestamp=System.currentTimeMillis();
						miscText=payAmountPerPayPeriod+";"+payPeriodLengthInMudDays+";"+lastPayDayTimestamp;
						final LandTitle t=CMLib.law().getLandTitle(mob.location());
						String owner="";
						if(mob.amFollowing()!=null)
						{
							owner=mob.amFollowing().Name();
							if((t!=null)
							&&(t.getOwnerName().length()>0)
							&&(!t.getOwnerName().equalsIgnoreCase(mob.amFollowing().Name()))
							&&(mob.amFollowing().getClanRole(t.getOwnerName())==null))
							{
								CMLib.commands().postSay(mob,null,L("Hey, I'm not a crook!"),false,false);
								mob.setFollowing(null);
								CMLib.tracking().wanderAway(mob,true,false);
								mob.destroy();
								return false;
							}
						}
						else
						if((t!=null)&&(t.getOwnerName().length()>0))
							owner=t.getOwnerName();

						if(owner.length()==0)
						{
							CMLib.commands().postSay(mob,null,L("Argh! I quit!"),false,false);
							mob.setFollowing(null);
							CMLib.tracking().wanderAway(mob,true,false);
							mob.destroy();
							return false;
						}
						final boolean paid=CMLib.beanCounter().modifyLocalBankGold(mob.location().getArea(),
								owner,
								CMLib.utensils().getFormattedDate(mob)+": Withdrawal of "+CMLib.beanCounter().nameCurrencyShort(mob,payAmountPerPayPeriod)+": Payroll: "+Name(),
								CMLib.beanCounter().getCurrency(mob),
								(-payAmountPerPayPeriod));
						if(paid)
							CMLib.commands().postSay(mob,null,L("Payday!"),false,false);
						else
						{
							CMLib.commands().postSay(mob,null,L("I don't work for free!  I quit!"),false,false);
							mob.setFollowing(null);
							CMLib.tracking().wanderAway(mob,true,false);
							mob.destroy();
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public void tellSkills(MOB me, MOB toMe)
	{
		final StringBuffer skills = new StringBuffer("");
		if(me instanceof ShopKeeper)
			skills.append(", selling "+CMLib.coffeeShops().storeKeeperString(((ShopKeeper)me).getShop(), ((ShopKeeper)me)).toLowerCase());
		for(final Enumeration<Ability> a=me.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				if(A.proficiency() == 0)
					A.setProficiency(50 + me.phyStats().level() - CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				skills.append(", " + A.name());
			}
		}
		if(me instanceof ShopKeeper)
			skills.append(".  Once I'm at my permanent post, you may give me appropriate items to sell at any time");
		if(skills.length()>2)
			CMLib.commands().postSay(me, toMe, L("My skills include: @x1.",skills.substring(2)),false,false);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(mob.location()!=null)
			{
				if((msg.tool()==affected)
				&&(msg.targetMinor()==CMMsg.TYP_BUY))
				{
					tellSkills(mob,msg.source());
					if(payPeriodLengthInMudDays>0)
						CMLib.commands().postSay(mob,msg.source(),L("I accept your terms of employment, and I understand I will be paid @x1 every @x2 days.",CMLib.beanCounter().abbreviatedPrice(mob,payAmountPerPayPeriod),""+payPeriodLengthInMudDays),false,false);
					else
						CMLib.commands().postSay(mob,msg.source(),L("I accept your terms of employment."),false,false);
					CMLib.commands().postSay(mob,msg.source(),L("Please show me the way to my permanent post."),false,false);
					lastPayDayTimestamp=0;
				}
				else
				if(mob.amFollowing()!=null)
				{
					final Room room=mob.location();
					if((room!=lastRoom)
					&&(CMLib.law().doesHavePriviledgesHere(mob.amFollowing(),room))
					&&(room.isInhabitant(mob)))
					{
						lastRoom=room;
						mob.basePhyStats().setRejuv(PhyStats.NO_REJUV);
						mob.setStartRoom(room);
						lastMoveIn=System.currentTimeMillis();
					}
					if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
					||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&((msg.target()==room)||(msg.target()==mob)||(msg.target()==mob.amFollowing())))
					||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
						mob.setFollowing(null);
				}
			}
		}
	}
}
