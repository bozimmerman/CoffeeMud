package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonFood extends Chant
{
	public Chant_SummonFood()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Food";
		baseEnvStats().setLevel(5);

		canAffectCode=0;
		canTargetCode=0;
		
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_SummonFood();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to try this.");
			return false;
		}
		if(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> chant(s) to the ground.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<(adjustedLevel(mob)/4);i++)
				{
					Food newItem=(Food)CMClass.getStdItem("GenFoodResource");
					newItem.setName("some berries");
					newItem.setDisplayText("Some berries are growing here.");
					newItem.setDescription("Small and round, these little red berries look juicy and good.");
					newItem.setMaterial(EnvResource.RESOURCE_BERRIES);
					newItem.setNourishment(150);
					newItem.setMiscText(newItem.text());
					mob.location().addItemRefuse(newItem);
				}
				mob.location().showHappens(Affect.MSG_OK_ACTION,"Some berries quickly begin to grow here.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
