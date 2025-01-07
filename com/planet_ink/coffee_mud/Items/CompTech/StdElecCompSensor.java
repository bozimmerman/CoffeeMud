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
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2024 Bo Zimmerman

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

	protected final static Coord3D emptyCoords = new Coord3D();
	protected final static Dir3D emptyDirection = new Dir3D();
	protected final static BoundedCube smallCube = new BoundedCube(1,1,1,1,1,1);
	protected final static BoundedSphere smallSphere = new BoundedSphere(new long[] {1,1,1},1);
	protected Map<Software,Room> feedbackObjects = new TreeMap<Software,Room>(XTreeSet.comparator);
	protected Map<Environmental,Environmental> lastSensedObjects = new TreeMap<Environmental,Environmental>(XTreeSet.comparator);
	protected volatile long nextFailureCheck = System.currentTimeMillis();
	protected volatile Set<Environmental> lastFailures = Collections.synchronizedSet(new HashSet<Environmental>());

	private static final Filterer<Environmental> acceptEverythingFilter = new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return true;
		}
	};

	protected List<Software> getFeedbackers()
	{
		final List<Software> fbList = new LinkedList<Software>();
		synchronized(feedbackObjects)
		{
			for(final Iterator<Software> i=feedbackObjects.keySet().iterator();i.hasNext();)
			{
				final Software S=i.next();
				if(!S.amDestroyed())
					fbList.add(S);
				else
					i.remove();
			}
		}
		return fbList;
	}

	protected Item getHostItem()
	{
		final SpaceObject O=CMLib.space().getSpaceObject(this, true);
		if(O instanceof Boardable)
			return ((Boardable)O).getBoardableItem();
		if(O instanceof Item)
			return (Item)O;
		return null;
	}

	protected SpaceShip getSpaceShip()
	{
		final Item I=getHostItem();
		if(I instanceof SpaceShip)
			return (SpaceShip)I;
		return null;
	}

	protected boolean isInSpace()
	{
		final SpaceObject O=CMLib.space().getSpaceObject(this, true);
		if(O != null)//&&(this.powerRemaining() > this.powerNeeds()))
			return CMLib.space().isObjectInSpace(O);
		return false;
	}

	protected boolean canPassivelySense(final CMMsg msg)
	{
		// don't sense the things people in the room do
		if((msg.source().location()!=null)
		&&(msg.source().location()==owner()))
			return false;
		if((msg.source().location()!=null)
		&&(owner() instanceof Room)
		&&(msg.source().location().getArea()==((Room)owner()).getArea()))
			return false;
		return true;
	}

	protected String renderMessageForComputer(final CMMsg msg)
	{
		// this should reflect the visibility of the various objects involved, not simply
		// render them all directly.
		return CMLib.coffeeFilter().fullOutFilter(null, msg.source(), msg.source(), msg.target(), msg.tool(), msg.othersMessage(), false);
	}

	private static final Converter<Environmental, Environmental> directConverter = new Converter<Environmental, Environmental>()
	{
		@Override
		public Environmental convert(final Environmental obj)
		{
			final Environmental me=obj;
			return new SpaceObject.SensedSpaceObject()
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
				public Environmental get()
				{
					return obj;
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
				public BoundedCube getCube()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.getCube();
					return smallCube;
				}

				@Override
				public BoundedSphere getSphere()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.getSphere();
					return smallSphere;
				}

				@Override
				public Coord3D coordinates()
				{
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.coordinates().copyOf();
					return emptyCoords.copyOf();
				}

				@Override
				public void setCoords(final Coord3D coords)
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
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
					if(sobj!=null)
						return sobj.direction();
					return emptyDirection;
				}

				@Override
				public void setDirection(final Dir3D dir)
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
					final SpaceObject sobj=CMLib.space().getSpaceObject(obj, false);
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

	private static class DistanceSorter implements Comparator<Environmental>
	{
		private final GalacticMap space;
		private final SpaceObject spaceObject;

		private DistanceSorter(final SpaceObject me)
		{
			space=CMLib.space();
			spaceObject=me;
		}

		@Override
		public int compare(final Environmental o1, final Environmental o2)
		{
			if((o1 == null)||(!(o1 instanceof SpaceObject)))
				return ((o2 == null)||(!(o2 instanceof SpaceObject))) ? 0 : 1;
			if((o2 == null)||(!(o2 instanceof SpaceObject)))
				return -1;
			final SpaceObject s1=(SpaceObject)o1;
			final SpaceObject s2=(SpaceObject)o2;
			if(s1.coordinates() == null)
				return (s2.coordinates() == null) ? 0 : 1;
			if(s2.coordinates() == null)
				return -1;
			final long distance1 = space.getDistanceFrom(spaceObject, s1) - s1.radius();
			final long distance2 = space.getDistanceFrom(spaceObject, s2) - s2.radius();
			if(distance1 < distance2)
				return -1;
			if(distance1 > distance2)
				return 1;
			return 0;
		}
	}

	protected List<? extends Environmental> getAllSensibleObjects()
	{
		final SpaceObject O=CMLib.space().getSpaceObject(this, true);
		if((O!=null)
		&&(this.powerRemaining() > this.powerNeeds()))
		{
			final long maxRange = Math.round(getSensorMaxRange() * this.getComputedEfficiency());
			final List<? extends Environmental> found = CMLib.space().getSpaceObjectsWithin(O, O.radius()+1, maxRange);
			found.remove(O);
			if(found.size() > 1)
			{
				if(System.currentTimeMillis() > this.nextFailureCheck)
				{
					lastFailures.clear();
					if(CMLib.dice().rollPercentage() > (100*this.getFinalManufacturer().getReliabilityPct()))
					{
						Collections.sort(found, new DistanceSorter(O));
						nextFailureCheck = System.currentTimeMillis() + 360000;
						int num = found.size() / 10; // failing reliability check always loses 10% of distant found things
						if(num <= 0)
							num = 1;
						for(int i=0;i<num && (found.size() > 0);i++)
						{
							final Environmental E =  found.remove(found.size()-1);
							if(E instanceof SpaceObject)
								lastFailures.add(E);
						}
					}
				}
				else
					found.removeAll(lastFailures);
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

	protected void sendDetectionAnnouncement(final MOB mob, final Environmental sensedObject)
	{
		final CMMsg detMsg = CMClass.getMsg(mob, sensedObject,null,CMMsg.MSG_EMISSION,null,CMMsg.MSG_EMISSION,null,CMMsg.MSG_EMISSION,
				L("@x1: <T-NAME> has been detected.",name()));
		final String renderedMsg = renderMessageForComputer(detMsg);
		for(final Software controlSW : getFeedbackers())
			controlSW.addScreenMessage(renderedMsg);
	}

	protected void sendLostDetectionAnnouncement(final MOB mob, final Environmental sensedObject)
	{
		final CMMsg lostMsg = CMClass.getMsg(mob, sensedObject,null,CMMsg.MSG_EMISSION,null,CMMsg.MSG_EMISSION,null,CMMsg.MSG_EMISSION,
				L("@x1: <T-NAME> is no longer detected.",name()));
		final String renderedMsg = renderMessageForComputer(lostMsg);
		for(final Software controlSW : getFeedbackers())
			controlSW.addScreenMessage(renderedMsg);
	}

	protected boolean doSensing(final MOB mob, final Software controlI)
	{
		final List<? extends Environmental> found= getSensedObjects();
		final Converter<Environmental, Environmental> converter = getSensedObjectConverter();
		final Set<Environmental> newlySensed = new TreeSet<Environmental>();
		for(final Environmental obj : found)
		{
			final Environmental sensedObject = converter.convert(obj);
			synchronized(lastSensedObjects)
			{
				if(!lastSensedObjects.containsKey(obj))
				{
					newlySensed.add(sensedObject);
					lastSensedObjects.put(obj,sensedObject);
				}
			}
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
			for(final Iterator<Environmental> i=lastSensedObjects.keySet().iterator();i.hasNext();)
			{
				final Environmental nobj = i.next();
				if(!found.contains(nobj))
				{
					final Environmental sensedObj = lastSensedObjects.get(nobj);
					i.remove();
					prevSensedObjects.add(sensedObj);
				}
			}
		}
		// newlySensed contains the new things
		// prevSensedObjects things we no longer sense
		// should these be converted?  i kinda think so...
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
			sendLostDetectionAnnouncement(mob, sensedObject);
		}
		for(final Environmental newSensedObject : newlySensed)
			sendDetectionAnnouncement(mob, newSensedObject);
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
					final TechCommand command=TechCommand.findCommand(msg.targetMessage());
					final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
					synchronized(feedbackObjects)
					{
						if((!feedbackObjects.containsKey(controlI))
						&&(controlI!=null)
						&&(controlI.owner() instanceof Room))
						{
							final Room R=(Room)controlI.owner();
							if(R!=null)
								feedbackObjects.put(controlI,R);
						}
					}
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
							if(powerRemaining()>0)
							{
								if(doSensing(mob, controlI))
								{
									this.setPowerRemaining(powerRemaining()-1);
									this.activate(true);
								}
							}
						}
						else
							reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
					}
				}
				else
				{
					synchronized(feedbackObjects)
					{
						if((msg.tool() instanceof Software)
						&&(!feedbackObjects.containsKey(msg.tool()))
						&&(((Item)msg.tool()).owner() instanceof Room))
						{
							final Room R=(Room)((Item)msg.tool()).owner();
							if(R!=null)
								feedbackObjects.put((Software)msg.tool(),R);
						}
					}
					this.activate(true);
				}
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
			{
				synchronized(feedbackObjects)
				{
					if((msg.tool() instanceof Software)
					&&(feedbackObjects.containsKey(msg.tool())))
						feedbackObjects.remove(msg.tool());
				}
				this.activate(false);
				break;
			}
			}
		}
		else
		// all sensor messages go through here.  act/deact messages targeting me are never emissions
		if((msg.othersMessage() != null)
		&& canPassivelySense(msg))
		{
			final String renderedMsg = L("@x1 detects: ",name())+this.renderMessageForComputer(msg);
			for(final Software controlI : getFeedbackers())
				controlI.addScreenMessage(renderedMsg);
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
