package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_HealUndead extends Prayer
{
	public String ID() { return "Prayer_HealUndead"; }
	public String name(){ return "Heal Undead";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_HEALING;}
	public Environmental newInstance(){	return new Prayer_HealUndead();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		boolean undead=target.charStats().getMyRace().racialCategory().equals("Undead");
		if((!undead)&&(!auto))
		{
			mob.tell("Only the undead are affected by this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,(undead?0:CMMsg.MASK_MALICIOUS)|affectType(auto),auto?"<T-NAME> become(s) surrounded by a white light.":"^S<S-NAME> "+prayWord(mob)+" for negative healing power into <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int healing=Dice.roll(5,adjustedLevel(mob),10);
				if(undead)
				{
					target.curState().adjHitPoints(healing,target.maxState());
					target.tell("You feel tons better!");
				}
				else
					MUDFight.postDamage(mob,target,this,healing,
											CMMsg.MASK_GENERAL|CMMsg.TYP_UNDEAD,
											Weapon.TYPE_BURNING,
											"The unholy spell <DAMAGE> <T-NAME>!");

			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
