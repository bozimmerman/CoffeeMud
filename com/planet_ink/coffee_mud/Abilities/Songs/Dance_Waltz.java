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
public class Dance_Waltz extends Dance
{
	public String ID() { return "Dance_Waltz"; }
	public String name(){ return "Waltz";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	private int[] statadd=null;

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		if(statadd==null)
		{
			statadd=new int[CharStats.NUM_BASE_STATS];
			int classLevel=CMAble.qualifyingClassLevel(invoker(),this);
			classLevel=(classLevel+1)/9;
			classLevel++;

			for(int i=0;i<classLevel;i++)
				statadd[Dice.roll(1,CharStats.NUM_BASE_STATS,-1)]+=3;
		}
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			affectedStats.setStat(i,affectedStats.getStat(i)+statadd[i]);
	}

}
