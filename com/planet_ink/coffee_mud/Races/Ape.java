package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ape extends Monkey
{
	public String ID(){	return "Ape"; }
	public String name(){ return "Ape"; }
	protected int shortestMale(){return 52;}
	protected int shortestFemale(){return 50;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 150;}
	protected int weightVariance(){return 80;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Primate";}
	
	protected static Vector resources=new Vector();
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
					("a strip of "+name().toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("an "+name().toLowerCase()+" nose",EnvResource.RESOURCE_HIDE));
				for(int i=0;i<3;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" flesh",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
