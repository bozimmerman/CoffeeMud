package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Nondetection extends Spell
{
	public String ID() { return "Spell_Nondetection"; }
	public String name(){return "Nondetection";}
	public String displayText(){return "(Nondetection)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Nondetection();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	int amountAbsorbed=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your undetectable field fades.");

		super.unInvoke();

	}


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&((((Ability)affect.tool()).classificationCode()&ALL_DOMAINS)==Ability.DOMAIN_DIVINATION)
		&&(!mob.amDead())
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(0,false)))
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"An undetectable field envelopes <T-NAME>!":"^S<S-NAME> invoke(s) an undetectable globe of protection around <T-NAMESELF>.^?"));
			if(mob.location().okAffect(mob,msg))
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
