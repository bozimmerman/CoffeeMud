package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_MysticShine extends Spell
{
	public String ID() { return "Spell_MysticShine"; }
	public String name(){return "Mystic Shine";}
	public String displayText(){return "(Mystic Shine)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(!(affected instanceof Room))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		if(Sense.isInDark(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null) return;
		Room room=CoffeeUtensils.roomLocation(affected);
		if((canBeUninvoked())&&(room!=null))
			room.showHappens(CMMsg.MSG_OK_VISUAL,affected,"The glow within <S-NAME> dims.");
		super.unInvoke();
		if((canBeUninvoked())&&(room!=null))
			room.recoverRoomStats();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=super.getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;
		if((!(target instanceof Item))
        ||(((((Item)target).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL)
            &&((((Item)target).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_MITHRIL)))
        {
		    mob.tell("This magic only affects metallic items.");
		    return false;
        }

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"^S<T-NAME> begin(s) to really shine!":"^S<S-NAME> invoke(s) a bright shine upon the surface of <T-NAMESELF>!^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			mob.location().recoverRoomStats(); // attempt to handle followers
		}
		else
			beneficialWordsFizzle(mob,mob.location(),"<S-NAME> attempt(s) to invoke shininess, but fail(s).");

		return success;
	}
}
