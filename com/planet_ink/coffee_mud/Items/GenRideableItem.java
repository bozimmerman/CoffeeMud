package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenBoat extends GenItem implements Boat
{
	public GenBoat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic boat";
		displayText="a generic boat sits here.";
		description="Looks like a boat";
		isReadable=false;
	}
	public Environmental newInstance()
	{
		return new GenBoat();
	}
	public boolean isGeneric(){return true;}
}
