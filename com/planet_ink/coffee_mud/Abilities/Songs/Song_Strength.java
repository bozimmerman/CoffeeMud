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
public class Song_Strength extends Song
{
	public String ID() { return "Song_Strength"; }
	public String name(){ return "Strength";}
	public int quality(){ return BENEFICIAL_OTHERS;}

	private int amount=0;

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker)
			affectableStats.setStat(CharStats.STRENGTH,(int)Math.round(affectableStats.getStat(CharStats.STRENGTH)-amount));
		else
			affectableStats.setStat(CharStats.STRENGTH,(int)Math.round(affectableStats.getStat(CharStats.STRENGTH)+amount));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		amount=Util.s_int(Util.combine(commands,0));

		if(amount<=0)
		{
			mob.tell("Sing about how much strength?");
			return false;
		}

		if(amount>=mob.charStats().getStat(CharStats.STRENGTH))
		{
			mob.tell("You can't sing away that much strength.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		return true;
	}
}
