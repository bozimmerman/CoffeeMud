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
   Copyright 2017-2017 Bo Zimmerman

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
public class Scholar extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Scholar";
	}

	private final static String localizedStaticName = CMLib.lang().L("Scholar");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Commoner";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 1;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return -1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_INTELLIGENCE;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 50;
	}

	@Override
	public int maxLanguages()
	{
		return CMProps.getIntVar(CMProps.Int.MAXLANGUAGES) + 3;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/9)+(1*(1?3))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/9)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_CLOTH;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_STAFFONLY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	public Scholar()
	{
		super();
		maxStatAdj[CharStats.STAT_WISDOM]=6;
		maxStatAdj[CharStats.STAT_INTELLIGENCE]=6;
	}
	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Herbology",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Studying",true);
		
/*
1	Studying (G),Herbology (G),  Swim (Q), Write (GAIN), Recall (GAIN), 		
2	Tagging (G)		
3	PaperMaking (G), Combat Logging (Q)		
4	Organizing (Q), Thief_Mark (Q)		
5	Dissertating  (G), Skill_WandUse(Q), Smoke Signals (Q)		
6	Copywriting (G), Morse Code (Q)		
7	Edit (G), Identify Poison (Q), Druid_KnowPlants		
8	Skill_SeaMapping (G), RevealText (Q)		
9	Bookcopying (G), Semaphore (Q), Wilderness Lore (Q)		
10	Speculating (G), Subtitling (Q), Siegecraft (Q)		
11	Thief_Lore (G), Skillcraft (Q), InvisibleInk (Q)		
12	Studying (G), Honorary Degree: Commonerness (Q)		
13	Cataloging (G), Songcraft (Q), Skill_Map (Q)		
14	Honorary Degree: Fighterness (Q), Thief_Observere (Q), Thief_AnalyzeMark (Q)		
15	Spell_DetectMagic (Q)  Spellcraft (Q), Plant Lore (Q)		
16	 Instructing (G), Honorary Degree: Bardness (Q), Taxidermy (Q)		
17	 Shush (Q)  Prayercraft (Q), Appraise (Q)			
18	Recipecopying (G) Honorary Degree: Thiefness (Q), 		
19	ScrollScribing (Q)  Chantcraft (Q)		
20	Publish (Q) Honorary Degree: Mageness (Q)		
21	Revise (Q), Encrypting (Q)		
22	Surveying (G), Honorary Degree: Druidness (Q)		
23	Lecturing (G)		
24	Honorary Degree: Clericness (Q), Thief_Comprehension		
25	Enrolling (G)		
26			
27			
28			
29			
30	 Guildmaster (G)		
 */
		
//		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Costuming",false,CMParms.parseSemicolons("Tailoring",true));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// no xp from combat
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(msg.source()==myHost)
		&&(msg.target() instanceof MOB)
		&&(((MOB)msg.target()).amDead())
		&&(msg.value()>0))
		{
			msg.setValue(0);
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean canBeADivider(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		return false;
	}

	@Override
	public boolean canBeABenificiary(MOB killer, MOB killed, MOB mob, Set<MOB> followers)
	{
		return false;
	}

	//TODO:
	/*
Can memorize 2 abilities plus 1 for each additional 15 levels.  Abilities at lvl 0 and 0%, go away when attempted, or used for training, scribing or potions, similar to Arcanist.
	 */

	@Override
	public int addedExpertise(final MOB host, final ExpertiseLibrary.Flag expertiseCode, final String abilityID)
	{
		if((expertiseCode == ExpertiseLibrary.Flag.XPCOST) && (abilityID.equals("ScrollScribing")))
			return 15;
		return 0;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.source()==myHost)
		{
			if(((msg.targetMinor()==CMMsg.TYP_WRITE)
				||(msg.targetMinor()==CMMsg.TYP_WROTE))
			&&(msg.target() instanceof Item)
			&&(msg.targetMessage()!=null)
			&&(msg.targetMessage().length()>0))
			{
				if((msg.tool() instanceof Ability)
				&&(msg.targetMinor()==CMMsg.TYP_WROTE)
				&&(msg.tool().ID().equals("Skill_Map")||msg.tool().ID().equals("Thief_TreasureMap")||msg.tool().ID().equals("Skill_SeaMapping")))
					CMLib.leveler().postExperience(msg.source(), null, null, 10, false);
				else
				{
					final String msgStr =msg.targetMessage().trim();
					int numChars = msgStr.length()-CMStrings.countChars(msgStr, ' ');
					if(numChars > 10)
					{
						final Map<String,Object> persMap = Resources.getPersonalMap(myHost, true);
						if(persMap != null)
						{
							int xp = numChars/10;
							long[] xpTrap = (long[])persMap.get("SCHOLAR_WRITEXP");
							if(xpTrap == null)
							{
								xpTrap = new long[2];
								persMap.put("SCHOLAR_WRITEXP", xpTrap);
							}
							if(System.currentTimeMillis() > xpTrap[1])
							{
								xpTrap[0]=0;
								xpTrap[1]=System.currentTimeMillis() + TimeManager.MILI_HOUR;
							}
							if(xpTrap[0]<100)
							{
								if(100-xpTrap[0]<xp)
									xp=(int)(100-xpTrap[0]);
								xpTrap[0]+=xp;
								CMLib.leveler().postExperience(msg.source(), null, null, xp, false);
							}
						}
					}
				}
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(ID().equals(mob.charStats().getCurrentClass().ID()))
			{
			}
		}
		return super.tick(ticking,tickID);
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]
	{
		new Pair<String,Integer>("Intelligence",Integer.valueOf(9)),
		new Pair<String,Integer>("Wisdom",Integer.valueOf(6))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices == null)
		{
			outfitChoices=new Vector<Item>();
			
			final Weapon w=CMClass.getWeapon("Staff");
			if(w == null)
				return new Vector<Item>();
			outfitChoices.add(w);
			
			final Item I=CMClass.getBasicItem("GenJournal");
			I.setName(L("Scholar`s Logbook"));
			I.setDisplayText(L("A Scholar`s Logbook has been left here."));
			outfitChoices.add(I);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherLimitsDesc()
	{
		return L("Earns no combat experience.");
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Earn experience from teaching skills, making maps, writing books and journals. Learns skills by study, and gives bonus profficiency gains for group members.");
	}
}
