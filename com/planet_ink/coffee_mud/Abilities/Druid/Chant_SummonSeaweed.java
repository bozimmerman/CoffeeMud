package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonSeaweed extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonSeaweed"; }
	public String name(){ return "Summon Seaweed";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonSeaweed();}
	protected boolean seaOk(){return true;}

	public static Item buildSeaweed(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_SEAWEED);
		switch(Dice.roll(1,5,0))
		{
		case 1:
			newItem.setName("some algae");
			newItem.setDisplayText("some algae is here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("some seaweed");
			newItem.setDisplayText("some seaweed is here.");
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName("some kelp");
			newItem.setDisplayText("some kelp is here");
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName("some coral");
			newItem.setDisplayText("some coral is here.");
			newItem.setDescription("");
			break;
		case 5:
			newItem.setName("some sponge");
			newItem.setDisplayText("some sponge is here.");
			newItem.setDescription("");
			break;
		}
		newItem.baseEnvStats().setWeight(1);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.setDispossessionTime(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" sprout(s) up here.");
		Chant_SummonSeaweed newChant=new Chant_SummonSeaweed();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		if((CoffeeUtensils.doesOwnThisProperty(mob,room))
		||((mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),room))))
		{
			newChant.setInvoker(mob);
			newChant.setMiscText(mob.name());
			newItem.addNonUninvokableEffect(newChant);
		}
		else
			newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public Item buildMyPlant(MOB mob, Room room)
	{
		return buildSeaweed(mob,room);
	}

	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		   &&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		return true;
	}


}
