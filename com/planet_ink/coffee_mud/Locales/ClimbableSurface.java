package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
	public Environmental newInstance()
	{
		return new ClimbableSurface();
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

		if((msg.target() instanceof Item)
		&&((!(msg.target() instanceof Rideable))
		   ||(((Rideable)msg.target()).rideBasis()!=Rideable.RIDEABLE_LADDER))
		&&(!Sense.isFlying(msg.target()))
		&&((msg.targetMinor()==CMMsg.TYP_DROP)
			||((msg.targetMinor()==CMMsg.TYP_THROW)
			   &&(msg.tool()!=null)
			   &&(msg.tool()==this))))
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
