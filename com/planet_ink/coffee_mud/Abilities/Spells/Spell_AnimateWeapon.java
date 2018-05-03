package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_AnimateWeapon extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_AnimateWeapon";
	}

	private final static String localizedName = CMLib.lang().L("Animate Weapon");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	public int overrideMana()
	{
		return 100;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room)
		&&(invoker()!=null)
		&&(invoker().location().isContent((Item)affected)))
		{
			final Item item=(Item)affected;
			final MOB invoker=invoker();
			if((invoker!=null)&&(invoker.isInCombat()))
			{
				final MOB victiM=invoker.getVictim();
				final boolean isHit=(CMLib.combat().rollToHit(CMLib.combat().adjustedAttackBonus(invoker(),victiM)+item.phyStats().attackAdjustment(),CMLib.combat().adjustedArmor(victiM), 0));
				if((!isHit)||(!(item instanceof Weapon)))
					invoker().location().show(invoker(),victiM,item,CMMsg.MSG_OK_ACTION,L("<O-NAME> attacks <T-NAME> and misses!"));
				else
				{
					CMLib.combat().postDamage(invoker(),victiM,item,
											CMLib.dice().roll(1,item.phyStats().damage(),5),
											CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,
											((Weapon)item).weaponDamageType(),L("@x1 attacks and <DAMAGE> <T-NAME>!",affected.name()));
				}
			}
			else
			if(CMLib.dice().rollPercentage()>75)
			{
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1:
					invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 twiches a bit.",affected.name()));
					break;
				case 2:
					invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 is looking for trouble.",affected.name()));
					break;
				case 3:
					invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 practices its moves.",affected.name()));
					break;
				case 4:
					invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 makes a few fake attacks.",affected.name()));
					break;
				case 5:
					invoker().location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 dances around.",affected.name()));
					break;
				}
			}
		}
		else
			unInvoke();
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!super.okMessage(myHost,msg))
		||(affected==null)
		||(!(affected instanceof Item)))
		{
			unInvoke();
			return false;
		}
		if(msg.amITarget(affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_REMOVE:
				unInvoke();
				break;
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(super.canBeUninvoked())
		&&(((Item)affected).owner() instanceof Room))
			((Room)((Item)affected).owner()).showHappens(CMMsg.MSG_OK_ACTION,L("@x1 stops moving.",affected.name()));
		super.unInvoke();
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if(!(target instanceof Weapon))
		{
			mob.tell(L("That's not a weapon!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.unWear();
				if(mob.isMine(target))
					mob.location().show(mob,target,CMMsg.MSG_DROP,L("<T-NAME> flies out of <S-YOUPOSS> hands!"));
				else
					mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> starts flying around!"));
				if(mob.location().isContent(target))
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> twitch(es) oddly, but does nothing more."));

		// return whether it worked
		return success;
	}
}
