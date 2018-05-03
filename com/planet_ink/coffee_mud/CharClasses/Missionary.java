package com.planet_ink.coffee_mud.CharClasses;
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

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class Missionary extends Cleric
{
	@Override
	public String ID()
	{
		return "Missionary";
	}

	private final static String localizedStaticName = CMLib.lang().L("Missionary");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Cleric";
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_WISDOM;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_NEUTRALCLERIC;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	public Missionary()
	{
		super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Annul",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_DivineLuck",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseLife",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Bury",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_InfuseBalance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_Position",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_BirdsEye",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CreateWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_SenseTraps",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_ElectricStrike",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Revival",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_AiryForm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_MinorInfusion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_HolyWind",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_Wings",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_RemoveCurse",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Etherealness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_Retribution",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_ChainStrike",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassMobility",true,CMParms.parseSemicolons("Prayer_ProtParalyzation",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_Monolith",0,"AIR",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Gateway",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_MoralBalance",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_ModerateInfusion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_Weather",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_UndeniableFaith",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"AIR",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ElectricHealing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_Sermon",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		for(final int i : CharStats.CODES.SAVING_THROWS())
		{
			affectableStats.setStat(i,
				affectableStats.getStat(i)
					+(affectableStats.getClassLevel(this)));
		}
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return super.getRequiredRaceList();
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Never fumbles neutral prayers, and receives 1pt/level luck bonus to all saving throws per level.  Receives 1pt/level electricity damage reduction.");
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Using non-neutral prayers introduces failure chance.  Vulnerable to acid attacks.");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_ELECTRIC))
		{
			final int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_ACID))
		{
			final int recovery=msg.value();
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("SmallMace");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

}
