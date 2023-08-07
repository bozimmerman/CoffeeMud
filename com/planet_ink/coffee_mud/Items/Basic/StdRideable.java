package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2023 Bo Zimmerman

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
public class StdRideable extends StdContainer implements Rideable
{
	@Override
	public String ID()
	{
		return "StdRideable";
	}

	protected Basis			rideBasis		= Rideable.Basis.WATER_BASED;
	protected int			riderCapacity	= 4;
	protected List<Rider>	riders			= new SVector<Rider>();
	protected String		putString		= "";
	protected String		rideString		= "";
	protected String		stateString		= "";
	protected String		stateSubjectStr	= "";
	protected String		mountString		= "";
	protected String		dismountString	= "";

	public StdRideable()
	{
		super();
		setName("a boat");
		setDisplayText("a boat is here.");
		setDescription("Looks like a boat");
		basePhyStats().setWeight(2000);
		recoverPhyStats();
		capacity=3000;
		material=RawMaterial.RESOURCE_OAK;
		setUsesRemaining(100);
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return (rideBasis() == Rideable.Basis.WATER_BASED);
	}

	@Override
	public void destroy()
	{
		while(riders.size()>0)
		{
			final Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		super.destroy();
	}

	public boolean savable()
	{
		Rider R=null;
		for(int r=0;r<numRiders();r++)
		{
			R=fetchRider(r);
			if(!R.isSavable())
				return false;
		}
		return super.isSavable();
	}

	@Override
	public boolean isMobileRideBasis()
	{
		switch(rideBasis())
		{
			case FURNITURE_SIT:
			case FURNITURE_TABLE:
			case ENTER_IN:
			case FURNITURE_SLEEP:
			case LADDER:
				return false;
			default:
				break;
		}
		return true;
	}

	// common item/mob stuff
	@Override
	public Basis rideBasis()
	{
		return rideBasis;
	}

	@Override
	public void setRideBasis(final Basis basis)
	{
		rideBasis = basis;
	}

	@Override
	public int riderCapacity()
	{
		return riderCapacity;
	}

	@Override
	public void setRiderCapacity(final int newCapacity)
	{
		riderCapacity = newCapacity;
	}

	@Override
	public int numRiders()
	{
		return riders.size();
	}

	@Override
	public Rider fetchRider(final int which)
	{
		try
		{
			return riders.get(which);
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException e)
		{
		}
		return null;
	}

	@Override
	public void addRider(final Rider mob)
	{
		if((mob!=null)&&(!riders.contains(mob)))
			riders.add(mob);
	}

	@Override
	public Enumeration<Rider> riders()
	{
		return new IteratorEnumeration<Rider>(riders.iterator());
	}

	@Override
	public void delRider(final Rider mob)
	{
		if(mob!=null)
			while(riders.remove(mob))
				{
				}
	}

	@Override
	protected void cloneFix(final Item E)
	{
		super.cloneFix(E);
		riders=new SVector<Rider>();
	}

	@Override
	public Set<MOB> getRideBuddies(final Set<MOB> list)
	{
		if(list==null)
			return list;
		for(int r=0;r<numRiders();r++)
		{
			final Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(!list.contains(R)))
				list.add((MOB)R);
		}
		return list;
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		switch(rideBasis)
		{
		case LAND_BASED:
			return L("a ground conveiance");
		case WATER_BASED:
			return L("a boat");
		case AIR_FLYING:
			return L("a flying conveiance");
		case FURNITURE_SIT:
			return L("a chair");
		case FURNITURE_SLEEP:
			return L("a bed");
		case FURNITURE_TABLE:
			return L("a table");
		case ENTER_IN:
			return L("a thing to get in");
		case LADDER:
			return L("a ladder");
		case WAGON:
			return L("a wagon");
		default:
			return L("a ride-able");
		}
	}

	@Override
	public boolean mobileRideBasis()
	{
		switch(rideBasis)
		{
		case AIR_FLYING:
		case LAND_BASED:
		case WAGON:
		case WATER_BASED:
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public String stateString(final Rider R)
	{
		if((R==null)||(stateString.length()==0))
		{
			switch(rideBasis)
			{
			case AIR_FLYING:
			case LAND_BASED:
			case WAGON:
			case WATER_BASED:
				return "riding in";
			case ENTER_IN:
				return "in";
			case FURNITURE_SIT:
				return "on";
			case FURNITURE_TABLE:
				return "at";
			case LADDER:
				return "climbing on";
			case FURNITURE_SLEEP:
				return "on";
			}
			return "riding in";
		}
		return stateString;
	}

	@Override
	public String getStateString()
	{
		return stateString;
	}

	@Override
	public void setStateString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			stateString="";
		else
		{
			if(str.equalsIgnoreCase(this.stateString(null)))
				stateString="";
			else
				stateString=str.trim();
		}
	}

	@Override
	public String putString(final Rider R)
	{
		if((R==null)||(putString.length()==0))
		{
			switch(rideBasis)
			{
			case AIR_FLYING:
			case LAND_BASED:
			case WAGON:
			case WATER_BASED:
			case FURNITURE_SLEEP:
			case ENTER_IN:
				return "in";
			case FURNITURE_SIT:
			case FURNITURE_TABLE:
			case LADDER:
				return "on";
			}
			return "in";
		}
		return putString;
	}

	@Override
	public String getPutString()
	{
		return putString;
	}

	@Override
	public void setPutString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			putString="";
		else
		{
			if(str.equalsIgnoreCase(this.putString(null)))
				putString="";
			else
				putString=str.trim();
		}
	}

	@Override
	public String rideString(final Rider R)
	{
		if((R==null)||(rideString.length()==0))
			return "ride(s)";
		return rideString;
	}

	@Override
	public String getRideString()
	{
		return rideString;
	}

	@Override
	public void setRideString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			rideString="";
		else
		{
			if(str.equalsIgnoreCase(this.rideString(null)))
				rideString="";
			else
				rideString=str.trim();
		}
	}

	@Override
	public String getMountString()
	{
		return mountString;
	}

	@Override
	public String mountString(final int commandType, final Rider R)
	{
		if((R==null)||(mountString.length()==0))
		{
			switch(rideBasis)
			{
			case AIR_FLYING:
			case LAND_BASED:
			case WAGON:
			case WATER_BASED:
				return "board(s)";
			case FURNITURE_SIT:
				return "sit(s) on";
			case FURNITURE_TABLE:
				return "sit(s) at";
			case ENTER_IN:
				return "get(s) into";
			case LADDER:
				return "climb(s) onto";
			case FURNITURE_SLEEP:
				if(commandType==CMMsg.TYP_SIT)
					return "sit(s) down on";
				return "lie(s) down on";
			}
			return "board(s)";
		}
		return mountString;
	}

	@Override
	public void setMountString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			mountString="";
		else
		{
			if(str.equalsIgnoreCase(this.mountString(0,null)))
				mountString="";
			else
				mountString=str.trim();
		}
	}

	@Override
	public String dismountString(final Rider R)
	{
		if((R==null)||(dismountString.length()==0))
		{
			switch(rideBasis)
			{
			case AIR_FLYING:
			case LAND_BASED:
			case WATER_BASED:
				return "disembark(s) from";
			case FURNITURE_TABLE:
				return "get(s) up from";
			case FURNITURE_SIT:
			case FURNITURE_SLEEP:
			case WAGON:
			case LADDER:
				return "get(s) off of";
			case ENTER_IN:
				return "get(s) out of";
			}
			return "disembark(s) from";
		}
		return dismountString;
	}

	@Override
	public String getDismountString()
	{
		return dismountString;
	}

	@Override
	public void setDismountString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			dismountString="";
		else
		{
			if(str.equalsIgnoreCase(this.dismountString(null)))
				dismountString="";
			else
				dismountString=str.trim();
		}
	}

	@Override
	public String stateStringSubject(final Rider R)
	{
		if((R==null)||(stateSubjectStr.length()==0))
		{
			switch(rideBasis)
			{
			case AIR_FLYING:
			case LAND_BASED:
			case WATER_BASED:
			case WAGON:
				return "being ridden by";
			case FURNITURE_TABLE:
				return "occupied by";
			case FURNITURE_SIT:
				return "";
			case FURNITURE_SLEEP:
				return "";
			case ENTER_IN:
				return "occupied by";
			case LADDER:
				return "occupied by";
			}
			return "";
		}
		return stateSubjectStr;
	}

	@Override
	public String getStateStringSubject()
	{
		return stateSubjectStr;
	}

	@Override
	public void setStateStringSubject(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			this.stateSubjectStr="";
		else
		{
			if(str.equalsIgnoreCase(this.stateStringSubject(null)))
				stateSubjectStr="";
			else
				stateSubjectStr=str.trim();
		}
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(rideBasis==Rideable.Basis.AIR_FLYING)
		{
			if((!subjectToWearAndTear())||(usesRemaining()>0))
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FLYING);
			else
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
		}
		else
		if(rideBasis==Rideable.Basis.WATER_BASED)
		{
			if((!subjectToWearAndTear())||(usesRemaining()>0))
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
			else
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(!CMLib.flags().isWithSeenContents(this))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
			if((mob.isInCombat())&&(mob.rangeToTarget()==0)&&(amRiding(mob)))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-mob.basePhyStats().attackAdjustment());
				affectableStats.setDamage(affectableStats.damage()-mob.basePhyStats().damage());
			}
			if((rideBasis()==Rideable.Basis.LADDER)
			&&(amRiding(mob)))
			{
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_CLIMBING);
				affectableStats.setSpeed(affectableStats.speed()/2);
			}
		}
	}

	@Override
	public String displayText(final MOB mob)
	{
 		if((numRiders()>0)
 		&&(stateStringSubject(this).length()>0)
 		&&(displayText!=null)
 		&&(displayText.length()>0)
 		&&CMLib.flags().isWithSeenContents(this))
		{
			final StringBuffer sendBack=new StringBuffer(name(mob));
			sendBack.append(" "+stateStringSubject(this)+" ");
			for(int r=0;r<numRiders();r++)
			{
				final Rider rider=fetchRider(r);
				if(rider!=null)
				{
					if(r>0)
					{
						sendBack.append(", ");
						if(r==numRiders()-1)
							sendBack.append("and ");
					}
					sendBack.append(rider.name(mob));
				}

			}
			return sendBack.toString();
		}
		return super.displayText(mob);
	}

	@Override
	public boolean amRiding(final Rider mob)
	{
		return riders.contains(mob);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_PUSH:
		case CMMsg.TYP_PULL:
			if(amRiding(msg.source()))
			{
				msg.source().tell(L("You cannot do that while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_ADVANCE:
			if((rideBasis()==Rideable.Basis.LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell(L("You cannot advance while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_RETREAT:
			if((rideBasis()==Rideable.Basis.LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell(L("You cannot retreat while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_DISMOUNT:
			if(msg.amITarget(this))
			{
				if(msg.tool() instanceof Rider)
				{
					if(!amRiding((Rider)msg.tool()))
					{
						msg.source().tell(L("@x1 is not @x2 @x3!",msg.tool().name(),stateString((Rider)msg.tool()),name(msg.source())));
						if(((Rider)msg.tool()).riding()==this)
							((Rider)msg.tool()).setRiding(null);
						return false;
					}
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().tell(L("You are not @x1 @x2!",stateString(msg.source()),name(msg.source())));
					if(msg.source().riding()==this)
						msg.source().setRiding(null);
					return false;
				}
				// protects from standard item rejection
				return true;
			}
			break;
		case CMMsg.TYP_SIT:
			if(amRiding(msg.source()))
			{
				switch(rideBasis())
				{
				case ENTER_IN:
				case WAGON:
				case WATER_BASED:
				case LAND_BASED:
				case AIR_FLYING:
				case FURNITURE_SIT:
					return true;
				case FURNITURE_SLEEP:
					if(CMLib.flags().isSleeping(msg.source()))
						return true;
					break;
				default:
					break;
				}
				msg.source().tell(L("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if(riding()!=msg.source())
			{
				if((msg.amITarget(this))
				&&(!amRiding(msg.source())))
				{
					if(numRiders()>=riderCapacity())
					{
						// for items
						msg.source().tell(L("@x1 is full.",name(msg.source())));
						// for mobs
						// msg.source().tell(L("No more can fit on @x1.",name(msg.source())));
						return false;
					}
					if((rideBasis()==Rideable.Basis.ENTER_IN)
					&&(super.hasALid && !super.isOpen))
					{
						msg.source().tell(L("@x1 is closed.",name(msg.source())));
						return false;
					}
				}
				return true;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell(L("You cannot sit on @x1.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if((amRiding(msg.source()))
			&&(((!msg.amITarget(this))&&(msg.target()!=null))
			   ||((rideBasis()!=Rideable.Basis.FURNITURE_SLEEP)
					&&(rideBasis()!=Rideable.Basis.ENTER_IN)
					&&(rideBasis()!=Rideable.Basis.WAGON)
					&&(rideBasis()!=Rideable.Basis.LAND_BASED)
					&&(rideBasis()!=Rideable.Basis.AIR_FLYING)
					&&(rideBasis()!=Rideable.Basis.WATER_BASED)
				)
			))
			{
				msg.source().tell(L("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.Basis.FURNITURE_SLEEP)
			||(rideBasis()==Rideable.Basis.ENTER_IN)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
					if(super.hasALid && !super.isOpen)
					{
						msg.source().tell(L("@x1 is closed.",name(msg.source())));
						return false;
					}
					// for items
					msg.source().tell(L("@x1 is full.",name(msg.source())));
					// for mobs
					// msg.source().tell(L("No more can fit on @x1.",name(msg.source())));
					return false;
				}
				return true;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell(L("You cannot lie down on @x1.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
		{
			if(amRiding(msg.source()))
			{
				msg.source().tell(null,msg.source(),null,L("<T-NAME> <T-IS-ARE> @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			if((riding()==msg.target())&&(msg.tool() instanceof Item))
			{
				msg.source().tell(null,msg.source(),null,L("<T-NAME> <T-IS-ARE> already @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			if((msg.tool() instanceof Item)
			&&(msg.amITarget(this))
			&&(((Item)msg.tool()).container() != container()))
			{
				msg.source().tell(null,msg.tool(),null,L("<T-NAME> can't be mounted to @x1 from where it is!",name(msg.source())));
				return false;
			}
			else
			if((msg.tool() instanceof MOB)
			&&(msg.amITarget(this)))
			{
				final Rider whoWantsToRide=(msg.tool() instanceof Rider)?(Rider)msg.tool():msg.source();
				if(amRiding(whoWantsToRide))
				{
					msg.source().tell(null,whoWantsToRide,null,L("<T-NAME> <T-IS-ARE> @x1 @x2!",stateString(msg.source()),name(msg.source())));
					whoWantsToRide.setRiding(this);
					return false;
				}
				if((msg.tool() instanceof MOB)
				&&(!CMLib.flags().isBoundOrHeld((MOB)msg.tool())))
				{
					msg.source().tell(L("@x1 won't let you do that.",((MOB)msg.tool()).name(msg.source())));
					return false;
				}
				else
				if(riding()==whoWantsToRide)
				{
					if(msg.tool() instanceof Physical)
						msg.source().tell(L("@x1 can not be mounted to @x2!",((Physical)msg.tool()).name(msg.source()),name(msg.source())));
					else
						msg.source().tell(L("@x1 can not be mounted to @x2!",msg.tool().name(),name(msg.source())));
					return false;
				}
				else
				if(msg.tool() instanceof Rideable)
				{
					msg.source().tell(L("@x1 is not allowed on @x2.",((Rideable)msg.tool()).name(msg.source()),name(msg.source())));
					return false;
				}
				else
				if((container() != null)
				&&((MOB)msg.tool()).riding() != container())
				{
					msg.source().tell(L("@x1 can not be mounted to @x2 from there!",msg.tool().name(),name(msg.source())));
					return false;
				}
				if(msg.tool()==null)
					switch(rideBasis())
					{
					case ENTER_IN:
					case FURNITURE_SIT:
					case FURNITURE_SLEEP:
						msg.source().tell(L("@x1 can not be mounted in this way.",name(msg.source())));
						return false;
					default:
						break;
					}
				if((numRiders()>=riderCapacity())
				&&(!amRiding(whoWantsToRide)))
				{
					// for items
					msg.source().tell(L("@x1 is full.",name(msg.source())));
					// for mobs
					// msg.source().tell(L("No more can fit on @x1.",name(msg.source())));
					return false;
				}
				// protects from standard item rejection
				return true;
			}
			break;
		}
		case CMMsg.TYP_ENTER:
			if(amRiding(msg.source())
			&&(msg.target() instanceof Room))
			{
				final Room sourceRoom=msg.source().location();
				final Room targetRoom=(Room)msg.target();
				if((sourceRoom!=null)&&(!msg.amITarget(sourceRoom)))
				{
					boolean ok=((targetRoom.domainType()&Room.INDOORS)==0)
								||(targetRoom.maxRange()>4)
								||(targetRoom.phyStats().weight()>2);
					switch(rideBasis)
					{
					case LAND_BASED:
					case WAGON:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||CMLib.flags().isWateryRoom(targetRoom))
							ok=false;
						if((rideBasis==Rideable.Basis.WAGON)
						&&((riding()==null)
						   ||(!(riding() instanceof MOB))
						   ||(((MOB)riding()).basePhyStats().weight()<(basePhyStats().weight()/5))))
						{
							msg.source().tell(L("@x1 doesn't seem to be moving.",name(msg.source())));
							return false;
						}
						break;
					case AIR_FLYING:
						break;
					case LADDER:
						ok=true;
						break;
					case WATER_BASED:
						if((!CMLib.flags().isWaterySurfaceRoom(sourceRoom))
						&&(!CMLib.flags().isWaterySurfaceRoom(targetRoom)))
							ok=false;
						else
							ok=true;
						if((targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(CMLib.flags().isUnderWateryRoom(targetRoom)))
							ok=false;
						break;
					default:
						break;
					}
					if(!ok)
					{
						msg.source().tell(L("You cannot ride @x1 that way.",name(msg.source())));
						return false;
					}
					if(CMLib.flags().isSitting(msg.source()))
					{
						msg.source().tell(L("You cannot crawl while @x1 @x2.",stateString(msg.source()),name(msg.source())));
						return false;
					}
				}
			}
			break;
		case CMMsg.TYP_GIVE:
			if(msg.target() instanceof MOB)
			{
				final MOB tmob=(MOB)msg.target();
				if((amRiding(tmob))&&(!amRiding(msg.source())))
				{
					if(rideBasis()==Rideable.Basis.ENTER_IN)
						msg.source().tell(msg.source(),tmob,null,L("<T-NAME> must exit first."));
					else
						msg.source().tell(msg.source(),tmob,null,L("<T-NAME> must disembark first."));
					return false;
				}
			}
			break;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_BID:
		case CMMsg.TYP_SELL:
			if((amRiding(msg.source()))
			&&(rideBasis()!=Rideable.Basis.FURNITURE_TABLE)
			&&(rideBasis()!=Rideable.Basis.FURNITURE_SIT))
			{
				msg.source().tell(L("You can not do that while @x1 @x2.",stateString(msg.source()),name(msg.source())));
				return false;
			}
			return super.okMessage(myHost,msg);
		}
		if((msg.sourceMajor(CMMsg.MASK_HANDS))
		&&(amRiding(msg.source()))
		&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
		&&(msg.target()!=this)
		&&(msg.tool()!=this)
		&&((!CMLib.utensils().reachableItem(msg.source(),msg.target()))
			|| (!CMLib.utensils().reachableItem(msg.source(),msg.tool()))
			|| ((msg.sourceMinor()==CMMsg.TYP_GIVE)&&(msg.target() instanceof MOB)&&(msg.target()!=this)&&(!amRiding((MOB)msg.target()))))
		&&(!((msg.sourceMinor()==CMMsg.TYP_GIVE)&&(msg.target() instanceof MOB)&&(amRiding((MOB)msg.target()))&&(CMLib.flags().isStanding(msg.source())))))
		{
			// some of the above applies to genrideable items only
			msg.source().tell(L("You can not do that while @x1 @x2.",stateString(msg.source()),name(msg.source())));
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
			if(msg.target()==this)
			{
				if((numRiders()>0)
				&&(CMLib.flags().canBeSeenBy(this,msg.source())))
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,displayText(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				if((this.subjectToWearAndTear())
				&& (this.rideBasis() == Rideable.Basis.WATER_BASED)
				&& (CMath.bset(material(), RawMaterial.MATERIAL_WOODEN)))
				{
					// this is for the small rideable boats
					final StringBuilder visualCondition = new StringBuilder("");
					if(this.subjectToWearAndTear() && (usesRemaining() <= 100))
					{
						final double pct=(CMath.div(usesRemaining(),100.0));
						StdSiegableBoardable.appendCondition(visualCondition,pct,CMStrings.capitalizeFirstLetter(name(msg.source())));
					}
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							msg.source().tell(visualCondition.toString());
							msg.trailerRunnables().remove(this);
						}
					});
				}
			}
			break;
		case CMMsg.TYP_DISMOUNT:
			if(msg.tool() instanceof Rider)
			{
				((Rider)msg.tool()).setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_ENTER:
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
			if((rideBasis()==Rideable.Basis.LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_SIT:
		case CMMsg.TYP_SLEEP:
			if(msg.amITarget(this))
			{
				if(msg.tool() instanceof Rider)
				{
					final Rider R = (Rider)msg.tool();
					R.setRiding(this);
					if(msg.tool() instanceof MOB)
					switch(rideBasis())
					{
					case FURNITURE_SIT:
					case ENTER_IN:
						R.basePhyStats().setDisposition(R.basePhyStats().disposition()|PhyStats.IS_SITTING);
						break;
					case FURNITURE_SLEEP:
						R.basePhyStats().setDisposition(R.basePhyStats().disposition()|PhyStats.IS_SLEEPING);
						break;
					default:
						break;
					}
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().setRiding(this);
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
			}
			break;
		case CMMsg.TYP_WEAPONATTACK:
			if(msg.target()==this)
			{
				Weapon weapon=null;
				if((msg.tool() instanceof Weapon))
					weapon=(Weapon)msg.tool();
				if((weapon!=null)&&(msg.source().riding()!=null))
				{
					final boolean isHit=msg.value()>0;
					if(isHit && CMLib.combat().isASiegeWeapon(weapon)
					&& (((AmmunitionWeapon)weapon).ammunitionCapacity() > 1))
					{
						int shotsRemaining = ((AmmunitionWeapon)weapon).ammunitionRemaining() + 1;
						((AmmunitionWeapon)weapon).setAmmoRemaining(0);
						final ArrayList<Pair<MOB,Room>> targets = new ArrayList<Pair<MOB,Room>>(5);
						final Room R=CMLib.map().roomLocation(this);
						if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
						{
							for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if((M!=null)&&(M.riding()==this))
									targets.add(new Pair<MOB,Room>(M,R));
							}
						}
						final int chanceToHit = targets.size() * 20;
						final Room oldRoom=msg.source().location();
						try
						{
							while(shotsRemaining-- > 0)
							{
								final Pair<MOB,Room> randomPair = (targets.size()>0)? targets.get(CMLib.dice().roll(1,targets.size(),-1)) : null;
								if((CMLib.dice().rollPercentage() < chanceToHit)&&(randomPair != null))
								{
									msg.source().setLocation(randomPair.second);
									final double pctLoss = CMath.div(msg.value(), phyStats().level());
									final int pointsLost = (int)Math.round(pctLoss * msg.source().maxState().getHitPoints());
									CMLib.combat().postWeaponDamage(msg.source(), randomPair.first, weapon, pointsLost);
								}
								else
								if(randomPair != null)
								{
									msg.source().setLocation(randomPair.second);
									CMLib.combat().postWeaponAttackResult(msg.source(), randomPair.first, weapon, 0, false);
								}
								else
								if(R!=null)
								{
									final CMMsg missMsg=CMClass.getMsg(msg.source(), msg.target(), weapon, CMMsg.MASK_ALWAYS|CMMsg.MSG_ATTACKMISS, weapon.missString());
									if(R.okMessage(msg.source(), missMsg))
										R.send(msg.source(), missMsg);
								}
							}
						}
						finally
						{
							msg.source().setLocation(oldRoom);
						}
					}
					else
						CMLib.combat().postSiegeWeaponAttackResult(msg.source(), msg.source().riding(), this, weapon, isHit);
				}
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_STAND:
		case CMMsg.TYP_QUIT:
		case CMMsg.TYP_PANIC:
		case CMMsg.TYP_DEATH:
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(msg.source().location()!=null)
					msg.source().location().recoverRoomStats();
			}
			break;
		}
	}

}
