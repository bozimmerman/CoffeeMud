package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Mouse extends Rodent
{
	protected static Vector resources=new Vector();
	public Mouse()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mouse";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("some "+name.toLowerCase()+" hair",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
					("a pair of "+name.toLowerCase()+" teeth",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
					("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
