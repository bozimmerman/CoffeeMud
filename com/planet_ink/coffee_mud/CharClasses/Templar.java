package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Templar extends Cleric
{
	public String ID(){return "Templar";}
	public String name(){return "Templar";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
		
	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}
	
	private int tickDown=0;
	
	public Templar()
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
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			CMAble.addCharAbilityMapping(ID(),3,"Specialization_EdgedWeapon",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Specialization_FlailedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);
			
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Parry",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			
			CMAble.addCharAbilityMapping(ID(),8,"Specialization_Polearm",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Skill_AttackHalf",true);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelGood",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Specialization_Ranged",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Specialization_Hammer",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			
			CMAble.addCharAbilityMapping(ID(),14,"Specialization_Axe",true);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Bash",true);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Hide",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Specialization_Natural",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",true);
			
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
			
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Sneak",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Thief_BackStab",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			
			CMAble.addCharAbilityMapping(ID(),25,"Skill_Attack3",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Prayer_Avatar",true);
		}
	}
	
	public boolean playerSelectable()
	{
		return true;
	}

	public void tick(MOB myChar, int tickID)
	{
		if((tickID==Host.MOB_TICK)&&((--tickDown)<=0))
		{
			tickDown=5;
			if(myChar.fetchAffect("Prayer_AuraStrife")==null)
			{
				Ability A=CMClass.getAbility("Prayer_AuraStrife");
				A.invoke(myChar,myChar,true);
			}
		}
		return;
	}
	
	public String statQualifications(){return "Wisdom 9+ Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Templar.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Templar.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives Aura of Strife which increases in power.";}
	public String otherLimitations(){return "Always fumbles good prayers.  Using non-evil prayers introduces failure chance.";}
	public String weaponLimitations(){return "";}

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
					
				int basis=0;
				if(hq==1000)
				{
					myChar.tell("The good nature of "+A.name()+" disrupts your prayer.");
					return false;
				}
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
					myChar.tell("The evil nature of "+A.name()+" disrupts your prayer.");
				else
				if(hq==1000)
					myChar.tell("The goodness of "+A.name()+" disrupts your prayer.");
				else
				if(align>650)
					myChar.tell("The anti-good nature of "+A.name()+" disrupts your thought.");
				else
				if(align<350)
					myChar.tell("The anti-evil nature of "+A.name()+" disrupts your thought.");
				return false;
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
