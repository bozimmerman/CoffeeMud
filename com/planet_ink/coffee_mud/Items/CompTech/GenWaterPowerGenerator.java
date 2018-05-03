package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class GenWaterPowerGenerator extends GenFuellessGenerator
{
	@Override
	public String ID()
	{
		return "GenWaterPowerGenerator";
	}

	public GenWaterPowerGenerator()
	{
		super();
		setName("a water power generator");
		setDisplayText("a water power generator sits here.");
		setDescription("");
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SWIMMING);
		phyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SWIMMING);
	}

	@Override
	protected boolean canGenerateRightNow()
	{
		if(activated())
		{
			final Room R=CMLib.map().roomLocation(this);
			if((R!=null)&&(CMLib.flags().isWaterySurfaceRoom(R)))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWaterPowerGenerator))
			return false;
		return super.sameAs(E);
	}
}
