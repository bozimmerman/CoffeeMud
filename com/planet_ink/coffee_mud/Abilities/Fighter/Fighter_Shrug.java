package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Shrug extends StdAbility
{
	public String ID() { return "Fighter_Shrug"; }
	public String name(){ return "Shrug Off";}
	public String displayText(){return "(Braced for a hit)";}
	private static final String[] triggerStrings = {"SHRUGOFF"};
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Fighter_Shrug();}
	public int classificationCode(){return Ability.SKILL;}

	public boolean okAffect(Affect msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amITarget((MOB)affected))
		&&((msg.targetCode()&Affect.MASK_HURT)>0)
		&&(!msg.amISource((MOB)affected))
		&&(Sense.aliveAwakeMobile((MOB)affected,true))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon))
		{
			MOB mob=(MOB)affected;
			mob.location().show(mob,msg.source(),Affect.MSG_OK_ACTION,"<S-NAME> shrug(s) off the attack from <T-NAME>");
			unInvoke();
			return false;
		}
		return super.okAffect(msg);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat first!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_QUIETMOVEMENT,"<S-NAME> brace(s) for an attack!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to brace <S-HIM-HERSELF>, but get(s) distracted.");

		// return whether it worked
		return success;
	}
}
