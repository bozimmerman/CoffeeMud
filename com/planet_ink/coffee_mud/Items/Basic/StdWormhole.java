package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip.ShipFlag;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2022 Bo Zimmerman

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
public class StdWormhole extends StdSpaceBody implements SpaceObject.SpaceGateway
{
	@Override
	public String ID()
	{
		return "StdWormhole";
	}

	private ShipDir[]	allPossDirs		= new ShipDir[] { ShipDir.FORWARD, ShipDir.AFT, ShipDir.DORSEL, ShipDir.VENTRAL, ShipDir.PORT, ShipDir.STARBOARD };
	private int			numPermitDirs	= allPossDirs.length;

	public StdWormhole()
	{
		super();
		setName("a wormhole");
		final Random random=new Random(System.currentTimeMillis());
		radius=SpaceObject.Distance.PlanetRadius.dm + (random.nextLong() % (SpaceObject.Distance.PlanetRadius.dm / 2));
		basePhyStats().setWeight(99999999);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STONE);
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a wormhole");
	}

	@Override
	public void setPermittedDirections(final ShipDir[] newPossDirs)
	{
		this.allPossDirs = newPossDirs;
	}

	@Override
	public ShipDir[] getPermittedDirections()
	{
		return allPossDirs;
	}

	@Override
	public void setPermittedNumDirections(final int numDirs)
	{
		this.numPermitDirs = numDirs;
	}

	@Override
	public int getPermittedNumDirections()
	{
		return numPermitDirs;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if( msg.amITarget(this)
		&& (msg.targetMinor() == CMMsg.TYP_COLLISION)
		&& (!amDestroyed())
		&& (msg.tool() instanceof SpaceObject)
		&& (knownTarget() != null)
		&& (knownTarget() != this))
		{
			final SpaceObject ship=(SpaceObject)msg.tool();
			final SpaceObject sobj = this;
			final double[] shipDir=CMLib.space().getDirection(ship, sobj);
			final ShipDir dir = CMLib.space().getAbsoluteDirectionalFromDir(shipDir);
			if (CMParms.contains(this.getPermittedDirections(), dir))
			{
				long distance = CMLib.space().getDistanceFrom(ship, sobj);
				if(distance < ship.radius() + knownTarget().radius() + 2)
					distance=ship.radius() + knownTarget().radius() + 2;
				final long[] newCoords = CMLib.space().moveSpaceObject(knownTarget().coordinates(), ship.direction(), distance);
				ship.setCoords(newCoords);
			}
		}
	}
}
