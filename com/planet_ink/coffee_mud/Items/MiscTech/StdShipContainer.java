package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class StdShipContainer extends StdElecContainer
{
	public String ID(){	return "StdShipContainer";}
	public StdShipContainer()
	{
		super();
		setName("a ships container");
		setDisplayText("a small ships container sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}
	
	private int componentType=ShipComponent.COMPONENT_MISC;
	public int componentType(){return componentType;}
	public void setComponentType(int type){componentType=type;}
}
