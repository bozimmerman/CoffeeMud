package com.planet_ink.coffee_mud.Abilities.Fighter;
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
public class Fighter_Tumble extends FighterSkill
{
	public int hits=0;
	public String ID() { return "Fighter_Tumble"; }
	public String name(){ return "Tumble";}
	public String displayText(){ return "(Tumbling)";}
	private static final String[] triggerStrings = {"TUMBLE"};
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC;}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
		   invoker=(MOB)affected;
		if(invoker!=null)
		{
            float f=(float)CMath.mul(0.2,getXLEVELLevel(invoker));
			affectableStats.setDamage(affectableStats.damage()-(int)Math.round(CMath.div(affectableStats.damage(),2.0+f)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(CMath.div(affectableStats.attackAdjustment(),2.0+f)));
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&((msg.value())>0))
		{
			if((msg.tool()!=null)
			&&(!mob.amDead())
			&&(msg.tool() instanceof Weapon))
			{
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
				if(!((MOB)msg.target()).amDead())
					msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> tumble(s) around the attack from <T-NAME>."));
				if((++hits)>=2)
					unInvoke();
			}
		}
		return true;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.fetchEffect(this.ID())!=null)
                return Ability.QUALITY_INDIFFERENT;
            if(!mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(!CMLib.flags().aliveAwakeMobile(mob,true))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.fetchEffect(this.ID())!=null)
		{
			mob.tell("You are already tumbling.");
			return false;
		}

		if((!auto)&&(!mob.isInCombat()))
		{
			mob.tell("You aren't in combat!");
			return false;
		}
		if(!CMLib.flags().aliveAwakeMobile(mob,true))
		{
			mob.tell("You need to stand up!");
			return false;
		}

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT,auto?"<T-NAME> begin(s) tumbling around!":"<S-NAME> tumble(s) around!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				hits=0;
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to tumble, but goof(s) it up.");
		return success;
	}
}
