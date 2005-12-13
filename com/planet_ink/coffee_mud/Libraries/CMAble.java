package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
   Copyright 2000-2005 Bo Zimmerman

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
public class CMAble extends StdLibrary implements AbilityMapper
{
    public String ID(){return "CMAble";}
								
	public Hashtable completeAbleMap=new Hashtable();
	public Hashtable lowestQualifyingLevelMap=new Hashtable();
	
	public void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 boolean autoGain)
	{
		addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false);
	}
	public void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 String defParm,
											 boolean autoGain)
	{
		addCharAbilityMapping(ID,qualLevel,ability,0,defParm,autoGain,false);
	}
	
	public void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 boolean autoGain)
	{
		addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false);
	}
	
	public void delCharAbilityMapping(String ID,
											 String ability)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new Hashtable());
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		if(ableMap.containsKey(ability))
			ableMap.remove(ability);
	}
	public void delCharMappings(String ID)
	{
		if(completeAbleMap.containsKey(ID))
			completeAbleMap.remove(ID);
	}
	
	public Enumeration getClassAbles(String ID)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new Hashtable());
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		return ableMap.elements();
	}
	
	public void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 String defaultParam,
											 boolean autoGain,
											 boolean secret)
	{
		delCharAbilityMapping(ID,ability);
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		AbilityMapping able=new AbilityMapping();
		able.abilityName=ability;
		able.qualLevel=qualLevel;
		able.autoGain=autoGain;
		able.isSecret=secret;
		able.defaultParm=defaultParam;
		able.defaultProfficiency=defaultProfficiency;
		ableMap.put(ability,able);
		int arc_level=getQualifyingLevel("Archon",true,ability);
		if((arc_level<0)||((qualLevel>=0)&&(qualLevel<arc_level)))
			addCharAbilityMapping("Archon",qualLevel,ability,true);
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(ability);
		if((lowLevel==null)
		||(qualLevel<lowLevel.intValue()))
			lowestQualifyingLevelMap.put(ability,new Integer(qualLevel));
	}
	
	public boolean qualifiesByAnyCharClass(String abilityID)
	{
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			CharClass C=(CharClass)e.nextElement();
			if(completeAbleMap.containsKey(C.ID()))
			{
				Hashtable ableMap=(Hashtable)completeAbleMap.get(C.ID());
				if(ableMap.containsKey(abilityID)) 
					return true;
			}
		}
		return false;
	}
	
	public int lowestQualifyingLevel(String ability)
	{
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(ability);
		if(lowLevel==null) return 0;
		return lowLevel.intValue();
	}
	
	public boolean classOnly(String classID, String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID)) 
				return false;
		}
		else
			return false;
		for(Enumeration e=completeAbleMap.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if((!key.equalsIgnoreCase(classID))
			&&(((Hashtable)completeAbleMap.get(classID)).containsKey(abilityID)))
				return false;
		}
		return true;
	}
	
	public Vector getLevelListings(String ID, 
										  boolean checkAll,
										  int level)
	{
		Vector V=new Vector();
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				AbilityMapping able=(AbilityMapping)ableMap.get(key);
				if(able.qualLevel==level)
					V.addElement(key);
			}
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				AbilityMapping able=(AbilityMapping)ableMap.get(key);
				if((able.qualLevel==level)
				&&(!V.contains(key)))
					V.addElement(key);
			}
		}
		return V;
	}
	public Vector getUpToLevelListings(String ID, 
											  int level, 
											  boolean ignoreAll,
											  boolean gainedOnly)
	{
		Vector V=new Vector();
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				AbilityMapping able=(AbilityMapping)ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
					V.addElement(key);
			}
		}
		if((completeAbleMap.containsKey("All"))&&(!ignoreAll))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				AbilityMapping able=(AbilityMapping)ableMap.get(key);
				if((able.qualLevel<=level)
				&&(!V.contains(key))
				&&((!gainedOnly)||(able.autoGain)))
					V.addElement(key);
			}
		}
		return V;
	}
	
	public int getQualifyingLevel(String ID, 
										 boolean checkAll,
										 String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).qualLevel;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).qualLevel;
		}
		return -1;
	}

	
	public int qualifyingLevel(MOB student, Ability A)
	{
		if(student==null) return -1;
		int theLevel=-1;
		int greatestDiff=-1;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theLevel=level;
			}
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		int classLevel=student.baseEnvStats().level();
		if((level>=0)
		&&(classLevel>=level)
		&&((classLevel-level)>greatestDiff))
		{
			greatestDiff=classLevel-level;
			theLevel=level;
		}
		if(theLevel<0) 
			return getQualifyingLevel(student.charStats().getCurrentClass().ID(),true,A.ID());
		return theLevel;
	}

	public int qualifyingClassLevel(MOB student, 
										   Ability A)
	{
		if(student==null) return -1;
		int greatestDiff=-1;
		CharClass theClass=null;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theClass=C;
			}
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		int classLevel=student.baseEnvStats().level();
		if((level>=0)
		&&(classLevel>=level)
		&&((classLevel-level)>greatestDiff))
			greatestDiff=classLevel-level;
		if(theClass==null) 
			return student.charStats().getClassLevel(student.charStats().getCurrentClass());
		return student.charStats().getClassLevel(theClass);
	}

	public Object lowestQualifyingClassRace(MOB student, 
												   Ability A)
	{
		if(student==null) return null;
		int theLevel=-1;
		CharClass theClass=null;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((theLevel<0)||(theLevel>=level)))
			{
				theLevel=level;
				theClass=C;
			}
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		if((level>=0)
		&&((theClass==null)||((student.baseEnvStats().level()>=level)&&(theLevel>level))))
			return student.charStats().getMyRace();
		return theClass;
	}

	
	public boolean qualifiesByCurrentClassAndLevel(MOB student, Ability A)
	{
		if(student==null) return false;
		CharClass C=student.charStats().getCurrentClass();
		int level=getQualifyingLevel(C.ID(),true,A.ID());
		if((level>=0)
		&&(student.charStats().getClassLevel(C)>=level))
			return true;
		level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		if((level>=0)&&(student.envStats().level()>=level))
			return true;
		return false;
	}

	public boolean qualifiesByLevel(MOB student, Ability A)
	{
		if(student==null) return false;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			if((level>=0)
			&&(student.charStats().getClassLevel(C)>=level))
				return true;
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		if((level>=0)&&(student.envStats().level()>=level))
			return true;
		return false;
	}

	public boolean getDefaultGain(String ID, 
										 boolean checkAll,
										 String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).autoGain;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).autoGain;
		}
		return false;
	}
	
	
	public AbilityMapping getAllAbleMap(String ability)
	{
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return (AbilityMapping)ableMap.get(ability);
		}
		return null;
	}
	
	public boolean getSecretSkill(String ID, 
										 boolean checkAll,
										 String ability)
	{
		boolean secretFound=false;
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
            {
				if(!((AbilityMapping)ableMap.get(ability)).isSecret)
					return false;
				secretFound=true;
            }
		}
		if(checkAll)
		{
			AbilityMapping AB=getAllAbleMap(ability);
			if(AB!=null) return AB.isSecret;
		}
		return secretFound;
	}
	
	public boolean getAllSecretSkill(String ability)
	{
		AbilityMapping AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return false;
	}
	
	public boolean getSecretSkill(MOB mob,
										 String ability)
	{
		boolean secretFound=false;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			String charClass=mob.charStats().getMyClass(c).ID();
			if(completeAbleMap.containsKey(charClass))
			{
				Hashtable ableMap=(Hashtable)completeAbleMap.get(charClass);
				if(ableMap.containsKey(ability))
                {
					if(!((AbilityMapping)ableMap.get(ability)).isSecret)
						return false;
					secretFound=true;
                }
			}
		}
		if(completeAbleMap.containsKey(mob.charStats().getMyRace().ID()))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(mob.charStats().getMyRace().ID());
			if(ableMap.containsKey(ability))
            {
				if(!((AbilityMapping)ableMap.get(ability)).isSecret)
					return false;
				secretFound=true;
            }
		}
		AbilityMapping AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}
	
	public boolean getSecretSkill(String ability)
	{
		boolean secretFound=false;
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			String charClass=((CharClass)e.nextElement()).ID();
			if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
			{
				Hashtable ableMap=(Hashtable)completeAbleMap.get(charClass);
				if(ableMap.containsKey(ability))
                {
					if(!((AbilityMapping)ableMap.get(ability)).isSecret)
						return false;
					secretFound=true;
                }
			}
		}
		for(Enumeration e=CMClass.races();e.hasMoreElements();)
		{
			String ID=((Race)e.nextElement()).ID();
			if(completeAbleMap.containsKey(ID))
			{
				Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
				if(ableMap.containsKey(ability))
                {
					if(!((AbilityMapping)ableMap.get(ability)).isSecret)
						return false;
					secretFound=true;
                }
			}
		}
		AbilityMapping AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}
	
	public String getDefaultParm(String ID, 
										boolean checkAll,
										String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).defaultParm;
		}
		
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).defaultParm;
		}
		return "";
	}
	
	public int getDefaultProfficiency(String ID, 
											 boolean checkAll,
										     String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).defaultProfficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).defaultProfficiency;
		}
		return 0;
	}
}
