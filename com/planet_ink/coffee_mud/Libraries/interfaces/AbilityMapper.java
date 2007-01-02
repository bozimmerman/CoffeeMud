package com.planet_ink.coffee_mud.Libraries.interfaces;
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
   Copyright 2000-2006 Bo Zimmerman

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
    /**
     * @author Owner
     *
     */
    public static class AbilityMapping
    {
        public String abilityName="";
        public int qualLevel=-1;
        public boolean autoGain=false;
        public int defaultProficiency=0;
        public String defaultParm="";
        public boolean isSecret=false;
        public DVector skillPreReqs=new DVector(2);
        public String extraMask="";
        public int[] pracTrainCost=new int[2];
    }
    
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param autoGain
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param autoGain
     * @param preReqSkillsList
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain, Vector preReqSkillsList);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param autoGain
     * @param extraMasks
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain, String extraMasks);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param autoGain
     * @param preReqSkillsList
     * @param extraMasks
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain, Vector preReqSkillsList, String extraMasks);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param defParm
     * @param autoGain
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, String defParm, boolean autoGain);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param defParm
     * @param autoGain
     * @param extraMasks
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, String defParm, boolean autoGain, String extraMasks);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param autoGain
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, boolean autoGain);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param autoGain
     * @param extraMasks
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, boolean autoGain, String extraMasks);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param defaultParam
     * @param autoGain
     * @param secret
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
									  String defaultParam, boolean autoGain, boolean secret);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param defaultParam
     * @param autoGain
     * @param secret
     * @param extraMasks
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
									  String defaultParam, boolean autoGain, boolean secret, String extraMasks);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param defaultParam
     * @param autoGain
     * @param secret
     * @param preReqSkillsList
     * @param extraMask
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
    								  String defaultParam, boolean autoGain, boolean secret,
    								  Vector preReqSkillsList, String extraMask);
    /**
     * @param ID
     * @param qualLevel
     * @param ability
     * @param defaultProficiency
     * @param defaultParam
     * @param autoGain
     * @param secret
     * @param preReqSkillsList
     * @param extraMask
     * @param pracTrainCost
     */
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
                                      String defaultParam, boolean autoGain, boolean secret,
                                      Vector preReqSkillsList, String extraMask, int[] pracTrainCost);
    /**
     * @param ID
     * @param ability
     */
    public void delCharAbilityMapping(String ID, String ability);
	/**
	 * @param ID
	 * @param preReqSkillsList
	 * @param extraMask
	 */
	public void addPreRequisites(String ID, Vector preReqSkillsList, String extraMask);
    /**
     * @param ID
     */
    public void delCharMappings(String ID);
    /**
     * @param ID
     * @return
     */
    public Enumeration getClassAbles(String ID);
    /**
     * @param abilityID
     * @return
     */
    public boolean qualifiesByAnyCharClass(String abilityID);
    /**
     * @param ability
     * @return
     */
    public int lowestQualifyingLevel(String ability);
    /**
     * @param classID
     * @param abilityID
     * @return
     */
    public boolean classOnly(String classID, String abilityID);
	/**
	 * @param mob
	 * @param classID
	 * @param abilityID
	 * @return
	 */
	public boolean classOnly(MOB mob, String classID, String abilityID);
	/**
	 * @param abilityID
	 * @param theme
	 * @param publicly
	 * @return
	 */
	public boolean availableToTheme(String abilityID, int theme, boolean publicly);
	/**
	 * @param ableID
	 * @return
	 */
	public Vector getAbilityAllowsList(String ableID);
    /**
     * @param ableID
     * @return
     */
    public Vector getClassAllowsList(String ID);
    /**
     * @param ID
     * @param checkAll
     * @param level
     * @return
     */
    public Vector getLevelListings(String ID, boolean checkAll, int level);
    /**
     * @param ID
     * @param level
     * @param ignoreAll
     * @param gainedOnly
     * @return
     */
    public Vector getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public int getQualifyingLevel(String ID, boolean checkAll, String ability);
    /**
     * @param student
     * @param A
     * @return
     */
    public int qualifyingLevel(MOB student, Ability A);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public String getExtraMask(String ID, boolean checkAll, String ability);
	/**
	 * @param student
	 * @param A
	 * @return
	 */
	public String getApplicableMask(MOB student, Ability A);
	/**
	 * @param ID
	 * @param checkAll
	 * @param ability
	 * @return
	 */
	public DVector getPreReqs(String ID, boolean checkAll, String ability);
	/**
	 * @param student
	 * @param A
	 * @return
	 */
	public DVector getUnmetPreRequisites(MOB student, Ability A);
	/**
	 * @param mob
	 * @param A
	 * @return
	 */
	public DVector getApplicablePreRequisites(MOB mob, Ability A);
	/**
	 * @param A
	 * @return
	 */
	public DVector getCommonPreRequisites(Ability A);
	/**
	 * @param A
	 * @return
	 */
	public String getCommonExtraMask(Ability A);
	/**
	 * @param preReqs
	 * @return
	 */
	public String formatPreRequisites(DVector preReqs);
    /**
     * @param student
     * @param A
     * @return
     */
    public int qualifyingClassLevel(MOB student, Ability A);
    /**
     * @param student
     * @param A
     * @return
     */
    public Object lowestQualifyingClassRace(MOB student, Ability A);
    /**
     * @param student
     * @param A
     * @return
     */
    public boolean qualifiesByCurrentClassAndLevel(MOB student, Ability A);
    /**
     * @param student
     * @param A
     * @return
     */
    public boolean qualifiesByLevel(MOB student, Ability A);
	/**
	 * @param student
	 * @param ability
	 * @return
	 */
	public boolean qualifiesByLevel(MOB student, String ability);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public boolean getDefaultGain(String ID, boolean checkAll, String ability);
    /**
     * @param ability
     * @return
     */
    public AbilityMapping getAllAbleMap(String ability);
    /**
     * @param ID
     * @param ability
     * @return
     */
    public AbilityMapping getAbleMap(String ID, String ability);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public boolean getSecretSkill(String ID, boolean checkAll, String ability);
    /**
     * @param ability
     * @return
     */
    public boolean getAllSecretSkill(String ability);
    /**
     * @param mob
     * @param ability
     * @return
     */
    public boolean getSecretSkill(MOB mob, String ability);
    /**
     * @param ability
     * @return
     */
    public boolean getSecretSkill(String ability);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public int[] getPracTrainCost(String ID, boolean checkAll, String ability);
    /**
     * @param ability
     * @return
     */
    public int[] getAllPracTrainCost(String ability);
    /**
     * @param mob
     * @param ability
     * @return
     */
    public int[] getPracTrainCost(MOB mob, String ability);
    /**
     * @param ability
     * @return
     */
    public int[] getPracTrainCost(String ability);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public String getDefaultParm(String ID, boolean checkAll, String ability);
    /**
     * @param ID
     * @param checkAll
     * @param ability
     * @return
     */
    public int getDefaultProficiency(String ID, boolean checkAll, String ability);
	/**
	 * @param mob
	 * @param req
	 * @return
	 */
	public Vector componentCheck(MOB mob, DVector req);
	/**
	 * @param mob
	 * @param AID
	 * @return
	 */
	public String getAbilityComponentDesc(MOB mob, String AID);
	/**
	 * @return
	 */
	public Hashtable getAbilityComponentMap();
	/**
	 * @param s
	 * @param to
	 * @return
	 */
	public String addAbilityComponent(String s, Hashtable to);

    /**
     * @param AID
     * @return
     */
    public String getAbilityComponentCodedString(String AID);
    
    /**
     * @param AID
     * @return
     */
    public DVector getAbilityComponentDVector(String AID);
    /**
     * @param mob
     * @param req
     * @param r
     * @return
     */
    public String getAbilityComponentDesc(MOB mob, DVector req, int r);
    /**
     * @param req
     * @return
     */
    public Vector getAbilityComponentDecodedDVectors(DVector req);
    /**
     * @param AID
     * @return
     */
    public Vector getAbilityComponentDecodedDVectors(String AID);
    
    /**
     * @param decodedDV
     * @param codedDV
     * @param row
     * @return
     */
    public void setAbilityComponentCodedFromDecodedDVector(DVector decodedDV, DVector codedDV, int row);
    /**
     * @param codedDV
     * @param r
     * @return
     */
    public DVector getAbilityComponentDecodedDVector(DVector codedDV, int r);
    /**
     * @param codedDV
     * @return
     */
    public void addBlankAbilityComponent(DVector codedDV);
    /**
     * @param s
     * @param to
     * @return
     */
    public boolean isDomainIncludedInAnyAbility(int domain, int acode);
}
