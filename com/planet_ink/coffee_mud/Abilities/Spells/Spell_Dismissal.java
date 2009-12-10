package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Dismissal extends Spell
{
	public String ID() { return "Spell_Dismissal"; }
	public String name(){return "Dismissal";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_MOVING|Ability.FLAG_TRANSPORTING;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if((target instanceof MOB)
        &&(((MOB)target).amFollowing()==mob)
        &&(((MOB)target).isMonster()))
            return Ability.QUALITY_INDIFFERENT;
        return super.castingQuality(mob,target);
    }
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		if(target.getStartRoom()==null)
		{
			int levelDiff=target.envStats().level()-(mob.envStats().level()+(2*getXLEVELLevel(mob)));
			if(levelDiff<0) levelDiff=0;
			success=proficiencyCheck(mob,-(levelDiff*5),auto);
		}
		else
			success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive spell!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(target.getStartRoom()==null)
						target.destroy();
					else
					{
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> vanish(es) in dismissal!");
						target.getStartRoom().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> appear(s)!");
						target.getStartRoom().bringMobHere(target,false);
						CMLib.commands().postLook(target,true);
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and utter(s) a dismissive but fizzled spell!");


		// return whether it worked
		return success;
	}
}
