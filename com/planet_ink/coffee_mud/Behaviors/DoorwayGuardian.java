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
		if(affect.target()==null) return true;
		if(!Sense.canBeSeenBy(affect.source(),oking))
			return true;
		if(affect.target() instanceof Exit)
		{
			Exit exit=(Exit)affect.target();
			if(!exit.hasADoor()) return true;

			if(affect.targetMinor()!=Affect.TYP_CLOSE)
			{
				FullMsg msgs=new FullMsg(monster,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> leave.");
				if(monster.location().okAffect(msgs))
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
			if(!exit.hasADoor())
				return true;
			FullMsg msgs=new FullMsg(monster,mob,Affect.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> leave.");
			if(monster.location().okAffect(msgs))
			{
				monster.location().send(monster,msgs);
				return false;
			}
		}
		return true;
	}
}