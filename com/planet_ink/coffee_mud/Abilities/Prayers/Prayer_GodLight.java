package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_GodLight extends Prayer
{
	public String ID() { return "Prayer_GodLight"; }
	public String name(){ return "Godlight";}
	public String displayText(){return "(Godlight)";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
        if(CMath.bset(affectableStats.disposition(),EnvStats.IS_DARK))
            affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
		if(!(affected instanceof MOB)) return;
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your vision returns.");
	}


    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
            {
                if(CMLib.flags().isInDark(mob.location()))
                    return Ability.QUALITY_INDIFFERENT;
                if(target instanceof MOB)
                {
                    if(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0)
                        return Ability.QUALITY_INDIFFERENT;
                    if(!CMLib.flags().canSee((MOB)target))
                        return Ability.QUALITY_INDIFFERENT;
                }
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		Environmental target=null;
		if((!auto)
        &&((commands.size()==0)||(((String)commands.firstElement()).equalsIgnoreCase("ROOM")))
        &&(!mob.isInCombat()))
			target=mob.location();
		else
		{
			if((commands.size()==0)&&(mob.isInCombat()))
				target=mob.getVictim();
			if(target==null)
				target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		}
		if(target==null) return false;
		if((target instanceof Room)&&(target.fetchEffect(ID())!=null))
		{
			mob.tell("This place already has the god light.");
			return false;
		}
		
		if((target instanceof MOB)
		&&(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0))
		{
			mob.tell(target.name()+" has no eyes, and would not be affected.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			if(target instanceof Room) mob.envStats().setSensesMask(mob.envStats().sensesMask()|EnvStats.CAN_SEE_DARK);
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":((target instanceof MOB)?"^S<S-NAME> point(s) to <T-NAMESELF> and "+prayWord(mob)+". A beam of bright sunlight flashes into <T-HIS-HER> eyes!^?":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+".^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.recoverEnvStats();
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(target instanceof MOB)
						mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> go(es) blind!");
					maliciousAffect(mob,target,asLevel,0,-1);
                    mob.location().recoverRoomStats();
                    mob.location().recoverRoomStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
