package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Elf extends StdRace
{
	public String ID(){	return "Elf"; }
	public String name(){ return "Elf"; }
	protected int shortestMale(){return 59;}
	protected int shortestFemale(){return 59;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 80;}
	protected int weightVariance(){return 80;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Elf";}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return true;}

	public void startRacing(MOB mob, boolean verifyOnly)
	{
		super.startRacing(mob,verifyOnly);
		Ability A=CMClass.getAbility("Elvish");
		if(A!=null)
		{
			A.setProfficiency(100);
			mob.addAbility(A);
			A.autoInvocation(mob);
			if(mob.isMonster())
				A.invoke(mob,mob,true);
		}
		A=CMClass.getAbility("Foraging");
		if(A!=null)
		{
			A.setProfficiency(50);
			mob.addAbility(A);
			A.autoInvocation(mob);
		}
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+1);
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-1);
		affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+10);
	}
	public void outfit(MOB mob)
	{
		// Have to, since it requires use of special constructor
		Armor s1=CMClass.getArmor("GenShirt");
		s1.setName("a delicate green shirt");
		s1.setDisplayText("a delicate green shirt sits gracefully here.");
		s1.setDescription("Obviously fine craftmenship, with sharp folds and intricate designs.");
		
		Armor s2=CMClass.getArmor("GenShoes");
		s2.setName("a pair of sandals");
		s2.setDisplayText("a pair of sandals lie here.");
		s2.setDescription("Obviously fine craftmenship, these light leather sandals have tiny woodland drawings in them.");
			
		Armor p1=CMClass.getArmor("GenPants");
		p1.setName("some delicate leggings");
		p1.setDisplayText("a pair delicate brown leggings sit here.");
		p1.setDescription("Obviously fine craftmenship, with sharp folds and intricate designs.  They look perfect for dancing in!");
		wearOutfit(mob,s1,s2,p1);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is facing mortality!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" ears",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
