package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Snatch extends StdAbility
{
	public String ID() { return "Thief_Snatch"; }
	public String name(){ return "Weapon Snatch";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"SNATCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Snatch();}

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
		Item weapon=mob.fetchWieldedItem();
		if(weapon==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		else
		if(mob.amWearingSomethingHere(Item.HELD))
		{
			mob.tell("Your other hand needs to be free to do a weapon snatch.");
			return false;
		}

		Item hisItem=mob.getVictim().fetchWieldedItem();
		if((hisItem!=null)
		   ||(!(hisItem instanceof Weapon))
		   ||((((Weapon)hisItem).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}
		else
		if(hisItem.rawLogicalAnd())
		{
			mob.tell("You can't snatch a two-handed weapon!");
			return false;
		}
		Weapon hisWeapon=(Weapon)hisItem;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-mob.envStats().level();
		if(levelDiff>0)
			levelDiff=levelDiff*6;
		else
			levelDiff=0;
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(-levelDiff,auto)&&(hit);
		if((success)
		   &&(hisWeapon!=null)
		   &&((hisWeapon.rawProperLocationBitmap()==Item.WIELD)
			  ||(hisWeapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
		{
			FullMsg msg=new FullMsg(mob.getVictim(),hisWeapon,null,Affect.MSG_DROP,null);
			FullMsg msg2=new FullMsg(mob,null,this,Affect.MSG_THIEF_ACT,null);
			if((mob.location().okAffect(mob,msg))&&(mob.location().okAffect(mob,msg2)))
			{
				mob.location().send(mob.getVictim(),msg);
				mob.location().send(mob,msg2);
				mob.location().show(mob,mob.getVictim(),Affect.MSG_OK_VISUAL,"<S-NAME> disarm(s) <T-NAMESELF>!");
				if(mob.location().isContent(hisWeapon))
				{
					ExternalPlay.get(mob,null,hisWeapon,true);
					if(mob.isMine(hisWeapon))
					{
						msg=new FullMsg(mob,hisWeapon,null,Affect.MSG_HOLD,"<S-NAME> snatch(es) the <T-NAME> out of mid-air!");
						if(mob.location().okAffect(mob,msg))
							mob.location().send(mob,msg);
					}
				}
			}
		}
		else
			maliciousFizzle(mob,mob.getVictim(),"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}
}