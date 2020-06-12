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
public class Spell_ImprovedPlanarmorph extends Spell_Planarmorph
{
	@Override
	public String ID()
	{
		return "Spell_ImprovedPlanarmorph";
	}

	private final static String localizedName = CMLib.lang().L("Improved Planarmorph");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Improved Planarmorph)");

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
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	protected volatile String selectedPlane = "";

	@Override
	protected String getPlanarTarget()
	{
		return selectedPlane;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final PlanarAbility plane =(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		final Vector<String> cmds = new XVector<String>(commands);
		if(commands.size()>=2)
		{
			final String selectedPlane = CMParms.combine(cmds,1).toUpperCase();
			while(cmds.size()>=2)
				cmds.remove(1);
			if(plane.getPlanarVars(selectedPlane)==null)
			{
				mob.tell(L("Which plane?"));
				mob.tell(L("Known planes: @x1",plane.listOfPlanes()+L("Prime Material")));
				return false;
			}
			this.selectedPlane = selectedPlane;
		}
		else
		if(mob.isMonster())
		{
			if(commands.size()==0)
				cmds.add(givenTarget==null?"all":givenTarget.Name());
			this.selectedPlane = plane.getAllPlaneKeys().get(CMLib.dice().roll(1, plane.getAllPlaneKeys().size(), -1)).toUpperCase();
		}
		else
		{
			mob.tell(L("You must specify a target, and the name of a plane of existence."));
			return false;
		}
		return super.invoke(mob, cmds, givenTarget, auto, asLevel);
	}
}
