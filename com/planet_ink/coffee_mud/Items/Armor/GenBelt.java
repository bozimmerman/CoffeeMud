package com.planet_ink.coffee_mud.Items.Armor;
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

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class GenBelt extends GenArmor
{
	@Override
	public String ID()
	{
		return "GenBelt";
	}

	public GenBelt()
	{
		super();

		setName("a knitted weapon belt");
		setDisplayText("a knitted weapon belt is crumpled up here.");
		setDescription("a belt knitted from tough cloth with a simple sheath built in.");
		properWornBitmap=Wearable.WORN_WAIST;
		wornLogicalAnd=false;
		basePhyStats().setArmor(0);
		basePhyStats().setWeight(1);
		setCapacity(20);
		basePhyStats().setAbility(0);
		setContainTypes(Container.CONTAIN_DAGGERS|Container.CONTAIN_ONEHANDWEAPONS|Container.CONTAIN_SWORDS|Container.CONTAIN_OTHERWEAPONS);
		baseGoldValue=1;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_COTTON;
	}
}
