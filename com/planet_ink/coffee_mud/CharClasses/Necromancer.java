package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Necromancer extends Cleric
{
	public String ID(){return "Necromancer";}
	public String name(){return "Necromancer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}

	private int tickDown=0;

	public Necromancer()
	{
		maxStatAdj[CharStats.WISDOM]=4;
		maxStatAdj[CharStats.CONSTITUTION]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_ControlUndead",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_AnimateSkeleton",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_UndeadInvisibility",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_PreserveBody",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_FeedTheDead",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_AnimateZombie",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_AnimateGhoul",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_AnimateGhast",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_SenseHidden",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_AnimateSpectre",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_HealUndead",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_FeignLife",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",true);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_AnimateGhost",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_AnimateMummy",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Vampirism",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_AnimateDead",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_AnimateVampire",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Wisdom 9+ Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Necromancer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Necromancer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Becomes Lich upon death after reaching 30th level Necromancer.  Undead followers will not drain experience.";}
	public String otherLimitations(){return "Always fumbles good prayers.  Qualifies and receives evil prayers.  Using non-aligned prayers introduces failure chance.";}
	public String weaponLimitations(){return "May only use sword, axe, polearm, and some edged weapons.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool()!=null)
			&&(CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0)
			&&(myChar.isMine(msg.tool()))
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
			{
				int align=myChar.getAlignment();
				Ability A=(Ability)msg.tool();

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
			else
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			   &&(myChar.baseCharStats().getClassLevel(this)>=30)
			   &&(!myChar.baseCharStats().getMyRace().ID().equals("Lich")))
			{
				Race newRace=(Race)CMClass.getRace("Lich");
				if(newRace!=null)
				{
					myChar.tell("You are being transformed into a "+newRace.name()+"!!");
					myChar.baseCharStats().setMyRace(newRace);
					myChar.recoverCharStats();
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{

				if((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_EDGED)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_POLEARM)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_DAGGER)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_AXE)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_SWORD))
					return true;
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
					return false;
				}
			}
		}
		return true;
	}

	protected boolean isValidBeneficiary(MOB killer,
									     MOB killed,
									     MOB mob,
									     HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!mob.charStats().getMyRace().racialCategory().equals("Undead")))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}

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
