package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Bind extends ThiefSkill
{
	public int amountRemaining=0;

	public Thief_Bind()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Bind";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Bound)";

		triggerStrings.addElement("BIND");
		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Bind();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOVE))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the ropes binding <S-HIM-HER>.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(!mob.amDead())
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the ropes.");
		ExternalPlay.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if(!Sense.isSleeping(target))
		{
			mob.tell(target.name()+" doesn't look willing to cooperate.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_DELICATE_HANDS_ACT|Affect.ACT_SOUND|Affect.MASK_MALICIOUS,"<S-NAME> bind(s) <T-NAME> with strong ropes.");
			if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					amountRemaining=adjustedLevel(mob)*20;
					if(target.location()==mob.location())
						success=maliciousAffect(mob,target,Integer.MAX_VALUE-1000,-1);
				}
				if(mob.getVictim()==target) mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bind <T-NAME> and fail(s).");


		// return whether it worked
		return success;
	}
}