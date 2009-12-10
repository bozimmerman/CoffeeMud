package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant_MassFungalGrowth extends Chant_SummonFungus
{
	public String ID() { return "Chant_MassFungalGrowth"; }
	public String name(){ return "Mass Fungal Growth";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Vector V=new Vector();
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.OPENONLY)
				.add(TrackingLibrary.TrackingFlag.AREAONLY)
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.add(TrackingLibrary.TrackingFlag.NOAIR)
				.add(TrackingLibrary.TrackingFlag.NOWATER);
		CMLib.tracking().getRadiantRooms(mob.location(),V,flags,null,adjustedLevel(mob,asLevel),null);
		for(int v=V.size()-1;v>=0;v--)
		{
			Room R=(Room)V.elementAt(v);
			if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
			||(R==mob.location()))
				V.removeElementAt(v);
		}
		if(V.size()>0)
		{
			mob.location().show(mob,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_NOISE,"The faint sound of fungus popping into existence can be heard.");
			int done=0;
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				if(R==mob.location()) continue;
				buildMyThing(mob,R);
				if((done++)==adjustedLevel(mob,asLevel))
					break;
			}
		}

		return true;
	}
}
