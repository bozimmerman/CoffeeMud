package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdDrink;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class LifeFountain extends StdDrink implements MiscMagic
{
	public String ID(){	return "LifeFountain";}

	private Hashtable lastDrinks=new Hashtable();

	public LifeFountain()
	{
		super();
		setName("a fountain");
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		setDisplayText("a fountain of life flows here.");
		setDescription("The fountain is coming magically from the ground.  The water looks pure and clean.");
		baseGoldValue=10;
		material=EnvResource.RESOURCE_FRESHWATER;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOTGET);
		recoverEnvStats();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.othersMessage()==null)
				&&(msg.sourceMessage()==null))
					return true;
				break;
			case CMMsg.TYP_FILL:
				mob.tell("You can't fill the fountain of life.");
				return false;
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
			case CMMsg.TYP_DRINK:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					Long time=(Long)lastDrinks.get(msg.source());
					if((time==null)||(time.longValue()<(System.currentTimeMillis()-16000)))
					{
						Ability A=CMClass.getAbility("Prayer_CureLight");
						if(A!=null) A.invoke(msg.source(),msg.source(),true,envStats().level());
						A=CMClass.getAbility("Prayer_RemovePoison");
						if(A!=null) A.invoke(msg.source(),msg.source(),true,envStats().level());
						A=CMClass.getAbility("Prayer_CureDisease");
						if(A!=null) A.invoke(msg.source(),msg.source(),true,envStats().level());
						time=new Long(System.currentTimeMillis());
						lastDrinks.put(msg.source(),time);
					}
				}
				else
				{
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
					super.executeMsg(myHost,msg);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

}
