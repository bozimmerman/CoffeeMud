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

public class DoorwayGuardian extends StdBehavior
{
	public DoorwayGuardian()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{ 
		return new DoorwayGuardian();
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
		if(affect.target() instanceof Exit)
		{
			Exit exit=(Exit)affect.target();
			if(!exit.hasADoor()) return true;
			
			if(affect.targetCode()!=Affect.HANDS_CLOSE)
			{
				mob.tell(monster.name()+" won't let you.");
				return false;
			}
		}
		else
		if((affect.tool()!=null)
		&&(affect.target() instanceof Room)
		&&(affect.tool() instanceof Exit))
		{
			Exit exit=(Exit)affect.tool();
			if(!exit.hasADoor()) 
				return true;
			mob.tell(monster.name()+" won't let you.");
			return false;
		}
		return true;
	}
}