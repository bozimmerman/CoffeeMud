package com.planet_ink.coffee_mud.common;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class CMAble
{
	private int qualLevel=-1;
	private boolean autoGain=false;
	private int defaultProfficiency=0;
	private String defaultParm="";
								
	private static Hashtable classAbleMap=new Hashtable();
	private static Hashtable lowestQualifyingLevelMap=new Hashtable();
	
	public static void addCharAbilityMapping(String charClass, 
											 int qualLevel,
											 String ability, 
											 boolean autoGain)
	{
		addCharAbilityMapping(charClass,qualLevel,ability,0,"",autoGain);
	}
	public static void addCharAbilityMapping(String charClass, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 boolean autoGain)
	{
		addCharAbilityMapping(charClass,qualLevel,ability,0,"",autoGain);
	}
	public static void addCharAbilityMapping(String charClass, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
											 String defaultParam,
											 boolean autoGain)
	{
		if(!classAbleMap.containsKey(charClass))
			classAbleMap.put(charClass,new Hashtable());
		Hashtable ableMap=(Hashtable)classAbleMap.get(charClass);
		if(ableMap.containsKey(ability))
			ableMap.remove(ableMap.get(ability));
		CMAble able=new CMAble();
		able.qualLevel=qualLevel;
		able.autoGain=autoGain;
		able.defaultParm=defaultParam;
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
	
	public static Vector getLevelListings(String charClass, int level)
	{
		Vector V=new Vector();
		if(classAbleMap.containsKey(charClass))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get(charClass);
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				CMAble able=(CMAble)ableMap.get(key);
				if(able.qualLevel==level)
					V.addElement(key);
			}
		}
		if(classAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get("All");
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
	
	public static int getQualifyingLevel(String charClass, 
										 String ability)
	{
		if(classAbleMap.containsKey(charClass))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get(charClass);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).qualLevel;
		}
		if(classAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get("All");
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
			if((level>=0)&&(classLevel>=level)&&((level-classLevel)>greatestDiff))
			{
				greatestDiff=level-classLevel;
				theLevel=level;
			}
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
			if((level>=0)&&(classLevel>=level)&&((level-classLevel)>greatestDiff))
			{
				greatestDiff=level-classLevel;
				theLevel=level;
				theClass=C;
			}
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
		return false;
	}

	public static boolean getDefaultGain(String charClass, 
										 String ability)
	{
		if(classAbleMap.containsKey(charClass))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get(charClass);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).autoGain;
		}
		if(classAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).autoGain;
		}
		return false;
	}
	
	public static String getDefaultParm(String charClass, 
										String ability)
	{
		if(classAbleMap.containsKey(charClass))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get(charClass);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultParm;
		}
		if(classAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultParm;
		}
		return "";
	}
	
	public static int getDefaultProfficiency(String charClass, 
										     String ability)
	{
		if(classAbleMap.containsKey(charClass))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get(charClass);
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultProfficiency;
		}
		if(classAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)classAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((CMAble)ableMap.get(ability)).defaultProfficiency;
		}
		return 0;
	}
}
