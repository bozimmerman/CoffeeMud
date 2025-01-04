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
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2021-2025 Bo Zimmerman

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
public class Spell_ImprovedHarden extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ImprovedHarden";
	}

	private final static String localizedName = CMLib.lang().L("Improved Harden");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Improved Harden spell)");

	private final AtomicInteger lastUses = new AtomicInteger(-1);

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof SiegableItem)
			affectableStats.setArmor(affectableStats.armor() + 20 + (adjustedLevel(invoker(),0)/2) + (super.getXLEVELLevel(invoker())) );
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if((affected instanceof Item)
		&&(!(affected instanceof Boardable))
		&&(!affected.amDestroyed()))
		{
			final Item I=(Item)affected;
			if((I.usesRemaining()<lastUses.get())
			&&(lastUses.get()>0))
			{
				if(CMLib.dice().rollPercentage()<40+(5*super.getXLEVELLevel(invoker())))
					I.setUsesRemaining(I.usesRemaining()+1);
			}
			lastUses.set(I.usesRemaining());
		}

		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Room R=CMLib.map().roomLocation(mob);
		if(R==null)
			R=mob.location();

		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if(target.fetchEffect("Spell_Harden")!=null)
		{
			mob.tell(L("@x1 is already hardened.",target.name(mob)));
			return false;
		}
		if(!target.subjectToWearAndTear())
		{
			mob.tell(L("@x1 cannot be hardened.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands, givenTarget, auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,(((mob.phyStats().level()+(2*getXLEVELLevel(mob)))-target.phyStats().level())*5),auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> invoke(s) a powerfully hard spell upon <T-NAMESELF>.^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				R.show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> look(s) powerfully hardened!"));
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a powerfully hard spell on <T-NAME>, but fail(s) miserably."));

		// return whether it worked
		return success;
	}
}
