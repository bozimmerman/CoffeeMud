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
public class Prayer_CureDisease extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_CureDisease"; }
	public String name(){ return "Cure Disease";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean supportsMending(Environmental E)
	{ 
		if(!(E instanceof MOB)) return false;
		boolean canMend=returnOffensiveAffects(E).size()>0;
		return canMend;
	}
	
	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A instanceof DiseaseAffect))
				offenders.addElement(A);
		}
		return offenders;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(supportsMending((MOB)target))
                    return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"A healing glow surrounds <T-NAME>.":"^S<S-NAME> "+prayWord(mob)+" for <T-YOUPOSS> health.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean badOnes=false;
				for(int a=offensiveAffects.size()-1;a>=0;a--)
				{
				    Ability A=((Ability)offensiveAffects.elementAt(a));
				    if(A instanceof DiseaseAffect)
				    {
				        if((A.invoker()!=mob)
				        &&((((DiseaseAffect)A).difficultyLevel()*10)>adjustedLevel(mob,asLevel)))
				            badOnes=true;
				        else
							A.unInvoke();
				    }
				    else
				        A.unInvoke();
				        
				}
				if(badOnes)
				    mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,"<T-NAME> had diseases too powerful for <S-YOUPOSS> magic.");
				else
				    mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> cure(s) the diseases in <T-NAMESELF>.");
				if(!CMLib.flags().stillAffectedBy(target,offensiveAffects,false))
					target.tell("You feel much better!");
			}
		}
		else
		if(!auto)
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
