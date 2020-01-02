package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2004-2020 Bo Zimmerman

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
public class Prayer_ProtDeath extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_ProtDeath";
	}

	private final static String localizedName = CMLib.lang().L("Protection Death");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Protection from Death)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room startRoom=mob.getStartRoom();
			if(msg.amISource(mob)
			&&(msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(startRoom!=null))
			{
				if(mob.fetchAbility("Dueling")!=null)
					return super.okMessage(host,msg);
				final int chance=30 + super.getXLEVELLevel(invoker());
				if(CMLib.dice().rollPercentage()<chance)
				{
					final Room oldRoom=mob.location();
					mob.resetToMaxState();
					oldRoom.show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> <S-IS-ARE> protected from death!^?"));
					startRoom.bringMobHere(mob,false);
					unInvoke();
					for(int a=mob.numEffects()-1;a>=0;a--) // personal effects
					{
						final Ability A=mob.fetchEffect(a);
						if(A!=null)
							A.unInvoke();
					}
					if((oldRoom!=startRoom) && oldRoom.isInhabitant(mob) && startRoom.isInhabitant(mob))
						oldRoom.delInhabitant(mob); // hopefully unnecessary
					return false;
				}
				else
					unInvoke();
			}
		}
		return super.okMessage(host,msg);
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
			mob.tell(L("Your death protection fades."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> become(s) protected from death!"):L("^S<S-NAME> @x1 for <T-NAME> to be protected from death!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for <T-NAMESELF>, but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
