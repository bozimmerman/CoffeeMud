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
public class Prayer_AuraHeal extends Prayer
{
	public String ID() { return "Prayer_AuraHeal"; }
	public String name(){ return "Aura of Healing";}
	public String displayText(){ return "(Heal Aura)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALINGMAGIC;}

    public Prayer_AuraHeal()
    {
        super();

        tickDown = 4;
    }

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		Room R=(Room)affected;

		super.unInvoke();

		if(canBeUninvoked())
			R.showHappens(CMMsg.MSG_OK_VISUAL,"The healing aura around you fades.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Room)))
			return super.tick(ticking,tickID);

		if((--tickDown)>=0) return super.tick(ticking,tickID);
		tickDown=4;

		HashSet H=null;
		if((invoker()!=null)&&(invoker().location()==affected))
		{
			H=new HashSet();
			invoker().getGroupMembers(H);
		}
		Room R=(Room)affected;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			   &&(M.curState().getHitPoints()<M.maxState().getHitPoints())
			   &&((H==null)
				||(M.getVictim()==null)
				||(!H.contains(M.getVictim()))))
			{
				int oldHP=M.curState().getHitPoints();
				if(invoker()!=null)
				{
					int healing=CMLib.dice().roll(2,adjustedLevel(invoker(),0),4);
					CMLib.combat().postHealing(invoker(),M,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
				}
				else
				{
					int healing=CMLib.dice().roll(2,CMLib.ableMapper().lowestQualifyingLevel(ID()),4);
					CMLib.combat().postHealing(M,M,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
				}
				if(M.curState().getHitPoints()>oldHP)
					M.tell("You feel a little better!");
			}
		}
		return super.tick(ticking,tickID);
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
        	if(target instanceof MOB)
        	{
	            if(((MOB)target).charStats().getMyRace().racialCategory().equals("Undead"))
	                return Ability.QUALITY_INDIFFERENT;
	            if(target!=mob)
	            {
	                if(((MOB)target).charStats().getMyRace().racialCategory().equals("Undead"))
	                    return super.castingQuality(mob, target,Ability.QUALITY_MALICIOUS);
	            }
        	}
            return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("The aura of healing is already here.");
			return false;
		}
		if(target.fetchEffect("Prayer_AuraHarm")!=null)
		{
			target.fetchEffect("Prayer_AuraHarm").unInvoke();
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for all to feel better.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"A healing aura descends over the area!");
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for an aura of healing, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
