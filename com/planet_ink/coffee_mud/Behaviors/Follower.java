package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Follower extends StdBehavior
{
	public String ID(){return "Follower";}
    private int followChance = 100;
	private boolean realFollow=false;
	private int lastNumPeople=-1;
	
	public Follower()
	{
		direction=-1;
	}
	
    public void setParms(String newParms) 
	{
        super.setParms(newParms);
        followChance = Util.getParmInt(newParms, "chance", 100);
		realFollow=Util.parse(newParms.toUpperCase()).contains("GROUP");
    }
	

	int direction=-1;

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);

		if((!canFreelyBehaveNormal(affecting))||(realFollow)) return;

		MOB mob=msg.source();
		if(mob.amDead()) return;
		if(mob.location()==null) return;


		if((direction<0)
		&&(msg.amITarget(((MOB)affecting).location()))
		&&(Sense.canBeSeenBy(mob,(MOB)affecting))
		&&(msg.othersMessage()!=null)
		&&((msg.targetMinor()==CMMsg.TYP_LEAVE)
		 ||(msg.targetMinor()==CMMsg.TYP_FLEE))
        &&(followChance>Dice.rollPercentage())
		&&(MUDZapper.zapperCheck(getParms(),mob)))
		{
			String directionWent=msg.othersMessage();
			int x=directionWent.lastIndexOf(" ");
			if(x>=0)
			{
				directionWent=directionWent.substring(x+1);
				direction=Directions.getDirectionCode(directionWent);
			}
			else
				direction=-1;
		}

	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=MudHost.TICK_MOB) return true;
		if((realFollow)&&(ticking instanceof MOB))
		{
			if(!canFreelyBehaveNormal(ticking)) 
				return true;
			MOB mob=(MOB)ticking;
			Room room=mob.location();
			if(room.numInhabitants()!=lastNumPeople)
			{
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if((M!=null)
					&&(M!=ticking)
					&&(!CMSecurity.isAllowed(M,room,"CMDMOBS"))
					&&(!CMSecurity.isAllowed(M,room,"CMDROOMS"))
					&&(MUDZapper.zapperCheck(getParms(),mob))
					&&(followChance>Dice.rollPercentage()))
					{
						CommonMsgs.follow(mob,M,false);
						break;
					}
				}
				lastNumPeople=room.numInhabitants();
			}
		}
		else
		if((direction>=0)&&(ticking instanceof MOB))
		{
			if(!canFreelyBehaveNormal(ticking)) 
				return true;
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			Room otherRoom=(Room)thisRoom.getRoomInDir(direction);

			if(otherRoom!=null)
			{
				if(!otherRoom.getArea().Name().equals(thisRoom.getArea().Name()))
					direction=-1;
			}
			else
				direction=-1;

			if(direction<0)
				return true;


			boolean move=true;
			for(int m=0;m<thisRoom.numInhabitants();m++)
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)
				&&(CMSecurity.isAllowed(inhab,thisRoom,"CMDMOBS")
				   ||CMSecurity.isAllowed(inhab,thisRoom,"CMDROOMS")))
					move=false;
			}
			if(move)
				MUDTracker.move(mob,direction,false,false);
			direction=-1;
		}
		return true;
	}
}
