package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Plague extends Prayer
{
	int plagueDown=4;

	public Prayer_Plague()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Plague";
		displayText="(Plague)";
		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(12);

		addQualifyingClass("Cleric",baseEnvStats().level());
		addQualifyingClass("Paladin",baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Plague();
	}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(tickID);

		if((--plagueDown)<=0)
		{
			MOB mob=(MOB)affected;
			plagueDown=4;
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> body erupt with a fresh batch of painful oozing sores!");
			if(invoker==null) invoker=mob;
			ExternalPlay.postDamage(invoker,mob,this,mob.envStats().level());
			MOB target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if((target!=invoker)&&(target!=mob)&&(target.fetchAffect(ID())==null))
				if(Dice.rollPercentage()>(target.charStats().getConstitution()*4))
					maliciousAffect(invoker,target,48,-1);
		}
		return super.tick(tickID);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setConstitution(3);
		affectableStats.setDexterity(3);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

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
			FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"":"<S-NAME> inflict(s) an unholy plague upon <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					invoker=mob;
					maliciousAffect(mob,target,48,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> look(s) seriously ill!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inflict a plague upon <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
