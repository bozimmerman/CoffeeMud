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
public class Prayer_Faithless extends Prayer
{
	public String ID() { return "Prayer_Faithless"; }
	public String name(){ return "Faithless";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return 100;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
                if(((MOB)target).charStats().getCurrentClass().baseClass().equals("Cleric"))
                    return Ability.QUALITY_INDIFFERENT;
                if(CMLib.flags().isAnimalIntelligence(((MOB)target))||CMLib.flags().isGolem(target))
                    return Ability.QUALITY_INDIFFERENT;
                if(((MOB)target).getWorshipCharID().length()==0)
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((!auto)&&(target.charStats().getCurrentClass().baseClass().equals("Cleric")))
		{
			mob.tell(target.name()+" can not be affected by this prayer.");
			return false;
		}
		if(CMLib.flags().isAnimalIntelligence(target)||CMLib.flags().isGolem(target))
		{
			if(!auto)mob.tell(target.name()+" can not be affected by this prayer.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		boolean success=proficiencyCheck(mob,-(levelDiff*25),auto);
		Deity D=null;
		if(target.getWorshipCharID().length()>0)
			D=CMLib.map().getDeity(target.getWorshipCharID());
		int type=verbalCastCode(mob,target,auto);
		int mal=CMMsg.MASK_MALICIOUS;
		if(auto){ type=CMath.unsetb(type,CMMsg.MASK_MALICIOUS); mal=0;}
		if((success)&&(D!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,type,auto?"":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to lose faith!^?");
			CMMsg msg2=CMClass.getMsg(target,D,this,CMMsg.MSG_REBUKE,"<S-NAME> LOSE(S) FAITH!!!");
			CMMsg msg3=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_VERBAL|mal|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,msg3))
			&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg3);
				if((msg.value()<=0)&&(msg3.value()<=0))
					mob.location().send(mob,msg2);
			}
		}
		else
			maliciousFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
