package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Reinforce extends Spell
{
	public Spell_Reinforce()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Reinforce";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;

		baseEnvStats().setLevel(4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Reinforce();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;
		if(!target.subjectToWearAndTear())
		{	mob.tell(target.name()+" cannot be reinforced."); return false;}
		else
		if(target.usesRemaining()<100)
		{	mob.tell(target.name()+" must be repaired before it can be reinforced."); return false;}

		if(!super.invoke(mob,commands, givenTarget, auto))
			return false;

		boolean success=profficiencyCheck(((mob.envStats().level()-target.envStats().level())*5),auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,
									(auto?"<T-NAME> begins to shimmer!"
										 :"^S<S-NAME> incant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.usesRemaining()>=150)
					mob.tell(target.name()+" cannot be reinforced further.");
				else
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> begin(s) to glow and harden!");
					target.setUsesRemaining(target.usesRemaining()+50);
					target.recoverEnvStats();
					mob.location().recoverRoomStats();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
