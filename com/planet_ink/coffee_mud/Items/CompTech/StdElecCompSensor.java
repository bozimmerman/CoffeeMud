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
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2020 Bo Zimmerman

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
public class StdElecCompSensor extends StdElecCompItem implements TechComponent
{
	@Override
	public String ID()
	{
		return "StdElecCompSensor";
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_SENSOR;
	}

	protected final static long[] emptyCoords = new long[] {0,0,0};
	protected final static double[] emptyDirection = new double[] {0,0};
	protected final static BoundedCube smallCube = new BoundedCube(1,1,1,1,1,1);
	protected Set<Environmental> lastSensedObjects = new TreeSet<Environmental>(XTreeSet.comparator);

	private static final Filterer<Environmental> acceptEverythingFilter = new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return true;
		}
	};

	private static final Converter<Environmental, Environmental> directConverter = new Converter<Environmental, Environmental>()
	{
		@Override
		public Environmental convert(final Environmental obj)
		{
			final Environmental me=obj;
			return new SpaceObject()
			{
				final Environmental obj = me;

				@Override
				public String ID()
				{
					return ""+obj;
				}

				@Override
				public String Name()
				{
					return obj.Name();
				}

				@Override
				public void setName(final String newName)
				{
				}

				@Override
				public String displayText()
				{
					return obj.displayText();
				}

				@Override
				public void setDisplayText(final String newDisplayText)
				{
				}

				@Override
				public String description()
				{
					return obj.description();
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
					return obj.name();
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.getBounds();
					return smallCube;
				}

				@Override
				public long[] coordinates()
				{
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
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
					final SpaceObject sobj=CMLib.map().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.getMass();
					return 1;
				}
			};
		}
	};

	/**
	 * The maximum range of objects that this sensor can detect
	 * @return maximum range of objects that this sensor can detect
	 */
	protected long getSensorMaxRange()
	{
		return SpaceObject.Distance.Parsec.dm;
	}

	/**
	 * Filter to pick out which objects this sensor can actually pick up.
	 * @see com.planet_ink.coffee_mud.core.collections.Filterer
	 * @return a Filterer to pick out which objects this sensor can actually pick up.
	 */
	protected Filterer<Environmental> getSensedObjectFilter()
	{
		return acceptEverythingFilter;
	}

	/**
	 * Converter to convert from the actual sensed object, to a CMObject, which may
	 * or may not contain all the information of the actual one.
	 * @see com.planet_ink.coffee_mud.core.collections.Converter
	 * @return  to convert from the actual sensed object, to a CMObject
	 */
	protected Converter<Environmental, Environmental> getSensedObjectConverter()
	{
		return directConverter;
	}

	protected static final List<? extends Environmental> empty=new ReadOnlyVector<Environmental>();

	protected List<? extends Environmental> getAllSensibleObjects()
	{
		final SpaceObject O=CMLib.map().getSpaceObject(this, true);
		if((O!=null)&&(this.powerRemaining() > this.powerNeeds()))
		{
			final long maxRange = Math.round(getSensorMaxRange() * this.getComputedEfficiency());
			final List<? extends Environmental> found = CMLib.map().getSpaceObjectsWithin(O, O.radius()+1, maxRange);
			found.remove(O);
			if(found.size() > 1)
			{
				if(CMLib.dice().rollPercentage() > (100*this.getFinalManufacturer().getReliabilityPct()))
				{
					//TODO: better to filter out the most distant!
					int num = found.size() / 10; // failing reliability check always loses 10% of found things
					if(num <= 0)
						num = 1;
					for(int i=0;i<num && (found.size() > 0);i++)
					{
						found.remove(CMLib.dice().roll(1, found.size(), -1));
					}
				}
			}
			return found;
		}
		return empty;
	}

	protected List<? extends Environmental> getSensedObjects()
	{
		final List<? extends Environmental> found= getAllSensibleObjects();
		if(found.size() > 0)
		{
			final Filterer<Environmental> filter = this.getSensedObjectFilter();
			for(final Iterator<? extends Environmental> o = found.iterator();o.hasNext();)
			{
				final Environmental obj=o.next();
				if(!filter.passesFilter(obj))
					o.remove();
			}
		}
		return found;
	}

	protected boolean doSensing(final MOB mob, final Software controlI)
	{
		final List<? extends Environmental> found= getSensedObjects();
		final Converter<Environmental, Environmental> converter = this.getSensedObjectConverter();
		for(final Environmental obj : found)
		{
			synchronized(lastSensedObjects)
			{
				if(!lastSensedObjects.contains(obj))
					lastSensedObjects.add(obj);
			}
			final Environmental sensedObject = converter.convert(obj);
			final String code=Technical.TechCommand.SENSE.makeCommand(this,Boolean.TRUE);
			final CMMsg msg=CMClass.getMsg(mob, controlI, sensedObject, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			if(controlI.owner() instanceof Room)
			{
				if(((Room)controlI.owner()).okMessage(mob, msg))
					((Room)controlI.owner()).send(mob, msg);
			}
			else
			if(controlI.okMessage(mob, msg))
				controlI.executeMsg(mob, msg);
		}
		final Set<Environmental> prevSensedObjects = new TreeSet<Environmental>(XTreeSet.comparator);
		synchronized(lastSensedObjects)
		{
			for(final Iterator<Environmental> i=lastSensedObjects.iterator();i.hasNext();)
			{
				final Environmental nobj = i.next();
				if(!found.contains(nobj))
				{
					i.remove();
					prevSensedObjects.add(nobj);
				}
			}
		}
		for(final Environmental sensedObject : prevSensedObjects)
		{
			final String code=Technical.TechCommand.SENSE.makeCommand(this,Boolean.FALSE);
			final CMMsg msg=CMClass.getMsg(mob, controlI, sensedObject, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			if(controlI.owner() instanceof Room)
			{
				if(((Room)controlI.owner()).okMessage(mob, msg))
					((Room)controlI.owner()).send(mob, msg);
			}
			else
			if(controlI.okMessage(mob, msg))
				controlI.executeMsg(mob, msg);
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
			{
				if((CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
				&&(msg.targetMessage()!=null))
				{
					final LanguageLibrary lang=CMLib.lang();
					final String[] parts=msg.targetMessage().split(" ");
					final TechCommand command=TechCommand.findCommand(parts);
					final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
					final MOB mob=msg.source();
					if(command==null)
						reportError(this, controlI, mob, lang.L("@x1 does not respond.",me.name(mob)), lang.L("Failure: @x1: control failure.",me.name(mob)));
					else
					{
						/*
						final Object[] parms=command.confirmAndTranslate(parts);
						if(parms==null)
							reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
						else
						*/
						if(command == TechCommand.SENSE)
						{
							if(doSensing(mob, controlI))
								this.activate(true);

						}
						else
							reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
					}
				}
				else
				{
					this.activate(true);
				}
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
				this.activate(false);
				//TODO:what does the ship need to know?
				break;
			}
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdElecCompSensor))
			return false;
		return super.sameAs(E);
	}
}
