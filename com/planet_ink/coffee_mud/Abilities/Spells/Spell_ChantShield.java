package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ChantShield extends Spell
{
	public String ID() { return "Spell_ChantShield"; }
	public String name(){return "Chant Shield";}
	public String displayText(){return "(Chant Shield)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_ChantShield();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your shield against chants fades.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
		&&(invoker!=null)
		&&(!mob.amDead())
		&&(Dice.rollPercentage()<35))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"The barrier around <S-NAME> blocks off "+msg.tool().name()+"!");
			return false;
		}
		return super.okMessage(myHost,msg);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<S-NAME> look(s) protected from chants.":"^S<S-NAME> invoke(s) an anti-chant shield around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a shield, but fail(s).");

		return success;
	}
}
