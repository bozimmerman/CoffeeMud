package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Song_Distraction extends Song
{
	public String ID() { return "Song_Distraction"; }
	public String name(){ return "Distraction";}
	public int quality(){ return MALICIOUS;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&(mob.isInCombat())
			&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+50))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appear(s) distracted by the singing.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

}
