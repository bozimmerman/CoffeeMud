package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prayer_Sacrifice extends Prayer
{
	public String ID() { return "Prayer_Sacrifice"; }
	public String name(){ return "Sacrifice";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public static Item getBody(Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I!=null)
			&&(I instanceof DeadBody)
			&&(!((DeadBody)I).playerCorpse())
			&&(((DeadBody)I).mobName().length()>0))
				return I;
		}
		return null;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=Prayer_Sacrifice.getBody(mob.location());
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if((!(target instanceof DeadBody))
		   ||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell("You may only sacrifice the dead.");
			return false;
		}

		if((((DeadBody)target).playerCorpse())&&(!((DeadBody)target).mobName().equals(mob.Name())))
		{
			mob.tell("You are not allowed to sacrifice a players corpse.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> sacrifice(s) <T-HIM-HERSELF>.":"^S<S-NAME> sacrifice(s) <T-NAMESELF> to "+hisHerDiety(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.destroy();
				if(mob.getAlignment()>=500)
				{
					double exp=5.0;
					int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
					int levelDiff=mob.envStats().level()-target.envStats().level();
					if(levelDiff>levelLimit) exp=0.0;
					MUDFight.postExperience(mob,null,null,(int)Math.round(exp),false);
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> attempt(s) to sacrifice <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}
