package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdDrink;
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
import java.util.concurrent.atomic.AtomicLong;

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

public class LifeFountain extends StdDrink implements MiscMagic
{
	@Override
	public String ID()
	{
		return "LifeFountain";
	}

	private final Hashtable<MOB,Long> lastDrinks=new Hashtable<MOB,Long>();
	private final AtomicLong lastCheck = new AtomicLong(0);

	public LifeFountain()
	{
		super();
		setName("a fountain");
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		basePhyStats().setWeight(5);
		capacity=0;
		setDisplayText("a fountain of life flows here.");
		setDescription("The fountain is coming magically from the ground.  The water looks pure and clean.");
		baseGoldValue=10;
		material=RawMaterial.RESOURCE_FRESHWATER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMNOTGET);
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.othersMessage()==null)
				&&(msg.sourceMessage()==null))
					return true;
				break;
			case CMMsg.TYP_FILL:
				mob.tell(L("You can't fill the fountain of life."));
				return false;
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if((msg.sourceMessage()==null)&&(msg.othersMessage()==null))
				{
					synchronized(lastDrinks)
					{
						if(lastCheck.get() < (System.currentTimeMillis()-16000))
						{
							lastCheck.set(System.currentTimeMillis());
							List<MOB> delList = null;
							for(MOB M : lastDrinks.keySet())
							{
								Long time = lastDrinks.get(M);
								if(time.longValue()<(System.currentTimeMillis()-16000))
								{
									if(delList == null)
										delList = new LinkedList<MOB>();
									delList.add(M);
								}
							}
							if(delList != null)
							{
								for(MOB M : delList)
									lastDrinks.remove(M);
							}
						}
					}
					Long time=lastDrinks.get(msg.source());
					if(time==null)
					{
						Ability A=CMClass.getAbility("Prayer_CureLight");
						if(A!=null)
							A.invoke(msg.source(),msg.source(),true,phyStats().level());
						A=CMClass.getAbility("Prayer_RemovePoison");
						if(A!=null)
							A.invoke(msg.source(),msg.source(),true,phyStats().level());
						A=CMClass.getAbility("Prayer_CureDisease");
						if(A!=null)
							A.invoke(msg.source(),msg.source(),true,phyStats().level());
						time=Long.valueOf(System.currentTimeMillis());
						lastDrinks.put(msg.source(),time);
					}
				}
				else
				{
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,msg.targetCode(),msg.targetMessage(),CMMsg.NO_EFFECT,null));
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
