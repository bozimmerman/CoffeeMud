package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_MassParalyze extends Prayer
{
	public Prayer_MassParalyze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Paralyze";
		displayText="(Paralyzed)";
		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(20);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_MassParalyze();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_MOVE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The paralysis eases out of your muscles.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null) return false;

		boolean success=profficiencyCheck(0,auto);
		boolean nothingDone=true;
		if(success)
		{
			for(Enumeration e=h.elements();e.hasMoreElements();)
			{
				MOB target=(MOB)e.nextElement();
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"":"<S-NAME> invoke(s) an unholy paralysis upon <T-NAMESELF>.");
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MASK_MALICIOUS|Affect.TYP_PARALYZE,null);
				if((target!=mob)&&(mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((!msg.wasModified())&&(!msg2.wasModified()))
					{
						success=maliciousAffect(mob,target,7,-1);
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> can't move!");
					}
					nothingDone=false;
				}
			}
		}

		if(nothingDone)
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to paralyze everyone, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
