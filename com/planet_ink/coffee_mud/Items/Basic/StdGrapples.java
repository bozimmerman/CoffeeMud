package com.planet_ink.coffee_mud.Items.Basic;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.Items.Basic.StdRideable;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


/*
   Copyright 2016-2016 Bo Zimmerman

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
public class StdGrapples extends StdPortal
{
	public StdGrapples()
	{
		super();
		setName("a set of ship grapples");
		setDisplayText("a set of ship grapples lie here.");
		setDescription("They look like long metal chains with long curved hooks.");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(50);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		//basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.SENSE_ITEMNOTGET);
		baseGoldValue=15;
		recoverPhyStats();
		setRiderCapacity(0);
		material=RawMaterial.RESOURCE_IRON;
		properWornBitmap=0;
		wornLogicalAnd = false;
	}

	protected volatile Room sourceR = null;
	protected volatile Room targetR = null;
	
	@Override
	public int maxRange()
	{
		return 0;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_GET:
		case CMMsg.TYP_DROP:
			sourceR=null;
			targetR=null;
			break;
		case CMMsg.TYP_ENTER:
			break;
		case CMMsg.TYP_THROW:
		{
			final Room sourceRoom=CMLib.map().roomLocation(msg.source());
			if(msg.target() instanceof Room)
			{
				final Room targetRoom=(Room)msg.target();
				if((sourceRoom!=null)
				&&(targetRoom!=null)
				&&(sourceRoom.getArea()!=targetRoom.getArea())
				&&(sourceRoom.getArea() instanceof BoardableShip)
				&&(targetRoom.getArea() instanceof BoardableShip))
				{
					this.sourceR=sourceRoom;
					this.targetR=targetRoom;
				}
			}
			break;
		}
		}
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_SIT:
			if(msg.amITarget(this))
			{
				if(msg.sourceMessage().indexOf(mountString(CMMsg.TYP_SIT,msg.source()))>0)
				{
					if(getDestinationRoom(msg.source().location())==null)
					{
						return false;
					}
					msg.modify(msg.source(),msg.target(),msg.tool(),
							   msg.sourceMajor()|CMMsg.TYP_ENTER,msg.sourceMessage(),
							   msg.targetMajor()|CMMsg.TYP_ENTER,msg.targetMessage(),
							   msg.othersMajor()|CMMsg.TYP_ENTER,null);
					return true;
				}
				msg.source().tell(L("You cannot sit on @x1.",name()));
				return false;
			}
			break;
		}
		if(!super.okMessage(myHost,msg))
			return false;
		return true;
	}

	@Override
	protected Room getDestinationRoom(Room fromRoom)
	{
		if(fromRoom == sourceR)
			return targetR;
		else
		if(fromRoom == targetR)
			return sourceR;
		return null;
	}

	@Override
	public StringBuilder viewableText(MOB mob, Room myRoom)
	{
		Room room=this.getDestinationRoom(myRoom);
		if(room == null)
			room = myRoom;
		if(room==null)
			return new StringBuilder(this.displayText(mob));
		return super.viewableText(mob, myRoom);
	}

}
