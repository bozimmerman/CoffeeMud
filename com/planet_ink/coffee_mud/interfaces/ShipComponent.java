package com.planet_ink.coffee_mud.interfaces;

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
public interface ShipComponent extends Item
{
	public final static int COMPONENT_MISC=0;
	public final static int COMPONENT_PANEL_ANY=1;
	public final static int COMPONENT_POWER=2;
	public final static int COMPONENT_COMPUTER=3;
	public final static int COMPONENT_ENGINE=4;
	public final static int COMPONENT_WEAPON=5;
	public final static int COMPONENT_SENSOR=6;
	public final static int COMPONENT_PANEL_WEAPON=6;
	public final static int COMPONENT_PANEL_ENGINE=7;
	public final static int COMPONENT_PANEL_SENSOR=8;
	public final static int COMPONENT_PANEL_POWER=9;
	public final static int COMPONENT_PANEL_COMPUTER=10;
	public final static String[] COMPONENT_DESC={
		"MISC","PANEL-ANY","POWER","COMPUTER","ENGINE","WEAPON","SENSOR",
		"PANEL-WEAPON","PANEL-ENGINE","PANEL-SENSOR","PANEL-POWER", "PANEL-COMPUTER"
	};
	
	
	public int componentType();
	public void setComponentType(int type);
	
	
}
