package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonFlower extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonFlower"; }
	public String name(){ return "Summon Flower";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonFlower();}
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
	public static Item buildFlower(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_GREENS);
		switch(Dice.roll(1,5,0))
		{
		case 1:
			newItem.setName("a red rose");
			newItem.setDisplayText("a red rose is growing here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("a nice daisy");
			newItem.setDisplayText("a nice daisy is growing here.");
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName("a white carnation");
			newItem.setDisplayText("a beautiful white carnation is growing here");
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName("a happy sunflower");
			newItem.setDisplayText("a happy sunflower is growing here.");
			newItem.setDescription("Happy flowers have little yellow blooms.");
			break;
		case 5:
			newItem.setName("a lovely gladiola");
			newItem.setDisplayText("a lovely gladiola is growing here.");
			newItem.setDescription("");
			break;
		}
		newItem.setSecretIdentity(mob.name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.setDispossessionTime(0);
		room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" sprout(s) up here.");
		Chant_SummonFlower newChant=new Chant_SummonFlower();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		newChant.beneficialAffect(mob,newItem,(newChant.adjustedLevel(mob)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public Item buildMyPlant(MOB mob, Room room)
	{
		return buildFlower(mob,room);
	}
}
