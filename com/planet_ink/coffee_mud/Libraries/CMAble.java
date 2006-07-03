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
public class CMAble extends StdLibrary implements AbilityMapper
{
    public String ID(){return "CMAble";}
								
	public Hashtable completeAbleMap=new Hashtable();
	public Hashtable lowestQualifyingLevelMap=new Hashtable();
	public Hashtable allows=new Hashtable();
	
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID, 
			  int qualLevel,
			  String ability, 
			  boolean autoGain,
			  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false,new Vector(),extraMasks); }
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  boolean autoGain,
									  Vector skillPreReqs)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false,skillPreReqs,""); }
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  boolean autoGain,
									  Vector skillPreReqs,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false,skillPreReqs,extraMasks); }
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  int defaultProficiency,
									  String defParm,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,defParm,autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  int defaultProficiency,
									  String defParm,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,defParm,autoGain,false,new Vector(),extraMasks); }
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  int defaultProficiency,
									  boolean autoGain)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false,new Vector(),""); }
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  int defaultProficiency,
									  boolean autoGain,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,0,"",autoGain,false,new Vector(),extraMasks); }
	
	public void delCharAbilityMapping(String ID, String ability)
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
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,defaultParam,autoGain,secret,new Vector(),"");}
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  String extraMasks)
	{ addCharAbilityMapping(ID,qualLevel,ability,defaultProficiency,defaultParam,autoGain,secret,new Vector(),extraMasks);}
	
	public void addPreRequisites(String ID, Vector preReqSkillsList, String extraMask)
	{
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
	
	public Vector getAllowsList(String ID)
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
					Vector orset=getOrSet(ID,abilityID);
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
		return (Vector)allows.get(ID);
	}
	
	public void addCharAbilityMapping(String ID, 
									  int qualLevel,
									  String ability, 
									  int defaultProficiency,
									  String defaultParam,
									  boolean autoGain,
									  boolean secret,
									  Vector preReqSkillsList,
									  String extraMask)
	{
		delCharAbilityMapping(ID,ability);
		Hashtable ableMap=(Hashtable)completeAbleMap.get(ID);
		AbilityMapping able=new AbilityMapping();
		able.abilityName=ability;
		able.qualLevel=qualLevel;
		able.autoGain=autoGain;
		able.isSecret=secret;
		able.defaultParm=defaultParam;
		able.defaultProficiency=defaultProficiency;
		able.extraMask=extraMask;
		
		able.skillPreReqs=new DVector(2);
		addPreRequisites(ability,preReqSkillsList,extraMask);
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
			able.skillPreReqs.addElement(s,new Integer(prof));
		}
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
	public Vector getUpToLevelListings(String ID, int level, boolean ignoreAll, boolean gainedOnly)
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
				return ((AbilityMapping)ableMap.get(ability)).qualLevel;
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
}
