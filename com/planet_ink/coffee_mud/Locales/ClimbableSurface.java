package com.planet_ink.coffee_mud.Locales;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
	@Override public String ID(){return "ClimbableSurface";}
	public ClimbableSurface()
	{
		super();
		name="the surface";
		basePhyStats.setWeight(4);
		recoverPhyStats();
	}
	@Override public int domainType(){return Room.DOMAIN_OUTDOORS_ROCKS;}

	public void mountLadder(MOB mob, Rideable ladder)
	{
		String mountStr=ladder.mountString(CMMsg.TYP_MOUNT,mob);
		CMMsg msg=CMClass.getMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
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
			Item I=room.getItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(CMLib.flags().canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(CMLib.flags().isSleeping(this))
			return true;

		if(msg.amITarget(this)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE))
		&&(!CMLib.flags().isFalling(msg.source()))
		&&(msg.sourceMinor()!=CMMsg.TYP_RECALL)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(!(msg.tool() instanceof Ability))||(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		&&(!CMLib.flags().isClimbing(msg.source()))
		&&(!CMLib.flags().isInFlight(msg.source())))
		{
			Rideable ladder=findALadder(msg.source(),this);
			if(ladder!=null)
				mountLadder(msg.source(),ladder);
			if((!CMLib.flags().isClimbing(msg.source()))
			&&(!CMLib.flags().isFalling(msg.source())))
			{
				msg.source().tell("You need to climb that way, if you know how.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(CMLib.flags().isSleeping(this)) return;

		if((msg.sourceMinor()==CMMsg.TYP_THROW)
		&&(CMLib.map().roomLocation(msg.target())==this)
		&&(msg.tool() instanceof Item)
		&&((!(msg.tool() instanceof Rideable))
		   ||(((Rideable)msg.tool()).rideBasis()!=Rideable.RIDEABLE_LADDER))
		&&(!CMLib.flags().isFlying((Item)msg.tool())))
			InTheAir.makeFall((Item)msg.tool(),this,0);
		else
		if((msg.targetMinor()==CMMsg.TYP_DROP)
		&&(msg.target() instanceof Item)
		&&((!(msg.target() instanceof Rideable))
		   ||(((Rideable)msg.target()).rideBasis()!=Rideable.RIDEABLE_LADDER))
		&&(!CMLib.flags().isFlying((Item)msg.target())))
			InTheAir.makeFall((Item)msg.target(),this,0);
		else
		if(msg.amITarget(this)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE))
		&&(!CMLib.flags().isFalling(msg.source())))
		{
			MOB mob=msg.source();
			if(isInhabitant(mob))
			{
				if((!CMLib.flags().isInFlight(mob))
				&&(!CMLib.flags().isClimbing(mob))
				&&(getRoomInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN).isOpen()))
				{
					Rideable ladder=findALadder(mob,this);
					if(ladder!=null)
						mountLadder(mob,ladder);
					if(!CMLib.flags().isClimbing(mob))
					{
						ladder=findALadder(mob,getRoomInDir(Directions.DOWN));
						if(ladder!=null)
						{
							CMLib.commands().postLook(mob,false);
							mountLadder(mob,ladder);
						}
						if(CMLib.flags().isClimbing(mob))
							CMLib.tracking().walk(mob,Directions.DOWN,false,true);
						else
							InTheAir.makeFall(mob,this,0);
					}
				}
			}
		}
	}
}
