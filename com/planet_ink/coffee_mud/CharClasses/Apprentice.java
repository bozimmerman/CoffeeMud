package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Apprentice extends StdCharClass
{
	public String ID(){return "Apprentice";}
	public String name(){return "Apprentice";}
	public String baseClass(){return "Commoner";}
	public int getMaxHitPointsLevel(){return 5;}
	public int getBonusPracLevel(){return 5;}
	public int getBonusManaLevel(){return 12;}
	public int getBonusAttackLevel(){return -1;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 25;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int getTrainsFirstLevel(){return 6;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	protected static int[] allowedWeapons={
				Weapon.CLASS_NATURAL,
				Weapon.CLASS_DAGGER};
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};


	public Apprentice()
	{
		super();
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",true);
			CMAble.addCharAbilityMapping(ID(),1,"ClanCrafting",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"SmokeRings",false);
			
			CMAble.addCharAbilityMapping(ID(),2,"Butchering",false);
			CMAble.addCharAbilityMapping(ID(),2,"Chopping",false);
			CMAble.addCharAbilityMapping(ID(),2,"Digging",false);
			CMAble.addCharAbilityMapping(ID(),2,"Drilling",false);
			CMAble.addCharAbilityMapping(ID(),2,"Fishing",false);
			CMAble.addCharAbilityMapping(ID(),2,"Foraging",false);
			CMAble.addCharAbilityMapping(ID(),2,"Hunting",false);
			CMAble.addCharAbilityMapping(ID(),2,"Mining",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"FireBuilding",false);
			CMAble.addCharAbilityMapping(ID(),3,"Searching",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Blacksmithing",false);
			CMAble.addCharAbilityMapping(ID(),4,"CageBuilding",false);
			CMAble.addCharAbilityMapping(ID(),4,"Carpentry",false);
			CMAble.addCharAbilityMapping(ID(),4,"Cooking",false);
			CMAble.addCharAbilityMapping(ID(),4,"LeatherWorking",false);
			CMAble.addCharAbilityMapping(ID(),4,"GlassBlowing",false);
			CMAble.addCharAbilityMapping(ID(),4,"Pottery",false);
			CMAble.addCharAbilityMapping(ID(),4,"JewelMaking",false);
			CMAble.addCharAbilityMapping(ID(),4,"ScrimShaw",false);
			CMAble.addCharAbilityMapping(ID(),4,"Sculpting",false);
			CMAble.addCharAbilityMapping(ID(),4,"Tailoring",false);
			CMAble.addCharAbilityMapping(ID(),4,"Weaving",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Dyeing",false);
			CMAble.addCharAbilityMapping(ID(),5,"Embroidering",false);
			CMAble.addCharAbilityMapping(ID(),5,"Engraving",false);
			CMAble.addCharAbilityMapping(ID(),5,"Lacquerring",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Shipwright",false);
			CMAble.addCharAbilityMapping(ID(),6,"Wainwrighting",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"PaperMaking",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Farming",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"LockSmith",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Distilling",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Speculate",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Smelting",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Taxidermy",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Armorsmithing",false);
			CMAble.addCharAbilityMapping(ID(),18,"Fletching",false);
			CMAble.addCharAbilityMapping(ID(),19,"Weaponsmithing",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Merchant",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Construction",false);
			CMAble.addCharAbilityMapping(ID(),22,"Masonry",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Painting",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Scrapping",false);
		}
	}

	public boolean playerSelectable(){	return true;}

	public String statQualifications(){return "Wisdom 5+, Intelligence 5+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=4)
		{
			if(!quiet)
				mob.tell("You need at least a 5 Wisdom to become a Apprentice.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=4)
		{
			if(!quiet)
				mob.tell("You need at least a 5 Intelligence to become a Apprentice.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String weaponLimitations(){return "To avoid fumble chance, must use natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear cloth, or vegetation based armor to avoid skill failure.";}

	protected boolean isAllowedWeapon(int wclass){
		for(int i=0;i<allowedWeapons.length;i++)
			if(wclass==allowedWeapons[i]) return true;
		return false;
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
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)
		&&(msg.source().charStats().getCurrentClass()==this)			  
		&&(!myChar.isMonster()))
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
	public String otherBonuses(){return "";}
}