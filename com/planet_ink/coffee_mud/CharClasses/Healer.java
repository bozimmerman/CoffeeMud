package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Healer extends Cleric
{
	public String ID(){return "Healer";}
	public String name(){return "Healer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
		
	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}
	
	private int fiveDown=5;
	private int tenDown=10;
	private int twentyDown=20;
	
	public Healer()
	{
		maxStat[CharStats.WISDOM]=22;
		maxStat[CharStats.CHARISMA]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_TurnUndead",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
		
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",true);
			
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
			
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);
			
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",true);
			
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",true);
			
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",true);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_GodLight",true);
			
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
			
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
			
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",true);
			
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",true);
			
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true);
			
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
			
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
			
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_DispelUndead",true);
			
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
			
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BlessItem",true);
			
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",true);
			
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_HolyWord",true);
			
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",true);
			
		}
	}
	
	public boolean playerSelectable()
	{
		return true;
	}

	public void tick(MOB myChar, int tickID)
	{
		if((tickID==Host.MOB_TICK)&&(myChar.charStats().getClassLevel(this)>=30))
		{
			if((fiveDown>1)&&(tenDown>1)&&(twentyDown>1)) return;
			
			Hashtable followers=myChar.getGroupMembers(new Hashtable());
			if(myChar.location()!=null)
				for(int i=0;i<myChar.location().numInhabitants();i++)
				{
					MOB M=myChar.location().fetchInhabitant(i);
					if((M!=null)
					&&((M.getVictim()==null)||(followers.get(M.getVictim())==null)))
						followers.put(M,M);
				}
			if((--fiveDown)<=0)
			{
				fiveDown=5;
				Ability A=CMClass.getAbility("Prayer_CureLight");
				if(A!=null)
				for(Enumeration e=followers.elements();e.hasMoreElements();)
					A.invoke(myChar,((MOB)e.nextElement()),true);
			}
			if((--tenDown)<=0)
			{
				tenDown=10;
				Ability A=CMClass.getAbility("Prayer_RemovePoison");
				if(A!=null)
				for(Enumeration e=followers.elements();e.hasMoreElements();)
					A.invoke(myChar,((MOB)e.nextElement()),true);
			}
			if((--twentyDown)<=0)
			{
				twentyDown=10;
				Ability A=CMClass.getAbility("Prayer_CureDisease");
				if(A!=null)
				for(Enumeration e=followers.elements();e.hasMoreElements();)
					A.invoke(myChar,((MOB)e.nextElement()),true);
			}
		}
		return;
	}
	
	public String statQualifications(){return "Wisdom 9+ Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Healer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CHARISMA)<=9)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Healer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "All healing prayers give bonus healing.  Attains healing aura after 30th level.";}
	public String otherLimitations(){return "Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.";}
	public String weaponLimitations(){return "May use Blunt, Flailed weapons, Hammers, and Natural (unarmed) weapons only.";}

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
				if(hq==0)
				{
					myChar.tell("The evil nature of "+A.name()+" disrupts your prayer.");
					return false;
				}
				else
				if(myChar.getAlignment()<500)
					basis=100;
				else
				if(hq==1000)
					basis=(1000-align)/10;
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
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				
				if((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_BLUNT)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_HAMMER)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_FLAILED)
				||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_NATURAL))
					return true;
				if(myChar.fetchWieldedItem()==null) return true;
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"A conflict of <S-HIS-HER> conscience makes <S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
					return false;
				}
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&((affect.sourceMinor()==Affect.TYP_COLD)
				||(affect.sourceMinor()==Affect.TYP_WATER)))
			{
				int recovery=affect.targetCode()-Affect.MASK_HURT;
				recovery=recovery-myChar.charStats().getClassLevel(this);
				if(recovery<0) recovery=1;
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
			else
			if((affect.amITarget(myChar))
			&&((affect.targetCode()&Affect.MASK_HURT)>0)
			&&(affect.sourceMinor()==Affect.TYP_FIRE))
			{
				int recovery=affect.targetCode()-Affect.MASK_HURT;
				recovery=recovery*2;
				if(recovery>1000) recovery=1000;
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),Affect.MASK_HURT+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
			}
		}
		return true;
	}

	public void affect(MOB myChar, Affect affect)
	{
		super.affect(myChar,affect);
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(myChar.isMine(affect.tool()))
			&&((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
			{
				if((affect.target()!=null)
				   &&(affect.target() instanceof MOB))
				{
					MOB tmob=(MOB)affect.target();
					if(affect.tool().ID().equals("Prayer_CureLight"))
						tmob.curState().adjHitPoints(Dice.roll(2,6,4),tmob.maxState());
					else
					if(affect.tool().ID().equals("Prayer_CureSerious"))
						tmob.curState().adjHitPoints(Dice.roll(2,16,4),tmob.maxState());
					else
					if(affect.tool().ID().equals("Prayer_CureCritical"))
						tmob.curState().adjHitPoints(Dice.roll(4,16,4),tmob.maxState());
					else
					if(affect.tool().ID().equals("Prayer_Heal"))
						tmob.curState().adjHitPoints(Dice.roll(5,20,4),tmob.maxState());
					else
					if(affect.tool().ID().equals("Prayer_MassHeal"))
						tmob.curState().adjHitPoints(Dice.roll(5,20,4),tmob.maxState());
				}
			}
		}
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