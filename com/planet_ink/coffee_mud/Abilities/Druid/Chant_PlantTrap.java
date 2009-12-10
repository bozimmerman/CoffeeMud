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
@SuppressWarnings("unchecked")
public class Chant_PlantTrap extends Chant implements Trap
{
	public String ID() { return "Chant_PlantTrap"; }
	public String name(){ return "Plant Trap";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 100;}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
    public Vector getTrapComponents() { return new Vector(); }
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean permanent)
	{beneficialAffect(mob,E,qualifyingClassLevel+trapBonus,0); return (Trap)E.fetchEffect(ID());}

	public boolean disabled(){return false;}
	public boolean sprung(){return false;}
	public void disable(){unInvoke();}
	public void spring(MOB M)
	{
		doMyThing(M);
	}

	public static final String[] choices={"Chant_PlantChoke","Chant_PlantConstriction"};
	public void doMyThing(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))
			||(invoker().getGroupMembers(new HashSet()).contains(target))
			||(CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) some agressive plants!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> <S-IS-ARE> assaulted by the plants!"))
			{
				Vector them=CMParms.makeVector(choices);
				if(invoker()!=null)
				for(int i=0;i<choices.length;i++)
					if(invoker().fetchAbility(choices[i])==null)
						them.removeElement(choices[i]);
				if(them.size()>0)
				{
					String s=(String)them.elementAt(CMLib.dice().roll(1,them.size(),-1));
					Ability A=CMClass.getAbility(s);
					A.invoke(target,target,true,0);
				}
			}
		}
	}

    public boolean helpfulAbilityFound(MOB mob)
    {
        for(int i=0;i<choices.length;i++)
            if(mob.fetchAbility(choices[i])!=null)
             return true;
        return false;
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(affected)&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(!msg.amISource(invoker))
		&&(msg.source().amFollowing()!=invoker))
			spring(msg.source());
		super.executeMsg(myHost,msg);
	}
    
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!helpfulAbilityFound(mob))
                return Ability.QUALITY_INDIFFERENT;
            Room R=mob.location();
            if(R!=null)
            {
                if(((R.domainType()&Room.INDOORS)>0))
                    return Ability.QUALITY_INDIFFERENT;
                if((R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
                   ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
                   ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
                   ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
                   ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already trapped.");
			return false;
		}
		if(!helpfulAbilityFound(mob))
		{
			mob.tell("You must know plant choke or plant constriction for this chant to work.");
			return false;
		}

		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		&&(!auto))
		{
			mob.tell("This chant does not work here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"This area seems to writh with malicious plants.":"^S<S-NAME> chant(s), stirring the plant life into maliciousness.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but the magic fades.");

		// return whether it worked
		return success;
	}
}
