package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Dragon extends StdRace
{
	public String ID(){	return "Dragon"; }
	public String name(){ return "Dragon"; }
	public int shortestMale(){return 84;}
	public int shortestFemale(){return 78;}
	public int heightVariance(){return 80;}
	public int lightestWeight(){return 2000;}
	public int weightVariance(){return 500;}
	public long forbiddenWornBits(){return Item.WIELD|Item.ON_WAIST|Item.ON_BACK|Item.ABOUT_BODY|Item.ON_FEET|Item.ON_HANDS;}
	public String racialCategory(){return "Dragon";}
	private String[]racialAbilityNames={"Dragonbreath","WingFlying"};
	private int[]racialAbilityLevels={5,1};
	private int[]racialAbilityProfficiencies={100,100};
	private boolean[]racialAbilityQuals={false,false};
	protected String[] racialAbilityNames(){return racialAbilityNames;}
	protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	protected int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,2 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,5,20,110,325,500,850,950,1050};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
		if(!Sense.isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+15);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+15);
		affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)+15);
	}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		super.startRacing(mob,verifyOnly);
		if(!verifyOnly)
		{
			Ability A=CMClass.getAbility("Draconic");
			if(A!=null)
			{
				mob.addAbility(A);
				A.autoInvocation(mob);
				if(mob.isMonster())
					A.invoke(mob,mob,false,0);
			}
		}
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("huge talons");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is raging in bloody pain!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and gashed scales.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised heavily.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and scratched scales.^N";
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
				("a "+name().toLowerCase()+" claw",EnvResource.RESOURCE_BONE));
				for(int i=0;i<100;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" scales",EnvResource.RESOURCE_SCALES));
				for(int i=0;i<50;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
