package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonMonster extends Spell
{
	public Spell_SummonMonster()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Monster Summoning";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Monster Summoning)";

		quality=Ability.BENEFICIAL_SELF;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_SummonMonster();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) and summon(s) help from the Java Plain.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				ExternalPlay.follow(target,mob,true);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) and call(s), but chokes on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{

		String mobID = "Dog";

		if (level >= 5)
		{
			mobID = "Orc";
		}

		if (level >= 10)
		{
			mobID = "Tiger";
		}

		if (level >= 15)
		{
			mobID = "Troll";
		}


		MOB newMOB=(MOB)CMClass.getMOB(mobID);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
