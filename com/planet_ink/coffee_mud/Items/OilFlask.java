package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class OilFlask extends StdContainer
{

	public int amountOfLiquidHeld=5;
	public int amountOfLiquidRemaining=5;

	public OilFlask()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an oil flask";
		baseEnvStats.setWeight(10);
		capacity=0;
		setMaterial(EnvResource.RESOURCE_GLASS);
		displayText="an oil flask sits here.";
		description="A small glass flask containing lamp oil, with a lid.";
		baseGoldValue=5;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new OilFlask();
	}

	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
				case Affect.TYP_FILL:
					if(mob.isMine(this))
					{
						if(amountOfLiquidRemaining>=amountOfLiquidHeld)
						{
							mob.tell(name()+" is full.");
							return false;
						}
						if((affect.tool()!=null)&&(affect.tool() instanceof OilFlask))
						{
							OilFlask thePuddle=(OilFlask)affect.tool();
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
				default:
					break;
			}
		}
		return super.okAffect(affect);
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_FILL:
				if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
				{
					OilFlask thePuddle=(OilFlask)affect.tool();
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