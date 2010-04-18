package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Oracle extends Cleric
{
	public String ID(){return "Oracle";}
	public String name(){return "Oracle";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_GOODCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected int alwaysFlunksThisQuality(){return 0;}

	public Oracle()
	{
        super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_SenseLife",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseGood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_SenseAllergies",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_SenseAlignment",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_Freedom",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_TurnUndead",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_ControlUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Spell_DetectMetal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_SenseDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Bless",true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_CureFatigue",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Spell_IdentifyObject",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_MinorInfusion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Spell_Augury",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_WildernessLore",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Farsight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_SenseHidden",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_Spellcraft",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_CureDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CureCritical",false,CMParms.parseSemicolons("Prayer_CureSerious",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_SenseProfessions",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_KnowValue",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_LocateObject",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_SenseSkills",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_SeeAura",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_InfuseHoliness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_CureExhaustion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false,CMParms.parseSemicolons("Prayer_Freedom",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_SenseSongs",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Heal",true,CMParms.parseSemicolons("Prayer_CureCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Philosophy",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_SenseSpells",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHeal",false,CMParms.parseSemicolons("Prayer_Heal",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false,CMParms.parseSemicolons("Prayer_HolyAura",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_SensePrayers",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Resurrect",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_DivinePerspective",false);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public boolean tick(Tickable myChar, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
		}
		return true;
	}

	public String getStatQualDesc(){return "Wisdom 9+ Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Oracle.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Oracle.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String getOtherBonusDesc(){return "Receives a non-class skill at 30th level, and every Oracle level thereafter.";}
	public String getOtherLimitsDesc(){return "Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.";}

	protected int numNonQualified(MOB mob)
	{
		int numNonQualified=0;
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			boolean qualifies=false;
			for(int c=0;c<mob.charStats().numClasses();c++)
			{
				CharClass C=mob.charStats().getMyClass(c);
				if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>0)
					qualifies=true;
			}
			if((!qualifies)
			&&(CMLib.ableMapper().getQualifyingLevel(mob.baseCharStats().getMyRace().ID(),true,A.ID())<0)
			&&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID())))
				numNonQualified++;
		}
		return numNonQualified;
	}
	
	protected int maxNonQualified(MOB mob)
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
				&&(!C.ID().equals(ID()))
				&&(!C.ID().equalsIgnoreCase("Archon"))
				&&(mob.charStats().getClassLevel(C)<0))
				{
					int tries2=0;
					while((newOne==null)&&((++tries2)<10000))
					{
						Ability A=CMClass.randomAbility();
						if( A != null )
						{
						  int lql=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
						  if((lql<25)
						      &&(lql>0)
						      &&(!CMLib.ableMapper().getSecretSkill(C.ID(),true,A.ID()))
						      &&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())<0)
						      &&(CMLib.ableMapper().availableToTheme(A.ID(),Area.THEME_FANTASY,true))
						      &&(CMLib.ableMapper().qualifiesByAnyCharClass(A.ID()))
                          &&(A.isAutoInvoked()||((A.triggerStrings()!=null)&&(A.triggerStrings().length>0)))
                          &&(mob.fetchAbility(A.ID())==null))
						  {
						    newOne=A;
						  }
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

	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
	public void level(MOB mob, Vector newAbilityIDs)
	{
	    if(CMSecurity.isDisabled("LEVELS")) return;
		if((!mob.isMonster())&&(mob.charStats().getClassLevel(this)>=30))
		{
			if((newAbilityIDs.size()==0)&&(numNonQualified(mob)>=maxNonQualified(mob)))
				mob.tell("^NYou have learned no new secrets this level, as you already know ^H"+numNonQualified(mob)+"/"+maxNonQualified(mob)+"^? secret skills.^N");
		}
	}
}
