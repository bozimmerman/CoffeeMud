package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Halfling extends StdRace
{
	public String ID(){	return "Halfling"; }
	public String name(){ return "Halfling"; }
	public int shortestMale(){return 40;}
	public int shortestFemale(){return 36;}
	public int heightVariance(){return 6;}
	public int lightestWeight(){return 80;}
	public int weightVariance(){return 50;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Halfling";}
	private String[]culturalAbilityNames={"Elvish","Cooking"};
	private int[]culturalAbilityProfficiencies={25,75};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProfficiencies(){return culturalAbilityProfficiencies;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_ALL;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+1);
		affectableStats.setStat(CharStats.MAX_DEXTERITY_ADJ,affectableStats.getStat(CharStats.MAX_DEXTERITY_ADJ)+1);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-1);
		affectableStats.setStat(CharStats.MAX_STRENGTH_ADJ,affectableStats.getStat(CharStats.MAX_STRENGTH_ADJ)-1);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+10);
	}
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			// Have to, since it requires use of special constructor
			Armor s1=CMClass.getArmor("GenShirt");
			s1.setName("a small tunic");
			s1.setDisplayText("a small tunic is folded neatly here.");
			s1.setDescription("It is a small but nicely made button-up tunic.");
			s1.text();
			outfitChoices.addElement(s1);
			Armor p1=CMClass.getArmor("GenPants");
			p1.setName("some small pants");
			p1.setDisplayText("some small pants lie here.");
			p1.setDescription("They appear to be for a dimunitive person, and extend barely past the knee at that.");
			p1.text();
			outfitChoices.addElement(p1);
		}
		return outfitChoices;
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r has very little life left.^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in small streams of blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of small wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and small gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and small gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few small bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised in small places.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some small cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and small scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of hairy "+name().toLowerCase()+" feet",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
