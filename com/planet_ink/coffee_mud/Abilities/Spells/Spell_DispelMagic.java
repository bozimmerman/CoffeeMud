package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_DispelMagic extends Spell
{
	public String ID() { return "Spell_DispelMagic"; }
	public String name(){return "Dispel Magic";}
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS|CAN_EXITS|CAN_ROOMS;}
	public Environmental newInstance(){	return new Spell_DispelMagic();	}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		Ability revokeThis=null;
		boolean foundSomethingAtLeast=false;
		for(int a=0;a<target.numAffects();a++)
		{
			Ability A=(Ability)target.fetchAffect(a);
			if((A!=null)&&(A.canBeUninvoked())
			&&(((A.classificationCode()&Ability.SPELL)>0)||((A.classificationCode()&Ability.CHANT)>0)))
			{
				foundSomethingAtLeast=true;
				if((A.invoker()!=null)
				&&(((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
				   ||((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT))
				&&((A.invoker()==mob)
				||(A.invoker().envStats().level()<=mob.envStats().level()+5)))
					revokeThis=A;
			}
		}

		if(revokeThis==null)
		{
			if(foundSomethingAtLeast)
				mob.tell(mob,target,null,"The magic on <T-NAME> appears too powerful to dispel.");
			else
			if(auto)
				mob.tell("Nothing seems to be happening.");
			else
				mob.tell(mob,target,null,"<T-NAME> does not appear to be affected by anything you can dispel.");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		
		int diff=revokeThis.invoker().envStats().level()-mob.envStats().level();
		if(diff<0) diff=0;
		else diff=diff*-20;

		boolean success=profficiencyCheck(diff,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?revokeThis.name()+" is dispelled from <T-NAME>.":"^S<S-NAME> dispel(s) "+revokeThis.name()+" from <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
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