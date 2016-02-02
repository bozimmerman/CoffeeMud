package com.planet_ink.coffee_mud.Items.ShipTech;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2016-2016 Bo Zimmerman

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
public class GenLightSwitch extends GenElecCompItem
{
	@Override
	public String ID()
	{
		return "GenLightSwitch";
	}

	protected String readableText = "";

	public GenLightSwitch()
	{
		super();
		setName("a light switch");
		basePhyStats.setWeight(1);
		setDisplayText("A light switch is on the wall here.");
		setDescription("");
		baseGoldValue=15;
		super.setPowerCapacity(1);
		super.setPowerRemaining(0);
		basePhyStats().setSensesMask(basePhyStats.sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED|PhyStats.SENSE_ITEMNOTGET);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}
	
	@Override
	public void recoverPhyStats()
	{
		if(activated() && (this.powerRemaining() > 0))
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_LIGHTSOURCE);
		else
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_GLOWING);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(!activated() || (this.powerRemaining() <= 0))
		{
			if(CMLib.flags().isGlowing(affected))
				affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_GLOWING);
			if(CMLib.flags().isLightSource(affected))
				affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_LIGHTSOURCE);
			affectableStats.setDisposition(phyStats().disposition()|PhyStats.IS_DARK);
		}
	}
	
	@Override
	public int powerNeeds()
	{
		if(activated())
			return 1;
		return super.powerNeeds();
	}
	
	public void tellOtherSwitches(Environmental host, CMMsg msg, boolean goAhead)
	{
		Room R=CMLib.map().roomLocation(this);
		if(R!=null)
		{
			R.recoverPhyStats();
			if((activated() == goAhead)&&(displayText().length()>0)&&(CMLib.flags().isSeeable(this)))
			{
				final Stack<Room> switchStack = new Stack<Room>();
				final CMMsg oMsg = (CMMsg)msg.copyOf();
				oMsg.setSourceMessage("");
				oMsg.setTargetMessage("");
				oMsg.setOthersMessage("");
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					Room oR=R.getRoomInDir(d);
					if((oR!=null)&&(R.getExitInDir(d)!=null))
						switchStack.push(oR);
				}
				while(switchStack.size()>0)
				{
					R=switchStack.pop();
					boolean didAnything = false;
					for(int i=0;i<R.numItems();i++)
					{
						final Item I=R.getItem(i);
						if((I instanceof GenLightSwitch)
						&&(I!=this)
						&&((I.displayText().length()==0)||(!CMLib.flags().isSeeable(I)))
						&&(!((GenLightSwitch)I).activated()))
						{
							oMsg.setTarget(I);
							didAnything=true;
							if(R.okMessage(host, oMsg))
								R.sendOthers(oMsg.source(), oMsg);
						}
					}
					if(didAnything)
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							Room oR=R.getRoomInDir(d);
							if((oR!=null)&&(R.getExitInDir(d)!=null))
								switchStack.push(oR);
						}
					}
				}
				
			}
		}
	}
	
	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		final boolean wasActivated = activated();
		super.executeMsg(host, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
			{
				tellOtherSwitches(host,msg,!wasActivated);
				break;
			}
			case CMMsg.TYP_DEACTIVATE:
			{
				tellOtherSwitches(host,msg,wasActivated);
				break;
			}
			}
		}
	}
	
}
