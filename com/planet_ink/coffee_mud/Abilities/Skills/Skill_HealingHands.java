package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_HealingHands extends StdAbility
{
	public Skill_HealingHands()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Lay Hands";
		displayText="(in the holy dominion of the gods)";
		miscText="";
		triggerStrings.addElement("HANDS");

		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(1);

		canBeUninvoked=true;
		isAutoinvoked=false;
		recoverEnvStats();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public Environmental newInstance()
	{
		return new Skill_HealingHands();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		if((!auto)&&(mob.getAlignment()<500))
		{
			mob.tell("Your alignment has alienated your god from you.");
			return false;
		}

		if(mob.curState().getMana()==0)
		{
			mob.tell("You don't have enough mana to do that.");
			return false;
		}

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		helpProfficiency(mob);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_CAST_SOMANTIC_SPELL,auto?"A pair of celestial hands surround <T-NAME>":"<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.curState().adjMana(-1,mob.maxState());
				int healing=1+(int)Math.round(Util.div(mob.envStats().level(),10.0));
				target.curState().adjHitPoints(healing,target.maxState());
				target.tell("You feel a little better!");
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>, but <S-HIS-HER> god does not heed.");


		// return whether it worked
		return success;
	}

}
