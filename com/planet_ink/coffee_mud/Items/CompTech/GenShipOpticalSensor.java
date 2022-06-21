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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2022 Bo Zimmerman

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
public class GenShipOpticalSensor extends GenElecCompSensor
{
	@Override
	public String ID()
	{
		return "GenShipOpticalSensor";
	}

	public GenShipOpticalSensor()
	{
		super();
		setName("a generic optical ship sensor");
		setDisplayText("a generic optical ship sensor sits here.");
		setDescription("");
		basePhyStats().setSensesMask(CMath.unsetb(basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
		phyStats().setSensesMask(CMath.unsetb(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
	}

	@Override
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.Parsec.dm;
	}

	protected boolean isHiddenFromSensors(final GalacticMap space, final LinkedList<Environmental> revList,
										  final SpaceObject O, final SpaceObject hO,
											final Map<Environmental, Double> visualRadiuses)
	{
		final double[] hDirTo = space.getDirection(O, hO);
		final long hDistance = space.getDistanceFrom(O, hO);
		final BoundedCube hCube=O.getBounds().expand(hDirTo,hDistance);
		for(final Iterator<Environmental> rb=revList.descendingIterator();rb.hasNext();)
		{
			final Environmental bE=rb.next();
			if((bE instanceof SpaceObject)
			&&(bE != hO)
			&&(bE != O))
			{
				final Double hL=visualRadiuses.get(hO);
				final Double bL=visualRadiuses.get(bE);
				if(hL.doubleValue() < bL.doubleValue()) // if moon is smaller than planet, proceed with hide check
				{
					final SpaceObject bO=(SpaceObject)bE;
					if(hCube.intersects(bO.getBounds()))
						return true;
				}
			}
		}
		return false;
	}

	protected Map<Environmental, Double> makeVisualRadiusMap(final GalacticMap space, final SpaceObject O, final LinkedList<Environmental> revList)
	{
		final Map<Environmental, Double> visualRadiuses=new HashMap<Environmental, Double>();
		for(final Environmental E : revList)
		{
			if(E instanceof SpaceObject)
			{
				if(E == O)
					visualRadiuses.put(O,Double.valueOf(0));
				else
				{
					final SpaceObject sO=(SpaceObject)E;
					final double objSize = sO.radius();
					final double distanceDm = space.getDistanceFrom(O, sO);
					final Double viewSize = Double.valueOf(Math.atan(objSize/distanceDm));
					visualRadiuses.put(sO,viewSize);
				}
			}
		}
		return visualRadiuses;
	}

	@Override
	protected List<? extends Environmental> getAllSensibleObjects()
	{
		if(isInSpace())
		{
			final GalacticMap space=CMLib.space();
			final SpaceObject O=space.getSpaceObject(this, true);
			final List<? extends Environmental> objs = super.getAllSensibleObjects();
			final LinkedList<Environmental> revList = new LinkedList<Environmental>();
			revList.addAll(objs);
			final Map<Environmental, Double> visualRadiuses=this.makeVisualRadiusMap(space, O, revList);
			for(final Iterator<Environmental> rh=revList.descendingIterator();rh.hasNext();)
			{
				final Environmental hE=rh.next();
				if((hE instanceof SpaceObject)
				&&(hE != O))
				{
					final SpaceObject hO=(SpaceObject)hE;
					if(isHiddenFromSensors(space, revList, O, hO, visualRadiuses))
						rh.remove();
				}
			}
			objs.retainAll(revList);
			return objs;
		}
		Room R=null;
		final Area A=CMLib.map().areaLocation(this);
		if(A instanceof Boardable)
		{
			final Boardable shipO = (Boardable)A;
			final Room dockR = shipO.getIsDocked();
			if(dockR != null)
				R=dockR;
			else
			{
				final Item shipI=shipO.getBoardableItem();
				if(shipI != null)
					R=CMLib.map().roomLocation(shipI);
			}
		}
		else
			R=CMLib.map().roomLocation(this);
		if(R==null)
			return empty;
		final List<Environmental> found = new LinkedList<Environmental>();
		found.add(R);
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(CMLib.flags().isSeeable(M))
				found.add(M);
		}
		for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if(CMLib.flags().isSeeable(I)
			&&(I.displayText().length()>0))
				found.add(I);
		}
		return found;
	}

	@Override
	protected boolean canPassivelySense(final CMMsg msg)
	{
		if(!super.canPassivelySense(msg))
			return false;
		final SpaceObject O = CMLib.space().getSpaceObject(this, true);
		if((O!=null)
		&&(msg.target()==O))
			return true;
		if(!msg.isOthers(CMMsg.MASK_EYES))
			return false;
		if((!(msg.target() instanceof SpaceObject))
		||(!isInSpace()))
			return true;
		final SpaceObject hO = (SpaceObject)msg.target();
		final List<? extends Environmental> objs = super.getAllSensibleObjects();
		if(!objs.contains(hO))
			return false; // covers range and filters!
		final GalacticMap space=CMLib.space();
		final LinkedList<Environmental> revList = new LinkedList<Environmental>();
		revList.addAll(objs);
		final Map<Environmental, Double> visualRadiuses=makeVisualRadiusMap(space, O, revList);
		if(isHiddenFromSensors(space, revList, O, hO, visualRadiuses))
			return false;
		return true;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenShipOpticalSensor))
			return false;
		final String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
		{
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		}
		return true;
	}

}
