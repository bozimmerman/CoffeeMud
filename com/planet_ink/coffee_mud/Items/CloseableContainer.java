package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;

public class CloseableContainer extends Container
{
	public CloseableContainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		hasALid=true;
		isOpen=false;
	}
	
	public Environmental newInstance()
	{
		return new CloseableContainer();
	}
}
