package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_NeutralizePoison extends Chant
{
	public String ID() { return "Chant_NeutralizePoison"; }
	public String name(){ return "Neutralize Poison";}
	public int quality(){return Ability.OK_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_NeutralizePoison();}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numAffects();a++)
		{
			Ability A=fromMe.fetchAffect(a);
			if((A!=null)&&(A.classificationCode()==Ability.POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&((offensiveAffects.size()>0)
					   ||((target instanceof Drink)&&(((Drink)target).liquidHeld()==EnvResource.RESOURCE_POISON))))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> look(s) cleansed of any poisons.":"^S<S-NAME> chant(s) for <T-NAME> to be cleansed of poisons.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				int old=target.numAffects();
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if((target instanceof Drink)&&(((Drink)target).liquidHeld()==EnvResource.RESOURCE_POISON))
				{
					((Drink)target).setLiquidHeld(EnvResource.RESOURCE_FRESHWATER);
					target.baseEnvStats().setAbility(0);
				}
				if(old>target.numAffects())
				{
					if(target instanceof MOB)
					{
						((MOB)target).tell("You feel much better!");
						((MOB)target).recoverCharStats();
						((MOB)target).recoverMaxState();
					}
				}
				target.recoverEnvStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> chant(s) for <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
