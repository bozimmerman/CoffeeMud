package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_MassFungalGrowth extends Chant_SummonFungus
{
	public String ID() { return "Chant_MassFungalGrowth"; }
	public String name(){ return "Mass Fungal Growth";}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Vector V=new Vector();
		MUDTracker.getRadiantRooms(mob.location(),V,false,false,true,null,adjustedLevel(mob,asLevel));
		for(int v=V.size()-1;v>=0;v--)
		{
			Room R=(Room)V.elementAt(v);
			if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
			||(R==mob.location()))
				V.removeElementAt(v);
		}
		if(V.size()>0)
		{
			mob.location().show(mob,null,CMMsg.MASK_GENERAL|CMMsg.TYP_NOISE,"The faint sound of fungus popping into existence can be heard.");
			int done=0;
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				if(R==mob.location()) continue;
				buildMyPlant(mob,R);
				if((done++)==adjustedLevel(mob,asLevel))
					break;
			}
		}

		return true;
	}
}
