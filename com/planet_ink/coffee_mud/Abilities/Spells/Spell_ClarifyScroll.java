package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ClarifyScroll extends Spell
{
	public String ID() { return "Spell_ClarifyScroll"; }
	public String name(){return "Clarify Scroll";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_ClarifyScroll();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Scroll))
		{
			mob.tell("You can't clarify that.");
			return false;
		}
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.charStats().getCurrentClass().loseExperience(mob,25);
		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> wave(s) <S-HIS-HER> fingers at <T-NAMESELF>, uttering a magical phrase.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"The words on <T-NAME> become more definite!");
				((Scroll)target).setUsesRemaining(((Scroll)target).usesRemaining()+((Scroll)target).numSpells());
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> fingers at <T-NAMESELF>, uttering a magical phrase, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
