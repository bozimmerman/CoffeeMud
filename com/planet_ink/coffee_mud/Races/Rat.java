package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;

public class Rat extends Rodent
{
	protected static Vector resources=new Vector();
	public Rat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rat";
		// inches
		shortestMale=6;
		shortestFemale=6;
		heightVariance=6;
		// pounds
		lightestWeight=10;
		weightVariance=10;
	}
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
					("ape fur",EnvResource.RESOURCE_FUR));
			}
		}
		return resources;
	}
}
