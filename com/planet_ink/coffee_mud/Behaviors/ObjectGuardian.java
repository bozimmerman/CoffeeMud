package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class ObjectGuardian extends StdBehavior
{
	public String ID(){return "ObjectGuardian";}


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
