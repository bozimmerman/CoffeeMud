package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_ShockingGrasp extends Spell
	implements AlterationDevotion
{
	public Spell_ShockingGrasp()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shocking Grasp";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Shocking Grasp spell)";


		malicious=true;

		baseEnvStats().setLevel(6);

		addQualifyingClass(new Mage().ID(),6);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ShockingGrasp();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> grab(s) at <T-NAME>.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_ELECTRIC,Affect.SOUND_MAGIC,null);
			if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().send(mob,msg2);
					if(!msg2.wasModified())
					{
						invoker=mob;

						int damage = Dice.roll(1,8,mob.envStats().level());
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The grasp of <S-NAME> "+TheFight.hitWord(-1,damage)+" <T-NAME>!");

						TheFight.doDamage(target, damage);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> grab(s) at <T-NAME>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
