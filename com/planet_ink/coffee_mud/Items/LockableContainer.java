package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class LockableContainer extends StdContainer
{
	public String ID(){	return "LockableContainer";}
	public LockableContainer()
	{
		super();
		hasALid=true;
		isOpen=false;
		hasALock=true;
		isLocked=true;
		setMaterial(EnvResource.RESOURCE_OAK);
	}

	public Environmental newInstance()
	{
		return new LockableContainer();
	}
}
