package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Sasquatch extends Gorilla
{
	public String ID(){	return "Sasquatch"; }
	public String name(){ return "Sasquatch"; }
	protected long forbiddenWornBits(){return 0;}
	public String racialCatagory(){return "Primate";}
	
	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+5);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+5);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<4;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" spleen",EnvResource.RESOURCE_HIDE));
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
