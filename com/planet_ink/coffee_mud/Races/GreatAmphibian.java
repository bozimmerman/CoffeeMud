package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GreatAmphibian extends StdRace
{
	public String ID(){	return "GreatAmphibian"; }
	public String name(){ return "Great Amphibian"; }
	public int shortestMale(){return 20;}
	public int shortestFemale(){return 25;}
	public int heightVariance(){return 5;}
	public int lightestWeight(){return 155;}
	public int weightVariance(){return 40;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_EYES;}
	public String racialCategory(){return "Amphibian";}
	protected static Vector resources=new Vector();
	private String[]racialAbilityNames={"Skill_Swim"};
	private int[]racialAbilityLevels={1};
	private int[]racialAbilityProfficiencies={100};
	private boolean[]racialAbilityQuals={false};
	protected String[] racialAbilityNames(){return racialAbilityNames;}
	protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	protected int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,2 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setPermaStat(CharStats.INTELLIGENCE,1);
		affectableStats.setPermaStat(CharStats.DEXTERITY,13);
	}
	public String arriveStr()
	{
		return "shuffles in";
	}
	public String leaveStr()
	{
		return "shuffles";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("some sharp teeth");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		MOB mob=(MOB)affected;
		if(mob.location()!=null)
	    {
			if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
			||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			{
				if((affectableStats.sensesMask()&EnvStats.CAN_NOT_BREATHE)==EnvStats.CAN_NOT_BREATHE)
					affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_NOT_BREATHE);
			}
		}
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<15;i++)
				resources.addElement(makeResource
				("some "+name().toLowerCase(),EnvResource.RESOURCE_FISH));
				for(int i=0;i<5;i++)
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
