package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirComponent.ShipDir;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2021 Bo Zimmerman

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
public class GenShipWindow extends GenShipViewScreen
{
	@Override
	public String ID()
	{
		return "GenShipWindow";
	}

	public GenShipWindow()
	{
		super();
		setName("the viewport");
		setDisplayText("a large clear viewport is set into the hull");
		setDescription("");
		basePhyStats().setSensesMask(CMath.unsetb(basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
		phyStats().setSensesMask(CMath.unsetb(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
	}

	@Override
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.AstroUnit.dm;
	}

	@Override
	public long powerCapacity()
	{
		return 0;
	}

	@Override
	public long powerRemaining()
	{
		return 1;
	}

	@Override
	public boolean activated()
	{
		return true;
	}

	@Override
	public int powerNeeds()
	{
		return 0;
	}

	@Override
	protected boolean requiresPower()
	{
		return false;
	}

	@Override
	protected ShipDir[] getFacingDirs()
	{
		return this.getPermittedDirections();
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenShipWindow))
			return false;
		return super.sameAs(E);
	}
}
