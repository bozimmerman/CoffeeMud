package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.StdItem;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenSSConsole extends GenShipItem
{
	public String ID(){	return "GenSSConsole";}
	public GenSSConsole()
	{
		super();
		setName("a generic computer console");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic computer console sits here.");
		setDescription("");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_STEEL);
	}

	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSSConsole)) return false;
		return super.sameAs(E);
	}
}