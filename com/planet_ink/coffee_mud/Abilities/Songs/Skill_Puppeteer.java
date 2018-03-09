package com.planet_ink.coffee_mud.Abilities.Songs;
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

public class Skill_Puppeteer extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Puppeteer";
	}

	private final static String localizedName = CMLib.lang().L("Puppeteer");

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
		return CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"PUPPETEER","PUPPET"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		final Item puppet=(Item)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(invoker()))
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&((msg.sourceMajor(CMMsg.MASK_HANDS))
		||(msg.sourceMajor(CMMsg.MASK_MOVE)))
		&&(msg.targetMinor()!=CMMsg.TYP_SPEAK)
		&&(msg.targetMinor()==CMMsg.TYP_ORDER)
		&&(msg.targetMinor()!=CMMsg.TYP_PANIC)
		&&(!((msg.tool() instanceof Song)))
		&&(!((msg.tool() instanceof Skill_Puppeteer)))
		&&(!((msg.tool() instanceof Dance)))
		&&(!msg.amITarget(puppet)))
		{
			if((!msg.source().isInCombat())&&(msg.target() instanceof MOB))
			{
				if((msg.sourceMajor(CMMsg.MASK_MALICIOUS))
				||(msg.targetMajor(CMMsg.MASK_MALICIOUS)))
					msg.source().setVictim((MOB)msg.target());
			}
			invoker().location().show(invoker(),puppet,CMMsg.MSG_OK_ACTION,L("<S-NAME> animate(s) <T-NAMESELF>."));
			return false;
		}
		else
		if(msg.amITarget(puppet))
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_REMOVE:
				unInvoke();
				break;
			}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		final MOB M=invoker();
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room)
		&&(M!=null)
		&&(M.location().isContent((Item)affected)))
		{
			if(M.isInCombat())
			{
				final MOB victiM=M.getVictim();
				final boolean isHit=(CMLib.combat().rollToHit(CMLib.combat().adjustedAttackBonus(M,victiM)+(5*getXLEVELLevel(M))
							+((Item)affected).phyStats().attackAdjustment(),CMLib.combat().adjustedArmor(victiM), 0));
				if(!isHit)
					M.location().show(M,victiM,affected,CMMsg.MSG_OK_ACTION,L("<O-NAME> attacks <T-NAME> and misses!"));
				else
					CMLib.combat().postDamage(M,victiM,affected,
											CMLib.dice().roll(1,affected.phyStats().level()+(2*getXLEVELLevel(M)),1),
											CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,
											Weapon.TYPE_BASHING,L("@x1 attacks and <DAMAGE> <T-NAME>!",affected.name()));
			}
			else
			if(CMLib.dice().rollPercentage()>75)
			switch(CMLib.dice().roll(1,5,0))
			{
			case 1:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 walks around.",affected.name()));
				break;
			case 2:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 waves its little arms.",affected.name()));
				break;
			case 3:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 hugs you.",affected.name()));
				break;
			case 4:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 makes a few fake attacks.",affected.name()));
				break;
			case 5:
				M.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 dances around.",affected.name()));
				break;
			}
		}
		else
			unInvoke();
		return super.tick(ticking,tickID);
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
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("@x1 is already animated!",target.name(mob)));
			return false;
		}
		if((!target.Name().toLowerCase().endsWith(" puppet"))
		&&(!target.Name().toLowerCase().endsWith(" marionette")))
		{
			mob.tell(L("That's not a puppet!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,CMMsg.TYP_DELICATE_HANDS_ACT,CMMsg.TYP_DELICATE_HANDS_ACT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.unWear();
				if(mob.isMine(target))
					mob.location().show(mob,target,CMMsg.MSG_DROP,L("<S-NAME> start(s) animating <T-NAME>!"));
				else
					mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<S-NAME> start(s) animating <T-NAME>!"));
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
