package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Earthshield extends Prayer
{
	public String ID() { return "Prayer_Earthshield"; }
	public String name(){ return "Earthshield";}
	public int quality(){ return OK_OTHERS;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	public String displayText(){ return "(In Earthshield)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_Earthshield();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		affectableStats.setArmor(affectableStats.armor()-(2*CMAble.qualifyingClassLevel((MOB)affected,this)));
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectableState.setMovement(affectableState.getMovement()/2);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The earth shield has dissipated.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+".^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=beneficialAffect(mob,target,0);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> is covered by an Earth Shield!");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but flub(s) it.");


		// return whether it worked
		return success;
	}
}
