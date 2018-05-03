package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Chant_Treeform extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Treeform";
	}

	private final static String localizedName = CMLib.lang().L("Treeform");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Treeform)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_SHAPE_SHIFTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(3);
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	private CharState oldState=null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(msg.source().getVictim()==mob)
			msg.source().setVictim(null);
		if(mob.isInCombat())
		{
			final MOB victim=mob.getVictim();
			if(victim!=null)
				victim.makePeace(true);
			mob.makePeace(true);
		}
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.curState().setHunger(1000);
		mob.curState().setThirst(1000);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		
		if((msg.tool() instanceof Ability)
		&&(msg.tool().ID().equals("Skill_Revoke")))
			return super.okMessage(myHost, msg);

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_TRAVEL))
				unInvoke();
			else
			if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMajor()>0))
			{
				mob.tell(L("Trees can't do that."));
				return false;
			}
		}
		if(msg.amITarget(mob))
		{
			if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)||(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS)))
			{
				msg.source().tell(L("Attack a tree?!"));
				msg.source().setVictim(null);
				mob.setVictim(null);
				return false;
			}
			final Item item=CMClass.getItem("GenResource");
			item.setName(mob.Name());
			item.setDescription(mob.description());
			item.setDisplayText(mob.displayText());
			item.setMaterial(RawMaterial.RESOURCE_WOOD);
			CMLib.flags().setGettable(item,false);
			item.phyStats().setWeight(2000);
			final CMMsg msg2=CMClass.getMsg(msg.source(),item,msg.targetCode(),null);
			if(!okMessage(msg.source(),msg2))
				return false;
		}
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.source().getVictim()==mob)
			msg.source().setVictim(null);
		if(mob.isInCombat())
		{
			final MOB victim=mob.getVictim();
			if(victim!=null)
				victim.makePeace(true);
			mob.makePeace(true);
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setName(L("a tree that reminds you of @x1",affected.name()));
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SMELL);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TASTE);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> body is no longer treeish."));
			if(oldState!=null)
			{
				mob.curState().setHitPoints(oldState.getHitPoints());
				mob.curState().setHunger(oldState.getHunger());
				mob.curState().setMana(oldState.getMana());
				mob.curState().setMovement(oldState.getMovement());
				mob.curState().setThirst(oldState.getThirst());
			}
			else
			{
				mob.curState().setHitPoints(1);
				mob.curState().setMana(0);
				mob.curState().setMovement(0);
				mob.curState().setHunger(0);
				mob.curState().setThirst(0);
			}
			CMLib.commands().postStand(mob,true);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors to try this."));
			return false;
		}

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already a tree."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					target.makePeace(true);
					CMLib.commands().postStand(target,true);
					oldState=(CharState)target.curState().copyOf();
					success=beneficialAffect(mob,target,asLevel,(mob.phyStats().level()+(2*getXLEVELLevel(mob)))*50)!=null;
					if(success)
					{
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> transform(s) into a tree!!"));
						target.tell(L("To return to your flesh body, try to leave this area."));
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
