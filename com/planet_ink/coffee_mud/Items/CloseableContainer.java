package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class CloseableContainer extends StdContainer
{
	public CloseableContainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		hasALid=true;
		isOpen=false;
		material=EnvResource.RESOURCE_OAK;
	}

	public Environmental newInstance()
	{
		return new CloseableContainer();
	}
}
