package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ClarifyScroll extends Spell
{
	public Spell_ClarifyScroll()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Clarify Scroll";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(17);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ClarifyScroll();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,null,givenTarget,commands);
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

		mob.charStats().getMyClass().loseExperience(mob,50);
		mob.curState().setMana(0);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> wave(s) <S-HIS-HER> fingers at <T-NAMESELF>, uttering a magical phrase.");
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
