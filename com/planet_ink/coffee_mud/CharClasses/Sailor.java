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
   Copyright 2016-2016 Bo Zimmerman

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
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return  0;//Area.THEME_FANTASY; //
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
		
		/*hide on a ship to get transport between two port cities.  If the player has charting, they can 
		 * predetermine what port they end up in...otherwise, takes to a random connected shore room.
		 */
		//CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Stowaway",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Trawling",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Shipwright",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		
		/*Language transmitted via light or sound.  Can be heard for 10 continuous outdoor rooms 
		 * (via light) or 3 continuous indoor rooms (via sound).  Must have light source if outdoors.
		 */
		//CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_MorseCode",false);
		/*The character climbs the mast to the crows nest to view the surrounding rooms.  Character can see 
		 * details from other adjacent rooms based on skill/expertise.  Number of rooms distance, ships, items 
		 * on ships.  Sample commands Crowsnest North, Crowsnest North North, Crowsnest North Ulysses (the 
		 * name of a ship/mob in the room).  Results would be as a look at that location/target.  Level should 
		 * determine range (1 additional room per 20 levels), expertise should determine resolution (just the 
		 * room name/description, exits, items/ships/mobs).  A level 90 sailor with 1 level of expertise would 
		 * be able to see 5 rooms out, but only get name/description of the rooms...except rooms 1 range out he 
		 * could also get exits.  With 2 expertises, he would also get items/ships in the 1 range room.  With 
		 * 3, he would get exits at range 2.  4 would provide items at range 2.  5 would provide exits at range 3.
		 *   7 would provide exits at range 4.
		 */
		//CMLib.ableMapper().addCharAbilityMapping(ID(),6,"CrowsNest",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_AvoidCurrents",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_SeaMapping",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Diving",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_SeaCharting",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_DeadReckoning",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_SeaNavigation",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Bind",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Scuttle",true);
		
		//Manuevering-Enables the ship to perform one additional turn per ship combat round.  Costs 200 movement.
		//CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_SeaManeuvering",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Siegecraft",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Specialization_SiegeWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_WaterTactics",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_DeepBreath",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_CombatRepairs",true);
		
		/*The character recovers some wood and supplies from a recently sunk vessel (probably needs to be used 
		 * while a ship is sinking in the region.  Generates wood and money that go straight to the characters 
		 * inventory.  Alternatively, maybe the gold goes to the players inventory (autogold), and the wood just 
		 * floats and must be harvested separately.
		 */
		//CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Salvage",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Skill_FoulWeatherSailing",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Skill_NavalTactics",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_RammingSpeed",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_InterceptShip",true);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_AwaitShip",true);
		
		//The sailor hires a mob 5-10 levels lower than themselves to join his crew and work on his boat.
		//Repair guy, Sailor, Defensive, Trawler
		//CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Skill_HireCrewmember",true);
		

		// to separate from artisan --------------------------------------------------------------------------------------------
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Chopping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Digging",0,"",false,true);
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Carpentry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Drilling",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fishing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Foraging",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Herbology",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Cobbling",0,"",false,true,CMParms.parseSemicolons("LeatherWorking",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Hunting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Mining",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Pottery",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"ScrimShaw",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"LeatherWorking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"GlassBlowing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Sculpting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Tailoring",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaving",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"CageBuilding",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"JewelMaking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Dyeing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Embroidering",0,"",false,true,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Engraving",0,"",false,true,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Lacquerring",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Smelting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Armorsmithing",0,"",false,true,CMParms.parseSemicolons("Blacksmithing",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fletching",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaponsmithing",0,"",false,true,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true),"");
		//CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shipwright",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Wainwrighting",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"PaperMaking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Distilling",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Farming",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Speculate",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Painting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Construction",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Masonry",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Excavation",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Irrigation",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Landscaping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Taxidermy",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Merchant",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Scrapping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Costuming",0,"",false,true);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
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
						exp++;
				}
				if(exp>0)
					CMLib.leveler().postExperience(mob,null,null,exp,true);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.value()>0)
		&&(msg.source().charStats().getCurrentClass() == this)
		&&(CMLib.map().areaLocation(msg.source()) instanceof BoardableShip)
		&&(msg.source() != msg.target()))
		{
			if(msg.target() instanceof MOB)
				msg.setValue(msg.value() * 2);
			else
			if(msg.target() == null)
			{
				BoardableShip shipArea = (BoardableShip)CMLib.map().areaLocation(msg.source());
				Room R=CMLib.map().roomLocation(shipArea.getShipItem());
				if(R!=null)
				{
					for(Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						Item I=i.nextElement();
						if((I instanceof BoardableShip)
						&&(I.fetchEffect("Sinking")!=null)
						&&(I!=shipArea.getShipItem()))
						{
							msg.setValue(msg.value() * 2);
							break;
						}
					}
				}
			}
		}
	}
	
	private final String[] raceRequiredList=new String[]{"All","-Equine"};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}


	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(5)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(5))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Whip");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Gains experience when using certain skills.  Screams of flayed, amputated, tattooed, body pierced, or chirguried victims grants xp/hr.");
	}
}
