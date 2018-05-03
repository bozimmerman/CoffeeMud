package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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

public class Thief_Robbery extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Robbery";
	}

	private final static String localizedName = CMLib.lang().L("Robbery");

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
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;
	}

	private static final String[] triggerStrings =I(new String[] {"ROBBERY","ROB"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	public List<MOB> mobs=new Vector<MOB>();
	private final PairVector<MOB,Integer> lastOnes=new PairVector<MOB,Integer>();

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	protected int timesPicked(MOB target)
	{
		int times=0;
		for(int x=0;x<lastOnes.size();x++)
		{
			final MOB M=lastOnes.getFirst(x);
			final Integer I=lastOnes.getSecond(x);
			if(M==target)
			{
				times=I.intValue();
				lastOnes.removeElementFirst(M);
				break;
			}
		}
		if(lastOnes.size()>=50)
			lastOnes.removeElementAt(0);
		lastOnes.addElement(target,Integer.valueOf(times+1));
		return times+1;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(affected))
		   &&(mobs.contains(msg.source())))
		{
			if((msg.targetMinor()==CMMsg.TYP_BUY)
			   ||(msg.targetMinor()==CMMsg.TYP_BID)
			   ||(msg.targetMinor()==CMMsg.TYP_SELL)
			   ||(msg.targetMinor()==CMMsg.TYP_LIST)
			   ||(msg.targetMinor()==CMMsg.TYP_VALUE)
			   ||(msg.targetMinor()==CMMsg.TYP_VIEW))
			{
				msg.source().tell(L("@x1 looks unwilling to do business with you.",affected.name()));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if((((MOB)target).amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
				return Ability.QUALITY_INDIFFERENT;
			if((!((MOB)target).mayIFight(mob))||(CMLib.coffeeShops().getShopKeeper(target)==null))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Rob what from whom?"));
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}

		final String itemToSteal=commands.get(0);

		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands,1));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",CMParms.combine(commands,1)));
			return false;
		}
		final int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)*2));

		if((!target.mayIFight(mob))||(CMLib.coffeeShops().getShopKeeper(target)==null))
		{
			mob.tell(L("You cannot rob from @x1.",target.charStats().himher()));
			return false;
		}
		if(target==mob)
		{
			mob.tell(L("You cannot rob yourself."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final ShopKeeper shop=CMLib.coffeeShops().getShopKeeper(target);
		final Environmental stock=shop.getShop().getStock(itemToSteal,mob);
		Physical stolen=(stock instanceof Physical)?(Physical)stock:null;
		if(stolen!=null)
		{
			final ShopKeeper.ShopPrice price=CMLib.coffeeShops().sellingPrice(target,mob,stolen,shop,shop.getShop(), false);
			if((stolen instanceof Ability)
			||(stolen instanceof MOB)
			||(stolen instanceof Room)
			||(stolen instanceof LandTitle)
			||((price.experiencePrice>0)||(price.questPointPrice>0)))
			{
				mob.tell(mob,target,stolen,L("You cannot rob '<O-NAME>' from <T-NAME>."));
				return false;
			}
			if(!shop.getShop().doIHaveThisInStock(stolen.Name(),mob))
				stolen=null;
		}

		int discoverChance=(mob.charStats().getStat(CharStats.STAT_CHARISMA)-(target.charStats().getStat(CharStats.STAT_WISDOM))*5)
						+(getX1Level(mob)*5);
		final int times=timesPicked(target);
		if(times>5)
			discoverChance-=(20*(times-5));
		if(!CMLib.flags().canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95)
			discoverChance=95;
		if(discoverChance<5)
			discoverChance=5;
		final boolean success=proficiencyCheck(mob,-(levelDiff),auto);

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>discoverChance)
			{
				final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":L("You fumble the attempt to rob <T-NAMESELF>; <T-NAME> spots you!"),CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to rob you and fails!"),CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to rob <T-NAME> and fails!"));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				final Thief_Robbery A=(Thief_Robbery)target.fetchEffect(ID());
				if(A==null)
				{
					mobs.clear();
					mobs.add(mob);
					beneficialAffect(mob,target,asLevel,0);
				}
				else
					A.mobs.add(mob);
			}
			else
				mob.tell(mob,target,null,auto?"":L("You fumble the attempt to rob <T-NAME>."));
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
			{
				if(stolen!=null)
					str=L("<S-NAME> rob(s) @x1 from <T-NAMESELF>.",stolen.name());
				else
				{
					str=L("<S-NAME> attempt(s) to rob <T-HIM-HER>, but it doesn't appear @x1 has that in <T-HIS-HER> inventory!",target.charStats().heshe());
					code=CMMsg.MSG_QUIETMOVEMENT;
				}
			}

			final boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(CMLib.dice().rollPercentage()<discoverChance)
				hisStr=null;
			else
			{
				str+=" <T-NAME> spots you!";
				hisCode=hisCode|((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);
				if((target.isMonster())&&(mob.getVictim()==null))
					mob.setVictim(target);
			}

			CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Thief_Robbery A=(Thief_Robbery)target.fetchEffect(ID());
				if(A==null)
					beneficialAffect(mob,target,asLevel,0);
				A=(Thief_Robbery)target.fetchEffect(ID());
				if(A!=null)
					A.mobs.add(mob);

				if((!target.isMonster())&&(mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace(true);
					if(mob.getVictim()==target)
						mob.makePeace(true);
				}
				else
				if(((hisStr==null)||mob.isMonster())
				&&(!alreadyFighting)
				&&((stolen==null)||(CMLib.dice().rollPercentage()>stolen.phyStats().level())))
				{
					if(target.getVictim()==mob)
						target.makePeace(true);
				}
				if(stolen!=null)
				{
					final List<Environmental> products=shop.getShop().removeSellableProduct(stolen.Name(),mob);
					if(products.get(0) instanceof Item)
					{
						stolen=(Item)products.get(0);
						mob.location().addItem((Item)stolen,ItemPossessor.Expire.Player_Drop);
						msg=CMClass.getMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							if(stolen!=null)
							{
								Ability propertyProp=stolen.fetchEffect("Prop_PrivateProperty");
								if(propertyProp==null)
								{
									propertyProp=CMClass.getAbility("Prop_PrivateProperty");
									propertyProp.setMiscText("owner=\""+mob.Name()+"\" expiresec=60");
									stolen.addNonUninvokableEffect(propertyProp);
								}
							}
						}
					}
				}
			}
		}
		return success;
	}

}
