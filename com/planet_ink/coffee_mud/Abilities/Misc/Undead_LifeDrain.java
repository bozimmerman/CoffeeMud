package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Undead_LifeDrain extends StdAbility
{
	public String ID() { return "Undead_LifeDrain"; }
	public String name(){ return "Drain Life";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"DRAINLIFE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Undead_LifeDrain();	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);

		if(target==null) return false;
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);


		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			int much=mob.envStats().level();
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_UNDEAD|(auto?CMMsg.MASK_GENERAL:0),auto?"":"^S<S-NAME> clutch(es) <T-NAMESELF>, and drain(s) <T-HIS-HER> life!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.curState().adjMana(-much,mob.maxState());
				if(msg.wasModified())
					much = (int)Math.round(Util.div(much,2.0));
				ExternalPlay.postDamage(mob,target,this,much,CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,Weapon.TYPE_GASSING,"The drain <DAMAGE> <T-NAME>!");
			}
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> attempt(s) to drain life from <T-NAMESELF>, but fail(s).");

		// return whether it worked
		return success;
	}
}