package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
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
public class IndoorWaterSurface extends StdRoom implements Drink
{
	public String ID(){return "IndoorWaterSurface";}
	public IndoorWaterSurface()
	{
		super();
		name="the water";
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		switch(WaterSurface.isOkWaterSurfaceAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}
	public int thirstQuenched(){return 1000;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
    public boolean disappearsAfterDrinking(){return false;}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
