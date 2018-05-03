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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_DetectSentience extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DetectSentience";
	}

	private final static String	localizedName	= CMLib.lang().L("Detect Sentience");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_DIVINATION;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,mob,auto),auto?"":L("^S<S-NAME> incant(s) softly to <S-HIM-HERSELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final StringBuffer lines=new StringBuffer("^x");
				lines.append(CMStrings.padRight(L("Name"),25)+"| ");
				lines.append(CMStrings.padRight(L("Location"),17)+"^.^N\n\r");
				TrackingLibrary.TrackingFlags flags;
				flags = CMLib.tracking().newFlags()
						.plus(TrackingLibrary.TrackingFlag.AREAONLY);
				final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,35+this.getXMAXRANGELevel(mob));
				if(!checkSet.contains(mob.location()))
					checkSet.add(mob.location());
				final CMMsg msg2=CMClass.getMsg(mob,null,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_CAST,null);
				for (final Room room : checkSet)
				{
					final Room R=CMLib.map().getRoom(room);
					if(CMLib.flags().canAccess(mob, R))
					{
						for(int m=0;m<R.numInhabitants();m++)
						{
							final MOB M=R.fetchInhabitant(m);
							if((M!=null)
							&&(M.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=2)
							&&(M.fetchEffect("Prop_WizInvis")==null))
							{
								msg2.setTarget(M);
								if(R.okMessage(mob,msg2))
								{
									R.send(mob,msg2);
									lines.append("^!"+CMStrings.padRight(M.name(mob),25)+"^?| ");
									lines.append(R.displayText(mob));
									lines.append("\n\r");
								}
							}
						}
					}
				}
				mob.tell(lines.toString()+"^.");
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> incant(s) to <S-HIM-HERSELF>, but the spell fizzles."));

		return success;
	}
}
