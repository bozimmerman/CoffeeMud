package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;

public class LockableContainer extends Container
{
	public LockableContainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		hasALid=true;
		isOpen=false;
		hasALock=true;
		isLocked=true;
	}
	
	public Environmental newInstance()
	{
		return new LockableContainer();
	}
}
