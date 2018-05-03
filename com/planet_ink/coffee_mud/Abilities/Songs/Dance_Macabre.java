package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Dance_Macabre extends Dance
{
	@Override
	public String ID()
	{
		return "Dance_Macabre";
	}

	private final static String localizedName = CMLib.lang().L("Macabre");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected String danceOf()
	{
		return name()+" Dance";
	}

	protected boolean activated=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(activated)
		{
			affectableStats.setDamage(affectableStats.damage()+(adjustedLevel(invoker(),0)/2));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(adjustedLevel(invoker(),0)*3));
		}
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).isInCombat())
		&&(((MOB)affected).getVictim().isInCombat())
		&&(((MOB)affected).getVictim()!=affected))
		{
			affectableStats.setDamage(affectableStats.damage()+(adjustedLevel(invoker(),0)/4));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+adjustedLevel(invoker(),0));
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(CMLib.flags().isHidden(affected))
		{
			if(!activated)
			{
				activated=true;
				affected.recoverPhyStats();
			}
		}
		else
		if(activated)
		{
			activated=false;
			affected.recoverPhyStats();
		}
		return super.tick(ticking,tickID);
	}

}
