package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonPlants extends Chant
{
	public String ID() { return "Chant_SummonPlants"; }
	public String name(){ return "Summon Plants";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Room PlantsLocation=null;
	private Item littlePlants=null;
	public Environmental newInstance(){	return new Chant_SummonPlants();}

	public void unInvoke()
	{
		if(PlantsLocation==null)
			return;
		if(littlePlants==null)
			return;
		if(canBeUninvoked())
			PlantsLocation.showHappens(Affect.MSG_OK_VISUAL,littlePlants.name()+" wither"+(littlePlants.name().startsWith("s")?"":"s")+" away.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item plants=littlePlants; // protects against uninvoke loops!
			littlePlants=null;
			plants.destroyThis();
			PlantsLocation.recoverRoomStats();
			PlantsLocation=null;
		}
	}

	public void affect(Affect affect)
	{
		if((affect.amITarget(littlePlants))
		&&(affect.targetMinor()==Affect.TYP_GET))
			affect.addTrailerMsg(new FullMsg(affect.source(),littlePlants,null,Affect.MSG_OK_VISUAL,Affect.MASK_GENERAL|Affect.MSG_DEATH,-1,null));
	}

	public static Item buildPlant(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_GREENS);
		switch(Dice.roll(1,5,0))
		{
		case 1:
			newItem.setName("some happy flowers");
			newItem.setDisplayText("some happy flowers are growing here.");
			newItem.setDescription("Happy flowers with little red and yellow blooms.");
			break;
		case 2:
			newItem.setName("some happy weeds");
			newItem.setDisplayText("some happy weeds are growing here.");
			newItem.setDescription("Long stalked little plants with tiny bulbs on top.");
			break;
		case 3:
			newItem.setName("a pretty fern");
			newItem.setDisplayText("a pretty fern is growing here.");
			newItem.setDescription("Like a tiny bush, this dark green plant is lovely.");
			break;
		case 4:
			newItem.setName("a patch of sunflowers");
			newItem.setDisplayText("a patch of sunflowers is growing here.");
			newItem.setDescription("Happy flowers with little yellow blooms.");
			break;
		case 5:
			newItem.setName("a patch of bluebonnets");
			newItem.setDisplayText("a patch of bluebonnets is growing here.");
			newItem.setDescription("Happy flowers with little blue and purple blooms.");
			break;
		}
		newItem.setSecretIdentity(mob.name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.setDispossessionTime(null);
		room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" sprout(s) up here.");
		Chant_SummonPlants newChant=new Chant_SummonPlants();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*120)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				buildPlant(mob,mob.location());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}