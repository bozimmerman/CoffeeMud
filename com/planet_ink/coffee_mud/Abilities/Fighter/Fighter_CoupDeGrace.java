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
		Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if(!auto)
		{
			if((w==null)||(!(w instanceof Weapon)))
			{
				mob.tell("You cannot coup de grace without a weapon!");
				return false;
			}
			ww=(Weapon)w;
			if((ww.weaponClassification()!=Weapon.TYPE_SLASHING)
			&&(ww.weaponClassification()!=Weapon.TYPE_PIERCING))
			{
				mob.tell("You cannot coup de grace with a "+ww.name()+"!");
				return false;
			}
			if(mob.curState().getMovement()<150)
			{
				mob.tell("You don't have the energy to try it.");
				return false;
			}
			if(!Sense.isSleeping(mob.getVictim()))
			{
				mob.tell(mob.getVictim().charStats().HeShe()+" is not prone!");
				return false;
			}
		}

		MOB target=mob.getVictim();
		int dmg=target.curState().getHitPoints();
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob);
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		mob.curState().adjMovement(-150,mob.maxState());
		int chance=(-levelDiff)+(-(target.charStats().getStat(CharStats.CONSTITUTION)*2));
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(chance,auto)&&(hit);
		if((success)&&((dmg<50)||(dmg<(target.maxState().getHitPoints()/2))))
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(dmg<50) target.curState().setHitPoints(0);
				ExternalPlay.postDamage(mob,target,ww,dmg,Affect.MSG_WEAPONATTACK,ww.weaponClassification(),auto?"":"<S-NAME> rear(s) back and Coup de Graces <T-NAME>!");
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			String str=auto?"":"<S-NAME> attempt(s) a Coup de Grace and fail(s)!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MASK_MALICIOUS|Affect.MSG_OK_ACTION,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}