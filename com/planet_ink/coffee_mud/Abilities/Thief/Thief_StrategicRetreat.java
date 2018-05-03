package com.planet_ink.coffee_mud.Abilities.Thief;
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

public class Thief_StrategicRetreat extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_StrategicRetreat";
	}

	private final static String localizedName = CMLib.lang().L("Strategic Retreat");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"FREEFLEE","STRATEGICRETREAT"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You can only retreat from combat!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String where=CMParms.combine(commands,0);
		if(!success)
		{
			mob.tell(L("Your attempt to flee with grace and honor FAILS!"));
			CMLib.commands().postFlee(mob,where);
		}
		else
		{
			int directionCode=-1;
			if(!where.equals("NOWHERE"))
			{
				if(where.length()==0)
				{
					final Vector<Integer> directions=new Vector<Integer>();
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Exit thisExit=mob.location().getExitInDir(d);
						final Room thisRoom=mob.location().getRoomInDir(d);
						if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
							directions.addElement(Integer.valueOf(d));
					}
					// up is last resort
					if(directions.size()>1)
						directions.removeElement(Integer.valueOf(Directions.UP));
					if(directions.size()>0)
					{
						directionCode=directions.elementAt(CMLib.dice().roll(1,directions.size(),-1)).intValue();
						where=CMLib.directions().getDirectionName(directionCode);
					}
				}
				else
					directionCode=CMLib.directions().getGoodDirectionCode(where);
				if(directionCode<0)
				{
					mob.tell(L("Flee where?!"));
					return false;
				}
				mob.makePeace(true);
				CMLib.tracking().walk(mob,directionCode,true,false);
			}
		}
		return success;
	}
}
