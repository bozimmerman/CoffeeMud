package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantBed extends Chant
{
	public String ID() { return "Chant_PlantBed"; }
	public String name(){ return "Plant Bed";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_PlantBed();}
	private Item peaPod=null;

	public void unInvoke()
	{
		super.unInvoke();
		if(peaPod!=null)
		{
			Room R=CoffeeUtensils.roomLocation(peaPod);
			if(R!=null)
				R.showHappens(CMMsg.MSG_OK_VISUAL,"A pea-pod shrivels up!");
			Rideable RI=(Rideable)peaPod;
			for(int r=RI.numRiders()-1;r>=0;r--)
				RI.fetchRider(r).setRiding(null);
			peaPod.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((((mob.location().domainType()&Room.INDOORS)>0)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_DESERT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR))
		&&(!auto))
		{
			mob.tell("This chant will not work here.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=(Item)CMClass.getItem("GenBed");
				Rideable newRide=(Rideable)newItem;
				newItem.setName("a plant bed");
				newItem.setDisplayText("A enormously comfortable pea-pod looks ready to sleep in.");
				newItem.setDescription("The plant bed looks like a hollowed pea-pod with fern-like cushioning inside.  Looks like a nice place to take a nap in!");
				newRide.setRideBasis(Rideable.RIDEABLE_SLEEP);
				newRide.setRiderCapacity(1);
				newItem.setMaterial(EnvResource.RESOURCE_HEMP);
				newItem.baseEnvStats().setWeight(1000);
				newItem.setBaseValue(0);
				Sense.setGettable(newItem,false);
				Ability A=CMClass.getAbility("Prop_RideResister");
				A.setMiscText("disease poison");
				newItem.addNonUninvokableEffect(A);
				newItem.recoverEnvStats();
				newItem.setMiscText(newItem.text());
				peaPod=newItem;
				mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A comfortable pea-pod bed grows nearby.");
				mob.location().recoverEnvStats();
				beneficialAffect(mob,newItem,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
