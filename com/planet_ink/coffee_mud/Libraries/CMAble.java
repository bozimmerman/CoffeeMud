package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
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
   Copyright 2000-2010 Bo Zimmerman

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
public class CMAble extends StdLibrary implements AbilityMapper
{
    public String ID(){return "CMAble";}
    
    protected Map<String, Map<String, AbilityMapping>> 
										completeAbleMap 			= new SHashtable<String, Map<String, AbilityMapping>>();
	protected Map<String, Integer> 		lowestQualifyingLevelMap	= new SHashtable<String, Integer>();
	protected Map<String, Integer>		maxProficiencyMap			= new SHashtable<String, Integer>();
	protected Map<String, Object>		allows						= new SHashtable<String, Object>();
	protected Map<Integer, Set<Integer>>completeDomainMap			= new SHashtable<Integer,Set<Integer>>();
    protected Map<String, Map<String, AbilityMapping>> 
    									reverseAbilityMap			= new TreeMap<String, Map<String, AbilityMapping>>();
    protected List<AbilityMapping>		eachClassSet				= null;

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,100,"",autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,100,"",autoGain,false,new Vector(),extraMasks); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  boolean autoGain,
									  List<String> skillPreReqs)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,100,"",autoGain,false,skillPreReqs,""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  boolean autoGain,
									  List<String> skillPreReqs,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,100,"",autoGain,false,skillPreReqs,extraMasks); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  String defParm,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,defParm,autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  String defParm,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,defParm,autoGain,false,new Vector(),extraMasks); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,"",autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,"",autoGain,false,new Vector(),extraMasks); }

	public int numMappedAbilities()
	{
		return reverseAbilityMap.size();
	}
	
	public void delCharAbilityMapping(String ID, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String, AbilityMapping> ableMap = completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				ableMap.remove(ability);
		}
		Map<String,AbilityMapping> revT=reverseAbilityMap.get(ability);
		if(revT!=null) revT.remove(ID);
	}
	public void delCharMappings(String ID)
	{
		if(completeAbleMap.containsKey(ID))
			completeAbleMap.remove(ID);
		for(String ability : reverseAbilityMap.keySet())
		{
			Map<String,AbilityMapping> revT=reverseAbilityMap.get(ability);
			if(revT!=null) revT.remove(ID);
		}
	}

	public Enumeration<AbilityMapping> getClassAbles(String ID, boolean addAll)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new SHashtable<String, AbilityMapping>());
		Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		Map<String, AbilityMapping> allAbleMap=completeAbleMap.get("All");
		if((!addAll)||(allAbleMap==null)) 
			return new IteratorEnumeration<AbilityMapping>(ableMap.values().iterator());
		Iterator[] iters=new Iterator[]{ableMap.values().iterator(),allAbleMap.values().iterator()};
		return new IteratorEnumeration(new MultiIterator(iters));
	}
	
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,defaultParam,autoGain,secret,new Vector(),"");}
	
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,defaultParam,autoGain,secret,new Vector(),extraMasks);}
	
	
	public void addCharAbilityMapping(String ID,
							          int qualLevel,
							          String ability,
							          int defaultProficiency,
							          String defaultParam,
							          boolean autoGain,
							          boolean secret,
							          List<String> preReqSkillsList,
							          String extraMask)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);}

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,new Vector(),"");}
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,new Vector(),extraMasks);}

	public void addRaceAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ 
		delCharAbilityMapping(ID,ability);
    	if(CMSecurity.isDisabled("ABILITY_"+ID.toUpperCase())) return;
    	Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		if(ableMap == null)
		{
			ableMap=new SHashtable<String,AbilityMapping>();
			completeAbleMap.put(ID,ableMap);
		}
		AbilityMapping able = makeAbilityMapping(ID,qualLevel,ability,defaultProficiency,100,defaultParam,autoGain,secret, new Vector(),"",null);
		addClassAbility(ability,ableMap,able);
	}

    public void addCharAbilityMapping(String ID,
                                      int qualLevel,
                                      String ability,
                                      int defaultProficiency,
                                      int maxProficiency,
                                      String defaultParam,
                                      boolean autoGain,
                                      boolean secret,
                                      List<String> preReqSkillsList,
                                      String extraMask)
    { addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);}

	
	public void addPreRequisites(String ID, List<String> preReqSkillsList, String extraMask)
	{
		if(preReqSkillsList==null) return;
		for(int v=0;v<preReqSkillsList.size();v++)
		{
			String s=(String)preReqSkillsList.get(v);
			int x=s.indexOf('(');
			if((x>=0)&&(s.endsWith(")")))
				s=s.substring(0,x);
			if((s.indexOf('*')>=0)||(s.indexOf(',')>=0))
			{
				String ID2=ID;
				while(allows.containsValue("*"+ID2))
					ID2="*"+ID2;
				allows.put("*"+ID2,s);
			}
			else
			{
				SVector V=(SVector)allows.get(s);
				if(V==null){ V=new SVector(); allows.put(s,V);}
				if(!V.contains(ID))V.addElement(ID);
			}
		}
		if((extraMask!=null)&&(extraMask.trim().length()>0))
		{
			Vector preReqsOf=CMLib.masking().getAbilityEduReqs(extraMask);
			for(int v=0;v<preReqsOf.size();v++)
			{
				String s=(String)preReqsOf.elementAt(v);
				if((s.indexOf('*')>=0)||(s.indexOf(',')>=0))
				{
					String ID2=ID;
					while(allows.containsValue("*"+ID2))
						ID2="*"+ID2;
					allows.put("*"+ID2,s);
				}
				else
				{
					SVector V=(SVector)allows.get(s);
					if(V==null)
					{
						V=new SVector();
						allows.put(s,V);
					}
					if(!V.contains(ID))
						V.addElement(ID);
				}
			}
		}
	}

    public boolean isDomainIncludedInAnyAbility(int domain, int acode)
    {
        STreeSet<Integer> V=(STreeSet<Integer>)completeDomainMap.get(Integer.valueOf(domain));
        if(V==null)
        {
            Ability A=null;
            V=new STreeSet<Integer>();
            for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
            {
                A=(Ability)e.nextElement();
                if(((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
                &&(!V.contains(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)))))
                    V.add(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)));
            }
            completeDomainMap.put(Integer.valueOf(domain),V);
        }
        return V.contains(Integer.valueOf(acode));
    }

    public List<QualifyingID> getClassAllowsList(String classID)
    {
    	List<AbilityMapping> ABLES=getUpToLevelListings(classID,CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL),false,false);
        SHashtable alreadyDone=new SHashtable();
        List<QualifyingID> DV=new Vector<QualifyingID>(2);
        Integer Ix=null;
        for(AbilityMapping able : ABLES)
        {
            for(Iterator<String> i=getAbilityAllowsList(able.abilityName);i.hasNext();)
            {
            	String s = i.next();
            	Ix=(Integer)alreadyDone.get(s);
            	if(Ix==null)
            	{
	                alreadyDone.put(s, Integer.valueOf(DV.size()));
	                DV.add(new QualifyingID(s,able.qualLevel));
            	}
            	else
            	{
            		QualifyingID Q=DV.get(Ix.intValue());
	            	if((Q!=null)&&(Q.qualifyingLevel>able.qualLevel))
	            		Q.qualifyingLevel=able.qualLevel;
            	}
            }
        }
        return DV;
    }

	public Iterator<String> getAbilityAllowsList(String ableID)
	{
		String abilityID=null;
		Vector remove=null;
		for(String KEYID : allows.keySet())
		{
			if(KEYID.startsWith("*"))
			{
				abilityID=(String)allows.get(KEYID);
				if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(',')>0))
				{
					List<String> orset=getOrSet(ableID,abilityID);
					if(orset.size()!=0)
					{
						String KEYID2=KEYID;
						while(KEYID2.startsWith("*")) KEYID2=KEYID2.substring(1);
						addPreRequisites(KEYID2,orset,"");
						if(remove==null) remove=new Vector();
						remove.addElement(KEYID);
					}
				}
			}
		}
		if(remove!=null)
		{
			for(int r=0;r<remove.size();r++)
				allows.remove(remove.elementAt(r));
		}
		SVector<String> set = (SVector<String>)allows.get(ableID); 
		return (set==null)?new SVector<String>(1).iterator():set.iterator();
	}

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  List<String> preReqSkillsList,
									  String extraMask,
                                      Integer[] costOverrides)
	{
		delCharAbilityMapping(ID,ability);
    	if(CMSecurity.isDisabled("ABILITY_"+ID.toUpperCase())) return;
    	Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		if(ableMap == null)
		{
			ableMap=new SHashtable<String,AbilityMapping>();
			completeAbleMap.put(ID,ableMap);
			handleEachAndClassAbility(ableMap, ID);
		}
		AbilityMapping able = makeAbilityMapping(ID,qualLevel,ability,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,preReqSkillsList,extraMask,costOverrides);
		addClassAbility(ability,ableMap,able);
	}

	public AbilityMapping makeAbilityMapping(String ID,
											 int qualLevel,
											 String ability,
											 int defaultProficiency,
											 int maxProficiency,
											 String defaultParam,
											 boolean autoGain,
											 boolean secret,
											 List<String> preReqSkillsList,
											 String extraMask,
								             Integer[] costOverrides)
	{
		AbilityMapping able=new AbilityMapping(ID);
		able.abilityName=ability;
		able.qualLevel=qualLevel;
		able.autoGain=autoGain;
		able.isSecret=secret;
		able.defaultParm=defaultParam==null?"":defaultParam;
		able.defaultProficiency=defaultProficiency;
		able.maxProficiency=maxProficiency;
		able.extraMask=extraMask==null?"":extraMask;
        able.costOverrides=costOverrides;
        able.originalSkillPreReqList=CMParms.toStringList(preReqSkillsList);

		able.skillPreReqs=new DVector(2);
		addPreRequisites(ability,preReqSkillsList,extraMask);
		if(preReqSkillsList!=null)
			for(int v=0;v<preReqSkillsList.size();v++)
			{
				String s=(String)preReqSkillsList.get(v);
				int prof=0;
				int x=s.indexOf('(');
				if((x>=0)&&(s.endsWith(")")))
				{
					prof=CMath.s_int(s.substring(x+1,s.length()-1));
					s=s.substring(0,x);
				}
				able.skillPreReqs.addElement(s,Integer.valueOf(prof));
			}
		return able;
	}

	public void addClassAbility(String ability, Map<String, AbilityMapping> ableMap, AbilityMapping able)
	{
    	if(CMSecurity.isDisabled("ABILITY_"+able.abilityName.toUpperCase())) return;
		ableMap.put(ability,able);

		int qualLevel = able.qualLevel;
		int maxProficiency = able.maxProficiency;
		
		// set lowest level map
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(ability);
		if((lowLevel==null)
		||(qualLevel<lowLevel.intValue()))
			lowestQualifyingLevelMap.put(ability,Integer.valueOf(qualLevel));
		
		// and max proficiency maps
		Integer maxProf=(Integer)maxProficiencyMap.get(ability);
		if((maxProf==null)
		||(maxProficiency>maxProf.intValue()))
			maxProficiencyMap.put(ability,Integer.valueOf(maxProficiency));
		
		// and the reverse lookup map
		Map<String, AbilityMapping> revT = reverseAbilityMap.get(ability);
		if(revT==null)
		{
			revT = new TreeMap<String, AbilityMapping>();
			reverseAbilityMap.put(ability,revT);
		}
		if(!revT.containsKey(able.ID)) 
			revT.put(able.ID, able);
		
		// archons get everything
		int arc_level=getQualifyingLevel("Archon",true,ability);
		if(((arc_level<0)||((qualLevel>=0)&&(qualLevel<arc_level)))
		&&(!able.ID.equalsIgnoreCase("Archon"))
		&&(!able.ID.equalsIgnoreCase("All")))
			addCharAbilityMapping("Archon",qualLevel,ability,true);
	}
	

	public synchronized void handleEachAndClassAbility(Map<String, AbilityMapping> ableMap, String ID)
	{
		if(eachClassSet == null)
		{
			eachClassSet = new SLinkedList<AbilityMapping>();
			CMFile f = new CMFile(Resources.makeFileResourceName("skills/allqualifylist.txt"),null,false);
			if(f.exists() && f.canRead())
			{
				List<String> list = Resources.getFileLineVector(f.text());
				boolean eachMode = false;
				for(String s : list)
				{
					s=s.trim();
					if(s.equalsIgnoreCase("[EACH]"))
						eachMode=true;
					else
					if(s.equalsIgnoreCase("[ALL]"))
						eachMode=false;
					else
					if(s.startsWith("#")||s.length()==0)
						continue;
					else
					{
						int x=s.indexOf(' ');
						if(x<0) continue;
						String lvlStr = s.substring(0,x).trim();
						if(!CMath.isInteger(lvlStr))
							continue;
						s=s.substring(x+1).trim();
						int qualLevel=CMath.s_int(lvlStr);
						x=s.indexOf(' ');
						String abilityID;
						StringBuilder mask=new StringBuilder("");
						StringBuilder preReqs=new StringBuilder("");
						StringBuilder prof=new StringBuilder("");
						boolean autogain=false;
						if(x<0)
							abilityID=s;
						else
						{
							abilityID=s.substring(0,x).trim();
							s=s.substring(x+1).trim();
							String us=s.toUpperCase();
							int lastC=' ';
							StringBuilder cur=null;
							for(int i=0;i<s.length();i++)
							{
								if((lastC==' ')&&(Character.isLetter(us.charAt(i))))
								{
									String ss=us.substring(i);
									if(ss.startsWith("MASK="))
									{
										cur=mask;
										i+=4;
									}
									else
									if(ss.startsWith("PROF="))
									{
										cur=prof;
										i+=4;
									}
									else
									if(ss.startsWith("REQUIRES="))
									{
										cur=preReqs;
										i+=8;
									}
									else
									if(ss.startsWith("AUTOGAIN "))
									{
										cur=null;
										autogain=true;
										i+=8;
									}
									else
									if(ss.startsWith("AUTOGAIN") 
									&& (ss.length()==8))
									{
										cur=null;
										autogain=true;
										break;
									}
									else
									if(cur!=null)
										cur.append(s.charAt(i));
								}
								else
								if(cur!=null)
									cur.append(s.charAt(i));
								lastC=s.charAt(i);
							}
						}
				    	if(CMSecurity.isDisabled("ABILITY_"+abilityID.toUpperCase())) continue;
						AbilityMapping able = 
							makeAbilityMapping(ID,qualLevel,abilityID,CMath.s_int(prof.toString().trim()),100,"",autogain,false,
									CMParms.parseSpaces(preReqs.toString().trim(), true), mask.toString().trim(),null);
						if(eachMode)
							eachClassSet.add(able);
						else
						{
							able.ID="All";
					    	Map<String, AbilityMapping> allMap=completeAbleMap.get("All");
							if(allMap == null)
							{
								allMap=new SHashtable<String,AbilityMapping>();
								completeAbleMap.put("All",allMap);
							}
							addClassAbility(able.abilityName, allMap, able);
						}
					}
				}
			}
		}
		for(AbilityMapping able : eachClassSet)
			addClassAbility(able.abilityName, ableMap, able);
	}
	
	
	public boolean qualifiesByAnyCharClass(String abilityID)
	{
		if(completeAbleMap.containsKey("All"))
		{
			Map<String, AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return true;
		}
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			CharClass C=(CharClass)e.nextElement();
			if(completeAbleMap.containsKey(C.ID()))
			{
				Map<String, AbilityMapping> ableMap=completeAbleMap.get(C.ID());
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
			Map<String, AbilityMapping> ableMap=completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID))
				return false;
		}
		else
			return false;
		for(String key : completeAbleMap.keySet())
		{
			if((!key.equalsIgnoreCase(classID))
			&&(completeAbleMap.get(classID).containsKey(abilityID)))
				return false;
		}
		return true;
	}


	public boolean classOnly(MOB mob, String classID, String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			Map<String, AbilityMapping> ableMap=completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID))
				return false;
		}
		else
			return false;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			CharClass C=mob.charStats().getMyClass(c);
			if((!C.ID().equals(classID))
			&&(completeAbleMap.containsKey(classID))
			&&(completeAbleMap.get(classID).containsKey(abilityID)))
				return false;
		}
		return true;
	}


	public boolean availableToTheme(String abilityID, int theme, boolean publicly)
	{
		for(String key : completeAbleMap.keySet())
		{
			if(completeAbleMap.get(key).containsKey(abilityID))
			{
				if(key.equalsIgnoreCase("All")) return true;
				CharClass C=CMClass.getCharClass(key);
				if((C!=null)
				&&((C.availabilityCode()&theme)==theme)
				&&((!publicly)||((C.availabilityCode()&Area.THEME_SKILLONLYMASK)==0)))
					return true;
			}
		}
		return false;
	}

	public List<String> getLevelListings(String ID, boolean checkAll, int level)
	{
		List<String> V=new Vector<String>();
        CharClass C=CMClass.getCharClass(ID);
        if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
            return V;
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if(able.qualLevel==level)
					V.add(key);
			}
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if((able.qualLevel==level)
				&&(!V.contains(key)))
					V.add(key);
			}
		}
		return V;
	}
	public List<AbilityMapping> getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly)
	{
		List<AbilityMapping> DV=new Vector<AbilityMapping>();
        CharClass C=CMClass.getCharClass(ID);
        if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
        	level=C.getLevelCap();
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
					DV.add(able);
			}
		}
		if((completeAbleMap.containsKey("All"))&&(!ignoreAll))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			for(String key : ableMap.keySet())
			{
				AbilityMapping able=ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
				{
					boolean found=false;
					for(AbilityMapping A : DV)
						if(A.ID.equalsIgnoreCase(key))
						{
							found=true;
							break;
						}
					if(!found)
						DV.add(able);
				}
			}
		}
		return DV;
	}

	public int getQualifyingLevel(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
                return ableMap.get(ability).qualLevel;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
            {
                int qualLevel = ableMap.get(ability).qualLevel;
                CharClass C=CMClass.getCharClass(ID);
                if((C!=null)&&(C.getLevelCap()>=0))
                    return qualLevel>C.getLevelCap()?-1:qualLevel;
                return qualLevel;
            }
		}
		return -1;
	}

	protected List<String> getOrSet(String errStr, String abilityID)
	{
		Ability preA=null;
		List<String> orset=new Vector();
		List<String> preorset=CMParms.parseCommas(abilityID,true);
		for(int p=0;p<preorset.size();p++)
		{
			abilityID=(String)preorset.get(p);
			if(abilityID.startsWith("*"))
			{
				String a=abilityID.substring(1).toUpperCase();
				for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=(Ability)e.nextElement();
					if(preA.ID().toUpperCase().endsWith(a))
						orset.add(preA.ID());
				}
			}
			else
			if(abilityID.endsWith("*"))
			{
				String a=abilityID.substring(0,abilityID.length()-1).toUpperCase();
				for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=(Ability)e.nextElement();
					if(preA.ID().toUpperCase().startsWith(a))
						orset.add(preA.ID());
				}
			}
			else
				orset.add(abilityID);
		}
		for(int o=orset.size()-1;o>=0;o--)
		{
			abilityID=(String)orset.get(o);
			preA=CMClass.getAbility(abilityID);
			if(preA==null)
			{
				preA=CMClass.findAbility(abilityID);
				if(preA!=null)
					orset.set(o,preA.ID());
				else
				{
					Log.errOut("CMAble","Skill "+errStr+" requires nonexistant skill "+abilityID+".");
					orset.clear();
					break;
				}
			}
		}
		return orset;
	}

	public void fillPreRequisites(Ability A, DVector rawPreReqs)
	{
		for(int v=0;v<rawPreReqs.size();v++)
		{
			String abilityID=(String)rawPreReqs.elementAt(v,1);
			if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(',')>0))
			{
				List<String> orset=getOrSet(A.ID(),abilityID);
				if(orset.size()!=0)
					rawPreReqs.setElementAt(v,1,orset);
			}
			else
			{
				Ability otherAbility=CMClass.getAbility(abilityID);
				if(otherAbility==null)
				{
					otherAbility=CMClass.findAbility(abilityID);
					if(otherAbility!=null)
						rawPreReqs.setElementAt(v,1,otherAbility.ID());
					else
					{
						Log.errOut("CMAble","Skill "+A.ID()+" requires nonexistant skill "+abilityID+".");
						break;
					}
				}
			}
		}
	}

	public DVector getCommonPreRequisites(Ability A)
	{
		DVector preReqs=null;
		{
			Map<String,AbilityMapping> ableMap=null;
			if(completeAbleMap.containsKey("All"))
			{
				ableMap=completeAbleMap.get("All");
				if(ableMap.containsKey(A.ID()))
					preReqs=ableMap.get(A.ID()).skillPreReqs;
			}
		}
		if(preReqs==null)
			for(Map<String,AbilityMapping> ableMap : completeAbleMap.values())
				if(ableMap.containsKey(A.ID()))
				{
					preReqs=ableMap.get(A.ID()).skillPreReqs;
					if((preReqs!=null)&&(preReqs.size()>0)) break;
				}
		if((preReqs==null)||(preReqs.size()==0)) return new DVector(2);
		DVector reqs=preReqs.copyOf();
		fillPreRequisites(A,reqs);
		return reqs;

	}

	public String getCommonExtraMask(Ability A)
	{
		String mask=null;
		{
			Map<String,AbilityMapping> ableMap=null;
			if(completeAbleMap.containsKey("All"))
			{
				ableMap=completeAbleMap.get("All");
				if(ableMap.containsKey(A.ID()))
					mask=ableMap.get(A.ID()).extraMask;
			}
		}
		if((mask==null)||(mask.length()==0))
			for(Map<String,AbilityMapping> ableMap : completeAbleMap.values())
				if(ableMap.containsKey(A.ID()))
				{
					mask=ableMap.get(A.ID()).extraMask;
					if((mask!=null)&&(mask.length()>0)) break;
				}
		if((mask==null)||(mask.length()==0)) return "";
		return mask;
	}

	
	public DVector getUnmetPreRequisites(MOB student, Ability A)
	{
		DVector V=getRawPreRequisites(student,A);
		if((V==null)||(V.size()==0)) return new DVector(2);
		fillPreRequisites(A,V);
		String abilityID=null;
		Integer prof=null;
		Ability A2=null;
		for(int v=V.size()-1;v>=0;v--)
		{
			prof=(Integer)V.elementAt(v,2);
			if(V.elementAt(v,1) instanceof String)
			{
				abilityID=(String)V.elementAt(v,1);
				A2=student.fetchAbility(abilityID);
				if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					V.removeElementAt(v);
				else
				if(!qualifiesByLevel(student,abilityID))
					V.removeElementAt(v);
			}
			else
			{
				List<String> orset=(List<String>)V.elementAt(v,1);
				for(int o=orset.size()-1;o>=0;o--)
				{
					abilityID=(String)orset.get(o);
					A2=student.fetchAbility(abilityID);
					if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					{
						orset.clear();
						break;
					}
					if(!qualifiesByLevel(student,abilityID))
						orset.remove(o);
				}
				if(orset.size()==0)
					V.removeElementAt(v);
			}
		}
		return V;
	}

	public DVector getPreReqs(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).skillPreReqs;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).skillPreReqs;
		}
		return null;
	}

	
	public String formatPreRequisites(DVector preReqs)
	{
		StringBuffer names=new StringBuffer("");
		if((preReqs!=null)&&(preReqs.size()>0))
		{
			Integer prof=null;
			for(int p=0;p<preReqs.size();p++)
			{
				prof=(Integer)preReqs.elementAt(p,2);
				if(preReqs.elementAt(p,1) instanceof List)
				{
					List V=(List)preReqs.elementAt(p,1);
					names.append("(One of: ");
					for(int v=0;v<V.size();v++)
					{
						Ability A=CMClass.getAbility((String)V.get(v));
						if(A!=null)
						{
							names.append("'"+A.name()+"'");
							if(V.size()>1)
							{
								if(v==(V.size()-2))
									names.append(", or ");
								else
								if(v<V.size()-2)
									names.append(", ");
							}
						}
					}
					if(prof.intValue()>0)
						names.append(" at "+prof+"%)");
					else
						names.append(")");
				}
				else
				{
					Ability A=CMClass.getAbility((String)preReqs.elementAt(p,1));
					if(A!=null)
					{
						names.append("'"+A.name()+"'");
						if(prof.intValue()>0)
							names.append(" at "+prof+"%");
					}
				}
				if(preReqs.size()>1)
				{
					if(p==(preReqs.size()-2))
						names.append(", and ");
					else
					if(p<preReqs.size()-2)
						names.append(", ");
				}
			}
		}
		return names.toString();
	}

	public DVector getRawPreRequisites(MOB student, Ability A)
	{
		if(student==null) return new DVector(2);
		DVector reqs=null;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)&&(classLevel>=level))
			{
				reqs=getPreReqs(C.ID(),true,A.ID());
				if(reqs!=null) return reqs.copyOf();
			}
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		int classLevel=student.basePhyStats().level();
		if((level>=0)&&(classLevel>=level))
		{
			reqs=getPreReqs(student.charStats().getMyRace().ID(),false,A.ID());
			if(reqs!=null) return reqs.copyOf();
		}
		reqs=getPreReqs(student.charStats().getCurrentClass().ID(),true,A.ID());
		return (reqs==null)?new DVector(2):reqs.copyOf();
	}

	public String getExtraMask(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).extraMask;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).extraMask;
		}
		return null;
	}

	public String getApplicableMask(MOB student, Ability A)
	{
		if(student==null) return "";
		String mask=null;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,A.ID());
			int classLevel=student.charStats().getClassLevel(C);
			if((level>=0)&&(classLevel>=level))
			{
				mask=getExtraMask(C.ID(),true,A.ID());
				if(mask!=null) return mask;
			}
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,A.ID());
		int classLevel=student.basePhyStats().level();
		if((level>=0)&&(classLevel>=level))
		{
			mask=getExtraMask(student.charStats().getMyRace().ID(),false,A.ID());
			if(mask!=null) return mask;
		}
		mask=getExtraMask(student.charStats().getCurrentClass().ID(),true,A.ID());
		return mask==null?"":mask;
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
		int classLevel=student.basePhyStats().level();
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

	public int qualifyingClassLevel(MOB student, Ability A)
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
		int classLevel=student.basePhyStats().level();
		if((level>=0)
		&&(classLevel>=level)
		&&((classLevel-level)>greatestDiff))
			greatestDiff=classLevel-level;
		if(theClass==null)
			return student.charStats().getClassLevel(student.charStats().getCurrentClass());
		return student.charStats().getClassLevel(theClass);
	}

	public Object lowestQualifyingClassRace(MOB student, Ability A)
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
		&&((theClass==null)||((student.basePhyStats().level()>=level)&&(theLevel>level))))
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
		if((level>=0)&&(student.phyStats().level()>=level))
			return true;
		return false;
	}

	public boolean qualifiesByLevel(MOB student, Ability A){return (A==null)?false:qualifiesByLevel(student,A.ID());}
	public boolean qualifiesByLevel(MOB student, String ability)
	{
		if(student==null) return false;
		for(int c=student.charStats().numClasses()-1;c>=0;c--)
		{
			CharClass C=student.charStats().getMyClass(c);
			int level=getQualifyingLevel(C.ID(),true,ability);
			if((level>=0)
			&&(student.charStats().getClassLevel(C)>=level))
				return true;
		}
		int level=getQualifyingLevel(student.charStats().getMyRace().ID(),false,ability);
		if((level>=0)&&(student.phyStats().level()>=level))
			return true;
		return false;
	}

	public boolean getDefaultGain(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).autoGain;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).autoGain;
		}
		return false;
	}

    public AbilityMapping getAbleMap(String ID, String ability)
    {
        if(completeAbleMap.containsKey(ID))
        {
        	Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
            if(ableMap.containsKey(ability))
                return ableMap.get(ability);
        }
        return null;
    }
	public AbilityMapping getAllAbleMap(String ability){ return getAbleMap("All",ability);}

	public boolean getSecretSkill(String ID, boolean checkAll, String ability)
	{
		boolean secretFound=false;
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
            {
				if(!ableMap.get(ability).isSecret)
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

	public boolean getSecretSkill(MOB mob, String ability)
	{
		boolean secretFound=false;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			String charClass=mob.charStats().getMyClass(c).ID();
			if(completeAbleMap.containsKey(charClass))
			{
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if(ableMap.containsKey(ability))
                {
					if(!ableMap.get(ability).isSecret)
						return false;
					secretFound=true;
                }
			}
		}
		if(completeAbleMap.containsKey(mob.charStats().getMyRace().ID()))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(mob.charStats().getMyRace().ID());
			if(ableMap.containsKey(ability))
            {
				if(!ableMap.get(ability).isSecret)
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
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if(ableMap.containsKey(ability))
                {
					if(!ableMap.get(ability).isSecret)
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
				Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if(ableMap.containsKey(ability))
                {
					if(!ableMap.get(ability).isSecret)
						return false;
					secretFound=true;
                }
			}
		}
		AbilityMapping AB=getAllAbleMap(ability);
		if(AB!=null) return AB.isSecret;
		return secretFound;
	}

    public Integer[] getCostOverrides(String ID, boolean checkAll, String ability)
    {
    	Integer[] found=null;
        if(completeAbleMap.containsKey(ID))
        {
        	Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
            if(ableMap.containsKey(ability))
                found=ableMap.get(ability).costOverrides;
        }
        if((checkAll)&&(found==null))
        {
            AbilityMapping AB=getAllAbleMap(ability);
            if(AB!=null) found=AB.costOverrides;
        }
        return found;
    }

    public Integer[] getAllCostOverrides(String ability)
    {
        AbilityMapping AB=getAllAbleMap(ability);
        if(AB!=null) return AB.costOverrides;
        return null;
    }

    public Integer[] getCostOverrides(MOB mob, String ability)
    {
    	Integer[] found=null;
        for(int c=0;c<mob.charStats().numClasses();c++)
        {
            String charClass=mob.charStats().getMyClass(c).ID();
            if(completeAbleMap.containsKey(charClass))
            {
            	Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
                if((ableMap.containsKey(ability))&&(found==null))
                    found=ableMap.get(ability).costOverrides;
            }
        }
        if(completeAbleMap.containsKey(mob.charStats().getMyRace().ID()))
        {
        	Map<String,AbilityMapping> ableMap=completeAbleMap.get(mob.charStats().getMyRace().ID());
            if((ableMap.containsKey(ability))&&(found==null))
                found=ableMap.get(ability).costOverrides;
        }
        AbilityMapping AB=getAllAbleMap(ability);
        if((AB!=null)&&(found==null))
            return found=AB.costOverrides;
        return found;
    }

    public Integer[] getCostOverrides(String ability)
    {
    	Integer[] found=null;
        for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
        {
            String charClass=((CharClass)e.nextElement()).ID();
            if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
            {
            	Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
                if((ableMap.containsKey(ability))&&(found==null))
                    found=ableMap.get(ability).costOverrides;
            }
        }
        for(Enumeration e=CMClass.races();e.hasMoreElements();)
        {
            String ID=((Race)e.nextElement()).ID();
            if(completeAbleMap.containsKey(ID))
            {
            	Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
                if((ableMap.containsKey(ability))&&(found==null))
                    found=ableMap.get(ability).costOverrides;
            }
        }
        AbilityMapping AB=getAllAbleMap(ability);
        if((AB!=null)&&(found==null))
            return found=AB.costOverrides;
        return found;
    }

	public String getDefaultParm(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).defaultParm;
		}

		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).defaultParm;
		}
		return "";
	}

    public String getPreReqStrings(String ID, boolean checkAll, String ability)
    {
        if(completeAbleMap.containsKey(ID))
        {
        	Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
            if(ableMap.containsKey(ability))
                return ableMap.get(ability).originalSkillPreReqList;
        }

        if((checkAll)&&(completeAbleMap.containsKey("All")))
        {
        	Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
            if(ableMap.containsKey(ability))
                return ableMap.get(ability).originalSkillPreReqList;
        }
        return "";
    }

	public int getMaxProficiency(MOB mob, boolean checkAll, String ability)
	{
		if(mob==null) return getMaxProficiency(ability);
		CharClass C=mob.charStats().getCurrentClass();
		if(C==null) return getMaxProficiency(ability);
		return getMaxProficiency(C.ID(),checkAll,ability);
	}
	
	public int getMaxProficiency(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).maxProficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).maxProficiency;
		}
		return getMaxProficiency(ability);
	}
	public int getMaxProficiency(String abilityID)
	{
		if(maxProficiencyMap.containsKey(abilityID))
			return ((Integer)maxProficiencyMap.get(abilityID)).intValue();
		return 100;
	}
	public int getDefaultProficiency(String ID, boolean checkAll, String ability)
	{
		if(completeAbleMap.containsKey(ID))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).defaultProficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ableMap.get(ability).defaultProficiency;
		}
		return 0;
	}

	// returns Vector of components found if all good, returns Integer of bad row if not.
	public List<Object> componentCheck(MOB mob, List<AbilityComponent> req)
	{
		if((mob==null)||(req==null)||(req.size()==0))
			return new Vector();
		boolean currentAND=false;
		boolean previousValue=true;
		int amt=0;
		Vector passes=new Vector();
		Item I=null;
		Item container=null;
		Vector thisSet=new Vector();
		AbilityComponent comp = null;
		for(int i=0;i<req.size();i++)
		{
			comp=req.get(i);
			currentAND=comp.getConnector()==AbilityComponent.CompConnector.AND;
			if(previousValue&&(!currentAND)) return passes;

			// if they fail the zappermask, its like the req is NOT even there...
			if((comp.getCompiledMask()!=null)
			&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
				continue;
			amt=comp.getAmount();
			thisSet.clear();
			for(int ii=0;ii<mob.numItems();ii++)
			{
				I=mob.getItem(ii);
				if(I==null) continue;
				if((comp.getType()==AbilityComponent.CompType.STRING)&&(!CMLib.english().containsString(I.name(),comp.getStringType())))
					continue;
				else
				if((comp.getType()==AbilityComponent.CompType.RESOURCE)&&((!(I instanceof RawMaterial))||(I.material()!=comp.getLongType())))
					continue;
				else
				if((comp.getType()==AbilityComponent.CompType.MATERIAL)&&((!(I instanceof RawMaterial))||((I.material()&RawMaterial.MATERIAL_MASK)!=comp.getLongType())))
					continue;
				container=I.ultimateContainer();
				if(container==null) container=I;
				if((comp.getLocation()==AbilityComponent.CompLocation.INVENTORY)&&(!container.amWearingAt(Wearable.IN_INVENTORY)))
					continue;
				if((comp.getLocation()==AbilityComponent.CompLocation.HELD)&&(!container.amWearingAt(Wearable.WORN_HELD)))
					continue;
				if((comp.getLocation()==AbilityComponent.CompLocation.WORN)&&(container.amWearingAt(Wearable.IN_INVENTORY)))
					continue;
                if((comp.getType()!=AbilityComponent.CompType.STRING)
                &&(CMLib.flags().isOnFire(I)||CMLib.flags().enchanted(I)))
                    continue;
				if(comp.getType()==AbilityComponent.CompType.STRING)
                {
                    if(I instanceof PackagedItems)
                        I=(Item)CMLib.materials().unbundle(I,amt);
					amt-=I.numberOfItems();
                }
				else
                if(I.phyStats().weight()>amt)
                {
                    I=(Item)CMLib.materials().unbundle(I,amt);
                    if(I==null) continue;
					amt=amt-I.phyStats().weight();
                }
                else
                    amt=amt-I.phyStats().weight();
                thisSet.addElement(I);

				if(amt<=0)
				{
					if(thisSet.size()>0)
                        thisSet.addElement(Boolean.valueOf(comp.isConsumed()));
					break;
				}
			}
			if((amt>0)&&(currentAND)&&(i>0)) return null;
			previousValue=amt<=0;
			if(previousValue) passes.addAll(thisSet);
		}
		if(passes.size()==0) return null;
		return passes;
	}

    public List<AbilityComponent> getAbilityComponentDVector(String AID){ return (List<AbilityComponent>)getAbilityComponentMap().get(AID.toUpperCase().trim());}
    public List<DVector> getAbilityComponentDecodedDVectors(String AID){ return getAbilityComponentDecodedDVectors(getAbilityComponentDVector(AID));}
    public DVector getAbilityComponentDecodedDVector(List<AbilityComponent> codedDV, int r)
    {
        DVector curr=new DVector(2);
        String itemDesc=null;
        AbilityComponent comp = codedDV.get(r);
        curr.addElement("ANDOR",comp.getConnector()==AbilityComponent.CompConnector.AND?"&&":"||");
        if(comp.getLocation()==AbilityComponent.CompLocation.HELD)
        	curr.addElement("DISPOSITION","held");
        else
        if(comp.getLocation()==AbilityComponent.CompLocation.WORN)
        	curr.addElement("DISPOSITION","worn");
        else
        	curr.addElement("DISPOSITION","inventory");
        if(comp.isConsumed())
            curr.addElement("FATE","consumed");
        else
            curr.addElement("FATE","kept");
        curr.addElement("AMOUNT",""+comp.getAmount());
        if(comp.getType()==AbilityComponent.CompType.STRING)
            itemDesc=comp.getStringType();
        else
        if(comp.getType()==AbilityComponent.CompType.MATERIAL)
            itemDesc=RawMaterial.MATERIAL_DESCS[(int)comp.getLongType()>>8].toUpperCase();
        else
        if(comp.getType()==AbilityComponent.CompType.RESOURCE)
            itemDesc=RawMaterial.CODES.NAME((int)comp.getLongType()).toUpperCase();
        curr.addElement("COMPONENTID",itemDesc);
        curr.addElement("MASK",comp.getMaskStr());
        return curr;
    }

    public void setAbilityComponentCodedFromDecodedDVector(DVector decodedDV, List<AbilityComponent> codedDV, int row)
    {
        String[] s=new String[6];
        for(int i=0;i<6;i++)
            s[i]=(String)decodedDV.elementAt(i,2);
        AbilityComponent comp = codedDV.get(row);
        if(s[0].equalsIgnoreCase("||"))
            comp.setConnector(AbilityComponent.CompConnector.OR);
        else
            comp.setConnector(AbilityComponent.CompConnector.AND);
        if(s[1].equalsIgnoreCase("held"))
            comp.setLocation(AbilityComponent.CompLocation.HELD);
        else
        if(s[1].equalsIgnoreCase("worn"))
            comp.setLocation(AbilityComponent.CompLocation.WORN);
        else
            comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
        if(s[2].equalsIgnoreCase("consumed"))
            comp.setConsumed(true);
        else
            comp.setConsumed(false);
        comp.setAmount(CMath.s_int(s[3]));
        int depth=CMLib.materials().getResourceCode(s[4],false);
        if(depth>=0)
        	comp.setType(AbilityComponent.CompType.RESOURCE, Integer.valueOf(depth));
        else
        {
            depth=CMLib.materials().getMaterialCode(s[4],false);
            if(depth>=0)
            	comp.setType(AbilityComponent.CompType.MATERIAL, Integer.valueOf(depth));
            else
            	comp.setType(AbilityComponent.CompType.STRING, s[4].toUpperCase().trim());
        }
        comp.setMask(s[5]);
    }

    public List<DVector> getAbilityComponentDecodedDVectors(List<AbilityComponent> req)
    {
        if(req==null) return null;
        List<DVector> V=new Vector<DVector>();
        for(int r=0;r<req.size();r++)
        {
            DVector curr=getAbilityComponentDecodedDVector(req,r);
            V.add(curr);
        }
        return V;
    }

    public void addBlankAbilityComponent(List<AbilityComponent> codedDV)
    {
    	AbilityComponent comp = (AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
    	comp.setConnector(AbilityComponent.CompConnector.AND);
    	comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
    	comp.setConsumed(false);
    	comp.setAmount(1);
    	comp.setType(AbilityComponent.CompType.STRING, "resource-material-item name");
    	comp.setMask("");
        codedDV.add(comp);
    }

    public String getAbilityComponentCodedString(String AID)
    {
        StringBuffer buf=new StringBuffer("");
        List<DVector> comps=getAbilityComponentDecodedDVectors(AID);
        DVector curr=null;
        for(int c=0;c<comps.size();c++)
        {
            curr=(DVector)comps.get(c);
            if(c>0) buf.append((String)curr.elementAt(0,2));
            buf.append("(");
            buf.append((String)curr.elementAt(1,2));
            buf.append(":");
            buf.append((String)curr.elementAt(2,2));
            buf.append(":");
            buf.append((String)curr.elementAt(3,2));
            buf.append(":");
            buf.append((String)curr.elementAt(4,2));
            buf.append(":");
            buf.append((String)curr.elementAt(5,2));
            buf.append(")");
        }
        return AID+"="+buf.toString();
    }

    public String getAbilityComponentDesc(MOB mob, List<AbilityComponent> req, int r)
    {
        int amt=0;
        String itemDesc=null;
        StringBuffer buf=new StringBuffer("");
        AbilityComponent comp = req.get(r);
        if(r>0) buf.append(comp.getConnector()==AbilityComponent.CompConnector.AND?", and ":", or ");
        if((mob!=null)
        &&(comp.getCompiledMask()!=null)
        &&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
            return "";
        if(mob==null)
        {
            if(comp.getCompiledMask()!=null)
                buf.append("MASK: "+comp.getMaskStr()+": ");
        }
        amt=comp.getAmount();
        if(comp.getType()==AbilityComponent.CompType.STRING)
            itemDesc=((amt>1)?(amt+" "+comp.getStringType()+"s"):CMLib.english().startWithAorAn(comp.getStringType()));
        else
        if(comp.getType()==AbilityComponent.CompType.MATERIAL)
            itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.MATERIAL_DESCS[(int)comp.getLongType()>>8].toLowerCase();
        else
        if(comp.getType()==AbilityComponent.CompType.RESOURCE)
            itemDesc=amt+((amt>1)?" pounds":" pound")+" of "+RawMaterial.CODES.NAME((int)comp.getLongType()).toLowerCase();
        if(comp.getLocation()==AbilityComponent.CompLocation.INVENTORY)
        	buf.append(itemDesc);
        else
        if(comp.getLocation()==AbilityComponent.CompLocation.HELD)
        	buf.append(itemDesc+" held");
        else
        if(comp.getLocation()==AbilityComponent.CompLocation.WORN)
        	buf.append(itemDesc+" worn or wielded");
        return buf.toString();
    }

	public String getAbilityComponentDesc(MOB mob, String AID)
	{
		List<AbilityComponent> req=getAbilityComponentDVector(AID);
		if(req==null) return null;
		StringBuffer buf=new StringBuffer("");
		for(int r=0;r<req.size();r++){ buf.append(getAbilityComponentDesc(mob,req,r));}
		return buf.toString();
	}

	public String addAbilityComponent(String s, Map<String, List<AbilityComponent>> H)
	{
		int x=s.indexOf('=');
		if(x<0) return "Malformed component line (code 0): "+s;
		String id=s.substring(0,x).toUpperCase().trim();
		String parms=s.substring(x+1);

		String parmS=null;
		String rsc=null;
		List<AbilityComponent> parm=null;
		AbilityComponent build=null;
		int depth=0;
		parm=new Vector<AbilityComponent>();
		String error=null;
		while(parms.length()>0)
		{
			build=(AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
			build.setConnector(AbilityComponent.CompConnector.AND);
			if(parms.startsWith("||"))
			{ 
				build.setConnector(AbilityComponent.CompConnector.OR); 
				parms=parms.substring(2).trim();
			}
			else
			if(parms.startsWith("&&"))
			{ parms=parms.substring(2).trim();}

			if(!parms.startsWith("("))
			{error="Malformed component line (code 1): "+parms; break;}

			depth=0;
			x=1;
			for(;x<parms.length();x++)
				if((parms.charAt(x)==')')&&(depth==0))
					break;
				else
				if(parms.charAt(x)=='(')
					depth++;
				else
				if(parms.charAt(x)==')')
					depth--;
			if(x==parms.length()){error="Malformed component line (code 2): "+parms; break;}
			parmS=parms.substring(1,x).trim();
			parms=parms.substring(x+1).trim();

			build.setLocation(AbilityComponent.CompLocation.INVENTORY);
			x=parmS.indexOf(':');
			if(x<0)
			{
				error="Malformed component line (code 0-1): "+parmS; 
				continue;
			}
			if(parmS.substring(0,x).equalsIgnoreCase("held")) 
				build.setLocation(AbilityComponent.CompLocation.HELD);
			else
			if(parmS.substring(0,x).equalsIgnoreCase("worn")) 
				build.setLocation(AbilityComponent.CompLocation.WORN);
			else
			if((x>0)&&(!parmS.substring(0,x).equalsIgnoreCase("inventory")))
			{
				error="Malformed component line (code 0-2): "+parmS; 
				continue;
			}
			parmS=parmS.substring(x+1);

			build.setConsumed(true);
			x=parmS.indexOf(':');
			if(x<0){error="Malformed component line (code 1-1): "+parmS; continue;}
			if(parmS.substring(0,x).equalsIgnoreCase("kept")) 
				build.setConsumed(false);
			else
			if((x>0)&&(!parmS.substring(0,x).equalsIgnoreCase("consumed")))
			{
				error="Malformed component line (code 1-2): "+parmS; 
				continue;
			}
			parmS=parmS.substring(x+1);

			build.setAmount(1);
			x=parmS.indexOf(':');
			if(x<0){error="Malformed component line (code 2-1): "+parmS; continue;}
			if((x>0)&&(!CMath.isInteger(parmS.substring(0,x))))
			{
				error="Malformed component line (code 2-2): "+parmS; 
				continue;
			}
			if(x>0) 
				build.setAmount(CMath.s_int(parmS.substring(0,x)));
			parmS=parmS.substring(x+1);

			build.setType(AbilityComponent.CompType.STRING, "");
			x=parmS.indexOf(':');
			if(x<=0){error="Malformed component line (code 3-1): "+parmS; continue;}
			rsc=parmS.substring(0,x);
			depth=CMLib.materials().getResourceCode(rsc,false);
			if(depth>=0)
				build.setType(AbilityComponent.CompType.RESOURCE, Long.valueOf(depth));
			else
			{
				depth=CMLib.materials().getMaterialCode(rsc,false);
				if(depth>=0)
					build.setType(AbilityComponent.CompType.MATERIAL, Long.valueOf(depth));
				else
					build.setType(AbilityComponent.CompType.STRING, rsc.toUpperCase().trim());
			}
			parmS=parmS.substring(x+1);

			build.setMask(parmS);

			parm.add(build);
		}
		if(parm instanceof Vector)
			((Vector)parm).trimToSize();
		if(parm instanceof SVector)
			((SVector)parm).trimToSize();
		if(error!=null) return error;
		if(parm.size()>0) H.put(id.toUpperCase(),parm);
		return null;
	}

	// format of each data entry is 1=ANDOR(B), 2=DISPO(I), 3=CONSUMED(B), 4=AMT(I), 5=MATERIAL(L)RESOURCE(I)NAME(S), 6=MASK(S)
	
	public Map<String, List<AbilityComponent>> getAbilityComponentMap()
	{
		Map<String, List<AbilityComponent>> H=(Map)Resources.getResource("COMPONENT_MAP");
		if(H==null)
		{
			H=new Hashtable();
			StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true).text();
			List<String> V=new Vector();
			if(buf!=null)
				V=Resources.getFileLineVector(buf);
			String s=null;
			String error=null;
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				s=((String)V.get(v)).trim();
				if(s.startsWith("#")||(s.length()==0)||s.startsWith(";")||s.startsWith(":")) continue;
				error=addAbilityComponent(s,H);
				if(error!=null) Log.errOut("CMAble",error);
			}
			Resources.submitResource("COMPONENT_MAP",H);
		}
		return H;
	}
}
