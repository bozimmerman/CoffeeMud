package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_HealingHands extends StdAbility
{
	public String ID() { return "Paladin_HealingHands"; }
	public String name(){ return "Healing Hands";}
	private static final String[] triggerStrings = {"HANDS"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Paladin_HealingHands();}
	protected long lastDone=0;
													 

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		if((!auto)&&(mob.getAlignment()<650))
		{
			mob.tell("Your alignment has alienated your god from you.");
			return false;
		}

		if(mob.curState().getMana()==0)
		{
			mob.tell("You don't have enough mana to do that.");
			return false;
		}
		
		long now=System.currentTimeMillis();
		if((now-lastDone)<1000)
		{
			mob.tell("You need a second to regather your strength.");
			return false;
		}
		lastDone=now;

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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_CAST_SOMANTIC_SPELL,auto?"A pair of celestial hands surround <T-NAME>":"^S<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.curState().adjMana(-(1+(int)Math.round(Util.div(adjustedLevel(mob),5.0))),mob.maxState());
				int healing=1+(int)Math.round(Util.div(adjustedLevel(mob),5.0));
				target.curState().adjHitPoints(healing,target.maxState());
				target.tell("You feel a little better!");
			}
		}
		else
			return beneficialVisualFizzle(mob,mob,"<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>, but <S-HIS-HER> god does not heed.");


		// return whether it worked
		return success;
	}

}
