package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class StdShipComponent extends StdShipItem implements ShipComponent
{
	public String ID(){	return "StdShipComponent";}
	public StdShipComponent()
	{
		super();
		setName("a ships component");
		setDisplayText("a small ships component sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}
	
	private int componentType=ShipComponent.COMPONENT_MISC;
	public int componentType(){return componentType;}
	public void setComponentType(int type){componentType=type;}
}
