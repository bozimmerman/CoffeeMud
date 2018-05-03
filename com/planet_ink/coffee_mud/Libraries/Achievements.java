package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementLoadFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.CostType;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericEditor.CMEval;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class Achievements extends StdLibrary implements AchievementLibrary
{
	@Override 
	public String ID()
	{
		return "Achievements";
	}
	
	// order is now significant, so they are Lists
	private List<Achievement> 			playerAchievements	= null;
	private List<Achievement> 			accountAchievements = null;
	private Map<Event,List<Achievement>>eventMap			= null;
	
	private final static String achievementFilename  = "achievements.ini";

	private static CMEval CMEVAL_INSTANCE = new CMEval()
	{
		@Override
		public Object eval(Object val, Object[] choices, boolean emptyOK) throws CMException
		{
			if(choices.length==0)
				return "";
			final String str=val.toString().trim();
			for(final Object o : choices)
			{
				if(str.equalsIgnoreCase(o.toString()))
					return o.toString();
			}
			throw new CMException("That was not one of your choices.");
		}
	};
	
	@Override
	public String evaluateAchievement(final Agent agent, String row, boolean addIfPossible)
	{
		if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
			return null;
		int x=row.indexOf('=');
		while((x>=1)&&(row.charAt(x-1)=='\\'))
			x=row.indexOf('=',x+1);
		if(x<0)
			return "Error: Invalid line! Not comment, whitespace, and does not contain an = sign!";
		final String tattoo=row.substring(0,x).toUpperCase().trim();
		if(tattoo.length()==0)
			return "Error: Blank achievement tattoo: "+tattoo+"!";
		if(Character.isDigit(tattoo.charAt(0)))
			return "Error: Invalid achievement tattoo: "+tattoo+"!";
		final String params=row.substring(x+1).trim();
		String eventStr=CMParms.getParmStr(params, "EVENT", "");
		final Event eventType = (Event)CMath.s_valueOf(Event.class, eventStr.toUpperCase().trim());
		if(eventType == null)
			return "Error: Blank or unknown achievement type: "+eventStr+"!";
		final String displayStr=CMStrings.deEscape(CMParms.getParmStr(params, "DISPLAY", ""));
		final String titleStr=CMStrings.deEscape(CMParms.getParmStr(params, "TITLE", ""));
		String rewardStr=CMStrings.deEscape(CMParms.getParmStr(params, "REWARDS", ""));
		String[] awardSet = CMParms.parse(rewardStr).toArray(new String[0]);
		List<Award> awardsList = new ArrayList<Award>();
		if(titleStr.length()>0)
		{
			awardsList.add(new TitleAward()
			{
				@Override
				public AwardType getType()
				{
					return AwardType.TITLE;
				}

				@Override
				public String getTitle()
				{
					return titleStr;
				}

				@Override
				public boolean isPreAwarded()
				{
					return false;
				}

				@Override
				public String getDescription()
				{
					return L("The title: @x1",getTitle());
				}

				@Override
				public boolean isNotAwardedOnRemort()
				{
					return false;
				}
			});
		}
		for(int a=0;a<awardSet.length;a++)
		{
			if(awardSet[a].length()>0)
			{
				String thing = "";
				if(CMath.isInteger(awardSet[a]))
				{
					final int number = CMath.s_int(awardSet[a]);
					a++;
					while((a<awardSet.length)&&(awardSet[a].length()>0)&&(!CMath.isInteger(awardSet[a])))
					{
						thing += awardSet[a]+" ";
						a++;
					}
					a--;
					thing = thing.toUpperCase().trim();
					if(thing.equals("XP") || thing.startsWith("EXPERIEN") || thing.equals("EXP"))
					{
						awardsList.add(new AmountAward()
						{
							@Override
							public AwardType getType()
							{
								return AwardType.XP;
							}

							@Override
							public int getAmount()
							{
								return number;
							}

							@Override
							public boolean isPreAwarded()
							{
								return false;
							}

							@Override
							public boolean isNotAwardedOnRemort()
							{
								return false;
							}

							@Override
							public String getDescription()
							{
								return L("@x1 Experience Points",""+getAmount());
							}
						});
					}
					else
					if(thing.equals("QP") || thing.startsWith("QUEST"))
					{
						awardsList.add(new AmountAward()
						{
							@Override
							public AwardType getType()
							{
								return AwardType.QP;
							}

							@Override
							public int getAmount()
							{
								return number;
							}

							@Override
							public boolean isPreAwarded()
							{
								return false;
							}

							@Override
							public String getDescription()
							{
								return L("@x1 Quest Points",""+getAmount());
							}
							
							@Override
							public boolean isNotAwardedOnRemort()
							{
								return false;
							}
						});
					}
					else
					{
						int y=thing.indexOf('(');
						String parms="";
						if((y>0) && thing.endsWith(")"))
						{
							parms=thing.substring(y+1,thing.length()-1);
							thing=thing.substring(0,y);
						}
						Ability A=CMClass.getAbility(thing);
						if(A!=null)
						{
							String ableParms = CMStrings.deEscape(CMParms.getParmStr(parms, "PARMS", ""));
							String mask = CMStrings.deEscape(CMParms.getParmStr(parms, "MASK", ""));
							String preReqs = CMStrings.deEscape(CMParms.getParmStr(parms, "PREREQS", ""));
							boolean autoGain = CMParms.getParmBool(parms, "AUTOGAIN", true);
							final AbilityMapper.AbilityMapping mapp=CMLib.ableMapper().newAbilityMapping();
							mapp.abilityID(A.ID())
								.qualLevel(number)
								.autoGain(autoGain)
								.extraMask(mask)
								.defaultParm(ableParms);
							if(preReqs.length()>0)
							{
								mapp.originalSkillPreReqList(preReqs);
							}
							awardsList.add(new AbilityAward()
							{
								@Override
								public AwardType getType()
								{
									return AwardType.ABILITY;
								}

								@Override
								public AbilityMapping getAbilityMapping()
								{
									return mapp;
								}
								
								@Override
								public boolean isPreAwarded()
								{
									return false;
								}
								
								@Override
								public String getDescription()
								{
									final Ability skillA = CMClass.getAbility(getAbilityMapping().abilityID());
									if(getAbilityMapping().autoGain())
										return L("@x1 at level @x2",skillA.name(),""+getAbilityMapping().qualLevel());
									else
										return L("@x1 qualification at level @x2",skillA.name(),""+getAbilityMapping().qualLevel());
								}

								@Override
								public boolean isNotAwardedOnRemort()
								{
									return false;
								}
							});
						}
						else
						if(CMLib.expertises().findDefinition(thing, true) != null)
						{
							final boolean isAutoGained = CMParms.getParmBool(parms, "AUTOGAIN", true);
							final ExpertiseDefinition oldDef=CMLib.expertises().findDefinition(thing, true);
							final String ID = oldDef.ID();
							final ExpertiseDefinition def = new ExpertiseDefinition()
							{
								volatile WeakReference<ExpertiseDefinition> ref=new WeakReference<ExpertiseDefinition>(CMLib.expertises().findDefinition(ID, true));
								private ExpertiseDefinition baseDef()
								{
									if(ref == null)
										return null;
									ExpertiseDefinition curDef = ref.get();
									if(curDef == null)
									{
										curDef = CMLib.expertises().findDefinition(ID, true); 
										if(curDef==null)
										{
											ref = null;
										}
										else
											ref = new WeakReference<ExpertiseDefinition>(curDef);
									}
									return curDef;
								}
								
								@Override
								public String ID()
								{
									return ID;
								}

								@Override
								public String name()
								{
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? "" : curDef.name();
								}

								@Override
								public CMObject newInstance()
								{
									return baseDef();
								}

								@Override
								public CMObject copyOf()
								{
									return baseDef();
								}

								@Override
								public void initializeClass()
								{
								}

								@Override
								public int compareTo(CMObject o)
								{
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? -1 : curDef.compareTo(o);
								}

								@Override
								public String getBaseName()
								{
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? "" : curDef.getBaseName();
								}

								@Override
								public void setBaseName(String baseName)
								{
								}

								@Override
								public void setName(String name)
								{
								}

								@Override
								public void setID(String ID)
								{
								}

								@Override
								public void setData(String[] data)
								{
								}

								@Override
								public ExpertiseDefinition getParent()
								{
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? null : curDef.getParent();
								}

								@Override
								public int getMinimumLevel()
								{
									return number;
								}

								@Override
								public String[] getData()
								{
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? new String[0] : curDef.getData();
								}

								@Override
								public CompiledZMask compiledListMask()
								{
									return CMLib.masking().getPreCompiledMask("-LEVEL +>="+number);
								}

								@Override
								public CompiledZMask compiledFinalMask()
								{
									return CMLib.masking().createEmptyMask();
								}

								@Override
								public String allRequirements()
								{
									return "-LEVEL +>="+number;
								}

								@Override
								public String listRequirements()
								{
									return "-LEVEL +>="+number;
								}

								@Override
								public String finalRequirements()
								{
									return "";
								}

								@Override
								public void addListMask(String mask)
								{
								}

								@Override
								public void addFinalMask(String mask)
								{
								}

								@Override
								public void addCost(CostType type, Double value)
								{
								}

								@Override
								public String costDescription()
								{
									if(isAutoGained)
										return "";
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? "" : curDef.costDescription();
								}

								@Override
								public boolean meetsCostRequirements(MOB mob)
								{
									if(isAutoGained)
										return true;
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? false : curDef.meetsCostRequirements(mob);
								}

								@Override
								public void spendCostRequirements(MOB mob)
								{
									if(isAutoGained)
										return;
									final ExpertiseDefinition curDef = baseDef();
									if(curDef != null)
										curDef.spendCostRequirements(mob);
								}
							};
							awardsList.add(new ExpertiseAward()
							{
								@Override
								public AwardType getType()
								{
									return AwardType.EXPERTISE;
								}

								@Override
								public int getLevel()
								{
									return number;
								}

								@Override
								public ExpertiseDefinition getExpertise()
								{
									return def;
								}
								
								@Override
								public boolean isPreAwarded()
								{
									return false;
								}

								@Override
								public String getDescription()
								{
									final ExpertiseDefinition defE = getExpertise();
									if(defE.costDescription().length()==0)
										return L("@x1 at level @x2",defE.name(),""+defE.getMinimumLevel());
									else
										return L("@x1 qualification at level @x1",""+defE.getMinimumLevel());
								}

								@Override
								public boolean isNotAwardedOnRemort()
								{
									return false;
								}
							});
						}
						else
						if(CMLib.coffeeMaker().isAnyGenStat(CMClass.samplePlayer(), thing))
						{
							final String stat1 = thing.toUpperCase().trim();
							String astat=CMLib.coffeeMaker().getFinalStatName(stat1);
							boolean isSave = false;
							for(int t : CharStats.CODES.SAVING_THROWS())
								isSave  = isSave  || CharStats.CODES.NAME(t).equals(astat.toUpperCase());
							final boolean isSavingThrow = isSave;
							final boolean isPreAwarded=
								(astat.startsWith("MAX") 
									&& (CharStats.CODES.findWhole(astat,true) >=0)
									&& (CMParms.contains(CharStats.CODES.MAXCODES(),CharStats.CODES.findWhole(astat,true))))
								|| astat.endsWith("BONUSCHARSTATS");
							final boolean isNotPremortAwarded = ((PlayerStats)CMClass.getCommonPrototype("DefaultPlayerStats")).isStat(astat);
							awardsList.add(new StatAward()
							{
								final String stat = stat1;
								final boolean savingThrow = isSavingThrow;
								final boolean preAwarded = isPreAwarded;
								final boolean noRemort = isNotPremortAwarded;

								@Override
								public AwardType getType()
								{
									return AwardType.STAT;
								}

								@Override
								public int getAmount()
								{
									return number;
								}

								@Override
								public String getStat()
								{
									return stat;
								}

								@Override
								public String getDescription()
								{
									if(getAmount()<0)
										return getAmount() + " " + L(CMStrings.capitalizeAndLower(getStat())+(savingThrow ? " resistance":""));
									else
										return "+"+getAmount() + " " + L(CMStrings.capitalizeAndLower(getStat())+(savingThrow ? " resistance":""));
								}

								@Override
								public boolean isPreAwarded()
								{
									return preAwarded;
								}

								@Override
								public boolean isNotAwardedOnRemort()
								{
									return noRemort;
								}
							});
						}
						else
						if(thing.toUpperCase().startsWith("ACCOUNT ")
						&&(CMClass.samplePlayer().playerStats()!=null)
						&&(CMClass.samplePlayer().playerStats().getAccount()!=null)
						&&CMLib.coffeeMaker().isAnyGenStat(CMClass.samplePlayer(), thing.substring(8)))
						{
							final String stat1 = thing.toUpperCase().trim();
							String astat=CMLib.coffeeMaker().getFinalStatName(stat1);
							boolean isSave = false;
							for(int t : CharStats.CODES.SAVING_THROWS())
								isSave  = isSave  || CharStats.CODES.NAME(t).equals(astat.toUpperCase());
							final boolean isSavingThrow = isSave;
							final boolean isPreAwarded=
								(astat.startsWith("MAX") 
									&& (CharStats.CODES.findWhole(astat,true) >=0)
									&& (CMParms.contains(CharStats.CODES.MAXCODES(),CharStats.CODES.findWhole(astat,true))))
								|| astat.endsWith("BONUSCHARSTATS");
							final boolean isNotPremortAwarded = ((PlayerStats)CMClass.getCommonPrototype("DefaultPlayerStats")).isStat(astat);
							awardsList.add(new StatAward()
							{
								final String stat = stat1;
								final boolean savingThrow = isSavingThrow;
								final boolean preAwarded = isPreAwarded;
								final boolean noRemort = isNotPremortAwarded;
								
								@Override
								public AwardType getType()
								{
									return AwardType.STAT;
								}

								@Override
								public int getAmount()
								{
									return number;
								}

								@Override
								public String getStat()
								{
									return stat;
								}

								@Override
								public String getDescription()
								{
									return "+"+getAmount() + " " + L(CMStrings.capitalizeAndLower(getStat())+(savingThrow?" resistance":""));
								}

								@Override
								public boolean isPreAwarded()
								{
									return preAwarded;
								}

								@Override
								public boolean isNotAwardedOnRemort()
								{
									return noRemort;
								}
							});
						}
						else
						{
							String currency = CMLib.english().matchAnyCurrencySet(thing);
							if(currency == null)
								Log.debugOut("Achievement", "Unknown award type: "+thing);
							else
							{
								double denomination = CMLib.english().matchAnyDenomination(currency, thing);
								if(denomination == 0.0)
									Log.debugOut("Achievement", "Unknown award type: "+thing);
								else
								{
									final String currencyName = thing;
									awardsList.add(new CurrencyAward()
									{
										@Override
										public AwardType getType()
										{
											return AwardType.CURRENCY;
										}

										@Override
										public int getAmount()
										{
											return number;
										}

										@Override
										public String getCurrency()
										{
											return currencyName;
										}

										@Override
										public boolean isPreAwarded()
										{
											return false;
										}

										@Override
										public String getDescription()
										{
											return getAmount() + " " + CMStrings.capitalizeAndLower(getCurrency());
										}

										@Override
										public boolean isNotAwardedOnRemort()
										{
											return false;
										}
									});
								}
							}
						}
					}
				}
			}
		}
		final Award[] rewardList = awardsList.toArray(new Award[0]); 
		Achievement A;
		switch(eventType)
		{
		case KILLS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask npcMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, (MOB)parms[0], true)))
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ZAPPERMASK", ""));
					if(zapperMask.trim().length()==0)
						return "Error: Missing or invalid ZAPPERMASK parameter: "+zapperMask+"!";
					this.npcMask = CMLib.masking().getPreCompiledMask(zapperMask);
					zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case JUSTBE:
			A=new Achievement()
			{
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return Integer.MIN_VALUE;
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
								return true;
							return false;
						}

						@Override
						public int getCount(MOB mob)
						{
							return 0;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
							{
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					if(zapperMask.trim().length()==0)
						return "Error: Missing or invalid PLAYERMASK parameter: "+zapperMask+"!";
					this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case STATVALUE:
			A=new Achievement()
			{
				private String	statName= "";
				private int 	value	= 0;
				private int		abelo	= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return value;
				}

				@Override
				public boolean isTargetFloor()
				{
					return abelo > 0;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (abelo > 0) ? (getCount(mob) > value) : (getCount(mob) < value);
						}

						@Override
						public int getCount(MOB mob)
						{
							return CMath.s_int(CMLib.coffeeMaker().getAnyGenStat(mob, statName));
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					MOB mob = CMClass.getFactoryMOB();
					final String numStr=CMParms.getParmStr(parms, "VALUE", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid VALUE parameter: "+numStr+"!";
					value=CMath.s_int(numStr);
					final String aboveBelow=CMParms.getParmStr(parms, "ABOVEBELOW", "").toUpperCase().trim();
					if((!aboveBelow.equals("ABOVE")) && (!aboveBelow.equals("BELOW")))
						return "Error: Missing or invalid ABOVEBELOW parameter: "+aboveBelow+"!";
					this.abelo = aboveBelow.equals("ABOVE")? 1 : -1;
					final String statName=CMParms.getParmStr(parms, "STAT", "").toUpperCase().trim();
					if((statName.length()==0)||(!CMLib.coffeeMaker().isAnyGenStat(mob, statName)))
						return "Error: Missing or invalid STAT parameter: "+statName+"!";
					this.statName = statName;
					return "";
				}
			};
			break;
		case FACTION:
			A=new Achievement()
			{
				private String	factionID	= "";
				private int 	value		= 0;
				private int		abelo		= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public boolean isTargetFloor()
				{
					return abelo > 0;
				}
				
				@Override
				public int getTargetCount()
				{
					return value;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							if(mob.fetchFaction(factionID)==Integer.MAX_VALUE)
								return false;
							return (abelo > 0) ? (getCount(mob) > value) : (getCount(mob) < value);
						}

						@Override
						public int getCount(MOB mob)
						{
							int f=mob.fetchFaction(factionID);
							if(f == Integer.MAX_VALUE)
								return 0;
							return f;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "VALUE", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid VALUE parameter: "+numStr+"!";
					value=CMath.s_int(numStr);
					final String aboveBelow=CMParms.getParmStr(parms, "ABOVEBELOW", "").toUpperCase().trim();
					if((!aboveBelow.equals("ABOVE")) && (!aboveBelow.equals("BELOW")))
						return "Error: Missing or invalid ABOVEBELOW parameter: "+aboveBelow+"!";
					this.abelo = aboveBelow.equals("ABOVE")? 1 : -1;
					final String factionID=CMParms.getParmStr(parms, "ID", "").toUpperCase().trim();
					if(factionID.length()==0)
						return "Error: Missing ID parameter: "+factionID+"!";
					if(CMLib.factions().getFaction(factionID)==null)
						return "Error: Unknown faction ID parameter: "+factionID+"!";
					this.factionID=factionID;
					return "";
				}
			};
			break;
		case FACTIONS:
			A=new Achievement()
			{
				private final List<Faction>	factions	= new LinkedList<Faction>();
				private int 			number		= 0;
				private int 			value		= 0;
				private int				abelo		= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public boolean isTargetFloor()
				{
					return abelo > 0;
				}
				
				@Override
				public int getTargetCount()
				{
					return number;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= number;
						}

						@Override
						public int getCount(MOB mob)
						{
							int num=0;
							for(Faction F : factions)
							{
								int f = mob.fetchFaction(F.factionID());
								if((f!=Integer.MAX_VALUE)
								&&((abelo > 0) ? (f > value) : (f < value)))
									num++;
							}
							return num;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					String numStr=CMParms.getParmStr(parms, "VALUE", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid VALUE parameter: "+numStr+"!";
					value=CMath.s_int(numStr);
					numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					number=CMath.s_int(numStr);
					final String aboveBelow=CMParms.getParmStr(parms, "ABOVEBELOW", "").toUpperCase().trim();
					if((!aboveBelow.equals("ABOVE")) && (!aboveBelow.equals("BELOW")))
						return "Error: Missing or invalid ABOVEBELOW parameter: "+aboveBelow+"!";
					this.abelo = aboveBelow.equals("ABOVE")? 1 : -1;
					final String factionIDMask=CMParms.getParmStr(parms, "IDMASK", "").toUpperCase().trim();
					this.factions.clear();
					for(Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
					{
						final Faction F=f.nextElement();
						if(CMStrings.filenameMatcher(F.factionID(), factionIDMask))
							this.factions.add(F);
					}
					if(this.factions.size()==0)
						return "Error: No existing factions match: "+factionIDMask+"!";
					return "";
				}
			};
			break;
		case EXPLORE:
			A=new Achievement()
			{
				private String	areaID	= "";
				private int	 	pct		= 0;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public int getTargetCount()
				{
					return pct;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}
				
				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= pct;
						}

						@Override
						public int getCount(MOB mob)
						{
							final PlayerStats pstats=mob.playerStats();
							if(pstats != null)
							{
								if(areaID.equals("WORLD"))
								{
									final Room R=mob.location();
									if((R!=null)&&(CMLib.map().getExtendedRoomID(CMLib.map().getRoom(R)).length()>0))
										return pstats.percentVisited(mob,null);
									else
										return 0;
								}
								else
								{
									final Area A=CMLib.map().getArea(areaID);
									if(A!=null)
									{
										return pstats.percentVisited(mob, A);
									}
								}
							}
							return 0;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}

				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "PERCENT", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid PERCENT parameter: "+numStr+"!";
					this.pct=CMath.s_int(numStr);
					final String areaID=CMParms.getParmStr(parms, "AREA", "").toUpperCase().trim();
					if(areaID.length()==0)
						return "Error: Missing AREA parameter: "+areaID+"!";
					if((CMLib.map().getArea(areaID)==null)&&(!areaID.equals("WORLD")))
						return "Error: Unknown AREA: "+areaID+"!";
					this.areaID=areaID;
					return "";
				}
			};
			break;
		case CRAFTING:
		case MENDER:
			A=new Achievement()
			{
				private int 				num 		= 0;
				private final Set<String>	abilityIDs 	= new TreeSet<String>();
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}
				
				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;
						
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= num;
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							final Ability A;
							if(parms.length>0)
							{
								if(parms[0] instanceof String)
									A=CMClass.getAbility((String)parms[0]);
								else
								if(parms[0] instanceof Ability)
									A=(Ability)parms[0];
								else
									A=null;
								if((A!=null)&&(abilityIDs.contains("*")||abilityIDs.contains(A.ID())))
								{
									count+=bumpNum;
									return true;
								}
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}

				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					this.num=CMath.s_int(numStr);
					final String abilityIDs=CMParms.getParmStr(parms, "ABILITYID", "").toUpperCase().trim();
					if(abilityIDs.length()==0)
						return "Error: Missing ABILITYID parameter: "+abilityIDs+"!";
					final String[] strList=abilityIDs.split(",");
					this.abilityIDs.clear();
					for(int i=0;i<strList.length;i++)
					{
						String abilityID = strList[i].trim();
						if(abilityID.equals("*"))
						{
							this.abilityIDs.add(abilityID);
							break;
						}
						else
						{
							final Ability A=CMClass.getAbility(abilityID);
							if((A==null)
							||((CMClass.getAbility(abilityID).classificationCode() & Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL)
							||((CMClass.getAbility(abilityID).classificationCode() & Ability.ALL_DOMAINS)!=Ability.DOMAIN_CRAFTINGSKILL))
								return "Error: Unknown crafting ABILITYID: "+abilityID+"!";
							this.abilityIDs.add(A.ID());
						}
					}
					if(this.abilityIDs.size()==0)
						return "Error: Unknown crafting ABILITYIDs: "+abilityIDs+"!";
					return "";
				}
			};
			break;
		case SKILLUSE:
			A=new Achievement()
			{
				private int 				num 		= 0;
				private final Set<String>	abilityIDs 	= new TreeSet<String>();
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public int getTargetCount()
				{
					return num;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;
						
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= num;
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							final Ability A;
							if(parms.length>0)
							{
								if(parms[0] instanceof String)
									A=CMClass.getAbility((String)parms[0]);
								else
								if(parms[0] instanceof Ability)
									A=(Ability)parms[0];
								else
									A=null;
								if((A!=null)
								&&(abilityIDs.contains("*")
									||abilityIDs.contains(A.ID())
									||(abilityIDs.contains(Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES]))
									||(abilityIDs.contains(Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5]))))
								{
									count+=bumpNum;
									return true;
								}
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}

				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					this.num=CMath.s_int(numStr);
					final String abilityIDs=CMParms.getParmStr(parms, "ABILITYID", "").toUpperCase().trim();
					if(abilityIDs.length()==0)
						return "Error: Missing ABILITYID parameter: "+abilityIDs+"!";
					final String[] strList=abilityIDs.split(",");
					this.abilityIDs.clear();
					for(int i=0;i<strList.length;i++)
					{
						String abilityID = strList[i].trim();
						if(abilityID.equals("*"))
						{
							this.abilityIDs.add(abilityID);
							break;
						}
						else
						{
							final Ability A;
							if(CMParms.contains(Ability.ACODE_DESCS,abilityID)
							||CMParms.contains(Ability.DOMAIN_DESCS,abilityID))
								A=null;
							else
							{
								A=CMClass.getAbility(abilityID);
								if(A==null)
									return "Error: Unknown ABILITYID: "+abilityID+"!";
							}
							if(A!=null)
								this.abilityIDs.add(A.ID());
							else
								this.abilityIDs.add(abilityID.toUpperCase());
						}
					}
					if(this.abilityIDs.size()==0)
						return "Error: Unknown crafting ABILITYIDs: "+abilityIDs+"!";
					return "";
				}
			};
			break;
		case QUESTOR:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask mask = null;
				private java.util.regex.Pattern questPattern = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public int getTargetCount()
				{
					return num;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;
						
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && getCount(mob) >= num;
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							if((mask!=null)&&(!CMLib.masking().maskCheck(mask, mob, true)))
								return false;
							if(parms.length>0)
							{
								boolean match=questPattern == null;
								if(!match)
								{
									if(parms[0] instanceof String)
									{
										match = questPattern.matcher((String)parms[0]).find();
									}
									else
									if(parms[0] instanceof Quest)
									{
										Quest Q=(Quest)parms[0];
										match = 
											questPattern.matcher(Q.name()).find()
											|| questPattern.matcher(Q.displayName()).find();
									}
								}
								if(match)
								{
									count+=bumpNum;
									return true;
								}
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}

				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					if(zapperMask.trim().length()>0)
						this.mask = CMLib.masking().getPreCompiledMask(zapperMask);
					else
						this.mask = null;
					final String questMask=CMStrings.deEscape(CMParms.getParmStr(parms, "QUESTMASK", ""));
					this.questPattern = null;
					if(questMask.trim().length()>0)
					{
						try
						{
							java.util.regex.Pattern P = java.util.regex.Pattern.compile(questMask);
							if(P!=null)
								questPattern = P;
						}
						catch(Exception e)
						{
							return "Error: Invalid QUESTMASK regular expression parameter: "+questMask+": "+e.getMessage()+"!";
						}
					}
					return "";
				}
			};
			break;
		case ACHIEVER:
			A=new Achievement()
			{
				final Set<String> achievementList = new TreeSet<String>();

				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public int getTargetCount()
				{
					if(achievementList.size()<2)
						return Integer.MIN_VALUE;
					else
						return achievementList.size();
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= achievementList.size();
						}

						@Override
						public int getCount(MOB mob)
						{
							int count = 0;
							Tattooable other;
							if((CMProps.isUsingAccountSystem())
							&&(mob.playerStats()!=null))
								other=mob.playerStats().getAccount();
							else
								other=null;
							for(String s : achievementList)
							{
								if(mob.findTattoo(s)!=null)
									count++;
								else
								if((other!=null)&&(other.findTattoo(s)!=null))
									count++;
							}
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}

				@Override
				public String parseParms(final String parms)
				{
					final String list=CMStrings.deEscape(CMParms.getParmStr(parms, "ACHIEVEMENTLIST", ""));
					if(list.trim().length()==0)
						return "Error: Missing or invalid ACHIEVEMENTLIST parameter: "+list+"!";
					final String[] listArray = list.toUpperCase().trim().split(",");
					achievementList.clear();
					for(String s : listArray)
					{
						if(s.trim().length()>0)
							achievementList.add(s.trim());
					}
					if(achievementList.size()==0)
						return "Error: Missing or invalid ACHIEVEMENTLIST parameter: "+list+"!";
					return "";
				}
			};
			break;
		case ROOMENTER:
			A=new Achievement()
			{
				final Set<String> roomIDs = new TreeSet<String>();

				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public int getTargetCount()
				{
					return roomIDs.size();
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= roomIDs.size();
						}

						@Override
						public int getCount(MOB mob)
						{
							int count = 0;
							for(String s : roomIDs)
							{
								if(mob.playerStats().hasVisited(CMLib.map().getRoom(s)))
									count++;
							}
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms) 
						{
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}

				@Override
				public String parseParms(final String parms)
				{
					final String list=CMStrings.deEscape(CMParms.getParmStr(parms, "ROOMID", ""));
					if(list.trim().length()==0)
						return "Error: Missing or invalid ROOMID parameter: "+list+"!";
					final String[] listArray = list.toUpperCase().trim().split(",");
					roomIDs.clear();
					for(String s : listArray)
					{
						s=s.trim();
						if(s.length()>0)
						{
							Room R=CMLib.map().getRoom(s);
							if(R==null)
							{
								if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
									return "Error: Missing or invalid ROOMID: "+s+"!";
								else
									roomIDs.add(s);
							}
							else
								roomIDs.add(CMLib.map().getExtendedRoomID(R));
						}
					}
					if(roomIDs.size()==0)
						return "Error: Missing or invalid ROOMID parameter: "+list+"!";
					return "";
				}
			};
			break;
		case CLASSLEVELSGAINED:
			A=new Achievement()
			{
				private int num = -1;
				private CharClass charClass = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) > num);
						}

						@Override
						public int getCount(MOB mob)
						{
							if(mob == null)
								return 0;
							int classLevel = mob.charStats().getClassLevel(charClass);
							if(classLevel < 0)
								return 0;
							return classLevel + 1;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if(((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							&&(mob.charStats().getCurrentClass() == charClass))
							{
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					String charClassID=CMStrings.deEscape(CMParms.getParmStr(parms, "CLASS", ""));
					this.charClass = CMClass.getCharClass(charClassID);
					if(this.charClass == null)
						this.charClass = CMClass.findCharClass(charClassID);
					if(this.charClass == null)
						return "Error: Missing or invalid CLASS parameter: "+charClassID+"!";
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case RETIRE:
		case REMORT:
		case LEVELSGAINED:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}

				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
				}

				@Override
				public boolean isTargetFloor()
				{
					return true;
				}

				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case GOTITEM:
			A=new Achievement()
			{
				private MaskingLibrary.CompiledZMask playerMask = null;
				private MaskingLibrary.CompiledZMask itemMask = null;
				private int num = 1;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							if(mob != null)
							{
								if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
								{
									int count = 0;
									final boolean noMask=(itemMask==null);
									for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
									{
										final Item I=i.nextElement();
										if(noMask || (CMLib.masking().maskCheck(itemMask, I, true)))
											count++;
									}
									return count;
								}
							}
							return 0;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							{
								if((parms[0] instanceof Item)
								&&((itemMask==null)||(CMLib.masking().maskCheck(itemMask, (Item)parms[0], true))))
								{
									return true;
								}
							}
							return false;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return false;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(numStr.trim().length()==0)
						num=1;
					else
					if(!CMath.isInteger(numStr))
						return "Error: Invalid NUM parameter!";
					else
						num=CMath.s_int(numStr);
					String itemZapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ITEMMASK", ""));
					if(itemZapperMask.trim().length()>0)
						this.itemMask = CMLib.masking().getPreCompiledMask(itemZapperMask);
					else
						return "Error: Missing or invalid ITEMMASK parameter!";
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case TIMEPLAYED:
			A=new Achievement()
			{
				private int seconds = 0;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}

				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return seconds;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}

				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (getCount(mob) >= seconds);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "SECONDS", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid SECONDS parameter: "+numStr+"!";
					seconds=CMath.s_int(numStr);
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case BIRTHS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask npcMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, (MOB)parms[0], true)))
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					this.npcMask = null;
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ZAPPERMASK", ""));
					if(zapperMask.trim().length()>=0)
						this.npcMask = CMLib.masking().getPreCompiledMask(zapperMask);
					zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
			//("Births",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		case RACEBIRTH:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask npcMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, (MOB)parms[0], true)))
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					this.npcMask = null;
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ZAPPERMASK", ""));
					if(zapperMask.trim().length()>=0)
						this.npcMask = CMLib.masking().getPreCompiledMask(zapperMask);
					zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
			//("Race Creation",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		case PLAYERBORNPARENT:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask npcMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, (MOB)parms[0], true)))
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					this.npcMask = null;
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ZAPPERMASK", ""));
					if(zapperMask.trim().length()>=0)
						this.npcMask = CMLib.masking().getPreCompiledMask(zapperMask);
					zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case PLAYERBORN:
			A=new Achievement()
			{
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return 1;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return getCount(mob) >= 1;
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
			//("Player Birth",new String[]{"NUM","ZAPPERMASK","PLAYERMASK"}),
		case DEATHS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask npcMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				
				@Override
				public Event getEvent()
				{
					return eventType;
				}
				
				@Override
				public Agent getAgent()
				{
					return agent;
				}
				
				@Override
				public String getTattoo()
				{
					return tattoo;
				}

				@Override
				public int getTargetCount()
				{
					return num;
					
				}
				
				@Override
				public boolean isTargetFloor()
				{
					return true;
				}
				
				@Override
				public String getDisplayStr()
				{
					return displayStr;
				}

				@Override
				public Award[] getRewards()
				{
					return rewardList;
				}
				
				@Override
				public String getRawParmVal(String str)
				{
					return CMParms.getParmStr(params,str,"");
				}

				@Override
				public Tracker getTracker(final int oldCount)
				{
					final Achievement me=this;
					return new Tracker()
					{
						private volatile int count = oldCount;

						@Override
						public Achievement getAchievement() 
						{
							return me;
						}

						@Override
						public boolean isAchieved(MOB mob) 
						{
							return (num>=0) && (getCount(mob) >= num);
						}

						@Override
						public int getCount(MOB mob)
						{
							return count;
						}

						@Override
						public boolean testBump(MOB mob, int bumpNum, Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof MOB)
							&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, (MOB)parms[0], true)))
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
							{
								count+=bumpNum;
								return true;
							}
							return false;
						}
						
						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(Exception e)
							{
								return this;
							}
						}
					};
				}
				
				@Override
				public boolean isSavableTracker()
				{
					return true;
				}
				
				@Override
				public String parseParms(final String parms)
				{
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					this.npcMask = null;
					String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ZAPPERMASK", ""));
					if(zapperMask.trim().length()>=0)
						this.npcMask = CMLib.masking().getPreCompiledMask(zapperMask);
					zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		default:
			A=null;
			break;
		}
		
		if(A==null)
		{
			return "Error: Unimplemented achievement type: "+eventStr+"!";
		}
		
		final String err = A.parseParms(params);
		if((err!=null)&&(err.length()>0))
		{
			return err;
		}
		
		if(addIfPossible)
		{
			// the call to achievements below will ensure others are loaded.
			for(final Enumeration<Achievement> a2  = achievements(null); a2.hasMoreElements();)
			{
				final Achievement A2=a2.nextElement();
				if(A2.getTattoo().equalsIgnoreCase(tattoo))
					return "Error: Duplicate achievement ID: "+tattoo+"!";
			}
			switch(agent)
			{
			case ACCOUNT:
				accountAchievements.add(A);
				break;
			case PLAYER:
				playerAchievements.add(A);
				break;
			}
			List<Achievement> eventList = eventMap.get(A.getEvent());
			if(eventList == null)
			{
				eventList = new LinkedList<Achievement>();
				eventMap.put(A.getEvent(), eventList);
			}
			eventList.add(A);
		}
		return null;
	}
	
	private void ensureAchievementsLoaded()
	{
		if((playerAchievements==null)||(accountAchievements==null))
		{
			synchronized(this)
			{
				if((playerAchievements==null)||(accountAchievements==null))
				{
					reloadAchievements();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")

	@Override
	public synchronized Enumeration<Achievement> achievements(Agent agent)
	{
		ensureAchievementsLoaded();
		if(agent == null)
		{
			return new MultiEnumeration<Achievement>(new Enumeration[]{
				new IteratorEnumeration<Achievement>(accountAchievements.iterator()),
				new IteratorEnumeration<Achievement>(playerAchievements.iterator()),
			});
		}
		switch(agent)
		{
		case ACCOUNT:
			return new IteratorEnumeration<Achievement>(accountAchievements.iterator());
		default:
		case PLAYER:
			return new IteratorEnumeration<Achievement>(playerAchievements.iterator());
		}
	}

	protected void possiblyBumpPlayerAchievement(final MOB mob, final Achievement A, final PlayerStats pStats, final Event E, int bumpNum, Object... parms)
	{
		if(mob.findTattoo(A.getTattoo())==null)
		{
			final Tracker T=pStats.getAchievementTracker(A, mob);
			if(T.testBump(mob, bumpNum, parms))
			{
				if(T.isAchieved(mob))
				{
					giveAwards(A,mob,mob,AchievementLoadFlag.NORMAL);
				}
			}
		}
	}
	
	protected void possiblyBumpAccountAchievement(final MOB mob, final Achievement A, final PlayerAccount account, final Event E, int bumpNum, Object... parms)
	{
		if(account != null)
		{
			if(account.findTattoo(A.getTattoo())==null)
			{
				final Tracker T=account.getAchievementTracker(A, mob);
				if(T.testBump(mob, bumpNum, parms))
				{
					if(T.isAchieved(mob))
					{
						giveAwards(A,account,mob,AchievementLoadFlag.NORMAL);
					}
				}
			}
		}
	}
	
	@Override
	public void possiblyBumpAchievement(final MOB mob, final Event E, int bumpNum, Object... parms)
	{
		if((mob != null)&&(E!=null)&&(!mob.isMonster())&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
		{
			ensureAchievementsLoaded();
			final PlayerStats pStats = mob.playerStats();
			if(pStats != null)
			{
				if(eventMap.containsKey(E))
				{
					final PlayerAccount account = pStats.getAccount();
					for(final Achievement A :  eventMap.get(E))
					{
						switch(A.getAgent())
						{
						case PLAYER:
							possiblyBumpPlayerAchievement(mob, A, pStats, E, bumpNum, parms);
							break;
						case ACCOUNT:
							possiblyBumpAccountAchievement(mob, A, account, E, bumpNum, parms);
							break;
						}
					}
				}
			}
		}
	}

	protected List<Achievement> fakeBumpPlayerAchievement(final MOB mob, final Achievement A, final PlayerStats pStats, final Event E, int bumpNum, Object... parms)
	{
		List<Achievement> achievements=new ArrayList<Achievement>(1);
		if(mob.findTattoo(A.getTattoo())==null)
		{
			Tracker T=pStats.getAchievementTracker(A, mob);
			T=T.copyOf();
			if(T.testBump(mob, bumpNum, parms))
			{
				if(T.isAchieved(mob))
				{
					achievements.add(A);
				}
			}
		}
		return achievements;
	}
	
	protected List<Achievement> fakeBumpAccountAchievement(final MOB mob, final Achievement A, final PlayerAccount account, final Event E, int bumpNum, Object... parms)
	{
		List<Achievement> achievements=new ArrayList<Achievement>(1);
		if(account != null)
		{
			if(account.findTattoo(A.getTattoo())==null)
			{
				Tracker T=account.getAchievementTracker(A, mob);
				T=T.copyOf();
				if(T.testBump(mob, bumpNum, parms))
				{
					if(T.isAchieved(mob))
					{
						achievements.add(A);
					}
				}
			}
		}
		return achievements;
	}
	
	@Override
	public List<Achievement> fakeBumpAchievement(final MOB mob, final Event E, int bumpNum, Object... parms)
	{
		List<Achievement> achievements=new ArrayList<Achievement>(1);
		if((mob != null)&&(E!=null)&&(!mob.isMonster())&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
		{
			ensureAchievementsLoaded();
			final PlayerStats pStats = mob.playerStats();
			if(pStats != null)
			{
				if(eventMap.containsKey(E))
				{
					final PlayerAccount account = pStats.getAccount();
					for(final Achievement A :  eventMap.get(E))
					{
						switch(A.getAgent())
						{
						case PLAYER:
							achievements.addAll(fakeBumpPlayerAchievement(mob, A, pStats, E, bumpNum, parms));
							break;
						case ACCOUNT:
							achievements.addAll(fakeBumpAccountAchievement(mob, A, account, E, bumpNum, parms));
							break;
						}
					}
				}
			}
		}
		return achievements;
	}

	protected void giveAwards(final MOB mob, final Award[] awardSet, final AchievementLoadFlag flag)
	{
		if(mob == null)
			return;
		final PlayerStats pStats = mob.playerStats();
		StringBuilder awardMessage = new StringBuilder("");
		for(final Award award : awardSet)
		{
			switch(flag)
			{
			case REMORT_PRELOAD:
				if(!award.isPreAwarded())
					continue;
				if(award.isNotAwardedOnRemort())
					continue;
				break;
			case REMORT_POSTLOAD:
				if(award.isPreAwarded())
					continue;
				if(award.isNotAwardedOnRemort())
					continue;
				break;
			case CHARCR_PRELOAD:
				if(!award.isPreAwarded())
					continue;
				break;
			case CHARCR_POSTLOAD:
				if(award.isPreAwarded())
					continue;
				break;
			case NORMAL:
				break;
			}
			switch(award.getType())
			{
			case ABILITY:
			{
				final AbilityAward aaward = (AbilityAward)award;
				if((pStats!=null) && (!pStats.getExtraQualifiedSkills().containsKey(aaward.getAbilityMapping().abilityID())))
				{
					final Ability A=CMClass.getAbility(aaward.getAbilityMapping().abilityID());
					if(A!=null)
					{
						pStats.getExtraQualifiedSkills().put(A.ID(), aaward.getAbilityMapping());
						awardMessage.append(L("^HYou are awarded a qualification for @x1 at level @x2!\n\r^?",A.name(),""+aaward.getAbilityMapping().qualLevel()));
					}
				}
				break;
			}
			case CURRENCY:
			{
				final CurrencyAward aaward=(CurrencyAward)award;
				String currency = CMLib.english().matchAnyCurrencySet(aaward.getCurrency());
				if(currency != null)
				{
					double denomination = CMLib.english().matchAnyDenomination(currency, aaward.getCurrency());
					if(denomination != 0.0)
					{
						double money=CMath.mul(aaward.getAmount(),  denomination);
						CMLib.beanCounter().giveSomeoneMoney(mob, money);
						awardMessage.append(L("^HYou are awarded @x1!\n\r^?",CMLib.beanCounter().getDenominationName(currency, denomination, aaward.getAmount())));
					}
				}
				break;
			}
			case STAT:
			{
				final StatAward aaward=(StatAward)award;
				if((mob.playerStats()!=null)
				&&(mob.playerStats().getAccount()!=null)
				&&(aaward.getStat().startsWith("ACCOUNT ")))
				{
					final String stat = aaward.getStat().substring(8).trim();
					String value = mob.playerStats().getAccount().getStat(stat);
					if(CMath.isNumber(value))
					{
						awardMessage.append(L("^HYour account is awarded @x1!\n\r^?",aaward.getAmount() + " " + stat));
						mob.playerStats().getAccount().setStat(stat, "" + (CMath.s_int(value) + aaward.getAmount()));
					}
				}
				else
				{
					String value = CMLib.coffeeMaker().getAnyGenStat(mob, aaward.getStat());
					if(CMath.isNumber(value))
					{
						awardMessage.append(L("^HYou are awarded @x1!\n\r^?",aaward.getAmount() + " " + aaward.getStat()));
						CMLib.coffeeMaker().setAnyGenStat(mob, aaward.getStat(), "" + (CMath.s_int(value) + aaward.getAmount()));
					}
				}
				break;
			}
			case EXPERTISE:
			{
				final ExpertiseAward aaward = (ExpertiseAward)award;
				if(pStats!=null)
				{
					if(!pStats.getExtraQualifiedExpertises().containsKey(aaward.getExpertise().ID()))
					{
						pStats.getExtraQualifiedExpertises().put(aaward.getExpertise().ID(), aaward.getExpertise());
						awardMessage.append(L("^HYou are awarded a qualification for @x1 at level @x2!\n\r^?",aaward.getExpertise().name(),""+aaward.getLevel()));
					}
				}
				break;
			}
			case QP:
			{
				final AmountAward aaward=(AmountAward)award;
				awardMessage.append(L("^HYou are awarded @x1 quest points!\n\r^?\n\r",""+aaward.getAmount()));
				mob.setQuestPoint(mob.getQuestPoint() + aaward.getAmount());
				break;
			}
			case TITLE:
			{
				final TitleAward aaward=(TitleAward)award;
				if((pStats != null) && (!pStats.getTitles().contains(aaward.getTitle())))
				{
					pStats.getTitles().add(aaward.getTitle());
					awardMessage.append(L("^HYou are awarded the title: @x1!\n\r^?",CMStrings.replaceAll(aaward.getTitle(),"*",mob.Name())));
				}
				break;
			}
			case XP:
			{
				final AmountAward aaward=(AmountAward)award;
				awardMessage.append(L("^HYou are awarded experience points!\n\r^?\n\r"));
				CMLib.leveler().postExperience(mob, null, null, aaward.getAmount(), false);
				break;
			}
			default:
				break;
			
			}
		}
		mob.tell(awardMessage.toString());
		grantAbilitiesAndExpertises(mob);
	}
	
	@Override
	public String removeAwards(final MOB mob, final Award[] awardSet, final AchievementLoadFlag flag)
	{
		if(mob == null)
			return "";
		final PlayerStats pStats = mob.playerStats();
		StringBuilder awardMessage = new StringBuilder("");
		for(final Award award : awardSet)
		{
			switch(flag)
			{
			case REMORT_PRELOAD:
				if(!award.isPreAwarded())
					continue;
				if(award.isNotAwardedOnRemort())
					continue;
				break;
			case REMORT_POSTLOAD:
				if(award.isPreAwarded())
					continue;
				if(award.isNotAwardedOnRemort())
					continue;
				break;
			case CHARCR_PRELOAD:
				if(!award.isPreAwarded())
					continue;
				break;
			case CHARCR_POSTLOAD:
				if(award.isPreAwarded())
					continue;
				break;
			case NORMAL:
				break;
			}
			switch(award.getType())
			{
			case ABILITY:
			{
				final AbilityAward aaward = (AbilityAward)award;
				if((pStats!=null) && (pStats.getExtraQualifiedSkills().containsKey(aaward.getAbilityMapping().abilityID())))
				{
					final Ability A=CMClass.getAbility(aaward.getAbilityMapping().abilityID());
					if(A!=null)
					{
						pStats.getExtraQualifiedSkills().remove(A.ID());
						awardMessage.append(L("^HYou have lost your qualification for @x1 at level @x2!\n\r^?",A.name(),""+aaward.getAbilityMapping().qualLevel()));
					}
				}
				break;
			}
			case CURRENCY:
			{
				// is never un-awarded
				break;
			}
			case STAT:
			{
				final StatAward aaward=(StatAward)award;
				if((mob.playerStats()!=null)
				&&(mob.playerStats().getAccount()!=null)
				&&(aaward.getStat().startsWith("ACCOUNT ")))
				{
					final String stat = aaward.getStat().substring(8).trim();
					String value = mob.playerStats().getAccount().getStat(stat);
					if(CMath.isNumber(value))
					{
						int oldVal = CMath.s_int(value);
						if(oldVal >= aaward.getAmount())
						{
							awardMessage.append(L("^HYour account has lost @x1!\n\r^?",aaward.getAmount() + " " + stat));
							mob.playerStats().getAccount().setStat(stat, "" + (oldVal - aaward.getAmount()));
						}
					}
				}
				else
				{
					String value = CMLib.coffeeMaker().getAnyGenStat(mob, aaward.getStat());
					if(CMath.isNumber(value))
					{
						int oldVal = CMath.s_int(value);
						if(oldVal >= aaward.getAmount())
						{
							awardMessage.append(L("^HYou have lost @x1!\n\r^?",aaward.getAmount() + " " + aaward.getStat()));
							CMLib.coffeeMaker().setAnyGenStat(mob, aaward.getStat(), "" + (CMath.s_int(value) - aaward.getAmount()));
						}
					}
				}
				break;
			}
			case EXPERTISE:
			{
				final ExpertiseAward aaward = (ExpertiseAward)award;
				if(pStats!=null)
				{
					if(pStats.getExtraQualifiedExpertises().containsKey(aaward.getExpertise().ID()))
					{
						pStats.getExtraQualifiedExpertises().remove(aaward.getExpertise().ID());
						awardMessage.append(L("^HYou have lost a qualification for @x1 at level @x2!\n\r^?",aaward.getExpertise().name(),""+aaward.getLevel()));
					}
				}
				break;
			}
			case QP:
			{
				final AmountAward aaward=(AmountAward)award;
				if(mob.getQuestPoint() >= aaward.getAmount())
				{
					awardMessage.append(L("^HYou have lost @x1 quest points!\n\r^?\n\r",""+aaward.getAmount()));
					mob.setQuestPoint(mob.getQuestPoint() - aaward.getAmount());
				}
				break;
			}
			case TITLE:
			{
				final TitleAward aaward=(TitleAward)award;
				if((pStats != null) && (pStats.getTitles().contains(aaward.getTitle())))
				{
					pStats.getTitles().remove(aaward.getTitle());
					awardMessage.append(L("^HYou have lost the title: @x1!\n\r^?",CMStrings.replaceAll(aaward.getTitle(),"*",mob.Name())));
				}
				break;
			}
			case XP:
			{
				// also, never lost
				break;
			}
			default:
				break;
			
			}
		}
		return awardMessage.toString();
	}
	
	protected boolean giveAwards(final Achievement A, final Tattooable holder, final MOB mob, final AchievementLoadFlag flag)
	{
		if(holder.findTattoo(A.getTattoo())==null)
		{
			holder.addTattoo(A.getTattoo());
			StringBuilder awardMessage = new StringBuilder(L("^HYou have completed the '@x1' @x2 achievement!^?\n\r",A.getDisplayStr(),A.getAgent().name().toLowerCase()));
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.ACHIEVEMENTS);
			if(!CMLib.flags().isCloaked(mob))
			{
				final PlayerStats pStats = mob.playerStats();
				final PlayerAccount account = (pStats != null) ? pStats.getAccount() : null;
				final String name = ((A.getAgent() == Agent.ACCOUNT) && (account != null)) ? account.getAccountName() : mob.name();
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has completed the '@x2' @x3 achievement!",name,A.getDisplayStr(),A.getAgent().name().toLowerCase()),true);
			}
			final Award[] awardSet = A.getRewards();
			mob.tell(awardMessage.toString());
			if(A.getAgent() == Agent.PLAYER)
			{
				giveAwards(mob,awardSet,flag);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public Achievement getAchievement(String tattoo)
	{
		for(Enumeration<Achievement> a = achievements(null); a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if(A.getTattoo().equalsIgnoreCase(tattoo))
				return A;
		}
		return null;
	}
	
	@Override
	public Achievement deleteAchievement(String tattoo)
	{
		final Achievement A=getAchievement(tattoo);
		if(A!=null)
		{
			switch(A.getAgent())
			{
			case ACCOUNT:
				accountAchievements.remove(A);
				break;
			case PLAYER:
				playerAchievements.remove(A);
				break;
			}
			final List<Achievement> list=eventMap.get(A.getEvent());
			if(list != null)
				list.remove(A);
		}
		return A;
	}
	
	@Override
	public String getAchievementsHelpFromMap(Map<String,Map<String,String>> helpMap, Event E, String parmName)
	{
		Map<String,String> entryMap;
		if(E==null)
			entryMap = helpMap.get("");
		else
			entryMap = helpMap.get(E.name());
		if((entryMap == null) && (E!=null))
			entryMap = helpMap.get("");
		if(entryMap == null)
		{
			for(Map<String,String> map : helpMap.values())
			{
				if(map.containsKey(parmName))
				{
					entryMap = map;
					break;
				}
			}
		}
		if(entryMap == null)
			return null;
		if(entryMap.containsKey(parmName))
			return entryMap.get(parmName);
		for(Map<String,String> map : helpMap.values())
		{
			if(map.containsKey(parmName))
				return map.get(parmName);
		}
		return null;
	}
	
	@Override
	public Map<String,Map<String,String>> getAchievementsHelpMap()
	{
		final Map<String,Map<String,String>> help = new TreeMap<String,Map<String,String>>();
		
		final String achievementFilename = getAchievementFilename();
		final List<String> V=Resources.getFileLineVector(Resources.getRawFileResource(achievementFilename,true));
		Resources.removeResource(achievementFilename);
		String eventName = "";
		String keyName = "";
		for(String s : V)
		{
			s=s.trim();
			if((s.length()==0)
			||((!s.startsWith("#"))&&(!s.startsWith(";"))))
				continue;
			s=s.substring(1);
			int x=s.indexOf("EVENT=\"");
			if(x>=0)
			{
				int y=s.indexOf("\"",x+7);
				if(y>x)
					eventName=s.substring(x+7,y);
				if(CMath.s_valueOf(Event.class, eventName)==null)
					eventName="";
				continue;
			}
			Map<String,String> parmMap=help.get(eventName);
			if(parmMap==null)
			{
				parmMap=new TreeMap<String,String>();
				help.put(eventName, parmMap);
			}
			String value;
			if(s.trim().startsWith("["))
			{
				int start=s.indexOf('[');
				x=s.indexOf(']',start+1);
				if(x>0)
				{
					keyName = s.substring(start+1,x);
					value = s.substring(x+1); 
				}
				else
					value="";
			}
			else
				value = s;
			if((keyName.length()>0)&&(value.length()>0))
			{
				String oldS=parmMap.containsKey(keyName) ? parmMap.get(keyName) : "";
				value = oldS + "\n\r" + value;
				parmMap.put(keyName, value);
			}
			
		}
		return help;
	}

	public String buildRow(final Event E, Map<String,String> parmTree)
	{
		StringBuilder str=new StringBuilder(parmTree.get("TATTOO")+"=");
		for(final String parmName : E.getParameters())
		{
			String value = parmTree.get(parmName);
			if((value != null) && (value.trim().length()>0))
			{
				str.append(parmName+"=");
				if(CMath.isMathExpression(value))
					str.append(value).append(" ");
				else
					str.append("\"").append(CMStrings.escape(value)).append("\" ");
			}
		}
		return str.toString();
	}

	@Override
	public String getAwardString(final Award[] awards)
	{
		final StringBuilder awardStr=new StringBuilder();
		for(Award award : awards)
		{
			switch(award.getType())
			{
			case ABILITY:
			{
				final AbilityMapping map = ((AbilityAward)award).getAbilityMapping();
				awardStr.append(" ").append(map.qualLevel());
				final StringBuilder parms=new StringBuilder("");
				if(!map.autoGain())
					parms.append(" AUTOGAIN=FALSE");
				if((map.extraMask().length()>0)&&(map.extraMask().length()>0))
					parms.append(" MASK="+CMStrings.escape("\""+CMStrings.escape(map.extraMask()))+"\"");
				if((map.defaultParm().length()>0)&&(map.defaultParm().length()>0))
					parms.append(" PARMS="+CMStrings.escape("\""+CMStrings.escape(map.defaultParm()))+"\"");
				if((map.originalSkillPreReqList()!=null)&&(map.originalSkillPreReqList().length()>0))
					parms.append(" PREREQS="+CMStrings.escape("\""+CMStrings.escape(map.originalSkillPreReqList()))+"\"");
				if(parms.toString().trim().length()>0)
				{
					awardStr.append(" \"")
							.append(map.abilityID())
							.append("(").append(parms).append(")")
							.append("\"");
				}
				else
					awardStr.append(" ").append(map.abilityID());
				break;
			}
			case CURRENCY:
				awardStr.append(" ").append(((CurrencyAward)award).getAmount())
						.append(" ").append(((CurrencyAward)award).getCurrency());
				break;
			case STAT:
				awardStr.append(" ").append(((StatAward)award).getAmount())
						.append(" ").append(((StatAward)award).getStat());
				break;
			case EXPERTISE:
				awardStr.append(" ").append(((ExpertiseAward)award).getLevel())
						.append(" ").append(((ExpertiseAward)award).getExpertise().ID());
				break;
			case QP:
				awardStr.append(" ").append(((AmountAward)award).getAmount())
						.append(" ").append("QP");
				break;
			case TITLE:
				break;
			case XP:
				awardStr.append(" ").append(((AmountAward)award).getAmount())
						.append(" ").append("XP");
				break;
			default:
				break;
			}
		}
		return awardStr.toString();
	}
	
	private void fillAchievementParmTree(final Map<String,String> parmTree, final Achievement A)
	{
		parmTree.put("TATTOO",A.getTattoo());
		parmTree.put("EVENT", A.getEvent().name());
		parmTree.put("DISPLAY", A.getDisplayStr());
		parmTree.put("TITLE", "");
		for(Award award : A.getRewards())
		{
			if(award.getType() == AwardType.TITLE)
				parmTree.put("TITLE", ((TitleAward)award).getTitle());
		}
		String awardStr=getAwardString(A.getRewards());
		parmTree.put("REWARDS", awardStr);
		for(String s : A.getEvent().getParameters())
		{
			if(!CMParms.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, s))
				parmTree.put(s, CMStrings.deEscape(A.getRawParmVal(s)));
		}
	}
	
	@Override
	public boolean addModifyAchievement(final MOB mob, final Agent agent, final String tattoo, Achievement A)
	{
		if(mob.isMonster())
			return false;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		final TreeMap<String,String> parmTree=new TreeMap<String,String>();
		for(final Event E : Event.values())
		{
			for(String s : E.getParameters())
				parmTree.put(s,"");
		}
		final Map<String,Map<String,String>> helpMap = getAchievementsHelpMap();
		Event E=Event.KILLS;
		parmTree.put("TATTOO",tattoo.toUpperCase().trim());
		if(A!=null)
		{
			E=A.getEvent();
			fillAchievementParmTree(parmTree,A);
		}
		parmTree.put("EVENT",E.name());
		try 
		{
			while((mob.session()!=null)&&(!mob.session().isStopped())&&(!ok))
			{
				int showNumber=0;
				String help;
				
				help=getAchievementsHelpFromMap(helpMap,null,"EVENT");
				parmTree.put("EVENT",CMLib.genEd().prompt(mob, parmTree.get("EVENT"), ++showNumber, showFlag, L("Event Type"), false, false, help, CMEVAL_INSTANCE, Event.getEventChoices()));
				E = (Event)CMath.s_valueOf(Event.class, parmTree.get("EVENT"));
				
				help=getAchievementsHelpFromMap(helpMap,null,"DISPLAY");
				parmTree.put("DISPLAY",CMLib.genEd().prompt(mob, parmTree.get("DISPLAY"), ++showNumber, showFlag, L("Display Desc"), false, false, help, null, null));
				
				help=getAchievementsHelpFromMap(helpMap,null,"TITLE");
				parmTree.put("TITLE",CMLib.genEd().prompt(mob, parmTree.get("TITLE"), ++showNumber, showFlag, L("Title Award"), true, false, help, null, null));
				
				help=getAchievementsHelpFromMap(helpMap,null,"REWARDS");
				parmTree.put("REWARDS",CMLib.genEd().prompt(mob, parmTree.get("REWARDS"), ++showNumber, showFlag, L("Rewards List"), true, false, help, null, null));
				
				for(final String parmName : E.getParameters())
				{
					if(!CMStrings.contains(BASE_ACHIEVEMENT_PARAMETERS, parmName))
					{
						help=getAchievementsHelpFromMap(helpMap,E,parmName);
						parmTree.put(parmName,CMLib.genEd().prompt(mob, parmTree.get(parmName), ++showNumber, showFlag, CMStrings.capitalizeAndLower(parmName), false, false, help, null, null));
					}
				}
				
				for(final String parmName : parmTree.keySet())
				{
					if((!parmName.equals("TATTOO"))&&(!CMStrings.contains(E.getParameters(), parmName)))
						parmTree.put(parmName, "");
				}
				
				String achievementRow = buildRow(E,parmTree);
				String err = evaluateAchievement(agent, achievementRow, false);
				if((err != null)&&(err.trim().length()>0)&&(mob.session()!=null))
				{
					mob.session().println(L("^HERRORS: ^r@x1^N",err));
				}
				else
				if(showFlag<-900)
				{ 
					ok=true; 
					break;
				}
				if(showFlag>0)
				{ 
					showFlag=-1; 
					continue;
				}
				final String promptStr=mob.session().prompt(L("Edit which (or CANCEL)? "),"");
				showFlag=CMath.s_int(promptStr);
				if(showFlag<=0)
				{
					if(promptStr.equalsIgnoreCase("CANCEL"))
					{
						mob.tell(L("Canceled."));
						return false;
					}
					else
					if((err != null)&&(err.trim().length()>0)&&(mob.session()!=null))
					{
						mob.session().println(L("^HCorrect errors first or enter CANCEL: ^r@x1^N",err));
						showFlag=-1;
						continue;
					}
					else
					{
						showFlag=-1;
						ok=true;
					}
				}
			}
			if(A!=null)
			{
				this.deleteAchievement(A.getTattoo());
			}
			this.evaluateAchievement(agent, buildRow(E,parmTree), true);
			for(Enumeration<MOB> m = CMLib.players().players();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(M.playerStats()!=null)
				{
					M.playerStats().rebuildAchievementTracker(M, parmTree.get("TATTOO"));
				}
			}
			this.resaveAchievements(parmTree.get("TATTOO"));
			return true;
		} 
		catch (IOException e) 
		{
			return false;
		}
	}

	private void finishSaveAchievementsSection(final Agent agent, final Set<String> added, final String EOL, final StringBuffer newFileData)
	{
		for(final Enumeration<Achievement> a=achievements(agent);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if(!added.contains(A.getTattoo().toUpperCase().trim()))
			{
				final Map<String,String> parmTree = new TreeMap<String,String>();
				fillAchievementParmTree(parmTree,A);
				newFileData.append(buildRow(A.getEvent(),parmTree)).append(EOL);
			}
		}
		added.clear(); // reset for the next section
	}
	
	@SuppressWarnings("unchecked")

	@Override
	public void resaveAchievements(final String modifyTattoo)
	{
		final String loadAchievementFilename = getAchievementFilename();
		final StringBuffer buf = Resources.getRawFileResource(loadAchievementFilename,true);
		Resources.removeResource(loadAchievementFilename);
		final List<String> V=Resources.getFileLineVector(buf);
		final StringBuffer newFileData = new StringBuffer("");
		final Map<String,Achievement>[] maps=new Map[Agent.values().length];
		for(int i=0;i<Agent.values().length;i++)
			maps[i]=new TreeMap<String,Achievement>();
		for(Agent agent : Agent.values())
		{
			for(final Enumeration<Achievement> a=achievements(agent);a.hasMoreElements();)
			{
				final Achievement A=a.nextElement();
				if(A.getAgent() == agent)
					maps[agent.ordinal()].put(A.getTattoo().toUpperCase().trim(), A);
			}
		}
		final String EOL= Resources.getEOLineMarker(buf);
		final Set<String> added = new HashSet<String>();
		Agent currentAgent = Agent.PLAYER;
		for(String row : V)
		{
			if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
				newFileData.append(row).append(EOL);
			else
			{
				int x=row.indexOf('=');
				if(x<=0)
				{
					if(row.trim().startsWith("["))
					{
						Agent oldAgent = currentAgent;
						for(Agent ag : Agent.values())
						{
							if(row.trim().toUpperCase().equals("["+ag.name()+"]"))
							{
								currentAgent = ag;
								break;
							}
						}
						if(oldAgent != currentAgent)
						{
							finishSaveAchievementsSection(oldAgent, added, EOL, newFileData);
						}
					}
					newFileData.append(row).append(EOL);
				}
				else
				{
					final String tatt = row.substring(0,x).trim();
					if(maps[currentAgent.ordinal()].containsKey(tatt.toUpperCase().trim()))
					{
						final Achievement A=maps[currentAgent.ordinal()].get(tatt);
						if((modifyTattoo != null)&&(modifyTattoo.equalsIgnoreCase(tatt)))
						{
							final Map<String,String> parmTree = new TreeMap<String,String>();
							fillAchievementParmTree(parmTree,A);
							newFileData.append(buildRow(A.getEvent(),parmTree)).append(EOL);
						}
						else
							newFileData.append(row).append(EOL);
						added.add(tatt.toUpperCase().trim());
					}
				}
			}
		}
		finishSaveAchievementsSection(currentAgent, added, EOL, newFileData);
		Resources.updateFileResource(achievementFilename, newFileData);
		Resources.removeResource(achievementFilename);
	}
	
	private boolean evaluatePlayerAchievement(final Achievement A, final PlayerStats pStats, final MOB mob)
	{
		if(mob.findTattoo(A.getTattoo())==null)
		{
			final Tracker T=pStats.getAchievementTracker(A, mob);
			if(T.isAchieved(mob))
			{
				return giveAwards(A, mob, mob,AchievementLoadFlag.NORMAL);
			}
		}
		return false;
	}
	
	private boolean evaluateAccountAchievement(final Achievement A, final PlayerAccount account, final MOB mob)
	{
		if(account != null)
		{
			if(account.findTattoo(A.getTattoo())==null)
			{
				final Tracker T=account.getAchievementTracker(A, mob);
				if(T.isAchieved(mob))
				{
					return giveAwards(A, account, mob,AchievementLoadFlag.NORMAL);
				}
			}
		}
		return false;
	}
	
	private boolean evaluateAchievement(final Achievement A, final PlayerStats pStats, final PlayerAccount aStats, final MOB mob)
	{
		switch(A.getAgent())
		{
		case PLAYER:
			return evaluatePlayerAchievement(A,pStats,mob);
		case ACCOUNT:
			return evaluateAccountAchievement(A,aStats,mob);
		}
		return false;
	}
	
	@Override
	public boolean evaluateAchievements(final MOB mob)
	{
		if((mob==null)||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			return false;
		final PlayerStats P=mob.playerStats();
		if(P==null)
			return false;
		final PlayerAccount C=P.getAccount() != null ? P.getAccount() : null;
		
		boolean somethingDone = false;
		for(Enumeration<Achievement> a=achievements(null);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if(evaluateAchievement(A,P,C,mob))
				somethingDone = true;
		}
		return somethingDone;
	}
	
	private String getAchievementFilename()
	{
		CMFile F = new CMFile(Resources.makeFileResourceName(achievementFilename),null);
		if(F.exists() && (F.canRead()))
			return achievementFilename;
		final String oldFilename = achievementFilename.substring(0,achievementFilename.length()-4)+".txt"; 
		F = new CMFile(Resources.makeFileResourceName(oldFilename),null);
		if(F.exists() && (F.canRead()))
			return oldFilename;
		return achievementFilename;
	}

	@Override
	public synchronized void reloadAchievements()
	{
		accountAchievements=new SLinkedList<Achievement>();
		playerAchievements=new SLinkedList<Achievement>();
		eventMap=new TreeMap<Event,List<Achievement>>();
		final String achievementFilename = getAchievementFilename();
		final List<String> V=Resources.getFileLineVector(Resources.getRawFileResource(achievementFilename,true));
		Resources.removeResource(achievementFilename);
		V.addAll(Resources.getFileLineVector(Resources.getRawFileResource(achievementFilename+".2",false)));
		Resources.removeResource(achievementFilename+".2");
		String WKID=null;
		Agent agent = Agent.PLAYER;
		for(int v=0;v<V.size();v++)
		{
			final String row=V.get(v);
			if(row.trim().startsWith("["))
			{
				final String upTrimRow = row.trim().toUpperCase();
				boolean found=false;
				for(Agent ag : Agent.values())
				{
					if(upTrimRow.equals("["+ag.name()+"]"))
					{
						agent=ag;
						found=true;
						break;
					}
				}
				if(!found)
					Log.errOut("Achievements","Unknown section name in "+achievementFilename+": "+row);
				continue;
			}
			WKID=evaluateAchievement(agent,row,true);
			if(WKID==null)
				continue;
			if(WKID.startsWith("Error: "))
				Log.errOut("Achievements",WKID);
		}
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if(M.playerStats()!=null)
			{
				if((evaluateAchievements(M))&&(!CMLib.flags().isInTheGame(M,true)))
					CMLib.database().DBUpdatePlayerPlayerStats(M);
			}
		}
	}

	@Override
	public void grantAbilitiesAndExpertises(MOB mob)
	{
		final PlayerStats pStats = (mob == null) ? null : mob.playerStats();
		if((pStats != null) && (mob!=null))
		{
			for(final AbilityMapper.AbilityMapping map : pStats.getExtraQualifiedSkills().values())
			{
				if(map.autoGain() && (mob.fetchAbility(map.abilityID()) == null))
				{
					final Ability A=CMClass.getAbility(map.abilityID());
					if((A!=null)
					&&(map.qualLevel()<=mob.basePhyStats().level())
					&&((map.extraMask().length()==0)||(CMLib.masking().maskCheck(map.extraMask(),mob,true))))
					{
						A.setSavable(true);
						A.setProficiency(map.defaultProficiency());
						A.setMiscText(map.defaultParm());
						mob.addAbility(A);
						A.autoInvocation(mob, false);
					}
				}
			}
			for(final ExpertiseDefinition def : pStats.getExtraQualifiedExpertises().values())
			{
				if((def.costDescription().length()==0)
				&&((def.compiledListMask()==null)||CMLib.masking().maskCheck(def.compiledListMask(), mob, true))
				&&((def.compiledFinalMask()==null)||CMLib.masking().maskCheck(def.compiledFinalMask(), mob, true))
				&&(mob.fetchExpertise(def.ID())==null))
				{
					mob.addExpertise(def.ID());
				}
			}
		}
	}
	
	@Override
	public void loadPlayerSkillAwards(Tattooable mob, PlayerStats stats)
	{
		if((mob != null) && (stats != null))
		{
			for(final Enumeration<Tattoo> t = mob.tattoos();t.hasMoreElements();)
			{
				final Achievement A=getAchievement(t.nextElement().getTattooName());
				if(A != null)
				{
					for(final Award award : A.getRewards())
					{
						if(award.getType() == AwardType.ABILITY)
						{
							final AbilityAward aaward = (AbilityAward)award;
							if(!stats.getExtraQualifiedSkills().containsKey(aaward.getAbilityMapping().abilityID()))
							{
								final Ability abilityCheck=CMClass.getAbility(aaward.getAbilityMapping().abilityID());
								if(abilityCheck!=null)
									stats.getExtraQualifiedSkills().put(abilityCheck.ID(), aaward.getAbilityMapping());
							}
						}
						else
						if(award.getType() == AwardType.EXPERTISE)
						{
							final ExpertiseAward aaward = (ExpertiseAward)award;
							if(!stats.getExtraQualifiedExpertises().containsKey(aaward.getExpertise().ID()))
								stats.getExtraQualifiedExpertises().put(aaward.getExpertise().ID(), aaward.getExpertise());
						}
					}
				}
			}
		}
	}

	@Override
	public void reloadPlayerAwards(MOB mob, AchievementLoadFlag flag)
	{
		if(mob != null)
		{
			final PlayerStats pStats = mob.playerStats();
			boolean somethingDone=false;
			for(final Enumeration<Tattoo> t = mob.tattoos();t.hasMoreElements();)
			{
				final Achievement A=getAchievement(t.nextElement().getTattooName());
				if((A != null)&&(A.getAgent()==Agent.PLAYER))
				{
					giveAwards(mob, A.getRewards(), flag);
					somethingDone=true;
				}
			}
			if(somethingDone)
			{
				loadPlayerSkillAwards(mob, pStats);
				grantAbilitiesAndExpertises(mob);
			}
		}
	}
	
	@Override
	public void loadAccountAchievements(final MOB mob, final AchievementLoadFlag flag)
	{
		final PlayerStats pStats = (mob==null) ? null : mob.playerStats();
		final PlayerAccount account = (pStats == null) ? null : pStats.getAccount();
		if((mob!=null) && (account != null))
		{
			boolean somethingDone = false;
			for(Enumeration<Tattoo> t=account.tattoos();t.hasMoreElements();)
			{
				final Tattoo T = t.nextElement();
				final Achievement A=getAchievement(T.getTattooName());
				if(A != null)
				{
					if(mob.findTattoo(T.getTattooName())==null)
					{
						if((flag != AchievementLoadFlag.CHARCR_PRELOAD)
						&&(flag != AchievementLoadFlag.REMORT_PRELOAD))
						{
							mob.addTattoo(A.getTattoo());
							somethingDone=true;
						}
						giveAwards(mob, A.getRewards(), flag);
					}
				}
			}
			if(somethingDone)
			{
				loadPlayerSkillAwards(mob, pStats);
				grantAbilitiesAndExpertises(mob);
			}
		}
	}

	protected String makeAchievmentHelp(Achievement A)
	{
		if(A == null)
			return null;
		@SuppressWarnings("unchecked")
		Map<String,String> helpMap = (Map<String,String>)Resources.getResource("SYSTEM_ACHIEVEMENT_HELP");
		if(helpMap == null)
		{
			helpMap = new Hashtable<String,String>();
			Resources.submitResource("SYSTEM_ACHIEVEMENT_HELP", helpMap);
		}
		String help = helpMap.get(A.getTattoo());
		if(help != null)
			return help;
		StringBuilder str = new StringBuilder("");
		int cols = 20;
		if(A.getAgent() == Agent.ACCOUNT)
			str.append("\n\r").append(CMStrings.padRight(L("Account Achievement:"),cols)).append(" ").append(A.getTattoo());
		else
			str.append("\n\r").append(CMStrings.padRight(L("Char. Achievement:"),cols)).append(" ").append(A.getTattoo());
		str.append("\n\r").append(CMStrings.padRight(L("Description:"),cols)).append(" ").append(A.getDisplayStr());
		str.append("\n\r").append(CMStrings.padRight(L("Achievement Type:"),cols)).append(" ");
		str.append(L(A.getEvent().displayName()));
		if(A.getRewards().length>0)
		{
			if(A.getAgent() == Agent.ACCOUNT)
				str.append("\n\r").append(CMStrings.padRight(L("Rewards Granted:"),cols)).append(" ").append(L("New Characters, Children, and Remorted"));
			else
				str.append("\n\r").append(CMStrings.padRight(L("Rewards Granted:"),cols)).append(" ").append(L("Immediately"));
		}
		for(Award W : A.getRewards())
			str.append("\n\r").append(CMStrings.padRight(L("Award:"),cols)).append(" ").append(W.getDescription());
		str.append("\n\r");
		helpMap.put(A.getTattoo(), str.toString());
		return str.toString();
	}
	
	@Override
	public String getAchievementsHelp(String ID, boolean exact)
	{
		if(ID==null)
			return null;
		ID = ID.replace('`','\'');
		Achievement A=this.getAchievement(ID.toUpperCase());
		if(A == null)
		{
			for(final Enumeration<Achievement> a=achievements(null); a.hasMoreElements();)
			{
				final Achievement A2 = a.nextElement();
				if(A2.getDisplayStr().equalsIgnoreCase(ID))
				{
					A=A2;
					break;
				}
			}
		}
		if(exact)
			return makeAchievmentHelp(A);
		if(A == null)
		{
			for(final Enumeration<Achievement> a=achievements(null); a.hasMoreElements();)
			{
				final Achievement A2 = a.nextElement();
				if(A2.getTattoo().toUpperCase().startsWith(ID.toUpperCase()))
				{
					A=A2;
					break;
				}
			}
		}
		if(A == null)
		{
			for(final Enumeration<Achievement> a=achievements(null); a.hasMoreElements();)
			{
				final Achievement A2 = a.nextElement();
				if(A2.getDisplayStr().toLowerCase().startsWith(ID.toLowerCase()))
				{
					A=A2;
					break;
				}
			}
		}
		return makeAchievmentHelp(A);
	}
	
	@Override
	public boolean shutdown()
	{
		Resources.removeResource("SYSTEM_ACHIEVEMENT_HELP");
		accountAchievements=null;
		playerAchievements=null;
		eventMap=null;
		return super.shutdown();
	}
}
