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
public class Song_Disgust extends Song
{
	public String ID() { return "Song_Disgust"; }
	public String name(){ return "Disgust";}
	public int quality(){ return MALICIOUS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		if(invoker==null) return true;
		Room room=invoker.location();
		if((!mob.isInCombat())&&(room!=null))
		{
			MOB newMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
			if(newMOB!=mob)
			{
				room.show(mob,newMOB,CMMsg.MSG_OK_ACTION,"<S-NAME> appear(s) disgusted with <T-NAMESELF>.");
				MUDFight.postAttack(mob,newMOB,mob.fetchWieldedItem());
			}
		}
		return true;
	}

}
