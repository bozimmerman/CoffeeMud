package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Griffon extends GreatBird
{
	public String ID(){	return "Griffon"; }
	public String name(){ return "Griffon"; }
	protected int shortestMale(){return 56;}
	protected int shortestFemale(){return 59;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 160;}
	protected int weightVariance(){return 80;}
	protected long forbiddenWornBits(){return Item.HELD|Item.WIELD;}
	
	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)	
	{}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some greasy "+name().toLowerCase()+" claws",EnvResource.RESOURCE_BONE));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("some dirty "+name().toLowerCase()+" feathers",EnvResource.RESOURCE_FEATHERS));
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
