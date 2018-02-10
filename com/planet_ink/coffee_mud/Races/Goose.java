package com.planet_ink.coffee_mud.Races;
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
   Copyright 2013-2018 Bo Zimmerman

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
public class Goose extends WaterFowl
{
	@Override
	public String ID()
	{
		return "Duck";
	}

	private final static String localizedStaticName = CMLib.lang().L("Duck");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	private final String[]	racialAbilityNames			= CMParms.combine(super.racialAbilityNames(), new String[] { "GooseSpeak" });
	private final int[]		racialAbilityLevels			= CMParms.combine(super.racialAbilityLevels(), new int[] { 1 });
	private final int[]		racialAbilityProficiencies	= CMParms.combine(super.racialAbilityProficiencies(), new int[] { 100 });
	private final boolean[]	racialAbilityQuals			= CMParms.combine(super.racialAbilityQuals(), new boolean[] { false });
	private final String[]	racialAbilityParms			= CMParms.combine(super.racialAbilityParms(), new String[] { "" });

	@Override
	protected String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	protected int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	protected int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	protected boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return "gosling";
			case Race.AGE_YOUNGADULT:
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
				switch(gender)
				{
				case 'M':
				case 'm':
					return "gander";
				case 'F':
				case 'f':
					return "goose";
				default:
					return name().toLowerCase();
				}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
				switch(gender)
				{
				case 'M':
				case 'm':
					return "old gander";
				case 'F':
				case 'f':
					return "old goose";
				default:
					return "old " + name().toLowerCase();
				}
		}
	}
	
	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("some webbed @x1 feet",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				resources.addElement(makeResource
				(L("some @x1 feathers",name().toLowerCase()),RawMaterial.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				(L("some @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_POULTRY));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
