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
public class Spell_DispelMagic extends Spell
{
	public String ID() { return "Spell_DispelMagic"; }
	public String name(){return "Dispel Magic";}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS|CAN_EXITS|CAN_ROOMS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            Ability A=null;
            if(target==mob)
            {
                for(int e=0;e<mob.numEffects();e++)
                {
                    A=mob.fetchEffect(e);
                    if((A!=null)
                    &&(A.canBeUninvoked())
                    &&(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
                    &&(A.invoker()!=mob)
                    &&(A.invoker().envStats().level()<=mob.envStats().level()+5))
                        return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
                }
            }
            else
            if(target instanceof MOB)
            {
                for(int e=0;e<((MOB)target).numEffects();e++)
                {
                    A=((MOB)target).fetchEffect(e);
                    if((A!=null)
                    &&((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
                        ||(A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF))
                    &&(A.invoker()==((MOB)target))
                    &&(A.invoker().envStats().level()<=mob.envStats().level()+5))
                        return super.castingQuality(mob, target,Ability.QUALITY_MALICIOUS);
                }
            }
            if((mob.isMonster())&&(mob.isInCombat()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null) return false;

		Ability revokeThis=null;
		boolean foundSomethingAtLeast=false;
        boolean admin=CMSecurity.isASysOp(mob);
		for(int a=0;a<target.numEffects();a++)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)
			&&(A.canBeUninvoked())
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)))
			{
				foundSomethingAtLeast=true;
				if((A.invoker()!=null)
				&&((A.invoker()==mob)
    				||(A.invoker().envStats().level()<=mob.envStats().level()+5)
                    ||admin))
					revokeThis=A;
			}
		}

		if(revokeThis==null)
		{
			if(foundSomethingAtLeast)
				mob.tell(mob,target,null,"The magic on <T-NAME> appears too powerful to dispel.");
			else
			if(auto)
				mob.tell("Nothing seems to be happening.");
			else
				mob.tell(mob,target,null,"<T-NAME> do(es) not appear to be affected by anything you can dispel.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int diff=revokeThis.invoker().envStats().level()-mob.envStats().level();
		if(diff<0) diff=0;
		else diff=diff*-20;

		boolean success=proficiencyCheck(mob,diff,auto);
		if(success)
		{
			int affectType=verbalCastCode(mob,target,auto);
			if(((!mob.isMonster())&&(target instanceof MOB)&&(!((MOB)target).isMonster()))
			||(mob==target)
			||(mob.getGroupMembers(new HashSet()).contains(target)))
				affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
			if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;

			CMMsg msg=CMClass.getMsg(mob,target,this,affectType,auto?revokeThis.name()+" is dispelled from <T-NAME>.":"^S<S-NAME> dispel(s) "+revokeThis.name()+" from <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to dispel "+revokeThis.name()+" from <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
