package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Missionary extends Cleric
{
	public String ID(){return "Missionary";}
	public String name(){return "Missionary";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}

	public Missionary()
	{
		maxStat[CharStats.WISDOM]=22;
		maxStat[CharStats.DEXTERITY]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_DivineLuck",true);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",true);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Bury",true);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",true);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CreateWater",true);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_ElectricStrike",true);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_AiryForm",true);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",true);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_SenseHidden",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",true);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_HolyWind",true);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_Wings",true);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Etherealness",true);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",true);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",true);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_ChainStrike",true);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_Monolith",0,"AIR",true);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Gateway",true);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",true);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_Weather",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"AIR",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Sermon",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_ElectricHealing",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	private static final int[] allSaves={
		CharStats.SAVE_ACID,
		CharStats.SAVE_COLD,
		CharStats.SAVE_DISEASE,
		CharStats.SAVE_ELECTRIC,
		CharStats.SAVE_FIRE,
		CharStats.SAVE_GAS,
		CharStats.SAVE_GENERAL,
		CharStats.SAVE_JUSTICE,
		CharStats.SAVE_MAGIC,
		CharStats.SAVE_MIND,
		CharStats.SAVE_PARALYSIS,
		CharStats.SAVE_POISON,
		CharStats.SAVE_UNDEAD,
		CharStats.SAVE_WATER,
		CharStats.SAVE_TRAPS};

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		for(int i=0;i<allSaves.length;i++)
			affectableStats.setStat(allSaves[i],
				affectableStats.getStat(allSaves[i])
					+(affectableStats.getClassLevel(this)));
	}

	public void tick(MOB myChar, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
		}
		return;
	}

	public String statQualifications(){return "Wisdom 9+ Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Missionary.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Missionary.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Never fumbles neutral prayers, and receives 1pt/level luck bonus to all saving throws per level.  Receives 1pt/level electricity damage reduction.";}
	public String otherLimitations(){return "Using non-neutral prayers introduces failure chance.  Vulnerable to acid attacks.";}
	public String weaponLimitations(){return "May use Blunt, Ranged weapons, and Swords only.";}

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
				if(hq==0)
					basis=align/10;
				else
				if(hq==1000)
					basis=(1000-align)/10;
				else
					return true;

				if(Dice.rollPercentage()>basis)
					return true;

				if(hq==0)
					myChar.tell("The evil nature of "+A.name()+" disrupts your prayer.");
				else
				if(hq==1000)
					myChar.tell("The goodness of "+A.name()+" disrupts your prayer.");
				return false;
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{

				if((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_BLUNT)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_SWORD))
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
			&&(affect.sourceMinor()==Affect.TYP_ELECTRIC))
			{
				int recovery=affect.targetCode()-Affect.MASK_HURT;
				recovery=recovery-myChar.charStats().getClassLevel(this);
				if(recovery<0) recovery=1;
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(affect.sourceMinor()==Affect.TYP_ACID))
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