package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Kitten extends Cat
{
	protected static Vector resources=new Vector();
	public Kitten()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=4;
		shortestFemale=4;
		heightVariance=3;
		// pounds
		lightestWeight=7;
		weightVariance=10;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET;
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("ape fur",EnvResource.RESOURCE_FUR));
			}
		}
		return resources;
	}
}
