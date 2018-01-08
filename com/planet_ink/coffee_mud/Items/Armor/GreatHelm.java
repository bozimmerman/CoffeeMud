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
   Copyright 2001-2018 Bo Zimmerman

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
public class GreatHelm extends StdArmor
{
	@Override
	public String ID()
	{
		return "GreatHelm";
	}

	public GreatHelm()
	{
		super();

		setName("a steel Great Helm.");
		setDisplayText("a steel great helm sits here.");
		setDescription("This is a steel helmet that completely encloses the head.");
		properWornBitmap=Wearable.WORN_HEAD;
		wornLogicalAnd=false;
		basePhyStats().setArmor(18);
		basePhyStats().setWeight(10);
		basePhyStats().setAbility(0);
		baseGoldValue=60;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_STEEL;
	}

}
