package com.planet_ink.coffee_mud.Abilities.Diseases;
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

public class Disease_Fever extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Fever";
	}

	private final static String localizedName = CMLib.lang().L("Fever");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Fever)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 1;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 15;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 3;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You head stops hurting.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> come(s) down with a fever.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		final MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			final MOB newvictim=mob.location().fetchRandomInhabitant();
			if(newvictim!=mob)
				mob.setVictim(newvictim);
		}
		else
		if(CMLib.flags().isAliveAwakeMobile(mob,false)
		&&(CMLib.flags().canSee(mob))
		&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1: mob.tell(L("You think you just saw your mother swim by.")); break;
			case 2: mob.tell(L("A pink elephant just attacked you!")); break;
			case 3: mob.tell(L("A horse just asked you a question.")); break;
			case 4: mob.tell(L("Your hands look very green.")); break;
			case 5: mob.tell(L("You think you just saw your father float by.")); break;
			case 6: mob.tell(L("A large piece of bread swings at you and misses!")); break;
			case 7: mob.tell(L("Oh, the pretty colors!")); break;
			case 8: mob.tell(L("You think you just saw something, but aren't sure.")); break;
			case 9: mob.tell(L("Hundreds of little rainbow bees buzz around your head.")); break;
			case 10: mob.tell(L("Everything looks upside-down.")); break;
			}
		}
		return super.tick(ticking,tickID);
	}

}
