package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class GenPipe extends GenCigar
{
	public String ID(){	return "GenPipe";}
	private String readableText = "";
	public GenPipe()
	{
		super();
		setName("a generic pipe");
		baseEnvStats.setWeight(1);
		setDisplayText("a generic pipe sits here.");
		setDescription("This nice wooden pipe could use some herbs in it to smoke.");
		setMaterial(EnvResource.RESOURCE_OAK);
		durationTicks=0;
		destroyedWhenBurnedOut=false;
		baseGoldValue=5;
		capacity=2;
		recoverEnvStats();
	}

}
