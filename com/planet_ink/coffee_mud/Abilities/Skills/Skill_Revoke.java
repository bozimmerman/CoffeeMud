package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_Revoke extends StdAbility
{
	public String ID() { return "Skill_Revoke"; }
	public String name(){ return "Revoke";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"REVOKE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int maxRange(){return 10;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		String whatToRevoke=Util.combine(commands,0);

		Environmental target=null;
		if((whatToRevoke.length()==0)
		&&(mob.location().numEffects()>0))
			target=mob.location();
		else
		if(whatToRevoke.equalsIgnoreCase("room"))
		   target=mob.location();
		else
		if(whatToRevoke.equalsIgnoreCase("self"))
		    target=mob;
		else
		{
			int dir=Directions.getGoodDirectionCode(whatToRevoke);
			if(dir>=0)
				target=mob.location().getExitInDir(dir);
			else
				target=mob.location().fetchFromRoomFavorMOBs(null,whatToRevoke,Item.WORN_REQ_ANY);
		}

		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("Revoke from what?  You don't see '"+whatToRevoke+"' here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Ability revokeThis=null;
		for(int a=0;a<target.numEffects();a++)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)
			&&(A.invoker()==mob)
			&&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
			   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
			   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT))
			&&(A.canBeUninvoked()))
				revokeThis=A;
		}

		if(revokeThis==null)
		{
			if(target instanceof Room)
				mob.tell("Revoke your magic from what?");
			else
				mob.tell(mob,target,null,"<T-NAME> do(es) not appear to be affected by anything you can revoke.");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> revoke(s) "+revokeThis.name()+" from "+target.name());
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to revoke "+revokeThis.name()+" from "+target.name()+", but flub(s) it.");
		return success;
	}

}
