package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;

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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Reliquist extends Thief
{
	@Override
	public String ID()
	{
		return "Reliquist";
	}

	private final static String localizedStaticName = CMLib.lang().L("Reliquist");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_ANY;
	}

	@Override
	public String[] getRequiredRaceList()
	{
		return super.getRequiredRaceList();
	}

	private final Pair<String, Integer>[] minimumStatRequirements = new Pair[]
	{
		new Pair<String, Integer>("Dexterity", Integer.valueOf(9)),
		new Pair<String, Integer>("Wisdom", Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Magic resistance, 1%/level.  Huge discounts when buying holy potions after 5th level.  Ability to memorize spells learned through PrayerCraft. "
				+ "Can see scroll charges at level 30.");
	}

	public Reliquist()
	{
		super();
		maxStatAdj[CharStats.STAT_DEXTERITY]=4;
		maxStatAdj[CharStats.STAT_WISDOM]=4;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Whip",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Staff",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Flailed",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ThievesCant",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_ReadPrayer",0,true);

		//Q=Qualify G=GAIN A=Gain if alignment is the same as the prayer
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_MaskFaith",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_NeutralizeScroll",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_RelicUse",true);
		//3	Relics (G), Graverobbing (Q)
		//4	Sneak (Q), Prayer_ClarifyScroll (G), Mark (G), Prayer_DepleteScroll (A), Prayer_EmpowerScroll (A), Prayer_AttuneScroll (A)
		//5	Carpentry(G), Revoke (Q), Detect traps (Q), Whiplash (Q)
		//6	Pick Locks (Q), Shield Bash (Q), Store Prayer (A), Tongues (A), Fluency (A)
		//7	Prayercraft (G), Prayer_SenseDevotion(Q), Unearth Clues (Q)
		//8	Hammerring(Q), Decipher Script (Q), Sense Magic (G), Empower Shield (G)
		//9	Borrow Boon (G), Remove Traps (Q), Read Languages (G)
		//10	Cure Light Wounds (A), Recharge Relic (A), Cause Light Wounds (A)
		//11	Trip (G),  Lore(Q), Lesser Warding Glyph (Q), Deflect Prayer (Q)
		//12	ScrollScribe(Q), Staff Making (Q), Sow Discord (G)
		//13	RevealText(Q), Skill_Map (Q), Deplete Relic (A), Empower Relic (A), Attune Relic(A)
		//14	TransferBane(A), Prayer_DivineTransferrence (A), TransferBoon(A)
		//15	Enchant Relic(Q), Power Grab (Q), Research Item (G), Whipsmack (Q)
		//16	Improved Warding Glyph (Q), Prayer_SenseParish(Q)
		//17	Second Attack (Q), Disguise Faith (G), Create Digsite (Q)
		//18	Skill_FalseService (G), Suppression (Q)
		//19	Detection(Q), Empower Holy Weapon (Q), Empower Unholy Weapon (Q), Empower Sacred Weapon (Q), Reflect Prayer (Q)
		//20	Heroic Leap(Q), Sin Mark(G), Imbue Shield (Q), Defile Shield (Q)
		//21	Alchemy(G), Prayer_Seekersprayer(A), Share Boon (A), Steal Boon (A), Empower Just Weapon (Q), Empower Modest Weapon (Q), Empower Foul Weapon (Q)
		//22	AutoHammerring (Q), Greater Warding Glyph (Q), Find Sacred Item (Q)
		//23	Skill_BefoulShrine(Q), Empower Holy Armor (Q), Empower Unholy Armor (Q), Empower Sacred Armor (Q)
		//24	Divine Feud(G), Prayer_ProtectRelic(Q), Whipstrip (Q)
		//25	Imbue Unholy Weapon (Q), Imbue Sacred Weapon (Q), Imbue Holy Weapon (Q), Imbue Just Weapon (Q), Imbue Modest Weapon (Q), Imbue Foul Weapon (Q)
		//26
		//27
		//28
		//29
		//30	Divine Quest (G)
		//35	Divine Pilgrimage (Q)
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_MAGIC, affectableStats.getStat(CharStats.STAT_SAVE_MAGIC) + affectableStats.getClassLevel(this));
		if(CMath.bset(affected.basePhyStats().disposition(),PhyStats.IS_BONUS))
			affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+30);
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
			if((msg.tool() instanceof Ability)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL))
			{
				if(msg.source().baseCharStats().getMyDeity()==null)
				{
					msg.source().tell(L("You lack the true faith to do that."));
					return false;
				}
			}
			if(mob.charStats().getClassLevel(this)>4)
			{
				if(((msg.sourceMinor()==CMMsg.TYP_BUY)
					||(msg.sourceMinor()==CMMsg.TYP_LIST))
				&&(msg.tool() instanceof Potion))
				{
					final Potion P=(Potion)msg.tool();
					boolean hasPrayer=true;
					for(final Ability A : P.getSpells())
					{
						if((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER)
							hasPrayer=false;
					}
					if(hasPrayer)
					{
						mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_BONUS);
						mob.recoverPhyStats();
						mob.recoverCharStats();
					}
				}
				else
				if((mob.basePhyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS)
				{
					mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()-PhyStats.IS_BONUS);
					mob.recoverPhyStats();
					mob.recoverCharStats();
				}
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
			&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
				otherChoices.addElement(A2);
		}
		for(int a=0;a<otherChoices.size();a++)
			mob.delAbility(otherChoices.elementAt(a));
	}

	private void addAbilityToPrayercraftist(final MOB mob, final Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Prayercraft");
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
				A.setProficiency(A.proficiency()+(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)/3));
		}
	}

	private void clearAbilityFromPrayercraftist(final MOB mob, final Ability A)
	{
		final Ability enabledA=mob.fetchAbility("Skill_Prayercraft");
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
			&&((((Wand)msg.target()).getEnchantType()<0)
				||(((Wand)msg.target()).getEnchantType()==Ability.ACODE_PRAYER))
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
				if(msg.tool().ID().equals("Skill_Prayercraft"))
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
							&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
							&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<30))
								addAbilityToPrayercraftist(mob,A);
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
								&&((A2.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
									otherChoices.addElement(A2);
							}
							A=(Ability)A.copyOf();
							A.setProficiency(0);
							A.setSavable(false);
							if(otherChoices.size()>(mob.charStats().getClassLevel(this)/3))
							{
								final Ability A2=otherChoices.elementAt(CMLib.dice().roll(1,otherChoices.size(),-1));
								clearAbilityFromPrayercraftist(mob,A2);
							}
							addAbilityToPrayercraftist(mob,A);
						}
					}
				}
				else
				if((msg.sourceMinor()!=CMMsg.TYP_PREINVOKE)
				&&(msg.tool().ID().equals("Spell_EnchantRelic")))
				{
					final Ability A=mob.fetchAbility(msg.tool().text());
					if((A!=null)&&(!A.isSavable()))
						clearAbilityFromPrayercraftist(mob,A);
				}
				else
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
				{
					final Ability A=mob.fetchAbility(msg.tool().ID());
					if((A!=null)&&(!A.isSavable())
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
						clearAbilityFromPrayercraftist(mob,A);
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
