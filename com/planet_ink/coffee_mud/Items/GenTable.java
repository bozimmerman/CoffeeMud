package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenTable extends GenRideable
{
	public String ID(){	return "GenTable";}
	protected String	readableText="";
	public GenTable()
	{
		super();
		setName("a generic table");
		baseEnvStats.setWeight(250);
		setDisplayText("a generic table is here.");
		setDescription("");
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setRiderCapacity(4);
		setRideBasis(Rideable.RIDEABLE_TABLE);
		recoverEnvStats();
	}

}
