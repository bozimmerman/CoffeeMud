package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Nondetection extends Spell
{
	int amountAbsorbed=0;
	public Spell_Nondetection()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Nondetection";
		displayText="(Nondetection)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_OTHERS;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		baseEnvStats().setLevel(9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Nondetection();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ABJURATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		mob.tell("Your undetectable field fades.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&((((Ability)affect.tool()).classificationCode()&ALL_DOMAINS)==Ability.DOMAIN_DIVINATION)
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
			return false;
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,(auto?"An undetectable field envelopes <T-NAME>!":"<S-NAME> invoke(s) an undetectable globe of protection around <T-NAMESELF>."));
			if(mob.location().okAffect(msg))
			{
				amountAbsorbed=0;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an undetectable globe, but fail(s).");

		return success;
	}
}
