package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Lantern extends LightSource
{
	public String ID(){	return "Lantern";}
	public static final int DURATION_TICKS=800;
	public Lantern()
	{
		super();
		setName("a hooded lantern");
		setDisplayText("a hooded lantern sits here.");
		setDescription("The lantern still looks like it has some oil in it.");

		baseEnvStats().setWeight(5);
		setMaterial(EnvResource.RESOURCE_STEEL);
		durationTicks=DURATION_TICKS;
		this.destroyedWhenBurnedOut=false;
		this.goesOutInTheRain=false;
		baseGoldValue=60;
	}
	public Environmental newInstance()
	{
		return new Lantern();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
				case CMMsg.TYP_FILL:
					if((msg.tool()!=null)
					&&(msg.tool()!=msg.target())
					&&(msg.tool() instanceof Drink))
					{
						if(((Drink)msg.tool()).liquidType()!=EnvResource.RESOURCE_LAMPOIL)
						{
							mob.tell("You can only fill "+name()+" with lamp oil!");
							return false;
						}
						Drink thePuddle=(Drink)msg.tool();
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
				default:
					break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_FILL:
				if((msg.tool()!=null)&&(msg.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)msg.tool();
					int amountToTake=1;
					if(!thePuddle.containsDrink()) amountToTake=0;
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
					setDuration(DURATION_TICKS);
					setDescription("The lantern still looks like it has some oil in it.");
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}

