package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GiantRat extends Rat
{
	public String ID(){	return "GiantRat"; }
	public String name(){ return "Giant Rat"; }
	public int shortestMale(){return 12;}
	public int shortestFemale(){return 12;}
	public int heightVariance(){return 6;}
	public int lightestWeight(){return 25;}
	public int weightVariance(){return 10;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_EARS-Item.ON_EYES;}
	public String racialCategory(){return "Rodent";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
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
				for(int i=0;i<5;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hair",EnvResource.RESOURCE_HIDE));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" teeth",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}