package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Kick extends StdAbility
{
	public Fighter_Kick()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Kick";
		displayText="(the great kick of the warrior)";
		miscText="";

		triggerStrings.addElement("KICK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(2);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Kick();
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

		// now see if it worked
		boolean success=profficiencyCheck(mob.charStats().getStrength()-target.charStats().getStrength()-10,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			int topDamage=mob.envStats().level()+2;
			int damage=Dice.roll(1,topDamage,0);
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),auto?"<T-NAME> get(s) kicked!":"<S-NAME> kick(s) and "+ExternalPlay.standardHitWord(Weapon.TYPE_BASHING,damage)+" <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				ExternalPlay.postDamage(mob,target,this,damage);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to kick <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
