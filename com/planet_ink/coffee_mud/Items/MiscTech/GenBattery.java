package com.planet_ink.coffee_mud.Items.MiscTech;
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

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class GenBattery extends GenElecItem implements Electronics.PowerSource
{
	public String ID(){	return "GenBattery";}

	private volatile String circuitKey=null;

	public GenBattery()
	{
		super();
		setName("a generic battery");
		basePhyStats.setWeight(2);
		setDisplayText("a generic battery sits here.");
		setDescription("");
		baseGoldValue=5;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenBattery)) return false;
		return super.sameAs(E);
	}
	
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
		}
		super.destroy();
	}
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if(prevOwner != owner)
		{
			if(owner instanceof Room)
			{
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			}
			else
			{
				CMLib.tech().unregisterElectronics(this,circuitKey);
				circuitKey=null;
			}
		}
	}
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(!StdElecItem.isAllWiringConnected(this))
				{
					if(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
						msg.source().tell("The panel containing "+name()+" is not activated or connected.");
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				break;
			case CMMsg.TYP_LOOK:
				break;
			case CMMsg.TYP_POWERCURRENT:
				break;
			}
		}
		return super.okMessage(host, msg);
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> activate(s) <T-NAME>.");
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, "<S-NAME> deactivate(s) <T-NAME>.");
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(name()+" is currently "+(activated()?"delivering power.\n\r":"deactivated/disconnected.\n\r"));
				return;
			}
		}
		super.executeMsg(host, msg);
	}
	
}
