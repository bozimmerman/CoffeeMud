package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Dragonfire extends Spell
	implements EvocationDevotion, InvocationDevotion
{
	public Spell_Dragonfire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dragonfire";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Dragonfire)";

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(23);

		addQualifyingClass("Mage",23);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Dragonfire();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth burning.");
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

			mob.location().show(mob,null,affectType,auto?"A blast of flames erupt!":"<S-NAME> blast(s) flames from <S-HIS-HER> mouth!");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_FIRE,null);
				if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						mob.location().send(mob,msg2);
						invoker=mob;

						int damage = 0;
						int maxDie =  mob.envStats().level();
						if (maxDie > 30)
							maxDie = 30;
						damage += Dice.roll(maxDie,3,1);
						if(msg2.wasModified())
							damage = (int)Math.round(Util.div(damage,2.0));

						mob.location().show(target,null,Affect.MSG_OK_ACTION,"The flames "+ExternalPlay.hitWord(Weapon.TYPE_BURNING,damage)+" <S-NAME>!");
						ExternalPlay.postDamage(mob,target,this,damage);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> puff(s) smoke from <S-HIS-HER> mouth.");


		// return whether it worked
		return success;
	}
}
