package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_CoupDeGrace extends StdAbility
{
	public String ID() { return "Fighter_CoupDeGrace"; }
	public String name(){ return "Coup de Grace";}
	private static final String[] triggerStrings = {"COUP","COUPDEGRACE"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int maxRange(){return 0;}
	public Environmental newInstance(){	return new Fighter_CoupDeGrace();}
	public int classificationCode(){ return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to try that!");
			return false;
		}
		Weapon w=mob.fetchWieldedItem();
		if((!auto)&&(w==null))
		{
			mob.tell("You need a weapon to break someone elses!");
			return false;
		}
		if(!Sense.isSleeping(mob.getVictim()))
		{
			mob.tell(mob.getVictim().charStats().HeShe()+" is not prone!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-adjustedLevel(mob);
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		int chance=(-levelDiff)+(-(mob.getVictim().charStats().getStat(CharStats.DEXTERITY)*2));
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(chance,auto)&&(hit);
		if(success)
		{
			String str=auto?"":"<S-NAME> attempt(s) a Coup de Grace against <T-NAME>!";
			FullMsg msg=new FullMsg(mob,mob.getVictim(),this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			String str=auto?"":"<S-NAME> attempt(s) a Coup de Grace and fail(s)!";
			FullMsg msg=new FullMsg(mob,mob.getVictim(),null,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}