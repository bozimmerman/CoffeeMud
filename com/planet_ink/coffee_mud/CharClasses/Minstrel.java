package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Minstrel extends StdCharClass
{
	public String ID(){return "Minstrel";}
	public String name(){return "Minstrel";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 6;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 4;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	protected String armorFailMessage(){return "<S-NAME> armor make(s) <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Minstrel()
	{
		super();
		maxStatAdj[CharStats.CHARISMA]=4;
		maxStatAdj[CharStats.INTELLIGENCE]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),1,"InstrumentMaking",false);

			CMAble.addCharAbilityMapping(ID(),1,"Song_Nothing",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Play_Tempo",true);
			CMAble.addCharAbilityMapping(ID(),1,"Play_Break",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Play_Woods",false);

			CMAble.addCharAbilityMapping(ID(),2,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),2,"Play_Flutes",false);
			CMAble.addCharAbilityMapping(ID(),2,"Play_Rhythm",true);

			CMAble.addCharAbilityMapping(ID(),3,"Play_Drums",false);
			CMAble.addCharAbilityMapping(ID(),3,"Play_March",true);

			CMAble.addCharAbilityMapping(ID(),4,"Ranger_FindWater",false);
			CMAble.addCharAbilityMapping(ID(),4,"Play_Harps",false);
			CMAble.addCharAbilityMapping(ID(),4,"Play_Background",true);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_TuneInstrument",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),5,"Play_Cymbals",false);
			CMAble.addCharAbilityMapping(ID(),5,"Play_Melody",true);

			CMAble.addCharAbilityMapping(ID(),6,"Thief_TrophyCount",false);
			CMAble.addCharAbilityMapping(ID(),6,"Play_Guitars",false);
			CMAble.addCharAbilityMapping(ID(),6,"Play_LoveSong",true);
			CMAble.addCharAbilityMapping(ID(),6,"Song_Armor",false);

			CMAble.addCharAbilityMapping(ID(),7,"Play_Clarinets",false);
			CMAble.addCharAbilityMapping(ID(),7,"Play_Carol",true);

			CMAble.addCharAbilityMapping(ID(),8,"Fighter_Rescue",false);
			CMAble.addCharAbilityMapping(ID(),8,"Play_Violins",false);
			CMAble.addCharAbilityMapping(ID(),8,"Play_Blues",true);

			CMAble.addCharAbilityMapping(ID(),9,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),9,"Song_Serenity",false);
			CMAble.addCharAbilityMapping(ID(),9,"Play_Oboes",false);
			CMAble.addCharAbilityMapping(ID(),9,"Play_Ballad",true);

			CMAble.addCharAbilityMapping(ID(),10,"Skill_InstrumentBash",true);
			CMAble.addCharAbilityMapping(ID(),10,"Play_Horns",false);
			CMAble.addCharAbilityMapping(ID(),10,"Play_Retreat",true);

			CMAble.addCharAbilityMapping(ID(),11,"Play_Charge",true);

			CMAble.addCharAbilityMapping(ID(),12,"Thief_Listen",true);
			CMAble.addCharAbilityMapping(ID(),12,"Play_Xylophones",false);
			CMAble.addCharAbilityMapping(ID(),12,"Play_Reveille",true);

			CMAble.addCharAbilityMapping(ID(),13,"Play_Symphony",true);

			CMAble.addCharAbilityMapping(ID(),14,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),14,"Play_Trumpets",false);
			CMAble.addCharAbilityMapping(ID(),14,"Play_Dirge",true);

			CMAble.addCharAbilityMapping(ID(),15,"Play_Ditty",true);

			CMAble.addCharAbilityMapping(ID(),16,"Play_Pianos",false);
			CMAble.addCharAbilityMapping(ID(),16,"Play_Solo",true);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),17,"Song_Quickness",false);

			CMAble.addCharAbilityMapping(ID(),18,"Skill_EscapeBonds",true);
			CMAble.addCharAbilityMapping(ID(),18,"Play_Harmonicas",false);
			CMAble.addCharAbilityMapping(ID(),18,"Play_Lullabies",true);

			CMAble.addCharAbilityMapping(ID(),19,"Song_Thanks",false);

			CMAble.addCharAbilityMapping(ID(),20,"Skill_Feint",true);
			CMAble.addCharAbilityMapping(ID(),20,"Play_Tubas",false);
			CMAble.addCharAbilityMapping(ID(),20,"Play_Accompaniment",true);

			CMAble.addCharAbilityMapping(ID(),21,"Play_Spiritual",false);

			CMAble.addCharAbilityMapping(ID(),22,"Paladin_Defend",false);
			CMAble.addCharAbilityMapping(ID(),22,"Play_Organs",false);
			CMAble.addCharAbilityMapping(ID(),22,"Play_Tribal",false);

			CMAble.addCharAbilityMapping(ID(),23,"Play_Harmony",true);

			CMAble.addCharAbilityMapping(ID(),24,"Play_Trombones",false);
			CMAble.addCharAbilityMapping(ID(),24,"Play_Mystical",false);

			CMAble.addCharAbilityMapping(ID(),25,"Play_Battlehymn",true);

			CMAble.addCharAbilityMapping(ID(),30,"Skill_Conduct",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.isMonster())
		{
			Vector V=CMAble.getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(Enumeration a=V.elements();a.hasMoreElements();)
			{
				Ability A=CMClass.getAbility((String)a.nextElement());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SONG)
				&&(!CMAble.getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if(E instanceof MusicalInstrument)
			return true;
		return super.weaponCheck(mob,sourceCode,E);
	}
	public String statQualifications(){return "Charisma 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Minstrel.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Minstrel.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,
			affectableStats.getStat(CharStats.SAVE_POISON)
			+(affectableStats.getClassLevel(this)*2));
	}

	public String otherLimitations(){return "";}
	public String otherBonuses(){return "";}
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
}
