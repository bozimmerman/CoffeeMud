package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class ArtisanalFocus extends StdAbility
{
	@Override
	public String ID()
	{
		return "ArtisanalFocus";
	}

	@Override
	public String displayText()
	{
		return "";
	}

	private final static String	localizedName	= CMLib.lang().L("Artisanal Focus");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int usageType()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY | Ability.DOMAIN_ARTISTIC;
	}

	protected final static String[][] nameSets = new String[][] {
		{"Farmer","Irrigation","Farming","Composting","PlantLore","Floristry","Herbology","Gardening","MasterFloristry","MasterHerbology","MasterFarming","MasterGardening"},
		{"Rancher","AnimalHusbandry","CageBuilding","Taxidermy","Branding","Baiting","Shearing","MasterShearing,"},
		{"Smith","Smelting","Armorsmithing","Weaponsmithing","JewelMaking","Blacksmithing","Rodsmithing","LockSmith","MasterWeaponsmithing","MasterArmorsmithing"},
		{"Tailor","Tailoring","LeatherWorking","Weaving","Cobbling","Tanning","Textiling","MasterLeatherWorking","MasterTailoring"},
		{"Sculptor","GlassBlowing","ScrimShaw","Sculpting","Pottery"},
		{"Prospector","Chopping","Fishing","Foraging","Mining","Drilling","Skill_WildernessLore","Trawling","Hunting","Digging","Speculate","Hunting","MasterMining","MasterDrilling","MasterDigging","MasterTrawling","MasterFishing","MasterChopping","MasterForaging"},
		{"Chef","Baking","FoodPrep","Cooking","Distilling","MeatCuring","FoodPreserving","Butchering","MasterButchering","MasterFoodPrep","MasterBaking","MasterDistilling","MasterCooking"},
		{"Carpenter","InstrumentMaking","Boatwright","Wainwrighting","Carpentry","WandMaking","StaffMaking","Fletching","Siegecraft","Skill_ShipLore","Costuming","PaperMaking","MasterCostuming"},
		{"Architect","Shipwright","Masonry","Excavation","Construction","Landscaping"},
		{"Artist","Lacquerring","Dyeing","Skill_Write","Painting","Decorating","Engraving","Embroidering","MasterLacquerring","MasterDyeing"},
	};
	protected final static Map<String, String[]>	namesLookup	= new Hashtable<String, String[]>();
	protected final static Map<String, Integer[]>	indexLookup	= new Hashtable<String, Integer[]>();
	static
	{
		for(int n=0;n<nameSets.length;n++)
		{
			final String[] nameSet=nameSets[n];
			for(int i=1;i<nameSet.length;i++)
			{
				final String[] oldS=namesLookup.get(nameSet[i]);
				if(oldS != null)
				{
					final String[] copy=Arrays.copyOf(oldS,oldS.length+1);
					copy[oldS.length]=nameSet[0];
					namesLookup.put(nameSet[i], copy);
				}
				else
					namesLookup.put(nameSet[i], new String[] {nameSet[0]});
				final Integer[] oldI=indexLookup.get(nameSet[i]);
				if(oldI != null)
				{
					final Integer[] copy=Arrays.copyOf(oldI,oldI.length+1);
					copy[oldI.length]=Integer.valueOf(n);
					indexLookup.put(nameSet[i], copy);
				}
				else
					indexLookup.put(nameSet[i], new Integer[] {Integer.valueOf(n)});
			}
		}
	}

	protected volatile long nextCheck = 0;
	protected String className = null;
	protected volatile boolean disabled = false;

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		disabled = newMiscText.trim().equalsIgnoreCase("disabled");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null))
		{
			final MOB mob=msg.source();
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			if("ARTFOCUS".startsWith(word))
			{
				if(disabled)
				{
					setMiscText("");
					mob.tell(L("Your artisanal focus notoriety is apparent."));
				}
				else
				{
					setMiscText("DISABLED");
					mob.tell(L("Your artisanal focus notoriety is no longer apparent."));
				}
				mob.recoverCharStats();
				return false;
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		if(disabled)
			return;
		if((affectedMOB.isPlayer())
		&& (System.currentTimeMillis() > nextCheck))
		{
			nextCheck=System.currentTimeMillis() + ((20+CMLib.dice().roll(1, 10, 0)) * 60 * 1000);
			final boolean isApprentice = affectableStats.getCurrentClass().ID().equals("Apprentice");
			if(isApprentice || (affectableStats.getCurrentClass().ID().equals("Artisan")))
			{
				final double[] scores = new double[nameSets.length];
				final int[] counts = new int[nameSets.length];
				for(final Enumeration<Ability> a=affectedMOB.abilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(indexLookup.containsKey(A.ID()))
					{
						double score = A.proficiency();
						final String[] expertises = CMLib.expertises().getApplicableExpertises(A.ID(), null);
						if(expertises != null)
						{
							for(final String experID : expertises)
							{
								final Pair<String,Integer> p=affectedMOB.fetchExpertise(experID);
								if(p != null)
								{
									final double experValue = 1.0 + CMath.div(10 * p.second.intValue(),CMLib.expertises().getStages(experID));
									score *= experValue;
								}
							}
						}
						for(final Integer I : indexLookup.get(A.ID()))
						{
							counts[I.intValue()]++;
							scores[I.intValue()]+=score;
						}
					}
				}
				double mean = 0.0;
				double meancount = 0;
				int highestIndex = -1;
				for(int n=0;n<nameSets.length;n++)
				{
					if(counts[n]>0)
					{
						scores[n] /= counts[n];
						if((highestIndex<0)||(scores[n] > scores[highestIndex]))
							highestIndex=n;
						mean += scores[n];
						meancount+=1.0;
					}
				}
				mean = mean / meancount;
				double variance = 0.0;
				for(int n=0;n<nameSets.length;n++)
				{
					if(counts[n]>0)
						variance += ((scores[n]-mean)*(scores[n]-mean));
				}
				if(meancount > 1)
					variance /= meancount;
				if(highestIndex < 0)
					className=null;
				else
				{
					final double stddev = Math.sqrt(variance);
					if((scores[highestIndex] >= mean)
					&&((meancount<3) || ((scores[highestIndex]-mean) >= stddev)))
						className = L(nameSets[highestIndex][0]+(isApprentice?" Apprentice":""));
					else
						className = null;
				}
			}
		}
		if(className != null)
			affectableStats.setDisplayClassName(className);

	}
}
