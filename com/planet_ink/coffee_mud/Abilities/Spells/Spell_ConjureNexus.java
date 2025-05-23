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
   Copyright 2003-2025 Bo Zimmerman

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
public class Spell_ConjureNexus extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ConjureNexus";
	}

	private final static String localizedName = CMLib.lang().L("Conjure Nexus");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void unInvoke()
	{
		if((canBeUninvoked())&&(invoker()!=null)&&(affected instanceof Room))
			invoker().tell(L("The Nexus in '@x1' dissipates.",((Room)affected).displayText()));
		super.unInvoke();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof Room)))
			return false;
		final Room R=(Room)affected;
		if(tickID==Tickable.TICKID_MOB)
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB mob=R.fetchInhabitant(m);
				if(mob!=null)
				{
					final int oldHP=mob.curState().getHitPoints();
					final int oldMV=mob.curState().getMovement();
					final int oldHU=mob.curState().getHunger();
					final int oldTH=mob.curState().getThirst();
					CMLib.combat().recoverTick(mob);
					mob.curState().setHitPoints(oldHP);
					mob.curState().setMovement(oldMV);
					mob.curState().setHunger(oldHU);
					mob.curState().setThirst(oldTH);
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		try
		{
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
				{
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(A.ID().equals(ID())))
						{
							A.unInvoke();
							break;
						}
					}
				}
			}
		}
		catch (final NoSuchElementException nse)
		{
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"A Nexus of Mana appears!":L("^S<S-NAME> summon(s) the Nexus of mana!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to summon a Nexus, but fail(s)."));

		// return whether it worked
		return success;
	}
}
