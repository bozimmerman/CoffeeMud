package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_BladeBarrier extends Prayer
{
	public Prayer_BladeBarrier()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blade Barrier";
		displayText="(Blade Barrier)";

		holyQuality=Prayer.HOLY_NEUTRAL;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(18);

		addQualifyingClass("Cleric",baseEnvStats().level());
		addQualifyingClass("Paladin",baseEnvStats().level()+4);
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
			if((Dice.rollPercentage()>50+((MOB)affect.target()).charStats().getDexterity())
			&&(!Util.bset(affect.targetCode(),Affect.ACT_GENERAL))
			&&((Util.bset(affect.targetMajor(),Affect.AFF_TOUCHED))
			   ||(Util.bset(affect.targetMajor(),Affect.AFF_MOVEDON))))
			{
				int level=(int)Math.round(Util.div(invoker.envStats().level(),6.0));
				if(level>5) level=5;
				int damage=Dice.roll(2,level,0);
				affect.source().location().show((MOB)affect.target(),affect.source(),Affect.MSG_OK_ACTION,"The blade barrier around <S-NAME> "+ExternalPlay.hitWord(-1,damage)+" <T-NAME>!");
				ExternalPlay.postDamage((MOB)affect.target(),affect.source(),this,damage);
			}

		}
		return;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		affectableStats.setArmor(affectableStats.armor()-mob.envStats().level());
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,(auto?"":"<S-NAME> pray(s) for divine protection!  ")+"A barrier of blades begin to spin around <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for divine protection, but nothing happens.");


		// return whether it worked
		return success;
	}
}
