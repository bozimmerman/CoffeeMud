package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Disenchant extends Spell
{
	public Spell_Disenchant()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Disenchant";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(22);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Disenchant();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_EVOCATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> hold(s) <T-NAMESELF> and chant(s).");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.envStats().ability()<=0)
					mob.tell(target.name()+" doesn't seem to be enchanted.");
				else
				{
					mob.location().show(mob,target,Affect.MSG_OK_VISUAL,target.name()+" fades and becomes dull!");
					target.baseEnvStats().setLevel(target.baseEnvStats().level()-(baseEnvStats().level()*3));
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
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and chant(s), looking very frustrated.");


		// return whether it worked
		return success;
	}
}