package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Enlarge extends Spell
{

	private static final String addOnString=" of ENORMOUS SIZE!!!";

	public Spell_Enlarge()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Enlarge Object";

		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Enlarge();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(affectableStats.weight()+9999);
		affectableStats.setHeight(affectableStats.height()+9999);
		affectableStats.setReplacementName(affected.name()+addOnString);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(mob.isMine(target))
		{
			mob.tell("You'd better put it down first.");
			return false;
		}
		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already HUGE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> grow(s) to an enormous size!");
				beneficialAffect(mob,target,100);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, encanting but nothing happens.");


		// return whether it worked
		return success;
	}
}