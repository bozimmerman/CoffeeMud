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
public class Prayer_MoralBalance extends Prayer
{
	public String ID() { return "Prayer_MoralBalance"; }
	public String name(){ return "Moral Balance";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY | Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

        
		boolean success=proficiencyCheck(mob,0,auto);
        CMMsg msg2=null;
        if((mob!=target)&&(!mob.getGroupMembers(new HashSet()).contains(target)))
            msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,"<T-NAME> do(es) not seem to like <S-NAME> messing with <T-HIS-HER> head.");

		if((success)&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"<T-NAME> feel(s) completely different about the world.":"^S<S-NAME> "+prayWord(mob)+" to bring balance to <T-NAMESELF>!^?"));
			if((mob.location().okMessage(mob,msg))
            &&((msg2==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
                if((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
				{
					target.tell("Your views on the world suddenly change.");
                    Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
                    if(F!=null)
                    {
                     	int bredth=F.maximum()-F.minimum();
                    	int midpoint=F.minimum()+(bredth/2);
                    	int distance=midpoint-target.fetchFaction(F.factionID());
                    	int amt=target.fetchFaction(F.factionID())+(distance/8);
                        int change=amt-target.fetchFaction(F.factionID());
    					CMLib.factions().postFactionChange(target,this, CMLib.factions().AlignID(),change);
                    }
				}
                if(msg2!=null) mob.location().send(mob,msg2);
			}
		}
		else
		{
            if((msg2!=null)&&(mob.location().okMessage(mob,msg2)))
                mob.location().send(mob,msg2);
			return beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");
		}


		// return whether it worked
		return success;
	}
}
