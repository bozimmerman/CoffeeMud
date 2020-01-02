package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class QuickSwipe extends StdAbility
{
	@Override
	public String ID()
	{
		return "QuickSwipe";
	}

	private final static String localizedName = CMLib.lang().L("Quick Swipe");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_RACIALABILITY;
	}

	private static final String[] triggerStrings =I(new String[] {"QUICKSWIPE","QSWIPE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code=newCode;
	}

	private final PairVector<MOB,Integer> lastOnes=new PairVector<MOB,Integer>();
	protected int timesPicked(final MOB target)
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
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(((MOB)target).amDead()||(!CMLib.flags().canBeSeenBy(target,mob)))
				return Ability.QUALITY_INDIFFERENT;
			if((mob.isInCombat())&&(CMLib.flags().isAliveAwakeMobile((MOB)target,true)||(mob.getVictim()!=target)))
				return Ability.QUALITY_INDIFFERENT;
			if(!((MOB)target).mayIFight(mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Quickly Swipe from whom?"));
			return false;
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((mob.isInCombat())&&(CMLib.flags().isAliveAwakeMobile(target,true)||(mob.getVictim()!=target)))
		{
			mob.tell(mob,mob.getVictim(),null,L("Not while you are fighting <T-NAME>!"));
			return false;
		}

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));
		if((!target.mayIFight(mob))||(levelDiff>15))
		{
			mob.tell(L("You cannot quick swipe from @x1.",target.charStats().himher()));
			return false;
		}
		if(target==mob)
		{
			mob.tell(L("You cannot swipe from yourself."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final String currency=CMLib.beanCounter().getCurrency(target);
		int discoverChance=0;
		if(!CMLib.flags().isAliveAwakeMobile(target,true))
		{
			levelDiff=100;
			discoverChance=100;
		}

		Item swipeItem=null;
		if(CMLib.dice().rollPercentage() < 50)
		{
			final long stealableLocations = Wearable.WORN_LEFT_FINGER
									| Wearable.WORN_RIGHT_FINGER
									| Wearable.WORN_RIGHT_WRIST
									| Wearable.WORN_LEFT_WRIST
									| Wearable.WORN_HEAD
									| Wearable.WORN_EYES
									| Wearable.WORN_MOUTH
									| Wearable.WORN_HELD;
			final List<Item> choices = new ArrayList<Item>();
			final PairList<Item, Long> equipped = CMLib.utensils().getSeenEquipment(target, stealableLocations);
			for(final Pair<Item, Long> i : equipped)
			{
				final Item I=i.first;
				if(((I.rawWornCode() & ~stealableLocations) == 0)
				&&(I.phyStats().weight()<=5)
				&&(I.phyStats().level()<=mob.phyStats().level()))
					choices.add(I);
			}
			if(choices.size()==0)
			{
				for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if((I.amWearingAt(Item.IN_INVENTORY))
					&&(I.phyStats().weight()<5)
					&& (I.container()==null)
					&&(I.phyStats().level()<=mob.phyStats().level()))
						choices.add(I);
				}
			}

			if(choices.size()>0)
				swipeItem=choices.get(CMLib.dice().roll(1, choices.size(), -1));
		}

		final boolean success=proficiencyCheck(mob,levelDiff,auto);
		final boolean atPeace = !mob.isInCombat() && (target.getVictim()!=mob);
		final boolean spotted=(CMLib.dice().rollPercentage()>discoverChance);
		if(!success)
		{
			if(spotted)
			{
				if((target.isMonster())&&(mob.getVictim()==null))
					mob.setVictim(target);
				final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":L("You fail to quick-swipe; <T-NAME> spots you!"),CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to swipe from you and fails!"),CMMsg.MSG_OK_VISUAL,auto?"":L("<S-NAME> tries to swipe from <T-NAME> and fails!"));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":L("You fumble the quick swipe."));
		}
		else
		if(swipeItem == null)
		{
			double pct=0.10;
			if(levelDiff>0)
				pct=0.10;
			if(levelDiff>5)
				pct=0.05;
			if(levelDiff>10)
				pct=0.03;
			double goldTaken=CMLib.beanCounter().getTotalAbsoluteNativeValue(target)*pct*Math.random();
			if(goldTaken<(CMLib.ableMapper().qualifyingClassLevel(mob,this)))
				goldTaken=CMLib.ableMapper().qualifyingClassLevel(mob,this);
			if(goldTaken>CMLib.beanCounter().getTotalAbsoluteNativeValue(target))
				goldTaken=CMLib.beanCounter().getTotalAbsoluteNativeValue(target);
			final String goldTakenStr=CMLib.beanCounter().nameCurrencyShort(target,goldTaken);

			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
			{
				if(goldTaken > 0)
					str=L("<S-NAME> swipe(s) @x1 from <T-HIS-HER>.",goldTakenStr);
				else
				{
					str=L("<S-NAME> attempt(s) to swipe from <T-HIS-HER>, but nothing was found to take!");
					code=CMMsg.MSG_QUIETMOVEMENT;
				}
			}

			final String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(spotted)
				hisCode=hisCode|(auto?0:CMMsg.MASK_MALICIOUS);

			final CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.beanCounter().addMoney(mob,currency,goldTaken);
				mob.recoverPhyStats();
				CMLib.beanCounter().subtractMoney(target,currency,goldTaken);
				target.recoverPhyStats();
			}
		}
		else
		{
			final Item stolen=swipeItem;
			final String str=(auto)?null:L("<S-NAME> quickly swipe(s) @x1 from <T-NAMESELF>.",stolen.name());
			final int code=CMMsg.MSG_THIEF_ACT;
			final String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(spotted)
				hisCode=hisCode|(auto?0:CMMsg.MASK_MALICIOUS);
			CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,hisCode,hisStr);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!stolen.amWearingAt(Item.IN_INVENTORY))
				{
					msg=CMClass.getMsg(target,stolen,null,CMMsg.MSG_REMOVE,CMMsg.MSG_REMOVE,CMMsg.MSG_NOISE,null);
					if(target.location().okMessage(target,msg))
						target.location().send(mob,msg);
				}
				msg=CMClass.getMsg(target,stolen,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(mob,msg);
					msg=CMClass.getMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
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
		if(atPeace && (mob.isInCombat() || (target.getVictim()==mob)))
			CMLib.commands().postFlee(mob, null);
		return success;
	}

}
