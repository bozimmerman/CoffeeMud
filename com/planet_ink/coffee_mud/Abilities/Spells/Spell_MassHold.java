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
   Copyright 2012-2018 Bo Zimmerman

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

public class Spell_MassHold extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_MassHold";
	}

	private final static String localizedName = CMLib.lang().L("Mass Hold");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(L("There doesn't appear to be anyone here worth putting to sleep."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,-20,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms.^?")))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// if they can't hear the sleep spell, it
					// won't happen
					if(CMLib.flags().canBeHeardSpeakingBy(mob,target))
					{
						final MOB oldVictim=mob.getVictim();
						final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),null);
						if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
						{
							mob.location().send(mob,msg);
							if(msg.value()<=0)
							{
								int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
								if(levelDiff<0)
									levelDiff=0;
								if(levelDiff>6)
									levelDiff=6;

								final Spell_Hold spell=new Spell_Hold();
								spell.setProficiency(proficiency());
								success=spell.maliciousAffect(mob,target,asLevel,7-levelDiff,-1)!=null;
								if(success)
									if(target.location()==mob.location())
										target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> become(s) perfectly still!!"));
							}
						}
						if(oldVictim==null)
							mob.setVictim(null);
					}
					else
						maliciousFizzle(mob,target,L("<T-NAME> seem(s) unaffected by the spell from <S-NAME>."));
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> incant(s) a spell, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
