package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Items.Basic.GenSailingShip;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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
public class Skill_BoulderThrowing extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_BoulderThrowing";
	}

	private final static String	localizedName	= CMLib.lang().L("Boulder Throwing");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected volatile int	tickDownThisShipCombatRound	= CombatLibrary.TICKS_PER_SHIP_COMBAT;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(tickDownThisShipCombatRound>0)
				tickDownThisShipCombatRound--;
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room R=(mob!=null)?mob.location():null;
				final Area A=(R != null) ? R.getArea() : null;
				final Item shipItem=(A instanceof BoardableShip) ? ((BoardableShip)A).getShipItem() : null;
				if((mob!=null)
				&&(mob.isMonster())
				&&(shipItem instanceof SailingShip)
				&&(!mob.isInCombat()))
				{
					final Item ammoI=mob.fetchHeldItem();
					if(ammoI instanceof Ammunition)
					{
						if(tickDownThisShipCombatRound==0)
						{
							if(((SailingShip)shipItem).isInCombat())
								CMLib.commands().forceStandardCommand(mob, "Throw", new XVector<String>("THROW",ammoI.Name()));
						}
					}
					else
					{
						if(ammoI!=null)
							CMLib.commands().postRemove(mob, ammoI, false);
						Item holdI=null;
						for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof Ammunition)
							&&(I.container()==null)
							&&(I.amWearingAt(Item.IN_INVENTORY)))
							{
								holdI=I;
								break;
							}
						}
						if(holdI!=null)
							CMLib.commands().forceStandardCommand(mob, "Hold", new XVector<String>("HOLD",holdI.Name()));
						else
						if(R!=null)
						{
							for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
							{
								final Item I=i.nextElement();
								if((I instanceof Ammunition)
								&&(I.container()==null)
								&&(CMLib.flags().isGettable(I)))
								{
									holdI=I;
									break;
								}
							}
							if(holdI!=null)
								CMLib.commands().postGet(mob, null, holdI, false);
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	private AmmunitionWeapon boulderThrower=null;
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.source()==mob)
		&&(msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null))
		{
			final List<String> cmd =CMParms.parse(msg.targetMessage());
			final Room R=mob.location();
			if(cmd.get(0).toUpperCase().startsWith("TH") && (R!=null))
			{
				final CMObject O = CMLib.english().findCommand(mob, cmd);
				if((O instanceof Command) && (((Command)O).ID().equals("Throw")))
				{
					final Area A=R.getArea();
					final Item shipItem=(A instanceof BoardableShip) ? ((BoardableShip)A).getShipItem() : null;
					final Room shipR=(shipItem != null)?CMLib.map().roomLocation(shipItem):null;
					
					final String str;
					if(cmd.size()>2)
					{
						str=cmd.get(cmd.size()-1);
						cmd.remove(str);
					}
					else
					if((shipItem instanceof SailingShip)
					&&(shipR!=null)
					&&(((SailingShip)shipItem).getCombatant()!=null))
						str=shipR.getContextName(((SailingShip)shipItem).getCombatant());
					else
						return true;
					
					final String what=CMParms.combine(cmd,1);
					Item item=mob.fetchItem(null,Wearable.FILTER_WORNONLY,what);
					if(item==null)
						item=mob.findItem(null,what);
					if((item==null)||(!CMLib.flags().canBeSeenBy(item,mob)))
						return true;
					if((!item.amWearingAt(Wearable.WORN_HELD))&&(!item.amWearingAt(Wearable.WORN_WIELD)))
						return true;
					final Item target;
					if((shipItem != null)&&(shipR!=null))
						target=shipR.findItem(null,str);
					else
						target=R.findItem(null,str);
					if((target instanceof BoardableShip)
					&&(CMLib.flags().canBeSeenBy(target, mob)))
					{
						if(tickDownThisShipCombatRound>0)
							msg.setSourceMessage(L("You've already thrown at a ship this round."));
						else
						if(mob.charStats().getStat(CharStats.STAT_STRENGTH)<10)
							msg.setSourceMessage(L("You aren't strong enough."));
						else
						if((!(item instanceof Ammunition))||(((Ammunition)item).ammunitionRemaining()<1))
							msg.setSourceMessage(L("You can't throw that at another ship."));
						else
						if((shipItem instanceof SailingShip)
						&&((R.domainType()&Room.INDOORS)==0))
						{
							final SailingShip sailShip=(SailingShip)shipItem;
							if(sailShip.isInCombat() && (sailShip.getCombatant()==target))
							{
								if(this.boulderThrower==null)
								{
									boulderThrower=(AmmunitionWeapon)CMClass.getWeapon("GenSiegeWeapon");
									boulderThrower.setAmmoCapacity(1);
									boulderThrower.setWeaponClassification(Weapon.CLASS_THROWN);
									boulderThrower.setWeaponDamageType(Weapon.TYPE_BASHING);
								}
								final AmmunitionWeapon weapon=boulderThrower;
								final Ammunition ammo=(Ammunition)item;
								boulderThrower.setOwner(null);
								weapon.setAmmunitionType(ammo.ammunitionType());
								weapon.setName(ammo.Name());
								weapon.setRanges(0, 1+(mob.charStats().getStat(CharStats.STAT_STRENGTH)-10)/2);
								final int distance=sailShip.rangeToTarget();
								boolean wasHit=true;
								if((weapon.maxRange() < distance)||(weapon.minRange() > distance))
								{
									msg.setSourceMessage(L("Your target is presently at distance of @x1, but your range is @x2 to @x3.",
														""+distance,""+weapon.minRange(),""+weapon.maxRange()));
								}
								else
								{
									if(ammo.ammunitionType().equals("catapult-boulders"))
										weapon.basePhyStats().setDamage(10 + super.getXLEVELLevel(mob));
									else
									if(ammo.ammunitionType().equals("ballista-bolts"))
										weapon.basePhyStats().setDamage(5 + super.getXLEVELLevel(mob));
									else
									if(ammo.ammunitionType().equals("trebuchet-shots"))
										weapon.basePhyStats().setDamage(15 + super.getXLEVELLevel(mob));
									else
										weapon.basePhyStats().setDamage(1 + (super.getXLEVELLevel(mob)/2));
									weapon.phyStats().setDamage(weapon.basePhyStats().damage());
									weapon.setAmmoRemaining(1);
									weapon.setAmmoRemaining(ammo.ammunitionRemaining()-1);
									final LinkedList<Ability> removeThese=new LinkedList<Ability>();
									for(final Enumeration<Ability> a=weapon.effects();a.hasMoreElements();)
									{
										final Ability A1=a.nextElement();
										if((A1!=null)&&(!A1.isSavable())&&(A1.invoker()==null))
											removeThese.add(A1);
									}
									for(final Ability A1 : removeThese)
										weapon.delEffect(A1);
									for(final Enumeration<Ability> a=ammo.effects();a.hasMoreElements();)
									{
										Ability A1=a.nextElement();
										if((A!=null)&&(A.isSavable())&&(weapon.fetchEffect(A1.ID())==null))
										{
											A1=(Ability)A1.copyOf();
											A1.setInvoker(null);
											A1.setSavable(false);
											weapon.addEffect(A1);
										}
									}
									tickDownThisShipCombatRound=CombatLibrary.TICKS_PER_SHIP_COMBAT;
									for(int i=0;i<distance;i++)
										wasHit = wasHit && super.proficiencyCheck(mob, -50+mob.charStats().getStat(CharStats.STAT_DEXTERITY)+(super.getXLEVELLevel(mob)*2), false);
									CMLib.combat().postShipAttack(mob, shipItem, target, weapon, wasHit);
									if(ammo.ammunitionRemaining()<=0)
										ammo.destroy();
								}
							}
							else
								msg.setSourceMessage(L("Your ship must first be in combat with @x1.",target.name(mob)));
						}
						else
							msg.setSourceMessage(L("You must be on the deck of a ship to throw at another ship."));
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((this.boulderThrower!=null)
		&&(msg.tool()==this.boulderThrower))
			this.boulderThrower.executeMsg(myHost, msg);
		super.executeMsg(myHost, msg);
	}
}
