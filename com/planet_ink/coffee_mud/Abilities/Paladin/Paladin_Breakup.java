package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_Breakup extends StdAbility
{
	public String ID() { return "Paladin_Breakup"; }
	public String name(){ return "Breakup Fight";}
	private static final String[] triggerStrings = {"BREAKUP"};
	public int quality(){return Ability.OK_OTHERS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Paladin_Breakup();}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("You must end combat before trying to break up someone elses fight.");
			return false;
		}
		if((!auto)&&(mob.getAlignment()<650))
		{
			mob.tell("You don't feel worthy of a such a good act.");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(!target.isInCombat())
		{
			mob.tell(target.name()+" is not fighting anyone!");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"<T-NAME> exude(s) a peaceful aura.":"<S-NAME> break(s) up the fight between <T-NAME> and "+target.getVictim().name()+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.makePeace();
				if((target.getVictim()!=null)
				   &&(target.getVictim().getVictim()==target))
						target.getVictim().makePeace();
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to break up <T-NAME>'s fight, but fail(s).");


		// return whether it worked
		return success;
	}
}