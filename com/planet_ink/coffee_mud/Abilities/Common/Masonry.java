package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Building;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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

public class Masonry extends BuildingSkill
{
	@Override
	public String ID()
	{
		return "Masonry";
	}

	private final static String	localizedName	= CMLib.lang().L("Masonry");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MASONRY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "ROCK|STONE";
	}

	@Override
	public String parametersFile()
	{
		return "masonry.txt";
	}

	@Override
	protected String getMainResourceName()
	{
		return "Stone";
	}

	@Override
	protected String getSoundName()
	{
		return "stone.wav";
	}

	public Masonry()
	{
		super();
	}

	@Override
	protected int[][] getBasicMaterials(final MOB mob, int woodRequired, String miscType)
	{
		final int[] req1;
		if(miscType.length()==0)
		{
			req1=new int[]
			{
				RawMaterial.RESOURCE_STONE,
				RawMaterial.RESOURCE_LIMESTONE,
				RawMaterial.RESOURCE_FLINT,
				RawMaterial.RESOURCE_GRANITE,
				RawMaterial.RESOURCE_OBSIDIAN,
				RawMaterial.RESOURCE_MARBLE
			};
		}
		else
			req1=null;
		final int[][] idata=fetchFoundResourceData(mob,
													woodRequired,miscType,req1,
													0,null,null,
													false,
													0,null);
		return idata;
	}
}
