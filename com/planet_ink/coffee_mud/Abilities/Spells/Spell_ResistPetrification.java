package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ResistPetrification extends Spell
{
	public String ID() { return "Spell_ResistPetrification"; }
	public String name(){return "Resist Petrification";}
	public String displayText(){return "(Resist Petrification)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_ResistPetrification();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your flowing protection dissipates.");

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
		&&(affect.tool().ID().equalsIgnoreCase("Spell_FleshStone"))
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			mob.location().show(mob,affect.source(),Affect.MSG_OK_VISUAL,"The barrier around <S-NAME> absorbs the Stone to Flesh spell from <T-NAME>!");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<S-NAME> feel(s) flowingly protected.":"^S<S-NAME> invoke(s) a flowing barrier of protection around <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a flowing barrier, but fail(s).");

		return success;
	}
}
