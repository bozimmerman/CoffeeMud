package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
public class Door extends StdClosedDoorway
{
	public Door()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Environmental newInstance()
	{
		return new Door();
	}
	
}
