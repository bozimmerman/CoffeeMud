package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GiantRat extends Rat
{
	protected static Vector resources=new Vector();
	public GiantRat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Giant Rat";
		// inches
		shortestMale=12;
		shortestFemale=12;
		heightVariance=6;
		// pounds
		lightestWeight=25;
		weightVariance=10;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD;
	}
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,6);
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