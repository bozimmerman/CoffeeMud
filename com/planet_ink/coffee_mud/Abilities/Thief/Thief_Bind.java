package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Bind extends ThiefSkill
{
	public String ID() { return "Thief_Bind"; }
	public String name(){ return "Bind";}
	public String displayText(){ return "(Bound)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BIND"};
	public String[] triggerStrings(){return triggerStrings;}
	public int amountRemaining=500;

	public Environmental newInstance(){	return new Thief_Bind();}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_MOVE))))
			{
				if(mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the ropes binding <S-HIM-HER>."))
				{
					amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the ropes.");
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!Sense.isSleeping(target))
		{
			mob.tell(target.displayName()+" doesn't look willing to cooperate.");
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_THIEF_ACT|Affect.MASK_SOUND|Affect.MASK_MALICIOUS,"<S-NAME> bind(s) <T-NAME> with strong ropes.");
			if((mob.location().okAffect(mob,msg))&&(target.fetchAffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					amountRemaining=adjustedLevel(mob)*25;
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