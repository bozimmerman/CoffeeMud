package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Paralyze extends Prayer
{
	public Prayer_Paralyze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Paralyze";
		displayText="(Paralyzed)";
		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;
		baseEnvStats().setLevel(8);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Paralyze();
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
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>=3)
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}
		else
			levelDiff=0;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;



		boolean success=profficiencyCheck(-25-((target.charStats().getStat(CharStats.WISDOM)*2)+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> invoke(s) an unholy paralysis upon <T-NAMESELF>.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MASK_MALICIOUS|Affect.TYP_PARALYZE|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					success=maliciousAffect(mob,target,8,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> can't move!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to paralyze <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
