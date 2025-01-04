package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2023-2025 Bo Zimmerman

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
public class Paladin_CrushingAura extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_CrushingAura";
	}

	private final static String localizedName = CMLib.lang().L("Crushing Aura");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_VEXING;
	}

	public Paladin_CrushingAura()
	{
		super();
		paladinsGroup=new SHashSet<MOB>();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((mob!=null)
			&&(mob.isInCombat())
			&&(super.appropriateToMyFactions((MOB)affected)))
			{
				final MOB vicM=mob.getVictim();
				if((vicM!=null)
				&&(CMath.div(vicM.curState().getHitPoints(), vicM.maxState().getHitPoints())<=0.1)
				&&(CMLib.dice().rollPercentage()>vicM.charStats().getSave(CharStats.STAT_SAVE_JUSTICE)))
				{
					if(mob.location().show(mob,vicM,this,CMMsg.MSG_OK_VISUAL, L("^SAn aura around <S-NAME> crushes <T-NAME>.^?")))
						CMLib.combat().postDeath(mob, vicM, null);
				}
			}
		}
		return true;
	}
}
