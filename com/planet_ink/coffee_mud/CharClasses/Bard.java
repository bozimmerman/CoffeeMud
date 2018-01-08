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
   Copyright 2001-2018 Bo Zimmerman

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
public class Bard extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Bard";
	}

	private final static String localizedStaticName = CMLib.lang().L("Bard");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return ID();
	}

	@Override
	public int getBonusPracLevel()
	{
		return 1;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_CHARISMA;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 10;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/3)+(2*(1?6))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/6)+(1*(1?2))";
	}

	@Override
	protected String armorFailMessage()
	{
		return L("<S-NAME> armor makes <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!");
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_NONMETAL;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_THIEFLIKE;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	public Bard()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=7;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Befriend",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Detection",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Song_Nothing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Haggle",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Song_Seeing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Song_Valor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Song_Charm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Song_Armor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Song_Babble",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Song_Clumsiness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Song_Rage",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Song_Mute",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Distract",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Peek",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Song_Serenity",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Song_Revelation",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Song_Friendship",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Unbinding",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Inebriation",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Song_Comprehension",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Health",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Song_Mercy",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Trip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Song_Silence",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Song_Dexterity",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_DetectTraps",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Song_Protection",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_SongWrite",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Song_Mana",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Song_Quickness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Lethargy",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Song_Flight",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Song_Knowledge",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Swipe",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Song_Blasting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Song_Strength",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Song_Thanks",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Song_Lullibye",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Song_Distraction",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Song_Flying",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_Steal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Death",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Song_Disgust",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Song_Rebirth",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Song_Ode",true);
	}

	@Override
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount)
	{
		return Bard.bardAdjustExperienceGain(host, mob, victim, amount, 5.0);
	}
	
	public static int bardAdjustExperienceGain(MOB host, MOB mob, MOB victim, int amount, double rate)
	{
		double theAmount=amount;
		if((mob!=null)&&(victim!=null)&&(theAmount>10.0))
		{
			if(host == mob)
			{
				final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
				final double origAmount=theAmount;
				for (final Object element : H)
				{
					final MOB mob2=(MOB)element;
					if((mob2!=mob)
					&&(mob2!=victim)
					&&(mob2.location()!=null)
					&&(mob2.location()==mob.location()))
					{
						if(!mob2.isMonster())
							theAmount+=(origAmount/rate);
						else
						if(!CMLib.flags().isAnimalIntelligence(mob2))
							theAmount+=1.0;
					}
				}
			}
			else
			if((!host.isMonster())&&(!mob.isMonster()))
				theAmount = 1.1 * theAmount;
		}
		return (int)Math.round(theAmount);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		Bard.visitationBonusMessage(host,msg);
	}
	
	public static void visitationBonusMessage(Environmental host, CMMsg msg)
	{
		if((msg.target() instanceof Room)
		&&(msg.source()==host)
		&&(!msg.source().isMonster())
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source().playerStats()!=null))
		{
			final Room R=(Room)msg.target();
			final MOB mob=msg.source();
			final Physical hostP=(Physical)host;
			if(((R.roomID().length()>0)
				||((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0)))
			&&(!CMath.bset(R.getArea().flags(),Area.FLAG_INSTANCE_CHILD))
			&&(!msg.source().playerStats().hasVisited(R))
			)
			{
				final Area A=R.getArea();
				MOB M=null;
				boolean pub=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m);
					if((M instanceof ShopKeeper)
					&&(M.getStartRoom()==R))
					{
						final List<Ability> V2=new Vector<Ability>();
						for(final Iterator<Environmental> i=((ShopKeeper)M).getShop().getStoreInventory();i.hasNext();)
						{
							final Environmental O=i.next();
							if(O instanceof Potion)
							{
								V2.addAll(((Potion)O).getSpells());
								for(int v=V2.size()-1;v>=0;v--)
								{
									if((V2.get(v).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_POISON)
										V2.get(v);
								}
							}
							if(O instanceof Drink)
							{
								V2.addAll(CMLib.flags().domainAffects((Drink)O,Ability.ACODE_POISON));
								final String name=" "+O.Name().toLowerCase();
								if(name.endsWith(" beer")
								||name.endsWith(" liquor")
								||name.endsWith(" ale")
								||name.endsWith(" whiskey")
								||name.endsWith(" wine"))
									pub=true;
							}
							for(int v=0;v<V2.size();v++)
								pub=pub||CMath.bset(V2.get(v).flags(),Ability.FLAG_INTOXICATING);
						}
					}
				}
				if(pub)
				{
					final int xpGain=50;
					if(CMLib.leveler().postExperience((MOB)host,null,null,xpGain,true))
						msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have discovered a new pub, you gain @x1 experience.^?",""+xpGain),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				}
				if((!mob.playerStats().hasVisited(A))&&(mob.soulMate()==null))
				{
					if(mob.playerStats().addRoomVisit(R))
					{
						CMLib.players().bumpPrideStat(mob,AccountStats.PrideStat.ROOMS_EXPLORED,1);
						int xp=(int)Math.round(100.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],hostP.phyStats().level()));
						if(xp>250)
							xp=250;
						if((xp>0)&&CMLib.leveler().postExperience((MOB)host,null,null,xp,true))
							msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have discovered '@x1', you gain @x2 experience.^?",A.name(),""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					}
				}
				else
				{
					final int pctBefore=mob.playerStats().percentVisited((MOB)host,A);
					if(mob.playerStats().addRoomVisit(R))
					{
						CMLib.players().bumpPrideStat(mob,AccountStats.PrideStat.ROOMS_EXPLORED,1);
						final int pctAfter=mob.playerStats().percentVisited((MOB)host,A);
						if((pctBefore<50)&&(pctAfter>=50))
						{
							int xp=(int)Math.round(50.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],hostP.phyStats().level()));
							if(xp>125)
								xp=125;
							if((xp>0)&&CMLib.leveler().postExperience((MOB)host,null,null,xp,true))
								msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have familiarized yourself with '@x1', you gain @x2 experience.^?",A.name(),""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
						}
						else
						if((pctBefore<90)&&(pctAfter>=90))
						{
							int xp=(int)Math.round(100.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],hostP.phyStats().level()));
							if(xp>250)
								xp=250;
							if((xp>0)&&CMLib.leveler().postExperience((MOB)host,null,null,xp,true))
								msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have explored '@x1', you gain @x2 experience.^?",A.name(),""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
						}
					}
				}
			}
		}
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
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
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	@Override
	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if(E instanceof MusicalInstrument)
			return true;
		return super.weaponCheck(mob,sourceCode,E);
	}

	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Elf","Dwarf","Halfling","Elf-kin","Centaur",
		"Svirfneblin","Githyanki","Faerie","Aarakocran","Merfolk"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Charisma",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return "";
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Receives group bonus combat experience when in an intelligent group, and more for a group with players.  "
				+ "Receives exploration and pub-finding experience based on danger level.");
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
