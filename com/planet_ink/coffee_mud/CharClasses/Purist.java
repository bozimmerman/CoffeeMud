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
import com.planet_ink.coffee_mud.Libraries.Factions;
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
public class Purist extends Cleric
{
	@Override
	public String ID()
	{
		return "Purist";
	}

	private final static String localizedStaticName = CMLib.lang().L("Purist");

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
		return CharClass.WEAPONS_GOODCLERIC;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	protected int alwaysFlunksThisQuality()
	{
		return 0;
	}

	public Purist()
	{
		super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_TurnUndead",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Marry",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Extinguish",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_CreateWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_Purify",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Fidelity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sanctimonious",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CreateFood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_ProtCold",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_RighteousIndignation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_HuntEvil",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_Gills",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_CureFatigue",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Monolith",0,"ICE",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_FountainLife",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true,CMParms.parseSemicolons("Prayer_CureSerious",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_FreezeMetal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Conviction",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_InfuseHoliness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_PietyCurse",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_Wave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_AuraDivineEdict",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false,CMParms.parseSemicolons("Prayer_Freedom",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassMobility",false,CMParms.parseSemicolons("Prayer_ProtParalyzation",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Heal",true,CMParms.parseSemicolons("Prayer_CureCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Atonement",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false,CMParms.parseSemicolons("Prayer_Heal",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_Judgement",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false,CMParms.parseSemicolons("Prayer_HolyAura",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"WATER",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_IceHealing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_AuraIntolerance",false);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public boolean tick(Tickable myChar, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
		}
		return true;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Dwarf","Elf","HalfElf","Elf-kin",
		"Fairy-kin","Svirfneblin","Aarakocran","Merfolk","Faerie",
		"-Duergar","-Drow"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9)),
		new Pair<String,Integer>("Charisma",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives 1pt/level cold damage reduction.");
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Always fumbles evil prayers, and fumbles all prayers when alignment is below pure neutral.  Qualifies and receives good prayers, "
				+ "and bonus damage from good spells.  Using non-aligned prayers introduces failure chance.  Vulnerable to fire attacks.");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(myHost instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool())))
		{
			final int alignment = myChar.fetchFaction(CMLib.factions().AlignID());
			final int pct = CMLib.factions().getPercent(CMLib.factions().AlignID(), alignment);
			if(pct < 50)
			{
				myChar.tell(L("Your impurity disrupts the prayer."));
				return false;
			}
			final int hq=holyQuality((Ability)msg.tool());
			if(hq==0)
			{
				myChar.tell(L("You most certainly should not be casting that."));
				return false;
			}
		}

		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.sourceMinor()==CMMsg.TYP_COLD)
			||(msg.sourceMinor()==CMMsg.TYP_WATER)))
		{
			final int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
		{
			final int recovery=msg.value();
			msg.setValue(msg.value()+recovery);
		}
		else
		if(msg.amISource(myChar)
		&&(!msg.amITarget(myChar))
		&&(msg.tool() instanceof Ability)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(holyQuality((Ability)msg.tool())==1000))
			msg.setValue(msg.value()*2);
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
