package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Shaman extends Cleric
{
	public String ID(){return "Shaman";}
	public String name(){return "Shaman";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
		
	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}
	
	public Shaman()
	{
		maxStat[CharStats.WISDOM]=22;
		maxStat[CharStats.CONSTITUTION]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CauseLight",false);
			
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_FortifyFood",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);
			
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_EarthMud",true);
			
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);
			
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_Earthshield",true);
			
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",true);
			
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",true);
			
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Sober",true);
			
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
			
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_Fertilize",true);
			
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_Rockskin",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Tremor",true);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",false);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);
			
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",true);
			
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_RockFlesh",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_FleshRock",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_DrunkenStupor",true);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",true);
			
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",true);
			CMAble.addCharAbilityMapping(ID(),23,"Skill_Meditation",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);
			
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"EARTH",true);
						
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_AcidHealing",true);
		}
	}
	
	public boolean playerSelectable()
	{
		return true;
	}

	public void tick(MOB myChar, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
		}
		return;
	}
	
	public String statQualifications(){return "Wisdom 9+ Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Shaman.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Shaman.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Never fumbles neutral prayers, receives smallest prayer fumble chance, and receives 1pt/level of acid damage reduction.";}
	public String otherLimitations(){return "Using non-neutral prayers introduces small failure chance.  Vulnerable to electric attacks.";}
	public String weaponLimitations(){return "May use Blunt and Hammer-like weapons only.";}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(myChar.isMine(affect.tool()))
			&&((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
			{
				int align=myChar.getAlignment();
				Ability A=(Ability)affect.tool();
		
				if(A.appropriateToMyAlignment(align))	
					return true;
				int hq=holyQuality(A);
					
				if((hq==500)||((align>=150)&&(align<=850)))
					return true;
				
				int basis=(500-align)/20;
				if(basis<0) basis=basis*-1;
				basis-=10;
		
				if(Dice.rollPercentage()>basis)
					return true;

				if(align>650)
					myChar.tell("The anti-good nature of "+A.name()+" disrupts your thought.");
				else
				if(align<350)
					myChar.tell("The anti-evil nature of "+A.name()+" disrupts your thought.");
				return false;
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				
				if((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_BLUNT)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_HAMMER))
					return true;
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"A conflict of <S-HIS-HER> conscience makes <S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
					return false;
				}
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(affect.sourceMinor()==Affect.TYP_ACID))
			{
				int recovery=affect.targetCode()-Affect.MASK_HURT;
				recovery=recovery-myChar.charStats().getClassLevel(this);
				if(recovery<0) recovery=1;
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(affect.sourceMinor()==Affect.TYP_ELECTRIC))
			{
				int recovery=affect.targetCode()-Affect.MASK_HURT;
				recovery=recovery*2;
				if(recovery>1000) recovery=1000;
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
		}
		return true;
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
}