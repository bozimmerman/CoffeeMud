package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;

public class GardenSnake extends Snake
{
	protected static Vector resources=new Vector();
	public GardenSnake()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Garden Snake";
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
