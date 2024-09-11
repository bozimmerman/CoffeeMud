package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Gypsy extends Thief
{
	@Override
	public String ID()
	{
		return "Gypsy";
	}

	private final static String localizedStaticName = CMLib.lang().L("Gypsy");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_GYPSY;
	}

	private final String[] raceRequiredList=new String[]{
		"Aarakocran", "Centaur", "Githyanki", "Gnoll",
		"Gnome", "HalfElf", "Human", "LizardMan"
	};

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[]
	{
		new Pair<String, Integer>("Dexterity", Integer.valueOf(9)),
		new Pair<String, Integer>("Constitution", Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public int maxLanguages()
	{
		return CMProps.getIntVar(CMProps.Int.MAXLANGUAGES) * 5;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Huge discounts when buying druidic potions after 5th level.  Ability to memorize chants learned through ChantCraft. "
				+ "Can see druidic scroll charges at level 30.");
	}

	public Gypsy()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
	}

	@Override
	public int getBonusPracLevel()
	{
		return 2;
	}

	@Override
	public String getMovementFormula()
	{
		return "10*((@x2<@x3)/18)";
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/6)+(1*(1?5))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/10)+(1*(1?2))";
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Apothecary",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Unbinding",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ScrollScribing",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_ReadRunes",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Staff",0,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Runecasting",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Thief_Kidnapping",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_ShardUse",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Chant_KnowAnimal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Sneak",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_EnhancePotion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Chant_SparkRunes",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"JewelMaking",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Chant_InspectShard",0,false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_Urchinize",0,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Thief_Pick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Chant_RefreshRunes",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Peek",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_StaffSweep",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_TarotReading",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_UsePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Chant_ReleaseMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_FindRunaway",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_Tasseography",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_RechargeShards",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Chant_CurseMood",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_Haggle",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Herbalism",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_MyUrchins",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Steal",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_EnhanceJewelry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Chant_ExtendFortune",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_Listen",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"ShardMaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_StaffBlock",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_Astrology",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_SuppressFortune",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Chant_EnchantShards",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Chant_BountifulWomb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_NameUrchin",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_PlantItem",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_PowerGrab",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Chant_EnhanceShard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"CaravanBuilding",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"ImprovedHerbalism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Tumble",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Chant_CurseFortune",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_Shadow",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_StaffSpin",false,
				CMParms.parseSemicolons("Fighter_StaffBlock",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Thief_PalmReading",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_UsePotion",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_ScrollFamiliarity",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Chant_StealFortune",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Thief_Detection",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Chant_StrengthenSeed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_TradeCharting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Chant_EndowGemstones",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Thief_PromoteUrchin",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_ConcealPathway",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Chant_ReverseFortune",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_CallUrchins",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Chant_EndowClub",true);

		//CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_UrchinSpy",false); // untested
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_StaffThrust",true); // untested

		//CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Chant_CurseSeed",true); // untested
		//CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_CaravanTravel",false); // untested

		//CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_EndowJewelry",false); // untested
		//CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Chant_FortuneTelling",true); // untested

		//CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chant_EndowIounStone",true); // untested
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("SmallHammer");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((myHost==null)
		||(!(myHost instanceof MOB)))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)myHost;
		if(msg.amISource(mob))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_CAST_SPELL:
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				&&(!mob.isMonster())
				&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
				&&(mob.isMine(msg.tool()))
				&&(isQualifyingAuthority(mob,(Ability)msg.tool())))
				{
					final Ability A=(Ability)msg.tool();
					if(A.appropriateToMyFactions(mob))
						return true;
					msg.source().tell(L("Extreme emotions disrupt your magic."));
					return false;

				}
				break;
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_LIST:
				{
					if(msg.tool() instanceof LandTitle)
					{
						final LandTitle t = (LandTitle)msg.tool();
						final Room R = t.getATitledRoom();
						if((R != null)&&(R.getArea() instanceof Boardable))
						{
							final Item I = ((Boardable)R.getArea()).getBoardableItem();
							if((I != null)
							&&((((Rideable)I).rideBasis() == Rideable.Basis.LAND_BASED)
								||(((Rideable)I).rideBasis() == Rideable.Basis.WAGON)))
							{
								return true;
							}
							msg.source().tell(L("A Gypsy may not own real estate."));
							return false;
						}
					}
					if((msg.tool() instanceof Potion)
					&&(mob.charStats().getClassLevel(this)>4))
					{
						final Potion P=(Potion)msg.tool();
						boolean hasChant=true;
						for(final Ability A : P.getSpells())
						{
							if((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_CHANT)
								hasChant=false;
						}
						if(hasChant)
						{
							mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_BONUS);
							mob.recoverPhyStats();
							mob.recoverCharStats();
						}
					}
				}
				break;
			case CMMsg.TYP_DROP:
				if((msg.target() instanceof Item)
				&&(!CMLib.flags().isDroppable((Item)msg.target())))
				{
					CMLib.flags().setDroppable((Item)msg.target(), true);
					if(!msg.source().okMessage(msg.source(), msg))
					{
						CMLib.flags().setDroppable((Item)msg.target(), false);
						return false;
					}
					final Item fi = (Item)msg.target();
					msg.addTrailerRunnable(new Runnable() {
						final Item I = fi;
						@Override
						public void run()
						{
							CMLib.flags().setDroppable(I, false);
						}
					});
				}
				break;
			case CMMsg.TYP_REMOVE:
				if((msg.target() instanceof Item)
				&&(!CMLib.flags().isRemovable((Item)msg.target())))
				{
					CMLib.flags().setRemovable((Item)msg.target(), true);
					if(!msg.source().okMessage(msg.source(), msg))
					{
						CMLib.flags().setRemovable((Item)msg.target(), false);
						return false;
					}
					final Item fi = (Item)msg.target();
					msg.addTrailerRunnable(new Runnable() {
						final Item I = fi;
						@Override
						public void run()
						{
							CMLib.flags().setRemovable(I, false);
						}
					});
				}
				break;
			default:
				if((mob.basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS)
				{
					mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()-PhyStats.IS_BONUS);
					mob.recoverPhyStats();
					mob.recoverCharStats();
				}
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void endCharacter(final MOB mob)
	{
		final Vector<Ability> otherChoices=new Vector<Ability>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A2=mob.fetchAbility(a);
			if((A2!=null)
			&&(!A2.isSavable())
			&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT))
				otherChoices.addElement(A2);
		}
		for(int a=0;a<otherChoices.size();a++)
			mob.delAbility(otherChoices.elementAt(a));
	}

	private void addAbilityToChantcraftist(final MOB mob, final Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Chantcraft");
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
				A.setProficiency(A.proficiency()+(mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)/3));
		}
	}

	private void clearAbilityFromChantcraftist(final MOB mob, final Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Chantcraft");
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
	public void grantAbilities(final MOB mob, final boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);

		if(mob.playerStats()==null)
		{
			final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(final AbilityMapper.AbilityMapping able : V)
			{
				final Ability A=CMClass.getAbility(able.abilityID());
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID()))
				&&(!CMLib.ableMapper().getAllQualified(ID(),true,A.ID())))
				{
					giveMobAbility(mob,A,
								   CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),
								   CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),
								   isBorrowedClass);
				}
			}
			return;
		}

		if(!ID().equals("Gypsy"))
			return;

		final int classLevel = mob.baseCharStats().getClassLevel(this);
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==classLevel)
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
				return;
		}
		// now only give one, for current level, respecting alignment!
		for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())>0)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			&&(A.appropriateToMyFactions(mob))
			&&(CMLib.ableMapper().getSecretSkill(ID(),true,A.ID())==SecretFlag.PUBLIC)
			&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID())==classLevel)
			&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
			{
				//final AbilityMapper.AbilityMapping able=CMLib.ableMapper().getQualifyingMapping(ID(),true,A.ID());
				giveMobAbility(mob,A,
							   CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),
							   CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),
							   isBorrowedClass);
				break; // one is enough
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
			&&((((Wand)msg.target()).getEnchantType()<0)
				||(((Wand)msg.target()).getEnchantType()==Ability.ACODE_CHANT))
			&&(mob.charStats().getClassLevel(this)>=30))
			{
				final int maxCharges = ((Wand)msg.target()).usesRemaining();
				final String message;
				if((maxCharges < Integer.MAX_VALUE/2)&&(maxCharges > 0))
					message=L("<O-NAME> has @x1/@x2 charges remaining.",""+((Wand)msg.target()).usesRemaining(),""+maxCharges);
				else
					message=L("<O-NAME> has @x1 charges remaining.",""+((Wand)msg.target()).usesRemaining());
				msg.addTrailerMsg(CMClass.getMsg(mob, null, msg.target(), CMMsg.MSG_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, message));
			}
			else
			if((msg.tool()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
			{
				if(msg.tool().ID().equals("Skill_Chantcraft"))
				{
					if((msg.tool().text().length()>0)
					&&(msg.target() instanceof MOB)
					&&(msg.source().baseCharStats().getMyDeity()!=null))
					{
						Ability A=((MOB)msg.target()).fetchAbility(msg.tool().text());
						if(A==null)
							return;
						final Ability myA=mob.fetchAbility(A.ID());
						if(myA!=null)
						{
							if((!A.isSavable())
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
							&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30))
								addAbilityToChantcraftist(mob,A);
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
								&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT))
									otherChoices.addElement(A2);
							}
							A=(Ability)A.copyOf();
							A.setProficiency(0);
							A.setSavable(false);
							if(otherChoices.size()>(mob.charStats().getClassLevel(this)/3))
							{
								final Ability A2=otherChoices.elementAt(CMLib.dice().roll(1,otherChoices.size(),-1));
								clearAbilityFromChantcraftist(mob,A2);
							}
							addAbilityToChantcraftist(mob,A);
						}
					}
				}
				else
				if((msg.sourceMinor()!=CMMsg.TYP_PREINVOKE)
				&&(msg.tool().ID().equals("Chant_EnchantShards")))
				{
					final Ability A=mob.fetchAbility(msg.tool().text());
					if((A!=null)&&(!A.isSavable()))
						clearAbilityFromChantcraftist(mob,A);
				}
				else
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT))
				{
					final Ability A=mob.fetchAbility(msg.tool().ID());
					if((A!=null)&&(!A.isSavable())
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT))
						clearAbilityFromChantcraftist(mob,A);
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((ticking instanceof MOB)
		&&((((MOB)ticking).basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS))
		{
			((MOB)ticking).basePhyStats().setDisposition(((MOB)ticking).basePhyStats().disposition()-PhyStats.IS_BONUS);
			((MOB)ticking).recoverPhyStats();
		}
		return super.tick(ticking,tickID);
	}
}
