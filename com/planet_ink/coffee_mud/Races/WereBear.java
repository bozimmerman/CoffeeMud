package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WereBear extends Bear
{
	protected static Vector resources=new Vector();
	public WereBear()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="WereBear";
		// inches
		shortestMale=59;
		shortestFemale=59;
		heightVariance=12;
		// pounds
		lightestWeight=80;
		weightVariance=80;
		forbiddenWornBits=0;
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
					("ape fur",EnvResource.RESOURCE_FUR));
			}
		}
		return resources;
	}
}