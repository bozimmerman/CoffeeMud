package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_ChainLightening extends Spell
	implements EvocationDevotion
{
	public Spell_ChainLightening()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Chain Lightning";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Chain Lightning)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
		minRange=1;
		maxRange=1;
	}

	public Environmental newInstance()
	{
		return new Spell_ChainLightening();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth electrocuting.");
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

			mob.location().show(mob,null,affectType,auto?"A thunderous crack of lightning erupts!":"<S-NAME> invoke(s) a thunderous crack of lightning.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ELECTRIC,null);
				if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						mob.location().send(mob,msg2);
						invoker=mob;

						int damage = 0;
						int maxDie =  mob.envStats().level();
						if (maxDie > 10)
							maxDie = 10;
						damage += Dice.roll(maxDie,4,1);
						if(msg2.wasModified())
							damage = (int)Math.round(Util.div(damage,2.0));
						if(target.location()==mob.location())
						{
							target.location().show(target,null,Affect.MSG_OK_ACTION,"The strike "+ExternalPlay.standardHitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
							ExternalPlay.postDamage(mob,target,this,damage);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}