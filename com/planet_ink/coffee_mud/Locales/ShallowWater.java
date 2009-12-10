package com.planet_ink.coffee_mud.Locales;
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
public class ShallowWater extends StdRoom implements Drink
{
	public String ID(){return "ShallowWater";}
	public ShallowWater()
	{
		super();
		name="the water";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
	}
	public int domainType(){return Room.DOMAIN_OUTDOORS_WATERSURFACE;}
	public int domainConditions(){return Room.CONDITION_WET;}
	protected int baseThirst(){return 0;}
	public long decayTime(){return 0;}
	public void setDecayTime(long time){}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			if(liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell("You don't want to be drinking saltwater.");
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.source().playerStats()!=null)
		&&(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))
		&&(msg.source().soulMate()==null)
		&&(msg.source().playerStats().getHygiene()>100)
		&&(msg.source().riding()==null))
		{
		    if((domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		    ||(domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
			    msg.source().playerStats().adjHygiene(PlayerStats.HYGIENE_WATERCLEAN);
		    else
		    if(!CMLib.flags().isFlying(msg.source()))
			    msg.source().playerStats().adjHygiene(PlayerStats.HYGIENE_WATERCLEAN);
		}
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			MOB mob=msg.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
	}

	public int thirstQuenched(){return 500;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return RawMaterial.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
    public boolean disappearsAfterDrinking(){return false;}
	public boolean containsDrink(){return true;}
    public int amountTakenToFillMe(Drink theSource){return 0;}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
