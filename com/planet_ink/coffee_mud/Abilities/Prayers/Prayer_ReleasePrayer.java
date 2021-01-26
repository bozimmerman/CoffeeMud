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
   Copyright 2020-2021 Bo Zimmerman

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
public class Prayer_ReleasePrayer extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_ReleasePrayer";
	}

	private final static String localizedName = CMLib.lang().L("Release Prayer");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_ARCANELORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String whichA=CMParms.combine(commands,0);

		final List<Ability> mySpells=new ArrayList<Ability>();
		for(final Enumeration<Ability> a=mob.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(!A.isSavable())
			&&((whichA.length()==0)||(CMLib.english().containsString(A.Name(), whichA))))
				mySpells.add(A);
		}

		if(mySpells.size()==0)
		{
			if(whichA.length()>0)
				mob.tell(L("You don't know any prayers called '@x1'",whichA));
			else
				mob.tell(L("You have no divine magic to release."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),L("^S<S-NAME> @x1 for a divine release!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Room R=mob.location();
				for(final Ability A : mySpells)
				{
					final CMMsg cmsg=CMClass.getMsg(mob,null,A,CMMsg.TYP_CAST_SPELL,L("You feel <S-NAME> release <O-NAME>"),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
					R.send(mob, cmsg);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 for a divine release, but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
