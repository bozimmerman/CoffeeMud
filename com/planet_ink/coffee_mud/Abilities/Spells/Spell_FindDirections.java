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

public class Spell_FindDirections extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FindDirections";
	}

	private final static String localizedName = CMLib.lang().L("Find Directions");

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
		return Ability.CAN_AREAS;
	}

	@Override
	protected int canAffectCode()
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
		final Room targetR=mob.location();
		if(targetR==null)
			return false;

		if((commands.size()>0)
		&&((commands.get(0)).toLowerCase().startsWith("direction")))
			commands.remove(0);
		Area A=null;
		if(commands.size()>0)
		{
			A=CMLib.map().findArea(CMParms.combine(commands));
			if(A!=null)
			{
				if(!CMLib.flags().canAccess(mob, A))
					A=null;
				else
				{
					boolean foundOne=false;
					for(int i=0;i<10;i++)
					{
						if(CMLib.flags().canAccess(mob, A.getRandomProperRoom()))
						{
							foundOne=true;
							break;
						}
					}
					if(!foundOne)
						A=null;
				}
			}
		}

		if(A==null)
		{
			mob.tell(L("You know of nowhere called \"@x1\".",CMParms.combine(commands)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,targetR,this,somanticCastCode(mob,targetR,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> hands around, pointing towards '@x1'.^?",A.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(L("The directions are taking shape in your mind: \n\r@x1",CMLib.tracking().getTrailToDescription(targetR, new Vector<Room>(), A.Name(), false, false, 100, null,1)));
			}
		}
		else
			beneficialVisualFizzle(mob,targetR,L("<S-NAME> wave(s) <S-HIS-HER> hands around, looking more frustrated every second."));

		// return whether it worked
		return success;
	}
}
