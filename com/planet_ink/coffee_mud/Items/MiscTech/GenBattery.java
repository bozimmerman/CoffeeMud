package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.StdItem;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenBattery extends GenElecItem
{
	public String ID(){	return "GenBattery";}
	public GenBattery()
	{
		super();
		setName("a generic battery");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic battery sits here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_STEEL);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenBattery)) return false;
		return super.sameAs(E);
	}
}