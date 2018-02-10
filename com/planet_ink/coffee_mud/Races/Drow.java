package com.planet_ink.coffee_mud.Races;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2006 Lee Fox

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
public class Drow extends Elf
{
	@Override
	public String ID()
	{
		return "Drow";
	}

	public Drow()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Syphilis");
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Drow");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 59;
	}

	@Override
	public int shortestFemale()
	{
		return 59;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 80;
	}

	@Override
	public int weightVariance()
	{
		return 80;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Elf");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	//todo: re-add light sensitivity
	private final String[]	racialEffectNames			= { "Prayer_TaintOfEvil", "Spell_LightSensitivity" };
	private final int[]		racialEffectLevels			= { 1, 1 };
	private final String[]	racialEffectParms			= {  "", "" };

	@Override
	protected String[] racialEffectNames()
	{
		return racialEffectNames;
	}

	@Override
	protected int[] racialEffectLevels()
	{
		return racialEffectLevels;
	}

	@Override
	protected String[] racialEffectParms()
	{
		return racialEffectParms;
	}

	private final String[]	culturalAbilityNames			= { "Drowish", "Spell_DarknessGlobe", "Spell_FaerieFire", "Undercommon" };
	private final int[]		culturalAbilityProficiencies	= { 75, 100, 25, 25 };
	private final int[]		culturalAbilityLevels			= { 0, 1, 0, 0 };
	private final boolean[] culturalAbilityAutoGains		= { true, true, true, true };

	@Override
	public String[] culturalAbilityNames()
	{
		return culturalAbilityNames;
	}

	@Override
	public int[] culturalAbilityProficiencies()
	{
		return culturalAbilityProficiencies;
	}

	@Override
	public int[] culturalAbilityLevels()
	{
		return culturalAbilityLevels;
	}

	@Override
	protected boolean[] culturalAbilityAutoGains()
	{
		return culturalAbilityAutoGains;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 2, 20, 110, 175, 263, 350, 390, 430 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(ticking instanceof MOB)
		{
			final MOB mob=(MOB)ticking;
			if((!mob.isPlayer())
			&&(!CMLib.flags().canBeSeenBy(mob.location(), mob))
			&&(!CMLib.flags().isInDark(mob.location()))
			&&(mob.location()!=null)
			&&(mob.fetchEffect("Spell_DarknessGlobe")==null)
			&&(CMLib.dice().roll(1, 10, -1)==1))
			{
				Ability A=mob.fetchAbility("Spell_DarknessGlobe");
				if(A!=null)
					mob.enqueCommands(new XVector<List<String>>(new XVector<String>("CAST","DARKNESS GLOBE")), 0);
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
		{
			final MOB mob=(MOB)affected;
			final Room room=mob.location();
			if(room.getArea().getClimateObj().canSeeTheSun(room)
			&&(affectableStats.armor()<0))
				affectableStats.setArmor(0);
		}

		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED|PhyStats.CAN_SEE_DARK);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+1);
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+2);
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectableStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)+2);
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-3);
		affectableStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-3);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-3);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-3);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC,affectableStats.getStat(CharStats.STAT_SAVE_MAGIC)+20);

		if(affectedMOB.location()!=null)
		{
			final MOB mob=affectedMOB;
			final Room room=mob.location();
			if(room.getArea().getClimateObj().canSeeTheSun(room))
			{
				affectableStats.setStat(CharStats.STAT_DEXTERITY, affectableStats.getStat(CharStats.STAT_DEXTERITY) /2);
			}
		}
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			// Have to, since it requires use of special constructor
			final Armor s1=CMClass.getArmor("GenShirt");
			if(s1 == null)
				return new Vector<Item>();
			s1.setName(L("a delicate black shirt"));
			s1.setDisplayText(L("a delicate black shirt sits gracefully here."));
			s1.setDescription(L("Obviously fine craftmenship, with sharp folds and intricate designs."));
			s1.text();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(s1);

			final Armor s2=CMClass.getArmor("GenShoes");
			s2.setName(L("a pair of sandals"));
			s2.setDisplayText(L("a pair of sandals lie here."));
			s2.setDescription(L("Obviously fine craftmenship, these light leather sandals have tiny woodland drawings in them."));
			s2.text();
			outfitChoices.add(s2);

			final Armor p1=CMClass.getArmor("GenPants");
			p1.setName(L("some delicate leggings"));
			p1.setDisplayText(L("a pair delicate black leggings sit here."));
			p1.setDescription(L("Obviously fine craftmenship, with sharp folds and intricate designs.  They look perfect for dancing in!"));
			p1.text();
			outfitChoices.add(p1);

			final Armor s3=CMClass.getArmor("GenBelt");
			outfitChoices.add(s3);
		}
		return outfitChoices;
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		return funHumanoidWeapon();
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is facing mortality!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in blood.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding badly from lots of wounds.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has numerous bloody wounds and gashes.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some bloody wounds and gashes.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a few bloody wounds.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is cut and bruised.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has some minor cuts and bruises.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few bruises and scratches.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a few small bruises.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a pair of @x1 ears",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}

