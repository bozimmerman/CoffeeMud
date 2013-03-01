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
public class StdElecPanel extends StdElecContainer implements Electronics.ElecPanel
{
	private volatile String circuitKey=null;
	
	protected ElecPanelType panelType=ElecPanelType.ANY;
	public ElecPanelType panelType(){return panelType;}
	public void setPanelType(ElecPanelType type){panelType=type;}

	public String displayText(){
		if(isOpen())
			return name()+" is opened here.";
		return "";
	}
	public boolean canContain(Environmental E)
	{
		if(!super.canContain(E)) return false;
		if(E instanceof Electronics)
		{
			switch(panelType())
			{
			case ANY:
				return true;
			case ENGINE:
				return E instanceof ShipComponent.ShipEngine;
			case POWER:
				return E instanceof PowerSource;
			case SENSOR:
				return E instanceof ShipComponent.ShipSensor;
			case WEAPON:
				return E instanceof ShipComponent.ShipWeapon;
			case COMPUTER:
				return E instanceof Software;
			case ENVIRO_CONTROL:
				return E instanceof ShipComponent.ShipEnviroControl;
			case GENERATOR:
				return E instanceof PowerGenerator;
			default:
				return true;
			}
		}
		return true;
	}

	public boolean isGeneric(){return true;}
	
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Room)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ROOMCIRCUITED);
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
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			if(msg.sourceMinor()==CMMsg.TYP_POWERCURRENT) // these double as ticks!
			{
				final Room R=CMLib.map().roomLocation(this);
				int powerRemaining=msg.value();
				final List<Item> contents=getContents();
				final CMMsg powerMsg=CMClass.getMsg(msg.source(), CMMsg.MSG_POWERCURRENT, null);
				for(int i=contents.size()-1;i>=0;i--)
				{
					Item I=contents.get(i);
					if(I instanceof Electronics)
					{
						int powerToTake=powerRemaining/(i+1);
						powerMsg.setValue(powerToTake);
						powerMsg.setTarget(I);
						if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
							R.send(powerMsg.source(), powerMsg);
						powerRemaining-=(powerMsg.value()<0)?powerToTake:(powerToTake-powerMsg.value());
					}
				}
				CMClass.returnMsg(powerMsg);
				msg.setValue(powerRemaining);
			}
		}
	}
}
