package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Artisan extends StdCharClass
{
	public String ID(){return "Artisan";}
	public String name(){return "Artisan";}
	public String baseClass(){return "Commoner";}
	public int getMaxHitPointsLevel(){return 8;}
	public int getBonusPracLevel(){return 2;}
	public int getBonusManaLevel(){return 12;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 15;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	protected static int[] allowedWeapons={
				Weapon.CLASS_NATURAL,
				Weapon.CLASS_DAGGER};
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public boolean playerSelectable(){	return true;}


	public Artisan()
	{
		super();
		maxStatAdj[CharStats.WISDOM]=6;
		maxStatAdj[CharStats.INTELLIGENCE]=6;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",true);

			CMAble.addCharAbilityMapping(ID(),1,"ClanCrafting",false);

			CMAble.addCharAbilityMapping(ID(),1,"SmokeRings",false);

			CMAble.addCharAbilityMapping(ID(),2,"Butchering",true);
			CMAble.addCharAbilityMapping(ID(),2,"Chopping",true);
			CMAble.addCharAbilityMapping(ID(),2,"Digging",true);
			CMAble.addCharAbilityMapping(ID(),2,"Drilling",true);
			CMAble.addCharAbilityMapping(ID(),2,"Fishing",true);
			CMAble.addCharAbilityMapping(ID(),2,"Foraging",true);
			CMAble.addCharAbilityMapping(ID(),2,"Hunting",true);
			CMAble.addCharAbilityMapping(ID(),2,"Mining",true);

			CMAble.addCharAbilityMapping(ID(),3,"FireBuilding",true);
			CMAble.addCharAbilityMapping(ID(),3,"Searching",true);
			CMAble.addCharAbilityMapping(ID(),3,"Pottery",true);
			CMAble.addCharAbilityMapping(ID(),3,"ScrimShaw",true);

			CMAble.addCharAbilityMapping(ID(),4,"Blacksmithing",true);
			CMAble.addCharAbilityMapping(ID(),4,"Carpentry",true);
			CMAble.addCharAbilityMapping(ID(),4,"LeatherWorking",true);
			CMAble.addCharAbilityMapping(ID(),4,"GlassBlowing",true);
			CMAble.addCharAbilityMapping(ID(),4,"Sculpting",true);
			CMAble.addCharAbilityMapping(ID(),4,"Tailoring",true);
			CMAble.addCharAbilityMapping(ID(),4,"Weaving",true);

			CMAble.addCharAbilityMapping(ID(),5,"CageBuilding",true);
			CMAble.addCharAbilityMapping(ID(),5,"Cooking",true);
			CMAble.addCharAbilityMapping(ID(),5,"JewelMaking",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Warrants",false);

			CMAble.addCharAbilityMapping(ID(),6,"Dyeing",true);
			CMAble.addCharAbilityMapping(ID(),6,"Embroidering",true);
			CMAble.addCharAbilityMapping(ID(),6,"Engraving",true);
			CMAble.addCharAbilityMapping(ID(),6,"Lacquerring",true);
			CMAble.addCharAbilityMapping(ID(),6,"Smelting",true);

			CMAble.addCharAbilityMapping(ID(),7,"Armorsmithing",true);
			CMAble.addCharAbilityMapping(ID(),7,"Fletching",true);
			CMAble.addCharAbilityMapping(ID(),7,"Weaponsmithing",true);

			CMAble.addCharAbilityMapping(ID(),8,"Shipwright",true);
			CMAble.addCharAbilityMapping(ID(),8,"Wainwrighting",true);

			CMAble.addCharAbilityMapping(ID(),9,"PaperMaking",true);

			CMAble.addCharAbilityMapping(ID(),10,"Distilling",true);
			CMAble.addCharAbilityMapping(ID(),10,"Farming",true);
			CMAble.addCharAbilityMapping(ID(),10,"Skill_WandUse",false);

			CMAble.addCharAbilityMapping(ID(),11,"Speculate",true);

			CMAble.addCharAbilityMapping(ID(),13,"Painting",true);
			CMAble.addCharAbilityMapping(ID(),13,"Construction",true);
			CMAble.addCharAbilityMapping(ID(),13,"Masonry",true);

			CMAble.addCharAbilityMapping(ID(),15,"LockSmith",true);

			CMAble.addCharAbilityMapping(ID(),17,"Thief_Appraise",false);

			CMAble.addCharAbilityMapping(ID(),19,"Thief_Haggle",false);

			CMAble.addCharAbilityMapping(ID(),20,"Taxidermy",true);
			CMAble.addCharAbilityMapping(ID(),20,"Merchant",true);

			CMAble.addCharAbilityMapping(ID(),22,"Skill_Cage",false);

			CMAble.addCharAbilityMapping(ID(),23,"Skill_Stability",false);

			CMAble.addCharAbilityMapping(ID(),25,"Scrapping",true);

			CMAble.addCharAbilityMapping(ID(),30,"Thief_Lore",false);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			int exp=0;
			for(int a=0;a<mob.numEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)
				&&(!A.isAutoInvoked())
				&&(mob.isMine(A))
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
					exp++;
			}
			if(exp>0)
				MUDFight.postExperience(mob,null,mob.getLiegeID(),exp,true);
		}
		return super.tick(ticking,tickID);
	}

	public String statQualifications(){return "Wisdom 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Artisan.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Artisan.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Dagger");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public String weaponLimitations(){return "To avoid fumble chance, must use natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear cloth, or vegetation based armor to avoid skill failure.";}

	protected boolean isAllowedWeapon(int wclass){
		for(int i=0;i<allowedWeapons.length;i++)
			if(wclass==allowedWeapons[i]) return true;
		return false;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			boolean spellLike=((msg.tool()!=null)
							   &&((CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0))
							   &&(myChar.isMine(msg.tool()))
							   &&(!msg.tool().ID().equals("Skill_Recall")));
			if((spellLike||((msg.sourceMajor()&CMMsg.MASK_DELICATE)>0))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.INTELLIGENCE)*2))
				{
					String name="in <S-HIS-HER> maneuver";
					if(spellLike)
						name=msg.tool().name().toLowerCase();
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) "+name+"!");
					return false;
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{
				int classification=((Weapon)msg.tool()).weaponClassification();
				if(!isAllowedWeapon(classification))
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.WISDOM)*2))
					{
						myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
						return false;
					}
			}
		}
		return super.okMessage(myChar,msg);
	}
	public String otherBonuses(){return "Gains experience when using common skills.";}
}