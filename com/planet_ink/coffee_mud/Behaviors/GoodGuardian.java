package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class GoodGuardian extends StdBehavior
{
	
	public GoodGuardian()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{ 
		return new GoodGuardian();
	}
	
	public static void keepPeace(MOB mob)
	{
		if(!canBehave(mob)) return;
		MOB victim=null;
		boolean anythingToDo=false;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB inhab=mob.location().fetchInhabitant(i);
			if(inhab.isInCombat())
			{
				if((inhab.getAlignment()>350)&&(inhab.getVictim().getAlignment()<350))
				{
					victim=inhab.getVictim();
					break;
				}
				else
				if((mob.envStats().level()>(inhab.envStats().level()+5))&&(mob.getAlignment()>350))
					anythingToDo=true;
			}
		}
		
		
		
		if(victim!=null)
			mob.setVictim(mob);
		else
		if(anythingToDo)
		{
			boolean didSomething=true;
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB inhab=mob.location().fetchInhabitant(i);
				if((inhab.isInCombat())
				&&((mob.envStats().level()>(inhab.envStats().level()+5))
				&&(mob.getAlignment()>350)))
				{
					inhab.makePeace();
					didSomething=true;
				}
			}
			if(didSomething)
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> stop(s) the fight.");
		}
	}
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public boolean okAffect(Environmental oking, Affect affect)
	{
		if(!super.okAffect(oking,affect)) return false;
		MOB mob=affect.source();
		if(!canBehave(oking)) return true;
		MOB monster=(MOB)oking;
		if(affect.target()==null) return true;
		if(!(affect.target() instanceof MOB)) 
			return true;
		MOB target=(MOB)affect.target();
		
		if((mob!=monster)
		&&(Sense.canBeSeenBy(mob,monster))
		&&(Sense.canBeSeenBy(target,monster))
		&&(target.getAlignment()>650)
		&&((affect.targetType()==Affect.STRIKE)
		||(affect.targetType()==Affect.STRIKE)))
		{
			mob.tell(monster.name()+" won't let you.");
			return false;
		}
		return true;
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		if(tickID!=ServiceEngine.MOB_TICK) return;
		if(!canBehave(ticking)) return;
		MOB mob=(MOB)ticking;
		Room thisRoom=mob.location();
		keepPeace(mob);
		int dirCode=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=thisRoom.getRoom(d);
			Exit exit=thisRoom.getExit(d);
			if((room!=null)&&(exit!=null)&&(room.getAreaID().equals(thisRoom.getAreaID())))
			{
				if(exit.isOpen())
				{
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if(inhab.isInCombat()&&(inhab.getAlignment()>650))
						{
							dirCode=d;
							break;
						}
					}
				}
			}
			if(dirCode>=0) break;
		}
		if(dirCode>=0)
		{
			Movement.move(mob,dirCode,false);
			keepPeace(mob);
		}
	}
}
