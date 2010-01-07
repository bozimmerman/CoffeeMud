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
public class StdDrink extends StdContainer implements Drink,Item
{
	public String ID(){	return "StdDrink";}
	protected int amountOfThirstQuenched=250;
	protected int amountOfLiquidHeld=2000;
	protected int amountOfLiquidRemaining=2000;
	protected boolean disappearsAfterDrinking=false;
	protected int liquidType=RawMaterial.RESOURCE_FRESHWATER;
	protected long decayTime=0;

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
		material=RawMaterial.RESOURCE_LEATHER;
		recoverEnvStats();
	}



	public long decayTime(){return decayTime;}
	public void setDecayTime(long time){decayTime=time;}
	public int thirstQuenched(){return amountOfThirstQuenched;}
	public int liquidHeld(){return amountOfLiquidHeld;}
	public int liquidRemaining(){return amountOfLiquidRemaining;}
    public boolean disappearsAfterDrinking(){return disappearsAfterDrinking;}
	public int liquidType(){
		if((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			return material();
		return liquidType;
	}
	public void setLiquidType(int newLiquidType){liquidType=newLiquidType;}

	public void setThirstQuenched(int amount){amountOfThirstQuenched=amount;}
	public void setLiquidHeld(int amount){amountOfLiquidHeld=amount;}
	public void setLiquidRemaining(int amount){amountOfLiquidRemaining=amount;}

	protected int totalDrinkContained()
	{
		int total = amountOfLiquidRemaining;
        Vector V=getContents();
        for(int v=0;v<V.size();v++)
            if((V.elementAt(v) instanceof Item)
            &&(V.elementAt(v) instanceof Drink)
            &&((((Item)V.elementAt(v)).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
                total += ((Drink)V.elementAt(v)).liquidRemaining();
        return total;
	}
	
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
				if((totalDrinkContained()>=amountOfLiquidHeld)
				&&(liquidHeld()<500000))
				{
					mob.tell(name()+" is full.");
					return false;
				}
                if((msg.tool() instanceof Container)
                &&((!(msg.tool() instanceof Drink))||((Drink)msg.tool()).liquidRemaining()<=0)
                &&(msg.tool()!=msg.target()))
                {
                    Vector V=((Container)msg.tool()).getContents();
                    Item I=null;
                    for(int i=0;i<V.size();i++)
                    {
                        I=(Item)V.elementAt(i);
                        if(I instanceof Drink)
                            break;
                    }
                    if(I instanceof Drink)
                        msg.modify(msg.source(),msg.target(),I,
                                msg.sourceCode(),msg.sourceMessage(),
                                msg.targetCode(),msg.targetMessage(),
                                msg.othersCode(),msg.othersMessage());
                    else
                    {
                        msg.source().tell(msg.tool().name()+" has nothing you can fill this with.");
                        return false;
                    }
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
        int amountToTake=amountOfLiquidHeld-totalDrinkContained();
        if(amountOfLiquidHeld>=500000)
            amountToTake=theSource.liquidRemaining();
        if(amountToTake>theSource.liquidRemaining())
            amountToTake=theSource.liquidRemaining();
        return amountToTake;
    }

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
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
					{
						setLiquidType(thePuddle.liquidType());
						if((thePuddle instanceof RawMaterial)&&(((RawMaterial)thePuddle).container()!=null))
						{
							Vector V = this.getContents();
							Drink addHereI = null;
							for(Enumeration e = V.elements();e.hasMoreElements();)
							{
								Item I = (Item)e.nextElement();
								if((I instanceof Drink) && (I instanceof RawMaterial) && (I.Name().equals(thePuddle.Name())))
									addHereI=(Drink)I;
							}
							if(addHereI==null)
							{
								addHereI=(Drink)thePuddle.copyOf();
								addHereI.setLiquidRemaining(0);
								((RawMaterial)addHereI).setContainer(this);
								if(((RawMaterial)thePuddle).owner() instanceof MOB)
									((MOB)(((RawMaterial)thePuddle).owner())).addInventory((RawMaterial)addHereI);
								else
								if(((RawMaterial)thePuddle).owner() instanceof Room)
									((Room)(((RawMaterial)thePuddle).owner())).addItemRefuse((RawMaterial)addHereI,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
							}
							addHereI.setLiquidRemaining(addHereI.liquidRemaining() + amountToTake);
							addHereI.setLiquidHeld(addHereI.liquidHeld() + amountToTake);
							amountToTake=0;
						}
					}
					if(amountToTake>0)
					{
						if( ( (long)amountOfLiquidRemaining + (long)amountToTake ) <= (long)Integer.MAX_VALUE )
							amountOfLiquidRemaining+=amountToTake;
						if(amountOfLiquidRemaining>amountOfLiquidHeld)
							amountOfLiquidRemaining=amountOfLiquidHeld;
					}
					if((thePuddle.liquidRemaining()<=0)
                    &&(thePuddle instanceof Item)
					&&((thePuddle.disappearsAfterDrinking())||(thePuddle instanceof RawMaterial)))
						((Item)thePuddle).destroy();
                    if((amountOfLiquidRemaining<=0)
                    &&((disappearsAfterDrinking)||(this instanceof RawMaterial)))
                        destroy();
				}
				break;
			default:
				break;
			}
		}
	}
}
