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
public class FaithHelper extends StdBehavior
{
	public String ID(){return "FaithHelper";}

	protected boolean mobKiller=false;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
				((MOB)forMe).setWorshipCharID(parms.trim());
		}
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

		if((target==null)||(observer==null)) return;
		if((source!=observer)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(target!=observer)
		&&(source!=target)
		&&(observer.getWorshipCharID().length()>0)
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(target,observer))
		&&(!BrotherHelper.isBrother(source,observer)))
		{
			if(observer.getWorshipCharID().equalsIgnoreCase(target.getWorshipCharID()))
			{
				boolean yep=Aggressive.startFight(observer,source,true);
				String reason="THAT`S MY FRIEND!! CHARGE!!";
				if((observer.getWorshipCharID().equals(target.getWorshipCharID()))
				&&(!observer.getWorshipCharID().equals(source.getWorshipCharID())))
					reason="BELIEVERS OF "+observer.getWorshipCharID().toUpperCase()+" UNITE! CHARGE!";
				if(yep)	CommonMsgs.say(observer,null,reason,false,false);
			}
		}
	}
}
