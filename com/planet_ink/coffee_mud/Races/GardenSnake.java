package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;

public class GardenSnake extends Snake
{
	public String ID(){	return "GardenSnake"; }
	public String name(){ return "Garden Snake"; }
	protected static Vector resources=new Vector();
	public String racialCategory(){return "Serpent";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}
	
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" scales",EnvResource.RESOURCE_SCALES));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
