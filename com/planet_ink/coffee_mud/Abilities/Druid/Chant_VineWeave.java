package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.*;
import java.util.*;

public class Chant_VineWeave extends Chant
{
	public String ID() { return "Chant_VineWeave"; }
	public String name(){ return "Vine Weave";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.location().resourceChoices()==null)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if(((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_COTTON)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SILK)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_HEMP)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_VINE)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_WHEAT)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SEAWEED))))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_VINE;
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_VINE)))
			material=EnvResource.RESOURCE_VINE;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SILK)))
			material=EnvResource.RESOURCE_SILK;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_HEMP)))
			material=EnvResource.RESOURCE_HEMP;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_WHEAT)))
			material=EnvResource.RESOURCE_WHEAT;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SEAWEED)))
			material=EnvResource.RESOURCE_SEAWEED;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the plants.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				Item building=null;
				Ability A=CMClass.getAbility("Weaving");
				if(A!=null)
				{
					while((building==null)||(building.name().endsWith(" bundle")))
					{
						Vector V=new Vector();
						V.addElement(new Integer(material));
						A.invoke(mob,V,A,true);
						if((V.size()>0)&&(V.lastElement() instanceof Item))
							building=(Item)V.lastElement();
						else
							break;
					}
				}
				if(building==null)
				{
					mob.tell("The chant failed for some reason...");
					return false;
				}

				building.recoverEnvStats();
				building.text();
				building.recoverEnvStats();

				mob.location().addItemRefuse(building,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,building.name()+" twists out of some vines and grows still.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the plants, but nothing happens.");

		// return whether it worked
		return success;
	}
}