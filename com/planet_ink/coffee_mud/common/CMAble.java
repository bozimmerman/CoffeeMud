package com.planet_ink.coffee_mud.common;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class CMAble
{
	public String abilityName="";
	public int qualLevel=-1;
	public boolean autoGain=false;
	public int defaultProfficiency=0;
	public String defaultParm="";
	public boolean isSecret=false;
								
	private static Hashtable completeAbleMap=new Hashtable();
	private static Hashtable lowestQualifyingLevelMap=new Hashtable();
	
	public static void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 boolean autoGain)
	{
		addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false);
	}
	public static void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 String defParm,
											 boolean autoGain)
	{
		addCharAbilityMapping(ID,qualLevel,ability,0,defParm,autoGain,false);
	}
	
	public static void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 boolean autoGain)
	{
		addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false);
	}
	
	public static void delCharAbilityMapping(String ID,
											 String ability)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new Hashtable());
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		if(ableMap.containsKey(ability))
			ableMap.remove(ability);
	}
	public static void delCharMappings(String ID)
	{
		if(completeAbleMap.containsKey(ID))
			completeAbleMap.remove(ID);
	}
	
	public static Enumeration getClassAbles(String ID)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new Hashtable());
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		return ableMap.elements();
	}
	
	public static void addCharAbilityMapping(String ID, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 String defaultParam,
											 boolean autoGain,
											 boolean secret)
	{
		delCharAbilityMapping(ID,ability);
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		CMAble able=new CMAble();
		able.abilityName=ability;
		able.qualLevel=qualLevel;
		able.autoGain=autoGain;
		able.isSecret=secret;
		able.defaultParm=defaultParam;
		able.defaultProfficiency=defaultProfficiency;
		ableMap.put(ability,able);
		int arc_level=getQualifyingLevel("Archon",ability);
		if((arc_level<0)||((qualLevel>=0)&&(qualLevel<arc_level)))
			addCharAbilityMapping("Archon",qualLevel,ability,true);
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(ability);
		if((lowLevel==null)
		||(qualLevel<lowLevel.intValue()))
			lowestQualifyingLevelMap.put(ability,new Integer(qualLevel));
	}
	
	public static int lowestQualifyingLevel(String ability)
	{
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(ability);
		if(lowLevel==null) return 0;
		return lowLevel.intValue();
	}
	
	public static Vector getLevelListings(String ID, int level)
	{
		Vector V=new Vector();
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				CMAble able=(CMAble)ableMap.get(key);
				if(able.qualLevel==level)
					V.addElement(key);
			}
		}
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				CMAble able=(CMAble)ableMap.get(key);
				if(able.qualLevel==level)
					V.addElement(key);
			}
		}
		return V;
	}
	public static Vector getUpToLevelListings(String ID, 
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
				CMAble able=(CMAble)ableMap.get(key);
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
				CMAble able=(CMAble)ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
					V.addElement(key);
			}
		}
		return V;
	}
	
	public static int getQualifyingLevel(String ID, 
										 String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).qualLevel;
		}
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).qualLevel;
		}
		return -1;
	}

	
	public static int qualifyingLevel(MOB student, Ability A)
	{
		if(student==null) return -1;
		int theLevel=-1;
		int greatestDiff=-1;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=CMAble.getQualifyingLevel(C.ID(),A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theLevel=level;
			}
		}
		int level=CMAble.getQualifyingLevel(student.charStats().getMyRace().ID(),A.ID());
		int classLevel=student.baseEnvStats().level();
		if((level>=0)
		&&(classLevel>=level)
		&&((classLevel-level)>greatestDiff))
		{
			greatestDiff=classLevel-level;
			theLevel=level;
		}
		if(theLevel<0) 
			return CMAble.getQualifyingLevel(student.charStats().getCurrentClass().ID(),A.ID());
		else
			return theLevel;
	}

	public static int qualifyingClassLevel(MOB student, Ability A)
	{
		if(student==null) return -1;
		int theLevel=-1;
		int greatestDiff=-1;
		CharClass theClass=null;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=CMAble.getQualifyingLevel(C.ID(),A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theLevel=level;
				theClass=C;
			}
		}
		int level=CMAble.getQualifyingLevel(student.charStats().getMyRace().ID(),A.ID());
		int classLevel=student.baseEnvStats().level();
		if((level>=0)
		&&(classLevel>=level)
		&&((classLevel-level)>greatestDiff))
		{
			greatestDiff=classLevel-level;
			theLevel=level;
		}
		if(theClass==null) 
			return student.charStats().getClassLevel(student.charStats().getCurrentClass());
		else
			return student.charStats().getClassLevel(theClass);
	}

	
	public static boolean qualifiesByLevel(MOB student, Ability A)
	{
		if(student==null) return false;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=CMAble.getQualifyingLevel(C.ID(),A.ID());
			if((level>=0)
			&&(student.charStats().getClassLevel(C)>=level))
				return true;
		}
		int level=CMAble.getQualifyingLevel(student.charStats().getMyRace().ID(),A.ID());
		if((level>=0)
		&&(student.charStats().getClassLevel(student.charStats().getMyRace().ID())>=level))
			return true;
		return false;
	}

	public static boolean getDefaultGain(String ID, 
										 String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).autoGain;
		}
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).autoGain;
		}
		return false;
	}
	
	
	public static CMAble getAllAbleMap(String ability)
	{
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return (CMAble)ableMap.get(ability);
		}
		return null;
	}
	
	public static boolean getSecretSkill(String ID, 
										 String ability)
	{
		boolean secretFound=false;
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				if(!((CMAble)ableMap.get(ability)).isSecret)
					return false;
				else
					secretFound=true;
		}
		CMAble AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}
	
	public static boolean getAllSecretSkill(String ability)
	{
		CMAble AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return false;
	}
	
	public static boolean getSecretSkill(MOB mob,
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
					if(!((CMAble)ableMap.get(ability)).isSecret)
						return false;
					else
						secretFound=true;
			}
		}
		if(completeAbleMap.containsKey(mob.charStats().getMyRace().ID()))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(mob.charStats().getMyRace().ID());
			if(ableMap.containsKey(ability))
				if(!((CMAble)ableMap.get(ability)).isSecret)
					return false;
				else
					secretFound=true;
		}
		CMAble AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}
	
	public static boolean getSecretSkill(String ability)
	{
		boolean secretFound=false;
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			String charClass=((CharClass)e.nextElement()).ID();
			if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
			{
				Hashtable ableMap=(Hashtable)completeAbleMap.get(charClass);
				if(ableMap.containsKey(ability))
					if(!((CMAble)ableMap.get(ability)).isSecret)
						return false;
					else
						secretFound=true;
			}
		}
		for(Enumeration e=CMClass.races();e.hasMoreElements();)
		{
			String ID=((Race)e.nextElement()).ID();
			if(completeAbleMap.containsKey(ID))
			{
				Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
				if(ableMap.containsKey(ability))
					if(!((CMAble)ableMap.get(ability)).isSecret)
						return false;
					else
						secretFound=true;
			}
		}
		CMAble AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}
	
	public static String getDefaultParm(String ID, 
										String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultParm;
		}
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultParm;
		}
		return "";
	}
	
	public static int getDefaultProfficiency(String ID, 
										     String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultProfficiency;
		}
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultProfficiency;
		}
		return 0;
	}
}
