package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Oracle extends Cleric
{
	public String ID(){return "Oracle";}
	public String name(){return "Oracle";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};

	protected boolean disableAlignedWeapons(){return true;}
	protected boolean disableClericSpellGrant(){return true;}
	protected boolean disableAlignedSpells(){return true;}

	public Oracle()
	{
		maxStatAdj[CharStats.WISDOM]=4;
		maxStatAdj[CharStats.INTELLIGENCE]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_SenseLife",true);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseUndead",true);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",true);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_SenseAlignment",true);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_TurnUndead",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_ControlUndead",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",false);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DetectMetal",true);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",true);

			CMAble.addCharAbilityMapping(ID(),8,"Spell_IdentifyObject",true);

			CMAble.addCharAbilityMapping(ID(),9,"Spell_Augury",true);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",true);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Farsight",true);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_SenseHidden",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true);

			CMAble.addCharAbilityMapping(ID(),15,"Spell_KnowValue",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false);

			CMAble.addCharAbilityMapping(ID(),16,"Spell_LocateObject",true);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_SeeAura",true);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Philosophy",false);

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
		if(tickID==Host.TICK_MOB)
		{
		}
		return;
	}

	public String statQualifications(){return "Wisdom 9+ Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Oracle.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Oracle.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Receives a non-class skill at 30th level, and every Oracle level thereafter.";}
	public String otherLimitations(){return "Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.";}
	public String weaponLimitations(){return "May use Blunt, Flailed weapons, Hammers, and Natural (unarmed) weapons only.";}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		// if he already has one, don't give another!
		if((!mob.isMonster())&&(mob.charStats().getClassLevel(this)>=30))
		{
			int numNonQualified=0;
			for(int a=0;a<mob.numLearnedAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if(CMAble.getQualifyingLevel(ID(),A.ID())<0)
					numNonQualified++;
			}
			int level=mob.charStats().getClassLevel(this)-30;
			level++;
			if(numNonQualified>=level) return;
			Ability newOne=null;
			int tries=0;
			while((newOne==null)&&((++tries)<100))
			{
				CharClass C=CMClass.randomCharClass();
				if((C!=null)&&(C!=this))
				{
					int tries2=0;
					while((newOne==null)&&((++tries2)<1000))
					{
						Ability A=CMClass.randomAbility();
						if((A!=null)
						   &&(!CMAble.getSecretSkill(C.ID(),A.ID()))
						   &&(CMAble.getQualifyingLevel(C.ID(),A.ID())>=1)
						   &&(CMAble.getQualifyingLevel(this.ID(),A.ID())<0))
						{
							newOne=A;
							break;
						}
					}
				}
			}
			if(newOne!=null)
				mob.addAbility(newOne);
		}
	}

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
			&&(msg.tool() instanceof Ability)
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
			&&(Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
			&&((msg.sourceMinor()==CMMsg.TYP_COLD)
				||(msg.sourceMinor()==CMMsg.TYP_WATER)))
			{
				int recovery=myChar.charStats().getClassLevel(this);
				SaucerSupport.adjustDamageMessage(msg,recovery*-1);
			}
			else
			if((msg.amITarget(myChar))
			&&(Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
			&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
			{
				int recovery=msg.targetCode()-CMMsg.MASK_HURT;
				SaucerSupport.adjustDamageMessage(msg,recovery);
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
			if(mob.freeWearPositions(Item.WIELD)>0)
				w.wearAt(Item.WIELD);
		}
	}
	public void level(MOB mob)
	{
		Vector V=new Vector();
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if(A!=null)	V.addElement(A);
		}
		super.level(mob);
		if((!mob.isMonster())&&(mob.charStats().getClassLevel(this)>=30))
		{
			Ability able=null;
			for(int a=0;a<mob.numLearnedAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if((A!=null)
				&&(!V.contains(A)))
					able=A;
			}
			if(able!=null)
			{
				String type=Ability.TYPE_DESCS[(able.classificationCode()&Ability.ALL_CODES)].toLowerCase();
				mob.tell("^NYou have learned the secret to the "+type+" ^H"+able.name()+"^?.^N");
			}
		}
	}
}