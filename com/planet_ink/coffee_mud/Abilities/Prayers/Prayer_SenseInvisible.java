package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SenseInvisible extends Prayer
{
	public String ID() { return "Prayer_SenseInvisible"; }
	public String name(){ return "Sense Invisible";}
	public String displayText(){ return "(Sense Invisible)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return OK_SELF;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	public Environmental newInstance(){	return new Prayer_SenseInvisible();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("The clearness fades from your eyes.");
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

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"<S-NAME> attain(s) clear eyes.":"^S<S-NAME> pray(s) for divine revelation, and <S-HIS-HER> eyes become clear.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> pray(s) for divine revelation, but <S-HIS-HER> prayer is not heard.");


		// return whether it worked
		return success;
	}
}
