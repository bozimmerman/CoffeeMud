package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Cartwheel extends StdAbility
{
	public String ID() { return "Fighter_Cartwheel"; }
	public String name(){ return "Cartwheel";}
	private static final String[] triggerStrings = {"CARTWHEEL"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_Cartwheel();}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		MOB victim=mob.getVictim();
		if(victim==null)
		{
			mob.tell("You can only do this in combat!");
			return false;
		}
		if(mob.rangeToTarget()>=mob.location().maxRange())
		{
			mob.tell("You can not get any further away here!");
			return false;
		}

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
			FullMsg msg=new FullMsg(mob,victim,this,Affect.MSG_RETREAT,"<S-NAME> cartwheel(s) away from <T-NAMESELF>!");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(mob.rangeToTarget()<mob.location().maxRange())
				{
					msg=new FullMsg(mob,victim,this,Affect.MSG_RETREAT,null);
					if(mob.location().okAffect(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to cartwheel and fail(s).");

		// return whether it worked
		return success;
	}
}
