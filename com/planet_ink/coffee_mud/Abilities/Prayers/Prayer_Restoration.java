package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Restoration extends Prayer
{
	public String ID() { return "Prayer_Restoration"; }
	public String name(){ return "Restoration";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALING;}
	public Environmental newInstance(){	return new Prayer_Restoration();}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) surrounded by a bright light.":"^S<S-NAME> "+prayWord(mob)+" over <T-NAMESELF> for restorative healing.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int healing=target.maxState().getHitPoints()-target.curState().getHitPoints();
				if(healing>0)
				{
					MUDFight.postHealing(mob,target,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
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
				Vector offensiveAffects=Prayer_RestoreSmell.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can smell again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_RestoreVoice.returnOffensiveAffects(mob,target);
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
				offensiveAffects=Prayer_Freedom.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can move again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_CureDisease.returnOffensiveAffects(target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> is cured of <S-HIS-HER> disease!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_CureBlindness.returnOffensiveAffects(mob,target);
				if(offensiveAffects.size()>0)
				{
					for(int a=offensiveAffects.size()-1;a>=0;a--)
						((Ability)offensiveAffects.elementAt(a)).unInvoke();
					mob.location().showOthers(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> can see again!");
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
				}
				offensiveAffects=Prayer_CureDeafness.returnOffensiveAffects(mob,target);
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
