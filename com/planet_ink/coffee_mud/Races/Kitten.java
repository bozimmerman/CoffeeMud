package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Kitten extends Cat
{
	public String ID(){	return "Kitten"; }
	public String name(){ return "Kitten"; }
	protected int shortestMale(){return 4;}
	protected int shortestFemale(){return 4;}
	protected int heightVariance(){return 3;}
	protected int lightestWeight(){return 7;}
	protected int weightVariance(){return 10;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_EARS-Item.ON_EYES;}
	public String racialCatagory(){return "Feline";}
	
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
