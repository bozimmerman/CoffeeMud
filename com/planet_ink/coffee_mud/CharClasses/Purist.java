package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Purist extends Cleric
{
	public String ID(){return "Purist";}
	public String name(){return "Purist";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}

	public Purist()
	{
		maxStatAdj[CharStats.WISDOM]=4;
		maxStatAdj[CharStats.CHARISMA]=4;
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
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Extinguish",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_InfuseUnholiness",true);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_CreateWater",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_Purify",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",true);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CreateFood",false);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_ProtCold",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_RitegousIndignation",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
			CMAble.addCharAbilityMapping(ID(),7,"Prayer_HuntEvil",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_Gills",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Monolith",0,"ICE",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_FountainLife",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_FreezeMetal",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Conviction",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_Wave",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_AuraDivineEdict",true);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassMobility",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Atonement",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"WATER",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_IceHealing",false);

			CMAble.addCharAbilityMapping(ID(),30,"Prayer_AuraIntolerance",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public void tick(MOB myChar, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
		}
		return;
	}

	public String statQualifications(){return "Wisdom 9+ Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Purist.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CHARISMA)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Purist.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives 1pt/level cold damage reduction.";}
	public String otherLimitations(){return "Always fumbles evil prayers, and fumbles all prayers when alignment is below 500.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.  Vulnerable to fire attacks.";}
	public String weaponLimitations(){return "May use Blunt, Flailed weapons, Hammers, and Natural (unarmed) weapons only.";}

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
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{

				if((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_BLUNT)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_HAMMER)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_FLAILED)
				||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL))
					return true;
				if(myChar.fetchWieldedItem()==null) return true;
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.WISDOM)*2)
				{
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"A conflict of <S-HIS-HER> conscience makes <S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
					return false;
				}
			}
			else
			if((msg.amITarget(myChar))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&((msg.sourceMinor()==CMMsg.TYP_COLD)
				||(msg.sourceMinor()==CMMsg.TYP_WATER)))
			{
				int recovery=myChar.charStats().getClassLevel(this);
				msg.setValue(msg.value()-recovery);
			}
			else
			if((msg.amITarget(myChar))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
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
			Weapon w=(Weapon)CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}