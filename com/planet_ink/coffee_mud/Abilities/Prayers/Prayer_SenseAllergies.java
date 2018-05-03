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
   Copyright 2008-2018 Bo Zimmerman

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

public class Prayer_SenseAllergies extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SenseAllergies";
	}

	private final static String localizedName = CMLib.lang().L("Sense Allergies");

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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":(mob==target)?"^S<S-NAME> close(s) <T-HIS-HER> eyes and peer(s) into <T-HIS-HER> own nostrils.^?":"^S<S-NAME> peer(s) into the nostrils of <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=target.fetchEffect("Allergies");
				if(A==null)
					mob.tell(mob,target,null,L("<T-NAME> seem(s) like <T-HE-SHE> is not allergic to anything."));
				else
				{

					final Vector<String> allergies=new Vector<String>();
					final Vector<String> V=CMParms.parse(A.text().toUpperCase().trim());
					for(int i=0;i<V.size();i++)
					{
						if(CMParms.contains(RawMaterial.CODES.NAMES(), V.elementAt(i)))
							allergies.addElement(V.elementAt(i).toLowerCase());
						else
						{
							final Race R=CMClass.getRace(V.elementAt(i));
							if(R!=null)
								allergies.addElement(R.name());
						}
					}
					mob.tell(mob,target,null,L("<T-NAME> seem(s) like <T-HE-SHE> is allergic to @x1.",CMParms.toListString(V)));
				}
			}
		}
		else
		if(mob==target)
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> close(s) <T-HIS-HER> eyes and peer(s) into <T-HIS-HER> own nostrils, but then blink(s)."));
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> peer(s) into the nostrils of <T-NAMESELF>, but then blink(s)."));

		// return whether it worked
		return success;
	}
}
