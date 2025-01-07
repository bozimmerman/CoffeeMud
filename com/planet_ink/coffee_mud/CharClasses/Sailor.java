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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2024 Bo Zimmerman

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
public class Sailor extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Sailor";
	}

	private final static String localizedStaticName = CMLib.lang().L("Sailor");

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
		return CharStats.STAT_DEXTERITY;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 15;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/6)+(1*(1?5))";
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
		return CharClass.WEAPONS_ANY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(final MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	public Hashtable<String, int[]> mudHourMOBXPMap = new Hashtable<String, int[]>();

	public Sailor()
	{
		super();
		maxStatAdj[CharStats.STAT_CONSTITUTION]=6;
		maxStatAdj[CharStats.STAT_DEXTERITY]=6;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Unbinding",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Fishing",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Semaphore",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chopping",0,"",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Carpentry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Skill_SeaLegs",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Belay",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Stowaway",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_AbandonShip",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Trawling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Boatwright",0,"",true,SecretFlag.PUBLIC,CMParms.parseSemicolons("Carpentry",true),"");

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_MorseCode",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_FindShip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_CrowsNest",0,"",true,SecretFlag.PUBLIC,CMParms.parseSemicolons("Skill_Climb(74)", true),"");

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_AvoidCurrents",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_TieDown",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_SeaMapping",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Diving",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_ShipLore",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Shipwright",0,"",false,SecretFlag.PUBLIC,CMParms.parseSemicolons("Boatwright",true),"");

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_SeaCharting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Baiting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Breakout",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_DeadReckoning",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"FishLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_SeaNavigation",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"CargoLoading",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Bind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Scuttle",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_SeaManeuvers",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Siegecraft",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Specialization_SiegeWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_WaterTactics",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_DeepBreath",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_HardToPort",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_CombatRepairs",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Salvaging",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"MasterFishing",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_FoulWeatherSailing",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_HardToStern",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Skill_NavalTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_RammingSpeed",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_InterceptShip",0,"",true,SecretFlag.PUBLIC,CMParms.parseSemicolons("Skill_SeaCharting(50)", true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_HardToStern",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"MasterTrawling",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_AwaitShip",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Skill_HireCrewmember",true);

		// to separate from artisan --------------------------------------------------------------------------------------------
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Chopping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Digging",0,"",false,SecretFlag.SECRET);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Carpentry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Drilling",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Blacksmithing",0,"",false,SecretFlag.SECRET);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fishing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Foraging",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Herbology",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Cobbling",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Hunting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Mining",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Pottery",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shearing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"LockSmith",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Baking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"FoodPrep",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"ScrimShaw",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"LeatherWorking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"GlassBlowing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Sculpting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Tailoring",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaving",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"CageBuilding",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"JewelMaking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Dyeing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Embroidering",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Engraving",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Lacquerring",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Smelting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Armorsmithing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fletching",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaponsmithing",0,"",false,SecretFlag.SECRET);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Boatwright",0,"",false,SecretFlag.SECRET);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shipwright",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Wainwrighting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"PaperMaking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Distilling",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Farming",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shrooming",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Speculate",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Painting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Construction",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Masonry",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Excavation",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Irrigation",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Landscaping",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Taxidermy",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Merchant",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Scrapping",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Costuming",0,"",false,SecretFlag.SECRET);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(mob.charStats().getCurrentClass().ID().equals(ID()))
			{
				int exp=0;
				for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(!A.isAutoInvoked())
					&&(mob.isMine(A))
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
					{
						exp++;
						if((A.ID().equalsIgnoreCase("Trawling"))//||(A.ID().equalsIgnoreCase("Fishing"))))
						||(A.ID().equalsIgnoreCase("MasterTrawling")))//||(A.ID().equalsIgnoreCase("MasterFishing"))))
							exp++;
					}
				}
				if(exp>0)
					CMLib.leveler().postExperience(mob,"CLASS:"+ID(),null,null,exp, true);
			}
		}
		return super.tick(ticking,tickID);
	}

	protected void giveExploreXP(final MOB mob, final Room R, final int amt, final CMMsg msg)
	{
		if(((R.roomID().length()>0)
			||((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0)))
		&&(!CMath.bset(R.getArea().flags(),Area.FLAG_INSTANCE_CHILD))
		&&(!mob.playerStats().hasVisited(R))
		&&(mob.soulMate()==null)
		&&(mob.charStats().getCurrentClass() == this)
		&&(CMLib.flags().isWaterySurfaceRoom(R))
		)
		{
			final int pctBefore=mob.playerStats().percentVisited(mob,R.getArea());
			if(mob.playerStats().addRoomVisit(R))
			{
				final Area A=R.getArea();
				CMLib.players().bumpPrideStat(mob,PrideStats.PrideStat.ROOMS_EXPLORED,1);
				if(mob.playerStats().hasVisited(R))
				{
					CMLib.leveler().postExperience(mob, "CLASS:"+ID(), null, null, amt, false);
					final double totalCountableRooms=A.getIStat(Area.Stats.COUNTABLE_ROOMS);
					if((totalCountableRooms > 0)
					&&(CMath.div(A.getIStat(Area.Stats.WATER_ROOMS), totalCountableRooms)>.80))
					{
						final int pctAfter=mob.playerStats().percentVisited(mob,A);
						if((pctBefore<50)&&(pctAfter>=50))
						{
							int xp=(int)Math.round(50.0*CMath.div(A.getIStat(Area.Stats.AVG_LEVEL),mob.phyStats().level()));
							if(xp>125)
								xp=125;
							if(xp<50)
								xp=50;
							if((xp>0)
							&&((xp=CMLib.leveler().postExperience(mob,"CLASS:"+ID(),null,null,xp, true))>0))
								msg.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have familiarized yourself with '@x1', you gain @x2 experience.^?",A.name(),""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
						}
						else
						if((pctBefore<90)&&(pctAfter>=90))
						{
							int xp=(int)Math.round(100.0*CMath.div(A.getIStat(Area.Stats.AVG_LEVEL),mob.phyStats().level()));
							if(xp>250)
								xp=250;
							if(xp<125)
								xp=125;
							if((xp>0)
							&&((xp=CMLib.leveler().postExperience(mob,"CLASS:"+ID(),null,null,xp, true))>0))
								msg.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_VISUAL,CMLib.lang().L("^HYou have explored '@x1', you gain @x2 experience.^?",A.name(),""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
						}
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);

		if((msg.target() instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER))
		{
			if((msg.source()==myHost)
			&&(msg.source().riding() !=null)
			&&(!msg.source().isMonster())
			&&(msg.source().playerStats()!=null)
			&&(msg.source().riding().rideBasis() == Rideable.Basis.WATER_BASED))
				giveExploreXP(msg.source(), (Room)msg.target(), 5, msg);
			if((msg.source().riding() instanceof NavigableItem)
			&&(((NavigableItem)msg.source().riding()).navBasis() == Rideable.Basis.WATER_BASED)
			&&(msg.source().Name().equals(msg.source().riding().Name()))
			&&(myHost instanceof MOB)
			&&(((MOB)myHost).playerStats()!=null)
			&&(((MOB)myHost).location()!=null)
			&&(((MOB)myHost).location().getArea() == ((Boardable)msg.source().riding()).getArea()))
				giveExploreXP((MOB)myHost, (Room)msg.target(), 10, msg);
		}

		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.value()>0)
		&&(msg.source().charStats().getCurrentClass() == this)
		&&(CMLib.map().areaLocation(msg.source()) instanceof Boardable)
		&&(msg.source() != msg.target()))
		{
			if(msg.target() instanceof MOB)
				msg.setValue(msg.value() * 2);
			else
			if(msg.target() == null)
			{
				final Boardable shipArea = (Boardable)CMLib.map().areaLocation(msg.source());
				final Room R=CMLib.map().roomLocation(shipArea.getBoardableItem());
				if(R!=null)
				{
					for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I instanceof Boardable)
						&&(I.fetchEffect("Sinking")!=null)
						&&(I!=shipArea.getBoardableItem()))
						{
							msg.setValue(msg.value() * 2);
							break;
						}
					}
				}
			}
		}
	}

	private final String[] raceRequiredList=new String[]{"All","-Equine","-Svirfneblin","-Aarakocran","-Faerie"};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Constitution",Integer.valueOf(5)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(5))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector<Item>();
			final Weapon w=CMClass.getWeapon("GenWeapon");
			if(w != null)
			{
				w.setName("a belaying pin");
				w.setDisplayText("a belaying pin lies here.");
				w.setMaterial(RawMaterial.RESOURCE_WOOD);
				w.setWeaponClassification(Weapon.CLASS_BLUNT);
				w.setWeaponDamageType(Weapon.TYPE_BASHING);
				w.setBaseValue(0);
				w.basePhyStats().setDamage(4);
				w.basePhyStats().setWeight(1);
				w.basePhyStats().setAttackAdjustment(0);
				w.recoverPhyStats();
				outfitChoices.add(w);
				cleanOutfit(outfitChoices);
			}
		}
		return outfitChoices;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Sailors earn twice as much XP as other commoners when trawling, sea explore xp, and double experience in ship combat.");
	}
}
