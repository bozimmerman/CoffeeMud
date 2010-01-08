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
public class Prayer_RockFlesh extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_RockFlesh"; }
	public String name(){return "Rock Flesh";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public long flags(){return Ability.FLAG_HOLY;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean supportsMending(Environmental E)
	{ 
		if(!(E instanceof MOB)) return false;
		return (E.fetchEffect("Spell_FleshStone")!=null)||(E.fetchEffect("Prayer_FleshRock")!=null);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		Environmental target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		Ability revokeThis=null;
		for(int a=0;a<target.numEffects();a++)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)&&(A.canBeUninvoked())
			   &&((A.ID().equalsIgnoreCase("Spell_FleshStone"))
				  ||(A.ID().equalsIgnoreCase("Prayer_FleshRock"))))
			{
				revokeThis=A;
				break;
			}
		}

		if(revokeThis==null)
		{
			if(auto)
				mob.tell("Nothing happens.");
			else
				mob.tell(mob,target,null,"<T-NAME> can not be affected by this prayer.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to dispel "+revokeThis.name()+" from <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" on <T-YOUPOSS> behalf, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
