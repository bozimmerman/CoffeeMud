package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_TurnUndead extends StdAbility
{
	public String ID() { return "Skill_TurnUndead"; }
	public String name(){ return "Turn Undead";}
	public String displayText(){ return "(Turned)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"TURN","CONTROL"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_TurnUndead();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((target.baseCharStats().getMyRace()==null)
		   ||(!target.baseCharStats().getMyRace().racialCategory().equals("Undead")))
		{
			mob.tell(auto?"Only the undead can be turned.":"You can only turn the undead.");
			return false;
		}

		if(mob.getAlignment()<350)
		{
			mob.tell("Only the riteous may turn the undead.");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck((mob.envStats().level()-target.envStats().level())*30,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_CAST_ATTACK_SOMANTIC_SPELL|(auto?Affect.MASK_GENERAL:0),auto?"<T-NAME> turn(s) away.":"^S<S-NAME> turn(s) <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if((mob.envStats().level()-target.envStats().level())>6)
					{
						mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> wither(s)"+(auto?".":" under <S-HIS-HER> holy power!"));
						ExternalPlay.postDamage(mob,target,this,target.curState().getHitPoints(),Affect.NO_EFFECT,-1,null);
					}
					else
					{
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> shake(s) in fear!");
						ExternalPlay.flee(target,"");
					}
					invoker=mob;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to turn <T-NAMESELF>, but fail(s).");


		// return whether it worked
		return success;
	}
}