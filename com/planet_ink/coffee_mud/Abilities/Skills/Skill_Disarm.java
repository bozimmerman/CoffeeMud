package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Disarm extends StdAbility
{
	public String ID() { return "Skill_Disarm"; }
	public String name(){ return "Disarm";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"DISARM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to disarm!");
			return false;
		}
		if(mob.fetchWieldedItem()==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		Item hisWeapon=mob.getVictim().fetchWieldedItem();
		if(hisWeapon==null) hisWeapon=mob.getVictim().fetchFirstWornItem(Item.HELD);
		if((hisWeapon==null)
		||(!(hisWeapon instanceof Weapon))
		||((((Weapon)hisWeapon).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell(mob.getVictim().charStats().HeShe()+" is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-mob.envStats().level();
		if(levelDiff>0)
			levelDiff=levelDiff*5;
		else
			levelDiff=0;
		boolean hit=(auto)||(Dice.normalizeAndRollLess(mob.adjustedAttackBonus(mob.getVictim())+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(mob,-levelDiff,auto)&&(hit);
		if((success)&&(hisWeapon!=null)
		   &&((hisWeapon.fitsOn(Item.WIELD))
			  ||hisWeapon.fitsOn(Item.WIELD|Item.HELD)))
		{
			if(mob.location().show(mob,mob.getVictim(),this,CMMsg.MSG_NOISYMOVEMENT,null))
			{
				FullMsg msg=new FullMsg(mob.getVictim(),hisWeapon,null,CMMsg.MSG_DROP,null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob.getVictim(),msg);
					mob.location().show(mob,mob.getVictim(),CMMsg.MSG_NOISYMOVEMENT,auto?"<T-NAME> is disarmed!":"<S-NAME> disarm(s) <T-NAMESELF>!");
				}
			}
		}
		else
			maliciousFizzle(mob,mob.getVictim(),"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}

}