package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Drink extends Container
{
	
	public int amountOfThirstQuenched=50;
	public int amountOfLiquidHeld=500;
	public int amountOfLiquidRemaining=500;
	
	public Drink()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a cup";
		baseEnvStats.setWeight(10);
		capacity=0;
		displayText="a cup sits here.";
		description="A small wooden cup with a lid.";
		baseGoldValue=5;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new Drink();
	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
				case Affect.TASTE_WATER:
					if((mob.isMine(this))||(envStats().weight()>1000)||(!this.isGettable()))
					{
						if(amountOfLiquidRemaining<=0)
						{
							mob.tell(name()+" is empty.");
							return false;
						}
						return true;
					}
					else
					{
						mob.tell("You don't have that.");
						return false;
					}
				case Affect.HANDS_FILL:
					if(mob.isMine(this))
					{
						if(amountOfLiquidRemaining>=amountOfLiquidHeld)
						{
							mob.tell(name()+" is full.");
							return false;
						}
						if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
						{
							Drink thePuddle=(Drink)affect.tool();
							if(thePuddle.amountOfLiquidRemaining<1)
							{
								mob.tell(thePuddle.name()+" is empty.");
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
	
	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.TASTE_WATER:
				amountOfLiquidRemaining-=10;
				if(mob.curState().adjThirst(amountOfThirstQuenched,mob.maxState()))
					mob.tell("You are no longer thirsty.");
				break;
			case Affect.HANDS_FILL:
				if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)affect.tool();
					int amountToTake=amountOfLiquidHeld-amountOfLiquidRemaining;
					if(amountToTake>thePuddle.amountOfLiquidRemaining)
						amountToTake=thePuddle.amountOfLiquidRemaining;
					thePuddle.amountOfLiquidRemaining-=amountToTake;
					amountOfLiquidRemaining+=amountToTake;
				}
				break;
			default:
				break;
			}
		}
		super.affect(affect);
	}
}
