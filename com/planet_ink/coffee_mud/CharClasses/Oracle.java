package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Oracle extends Cleric
{
	public String ID(){return "Oracle";}
	public String name(){return "Oracle";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int allowedWeaponLevel(){return CharClass.WEAPONS_GOODCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected boolean disableClericSpellGrant(){return true;}
	protected int alwaysFlunksThisQuality(){return 0;}

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

			CMAble.addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_SenseLife",true);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseGood",true);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseUndead",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_SenseAlignment",true);

			CMAble.addCharAbilityMapping(ID(),5,"Skill_TurnUndead",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_ControlUndead",false);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
			CMAble.addCharAbilityMapping(ID(),5,"Spell_DetectMetal",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true);
			CMAble.addCharAbilityMapping(ID(),6,"Prayer_SenseDisease",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Bless",true);

			CMAble.addCharAbilityMapping(ID(),8,"Spell_IdentifyObject",false);

			CMAble.addCharAbilityMapping(ID(),9,"Spell_Augury",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_WildernessLore",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Farsight",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_SenseHidden",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_CureCritical",false);

			CMAble.addCharAbilityMapping(ID(),15,"Spell_KnowValue",false);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false);

			CMAble.addCharAbilityMapping(ID(),16,"Spell_LocateObject",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);

			CMAble.addCharAbilityMapping(ID(),18,"Spell_SeeAura",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Heal",true);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Philosophy",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_BlessItem",true);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_HolyWord",true);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Resurrect",true);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_DivinePerspective",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean tick(Tickable myChar, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
		}
		return true;
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

	private int numNonQualified(MOB mob)
	{
		int numNonQualified=0;
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			boolean qualifies=false;
			for(int c=0;c<mob.charStats().numClasses();c++)
			{
				CharClass C=mob.charStats().getMyClass(c);
				if(CMAble.getQualifyingLevel(C.ID(),true,A.ID())>0)
					qualifies=true;
			}
			if(!qualifies)
				numNonQualified++;
		}
		return numNonQualified;
	}
	
	private int maxNonQualified(MOB mob)
	{
		int level=mob.charStats().getClassLevel(this)-30;
		level++;
		return level;
	}
	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		// if he already has one, don't give another!
		if((mob.playerStats()!=null)
		&&(mob.charStats().getClassLevel(this)>=30))
		{
			if(numNonQualified(mob)>=maxNonQualified(mob)) return;
			
			Ability newOne=null;
			int tries=0;
			while((newOne==null)&&((++tries)<100))
			{
				CharClass C=CMClass.randomCharClass();
				if((C!=null)
				&&(C!=this)
				&&(!C.ID().equalsIgnoreCase("Archon"))
				&&(mob.charStats().getClassLevel(C)<0))
				{
					int tries2=0;
					while((newOne==null)&&((++tries2)<10000))
					{
						Ability A=CMClass.randomAbility();
						int lql=CMAble.lowestQualifyingLevel(A.ID());
						if((A!=null)
						&&(lql<25)
						&&(lql>0)
						&&(!CMAble.getSecretSkill(C.ID(),true,A.ID()))
						&&(CMAble.getQualifyingLevel(ID(),true,A.ID())<0)
						&&(!CMAble.classOnly("Archon",A.ID()))
					    &&(CMAble.qualifiesByAnyCharClass(A.ID()))
						&&(mob.fetchAbility(A.ID())==null))
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

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
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
			else
			if(numNonQualified(mob)>=maxNonQualified(mob))
				mob.tell("^NYou have learned no new secrets this level, as you already know ^H"+numNonQualified(mob)+"/"+maxNonQualified(mob)+"^? secret skills.^N");
		}
	}
}
