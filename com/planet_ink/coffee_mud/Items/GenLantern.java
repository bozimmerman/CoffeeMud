package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenLantern extends GenLightSource
{
	public GenLantern()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a hooded lantern";
		displayText="a hooded lantern sits here.";
		description="The lantern still looks like it has some oil in it.";

		baseEnvStats().setWeight(5);
		setDuration(200);
		destroyedWhenBurnedOut=false;
		goesOutInTheRain=false;
		baseGoldValue=60;
		setMaterial(EnvResource.RESOURCE_STEEL);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenLantern();
	}
	public boolean isGeneric(){return true;}

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
						if(getDuration()>0)
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
			switch(affect.targetMinor())
			{
			case Affect.TYP_FILL:
				if((affect.tool()!=null)&&(affect.tool() instanceof Drink))
				{
					OilFlask thePuddle=(OilFlask)affect.tool();
					int amountToTake=1;
					if(amountToTake>thePuddle.amountOfLiquidRemaining)
						amountToTake=0;
					thePuddle.amountOfLiquidRemaining-=amountToTake;
					lit=false;
					setDuration(200);
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
