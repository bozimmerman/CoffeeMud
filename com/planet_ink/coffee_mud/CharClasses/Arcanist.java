package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
public class Arcanist extends Thief
{
	public String ID(){return "Arcanist";}
	public String name(){return "Arcanist";}
	public int availabilityCode(){return Area.THEME_FANTASY;}
	private static boolean abilitiesLoaded2=false;
	public boolean loaded(){return abilitiesLoaded2;}
	public void setLoaded(boolean truefalse){abilitiesLoaded2=truefalse;};
	public int getManaDivisor(){return 3;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 6;}

	public Arcanist()
	{
		super();
		maxStatAdj[CharStats.DEXTERITY]=4;
		maxStatAdj[CharStats.INTELLIGENCE]=4;
		if(!loaded())
		{
			setLoaded(true);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"ThievesCant",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Alchemy",false);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_ReadMagic",true);

			// clan magic
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqAcid",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqCold",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqElectric",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqFire",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqGas",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqMind",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqParalysis",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqPoison",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqWater",0,"",false,true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_CEqDisease",0,"",false,true);

			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Erase",false);

			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",25,true);

			CMAble.addCharAbilityMapping(ID(),4,"Spell_ClarifyScroll",false);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",false);

			CMAble.addCharAbilityMapping(ID(),5,"PaperMaking",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",false);

			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",false);

			CMAble.addCharAbilityMapping(ID(),7,"Skill_Spellcraft",true);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_IdentifyPoison",false);

			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_UsePoison",false);

			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_RemoveTraps",false);

			CMAble.addCharAbilityMapping(ID(),10,"Spell_RechargeWand",false);

			CMAble.addCharAbilityMapping(ID(),11,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",true);

			CMAble.addCharAbilityMapping(ID(),12,"Spell_Scribe",false);

			CMAble.addCharAbilityMapping(ID(),13,"Spell_DisenchantWand",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Map",false);

			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);

			CMAble.addCharAbilityMapping(ID(),15,"Spell_EnchantWand",false);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Bash",true);

			CMAble.addCharAbilityMapping(ID(),16,"Spell_WardArea",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),17,"Thief_Shadow",true);

			CMAble.addCharAbilityMapping(ID(),18,"Thief_Detection",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Knock",false);

			CMAble.addCharAbilityMapping(ID(),19,"Spell_Refit",true);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_LightenItem",false);

			CMAble.addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",false);

			CMAble.addCharAbilityMapping(ID(),21,"Thief_Observation",true);

			CMAble.addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",false);

			CMAble.addCharAbilityMapping(ID(),23,"Spell_Mend",true);

			CMAble.addCharAbilityMapping(ID(),24,"Spell_Disenchant",false);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_ComprehendLangs",false);

			CMAble.addCharAbilityMapping(ID(),25,"Spell_StoreSpell",false);

			CMAble.addCharAbilityMapping(ID(),30,"Spell_MagicItem",true);
		}
	}


	public String statQualifications(){return "Dexterity 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become an Arcanist.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become an Arcanist.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public String otherBonuses()
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
			&&(A2.isBorrowed(mob))
			&&((A2.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
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
					&&(CMAble.lowestQualifyingLevel(A.ID())<30))
					{
						Vector otherChoices=new Vector();
						for(int a=0;a<mob.numLearnedAbilities();a++)
						{
							Ability A2=mob.fetchAbility(a);
							if((A2!=null)
							&&(A2.isBorrowed(mob))
							&&((A2.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
								otherChoices.addElement(A2);
						}
						A=(Ability)A.copyOf();
						A.setProfficiency(0);
						A.setBorrowed(mob,true);
						mob.addAbility(A);
						if(otherChoices.size()>(mob.charStats().getClassLevel(this)/3))
							mob.delAbility((Ability)otherChoices.elementAt(Dice.roll(1,otherChoices.size(),-1)));
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
				if((A!=null)&&(A.isBorrowed(mob)))
					mob.delAbility(A);
			}
			else
			if(msg.tool() instanceof Ability)
			{
				Ability A=mob.fetchAbility(msg.tool().ID());
				if((A!=null)&&(A.isBorrowed(mob))
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.SPELL))
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
		affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+(affectableStats.getClassLevel(this)));
		if(Util.bset(affected.baseEnvStats().disposition(),EnvStats.IS_BONUS))
			affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+30);
	}
}
