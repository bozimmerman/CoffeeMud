package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_FindDirections extends Spell
{
	public String ID() { return "Spell_FindDirections"; }
	public String name(){return "Find Directions";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return Ability.CAN_AREAS;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room targetR=mob.location();
		if(targetR==null)
			return false;
		
		if((commands.size()>0)
		&&(commands.firstElement() instanceof String)
		&&(((String)commands.firstElement()).toLowerCase().startsWith("direction")))
			commands.remove(0);
		Area A=null;
		if(commands.size()>0)
		{
			A=CMLib.map().findArea(CMParms.combine(commands));
			if(A!=null)
			{
				if(!CMLib.flags().canAccess(mob, A))
					A=null;
				else
				{
					boolean foundOne=false;
					for(int i=0;i<10;i++)
						if(CMLib.flags().canAccess(mob, A.getRandomProperRoom()))
						{
							foundOne=true;
							break;
						}
					if(!foundOne)
						A=null;
				}
			}
		}
		
		if(A==null)
		{
			mob.tell("You know of nowhere called \""+CMParms.combine(commands)+"\".");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		boolean success=proficiencyCheck(mob,0,auto);
		if(success && (targetR != null) )
		{
			CMMsg msg=CMClass.getMsg(mob,targetR,this,somanticCastCode(mob,targetR,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around, pointing towards '"+A.name()+"'.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell("The directions are taking shape in your mind: \n\r" +
					CMLib.tracking().getTrailToDescription(targetR, new Vector<Room>(), A.Name(), false, false, 100, null,1));
			}
		}
		else
			beneficialVisualFizzle(mob,targetR,"<S-NAME> wave(s) <S-HIS-HER> hands around, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
