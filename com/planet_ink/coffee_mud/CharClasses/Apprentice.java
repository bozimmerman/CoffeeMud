package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.CostDef.Cost;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass.SubClassRule;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Apprentice extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Apprentice";
	}

	private final static String localizedStaticName = CMLib.lang().L("Apprentice");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Commoner";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 5;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return -1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_WISDOM;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 10;
	}

	@Override
	public int getTrainsFirstLevel()
	{
		return 6;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/9)+(1*(1?4))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/10)+(1*(1?2))";
	}

	@Override
	public int getLevelCap()
	{
		return 1;
	}

	@Override
	public SubClassRule getSubClassRule()
	{
		return SubClassRule.ANY;
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_CLOTH;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_DAGGERONLY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(final MOB mob)
	{
		return disallowedWeapons;
	}

	protected Set<Tickable> currentApprentices = Collections.synchronizedSet(new HashSet<Tickable>());

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ClanCrafting",false);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_HEROIC | Area.THEME_TECHNOLOGY;
	}

	@Override
	public boolean qualifiesForThisClass(final MOB mob, final boolean quiet)
	{
		if(!super.qualifiesForThisClass(mob, quiet))
			return false;
		if(mob==null)
			return true;
		final CharClass curClass = mob.baseCharStats().getCurrentClass();
		final String currentClassID=curClass.ID();
		if(currentClassID.equalsIgnoreCase("StdCharClass")) // this is the starting character rule
			return true;
		if(mob.basePhyStats().level()>1)
		{
			if(!quiet)
				mob.tell(L("You are beyond apprentice skill at this point."));
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==myHost)
		&&(msg.targetMinor()==CMMsg.TYP_TRAIN)
		&&(((MOB)msg.target()).charStats().getCurrentClass().ID().equals("Apprentice"))
		&&(msg.tool() != null))
		{
			final String costStr = msg.tool().getStat("COST");
			final String typStr = msg.tool().getStat("TYPE");
			if((costStr!=null)
			&&(costStr.length()>0)
			&&(typStr!=null)
			&&(typStr.length()>0))
			{
				final Cost C = Cost.valueOf(costStr);
				if((C != null)
				&&(CMClass.getCharClass(typStr)==null))
				{
					//final CostManager cM = CMLib.utensils().createCostManager(C);
					//TODO: something that prevents an Apprentice from using their last train
				}
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		if(affectableStats.getCurrentClass().ID().equals("Apprentice"))
		{
			if(!currentApprentices.contains(affectedMOB))
				currentApprentices.add(affectedMOB);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(ticking instanceof MOB)
		&&(!((MOB)ticking).isMonster()))
		{
			if((!((MOB)ticking).charStats().getCurrentClass().ID().equals("Apprentice"))
			&&(currentApprentices.contains(ticking)))
			{
				currentApprentices.remove(ticking);
				((MOB)ticking).tell(L("\n\r\n\r^ZYou are no longer an apprentice!!!!^N\n\r\n\r"));
				CMLib.leveler().postExperience((MOB)ticking,"CLASS:"+ID(),null,null,1000, false);
			}
		}
		return super.tick(ticking,tickID);
	}

	private final String[]	raceRequiredList	= new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String, Integer>[]	minimumStatRequirements	= new Pair[]
	{
		new Pair<String, Integer>("Wisdom", Integer.valueOf(5)),
		new Pair<String, Integer>("Intelligence", Integer.valueOf(5))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public void startCharacter(final MOB mob, final boolean isBorrowedClass, final boolean verifyOnly)
	{
		super.startCharacter(mob, isBorrowedClass, verifyOnly);
		if(!verifyOnly)
		{
			if(mob.playerStats()!=null)
			{
				mob.playerStats().setBonusCommonSkillLimits(mob.playerStats().getBonusCommonSkillLimits()+1);
				mob.playerStats().setBonusCraftingSkillLimits(mob.playerStats().getBonusCraftingSkillLimits()+1);
				mob.playerStats().setBonusNonCraftingSkillLimits(mob.playerStats().getBonusNonCraftingSkillLimits()+1);
			}
		}
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Dagger");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	@Override
	public int adjustExperienceGain(final MOB host, final MOB mob, final MOB victim, int amount)
	{
		if((amount > 0)&&(!expless()))
		{
			if(mob.charStats().getCurrentClass() == this)
			{
				if(mob.getExperience() + amount > mob.getExpNextLevel())
				{
					amount = mob.getExpNextLevel() - mob.getExperience() - 1;
					if(amount < 0)
						amount = 0;
				}
			}
		}
		return amount;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Gains lots of xp for training to a new class, and gets bonus common skills.");
	}
}
