package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Mouse extends Rodent
{
	public String ID(){	return "Mouse"; }
	public String name(){ return "Mouse"; }
	public String racialCatagory(){return "Rodent";}
	
	protected static Vector resources=new Vector();
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("some "+name().toLowerCase()+" hair",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
					("a pair of "+name().toLowerCase()+" teeth",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
					("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
