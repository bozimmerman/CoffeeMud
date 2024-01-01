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
   Copyright 2020-2024 Bo Zimmerman

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
public class Prayer_ShareBoon extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_ShareBoon";
	}

	private final static String localizedName = CMLib.lang().L("Share Boon");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_BLESSING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	public Ability getBoon(final MOB mob)
	{
		final List<Ability> choices = new ArrayList<Ability>();
		for(final Enumeration<Ability> m=mob.effects();m.hasMoreElements();)
		{
			final Ability A=m.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(A.canBeUninvoked())
			&&(!A.isAutoInvoked())
			&&(A.triggerStrings()!=null)
			&&(A.triggerStrings().length>0)
			&&(A.castingQuality(mob, mob)!=Ability.QUALITY_MALICIOUS)
			&&(!A.isSavable())
			&&(A.displayText().length()>0)
			&&(A.invoker()!=null))
				choices.add(A);
		}
		if(choices.size()==0)
			return null;
		return choices.get(CMLib.dice().roll(1, choices.size(), -1));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{

		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h.size()<2)
		{
			mob.tell(L("You've no one here to share a boon with."));
			return false;
		}

		if(this.getBoon(mob)==null)
		{
			mob.tell(L("You lack an appropriate boon to share."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;

			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),
					(auto?"":L("^S<S-NAME> @x1 while waving <S-HIS-HER> hands around.^?",prayWord(mob)))))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;
					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto),null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						final Ability boonA=getBoon(mob);
						if((boonA!=null)
						&&(target.fetchEffect(boonA.ID())==null))
						{
							final MOB invokerM=boonA.invoker();
							boonA.invoke(invokerM, commands, target, true, asLevel);
							final Ability boonE=target.fetchEffect(boonA.ID());
							if((boonE!=null)
							&&(boonA.expirationDate()>0))
								boonE.setExpirationDate(System.currentTimeMillis() + boonA.expirationDate());
						}
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> @x1, but <S-HIS-HER> faith fail(s) miserably.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
