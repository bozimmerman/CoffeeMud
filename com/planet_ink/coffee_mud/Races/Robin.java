package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Robin extends Bird
{
	public String ID(){	return "Robin"; }
	public String name(){ return "Robin"; }
	public String racialCatagory(){return "Avian";}
	
	protected static Vector resources=new Vector();
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" feet",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" feathers",EnvResource.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",EnvResource.RESOURCE_POULTRY));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
