package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class VeryAggressive extends Aggressive
{
	public String ID(){return "VeryAggressive";}
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}
	protected int tickWait=0;
	protected int tickDown=0;
	public Behavior newInstance()
	{
		return new VeryAggressive();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=Util.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		return MUDZapper.zapperCheck(getParms(),M);
	}

	public static void tickVeryAggressively(Tickable ticking,
											int tickID,
											boolean wander,
											boolean mobKiller,
											String zapStr)
	{
		if(tickID!=MudHost.TICK_MOB) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;

		// ridden things dont wander!
		if(ticking instanceof Rideable)
			if(((Rideable)ticking).numRiders()>0)
				return;

		if(((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location()))
		||(!Sense.canTaste(mob)))
		   return;

		// let's not do this 100%
		if(Dice.rollPercentage()>15) return;

		Room thisRoom=mob.location();
		for(int m=0;m<thisRoom.numInhabitants();m++)
		{
			MOB inhab=thisRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(CMSecurity.isAllowed(inhab,thisRoom,"ORDER"))
			&&(CMSecurity.isAllowed(inhab,thisRoom,"CMROOMS")))
				return;
		}

		int dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=thisRoom.getRoomInDir(d);
			Exit exit=thisRoom.getExitInDir(d);
			if((room!=null)
			   &&(exit!=null)
			   &&(wander||room.getArea().Name().equals(thisRoom.getArea().Name())))
			{
				if(exit.isOpen())
				{
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if((inhab!=null)
						&&((!inhab.isMonster())||(mobKiller))
						&&(Sense.canSenseMoving(inhab,mob))
						&&(MUDZapper.zapperCheck(zapStr,inhab))
						&&((zapStr.length()==0)
						   ||((inhab.envStats().level()<(mob.envStats().level()+15))
							  &&(inhab.envStats().level()>(mob.envStats().level()-15)))))
						{
							dirCode=d;
							break;
						}
					}
				}
			}
			if(dirCode>=0) break;
		}
		if((dirCode>=0)
		&&(!CMSecurity.isDisabled("MOBILITY")))
		{
			MUDTracker.move(mob,dirCode,false,false);
			pickAFight(mob,zapStr,mobKiller);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickVeryAggressively(ticking,tickID,
								 (getParms().toUpperCase().indexOf("WANDER")>=0),
								 (getParms().toUpperCase().indexOf("MOBKILL")>=0),
								 getParms());
		}
		return true;
	}
}
