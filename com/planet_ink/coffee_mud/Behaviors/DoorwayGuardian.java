package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DoorwayGuardian extends StdBehavior
{
	public String ID(){return "DoorwayGuardian";}
	public Behavior newInstance()
	{
		return new DoorwayGuardian();
	}

	public Exit[] getParmExits(MOB monster)
	{
		if(monster==null) return null;
		if(monster.location()==null) return null;
		if(getParms().length()==0) return null;
		Room room=monster.location();
		Vector V=Util.parse(getParms());
		for(int v=0;v<V.size();v++)
		{
			int dir=Directions.getGoodDirectionCode((String)V.elementAt(v));
			if(dir>=0)
				if(room.getExitInDir(dir)!=null)
				{
					Exit[] exits={room.getExitInDir(dir),room.getPairedExit(dir)};
					return exits;
				}
		}
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit E=room.getExitInDir(d);
			if((E!=null)&&(E.hasADoor()))
			{
				Exit[] exits={E,room.getPairedExit(d)};
				return exits;
			}
		}
		return null;
	}
	
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Environmental oking, Affect affect)
	{
		if(!super.okAffect(oking,affect)) return false;
		MOB mob=affect.source();
		if(!canFreelyBehaveNormal(oking)) return true;
		MOB monster=(MOB)oking;
		if((mob.location()==monster.location())
		&&(mob!=monster)
		&&(affect.target()!=null)
		&&(!BrotherHelper.isBrother(mob,monster))
		&&(Sense.canSenseMoving(mob,monster))
		&&(!SaucerSupport.zapperCheck(getParms(),mob)))
		{
			if(affect.target() instanceof Exit)
			{
				Exit exit=(Exit)affect.target();
				Exit texit[]=getParmExits(monster);
				if((texit!=null)
				&&(texit[0]!=exit)
				&&(texit[1]!=exit))
					return true;

				if((affect.targetMinor()!=Affect.TYP_CLOSE)
				&&(affect.targetMinor()!=Affect.TYP_LOCK))
				{
					FullMsg msgs=new FullMsg(monster,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> through there.");
					if(monster.location().okAffect(monster,msgs))
					{
						monster.location().send(monster,msgs);
						return false;
					}
				}
			}
			else
			if((affect.tool()!=null)
			&&(affect.target() instanceof Room)
			&&(affect.tool() instanceof Exit))
			{
				Exit exit=(Exit)affect.tool();
				Exit texit[]=getParmExits(monster);
				if((texit!=null)
				&&(texit[0]!=exit)
				&&(texit[1]!=exit))
					return true;
				
				FullMsg msgs=new FullMsg(monster,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> through there.");
				if(monster.location().okAffect(monster,msgs))
				{
					monster.location().send(monster,msgs);
					return false;
				}
			}
		}
		return true;
	}
}