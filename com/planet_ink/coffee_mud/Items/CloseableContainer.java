package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class CloseableContainer extends StdContainer
{
	public String ID(){	return "CloseableContainer";}
	public CloseableContainer()
	{
		super();

		hasALid=true;
		isOpen=false;
		material=EnvResource.RESOURCE_OAK;
	}


}
