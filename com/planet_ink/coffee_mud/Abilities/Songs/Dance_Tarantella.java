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
public class Dance_Tarantella extends Dance
{
	public String ID() { return "Dance_Tarantella"; }
	public String name(){ return "Tarantella";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	private int ticks=1;
	protected String danceOf(){return name()+" Dance";}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+(CMAble.qualifyingClassLevel(invoker(),this)*2));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((++ticks)>=15)
		{
			Vector offenders=null;
			for(int a=0;a<mob.numEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&(A.classificationCode()==Ability.POISON))
				{
					if(offenders==null) offenders=new Vector();
					offenders.addElement(A);
				}
			}
			if(offenders!=null)
				for(int a=0;a<offenders.size();a++)
					((Ability)offenders.elementAt(a)).unInvoke();
		}

		return true;
	}

}
