package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class GoodExecutioner  extends StdBehavior
{
	public String ID(){return "GoodExecutioner";}
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE;}


	public boolean grantsAggressivenessTo(MOB M)
	{
		if(M==null) return false;
		if(Sense.isBoundOrHeld(M)) return false;
		if(!M.isMonster()) return false;
		for(int b=0;b<M.numBehaviors();b++)
		{
			Behavior B=M.fetchBehavior(b);
			if((B!=null)&&(B.grantsAggressivenessTo(M)))
				return true;
		}
		return ((M.getAlignment()<350)||(M.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief")));
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting)) return;
		MOB observer=(MOB)affecting;
		// base 90% chance not to be executed
		if((source.isMonster())&&(source!=observer)&&(grantsAggressivenessTo(source)))
		{
			String reason="EVIL";
			if(source.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief"))
				reason="A THIEF";
			MOB oldFollowing=source.amFollowing();
			source.setFollowing(null);
			boolean yep=Aggressive.startFight(observer,source,true);
			if(yep)
				CommonMsgs.say(observer,null,source.name().toUpperCase()+" IS "+reason+", AND MUST BE DESTROYED!",false,false);
			else
			if(oldFollowing!=null)
				source.setFollowing(oldFollowing);
		}
	}
}
