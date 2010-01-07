package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdTub extends StdRideable implements Drink
{
	public String ID(){	return "StdTub";}
	protected int amountOfThirstQuenched=250;
	protected int amountOfLiquidHeld=2000;
	protected int amountOfLiquidRemaining=2000;
	protected boolean disappearsAfterDrinking=false;
	protected int liquidType=RawMaterial.RESOURCE_FRESHWATER;
	protected long decayTime=0;

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
		material=RawMaterial.RESOURCE_CLAY;
		rideBasis=Rideable.RIDEABLE_SIT;
		riderCapacity=4;
		recoverEnvStats();
	}

	public long decayTime(){return decayTime;}
	public void setDecayTime(long time){decayTime=time;}
    public boolean disappearsAfterDrinking(){return disappearsAfterDrinking;}
	public int thirstQuenched(){return amountOfThirstQuenched;}
	public int liquidHeld(){return amountOfLiquidHeld;}
	public int liquidRemaining(){return amountOfLiquidRemaining;}
	public int liquidType(){
		if((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			return material();
		return liquidType;
	}
	public void setLiquidType(int newLiquidType){liquidType=newLiquidType;}

	public void setThirstQuenched(int amount){amountOfThirstQuenched=amount;}
	public void setLiquidHeld(int amount){amountOfLiquidHeld=amount;}
	public void setLiquidRemaining(int amount){amountOfLiquidRemaining=amount;}

	public boolean containsDrink()
	{
        if((!CMLib.flags().isGettable(this))
        &&(owner()!=null)
        &&(owner() instanceof Room)
        &&(((Room)owner()).getArea()!=null)
        &&(((Room)owner()).getArea().getClimateObj().weatherType((Room)owner())==Climate.WEATHER_DROUGHT))
            return false;
        if(liquidRemaining()<1)
        {
            Vector V=getContents();
            for(int v=0;v<V.size();v++)
                if((V.elementAt(v) instanceof Item)
                &&(V.elementAt(v) instanceof Drink)
                &&((((Item)V.elementAt(v)).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
                    return true;
            return false;
        }
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
				if((mob.isMine(this))||(envStats().weight()>1000)||(!CMLib.flags().isGettable(this)))
				{
					if(!containsDrink())
					{
						mob.tell(name()+" is empty.");
						return false;
					}
					if((liquidType()==RawMaterial.RESOURCE_SALTWATER)
					||(liquidType()==RawMaterial.RESOURCE_LAMPOIL))
					{
						mob.tell("You don't want to be drinking "+RawMaterial.CODES.NAME(liquidType()).toLowerCase()+".");
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
						mob.tell("There is still some "+RawMaterial.CODES.NAME(liquidType()).toLowerCase()
								 +" left in "+name()+".  You must empty it before you can fill it with "
								 +RawMaterial.CODES.NAME(thePuddle.liquidType()).toLowerCase()+".");
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
        &&(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)
            ||((msg.tool() instanceof Social)
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
                {
                    destroy();
                    return;
                }
				break;
			case CMMsg.TYP_FILL:
				if((msg.tool()!=null)&&(msg.tool() instanceof Drink))
				{
					Drink thePuddle=(Drink)msg.tool();
					int amountToTake=amountTakenToFillMe(thePuddle);
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
					if(amountOfLiquidRemaining<=0)
						setLiquidType(thePuddle.liquidType());
					if( ( (long)amountOfLiquidRemaining + (long)amountToTake ) <= (long)Integer.MAX_VALUE )
						amountOfLiquidRemaining+=amountToTake;
					if(amountOfLiquidRemaining>amountOfLiquidHeld)
						amountOfLiquidRemaining=amountOfLiquidHeld;
					if((amountOfLiquidRemaining<=0)&&(disappearsAfterDrinking))
                    {
						destroy();
                        return;
                    }
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
