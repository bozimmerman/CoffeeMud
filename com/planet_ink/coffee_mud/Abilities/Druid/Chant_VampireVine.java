package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_VampireVine extends Chant_SummonVine
{
	public String ID() { return "Chant_VampireVine"; }
	public String name(){ return "Vampire Vine";}
	public String displayText(){return "(Vampire Vine)";}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			{
				int amount=msg.value();
				if(amount>3)
				{
					amount=amount/4;
					MUDFight.postHealing(((MOB)affected),((MOB)affected),this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,amount,null);
					if(invoker!=null)
						MUDFight.postHealing(invoker,invoker,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,amount,null);
				}
			}
		}

		return true;
	}

	public MOB determineMonster(MOB caster, int material)
	{
		MOB victim=caster.getVictim();
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster);
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setAbility(19);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
		String name="a vampire vine";
		newMOB.setName(name);
		newMOB.setDisplayText(name+" looks enraged!");
		newMOB.setDescription("");
		newMOB.setAlignment(500);
		Ability A=CMClass.getAbility("Fighter_Rescue");
		A.setProfficiency(100);
		newMOB.addAbility(A);
		newMOB.setVictim(victim);
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseEnvStats().setDamage(30+(9*(level/5)));
		newMOB.baseEnvStats().setAttackAdjustment(10+(level));
		newMOB.baseEnvStats().setArmor(100-(30+(level/2)));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'N');
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		newMOB.setMoney(0);
		//if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> start(s) attacking "+victim.name()+"!");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
