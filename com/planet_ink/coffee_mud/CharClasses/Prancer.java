package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Prancer extends StdCharClass
{
	public String ID(){return "Prancer";}
	public String name(){return "Prancer";}
	public String baseClass(){return "Bard";}
	public int getMaxHitPointsLevel(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 4;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	public Prancer()
	{
		super();
		maxStatAdj[CharStats.CHARISMA]=4;
		maxStatAdj[CharStats.STRENGTH]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Dance_Stop",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Dance_CanCan",true);

			CMAble.addCharAbilityMapping(ID(),2,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),2,"Dance_Foxtrot",true);

			CMAble.addCharAbilityMapping(ID(),3,"Fighter_Kick",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),3,"Dance_Tarantella",true);

			CMAble.addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
			CMAble.addCharAbilityMapping(ID(),4,"Dance_Waltz",true);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),5,"Dance_Salsa",true);
			CMAble.addCharAbilityMapping(ID(),5,"Dance_Grass",true);

			CMAble.addCharAbilityMapping(ID(),6,"Dance_Clog",true);

			CMAble.addCharAbilityMapping(ID(),7,"Thief_Distract",false);
			CMAble.addCharAbilityMapping(ID(),7,"Dance_Capoeira",true);

			CMAble.addCharAbilityMapping(ID(),8,"Dance_Tap",true);
			CMAble.addCharAbilityMapping(ID(),8,"Dance_Swing",true);

			CMAble.addCharAbilityMapping(ID(),9,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),9,"Dance_Basse",true);

			CMAble.addCharAbilityMapping(ID(),10,"Fighter_BodyFlip",true);
			CMAble.addCharAbilityMapping(ID(),10,"Dance_Tango",true);

			CMAble.addCharAbilityMapping(ID(),11,"Fighter_Spring",false);
			CMAble.addCharAbilityMapping(ID(),11,"Dance_Polka",true);

			CMAble.addCharAbilityMapping(ID(),12,"Dance_RagsSharqi",true);
			CMAble.addCharAbilityMapping(ID(),12,"Dance_Manipuri",true);

			CMAble.addCharAbilityMapping(ID(),13,"Skill_Trip",false);
			CMAble.addCharAbilityMapping(ID(),13,"Dance_Cotillon",true);

			CMAble.addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),14,"Dance_Ballet",true);

			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Tumble",false);
			CMAble.addCharAbilityMapping(ID(),15,"Dance_Jitterbug",true);

			CMAble.addCharAbilityMapping(ID(),16,"Dance_Butoh",true);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),17,"Dance_Courante",true);

			CMAble.addCharAbilityMapping(ID(),18,"Dance_Musette",true);

			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Endurance",true);
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Cartwheel",false);
			CMAble.addCharAbilityMapping(ID(),19,"Dance_Swords",true);

			CMAble.addCharAbilityMapping(ID(),20,"Dance_Flamenco",true);

			CMAble.addCharAbilityMapping(ID(),21,"Fighter_Roll",false);
			CMAble.addCharAbilityMapping(ID(),21,"Dance_Jingledress",true);

			CMAble.addCharAbilityMapping(ID(),22,"Dance_Morris",true);

			CMAble.addCharAbilityMapping(ID(),23,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),23,"Dance_Butterfly",true);

			CMAble.addCharAbilityMapping(ID(),24,"Dance_Macabre",true);

			CMAble.addCharAbilityMapping(ID(),25,"Fighter_CircleTrip",false);
			CMAble.addCharAbilityMapping(ID(),25,"Dance_War",true);

			CMAble.addCharAbilityMapping(ID(),30,"Dance_Square",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public int getMovementMultiplier(){return 18;}

	public String statQualifications(){return "Charisma 9+, Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.CHARISMA) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Prancer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH) <= 8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Prancer.");
			return false;
		}
		if((!(mob.charStats().getMyRace().ID().equals("Human")))
		&&(!(mob.charStats().getMyRace().ID().equals("Elf")))
		&&(!(mob.charStats().getMyRace().ID().equals("Halfling")))
		&&(!(mob.charStats().getMyRace().ID().equals("HalfElf"))))
		{
			if(!quiet)
				mob.tell("You must be Human, Elf, Halfling, or Half Elf to be a Prancer");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
	}
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear cloth or vegetation armor to avoid skill failure.";}
	public String otherLimitations(){return "";}
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
	

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			if((!Sense.isSleeping(affected))&&(!Sense.isSitting(affected)))
			{
				MOB mob=(MOB)affected;
				int attArmor=(((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1)*(mob.charStats().getClassLevel(this)-1);
				affectableStats.setArmor(affectableStats.armor()-attArmor);
			}
		}
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);

		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		attArmor=attArmor*-1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			boolean spellLike=((msg.tool()!=null)
							   &&((CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0))
							   &&(myChar.isMine(msg.tool())));
			if((spellLike||((msg.sourceMajor()&CMMsg.MASK_DELICATE)>0))
			&&(msg.tool()!=null)
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> mess up <S-HIS-HER> "+msg.tool().name()+"!");
					return false;
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{
				int classification=((Weapon)msg.tool()).weaponClassification();
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
						myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
						return false;
					}
					break;
				}
			}
		}
		return true;
	}

	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level.";}

	public void level(MOB mob)
	{
		super.level(mob);
		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		mob.tell("^NYour grace grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
}

