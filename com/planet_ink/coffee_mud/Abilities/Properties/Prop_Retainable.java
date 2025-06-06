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
   Copyright 2003-2025 Bo Zimmerman

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

	protected Room	lastRoom						= null;

	protected long		payPeriodLengthInMS			= 0;
	protected int		payPeriodLengthInMudDays	= 0;
	protected int		payAmountPerPayPeriod		= 0;
	protected long		lastPayDayTimestamp			= 0;
	protected long		lastMoveIn					= 0;
	protected boolean	persist						= false;

	@Override
	public String accountForYourself()
	{
		return "Retainable";
	}

	private int findInt(final String str)
	{
		for(final String s : CMParms.parse(str))
		{
			if(CMath.isInteger(s))
				return CMath.s_int(s);
		}
		return 0;
	}

	private long findLong(final String str)
	{
		for(final String s : CMParms.parse(str))
		{
			if(CMath.isInteger(s))
				return CMath.s_long(s);
		}
		return 0;
	}

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		persist=false;
		final List<String> parts=CMParms.parseSemicolons(text,true);
		lastPayDayTimestamp=0;
		payPeriodLengthInMudDays=0;
		payAmountPerPayPeriod=0;
		if((parts.size()>0)&&(text.length()>0))
		{
			for(final String txt : parts)
				persist=CMParms.getParmBool(txt, "PERSIST", persist);
			payAmountPerPayPeriod=findInt(parts.get(0));
			lastPayDayTimestamp=0;
			payPeriodLengthInMudDays=0;
			if(parts.size()>1)
			{
				payPeriodLengthInMudDays=findInt(parts.get(1));
				if(parts.size()>2)
				{
					lastPayDayTimestamp=findLong(CMParms.combine(parts,2));
				}
			}
		}
	}

	@Override
	public String text()
	{
		if(persist)
			miscText=payAmountPerPayPeriod+" PERSIST=true;"+payPeriodLengthInMudDays+";"+lastPayDayTimestamp;
		else
			miscText=payAmountPerPayPeriod+";"+payPeriodLengthInMudDays+";"+lastPayDayTimestamp;
		return miscText;
	}

	public void quit(final MOB mob, final String msg)
	{
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
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
					if((mob.location()==lastRoom)
					&&(!CMLib.flags().isAnimalIntelligence(mob)))
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
						lastPayDayTimestamp=System.currentTimeMillis();
					if(payPeriodLengthInMS<=0)
					{
						payPeriodLengthInMS=((long)payPeriodLengthInMudDays)
										*((long)CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY))*CMProps.getTickMillis();
					}
					if((System.currentTimeMillis()>(lastPayDayTimestamp+payPeriodLengthInMS))
					&&(CMLib.flags().isInTheGame(mob,true)))
					{
						lastPayDayTimestamp=System.currentTimeMillis();
						final LandTitle t=CMLib.law().getLandTitle(mob.location());
						if(mob.amFollowing()!=null)
						{
							if((t!=null)
							&&(t.getOwnerName().length()>0)
							&&(!CMLib.law().doesHavePriviledgesHere(mob.amFollowing(), mob.location())))
							{
								CMLib.commands().postSay(mob,null,L("Hey, I'm not a crook!"),false,false);
								mob.setFollowing(null);
								CMLib.tracking().wanderAway(mob,false,false);
								mob.destroy();
								return false;
							}
						}
						final String owner=CMLib.law().getPropertyOwnerName(mob.location());
						if(owner.length()==0)
						{
							CMLib.commands().postSay(mob,null,L("Argh! I quit!"),false,false);
							mob.setFollowing(null);
							CMLib.tracking().wanderAway(mob,false,false);
							mob.destroy();
							return false;
						}
						final boolean paid=CMLib.beanCounter().modifyLocalBankGold(mob.location().getArea(),
								owner,
								CMLib.utensils().getFormattedDate(mob)+": Withdrawal of "+CMLib.beanCounter().nameCurrencyShort(mob,payAmountPerPayPeriod)+": Payroll: "+Name(),
								(-payAmountPerPayPeriod));
						if(paid)
							CMLib.commands().postSay(mob,null,L("Payday!"),false,false);
						else
						{
							CMLib.commands().postSay(mob,null,L("I don't work for free!  I quit!"),false,false);
							mob.setFollowing(null);
							CMLib.tracking().wanderAway(mob,false,false);
							mob.destroy();
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public void tellSkills(final MOB me, final MOB toMe)
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
					if(room!=lastRoom)
					{
						lastRoom=room;
						if((CMLib.law().doesHavePriviledgesHere(mob.amFollowing(),room))
						&&(room.isInhabitant(mob)))
						{
							mob.basePhyStats().setRejuv(PhyStats.NO_REJUV);
							mob.setStartRoom(room);
							lastMoveIn=System.currentTimeMillis();
						}
					}
					/*
					 * If a retainer is in your group when you log out:
					 *  -- On your property: they are removed from the game, and thus can't be saved on p.p.
					 *  -- Somewhere else: they aren't on the property any more, and won't be saved.
					 *
					 */
					if(persist)
					{
						if((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
						||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&((msg.target()==room)||(msg.target()==mob)||(msg.target()==mob.amFollowing())))
						||((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(msg.amISource(mob.amFollowing()))))
							mob.setFollowing(null);
					}
				}
			}
		}
	}
}
