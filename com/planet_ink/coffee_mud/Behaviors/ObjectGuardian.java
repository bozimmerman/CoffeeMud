package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ObjectGuardian extends StdBehavior
{
	public String ID(){return "ObjectGuardian";}
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
		if(!canFreelyBehaveNormal(oking)) return true;
		MOB monster=(MOB)oking;
		if((mob!=monster)
		&&((affect.sourceMinor()==Affect.TYP_GET)
		||((affect.sourceMinor()==Affect.TYP_THROW)&&(monster.location()==affect.tool()))
		||(affect.sourceMinor()==Affect.TYP_DROP)))
		{
			FullMsg msgs=new FullMsg(monster,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> touch that.");
			if(monster.location().okAffect(monster,msgs))
			{
				monster.location().send(monster,msgs);
				return false;
			}
		}
		return true;
	}
}
