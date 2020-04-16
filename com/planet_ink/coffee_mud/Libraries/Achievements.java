package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.FullMemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2015-2020 Bo Zimmerman

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
	private List<Achievement> 			clanAchievements	= null;
	private Map<Event,List<Achievement>>eventMap			= null;

	private final static String achievementFilename  = "achievements.ini";

	private static CMEval CMEVAL_INSTANCE = new CMEval()
	{
		@Override
		public Object eval(final Object val, final Object[] choices, final boolean emptyOK) throws CMException
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
	public String evaluateAchievement(final Agent agent, final String row, final boolean addIfPossible)
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
		final String eventStr=CMParms.getParmStr(params, "EVENT", "");
		final Event eventType = (Event)CMath.s_valueOf(Event.class, eventStr.toUpperCase().trim());
		if(eventType == null)
			return "Error: Blank or unknown achievement type: "+eventStr+"!";
		final String displayStr=CMStrings.deEscape(CMParms.getParmStr(params, "DISPLAY", ""));
		final String titleStr=CMStrings.deEscape(CMParms.getParmStr(params, "TITLE", ""));
		final String rewardStr=CMStrings.deEscape(CMParms.getParmStr(params, "REWARDS", ""));
		final String[] awardSet = CMParms.parse(rewardStr).toArray(new String[0]);
		final List<Award> awardsList = new ArrayList<Award>();
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
					if(thing.equals("CLANXP") || thing.startsWith("CLANEXPERIEN") || thing.equals("CLANEXP"))
					{
						awardsList.add(new AmountAward()
						{
							@Override
							public AwardType getType()
							{
								return AwardType.CLANXP;
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
								return L("@x1 Clan Experience Points",""+getAmount());
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
					if(thing.equals("NOPURGE"))
					{
						awardsList.add(new AmountAward()
						{
							@Override
							public AwardType getType()
							{
								return AwardType.NOPURGE;
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
								return L("Protection from auto-purge");
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
						final int y=thing.indexOf('(');
						String parms="";
						if((y>0) && thing.endsWith(")"))
						{
							parms=thing.substring(y+1,thing.length()-1);
							thing=thing.substring(0,y);
						}
						final Ability A=CMClass.getAbility(thing);
						if(A!=null)
						{
							final String ableParms = CMStrings.deEscape(CMParms.getParmStr(parms, "PARMS", ""));
							final String mask = CMStrings.deEscape(CMParms.getParmStr(parms, "MASK", ""));
							final String preReqs = CMStrings.deEscape(CMParms.getParmStr(parms, "PREREQS", ""));
							final boolean autoGain = CMParms.getParmBool(parms, "AUTOGAIN", true);
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
							final String finalParms = parms;
							final ExpertiseDefinition oldDef=CMLib.expertises().findDefinition(thing, true);
							final ExpertiseDefinition def = new ExpertiseDefinition()
							{
								final String ID = oldDef.ID();
								final boolean isAutoGained = CMParms.getParmBool(finalParms, "AUTOGAIN", true);

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
								public int compareTo(final CMObject o)
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
								public void setBaseName(final String baseName)
								{
								}

								@Override
								public void setName(final String name)
								{
								}

								@Override
								public void setID(final String ID)
								{
								}

								@Override
								public void setData(final String[] data)
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
								public void addListMask(final String mask)
								{
								}

								@Override
								public void addFinalMask(final String mask)
								{
								}

								@Override
								public void addCost(final CostType type, final Double value)
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
								public boolean meetsCostRequirements(final MOB mob)
								{
									if(isAutoGained)
										return true;
									final ExpertiseDefinition curDef = baseDef();
									return (curDef == null) ? false : curDef.meetsCostRequirements(mob);
								}

								@Override
								public void spendCostRequirements(final MOB mob)
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
							final String astat=CMLib.coffeeMaker().getFinalStatName(stat1);
							boolean isSave = false;
							for(final int t : CharStats.CODES.SAVING_THROWS())
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
							final String astat=CMLib.coffeeMaker().getFinalStatName(stat1);
							boolean isSave = false;
							for(final int t : CharStats.CODES.SAVING_THROWS())
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
							thing=thing.trim();
							final boolean clan=thing.toLowerCase().startsWith("clan ");
							if(clan)
								thing=thing.substring(5);
							final String currency = CMLib.english().matchAnyCurrencySet(thing);
							if(currency == null)
								Log.debugOut("Achievement", "Unknown award type: "+thing);
							else
							{
								final double denomination = CMLib.english().matchAnyDenomination(currency, thing);
								if(denomination == 0.0)
									Log.debugOut("Achievement", "Unknown award type: "+thing);
								else
								{
									final String currencyName = thing;
									awardsList.add(new CurrencyAward()
									{
										final boolean isClanCurrency = clan;

										@Override
										public AwardType getType()
										{
											return isClanCurrency?AwardType.CLANCURRENCY:AwardType.CURRENCY;
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canApplyTo(final Agent agent)
				{
					return true;
				}

				@Override
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							if(parms.length>0)
							{
								final MOB killed = (MOB)parms[0];
								if((killed != null)
								&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, killed, true)))
								&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							if((tracked instanceof MOB)
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, (MOB)tracked, true))))
								return true;
							return false;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (abelo > 0) ? (getCount(tracked) > value) : (getCount(tracked) < value);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
								return CMath.s_int(CMLib.coffeeMaker().getAnyGenStat((MOB)tracked, statName));
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final MOB mob = CMClass.getFactoryMOB();
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								if(((MOB)tracked).fetchFaction(factionID)==Integer.MAX_VALUE)
									return false;
								return (abelo > 0) ? (getCount(tracked) > value) : (getCount(tracked) < value);
							}
							return false;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								final int f=((MOB)tracked).fetchFaction(factionID);
								if(f == Integer.MAX_VALUE)
									return 0;
								return f;
							}
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= number;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								int num=0;
								for(final Faction F : factions)
								{
									final int f = ((MOB)tracked).fetchFaction(F.factionID());
									if((f!=Integer.MAX_VALUE)
									&&((abelo > 0) ? (f > value) : (f < value)))
										num++;
								}
								return num;
							}
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
					for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= pct;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								final PlayerStats pstats=((MOB)tracked).playerStats();
								if(pstats != null)
								{
									if(areaID.equals("WORLD"))
									{
										final Room R=((MOB)tracked).location();
										if((R!=null)&&(CMLib.map().getExtendedRoomID(CMLib.map().getRoom(R)).length()>0))
											return pstats.percentVisited((MOB)tracked,null);
										else
											return 0;
									}
									else
									{
										final Area A=CMLib.map().getArea(areaID);
										if(A!=null)
										{
											return pstats.percentVisited((MOB)tracked, A);
										}
									}
								}
							}
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "PERCENT", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid PERCENT parameter: "+numStr+"!";
					this.pct=CMath.s_int(numStr);
					final String areaID=CMParms.getParmStr(parms, "AREA", "").toUpperCase().trim();
					if(areaID.length()==0)
						return "Error: Missing AREA parameter: "+areaID+"!";
					if((CMLib.map().getArea(areaID)==null)
					&&(!areaID.equals("WORLD"))
					&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= num;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
						final String abilityID = strList[i].trim();
						if(abilityID.equals("*"))
						{
							this.abilityIDs.add(abilityID);
							break;
						}
						else
						{
							final Ability A;
							A=CMClass.getAbility(abilityID);
							if((A==null)
							||((A.classificationCode() & Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL)
							||(((A.classificationCode() & Ability.ALL_DOMAINS)!=Ability.DOMAIN_BUILDINGSKILL)
								&&((A.classificationCode() & Ability.ALL_DOMAINS)!=Ability.DOMAIN_EPICUREAN)
								&&((A.classificationCode() & Ability.ALL_DOMAINS)!=Ability.DOMAIN_CRAFTINGSKILL)))
							{
								if(CMParms.contains(Ability.ACODE_DESCS,abilityID)
								&&(CMParms.indexOfIgnoreCase(Ability.ACODE_DESCS, abilityID)==Ability.ACODE_COMMON_SKILL))
								{
									this.abilityIDs.add(abilityID);
								}
								else
								if(CMParms.contains(Ability.DOMAIN_DESCS,abilityID)
								&&((CMParms.indexOfIgnoreCase(Ability.DOMAIN_DESCS, abilityID)==(Ability.DOMAIN_BUILDINGSKILL>>5))
									||(CMParms.indexOfIgnoreCase(Ability.DOMAIN_DESCS, abilityID)==(Ability.DOMAIN_EPICUREAN>>5))
									||(CMParms.indexOfIgnoreCase(Ability.DOMAIN_DESCS, abilityID)==(Ability.DOMAIN_CRAFTINGSKILL>>5))))
								{
									this.abilityIDs.add(abilityID);
								}
								else
									return "Error: Unknown crafting ABILITYID: "+abilityID+"!";
							}
							else
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= num;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
						final String abilityID = strList[i].trim();
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
								{
									if((!CMSecurity.isDisabled(DisFlag.LANGUAGES))
									||(!CMClass.isLanguage(abilityID)))
										return "Error: Unknown ABILITYID: "+abilityID+"!";
									else
										return "";
								}
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
		case SOCIALUSE:
			A=new Achievement()
			{
				private int 				num 		= 0;
				private final Set<String>	socialIDs 	= new TreeSet<String>();
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= num;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							final Social S;
							if(parms.length>0)
							{
								if(parms[0] instanceof String)
									S=CMLib.socials().fetchSocial((String)parms[0], true);
								else
								if(parms[0] instanceof Social)
									S=(Social)parms[0];
								else
									S=null;
								if((S!=null)
								&&(socialIDs.contains("*")
									||socialIDs.contains(S.Name())
									||(socialIDs.contains(S.baseName()))
									||(socialIDs.contains(S.tailName()))))
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					this.num=CMath.s_int(numStr);
					final String abilityIDs=CMParms.getParmStr(parms, "SOCIALID", "").toUpperCase().trim();
					if(abilityIDs.length()==0)
						return "Error: Missing SOCIALID parameter: "+abilityIDs+"!";
					final String[] strList=abilityIDs.split(",");
					this.socialIDs.clear();
					final Set<String> tails=new TreeSet<String>();
					final Set<String> heads=new TreeSet<String>();
					for(final Enumeration<Social> s= CMLib.socials().getAllSocials();s.hasMoreElements();)
					{
						final Social S=s.nextElement();
						if(!tails.contains(S.tailName()))
							tails.add(S.tailName());
						if(!heads.contains(S.baseName()))
							heads.add(S.baseName());
					}
					for(int i=0;i<strList.length;i++)
					{
						final String socialID = strList[i].trim();
						if(socialID.equals("*"))
						{
							this.socialIDs.add(socialID);
							break;
						}
						else
						{
							final Social S=CMLib.socials().fetchSocial(socialID, false);
							if(S!=null)
								this.socialIDs.add(S.Name());
							else
							if(tails.contains(socialID))
								this.socialIDs.add(socialID);
							else
							if(heads.contains(socialID))
								this.socialIDs.add(socialID);
							else
								return "Error: Unknown SOCIALID: "+socialID+"! Check case.";
						}
					}
					if(this.socialIDs.size()==0)
						return "Error: Unknown crafting SOCIALIDs: "+socialIDs+"! Check case.";
					return "";
				}
			};
			break;
		case CMDUSE:
			A=new Achievement()
			{
				private int 				num 		= 0;
				private final Set<String>	commandIDs 	= new TreeSet<String>();
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= num;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							final Command C;
							if(parms.length>0)
							{
								if(parms[0] instanceof String)
									C=CMClass.getCommand((String)parms[0]);
								else
								if(parms[0] instanceof Command)
									C=(Command)parms[0];
								else
									C=null;
								if((C!=null)
								&&(commandIDs.contains("*")
									||(commandIDs.contains(C.ID()))))
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					this.num=CMath.s_int(numStr);
					final String abilityIDs=CMParms.getParmStr(parms, "COMMANDID", "").toUpperCase().trim();
					if(abilityIDs.length()==0)
						return "Error: Missing COMMANDID parameter: "+abilityIDs+"!";
					final String[] strList=abilityIDs.split(",");
					this.commandIDs.clear();
					for(int i=0;i<strList.length;i++)
					{
						final String commandID = strList[i].trim();
						if(commandID.equals("*"))
						{
							this.commandIDs.add(commandID);
							break;
						}
						else
						{
							final Command C=CMClass.getCommand(commandID);
							if(C!=null)
								this.commandIDs.add(C.ID());
							else
								return "Error: Unknown COMMANDID: "+commandID+"! Check case.";
						}
					}
					if(this.commandIDs.size()==0)
						return "Error: Unknown crafting SOCIALIDs: "+commandIDs+"! Check case.";
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && getCount(tracked) >= num;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
										final Quest Q=(Quest)parms[0];
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
							final java.util.regex.Pattern P = java.util.regex.Pattern.compile(questMask);
							if(P!=null)
								questPattern = P;
						}
						catch(final Exception e)
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= achievementList.size();
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							int count = 0;
							Tattooable other;
							if((CMProps.isUsingAccountSystem())
							&&(tracked instanceof MOB)
							&&(((MOB)tracked).playerStats()!=null))
								other=((MOB)tracked).playerStats().getAccount();
							else
								other=null;
							for(final String s : achievementList)
							{
								if(tracked.findTattoo(s)!=null)
									count++;
								else
								if((other!=null)&&(other.findTattoo(s)!=null))
									count++;
							}
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String list=CMStrings.deEscape(CMParms.getParmStr(parms, "ACHIEVEMENTLIST", ""));
					if(list.trim().length()==0)
						return "Error: Missing or invalid ACHIEVEMENTLIST parameter: "+list+"!";
					final String[] listArray = list.toUpperCase().trim().split(",");
					achievementList.clear();
					for(final String s : listArray)
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= roomIDs.size();
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								int count = 0;
								for(final String s : roomIDs)
								{
									if(((MOB)tracked).playerStats().hasVisited(CMLib.map().getRoom(s)))
										count++;
								}
								return count;
							}
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
							final Room R=CMLib.map().getRoom(s);
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) > num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								final int classLevel = ((MOB)tracked).charStats().getClassLevel(charClass);
								if(classLevel < 0)
									return 0;
								return classLevel + 1;
							}
							else
								return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							if(mob == null)
								return false;
							if(((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							&&(mob.charStats().getCurrentClass() == charClass))
							{
								count+=bumpNum;
								if(count<0)
									count++;
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
							catch(final Exception e)
							{
								return this;
							}
						}
					};
				}

				@Override
				public boolean isSavableTracker()
				{
					return agent == Agent.CLAN;
				}

				@Override
				public String parseParms(final String parms)
				{
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String charClassID=CMStrings.deEscape(CMParms.getParmStr(parms, "CLASS", ""));
					this.charClass = CMClass.getCharClass(charClassID);
					if(this.charClass == null)
						this.charClass = CMClass.findCharClass(charClassID);
					if(this.charClass == null)
						return "Error: Missing or invalid CLASS parameter: "+charClassID+"!";
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
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
				private MaskingLibrary.CompiledZMask seenMask = null;
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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT) || (agent == Agent.PLAYER);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof MOB)
							{
								if((playerMask==null)||(CMLib.masking().maskCheck(playerMask, (MOB)tracked, true)))
								{
									int count = 0;
									final boolean noMask=(itemMask==null);
									for(final Enumeration<Item> i=((MOB)tracked).items();i.hasMoreElements();)
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
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(numStr.trim().length()==0)
						num=1;
					else
					if(!CMath.isInteger(numStr))
						return "Error: Invalid NUM parameter!";
					else
						num=CMath.s_int(numStr);
					final String itemZapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "ITEMMASK", ""));
					if(itemZapperMask.trim().length()>0)
						this.itemMask = CMLib.masking().getPreCompiledMask(itemZapperMask);
					else
						return "Error: Missing or invalid ITEMMASK parameter!";
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
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
				private MaskingLibrary.CompiledZMask seenMask = null;
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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (getCount(tracked) >= seconds);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "SECONDS", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid SECONDS parameter: "+numStr+"!";
					seconds=CMath.s_int(numStr);
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return getCount(tracked) >= 1;
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
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
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
		case CHARACTERS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask playerMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.ACCOUNT);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							PlayerAccount A = null;
							if(tracked instanceof PlayerAccount)
								A=(PlayerAccount)tracked;
							else
							if((tracked instanceof MOB)
							&&(((MOB)tracked).playerStats()!=null))
								A=((MOB)tracked).playerStats().getAccount();
							if(A!=null)
							{
								int num=0;
								if((playerMask==null)||(playerMask.empty()))
								{
									for(final Enumeration<String> p = A.getPlayers();p.hasMoreElements();)
										num++;
								}
								else
								{
									for(final Enumeration<ThinPlayer> p=A.getThinPlayers();p.hasMoreElements();)
									{
										final ThinPlayer P=p.nextElement();
										final MOB M=CMLib.players().getPlayerAllHosts(P.name());
										if(M != null)
										{
											if(CMLib.masking().maskCheck(playerMask, M, true))
												num++;
										}
										else
										{
											if(CMLib.masking().maskCheck(playerMask, P))
												num++;
										}
									}
								}
								if(num > 0)
									return num;
							}
							return 1;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							return true;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case CLANKILLS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask npcMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							if(parms.length>0)
							{
								MOB killed = null;
								for(final Object o : parms)
								{
									if(o instanceof MOB)
										killed=(MOB)o;
									else
									if(o instanceof Clan)
									{
										if((tracked instanceof Clan)
										&&(tracked != o))
											return false;
									}
								}
								if((killed != null)
								&&((npcMask==null)||(CMLib.masking().maskCheck(npcMask, killed, true)))
								&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true))))
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

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
		case CLANMEMBERS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask playerMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.CLAN);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof Clan)
							{
								if((playerMask==null)||(playerMask.empty()))
								{
									return ((Clan)tracked).getSize();
								}
								else
								{
									int num = 0;
									for(final FullMemberRecord m :((Clan)tracked).getFullMemberList())
									{
										final MOB M=CMLib.players().getPlayerAllHosts(m.name);
										if(M != null)
										{
											if(CMLib.masking().maskCheck(playerMask, M, true))
												num++;
										}
										else
										{
											final ThinPlayer P=CMLib.database().getThinUser(m.name);
											if((P!=null)&&(CMLib.masking().maskCheck(playerMask, P)))
												num++;
										}
									}
									return num;
								}
							}
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							for(final Object o : parms)
							{
								if(o instanceof Clan)
								{
									if((tracked instanceof Clan)
									&&(tracked != o))
										return false;
								}
							}
							return true;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case CLANPROPERTY:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask areaMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return (agent == Agent.CLAN);
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							Room R=null;
							for(final Object o : parms)
							{
								if(o instanceof Clan)
								{
									if((tracked instanceof Clan)
									&&(tracked != o))
										return false;
								}
								else
								if(o instanceof Room)
									R=(Room)o;
								else
								if(o instanceof Area)
									R=((Area)o).getRandomProperRoom();
							}
							final Area A=(R==null)?null:R.getArea();
							if((areaMask==null)
							||(A==null)
							||(CMLib.masking().maskCheck(areaMask, A, true)))
							{
								count+=bumpNum;
								if(count < 0)
									count = 0;
							}
							return true;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String areaMaskStr = CMStrings.deEscape(CMParms.getParmStr(parms, "AREAMASK", ""));
					this.areaMask = null;
					if(areaMaskStr.trim().length()>0)
						this.areaMask = CMLib.masking().getPreCompiledMask(areaMaskStr);
					return "";
				}
			};
			break;
		case SHIPSSUNK:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask shipMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							if((parms.length>0)
							&&(parms[0] instanceof BoardableShip)
							&&((shipMask==null)||(CMLib.masking().maskCheck(shipMask, (BoardableShip)parms[0], true)))
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					this.shipMask = null;
					final String shipMask=CMStrings.deEscape(CMParms.getParmStr(parms, "SHIPMASK", ""));
					if(shipMask.trim().length()>=0)
						this.shipMask = CMLib.masking().getPreCompiledMask(shipMask);
					final String playerMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(playerMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(playerMask);
					return "";
				}
			};
			break;
		case CONQUEREDAREAS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask areaMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							Room R=null;
							for(final Object o : parms)
							{
								if(o instanceof Clan)
								{
									if((tracked instanceof Clan)
									&&(tracked != o))
										return false;
								}
								else
								if(o instanceof Room)
									R=(Room)o;
								else
								if(o instanceof Area)
									R=((Area)o).getRandomProperRoom();
							}
							final Area A=(R==null)?null:R.getArea();
							if((areaMask==null)
							||(A==null)
							||(CMLib.masking().maskCheck(areaMask, A, true)))
							{
								count+=bumpNum;
								if(count < 0)
									count = 0;
							}
							return true;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String areaMaskStr = CMStrings.deEscape(CMParms.getParmStr(parms, "AREAMASK", ""));
					this.areaMask = null;
					if(areaMaskStr.trim().length()>0)
						this.areaMask = CMLib.masking().getPreCompiledMask(areaMaskStr);
					return "";
				}
			};
			break;
		case AREAVISIT:
			A=new Achievement()
			{
				private int num = -1;
				private long minTime = 10L * 60L * 1000L;
				private MaskingLibrary.CompiledZMask areaMask = null;
				private MaskingLibrary.CompiledZMask playerMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						private volatile long recentVisit = 0;

						@Override
						public Achievement getAchievement()
						{
							return me;
						}

						@Override
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							Area A=null;
							for(final Object o : parms)
							{
								if(o instanceof Clan)
								{
									if((tracked instanceof Clan)
									&&(tracked != o))
										return false;
								}
								else
								if(o instanceof Room)
									A=CMLib.map().getRoom((Room)o).getArea();
								else
								if(o instanceof Area)
									A=(Area)o;
							}
							if(((areaMask==null)||(A==null)||(CMLib.masking().maskCheck(areaMask, A, true)))
							&&((playerMask==null)||(CMLib.masking().maskCheck(playerMask, mob, true)))
							&&((recentVisit==0)||(System.currentTimeMillis()>recentVisit)))
							{
								recentVisit=System.currentTimeMillis() + minTime;
								count+=bumpNum;
								if(count < 0)
									count = 0;
							}
							return true;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String minTimeStr=CMParms.getParmStr(parms, "TIME_MINS", "");
					if(CMath.isInteger(minTimeStr))
						this.minTime = CMath.s_int(minTimeStr) * 60 * 1000;
					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String areaMaskStr = CMStrings.deEscape(CMParms.getParmStr(parms, "AREAMASK", ""));
					this.areaMask = null;
					if(areaMaskStr.trim().length()==0)
						return "Error: Missing or invalid AREAMASK parameter: "+numStr+"!";
					this.areaMask = CMLib.masking().getPreCompiledMask(areaMaskStr);
					final String zapperMask=CMStrings.deEscape(CMParms.getParmStr(parms, "PLAYERMASK", ""));
					this.playerMask = null;
					if(zapperMask.trim().length()>0)
						this.playerMask = CMLib.masking().getPreCompiledMask(zapperMask);
					return "";
				}
			};
			break;
		case CLANDECLARE:
			A=new Achievement()
			{
				private int num = -1;
				private final Set<String> relationList = new TreeSet<String>();
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							if(parms.length>0)
							{
								String newRelation = null;
								for(final Object o : parms)
								{
									if(o instanceof Clan)
									{
										if((tracked instanceof Clan)
										&&(tracked != o))
											return false;
									}
									else
									if(o instanceof String)
										newRelation = ((String)o).toUpperCase().trim();
								}
								if((newRelation != null)
								&&(relationList.contains(newRelation)))
								{
									count+=bumpNum;
									if(count < 0)
										count = 0;
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final List<String> relationV = CMParms.parse(CMStrings.deEscape(CMParms.getParmStr(parms, "RELATION", "")));
					this.relationList.clear();
					for(final String r : relationV)
					{
						if(!CMStrings.contains(Clan.REL_DESCS, r.toUpperCase().trim()))
							return "Error: Invalid RELATION parameter: "+r+"!";
						this.relationList.add(r.toUpperCase().trim());
					}
					if(this.relationList.size()==0)
						return "Error: Missing relations list!";

					return "";
				}
			};
			break;
		case CONQUESTPOINTS:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask areaMask = null;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
				}

				@Override
				public boolean canApplyTo(final Agent agent)
				{
					return true;
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) >= num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							return count;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							Room R=null;
							for(final Object o : parms)
							{
								if(o instanceof Clan)
								{
									if((tracked instanceof Clan)
									&&(tracked != o))
										return false;
								}
								else
								if(o instanceof Room)
									R=(Room)o;
								else
								if(o instanceof Area)
									R=((Area)o).getRandomProperRoom();
							}
							final Area A=(R==null)?null:R.getArea();
							if((areaMask==null)
							||(A==null)
							||(CMLib.masking().maskCheck(areaMask, A, true)))
							{
								count+=bumpNum;
								if(count < 0)
									count = 0;
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
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
					final String areaMaskStr = CMStrings.deEscape(CMParms.getParmStr(parms, "AREAMASK", ""));
					this.areaMask = null;
					if(areaMaskStr.trim().length()>0)
						this.areaMask = CMLib.masking().getPreCompiledMask(areaMaskStr);
					return "";
				}
			};
			break;
		case CLANLEVELSGAINED:
			A=new Achievement()
			{
				private int num = -1;
				private MaskingLibrary.CompiledZMask seenMask = null;

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
				public boolean canApplyTo(final Agent agent)
				{
					return true;
				}

				@Override
				public boolean canBeSeenBy(final MOB mob)
				{
					return ((seenMask==null)||(CMLib.masking().maskCheck(seenMask, mob, true)));
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
				public String getRawParmVal(final String str)
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
						public boolean isAchieved(final Tattooable tracked)
						{
							return (num>=0) && (getCount(tracked) > num);
						}

						@Override
						public int getCount(final Tattooable tracked)
						{
							if(tracked instanceof Clan)
							{
								return ((Clan)tracked).getClanLevel();
							}
							return 0;
						}

						@Override
						public boolean testBump(final MOB mob, final Tattooable tracked, final int bumpNum, final Object... parms)
						{
							for(final Object o : parms)
							{
								if(o instanceof Clan)
								{
									if((tracked instanceof Clan)
									&&(tracked != o))
										return false;
								}
							}
							return true;
						}

						@Override
						public Tracker copyOf()
						{
							try
							{
								return (Tracker)this.clone();
							}
							catch(final Exception e)
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
					this.seenMask=null;
					final String seenMask=CMStrings.deEscape(CMParms.getParmStr(parms, "VISIBLEMASK", ""));
					if(seenMask.trim().length()>0)
						this.seenMask = CMLib.masking().getPreCompiledMask(seenMask);

					final String numStr=CMParms.getParmStr(parms, "NUM", "");
					if(!CMath.isInteger(numStr))
						return "Error: Missing or invalid NUM parameter: "+numStr+"!";
					num=CMath.s_int(numStr);
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

		if(!A.canApplyTo(agent))
		{
			return "Error: Achievement type: "+eventStr+" can not apply to "+agent.name();
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
			case CLAN:
				clanAchievements.add(A);
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
		if(CMLib.expertises().numExpertises()==0)
		{
			Log.errOut("Achievements being loaded before Expertises!");
			Log.debugOut(new Exception("Debug Me!"));
		}
		if((playerAchievements==null)||(accountAchievements==null)||(clanAchievements==null))
		{
			synchronized(this)
			{
				if((playerAchievements==null)||(accountAchievements==null)||(clanAchievements==null))
				{
					reloadAchievements();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized Enumeration<Achievement> achievements(final Agent agent)
	{
		ensureAchievementsLoaded();
		if(agent == null)
		{
			return new MultiEnumeration<Achievement>(new Enumeration[]{
				new IteratorEnumeration<Achievement>(accountAchievements.iterator()),
				new IteratorEnumeration<Achievement>(playerAchievements.iterator()),
				new IteratorEnumeration<Achievement>(clanAchievements.iterator()),
			});
		}
		switch(agent)
		{
		case ACCOUNT:
			return new IteratorEnumeration<Achievement>(accountAchievements.iterator());
		case CLAN:
			return new IteratorEnumeration<Achievement>(clanAchievements.iterator());
		default:
		case PLAYER:
			return new IteratorEnumeration<Achievement>(playerAchievements.iterator());
		}
	}

	protected void possiblyBumpPlayerAchievement(final MOB mob, final Achievement A, final PlayerStats pStats, final Event E, final int bumpNum, final Object... parms)
	{
		if(mob.findTattoo(A.getTattoo())==null)
		{
			final Tracker T=pStats.getAchievementTracker(A, mob, mob);
			if(T.testBump(mob, mob, bumpNum, parms))
			{
				if(T.isAchieved(mob))
				{
					giveAwards(A,mob,mob,AchievementLoadFlag.NORMAL);
				}
			}
		}
	}

	protected void possiblyBumpAccountAchievement(final MOB mob, final Achievement A, final PlayerAccount account, final Event E, final int bumpNum, final Object... parms)
	{
		if(account != null)
		{
			if(account.findTattoo(A.getTattoo())==null)
			{
				final Tracker T=account.getAchievementTracker(A, mob, mob);
				if(T.testBump(mob, mob, bumpNum, parms))
				{
					if(T.isAchieved(mob))
					{
						giveAwards(A,account,mob,AchievementLoadFlag.NORMAL);
					}
				}
			}
		}
	}

	protected void possiblyBumpClanAchievement(final MOB mob, final Achievement A, final Iterable<Pair<Clan,Integer>> clans, final Event E, final int bumpNum, final Object... parms)
	{
		if(clans != null)
		{
			if((parms.length>0)
			&&(parms[0] instanceof Clan))
			{
				final Clan C=(Clan)parms[0];
				if(C.findTattoo(A.getTattoo())==null)
				{
					final Tracker T=C.getAchievementTracker(A, C, mob);
					if(T.testBump(mob, C, bumpNum, parms))
					{
						if(T.isAchieved(C))
						{
							giveAwards(A,C,mob,AchievementLoadFlag.NORMAL);
						}
					}
				}
			}
			else
			for(final Pair<Clan,Integer> p : clans)
			{
				final Clan C=p.first;
				if(C.findTattoo(A.getTattoo())==null)
				{
					final Tracker T=C.getAchievementTracker(A, C, mob);
					if(T.testBump(mob, C, bumpNum, parms))
					{
						if(T.isAchieved(C))
						{
							giveAwards(A,C,mob,AchievementLoadFlag.NORMAL);
						}
					}
				}
			}
		}
	}

	@Override
	public void possiblyBumpAchievement(final MOB mob, final Event E, final int bumpNum, final Object... parms)
	{
		if((mob != null)
		&&(E!=null)
		&&(mob.isPlayer())
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
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
						case CLAN:
							possiblyBumpClanAchievement(mob, A, mob.clans(), E, bumpNum, parms);
							break;
						}
					}
				}
			}
		}
	}

	protected List<Achievement> fakeBumpPlayerAchievement(final MOB mob, final Achievement A, final PlayerStats pStats, final Event E, final int bumpNum, final Object... parms)
	{
		final List<Achievement> achievements=new ArrayList<Achievement>(1);
		if(mob.findTattoo(A.getTattoo())==null)
		{
			Tracker T=pStats.getAchievementTracker(A, mob, mob);
			T=T.copyOf();
			if(T.testBump(mob, mob, bumpNum, parms))
			{
				if(T.isAchieved(mob))
				{
					achievements.add(A);
				}
			}
		}
		return achievements;
	}

	protected List<Achievement> fakeBumpAccountAchievement(final MOB mob, final Achievement A, final PlayerAccount account, final Event E, final int bumpNum, final Object... parms)
	{
		final List<Achievement> achievements=new ArrayList<Achievement>(1);
		if(account != null)
		{
			if(account.findTattoo(A.getTattoo())==null)
			{
				Tracker T=account.getAchievementTracker(A, mob, mob);
				T=T.copyOf();
				if(T.testBump(mob, mob, bumpNum, parms))
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

	protected List<Achievement> fakeBumpClanAchievements(final MOB mob, final Achievement A, final Iterable<Pair<Clan,Integer>> clans, final Event E, final int bumpNum, final Object... parms)
	{
		final List<Achievement> achievements=new ArrayList<Achievement>(1);
		if(clans != null)
		{
			for(final Pair<Clan,Integer> p : clans)
			{
				final Clan C=p.first;
				if(C.findTattoo(A.getTattoo())==null)
				{
					Tracker T=C.getAchievementTracker(A, C, mob);
					T=T.copyOf();
					if(T.testBump(mob, C, bumpNum, parms))
					{
						if(T.isAchieved(C))
						{
							achievements.add(A);
						}
					}
				}
			}
		}
		return achievements;
	}

	@Override
	public List<Achievement> fakeBumpAchievement(final MOB mob, final Event E, final int bumpNum, final Object... parms)
	{
		final List<Achievement> achievements=new ArrayList<Achievement>(1);
		if((mob != null)
		&&(E!=null)
		&&(mob.isPlayer())
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
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
						case CLAN:
							achievements.addAll(fakeBumpClanAchievements(mob, A, mob.clans(), E, bumpNum, parms));
							break;
						}
					}
				}
			}
		}
		return achievements;
	}

	protected void giveAwards(final MOB mob, final Clan forClan, final Award[] awardSet, final AchievementLoadFlag flag)
	{
		if(mob == null)
			return;
		final PlayerStats pStats = mob.playerStats();
		final StringBuilder awardMessage = new StringBuilder("");
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
			case CLANXP:
				// never granted to players
				break;
			case CLANCURRENCY:
				// never granted to players
				break;
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
				final String currency = CMLib.english().matchAnyCurrencySet(aaward.getCurrency());
				if(currency != null)
				{
					final double denomination = CMLib.english().matchAnyDenomination(currency, aaward.getCurrency());
					if(denomination != 0.0)
					{
						final double money=CMath.mul(aaward.getAmount(),  denomination);
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
					final String value = mob.playerStats().getAccount().getStat(stat);
					if(CMath.isNumber(value))
					{
						int amount=aaward.getAmount();
						if("ARMOR".startsWith(stat))
							amount = CMath.abs(amount);
						awardMessage.append(L("^HYour account is awarded @x1!\n\r^?",amount + " " + stat));
						mob.playerStats().getAccount().setStat(stat, "" + (CMath.s_int(value) + aaward.getAmount()));
					}
				}
				else
				{
					final String value = CMLib.coffeeMaker().getAnyGenStat(mob, aaward.getStat());
					if(CMath.isNumber(value))
					{
						int amount=aaward.getAmount();
						if("ARMOR".startsWith(aaward.getStat()))
							amount = CMath.abs(amount);
						awardMessage.append(L("^HYou are awarded @x1!\n\r^?",amount + " " + aaward.getStat()));
						CMLib.coffeeMaker().setAnyGenStat(mob, aaward.getStat(), "" + (CMath.s_int(value) + aaward.getAmount()));
						mob.recoverMaxState();
						mob.recoverCharStats();
						mob.recoverPhyStats();
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
			case NOPURGE:
			{
				final Command C=CMClass.getCommand("NoPurge");
				if(C!=null)
				{
					try
					{
						if(C.executeInternal(mob, 0, mob) == Boolean.TRUE)
							awardMessage.append(L("^HYou are now protected from auto-purge!\n\r^?\n\r"));
					}
					catch (final IOException e)
					{
						Log.errOut(e);
					}
				}
				break;
			}
			case TITLE:
			{
				final TitleAward aaward=(TitleAward)award;
				if(pStats != null)
				{
					String titleStr = aaward.getTitle();
					if(forClan != null)
						titleStr = CMStrings.replaceAlls(titleStr, new String[][] {{"@1",forClan.getGovernmentName()},{"@2",forClan.clanID()}});
					if(!pStats.getTitles().contains(titleStr))
					{
						pStats.getTitles().add(titleStr);
						awardMessage.append(L("^HYou are awarded the title: @x1!\n\r^?",CMStrings.replaceAll(titleStr,"*",mob.Name())));
					}
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
		if(awardMessage.length()>0)
			mob.tell(awardMessage.toString());
		grantAbilitiesAndExpertises(mob);
	}

	protected void giveAwards(final Clan clan, final Award[] awardSet, final AchievementLoadFlag flag)
	{
		if(clan == null)
			return;
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
			case XP:
				// awarded elsewhere, not directly to clan
				break;
			case CURRENCY:
				// awarded elsewhere, not directly to clan
				break;
			case ABILITY:
				// awarded elsewhere, not directly to clan
				break;
			case CLANCURRENCY:
			{
				final CurrencyAward aaward=(CurrencyAward)award;
				String currency = CMLib.english().matchAnyCurrencySet(aaward.getCurrency());
				if(currency != null)
				{
					final double denomination = CMLib.english().matchAnyDenomination(currency, aaward.getCurrency());
					if(denomination != 0.0)
					{
						final double money=CMath.mul(aaward.getAmount(),  denomination);
						Banker clanBank=null;
						for(final Enumeration<Banker> b = CMLib.map().banks();b.hasMoreElements();)
						{
							final Banker B=b.nextElement();
							if((B!=null)
							&&(!B.amDestroyed())
							&&(B.isAccountName(clan.clanID())))
							{
								clanBank=B;
								currency=CMLib.beanCounter().getCurrency(B);
								if((currency == null)
								||(currency.length()==0))
									currency="gold";
								break;
							}
						}
						if(clanBank == null)
							CMLib.clans().clanAnnounce(clan.getResponsibleMember(),L("Your @x2 @x3 would have had @x1 deposited in its account, if it had one.",CMLib.beanCounter().getDenominationName(currency, denomination, aaward.getAmount()),clan.getGovernmentName(),clan.name()));
						else
						{
							final MOB M=clan.getResponsibleMember();
							CMLib.clans().clanAnnounce(clan.getResponsibleMember(),L("Your @x2 @x3 has @x1 deposited in its account.",CMLib.beanCounter().getDenominationName(currency, denomination, aaward.getAmount()),clan.getGovernmentName(),clan.name()));
							CMLib.beanCounter().modifyBankGold(clanBank.bankChain(), clan.clanID(),
									CMLib.utensils().getFormattedDate(M)
									+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(currency,money)
									+": Achievement award",
									currency, money);
						}
					}
				}
				break;
			}
			case STAT:
				// awarded elsewhere, not directly to clan
				break;
			case EXPERTISE:
				// awarded elsewhere, not directly to clan
				break;
			case QP:
				// awarded elsewhere, not directly to clan
				break;
			case NOPURGE:
				// awarded elsewhere, not directly to clan
				break;
			case TITLE:
				// awarded elsewhere, not directly to clan
				break;
			case CLANXP:
			{
				final AmountAward aaward=(AmountAward)award;
				CMLib.clans().clanAnnounce(clan.getResponsibleMember(),L("Your @x2 @x3 has been granted @x1 experience.",""+aaward.getAmount(),clan.getGovernmentName(),clan.name()));
				clan.adjExp(null, aaward.getAmount());
				break;
			}
			default:
				break;

			}
		}
	}

	@Override
	public String removeClanAchievementAwards(final MOB mob, final Clan clan)
	{
		if((clan == null)||(mob==null))
			return "";
		final StringBuilder str=new StringBuilder("");
		for(final Enumeration<Tattoo> t=clan.tattoos();t.hasMoreElements();)
		{
			final Tattoo T=t.nextElement();
			final Achievement A = this.getAchievement(T.getTattooName());
			if((A!=null)
			&&(mob.findTattoo(T.getTattooName())!=null))
				str.append(removeAwards(mob, clan, A));
		}
		return str.toString();
	}

	@Override
	public String fixAwardDescription(final Achievement A, final Award award, final MOB forM, Tattooable forT)
	{
		if(award == null)
			return "";
		if(award.getType() == AwardType.TITLE)
		{
			final String name = (forM == null) ? "[Name]" : forM.name();
			if((A!=null)
			&&(A.getAgent()==Agent.CLAN)
			&&(!(forT instanceof Clan))
			&&(forM instanceof MOB))
			{
				for(final Pair<Clan, Integer> p : forM.clans())
				{
					final Clan C = p.first;
					if(C.findTattoo(A.getTattoo()) != null)
						forT=C;
				}
			}
			final String clanType = (forT instanceof Clan) ? ((Clan)forT).getGovernmentName() : "[CLANTYPE]";
			final String clanName = (forT instanceof Clan) ? ((Clan)forT).clanID() : "[CLANNAME]";
			return CMStrings.replaceAlls(award.getDescription(), new String[][] {{"*",name},{"@1",clanType},{"@2",clanName}});
		}
		return award.getDescription();
	}

	protected String removeAwards(final MOB mob, final Clan forClan, final Achievement achievement)
	{
		if(mob == null)
			return "";
		final PlayerStats pStats = mob.playerStats();
		final StringBuilder awardMessage = new StringBuilder("");
		final Award[] awardSet = achievement.getRewards();
		for(final Award award : awardSet)
		{
			final String chkAwardDesc = award.getDescription();
			boolean alsoAwardedElsewhere = false;
			for(final Enumeration<Tattoo> t = mob.tattoos();t.hasMoreElements();)
			{
				final Tattoo T=t.nextElement();
				final Achievement A = getAchievement(T.getTattooName());
				if((A!=null)
				&&(A!=achievement)
				&&(!alsoAwardedElsewhere)
				&&(mob.findTattoo(T.getTattooName())!=null))
				{
					for(final Award chkAward : A.getRewards())
					{
						if((chkAward.getType() == award.getType())
						&&(chkAwardDesc.equals(award.getDescription())))
						{
							alsoAwardedElsewhere = true;
							break;
						}
					}
				}
			}
			switch(award.getType())
			{
			case ABILITY:
			{
				final AbilityAward aaward = (AbilityAward)award;
				if((pStats!=null)
				&&(!alsoAwardedElsewhere)
				&& (pStats.getExtraQualifiedSkills().containsKey(aaward.getAbilityMapping().abilityID())))
				{
					final Ability A=CMClass.getAbility(aaward.getAbilityMapping().abilityID());
					if(A!=null)
					{
						pStats.getExtraQualifiedSkills().remove(A.ID());
						if(!CMLib.ableMapper().qualifiesByLevel(mob, A))
						{
							final Ability myA=mob.fetchAbility(A.ID());
							if(myA!=null)
							{
								mob.delAbility(myA);
								final Ability effectA=mob.fetchEffect(myA.ID());
								if(effectA.isNowAnAutoEffect())
								{
									effectA.unInvoke();
									mob.delEffect(effectA);
								}
							}
						}
						awardMessage.append(L("^HYou have lost your qualification for @x1 at level @x2!\n\r^?",A.name(),""+aaward.getAbilityMapping().qualLevel()));
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
					final String value = mob.playerStats().getAccount().getStat(stat);
					if(CMath.isNumber(value))
					{
						final int oldVal = CMath.s_int(value);
						if(oldVal >= aaward.getAmount())
						{
							awardMessage.append(L("^HYour account has lost @x1!\n\r^?",aaward.getAmount() + " " + stat));
							mob.playerStats().getAccount().setStat(stat, "" + (oldVal - aaward.getAmount()));
						}
					}
				}
				else
				{
					final String value = CMLib.coffeeMaker().getAnyGenStat(mob, aaward.getStat());
					if(CMath.isNumber(value))
					{
						final int oldVal = CMath.s_int(value);
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
					if((pStats.getExtraQualifiedExpertises().containsKey(aaward.getExpertise().ID()))
					&&(!alsoAwardedElsewhere))
					{
						final ExpertiseDefinition E=aaward.getExpertise();
						pStats.getExtraQualifiedExpertises().remove(E.ID());
						boolean found=false;
						for(final ExpertiseDefinition chkE : CMLib.expertises().myQualifiedExpertises(mob))
						{
							if(chkE.ID().equals(E.ID()))
								found=true;
						}
						if(!found)
							mob.delExpertise(E.ID());
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
			case NOPURGE:
			{
				final Command C=CMClass.getCommand("NoPurge");
				if((C!=null)
				&&(!alsoAwardedElsewhere))
				{
					try
					{
						if(C.executeInternal(mob, MUDCmdProcessor.METAFLAG_REVERSED, mob) == Boolean.TRUE)
							awardMessage.append(L("^HYou are no longer protected from auto-purge!\n\r^?\n\r"));
					}
					catch (final IOException e)
					{
						Log.errOut(e);
					}
				}
				break;
			}
			case TITLE:
			{
				final TitleAward aaward=(TitleAward)award;
				if(pStats != null)
				{
					String titleStr = aaward.getTitle();
					if(forClan != null)
						titleStr = CMStrings.replaceAlls(titleStr, new String[][] {{"@1",forClan.getGovernmentName()},{"@2",forClan.clanID()}});
					if((pStats.getTitles().contains(titleStr))
					&&(!alsoAwardedElsewhere))
					{
						pStats.getTitles().remove(titleStr);
						awardMessage.append(L("^HYou have lost the title: @x1!\n\r^?",CMStrings.replaceAll(titleStr,"*",mob.Name())));
					}
				}
				break;
			}
			case XP:
			{
				// also, never lost
				break;
			}
			case CLANXP:
			{
				// never lost to players per se
				break;
			}
			case CLANCURRENCY:
			{
				// never lost to players per se
				break;
			}
			case CURRENCY:
			{
				// is never un-awarded
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
			if((!CMLib.flags().isCloaked(mob))
			&&(A.canBeSeenBy(mob)))
			{
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.ACHIEVEMENTS, mob);
				final PlayerStats pStats = mob.playerStats();
				final PlayerAccount account = (pStats != null) ? pStats.getAccount() : null;
				final String name;
				if((A.getAgent() == Agent.ACCOUNT) && (account != null))
					name = account.getAccountName();
				else
				if((A.getAgent() == Agent.CLAN) && (holder instanceof Clan))
					name = ((Clan)holder).clanID();
				else
					name = mob.name();
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has completed the '@x2' @x3 achievement!",name,A.getDisplayStr(),A.getAgent().name().toLowerCase()),true);
			}
			final Award[] awardSet = A.getRewards();
			if((A.getAgent() == Agent.CLAN)
			&&(holder instanceof Clan))
			{
				final Clan C=(Clan)holder;
				if(A.canBeSeenBy(mob))
					CMLib.clans().clanAnnounce(mob,L("Your @x2 @x3 has completed the @x1 achievement.",A.getDisplayStr(),C.getGovernmentName(),C.name()));
				this.giveAwards(C, awardSet, flag);
				for(final Iterator<Session> s = CMLib.sessions().sessions();s.hasNext();)
				{
					final Session S=s.next();
					if(S!=null)
					{
						final MOB M=S.mob();
						if(M!=null)
						{
							final Pair<Clan,Integer> role=M.getClanRole(C.clanID());
							if(role!=null)
								loadClanAchievements(C,M,role.second.intValue(),AchievementLoadFlag.NORMAL);
						}
					}
				}
			}
			else
			{
				final StringBuilder awardMessage = new StringBuilder(L("^HYou have completed the '@x1' @x2 achievement!^?\n\r",A.getDisplayStr(),A.getAgent().name().toLowerCase()));
				if(A.canBeSeenBy(mob))
					mob.tell(awardMessage.toString());
			}
			if(A.getAgent() == Agent.PLAYER)
			{
				giveAwards(mob,null,awardSet,flag);
			}
			return true;
		}
		return false;
	}

	@Override
	public Achievement getAchievement(final String tattoo)
	{
		for(final Enumeration<Achievement> a = achievements(null); a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if(A.getTattoo().equalsIgnoreCase(tattoo))
				return A;
		}
		return null;
	}

	@Override
	public Achievement deleteAchievement(final String tattoo)
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
			case CLAN:
				clanAchievements.remove(A);
				break;
			}
			final List<Achievement> list=eventMap.get(A.getEvent());
			if(list != null)
				list.remove(A);
		}
		return A;
	}

	@Override
	public String getAchievementsHelpFromMap(final Map<String,Map<String,String>> helpMap, final Event E, final String parmName)
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
			for(final Map<String,String> map : helpMap.values())
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
		for(final Map<String,String> map : helpMap.values())
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
				final int y=s.indexOf("\"",x+7);
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
				final int start=s.indexOf('[');
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
				final String oldS=parmMap.containsKey(keyName) ? parmMap.get(keyName) : "";
				value = oldS + "\n\r" + value;
				parmMap.put(keyName, value);
			}

		}
		return help;
	}

	public String buildRow(final Event E, final Map<String,String> parmTree)
	{
		final StringBuilder str=new StringBuilder(parmTree.get("TATTOO")+"=");
		for(final String parmName : E.getParameters())
		{
			final String value = parmTree.get(parmName);
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
		for(final Award award : awards)
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
			case CLANCURRENCY:
				awardStr.append(" ").append(((CurrencyAward)award).getAmount())
						.append(" clan ").append(((CurrencyAward)award).getCurrency());
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
			case CLANXP:
				awardStr.append(" ").append(((AmountAward)award).getAmount())
						.append(" ").append("CLANXP");
				break;
			case NOPURGE:
				awardStr.append(" 1 NOPURGE");
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
		for(final Award award : A.getRewards())
		{
			if(award.getType() == AwardType.TITLE)
				parmTree.put("TITLE", ((TitleAward)award).getTitle());
		}
		final String awardStr=getAwardString(A.getRewards());
		parmTree.put("REWARDS", awardStr);
		String visibleMask = A.getRawParmVal("VISIBLEMASK");
		if(visibleMask == null)
			visibleMask="";
		parmTree.put("VISIBLEMASK", visibleMask);
		for(final String s : A.getEvent().getParameters())
		{
			if(!CMParms.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS, s))
				parmTree.put(s, CMStrings.deEscape(A.getRawParmVal(s)));
		}
	}

	@Override
	public boolean addModifyAchievement(final MOB mob, final Agent agent, final String tattoo, final Achievement A)
	{
		if(!mob.isPlayer())
			return false;
		boolean ok=false;
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		final TreeMap<String,String> parmTree=new TreeMap<String,String>();
		for(final Event E : Event.values())
		{
			for(final String s : E.getParameters())
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

				help=getAchievementsHelpFromMap(helpMap,null,"VISIBLEMASK");
				parmTree.put("VISIBLEMASK",CMLib.genEd().prompt(mob, parmTree.get("VISIBLEMASK"), ++showNumber, showFlag, L("Visible Mask"), true, false, help, null, null));

				for(final String parmName : E.getParameters())
				{
					if(!CMStrings.contains(BASE_ACHIEVEMENT_PARAMETERS, parmName))
					{
						help=getAchievementsHelpFromMap(helpMap,E,parmName);
						final boolean emptyOK = parmName.equals("VISIBLEMASK");
						parmTree.put(parmName,CMLib.genEd().prompt(mob, parmTree.get(parmName), ++showNumber, showFlag, CMStrings.capitalizeAndLower(parmName), emptyOK, false, help, null, null));
					}
				}

				for(final String parmName : parmTree.keySet())
				{
					if((!parmName.equals("TATTOO"))&&(!CMStrings.contains(E.getParameters(), parmName)))
						parmTree.put(parmName, "");
				}

				final String achievementRow = buildRow(E,parmTree);
				final String err = evaluateAchievement(agent, achievementRow, false);
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
			for(final Enumeration<MOB> m = CMLib.players().players();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(M.playerStats()!=null)
				{
					M.playerStats().rebuildAchievementTracker(mob, M, parmTree.get("TATTOO"));
				}
			}
			this.resaveAchievements(parmTree.get("TATTOO"));
			return true;
		}
		catch (final IOException e)
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
		for(final Agent agent : Agent.values())
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
		for(final String row : V)
		{
			if(row.trim().startsWith("#")||row.trim().startsWith(";")||(row.trim().length()==0))
				newFileData.append(row).append(EOL);
			else
			{
				final int x=row.indexOf('=');
				if(x<=0)
				{
					if(row.trim().startsWith("["))
					{
						final Agent oldAgent = currentAgent;
						for(final Agent ag : Agent.values())
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
			final Tracker T=pStats.getAchievementTracker(A, mob, mob);
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
				final Tracker T=account.getAchievementTracker(A, mob, mob);
				if(T.isAchieved(mob))
				{
					return giveAwards(A, account, mob,AchievementLoadFlag.NORMAL);
				}
			}
		}
		return false;
	}

	private boolean evaluateClanAchievement(final Achievement A, final Clan C, final MOB mob)
	{
		if(C != null)
		{
			if(C.findTattoo(A.getTattoo())==null)
			{
				final Tracker T=C.getAchievementTracker(A, C, mob);
				if(T.isAchieved(C))
				{
					return giveAwards(A, C, mob,AchievementLoadFlag.NORMAL);
				}
			}
		}
		return false;
	}

	@Override
	public boolean evaluatePlayerAchievements(final MOB mob)
	{
		if((mob==null)||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			return false;
		final PlayerStats P=mob.playerStats();
		if(P==null)
			return false;
		boolean somethingDone = false;
		for(final Enumeration<Achievement> a=achievements(Agent.PLAYER);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if(evaluatePlayerAchievement(A, P, mob))
				somethingDone = true;
		}
		return somethingDone;
	}

	@Override
	public boolean evaluateAccountAchievements(final MOB mob)
	{
		if((mob==null)||(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
			return false;
		final PlayerStats P=mob.playerStats();
		if(P==null)
			return false;
		final PlayerAccount C=P.getAccount() != null ? P.getAccount() : null;
		if(C==null)
			return false;
		boolean somethingDone = false;
		for(final Enumeration<Achievement> a=achievements(Agent.ACCOUNT);a.hasMoreElements();)
		{
			final Achievement A=a.nextElement();
			if(evaluateAccountAchievement(A, C, mob))
				somethingDone = true;
		}
		return somethingDone;
	}

	@Override
	public boolean evaluateClanAchievements()
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return false;
		boolean somethingDone=false;
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			final MOB mob=C.getResponsibleMember();
			if(mob != null)
			{
				for(final Enumeration<Achievement> a=achievements(Agent.CLAN);a.hasMoreElements();)
				{
					final Achievement A=a.nextElement();
					if(evaluateClanAchievement(A, C, mob))
						somethingDone = true;
				}
			}
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
		clanAchievements=new SLinkedList<Achievement>();
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
				for(final Agent ag : Agent.values())
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
				boolean doUpdateStats = false;
				if((evaluatePlayerAchievements(M)))
					doUpdateStats = doUpdateStats || (!CMLib.flags().isInTheGame(M,true));
				final PlayerAccount pA=M.playerStats().getAccount();
				if(pA != null)
				{
					if(evaluateAccountAchievements(M))
						doUpdateStats = doUpdateStats || (!CMLib.flags().isInTheGame(M,true));
				}
				if(doUpdateStats)
					CMLib.database().DBUpdatePlayerPlayerStats(M);
			}
		}
	}

	@Override
	public void grantAbilitiesAndExpertises(final MOB mob)
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
	public void loadPlayerSkillAwards(final Tattooable mob, final PlayerStats stats)
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
	public void reloadPlayerAwards(final MOB mob, final AchievementLoadFlag flag)
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
					giveAwards(mob, null, A.getRewards(), flag);
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
			for(final Enumeration<Tattoo> t=account.tattoos();t.hasMoreElements();)
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
						giveAwards(mob, null, A.getRewards(), flag);
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

	protected boolean loadClanAchievements(final Clan clan, final MOB mob, final int clanRole, final AchievementLoadFlag flag)
	{
		boolean somethingDone = false;
		if((mob!=null) && (clan != null))
		{
			if(clan.getAuthority(clanRole,Function.CLAN_BENEFITS)!=Clan.Authority.CAN_NOT_DO)
			{
				for(final Enumeration<Tattoo> t=clan.tattoos();t.hasMoreElements();)
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
							giveAwards(mob, clan, A.getRewards(), flag);
						}
					}
				}
			}
		}
		return somethingDone;
	}

	@Override
	public void loadClanAchievements(final MOB mob, final AchievementLoadFlag flag)
	{
		if(mob == null)
			return;
		boolean somethingDone = false;
		final PlayerStats pStats = mob.playerStats();
		for(final Pair<Clan,Integer> cp : mob.clans())
		{
			final Clan clan = cp.first;
			somethingDone = this.loadClanAchievements(clan, mob, cp.second.intValue(), flag) || somethingDone;
			if(somethingDone)
			{
				loadPlayerSkillAwards(mob, pStats);
				grantAbilitiesAndExpertises(mob);
			}
		}
	}

	protected String makeAchievmentHelp(final Achievement A)
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
		final String help = helpMap.get(A.getTattoo());
		if(help != null)
			return help;
		final StringBuilder str = new StringBuilder("");
		final int cols = 20;
		if(A.getAgent() == Agent.ACCOUNT)
			str.append("\n\r").append(CMStrings.padRight(L("Account Achievement:"),cols)).append(" ").append(A.getTattoo());
		else
		if(A.getAgent() == Agent.CLAN)
			str.append("\n\r").append(CMStrings.padRight(L("Clan Achievement:"),cols)).append(" ").append(A.getTattoo());
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
			if(A.getAgent() == Agent.CLAN)
				str.append("\n\r").append(CMStrings.padRight(L("Rewards Granted:"),cols)).append(" ").append(L("Existing Members"));
			else
				str.append("\n\r").append(CMStrings.padRight(L("Rewards Granted:"),cols)).append(" ").append(L("Immediately"));
		}
		for(final Award W : A.getRewards())
		{
			str.append("\n\r").append(CMStrings.padRight(L("Award:"),cols)).append(" ")
				.append(CMLib.achievements().fixAwardDescription(A, W, null, null));
		}
		str.append("\n\r");
		helpMap.put(A.getTattoo(), str.toString());
		return str.toString();
	}

	@Override
	public String getAchievementsHelp(String ID, final boolean exact)
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
		clanAchievements=null;
		eventMap=null;
		return super.shutdown();
	}
}
