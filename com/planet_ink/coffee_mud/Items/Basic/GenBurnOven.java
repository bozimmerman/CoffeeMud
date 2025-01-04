package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2024-2025 Bo Zimmerman

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
public class GenBurnOven extends GenContainer
{
	@Override
	public String ID()
	{
		return "GenBurnOven";
	}

	public GenBurnOven()
	{
		super();
		setName("a wood burning oven");
		basePhyStats.setWeight(1);
		setDisplayText("a wood burning oven sits here.");
		setDescription("It burn coal or wood to make a fine meal.  Open it and find out what's inside");
		baseGoldValue=10;
		basePhyStats.setWeight(20);
		material=RawMaterial.RESOURCE_STONE;
		capacity=150;
		super.hasALid=true;
		super.isOpen=true;
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==this)
		&&(msg.tool() instanceof Ability)
		&&(msg.tool().ID().equals("FireBuilding"))
		&&(super.isOpen())
		&&(!CMLib.flags().isOnFire(this)))
		{
			final List<Item> lightables = super.getContents();
			for(final Item I : lightables)
			{
				if(CMLib.materials().getBurnDuration(I)>0)
				{
					msg.setTarget(I);
					return false;
				}
			}
		}
		return true;
	}
}
