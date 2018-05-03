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
public class Thief_WarningShot extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_WarningShot";
	}

	private final static String localizedName = CMLib.lang().L("Warning Shot");

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
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings = I(new String[] { "WARNINGSHOT"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected List<Item> getSiegeWeapons(Physical P)
	{
		final List<Item> items=new ArrayList<Item>();
		if(P instanceof BoardableShip)
		{
			BoardableShip myShip=(BoardableShip)P;
			for(Enumeration<Room> r=myShip.getShipArea().getProperMap();r.hasMoreElements();)
			{
				final Room R2=r.nextElement();
				if((R2!=null)&&(R2.numItems()>0)&&(((R2.domainType()&Room.INDOORS)==0)))
				{
					for(Enumeration<Item> i=R2.items();i.hasMoreElements();)
					{
						final Item I2=i.nextElement();
						if((I2.container()==null)
						&&(CMLib.combat().isAShipSiegeWeapon(I2)))
							items.add(I2);
					}
				}
			}
		}
		return items;
	}
	
	protected double getAvgDamagePerRound(final List<Item> items)
	{
		double maxDamage=0.0;
		for(Item I : items)
		{
			if((I instanceof AmmunitionWeapon)
			&&(((AmmunitionWeapon)I).ammunitionCapacity()==1))
			{
				maxDamage = I.phyStats().damage();
			}
		}
		return maxDamage / 2.0;
	}
	
	public static void tellTheDeck(final Item ship, final MOB M, final String msg)
	{
		if(ship instanceof BoardableShip)
		{
			for(final Enumeration<Room> r=((BoardableShip)ship).getShipArea().getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((R!=null)&&((R.domainType()&Room.INDOORS)==0)&&(R.numPCInhabitants()>0))
				{
					for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
					{
						final MOB mob=m.nextElement();
						if((mob!=null)&&(mob.isPlayer())&&(mob!=M))
							mob.tell(msg);
					}
				}
			}
		}
		else
		if(ship instanceof Rideable)
		{
			for(Enumeration<Rider> r= ((Rideable)ship).riders();r.hasMoreElements();)
			{
				final Rider R=r.nextElement();
				if(R instanceof MOB)
				{
					final MOB mob=(MOB)R;
					if((mob.isPlayer())&&(mob!=M))
						mob.tell(msg);
				}
			}
		}
		if(M!=null)
			M.tell(msg);
	}
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof BoardableShip))
		||(!(((BoardableShip)R.getArea()).getShipItem() instanceof SailingShip))
		||((R.domainType()&Room.INDOORS)!=0))
		{
			mob.tell(L("You must be on the deck of a ship to fire a warning shot."));
			return false;
		}
		final BoardableShip myShip=(BoardableShip)R.getArea();
		final SailingShip myShipItem=(SailingShip)myShip.getShipItem();
		if((myShipItem==null)
		||(!(myShipItem.owner() instanceof Room))
		||(!CMLib.flags().isWateryRoom((Room)myShipItem.owner())))
		{
			mob.tell(L("Your ship must be at sea to fire a warning shot."));
			return false;
		}
		final Room fightR=(Room)myShipItem.owner();
		final String targetName;
		if(commands.size()>0)
			targetName=CMParms.combine(commands);
		else
			targetName=fightR.getContextName(myShipItem.getCombatant());
		Item I=(targetName.length()>0)?fightR.findItem(null, targetName):null;
		if((I==null)||(!CMLib.flags().canBeSeenBy(I, mob)))
		{
			mob.tell(L("You can't see a ship called '@x1' here.",targetName));
			return false;
		}
		if(!(I instanceof Rideable))
		{
			mob.tell(L("You can't target '@x1' with this skill.",targetName));
			return false;
		}
		
		if(!CMLib.flags().isStanding(mob)&&(!auto))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}
		
		final Physical target=I;
		final List<Item> myItems=getSiegeWeapons(myShipItem);
		final List<Item> hisItems=getSiegeWeapons(target);
		
		if(myItems.size()==0)
		{
			mob.tell(L("Your ship needs siege weapons on the deck to do this."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,auto?"":L("<S-NAME> load(s) and fire(s) @x1's weapons across <T-NAME>'s bow!",myShipItem.Name()));
			if(fightR.okMessage(mob,msg))
			{
				fightR.send(mob,msg);
				for(Item I2 : myItems)
				{
					fightR.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 fires @x2 across @x3's bow.",myShipItem.Name(),I2.Name(),target.Name()));
				}
				final String targetSeesStr;
				final String iSeeStr;
				final Item hisShipItem;
				if((target instanceof BoardableShip)
				&&(((BoardableShip)target).getShipItem() instanceof SailingShip)
				&&(hisItems.size()>0))
				{
					hisShipItem = ((BoardableShip)target).getShipItem();
					double mySpeed = myShipItem.getShipSpeed();
					if(mySpeed <=0)
						mySpeed = 1.0;
					double hisSpeed = ((SailingShip)hisShipItem).getShipSpeed();
					if(hisSpeed <=0)
						hisSpeed = 1.0;
					double myChancePerRoundToBeHit =  CMath.div(CMath.div(100.0, mySpeed + 1.0), 100.0);
					double hisChancePerRoundToBeHit =  CMath.div(CMath.div(100.0, hisSpeed + 1.0), 100.0);
					
					double myHullPoints = CMLib.combat().getShipHullPoints(myShip);
					if(myShipItem.subjectToWearAndTear())
						myHullPoints = myHullPoints * CMath.div(myShipItem.usesRemaining(), 100.0);
					double hisHullPoints = CMLib.combat().getShipHullPoints((BoardableShip)target);
					if(hisShipItem.subjectToWearAndTear())
						hisHullPoints = hisHullPoints * CMath.div(hisShipItem.usesRemaining(), 100.0);
					
					double avgDamagePerRound = this.getAvgDamagePerRound(myItems);
					double hisDamagePerRound = this.getAvgDamagePerRound(hisItems);
					
					avgDamagePerRound *= hisChancePerRoundToBeHit;
					hisDamagePerRound *= myChancePerRoundToBeHit;
					
					double roundsHeHasToSurviveMe=hisHullPoints / avgDamagePerRound;
					double roundsIHaveToSurviveHim=myHullPoints / hisDamagePerRound;
					
					if(roundsIHaveToSurviveHim > (roundsHeHasToSurviveMe * 1.05))
					{
						if(roundsIHaveToSurviveHim > (roundsHeHasToSurviveMe * 2))
						{
							targetSeesStr=L("You are no match for @x1.",myShipItem.Name());
							iSeeStr=L("@x1 is no match for you.",target.Name());
						}
						else
						{
							targetSeesStr=L("@x1 is a threat to you.",myShipItem.Name());
							iSeeStr=L("You can probably take out @x1.",target.Name());
						}
					}
					else
					if(roundsHeHasToSurviveMe > (roundsIHaveToSurviveHim * 1.05))
					{
						if(roundsHeHasToSurviveMe > (roundsIHaveToSurviveHim * 2))
						{
							iSeeStr=L("You are no match for @x1.",myShipItem.Name());
							targetSeesStr=L("@x1 is no match for you.",target.Name());
						}
						else
						{
							iSeeStr=L("@x1 is a threat to you.",myShipItem.Name());
							targetSeesStr=L("You can probably take out @x1.",target.Name());
						}
					}
					else
					{
						targetSeesStr=L("@x1 is a pretty even match.",myShipItem.Name());
						iSeeStr=L("@x1 is a pretty even match.",target.Name());
					}
				}
				else
				{
					hisShipItem=(Item)target;
					targetSeesStr=L("You are no match for @x1.",myShipItem.Name());
					iSeeStr=L("@x1 is no match for you.",target.Name());
				}
				CMLib.threads().scheduleRunnable(new Runnable(){
					@Override
					public void run()
					{
						tellTheDeck(myShipItem,mob,iSeeStr.toString());
						tellTheDeck(hisShipItem,null,targetSeesStr.toString());
					}
				}, 1000);
			}
		}
		else
			super.beneficialVisualFizzle(mob, target, L("<S-NAME> tr(ys) to load and fire a warning shot, but fail(s)."));
		return success;
	}
}
