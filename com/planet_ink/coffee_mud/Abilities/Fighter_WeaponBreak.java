package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Fighter_WeaponBreak extends StdAbility
{

	public Fighter_WeaponBreak()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Weapon Break";
		displayText="(the great power of the warrior)";
		miscText="";

		triggerStrings.addElement("BREAK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		addQualifyingClass(new Fighter().ID(),14);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_WeaponBreak();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.fetchWieldedItem()==null)
		{
			mob.tell("You need a weapon to break someone elses!");
			return false;
		}
		if((mob.getVictim().fetchWieldedItem()==null)||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))
		||((mob.getVictim().fetchWieldedItem() instanceof Natural)))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		Item weapon=mob.fetchWieldedItem();
		Item hisWeapon=mob.getVictim().fetchWieldedItem();
		int oldAtt=mob.envStats().attackAdjustment();
		mob.envStats().setAttackAdjustment(oldAtt-25);

		boolean success=profficiencyCheck(-(mob.getVictim().charStats().getDexterity()))&&(TheFight.isHit(mob,mob.getVictim()));

		mob.envStats().setAttackAdjustment(oldAtt);
		if((!success)||(hisWeapon.envStats().ability()>0)||(Sense.isABonusItems(hisWeapon)))
		{
			String str="<S-NAME> attempt(s) to destroy "+hisWeapon.name()+" and fail(s)!";
			FullMsg msg=new FullMsg(mob,mob.getVictim(),null,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		{
			String str="<S-NAME> disarm(s) <T-NAME> and destroy(s) "+hisWeapon.name()+"!";
			hisWeapon.remove();
			FullMsg msg=new FullMsg(mob,mob.getVictim(),null,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				hisWeapon.destroyThis();
				mob.location().recoverRoomStats();
			}
		}
		return success;
	}

}