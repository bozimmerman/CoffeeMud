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
public class BrotherHelper extends StdBehavior
{
	public String ID(){return "BrotherHelper";}

	protected boolean mobKiller=false;

	public static boolean isBrother(MOB target, MOB observer)
	{
		if((observer==null)||(target==null)) return false;
		if((observer.getStartRoom()!=null)&&(target.getStartRoom()!=null))
        {
			if (observer.getStartRoom() == target.getStartRoom())
				return true;
        }
        if((observer.ID().equals(target.ID()))
        &&(observer.name().equals(target.name())))
			return true;
		return false;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;
		if(msg.target()==null)
			return;
		if(!(msg.target() instanceof MOB))
			return;
		MOB target=(MOB)msg.target();

		Room R=source.location();
		if((source!=observer)
		&&(target!=observer)
		&&(source!=target)
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(isBrother(target,observer))
		&&(!isBrother(source,observer))
		&&(R!=null))
		{
			int numInFray=0;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.getVictim()==source))
					numInFray++;
			}
			int numAllowed=Util.s_int(getParms());
			boolean yep=true;
			if((numAllowed==0)||(numInFray<numAllowed))
				yep=Aggressive.startFight(observer,source,true);
			if(yep)	CommonMsgs.say(observer,null,"DON'T HURT MY FRIEND!",false,false);
		}
	}

}
