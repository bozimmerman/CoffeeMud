package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.Vector;
public class Cobra extends Snake
{
	public String ID(){	return "Cobra"; }
	public String name(){ return "Cobra"; }
	public int shortestMale(){return 6;}
	public int shortestFemale(){return 6;}
	public int heightVariance(){return 3;}
	public int lightestWeight(){return 15;}
	public int weightVariance(){return 20;}
	public String racialCategory(){return "Serpent";}
    private String[]racialAbilityNames={"Poison_Heartstopper"};
	private int[]racialAbilityLevels={5};
	private int[]racialAbilityProfficiencies={30};
	private boolean[]racialAbilityQuals={false};
	public String[] racialAbilityNames(){return racialAbilityNames;}
	public int[] racialAbilityLevels(){return racialAbilityLevels;}
	public int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	public boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}

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
