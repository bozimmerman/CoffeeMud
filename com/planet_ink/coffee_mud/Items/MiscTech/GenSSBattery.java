package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.StdItem;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenSSBattery extends GenShipComponent
{
	public String ID(){	return "GenSSBattery";}
	public GenSSBattery()
	{
		super();
		setName("a generic ships battery");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic ships battery sits here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_STEEL);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSSBattery)) return false;
		return super.sameAs(E);
	}
}