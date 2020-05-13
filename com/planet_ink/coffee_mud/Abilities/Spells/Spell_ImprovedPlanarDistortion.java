package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdPlanarAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
public class Spell_ImprovedPlanarDistortion extends Spell_PlanarDistortion
{

	@Override
	public String ID()
	{
		return "Spell_ImprovedPlanarDistortion";
	}

	private final static String localizedName = CMLib.lang().L("Improved Planar Distortion");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Improved Planar Distortion)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected String getPlaneName(final MOB mob, final List<String> commands)
	{
		final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		if((commands.size()<2)
		&&(mob.isMonster()))
			commands.add(planeA.getAllPlaneKeys().get(CMLib.dice().roll(1, planeA.getAllPlaneKeys().size(), -1)));
		if(commands.size()<2)
		{
			mob.tell(L("You need to specify which plane to create a distortion of."));
			mob.tell(L("Known planes: @x1",planeA.listOfPlanes()+L("Prime Material")));
			return null;
		}
		final String planeName = CMParms.combine(commands,1);
		if(!planeA.getAllPlaneKeys().contains(planeName.toUpperCase()))
		{
			mob.tell(L("'@x1' is not a plane name.",planeName));
			mob.tell(L("Known planes: @x1",planeA.listOfPlanes()+L("Prime Material")));
			return null;
		}
		final String name=commands.get(0);
		commands.clear();
		commands.add(name);
		return planeName;
	}

}
