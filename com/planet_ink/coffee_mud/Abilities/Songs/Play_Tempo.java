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
public class Play_Tempo extends Play
{
	public String ID() { return "Play_Tempo"; }
	public String name(){ return "Tempo";}
	public int quality(){ return BENEFICIAL_OTHERS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			for(int i=0;i<mob.numAllEffects();i++)
			{
				Ability A=mob.fetchEffect(i);
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
					A.tick(mob,MudHost.TICK_MOB);
			}
		}
		return true;
	}
}
