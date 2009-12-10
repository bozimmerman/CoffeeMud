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
public class Spell_FreeMovement extends Spell
{
	public String ID() { return "Spell_FreeMovement"; }
	public String name(){return "Free Movement";}
	public String displayText(){return "(Free Movement)";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;}

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
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(!mob.amDead()))
		{
			Ability A=(Ability)msg.tool();
			if(CMath.bset(A.flags(),Ability.FLAG_PARALYZING))
			{
				msg.addTrailerMsg(CMClass.getMsg(mob,null,CMMsg.MSG_OK_VISUAL,"The uninhibiting barrier around <S-NAME> repels the "+A.name()+"."));
				return false;
			}
			MOB newMOB=CMClass.getMOB("StdMOB");
			CMMsg msg2=CMClass.getMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
			newMOB.recoverEnvStats();
			try
			{
				A.affectEnvStats(newMOB,newMOB.envStats());
				if((!CMLib.flags().aliveAwakeMobileUnbound(newMOB,true))
				   ||(CMath.bset(A.flags(),Ability.FLAG_PARALYZING))
				   ||(!A.okMessage(newMOB,msg2)))
				{
					msg.addTrailerMsg(CMClass.getMsg(mob,null,CMMsg.MSG_OK_VISUAL,"The uninhibiting barrier around <S-NAME> repels the "+A.name()+"."));
                    newMOB.destroy();
					return false;
				}
			}
			catch(Exception e)
			{}
            newMOB.destroy();
		}
		return true;
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> feel(s) freely protected.":"^S<S-NAME> invoke(s) an uninhibiting barrier of protection around <T-NAMESELF>.^?");
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
