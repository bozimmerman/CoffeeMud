package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Dance_Tap extends Dance
{
	public String ID() { return "Dance_Tap"; }
	public String name(){ return "Tap";}
	public int quality(){ return MALICIOUS;}
	protected String danceOf(){return name()+" Dance";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if(mob!=invoker())
		{
			mob.curState().adjMovement(-(prancerLevel()/3),mob.maxState());
			mob.curState().adjMana(-prancerLevel(),mob.maxState());
		}
		return true;
	}
}
