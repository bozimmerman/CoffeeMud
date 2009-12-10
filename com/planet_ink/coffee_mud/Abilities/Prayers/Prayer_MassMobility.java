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
public class Prayer_MassMobility extends Prayer
{
	public String ID() { return "Prayer_MassMobility"; }
	public String name(){ return "Mass Mobility";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Mass Mobility)";}



	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(!mob.amDead()))
		{
			Ability A=(Ability)msg.tool();
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
					mob.location().show(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The aura around <S-NAME> repels the "+A.name()+" from <T-NAME>.");
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


	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectedStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+100);
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

		boolean success=proficiencyCheck(mob,0,auto);

		Room room=mob.location();
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
		if((success)&&(room!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,affectType,auto?"":"^S<S-NAME> "+prayWord(mob)+" for an aura of mobility!^?");
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
					msg=CMClass.getMsg(mob,target,this,affectType,"Mobility is invoked upon <T-NAME>.");
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
