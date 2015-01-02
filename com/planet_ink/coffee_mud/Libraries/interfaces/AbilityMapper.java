package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2015 Bo Zimmerman

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
/**
 * @author Owner
 *
 */
public interface AbilityMapper extends CMLibrary
{
	public static class QualifyingID
	{
		public String ID;
		public int qualifyingLevel;
		public QualifyingID(String id, int level)
		{ ID=id; qualifyingLevel=level;}
	}

	public static class AbilityPreReq
	{
		public String[] abilityIDs;
		public int proficiency;
		public AbilityPreReq(int prof, String[] ids)
		{ abilityIDs=ids; proficiency=prof;}
	}

	public static class AbilityLimits
	{
		public int commonSkills;
		public int craftingSkills;
		public int nonCraftingSkills;
		public int specificSkillLimit;
	}

	public static class AbilityMapping implements Cloneable
	{
		public static final int COST_PRAC=0;
		public static final int COST_TRAIN=1;
		public static final int COST_MANA=2;
		public static final int COST_PRACPRAC=3;
		public static final int COST_NUM=4;
		public String ID="";
		public String abilityID="";
		public int qualLevel=-1;
		public boolean autoGain=false;
		public int defaultProficiency=0;
		public int maxProficiency=100;
		public String defaultParm="";
		public boolean isSecret=false;
		public boolean isAllQualified=false;
		public DVector skillPreReqs=new DVector(2);
		public String extraMask="";
		public String originalSkillPreReqList="";
		public Integer[] costOverrides=new Integer[COST_NUM];
		public boolean allQualifyFlag=false;
		public Map<String,String> extFields=new Hashtable<String,String>(1);
		public AbilityMapping(String id){ ID=id;}
		public AbilityMapping copyOf()
		{
			try {
				final AbilityMapping A=(AbilityMapping)this.clone();
				A.skillPreReqs = skillPreReqs.copyOf();
				A.costOverrides = costOverrides.clone();
				return A;
			}catch(final Exception e) { return this;}
		}
	}

	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain, List<String> preReqSkillsList);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain, String extraMasks);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, boolean autoGain, List<String> preReqSkillsList, String extraMasks);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, String defParm, boolean autoGain);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, boolean autoGain);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
									  String defaultParam, boolean autoGain, boolean secret);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
									  String defaultParam, boolean autoGain, boolean secret,
									  List<String> preReqSkillsList, String extraMask);
	public AbilityMapping addCharAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
									  int maxProficiency, String defaultParam, boolean autoGain, boolean secret,
									  List<String> preReqSkillsList, String extraMask, Integer[] costOverrides);
	public AbilityMapping addDynaAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency,
			  						  String defaultParam, boolean autoGain, boolean secret, String extraMask);
	public AbilityMapping delCharAbilityMapping(String ID, String abilityID);

	public void addPreRequisites(String ID, List<String> preReqSkillsList, String extraMask);
	public void delCharMappings(String ID);
	public Enumeration<AbilityMapping> getClassAbles(String ID, boolean addAll);
	public boolean qualifiesByAnyCharClass(String abilityID);
	public int lowestQualifyingLevel(String abilityID);
	public boolean classOnly(String classID, String abilityID);
	public boolean classOnly(MOB mob, String classID, String abilityID);
	public boolean availableToTheme(String abilityID, int theme, boolean publicly);
	public int numMappedAbilities();
	public int getCalculatedMedianLowestQualifyingLevel();
	public Iterator<String> getAbilityAllowsList(String ableID);
	public List<QualifyingID> getClassAllowsList(String ID);
	public List<String> getLevelListings(String ID, boolean checkAll, int level);
	public List<AbilityMapping> getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly);
	public int getQualifyingLevel(String ID, boolean checkAll, String abilityID);
	public int qualifyingLevel(MOB studentM, Ability A);
	public String getExtraMask(String ID, boolean checkAll, String abilityID);
	public String getApplicableMask(MOB studentM, Ability A);
	public DVector getUnmetPreRequisites(MOB studentM, Ability A);
	public DVector getCommonPreRequisites(Ability A);
	public DVector getCommonPreRequisites(MOB mob, Ability A);
	public String getCommonExtraMask(Ability A);
	public String formatPreRequisites(DVector preReqs);
	public int qualifyingClassLevel(MOB studentM, Ability A);
	public boolean qualifiesOnlyByClan(MOB studentM, Ability A);
	public boolean qualifiesOnlyByRace(MOB studentM, Ability A);
	public CMObject lowestQualifyingClassRaceGovt(MOB studentM, Ability A);
	public boolean qualifiesByCurrentClassAndLevel(MOB studentM, Ability A);
	public boolean qualifiesOnlyByACharClass(MOB studentM, Ability A);
	public boolean qualifiesByLevel(MOB studentM, Ability A);
	public boolean qualifiesByLevel(MOB studentM, String abilityID);
	public boolean getDefaultGain(String ID, boolean checkAll, String abilityID);
	public boolean getAllQualified(String ID, boolean checkAll, String abilityID);
	public AbilityMapping getAllAbleMap(String abilityID);
	public AbilityMapping getAbleMap(String ID, String abilityID);
	public boolean getSecretSkill(String ID, boolean checkAll, String abilityID);
	public boolean getAllSecretSkill(String abilityID);
	public boolean getSecretSkill(MOB mob, String abilityID);
	public boolean getSecretSkill(String abilityID);
	public AbilityLimits getCommonSkillLimit(MOB studentM);
	public AbilityLimits getCommonSkillLimit(MOB studentM, Ability A);
	public AbilityLimits getCommonSkillRemainder(MOB studentM, Ability A);
	public AbilityLimits getCommonSkillRemainders(MOB student);
	public Integer[] getCostOverrides(String ID, boolean checkAll, String abilityID);
	public Integer[] getAllCostOverrides(String abilityID);
	public Integer[] getCostOverrides(MOB mob, String abilityID);
	public Integer[] getCostOverrides(String abilityID);
	public String getDefaultParm(String ID, boolean checkAll, String abilityID);
	public String getPreReqStrings(String ID, boolean checkAll, String abilityID);
	public int getDefaultProficiency(String ID, boolean checkAll, String abilityID);
	public int getMaxProficiency(String ID, boolean checkAll, String abilityID);
	public int getMaxProficiency(String abilityID);
	public int getMaxProficiency(MOB mob, boolean checkAll, String abilityID);
	public List<Object> componentCheck(MOB mob, List<AbilityComponent> req);
	public int destroyAbilityComponents(List<Object> found);
	public String getAbilityComponentDesc(MOB mob, String AID);
	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req);
	public Map<String, List<AbilityComponent>> getAbilityComponentMap();
	public String addAbilityComponent(String s, Map<String, List<AbilityComponent>> H);
	public String getAbilityComponentCodedString(String AID);
	public List<AbilityComponent> getAbilityComponentDVector(String AID);
	public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req, int r);
	public List<DVector> getAbilityComponentDecodedDVectors(List<AbilityComponent> req);
	public List<DVector> getAbilityComponentDecodedDVectors(String AID);
	public void setAbilityComponentCodedFromDecodedDVector(DVector decodedDV, AbilityComponent comp);
	public DVector getAbilityComponentDecodedDVector(AbilityComponent comp);
	public String getAbilityComponentCodedString(List<AbilityComponent> comps);
	public AbilityComponent createBlankAbilityComponent();
	public boolean isDomainIncludedInAnyAbility(int domain, int acode);
	public void alterAbilityComponentFile(String compID, boolean delete);
	public void saveAllQualifysFile(Map<String, Map<String,AbilityMapping>> newMap);
	public Map<String, Map<String,AbilityMapping>> getAllQualifiesMap(final Map<String,Object> cache);
	public AbilityMapping makeAbilityMapping(String ID, int qualLevel, String abilityID, int defaultProficiency, int maxProficiency, String defaultParam, boolean autoGain,
			 boolean secret, boolean isAllQualified, List<String> preReqSkillsList, String extraMask, Integer[] costOverrides);
	public Map<String, int[]> getHardOverrideManaCache();
}
