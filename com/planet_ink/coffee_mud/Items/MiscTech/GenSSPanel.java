package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.StdContainer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class GenSSPanel extends GenShipContainer
{
	public String ID(){	return "GenSSPanel";}
	public GenSSPanel()
	{
		super();
		setName("a generic space ship panel");
		baseEnvStats.setWeight(2);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		setLidsNLocks(true,true,false,false);
		capacity=500;
		setMaterial(EnvResource.RESOURCE_STEEL);
		recoverEnvStats();
	}

	public String displayText(){
		if(isOpen())
			return name()+" is opened here.";
		else
			return "";
	}
	public boolean canContain(Environmental E)
	{
		if(!super.canContain(E)) return false;
		if(E instanceof ShipComponent)
		{
			int myType=((ShipComponent)E).componentType();
			switch(componentType())
			{
			case ShipComponent.COMPONENT_PANEL_ANY:
				return true;
			case ShipComponent.COMPONENT_PANEL_ENGINE:
				return myType==ShipComponent.COMPONENT_ENGINE;
			case ShipComponent.COMPONENT_PANEL_POWER:
				return myType==ShipComponent.COMPONENT_POWER;
			case ShipComponent.COMPONENT_PANEL_SENSOR:
				return myType==ShipComponent.COMPONENT_SENSOR;
			case ShipComponent.COMPONENT_PANEL_WEAPON:
				return myType==ShipComponent.COMPONENT_WEAPON;
			case ShipComponent.COMPONENT_PANEL_COMPUTER:
				return myType==ShipComponent.COMPONENT_COMPUTER;
			}
		}
		return true;
	}

	public boolean isGeneric(){return true;}
}
