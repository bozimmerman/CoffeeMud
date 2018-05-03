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
   Copyright 2017-2018 Bo Zimmerman

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
public class Scholar extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Scholar";
	}

	private final static String localizedStaticName = CMLib.lang().L("Scholar");

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
		return 1;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return -1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_INTELLIGENCE;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 50;
	}

	@Override
	public int maxLanguages()
	{
		return CMProps.getIntVar(CMProps.Int.MAXLANGUAGES) + 3;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/9)+(1*(1?3))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/9)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_CLOTH;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_STAFFONLY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	public Scholar()
	{
		super();
		maxStatAdj[CharStats.STAT_WISDOM]=6;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=6;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Studying",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Labeling",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"PaperMaking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_CombatLog",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Mark",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Organizing",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Dissertating",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_SmokeSignals",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Titling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_MorseCode",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"BookEditing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Druid_KnowPlants",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_SeaMapping",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_DecipherScript",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_RevealText",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Transcribing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Semaphore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_WildernessLore",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"BookNaming",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Speculate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Siegecraft",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Lore",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Skillcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_SecretWriting",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_HonoraryDegreeCommoner", false, CMParms.parseSemicolons("Studying",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_RacialLore", true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Cataloging",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Songcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_HonoraryDegreeFighter",false, CMParms.parseSemicolons("Studying",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_Observation",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_AnalyzeMark",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"PlantLore",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_HonoraryDegreeBard",false, CMParms.parseSemicolons("Studying",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"AttributeTraining",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Taxidermy",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Shush",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Prayercraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Appraise",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Recollecting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_HonoraryDegreeThief",false, CMParms.parseSemicolons("Studying",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"ScrollScribing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_Chantcraft",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Publishing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_HonoraryDegreeMage",false, CMParms.parseSemicolons("Studying",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Skill_PlanarLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Skill_EncryptedWriting",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Surveying",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_HonoraryDegreeDruid",false, CMParms.parseSemicolons("Studying",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Lecturing",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Comprehension",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_HonoraryDegreeCleric",false, CMParms.parseSemicolons("Studying",true));
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Enrolling",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Skill_Guildmaster",true);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// no xp from combat
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.source()==myHost)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amDead()||(((MOB)msg.target()).curState().getHitPoints()<=0))
		&&(msg.value()>0))
		{
			msg.setValue(0);
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean canBeADivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		return false;
	}

	@Override
	public boolean canBeABenificiary(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		return false;
	}

	@Override
	public int addedExpertise(final MOB host, final ExpertiseLibrary.Flag expertiseCode, final String abilityID)
	{
		if((expertiseCode == ExpertiseLibrary.Flag.XPCOST) && (abilityID.equals("ScrollScribing")))
			return 15;
		return 0;
	}

	private static final int[] FAV_DOMAINS= new int[] { 
			Ability.DOMAIN_EDUCATIONLORE, Ability.DOMAIN_ARCANELORE, 
			Ability.DOMAIN_NATURELORE, Ability.DOMAIN_COMBATLORE,
			Ability.DOMAIN_CALLIGRAPHY };
	
	public static void visitationBonusMessage(Environmental host, CMMsg msg)
	{
		if((msg.target() instanceof Room)
		&&(msg.source()==host)
		&&(!msg.source().isMonster())
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source().playerStats()!=null))
		{
			final Room R=(Room)msg.target();
			if(((R.roomID().length()>0)
				||((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0)))
			&&(!CMath.bset(R.getArea().flags(),Area.FLAG_INSTANCE_CHILD))
			&&(!msg.source().playerStats().hasVisited(R))
			)
			{
				MOB M=null;
				boolean bookDealer=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m);
					if((M instanceof ShopKeeper)
					&&(M.getStartRoom()==R))
					{
						if((((ShopKeeper)M).getWhatIsSoldMask() & ShopKeeper.DEAL_BOOKS)!=0)
						{
							bookDealer=true;
						}
					}
				}
				if(bookDealer)
				{
					final int xpGain=50;
					if(CMLib.leveler().postExperience((MOB)host,null,null,xpGain,true))
						msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have discovered a new place of books and gain @x1 experience.^?",""+xpGain),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				}
			}
		}
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.source()==myHost)
		{
			Scholar.visitationBonusMessage(myHost,msg);
			
			if((msg.targetMinor()==CMMsg.TYP_TEACH)
			&&(msg.target() instanceof MOB))
				CMLib.leveler().postExperience(msg.source(), null, null, 100, false);
			else
			if(((msg.targetMinor()==CMMsg.TYP_WRITE)
				||(msg.targetMinor()==CMMsg.TYP_REWRITE)
				||(msg.targetMinor()==CMMsg.TYP_WROTE))
			&&(msg.target() instanceof Item)
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
			{
				if((msg.tool() instanceof Ability)
				&&(msg.targetMinor()==CMMsg.TYP_WROTE)
				&&(msg.tool().ID().equals("Skill_Map")||msg.tool().ID().equals("Thief_TreasureMap")||msg.tool().ID().equals("Skill_SeaMapping")))
					CMLib.leveler().postExperience(msg.source(), null, null, 10, false);
				else
				if((msg.tool() instanceof Ability)
				&&(msg.targetMinor()==CMMsg.TYP_WROTE)
				&&(msg.tool().ID().equals("Skill_Dissertation")))
					CMLib.leveler().postExperience(msg.source(), null, null, 25, false);
				else
				{
					final String msgStr =msg.targetMessage().trim();
					int numChars = msgStr.length()-CMStrings.countChars(msgStr, ' ');
					if(numChars > 10)
					{
						final Map<String,Object> persMap = Resources.getPersonalMap(msg.source(), true);
						if(persMap != null)
						{
							int xp = numChars/10;
							long[] xpTrap = (long[])persMap.get("SCHOLAR_WRITEXP");
							if(xpTrap == null)
							{
								xpTrap = new long[2];
								persMap.put("SCHOLAR_WRITEXP", xpTrap);
							}
							if(System.currentTimeMillis() > xpTrap[1])
							{
								xpTrap[0]=0;
								xpTrap[1]=System.currentTimeMillis() + (TimeManager.MILI_MINUTE * 10);
							}
							final long maxLevel = msg.source().getExpNeededLevel() / 24;
							if(xpTrap[0] < maxLevel)
							{
								if(100-xpTrap[0]<xp)
									xp=(int)(100-xpTrap[0]);
								xpTrap[0]+=xp;
								CMLib.leveler().postExperience(msg.source(), null, null, xp, false);
							}
						}
					}
				}
			}
		}
		if((msg.tool() instanceof Ability)
		&&(myHost instanceof MOB))
		{
			Ability A;
			if((myHost == msg.source())
			&&((A=msg.source().fetchAbility(msg.tool().ID()))!=null)
			&&(A.isSavable())
			&&(msg.source().isPlayer())
			&&(CMParms.contains(FAV_DOMAINS, ((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)))
			{
				final Map<String,Object> persMap = msg.source().playerStats().getClassVariableMap(this);
				if(persMap != null)
				{
					final String key = "LAST_DATE_FOR_"+A.ID().toUpperCase().trim();
					long[] lastTime = (long[])persMap.get(key);
					if(lastTime == null)
					{
						lastTime = new long[1];
						persMap.put(key, lastTime);
					}
					final Area homeA=CMLib.map().areaLocation(msg.source().getStartRoom());
					final TimeClock homeL = (homeA == null) ? null : homeA.getTimeObj();
					if((homeL!=null)
					&&((homeL.toHoursSinceEpoc() - lastTime[0])>homeL.getHoursInDay()))
					{
						lastTime[0] = homeL.toHoursSinceEpoc(); 
						CMLib.leveler().postExperience(msg.source(), null, null, 25, false);
					}
				}
			}
			
			if((((MOB)myHost).getVictim()!=msg.source())
			&&(msg.source().getVictim()!=myHost)
			&&(CMLib.dice().rollPercentage()<25)
			&&((A=msg.source().fetchAbility(msg.tool().ID()))!=null)
			&&(((MOB)myHost).fetchAbility(msg.tool().ID())!=null)
			&&(((MOB)myHost).getGroupMembers(new TreeSet<MOB>()).contains(msg.source())))
			{
				final Ability A1=(Ability)msg.tool();
				if((A1!=null)&&(A1.isSavable()))
					A1.helpProficiency(msg.source(), 0);
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(ID().equals(mob.charStats().getCurrentClass().ID()))
			{
			}
		}
		return super.tick(ticking,tickID);
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]
	{
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9)),
		new Pair<String,Integer>("Wisdom",Integer.valueOf(6))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices == null)
		{
			outfitChoices=new Vector<Item>();
			
			final Weapon w=CMClass.getWeapon("Staff");
			if(w == null)
				return new Vector<Item>();
			outfitChoices.add(w);
			
			final Item I=CMClass.getBasicItem("GenJournal");
			I.setName(L("Scholar`s Logbook"));
			I.setDisplayText(L("A Scholar`s Logbook has been left here."));
			outfitChoices.add(I);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Earns no combat experience.");
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Earn experience from teaching skills, making maps, writing books, visiting bookstores/libraries, and using certain skills daily. Gives bonus profficiency gains for group members.");
	}
}
