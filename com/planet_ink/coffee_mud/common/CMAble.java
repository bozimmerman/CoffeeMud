package com.planet_ink.coffee_mud.common;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class CMAble
{
	public int qualLevel=-1;
	public boolean autoGain=false;
	public int defaultProfficiency=0;
								
	public static Hashtable classAbleMap=new Hashtable();
	public static Hashtable lowestQualifyingLevelMap=new Hashtable();
	
	public static void addCharAbilityMapping(String charClass, 
											 int qualLevel,
											 String ability, 
											 boolean autoGain)
	{
		addCharAbilityMapping(charClass,qualLevel,ability,0,autoGain);
	}
	public static void addCharAbilityMapping(String charClass, 
											 int qualLevel,
											 String ability, 
											 int defaultProfficiency,
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
		ableMap.put(ability,able);
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
	
	public static int lowestQualifyingLevel(String ability)
	{
		if(lowestQualifyingLevelMap.containsKey(ability))
			return ((Integer)lowestQualifyingLevelMap.get(ability)).intValue();
		
		if(CMClass.charClasses.size()==0) return -1;
		
		int lowestQualifyingLevel=Integer.MAX_VALUE;
		if(CMClass.charClasses.size()>0)
		{
			for(int c=0;c<CMClass.charClasses.size();c++)
			{
				int lvl=getQualifyingLevel(((CharClass)CMClass.charClasses.elementAt(c)).ID(),ability);
				if((lvl>=0)&&(lvl<lowestQualifyingLevel))
					lowestQualifyingLevel=lvl;
			}
		}
		if(lowestQualifyingLevel==Integer.MAX_VALUE)
			lowestQualifyingLevel=-1;
		lowestQualifyingLevelMap.put(ability,new Integer(lowestQualifyingLevel));
		return lowestQualifyingLevel;
	}
}
