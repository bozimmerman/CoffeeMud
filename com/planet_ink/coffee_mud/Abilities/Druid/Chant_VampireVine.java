package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_VampireVine extends Chant_SummonVine
{
	public String ID() { return "Chant_VampireVine"; }
	public String name(){ return "Vampire Vine";}
	public String displayText(){return "(Vampire Vine)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	public long flags(){return Ability.FLAG_SUMMONING;}
    public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}

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
					CMLib.combat().postHealing(((MOB)affected),((MOB)affected),this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,amount,null);
					if(invoker!=null)
						CMLib.combat().postHealing(invoker,invoker,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,amount,null);
				}
			}
		}

		return true;
	}

	public MOB determineMonster(MOB caster, int material)
	{
		MOB victim=caster.getVictim();
		MOB newMOB=CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster,0);
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setAbility(19);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
		String name="a vampire vine";
		newMOB.setName(name);
		newMOB.setDisplayText(name+" looks enraged!");
		newMOB.setDescription("");
		CMLib.factions().setAlignment(newMOB,Faction.ALIGN_NEUTRAL);
		Ability A=CMClass.getAbility("Fighter_Rescue");
		A.setProficiency(100);
		newMOB.addAbility(A);
		newMOB.setVictim(victim);
		newMOB.baseEnvStats().setAbility(newMOB.baseEnvStats().ability()*2);
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseEnvStats().setDamage(30+(9*(level/5)));
		newMOB.baseEnvStats().setAttackAdjustment(10+(level));
		newMOB.baseEnvStats().setArmor(100-(30+(level/2)));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setStartRoom(null); // keep before postFollow for Conquest
        CMLib.commands().postFollow(newMOB,caster,true);
        if(newMOB.amFollowing()!=caster)
            caster.tell(newMOB.name()+" seems unwilling to follow you.");
        else
        {
            if(newMOB.getVictim()!=victim) newMOB.setVictim(victim);
            newMOB.location().showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,"<S-NAME> start(s) attacking <T-NAMESELF>!");
        }
		return(newMOB);
	}
}
