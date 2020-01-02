package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class Rodsmithing extends StaffMaking implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "Rodsmithing";
	}

	private final static String	localizedName	= CMLib.lang().L("Rodsmithing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "RODSMITH", "RODSMITHING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "METAL|MITHRIL";
	}

	@Override
	public String parametersFile()
	{
		return "rodsmithing.txt";
	}

	@Override
	protected String getActivePresentTenseVerb()
	{
		return "smithing";
	}

	@Override
	protected String getActiveVerb()
	{
		return "smith";
	}

	@Override
	protected String getTriggerKeyword()
	{
		return this.triggerStrings()[0].toLowerCase();
	}

	@Override
	protected String getSoundFile()
	{
		return "tinktinktink2.wav";
	}

	@Override
	protected String getBaseMaterialType()
	{
		return "Metal";
	}

	private static final int[] rodCodes = new int[]{RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};

	@Override
	protected int[] getBaseMaterialCodes()
	{
		return rodCodes;
	}

}
