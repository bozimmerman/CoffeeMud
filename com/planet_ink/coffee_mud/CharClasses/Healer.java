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
	public int allowedWeaponLevel(){return CharClass.WEAPONS_GOODCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(){return disallowedWeapons;}
	protected boolean disableClericSpellGrant(MOB mob){return true;}
	protected int alwaysFlunksThisQuality(){return 0;}

	private int fiveDown=5;
	private int tenDown=10;
	private int twentyDown=20;

	public Healer()
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
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Annul",false);

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_InfuseHoliness",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_RemoveDeathMark",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Fidelity",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_SenseDisease",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",false);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Freedom",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Forgive",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",false);
			CMAble.addCharAbilityMapping(ID(),9,"Prayer_GodLight",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_CureVampirism",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Benediction",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",true);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_AuraHeal",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyShield",true);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
			CMAble.addCharAbilityMapping(ID(),16,"Prayer_CureCannibalism",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_DispelUndead",false);
			CMAble.addCharAbilityMapping(ID(),18,"Prayer_BlessedHearth",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",false);
			CMAble.addCharAbilityMapping(ID(),19,"Prayer_DeathsDoor",false);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true);
			CMAble.addCharAbilityMapping(ID(),20,"Prayer_PeaceRitual",false);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Atonement",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_ConsecrateLand",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",true);
			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassCureDisease",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_DivineResistance",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",true);

		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public void tick(MOB myChar, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)&&(myChar.charStats().getClassLevel(this)>=30))
		{
			if(((--fiveDown)>1)&&((--tenDown)>1)&&((--twentyDown)>1)) return;

			HashSet followers=myChar.getGroupMembers(new HashSet());
			if(myChar.location()!=null)
				for(int i=0;i<myChar.location().numInhabitants();i++)
				{
					MOB M=myChar.location().fetchInhabitant(i);
					if((M!=null)
					&&((M.getVictim()==null)||(!followers.contains(M.getVictim()))))
						followers.add(M);
				}
			if((fiveDown)<=0)
			{
				fiveDown=5;
				Ability A=CMClass.getAbility("Prayer_CureLight");
				if(A!=null)
				for(Iterator e=followers.iterator();e.hasNext();)
					A.invoke(myChar,((MOB)e.next()),true);
			}
			if((tenDown)<=0)
			{
				tenDown=10;
				Ability A=CMClass.getAbility("Prayer_RemovePoison");
				if(A!=null)
				for(Iterator e=followers.iterator();e.hasNext();)
					A.invoke(myChar,((MOB)e.next()),true);
			}
			if((twentyDown)<=0)
			{
				twentyDown=10;
				Ability A=CMClass.getAbility("Prayer_CureDisease");
				if(A!=null)
				for(Iterator e=followers.iterator();e.hasNext();)
					A.invoke(myChar,((MOB)e.next()),true);
			}
		}
		super.tick(myChar,tickID);
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
		if(mob.baseCharStats().getStat(CharStats.CHARISMA)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Healer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "All healing prayers give bonus healing.  Attains healing aura after 30th level.";}
	public String otherLimitations(){return "Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

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
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(myHost instanceof MOB)) return;
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&(CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0)
		&&(myChar.isMine(msg.tool()))
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
		{
			if((msg.target()!=null)
			   &&(msg.target() instanceof MOB))
			{
				MOB tmob=(MOB)msg.target();
				if(msg.tool().ID().equals("Prayer_CureLight"))
					tmob.curState().adjHitPoints(Dice.roll(2,6,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_CureSerious"))
					tmob.curState().adjHitPoints(Dice.roll(2,16,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_CureCritical"))
					tmob.curState().adjHitPoints(Dice.roll(4,16,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_Heal"))
					tmob.curState().adjHitPoints(Dice.roll(5,20,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_MassHeal"))
					tmob.curState().adjHitPoints(Dice.roll(5,20,4),tmob.maxState());
			}
		}
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