package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Kitten extends Cat
{
	public String ID(){	return "Kitten"; }
	public String name(){ return "Kitten"; }
	public int shortestMale(){return 4;}
	public int shortestFemale(){return 4;}
	public int heightVariance(){return 3;}
	public int lightestWeight(){return 7;}
	public int weightVariance(){return 10;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_EARS-Item.ON_EYES;}
	public String racialCategory(){return "Feline";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
