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
import com.planet_ink.coffee_mud.Libraries.Factions;
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
public class Purist extends Cleric
{
	public String ID(){return "Purist";}
	public String name(){return "Purist";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_GOODCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected int alwaysFlunksThisQuality(){return 0;}

	public Purist()
	{
        super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_TurnUndead",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Marry",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CureLight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Extinguish",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_CreateWater",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_Purify",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Fidelity",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CreateFood",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_ProtCold",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_RighteousIndignation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Bless",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_HuntEvil",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Freedom",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_Gills",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_CureFatigue",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Monolith",0,"ICE",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_FountainLife",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_Sanctuary",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true,CMParms.parseSemicolons("Prayer_CureSerious",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_FreezeMetal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Conviction",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_InfuseHoliness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_Wave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_ProtectElements",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Godstrike",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_AuraDivineEdict",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",false,CMParms.parseSemicolons("Prayer_Freedom",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassMobility",false,CMParms.parseSemicolons("Prayer_ProtParalyzation",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Heal",true,CMParms.parseSemicolons("Prayer_CureCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Atonement",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHeal",true,CMParms.parseSemicolons("Prayer_Heal",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_LinkedHealth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false,CMParms.parseSemicolons("Prayer_HolyAura",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_Nullification",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_SummonElemental",0,"WATER",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_IceHealing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Prayer_AuraIntolerance",false);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public boolean tick(Tickable myChar, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
		}
		return true;
	}

	public String getStatQualDesc(){return "Wisdom 9+ Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Purist.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Purist.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String getOtherBonusDesc(){return "Receives 1pt/level cold damage reduction.";}
	public String getOtherLimitsDesc(){return "Always fumbles evil prayers, and fumbles all prayers when alignment is below pure neutral.  Qualifies and receives good prayers, and bonus damage from good spells.  Using non-aligned prayers introduces failure chance.  Vulnerable to fire attacks.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool())))
		{
			int alignment = myChar.fetchFaction(CMLib.factions().AlignID());
			int pct = CMLib.factions().getPercent(CMLib.factions().AlignID(), alignment);
			if(pct < 50)
			{
				myChar.tell("Your impurity disrupts the prayer.");
				return false;
			}
			int hq=holyQuality((Ability)msg.tool());
			if(hq==0)
			{
				myChar.tell("You most certainly should not be casting that.");
				return false;
			}
		}
		
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
		else
		if(msg.amISource(myChar)
		&&(!msg.amITarget(myChar))
		&&(msg.tool() instanceof Ability)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(myChar.isMine(msg.tool()))
		&&(isQualifyingAuthority(myChar,(Ability)msg.tool()))
		&&(holyQuality((Ability)msg.tool())==1000))
			msg.setValue(msg.value()*2);
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
	
}
