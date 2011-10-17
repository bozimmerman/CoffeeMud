package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2011 Bo Zimmerman

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
public class Skill_CenterOfAttention extends BardSkill
{
	public String ID() { return "Skill_CenterOfAttention"; }
	public String name(){ return "Center of Attention";}
	public String displayText(){ return "(Performing)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CENTEROFATTENTION"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int getTicksBetweenCasts() { return 70;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
	        if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
	        if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
        if(CMLib.flags().isSitting(mob))
        {
            mob.tell("You need to stand up!");
            return false;
        }
        if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
            return false;
        
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth performing for.");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":"<S-NAME> begin(s) flailing about while making loud silly noises.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Iterator e=h.iterator();e.hasNext();)
				{
					MOB target=(MOB)e.next();
					int levelDiff=target.phyStats().level()-(((2*getXLEVELLevel(mob))+mob.phyStats().level()));
					if(levelDiff>0)
						levelDiff=levelDiff*5;
					else
						levelDiff=0;
					msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> trip(s)!":"^F^<FIGHT^><S-NAME> trip(s) <T-NAMESELF>!^</FIGHT^>^?");
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						maliciousAffect(mob,target,asLevel,2,-1);
						target.location().show(target,mob,CMMsg.MSG_OK_ACTION,"<S-NAME> watch(es) <T-NAME> with an amused expression.");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to become the center of attention, but fail(s).");
		return success;
	}
}
