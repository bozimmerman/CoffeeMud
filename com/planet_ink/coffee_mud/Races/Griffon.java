package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Griffon extends GreatBird
{
	protected static Vector resources=new Vector();
	public Griffon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Griffon";
		// inches
		shortestMale=56;
		shortestFemale=59;
		heightVariance=12;
		// pounds
		lightestWeight=160;
		weightVariance=80;
		forbiddenWornBits=Item.HELD|Item.WIELD;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)	
	{}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some greasy "+name.toLowerCase()+" claws",EnvResource.RESOURCE_BONE));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("some dirty "+name.toLowerCase()+" feathers",EnvResource.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" meat",EnvResource.RESOURCE_POULTRY));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name.toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
