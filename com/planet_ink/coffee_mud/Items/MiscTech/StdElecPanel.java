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
}
