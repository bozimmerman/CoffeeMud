package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Cow extends StdMOB implements Drink
{
	public String ID(){return "Cow";}
	public Cow()
	{
		super();
		Username="a cow";
		setDescription("A large lumbering beast that looks too slow to get out of your way.");
		setDisplayText("A fat happy cow wanders around here.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(2);
		baseEnvStats().setArmor(90);
		baseCharStats().setMyRace(CMClass.getRace("Cow"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
			return true;
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			MOB mob=msg.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState());
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_FILL)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Container)
		&&(((Container)msg.target()).capacity()>0))
		{
			Item container=(Item)msg.target();
			Item I=CMClass.getItem("GenLiquidResource");
			I.setName("some milk");
			I.setDisplayText("some milk has been left here.");
			I.setDescription("It looks like milk");
			I.setMaterial(EnvResource.RESOURCE_MILK);
			I.setBaseValue(EnvResource.RESOURCE_DATA[EnvResource.RESOURCE_MILK&EnvResource.RESOURCE_MASK][1]);
			I.baseEnvStats().setWeight(1);
			I.recoverEnvStats();
			I.setContainer(container);
			if(container.owner()!=null)
				if(container.owner() instanceof MOB)
					((MOB)container.owner()).addInventory(I);
				else
				if(container.owner() instanceof Room)
					((Room)container.owner()).addItemRefuse(I,Item.REFUSE_RESOURCE);
		}
	}
	public int thirstQuenched(){return 100;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_MILK;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
}