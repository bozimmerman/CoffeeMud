package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Plague extends Chant implements DiseaseAffect
{
	public String ID() { return "Chant_Plague"; }
	public String name(){ return "Plague";}
	public String displayText(){return "(Plague)";}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Chant_Plague();}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_PROXIMITY|DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD;}
	int plagueDown=4;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if((--plagueDown)<=0)
		{
			MOB mob=(MOB)affected;
			plagueDown=4;
			if(invoker==null) invoker=mob;
			int dmg=(mob.envStats().level()/2)+1;
			ExternalPlay.postDamage(invoker,mob,this,dmg,Affect.TYP_DISEASE,-1,"<T-NAME> watch(es) <T-HIS-HER> body erupt with a fresh batch of painful oozing sores!");
			if(mob.location()==null) return false;
			MOB target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if((target!=null)&&(target!=invoker)&&(target!=mob)&&(target.fetchAffect(ID())==null))
				if(Dice.rollPercentage()>target.charStats().getStat(CharStats.SAVE_DISEASE))
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> look(s) seriously ill!");
					maliciousAffect(invoker,target,88,-1);
				}
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,3);
		affectableStats.setStat(CharStats.DEXTERITY,3);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The sores on your face clear up.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>!^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_DISEASE|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(mob,msg))&&(mob.location().okAffect(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					invoker=mob;
					maliciousAffect(mob,target,88,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> look(s) seriously ill!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}