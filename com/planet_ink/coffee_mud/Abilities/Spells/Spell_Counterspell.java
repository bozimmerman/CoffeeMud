package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Counterspell extends Spell
{
	public String ID() { return "Spell_Counterspell"; }
	public String name(){return "Counterspell";}
	public String displayText(){return "(Counterspell)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Counterspell();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	public boolean ticked=false;
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your counterspell fades.");

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
		&&((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
		&&(invoker!=null)
		&&(!mob.amDead())
		&&(Dice.rollPercentage()<(70+(2*(mob.envStats().level()-affect.source().envStats().level())))))
		{
			mob.location().show(mob,affect.source(),Affect.MSG_OK_VISUAL,"The barrier around <S-NAME> dispels the "+affect.tool().name()+" from <T-NAME>!");
			tickDown=0;
			return false;
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<S-NAME> feel(s) protected from spells.":"^S<S-NAME> invoke(s) a counterspell barrier around <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				ticked=false;
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a counterspell barrier, but fail(s).");

		return success;
	}
}
