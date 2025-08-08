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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class Prayer_BolsterPoison extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_BolsterPoison";
	}

	private final static String localizedName = CMLib.lang().L("Bolster Poison");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Poisoning Aura)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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

	private void bolsterTicksOfPoison(final Ability parentA, final Ability A, final int depth)
	{
		if((A!=null)
		&&(depth<3)
		&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)
		&&(A.canBeUninvoked())
		&&(!A.isNowAnAutoEffect()))
		{
			A.setStat("TICKDOWN", ""+(CMath.s_int(A.getStat("TICKDOWN"))+1));
			if(A instanceof SpellHolder)
			{
				final List<Ability> otherAs = ((SpellHolder)A).getSpells();
				for(final Ability A2 : otherAs)
					if((A2 != A)&&(A2 != parentA))
						bolsterTicksOfPoison(A,A2,depth+1);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID!=Tickable.TICKID_MOB)||(!(affected instanceof MOB)))
			return true;
		final MOB M = (MOB)affected;
		for(int i=0;i<M.numEffects();i++)
			bolsterTicksOfPoison(this,M.fetchEffect(i),0);
		return true;
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
			mob.tell(L("Your poisoning aura fades."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 to corrupt <T-NAMESELF> with an aura of poison.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				success = maliciousAffect(mob,target,asLevel,0, -1) != null;
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("^S<S-NAME> @x1 to corrupt <T-NAMESELF>, but <S-IS-ARE> not answered.",prayWord(mob)));

		return success;
	}
}
