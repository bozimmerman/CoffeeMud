package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class ClimbableSurface extends StdRoom
{
	public String ID(){return "ClimbableSurface";}
	public ClimbableSurface()
	{
		super();
		name="the surface";
		baseEnvStats.setWeight(4);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_ROCKS;
		domainCondition=Room.CONDITION_NORMAL;
	}


	public void mountLadder(MOB mob, Rideable ladder)
	{
		String mountStr=ladder.mountString(CMMsg.TYP_MOUNT,mob);
		FullMsg msg=new FullMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room) room=null;
		if((mob.location().okMessage(mob,msg))
		&&((room==null)||(room.okMessage(mob,msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	}

	public Rideable findALadder(MOB mob, Room room)
	{
		if(room==null) return null;
		if(mob.riding()!=null) return null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(Sense.canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(Sense.isSleeping(this))
			return true;

		if(msg.amITarget(this)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MOVE))
		&&(!Sense.isFalling(msg.source()))
		&&(msg.sourceMinor()!=CMMsg.TYP_RECALL)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(!(msg.tool() instanceof Ability))||(!Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		&&(!Sense.isClimbing(msg.source()))
		&&(!Sense.isInFlight(msg.source())))
		{
			Rideable ladder=findALadder(msg.source(),this);
			if(ladder!=null)
				mountLadder(msg.source(),ladder);
			if((!Sense.isClimbing(msg.source()))
			&&(!Sense.isFalling(msg.source())))
			{
				msg.source().tell("You need to climb that way, if you know how.");
				return false;
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(Sense.isSleeping(this)) return;

		if((msg.targetMinor()==CMMsg.TYP_THROW)
		&&(CoffeeUtensils.roomLocation(msg.target())==this)
		&&(msg.tool() instanceof Item)
		&&((!(msg.tool() instanceof Rideable))
		   ||(((Rideable)msg.tool()).rideBasis()!=Rideable.RIDEABLE_LADDER))
		&&(!Sense.isFlying(msg.tool())))
			InTheAir.makeFall(msg.tool(),this,0);
		else
		if((msg.targetMinor()==CMMsg.TYP_DROP)
		&&(msg.target() instanceof Item)
		&&((!(msg.target() instanceof Rideable))
		   ||(((Rideable)msg.target()).rideBasis()!=Rideable.RIDEABLE_LADDER))
		&&(!Sense.isFlying(msg.target())))
			InTheAir.makeFall(msg.target(),this,0);
		else
		if(msg.amITarget(this)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MOVE))
		&&(!Sense.isFalling(msg.source())))
		{
			MOB mob=msg.source();
			if(isInhabitant(mob))
			{
				if((!Sense.isInFlight(mob))
				&&(!Sense.isClimbing(mob))
				&&(getRoomInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN).isOpen()))
				{
					Rideable ladder=findALadder(mob,this);
					if(ladder!=null)
						mountLadder(mob,ladder);
					if(!Sense.isClimbing(mob))
					{
						ladder=findALadder(mob,getRoomInDir(Directions.DOWN));
						if(ladder!=null)
						{
							CommonMsgs.look(mob,false);
							mountLadder(mob,ladder);
						}
						if(Sense.isClimbing(mob))
							MUDTracker.move(mob,Directions.DOWN,false,true);
						else
							InTheAir.makeFall(mob,this,0);
					}
				}
			}
		}
	}
}
