package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Doomsayer extends Cleric
{
	public String ID(){return "Doomsayer";}
	public String name(){return "Doomsayer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
		
	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}
	
	private int tickDown=0;
	
	public Doomsayer()
	{
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.WISDOM]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Ember",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_CurseFlames",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtFire",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);
			
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_FlameWeapon",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_CurseMetal",true);
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_Intimidate",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_CurseMind",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_Demonshield",true);
			
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",true);
			
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
			
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_DemonicConsumption",true);
			
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_CurseMinds",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"FIRE",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_FireHealing",true);
		}
	}
	
	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Wisdom 9+ Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Doomsayer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Doomsayer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives 1 pt damage reduction/level from fire attacks.";}
	public String otherLimitations(){return "Always fumbles good prayers, and fumbles all prayers when alignment is above 500.  Qualifies and receives evil prayers.  Using non-aligned prayers introduces failure chance.  Vulnerable to cold attacks.";}
	public String weaponLimitations(){return "May only use sword, axe, polearm, and some edged weapons.";}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
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
					
				int basis=0;
				if(hq==1000)
				{
					myChar.tell("The good nature of "+A.displayName()+" disrupts your prayer.");
					return false;
				}
				else
				if(myChar.getAlignment()>500)
					basis=100;
				else
				if(hq==0)
					basis=align/10;
				else
				{
					basis=(500-align)/10;
					if(basis<0) basis=basis*-1;
					basis-=10;
				}
		
				if(Dice.rollPercentage()>basis)
					return true;

				if(hq==0)
					myChar.tell("The evil nature of "+A.displayName()+" disrupts your prayer.");
				else
				if(hq==1000)
					myChar.tell("The goodness of "+A.displayName()+" disrupts your prayer.");
				else
				if(align>650)
					myChar.tell("The anti-good nature of "+A.displayName()+" disrupts your thought.");
				else
				if(align<350)
					myChar.tell("The anti-evil nature of "+A.displayName()+" disrupts your thought.");
				return false;
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				
				if((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_EDGED)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_POLEARM)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_AXE)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_SWORD))
					return true;
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().displayName()+".");
					return false;
				}
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(affect.sourceMinor()==Affect.TYP_FIRE))
			{
				int recovery=affect.targetCode()-Affect.MASK_HURT;
				recovery=recovery-myChar.charStats().getClassLevel(this);
				if(recovery<0) recovery=1;
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(affect.sourceMinor()==Affect.TYP_COLD))
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
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
}
