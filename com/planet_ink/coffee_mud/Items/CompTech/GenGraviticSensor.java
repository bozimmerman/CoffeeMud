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
import com.planet_ink.coffee_mud.Libraries.interfaces.GalacticMap;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2025 Bo Zimmerman

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
public class GenGraviticSensor extends GenElecCompSensor
{
	@Override
	public String ID()
	{
		return "GenGraviticSensor";
	}

	public GenGraviticSensor()
	{
		super();
		setName("a gravitic sensor");
		setDisplayText("a gravitic sensor sits here.");
		setDescription("");
	}

	@Override
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.Parsec.dm;
	}

	protected String getGenericGraviticName(final Environmental E)
	{
		if(E instanceof SpaceObject)
		{
			final SpaceObject SO = (SpaceObject)E;
			final long radius = SO.radius();
			if(radius >= SpaceObject.Distance.StarBRadius.dm/2)
				return L("a super star-sized object");
			else
			if(radius >= SpaceObject.Distance.StarDRadius.dm)
				return L("a star-sized object");
			else
			if(radius >= SpaceObject.Distance.SaturnRadius.dm)
				return L("an enormous planet-sized object");
			else
			if(radius >= SpaceObject.Distance.SaturnRadius.dm/2)
				return L("a huge planet-sized object");
			else
			if(radius >= SpaceObject.Distance.PlanetRadius.dm*2)
				return L("an large planet-sized object");
			else
			if(radius >= SpaceObject.Distance.PlanetRadius.dm/2)
				return L("a planet-sized object");
			else
			if(radius >= SpaceObject.Distance.MoonRadius.dm/2)
				return L("a moon-sized object");
			else
			if(radius >= SpaceObject.Distance.AsteroidRadius.dm)
				return L("a moonlet-sized object");
			else
			if(radius >= SpaceObject.Distance.AsteroidRadius.dm/10)
				return L("an asteroid-sized object");
			else
				return L("an object");
		}
		return L("an object");
	}

	protected Converter<Environmental, Environmental> gravConverter = new Converter<Environmental, Environmental>()
	{
		@Override
		public Environmental convert(final Environmental obj)
		{
			return new SpaceObject.SensedSpaceObject()
			{
				final String	name		= getGenericGraviticName(obj);
				final String	displayText	= "";
				final String	description	= "";

				@Override
				public String ID()
				{
					return ""+obj;
				}

				@Override
				public String Name()
				{
					return obj.Name(); // the real, secret name.. do not show!
				}

				@Override
				public String name()
				{
					return name;
				}

				@Override
				public void setName(final String newName)
				{
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
				public Environmental get()
				{
					return obj;
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
					return "";
				}

				@Override
				public String rawImage()
				{
					return "";
				}

				@Override
				public void setImage(final String newImage)
				{
				}

				@Override
				public boolean isGeneric()
				{
					return false;
				}

				@Override
				public void setMiscText(final String newMiscText)
				{
				}

				@Override
				public String text()
				{
					return "";
				}

				@Override
				public String miscTextFormat()
				{
					return null;
				}

				@Override
				public boolean sameAs(Environmental E)
				{
					if(E==this)
						return true;
					if(E==null)
						return false;
					if(E instanceof SensedEnvironmental)
						E=((SensedEnvironmental)E).get();
					return E==obj || E.sameAs(obj);
				}

				@Override
				public long expirationDate()
				{
					return 0;
				}

				@Override
				public void setExpirationDate(final long dateTime)
				{
				}

				@Override
				public int maxRange()
				{
					return 0;
				}

				@Override
				public int minRange()
				{
					return 0;
				}

				@Override
				public String L(final String str, final String... xs)
				{
					return str;
				}

				@Override
				public int getTickStatus()
				{
					return 0;
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
					return false;
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
					return new String[0];
				}

				@Override
				public int getSaveStatIndex()
				{
					return 0;
				}

				@Override
				public String getStat(final String code)
				{
					return "";
				}

				@Override
				public boolean isStat(final String code)
				{
					return false;
				}

				@Override
				public void setStat(final String code, final String val)
				{
				}

				@Override
				public BoundedCube getCube()
				{
					if(obj instanceof SpaceObject)
						return ((SpaceObject)obj).getCube();
					return smallCube;
				}

				@Override
				public BoundedSphere getSphere()
				{
					if(obj instanceof SpaceObject)
						return ((SpaceObject)obj).getSphere();
					return smallSphere;
				}

				@Override
				public Coord3D coordinates()
				{
					final SpaceObject sobj =CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.coordinates().copyOf();
					return emptyCoords;
				}

				@Override
				public void setCoords(final Coord3D coords)
				{
				}

				@Override
				public long radius()
				{
					final SpaceObject sobj =CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.radius();
					return 0;
				}

				@Override
				public Coord3D center()
				{
					return coordinates();
				}

				@Override
				public void setRadius(final long radius)
				{
				}

				@Override
				public Dir3D direction()
				{
					return new Dir3D();
				}

				@Override
				public void setDirection(final Dir3D dir)
				{
				}

				@Override
				public double speed()
				{
					return 0;
				}

				@Override
				public void setSpeed(final double v)
				{
				}

				@Override
				public SpaceObject knownTarget()
				{
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
						return sobj;
					return null;
				}

				@Override
				public void setKnownSource(final SpaceObject O)
				{
				}

				@Override
				public long getMass()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.getMass();
					return 1;
				}
			};
		}
	};

	@Override
	protected Converter<Environmental, Environmental> getSensedObjectConverter()
	{
		return gravConverter;
	}

	protected static SpaceObject star = null;

	protected long getMassDetected()
	{
		if(star == null)
		{
			star = (SpaceObject)CMClass.getBasicItem("Star");
		}
		return star.getMass();
	}

	@Override
	protected Filterer<Environmental> getSensedObjectFilter()
	{
		return new Filterer<Environmental>()
		{
			final SpaceObject spaceMe = CMLib.space().getSpaceObject(me, true);

			@Override
			public boolean passesFilter(final Environmental obj)
			{
				if((spaceMe == null)||(me == obj)||(spaceMe == obj))
					return false;
				if(!(obj instanceof SpaceObject))
					return false;
				final SpaceObject sobj = (SpaceObject)obj;
				final long distance = CMLib.space().getDistanceFrom(spaceMe.coordinates(), sobj.coordinates());
				final long adjustedMax = Math.round(sobj.getMass() * (1.0 - CMath.div(distance, getSensorMaxRange())));
				// tiny objects are not detected, nor ships at great distance, nor things inside us
				return (adjustedMax > 100) && (distance > sobj.radius() + spaceMe.radius());
			}
		};
	}

	protected boolean isHiddenFromSensors(final GalacticMap space, final LinkedList<Environmental> revList,
										  final SpaceObject O, final SpaceObject hO)
	{
		final Dir3D hDirTo = space.getDirection(O, hO);
		final long hDistance = space.getDistanceFrom(O, hO);
		final BoundedTube hTube=O.getSphere().expand(hDirTo,hDistance);
		for(final Iterator<Environmental> rb=revList.descendingIterator();rb.hasNext();)
		{
			final Environmental bE=rb.next();
			if((bE instanceof SpaceObject)
			&&(bE != hO)
			&&(bE != O))
			{
				final SpaceObject bO=(SpaceObject)bE;
				final long hL=hO.getMass();
				final long bL=bO.getMass();
				if(hL < bL) // if moon is lighter than then planet, proceed with hide check
				{
					if(hTube.intersects(bO.getSphere()))
					{
						// the projection from the ship to prospect hidden object, which we know
						// appears lighter than the tested bO object, is also blocked BY
						// the bO object.  Therefore, the prospect object IS hidden
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected List<? extends Environmental> getAllSensibleObjects()
	{
		if(!isInSpace())
		{
			final SpaceObject O=CMLib.space().getSpaceObject(this, true);
			if(O instanceof SpaceShip)
			{
				final SpaceShip ship=(SpaceShip)O;
				if(ship.getIsDocked() !=null)
				{
					final SpaceObject shipO=CMLib.space().getSpaceObject(ship.getIsDocked(), true);
					if(shipO != null)
						return new XVector<Environmental>(shipO);
				}
			}
			if(O !=null)
				return new XVector<Environmental>(O);
			else
				return new XVector<Environmental>();
		}
		final GalacticMap space=CMLib.space();
		final SpaceObject O=space.getSpaceObject(this, true);
		final List<? extends Environmental> objs = super.getAllSensibleObjects();
		final LinkedList<Environmental> revList = new LinkedList<Environmental>();
		revList.addAll(objs);
		for(final Iterator<Environmental> rh=revList.descendingIterator();rh.hasNext();)
		{
			final Environmental hE=rh.next();
			if((hE instanceof SpaceObject)
			&&(hE != O))
			{
				final SpaceObject hO=(SpaceObject)hE;
				if(isHiddenFromSensors(space, revList, O, hO))
					rh.remove();
			}
		}
		objs.retainAll(revList);
		return objs;
	}

	@Override
	protected boolean canPassivelySense(final CMMsg msg)
	{
		if(!super.canPassivelySense(msg))
			return false;
		final GalacticMap space=CMLib.space();
		final SpaceObject O = space.getSpaceObject(this, true);
		if(O==null)
			return false;
		if((O!=null)
		&&(msg.target()==O))
			return true;
		if(!msg.isOthers(CMMsg.MASK_MOVE))
			return false;
		if((!(msg.target() instanceof SpaceObject))
		||(!isInSpace()))
			return true;
		final SpaceObject hO = (SpaceObject)msg.target();
		final long hDistance = space.getDistanceFrom(O, hO);
		if(hDistance > getSensorMaxRange())
			return false;
		final Filterer<Environmental> filter = this.getSensedObjectFilter();
		if(!filter.passesFilter(hO))
			return false;
		final Dir3D hDirTo = space.getDirection(O, hO);
		final BoundedTube hTube=O.getSphere().expand(hDirTo,hDistance);
		final List<SpaceObject> objs = space.getSpaceObjectsInBound(hTube.getCube());
		for(final SpaceObject cO : objs)
		{
			if((cO != O)
			&& (cO != hO)
			&&(hTube.intersects(cO.getSphere())))
			{
				if(cO.getMass() >= hO.getMass()) // an object is hiding the target object
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenGraviticSensor))
			return false;
		return super.sameAs(E);
	}
}
