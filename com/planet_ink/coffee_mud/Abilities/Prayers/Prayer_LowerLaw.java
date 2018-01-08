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
   Copyright 2011-2018 Bo Zimmerman

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

public class Prayer_LowerLaw extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_LowerLaw";
	}

	private final static String localizedName = CMLib.lang().L("Lower Law");

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
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	public void possiblyAddLaw(Law L, Vector<String> V, String code)
	{
		if(L.basicCrimes().containsKey(code))
		{
			final String name=L.basicCrimes().get(code)[Law.BIT_CRIMENAME];
			if(!V.contains(name))
				V.add(name);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> @x1 for knowledge of the lower law here.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Area O=CMLib.law().getLegalObject(mob.location());
				final LegalBehavior B=CMLib.law().getLegalBehavior(mob.location());
				if((B==null)||(O==null))
					mob.tell(L("No lower law is established here."));
				else
				{
					final Law L=B.legalInfo(O);
					final Vector<String> crimes=new Vector<String>();
					possiblyAddLaw(L,crimes,"TRESPASSING");
					possiblyAddLaw(L,crimes,"ASSAULT");
					possiblyAddLaw(L,crimes,"MURDER");
					possiblyAddLaw(L,crimes,"NUDITY");
					possiblyAddLaw(L,crimes,"ARMED");
					possiblyAddLaw(L,crimes,"RESISTINGARREST");
					possiblyAddLaw(L,crimes,"PROPERTYROB");
					for(final String key : L.abilityCrimes().keySet())
					{
						if(key.startsWith("$"))
							crimes.add(key.substring(1));
					}
					if(L.taxLaws().containsKey("TAXEVASION"))
						crimes.add(((String[])L.taxLaws().get("TAXEVASION"))[Law.BIT_CRIMENAME]);
					for(int x=0;x<L.bannedSubstances().size();x++)
					{
						final String name=L.bannedBits().get(x)[Law.BIT_CRIMENAME];
						if(!crimes.contains(name))
							crimes.add(name);
					}
					for(int x=0;x<L.otherCrimes().size();x++)
					{
						final String name=L.otherBits().get(x)[Law.BIT_CRIMENAME];
						if(!crimes.contains(name))
							crimes.add(name);
					}
					mob.tell(L("The following lower crimes are divinely revealed to you: @x1.",CMLib.english().toEnglishStringList(crimes.toArray(new String[0]))));
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> @x1, but nothing is revealed.",prayWord(mob)));

		return success;
	}
}
