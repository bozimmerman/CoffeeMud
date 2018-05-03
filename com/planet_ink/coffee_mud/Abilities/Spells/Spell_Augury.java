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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_Augury extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Augury";
	}

	private final static String localizedName = CMLib.lang().L("Augury");

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
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Divine the fate of which direction?"));
			return false;
		}
		final String targetName=CMParms.combine(commands,0);

		Exit exit=null;
		Exit opExit=null;
		Room room=null;
		final int dirCode=CMLib.directions().getGoodDirectionCode(targetName);
		if(dirCode>=0)
		{
			exit=mob.location().getExitInDir(dirCode);
			room=mob.location().getRoomInDir(dirCode);
			if(room!=null)
				opExit=mob.location().getReverseExit(dirCode);
		}
		else
		{
			mob.tell(L("Divine the fate of which direction?"));
			return false;
		}
		if((exit==null)||(room==null))
		{
			mob.tell(L("You couldn't go that way if you wanted to!"));
			return false;
		}

		if(!super.invoke(mob,commands,null,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> point(s) <S-HIS-HER> finger @x1, incanting.^?",CMLib.directions().getDirectionName(dirCode)));
			if(mob.location().okMessage(mob,msg))
			{
				boolean aggressiveMonster=false;
				for(int m=0;m<room.numInhabitants();m++)
				{
					final MOB mon=room.fetchInhabitant(m);
					if(mon!=null)
					{
						for(final Enumeration<Behavior> e=mob.behaviors();e.hasMoreElements();)
						{
							final Behavior B=e.nextElement();
							if((B!=null)&&(B.grantsAggressivenessTo(mob)))
							{
								aggressiveMonster=true;
								break;
							}
						}
					}
				}
				mob.location().send(mob,msg);
				if((aggressiveMonster)
				||CMLib.flags().isDeadlyOrMaliciousEffect(room)
				||CMLib.flags().isDeadlyOrMaliciousEffect(exit)
				||((opExit!=null)&&(CMLib.flags().isDeadlyOrMaliciousEffect(opExit))))
					mob.tell(L("You feel going that way would be bad."));
				else
					mob.tell(L("You feel going that way would be ok."));
			}

		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> point(s) <S-HIS-HER> finger @x1, incanting, but then loses concentration.",CMLib.directions().getDirectionName(dirCode)));

		// return whether it worked
		return success;
	}
}
