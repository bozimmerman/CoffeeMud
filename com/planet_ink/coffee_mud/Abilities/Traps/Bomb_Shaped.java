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
   Copyright 2022-2024 Bo Zimmerman

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
public class Bomb_Shaped extends StdBomb
{
	@Override
	public String ID()
	{
		return "Bomb_Shaped";
	}

	private final static String	localizedName	= CMLib.lang().L("shaped bomb");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Bomb_Shaped()
	{
		super();
		trapLevel = 28;
		reset = 10;
	}

	protected volatile Physical stuckBy = null;
	protected int minRequiredIron = 10;

	@Override
	public String requiresToSet()
	{
		return "some iron";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON));
		return V;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if((!(P instanceof RawMaterial))
		||((((Item)P).material()!=RawMaterial.RESOURCE_IRON))
		||(P.basePhyStats().weight()<minRequiredIron))
		{
			if(mob!=null)
				mob.tell(L("You at least "+minRequiredIron+" pounds of iron to make this out of."));
			return false;
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((stuckBy!=null)
		&&(affected instanceof Item))
		{
			if((affected.amDestroyed())||(stuckBy.amDestroyed()))
			{
				stuckBy.destroy();
				return;
			}
			if((stuckBy instanceof Item)
			&&(((Item)affected).owner()!=((Item)stuckBy).owner()))
				stuckBy=null;
			else
			if((stuckBy instanceof Exit)
			&&(!(((Item)affected).owner() instanceof Room)))
				stuckBy=null;
			if(stuckBy!=null)
				affectableStats.setName(L("@x1 sitting next to @x2",affected.Name(),stuckBy.name()));
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
			msg.setTargetCode(CMMsg.MSG_OK_VISUAL);
			stuckBy = (Physical)msg.target();
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
		&&(msg.target()==stuckBy))
		{
			final Physical stuckToP = stuckBy;
			super.activateBomb();
			ItemPossessor newOwner = null;
			if((stuckBy instanceof Exit)
			&&(!(((Item)affected).owner() instanceof Room)))
				newOwner = CMLib.map().roomLocation(affected);
			else
			if((stuckBy instanceof Item)
			&&(((Item)affected).owner() != ((Item)stuckBy).owner()))
				newOwner =  ((Item)stuckBy).owner();
			else
			if((stuckBy instanceof MOB)
			&&(((Item)affected).owner() != ((MOB)stuckBy).location()))
				newOwner = ((MOB)stuckBy).location();
			if(newOwner != null)
				newOwner.moveItemTo((Item)affected, Expire.Player_Drop, Move.Followers);
			stuckBy = stuckToP;
			affected.recoverPhyStats();
		}
		super.executeMsg(myHost, msg);
	}

	protected boolean isShaped()
	{
		final Physical target = stuckBy;
		final Room R=CMLib.map().roomLocation(affected);
		if((target != null)
		&&(!target.amDestroyed())
		&&(R!=null)
		&&(CMLib.map().roomLocation(target)==R))
			return true;
		return false;

	}

	@Override
	protected void explodeBomb(final Physical P)
	{
		final Physical target = stuckBy;
		if(isShaped()
		&&((!(target instanceof Item))
			||(CMLib.utensils().canBePlayerDestroyed(invoker(), (Item)target, false, false)))
		&&((!(target instanceof Boardable))
			||(CMLib.combat().mayIAttackThisVessel(invoker(), (PhysicalAgent)target))))

		{
			if((target instanceof Item)
			&&(((Item)target).subjectToWearAndTear()))
			{
				final int damageLevel = trapLevel()+abilityCode();
				final int die = Math.max(damageLevel,20);
				final int damage = CMLib.dice().roll(1, die, abilityCode());
				CMLib.combat().postItemDamage(invoker(), (Item)target, this,
						damage, CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,
						L("An incredible blast <DAMAGES> <T-NAME>!"));
			}
			else
			if(target instanceof Item)
			{
				final Room R=CMLib.map().roomLocation(target);
				if(R.show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						L("An incredible blast go(es) off at <T-NAME>.")+CMLib.protocol().msp("explode.wav",30)))
				{
					if(target instanceof Container)
					{
						super.explodeContainer((Container)target);
						return;
					}
					else
						((Item)target).destroy();
				}
			}
			else
			if(target instanceof Exit)
			{
				final Room R=CMLib.map().roomLocation(affected);
				if(R.show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						L("An incredible blast go(es) off at <T-NAME>.")+CMLib.protocol().msp("explode.wav",30)))
				{
					final MOB mob=invoker();
					int adjustment=(mob.phyStats().level()+abilityCode())-(target.phyStats().level())*5;
					if(adjustment>0)
						adjustment=0;
					if(CMLib.dice().rollPercentage()>adjustment)
					{
						final int dirCode=CMLib.map().getExitDir(R, (Exit)target);
						CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_UNLOCK,null);
						CMLib.utensils().roomAffectFully(msg,R,dirCode);
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OPEN,L("<T-NAME> opens."));
						CMLib.utensils().roomAffectFully(msg,R,dirCode);
					}
				}
			}
		}
		else
		if(isShaped())
		{
			final Room R=CMLib.map().roomLocation(target);
			R.show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
					L("An incredible blast go(es) off at <T-NAME>.")+CMLib.protocol().msp("explode.wav",30));
		}
		super.explodeBomb(P);
	}

	@Override
	protected boolean canExplodeOutOf(final int material)
	{
		return true;
	}

	@Override
	protected boolean doesInnerExplosionDestroy(final int material)
	{
		return true;
	}

	@Override
	public void spring(final MOB target)
	{
		final Room R=target.location();
		if((R!=null)
		&&(!isShaped()))
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(doesSaveVsTraps(target)))
			{
				R.show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						getAvoidMsg(L("<S-NAME> avoid(s) the massive shaped explosion!")));
			}
			else // *everyone* is vulnerable to a shaped blast
			if((target.isMine(affected))
			||(CMLib.dice().rollPercentage()<25))
			{
				final String triggerMsg = getTrigMsg(L("@x1 massively explodes at <T-NAME>!",affected.name()));
				final String damageMsg = getDamMsg(L("The incredible blast <DAMAGE> <T-NAME>!"));
				if(target.location().show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,
						triggerMsg+CMLib.protocol().msp("explode.wav",30)))
				{
					super.spring(target);
					int divider=1;
					if(affected.phyStats().weight()<minRequiredIron)
						divider=2;
					final int damageLevel = (trapLevel()+abilityCode())/divider;
					final int die = Math.max(damageLevel,20/divider);
					CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(damageLevel,die,1),
							CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,damageMsg);
				}
			}
		}
	}

}
