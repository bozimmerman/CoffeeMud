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
public class Doomsayer extends Cleric
{
	@Override
	public String ID()
	{
		return "Doomsayer";
	}

	private final static String localizedStaticName = CMLib.lang().L("Doomsayer");

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
		return CharClass.WEAPONS_EVILCLERIC;
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
		return 1000;
	}

	public Doomsayer()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Ember",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_CurseFlames",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_Rot",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_DarkeningAura",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtFire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtGood",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_Faithless",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_FlameWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CauseFatigue",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Cannibalism",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_CurseMetal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_Intimidate",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_CurseMind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_Plague",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Fortress",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_Demonshield",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_AuraHarm",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true,CMParms.parseSemicolons("Prayer_Curse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_MassDeafness",false,CMParms.parseSemicolons("Prayer_Deafness",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Anger",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_DailyBread",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_InfuseUnholiness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_DoomAura",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_CurseLuck",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",false,CMParms.parseSemicolons("Prayer_Curse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassBlindness",false,CMParms.parseSemicolons("Prayer_Blindness",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_DemonicConsumption",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Condemnation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true,CMParms.parseSemicolons("Prayer_Curse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_CurseMinds",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_Doomspout",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true,CMParms.parseSemicolons("Prayer_GreatCurse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"FIRE",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_FireHealing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_Stoning",0,"",false,true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return super.getRequiredRaceList();
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(9)),
		new Pair<String,Integer>("Wisdom",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives 1 pt damage reduction/level from fire attacks.");
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Always fumbles good prayers, and fumbles all prayers when alignment is above 500.  Qualifies and receives evil prayers.  "
				+ "Using non-aligned prayers introduces failure chance.  Vulnerable to cold attacks.");
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
		&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
		{
			final int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_COLD))
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
			final Weapon w=CMClass.getWeapon("Shortsword");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

}
