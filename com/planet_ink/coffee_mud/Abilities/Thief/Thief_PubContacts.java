package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2016 Bo Zimmerman

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

public class Thief_PubContacts extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_PubContacts";
	}

	private final static String	localizedName	= CMLib.lang().L("Pub Contacts");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PUBCONTACTS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	public int	code	= 0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code = newCode;
	}

	public Triad<Item,Double,String> cheapestAlcoholHere(MOB mob, Room room)
	{
		double lowestPrice=Integer.MAX_VALUE;
		Item lowestItem=null;
		String currency="";
		for(int m=0;m<room.numInhabitants();m++)
		{
			final MOB M=room.fetchInhabitant(m);
			if((M!=null)&&(M!=mob))
			{
				if(CMLib.flags().canBeSeenBy(M,mob))
				{
					final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
					if(SK!=null)
					{
						for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
						{
							final Environmental E=i.next();
							if((E instanceof Item)&&(CMLib.flags().isAlcoholic((Item)E)))
							{
								double moneyPrice=0;
								ShopKeeper.ShopPrice price=CMLib.coffeeShops().sellingPrice(M,mob,E,SK,true);
								if(price.experiencePrice>0)
									moneyPrice=(100 * price.experiencePrice);
								else
								if(price.questPointPrice>0)
									moneyPrice=(100 * price.questPointPrice);
								else
								{
									moneyPrice=price.absoluteGoldPrice;
								}
								if(moneyPrice < lowestPrice)
								{
									lowestPrice=moneyPrice;
									lowestItem=(Item)E;
									currency=CMLib.beanCounter().getCurrency(M);
								}
							}
						}
					}
				}
			}
		}
		if(lowestItem == null)
			return null;
		return new Triad<Item,Double,String>(lowestItem,Double.valueOf(lowestPrice),currency);
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		double money=0.0;
		String moneyStr="money";
		if(!auto)
		{
			final Triad<Item,Double,String> alco = cheapestAlcoholHere(mob,R);
			if(alco == null)
			{
				mob.tell(L("You can only establish contacts at a pub."));
				return false;
			}
			TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			List<Room> nearby=CMLib.tracking().findTrailToAnyRoom(R, TrackingFlag.WATERSURFACEONLY.myFilter, flags, 8);
			if((nearby==null)||(nearby.size()==0))
			{
				mob.tell(L("There's no sea or river nearby, so no one here will know anything."));
				return false;
			}
			double pct=0.5 + (CMath.mul(CMath.div(10-super.getXLOWCOSTLevel(mob),2.0), 0.1));
			money = CMath.mul(pct,alco.second.doubleValue()*2.0);
			moneyStr = CMLib.beanCounter().abbreviatedPrice(alco.third, money);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob, alco.third) < money)
			{
				mob.tell(L("You need at least @x1 to buy enough drinks to loosen tongues.",moneyStr));
				return false;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(money > 0.0)
			CMLib.beanCounter().subtractMoney(mob, money);
		
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> drop(s) @x1 on drinks.",moneyStr));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
			}
		}
		else
		{
			beneficialVisualFizzle(mob,null,L("<S-NAME> drop(s) @x1 on drinks, but no one is interested in talking to you.",moneyStr));
		}
		return success;
	}
}
