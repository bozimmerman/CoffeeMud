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
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_HeroicReflexes extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_HeroicReflexes";
	}

	private final static String localizedName = CMLib.lang().L("Heroic Reflexes");

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
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ACROBATIC;
	}

	private volatile boolean activated = false;
	
	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(activated)
			affectableStats.setStat(CharStats.STAT_SAVE_TRAPS,affectableStats.getStat(CharStats.STAT_SAVE_TRAPS)
																+(proficiency()/5)
																+(adjustedLevel(affected,0)/5)
																+(super.getXLEVELLevel(affected)));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool() instanceof Trap)
		&&(msg.source()!=affected)
		&&(msg.target()!=affected)
		&&(msg.target() instanceof MOB))
		{
			final Physical affected = this.affected;
			if((affected instanceof MOB)
			&&(((MOB)affected).getGroupMembers(new HashSet<MOB>()).contains(msg.target())))
			{
				final MOB mob=(MOB)affected;
				final Room R=mob.location();
				helpProficiency(mob, 0);
				if((msg.othersMessage()!=null)
				&&(msg.othersMessage().length()>0)
				&&(R!=null)
				&&(CMLib.flags().isAliveAwakeMobileUnbound(mob, true)))
				{
					R.send((MOB)msg.target(), msg);
					R.show(mob, msg.target(), CMMsg.MSG_OK_ACTION, L("<S-NAME> leap(s) in front of <T-NAME>!"));
					final Trap T=(Trap)msg.tool();
					try
					{
						activated=true;
						mob.recoverCharStats();
						T.spring(mob);
					}
					finally
					{
						activated=false;
					}
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
