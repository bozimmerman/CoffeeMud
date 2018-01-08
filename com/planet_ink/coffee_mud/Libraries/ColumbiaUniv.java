package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.CostType;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.SkillCost;
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
import java.util.Map.Entry;

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class ColumbiaUniv extends StdLibrary implements ExpertiseLibrary
{
	@Override
	public String ID()
	{
		return "ColumbiaUniv";
	}

	protected SHashtable<String,ExpertiseLibrary.ExpertiseDefinition> completeEduMap=new SHashtable<String,ExpertiseLibrary.ExpertiseDefinition>();
	protected SHashtable<String,List<String>> baseEduSetLists=new SHashtable<String,List<String>>();
	@SuppressWarnings("unchecked")
	protected Hashtable<String,String>[] completeUsageMap=new Hashtable[ExpertiseLibrary.Flag.values().length];
	protected Properties helpMap=new Properties();
	protected DVector rawDefinitions=new DVector(7);

	@Override
	public ExpertiseLibrary.ExpertiseDefinition addDefinition(String ID, String name, String baseName, String listMask, String finalMask, String[] costs, String[] data)
	{
		ExpertiseLibrary.ExpertiseDefinition def=getDefinition(ID);
		if(def!=null)
			return  def;
		if(CMSecurity.isExpertiseDisabled(ID.toUpperCase()))
			return null;
		if(CMSecurity.isExpertiseDisabled("*"))
			return null;
		for(int i=1;i<ID.length();i++)
		{
			if(CMSecurity.isExpertiseDisabled(ID.substring(0,i).toUpperCase()+"*"))
				return null;
		}
		def=createNewExpertiseDefinition(ID.toUpperCase(), name, baseName);
		def.addListMask(listMask);
		def.addFinalMask(finalMask);
		def.setData((data==null)?new String[0]:data);
		final int practices=CMath.s_int(costs[0]);
		final int trains=CMath.s_int(costs[1]);
		final int qpCost=CMath.s_int(costs[2]);
		final int expCost=CMath.s_int(costs[3]);
		//int timeCost=CMath.s_int(costs[0]);
		if(practices>0)
			def.addCost(CostType.PRACTICE, Double.valueOf(practices));
		if(trains>0)
			def.addCost(CostType.TRAIN, Double.valueOf(trains));
		if(qpCost>0)
			def.addCost(CostType.QP, Double.valueOf(qpCost));
		if(expCost>0)
			def.addCost(CostType.XP, Double.valueOf(expCost));
		//if(timeCost>0) def.addCost(CostType.PRACTICE, Double.valueOf(practices));
		completeEduMap.put(def.ID(),def);
		baseEduSetLists.clear();
		return def;
	}

	@Override
	public String getExpertiseHelp(String ID, boolean exact)
	{
		if(ID==null)
			return null;
		ID=ID.toUpperCase();
		if(exact)
			return helpMap.getProperty(ID);
		for(final Enumeration<Object> e = helpMap.keys();e.hasMoreElements();)
		{
			final String key = e.nextElement().toString();
			if(key.startsWith(ID))
				return helpMap.getProperty(key);
		}
		for(final Enumeration<Object> e = helpMap.keys();e.hasMoreElements();)
		{
			final String key = e.nextElement().toString();
			if(CMLib.english().containsString(key, ID))
				return helpMap.getProperty(key);
		}
		return null;
	}

	@Override
	public void delDefinition(String ID)
	{
		completeEduMap.remove(ID);
		baseEduSetLists.clear();
	}

	@Override
	public Enumeration<ExpertiseDefinition> definitions()
	{
		return completeEduMap.elements();
	}

	@Override
	public ExpertiseDefinition getDefinition(String ID)
	{
		if(ID!=null)
			return completeEduMap.get(ID.trim().toUpperCase());
		return null;
	}

	@Override
	public ExpertiseDefinition findDefinition(String ID, boolean exactOnly)
	{
		ExpertiseDefinition D=getDefinition(ID);
		if(D!=null)
			return D;
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(D.name().equalsIgnoreCase(ID))
				return D;
		}
		if(exactOnly)
			return null;
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(D.ID().startsWith(ID))
				return D;
		}
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(CMLib.english().containsString(D.name(),ID))
				return D;
		}
		return null;
	}

	@Override
	public List<ExpertiseDefinition> myQualifiedExpertises(MOB mob)
	{
		ExpertiseDefinition D=null;
		final List<ExpertiseDefinition> V=new Vector<ExpertiseDefinition>();
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(((D.compiledFinalMask()==null)||(CMLib.masking().maskCheck(D.compiledFinalMask(),mob,true)))
			&&((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true))))
				V.add(D);
		}
		final PlayerStats pStats = mob.playerStats();
		if(pStats != null)
		{
			for(final ExpertiseDefinition def : pStats.getExtraQualifiedExpertises().values())
			{
				if(((def.compiledFinalMask()==null)||(CMLib.masking().maskCheck(def.compiledFinalMask(),mob,true)))
				&&((def.compiledListMask()==null)||(CMLib.masking().maskCheck(def.compiledListMask(),mob,true))))
				{
					D = completeEduMap.get(def.ID());
					if(D!=null)
						V.remove(D);
					V.add(def);
				}
			}
		}
		return V;
	}

	@Override
	public List<ExpertiseDefinition> myListableExpertises(MOB mob)
	{
		ExpertiseDefinition D=null;
		final List<ExpertiseDefinition> V=new Vector<ExpertiseDefinition>();
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true)))
				V.add(D);
		}
		final PlayerStats pStats = mob.playerStats();
		if(pStats != null)
		{
			for(final ExpertiseDefinition def : pStats.getExtraQualifiedExpertises().values())
			{
				if((def.compiledListMask()==null)||(CMLib.masking().maskCheck(def.compiledListMask(),mob,true)))
				{
					D = completeEduMap.get(def.ID());
					if(D!=null)
						V.remove(D);
					V.add(def);
				}
			}
		}
		return V;
	}

	@Override
	public int getExpertiseLevel(final MOB mob, final String abilityID, final ExpertiseLibrary.Flag code)
	{
		if((mob==null)||(code==null)||(abilityID==null)||(abilityID.length()==0))
			return 0;
		int expertiseLvl=0;
		final int[][] usageCache=mob.getAbilityUsageCache(abilityID);
		if(usageCache[Ability.CACHEINDEX_EXPERTISE]!=null)
			expertiseLvl=usageCache[Ability.CACHEINDEX_EXPERTISE][code.ordinal()];
		else
		{
			final int[] xFlagCache=new int[ExpertiseLibrary.Flag.values().length];
			final CharClass charClass = mob.baseCharStats().getCurrentClass();
			for(ExpertiseLibrary.Flag flag : ExpertiseLibrary.Flag.values())
			{
				xFlagCache[flag.ordinal()]=CMLib.expertises().getApplicableExpertiseLevel(abilityID,flag,mob)
											+charClass.addedExpertise(mob, flag, abilityID);
			}
			usageCache[Ability.CACHEINDEX_EXPERTISE]=xFlagCache;
			expertiseLvl = xFlagCache[code.ordinal()];
		}
		return expertiseLvl;
	}
	
	private int getStageNumber(ExpertiseDefinition D)
	{
		if(D.ID().startsWith(D.getBaseName()))
		{
			final String remID=D.ID().substring(D.getBaseName().length());
			if(CMath.isInteger(remID))
				return CMath.s_int(remID);
			if(CMath.isRomanNumeral(remID))
				return CMath.convertFromRoman(remID);
		}
		return 0;
	}
	
	@Override
	public int getHighestListableStageBySkill(final MOB mob, String ableID, ExpertiseLibrary.Flag flag)
	{
		String expertiseID = completeUsageMap[flag.ordinal()].get(ableID);
		if(expertiseID == null)
			return 0;
		ExpertiseDefinition D=null;
		int max=0;
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if(expertiseID.equals(D.getBaseName()))
			{
				if((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true)))
				{
					int stage=getStageNumber(D);
					if(stage > max)
						max=stage;
				}
			}
		}
		final PlayerStats pStats = mob.playerStats();
		if(pStats != null)
		{
			for(final ExpertiseDefinition def : pStats.getExtraQualifiedExpertises().values())
			{
				if(expertiseID.equals(def.getBaseName()))
				{
					if((def.compiledListMask()==null)||(CMLib.masking().maskCheck(def.compiledListMask(),mob,true)))
					{
						int stage=getStageNumber(def);
						if(stage > max)
							max=stage;
					}
				}
			}
		}
		return max;
	}

	@Override
	public int numExpertises()
	{
		return completeEduMap.size();
	}

	private String expertMath(String s,int l)
	{
		int x=s.indexOf('{');
		while(x>=0)
		{
			final int y=s.indexOf('}',x);
			if(y<0)
				break;
			s=s.substring(0,x)+CMath.parseIntExpression(s.substring(x+1,y))+s.substring(y+1);
			x=s.indexOf('{');
		}
		return s;
	}

	@Override
	public List<String> getStageCodes(String baseExpertiseCode)
	{
		String key=null;
		if(baseExpertiseCode==null)
			return new ReadOnlyVector<String>(1);
		baseExpertiseCode=baseExpertiseCode.toUpperCase();
		if(!baseEduSetLists.containsKey(baseExpertiseCode))
		{
			synchronized(("ListedEduBuild:"+baseExpertiseCode).intern())
			{
				if(!baseEduSetLists.containsKey(baseExpertiseCode))
				{
					final List<String> codes=new LinkedList<String>();
					for(final Enumeration<String> e=completeEduMap.keys();e.hasMoreElements();)
					{
						key=e.nextElement();
						if(key.startsWith(baseExpertiseCode)
						&&(CMath.isInteger(key.substring(baseExpertiseCode.length()))||CMath.isRomanNumeral(key.substring(baseExpertiseCode.length()))))
							codes.add(key);
					}
					baseEduSetLists.put(baseExpertiseCode, new ReadOnlyVector<String>(codes));
				}
			}
		}
		return baseEduSetLists.get(baseExpertiseCode);
	}

	@Override
	public int getStages(String baseExpertiseCode)
	{
		return getStageCodes(baseExpertiseCode).size();
	}

	@Override
	public String getGuessedBaseExpertiseName(final String expertiseCode)
	{
		int lastBadChar=expertiseCode.length()-1;
		while( (lastBadChar>=0)
		&&(CMath.isInteger(expertiseCode.substring(lastBadChar))||CMath.isRomanNumeral(expertiseCode.substring(lastBadChar))))
			lastBadChar--;
		if(lastBadChar<expertiseCode.length()-1)
			return expertiseCode.substring(0,lastBadChar+1);
		return expertiseCode;
	}

	@Override
	public List<String> getPeerStageCodes(final String expertiseCode)
	{
		return getStageCodes(getGuessedBaseExpertiseName(expertiseCode));
	}

	@Override
	public String getApplicableExpertise(String ID, Flag code)
	{
		return completeUsageMap[code.ordinal()].get(ID);
	}

	@Override
	public int getApplicableExpertiseLevel(String ID, Flag code, MOB mob)
	{
		final Pair<String,Integer> e=mob.fetchExpertise(completeUsageMap[code.ordinal()].get(ID));
		if((e!=null)&&(e.getValue()!=null))
			return e.getValue().intValue();
		return 0;
	}

	@Override
	public SkillCost createNewSkillCost(final CostType costType, final Double value)
	{
		return new SkillCost()
		{
			/**
			 * Returns a simple description of the Type of
			 * this cost.  A MOB and sample value is required for
			 * money currencies.
			 * @param mob MOB, for GOLD type currency eval
			 * @return the type of currency
			 */
			@Override
			public String costType(final MOB mob)
			{
				final String ofWhat;
				switch(costType)
				{
				case XP: ofWhat="experience points"; break;
				case GOLD: ofWhat=CMLib.beanCounter().getDenominationName(mob, value.doubleValue()); break;
				case PRACTICE: ofWhat="practice points"; break;
				case QP: ofWhat="quest points"; break;
				default: ofWhat=CMLib.english().makePlural(costType.name().toLowerCase()); break;
				}
				return ofWhat;
			}

			@Override
			public String requirements(final MOB mob)
			{
				switch(costType)
				{
				case XP: return value.intValue()+" XP";
				case QP: return value.intValue()+" quest pts";
				case GOLD:
					if(mob==null)
						return CMLib.beanCounter().abbreviatedPrice("", value.doubleValue());
					else
						return CMLib.beanCounter().abbreviatedPrice(mob, value.doubleValue());
				default: return value.intValue()+" "
							   +((value.intValue()==1)
									   ?costType.name().toLowerCase()
									   :CMLib.english().makePlural(costType.name().toLowerCase()));
				}
			}

			/**
			 * Returns whether the given mob meets the given cost requirements.
			 * @param student the student to check
			 * @return true if it meets, false otherwise
			 */
			@Override
			public boolean doesMeetCostRequirements(final MOB student)
			{
				switch(costType)
				{
				case XP:
					return student.getExperience() >= value.intValue();
				case GOLD:
					return CMLib.beanCounter().getTotalAbsoluteNativeValue(student) >= value.doubleValue();
				case TRAIN:
					return student.getTrains() >= value.intValue();
				case PRACTICE:
					return student.getPractices() >= value.intValue();
				case QP:
					return student.getQuestPoint() >= value.intValue();
				}
				return false;
			}

			/**
			 * Expends the given cost upon the given student
			 * @param student the student to check
			 */
			@Override
			public void spendSkillCost(final MOB student)
			{
				switch(costType)
				{
				case XP:	   CMLib.leveler().postExperience(student, null, "", value.intValue(), true); break;
				case GOLD:     CMLib.beanCounter().subtractMoney(student, value.doubleValue()); break;
				case TRAIN:    student.setTrains(student.getTrains()-value.intValue()); break;
				case PRACTICE: student.setPractices(student.getPractices()-value.intValue()); break;
				case QP:	   student.setQuestPoint(student.getQuestPoint()-value.intValue()); break;
				}
			}
		};
	}
	
	@Override
	public String confirmExpertiseLine(String row, String ID, boolean addIfPossible)
	{
		int levels=0;
		final HashSet<String> flags=new HashSet<String>();
		String s=null;
		String skillMask=null;
		final String[] costs=new String[5];
		final String[] data=new String[0];
		String WKID=null;
		String name,WKname=null;
		String listMask,WKlistMask=null;
		String finalMask,WKfinalMask=null;
		List<String> skillsToRegister=null;
		ExpertiseLibrary.ExpertiseDefinition def=null;
		boolean didOne=false;
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
			return null;
		int x=row.indexOf('=');
		if(x<0)
			return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!";
		if(row.trim().toUpperCase().startsWith("DATA_"))
		{
			final String lastID=ID;
			ID=row.substring(0,x).toUpperCase();
			row=row.substring(x+1);
			ID=ID.substring(5).toUpperCase();
			if(ID.length()==0)
				ID=lastID;
			if((lastID==null)||(lastID.length()==0))
				return "Error: No last expertise found for data: "+lastID+"="+row;
			else
			if(this.getDefinition(ID)!=null)
			{
				def=getDefinition(ID);
				WKID=def.name().toUpperCase().replace(' ','_');
				if(addIfPossible)
				{
					def.setData(CMParms.parseCommas(row,true).toArray(new String[0]));
				}
			}
			else
			{
				final List<String> stages=getStageCodes(ID);
				if(addIfPossible)
				{
					for(int s1=0;s1<stages.size();s1++)
					{
						def=getDefinition(stages.get(s1));
						if(def==null)
							continue;
						def.setData(CMParms.parseCommas(row,true).toArray(new String[0]));
					}
				}
			}
			return null;
		}
		if(row.trim().toUpperCase().startsWith("HELP_"))
		{
			final String lastID=ID;
			ID=row.substring(0,x).toUpperCase();
			row=row.substring(x+1);
			ID=ID.substring(5).toUpperCase();
			if(ID.length()==0)
				ID=lastID;
			if((lastID==null)||(lastID.length()==0))
				return "Error: No last expertise found for help: "+lastID+"="+row;
			else
			if(getDefinition(ID)!=null)
			{
				def=getDefinition(ID);
				WKID=def.name().toUpperCase().replace(' ','_');
				if(addIfPossible)
				{
					helpMap.remove(WKID);
					helpMap.put(WKID,row);
				}
			}
			else
			{
				final List<String> stages=getStageCodes(ID);
				if((stages==null)||(stages.size()==0))
					return "Error: Expertise not yet defined: "+ID+"="+row;
				def=getDefinition(stages.get(0));
				if(def!=null)
				{
					WKID=def.name().toUpperCase().replace(' ','_');
					x=WKID.lastIndexOf('_');
					if((x>=0)&&(CMath.isInteger(WKID.substring(x+1))||CMath.isRomanNumeral(WKID.substring(x+1))))
					{
						WKID=WKID.substring(0,x);
						if(addIfPossible)
						if(!helpMap.containsKey(WKID))
							helpMap.put(WKID,row+"\n\r(See help on "+def.name()+").");
					}
				}
				if(addIfPossible)
				for(int s1=0;s1<stages.size();s1++)
				{
					def=getDefinition(stages.get(s1));
					if(def==null)
						continue;
					WKID=def.name().toUpperCase().replace(' ','_');
					if(!helpMap.containsKey(WKID))
						helpMap.put(WKID,row);
				}
			}
			return null;
		}
		ID=row.substring(0,x).toUpperCase();
		row=row.substring(x+1);
		final List<String> parts=CMParms.parseCommas(row,false);
		if(parts.size()!=11)
			return "Error: Expertise row malformed (Requires 11 entries/10 commas): "+ID+"="+row;
		name=parts.get(0);
		if(name.length()==0)
			return "Error: Expertise name ("+name+") malformed: "+ID+"="+row;
		if(!CMath.isInteger(parts.get(1)))
			return "Error: Expertise num ("+(parts.get(1))+") malformed: "+ID+"="+row;
		levels=CMath.s_int(parts.get(1));
		flags.clear();
		flags.addAll(CMParms.parseAny(parts.get(2).toUpperCase(),'|',true));

		skillMask=parts.get(3);
		if(skillMask.length()==0)
			return "Error: Expertise skill mask ("+skillMask+") malformed: "+ID+"="+row;
		skillsToRegister=CMLib.masking().getAbilityEduReqs(skillMask);
		if(skillsToRegister.size()==0)
		{
			skillsToRegister=CMLib.masking().getAbilityEduReqs(skillMask);
			return "Error: Expertise no skills ("+skillMask+") found: "+ID+"="+row;
		}
		listMask=skillMask+" "+(parts.get(4));
		finalMask=((parts.get(5)));
		for(int i=6;i<11;i++)
			costs[i-6]=parts.get(i);
		didOne=false;
		for(int u=0;u<completeUsageMap.length;u++)
		{
			didOne=didOne||flags.contains(ExpertiseLibrary.Flag.values()[u].name());
		}
		if(!didOne)
			return "Error: No flags ("+parts.get(2).toUpperCase()+") were set: "+ID+"="+row;
		if(addIfPossible)
		{
			final String baseName=CMStrings.replaceAll(CMStrings.replaceAll(ID,"@X2",""),"@X1","").toUpperCase();
			for(int l=1;l<=levels;l++)
			{
				WKID=CMStrings.replaceAll(ID,"@X1",""+l);
				WKID=CMStrings.replaceAll(WKID,"@X2",""+CMath.convertToRoman(l));
				WKname=CMStrings.replaceAll(name,"@x1",""+l);
				WKname=CMStrings.replaceAll(WKname,"@x2",""+CMath.convertToRoman(l));
				WKlistMask=CMStrings.replaceAll(listMask,"@x1",""+l);
				WKlistMask=CMStrings.replaceAll(WKlistMask,"@x2",""+CMath.convertToRoman(l));
				WKfinalMask=CMStrings.replaceAll(finalMask,"@x1",""+l);
				WKfinalMask=CMStrings.replaceAll(WKfinalMask,"@x2",""+CMath.convertToRoman(l));
				if((l>1)&&(listMask.toUpperCase().indexOf("-EXPERT")<0))
				{
					s=CMStrings.replaceAll(ID,"@X1",""+(l-1));
					s=CMStrings.replaceAll(s,"@X2",""+CMath.convertToRoman(l-1));
					WKlistMask="-EXPERTISE \"+"+s+"\" "+WKlistMask;
				}
				WKlistMask=expertMath(WKlistMask,l);
				WKfinalMask=expertMath(WKfinalMask,l);
				def=addDefinition(WKID,WKname,baseName,WKlistMask,WKfinalMask,costs,data);
				if(def!=null)
				{
					def.compiledFinalMask();
					def.compiledListMask();
				}
			}
		}
		ID=CMStrings.replaceAll(ID,"@X1","");
		ID=CMStrings.replaceAll(ID,"@X2","");
		for(int u=0;u<completeUsageMap.length;u++)
		{
			if(flags.contains(ExpertiseLibrary.Flag.values()[u].name()))
			{
				for(int k=0;k<skillsToRegister.size();k++)
					completeUsageMap[u].put(skillsToRegister.get(k),ID);
			}
		}
		return addIfPossible?ID:null;
	}

	@Override
	public void recompileExpertises()
	{
		for(int u=0;u<completeUsageMap.length;u++)
			completeUsageMap[u]=new Hashtable<String,String>();
		helpMap.clear();
		final List<String> V=Resources.getFileLineVector(Resources.getFileResource("skills/expertises.txt",true));
		String ID=null,WKID=null;
		for(int v=0;v<V.size();v++)
		{
			final String row=V.get(v);
			WKID=this.confirmExpertiseLine(row,ID,true);
			if(WKID==null)
				continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("ColumbiaUniv",WKID);
			else
				ID=WKID;
		}
	}

	@Override
	public Iterator<String> filterUniqueExpertiseIDList(Iterator<String> i)
	{
		final Set<String> ids=new HashSet<String>();
		for(;i.hasNext();)
		{
			final String id=i.next();
			ExpertiseDefinition def=CMLib.expertises().getDefinition(id);
			if((def != null)
			&&(!(def.ID().equals(def.getBaseName()+"1")||def.ID().equals(def.getBaseName()+"I")||(def.ID().equals(def.getBaseName())))))
			{
				continue;
			}
			ids.add(id);
		}
		return ids.iterator();
	}
	
	protected Object parseLearnID(final String msg)
	{
		if(msg==null)
			return null;
		int learnStart=msg.indexOf("^<LEARN");
		if(learnStart>=0)
		{
			int end=-1;
			learnStart=msg.indexOf("\"",learnStart+1);
			if(learnStart>=0)
				end=msg.indexOf("\"",learnStart+1);
			if(end>learnStart)
			{
				final String ID=msg.substring(learnStart+1,end);
				final Ability A=CMClass.getAbility(ID);
				if(A!=null)
					return A;
				final ExpertiseDefinition X = this.findDefinition(ID, true);
				if(X!=null)
					return X;
				return CMClass.getObjectOrPrototype(ID);
			}
		}
		return null;
	}

	@Override
	public boolean canBeTaught(MOB teacher, MOB student, Environmental item, String msg)
	{
		if(teacher.isAttributeSet(MOB.Attrib.NOTEACH))
		{
			teacher.tell(L("You are refusing to teach right now."));
			return false;
		}

		if((student.isAttributeSet(MOB.Attrib.NOTEACH))
		&&((!student.isMonster())||(!student.willFollowOrdersOf(teacher))))
		{
			if(teacher.isMonster())
				CMLib.commands().postSay(teacher,student,L("You are refusing training at this time."),true,false);
			else
				teacher.tell(L("@x1 is refusing training at this time.",student.name()));
			return false;
		}

		Object learnThis=item;
		if(learnThis==null)
			learnThis=parseLearnID(msg);

		String teachWhat="";
		if(learnThis instanceof Ability)
		{
			final Ability theA=(Ability)learnThis;
			teachWhat=theA.name();
			if(!theA.canBeTaughtBy(teacher,student))
				return false;
			if(!theA.canBeLearnedBy(teacher,student))
				return false;
			final Ability studA=student.fetchAbility(theA.ID());
			if((studA!=null)&&(studA.isSavable()))
			{
				if(teacher.isMonster())
					CMLib.commands().postSay(teacher,student,L("You already know '@x1'.",teachWhat),true,false);
				else
					teacher.tell(L("@x1 already knows how to do that.",student.name()));
				return false;
			}
		}
		else
		if(learnThis instanceof ExpertiseDefinition)
		{
			final ExpertiseDefinition theExpertise=(ExpertiseDefinition)learnThis;
			teachWhat=theExpertise.name();
			if(student.fetchExpertise(theExpertise.ID())!=null)
			{
				if(teacher.isMonster())
					CMLib.commands().postSay(teacher,student,L("You already know @x1",theExpertise.name()),true,false);
				else
					teacher.tell(L("@x1 already knows @x2",student.name(),theExpertise.name()));
				return false;
			}

			if(!myQualifiedExpertises(student).contains(theExpertise))
			{
				if(teacher.isMonster())
				{
					CMLib.commands().postSay(teacher,student,
							L("I'm sorry, you do not yet fully qualify for the expertise '@x1'.\n\rRequirements: @x2",
							theExpertise.name(),CMLib.masking().maskDesc(theExpertise.allRequirements())),true,false);
				}
				else
				{
					teacher.tell(L("@x1 does not yet fully qualify for the expertise '@x2'.\n\rRequirements: @x3",
							student.name(),theExpertise.name(),CMLib.masking().maskDesc(theExpertise.allRequirements())));
				}
				return false;
			}
			if(!theExpertise.meetsCostRequirements(student))
			{
				if(teacher.isMonster())
				{
					CMLib.commands().postSay(teacher,student,L("I'm sorry, but to learn the expertise '@x1' requires: @x2",
						theExpertise.name(),theExpertise.costDescription()),true,false);
				}
				else
					teacher.tell(L("Training for that expertise requires @x1.",theExpertise.costDescription()));
				return false;
			}
			teachWhat=theExpertise.name();
		}
		//TODO: move this to handleBeingTaught and make async!!
		try
		{
			if((!teacher.isMonster())
			&&(student.session()!=null)
			&&(!student.session().confirm(L("\n\r@x1 wants to teach you @x2.  Is this Ok (y/N)?",teacher.Name(),teachWhat),"N",10000)))
			{
				if(student.session()!=null)
					student.session().println("\n\r");
				teacher.tell(L("@x1 does not want you to.",student.charStats().HeShe()));
				return false;
			}
		}
		catch(final Exception e)
		{
			try
			{
				if(student.session()!=null)
					student.session().println("\n\r");
			}
			catch(final Exception e1)
			{
			}
			teacher.tell(L("@x1 does not answer you.",student.charStats().HeShe()));
			return false;
		}
		return true;
	}

	@Override
	public void handleBeingTaught(MOB teacher, MOB student, Environmental item, String msg)
	{
		Object learnThis=item;
		if(learnThis==null)
			learnThis=parseLearnID(msg);

		if(learnThis instanceof Ability)
		{
			final Ability theA=(Ability)learnThis;
			teacher.charStats().setStat(CharStats.STAT_WISDOM, teacher.charStats().getStat(CharStats.STAT_WISDOM)+5);
			teacher.charStats().setStat(CharStats.STAT_INTELLIGENCE, teacher.charStats().getStat(CharStats.STAT_INTELLIGENCE)+5);
			final Set<String> oldSkillSet = new HashSet<String>();
			for(Enumeration<Ability> a=student.abilities();a.hasMoreElements();)
				oldSkillSet.add(a.nextElement().ID());
			theA.teach(teacher,student);
			teacher.recoverCharStats();
			if((student.fetchAbility(theA.ID())==null) && (!oldSkillSet.contains(theA.ID())))
				student.tell(L("You failed to understand @x1.",theA.name()));
			else
			if((!teacher.isMonster()) && (!student.isMonster()))
				CMLib.leveler().postExperience(teacher, null, null, 100, false);
		}
		else
		if(learnThis instanceof ExpertiseDefinition)
		{
			final ExpertiseDefinition theExpertise=(ExpertiseDefinition)learnThis;
			theExpertise.spendCostRequirements(student);
			student.addExpertise(theExpertise.ID());
			if((!teacher.isMonster()) && (!student.isMonster()))
				CMLib.leveler().postExperience(teacher, null, null, 100, false);
		}
	}

	@Override
	public boolean postTeach(MOB teacher, MOB student, CMObject teachObj)
	{
		CMMsg msg=CMClass.getMsg(teacher,student,null,CMMsg.MSG_SPEAK,null);
		if(!teacher.location().okMessage(teacher,msg))
			return false;
		final Environmental tool=(teachObj instanceof Environmental)?(Environmental)teachObj:null;
		final String teachWhat=teachObj.name();
		final String ID=teachObj.ID();
		msg=CMClass.getMsg(teacher,student,tool,CMMsg.MSG_TEACH,L("<S-NAME> teach(es) <T-NAMESELF> '@x1'^<LEARN NAME=\"@x2\" /^>.",teachWhat,ID));
		if(!teacher.location().okMessage(teacher,msg))
			return false;
		teacher.location().send(teacher,msg);
		return true;
	}
	
	protected ExpertiseDefinition createNewExpertiseDefinition(String ID, String name, String baseName)
	{
		final ExpertiseDefinition definition = new ExpertiseDefinition()
		{
			private String								ID					= "";
			private String								name				= "";
			private String								baseName			= "";
			private String[]							data				= new String[0];
			private String								uncompiledListMask	= "";
			private String								uncompiledFinalMask	= "";
			private int									minLevel			= Integer.MIN_VALUE + 1;
			private MaskingLibrary.CompiledZMask		compiledListMask	= null;
			private final ExpertiseDefinition			parent				= null;
			private MaskingLibrary.CompiledZMask		compiledFinalMask	= null;
			private final List<SkillCost>				costs				= new LinkedList<SkillCost>();

			@Override
			public String getBaseName()
			{
				return baseName;
			}
			
			@Override
			public void setBaseName(String baseName)
			{
				this.baseName = baseName;
			}
			
			@Override
			public void setName(String name)
			{
				this.name = name;
			}
			
			@Override
			public void setID(String ID)
			{
				this.ID = ID;
			}
			
			@Override
			public void setData(String[] data)
			{
				this.data = data;
			}
			
			@Override
			public ExpertiseDefinition getParent()
			{
				return parent;
			}
			
			@Override
			public String name()
			{
				return name;
			}
			
			@Override
			public int getMinimumLevel()
			{
				if(minLevel==Integer.MIN_VALUE+1)
					minLevel=CMLib.masking().minMaskLevel(allRequirements(),0);
				return minLevel;
			}
			
			@Override
			public String[] getData()
			{
				return data;
			}
			
			@Override
			public MaskingLibrary.CompiledZMask compiledListMask()
			{
				if((this.compiledListMask==null)&&(uncompiledListMask.length()>0))
				{
					compiledListMask=CMLib.masking().maskCompile(uncompiledListMask);
					CMLib.ableMapper().addPreRequisites(ID,new Vector<String>(),uncompiledListMask.trim());
				}
				return this.compiledListMask;
			}
			
			@Override
			public MaskingLibrary.CompiledZMask compiledFinalMask()
			{
				if((this.compiledFinalMask==null)&&(uncompiledFinalMask.length()>0))
				{
					this.compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
					CMLib.ableMapper().addPreRequisites(ID,new Vector<String>(),uncompiledFinalMask.trim());
				}
				return this.compiledFinalMask;
			}
			
			@Override
			public String allRequirements()
			{
				String req=uncompiledListMask;
				if(req==null)
					req=""; else req=req.trim();
				if((uncompiledFinalMask!=null)&&(uncompiledFinalMask.length()>0))
					req=req+" "+uncompiledFinalMask;
				return req.trim();
			}
			
			@Override
			public String listRequirements()
			{
				return uncompiledListMask;
			}

			@Override
			public String finalRequirements()
			{
				return uncompiledFinalMask;
			}

			@Override
			public void addListMask(String mask)
			{
				if((mask==null)||(mask.length()==0))
					return;
				if(uncompiledListMask==null)
					uncompiledListMask=mask;
				else
					uncompiledListMask+=mask;
				compiledListMask=null;
			}

			@Override
			public void addFinalMask(String mask)
			{
				if((mask==null)||(mask.length()==0))
					return;
				if(uncompiledFinalMask==null)
					uncompiledFinalMask=mask;
				else
					uncompiledFinalMask+=mask;
				compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
				CMLib.ableMapper().addPreRequisites(ID,new Vector<String>(),uncompiledFinalMask.trim());
			}

			@Override
			public void addCost(CostType type, Double value)
			{
				costs.add(createNewSkillCost(type,value));
			}
			
			@Override
			public String costDescription()
			{
				final StringBuffer costStr=new StringBuffer("");
				for(final SkillCost cost : costs)
					costStr.append(cost.requirements(null)).append(", ");
				if(costStr.length()==0)
					return "";
				return costStr.substring(0,costStr.length()-2);
			}
			
			@Override
			public boolean meetsCostRequirements(MOB mob)
			{
				for(final SkillCost cost : costs)
				{
					if(!cost.doesMeetCostRequirements(mob))
						return false;
				}
				return true;
			}
			
			@Override
			public void spendCostRequirements(MOB mob)
			{
				for(final SkillCost cost : costs)
					cost.spendSkillCost(mob);
			}
			
			@Override
			public int compareTo(CMObject o)
			{
				return (o == this) ? 0 : 1;
			}

			@Override
			public String ID()
			{
				return ID;
			}

			@Override
			public CMObject newInstance()
			{
				return this;
			}

			@Override
			public CMObject copyOf()
			{
				return this;
			}

			@Override
			public void initializeClass()
			{
			}
		};
		definition.setID(ID.toUpperCase());
		definition.setName(name);
		definition.setBaseName(baseName);
		return definition;
	}
}
