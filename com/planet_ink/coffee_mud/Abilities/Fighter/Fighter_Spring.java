package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Spring extends StdAbility
{
	public String ID() { return "Fighter_Spring"; }
	public String name(){ return "Spring Attack";}
	private static final String[] triggerStrings = {"SPRINGATTACK","SPRING","SATTACK"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_Spring();}
	public int classificationCode(){return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to make a spring attack!");
			return false;
		}
		if(mob.curState().getMovement()<50)
		{
			mob.tell("You are too tired to make a spring attack.");
			return false;
		}
		if(mob.rangeToTarget()>=mob.location().maxRange())
		{
			mob.tell("There is no more room to spring back!");
			return false;
		}
		
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				ExternalPlay.postAttack(mob,target,mob.fetchWieldedItem());
				if(mob.getVictim()==target)
				{
					msg=new FullMsg(mob,target,this,Affect.MSG_RETREAT,"<S-NAME> spring(s) back!");
					if(mob.location().okAffect(msg))
					{
						mob.location().send(mob,msg);
						if(mob.rangeToTarget()<mob.location().maxRange())
						{
							msg=new FullMsg(mob,target,this,Affect.MSG_RETREAT,null);
							if(mob.location().okAffect(msg))
								mob.location().send(mob,msg);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to spring attack <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
