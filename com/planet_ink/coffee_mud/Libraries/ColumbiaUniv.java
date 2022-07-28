package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.CostType;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.SkillCost;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2006-2022 Bo Zimmerman

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

	protected SHashtable<String, ExpertiseDefinition>	completeEduMap		= new SHashtable<String, ExpertiseDefinition>();
	protected SHashtable<String, List<String>>			baseEduSetLists		= new SHashtable<String, List<String>>();

	@SuppressWarnings("unchecked")
	protected Map<String, String>[]		completeUsageMap	= new Hashtable[XType.values().length];
	@SuppressWarnings("unchecked")
	protected Map<String, String[]>[]	completeUsageMaps	= new Hashtable[XType.values().length];
	protected Map<String, String>		helpMap				= new TreeMap<String, String>();
	protected DVector					rawDefinitions		= new DVector(7);

	protected ExpertiseLibrary.ExpertiseDefinition addDefinition(final String ID, final String name, final String baseName, final String listMask, final String finalMask, final String[] costs, final String[] data)
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
		def.setStageNames((data==null)?new String[0]:data);
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
	public boolean addModifyDefinition(final String codedLine, final boolean andSave)
	{
		int x=codedLine.indexOf('=');
		if(x<0)
			return false;
		String ID=codedLine.substring(0,x).toUpperCase().trim();
		ID=CMStrings.replaceAll(CMStrings.replaceAll(ID,"@X2",""),"@X1","").toUpperCase();
		final String resp = this.confirmExpertiseLine(codedLine, ID, false);
		if((resp!=null)&&(resp.toLowerCase().startsWith("error")))
			return false;
		if(andSave)
		{
			String baseID=null;
			if(completeEduMap.containsKey(ID.trim().toUpperCase()))
			{
				final ExpertiseDefinition def=completeEduMap.get(ID.trim().toUpperCase());
				baseID=def.getBaseName();
			}
			else
			{
				for(final String defID : completeEduMap.keySet())
				{
					final ExpertiseDefinition def = completeEduMap.get(defID);
					if(def.getBaseName().equalsIgnoreCase(ID))
						baseID = def.getBaseName();
				}
			}
			if(baseID != null)
			{
				final String expertiseFilename="skills/expertises.txt";
				String fileName=Resources.getRawFileResourceName(expertiseFilename, false);
				final List<String> lines = getExpertiseLines();
				final StringBuilder buf = new StringBuilder("");
				boolean readyToSave=false;
				for(final String l : lines)
				{
					if(l.trim().startsWith("#"))
					{
						if(l.trim().startsWith("#FILE:"))
						{
							if(readyToSave)
								break;
							fileName = l.trim().substring(6).trim();
							buf.setLength(0);
							continue;
						}
					}
					else
					{
						x=l.indexOf('=');
						if(x>0)
						{
							String baseId=l.substring(0,x).trim().toUpperCase();
							baseId=CMStrings.replaceAll(CMStrings.replaceAll(baseId,"@X2",""),"@X1","").toUpperCase();
							if(baseId.equalsIgnoreCase(baseID))
							{
								buf.append(codedLine).append("\n\r");
								readyToSave=true;
								continue;
							}
						}
					}
					buf.append(l).append("\n\r");
				}
				if(readyToSave && (buf.length()>0))
					new CMFile(fileName,null).saveText(buf);
			}
			else
			{
				final CMFile F=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,CMFile.FLAG_LOGERRORS);
				F.saveText("\n"+codedLine,true);
			}
			Resources.removeResource(Resources.makeFileResourceName("skills/expertises.txt"));
			CMLib.expertises().recompileExpertises();
		}
		return true;
	}

	@Override
	public String findExpertiseID(String ID, final boolean exact)
	{
		if(ID==null)
			return null;
		ID=ID.toUpperCase();
		if(helpMap.containsKey(ID))
			return ID;
		if(exact)
			return null;
		for(final String key :  helpMap.keySet())
		{
			if(key.startsWith(ID))
				return key;
		}
		for(final String key :  helpMap.keySet())
		{
			if(CMLib.english().containsString(key, ID))
				return key;
		}
		return null;
	}


	@Override
	public String getExpertiseHelp(final String ID)
	{
		if(ID==null)
			return null;
		return helpMap.get(ID.toUpperCase());
	}

	@Override
	public boolean delDefinition(final String ID, final boolean andSave)
	{
		final List<String> delThese = new LinkedList<String>();
		String baseID=null;
		if(completeEduMap.containsKey(ID.trim().toUpperCase()))
		{
			final ExpertiseDefinition def=completeEduMap.get(ID.trim().toUpperCase());
			delThese.add(ID.trim().toUpperCase());
			baseID=def.getBaseName();
		}
		else
		{
			for(final String defID : completeEduMap.keySet())
			{
				final ExpertiseDefinition def = completeEduMap.get(defID);
				if(def.getBaseName().equalsIgnoreCase(ID))
				{
					baseID = def.getBaseName();
					delThese.add(defID);
				}
			}
		}
		for(final String id : delThese)
			completeEduMap.remove(id);
		baseEduSetLists.clear();
		if(andSave && (baseID != null))
		{
			final String expertiseFilename="skills/expertises.txt";
			String fileName=Resources.getRawFileResourceName(expertiseFilename, false);
			final List<String> lines = getExpertiseLines();
			final StringBuilder buf = new StringBuilder("");
			boolean readyToSave=false;
			boolean delNextHelp=false;
			for(final String l : lines)
			{
				if(l.trim().startsWith("#"))
				{
					if(l.trim().startsWith("#FILE:"))
					{
						if(readyToSave)
							break;
						fileName = l.trim().substring(6).trim();
						buf.setLength(0);
					}
				}
				else
				if(delNextHelp && l.trim().startsWith("HELP_=<EXPERTISE>"))
				{
					delNextHelp=false;
					continue;
				}
				else
				{
					delNextHelp=false;
					final int x=l.indexOf('=');
					if(x>0)
					{
						String baseId=l.substring(0,x).trim().toUpperCase();
						baseId=CMStrings.replaceAll(CMStrings.replaceAll(baseId,"@X2",""),"@X1","").toUpperCase();
						if(baseId.equalsIgnoreCase(baseID))
						{
							delNextHelp=true;
							readyToSave=true;
							continue;
						}
					}
				}
				buf.append(l).append("\n\r");
			}
			if(readyToSave && (buf.length()>0))
				new CMFile(fileName,null).saveText(buf);
			Resources.removeResource(fileName);
			recompileExpertises();
		}
		return delThese.size()>0;
	}

	@Override
	public Enumeration<ExpertiseDefinition> definitions()
	{
		return completeEduMap.elements();
	}

	@Override
	public ExpertiseDefinition getDefinition(final String ID)
	{
		if(ID!=null)
			return completeEduMap.get(ID.trim().toUpperCase());
		return null;
	}

	@Override
	public ExpertiseDefinition findDefinition(final String ID, final boolean exactOnly)
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
	public List<ExpertiseDefinition> myQualifiedExpertises(final MOB mob)
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
	public List<ExpertiseDefinition> myListableExpertises(final MOB mob)
	{
		ExpertiseDefinition D=null;
		final List<ExpertiseDefinition> V=new Vector<ExpertiseDefinition>();
		for(final Enumeration<ExpertiseDefinition> e=definitions();e.hasMoreElements();)
		{
			D=e.nextElement();
			if((D.compiledListMask()==null)||(CMLib.masking().maskCheck(D.compiledListMask(),mob,true)))
			{
				V.add(D);
			}
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
	public int getExpertiseLevelCached(final MOB mob, final String abilityID, final ExpertiseLibrary.XType code)
	{
		if((mob==null)||(code==null)||(abilityID==null)||(abilityID.length()==0))
			return 0;
		int expertiseLvl=0;
		final int[][] usageCache=mob.getAbilityUsageCache(abilityID);
		if(usageCache[Ability.CACHEINDEX_EXPERTISE]!=null)
			expertiseLvl=usageCache[Ability.CACHEINDEX_EXPERTISE][code.ordinal()];
		else
		{
			final int[] xFlagCache=new int[ExpertiseLibrary.XType.values().length];
			final CharClass charClass = mob.baseCharStats().getCurrentClass();
			if(charClass == null)
				return 0;
			for(final ExpertiseLibrary.XType flag : ExpertiseLibrary.XType.values())
			{
				xFlagCache[flag.ordinal()]=getExpertiseLevelCalced(mob,abilityID,flag)
											+charClass.addedExpertise(mob, flag, abilityID);
			}
			usageCache[Ability.CACHEINDEX_EXPERTISE]=xFlagCache;
			expertiseLvl = xFlagCache[code.ordinal()];
		}
		return expertiseLvl;
	}

	private int getStageNumber(final ExpertiseDefinition D)
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
	public int getHighestListableStageBySkill(final MOB mob, final String ableID, final ExpertiseLibrary.XType flag)
	{
		final String expertiseID = completeUsageMap[flag.ordinal()].get(ableID);
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
					final int stage=getStageNumber(D);
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
						final int stage=getStageNumber(def);
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

	private String expertMath(String s,final int l)
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
	public int numStages(final String baseExpertiseCode)
	{
		return getStageCodes(baseExpertiseCode).size();
	}

	protected String getGuessedBaseExpertiseName(final String expertiseCode)
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
	public String getApplicableExpertise(final String abilityID, final XType code)
	{
		return completeUsageMap[code.ordinal()].get(abilityID);
	}

	@Override
	public String[] getApplicableExpertises(final String abilityID, final XType code)
	{
		if(code == null)
		{
			final Set<String> all=new TreeSet<String>();
			for(final XType f : XType.values())
			{
				final String[] set=completeUsageMaps[f.ordinal()].get(abilityID);
				if(set != null)
					all.addAll(Arrays.asList(set));
			}
			return all.toArray(new String[0]);
		}
		return completeUsageMaps[code.ordinal()].get(abilityID);
	}

	@Override
	public ExpertiseDefinition getConfirmedDefinition(final MOB mob, final String ID)
	{
		if(mob == null)
			return getDefinition(ID);
		if(ID!=null)
		{
			final ExpertiseDefinition def=getDefinition(ID);
			if(def != null)
			{
				final Pair<String,Integer> e=mob.fetchExpertise(ID);
				if((e!=null)
				&&(e.getValue()!=null))
				{
					final int level = getConfirmedExpertiseLevel(mob, def.getBaseName(), e);
					if(level == e.second.intValue())
						return def;
					if(level == 0)
						return null;
					return getDefinition(def.getBaseName()+level);
				}
			}
			return completeEduMap.get(ID.trim().toUpperCase());
		}
		return null;
	}

	protected int getConfirmedExpertiseLevel(final MOB mob, final String baseID, final Pair<String,Integer> e)
	{
		if((!mob.isMonster())
		&&(!CMSecurity.isAllowedEverywhere(mob, SecFlag.ALLSKILLS)))
		{
			final ExpertiseDefinition def=getDefinition(baseID+e.getValue().toString());
			if((def == null)
			||(!CMLib.masking().maskCheck(def.compiledListMask(), mob, true))
			||(!CMLib.masking().maskCheck(def.compiledFinalMask(), mob, true)))
			{
				final List<String> defList = getStageCodes(baseID);
				for(int i = defList.size()-e.getValue().intValue();i<defList.size();i++)
				{
					final ExpertiseDefinition def2=getDefinition(defList.get(i));
					if((def2 != null)
					&&(CMLib.masking().maskCheck(def2.compiledListMask(), mob, true))
					&&(CMLib.masking().maskCheck(def2.compiledFinalMask(), mob, true)))
						return this.getStageNumber(def2);
				}
				return 0;
			}
		}
		return e.getValue().intValue();
	}

	@Override
	public int getExpertiseLevelCalced(final MOB mob, final String abilityID, final XType code)
	{
		final String[] applicableExpIDs = getApplicableExpertises(abilityID, code);
		if((applicableExpIDs==null)||(applicableExpIDs.length<1))
			return 0;
		final Pair<String,Integer> e=mob.fetchExpertise(applicableExpIDs[0]);
		if((e!=null)
		&&(e.getValue()!=null))
			return e.getValue().intValue();
			//return getConfirmedExpertiseLevel(mob, applicableExpIDs[0], e);
		if(applicableExpIDs.length<2)
			return 0;
		for(final String expID : applicableExpIDs)
		{
			final Pair<String,Integer> e2=mob.fetchExpertise(expID);
			if((e2!=null)
			&&(e2.getValue()!=null))
				return e2.getValue().intValue();
				//return getConfirmedExpertiseLevel(mob, applicableExpIDs[0], e2);
		}
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
				case XP:
					ofWhat = "experience points";
					break;
				case GOLD:
					ofWhat = CMLib.beanCounter().getDenominationName(mob, value.doubleValue());
					break;
				case PRACTICE:
					ofWhat = "practice points";
					break;
				case QP:
					ofWhat = "quest points";
					break;
				default:
					ofWhat = CMLib.english().makePlural(costType.name().toLowerCase());
					break;
				}
				return ofWhat;
			}

			@Override
			public String requirements(final MOB mob)
			{
				switch(costType)
				{
				case XP:
					return value.intValue() + " XP";
				case QP:
					return value.intValue() + " quest pts";
				case GOLD:
				{
					if (mob == null)
						return CMLib.beanCounter().abbreviatedPrice("", value.doubleValue());
					else
						return CMLib.beanCounter().abbreviatedPrice(mob, value.doubleValue());
				}
				default:
					return value.intValue() + " " + ((value.intValue() == 1) ? costType.name().toLowerCase() : CMLib.english().makePlural(costType.name().toLowerCase()));
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
				case XP:
					CMLib.leveler().postExperience(student, null, "", -value.intValue(), true);
					break;
				case GOLD:
					CMLib.beanCounter().subtractMoney(student, value.doubleValue());
					break;
				case TRAIN:
					student.setTrains(student.getTrains() - value.intValue());
					break;
				case PRACTICE:
					student.setPractices(student.getPractices() - value.intValue());
					break;
				case QP:
					student.setQuestPoint(student.getQuestPoint() - value.intValue());
					break;
				}
			}
		};
	}

	@Override
	public String confirmExpertiseLine(String row, String ID, final boolean addIfPossible)
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
					def.setStageNames(CMParms.parseCommas(row,true).toArray(new String[0]));
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
						def.setStageNames(CMParms.parseCommas(row,true).toArray(new String[0]));
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
					helpMap.put(WKID,row);
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
				{
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
			didOne=didOne||flags.contains(ExpertiseLibrary.XType.values()[u].name());
		}
		if(!didOne)
			return "Error: No flags ("+parts.get(2).toUpperCase()+") were set: "+ID+"="+row;
		if(addIfPossible)
		{
			final Set<XType> fflags = new HashSet<XType>();
			for(final String f : flags)
			{
				final XType fl = (XType)CMath.s_valueOf(XType.class, f.toUpperCase().trim());
				if(fl != null)
					fflags.add(fl);
			}
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
					def.addRawMasks(listMask, finalMask);
					def.getFlagTypes().addAll(fflags);
					def.compiledFinalMask();
					def.compiledListMask();
				}
			}
		}
		ID=CMStrings.replaceAll(ID,"@X1","");
		ID=CMStrings.replaceAll(ID,"@X2","");
		for(int u=0;u<completeUsageMap.length;u++)
		{
			if(flags.contains(ExpertiseLibrary.XType.values()[u].name()))
			{
				for(int k=0;k<skillsToRegister.size();k++)
				{
					final String skid = skillsToRegister.get(k);
					completeUsageMap[u].put(skid, ID);
					if(!completeUsageMaps[u].containsKey(skid))
						completeUsageMaps[u].put(skid, new String[]{ID});
					else
					{
						final String[] oldSet=completeUsageMaps[u].get(skid);
						final String[] newSet=Arrays.copyOf(oldSet, oldSet.length+1);
						newSet[newSet.length-1]=ID;
						completeUsageMaps[u].put(skid, newSet);
					}
				}
			}
		}
		return addIfPossible?ID:null;
	}

	@Override
	public String getExpertiseInstructions()
	{
		final StringBuilder inst = new StringBuilder("");
		final String expertiseFilename="skills/expertises.txt";
		final List<String> V=Resources.getFileLineVector(Resources.getFileResource(expertiseFilename,true));
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).startsWith("#"))
				inst.append(V.get(v).substring(1)+"\n\r");
			else
			if(V.get(v).length()>0)
				break;
		}
		return inst.toString();
	}

	protected List<String> getExpertiseLines()
	{
		final String expertiseFilename="skills/expertises.txt";
		final List<String> V=Resources.getFileLineVector(Resources.getFileResource(expertiseFilename,true));
		Resources.removeResource(Resources.makeFileResourceName("skills/expertises.txt"));
		for(int i=2;i<99;i++)
		{
			final String fileName=Resources.getRawFileResourceName(expertiseFilename, false)+"."+i;
			final StringBuffer buf = Resources.getFileResource(expertiseFilename+"."+i,false);
			Resources.removeResource(fileName);
			if(buf.length()==0)
				break;
			V.add("#FILE:"+fileName);
			V.addAll(Resources.getFileLineVector(buf));
		}
		return V;
	}

	@Override
	public void recompileExpertises()
	{
		for(int u=0;u<completeUsageMap.length;u++)
			completeUsageMap[u]=new Hashtable<String,String>();
		for(int u=0;u<completeUsageMaps.length;u++)
			completeUsageMaps[u]=new Hashtable<String,String[]>();
		helpMap.clear();
		final List<String> V = getExpertiseLines();
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
	public Iterator<String> filterUniqueExpertiseIDList(final Iterator<String> i)
	{
		final Set<String> ids=new HashSet<String>();
		for(;i.hasNext();)
		{
			final String id=i.next();
			final ExpertiseDefinition def=getDefinition(id);
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
	public boolean canBeTaught(final MOB teacher, final MOB student, final Environmental item, final String msg)
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
		return true;
	}

	@Override
	public void handleBeingTaught(final MOB teacher, final MOB student, final Environmental item, final String msg, final int add)
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
			for(final Enumeration<Ability> a=student.abilities();a.hasMoreElements();)
				oldSkillSet.add(a.nextElement().ID());
			theA.teach(teacher,student);
			teacher.recoverCharStats();
			final Ability studentA=student.fetchAbility(theA.ID());
			if((studentA==null) && (!oldSkillSet.contains(theA.ID())))
				student.tell(L("You failed to understand @x1.",theA.name()));
			else
			if((!teacher.isMonster()) && (!student.isMonster()))
				CMLib.leveler().postExperience(teacher, null, null, 100, false);
			if((studentA!=null)
			&& (!oldSkillSet.contains(theA.ID()))
			&& (add!=0))
			{
				int newProficiency=studentA.proficiency() + add;
				if(newProficiency > 75)
					newProficiency = 75;
				if(newProficiency < 0)
					newProficiency = 0;
				studentA.setProficiency(newProficiency);
				final Ability studentEffA = student.fetchEffect(theA.ID());
				if(studentEffA != null)
					studentEffA.setProficiency(newProficiency);
			}
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
	public boolean confirmAndTeach(final MOB teacherM, final MOB studentM, final CMObject teachableO, final Runnable callBack)
	{
		if((teacherM==null)||(studentM==null))
		{
			if(callBack != null)
				callBack.run();
			return false;
		}
		final Session sess=studentM.session();
		final Room R=studentM.location();
		if((sess==null)
		||(R==null)
		||((!teacherM.isPlayer())
			&&((teacherM.amFollowing()==null)||(!teacherM.amUltimatelyFollowing().isPlayer()))))
		{
			final boolean success=postTeach(teacherM,studentM,teachableO);
			if(callBack != null)
				callBack.run();
			return success;
		}

		final String name;
		if(teachableO instanceof Ability)
			name=((Ability)teachableO).Name();
		else
		if(teachableO instanceof ExpertiseLibrary.ExpertiseDefinition)
			name=((ExpertiseLibrary.ExpertiseDefinition)teachableO).name();
		else
			name=L("Something");

		final Environmental tool=(teachableO instanceof Environmental)?(Environmental)teachableO:null;
		final String teachWhat=teachableO.name();
		final String ID=teachableO.ID();
		final CMMsg msg=CMClass.getMsg(teacherM,studentM,tool,CMMsg.MSG_TEACH,null,teachWhat,ID);
		if((!R.show(teacherM, studentM, CMMsg.MSG_SPEAK, L("<S-NAME> offer(s) to teach <T-NAME>.")))
		||(!R.okMessage(teacherM, msg)))
		{
			if(callBack != null)
				callBack.run();
			return false;
		}

		sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",10000)
		{
			final MOB teacher = teacherM;
			final MOB student = studentM;
			final Session session = sess;
			final CMObject teachable=teachableO;
			final String teachWhat= name;
			final Runnable postCallback=callBack;

			@Override
			public void showPrompt()
			{
				sess.promptPrint(L("\n\r@x1 wants to teach you @x2.  Is this Ok (y/N)?",teacher.Name(),teachWhat));
			}

			@Override
			public void timedOut()
			{
				teacher.tell(L("@x1 does not answer you.",student.charStats().HeShe()));
				if(postCallback != null)
					postCallback.run();
			}

			@Override
			public void callBack()
			{
				try
				{
					if(this.input.equals("Y"))
					{
						if(studentM.location()!=teacherM.location())
						{
							studentM.tell(L("@x1 vanished.",teacherM.name()));
							teacherM.tell(L("@x1 vanished.",studentM.name()));
						}
						else
							postTeach(teacher,student,teachable);
					}
					else
					{
						if(!session.isStopped())
							session.println("\n\r");
						teacher.tell(L("@x1 does not want you to.",student.charStats().HeShe()));
					}
				}
				finally
				{
					if(postCallback != null)
						postCallback.run();
				}
			}
		});
		return true;
	}

	@Override
	public boolean postTeach(final MOB teacher, final MOB student, final CMObject teachObj)
	{
		CMMsg msg=CMClass.getMsg(teacher,student,null,CMMsg.MSG_SPEAK,null);
		if(!teacher.location().okMessage(teacher,msg))
			return false;
		final Environmental tool=(teachObj instanceof Environmental)?(Environmental)teachObj:null;
		final String teachWhat=teachObj.name();
		final String ID=teachObj.ID();
		msg=CMClass.getMsg(teacher,student,tool,CMMsg.MSG_TEACH,L("<S-NAME> teach(es) <T-NAMESELF> '@x1' ^<LEARN NAME=\"@x2\" /^>.",teachWhat,ID));
		if(!teacher.location().okMessage(teacher,msg))
			return false;
		teacher.location().send(teacher,msg);
		return true;
	}

	protected ExpertiseDefinition createNewExpertiseDefinition(final String ID, final String name, final String baseName)
	{
		final ExpertiseDefinition definition = new ExpertiseDefinition()
		{
			private String								ID					= "";
			private String								name				= "";
			private String								baseName			= "";
			private String[]							data				= new String[0];
			private String								uncompiledListMask	= "";
			private String								uncompiledFinalMask	= "";
			private String								rawListMask			= "";
			private String								rawFinalMask		= "";
			private int									minLevel			= Integer.MIN_VALUE + 1;
			private MaskingLibrary.CompiledZMask		compiledListMask	= null;
			private final ExpertiseDefinition			parent				= null;
			private MaskingLibrary.CompiledZMask		compiledFinalMask	= null;
			private final List<SkillCost>				costs				= new LinkedList<SkillCost>();
			private final Set<XType>					xTypes				= new HashSet<XType>();

			@Override
			public String getBaseName()
			{
				return baseName;
			}

			@Override
			public void setBaseName(final String baseName)
			{
				this.baseName = baseName;
			}

			@Override
			public void setName(final String name)
			{
				this.name = name;
			}

			@Override
			public void setID(final String ID)
			{
				this.ID = ID;
			}

			@Override
			public void setStageNames(final String[] data)
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
			public String[] getStageNames()
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
					req="";
				else
					req=req.trim();
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
			public void addListMask(final String mask)
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
			public void addFinalMask(final String mask)
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
			public void addRawMasks(final String listMask, final String finalMask)
			{
				this.rawListMask = listMask;
				this.rawFinalMask = finalMask;
			}

			@Override
			public String rawListMask()
			{
				return this.rawListMask;
			}

			@Override
			public String rawFinalMask()
			{
				return this.rawFinalMask;
			}

			@Override
			public void addCost(final CostType type, final Double value)
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
			public boolean meetsCostRequirements(final MOB mob)
			{
				for(final SkillCost cost : costs)
				{
					if(!cost.doesMeetCostRequirements(mob))
						return false;
				}
				return true;
			}

			@Override
			public void spendCostRequirements(final MOB mob)
			{
				for(final SkillCost cost : costs)
					cost.spendSkillCost(mob);
			}

			@Override
			public int compareTo(final CMObject o)
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

			@Override
			public Set<XType> getFlagTypes()
			{
				return xTypes;
			}
		};
		definition.setID(ID.toUpperCase());
		definition.setName(name);
		definition.setBaseName(baseName);
		return definition;
	}
}
