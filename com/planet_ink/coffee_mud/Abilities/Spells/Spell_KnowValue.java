package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_KnowValue extends Spell
{
	public Spell_KnowValue()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Know Value";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(8);

		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;
		
		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_KnowValue();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> weigh(s) the value of <T-NAMESELF> carefully.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.value()<=0)
					mob.tell(target.name()+" isn't worth anything.");
				else
				if(target.value()==0)
					mob.tell(target.name()+" is worth one puny gold piece");
				else
					mob.tell(target.name()+" is worth "+target.value()+" gold pieces");
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> weigh(s) the value of <T-NAMESELF>, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
