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
   Copyright 2002-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdRideable extends StdContainer implements Rideable
{
	@Override
	public String ID()
	{
		return "StdRideable";
	}

	protected int			rideBasis		= Rideable.RIDEABLE_WATER;
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
		return (rideBasis() == Rideable.RIDEABLE_WATER);
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
			case RIDEABLE_SIT:
			case RIDEABLE_TABLE:
			case RIDEABLE_ENTERIN:
			case RIDEABLE_SLEEP:
			case RIDEABLE_LADDER:
				return false;
		}
		return true;
	}
	
	// common item/mob stuff
	@Override
	public int rideBasis()
	{
		return rideBasis;
	}

	@Override
	public void setRideBasis(int basis)
	{
		rideBasis = basis;
	}

	@Override
	public int riderCapacity()
	{
		return riderCapacity;
	}

	@Override
	public void setRiderCapacity(int newCapacity)
	{
		riderCapacity = newCapacity;
	}

	@Override
	public int numRiders()
	{
		return riders.size();
	}

	@Override
	public Rider fetchRider(int which)
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
	public void addRider(Rider mob)
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
	public void delRider(Rider mob)
	{
		if(mob!=null)
			while(riders.remove(mob))
				{
				}
	}

	@Override
	protected void cloneFix(Item E)
	{
		super.cloneFix(E);
		riders=new SVector();
	}

	@Override
	public Set<MOB> getRideBuddies(Set<MOB> list)
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
	public boolean mobileRideBasis()
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return true;
		}
		return false;
	}

	@Override
	public String stateString(Rider R)
	{
		if((R==null)||(stateString.length()==0))
		{
			switch(rideBasis)
			{
			case Rideable.RIDEABLE_AIR:
			case Rideable.RIDEABLE_LAND:
			case Rideable.RIDEABLE_WAGON:
			case Rideable.RIDEABLE_WATER:
				return "riding in";
			case Rideable.RIDEABLE_ENTERIN:
				return "in";
			case Rideable.RIDEABLE_SIT:
				return "on";
			case Rideable.RIDEABLE_TABLE:
				return "at";
			case Rideable.RIDEABLE_LADDER:
				return "climbing on";
			case Rideable.RIDEABLE_SLEEP:
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
	public void setStateString(String str)
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
	public String putString(Rider R)
	{
		if((R==null)||(putString.length()==0))
		{
			switch(rideBasis)
			{
			case Rideable.RIDEABLE_AIR:
			case Rideable.RIDEABLE_LAND:
			case Rideable.RIDEABLE_WAGON:
			case Rideable.RIDEABLE_WATER:
			case Rideable.RIDEABLE_SLEEP:
			case Rideable.RIDEABLE_ENTERIN:
				return "in";
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_TABLE:
			case Rideable.RIDEABLE_LADDER:
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
	public void setPutString(String str)
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
	public String rideString(Rider R)
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
	public void setRideString(String str)
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
	public String mountString(int commandType, Rider R)
	{
		if((R==null)||(mountString.length()==0))
		{
			switch(rideBasis)
			{
			case Rideable.RIDEABLE_AIR:
			case Rideable.RIDEABLE_LAND:
			case Rideable.RIDEABLE_WAGON:
			case Rideable.RIDEABLE_WATER:
				return "board(s)";
			case Rideable.RIDEABLE_SIT:
				return "sit(s) on";
			case Rideable.RIDEABLE_TABLE:
				return "sit(s) at";
			case Rideable.RIDEABLE_ENTERIN:
				return "get(s) into";
			case Rideable.RIDEABLE_LADDER:
				return "climb(s) onto";
			case Rideable.RIDEABLE_SLEEP:
				if(commandType==CMMsg.TYP_SIT)
					return "sit(s) down on";
				return "lie(s) down on";
			}
			return "board(s)";
		}
		return mountString;
	}

	@Override
	public void setMountString(String str)
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
	public String dismountString(Rider R)
	{
		if((R==null)||(dismountString.length()==0))
		{
			switch(rideBasis)
			{
			case Rideable.RIDEABLE_AIR:
			case Rideable.RIDEABLE_LAND:
			case Rideable.RIDEABLE_WATER:
				return "disembark(s) from";
			case Rideable.RIDEABLE_TABLE:
				return "get(s) up from";
			case Rideable.RIDEABLE_SIT:
			case Rideable.RIDEABLE_SLEEP:
			case Rideable.RIDEABLE_WAGON:
			case Rideable.RIDEABLE_LADDER:
				return "get(s) off of";
			case Rideable.RIDEABLE_ENTERIN:
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
	public void setDismountString(String str)
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
	public String stateStringSubject(Rider R)
	{
		if((R==null)||(stateSubjectStr.length()==0))
		{
			switch(rideBasis)
			{
			case Rideable.RIDEABLE_AIR:
			case Rideable.RIDEABLE_LAND:
			case Rideable.RIDEABLE_WATER:
			case Rideable.RIDEABLE_WAGON:
				return "being ridden by";
			case Rideable.RIDEABLE_TABLE:
				return "occupied by";
			case Rideable.RIDEABLE_SIT:	return "";
			case Rideable.RIDEABLE_SLEEP: return "";
			case Rideable.RIDEABLE_ENTERIN: return "occupied by";
			case Rideable.RIDEABLE_LADDER: return "occupied by";
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
	public void setStateStringSubject(String str)
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
		if(rideBasis==Rideable.RIDEABLE_AIR)
		{
			if((!subjectToWearAndTear())||(usesRemaining()>0))
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FLYING);
			else
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
		}
		else
		if(rideBasis==Rideable.RIDEABLE_WATER)
		{
			if((!subjectToWearAndTear())||(usesRemaining()>0))
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
			else
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FALLING);
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
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
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(mob)))
			{
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_CLIMBING);
				affectableStats.setSpeed(affectableStats.speed()/2);
			}
		}
	}

	@Override
	public String displayText(MOB mob)
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
	public boolean amRiding(Rider mob)
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
		case CMMsg.TYP_ADVANCE:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
			&&(amRiding(msg.source())))
			{
				msg.source().tell(L("You cannot advance while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_RETREAT:
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
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
				if((rideBasis()==Rideable.RIDEABLE_SLEEP) && (CMLib.flags().isSleeping(msg.source())))
					return true;
				msg.source().tell(L("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_SIT)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)
			||(rideBasis()==Rideable.RIDEABLE_TABLE)
			||(rideBasis()==Rideable.RIDEABLE_SLEEP)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
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
				msg.source().tell(L("You cannot sit on @x1.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if((amRiding(msg.source()))
			&&(((!msg.amITarget(this))&&(msg.target()!=null))
			   ||((rideBasis()!=Rideable.RIDEABLE_SLEEP)&&(rideBasis()!=Rideable.RIDEABLE_ENTERIN))))
			{
				msg.source().tell(L("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if((riding()!=msg.source())
			&&((rideBasis()==Rideable.RIDEABLE_SLEEP)
			||(rideBasis()==Rideable.RIDEABLE_ENTERIN)))
			{
				if(msg.amITarget(this)
				&&(numRiders()>=riderCapacity())
				&&(!amRiding(msg.source())))
				{
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
			if(msg.amITarget(this))
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
				if(msg.tool()==null)
					switch(rideBasis())
					{
					case Rideable.RIDEABLE_ENTERIN:
					case Rideable.RIDEABLE_SIT:
					case Rideable.RIDEABLE_SLEEP:
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
								||(targetRoom.maxRange()>4);
					switch(rideBasis)
					{
					case Rideable.RIDEABLE_LAND:
					case Rideable.RIDEABLE_WAGON:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||CMLib.flags().isWateryRoom(targetRoom))
							ok=false;
						if((rideBasis==Rideable.RIDEABLE_WAGON)
						&&((riding()==null)
						   ||(!(riding() instanceof MOB))
						   ||(((MOB)riding()).basePhyStats().weight()<(basePhyStats().weight()/5))))
						{
							msg.source().tell(L("@x1 doesn't seem to be moving.",name(msg.source())));
							return false;
						}
						break;
					case Rideable.RIDEABLE_AIR:
						break;
					case Rideable.RIDEABLE_LADDER:
						ok=true;
						break;
					case Rideable.RIDEABLE_WATER:
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
					if(rideBasis()==Rideable.RIDEABLE_ENTERIN)
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
			&&(rideBasis()!=Rideable.RIDEABLE_TABLE)
			&&(rideBasis()!=Rideable.RIDEABLE_SIT))
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
				&& (this.rideBasis() == Rideable.RIDEABLE_WATER)
				&& (CMath.bset(material(), RawMaterial.MATERIAL_WOODEN)))
				{
					// this is for the small rideable boats
					final StringBuilder visualCondition = new StringBuilder("");
					if(this.subjectToWearAndTear() && (usesRemaining() <= 100))
					{
						final double pct=(CMath.div(usesRemaining(),100.0));
						GenSailingShip.appendCondition(visualCondition,pct,CMStrings.capitalizeFirstLetter(name(msg.source())));
					}
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							msg.source().tell(visualCondition.toString());
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
			if((rideBasis()==Rideable.RIDEABLE_LADDER)
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
					case Rideable.RIDEABLE_SIT:
					case Rideable.RIDEABLE_ENTERIN:
						R.basePhyStats().setDisposition(R.basePhyStats().disposition()|PhyStats.IS_SITTING);
						break;
					case Rideable.RIDEABLE_SLEEP:
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
					if(isHit && CMLib.combat().isAShipSiegeWeapon(weapon) 
					&& (((AmmunitionWeapon)weapon).ammunitionCapacity() > 1))
					{
						int shotsRemaining = ((AmmunitionWeapon)weapon).ammunitionRemaining() + 1;
						((AmmunitionWeapon)weapon).setAmmoRemaining(0);
						ArrayList<Pair<MOB,Room>> targets = new ArrayList<Pair<MOB,Room>>(5);
						final Room R=CMLib.map().roomLocation(this);
						if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
						{
							for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
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
									double pctLoss = CMath.div(msg.value(), phyStats().level());
									int pointsLost = (int)Math.round(pctLoss * msg.source().maxState().getHitPoints());
									CMLib.combat().postWeaponDamage(msg.source(), randomPair.first, weapon, pointsLost);
								}
								else
								if(randomPair != null)
								{
									msg.source().setLocation(randomPair.second);
									CMLib.combat().postWeaponAttackResult(msg.source(), randomPair.first, weapon, false);
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
						CMLib.combat().postShipWeaponAttackResult(msg.source(), msg.source().riding(), this, weapon, isHit);
				}
			}
			break;
		case CMMsg.TYP_DAMAGE:
			if((msg.target()==this) && (msg.value() > 0))
			{
				int level = phyStats().level();
				if(level < 10)
					level = 10;
				double pctLoss = CMath.div(msg.value(), level) * 10.0; // siege weapons against rideables is harsh
				int pointsLost = (int)Math.round(pctLoss * level);
				if(pointsLost > 0)
				{
					int weaponType = (msg.tool() instanceof Weapon) ? ((Weapon)msg.tool()).weaponDamageType() : Weapon.TYPE_BASHING;
					final String hitWord = CMLib.combat().standardHitWord(weaponType, pctLoss);
					final String msgStr = (msg.targetMessage() == null) ? L("<O-NAME> fired from <S-NAME> hits and @x1 the ship.",hitWord) : msg.targetMessage();
					final CMMsg deckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, msgStr);
					final Room targetRoom=CMLib.map().roomLocation(this);
					if(targetRoom.okMessage(msg.source(), deckHitMsg))
						targetRoom.send(msg.source(), deckHitMsg);
					final CMMsg underdeckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, L("Something hits and @x1 the ship.",hitWord));
					if(targetRoom.okMessage(msg.source(), underdeckHitMsg))
						targetRoom.send(msg.source(), underdeckHitMsg);
					if(pointsLost >= this.usesRemaining())
					{
						this.setUsesRemaining(0);
						this.recoverPhyStats(); // takes away the swimmability!
						final Room shipR=CMLib.map().roomLocation(this);
						if(shipR!=null)
						{
							CMLib.tracking().makeSink(this, shipR, false);
							final String sinkString = L("<T-NAME> start(s) sinking!");
							shipR.show(msg.source(), this, CMMsg.MSG_OK_ACTION, sinkString);
						}
						if(!CMLib.leveler().postExperienceToAllAboard(msg.source().riding(), 500))
							CMLib.leveler().postExperience(msg.source(), null, null, 500, false);
					}
					else
					{
						this.setUsesRemaining(this.usesRemaining() - pointsLost);
					}
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
