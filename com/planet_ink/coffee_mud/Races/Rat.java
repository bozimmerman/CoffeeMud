package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;

public class Rat extends Rodent
{
	public String ID(){	return "Rat"; }
	public String name(){ return "Rat"; }
	protected int shortestMale(){return 6;}
	protected int shortestFemale(){return 6;}
	protected int heightVariance(){return 6;}
	protected int lightestWeight(){return 10;}
	protected int weightVariance(){return 10;}
	public String racialCatagory(){return "Rodent";}
	
	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,4);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("some "+name().toLowerCase()+" hair",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
					("a pair of "+name().toLowerCase()+" teeth",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
					("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
