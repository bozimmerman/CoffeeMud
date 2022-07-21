package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ThinAbility;
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
   Copyright 2022-2022 Bo Zimmerman

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
public class Bomb_Sticky extends StdBomb
{
	@Override
	public String ID()
	{
		return "Bomb_Sticky";
	}

	private final static String	localizedName	= CMLib.lang().L("sticky bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Bomb_Sticky()
	{
		super();
		trapLevel = 25;
	}

	protected volatile Physical stuckTo = null;

	@Override
	public String requiresToSet()
	{
		return "some cotton";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_COTTON));
		return V;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if((!(P instanceof Item))
		||((((Item)P).material()!=RawMaterial.RESOURCE_COTTON)))
		{
			if(mob!=null)
				mob.tell(L("You some cotton to make this out of."));
			return false;
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((stuckTo!=null)
		&&(affected instanceof Item))
		{
			if((affected.amDestroyed())||(stuckTo.amDestroyed()))
			{
				stuckTo.destroy();
				return;
			}

			if((stuckTo instanceof Item)
			&&(((Item)affected).owner()!=((Item)stuckTo).owner()))
				((Item)affected).owner().moveItemTo((Item)stuckTo, Expire.Player_Drop, Move.Followers);
			else
			if((stuckTo instanceof Exit)
			&&(!(((Item)affected).owner() instanceof Room)))
				stuckTo=null;
			if(stuckTo!=null)
				affectableStats.setName(L("@x1 stuck to @x2",affected.Name(),stuckTo.name()));
			//affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_PUT)
		&&(affected instanceof Item)
		&&(msg.source()==(((Item)affected).owner()))
		&&(msg.target() instanceof Physical))
		{
			if(msg.target() instanceof Exit)
			{
				final Exit E=(Exit)msg.target();
				if(!E.hasADoor())
					return super.okMessage(myHost, msg);
			}
			msg.setTargetCode(CMMsg.MSG_OK_VISUAL);
			stuckTo = (Physical)msg.target();
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_PUT)
		&&(msg.targetMinor()==CMMsg.TYP_OK_VISUAL)
		&&(affected instanceof Item)
		&&(msg.source()==(((Item)affected).owner()))
		&&(msg.target() instanceof Physical)
		&&(msg.target()==stuckTo))
		{
			final Physical stuckToP = stuckTo;
			super.activateBomb();
			ItemPossessor newOwner = null;
			if((stuckTo instanceof Exit)
			&&(!(((Item)affected).owner() instanceof Room)))
				newOwner = CMLib.map().roomLocation(affected);
			else
			if((stuckTo instanceof Item)
			&&(((Item)affected).owner() != ((Item)stuckTo).owner()))
				newOwner =  ((Item)stuckTo).owner();
			else
			if((stuckTo instanceof MOB)
			&&(((Item)affected).owner() != ((MOB)stuckTo).location()))
				newOwner = ((MOB)stuckTo).location();
			if(newOwner != null)
				newOwner.moveItemTo((Item)affected, Expire.Player_Drop, Move.Followers);
			stuckTo = stuckToP;
			affected.recoverPhyStats();
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	protected void explodeBomb(final Physical P)
	{
		final Physical target = stuckTo;
		final Room R=CMLib.map().roomLocation(affected);
		if((target instanceof CloseableLockable)
		&&(((CloseableLockable)target).hasALock())
		&&(invoker()!=null)
		&&(R!=null))
		{
			final MOB mob=invoker();
			stuckTo = null;
			int sLevel = target.phyStats().level();
			if(sLevel == 0)
				sLevel = 1;
			final int diff = sLevel - trapLevel();
			if((diff<=0)
			||(CMLib.dice().rollPercentage()>(diff * 10)))
			{
				int dirCode = -1;
				if(target instanceof Exit)
					dirCode = CMLib.map().getExitDir(R, (Exit)target);
				CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
				msg.setValue(0);
				if(R.okMessage(mob,msg))
				{
					final boolean hadLock = ((CloseableLockable)target).hasALock();
					msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,L("The lock bursts off <T-NAME>."));
					CMLib.utensils().roomAffectFully(msg,R,dirCode);
					if(hadLock
					&&(((CloseableLockable)target).openDelayTicks()>0))
					{
						target.addEffect(new ThinAbility() {
							final long expires = System.currentTimeMillis()
									+(((CloseableLockable)target).openDelayTicks()*CMProps.getTickMillis());
							@Override
							public String ID()
							{
								return "Unlockable";
							}

							@Override
							public String name()
							{
								return "Unlockable";
							}

							@Override
							public boolean isSavable()
							{
								return false;
							}

							@Override
							public void unInvoke()
							{
								if(affected != null)
									affected.delEffect(this);
							}

							@Override
							public boolean okMessage(final Environmental myHost, final CMMsg msg)
							{
								if((affected==null)||(System.currentTimeMillis()>expires))
								{
									unInvoke();
									return true;
								}
								if((msg.amITarget(affected))||(msg.tool()==affected))
								{
									final CloseableLockable C = (CloseableLockable)affected;
									final MOB mob=msg.source();
									switch(msg.targetMinor())
									{
									case CMMsg.TYP_LOCK:
										if(C.hasALock())
											mob.tell(L("@x1`s lock is damaged.",affected.name()));
										return false;
									case CMMsg.TYP_UNLOCK:
										if(C.hasALock())
											mob.tell(L("@x1`s lock is damaged.",affected.name()));
										return false;
									case CMMsg.TYP_JUSTICE:
									{
										if(!msg.targetMajor(CMMsg.MASK_DELICATE))
											return true;
									}
									//$FALL-THROUGH$
									case CMMsg.TYP_DELICATE_HANDS_ACT:
										mob.tell(L("@x1 appears to be damaged.",affected.name()));
										return false;
									default:
										break;
									}
								}
								return true;
							}
						});
					}
				}
			}
			else
				R.showHappens(CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						L("Pops and sparks fly all over @x1!",target.name()));
		}
		else
		if(((!(target instanceof Item))
			||(CMLib.utensils().canBePlayerDestroyed(invoker(), (Item)target, false, false)))
		&&(((Item)target).subjectToWearAndTear()))
		{
			final int damageLevel = trapLevel()+abilityCode();
			int die = Math.max(damageLevel,5);
			switch(((Item)target).material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_CLOTH:
				die = die / 2;
				break;
			case RawMaterial.MATERIAL_LEATHER:
				die = die / 3;
				break;
			case RawMaterial.MATERIAL_PAPER:
				die = die * 2;
				break;
			case RawMaterial.MATERIAL_LIQUID:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_SYNTHETIC:
				die = 0;
				break;
			case RawMaterial.MATERIAL_FLESH:
				die = die / 4;
				break;
			default:
				die = die / 5;
				break;
			}
			final int damage = CMLib.dice().roll(1, die, abilityCode()/2);
			CMLib.combat().postItemDamage(invoker(), (Item)target, this,
					damage, CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,
					L("Some popping sparks from @x1 <DAMAGES> <T-NAME>!",affected.name()));
		}
		else
		if(target != null)
		{
			R.showHappens(CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
					L("Pops and sparks fly all over @x1!",target.name()));
		}
		super.explodeBomb(P);
	}

	@Override
	protected boolean canExplodeOutOf(final int material)
	{
		return false;
	}

	@Override
	public void spring(final MOB target)
	{
		final Room R=target.location();
		if(R!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				R.show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) the pops and sparks!")));
			}
			else
			{
				final String triggerMsg = getTrigMsg(L("@x1 pops and sparks on <T-NAME>!",affected.name()));
				if(R.show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE, triggerMsg))
					super.spring(target);
			}
		}
	}

}
