package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2005-2018 Bo Zimmerman

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
public class Prop_RoomUnmappable extends Property
{
	@Override
	public String ID()
	{
		return "Prop_RoomUnmappable";
	}

	@Override
	public String name()
	{
		return "Unmappable Room/Area";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	private int bitStream=PhyStats.SENSE_ROOMUNMAPPABLE;

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		bitStream=0;
		if(!CMParms.parse(newText.toUpperCase().trim()).contains("MAPOK"))
			bitStream=PhyStats.SENSE_ROOMUNMAPPABLE;
		if(CMParms.parse(newText.toUpperCase().trim()).contains("NOEXPLORE"))
			bitStream=bitStream|PhyStats.SENSE_ROOMUNEXPLORABLE;
	}

	@Override
	public String accountForYourself()
	{
		return "Unmappable";
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setSensesMask(affectableStats.sensesMask()|bitStream);
	}
}
