package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Arcanist";
	}

	private final static String localizedStaticName = CMLib.lang().L("Arcanist");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/5)+(1*(1?3))";
	}

	public Arcanist()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",false,"+WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ScrollScribing",10,true);

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
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_PowerGrab",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_WardArea",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Shadow",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Spell_Knock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Spell_Refit",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Detection",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Observation",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_DampenAuras",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Spell_Mend",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_Disenchant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Spell_ComprehendLangs",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Spell_StoreSpell",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Thief_SlipperyMind",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Spell_MagicItem",true);
	}

	private final String[] raceRequiredList=new String[]{"Drow","Elf","Gnome","HalfElf","Human","Svirfneblin",
														"Githyanki","Merfolk","Faerie","Orc","Mindflayer"};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}
	
	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]
	{
		new Pair<String,Integer>("Dexterity",Integer.valueOf(9)),
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Magic resistance, 1%/level.  Huge discounts when buying potions after 5th level.  Ability to memorize spells learned through SpellCraft. "
				+ "Can see wand charges at level 30.");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((myHost==null)
		||(!(myHost instanceof MOB)))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)myHost;
		if((msg.amISource(mob))
		&&(mob.charStats().getClassLevel(this)>4))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_BUY)
				||(msg.sourceMinor()==CMMsg.TYP_LIST))
			&&(msg.tool() instanceof Potion))
			{
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_BONUS);
				mob.recoverPhyStats();
				mob.recoverCharStats();
			}
			else
			if((mob.basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS)
			{
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()-PhyStats.IS_BONUS);
				mob.recoverPhyStats();
				mob.recoverCharStats();
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void endCharacter(MOB mob)
	{
		final Vector<Ability> otherChoices=new Vector<Ability>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A2=mob.fetchAbility(a);
			if((A2!=null)
			&&(!A2.isSavable())
			&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
				otherChoices.addElement(A2);
		}
		for(int a=0;a<otherChoices.size();a++)
			mob.delAbility(otherChoices.elementAt(a));
	}

	private void addAbilityToSpellcraftList(MOB mob, Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Spellcraft");
		if(enabledA!=null)
		{
			final List<String> ables=CMParms.parseCommas(enabledA.text(), true);
			if(!ables.contains(A.ID()))
			{
				if(enabledA.text().length()==0)
					enabledA.setMiscText(A.ID());
				else
					enabledA.setMiscText(enabledA.text()+", "+A.ID());
				mob.addAbility(A);
			}
			else
			if(mob.isMine(A) && (A.proficiency()<75) && (!A.isSavable()))
				A.setProficiency(A.proficiency()+(mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)/3));
		}
	}

	private void clearAbilityFromSpellcraftList(MOB mob, Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Spellcraft");
		if(enabledA!=null)
		{
			final List<String> ables=CMParms.parseCommas(enabledA.text(), true);
			if(ables.contains(A.ID()))
			{
				if(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.ALLSKILLS))
				{
					ables.remove(A.ID());
					enabledA.setMiscText(CMParms.toListString(ables));
					mob.delAbility(A);
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
			return;
		final MOB mob=(MOB)myHost;
		if(msg.amISource(mob))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE))
			&&(msg.target() instanceof Wand)
			&&(mob.charStats().getClassLevel(this)>=30))
			{
				final String message="<O-NAME> has "+((Wand)msg.target()).usesRemaining()+" charges remaining.";
				msg.addTrailerMsg(CMClass.getMsg(mob, null, msg.target(), CMMsg.MSG_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, message));
			}
			else
			if(msg.tool()!=null)
			{
				if(msg.tool().ID().equals("Skill_Spellcraft"))
				{
					if((msg.tool().text().length()>0)
					&&(msg.target() instanceof MOB))
					{
						Ability A=((MOB)msg.target()).fetchAbility(msg.tool().text());
						if(A==null)
							return;
						final Ability myA=mob.fetchAbility(A.ID());
						if(myA!=null)
						{
							if((!A.isSavable())
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
							&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30))
								addAbilityToSpellcraftList(mob,A);
						}
						else
						if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30)
						{
							final Vector<Ability> otherChoices=new Vector<Ability>();
							for(int a=0;a<mob.numAbilities();a++)
							{
								final Ability A2=mob.fetchAbility(a);
								if((A2!=null)
								&&(!A2.isSavable())
								&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
									otherChoices.addElement(A2);
							}
							A=(Ability)A.copyOf();
							A.setProficiency(0);
							A.setSavable(false);
							if(otherChoices.size()>(mob.charStats().getClassLevel(this)/3))
							{
								final Ability A2=otherChoices.elementAt(CMLib.dice().roll(1,otherChoices.size(),-1));
								clearAbilityFromSpellcraftList(mob,A2);
							}
							addAbilityToSpellcraftList(mob,A);
						}
					}
				}
				else
				if((msg.sourceMinor()!=CMMsg.TYP_PREINVOKE)
				&&(msg.tool().ID().equals("Spell_Scribe")
				||msg.tool().ID().equals("Spell_EnchantWand")
				||msg.tool().ID().equals("Spell_MagicItem")
				||msg.tool().ID().equals("Spell_StoreSpell")
				||msg.tool().ID().equals("Spell_WardArea")))
				{
					final Ability A=mob.fetchAbility(msg.tool().text());
					if((A!=null)&&(!A.isSavable()))
						clearAbilityFromSpellcraftList(mob,A);
				}
				else
				if(msg.tool() instanceof Ability)
				{
					final Ability A=mob.fetchAbility(msg.tool().ID());
					if((A!=null)&&(!A.isSavable())
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
						clearAbilityFromSpellcraftList(mob,A);
				}
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking instanceof MOB)
		&&((((MOB)ticking).basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS))
		{
			((MOB)ticking).basePhyStats().setDisposition(((MOB)ticking).basePhyStats().disposition()-PhyStats.IS_BONUS);
			((MOB)ticking).recoverPhyStats();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC,affectableStats.getStat(CharStats.STAT_SAVE_MAGIC)+(affectableStats.getClassLevel(this)));
		if(CMath.bset(affected.basePhyStats().disposition(),PhyStats.IS_BONUS))
			affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+30);
	}
}
