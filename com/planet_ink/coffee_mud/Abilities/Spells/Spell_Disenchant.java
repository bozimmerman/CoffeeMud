package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Disenchant extends Spell
{
	public String ID() { return "Spell_Disenchant"; }
	public String name(){return "Disenchant";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Disenchant();	}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()<=0)
					mob.tell(target.name()+" doesn't seem to be enchanted.");
				else
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,target.name()+" fades and becomes dull!");
					target.baseEnvStats().setLevel(target.baseEnvStats().level()-(mob.envStats().level()*3));
					if(target.baseEnvStats().level()<=0)
						target.baseEnvStats().setLevel(1);
					target.baseEnvStats().setAbility(0);
					if((target.baseEnvStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS)
						target.baseEnvStats().setDisposition(target.baseEnvStats().disposition()-EnvStats.IS_BONUS);
					target.recoverEnvStats();
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}