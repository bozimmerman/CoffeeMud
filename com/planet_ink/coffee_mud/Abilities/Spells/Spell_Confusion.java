package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Confusion extends Spell
{
	boolean notAgain=false;
	
	public Spell_Confusion()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Confusion";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Confusion spell)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(15);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Confusion();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(tickID);

		if(!super.tick(tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.isInCombat())
			mob.setVictim(mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1)));
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You feel less confused.");
		ExternalPlay.standIfNecessary(mob);
	}



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

		boolean success=profficiencyCheck(-target.envStats().level(),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> enchant(s) <T-NAMESELF>!");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					success=maliciousAffect(mob,target,15,-1);
					if(success)
						if(target.location()==mob.location())
							target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> look(s) confused!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to enchant <T-NAMESELF>, but the spell fizzles");

		// return whether it worked
		return success;
	}
}
