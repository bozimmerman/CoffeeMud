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
public class Arcanist extends Thief
{
	public String ID(){return "Arcanist";}
	public String name(){return "Arcanist";}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	public int getManaDivisor(){return 4;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 4;}

	public Arcanist()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",false,"+WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Alchemy",false,"+INT 12 +WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_ReadMagic",true);

		// clan magic
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqAcid",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqCold",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqElectric",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqFire",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqGas",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqMind",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqParalysis",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqPoison",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqWater",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_CEqDisease",0,"",false,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Spell_Erase",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_WandUse",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Spell_EnchantArrows",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Spell_ClarifyScroll",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"PaperMaking",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Revoke",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_DetectTraps",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Pick",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Spellcraft",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false,CMParms.parseSemicolons("Apothecary",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_UsePoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_RemoveTraps",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_RechargeWand",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Spell_RepairingAura",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Trip",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Spell_Scribe",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Spell_DisenchantWand",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Map",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Spell_EnchantWand",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Bash",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_WardArea",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Shadow",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Detection",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_Knock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Refit",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_LightenItem",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Observation",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_Mend",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Disenchant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_ComprehendLangs",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_StoreSpell",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_MagicItem",true);
	}


	public String getStatQualDesc(){return "Dexterity 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become an Arcanist.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become an Arcanist.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public String getOtherBonusDesc()
	{
		return "Magic resistance, 1%/level.  Huge discounts when buying potions after 5th level.  Ability to memorize spells learned through SpellCraft.";
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((myHost==null)
		||(!(myHost instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)myHost;
		if((msg.amISource(mob))
		&&(mob.charStats().getClassLevel(this)>4))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_BUY)
				||(msg.sourceMinor()==CMMsg.TYP_LIST))
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Potion))
			{
				mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|EnvStats.IS_BONUS);
				mob.recoverEnvStats();
				mob.recoverCharStats();
			}
			else
			if((mob.baseEnvStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS)
			{
				mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()-EnvStats.IS_BONUS);
				mob.recoverEnvStats();
				mob.recoverCharStats();
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void endCharacter(MOB mob)
	{
		Vector otherChoices=new Vector();
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A2=mob.fetchAbility(a);
			if((A2!=null)
			&&(!A2.savable())
			&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
				otherChoices.addElement(A2);
		}
		for(int a=0;a<otherChoices.size();a++)
			mob.delAbility((Ability)otherChoices.elementAt(a));
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
		   return;
		MOB mob=(MOB)myHost;
		if(msg.amISource(mob)&&(msg.tool()!=null))
		{
			if(msg.tool().ID().equals("Skill_Spellcraft"))
			{
				if((msg.tool().text().length()>0)
				&&(msg.target()!=null)
				&&(msg.target() instanceof MOB))
				{
					Ability A=((MOB)msg.target()).fetchAbility(msg.tool().text());
					if((A!=null)
					&&(mob.fetchAbility(A.ID())==null)
					&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30))
					{
						Vector otherChoices=new Vector();
						for(int a=0;a<mob.numLearnedAbilities();a++)
						{
							Ability A2=mob.fetchAbility(a);
							if((A2!=null)
							&&(!A2.savable())
							&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
								otherChoices.addElement(A2);
						}
						A=(Ability)A.copyOf();
						A.setProficiency(0);
						A.setSavable(false);
						mob.addAbility(A);
						if(otherChoices.size()>(mob.charStats().getClassLevel(this)/3))
							mob.delAbility((Ability)otherChoices.elementAt(CMLib.dice().roll(1,otherChoices.size(),-1)));
					}
				}
			}
			else
			if(msg.tool().ID().equals("Spell_Scribe")
			||msg.tool().ID().equals("Spell_EnchantWand")
			||msg.tool().ID().equals("Spell_MagicItem")
			||msg.tool().ID().equals("Spell_StoreSpell")
			||msg.tool().ID().equals("Spell_WardArea"))
			{
				Ability A=mob.fetchAbility(msg.tool().text());
				if((A!=null)&&(!A.savable()))
					mob.delAbility(A);
			}
			else
			if(msg.tool() instanceof Ability)
			{
				Ability A=mob.fetchAbility(msg.tool().ID());
				if((A!=null)&&(!A.savable())
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
					mob.delAbility(A);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&((((MOB)ticking).baseEnvStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS))
		{
			((MOB)ticking).baseEnvStats().setDisposition(((MOB)ticking).baseEnvStats().disposition()-EnvStats.IS_BONUS);
			((MOB)ticking).recoverEnvStats();
		}
		return super.tick(ticking,tickID);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC,affectableStats.getStat(CharStats.STAT_SAVE_MAGIC)+(affectableStats.getClassLevel(this)));
		if(CMath.bset(affected.baseEnvStats().disposition(),EnvStats.IS_BONUS))
			affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+30);
	}
}
