package com.planet_ink.coffee_mud.Items.BasicTech;

import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2025 Bo Zimmerman

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
public class StdTechItem extends StdItem implements Technical
{
	@Override
	public String ID()
	{
		return "StdTechItem";
	}

	protected String 		manufacturer	= "RANDOM";
	protected Manufacturer  cachedManufact  = null;

	public StdTechItem()
	{
		super();
		setName("a piece of technology");
		setDisplayText("a small piece of technology sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material = RawMaterial.RESOURCE_STEEL;
		baseGoldValue = 0;
		recoverPhyStats();
	}

	@Override
	public int techLevel()
	{
		return phyStats().ability();
	}

	@Override
	public void setTechLevel(final int lvl)
	{
		basePhyStats.setAbility(lvl);
		recoverPhyStats();
	}

	@Override
	public TechType getTechType()
	{
		return TechType.GIZMO;
	}

	@Override
	public String getManufacturerName()
	{
		return manufacturer;
	}

	@Override
	public void setManufacturerName(final String name)
	{
		cachedManufact = null;
		if (name != null)
			manufacturer = name;
	}

	@Override
	public Manufacturer getFinalManufacturer()
	{
		if(cachedManufact==null)
		{
			cachedManufact=CMLib.tech().getManufacturerOf(this,manufacturer.toUpperCase().trim());
			if(cachedManufact==null)
				cachedManufact=CMLib.tech().getDefaultManufacturer();
		}
		return cachedManufact;
	}
}
