package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MindBlock extends Spell
{
	public String ID() { return "Spell_MindBlock"; }
	public String name(){return "Mind Block";}
	public String displayText(){return "(Mind Block)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_MindBlock();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	int amountAbsorbed=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
			mob.tell("Your anti-psionic field fades.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			boolean yep=(affect.targetMinor()==Affect.TYP_MIND);
			if((!yep)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability))
			{
				Ability A=(Ability)affect.tool();
				if(((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ILLUSION)
				||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ENCHANTMENT))
				   yep=true;
			}
			if(yep)
			{
				affect.source().tell(affect.source(),mob,"<T-NAME> seem(s) unaffected.");
				return false;
			}
		}
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A anti-psionic field envelopes <T-NAME>!":"^S<S-NAME> invoke(s) an anti-psionic field of protection around <T-NAMESELF>.^?"));
			if(mob.location().okAffect(msg))
			{
				amountAbsorbed=0;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an anti-psionic field, but fail(s).");

		return success;
	}
}
