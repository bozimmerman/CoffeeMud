package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_StoneFlesh extends Spell
{
	public Spell_StoneFlesh()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Stone Flesh";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(19);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_StoneFlesh();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_EVOCATION;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		Environmental target=getAnyTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Ability revokeThis=null;
		for(int a=0;a<target.numAffects();a++)
		{
			Ability A=(Ability)target.fetchAffect(a);
			if((A!=null)&&(A.canBeUninvoked())&&(A.ID().equalsIgnoreCase("Spell_FleshStone")))
			{
				revokeThis=A;
				break;
			}
		}

		if(revokeThis==null)
		{
			mob.tell(mob,target,"<T-NAME> can not be affected by this spell.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> dispel(s) "+revokeThis.name()+" from <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to dispel "+revokeThis.name()+" from <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}