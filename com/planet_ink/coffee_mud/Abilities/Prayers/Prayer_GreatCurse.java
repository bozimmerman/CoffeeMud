package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_GreatCurse extends Prayer
{
	public String ID() { return "Prayer_GreatCurse"; }
	public String name(){ return "Great Curse";}
	public String displayText(){ return "(Cursed)";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_GreatCurse();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setArmor(affectableStats.armor()+20);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-10);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The great curse is lifted.");
	}


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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"<T-NAME> is horribly cursed!":"^S<S-NAME> curse(s) <T-NAMESELF> horribly.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					Item I=Prayer_Curse.getSomething(mob,true);
					if(I!=null)
					{
						Prayer_Curse.endLowerBlessings(I,CMAble.lowestQualifyingLevel(ID()));
						I.recoverEnvStats();
					}
					Prayer_Curse.endLowerBlessings(target,CMAble.lowestQualifyingLevel(ID()));
					success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to horribly curse <T-NAMESELF> , but nothing happens.");


		// return whether it worked
		return success;
	}
}
