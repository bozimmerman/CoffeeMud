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
public class Spell_FreeMovement extends Spell
{
	public String ID() { return "Spell_FreeMovement"; }
	public String name(){return "Free Movement";}
	public String displayText(){return "(Free Movement)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your uninhibiting protection dissipates.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(!mob.amDead()))
		{
			Ability A=(Ability)msg.tool();
			if(Util.bset(A.flags(),Ability.FLAG_BINDING))
			{
				msg.addTrailerMsg(new FullMsg(mob,null,CMMsg.MSG_OK_VISUAL,"The uninhibiting barrier around <S-NAME> repels the "+A.name()+"."));
				return false;
			}
			else
			{
				MOB newMOB=(MOB)CMClass.getMOB("StdMOB");
				FullMsg msg2=new FullMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
				newMOB.recoverEnvStats();
				try
				{
					A.affectEnvStats(newMOB,newMOB.envStats());
					if((!Sense.aliveAwakeMobile(newMOB,true))
					   ||(Util.bset(A.flags(),Ability.FLAG_BINDING))
					   ||(!A.okMessage(newMOB,msg2)))
					{
						msg.addTrailerMsg(new FullMsg(mob,null,CMMsg.MSG_OK_VISUAL,"The uninhibiting barrier around <S-NAME> repels the "+A.name()+"."));
						return false;
					}
				}
				catch(Exception e)
				{}
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) freely protected.":"^S<S-NAME> invoke(s) an uninhibiting barrier of protection around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an uninhibiting barrier, but fail(s).");

		return success;
	}
}
