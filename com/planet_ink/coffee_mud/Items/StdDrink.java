package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdDrink extends StdContainer implements Drink
{

	protected int amountOfThirstQuenched=250;
	protected int amountOfLiquidHeld=2000;
	protected int amountOfLiquidRemaining=2000;

	public StdDrink()
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
		return new StdDrink();
	}

	public int thirstQuenched(){return amountOfThirstQuenched;}
	public int liquidHeld(){return amountOfLiquidHeld;}
	public int liquidRemaining(){return amountOfLiquidRemaining;}

	public void setThirstQuenched(int amount){amountOfThirstQuenched=amount;}
	public void setLiquidHeld(int amount){amountOfLiquidHeld=amount;}
	public void setLiquidRemaining(int amount){amountOfLiquidRemaining=amount;}
	
	public boolean containsDrink()
	{
		if((liquidRemaining()<1)
		||
		 ((!isGettable())
		&&(myOwner()!=null)
		&&(myOwner() instanceof Room)
		&&(((Room)myOwner()).getArea()!=null)
		&&(((Room)myOwner()).getArea().weatherType((Room)myOwner())==Area.WEATHER_DROUGHT)))
			return true;
		return false;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
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
				else
				break;
			case Affect.TYP_FILL:
				if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)affect.tool();
					int amountToTake=amountOfLiquidHeld-amountOfLiquidRemaining;
					if(amountToTake>thePuddle.liquidRemaining())
						amountToTake=thePuddle.liquidRemaining();
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
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
