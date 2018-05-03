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
public class Play_Accompaniment extends Play
{
	@Override
	public String ID()
	{
		return "Play_Accompaniment";
	}

	private final static String localizedName = CMLib.lang().L("Accompaniment");

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
	public void affectPhyStats(Physical E, PhyStats stats)
	{
		super.affectPhyStats(E,stats);
		if((E instanceof MOB)&&(E!=invoker())&&(((MOB)E).charStats().getCurrentClass().baseClass().equals("Bard")))
		{
			int lvl=adjustedLevel(invoker(),0)/10;
			if(lvl<1)
				lvl=1;
			stats.setLevel(stats.level()+lvl);
		}
	}

	@Override
	public void affectCharStats(MOB E, CharStats stats)
	{
		super.affectCharStats(E,stats);
		if((E != null)&&(E!=invoker())&&(stats.getCurrentClass().baseClass().equals("Bard")))
		{
			int lvl=adjustedLevel(invoker(),0)/10;
			if(lvl<1)
				lvl=1;
			stats.setClassLevel(stats.getCurrentClass(),stats.getCurrentClassLevel()+lvl);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.getGroupMembers(new HashSet<MOB>()).size()<2)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

}
