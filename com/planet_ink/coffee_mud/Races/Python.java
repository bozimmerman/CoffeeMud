package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Python extends Snake
{
	protected static Vector resources=new Vector();
	public Python()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Python";
		// inches
		shortestMale=6;
		shortestFemale=6;
		heightVariance=3;
		// pounds
		lightestWeight=15;
		weightVariance=20;
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<5;i++)
					resources.addElement(makeResource
					("a strip of "+name.toLowerCase()+" hide",EnvResource.RESOURCE_SCALES));
				for(int i=0;i<3;i++)
					resources.addElement(makeResource
					("a pound of "+name.toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
