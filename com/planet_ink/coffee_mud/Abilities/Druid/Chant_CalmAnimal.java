package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_CalmAnimal extends Chant
{
	public Chant_CalmAnimal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Calm Animal";
		baseEnvStats().setLevel(21);
		quality=Ability.OK_OTHERS;
		canAffectCode=0;
		canTargetCode=Ability.CAN_MOBS;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_CalmAnimal();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.charStats().getStat(CharStats.INTELLIGENCE)>1)
		{
			mob.tell(target.name()+" is not an animal!");
			return false;
		}

		if(!target.isInCombat())
		{
			mob.tell(target.name()+" doesn't seem particularly enraged at the moment.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> become(s) surrounded by a natural light.":"<S-NAME> chant(s) to <T-NAMESELF> for calm.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,3);
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB mob2=mob.location().fetchInhabitant(i);
					if((mob2.getVictim()==target)||(mob2==target))
						mob2.makePeace();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
