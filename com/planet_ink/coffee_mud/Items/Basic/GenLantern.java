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
public class GenLantern extends GenLightSource
{
	public String ID(){	return "GenLantern";}
	public static final int DURATION_TICKS=800;
	public GenLantern()
	{
		super();
		setName("a hooded lantern");
		setDisplayText("a hooded lantern sits here.");
		setDescription("");

		baseEnvStats().setWeight(5);
		setDuration(DURATION_TICKS);
		destroyedWhenBurnedOut=false;
		goesOutInTheRain=false;
		baseGoldValue=60;
		setMaterial(EnvResource.RESOURCE_STEEL);
		recoverEnvStats();
	}

	public boolean isGeneric(){return true;}

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
