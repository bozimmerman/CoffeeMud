package com.planet_ink.coffee_mud.Abilities.Prayers;

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
public class Prayer_MassMobility extends Prayer
{
	public String ID() { return "Prayer_MassMobility"; }
	public String name(){ return "Mass Mobility";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Mass Mobility)";}



	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(!mob.amDead()))
		{
			Ability A=(Ability)msg.tool();
			MOB newMOB=CMClass.getMOB("StdMOB");
			FullMsg msg2=new FullMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
			newMOB.recoverEnvStats();
			try
			{
				A.affectEnvStats(newMOB,newMOB.envStats());
				if((!Sense.aliveAwakeMobile(newMOB,true))
				   ||(Util.bset(A.flags(),Ability.FLAG_BINDING))
				   ||(!A.okMessage(newMOB,msg2)))
				{
					mob.location().show(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The aura around <S-NAME> repels the "+A.name()+" from <T-NAME>.");
					return false;
				}
			}
			catch(Exception e)
			{}
		}
		return true;
	}


	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_PARALYSIS,affectedStats.getStat(CharStats.SAVE_PARALYSIS)+100);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The aura of mobility around you fades.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		Room room=mob.location();
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		if((success)&&(room!=null))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"^S<S-NAME> "+prayWord(mob)+" for an aura of mobility!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB target=room.fetchInhabitant(i);
					if(target==null) break;

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					msg=new FullMsg(mob,target,this,affectType,"Mobility is invoked upon <T-NAME>.");
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						beneficialAffect(mob,target,asLevel,0);
					}
				}
			}
		}
		else
		{
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but nothing happens.");
			return false;
		}
		return success;
	}
}
