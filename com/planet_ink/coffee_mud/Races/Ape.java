package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ape extends Monkey
{
	protected static Vector resources=new Vector();
	public Ape()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Ape";
		// inches
		shortestMale=52;
		shortestFemale=50;
		heightVariance=12;
		// pounds
		lightestWeight=150;
		weightVariance=80;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,16);
		affectableStats.setStat(CharStats.DEXTERITY,15);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<3;i++)
				resources.addElement(makeResource
				("a strip of "+name.toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("an "+name.toLowerCase()+" nose",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" flesh",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name.toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
