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
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if(!super.okMessage(oking,msg)) return false;
		MOB mob=msg.source();
        MOB monster=(MOB)oking;
        if(parms.toUpperCase().indexOf("SENTINAL")>=0)
        {
            if(!canActAtAll(monster)) return true;
            if(monster.amFollowing()!=null)  return true;
            if(monster.curState().getHitPoints()<((int)Math.round(monster.maxState().getHitPoints()/4.0)))
                return true;
        }
        else
		if(!canFreelyBehaveNormal(oking))
			return true;

		if((mob!=monster)
		&&((msg.sourceMinor()==CMMsg.TYP_GET)
		||((msg.sourceMinor()==CMMsg.TYP_THROW)&&(monster.location()==msg.tool()))
		||(msg.sourceMinor()==CMMsg.TYP_DROP)))
		{
			FullMsg msgs=new FullMsg(monster,mob,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> won't let <T-NAME> touch that.");
			if(monster.location().okMessage(monster,msgs))
			{
				monster.location().send(monster,msgs);
				return false;
			}
		}
		return true;
	}
}
