package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Lantern extends LightSource
{
	public Lantern()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a hooded lantern";
		displayText="a hooded lantern sits here.";
		description="The lantern still looks like it has some oil in it.";
		
		baseEnvStats().setWeight(5);
		material=Item.METAL;
		durationTicks=200;
		destroyedWhenBurnedOut=false;
		baseGoldValue=60;
	}
	public Environmental newInstance()
	{
		return new Lantern();
	}

	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
				case Affect.HANDS_FILL:
					if(mob.isMine(this))
					{
						if(!burnedOut)
						{
							mob.tell(name()+" still has some oil left in it.");
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
			MOB mob=affect.source();
			switch(affect.targetCode())
			{
			case Affect.HANDS_FILL:
				if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
				{
					OilFlask thePuddle=(OilFlask)affect.tool();
					int amountToTake=1;
					if(amountToTake>thePuddle.amountOfLiquidRemaining)
						amountToTake=0;
					thePuddle.amountOfLiquidRemaining-=amountToTake;
					lit=false;
					burnedOut=false;
					description="The lantern still looks like it has some oil in it.";
				}
				break;
			default:
				break;
			}
		}
		super.affect(affect);
	}

}
