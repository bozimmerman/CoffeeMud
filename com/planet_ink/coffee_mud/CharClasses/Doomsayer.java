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
		maxStatAdj[CharStats.STRENGTH]=4;
		maxStatAdj[CharStats.WISDOM]=4;
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
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_CurseFlames",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtFire",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtGood",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Faithless",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_FlameWeapon",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Cannibalism",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_CurseMetal",false);
			CMAble.addCharAbilityMapping(ID(),9,"Fighter_Intimidate",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_CurseMind",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_Demonshield",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_AuraHarm",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_MassDeafness",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",false);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_DailyBread",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BladeBarrier",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_CurseLuck",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassBlindness",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_DemonicConsumption",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Condemnation",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_CurseMinds",false);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_Doomspout",false);

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
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
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
			&&(CMAble.getQualifyingLevel(ID(),msg.tool().ID())>0)
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
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{

				if((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_EDGED)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_POLEARM)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_AXE)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_DAGGER)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_SWORD))
					return true;
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
					return false;
				}
			}
			else
			if((msg.amITarget(myChar))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
			{
				int recovery=myChar.charStats().getClassLevel(this);
				msg.setValue(msg.value()-recovery);
			}
			else
			if((msg.amITarget(myChar))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.sourceMinor()==CMMsg.TYP_COLD))
			{
				int recovery=msg.value();
				msg.setValue(msg.value()+recovery);
			}
		}
		return true;
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
