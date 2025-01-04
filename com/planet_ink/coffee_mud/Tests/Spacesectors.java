package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
public class Spacesectors extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacesectors";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final Coord3D coordinates = new Coord3D();
		for(long x = -SpaceObject.Distance.GalaxyRadius.dm; x<= SpaceObject.Distance.GalaxyRadius.dm; x+= (SpaceObject.Distance.GalaxyRadius.dm / 88))
		{
			coordinates.x(x);
			final Coord3D in = CMLib.space().getInSectorCoords(coordinates);
			mob.tell(CMLib.space().getSectorName(coordinates) + ": "+in.x()+","+in.y()+","+in.z());
		}
		for(long x = -SpaceObject.Distance.GalaxyRadius.dm; x<= SpaceObject.Distance.GalaxyRadius.dm; x+= (SpaceObject.Distance.GalaxyRadius.dm / 88))
		{
			coordinates.y(x);
			final Coord3D in = CMLib.space().getInSectorCoords(coordinates);
			mob.tell(CMLib.space().getSectorName(coordinates) + ": "+in.x()+","+in.y()+","+in.z());
		}
		for(long x = -SpaceObject.Distance.GalaxyRadius.dm; x<= SpaceObject.Distance.GalaxyRadius.dm; x+= (SpaceObject.Distance.GalaxyRadius.dm / 88))
		{
			coordinates.z(x);
			final Coord3D in = CMLib.space().getInSectorCoords(coordinates);
			mob.tell(CMLib.space().getSectorName(coordinates) + ": "+in.x()+","+in.y()+","+in.z());
		}
		return null;
	}
}
