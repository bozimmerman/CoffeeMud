package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_BladeBarrier extends Prayer
{
	public Prayer_BladeBarrier()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blade Barrier";
		displayText="(Blade Barrier)";

		isNeutral=true;

		baseEnvStats().setLevel(18);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_BladeBarrier();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your blade barrier disappears.");
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(invoker==null) return;
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;

		if(affect.target()==invoker)
		{
			if((affect.targetCode()==Affect.STRIKE_HANDS)
			||(affect.targetType()==Affect.HANDS))
			{
				int dmg=Dice.roll(2,invoker.envStats().level(),0);
				affect.source().location().show((MOB)affect.target(),affect.source(),Affect.VISUAL_WNOISE,"The blade barrier around <S-NAME> "+TheFight.hitWord(-1,dmg)+" <T-NAME>!");

				TheFight.doDamage(affect.source(),dmg);
			}

		}
		return;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		affectableStats.setArmor(affectableStats.armor()-mob.envStats().level());
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=mob;
		if(target==null) return false;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> pray(s) for divine protection!  A barrier of blades begin to spin around <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> pray(s) for divine protection, but nothing happens.");


		// return whether it worked
		return success;
	}
}
