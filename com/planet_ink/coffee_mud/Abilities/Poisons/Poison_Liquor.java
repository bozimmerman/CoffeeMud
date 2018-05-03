package com.planet_ink.coffee_mud.Abilities.Poisons;
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

public class Poison_Liquor extends Poison_Alcohol
{
	@Override
	public String ID()
	{
		return "Poison_Liquor";
	}

	private final static String localizedName = CMLib.lang().L("Liquor");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "LIQUORUP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_POISON;
	}

	@Override
	protected int alchoholContribution()
	{
		return 2;
	}

	@Override
	protected int level()
	{
		return 2;
	}

	@Override
	public void unInvoke()
	{
		int drunkness=this.drunkness;
		MOB mob=null;
		if(affected instanceof MOB)
		{
			mob=(MOB)affected;
			if((CMLib.dice().rollPercentage()<(drunkness*10))&&(!((MOB)affected).isMonster()))
			{
				final Ability A=CMClass.getAbility("Disease_Migraines");
				if(A!=null)
					A.invoke(mob,mob,true,0);
			}
			CMLib.commands().postStand(mob,true);
		}
		super.unInvoke();
		if((mob!=null)&&(!mob.isInCombat())&&(drunkness>0)&&(super.canBeUninvoked()))
			mob.location().show(mob,null,CMMsg.MSG_SLEEP,L("<S-NAME> curl(s) up on the ground and fall(s) asleep."));
	}
}
