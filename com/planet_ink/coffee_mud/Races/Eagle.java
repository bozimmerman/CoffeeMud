package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;

public class Eagle extends GreatBird
{
	protected static Vector resources=new Vector();
	public Eagle()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Eagle";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("an "+name.toLowerCase()+" beak",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" feathers",EnvResource.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name.toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
