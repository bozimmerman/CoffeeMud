package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
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
	public Hashtable completeAbleMap=new Hashtable();
	public Hashtable lowestQualifyingLevelMap=new Hashtable();
	public Hashtable maxProficiencyMap=new Hashtable();
	public Hashtable allows=new Hashtable();
    public Hashtable completeDomainMap=new Hashtable();
    public Hashtable reverseAbilityMap=new Hashtable();

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
									  Vector skillPreReqs)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,100,"",autoGain,false,skillPreReqs,""); }
	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  boolean autoGain,
									  Vector skillPreReqs,
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
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new Hashtable());
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		if(ableMap.containsKey(ability))
			ableMap.remove(ability);
		DVector reV=(DVector)reverseAbilityMap.get(ability);
		if(reV!=null) reV.removeElement(ID);
	}
	public void delCharMappings(String ID)
	{
		if(completeAbleMap.containsKey(ID))
			completeAbleMap.remove(ID);
		for(Enumeration e=reverseAbilityMap.keys();e.hasMoreElements();)
		{
			String ability=(String)e.nextElement();
			DVector reV=(DVector)reverseAbilityMap.get(ability);
			reV.removeElement(ID);
		}
	}

	public Enumeration getClassAbles(String ID, boolean addAll)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new Hashtable());
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		Hashtable allAbleMap=(Hashtable)completeAbleMap.get("All");
		if((!addAll)||(allAbleMap==null)) return DVector.s_enum(ableMap, false);
		Vector V=new Vector();
		V.addAll(ableMap.values());
		V.addAll(allAbleMap.values());
		return V.elements();
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
							          Vector preReqSkillsList,
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

    public void addCharAbilityMapping(String ID,
                                      int qualLevel,
                                      String ability,
                                      int defaultProficiency,
                                      int maxProficiency,
                                      String defaultParam,
                                      boolean autoGain,
                                      boolean secret,
                                      Vector preReqSkillsList,
                                      String extraMask)
    { addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);}

	
	public void addPreRequisites(String ID, Vector preReqSkillsList, String extraMask)
	{
		if(preReqSkillsList==null) return;
		for(int v=0;v<preReqSkillsList.size();v++)
		{
			String s=(String)preReqSkillsList.elementAt(v);
			int x=s.indexOf("(");
			if((x>=0)&&(s.endsWith(")")))
				s=s.substring(0,x);
			if((s.indexOf("*")>=0)||(s.indexOf(",")>=0))
			{
				String ID2=ID;
				while(allows.contains("*"+ID2))
					ID2="*"+ID2;
				allows.put("*"+ID2,s);
			}
			else
			{
				Vector V=(Vector)allows.get(s);
				if(V==null){ V=new Vector(); allows.put(s,V);}
				if(!V.contains(ID))V.addElement(ID);
			}
		}
		if((extraMask!=null)&&(extraMask.trim().length()>0))
		{
			Vector preReqsOf=CMLib.masking().getAbilityEduReqs(extraMask);
			for(int v=0;v<preReqsOf.size();v++)
			{
				String s=(String)preReqsOf.elementAt(v);
				if((s.indexOf("*")>=0)||(s.indexOf(",")>=0))
				{
					String ID2=ID;
					while(allows.contains("*"+ID2))
						ID2="*"+ID2;
					allows.put("*"+ID2,s);
				}
				else
				{
					Vector V=(Vector)allows.get(s);
					if(V==null){ V=new Vector(); allows.put(s,V);}
					if(!V.contains(ID))V.addElement(ID);
				}
			}
		}
	}

    public boolean isDomainIncludedInAnyAbility(int domain, int acode)
    {
        Vector V=(Vector)completeDomainMap.get(Integer.valueOf(domain));
        if(V==null)
        {
            Ability A=null;
            V=new Vector();
            completeDomainMap.put(Integer.valueOf(domain),V);
            for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
            {
                A=(Ability)e.nextElement();
                if(((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
                &&(!V.contains(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)))))
                    V.addElement(Integer.valueOf((A.classificationCode()&Ability.ALL_ACODES)));
            }
        }
        return V.contains(Integer.valueOf(acode));
    }

    public DVector getClassAllowsList(String classID)
    {
        DVector ABLES=getUpToLevelListings(classID,CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL),false,false);
        Hashtable alreadyDone=new Hashtable();
        DVector DV=new DVector(2);
    	AbilityMapping able=null;
        Vector V2=null;
        Integer Ix=null;
        for(int a=0;a<ABLES.size();a++)
        {
            V2=getAbilityAllowsList((String)ABLES.elementAt(a,1));
            if((V2==null)||(V2.size()==0)) continue;
            able=(AbilityMapping)ABLES.elementAt(a,2);
            for(int v2=0;v2<V2.size();v2++)
            {
            	Ix=(Integer)alreadyDone.get(V2.elementAt(v2));
            	if(Ix==null)
            	{
	                alreadyDone.put(V2.elementAt(v2), Integer.valueOf(DV.size()));
	                DV.addElement(V2.elementAt(v2),Integer.valueOf(able.qualLevel));
            	}
            	else
            	if(((Integer)DV.elementAt(Ix.intValue(),2)).intValue()>able.qualLevel)
            		DV.setElementAt(Ix.intValue(),2,Integer.valueOf(able.qualLevel));
            }
        }
        return DV;
    }

	public Vector getAbilityAllowsList(String ableID)
	{
		String KEYID=null;
		String abilityID=null;
		Vector remove=null;
		for(Enumeration e=allows.keys();e.hasMoreElements();)
		{
			KEYID=(String)e.nextElement();
			if(KEYID.startsWith("*"))
			{
				abilityID=(String)allows.get(KEYID);
				if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(",")>0))
				{
					Vector orset=getOrSet(ableID,abilityID);
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
			for(int r=0;r<remove.size();r++)
				allows.remove(remove.elementAt(r));
		return (Vector)allows.get(ableID);
	}

	public void addCharAbilityMapping(String ID,
									  int qualLevel,
									  String ability,
									  int defaultProficiency,
									  int maxProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  Vector preReqSkillsList,
									  String extraMask,
                                      Integer[] costOverrides)
	{
		delCharAbilityMapping(ID,ability);
		
    	if(CMSecurity.isDisabled("ABILITY_"+ID.toUpperCase())) return;
    	
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		AbilityMapping able=new AbilityMapping();
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
			String s=(String)preReqSkillsList.elementAt(v);
			int prof=0;
			int x=s.indexOf("(");
			if((x>=0)&&(s.endsWith(")")))
			{
				prof=CMath.s_int(s.substring(x+1,s.length()-1));
				s=s.substring(0,x);
			}
			able.skillPreReqs.addElement(s,Integer.valueOf(prof));
		}
		ableMap.put(ability,able);
		int arc_level=getQualifyingLevel("Archon",true,ability);
		if((arc_level<0)||((qualLevel>=0)&&(qualLevel<arc_level)))
			addCharAbilityMapping("Archon",qualLevel,ability,true);
		Integer lowLevel=(Integer)lowestQualifyingLevelMap.get(ability);
		if((lowLevel==null)
		||(qualLevel<lowLevel.intValue()))
			lowestQualifyingLevelMap.put(ability,Integer.valueOf(qualLevel));
		Integer maxProf=(Integer)maxProficiencyMap.get(ability);
		if((maxProf==null)
		||(maxProficiency>maxProf.intValue()))
			maxProficiencyMap.put(ability,Integer.valueOf(maxProficiency));
		DVector reV=(DVector)reverseAbilityMap.get(ability);
		if(reV==null){ reV=new DVector(2); reverseAbilityMap.put(ability,reV);}
		if(!reV.contains(ID)) reV.addElement(ID, able);
	}

	public boolean qualifiesByAnyCharClass(String abilityID)
	{
		if(completeAbleMap.containsKey("All"))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return true;
		}
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


	public boolean classOnly(MOB mob, String classID, String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(classID);
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
			&&(((Hashtable)completeAbleMap.get(classID)).containsKey(abilityID)))
				return false;
		}
		return true;
	}


	public boolean availableToTheme(String abilityID, int theme, boolean publicly)
	{
		for(Enumeration e=completeAbleMap.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(((Hashtable)completeAbleMap.get(key)).containsKey(abilityID))
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

	public Vector getLevelListings(String ID, boolean checkAll, int level)
	{
		Vector V=new Vector();
        CharClass C=CMClass.getCharClass(ID);
        if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
            return V;
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
	public DVector getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly)
	{
		DVector DV=new DVector(2);
        CharClass C=CMClass.getCharClass(ID);
        if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
        	level=C.getLevelCap();
		if(completeAbleMap.containsKey(ID))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			for(Enumeration e=ableMap.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				AbilityMapping able=(AbilityMapping)ableMap.get(key);
				if((able.qualLevel<=level)
				&&((!gainedOnly)||(able.autoGain)))
					DV.addElement(key,able);
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
				&&(!DV.contains(key))
				&&((!gainedOnly)||(able.autoGain)))
					DV.addElement(key,able);
			}
		}
		return DV;
	}

	public int getQualifyingLevel(String ID, boolean checkAll, String ability)
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
            {
                int qualLevel = ((AbilityMapping)ableMap.get(ability)).qualLevel;
                CharClass C=CMClass.getCharClass(ID);
                if((C!=null)&&(C.getLevelCap()>=0))
                    return qualLevel>C.getLevelCap()?-1:qualLevel;
                return qualLevel;
            }
		}
		return -1;
	}

	public Vector getOrSet(String errStr, String abilityID)
	{
		Ability preA=null;
		Vector orset=new Vector();
		Vector preorset=CMParms.parseCommas(abilityID,true);
		for(int p=0;p<preorset.size();p++)
		{
			abilityID=(String)preorset.elementAt(p);
			if(abilityID.startsWith("*"))
			{
				String a=abilityID.substring(1).toUpperCase();
				for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=(Ability)e.nextElement();
					if(preA.ID().toUpperCase().endsWith(a))
						orset.addElement(preA.ID());
				}
			}
			else
			if(abilityID.endsWith("*"))
			{
				String a=abilityID.substring(0,abilityID.length()-1).toUpperCase();
				for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=(Ability)e.nextElement();
					if(preA.ID().toUpperCase().startsWith(a))
						orset.addElement(preA.ID());
				}
			}
			else
				orset.addElement(abilityID);
		}
		for(int o=orset.size()-1;o>=0;o--)
		{
			abilityID=(String)orset.elementAt(o);
			preA=CMClass.getAbility(abilityID);
			if(preA==null)
			{
				preA=CMClass.findAbility(abilityID);
				if(preA!=null)
					orset.setElementAt(preA.ID(),o);
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
			if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(",")>0))
			{
				Vector orset=getOrSet(A.ID(),abilityID);
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

	public DVector getApplicablePreRequisites(MOB mob, Ability A)
	{
		DVector V=getRawPreRequisites(mob,A);
		if((V==null)||(V.size()==0)) return new DVector(2);
		fillPreRequisites(A,V);
		return V;
	}

	public DVector getCommonPreRequisites(Ability A)
	{
		DVector preReqs=null;
		Hashtable ableMap=null;
		if(completeAbleMap.containsKey("All"))
		{
			ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(A.ID()))
				preReqs=((AbilityMapping)ableMap.get(A.ID())).skillPreReqs;
		}
		if(preReqs==null)
		for(Enumeration e=completeAbleMap.elements();e.hasMoreElements();)
		{
			ableMap=(Hashtable)e.nextElement();
			if(ableMap.containsKey(A.ID()))
			{
				preReqs=((AbilityMapping)ableMap.get(A.ID())).skillPreReqs;
				if((preReqs!=null)&&(preReqs.size()>0)) break;
			}
		}
		if((preReqs==null)||(preReqs.size()==0)) return new DVector(2);
		DVector reqs=preReqs.copyOf();
		fillPreRequisites(A,reqs);
		return reqs;

	}

	public String getCommonExtraMask(Ability A)
	{
		String mask=null;
		Hashtable ableMap=null;
		if(completeAbleMap.containsKey("All"))
		{
			ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(A.ID()))
				mask=((AbilityMapping)ableMap.get(A.ID())).extraMask;
		}
		if((mask==null)||(mask.length()==0))
		for(Enumeration e=completeAbleMap.elements();e.hasMoreElements();)
		{
			ableMap=(Hashtable)e.nextElement();
			if(ableMap.containsKey(A.ID()))
			{
				mask=((AbilityMapping)ableMap.get(A.ID())).extraMask;
				if((mask!=null)&&(mask.length()>0)) break;
			}
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
				Vector orset=(Vector)V.elementAt(v,1);
				for(int o=orset.size()-1;o>=0;o--)
				{
					abilityID=(String)orset.elementAt(o);
					A2=student.fetchAbility(abilityID);
					if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					{
						orset.clear();
						break;
					}
					if(!qualifiesByLevel(student,abilityID))
						orset.removeElementAt(o);
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
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).skillPreReqs;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).skillPreReqs;
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
				if(preReqs.elementAt(p,1) instanceof Vector)
				{
					Vector V=(Vector)preReqs.elementAt(p,1);
					names.append("(One of: ");
					for(int v=0;v<V.size();v++)
					{
						Ability A=CMClass.getAbility((String)V.elementAt(v));
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
		int classLevel=student.baseEnvStats().level();
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
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).extraMask;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).extraMask;
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
		int classLevel=student.baseEnvStats().level();
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
		int classLevel=student.baseEnvStats().level();
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
		if((level>=0)&&(student.envStats().level()>=level))
			return true;
		return false;
	}

	public boolean getDefaultGain(String ID, boolean checkAll, String ability)
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

    public AbilityMapping getAbleMap(String ID, String ability)
    {
        if(completeAbleMap.containsKey(ID))
        {
            Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
            if(ableMap.containsKey(ability))
                return (AbilityMapping)ableMap.get(ability);
        }
        return null;
    }
	public AbilityMapping getAllAbleMap(String ability){ return getAbleMap("All",ability);}

	public boolean getSecretSkill(String ID, boolean checkAll, String ability)
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

	public boolean getSecretSkill(MOB mob, String ability)
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

    public Integer[] getCostOverrides(String ID, boolean checkAll, String ability)
    {
    	Integer[] found=null;
        if(completeAbleMap.containsKey(ID))
        {
            Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
            if(ableMap.containsKey(ability))
                found=((AbilityMapping)ableMap.get(ability)).costOverrides;
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
                Hashtable ableMap=(Hashtable)completeAbleMap.get(charClass);
                if((ableMap.containsKey(ability))&&(found==null))
                    found=((AbilityMapping)ableMap.get(ability)).costOverrides;
            }
        }
        if(completeAbleMap.containsKey(mob.charStats().getMyRace().ID()))
        {
            Hashtable ableMap=(Hashtable)completeAbleMap.get(mob.charStats().getMyRace().ID());
            if((ableMap.containsKey(ability))&&(found==null))
                found=((AbilityMapping)ableMap.get(ability)).costOverrides;
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
                Hashtable ableMap=(Hashtable)completeAbleMap.get(charClass);
                if((ableMap.containsKey(ability))&&(found==null))
                    found=((AbilityMapping)ableMap.get(ability)).costOverrides;
            }
        }
        for(Enumeration e=CMClass.races();e.hasMoreElements();)
        {
            String ID=((Race)e.nextElement()).ID();
            if(completeAbleMap.containsKey(ID))
            {
                Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
                if((ableMap.containsKey(ability))&&(found==null))
                    found=((AbilityMapping)ableMap.get(ability)).costOverrides;
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

    public String getPreReqStrings(String ID, boolean checkAll, String ability)
    {
        if(completeAbleMap.containsKey(ID))
        {
            Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
            if(ableMap.containsKey(ability))
                return ((AbilityMapping)ableMap.get(ability)).originalSkillPreReqList;
        }

        if((checkAll)&&(completeAbleMap.containsKey("All")))
        {
            Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
            if(ableMap.containsKey(ability))
                return ((AbilityMapping)ableMap.get(ability)).originalSkillPreReqList;
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
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).maxProficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).maxProficiency;
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
			Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).defaultProficiency;
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			Hashtable ableMap=(Hashtable)completeAbleMap.get("All");
			if(ableMap.containsKey(ability))
				return ((AbilityMapping)ableMap.get(ability)).defaultProficiency;
		}
		return 0;
	}

	// returns Vector of components found if all good, returns Integer of bad row if not.
	public Vector componentCheck(MOB mob, Vector<AbilityComponent> req)
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
			comp=req.elementAt(i);
			currentAND=comp.getConnector()==AbilityComponent.CompConnector.AND;
			if(previousValue&&(!currentAND)) return passes;

			// if they fail the zappermask, its like the req is NOT even there...
			if((comp.getCompiledMask()!=null)
			&&(!CMLib.masking().maskCheck(comp.getCompiledMask(),mob,true)))
				continue;
			amt=comp.getAmount();
			thisSet.clear();
			for(int ii=0;ii<mob.inventorySize();ii++)
			{
				I=mob.fetchInventory(ii);
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
                if(I.envStats().weight()>amt)
                {
                    I=(Item)CMLib.materials().unbundle(I,amt);
                    if(I==null) continue;
					amt=amt-I.envStats().weight();
                }
                else
                    amt=amt-I.envStats().weight();
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
			if(previousValue) CMParms.addToVector(thisSet,passes);
		}
		if(passes.size()==0) return null;
		return passes;
	}

    public Vector<AbilityComponent> getAbilityComponentDVector(String AID){ return (Vector<AbilityComponent>)getAbilityComponentMap().get(AID.toUpperCase().trim());}
    public Vector getAbilityComponentDecodedDVectors(String AID){ return getAbilityComponentDecodedDVectors(getAbilityComponentDVector(AID));}
    public DVector getAbilityComponentDecodedDVector(Vector<AbilityComponent> codedDV, int r)
    {
        DVector curr=new DVector(2);
        String itemDesc=null;
        AbilityComponent comp = codedDV.elementAt(r);
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

    public void setAbilityComponentCodedFromDecodedDVector(DVector decodedDV, Vector<AbilityComponent> codedDV, int row)
    {
        String[] s=new String[6];
        for(int i=0;i<6;i++)
            s[i]=(String)decodedDV.elementAt(i,2);
        AbilityComponent comp = codedDV.elementAt(row);
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

    public Vector getAbilityComponentDecodedDVectors(Vector<AbilityComponent> req)
    {
        if(req==null) return null;
        Vector V=new Vector();
        for(int r=0;r<req.size();r++)
        {
            DVector curr=getAbilityComponentDecodedDVector(req,r);
            V.addElement(curr);
        }
        return V;
    }

    public void addBlankAbilityComponent(Vector<AbilityComponent> codedDV)
    {
    	AbilityComponent comp = (AbilityComponent)CMClass.getCommon("DefaultAbilityComponent");
    	comp.setConnector(AbilityComponent.CompConnector.AND);
    	comp.setLocation(AbilityComponent.CompLocation.INVENTORY);
    	comp.setConsumed(false);
    	comp.setAmount(1);
    	comp.setType(AbilityComponent.CompType.STRING, "resource-material-item name");
    	comp.setMask("");
        codedDV.addElement(comp);
    }

    public String getAbilityComponentCodedString(String AID)
    {
        StringBuffer buf=new StringBuffer("");
        Vector comps=getAbilityComponentDecodedDVectors(AID);
        DVector curr=null;
        for(int c=0;c<comps.size();c++)
        {
            curr=(DVector)comps.elementAt(c);
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

    public String getAbilityComponentDesc(MOB mob, Vector<AbilityComponent> req, int r)
    {
        int amt=0;
        String itemDesc=null;
        StringBuffer buf=new StringBuffer("");
        AbilityComponent comp = req.elementAt(r);
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
		Vector<AbilityComponent> req=getAbilityComponentDVector(AID);
		if(req==null) return null;
		StringBuffer buf=new StringBuffer("");
		for(int r=0;r<req.size();r++){ buf.append(getAbilityComponentDesc(mob,req,r));}
		return buf.toString();
	}

	public String addAbilityComponent(String s, Hashtable<String, Vector<AbilityComponent>> H)
	{
		int x=s.indexOf("=");
		if(x<0) return "Malformed component line (code 0): "+s;
		String id=s.substring(0,x).toUpperCase().trim();
		String parms=s.substring(x+1);

		String parmS=null;
		String rsc=null;
		Vector<AbilityComponent> parm=null;
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
			x=parmS.indexOf(":");
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
			x=parmS.indexOf(":");
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
			x=parmS.indexOf(":");
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
			x=parmS.indexOf(":");
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

			parm.addElement(build);
		}
		parm.trimToSize();
		if(error!=null) return error;
		if(parm.size()>0) H.put(id.toUpperCase(),parm);
		return null;
	}

	// format of each data entry is 1=ANDOR(B), 2=DISPO(I), 3=CONSUMED(B), 4=AMT(I), 5=MATERIAL(L)RESOURCE(I)NAME(S), 6=MASK(S)
	
	public Hashtable getAbilityComponentMap()
	{
		Hashtable H=(Hashtable)Resources.getResource("COMPONENT_MAP");
		if(H==null)
		{
			H=new Hashtable();
			StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true).text();
			Vector V=new Vector();
			if(buf!=null)
				V=Resources.getFileLineVector(buf);
			String s=null;
			String error=null;
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				s=((String)V.elementAt(v)).trim();
				if(s.startsWith("#")||(s.length()==0)||s.startsWith(";")||s.startsWith(":")) continue;
				error=addAbilityComponent(s,H);
				if(error!=null) Log.errOut("CMAble",error);
			}
			Resources.submitResource("COMPONENT_MAP",H);
		}
		return H;
	}
}
