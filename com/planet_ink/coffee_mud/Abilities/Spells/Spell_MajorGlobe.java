package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MajorGlobe extends Spell
{
	public String ID() { return "Spell_MajorGlobe"; }
	public String name(){return "Greater Globe";}
	public String displayText(){return "(Greater Globe of Invulnerability)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_MajorGlobe();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	int amountAbsorbed=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your anti-magic globe fades.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
			||((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			||((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.CHANT))
		&&(!mob.amDead())
		&&(CMAble.lowestQualifyingLevel(affect.tool().ID())<=15)
		&&(profficiencyCheck(0,false)))
		{
			amountAbsorbed+=CMAble.lowestQualifyingLevel(affect.tool().ID());
			mob.location().show(mob,affect.source(),Affect.MSG_OK_VISUAL,"The globe around <S-NAME> absorbs the "+affect.tool().name()+" from <T-NAME>!");
			return false;
		}
		if((invoker!=null)&&(amountAbsorbed>(invoker.envStats().level()*4)))
			unInvoke();
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"A great anti-magic field envelopes <T-NAME>!":"^S<S-NAME> invoke(s) a great anti-magic globe of protection around <T-NAMESELF>.^?"));
			if(mob.location().okAffect(msg))
			{
				amountAbsorbed=0;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a great anti-magic globe, but fail(s).");

		return success;
	}
}
