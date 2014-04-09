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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Bard extends StdCharClass
{
	public String ID(){return "Bard";}
	public String name(){return "Bard";}
	public String baseClass(){return ID();}
	public int getBonusPracLevel(){return 1;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 10;}
	public String getHitPointsFormula(){return "((@x6<@x7)/3)+(2*(1?6))"; }
	public String getManaFormula(){return "((@x4<@x5)/6)+(1*(1?2))"; }
	protected String armorFailMessage(){return "<S-NAME> armor makes <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Bard()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=7;
	}
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

	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount){ return Bard.bardAdjustExperienceGain(host,mob,victim,amount,5.0);}
	public static int bardAdjustExperienceGain(MOB host, MOB mob, MOB victim, int amount, double rate)
	{
		double theAmount=amount;
		if((mob!=null)&&(victim!=null)&&(theAmount>10.0))
		{
			if(host == mob)
			{
				Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
				double origAmount=theAmount;
				for(Iterator e=H.iterator();e.hasNext();)
				{
					MOB mob2=(MOB)e.next();
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

	public int availabilityCode(){return Area.THEME_FANTASY;}

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
			Room R=(Room)msg.target();
			MOB mob=msg.source();
			Physical hostP=(Physical)host;
			if(((R.roomID().length()>0)
			||((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0)))
			&&(!msg.source().playerStats().hasVisited(R)))
			{
				Area A=R.getArea();
				MOB M=null;
				boolean pub=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m);
					if((M instanceof ShopKeeper)
					&&(M.getStartRoom()==R))
					{
						List<Ability> V2=new Vector<Ability>();
						for(Iterator<Environmental> i=((ShopKeeper)M).getShop().getStoreInventory();i.hasNext();)
						{
							Environmental O=i.next();
							if(O instanceof Potion)
							{
								V2.addAll(((Potion)O).getSpells());
								for(int v=V2.size()-1;v>=0;v--)
									if((V2.get(v).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_POISON)
										V2.get(v);
							}
							if(O instanceof Drink)
							{
								V2.addAll(CMLib.flags().domainAffects((Drink)O,Ability.ACODE_POISON));
								String name=" "+O.Name().toLowerCase();
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
					if(CMLib.leveler().postExperience((MOB)host,null,null,50,true))
						msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,"^HYou have discovered a new pub, you gain "+50+" experience.^?",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				}
				if(!mob.playerStats().hasVisited(A))
				{
					if(mob.playerStats().addRoomVisit(R))
						CMLib.players().bumpPrideStat(mob,AccountStats.PrideStat.ROOMS_EXPLORED,1);
					int xp=(int)Math.round(100.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],hostP.phyStats().level()));
					if(xp>250) xp=250;
					if((xp>0)&&CMLib.leveler().postExperience((MOB)host,null,null,xp,true))
						msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,"^HYou have discovered '"+A.name()+"', you gain "+xp+" experience.^?",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
				}
				else
				{
					int pctBefore=mob.playerStats().percentVisited((MOB)host,A);
					if(mob.playerStats().addRoomVisit(R))
						CMLib.players().bumpPrideStat(mob,AccountStats.PrideStat.ROOMS_EXPLORED,1);
					int pctAfter=mob.playerStats().percentVisited((MOB)host,A);
					if((pctBefore<50)&&(pctAfter>=50))
					{
						int xp=(int)Math.round(50.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],hostP.phyStats().level()));
						if(xp>125) xp=125;
						if((xp>0)&&CMLib.leveler().postExperience((MOB)host,null,null,xp,true))
							msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,"^HYou have familiarized yourself with '"+A.name()+"', you gain "+xp+" experience.^?",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					}
					else
					if((pctBefore<90)&&(pctAfter>=90))
					{
						int xp=(int)Math.round(100.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],hostP.phyStats().level()));
						if(xp>250) xp=250;
						if((xp>0)&&CMLib.leveler().postExperience((MOB)host,null,null,xp,true))
							msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,"^HYou have explored '"+A.name()+"', you gain "+xp+" experience.^?",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					}
				}

			}
		}
	}

	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
															 mob.charStats().getClassLevel(ID()),
															 false,
															 false);
			for(AbilityMapper.AbilityMapping able : V)
			{
				Ability A=CMClass.getAbility(able.abilityID);
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if(E instanceof MusicalInstrument)
			return true;
		return super.weaponCheck(mob,sourceCode,E);
	}
	
	
	private final String[] raceRequiredList=new String[]{
		"Human","Humanoid","Elf","Dwarf","Halfling","Elf-kin"
	};
	public String[] getRequiredRaceList(){ return raceRequiredList; }

	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Charisma",Integer.valueOf(9))
	};
	public Pair<String,Integer>[] getMinimumStatRequirements() { return minimumStatRequirements; }
	
	public String getOtherLimitsDesc(){return "";}
	public String getOtherBonusDesc(){return "Receives group bonus combat experience when in an intelligent group, and more for a group with players.  Receives exploration and pub-finding experience based on danger level.";}
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.add(w);
		}
		return outfitChoices;
	}
}
