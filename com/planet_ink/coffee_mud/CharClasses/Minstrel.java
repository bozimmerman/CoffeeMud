package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Minstrel extends StdCharClass
{
	public String ID(){return "Minstrel";}
	public String name(){return "Minstrel";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_NONMETAL;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

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
	
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear non-metal armor to avoid skill failure.";}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "";}
	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(mob.freeWearPositions(Item.WIELD)>0)
				w.wearAt(Item.WIELD);
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			boolean spellLike=((affect.tool()!=null)&&(myChar.fetchAbility(affect.tool().ID())!=null))&&(myChar.isMine(affect.tool()));
			if((spellLike||((affect.sourceMajor()&Affect.MASK_DELICATE)>0))
			&&(affect.tool()!=null)
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> mess up <S-HIS-HER> "+affect.tool().name()+"!");
					return false;
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				int classification=((Weapon)affect.tool()).weaponClassification();
				switch(classification)
				{
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_DAGGER:
					break;
				default:
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
						return false;
					}
					break;
				}
			}
		}
		return true;
	}
}