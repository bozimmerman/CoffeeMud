package com.planet_ink.coffee_mud.Items.Basic;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdTub extends StdRideable implements Drink
{
	public String ID(){	return "StdTub";}
	protected int amountOfThirstQuenched=250;
	protected int amountOfLiquidHeld=2000;
	protected int amountOfLiquidRemaining=2000;
	protected boolean disappearsAfterDrinking=false;
	protected int liquidType=EnvResource.RESOURCE_FRESHWATER;

	public StdTub()
	{
		super();
		setName("a tub");
		baseEnvStats.setWeight(100);
		capacity=1000;
		containType=Container.CONTAIN_LIQUID;
		setDisplayText("a tub sits here.");
		setDescription("A porcelin bath tub.");
		baseGoldValue=500;
		material=EnvResource.RESOURCE_CLAY;
		rideBasis=Rideable.RIDEABLE_SIT;
		riderCapacity=4;
		recoverEnvStats();
	}


    public boolean disappearsAfterDrinking(){return disappearsAfterDrinking;}
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
		 ((!Sense.isGettable(this))
		&&(owner()!=null)
		&&(owner() instanceof Room)
		&&(((Room)owner()).getArea()!=null)
		&&(((Room)owner()).getArea().getClimateObj().weatherType((Room)owner())==Climate.WEATHER_DROUGHT)))
			return false;
		return true;
	}

	public String stateString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return "riding in";
		case Rideable.RIDEABLE_ENTERIN:
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_TABLE:
		case Rideable.RIDEABLE_LADDER:
		case Rideable.RIDEABLE_SLEEP:
			return "in";
		}
		return "riding in";
	}
	public String putString(Rider R)
	{
		return "in";
	}

	public String mountString(int commandType, Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_WATER:
			return "board(s)";
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_TABLE:
		case Rideable.RIDEABLE_ENTERIN:
		case Rideable.RIDEABLE_SLEEP:
			return "get(s) into";
		case Rideable.RIDEABLE_LADDER:
			return "climb(s) into";
		}
		return "board(s)";
	}
	public String dismountString(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
			return "disembark(s) from";
		case Rideable.RIDEABLE_TABLE:
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_WAGON:
		case Rideable.RIDEABLE_LADDER:
		case Rideable.RIDEABLE_ENTERIN:
			return "get(s) out of";
		}
		return "disembark(s) from";
	}
	public String stateStringSubject(Rider R)
	{
		switch(rideBasis)
		{
		case Rideable.RIDEABLE_AIR:
		case Rideable.RIDEABLE_LAND:
		case Rideable.RIDEABLE_WATER:
		case Rideable.RIDEABLE_WAGON:
			return "being ridden by";
		case Rideable.RIDEABLE_TABLE:
		case Rideable.RIDEABLE_SIT:
		case Rideable.RIDEABLE_SLEEP:
		case Rideable.RIDEABLE_ENTERIN:
		case Rideable.RIDEABLE_LADDER:
			return "occupied by";
		}
		return "";
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((mob.isMine(this))||(envStats().weight()>1000)||(!Sense.isGettable(this)))
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
				mob.tell("You don't have that.");
				return false;
			case CMMsg.TYP_FILL:
				if((liquidRemaining()>=amountOfLiquidHeld)
				&&(liquidHeld()<500000))
				{
					mob.tell(name()+" is full.");
					return false;
				}
				if((msg.tool()!=null)
				&&(msg.tool()!=msg.target())
				&&(msg.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)msg.tool();
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
				mob.tell("You can't fill "+name()+" from that.");
				return false;
			default:
				break;
			}
		}
		return true;
	}

    public int amountTakenToFillMe(Drink theSource)
    {
        int amountToTake=amountOfLiquidHeld-amountOfLiquidRemaining;
        if(amountOfLiquidHeld>=500000)
            amountToTake=theSource.liquidRemaining();
        if(amountToTake>theSource.liquidRemaining())
            amountToTake=theSource.liquidRemaining();
        return amountToTake;
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
	    if((msg.source().riding()==this)
        &&(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)
            ||((msg.tool()!=null)
                &&(msg.tool().ID().equals("Social"))
                &&((msg.tool().Name().toUpperCase().startsWith("BATHE"))
                ||(msg.tool().Name().toUpperCase().startsWith("WASH")))))
	    &&(msg.source().playerStats()!=null)
	    &&(msg.source().playerStats().getHygiene()>0)
	    &&(msg.source().soulMate()==null))
		    msg.source().playerStats().adjHygiene(PlayerStats.HYGIENE_WATERCLEAN);
	    
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				amountOfLiquidRemaining-=amountOfThirstQuenched;
				boolean thirsty=mob.curState().getThirst()<=0;
				boolean full=!mob.curState().adjThirst(amountOfThirstQuenched,mob.maxState().maxThirst(mob.baseWeight()));
				if(thirsty)
					mob.tell("You are no longer thirsty.");
				else
				if(full)
					mob.tell("You have drunk all you can.");
				if(disappearsAfterDrinking)
					destroy();
				break;
			case CMMsg.TYP_FILL:
				if((msg.tool()!=null)&&(msg.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)msg.tool();
					int amountToTake=amountTakenToFillMe(thePuddle);
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
					if(amountOfLiquidRemaining<=0)
						setLiquidType(thePuddle.liquidType());
					if((amountOfLiquidRemaining+amountToTake)<=Integer.MAX_VALUE)
						amountOfLiquidRemaining+=amountToTake;
					if(amountOfLiquidRemaining>amountOfLiquidHeld)
						amountOfLiquidRemaining=amountOfLiquidHeld;
					if((amountOfLiquidRemaining<=0)&&(disappearsAfterDrinking))
						destroy();
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
