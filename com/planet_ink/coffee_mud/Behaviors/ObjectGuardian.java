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

public class ObjectGuardian extends StdBehavior
{
	public ObjectGuardian()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{ 
		return new ObjectGuardian();
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
		if((mob!=monster)
		&&((affect.sourceCode()==Affect.HANDS_GET)
		||(affect.sourceCode()==Affect.HANDS_DROP)))
		{
			mob.tell(monster.name()+" won't let you.");
			return false;
		}
		return true;
	}
}
