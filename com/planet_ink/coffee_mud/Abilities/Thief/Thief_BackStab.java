package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_BackStab extends ThiefSkill
{

	public Thief_BackStab()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Back Stab";
		displayText="(in a dark realm of thievery)";
		miscText="";

		canTargetCode=Ability.CAN_MOBS;
		canAffectCode=0;
		
		triggerStrings.addElement("BACKSTAB");

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(7);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_BackStab();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Backstab whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely to do that.");
			return false;
		}

		Weapon weapon=null;
		if((mob.fetchWieldedItem()!=null)&&(mob.fetchWieldedItem() instanceof Weapon))
			weapon=(Weapon)mob.fetchWieldedItem();
		if(weapon==null)
		{
			mob.tell(mob,target,"Backstab <T-HIM-HER> with what? You need to wield a weapon!");
			return false;
		}
		if((weapon.weaponClassification()==Weapon.CLASS_BLUNT)
		||(weapon.weaponClassification()==Weapon.CLASS_HAMMER)
		||(weapon.weaponClassification()==Weapon.CLASS_RANGED)
		||(weapon.weaponClassification()==Weapon.CLASS_THROWN)
		||(weapon.weaponClassification()==Weapon.CLASS_STAFF))
		{
			mob.tell(mob,target,"You cannot stab anyone with "+weapon.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		int factor=(int)Math.round(Util.div(adjustedLevel(mob),4.0))+2;
		FullMsg msg=new FullMsg(mob,target,null,(auto?Affect.MSG_OK_ACTION:Affect.MSG_DELICATE_HANDS_ACT),auto?"":"<S-NAME> attempt(s) to stab <T-NAMESELF> in the back!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(!success)
				mob.location().show(target,mob,Affect.MSG_OK_VISUAL,auto?"":"<S-NAME> spot(s) <T-NAME>!");
			else
			{
				mob.envStats().setDamage(mob.envStats().damage()*factor);
				mob.envStats().setAttackAdjustment(mob.envStats().attackAdjustment()+100);
			}
			ExternalPlay.postAttack(mob,target,weapon);
			mob.recoverEnvStats();
		}
		else
			success=false;
		return success;
	}

}
