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
   Copyright 2020-2023 Bo Zimmerman

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
public class Prayer_SenseDevotion extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SenseDevotion";
	}

	private final static String localizedName = CMLib.lang().L("Sense Devotion");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_LAW;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(target==mob)
		{
			mob.tell(L("You already know your own faith!"));
			return false;
		}

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,-(levelDiff*5),auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 and peer(s) at <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()==0)
				{
					if(target.charStats().getWorshipCharID().length()==0)
					{
						if(target.charStats().deityName().length()>0)
							mob.tell(mob,target,null,L("<T-HE-SHE> seem(s) to worship @x1.",target.charStats().deityName()));
						else
							mob.tell(mob,target,null,L("<T-HE-SHE> lack(s) faith."));
					}
					else
					{
						if(target.charStats().deityName().length()==0)
							mob.tell(mob,target,null,L("<T-HE-SHE> seem(s) to lack faith."));
						else
						if(!target.charStats().deityName().equalsIgnoreCase(target.charStats().getWorshipCharID()))
							mob.tell(mob,target,null,L("<T-HE-SHE> seem(s) to worship @x1.",target.charStats().deityName()));
						else
							mob.tell(mob,target,null,L("<T-HE-SHE> worship(s) @x1.",target.charStats().deityName()));
					}
				}
				else
					beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> @x1 and peer(s) at <T-NAMESELF>, but then blink(s).",prayWord(mob)));
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> @x1 and peer(s) at <T-NAMESELF>, but then blink(s).",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
