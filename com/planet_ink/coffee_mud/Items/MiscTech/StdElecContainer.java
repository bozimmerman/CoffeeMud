package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.StdContainer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdElecContainer extends StdContainer implements Electronics
{
	public String ID(){	return "StdElecContainer";}
	public StdElecContainer()
	{
		super();
		setName("an electronic container");
		setDisplayText("an electronic container sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}

	protected int fuelType=EnvResource.RESOURCE_ENERGY;
	public int fuelType(){return fuelType;}
	public void setFuelType(int resource){fuelType=resource;}
	protected long powerCapacity=100;
	public long powerCapacity(){return powerCapacity;}
	public void setPowerCapacity(long capacity){powerCapacity=capacity;}
	protected long power=100;
	public long powerRemaining(){return power;}
	public void setPowerRemaining(long remaining){power=remaining;}
	protected boolean activated=false;
	public boolean activated(){return activated;}
	public void activate(boolean truefalse){activated=truefalse;}
}
