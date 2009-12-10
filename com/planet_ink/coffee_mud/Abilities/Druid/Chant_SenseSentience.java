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
public class Chant_SenseSentience extends Chant
{
	public String ID() { return "Chant_SenseSentience"; }
	public String name(){return "Sense Sentience";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) softly to <S-HIM-HERSELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer lines=new StringBuffer("^x");
				lines.append(CMStrings.padRight("Name",25)+"| ");
				lines.append(CMStrings.padRight("Location",17)+"^.^N\n\r");
				TrackingLibrary.TrackingFlags flags;
				flags = new TrackingLibrary.TrackingFlags()
						.add(TrackingLibrary.TrackingFlag.AREAONLY);
				Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,50);
				if(!checkSet.contains(mob.location())) checkSet.addElement(mob.location());
				for(Enumeration r=checkSet.elements();r.hasMoreElements();)
				{
					Room R=CMLib.map().getRoom((Room)r.nextElement());
					if((((R.domainType()&Room.INDOORS)==0)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_CITY)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SPACEPORT))
					||(R==mob.location()))
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=2))
						{
							lines.append("^!"+CMStrings.padRight(M.name(),25)+"^?| ");
							lines.append(R.displayText());
							lines.append("\n\r");
						}
					}
				}
				mob.tell(lines.toString()+"^.");
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) softly to <S-HIM-HERSELF>, but the magic fades.");

		return success;
	}
}
