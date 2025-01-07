package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.CompoundingRule;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CommonSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.CompLocation;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2015-2024 Bo Zimmerman

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
public class CMAbleMap extends StdLibrary implements AbilityMapper
{
	@Override
	public String ID()
	{
		return "CMAbleMap";
	}

	protected Map<String, Map<String, AbilityMapping>>
									completeAbleMap 		= new SHashtable<String, Map<String, AbilityMapping>>();
	protected Map<String, Map<String, AbilityMapping>>
									reverseAbilityMap		= new TreeMap<String, Map<String, AbilityMapping>>();
	public final static Map<String,AbilityMapping>
									emptyAbleMap 			= new ReadOnlySortedMap<String,AbilityMapping>();
	protected Map<String,List<CompoundingRule>>
									compounders				= new SHashtable<String, List<CompoundingRule>>();
	protected List<CompoundingRule>	compoundingRules		= new Vector<CompoundingRule>();
	protected volatile boolean		compoundingRulesLoaded	= false;
	protected Map<String, Integer>	lowestQualifyingLevelMap= new SHashtable<String, Integer>();
	protected Map<String, Integer>	maxProficiencyMap	 	= new SHashtable<String, Integer>();
	protected Map<String, Object>	allows  				= new SHashtable<String, Object>();
	protected List<AbilityMapping>	eachClassSet			= null;
	protected final Integer[]		costOverrides			= new Integer[AbilCostType.values().length];
	protected CMath.CompiledFormula	proficiencyGainFormula	= null;

	@Override
	public boolean activate()
	{
		super.activate();
		proficiencyGainFormula = CMath.compileMathExpression(CMProps.getVar(CMProps.Str.FORMULA_PROFGAIN));
		return true;
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final boolean autoGain)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,SecretFlag.PUBLIC,new Vector<String>(),"");
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final Integer[] costOverrides)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",false,SecretFlag.PUBLIC,new Vector<String>(),"",costOverrides);
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final boolean autoGain,
												final String extraMasks)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,SecretFlag.PUBLIC,new Vector<String>(),extraMasks);
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final boolean autoGain,
												final List<String> skillPreReqs)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,SecretFlag.PUBLIC,skillPreReqs,"");
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final boolean autoGain,
												final List<String> skillPreReqs,
												final String extraMasks)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,0,100,"",autoGain,SecretFlag.PUBLIC,skillPreReqs,extraMasks);
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final String defParm,
												final boolean autoGain)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defParm,autoGain,SecretFlag.PUBLIC,new Vector<String>(),"");
	}

	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final String defParm,
												final boolean autoGain,
												final String extraMasks)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defParm,autoGain,SecretFlag.PUBLIC,new Vector<String>(),extraMasks);
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final boolean autoGain)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,"",autoGain,SecretFlag.PUBLIC,new Vector<String>(),"");
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final boolean autoGain,
												final List<String> skillPreReqs)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,"",autoGain,SecretFlag.PUBLIC,skillPreReqs,"");
	}

	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final boolean autoGain,
												final String extraMasks)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,"",autoGain,SecretFlag.PUBLIC,new Vector<String>(),extraMasks);
	}

	@Override
	public int numMappedAbilities()
	{
		return reverseAbilityMap.size();
	}

	@Override
	public AbilityMapping delCharAbilityMapping(final String ID, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String, AbilityMapping> ableMap = completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.remove(abilityID);
		}
		final Map<String,AbilityMapping> revT=reverseAbilityMap.get(abilityID);
		if(revT!=null)
			return revT.remove(ID);
		return null;
	}

	@Override
	public void delCharMappings(final String ID)
	{
		if(completeAbleMap.containsKey(ID))
			completeAbleMap.remove(ID);
		for(final String abilityID : reverseAbilityMap.keySet())
		{
			final Map<String,AbilityMapping> revT=reverseAbilityMap.get(abilityID);
			if(revT!=null)
				revT.remove(ID);
		}
	}

	@Override
	public Enumeration<AbilityMapping> getClassAbles(final String ID, final boolean addAll)
	{
		if(!completeAbleMap.containsKey(ID))
			completeAbleMap.put(ID,new SHashtable<String, AbilityMapping>());
		final Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		final Map<String, AbilityMapping> allAbleMap=completeAbleMap.get("All");
		if((!addAll)||(allAbleMap==null))
			return new IteratorEnumeration<AbilityMapping>(ableMap.values().iterator());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Iterator<AbilityMapping>[] iters=new Iterator[]
		{
			ableMap.values().iterator(),
			new FilteredIterator(allAbleMap.values().iterator(),
				new Filterer<Object>()
				{
					@Override
					public boolean passesFilter(final Object obj)
					{
						if((obj instanceof AbilityMapping)
						&&(ableMap.containsKey(((AbilityMapping)obj).abilityID())))
							return false;
						return true;
					}
				}
			)
		};
		return new IteratorEnumeration<AbilityMapping>(new MultiIterator<AbilityMapping>(iters));
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret,new Vector<String>(),"");
	}

	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret,
												final String extraMasks)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret,new Vector<String>(),extraMasks);
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret,
												final List<String> preReqSkillsList,
												final String extraMask)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);
	}

	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final int maxProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,new Vector<String>(),"");
	}

	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final int maxProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret,
												final String extraMasks)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,new Vector<String>(),extraMasks);
	}

	@Override
	public AbilityMapping addDynaAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret,
												final String extraMask)
	{
		delCharAbilityMapping(ID,abilityID);
		if(CMSecurity.isAbilityDisabled(ID.toUpperCase()))
			return null;
		Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		if(ableMap == null)
		{
			ableMap=new SHashtable<String,AbilityMapping>();
			completeAbleMap.put(ID,ableMap);
		}
		final AbilityMapping able = makeAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,100,defaultParam,autoGain,secret, false,new Vector<String>(),extraMask,null);
		mapAbilityFinal(abilityID,ableMap,able);
		return able;
	}

	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final int maxProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret,
												final List<String> preReqSkillsList,
												final String extraMask)
	{
		return addCharAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,defaultParam,autoGain,secret,preReqSkillsList,extraMask,null);
	}

	@Override
	public void addPreRequisites(final String ID, final List<String> preReqSkillsList, final String extraMask)
	{
		if(preReqSkillsList==null)
			return;
		for(int v=0;v<preReqSkillsList.size();v++)
		{
			String s=preReqSkillsList.get(v);
			final int x=s.indexOf('(');
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
				@SuppressWarnings("unchecked")
				SVector<String> V=(SVector<String>)allows.get(s);
				if(V==null)
				{
					V=new SVector<String>();
					allows.put(s,V);
				}
				if(!V.contains(ID))
					V.addElement(ID);
			}
		}
		if((extraMask!=null)&&(extraMask.trim().length()>0))
		{
			final List<String> preReqsOf=CMLib.masking().getAbilityEduReqs(extraMask);
			for(int v=0;v<preReqsOf.size();v++)
			{
				final String s=preReqsOf.get(v);
				if((s.indexOf('*')>=0)||(s.indexOf(',')>=0))
				{
					String ID2=ID;
					while(allows.containsValue("*"+ID2))
						ID2="*"+ID2;
					allows.put("*"+ID2,s);
				}
				else
				{
					@SuppressWarnings("unchecked")
					SVector<String> V=(SVector<String>)allows.get(s);
					if(V==null)
					{
						V=new SVector<String>();
						allows.put(s,V);
					}
					if(!V.contains(ID))
						V.addElement(ID);
				}
			}
		}
	}

	@Override
	public List<QualifyingID> getClassAllowsList(final String classID)
	{
		final List<AbilityMapping> ABLES=getUpToLevelListings(classID,CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL),false,false);
		final SHashtable<String,Integer> alreadyDone=new SHashtable<String,Integer>();
		final List<QualifyingID> DV=new Vector<QualifyingID>(2);
		Integer Ix=null;
		for(final AbilityMapping able : ABLES)
		{
			for(final Iterator<String> i=getAbilityAllowsList(able.abilityID());i.hasNext();)
			{
				final String s = i.next();
				Ix=alreadyDone.get(s);
				if(Ix==null)
				{
					alreadyDone.put(s, Integer.valueOf(DV.size()));
					DV.add(new QualifyingID()
					{
						private int qualifyingLevel = able.qualLevel();

						@Override
						public String ID()
						{
							return s;
						}

						@Override
						public int qualifyingLevel()
						{
							return qualifyingLevel;
						}

						@Override
						public QualifyingID qualifyingLevel(final int newLevel)
						{
							qualifyingLevel = newLevel;
							return this;
						}
					});
				}
				else
				{
					final QualifyingID Q=DV.get(Ix.intValue());
					if((Q!=null)&&(Q.qualifyingLevel()>able.qualLevel()))
						Q.qualifyingLevel(able.qualLevel());
				}
			}
		}
		return DV;
	}

	@Override
	public Iterator<String> getAbilityAllowsList(final String ableID)
	{
		String abilityID=null;
		Vector<String> remove=null;
		for(final String KEYID : allows.keySet())
		{
			if(KEYID.startsWith("*"))
			{
				abilityID=(String)allows.get(KEYID);
				if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(',')>0))
				{
					final List<String> orset=getOrSet(ableID,abilityID);
					if(orset.size()!=0)
					{
						String KEYID2=KEYID;
						while(KEYID2.startsWith("*"))
							KEYID2=KEYID2.substring(1);
						addPreRequisites(KEYID2,orset,"");
						if(remove==null)
							remove=new Vector<String>();
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
		@SuppressWarnings("unchecked")
		final SVector<String> set = (SVector<String>)allows.get(ableID);
		return (set==null)?new SVector<String>(1).iterator():set.iterator();
	}

	@Override
	public AbilityMapping addCharAbilityMapping(final String ID,
												final int qualLevel,
												final String abilityID,
												final int defaultProficiency,
												final int maxProficiency,
												final String defaultParam,
												final boolean autoGain,
												final SecretFlag secret,
												final List<String> preReqSkillsList,
												final String extraMask,
												final Integer[] costOverrides)
	{
		delCharAbilityMapping(ID,abilityID);
		if(CMSecurity.isAbilityDisabled(ID.toUpperCase()))
			return null;
		Map<String, AbilityMapping> ableMap=completeAbleMap.get(ID);
		if(ableMap == null)
		{
			ableMap=new SHashtable<String,AbilityMapping>();
			completeAbleMap.put(ID,ableMap);
			handleEachAndClassAbility(ableMap, getAllQualifiesMap(null), ID);
		}
		final AbilityMapping able = makeAbilityMapping(ID,qualLevel,abilityID,defaultProficiency,maxProficiency,
				defaultParam,autoGain,secret,false,preReqSkillsList,extraMask,costOverrides);
		mapAbilityFinal(abilityID,ableMap,able);
		return able;
	}

	@Override
	public AbilityMapping newAbilityMapping()
	{
		return new AbilityMapping()
		{
			private String				ID						= "";
			private String				abilityID				= "";
			private int					qualLevel				= -1;
			private boolean				autoGain				= false;
			private int					defaultProficiency		= 0;
			private int					maxProficiency			= 100;
			private String				defaultParm				= "";
			private SecretFlag			isSecret				= SecretFlag.PUBLIC;
			private boolean				isAllQualified			= false;
			private DVector				skillPreReqs			= new DVector(2);
			private String				extraMask				= "";
			private String				originalSkillPreReqList	= "";
			private Integer[]			costOverrides			= new Integer[AbilCostType.values().length];
			private boolean				allQualifyFlag			= false;
			private Map<String, String>	extFields				= new Hashtable<String, String>(1);

			@Override
			public String ID()
			{
				return ID;
			}

			@Override
			public AbilityMapping ID(final String newValue)
			{
				ID = newValue;
				return this;
			}

			@Override
			public String abilityID()
			{
				return abilityID;
			}

			@Override
			public AbilityMapping abilityID(final String newValue)
			{
				abilityID = newValue;
				return this;
			}

			@Override
			public int qualLevel()
			{
				return qualLevel;
			}

			@Override
			public AbilityMapping qualLevel(final int newValue)
			{
				qualLevel = newValue;
				return this;
			}

			@Override
			public boolean autoGain()
			{
				return autoGain;
			}

			@Override
			public AbilityMapping autoGain(final boolean newValue)
			{
				autoGain = newValue;
				return this;
			}

			@Override
			public int defaultProficiency()
			{
				return defaultProficiency;
			}

			@Override
			public AbilityMapping defaultProficiency(final int newValue)
			{
				defaultProficiency = newValue;
				return this;
			}

			@Override
			public int maxProficiency()
			{
				return maxProficiency;
			}

			@Override
			public AbilityMapping maxProficiency(final int newValue)
			{
				maxProficiency = newValue;
				return this;
			}

			@Override
			public String defaultParm()
			{
				return defaultParm;
			}

			@Override
			public AbilityMapping defaultParm(final String newValue)
			{
				defaultParm = newValue;
				return this;
			}

			@Override
			public SecretFlag secretFlag()
			{
				return isSecret;
			}

			@Override
			public AbilityMapping secretFlag(final SecretFlag newValue)
			{
				isSecret = newValue;
				return this;
			}

			@Override
			public boolean isAllQualified()
			{
				return isAllQualified;
			}

			@Override
			public AbilityMapping isAllQualified(final boolean newValue)
			{
				isAllQualified = newValue;
				return this;
			}

			@Override
			public DVector skillPreReqs()
			{
				return skillPreReqs;
			}

			@Override
			public AbilityMapping skillPreReqs(final DVector newValue)
			{
				skillPreReqs = newValue;
				return this;
			}

			@Override
			public String extraMask()
			{
				return extraMask;
			}

			@Override
			public AbilityMapping extraMask(final String newValue)
			{
				extraMask = newValue;
				return this;
			}

			@Override
			public String originalSkillPreReqList()
			{
				return originalSkillPreReqList;
			}

			@Override
			public AbilityMapping originalSkillPreReqList(final String newValue)
			{
				originalSkillPreReqList = newValue;
				return this;
			}

			@Override
			public Integer[] costOverrides()
			{
				return costOverrides;
			}

			@Override
			public AbilityMapping costOverrides(final Integer[] newValue)
			{
				costOverrides = newValue;
				return this;
			}

			@Override
			public boolean allQualifyFlag()
			{
				return allQualifyFlag;
			}

			@Override
			public AbilityMapping allQualifyFlag(final boolean newValue)
			{
				allQualifyFlag = newValue;
				return this;
			}

			@Override
			public Map<String, String> extFields()
			{
				return extFields;
			}

			@Override
			public AbilityMapping extFields(final Map<String, String> newValue)
			{
				extFields = newValue;
				return this;
			}

			@Override
			public AbilityMapping copyOf()
			{
				final AbilityMapping map;
				try
				{
					map = (AbilityMapping) this.clone();
					map.skillPreReqs(skillPreReqs.copyOf());
					if(costOverrides != null)
						map.costOverrides(costOverrides.clone());
					map.extFields(new Hashtable<String,String>(extFields));
				}
				catch (final CloneNotSupportedException e)
				{
					return this;
				}
				return map;
			}

			@Override
			public void add()
			{
				final List<String> unfixedPreReqs=CMParms.parseCommas(this.originalSkillPreReqList(), true);
				makeAbilityMapping(ID, qualLevel, abilityID, defaultProficiency, maxProficiency, defaultParm, autoGain, isSecret,
									isAllQualified, unfixedPreReqs, extraMask,costOverrides);
			}
		};
	}

	@Override
	public AbilityMapping makeAbilityMapping(final String ID,
											 final int qualLevel,
											 final String abilityID,
											 final int defaultProficiency,
											 final int maxProficiency,
											 final String defaultParam,
											 final boolean autoGain,
											 final SecretFlag secret,
											 final boolean isAllQualified,
											 final List<String> preReqSkillsList,
											 final String extraMask,
											 final Integer[] costOverrides)
	{
		final AbilityMapping able=newAbilityMapping()
								.ID(ID)
								.qualLevel(qualLevel)
								.abilityID(abilityID)
								.defaultProficiency(defaultProficiency)
								.maxProficiency(maxProficiency)
								.defaultParm(defaultParam ==null ? "" : defaultParam)
								.autoGain(autoGain)
								.secretFlag(secret)
								.isAllQualified(isAllQualified)
								.extraMask(extraMask == null ? "" : extraMask)
								.costOverrides(costOverrides)
								.originalSkillPreReqList(CMParms.toListString(preReqSkillsList))
								.skillPreReqs(new DVector(2));
		addPreRequisites(abilityID,preReqSkillsList,extraMask);

		if(preReqSkillsList!=null)
		{
			for(int v=0;v<preReqSkillsList.size();v++)
			{
				String s=preReqSkillsList.get(v);
				int prof=0;
				final int x=s.indexOf('(');
				if((x>=0)&&(s.endsWith(")")))
				{
					prof=CMath.s_int(s.substring(x+1,s.length()-1));
					s=s.substring(0,x);
				}
				able.skillPreReqs().addElement(s,Integer.valueOf(prof));
			}
		}
		return able;
	}

	@Override
	public int getCalculatedMedianLowestQualifyingLevel()
	{
		final Integer[] allLevelsArray = lowestQualifyingLevelMap.values().toArray(new Integer[0]);
		Arrays.sort( allLevelsArray );
		if(allLevelsArray.length==0)
			return 0;
		if(allLevelsArray.length==1)
			return allLevelsArray[0].intValue();
		return allLevelsArray[(int)Math.round( CMath.div( allLevelsArray.length,2.0 ) )].intValue();
	}

	protected void mapAbilityFinal(final String abilityID, final Map<String, AbilityMapping> ableMap, final AbilityMapping able)
	{
		if(CMSecurity.isAbilityDisabled(able.abilityID().toUpperCase()))
			return;
		ableMap.put(abilityID,able);
		final CharClass ableC=CMClass.getCharClass(able.ID());

		final boolean isACharacterClass = ((ableC != null) && (!(ableC instanceof ArchonOnly)) && (CMProps.isTheme(ableC.availabilityCode())))
										|| (able.ID().equalsIgnoreCase("All"));

		final int qualLevel = able.qualLevel();
		final int maxProficiency = able.maxProficiency();

		if(isACharacterClass)
		{
			// set lowest level map
			final Integer lowLevel=lowestQualifyingLevelMap.get(abilityID);
			if(qualLevel > 0)
			{
				if((lowLevel==null)
				||(qualLevel<lowLevel.intValue()))
					lowestQualifyingLevelMap.put(abilityID,Integer.valueOf(qualLevel));
			}

			// and max proficiency maps
			final Integer maxProf=maxProficiencyMap.get(abilityID);
			if((maxProf==null)
			||(maxProficiency>maxProf.intValue()))
				maxProficiencyMap.put(abilityID,Integer.valueOf(maxProficiency));

			// archons qualify for everything at an appropriate level
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(C instanceof ArchonOnly)
				{
					final int arc_level=getQualifyingLevel(C.ID(),true,abilityID);
					if(((arc_level<0)||((qualLevel>=0)&&(qualLevel<arc_level)))
					&&(!able.ID().equalsIgnoreCase(C.ID()))
					&&(!able.ID().equalsIgnoreCase("All")))
					{
						addCharAbilityMapping(C.ID(),qualLevel,abilityID,true);
					}
				}
			}
		}

		// and the reverse lookup map
		Map<String, AbilityMapping> revT = reverseAbilityMap.get(abilityID);
		if(revT==null)
		{
			revT = new TreeMap<String, AbilityMapping>();
			reverseAbilityMap.put(abilityID,revT);
		}
		if(!revT.containsKey(able.ID()))
			revT.put(able.ID(), able);
	}

	protected synchronized void handleEachAndClassAbility(final Map<String, AbilityMapping> ableMap, final Map<String,Map<String,AbilityMapping>> allQualMap, final String ID)
	{
		if(eachClassSet == null)
		{
			eachClassSet = new SLinkedList<AbilityMapping>();
			final Map<String,AbilityMapping> eachMap=allQualMap.get("EACH");
			final Map<String,AbilityMapping> allAllMap=allQualMap.get("ALL");
			for(final AbilityMapping mapped : eachMap.values())
			{
				if(CMSecurity.isAbilityDisabled(mapped.abilityID().toUpperCase()))
					continue;
				final AbilityMapping able = mapped.copyOf();
				eachClassSet.add(able);
			}
			for(final AbilityMapping mapped : allAllMap.values())
			{
				if(CMSecurity.isAbilityDisabled(mapped.abilityID().toUpperCase()))
					continue;
				final AbilityMapping able = mapped.copyOf();
				able.ID("All");
				Map<String, AbilityMapping> allMap=completeAbleMap.get("All");
				if(allMap == null)
				{
					allMap=new SHashtable<String,AbilityMapping>();
					completeAbleMap.put("All",allMap);
				}
				able.allQualifyFlag(true);
				mapAbilityFinal(able.abilityID(), allMap, able);
			}
		}
		for (final AbilityMapping abilityMapping : eachClassSet)
		{
			final AbilityMapping able=abilityMapping.copyOf();
			able.ID(ID);
			able.allQualifyFlag(true);
			mapAbilityFinal(able.abilityID(), ableMap, able);
		}
	}

	@Override
	public boolean qualifiesByAnything(final String abilityID)
	{
		if(completeAbleMap.containsKey("All"))
		{
			final Map<String, AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return true;
		}
		final Map<String,AbilityMapping> revMap = reverseAbilityMap.get(abilityID);
		if(revMap != null)
			return true;
		return false;
	}

	@Override
	public boolean qualifiesByAnyCharClass(final String abilityID)
	{
		if(completeAbleMap.containsKey("All"))
		{
			final Map<String, AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return true;
		}
		final Map<String,AbilityMapping> revMap = reverseAbilityMap.get(abilityID);
		if(revMap != null)
		{
			for(final String str : revMap.keySet())
			{
				if(CMClass.getCharClass(str)!=null)
					return true;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getQualifyingEntities(final String abilityID)
	{
		if (reverseAbilityMap.containsKey(abilityID))
			return new ReadOnlySet<String>(reverseAbilityMap.get(abilityID).keySet());
		return XHashSet.empty;
	}

	@Override
	public boolean qualifiesByAnyCharClassOrRace(final String abilityID)
	{
		if(this.qualifiesByAnyCharClass(abilityID))
			return true;
		final Map<String,AbilityMapping> revMap = reverseAbilityMap.get(abilityID);
		if(revMap != null)
		{
			for(final String str : revMap.keySet())
			{
				if(CMClass.getRace(str)!=null)
					return true;
			}
		}
		return false;
	}

	@Override
	public int lowestQualifyingLevel(final String abilityID)
	{
		final Integer lowLevel=lowestQualifyingLevelMap.get(abilityID);
		if(lowLevel==null)
			return 0;
		return lowLevel.intValue();
	}

	@Override
	public boolean classOnly(final String classID, final String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			final Map<String, AbilityMapping> ableMap=completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID))
				return false;
		}
		else
			return false;
		for(final String key : completeAbleMap.keySet())
		{
			if((!key.equalsIgnoreCase(classID))
			&&(completeAbleMap.get(key).containsKey(abilityID)))
				return false;
		}
		return true;
	}

	@Override
	public boolean classOnly(final MOB mob, final String classID, final String abilityID)
	{
		if(completeAbleMap.containsKey(classID))
		{
			final Map<String, AbilityMapping> ableMap=completeAbleMap.get(classID);
			if(!ableMap.containsKey(abilityID))
				return false;
		}
		else
			return false;
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			final CharClass C=mob.charStats().getMyClass(c);
			if((!C.ID().equals(classID))
			&&(completeAbleMap.containsKey(classID))
			&&(completeAbleMap.get(classID).containsKey(abilityID)))
				return false;
		}
		return true;
	}

	@Override
	public boolean availableToTheme(final String abilityID, final int theme, final boolean publicly)
	{
		for(final String key : completeAbleMap.keySet())
		{
			if(completeAbleMap.get(key).containsKey(abilityID))
			{
				if(key.equalsIgnoreCase("All"))
					return true;
				final CharClass C=CMClass.getCharClass(key);
				if((C!=null)
				&&((C.availabilityCode()&theme)==theme)
				&&((!publicly)||(CMLib.login().isAvailableCharClass(C))))
					return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getLevelListings(final String ID, final boolean checkAll, final int level)
	{
		final List<String> V=new Vector<String>();
		final CharClass C=CMClass.getCharClass(ID);
		if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
			return V;
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			for(final String key : ableMap.keySet())
			{
				final AbilityMapping able=ableMap.get(key);
				if(able.qualLevel()==level)
					V.add(key);
			}
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			for(final String key : ableMap.keySet())
			{
				final AbilityMapping able=ableMap.get(key);
				if((able.qualLevel()==level)
				&&(!V.contains(key)))
					V.add(key);
			}
		}
		return V;
	}

	@Override
	public Map<String,AbilityMapping> getAbleMapping(final String ID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			return completeAbleMap.get(ID);
		}
		return emptyAbleMap;
	}

	@Override
	public List<AbilityMapping> getUpToLevelListings(final String ID, int level, final boolean ignoreAll, final boolean gainedOnly)
	{
		final List<AbilityMapping> DV=new Vector<AbilityMapping>();
		final CharClass C=CMClass.getCharClass(ID);
		if((C!=null)&&(C.getLevelCap()>=0)&&(level>C.getLevelCap()))
			level=C.getLevelCap();
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			for(final String key : ableMap.keySet())
			{
				final AbilityMapping able=ableMap.get(key);
				if((able.qualLevel()<=level)
				&&((!gainedOnly)||(able.autoGain())))
					DV.add(able);
			}
		}
		if((completeAbleMap.containsKey("All"))&&(!ignoreAll))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			for(final String key : ableMap.keySet())
			{
				final AbilityMapping able=ableMap.get(key);
				if((able.qualLevel()<=level)
				&&((!gainedOnly)||(able.autoGain())))
				{
					boolean found=false;
					for(final AbilityMapping A : DV)
					{
						if(A.ID().equalsIgnoreCase(key))
						{
							found=true;
							break;
						}
					}
					if(!found)
						DV.add(able);
				}
			}
		}
		return DV;
	}

	@Override
	public int getQualifyingLevel(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).qualLevel();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
			{
				final int qualLevel = ableMap.get(abilityID).qualLevel();
				final CharClass C=CMClass.getCharClass(ID);
				if((C!=null)&&(C.getLevelCap()>=0))
					return qualLevel>C.getLevelCap()?-1:qualLevel;
				return qualLevel;
			}
		}
		return -1;
	}

	@Override
	public AbilityMapping getQualifyingMapping(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID);
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
			{
				final AbilityMapping map=ableMap.get(abilityID);
				final int qualLevel = map.qualLevel();
				final CharClass C=CMClass.getCharClass(ID);
				if((C!=null)&&(C.getLevelCap()>=0))
					return (qualLevel>C.getLevelCap())?null:map;
				return map;
			}
		}
		return null;
	}

	@Override
	public List<AbilityMapping> getQualifyingMappings(final boolean checkAll, final String abilityID)
	{
		final List<AbilityMapping> maps=new Vector<AbilityMapping>();
		for(final String ID : completeAbleMap.keySet())
		{
			if((!ID.equals("All"))||(checkAll))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if(ableMap.containsKey(abilityID))
				{
					final AbilityMapping map=ableMap.get(abilityID);
					final int qualLevel = map.qualLevel();
					final CharClass C=CMClass.getCharClass(ID);
					if((C!=null)&&(C.getLevelCap()>=0))
					{
						if(qualLevel<=C.getLevelCap())
							maps.add(map);
					}
					else
						maps.add(map);
				}
			}
		}
		return maps;
	}

	protected List<String> getOrSet(final String errStr, String abilityID)
	{
		Ability preA=null;
		final List<String> orset=new Vector<String>();
		final List<String> preorset=CMParms.parseCommas(abilityID,true);
		for(int p=0;p<preorset.size();p++)
		{
			abilityID=preorset.get(p);
			if(abilityID.startsWith("*"))
			{
				final String a=abilityID.substring(1).toUpperCase();
				for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=e.nextElement();
					if(preA.ID().toUpperCase().endsWith(a))
						orset.add(preA.ID());
				}
			}
			else
			if(abilityID.endsWith("*"))
			{
				final String a=abilityID.substring(0,abilityID.length()-1).toUpperCase();
				for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				{
					preA=e.nextElement();
					if(preA.ID().toUpperCase().startsWith(a))
						orset.add(preA.ID());
				}
			}
			else
				orset.add(abilityID);
		}
		for(int o=orset.size()-1;o>=0;o--)
		{
			abilityID=orset.get(o);
			preA=CMClass.getAbility(abilityID);
			if(preA==null)
				preA=CMClass.findAbility(abilityID);
			if(preA!=null)
				orset.set(o,preA.ID());
			else
			{
				final ExpertiseDefinition def = CMLib.expertises().findDefinition(abilityID, true);
				if(def != null)
					orset.set(0,def.ID());
				else
				{
					if(CMLib.expertises().getStageCodes(abilityID).size()>0)
						orset.set(0,abilityID.toUpperCase().trim());
					else
					{
						Log.errOut("CMAble","Skill "+errStr+" requires nonexistant skill "+abilityID+".");
						orset.clear();
						break;
					}
				}
			}
		}
		return orset;
	}

	public void fillPreRequisites(final Ability A, final DVector rawPreReqs)
	{
		for(int v=0;v<rawPreReqs.size();v++)
		{
			final String abilityID=(String)rawPreReqs.elementAt(v,1);
			if(abilityID.startsWith("*")||abilityID.endsWith("*")||(abilityID.indexOf(',')>0))
			{
				final List<String> orset=getOrSet(A.ID(),abilityID);
				if(orset.size()!=0)
					rawPreReqs.setElementAt(v,1,orset);
			}
			else
			{
				Ability otherAbility=CMClass.getAbility(abilityID);
				if(otherAbility==null)
					otherAbility=CMClass.findAbility(abilityID);
				if(otherAbility!=null)
					rawPreReqs.setElementAt(v,1,otherAbility.ID());
				else
				{
					final ExpertiseDefinition def = CMLib.expertises().findDefinition(abilityID, true);
					if(def != null)
						rawPreReqs.setElementAt(v,1,def.ID());
					else
					{
						if(CMLib.expertises().getStageCodes(abilityID).size()>0)
							rawPreReqs.setElementAt(v,1,abilityID.toUpperCase().trim());
						else
						{
							Log.errOut("CMAble","Skill "+A.ID()+" requires nonexistant skill "+abilityID+".");
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public DVector getCommonPreRequisites(final MOB mob, final Ability A)
	{
		DVector preReqs=getRawPreRequisites(mob,A);
		if((preReqs==null)||(preReqs.size()==0))
		{
			preReqs=getRawPreRequisites("All", false, A.ID());
			if((preReqs!=null)&&(preReqs.size()>0))
				preReqs=preReqs.copyOf();
			else
				return new DVector(2);
		}
		fillPreRequisites(A,preReqs);
		return preReqs;
	}

	@Override
	public DVector getCommonPreRequisites(final Ability A)
	{
		DVector preReqs=getRawPreRequisites("All", false, A.ID());
		if(preReqs==null)
		{
			for(final Map<String,AbilityMapping> ableMap : completeAbleMap.values())
			{
				if(ableMap.containsKey(A.ID()))
				{
					preReqs=ableMap.get(A.ID()).skillPreReqs();
					if((preReqs!=null)&&(preReqs.size()>0))
						break;
				}
			}
		}
		if((preReqs==null)||(preReqs.size()==0))
			return new DVector(2);
		final DVector requisites=preReqs.copyOf();
		fillPreRequisites(A,requisites);
		return requisites;
	}

	@Override
	public String getCommonExtraMask(final Ability A)
	{
		String mask=null;
		{
			Map<String,AbilityMapping> ableMap=null;
			if(completeAbleMap.containsKey("All"))
			{
				ableMap=completeAbleMap.get("All");
				if(ableMap.containsKey(A.ID()))
					mask=ableMap.get(A.ID()).extraMask();
			}
		}
		if((mask==null)||(mask.length()==0))
		{
			for(final Map<String,AbilityMapping> ableMap : completeAbleMap.values())
			{
				if(ableMap.containsKey(A.ID()))
				{
					mask=ableMap.get(A.ID()).extraMask();
					if((mask!=null)&&(mask.length()>0))
						break;
				}
			}
		}
		if((mask==null)||(mask.length()==0))
			return "";
		return mask;
	}

	@Override
	public DVector getUnmetPreRequisites(final MOB studentM, final Ability A)
	{
		final DVector V=getRawPreRequisites(studentM,A);
		if((V==null)||(V.size()==0))
			return new DVector(2);
		fillPreRequisites(A,V);
		for(int v=V.size()-1;v>=0;v--)
		{
			final Integer prof=(Integer)V.elementAt(v,2);
			if(V.elementAt(v,1) instanceof String)
			{
				final String abilityID=(String)V.elementAt(v,1);
				final Ability A2=studentM.fetchAbility(abilityID);
				if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					V.removeElementAt(v);
				else
				{
					final Pair<String,Integer> xP = studentM.fetchExpertise(abilityID);
					if((xP!=null)&&(xP.second.intValue()>=prof.intValue()))
						V.removeElementAt(v);
					else
					if((!qualifiesByLevel(studentM,abilityID))
					&&(!getAllQualified("All",true,abilityID))
					&&(CMClass.getAbility(abilityID)!=null))
					{
						if(!getAllQualified("All",true,A.ID()))
							V.removeElementAt(v);
						// why are you even trying?
					}
				}
			}
			else
			{
				@SuppressWarnings("unchecked")
				final List<String> orset=(List<String>)V.elementAt(v,1);
				for(int o=orset.size()-1;o>=0;o--)
				{
					final String abilityID=orset.get(o);
					final Ability A2=studentM.fetchAbility(abilityID);
					if((A2!=null)&&(A2.proficiency()>=prof.intValue()))
					{
						orset.clear();
						break;
					}
					else
					{
						final Pair<String,Integer> xP = studentM.fetchExpertise(abilityID);
						if((xP!=null)&&(xP.second.intValue()>=prof.intValue()))
						{
							orset.clear();
							break;
						}
						else
						if((!qualifiesByLevel(studentM,abilityID))
						&&(!getAllQualified("All",true,abilityID))
						&&(CMClass.getAbility(abilityID)!=null))
						{
							if(!getAllQualified("All",true,A.ID()))
								orset.remove(o);
							// why are you even trying?
						}
					}
				}
				if(orset.size()==0)
					V.removeElementAt(v);
			}
		}
		return V;
	}

	public DVector getRawPreRequisites(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).skillPreReqs();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).skillPreReqs();
		}
		return null;
	}

	@Override
	public String formatPreRequisites(final DVector preReqs)
	{
		final StringBuffer names=new StringBuffer("");
		if((preReqs!=null)&&(preReqs.size()>0))
		{
			Integer prof=null;
			for(int p=0;p<preReqs.size();p++)
			{
				prof=(Integer)preReqs.elementAt(p,2);
				if(preReqs.elementAt(p,1) instanceof List)
				{
					@SuppressWarnings({ "unchecked", "rawtypes" })
					final List<String> V=(List)preReqs.elementAt(p,1);
					names.append("(One of: ");
					Ability A = null;
					for(int v=0;v<V.size();v++)
					{
						final String ID = V.get(v);
						A=CMClass.getAbility(ID);
						final String name;
						if(A!=null)
							name = A.Name();
						else
						{
							final ExpertiseDefinition X;
							if(prof.intValue()>0)
								X = CMLib.expertises().getDefinition(ID+prof.toString());
							else
								X = CMLib.expertises().getDefinition(ID);
							if(X != null)
								name = X.name();
							else
								continue;
						}
						names.append("'"+name+"'");
						if(V.size()>1)
						{
							if(v==(V.size()-2))
								names.append(", or ");
							else
							if(v<V.size()-2)
								names.append(", ");
						}
					}
					if((prof.intValue()>0)&&(A!=null))
						names.append(" at "+prof+"%)");
					else
						names.append(")");
				}
				else
				{
					final String ID=(String)preReqs.elementAt(p,1);
					final Ability A=CMClass.getAbility(ID);
					final String name;
					if(A!=null)
						name = A.Name();
					else
					{
						final ExpertiseDefinition X;
						if(prof.intValue()>0)
							X = CMLib.expertises().getDefinition(ID+prof.toString());
						else
							X = CMLib.expertises().getDefinition(ID);
						if(X != null)
							name = X.name();
						else
							continue;
					}
					names.append("'"+name+"'");
					if((prof.intValue()>0)&&(A!=null))
						names.append(" at "+prof+"%");
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

	protected final AbilityMapping getPersonalMapping(final MOB studentM, final String AID)
	{
		if(studentM != null)
		{
			final PlayerStats pStats = studentM.playerStats();
			if(pStats != null)
			{
				if(pStats.getExtraQualifiedSkills().containsKey(AID))
				{
					final AbilityMapping mapping = pStats.getExtraQualifiedSkills().get(AID);
					if(studentM.basePhyStats().level() >= mapping.qualLevel())
						return mapping;
				}
			}
		}
		return null;
	}

	@Override
	public final List<String> getCurrentlyQualifyingIDs(final MOB studentM, final String AID)
	{
		final List<String> ids=new LinkedList<String>();
		final CharStats cStats = studentM.charStats();
		final AbilityMapping personalMap = getPersonalMapping(studentM, AID);
		if(personalMap != null)
			ids.add(studentM.Name());
		for(int c=cStats.numClasses()-1;c>=0;c--)
		{
			final CharClass C=cStats.getMyClass(c);
			final int qualLevel=getQualifyingLevel(C.ID(),true,AID);
			final int classLevel=studentM.charStats().getClassLevel(C);
			if((qualLevel>=0)&&(classLevel>=qualLevel))
				ids.add(C.ID());
		}
		final int qualRacelevel=getQualifyingLevel(cStats.getMyRace().ID(),false,AID);
		final int charLevel=studentM.basePhyStats().level();
		if((qualRacelevel>=0)&&(charLevel>=qualRacelevel))
			ids.add(cStats.getMyRace().ID());
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			final int qualClanlevel=getQualifyingLevel(c.first.getGovernment().getName(),false,AID);
			if((qualClanlevel>=0)
			&&(c.first.getClanLevel()>=qualClanlevel)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
			{
				final String gvtName=c.first.getGovernment().getName();
				if(!ids.contains(gvtName))
					ids.add(gvtName);
			}
		}
		return ids;
	}

	public DVector getRawPreRequisites(final MOB studentM, final Ability A)
	{
		if((studentM==null)||(A==null))
			return new DVector(2);
		final String AID=A.ID();
		final AbilityMapping personalMap = getPersonalMapping(studentM, AID);
		if(personalMap != null)
			return personalMap.skillPreReqs() == null ? new DVector(2) : personalMap.skillPreReqs().copyOf();
		final List<String> qualifyingIDs=getCurrentlyQualifyingIDs(studentM,AID);
		DVector reqs=null;
		for (final String ID : qualifyingIDs)
		{
			reqs=getRawPreRequisites(ID,true,A.ID());
			if(reqs!=null)
				return reqs.copyOf();
		}
		reqs=getRawPreRequisites(studentM.charStats().getCurrentClass().ID(),true,A.ID());
		return (reqs==null)?new DVector(2):reqs.copyOf();
	}

	@Override
	public String getExtraMask(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).extraMask();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).extraMask();
		}
		return null;
	}

	@Override
	public String getApplicableMask(final MOB studentM, final Ability A)
	{
		if((studentM==null)||(A==null))
			return "";
		final String AID=A.ID();
		final AbilityMapping personalMap = getPersonalMapping(studentM, AID);
		if(personalMap != null)
			return personalMap.extraMask() == null ? "" : personalMap.extraMask();
		final List<String> qualifyingIDs=getCurrentlyQualifyingIDs(studentM,AID);
		String mask=null;
		for (final String ID : qualifyingIDs)
		{
			mask=getExtraMask(ID,true,AID);
			if(mask!=null)
				return mask;
		}

		mask=getExtraMask(studentM.charStats().getCurrentClass().ID(),true,A.ID());
		return mask==null?"":mask;
	}

	@Override
	public int qualifyingLevel(final MOB studentM, final Ability A)
	{
		if(studentM==null)
			return -1;
		int theLevel=-1;
		int greatestDiff=-1;
		final AbilityMapping personalMap = getPersonalMapping(studentM, A.ID());
		if((personalMap != null)&&(personalMap.qualLevel() <= studentM.phyStats().level()))
		{
			theLevel = personalMap.qualLevel();
			greatestDiff = studentM.phyStats().level() - personalMap.qualLevel();
		}
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,A.ID());
			final int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theLevel=level;
			}
		}
		final int raceLevel=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		final int charLevel=studentM.basePhyStats().level();
		if((raceLevel>=0)
		&&(charLevel>=raceLevel)
		&&((charLevel-raceLevel)>greatestDiff))
		{
			greatestDiff=charLevel-raceLevel;
			theLevel=raceLevel;
		}
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			final int clanLevel=getQualifyingLevel(c.first.getGovernment().getName(),false,A.ID());
			if((clanLevel>=0)
			&&(c.first.getClanLevel()>=clanLevel)
			&&((charLevel-clanLevel)>greatestDiff)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
			{
				greatestDiff=charLevel-clanLevel;
				theLevel=clanLevel;
			}
		}
		if(theLevel<0)
		{
			// return ANY qualifying level, even if above station -- prefer current class
			for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
			{
				final CharClass C=studentM.charStats().getMyClass(c);
				theLevel=getQualifyingLevel(C.ID(),true,A.ID());
				if(theLevel > 0)
					break;
			}
		}
		return theLevel;
	}

	@Override
	public String qualifyingID(final MOB studentM, final Ability A)
	{
		if(studentM==null)
			return null;
		String theObj = null;
		int theLevel=-1;
		int greatestDiff=-1;
		final AbilityMapping personalMap = getPersonalMapping(studentM, A.ID());
		if((personalMap != null)&&(personalMap.qualLevel() <= studentM.phyStats().level()))
		{
			theObj = studentM.Name();
			theLevel = personalMap.qualLevel();
			greatestDiff = studentM.phyStats().level() - personalMap.qualLevel();
		}
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,A.ID());
			final int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				theObj = C.ID();
				greatestDiff=classLevel-level;
				theLevel=level;
			}
		}
		final int raceLevel=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		final int charLevel=studentM.basePhyStats().level();
		if((raceLevel>=0)
		&&(charLevel>=raceLevel)
		&&((charLevel-raceLevel)>greatestDiff))
		{
			theObj = studentM.charStats().getMyRace().ID();
			greatestDiff=charLevel-raceLevel;
			theLevel=raceLevel;
		}
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			final int clanLevel=getQualifyingLevel(c.first.getGovernment().getName(),false,A.ID());
			if((clanLevel>=0)
			&&(c.first.getClanLevel()>=clanLevel)
			&&((charLevel-clanLevel)>greatestDiff)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
			{
				theObj = c.first.getGovernment().getName();
				greatestDiff=charLevel-clanLevel;
				theLevel=clanLevel;
			}
		}
		if(theLevel<0)
		{
			final String ID = studentM.charStats().getCurrentClass().ID();
			final String abilityID = A.ID();
			if(completeAbleMap.containsKey(ID))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if(ableMap.containsKey(abilityID))
					return ID;
			}
			if(completeAbleMap.containsKey("All"))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
				if(ableMap.containsKey(abilityID))
				{
					final int qualLevel = ableMap.get(abilityID).qualLevel();
					final CharClass C=CMClass.getCharClass(ID);
					if((C!=null)&&(C.getLevelCap()>=0))
						return qualLevel>C.getLevelCap()?null:"All";
					return "All";
				}
			}
		}
		return theObj;
	}

	@Override
	public CharClass qualifyingCharClassByLevel(final MOB studentM, final Ability A)
	{
		int greatestDiff=-1;
		CharClass theClass=null;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,A.ID());
			final int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((classLevel-level)>greatestDiff))
			{
				greatestDiff=classLevel-level;
				theClass=C;
			}
		}
		return theClass;
	}

	@Override
	public int qualifyingClassLevel(final MOB studentM, final Ability A)
	{
		if(studentM==null)
			return -1;
		final CharClass theClass = qualifyingCharClassByLevel(studentM,A);
		if(theClass==null)
			return studentM.charStats().getClassLevel(studentM.charStats().getCurrentClass());
		return studentM.charStats().getClassLevel(theClass);
	}

	@Override
	public CMObject lowestQualifyingClassRaceGovt(final MOB studentM, final Ability A)
	{
		if(studentM==null)
			return null;
		int theLevel=-1;
		CMObject theClass=null;
		final AbilityMapping personalMap = getPersonalMapping(studentM, A.ID());
		if(personalMap != null)
		{
			theLevel = personalMap.qualLevel();
			theClass = studentM;
		}
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,A.ID());
			final int classLevel=studentM.charStats().getClassLevel(C);
			if((level>=0)
			&&(classLevel>=level)
			&&((theLevel<0)||(theLevel>=level)))
			{
				theLevel=level;
				theClass=C;
			}
		}
		final int raceLevel=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		if((raceLevel>=0)
		&&((theClass==null)||((studentM.basePhyStats().level()>=raceLevel)&&(theLevel>raceLevel))))
			theClass=studentM.charStats().getMyRace();
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			final int clanLevel=getQualifyingLevel(c.first.getGovernment().getName(),false,A.ID());
			if((clanLevel>=0)
			&&(c.first.getClanLevel()>=clanLevel)
			&&(theLevel>clanLevel)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
			{
				theClass=c.first.getGovernment();
				theLevel=clanLevel;
			}
		}
		return theClass;
	}

	@Override
	public boolean qualifiesByCurrentClassAndLevel(final MOB studentM, final Ability A)
	{
		if(studentM==null)
			return false;
		final AbilityMapping personalMap = getPersonalMapping(studentM, A.ID());
		if((personalMap != null)&&(studentM.phyStats().level() >= personalMap.qualLevel()))
			return true;
		final CharClass C=studentM.charStats().getCurrentClass();
		int level=getQualifyingLevel(C.ID(),true,A.ID());
		if((level>=0)
		&&(studentM.charStats().getClassLevel(C)>=level))
			return true;
		level=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		if((level>=0)&&(studentM.phyStats().level()>=level))
			return true;
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			level=getQualifyingLevel(c.first.getGovernment().getName(),false,A.ID());
			if((level>=0)
			&&(c.first.getClanLevel()>=level)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
				return true;
		}
		return false;
	}

	@Override
	public boolean qualifiesOnlyByRace(final MOB studentM, final Ability A)
	{
		final int level=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,A.ID());
		if((level>=0)&&(studentM.phyStats().level()>=level))
			return true;
		return false;
	}

	@Override
	public boolean qualifiesOnlyByClan(final MOB studentM, final Ability A)
	{
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			final int level=getQualifyingLevel(c.first.getGovernment().getName(),false,A.ID());
			if((level>=0)
			&&(c.first.getClanLevel()>=level)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
				return true;
		}
		return false;
	}

	@Override
	public boolean qualifiesOnlyByACharClass(final MOB studentM, final Ability A)
	{
		if(studentM==null)
			return false;
		for(int c=0;c<studentM.charStats().numClasses();c++)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,A.ID());
			if((level>=0)
			&&(studentM.charStats().getClassLevel(C)>=level))
				return true;
		}
		return false;
	}

	@Override
	public boolean qualifiesByLevel(final MOB studentM, final Ability A)
	{
		return (A == null) ? false : qualifiesByLevel(studentM, A.ID());
	}

	@Override
	public boolean qualifiesByLevel(final MOB studentM, final String abilityID)
	{
		if(studentM==null)
			return false;
		final AbilityMapping personalMap = getPersonalMapping(studentM, abilityID);
		if((personalMap != null) && (studentM.phyStats().level() >= personalMap.qualLevel()))
			return true;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,abilityID);
			if((level>=0)
			&&(studentM.charStats().getClassLevel(C)>=level))
				return true;
		}
		int level=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,abilityID);
		if((level>=0)&&(studentM.phyStats().level()>=level))
			return true;
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			level=getQualifyingLevel(c.first.getGovernment().getName(),false,abilityID);
			if((level>=0)
			&&(c.first.getClanLevel()>=level)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
				return true;
		}
		return false;
	}

	@Override
	public boolean qualifiesByTrajectory(final MOB studentM, final String abilityID)
	{
		if(studentM==null)
			return false;
		final AbilityMapping personalMap = getPersonalMapping(studentM, abilityID);
		if(personalMap != null)
			return true;
		for(int c=studentM.charStats().numClasses()-1;c>=0;c--)
		{
			final CharClass C=studentM.charStats().getMyClass(c);
			final int level=getQualifyingLevel(C.ID(),true,abilityID);
			if(level>=0)
				return true;
		}
		int level=getQualifyingLevel(studentM.charStats().getMyRace().ID(),false,abilityID);
		if(level>=0)
			return true;
		for(final Pair<Clan,Integer> c : studentM.clans())
		{
			level=getQualifyingLevel(c.first.getGovernment().getName(),false,abilityID);
			if((level>=0)
			&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
				return true;
		}
		return false;
	}

	@Override
	public boolean getDefaultGain(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).autoGain();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).autoGain();
		}
		return false;
	}

	@Override
	public boolean getAllQualified(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).isAllQualified();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).isAllQualified();
		}
		return false;
	}

	@Override
	public AbilityMapping getAbleMap(final String ID, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID);
		}
		return null;
	}

	@Override
	public AbilityMapping getAllAbleMap(final String abilityID)
	{
		return getAbleMap("All", abilityID);
	}

	@Override
	public SecretFlag getSecretSkill(final String ID, final boolean checkAll, final String abilityID)
	{
		SecretFlag secretFound=SecretFlag.PUBLIC;
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
			{
				final SecretFlag found=ableMap.get(abilityID).secretFlag();
				if(found==SecretFlag.PUBLIC)
					return SecretFlag.PUBLIC;
				secretFound=found;
			}
		}
		if(checkAll)
		{
			final AbilityMapping AB=getAllAbleMap(abilityID);
			if(AB!=null)
				return AB.secretFlag();
		}
		return secretFound;
	}

	@Override
	public SecretFlag getAllSecretSkill(final String abilityID)
	{
		final AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null)
			return AB.secretFlag();
		return SecretFlag.PUBLIC;
	}

	public final List<AbilityMapping> getAllAbilityMappings(final MOB mob, final String abilityID)
	{
		final List<AbilityMapping> list=new LinkedList<AbilityMapping>();
		final AbilityMapping personalMap = getPersonalMapping(mob, abilityID);
		if(personalMap != null)
			list.add(personalMap);
		for(int c=0;c<mob.charStats().numClasses();c++)
		{
			final String charClass=mob.charStats().getMyClass(c).ID();
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
			if(ableMap != null)
			{
				if(ableMap.containsKey(abilityID))
					list.add(ableMap.get(abilityID));
			}
		}
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(mob.charStats().getMyRace().ID());
			if(ableMap!=null)
			{
				if(ableMap.containsKey(abilityID))
					list.add(ableMap.get(abilityID));
			}
		}
		for(final Pair<Clan,Integer> c : mob.clans())
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(c.first.getGovernment().getName());
			if(ableMap!=null)
			{
				if((ableMap.containsKey(abilityID))
				&&(c.first.getAuthority(c.second.intValue(),Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO))
					list.add(ableMap.get(abilityID));
			}
		}
		final AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null)
			list.add(AB);
		return list;
	}

	@Override
	public boolean getSecretSkill(final MOB mob, final String abilityID)
	{
		SecretFlag secretFound=SecretFlag.PUBLIC;
		String extraMask="";
		final AbilityMapping personalMap = getPersonalMapping(mob, abilityID);
		if(personalMap != null)
		{
			secretFound=personalMap.secretFlag();
			extraMask=personalMap.extraMask();
		}
		else
		{
			final List<AbilityMapping> mappings=getAllAbilityMappings(mob,abilityID);
			for (final AbilityMapping ableMap : mappings)
			{
				final SecretFlag found=ableMap.secretFlag();
				if(found==SecretFlag.PUBLIC)
					return false;
				secretFound=found;
				extraMask=ableMap.extraMask();
			}
		}
		switch(secretFound)
		{
		case PUBLIC:
			return false;
		case SECRET:
			return true;
		case MASKED:
			return !CMLib.masking().maskCheck(extraMask, mob, true);
		default:
			return false;
		}
	}

	@Override
	public SecretFlag getSecretSkill(final String abilityID)
	{
		SecretFlag secretFound=SecretFlag.PUBLIC;
		for(final Enumeration<CharClass> e=CMClass.charClasses();e.hasMoreElements();)
		{
			final String charClass=e.nextElement().ID();
			if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if(ableMap.containsKey(abilityID))
				{
					final SecretFlag found=ableMap.get(abilityID).secretFlag();
					if(found==SecretFlag.PUBLIC)
						return SecretFlag.PUBLIC;
					secretFound=found;
				}
			}
		}
		for(final Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
		{
			final String ID=e.nextElement().ID();
			if(completeAbleMap.containsKey(ID))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if(ableMap.containsKey(abilityID))
				{
					final SecretFlag found=ableMap.get(abilityID).secretFlag();
					if(found==SecretFlag.PUBLIC)
						return SecretFlag.PUBLIC;
					secretFound=found;
				}
			}
		}
		final AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null)
			return AB.secretFlag();
		return secretFound;
	}

	@Override
	public Integer[] getCostOverrides(final String ID, final boolean checkAll, final String abilityID)
	{
		Integer[] found=null;
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				found=ableMap.get(abilityID).costOverrides();
		}
		if((checkAll)&&(found==null))
		{
			final AbilityMapping AB=getAllAbleMap(abilityID);
			if(AB!=null)
				found=AB.costOverrides();
		}
		return found;
	}

	@Override
	public Integer[] getAllCostOverrides(final String abilityID)
	{
		final AbilityMapping AB=getAllAbleMap(abilityID);
		if(AB!=null)
			return AB.costOverrides();
		return null;
	}

	@Override
	public Integer[] getCostOverrides(final MOB mob, final String abilityID)
	{
		Integer[] found=null;
		final AbilityMapping personalMap = getPersonalMapping(mob, abilityID);
		if(personalMap != null)
			return personalMap.costOverrides();
		final List<AbilityMapping> mappings=getAllAbilityMappings(mob,abilityID);
		for (final AbilityMapping ableMap : mappings)
		{
			found=ableMap.costOverrides();
			if(found!=null)
				break;
		}
		return found;
	}

	@Override
	public Integer[] getCostOverrides(final String abilityID)
	{
		Integer[] found=null;
		for(final Enumeration<CharClass> e=CMClass.charClasses();e.hasMoreElements();)
		{
			final String charClass=e.nextElement().ID();
			if(completeAbleMap.containsKey(charClass)&&(!charClass.equals("Archon")))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(charClass);
				if((ableMap.containsKey(abilityID))&&(found==null))
					found=ableMap.get(abilityID).costOverrides();
			}
		}
		for(final Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
		{
			final String ID=e.nextElement().ID();
			if(completeAbleMap.containsKey(ID))
			{
				final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
				if((ableMap.containsKey(abilityID))&&(found==null))
					found=ableMap.get(abilityID).costOverrides();
			}
		}
		final AbilityMapping AB=getAllAbleMap(abilityID);
		if((AB!=null)&&(found==null))
			return found=AB.costOverrides();
		return found;
	}

	@Override
	public String getDefaultParm(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultParm();
		}

		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultParm();
		}
		return "";
	}

	@Override
	public String getPreReqStrings(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).originalSkillPreReqList();
		}

		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).originalSkillPreReqList();
		}
		return "";
	}

	@Override
	public int getMaxProficiency(final MOB mob, final boolean checkAll, final String abilityID)
	{
		if(mob==null)
			return getMaxProficiency(abilityID);
		final AbilityMapping personalMap = getPersonalMapping(mob, abilityID);
		if(personalMap != null)
			return personalMap.maxProficiency();
		final CharClass C=mob.charStats().getCurrentClass();
		if(C==null)
			return getMaxProficiency(abilityID);
		return getMaxProficiency(C.ID(),checkAll,abilityID);
	}

	@Override
	public int getMaxProficiency(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).maxProficiency();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).maxProficiency();
		}
		return getMaxProficiency(abilityID);
	}

	@Override
	public int getMaxProficiency(final String abilityID)
	{
		if(maxProficiencyMap.containsKey(abilityID))
			return maxProficiencyMap.get(abilityID).intValue();
		return 100;
	}

	@Override
	public int getDefaultProficiency(final String ID, final boolean checkAll, final String abilityID)
	{
		if(completeAbleMap.containsKey(ID))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get(ID);
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultProficiency();
		}
		if((checkAll)&&(completeAbleMap.containsKey("All")))
		{
			final Map<String,AbilityMapping> ableMap=completeAbleMap.get("All");
			if(ableMap.containsKey(abilityID))
				return ableMap.get(abilityID).defaultProficiency();
		}
		return 0;
	}

	public AbilityMapping makeAllQualifyMapping(String s)
	{
		int x=s.indexOf(' ');
		if(x<0)
			return null;
		final String lvlStr = s.substring(0,x).trim();
		if(!CMath.isInteger(lvlStr))
			return null;
		s=s.substring(x+1).trim();
		final int qualLevel=CMath.s_int(lvlStr);
		x=s.indexOf(' ');
		String abilityID;
		final StringBuilder mask=new StringBuilder("");
		final StringBuilder preReqs=new StringBuilder("");
		final StringBuilder prof=new StringBuilder("");
		boolean autogain=false;
		SecretFlag flag = SecretFlag.PUBLIC;
		if(x<0)
			abilityID=s;
		else
		{
			abilityID=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			final String us=s.toUpperCase();
			int lastC=' ';
			StringBuilder cur=null;
			for(int i=0;i<s.length();i++)
			{
				if((lastC==' ')&&(Character.isLetter(us.charAt(i))))
				{
					final String ss=us.substring(i);
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
					{
						for(final SecretFlag sf : SecretFlag.values())
						{
							if((ss.startsWith(sf.name()+" "))
							||(ss.startsWith(sf.name()) && (ss.length()==sf.name().length())))
							{
								cur=null;
								flag = sf;
								i+=sf.name().length();
								break;
							}
						}
						if(cur != null)
							cur.append(s.charAt(i));
					}
				}
				else
				if(cur!=null)
					cur.append(s.charAt(i));
				if(i<s.length())
					lastC=s.charAt(i);
			}
		}
		return
			makeAbilityMapping(abilityID,qualLevel,abilityID,
							  CMath.s_int(prof.toString().trim()),
							  100,"",autogain,flag,
							  true,CMParms.parseSpaces(preReqs.toString().trim(), true),
							  mask.toString().trim(),null);
	}

	@Override
	public final Converter<String,AbilityMapping> getMapper(final String classID)
	{
		return new Converter<String,AbilityMapping>()
		{
			@Override
			public AbilityMapping convert(final String obj)
			{
				return CMLib.ableMapper().getAbleMap(classID, obj);
			}
		};
	}

	@Override
	public CompoundingRule getCompoundingRule(final MOB mob, final Ability A)
	{
		if(A==null)
			return null;
		if(compoundingRules.isEmpty())
		{
			if(compoundingRulesLoaded)
				return null;
			loadCompoundingRules();
			if(compoundingRules.isEmpty())
				return null;
		}
		List<CompoundingRule> rules = compounders.get(A.ID().toUpperCase());
		if(rules == null)
		{
			rules=new LinkedList<CompoundingRule>();
			final List<CompoundingRule> remainRules = new LinkedList<CompoundingRule>();
			try
			{
				for(final CompoundingRule rule : compoundingRules)
				{
					if(rule.ableMask()==null)
						remainRules.add(rule);
					else
					if(CMLib.masking().maskCheck(rule.ableMask(), A, true))
						rules.add(rule);
				}
			}
			catch(final ConcurrentModificationException e)
			{
				return null;
			}
			if(rules.size()==0)
				rules.addAll(remainRules);
			compounders.put(A.ID().toUpperCase(), rules);
		}
		if(rules.isEmpty())
			return null;
		if(rules.size()==1)
		{
			final CompoundingRule rule = rules.get(0);
			if(rule.mobMask()==null)
				return rule;
			if((mob != null)
			&&(CMLib.masking().maskCheck(rule.mobMask(), mob, true)))
				return rule;
			return null;
		}
		CompoundingRule remainRule = null;
		for(final CompoundingRule rule : rules)
		{
			if(rule.mobMask()==null)
				remainRule=rule;
			else
			if((mob != null)
			&&(CMLib.masking().maskCheck(rule.mobMask(), mob, true)))
				return rule;
		}
		return remainRule;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Map<String,AbilityMapping>> getAllQualifiesMap(final Map<String,Object> cache)
	{
		Map<String, Map<String,AbilityMapping>> bothMaps;
		if(cache!=null)
		{
			bothMaps=(Map<String, Map<String,AbilityMapping>>)cache.get("ALLQUALIFIES_MAP");
			if(bothMaps!=null)
				return bothMaps;
		}

		bothMaps=new TreeMap<String,Map<String,AbilityMapping>>();
		bothMaps.put("ALL", new TreeMap<String,AbilityMapping>());
		bothMaps.put("EACH", new TreeMap<String,AbilityMapping>());
		final CMFile[] fileList = CMFile.getExistingExtendedFiles(Resources.makeFileResourceName("skills/allqualifylist.txt"),null,CMFile.FLAG_FORCEALLOW);
		for(final CMFile F : fileList)
		{
			if(F.canRead())
			{
				final List<String> list = Resources.getFileLineVector(F.text());
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
						final AbilityMapping able=makeAllQualifyMapping(s);
						if(able==null)
							continue;
						if(eachMode)
						{
							final Map<String, AbilityMapping> map=bothMaps.get("EACH");
							map.put(able.abilityID().toUpperCase().trim(),able);
						}
						else
						{
							final Map<String, AbilityMapping> map=bothMaps.get("ALL");
							map.put(able.abilityID().toUpperCase().trim(),able);
						}
					}
				}
			}
		}
		if(cache!=null)
			cache.put("ALLQUALIFIES_MAP",bothMaps);
		return bothMaps;
	}

	public String buildAllQualifysSection(final Map<String,AbilityMapping> map)
	{
		final TreeMap<Integer,List<AbilityMapping>> sortedMap=new TreeMap<Integer,List<AbilityMapping>>();
		for(final AbilityMapping mapped : map.values())
		{
			List<AbilityMapping> subMap=sortedMap.get(Integer.valueOf(mapped.qualLevel()));
			if(subMap==null)
			{
				subMap=new LinkedList<AbilityMapping>();
				sortedMap.put(Integer.valueOf(mapped.qualLevel()), subMap);
			}
			subMap.add(mapped);
		}
		final StringBuilder str=new StringBuilder("");
		for(final Integer LEVEL : sortedMap.keySet())
		{
			final List<AbilityMapping> subMap=sortedMap.get(LEVEL);
			for(final AbilityMapping mapped : subMap)
			{
				str.append(LEVEL.toString()).append(" ");
				str.append(mapped.abilityID()).append(" ");
				if(mapped.defaultProficiency()>0)
					str.append("PROF="+mapped.defaultProficiency()+" ");
				if(mapped.autoGain())
					str.append("AUTOGAIN ");
				if(mapped.secretFlag() != SecretFlag.PUBLIC)
					str.append(mapped.secretFlag().name()+" ");
				if((mapped.extraMask()!=null)&&(mapped.extraMask().length()>0))
					 str.append("MASK=").append(mapped.extraMask()).append(" ");
				if((mapped.originalSkillPreReqList()!=null)&&(mapped.originalSkillPreReqList().trim().length()>0))
					str.append("REQUIRES=").append(CMParms.combineWith(CMParms.parseCommas(mapped.originalSkillPreReqList(),true), ' ')).append(" ");
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		return str.toString();
	}

	protected void undoAllQualifysList()
	{
		for(final String abilityID : reverseAbilityMap.keySet())
		{
			final Map<String, AbilityMapping> revT = reverseAbilityMap.get(abilityID);
			final LinkedList<String> deleteThese=new LinkedList<String>();
			for(final String ID : revT.keySet())
			{
				final AbilityMapping able = revT.get(ID);
				if(able.allQualifyFlag())
					deleteThese.add(ID);
			}
			for(final String ID : deleteThese)
				revT.remove(ID);
		}

		for(final String ID : completeAbleMap.keySet())
		{
			final Map<String, AbilityMapping> ableMap = completeAbleMap.get(ID);
			final LinkedList<String> deleteThese=new LinkedList<String>();
			for(final String abilityID : ableMap.keySet())
			{
				final AbilityMapping able = ableMap.get(abilityID);
				if(able.allQualifyFlag())
					deleteThese.add(abilityID);
			}
			for(final String abilityID : deleteThese)
				ableMap.remove(abilityID);
		}
	}

	@Override
	public synchronized void saveAllQualifysFile(final Map<String, Map<String,AbilityMapping>> newMap)
	{
		// undo and then reapply the all qualifys list
		undoAllQualifysList();
		eachClassSet=null;
		for(final String ID : completeAbleMap.keySet())
		{
			if((!ID.equalsIgnoreCase("All"))
			&&(!ID.equalsIgnoreCase("Archon"))
			&&(CMClass.getCharClass(ID)!=null))
				handleEachAndClassAbility(completeAbleMap.get(ID), newMap, ID);
		}

		// now just save it
		final CMFile[] fileList = CMFile.getExistingExtendedFiles(Resources.makeFileResourceName("skills/allqualifylist.txt"),null,CMFile.FLAG_FORCEALLOW);
		final CMFile f = fileList[fileList.length-1]; //TODO: support them all
		List<String> set=new Vector<String>(0);
		if(f.exists() && f.canRead())
		{
			set=Resources.getFileLineVector(f.text());
		}
		final StringBuilder str=new StringBuilder("");
		for(final String line : set)
		{
			if(line.toUpperCase().startsWith("[ALL]")||line.toUpperCase().startsWith("[EACH]"))
			{
				str.append("\n\r\n\r");
				break;
			}
			else
			if(line.length()>0)
				str.append(line).append("\n\r");
		}
		Map<String,AbilityMapping> map;
		str.append("[EACH]").append("\n\r");
		map=newMap.get("EACH");
		if(map!=null)
			str.append(buildAllQualifysSection(map));
		str.append("[ALL]").append("\n\r");
		map=newMap.get("ALL");
		if(map!=null)
			str.append(buildAllQualifysSection(map));
		f.saveText(str.toString(),false);
	}

	@Override
	public PairList<String,Integer> getAvailabilityList(final Ability A, final int abbreviateAt)
	{
		final PairList<String,Integer> avail=new PairVector<String,Integer>();
		final Map<Integer,int[]> sortedByLevel=new TreeMap<Integer,int[]>();
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			final int lvl=getQualifyingLevel(C.ID(),true,A.ID());
			if((!C.ID().equalsIgnoreCase("Archon"))
			&&(lvl>=0)
			&&(C.availabilityCode()!=0)
			&&(getSecretSkill(C.ID(),true,A.ID())==SecretFlag.PUBLIC))
			{
				if(!sortedByLevel.containsKey(Integer.valueOf(lvl)))
					sortedByLevel.put(Integer.valueOf(lvl),new int[1]);
				sortedByLevel.get(Integer.valueOf(lvl))[0]++;
				avail.add(C.ID(),Integer.valueOf(lvl));
			}
		}
		for(final Iterator<Integer> e=sortedByLevel.keySet().iterator();e.hasNext();)
		{
			final Integer I=e.next();
			final int[] count=sortedByLevel.get(I);
			if(count[0]>abbreviateAt)
			{
				if(sortedByLevel.size()==1)
				{
					while(avail.size()>=abbreviateAt)
					{
						avail.remove(avail.size()-1);
						count[0]--;
					}
				}
				else
				for(int i=avail.size()-1;i>=0;i--)
				{
					if(avail.get(i).second.intValue()==I.intValue())
						avail.remove(i);
				}
				//if(count[0]>=(abbreviateAt*3))
				if(avail.size()==0)
					avail.add("Numerous Classes",I);
				else
					avail.add("+Other Classes",I);
			}
		}
		return avail;
	}

	@Override
	public Enumeration<CompoundingRule> compoundingRules()
	{
		return new IteratorEnumeration<CompoundingRule>(this.compoundingRules.iterator());
	}

	protected void loadCompoundingRules()
	{
		if(compoundingRulesLoaded || (!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			return;
		final List<String> compoundRuleStrs = CMParms.parseCommas(CMProps.getVar(Str.MANACOMPOUND_RULES), true);
		this.compounders.clear();
		this.compoundingRules.clear();
		for(String ruleStr : compoundRuleStrs)
		{
			final int x=ruleStr.indexOf(' ');
			if(x<=0)
			{
				Log.errOut("Bad rule in MANACOMPOUND: "+ruleStr);
				continue;
			}
			final String ticksStr=ruleStr.substring(0,x).trim();
			if(!CMath.isInteger(ticksStr))
			{
				Log.errOut("Bad ticks in MANACOMPOUND: "+ruleStr);
				continue;
			}
			final int finalTicks=CMath.s_int(ticksStr);
			ruleStr=ruleStr.substring(x+1).trim();
			final int y=ruleStr.indexOf(' ');
			String amtStr;
			if(y<0)
				amtStr=ruleStr;
			else
			{
				amtStr=ruleStr.substring(0,y).trim();
				ruleStr=ruleStr.substring(y+1).trim();
			}
			final double amtPct;
			final int amount;
			if(CMath.isPct(amtStr))
			{
				amtPct=CMath.s_pct(amtStr);
				amount=0;
			}
			else
			if(CMath.isInteger(amtStr))
			{
				amtPct=0.0;
				amount=CMath.s_int(amtStr);
			}
			else
			{
				Log.errOut("Bad amt in MANACOMPOUND: "+ruleStr);
				continue;
			}
			final String mobMaskStr = CMParms.getParmStr(ruleStr, "MOBMASK", "");
			final MaskingLibrary.CompiledZMask mobMask = (mobMaskStr.trim().length()>0) ? CMLib.masking().getPreCompiledMask(mobMaskStr) : null;
			final String ableMaskStr = CMParms.getParmStr(ruleStr, "ABILITYMASK", "");
			final MaskingLibrary.CompiledZMask ableMask = (ableMaskStr.trim().length()>0) ? CMLib.masking().getPreCompiledMask(ableMaskStr) : null;
			final AbilityMapper.CompoundingRule rule = new AbilityMapper.CompoundingRule()
			{
				final MaskingLibrary.CompiledZMask	mmask	= mobMask;
				final MaskingLibrary.CompiledZMask	amask	= ableMask;
				final double						pct		= amtPct;
				final int							amt		= amount;
				final int							ticks	= finalTicks;

				@Override
				public CompiledZMask mobMask()
				{
					return mmask;
				}

				@Override
				public CompiledZMask ableMask()
				{
					return amask;
				}

				@Override
				public int compoundingTicks()
				{
					return ticks;
				}

				@Override
				public double pctPenalty()
				{
					return pct;
				}

				@Override
				public int amtPenalty()
				{
					return amt;
				}
			};
			compoundingRules.add(rule);
		}
		compoundingRulesLoaded = true;
	}

	@Override
	public int getProfGainChance(final MOB mob, final Ability A)
	{
		final int qualLevel=CMLib.ableMapper().qualifyingLevel(mob,A);
		final double adjustedChance;
		if(qualLevel<0)
			adjustedChance=100.1;
		else
		{
			final int maxLevel=CMProps.get(mob.session()).getInt(CMProps.Int.LASTPLAYERLEVEL);
			final double[] vars = {
				maxLevel,
				qualLevel,
				(mob.curState().getFatigue() > CharState.FATIGUED_MILLIS) ? 1 : 0
			};
			adjustedChance = CMath.parseMathExpression(proficiencyGainFormula, vars, 0.0);
		}
		return (int)Math.round(adjustedChance);
	}

	@Override
	public void propertiesLoaded()
	{
		super.propertiesLoaded();
		activate();
		compoundingRulesLoaded=false;
		loadCompoundingRules();
	}
}
