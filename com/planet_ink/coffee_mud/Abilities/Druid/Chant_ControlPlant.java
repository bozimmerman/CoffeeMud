package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_ControlPlant extends Chant
{
	public String ID() { return "Chant_ControlPlant"; }
	public String name(){ return "Control Plant";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_ControlPlant();}

	public static Ability isPlant(Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(int a=0;a<I.numAffects();a++)
			{
				Ability A=I.fetchAffect(a);
				if((A!=null)
				&&(A.invoker()!=null)
				&&(A instanceof Chant_SummonPlants))
					return A;
			}
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item myPlant=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(myPlant==null) return false;

		if(isPlant(myPlant)==null)
		{
			mob.tell("You can't control "+myPlant.name()+".");
			return false;
		}

		if(myPlant.rawSecretIdentity().equals(mob.Name()))
		{
			mob.tell("You already control "+myPlant.name()+".");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,myPlant,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=isPlant(myPlant);
				if(A!=null)	A.setInvoker(mob);
				mob.tell("You wrest control of "+myPlant.name()+" from "+myPlant.secretIdentity()+".");
				myPlant.setSecretIdentity(mob.Name());
			}

		}
		else
			beneficialVisualFizzle(mob,myPlant,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
