package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Immunity extends Spell
{
	public String ID() { return "Spell_Immunity"; }
	public String name(){return "Immunity";}
	public String displayText(){return "(Immunity to "+immunityName+")";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	private int immunityType=-1;
	private String immunityName="";

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your immunity has passed.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==immunityType)
		&&(!mob.amDead())
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
		{
			mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seems immune to "+immunityName+" attack from <T-NAME>.");
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

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) an immunity barrier.":"^S<S-NAME> invoke(s) an immunity barrier around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				switch(Dice.roll(1,5,0))
				{
				case 1:
					immunityType=CMMsg.TYP_ACID;
					immunityName="acid";
					break;
				case 2:
					immunityType=CMMsg.TYP_FIRE;
					immunityName="fire";
					break;
				case 3:
					immunityType=CMMsg.TYP_GAS;
					immunityName="gas";
					break;
				case 4:
					immunityType=CMMsg.TYP_COLD;
					immunityName="cold";
					break;
				case 5:
					immunityType=CMMsg.TYP_ELECTRIC;
					immunityName="electricity";
					break;
				}
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an immunity barrier, but fail(s).");

		return success;
	}
}
