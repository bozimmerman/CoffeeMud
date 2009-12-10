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
public class Prayer_Restoration extends Prayer implements MendingSkill
{
	public String ID() { return "Prayer_Restoration"; }
	public String name(){ return "Restoration";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALINGMAGIC;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean supportsMending(Environmental E)
	{ 
		if(!(E instanceof MOB)) return false;
		
		if(((((MOB)E).curState()).getHitPoints()<(((MOB)E).maxState()).getHitPoints()))
			return true;
		MOB caster=CMClass.getMOB("StdMOB");
		caster.baseEnvStats().setLevel(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL));
		caster.envStats().setLevel(CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL));
		if(
		  (E.fetchEffect("Amputation")!=null)
		||(E.fetchEffect("Fighter_AtemiStrike")!=null)
		||(E.fetchEffect("Undead_EnergyDrain")!=null)
		||(E.fetchEffect("Undead_WeakEnergyDrain")!=null)
		||(E.fetchEffect("Undead_ColdTouch")!=null)
		||((new Prayer_RestoreSmell().returnOffensiveAffects(caster,E)).size()>0)
		||((new Prayer_RestoreVoice().returnOffensiveAffects(caster,E)).size()>0)
		||((Prayer_RemovePoison.returnOffensiveAffects(E)).size()>0)
		||((new Prayer_Freedom().returnOffensiveAffects(caster,E)).size()>0)
		||((new Prayer_CureBlindness().returnOffensiveAffects(caster,E)).size()>0)
		||((new Prayer_CureDeafness().returnOffensiveAffects(caster,E)).size()>0)
		)
		{
			caster.destroy();
			return true;
		}
		caster.destroy();
		return false;
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(!supportsMending(target))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) surrounded by a bright light.":"^S<S-NAME> "+prayWord(mob)+" over <T-NAMESELF> for restorative healing.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int healing=target.maxState().getHitPoints()-target.curState().getHitPoints();
				if(healing>0)
				{
					CMLib.combat().postHealing(mob,target,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> look(s) much healthier!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				Ability A=target.fetchEffect("Amputation");
				if(A!=null)
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> missing parts are restored!");
					A=target.fetchAbility(A.ID());
					if(A!=null) target.delAbility(A);
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				
				A=target.fetchEffect("Fighter_AtemiStrike");
				if((A!=null)&&(A.canBeUninvoked()))
				{
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> atemi damage is healed!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				
				A=target.fetchEffect("Undead_EnergyDrain");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> lost levels are restored!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				A=target.fetchEffect("Undead_WeakEnergyDrain");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> lost levels are restored!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				A=target.fetchEffect("Undead_ColdTouch");
				if(A!=null)
				{
					A.unInvoke();
					target.delEffect(A);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer cold and weak!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				Vector offensiveAffects=new Prayer_RestoreSmell().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can smell again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_RestoreVoice().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can speak again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_RemovePoison.returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is cured of <S-HIS-HER> poisonous afflication!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_Freedom().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can move again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_CureDisease().returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is cured of <S-HIS-HER> disease!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_CureBlindness().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can see again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=new Prayer_CureDeafness().returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can hear again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");


		// return whether it worked
		return success;
	}
}
