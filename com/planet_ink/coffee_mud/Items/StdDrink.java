package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdDrink extends StdContainer implements Drink,Item
{
	public String ID(){	return "StdDrink";}
	protected int amountOfThirstQuenched=250;
	protected int amountOfLiquidHeld=2000;
	protected int amountOfLiquidRemaining=2000;
	protected boolean disappearsAfterDrinking=false;
	protected int liquidType=EnvResource.RESOURCE_FRESHWATER;

	public StdDrink()
	{
		super();
		setName("a cup");
		baseEnvStats.setWeight(10);
		capacity=0;
		containType=Container.CONTAIN_LIQUID;
		setDisplayText("a cup sits here.");
		setDescription("A small wooden cup with a lid.");
		baseGoldValue=5;
		material=EnvResource.RESOURCE_LEATHER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdDrink();
	}

	public int thirstQuenched(){return amountOfThirstQuenched;}
	public int liquidHeld(){return amountOfLiquidHeld;}
	public int liquidRemaining(){return amountOfLiquidRemaining;}
	public int liquidType(){
		if((material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			return material();
		return liquidType;
	}
	public void setLiquidType(int newLiquidType){liquidType=newLiquidType;}

	public void setThirstQuenched(int amount){amountOfThirstQuenched=amount;}
	public void setLiquidHeld(int amount){amountOfLiquidHeld=amount;}
	public void setLiquidRemaining(int amount){amountOfLiquidRemaining=amount;}

	public boolean containsDrink()
	{
		if((liquidRemaining()<1)
		||
		 ((!isGettable())
		&&(owner()!=null)
		&&(owner() instanceof Room)
		&&(((Room)owner()).getArea()!=null)
		&&(((Room)owner()).getArea().weatherType((Room)owner())==Area.WEATHER_DROUGHT)))
			return false;
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
				case Affect.TYP_DRINK:
					if((mob.isMine(this))||(envStats().weight()>1000)||(!this.isGettable()))
					{
						if(!containsDrink())
						{
							mob.tell(name()+" is empty.");
							return false;
						}
						if((liquidType()==EnvResource.RESOURCE_SALTWATER)
						||(liquidType()==EnvResource.RESOURCE_LAMPOIL))
						{
							mob.tell("You don't want to be drinking "+EnvResource.RESOURCE_DESCS[liquidType()&EnvResource.RESOURCE_MASK].toLowerCase()+".");
							return false;
						}
						return true;
					}
					else
					{
						mob.tell("You don't have that.");
						return false;
					}
				case Affect.TYP_FILL:
					if(mob.isMine(this))
					{
						if(liquidRemaining()>=amountOfLiquidHeld)
						{
							mob.tell(name()+" is full.");
							return false;
						}
						if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
						{
							Drink thePuddle=(Drink)affect.tool();
							if(!thePuddle.containsDrink())
							{
								mob.tell(thePuddle.name()+" is empty.");
								return false;
							}
							if((liquidRemaining()>0)&&(liquidType()!=thePuddle.liquidType()))
							{
								mob.tell("There is still some "+EnvResource.RESOURCE_DESCS[liquidType()&EnvResource.RESOURCE_MASK].toLowerCase()
										 +" left in "+name()+".  You must empty it before you can fill it with "
										 +EnvResource.RESOURCE_DESCS[thePuddle.liquidType()&EnvResource.RESOURCE_MASK].toLowerCase()+".");
								return false;

							}
							return true;
						}
						else
						{
							mob.tell("You can't fill "+name()+" from that.");
							return false;
						}
					}
					else
					{
						mob.tell("You don't have that.");
						return false;
					}
			}
		}
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_DRINK:
				amountOfLiquidRemaining-=amountOfThirstQuenched;
				boolean thirsty=mob.curState().getThirst()<=0;
				boolean full=!mob.curState().adjThirst(amountOfThirstQuenched,mob.maxState());
				if(thirsty)
					mob.tell("You are no longer thirsty.");
				else
				if(full)
					mob.tell("You have drunk all you can.");
				if(disappearsAfterDrinking)
					destroy();
				break;
			case Affect.TYP_FILL:
				if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)affect.tool();
					int amountToTake=amountOfLiquidHeld-amountOfLiquidRemaining;
					if(amountToTake>thePuddle.liquidRemaining())
						amountToTake=thePuddle.liquidRemaining();
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
					if(amountOfLiquidRemaining<=0)
						setLiquidType(thePuddle.liquidType());
					amountOfLiquidRemaining+=amountToTake;
					if((amountOfLiquidRemaining<=0)&&(disappearsAfterDrinking))
						destroy();
				}
				break;
			default:
				break;
			}
		}
		super.affect(myHost,affect);
	}
}
