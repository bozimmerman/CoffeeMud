package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Cobra extends Snake
{
	public String ID(){	return "Cobra"; }
	public String name(){ return "Cobra"; }
	protected int shortestMale(){return 6;}
	protected int shortestFemale(){return 6;}
	protected int heightVariance(){return 3;}
	protected int lightestWeight(){return 15;}
	protected int weightVariance(){return 20;}
	public String racialCategory(){return "Serpent";}
	
	protected static Vector resources=new Vector();
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,18);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" fangs",EnvResource.RESOURCE_BONE));
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_SCALES));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
