package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonFungus extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonFungus"; }
	public String name(){ return "Summon Fungus";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonFungus();}
	private boolean processing=false;
	
	public void affect(Environmental myHost, Affect affect)
	{
		if((affect.amITarget(littlePlants))
		&&(!processing)
		&&(affect.targetMinor()==Affect.TYP_GET))
		{
			processing=true;
			Ability A=littlePlants.fetchAffect(ID());
			if(A!=null)
			{
				ExternalPlay.deleteTick(A,-1);
				littlePlants.delAffect(A);
				littlePlants.setSecretIdentity("");
			}
			if(littlePlants.fetchBehavior("Decay")==null)
			{
				Behavior B=CMClass.getBehavior("Decay");
				B.setParms("min="+Host.TICKS_PER_DAY+" max="+Host.TICKS_PER_DAY+" chance=100");
				littlePlants.addBehavior(B);
				B.affect(myHost,affect);
			}
			processing=false;
		}
	}
	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE))
		{
			mob.tell("This is not the place for fungus.");
			return false;
		}
		return true;
	}
	
	public static Item buildFungus(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenFoodResource");
		newItem.setMaterial(EnvResource.RESOURCE_MUSHROOMS);
		switch(Dice.roll(1,6,0))
		{
		case 1:
			newItem.setName("a mushroom");
			newItem.setDisplayText("a mushroom is here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("a shiitake mushroom");
			newItem.setDisplayText("a shiitake mushroom grows here.");
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName("a cremini mushroom");
			newItem.setDisplayText("a cremini mushroom grows here");
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName("a white mushroom");
			newItem.setDisplayText("a white mushroom grows here.");
			newItem.setDescription("");
			break;
		case 5:
			newItem.setName("a portabello mushroom");
			newItem.setDisplayText("a portabello mushroom grows here.");
			newItem.setDescription("");
			break;
		case 6:
			newItem.setName("a wood ear");
			newItem.setDisplayText("a wood ear grows here.");
			newItem.setDescription("");
			break;
		}
		newItem.setSecretIdentity(mob.name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.baseEnvStats().setWeight(1);
		newItem.setDispossessionTime(0);
		room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.displayName()+" sprouts up here.");
		Chant_SummonFungus newChant=new Chant_SummonFungus();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public Item buildMyPlant(MOB mob, Room room)
	{
		return buildFungus(mob,room);
	}
}
