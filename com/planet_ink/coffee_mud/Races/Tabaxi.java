package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Tabaxi extends GreatCat
{
	public String ID(){	return "Tabaxi"; }
	public String name(){ return "Tabaxi"; }
	public int shortestMale(){return 69;}
	public int shortestFemale(){return 69;}
	public int heightVariance(){return 8;}
	public int lightestWeight(){return 120;}
	public int weightVariance(){return 80;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Feline";}
	private String[] racialAbilityNames={"Skill_Hide","Skill_Sneak"};
	private int[]racialAbilityLevels={4,4};
	private int[]racialAbilityProfficiencies={50,50};
	private boolean[]racialAbilityQuals={false,false};
	public String[] racialAbilityNames(){return racialAbilityNames;}
	public int[] racialAbilityLevels(){return racialAbilityLevels;}
	public int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	public boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_MAGICONLY;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
	        affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED); //would like to turn this darkvison if it is okay with Zac.
		affectableStats.setSpeed(affectableStats.speed() * 2.0);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
	    affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+3);
		affectableStats.setStat(CharStats.MAX_DEXTERITY_ADJ,affectableStats.getStat(CharStats.MAX_DEXTERITY_ADJ)+3);
        affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+3);
		affectableStats.setStat(CharStats.MAX_STRENGTH_ADJ,affectableStats.getStat(CharStats.MAX_STRENGTH_ADJ)+3);
		affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)-4);
		affectableStats.setStat(CharStats.MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.MAX_INTELLIGENCE_ADJ)-4);
		affectableStats.setStat(CharStats.WISDOM,affectableStats.getStat(CharStats.WISDOM)-2);
		affectableStats.setStat(CharStats.MAX_WISDOM_ADJ,affectableStats.getStat(CharStats.MAX_WISDOM_ADJ)-2);
	}

	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a set of "+name().toLowerCase()+" fangs",EnvResource.RESOURCE_BONE));
				for(int i=0;i<5;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",EnvResource.RESOURCE_LEATHER));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
