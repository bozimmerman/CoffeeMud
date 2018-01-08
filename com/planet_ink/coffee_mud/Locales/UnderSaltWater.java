package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2002-2018 Bo Zimmerman

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
public class UnderSaltWater extends UnderWater
{
	@Override
	public String ID()
	{
		return "UnderSaltWater";
	}

	public UnderSaltWater()
	{
		super();
		climask=Places.CLIMASK_WET;
		atmosphere=RawMaterial.RESOURCE_SALTWATER;
		liquidType=RawMaterial.RESOURCE_SALTWATER;
	}

	public static final Integer[] resourceList=
	{
		Integer.valueOf(RawMaterial.RESOURCE_SEAWEED),
		Integer.valueOf(RawMaterial.RESOURCE_FISH),
		Integer.valueOf(RawMaterial.RESOURCE_TUNA),
		Integer.valueOf(RawMaterial.RESOURCE_SHRIMP),
		Integer.valueOf(RawMaterial.RESOURCE_SAND),
		Integer.valueOf(RawMaterial.RESOURCE_CLAY),
		Integer.valueOf(RawMaterial.RESOURCE_PEARL),
		Integer.valueOf(RawMaterial.RESOURCE_LIMESTONE),
		Integer.valueOf(RawMaterial.RESOURCE_SALT)
	};
	public static final List<Integer>	roomResources	= new Vector<Integer>(Arrays.asList(resourceList));

	@Override
	public List<Integer> resourceChoices()
	{
		return roomResources;
	}
}
