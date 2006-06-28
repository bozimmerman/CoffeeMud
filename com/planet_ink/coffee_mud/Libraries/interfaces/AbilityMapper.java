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
public interface AbilityMapper extends CMObject
{
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
    }
    
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain, Vector preReqSkillsList);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain, String extraMasks);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, boolean autoGain, Vector preReqSkillsList, String extraMasks);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, String defParm, boolean autoGain);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, String defParm, boolean autoGain, String extraMasks);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, boolean autoGain);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, boolean autoGain, String extraMasks);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
									  String defaultParam, boolean autoGain, boolean secret);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
									  String defaultParam, boolean autoGain, boolean secret, String extraMasks);
    public void addCharAbilityMapping(String ID, int qualLevel, String ability, int defaultProficiency, 
    								  String defaultParam, boolean autoGain, boolean secret,
    								  Vector preReqSkillsList, String extraMask);
    public void delCharAbilityMapping(String ID, String ability);
	public void addPreRequisites(String ID, Vector preReqSkillsList, String extraMask);
    public void delCharMappings(String ID);
    public Enumeration getClassAbles(String ID);
    public boolean qualifiesByAnyCharClass(String abilityID);
    public int lowestQualifyingLevel(String ability);
    public boolean classOnly(String classID, String abilityID);
	public boolean availableToTheme(String abilityID, int theme, boolean publicly);
	public Vector getAllowsList(String ID);
    public Vector getLevelListings(String ID, boolean checkAll, int level);
    public Vector getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly);
    public int getQualifyingLevel(String ID, boolean checkAll, String ability);
    public int qualifyingLevel(MOB student, Ability A);
    public String getExtraMask(String ID, boolean checkAll, String ability);
	public String getApplicableMask(MOB student, Ability A);
	public DVector getPreReqs(String ID, boolean checkAll, String ability);
	public DVector getUnmetPreRequisites(MOB student, Ability A);
	public DVector getApplicablePreRequisites(MOB mob, Ability A);
	public DVector getCommonPreRequisites(Ability A);
	public String getCommonExtraMask(Ability A);
	public String formatPreRequisites(DVector preReqs);
    public int qualifyingClassLevel(MOB student, Ability A);
    public Object lowestQualifyingClassRace(MOB student, Ability A);
    public boolean qualifiesByCurrentClassAndLevel(MOB student, Ability A);
    public boolean qualifiesByLevel(MOB student, Ability A);
	public boolean qualifiesByLevel(MOB student, String ability);
    public boolean getDefaultGain(String ID, boolean checkAll, String ability);
    public AbilityMapping getAllAbleMap(String ability);
    public boolean getSecretSkill(String ID, boolean checkAll, String ability);
    public boolean getAllSecretSkill(String ability);
    public boolean getSecretSkill(MOB mob, String ability);
    public boolean getSecretSkill(String ability);
    public String getDefaultParm(String ID, boolean checkAll, String ability);
    public int getDefaultProficiency(String ID, boolean checkAll, String ability);
}
