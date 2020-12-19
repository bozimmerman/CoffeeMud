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
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class GenShipWindow extends GenElecCompSensor
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

	protected ShipDir validOpenDir = ShipDir.FORWARD;

	@Override
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.Parsec.dm;
	}

	@Override
	public long powerCapacity()
	{
		return 1;
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
	public void setSecretIdentity(final String newIdentity)
	{
		super.setSecretIdentity(newIdentity);
		this.validOpenDir = ShipDir.FORWARD;
		if(this.secretIdentity!=null)
		{
			final ShipDir pdir = (ShipDir)CMath.s_valueOf(ShipDir.class, secretIdentity.toUpperCase().trim());
			if(pdir != null)
				this.validOpenDir = pdir;
		}
	}

	protected Room getLookAtRoom()
	{
		Room R=null;
		final Area A=CMLib.map().areaLocation(this);
		if(A instanceof BoardableShip)
		{
			final BoardableShip shipO = (BoardableShip)A;
			final Room dockR = shipO.getIsDocked();
			if(dockR != null)
				R=dockR;
			else
			{
				final Item shipI=shipO.getShipItem();
				if(shipI != null)
					R=CMLib.map().roomLocation(shipI);
			}
		}
		else
			R=CMLib.map().roomLocation(this);
		return R;
	}

	protected boolean isInSpace()
	{
		final SpaceObject O=CMLib.map().getSpaceObject(this, true);
		if(O != null)//&&(this.powerRemaining() > this.powerNeeds()))
			return CMLib.map().isObjectInSpace(O);
		return false;
	}

	protected List<? extends Environmental> getAllSensibleObjects()
	{
		if(isInSpace())
			return super.getAllSensibleObjects();
		Room R=null;
		final Area A=CMLib.map().areaLocation(this);
		if(A instanceof BoardableShip)
		{
			final BoardableShip shipO = (BoardableShip)A;
			final Room dockR = shipO.getIsDocked();
			if(dockR != null)
				R=dockR;
			else
			{
				final Item shipI=shipO.getShipItem();
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
	protected Filterer<Environmental> getSensedObjectFilter()
	{
		return new Filterer<Environmental>()
		{
			final SpaceObject spaceMe = CMLib.map().getSpaceObject(me, true);

			@Override
			public boolean passesFilter(final Environmental obj)
			{
				if((!(spaceMe instanceof SpaceShip))||(me == obj)||(spaceMe == obj))
					return false;
				if(obj instanceof SpaceObject)
				{
					final SpaceShip ship=(SpaceShip)spaceMe;
					final SpaceObject sobj = (SpaceObject)obj;
					final double[] proposedDirection=CMLib.map().getDirection(ship, sobj);
					final ShipDir dir = CMLib.map().getDirectionFromDir(ship.facing(), ship.roll(), proposedDirection);
					if (dir == validOpenDir)
					{
						final double distanceDm = CMLib.map().getDistanceFrom(spaceMe.coordinates(), sobj.coordinates());
						final double objSize = sobj.radius();
						final double viewSize = Math.atan(objSize/distanceDm);
						return viewSize >= 0.0015;
					}
					return false;
				}
				return true;
			}
		};
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==this)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			if(!isInSpace())
			{
				final Room R=getLookAtRoom();
				final CMMsg msg2=CMClass.getMsg(msg.source(), R, msg.tool(), msg.targetCode(), null);
				CMLib.commands().handleBeingLookedAt(msg2);
				return;
			}
			final Converter<Environmental, Environmental> converter = this.getSensedObjectConverter();
			final List<Environmental> finalList = new LinkedList<Environmental>();
			final List<? extends Environmental> found= getSensedObjects();
			for(final Environmental E : found)
			{
				final Environmental E2=converter.convert(E);
				if(E2!=null)
					finalList.add(E2);
			}
			if(finalList.size()==0)
				setDescription(L("You see the the blackness of space."));
			else
			{
				final StringBuilder desc=new StringBuilder(L("^WYou see: %0D"));
				desc.append(
					CMLib.lister().lister(msg.source(), finalList, false, null, null, msg.targetMinor()==CMMsg.TYP_EXAMINE, false)
				);
				setDescription(desc.toString());
			}
		}
		super.executeMsg(myHost, msg);
	}
	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenShipWindow))
			return false;
		return super.sameAs(E);
	}
}
