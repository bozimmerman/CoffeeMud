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
public class MOBHelper extends StdBehavior
{
	public String ID(){return "MOBHelper";}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB mob=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB monster=(MOB)affecting;
		if(msg.target()==null)
			return;
		if(!(msg.target() instanceof MOB))
			return;
		MOB target=(MOB)msg.target();

		if((mob!=monster)
		&&(target!=monster)
		&&(mob!=target)
		&&(Sense.canBeSeenBy(mob,monster))
		&&(Sense.canBeSeenBy(target,monster))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(target.isMonster()))
			Aggressive.startFight(monster,mob,true);
	}
}
