package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class GiantWolf extends Wolf
{
	protected static Vector resources=new Vector();
	public GiantWolf()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Giant Wolf";
		// inches
		shortestMale=26;
		shortestFemale=26;
		heightVariance=12;
		// pounds
		lightestWeight=80;
		weightVariance=60;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_NECK;
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
