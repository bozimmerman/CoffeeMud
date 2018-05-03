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

public class Spell_Toadstool extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Toadstool";
	}

	private final static String localizedName = CMLib.lang().L("Toadstool");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Toadstool)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	Race newRace=null;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.name().indexOf(' ')>0)
				affectableStats.setName(L("a @x1 called @x2",newRace.name(),affected.name()));
			else
				affectableStats.setName(L("@x1 the @x2",affected.name(),newRace.name()));
			final int oldAdd=affectableStats.weight()-affected.basePhyStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0)
				affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
		affectableStats.setLevel(1);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
		{
			final int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			affectableStats.setWearableRestrictionsBitmap(affectableStats.getWearableRestrictionsBitmap()|affectableStats.getMyRace().forbiddenWornBits());
			if(affected.baseCharStats().getStat(CharStats.STAT_AGE)>0)
				affectableStats.setStat(CharStats.STAT_AGE,newRace.getAgingChart()[oldCat]);
		}
		affectableStats.setMyClasses("StdCharClass");
		affectableStats.setMyLevels("1");
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectableState.setHitPoints(20);
		affectableState.setMana(100);
		affectableState.setMovement(0);
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> morph(s) back into <S-HIM-HERSELF> again."));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.baseCharStats().getMyRace() != target.charStats().getMyRace())
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already polymorphed."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int chance=-((target.phyStats().level()-adjustedLevel(mob,asLevel))*5);
		boolean success=proficiencyCheck(mob,chance-(target.charStats().getStat(CharStats.STAT_CONSTITUTION)*2),auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> form(s) a spell around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=CMClass.getRace("Toadstool");
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> become(s) a @x1!",newRace.name()));
					success=beneficialAffect(mob,target,asLevel,0)!=null;
					target.makePeace(true);
					for(int i=0;i<mob.location().numInhabitants();i++)
					{
						final MOB M=mob.location().fetchInhabitant(i);
						if((M!=null)&&(M.getVictim()==target))
							M.makePeace(true);
					}
					target.recoverCharStats();
					CMLib.utensils().confirmWearability(target);
					target.resetToMaxState();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> form(s) a spell around <T-NAMESELF>, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
