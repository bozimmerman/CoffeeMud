package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_WeaponBreak extends StdAbility
{
	public String ID() { return "Fighter_WeaponBreak"; }
	public String name(){ return "Weapon Break";}
	private static final String[] triggerStrings = {"BREAK"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int maxRange(){return 1;}
	public Environmental newInstance(){	return new Fighter_WeaponBreak();}
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
		if((!auto)&&(mob.fetchWieldedItem()==null))
		{
			mob.tell("You need a weapon to break someone elses!");
			return false;
		}
		if((mob.getVictim().fetchWieldedItem()==null)
		||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))
		||(((Weapon)mob.getVictim().fetchWieldedItem()).weaponClassification()==Weapon.CLASS_NATURAL))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-adjustedLevel(mob);
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		Item hisWeapon=mob.getVictim().fetchWieldedItem();
		int chance=(-levelDiff)+(-(mob.getVictim().charStats().getStat(CharStats.DEXTERITY)*2));
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(chance,auto)&&(hit);
		if((success)
		   &&(hisWeapon!=null)
		   &&(hisWeapon.envStats().ability()==0)
		   &&(!Sense.isABonusItems(hisWeapon))
		&&((hisWeapon.rawProperLocationBitmap()==Item.WIELD)
		   ||(hisWeapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
		{
			String str=auto?hisWeapon.name()+" break(s) in <T-HIS-HER> hands!":"<S-NAME> disarm(s) <T-NAMESELF> and destroy(s) "+hisWeapon.name()+"!";
			hisWeapon.remove();
			FullMsg msg=new FullMsg(mob,mob.getVictim(),this,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				hisWeapon.destroyThis();
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			String str=auto?"":"<S-NAME> attempt(s) to destroy "+hisWeapon.name()+" and fail(s)!";
			FullMsg msg=new FullMsg(mob,mob.getVictim(),null,Affect.MSG_NOISYMOVEMENT,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}