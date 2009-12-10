package com.planet_ink.coffee_mud.Items.interfaces;
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
public interface ShipComponent extends Item
{
	public interface ShipEngine extends ShipComponent
	{
	    public int getMaxThrust();
	    public void setMaxThrust(int max);
	    public int getThrust();
	    public void setThrust(int max);
	}
	public interface ShipPowerSource extends ShipComponent
	{
	    
	}
	public interface ShipWeapon extends ShipComponent
	{
	    
	}
	public interface ShipSensor extends ShipComponent
	{
	    
	}
	public interface ShipEnviroControl extends ShipComponent
	{
	    
	}
	public interface ShipPanel extends ShipComponent
	{
		public final static int COMPONENT_PANEL_ANY=0;
		public final static int COMPONENT_PANEL_WEAPON=1;
		public final static int COMPONENT_PANEL_ENGINE=2;
		public final static int COMPONENT_PANEL_SENSOR=3;
		public final static int COMPONENT_PANEL_POWER=4;
		public final static int COMPONENT_PANEL_COMPUTER=5;
		public final static int COMPONENT_PANEL_ENVIRO=6;
		public final static String[] COMPONENT_PANEL_DESC={
		    "ANY","WEAPON","ENGINE","SENSOR","POWER","COMPUTER","ENVIRO-CONTROL"
		};
		
		public int panelType();
		public void setPanelType(int type);
	}
}
