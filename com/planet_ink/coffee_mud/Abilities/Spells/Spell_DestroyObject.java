package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_DestroyObject extends Spell
{
	public Spell_DestroyObject()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Destroy Object";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(12);
		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;
		

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DestroyObject();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_EVOCATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands, givenTarget, auto))
			return false;

		boolean success=profficiencyCheck(((mob.envStats().level()-target.envStats().level())*25),auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,
									(auto?"<T-NAME> begins to glow!"
										 :"^S<S-NAME> incant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> vanish(es) into thin air!");
				((Item)target).destroyThis();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
