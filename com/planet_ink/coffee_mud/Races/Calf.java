package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Calf extends Cow
{
	protected static Vector resources=new Vector();
	public Calf()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=36;
		shortestFemale=36;
		heightVariance=6;
		// pounds
		lightestWeight=150;
		weightVariance=100;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,13);
		affectableStats.setStat(CharStats.DEXTERITY,5);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name.toLowerCase()+" hooves",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
				("a strip of "+name.toLowerCase()+" leather",EnvResource.RESOURCE_LEATHER));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" meat",EnvResource.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name.toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
