package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Fireball extends Spell
	implements EvocationDevotion, InvocationDevotion
{
	public Spell_Fireball()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fireball";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Fireball spell)";

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Fireball();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"A huge fireball appears and blazes towards <T-NAME>!":"<S-NAME> point(s) and chant(s) at <T-NAMESELF>, and shoot(s) forth a fireball!");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_FIRE,null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().send(mob,msg2);
					invoker=mob;

                    int numDice = 0;
                    if (mob.envStats().level() > 20)
                        numDice = 10;
                    else
                        numDice = (int)Math.round(new Integer(mob.envStats().level()).doubleValue()/2.0);

					int damage = Dice.roll(numDice, 6, 10);
					if(msg2.wasModified())
						damage = (int)Math.round(Util.div(damage,2.0));
					mob.location().show(target,null,Affect.MSG_OK_ACTION,"The blast "+ExternalPlay.hitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
					ExternalPlay.postDamage(mob,target,this,damage);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and chant(s) at <T-NAMESELF>, but nothing more happens.");

		return success;
	}
}
