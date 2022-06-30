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

	protected String getGenericOpticalName(final Environmental E)
	{
		if(E instanceof Physical)
			return ((Physical)E).genericName();
		return E.name();
	}

	protected final Converter<Environmental, Environmental> opticalConverter = new Converter<Environmental, Environmental>()
	{
		@Override
		public Environmental convert(final Environmental obj)
		{
			final Environmental me=obj;
			return new SpaceObject()
			{
				final Environmental	obj			= me;
				protected String	name		= getGenericOpticalName(obj);
				final String		displayText	= "";
				final String		description	= "";

				@Override
				public String ID()
				{
					return ""+obj;
				}

				@Override
				public String Name()
				{
					return name;
				}

				@Override
				public void setName(final String newName)
				{
					name = newName;
				}

				@Override
				public String displayText()
				{
					return displayText;
				}

				@Override
				public void setDisplayText(final String newDisplayText)
				{
				}

				@Override
				public String description()
				{
					return description;
				}

				@Override
				public void setDescription(final String newDescription)
				{
				}

				@Override
				public String image()
				{
					return obj.image();
				}

				@Override
				public String rawImage()
				{
					return obj.rawImage();
				}

				@Override
				public void setImage(final String newImage)
				{
				}

				@Override
				public boolean isGeneric()
				{
					return obj.isGeneric();
				}

				@Override
				public void setMiscText(final String newMiscText)
				{
				}

				@Override
				public String text()
				{
					return obj.text();
				}

				@Override
				public String miscTextFormat()
				{
					return obj.miscTextFormat();
				}

				@Override
				public boolean sameAs(final Environmental E)
				{
					return E.ID().equals(ID()) || (ID().equals(""+E)) || obj.sameAs(E);
				}

				@Override
				public long expirationDate()
				{
					return obj.expirationDate();
				}

				@Override
				public void setExpirationDate(final long dateTime)
				{
				}

				@Override
				public int maxRange()
				{
					return obj.maxRange();
				}

				@Override
				public int minRange()
				{
					return obj.minRange();
				}

				@Override
				public String L(final String str, final String... xs)
				{
					return obj.L(str, xs);
				}

				@Override
				public String name()
				{
					return Name();
				}

				@Override
				public int getTickStatus()
				{
					return obj.getTickStatus();
				}

				@Override
				public boolean tick(final Tickable ticking, final int tickID)
				{
					return false;
				}

				@Override
				public CMObject newInstance()
				{
					return obj.newInstance();
				}

				@Override
				public CMObject copyOf()
				{
					return obj.copyOf();
				}

				@Override
				public void initializeClass()
				{
				}

				@Override
				public int compareTo(final CMObject o)
				{
					if((o != null)
					&&((o.ID().equals(ID())||(ID().equals(""+o)))))
						return 0;
					return obj.compareTo(o);
				}

				@Override
				public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
				{
				}

				@Override
				public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
				{
				}

				@Override
				public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
				{
				}

				@Override
				public void executeMsg(final Environmental myHost, final CMMsg msg)
				{
				}

				@Override
				public boolean okMessage(final Environmental myHost, final CMMsg msg)
				{
					return true;
				}

				@Override
				public void destroy()
				{
					// Nope!
				}

				@Override
				public boolean isSavable()
				{
					return obj.isSavable();
				}

				@Override
				public boolean amDestroyed()
				{
					return obj.amDestroyed();
				}

				@Override
				public void setSavable(final boolean truefalse)
				{
				}

				@Override
				public String[] getStatCodes()
				{
					return obj.getStatCodes();
				}

				@Override
				public int getSaveStatIndex()
				{
					return obj.getSaveStatIndex();
				}

				@Override
				public String getStat(final String code)
				{
					return obj.getStat(code);
				}

				@Override
				public boolean isStat(final String code)
				{
					return obj.isStat(code);
				}

				@Override
				public void setStat(final String code, final String val)
				{
				}

				@Override
				public BoundedCube getBounds()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.getBounds();
					return smallCube;
				}

				@Override
				public long[] coordinates()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return Arrays.copyOf(sobj.coordinates(), sobj.coordinates().length);
					return Arrays.copyOf(emptyCoords, emptyCoords.length);
				}

				@Override
				public void setCoords(final long[] coords)
				{
				}

				@Override
				public long radius()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.radius();
					return 1;
				}

				@Override
				public void setRadius(final long radius)
				{
				}

				@Override
				public double[] direction()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.direction();
					return emptyDirection;
				}

				@Override
				public void setDirection(final double[] dir)
				{
				}

				@Override
				public double speed()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.speed();
					return 0;
				}

				@Override
				public void setSpeed(final double v)
				{
				}

				@Override
				public SpaceObject knownTarget()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.knownTarget();
					return null;
				}

				@Override
				public void setKnownTarget(final SpaceObject O)
				{
				}

				@Override
				public SpaceObject knownSource()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.knownSource();
					return null;
				}

				@Override
				public void setKnownSource(final SpaceObject O)
				{
				}

				@Override
				public long getMass()
				{
					return 0;
				}
			};
		}
	};

	@Override
	protected Converter<Environmental, Environmental> getSensedObjectConverter()
	{
		return opticalConverter;
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
			&&((!(bE instanceof Physical))||(CMLib.flags().isSeeable((Physical)bE)))
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
		final GalacticMap space = CMLib.space();
		final SpaceObject O = space.getSpaceObject(this, true);
		if(O==null)
			return false;
		if((O!=null)
		&&(msg.target()==O))
			return true;
		if((!msg.isOthers(CMMsg.MASK_EYES))
		&&((!(msg.target() instanceof Physical))||(!CMLib.flags().isSeeable((Physical)msg.target()))))
			return false;
		if((!(msg.target() instanceof SpaceObject))
		||(!isInSpace()))
			return true;
		final SpaceObject hO = (SpaceObject)msg.target();
		final long hDistance = space.getDistanceFrom(O, hO);
		if(hDistance > this.getSensorMaxRange())
			return false;
		final Filterer<Environmental> filter = this.getSensedObjectFilter();
		if(!filter.passesFilter(hO))
			return false;
		final double[] hDirTo = space.getDirection(O, hO);
		final BoundedCube hCube=O.getBounds().expand(hDirTo,hDistance);
		final List<SpaceObject> objs = space.getSpaceObjectsInBound(hCube);
		final double vO = Math.atan(hO.radius()/hDistance);
		for(final SpaceObject cO : objs)
		{
			if((cO != O)
			&& (cO != hO)
			&&(hCube.intersects(cO.getBounds())))
			{
				final long cDistance = space.getDistanceFrom(O, cO);
				final double vC = Math.atan(cO.radius()/cDistance);
				if(vC >= vO) // an object is hiding the target object
					return false;
			}
		}
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
