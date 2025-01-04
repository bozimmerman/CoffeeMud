package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Stoicism extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Stoicism";
	}

	private final static String	localizedName	= CMLib.lang().L("Stoicism");

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
		return CAN_MOBS;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	private volatile boolean activated = false;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==affected)
		&&(msg.tool() instanceof Ability)
		&&(((msg.tool()).ID().equalsIgnoreCase("Mood")))
		&&(affected instanceof MOB)
		&&(super.proficiencyCheck((MOB)affected, 0, false)))
		{
			msg.source().tell(msg.source(),msg.target(),null,L("<T-YOUPOSS> stoicism level(s) <T-HIS-HER> mood."));
			return false;
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final Physical affected = super.affected;
		if(!(affected instanceof MOB))
			return true;
		if(super.proficiencyCheck((MOB)affected, 0, false))
		{
			super.helpProficiency((MOB)affected, 0);
			if(!activated)
			{
				activated=true;
				((MOB)affected).recoverPhyStats();
			}
		}
		else
		if(activated)
		{
			activated=false;
			((MOB)affected).recoverPhyStats();
		}
		return true;
	}


	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(activated)
		{
			affectableStats.addAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD.code());
			affectableStats.addAmbiance(PhyStats.Ambiance.SUPPRESS_DRUNKENNESS.code());
		}
	}

}
