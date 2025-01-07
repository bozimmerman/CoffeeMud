package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2020-2024 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Reliquist extends Thief
{
	@Override
	public String ID()
	{
		return "Reliquist";
	}

	private final static String localizedStaticName = CMLib.lang().L("Reliquist");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_RELIQUIST;
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Elf","Humanoid","Aarakocran","Merfolk","Fairy-kin",
		"Goblinoid","Dwarf","Gnome",
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[]
	{
		new Pair<String, Integer>("Dexterity", Integer.valueOf(9)),
		new Pair<String, Integer>("Wisdom", Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public int maxLanguages()
	{
		return CMProps.getIntVar(CMProps.Int.MAXLANGUAGES) * 5;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Magic resistance, 1%/level.  Huge discounts when buying holy potions after 5th level.  Ability to memorize prayers learned through PrayerCraft. "
				+ "Can see prayer scroll charges at level 30.  Can see prayer boons and banes on marked.");
	}

	public Reliquist()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public String getMovementFormula()
	{
		return "6*((@x2<@x3)/18)";
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/3)+(1*(1?10))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/5)+(1*(1?3))";
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Whip",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Staff",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_ReadPrayer",0,true);

		//Q=Qualify G=GAIN A=Gain if alignment is the same as the prayer
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_MaskFaith",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_RepurposeText",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_ProtectItem",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_RelicUse",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Graverobbing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_IlluminateScroll",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Mark",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_DepleteScroll",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_EmpowerScroll",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_AttuneScroll",false); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Carpentry",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Revoke",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_DetectTraps",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_Whiplash",0,false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Pick",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Bash",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_StorePrayer",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_Tongues",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_Fluency",false); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Prayercraft",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_SenseDevotion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_UnearthDemography",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_HammerRing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_DecipherScript",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_SenseMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_EmpowerShield",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_BorrowBoon",true);
 		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_RemoveTraps",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_ReadLanguage",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_CureLight",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RechargeRelic",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_CauseLight",false); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Trip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_LesserWardingGlyph",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_DeflectPrayer",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"ScrollScribing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"StaffMaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ReligiousDoubt",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_RevealText",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_DepleteRelic",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_EmpowerRelic",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_AttuneRelic",false); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_TransferBane",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_ReleasePrayer",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_TransferBoon",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_ResearchRegionMap",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_EnchantRelic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_PowerGrab",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_ResearchItem",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Whipsmack",0,false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_ImprovedWardingGlyph",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_SenseParish",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_BlendIn",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"ImprovedAlchemy",false,CMParms.parseSemicolons("Alchemy(100)",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_FalseFaith",0,true); // disguise faith
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Digsite",0,false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_FalseService",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_Disenchant",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_SenseDigs",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_UsePotion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_ScrollFamiliarity",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Detection",0,false);
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_EmpowerHolyWeapon",false,"+SKILLS -Prayer_EmpowerUnholyWeapon -Prayer_EmpowerSacredWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_EmpowerUnholyWeapon",false,"+SKILLS -Prayer_EmpowerHolyWeapon -Prayer_EmpowerSacredWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.NEUTRAL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_EmpowerSacredWeapon",false,"+SKILLS -Prayer_EmpowerUnholyWeapon -Prayer_EmpowerHolyWeapon"); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_ReflectPrayer",false); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_HeroicReflexes",0,false); // heroic leap in the doc
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_ImbueShield",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_DefileShield",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_CondemnMark",0,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Alchemy",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_SeekersPrayer",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_ShareBoon",false); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_StealBoon",false, CMParms.parse("Thief_BorrowBoon"));
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_EmpowerJustWeapon",false,"+SKILLS -Prayer_EmpowerModestWeapon -Prayer_EmpowerFoulWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.MODERATE))
			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_EmpowerModestWeapon",false,"+SKILLS -Prayer_EmpowerJustWeapon -Prayer_EmpowerFoulWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.CHAOTIC))
			CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_EmpowerFoulWeapon",false,"+SKILLS -Prayer_EmpowerJustWeapon -Prayer_EmpowerModestWeapon"); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_AutoHammerRing",0,false, CMParms.parse("Fighter_HammerRing"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_GreaterWardingGlyph",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_FindSacredItem",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_BefoulShrine",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_EmpowerHolyArmor",false,"+SKILLS -Prayer_EmpowerUnholyArmor -Prayer_EmpowerSacredArmor"); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_EmpowerUnholyArmor",false,"+SKILLS -Prayer_EmpowerHolyArmor -Prayer_EmpowerSacredArmor"); // special A
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_EmpowerSacredArmor",false,"+SKILLS -Prayer_EmpowerHolyArmor -Prayer_EmpowerUnholyArmor"); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_InciteDivineFeud",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_ProtectSacredItem",false, new XVector<String>("Prayer_ProtectItem"));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_WhipStrip",0,false);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_EternalItem",false, new XVector<String>("Prayer_ProtectItem"));

		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ImbueHolyWeapon",false,"+SKILLS -Prayer_ImbueUnholyWeapon -Prayer_ImbueSacredWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.EVIL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ImbueUnholyWeapon",false,"+SKILLS -Prayer_ImbueHolyWeapon -Prayer_ImbueSacredWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.NEUTRAL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ImbueSacredWeapon",false,"+SKILLS -Prayer_ImbueHolyWeapon -Prayer_ImbueUnholyWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ImbueJustWeapon",false,"+SKILLS -Prayer_ImbueModestWeapon -Prayer_ImbueFoulWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.MODERATE))
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ImbueModestWeapon",false,"+SKILLS -Prayer_ImbueFoulWeapon -Prayer_ImbueJustWeapon"); // special A
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.CHAOTIC))
			CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_ImbueFoulWeapon",false,"+SKILLS -Prayer_ImbueModestWeapon -Prayer_ImbueJustWeapon"); // special A

		CMLib.ableMapper().addCharAbilityMapping(ID(),27,"Prayer_EternalItem",false, new XVector<String>("Prayer_ProtectItem"));

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_SacredImbuingQuest",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),35,"Prayer_PlanarPilgrimage", 0, "", false,
				 SecretFlag.MASKED, null, "+PLANE \"-Prime Material\"");
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC, affectableStats.getStat(CharStats.STAT_SAVE_MAGIC) + affectableStats.getClassLevel(this));
		if(CMath.bset(affected.basePhyStats().disposition(),PhyStats.IS_BONUS))
			affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+30);
		if(affectableStats.getCurrentClass()==this)
			affectableStats.setStat(CharStats.STAT_SAVE_TRAPS,affectableStats.getStat(CharStats.STAT_SAVE_TRAPS)+5+affectableStats.getClassLevel(this));
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("SmallHammer");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	protected int holyQuality(final Ability A)
	{
		if(CMath.bset(A.flags(),Ability.FLAG_HOLY))
		{
			if(!CMath.bset(A.flags(),Ability.FLAG_UNHOLY))
				return 1000;
		}
		else
		if(CMath.bset(A.flags(),Ability.FLAG_UNHOLY))
			return 0;
		return 500;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((myHost==null)
		||(!(myHost instanceof MOB)))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)myHost;
		if(msg.amISource(mob))
		{
			if((msg.tool() instanceof Ability)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(!mob.isMonster())
			&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(mob.isMine(msg.tool()))
			&&(isQualifyingAuthority(mob,(Ability)msg.tool())))
			{
				if((msg.source().baseCharStats().getMyDeity()==null)
				&&(CMLib.map().deities().hasMoreElements()))
				{
					msg.source().tell(L("You lack the true faith to do that.  Convert to the worship of a deity first."));
					return false;
				}
				final Ability A=(Ability)msg.tool();
				if(A.appropriateToMyFactions(mob))
					return true;

				final int hq=holyQuality(A);
				int basis=0;
				if(hq==0)
					basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().getAlignmentID()),Faction.Align.EVIL);
				else
				if(hq==1000)
					basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().getAlignmentID()),Faction.Align.GOOD);
				else
				{
					basis=CMLib.factions().getAlignPurity(mob.fetchFaction(CMLib.factions().getAlignmentID()),Faction.Align.NEUTRAL);
					basis-=10;
				}
				if(CMLib.dice().rollPercentage()>basis)
					return true;

				if(hq==0)
					mob.tell(L("The evil nature of @x1 disrupts your prayer.",A.name()));
				else
				if(hq==1000)
					mob.tell(L("The goodness of @x1 disrupts your prayer.",A.name()));
				else
				if(CMLib.flags().isGood(mob))
					mob.tell(L("The non-good nature of @x1 disrupts your thought.",A.name()));
				else
				if(CMLib.flags().isEvil(mob))
					mob.tell(L("The non-evil nature of @x1 disrupts your thought.",A.name()));
				return false;

			}
			if(mob.charStats().getClassLevel(this)>4)
			{
				if(((msg.sourceMinor()==CMMsg.TYP_BUY)
					||(msg.sourceMinor()==CMMsg.TYP_LIST))
				&&(msg.tool() instanceof Potion))
				{
					final Potion P=(Potion)msg.tool();
					boolean hasPrayer=true;
					for(final Ability A : P.getSpells())
					{
						if((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER)
							hasPrayer=false;
					}
					if(hasPrayer)
					{
						mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_BONUS);
						mob.recoverPhyStats();
						mob.recoverCharStats();
					}
				}
				else
				if((mob.basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS)
				{
					mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()-PhyStats.IS_BONUS);
					mob.recoverPhyStats();
					mob.recoverCharStats();
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void endCharacter(final MOB mob)
	{
		final Vector<Ability> otherChoices=new Vector<Ability>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A2=mob.fetchAbility(a);
			if((A2!=null)
			&&(!A2.isSavable())
			&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
				otherChoices.addElement(A2);
		}
		for(int a=0;a<otherChoices.size();a++)
			mob.delAbility(otherChoices.elementAt(a));
	}

	private void addAbilityToPrayercraftist(final MOB mob, final Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Prayercraft");
		if(enabledA!=null)
		{
			final List<String> ables=CMParms.parseCommas(enabledA.text(), true);
			if(!ables.contains(A.ID()))
			{
				if(enabledA.text().length()==0)
					enabledA.setMiscText(A.ID());
				else
					enabledA.setMiscText(enabledA.text()+", "+A.ID());
				mob.addAbility(A);
			}
			else
			if(mob.isMine(A) && (A.proficiency()<75) && (!A.isSavable()))
				A.setProficiency(A.proficiency()+(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)/3));
		}
	}

	private void clearAbilityFromPrayercraftist(final MOB mob, final Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Prayercraft");
		if(enabledA!=null)
		{
			final List<String> ables=CMParms.parseCommas(enabledA.text(), true);
			if(ables.contains(A.ID()))
			{
				if(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.ALLSKILLS))
				{
					ables.remove(A.ID());
					enabledA.setMiscText(CMParms.toListString(ables));
					mob.delAbility(A);
				}
			}
		}
	}

	@Override
	public void grantAbilities(final MOB mob, final boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		if(mob.playerStats()==null)
		{
			final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(final AbilityMapper.AbilityMapping able : V)
			{
				final Ability A=CMClass.getAbility(able.abilityID());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID()))
				&&(!CMLib.ableMapper().getAllQualified(ID(),true,A.ID())))
				{
					giveMobAbility(mob,A,
								   CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),
								   CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),
								   isBorrowedClass);
				}
			}
			return;
		}

		if(!ID().equals("Reliquist"))
			return;

		final int classLevel = mob.baseCharStats().getClassLevel(this);
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==classLevel)
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
				return;
		}
		// now only give one, for current level, respecting alignment!
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(A.appropriateToMyFactions(mob))
			&&(CMLib.ableMapper().getSecretSkill(ID(),true,A.ID())==SecretFlag.PUBLIC)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==classLevel)
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
			{
				//final AbilityMapper.AbilityMapping able=CMLib.ableMapper().getQualifyingMapping(ID(),true,A.ID());
				giveMobAbility(mob,A,
							   CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),
							   CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),
							   isBorrowedClass);
				break; // one is enough
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
			return;
		final MOB mob=(MOB)myHost;
		if(msg.amISource(mob))
		{
			if((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE))
			{
				if((msg.target() instanceof Wand)
				&&((((Wand)msg.target()).getEnchantType()<0)
					||(((Wand)msg.target()).getEnchantType()==Ability.ACODE_PRAYER))
				&&(mob.charStats().getClassLevel(this)>=30))
				{
					final int maxCharges = ((Wand)msg.target()).usesRemaining();
					final String message;
					if((maxCharges < Integer.MAX_VALUE/2)&&(maxCharges > 0))
						message=L("<O-NAME> has @x1/@x2 charges remaining.",""+((Wand)msg.target()).usesRemaining(),""+maxCharges);
					else
						message=L("<O-NAME> has @x1 charges remaining.",""+((Wand)msg.target()).usesRemaining());
					msg.addTrailerMsg(CMClass.getMsg(mob, null, msg.target(), CMMsg.MSG_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, message));
				}
				else
				if((msg.target() instanceof MOB)
				&&(msg.target() != mob)
				&&(((MOB)msg.target()).fetchEffect("Thief_Mark")!=null))
				{
					final MOB tgtM = (MOB)msg.target();
					final List<String> boons = new ArrayList<String>();
					final List<String> banes = new ArrayList<String>();
					for(final Enumeration<Ability> a=tgtM.effects();a.hasMoreElements();)
					{
						final Ability effA = a.nextElement();
						if((effA != null)
						&&(effA.canBeUninvoked())
						&&((effA.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
						{
							if((effA.abstractQuality() == Ability.QUALITY_MALICIOUS)
							&&(effA.invoker() != tgtM))
								banes.add(effA.Name());
							else
								boons.add(effA.Name());
						}
					}
					if(boons.size()>0)
					{
						msg.addTrailerMsg(CMClass.getMsg(mob, null, msg.target(), CMMsg.MSG_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT,
								L("Boons: @x1",CMLib.english().toEnglishStringList(boons))));
					}
					if(banes.size()>0)
					{
						msg.addTrailerMsg(CMClass.getMsg(mob, null, msg.target(), CMMsg.MSG_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT,
								L("Banes: @x1",CMLib.english().toEnglishStringList(banes))));
					}
				}

			}
			else
			if((msg.tool()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
			{
				if(msg.tool().ID().equals("Skill_Prayercraft"))
				{
					if((msg.tool().text().length()>0)
					&&(msg.target() instanceof MOB)
					&&(msg.source().baseCharStats().getMyDeity()!=null))
					{
						Ability A=((MOB)msg.target()).fetchAbility(msg.tool().text());
						if(A==null)
							return;
						final Ability myA=mob.fetchAbility(A.ID());
						if(myA!=null)
						{
							if((!A.isSavable())
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
							&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30))
								addAbilityToPrayercraftist(mob,A);
						}
						else
						if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30)
						{
							final Vector<Ability> otherChoices=new Vector<Ability>();
							for(int a=0;a<mob.numAbilities();a++)
							{
								final Ability A2=mob.fetchAbility(a);
								if((A2!=null)
								&&(!A2.isSavable())
								&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
									otherChoices.addElement(A2);
							}
							A=(Ability)A.copyOf();
							A.setProficiency(0);
							A.setSavable(false);
							if(otherChoices.size()>(mob.charStats().getClassLevel(this)/3))
							{
								final Ability A2=otherChoices.elementAt(CMLib.dice().roll(1,otherChoices.size(),-1));
								clearAbilityFromPrayercraftist(mob,A2);
							}
							addAbilityToPrayercraftist(mob,A);
						}
					}
				}
				else
				if((msg.sourceMinor()!=CMMsg.TYP_PREINVOKE)
				&&(msg.tool().ID().equals("Prayer_EnchantRelic")
					||msg.tool().ID().equals("Prayer_LesserWardingGlyph")
					||msg.tool().ID().equals("Prayer_ImprovedWardingGlyph")
					||msg.tool().ID().equals("Prayer_GreaterWardingGlyph")
					||msg.tool().ID().equals("Prayer_StorePrayer")
					||msg.tool().ID().equals("Prayer_ImbueShield")
					||msg.tool().ID().equals("Prayer_ImbueHolyWeapon")
					||msg.tool().ID().equals("Prayer_ImbueUnholyWeapon")
					||msg.tool().ID().equals("Prayer_ImbueSacredWeapon")
					||msg.tool().ID().equals("Prayer_ImbueJustWeapon")
					||msg.tool().ID().equals("Prayer_ImbueFoulWeapon")
					||msg.tool().ID().equals("Prayer_ImbueModestWeapon")
					||msg.tool().ID().equals("Prayer_DefileShield")
					//||msg.tool().ID().equals("Prayer_ReleasePrayer") // not necc because the spell is fake cast
					||msg.tool().ID().equals("Prayer_SacredImbuingQuest")))
				{
					final Ability A=mob.fetchAbility(msg.tool().text());
					if((A!=null)&&(!A.isSavable()))
						clearAbilityFromPrayercraftist(mob,A);
				}
				else
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
				{
					final Ability A=mob.fetchAbility(msg.tool().ID());
					if((A!=null)&&(!A.isSavable())
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
						clearAbilityFromPrayercraftist(mob,A);
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((ticking instanceof MOB)
		&&((((MOB)ticking).basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS))
		{
			((MOB)ticking).basePhyStats().setDisposition(((MOB)ticking).basePhyStats().disposition()-PhyStats.IS_BONUS);
			((MOB)ticking).recoverPhyStats();
		}
		return super.tick(ticking,tickID);
	}
}
