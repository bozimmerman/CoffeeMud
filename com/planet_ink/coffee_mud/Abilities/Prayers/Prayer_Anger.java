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
   Copyright 2001-2018 Bo Zimmerman

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

public class Prayer_Anger extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Anger";
	}

	private final static String localizedName = CMLib.lang().L("Anger");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	private boolean anyoneIsFighting(Room R)
	{
		if(R==null)
			return false;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB inhab=R.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
				return true;
		}
		return false;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!anyoneIsFighting(mob.location()))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.location().numInhabitants()>3)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final boolean someoneIsFighting=anyoneIsFighting(mob.location());

		if((success)&&(!someoneIsFighting)&&(mob.location().numInhabitants()>3))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?L("A feeling of anger descends"):L("^S<S-NAME> rage(s) for anger.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					final MOB inhab=mob.location().fetchInhabitant(i);
					if((inhab!=null)&&(inhab!=mob)&&(!inhab.isInCombat()))
					{
						int tries=0;
						MOB target=null;
						while((tries<100)&&(target==null))
						{
							target=mob.location().fetchRandomInhabitant();
							if(target!=null)
							{
								if(target==inhab)
									target=null;
								if(target==mob)
									target=null;
							}
							tries++;
						}
						final CMMsg amsg=CMClass.getMsg(mob,inhab,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
						if((target!=null)&&(mob.location().okMessage(mob,amsg)))
						{
							inhab.tell(L("You feel angry."));
							inhab.setVictim(target);
						}
					}
				}
			}
		}
		else
			maliciousFizzle(mob,null,L("<S-NAME> @x1 for rage, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
