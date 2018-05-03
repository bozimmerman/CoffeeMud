package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "StdTub";
	}

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
		basePhyStats.setWeight(100);
		capacity=1000;
		containType=Container.CONTAIN_LIQUID;
		setDisplayText("a tub sits here.");
		setDescription("A porcelin bath tub.");
		baseGoldValue=500;
		material=RawMaterial.RESOURCE_CLAY;
		rideBasis=Rideable.RIDEABLE_SIT;
		riderCapacity=4;
		recoverPhyStats();
	}

	@Override
	public long decayTime()
	{
		return decayTime;
	}

	@Override
	public void setDecayTime(long time)
	{
		decayTime=time;
	}

	@Override
	public boolean disappearsAfterDrinking()
	{
		return disappearsAfterDrinking;
	}

	@Override
	public int thirstQuenched()
	{
		return amountOfThirstQuenched;
	}

	@Override
	public int liquidHeld()
	{
		return amountOfLiquidHeld;
	}

	@Override
	public int liquidRemaining()
	{
		return amountOfLiquidRemaining;
	}

	@Override
	public int liquidType()
	{
		if((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			return material();
		return liquidType;
	}

	@Override
	public void setLiquidType(int newLiquidType)
	{
		liquidType=newLiquidType;
	}

	@Override
	public void setThirstQuenched(int amount)
	{
		amountOfThirstQuenched=amount;
	}

	@Override
	public void setLiquidHeld(int amount)
	{
		amountOfLiquidHeld=amount;
	}

	@Override
	public void setLiquidRemaining(int amount)
	{
		amountOfLiquidRemaining=amount;
	}

	protected int getExtraLiquidResourceType()
	{
		final List<Item> V=getContents();
		for(int v=0;v<V.size();v++)
			if((V.get(v) instanceof Drink)
			&&((V.get(v).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
				return V.get(v).material();
		return -1;
	}

	@Override
	public boolean containsDrink()
	{
		if((!CMLib.flags().isGettable(this))
		&&(owner()!=null)
		&&(owner() instanceof Room)
		&&(((Room)owner()).getArea()!=null)
		&&(((Room)owner()).getArea().getClimateObj().weatherType((Room)owner())==Climate.WEATHER_DROUGHT))
			return false;
		if(liquidRemaining()<1)
			return (getExtraLiquidResourceType()>0);
		return true;
	}

	@Override
	public String stateString(Rider R)
	{
		if((R==null)||(stateString.length()==0))
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
		return stateString;
	}

	@Override
	public String putString(Rider R)
	{
		if((R==null)||(putString.length()==0))
			return "in";
		return putString;
	}

	@Override
	public String mountString(int commandType, Rider R)
	{
		if((R==null)||(mountString.length()==0))
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
		return mountString;
	}

	@Override
	public String dismountString(Rider R)
	{
		if((R==null)||(dismountString.length()==0))
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
		return dismountString;
	}

	@Override
	public String stateStringSubject(Rider R)
	{
		if((R==null)||(stateSubjectStr.length()==0))
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
		return stateSubjectStr;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((mob.isMine(this))||(phyStats().weight()>1000)||(!CMLib.flags().isGettable(this)))
				{
					if(!containsDrink())
					{
						mob.tell(L("@x1 is empty.",name()));
						return false;
					}
					if((liquidType()==RawMaterial.RESOURCE_SALTWATER)
					||(liquidType()==RawMaterial.RESOURCE_LAMPOIL))
					{
						mob.tell(L("You don't want to be drinking @x1.",RawMaterial.CODES.NAME(liquidType()).toLowerCase()));
						return false;
					}
					return true;
				}
				mob.tell(L("You don't have that."));
				return false;
			case CMMsg.TYP_FILL:
				if((liquidRemaining()>=amountOfLiquidHeld)
				&&(liquidHeld()<500000))
				{
					mob.tell(L("@x1 is full.",name()));
					return false;
				}
				if((msg.tool()!=msg.target())
				&&(msg.tool() instanceof Drink))
				{
					final Drink thePuddle=(Drink)msg.tool();
					if(!thePuddle.containsDrink())
					{
						mob.tell(L("@x1 is empty.",thePuddle.name()));
						return false;
					}
					if((liquidRemaining()>0)&&(liquidType()!=thePuddle.liquidType()))
					{
						mob.tell(L("There is still some @x1 left in @x2.  You must empty it before you can fill it with @x3.",RawMaterial.CODES.NAME(liquidType()).toLowerCase(),name(),RawMaterial.CODES.NAME(thePuddle.liquidType()).toLowerCase()));
						return false;

					}
					return true;
				}
				mob.tell(L("You can't fill @x1 from that.",name()));
				return false;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public int amountTakenToFillMe(Drink theSource)
	{
		int amountToTake=amountOfLiquidHeld-amountOfLiquidRemaining;
		if(amountOfLiquidHeld>=500000)
			amountToTake=theSource.liquidRemaining();
		if(amountToTake>theSource.liquidRemaining())
			amountToTake=theSource.liquidRemaining();
		return amountToTake;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source().riding()==this)
		&&(CMLib.commands().isHygienicMessage(msg, 0, PlayerStats.HYGIENE_WATERCLEAN)))
		{
			final int extraRsc=getExtraLiquidResourceType();
			if((amountOfLiquidRemaining>1)
			&&(this.liquidType==RawMaterial.RESOURCE_FRESHWATER)
			&&((extraRsc<0)||(extraRsc==RawMaterial.RESOURCE_FRESHWATER)))
			{
				CMLib.commands().handleHygienicMessage(msg, 0, PlayerStats.HYGIENE_WATERCLEAN);
			}
		}

		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				amountOfLiquidRemaining-=amountOfThirstQuenched;
				final boolean thirsty=mob.curState().getThirst()<=0;
				final boolean full=!mob.curState().adjThirst(amountOfThirstQuenched,mob.maxState().maxThirst(mob.baseWeight()));
				if(thirsty)
					mob.tell(L("You are no longer thirsty."));
				else
				if(full)
					mob.tell(L("You have drunk all you can."));
				if(disappearsAfterDrinking)
				{
					destroy();
					return;
				}
				break;
			case CMMsg.TYP_FILL:
				if((msg.tool() instanceof Drink))
				{
					final Drink thePuddle=(Drink)msg.tool();
					final int amountToTake=amountTakenToFillMe(thePuddle);
					thePuddle.setLiquidRemaining(thePuddle.liquidRemaining()-amountToTake);
					if(amountOfLiquidRemaining<=0)
						setLiquidType(thePuddle.liquidType());
					if( ( (long)amountOfLiquidRemaining + (long)amountToTake ) <= Integer.MAX_VALUE )
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
