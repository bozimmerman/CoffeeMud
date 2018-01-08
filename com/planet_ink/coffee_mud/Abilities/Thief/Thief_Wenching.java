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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback.Type;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_Wenching extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Wenching";
	}

	private final static String	localizedName	= CMLib.lang().L("Wenching");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Well Wenched)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "WENCHING" });

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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		final int[] CMMSGMAP=CharStats.CODES.CMMSGMAP();
		final int bonus = 10 + this.getXLEVELLevel(affectedMob) + (adjustedLevel(affectedMob,0) / 5);
		for(final int c : CharStats.CODES.SAVING_THROWS())
		{
			if(CMMSGMAP[c]!=-1)
				affectableStats.setStat(c, affectableStats.getStat(c) + bonus);
		}
	}
	
	protected boolean isAWench(final char gender, final MOB M)
	{
		if(CMLib.flags().isAnimalIntelligence(M))
			return false;
		if(M.charStats().getStat(CharStats.STAT_GENDER)!=gender)
			return false;
		if(!M.isMonster())
			return false;
		if(M.amFollowing()!=null)
			return false;
		if(CMLib.flags().isAlcoholic(M))
			return true;
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
		if(SK!=null)
		{
			CoffeeShop shop = SK.getShop();
			if(shop != null)
			{
				for(Iterator<Environmental> e=shop.getStoreInventory();e.hasNext();)
				{
					Environmental E=e.next();
					if((E instanceof Physical)
					&&(CMLib.flags().isAlcoholic((Physical)E)))
						return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		
		char gender=(char)mob.charStats().getStat(CharStats.STAT_GENDER);
		switch(gender)
		{
		case 'M':
			gender='F';
			break;
		case 'F':
			gender='M';
			break;
		default:
			gender='F';
			break;
		}
		if(commands.size()>0)
		{
			String genderStr=commands.get(commands.size()-1);
			if("male".startsWith(genderStr.toLowerCase()))
			{
				gender='M';
				commands.remove(commands.size()-1);
			}
			else
			if("female".startsWith(genderStr.toLowerCase()))
			{
				gender='F';
				commands.remove(commands.size()-1);
			}
		}
		
		String genderName;
		switch(gender)
		{
		case 'M':
			genderName = CMLib.lang().L("male");
			break;
		case 'F':
			genderName = CMLib.lang().L("female");
			break;
		default:
			genderName = CMLib.lang().L("neuter");
			break;
		}
		MOB target=null; // 
		if(commands.size()>0)
		{
			MOB M=super.getTarget(mob, commands, givenTarget);
			if(M==null)
				return false;
			if((M==mob)||(!this.isAWench(gender, M)))
			{
				mob.tell(L("@x1 is not a @x2 wench!",M.Name(),genderName));
				return false;
			}
			target=M;
		}
		else
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)
				&&(M!=mob)
				&&(this.isAWench(gender, M))
				&&(CMLib.flags().canBeSeenBy(M, mob)))
				{
					target=M;
				}
			}
			if(target == null)
			{
				mob.tell(L("You don't see any proper @x1 wenches here.",genderName));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		int levelDiff = target.phyStats().level() - mob.phyStats().level();
		if(levelDiff < 0)
			levelDiff = 0;
		
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":L("<S-NAME> wink(s) at <T-NAME> and makes <T-HIM-HER> an offer..."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				int cost=100 - super.getXLOWCOSTLevel(mob) + ((10 - super.getXLOWCOSTLevel(mob)) * levelDiff);
				
				if(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(mob, target) < cost )
				{
					mob.tell(L("@x1 requires @x2, which you don't have.",target.name(),CMLib.beanCounter().abbreviatedPrice(target, cost)));
					return false;
				}
				CMLib.beanCounter().subtractMoney(mob, CMLib.beanCounter().getCurrency(target), cost);
				if(R.show(mob,target,this,CMMsg.MSG_OK_VISUAL, L("<S-NAME> gives <T-NAME> @x1, and then wander off together for a time...",CMLib.beanCounter().abbreviatedPrice(target, cost))))
				{
					final Session sess=mob.session();
					if(sess == null)
					{
						Ability A=mob.fetchEffect(ID());
						if(A!=null)
						{
							A.unInvoke();
							mob.delEffect(A);
						}
						beneficialAffect(mob,mob,asLevel,(CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)));
						R.show(mob,target,this,CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) more relaxed and confident!"));
					}
					else
					{
						final int oldMobDisposition=mob.basePhyStats().disposition();
						final int oldWenchDisposition=target.basePhyStats().disposition();
						mob.basePhyStats().setDisposition(oldMobDisposition|PhyStats.IS_NOT_SEEN);
						target.basePhyStats().setDisposition(oldWenchDisposition|PhyStats.IS_NOT_SEEN);
						final MOB targetM=target;
						mob.recoverPhyStats();
						target.recoverPhyStats();
						sess.prompt(new InputCallback(Type.WAIT,8000)
						{
							@Override
							public void showPrompt()
							{
							}

							@Override
							public void timedOut()
							{
								mob.basePhyStats().setDisposition(oldMobDisposition);
								targetM.basePhyStats().setDisposition(oldWenchDisposition);
								mob.recoverPhyStats();
								targetM.recoverPhyStats();
								Ability A=mob.fetchEffect(ID());
								if(A!=null)
								{
									A.unInvoke();
									mob.delEffect(A);
								}
								beneficialAffect(mob,mob,asLevel,(CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)));
								R.show(mob,null,null,CMMsg.MSG_OK_VISUAL, L("<S-NAME> seem(s) more relaxed and confident!"));
							}

							@Override
							public void callBack()
							{
							}
						});
					}
				}
			}
		}
		else
		{
			return beneficialVisualFizzle(mob,target,auto?"":L("<S-NAME> attempt(s) to go wenching, but can't even manage to pay for it."));
		}
		return success;
	}
}
