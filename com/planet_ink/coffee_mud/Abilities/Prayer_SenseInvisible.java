package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_SenseInvisible extends Prayer
{
	public Prayer_SenseInvisible()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sense Invisible";
		displayText="(Sense Invisible)";

		isNeutral=true;

		baseEnvStats().setLevel(10);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_SenseInvisible();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_INVISIBLE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The clearness fades from your eyes.");
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already affected by "+name()+".");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) for divine revelation, and <S-HIS-HER> eyes become clear.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialFizzle(mob,null,"<S-NAME> pray(s) for divine revelation, but <S-HIS-HER> prayer is not heard.");


		// return whether it worked
		return success;
	}
}
