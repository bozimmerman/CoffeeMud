package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class TaxiBehavior extends Concierge
{
	@Override
	public String ID()
	{
		return "TaxiBehavior";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ITEMS | Behavior.CAN_MOBS;
	}

	protected volatile Ability	isEnRouter		= null;
	protected Room				returnToRoom	= null;
	protected Room				destRoom		= null;
	protected MOB				riderM			= null;
	protected List<Room>		trailTo			= null;
	protected List<Rider>		defaultRiders	= null;

	@Override
	public String accountForYourself()
	{
		return "taking you from here to there";
	}

	@Override
	protected String getGiveMoneyMessage(Environmental observer, Environmental destination, String moneyName)
	{
		if(observer instanceof MOB)
			return L("I can take you to @x1, but you'll need to give me @x2 first.",getDestinationName(destination),moneyName);
		else
		if(observer instanceof Container)
			return L("I can take you to @x1, but you'll need to put @x2 into @x3 first.",getDestinationName(destination),moneyName,observer.name());
		else
			return L("I can take you to @x1, but you'll need to drop @x2 first.",getDestinationName(destination),moneyName);
	}
	
	@Override
	protected void giveMerchandise(MOB whoM, Room destR, Environmental observer, Room room, TrackingFlags trackingFlags)
	{
		MOB fromM=getTalker(observer,room);
		final ArrayList<Room> set=new ArrayList<Room>();
		CMLib.tracking().getRadiantRooms(fromM.location(),set,roomRadiusFlags,null,maxRange,null);
		trailTo=CMLib.tracking().findTrailToRoom(fromM.location(), destR, trackingFlags, maxRange);
		thingsToSay.addElement(whoM,L("OK, we're now on our way to @x1.",getDestinationName(destR)));
		this.returnToRoom=fromM.location();
		this.isEnRouter=CMClass.getAbility("Prop_Adjuster");
		this.isEnRouter.setMiscText("sen+"+PhyStats.CAN_NOT_TRACK);
		this.destRoom = destR;
		this.riderM = whoM;
		if(observer instanceof Affectable)
			((Affectable)observer).addNonUninvokableEffect(this.isEnRouter);
	}
	
	@Override
	protected boolean disableComingsAndGoings()
	{
		return (isEnRouter!=null);
	}

	@Override
	protected final MOB getTalker(Environmental o, Room room)
	{
		if(o instanceof Rideable)
		{
			if(defaultRiders == null)
				defaultRiders = new XVector<Rider>(((Rideable)o).riders());
		}
		return super.getTalker(o, room);
	}
	
	private void endTheRide(Environmental observer)
	{
		if(this.isEnRouter != null)
		{
			final Room room=CMLib.map().roomLocation(observer);
			MOB conciergeM=this.getTalker(observer,room);
			if(room==this.destRoom)
				CMLib.commands().postSay(conciergeM,null,L("We're here.  Best of luck!."),false,false);
			else
				CMLib.commands().postSay(conciergeM,null,L("This is as far as I can go.  Best of luck!."),false,false);
			Rideable rideable = null;
			if(observer instanceof Rideable)
				rideable = (Rideable)observer;
			else
			if((observer instanceof Rider)&&(((Rider)observer).riding()!=null))
				rideable=((Rider)observer).riding();
			if((rideable!=null)&&(room!=null))
			{
				MOB mob=this.getTalker(observer, room);
				for(final Enumeration<Rider> r = rideable.riders(); r.hasMoreElements(); )
				{
					final Rider rider=r.nextElement();
					if(!defaultRiders.contains(rider))
					{
						if(rider instanceof MOB)
							room.show((MOB)rider, rideable, mob, CMMsg.MASK_ALWAYS|CMMsg.MSG_DISMOUNT, L("<S-NAME> @x1 from <T-NAME>.",rideable.dismountString(rider)));
						else
							room.show(mob, rideable, rider, CMMsg.MASK_ALWAYS|CMMsg.MSG_DISMOUNT, L("<S-NAME> help(s) <O-NAME> off of <T-NAME>."));
						rider.setRiding(null);
					}
				}
			}
			if(returnToRoom != null)
			{
				if(observer instanceof MOB)
					CMLib.tracking().wanderFromTo((MOB)observer, returnToRoom, false );
				else
				if((observer instanceof Item)&&(room != null))
				{
					room.showHappens(CMMsg.MSG_OK_ACTION, L("@x1 heads off.",observer.name()));
					returnToRoom.moveItemTo((Item)observer);
				}
			}
			if(isEnRouter != null)
			{
				if(isEnRouter.affecting() != null)
					isEnRouter.affecting().delEffect(isEnRouter);
			}
			isEnRouter = null;
			returnToRoom = null;
			destRoom = null;
			trailTo= null;
			riderM = null;
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking instanceof Environmental) && (isEnRouter != null))
		{
			final Environmental observer=(Environmental)ticking;
			if((ticking instanceof MOB) && (!super.canFreelyBehaveNormal(ticking)))
				endTheRide(observer);
			else
			{
				final Room locR=CMLib.map().roomLocation(observer);
				if(locR==destRoom)
					endTheRide(observer);
				else
				if(locR!=null)
				{
					final int nextDirection=CMLib.tracking().trackNextDirectionFromHere(trailTo, locR, true);
					final Room nextR=locR.getRoomInDir(nextDirection);
					final Exit nextE=locR.getExitInDir(nextDirection);
					if((nextR != null) && (nextE != null) && (nextE.isOpen()))
					{
						if((trackingFlags.contains(TrackingFlag.OUTDOORONLY))&&((nextR.domainType()&Room.INDOORS)!=0))
							endTheRide(observer);
						else
						if(observer instanceof MOB)
						{
							if(!CMLib.tracking().walk((MOB)observer, nextDirection,false,false))
								endTheRide(observer);
						}
						else
						if(observer instanceof Item)
						{
							if(!CMLib.tracking().walk((Item)observer, nextDirection))
								endTheRide(observer);
						}
					}
					else
						endTheRide(observer);
				}
				else
					endTheRide(observer);
			}
		}
		return true;
	}
	
	@Override
	protected void resetDefaults()
	{
		super.resetDefaults();
		greeting="Need a lift? If so, come aboard.";
		mountStr=L("Where are you headed?");
		isEnRouter = null;
		returnToRoom = null;
		destRoom = null;
		trailTo= null;
		riderM = null;
		basePrice=10.0;
		perRoomPrice=1.0;
	}
	
	@Override
	public void startBehavior(PhysicalAgent behaving)
	{
		super.startBehavior(behaving);
		if((talkerName.length()==0) && (behaving instanceof Item))
			talkerName="the driver";
	}
	
	@Override
	protected void resetFlags()
	{
		trackingFlags = CMLib.tracking().newFlags().plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		trackingFlags.plus(TrackingFlag.OPENONLY);
		trackingFlags.plus(TrackingFlag.OUTDOORONLY);
		roomRadiusFlags = CMLib.tracking().newFlags();
		roomRadiusFlags.plus(TrackingFlag.OPENONLY);
		trackingFlags.plus(TrackingFlag.OUTDOORONLY);
	}
	
	@Override
	public void setParms(String newParm)
	{
		super.setParms(newParm);
		if(roomRadiusFlags.contains(TrackingFlag.AREAONLY))
			trackingFlags.add(TrackingFlag.AREAONLY);
	}
}
