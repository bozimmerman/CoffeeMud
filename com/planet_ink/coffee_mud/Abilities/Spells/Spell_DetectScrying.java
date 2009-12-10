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
public class Spell_DetectScrying extends Spell
{
	public String ID() { return "Spell_DetectScrying"; }
	public String name(){return "Detect Scrying";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int enchantQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(((MOB)target).isInCombat()||((MOB)target).isMonster())
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s) softly to <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer str=new StringBuffer("");
				if(target.session()!=null)
				for(int s1=0;s1<CMLib.sessions().size();s1++)
				{
					Session S1=CMLib.sessions().elementAt(s1);
					if(target.session().amBeingSnoopedBy(S1))
						str.append(S1.mob().name()+" is snooping on <T-NAME>.  ");
				}
				Ability A=target.fetchEffect("Spell_Scry");
				if((A!=null)&&(A.invoker()!=null))
					str.append(A.invoker().name()+" is scrying on <T-NAME>.");
				A=target.fetchEffect("Spell_Claireaudience");
				if((A!=null)&&(A.invoker()!=null))
					str.append(A.invoker().name()+" is listening to <T-NAME>.");
				A=target.fetchEffect("Spell_Clairevoyance");
				if((A!=null)&&(A.invoker()!=null))
					str.append(A.invoker().name()+" is watching <T-NAME>.");
				if(str.length()==0)
					str.append("There doesn't seem to be anyone scrying on <T-NAME>.");
				CMLib.commands().postSay(mob,target,str.toString(),false,false);
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> incant(s) to <T-NAMESELF>, but the spell fizzles.");

		return success;
	}
}
