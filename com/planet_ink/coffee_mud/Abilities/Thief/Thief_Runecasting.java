package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.CoffeeTime.TimeDelta;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMaskEntry;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_Runecasting extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Runecasting";
	}

	@Override
	public String displayText()
	{
		if(invoker() == affected)
			return L("(Runecasting)");
		else
			return "";
	}

	private final static String localizedName = CMLib.lang().L("Runecasting");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"RUNECASTING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected static String[] runeStarts = new String[]
	{
		"The runes indicate...",
		"Your fate has been cast...",
		"And the runes show your future..."
	};

	protected static String[] runeFails = new String[]
	{
		"Astral clouds are blocking your aura.",
		"Your future is unbound. Tread carefully.",
		"Your path is clear.",
		"The fates` gaze is elsewhere."
	};

	protected ExpireVector<MOB>	doneMs				= new ExpireVector<MOB>(getExpirationTime());
	protected List<String>		reports				= null;
	protected int				tickUp				= 0;
	protected MOB				forM				= null;

	protected long getExpirationTime()
	{
		return CMProps.getMillisPerMudHour();
	}

	protected Filterer<AutoProperties> runePlayerFilter = new Filterer<AutoProperties>()
	{
		@Override
		public boolean passesFilter(final AutoProperties obj)
		{
			boolean foundOne = false;
			for(final CompiledZMaskEntry[] entrySet : obj.getPlayerCMask().entries())
			{
				if(entrySet == null)
					continue;
				for(final CompiledZMaskEntry entry : entrySet)
				{
					switch(entry.maskType())
					{
					case ANYCLASS:
					case ANYCLASSLEVEL:
					case BASECLASS:
					case MAXCLASSLEVEL:
					case _ANYCLASS:
					case _ANYCLASSLEVEL:
					case _BASECLASS:
					case _MAXCLASSLEVEL:
						return false;
					case BIRTHDAY:
					case BIRTHDAYOFYEAR:
					case BIRTHMONTH:
					case BIRTHSEASON:
					case BIRTHWEEK:
					case BIRTHWEEKOFYEAR:
					case BIRTHYEAR:
					case _BIRTHDAY:
					case _BIRTHDAYOFYEAR:
					case _BIRTHMONTH:
					case _BIRTHSEASON:
					case _BIRTHWEEK:
					case _BIRTHWEEKOFYEAR:
					case _BIRTHYEAR:
						return false;
					case ALIGNMENT:
					case FACTION:
					case TATTOO:
					case _ALIGNMENT:
					case _FACTION:
					case _TATTOO:
						return false;
					case IF:
					case NPC:
					case OR:
					case PLAYER:
					case PORT:
					case SECURITY:
					case SUBOP:
					case SYSOP:
					case _IF:
					case _NPC:
					case _OR:
					case _PLAYER:
					case _PORT:
					case _SECURITY:
					case _SUBOP:
					case _SYSOP:
						// neutral
						break;
					case RACE:
					case RACECAT:
					case _RACE:
					case _RACECAT:
						return false;
					default:
						foundOne=true;
						break;
					}
				}
			}
			return foundOne;
		}
	};

	public String[] getStartPhrases()
	{
		return runeStarts;
	}

	public String[] getFailPhrases()
	{
		return runeFails;
	}

	protected static final String[] negativeAdjectives = new String[]
	{
		"dark", "vile", "bad", "evil"
	};

	protected static final String[] positiveAdjectives = new String[]
	{
		"light", "blessed", "good", "positive"
	};

	protected String getFTAdjective(final boolean positive)
	{
		if(positive)
			return positiveAdjectives[CMLib.dice().roll(1, positiveAdjectives.length, -1)];
		else
			return negativeAdjectives[CMLib.dice().roll(1, negativeAdjectives.length, -1)];
	}

	protected static final String[] negativeVerb = new String[]
	{
		"avoid","stay clear","be wary of","deny"
	};

	protected static final String[] positiveVerb = new String[]
	{
		"embrace","accept","welcome","endure","do not fear"
	};

	protected static final String[] neutralVerb = new String[]
	{
		"be mindful of","anticipate wildness of"
	};

	protected String getFTVerb(final boolean positive)
	{
		String str;
		if(CMLib.dice().rollPercentage()<10)
			str=neutralVerb[CMLib.dice().roll(1, neutralVerb.length, -1)];
		else
		if(positive)
			str=positiveVerb[CMLib.dice().roll(1, positiveVerb.length, -1)];
		else
			str=negativeVerb[CMLib.dice().roll(1, negativeVerb.length, -1)];
		return CMStrings.capitalizeAndLower(str);
	}

	protected String getBestTimeDenom(final long hours, final TimeClock C)
	{
		if(hours < C.getHoursInDay()*2)
			return L("@x1 hours",""+hours);
		else
		if(((hours < C.getHoursInDay()*C.getDaysInWeek()*2)
				&&(C.getDaysInWeek()>1))
		||((hours < C.getHoursInDay()*C.getDaysInMonth()*2)
				&&(C.getDaysInWeek()<=1)))
		{
			final long days = hours / C.getHoursInDay();
			return L("@x1 days",""+days);
		}
		else
		if((hours < C.getHoursInDay()*C.getDaysInMonth()*2)&&(C.getDaysInWeek()>1))
		{
			final long weeks = hours / (C.getHoursInDay() * C.getDaysInWeek());
			return L("@x1 weeks",""+weeks);
		}
		else
		if((hours < C.getHoursInDay()*C.getDaysInMonth()*C.getMonthsInYear()*2))
		{
			final long months = hours / (C.getHoursInDay() * C.getDaysInMonth());
			return L("@x1 months",""+months);
		}
		else
		{
			final long years = hours / (C.getHoursInDay() * C.getDaysInMonth()*C.getMonthsInYear());
			return L("@x1 years",""+years);
		}
	}

	protected String getFTTime(final TimeClock nextC, final TimeClock nowC, final TimeClock expireC)
	{
		if((invoker() != null)
		&&(invoker() != affected))
		{
			final Ability A = invoker().fetchAbility(ID());
			if((A!=null)&&(!A.proficiencyCheck(invoker(), 0, false)))
			{
				if(expireC != null)
					return L("for the next @x1",""+getBestTimeDenom(expireC.deriveMudHoursAfter(nowC), expireC));
				return L("within the next @x1",""+getBestTimeDenom(nextC.deriveMudHoursAfter(nowC), nextC));
			}
		}
		if(expireC != null)
			return L("until @x1",expireC.getShortTimeDescription());
		return L("on @x1",nextC.getShortTimeDescription());
	}

	public String generateEventPrediction(final MOB invokerM, final MOB mob)
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<XMLLibrary.XMLTag> predictionRoot = (List<XMLLibrary.XMLTag>)Resources.getResource("SYSTEM_PREDICTION_SCRIPTS");
			if(predictionRoot == null)
			{
				final CMFile file = new CMFile(Resources.buildResourcePath("skills/predictions.xml"),null);
				if(!file.canRead())
					throw new CMException(L("Random data file '@x1' not found.  Aborting.",file.getCanonicalPath()));
				final StringBuffer xml = file.textUnformatted();
				predictionRoot = CMLib.xml().parseAllXML(xml);
				Resources.submitResource("SYSTEM_PREDICTION_SCRIPTS", predictionRoot);
			}
			String s=null;
			String summary=null;
			for(int i=0;i<10;i++)
			{
				final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
				CMLib.percolator().buildDefinedIDSet(predictionRoot,definedIDs, new XTreeSet<String>(definedIDs.keys()));
				final XMLTag piece=(XMLTag)definedIDs.get("RANDOM_PREDICTION");
				if(piece == null)
					throw new CMException(L("Predictions not found.  Aborting."));
				CMLib.percolator().preDefineReward(piece, definedIDs);
				CMLib.percolator().defineReward(piece,definedIDs);
				s=CMLib.percolator().findString("STRING", piece, definedIDs);
				if((s==null)||(s.trim().length()==0))
					throw new CMException(L("Predictions not generated."));
				final ScriptingEngine testE = (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				testE.setScript(s);
				if(!testE.isFunc("Prediction"))
					throw new CMException(L("Prediction corrupt."));
				final MPContext ctx = new MPContext(mob, mob, mob, null, null, null, mob.Name(), null);
				summary = testE.callFunc("Prediction", s, ctx);
				if((summary != null)&&(summary.trim().length()>0))
					break;
				// try again!
			}
			final TimeClock predictionClock = (TimeClock)CMLib.time().homeClock(mob).copyOf();
			switch(CMLib.dice().roll(1, 3, 0))
			{
			case 1:
				break;
			case 2:
				predictionClock.bumpMonths(CMLib.dice().roll(1, 10, 3));
				break;
			case 3:
				predictionClock.bumpYears(CMLib.dice().roll(1, 10, 3));
				predictionClock.bumpMonths(CMLib.dice().roll(1, 10, 3));
				break;
			}
			predictionClock.bumpDays(CMLib.dice().roll(1, 10, 3));
			predictionClock.bumpHours(CMLib.dice().roll(1, 10, 3));
			final Ability effA = CMClass.getAbility("ScriptLater");
			effA.invoke(invokerM, new XVector<String>(mob.Name(), predictionClock.toTimePeriodCodeString(), s),mob,true,0);
			return summary;
		}
		catch(final CMException e)
		{
			Log.errOut(e.getMessage());
			return null;
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final MOB iM = invoker();
		if(affected == iM)
		{
			if((iM==null)
			||(iM.isInCombat())
			||(!CMLib.flags().isInTheGame(iM, true)))
			{
				unInvoke();
				return false;
			}
			if((forM == null)
			||(!CMLib.flags().isInTheGame(forM, true))
			||(forM.location() != iM.location()))
			{
				if(forM != null)
					iM.tell(L("I guess @x1 didn't really care.",forM.name()));
				else
					iM.tell(L("I guess nobody cares."));
				unInvoke();
				return false;
			}
			tickUp++;
			if(tickUp >= 3)
			{
				if((this.reports == null)||(this.reports.size()==0))
				{
					CMLib.commands().postSay(iM, forM, L("That is your future, <T-NAME>."));
					unInvoke();
					return false;
				}
				CMLib.commands().postSay(iM, forM, this.reports.remove(0));
			}
			else
			if(tickUp >= 2)
			{
				final int numToReturn = 1 + getXTIMELevel(iM);
				final AutoProperties[] APs = Thief_Runecasting.getApplicableAward(forM, runePlayerFilter, numToReturn);
				if((APs == null) || (APs.length==0))
				{
					final int x =CMLib.dice().roll(1, getFailPhrases().length, 0);
					CMLib.commands().postSay(iM, forM, L(getFailPhrases()[x]));
					unInvoke();
					return false;
				}
				else
				{
					final TimeClock nowC = CMLib.time().homeClock(forM);
					this.reports = new XVector<String>();
					for(final AutoProperties P : APs)
					{
						final TimeClock C = CMLib.masking().dateMaskToNextTimeClock(forM, P.getDateCMask());
						final boolean isNow = CMLib.masking().maskCheck(P.getDateCMask(), forM, true);
						final TimeClock expireC = isNow ? CMLib.masking().dateMaskToExpirationTimeClock(forM, P.getDateCMask()) : null;
						for(final Pair<String, String> ps : P.getProps())
						{
							final Ability A = CMClass.getAbility(ps.first);
							if(A != null)
							{
								final MOB M = CMClass.getFactoryMOB();
								M.recoverCharStats();
								M.recoverMaxState();
								M.recoverPhyStats();
								final CharStats cStats = (CharStats)M.charStats().copyOf();
								final CharState cState = (CharState)M.curState().copyOf();
								final PhyStats pStats = (PhyStats)M.phyStats().copyOf();
								A.setAffectedOne(M);
								A.setMiscText(ps.second);
								A.affectCharState(M, M.curState());
								A.affectCharStats(M, M.charStats());
								A.affectPhyStats(M, M.phyStats());
								final String format = "I see @x1 @x2. @x3 @x4 @x5";
								for(final int cd : CharStats.CODES.ALLCODES())
								{
									final int diff = M.charStats().getStat(cd) - cStats.getStat(cd);
									if(diff != 0)
									{
										final boolean positive = diff > 0;
										final String codeName;
										if(CMLib.dice().rollPercentage()<(10*this.getXLEVELLevel(iM)))
											codeName = L(CharStats.CODES.DESC(cd).toLowerCase())+"("+Math.abs(diff)+")";
										else
											codeName = L(CharStats.CODES.DESC(cd).toLowerCase());
										final String report = L(format,L(getFTAdjective(positive)),L(A.name().toLowerCase()),
															L(getFTVerb(positive)), codeName, getFTTime(C,nowC,expireC));
										this.reports.add(report);
									}
								}
								for(int cd=0; cd<CharState.STAT_NUMSTATS;cd++)
								{
									final int diff = M.curState().getStat(cd) - cState.getStat(cd);
									if(diff != 0)
									{
										final boolean positive = diff > 0;
										final String codeName;
										if(CMLib.dice().rollPercentage()<(10*this.getXLEVELLevel(iM)))
											codeName = L(CharState.STAT_DESCS[cd]).toLowerCase()+"("+Math.abs(diff)+")";
										else
											codeName = L(CharState.STAT_DESCS[cd]).toLowerCase();
										final String report = L(format,L(getFTAdjective(positive)),L(A.name().toLowerCase()),
												L(getFTVerb(positive)), codeName, getFTTime(C,nowC,expireC) );
										this.reports.add(report);
									}
								}
								for(int cd=0; cd<PhyStats.NUM_STATS;cd++)
								{
									final int diff = M.phyStats().getStat(cd) - pStats.getStat(cd);
									if(diff != 0)
									{
										boolean positive = diff > 0;
										if(cd == PhyStats.STAT_ARMOR)
											positive = !positive;
										if(cd == PhyStats.STAT_DISPOSITION)
										{
											for(int i=0;i<PhyStats.IS_DESCS.length;i++)
												if(CMath.isSet(pStats.getStat(cd), i))
												{
													final String codeName=PhyStats.IS_VERBS[i].toLowerCase();
													final String report = L(format,L(getFTAdjective(positive)),L(A.name().toLowerCase()),
															L(getFTVerb(positive)), codeName, getFTTime(C,nowC,expireC) );
													this.reports.add(report);
												}
										}
										else
										if(cd == PhyStats.STAT_SENSES)
										{
											for(int i=0;i<PhyStats.CAN_SEE_DESCS.length;i++)
												if(CMath.isSet(pStats.getStat(cd), i))
												{
													final String codeName=PhyStats.CAN_SEE_DESCS[i].toLowerCase();
													final String report = L(format,L(getFTAdjective(positive)),L(A.name().toLowerCase()),
															L(getFTVerb(positive)), codeName, getFTTime(C,nowC,expireC) );
													this.reports.add(report);
												}
										}
										else
										{
											final String codeName;
											if(CMLib.dice().rollPercentage()<(10*this.getXLEVELLevel(iM)))
												codeName =L(PhyStats.STAT_DESCS[cd].toLowerCase())+"("+Math.abs(diff)+")";
											else
												codeName = L(PhyStats.STAT_DESCS[cd].toLowerCase());
											final String report = L(format,L(getFTAdjective(positive)),L(A.name().toLowerCase()),
													L(getFTVerb(positive)), codeName, getFTTime(C,nowC,expireC) );
											this.reports.add(report);
										}
									}
								}
							}
						}
					}
					if(reports.size()==0)
					{
						final int x =CMLib.dice().roll(1, getFailPhrases().length, 0);
						CMLib.commands().postSay(iM, forM, L(getFailPhrases()[x]));
						unInvoke();
						return false;
					}
					if(CMLib.dice().roll(1, 10, 0) < super.getXLEVELLevel(iM)/3)
					{
						final String finalPrediction = generateEventPrediction(iM, forM);
						if((finalPrediction != null)&&(finalPrediction.trim().length()>0))
							this.reports.add(finalPrediction);
					}
					final int x =CMLib.dice().roll(1, getStartPhrases().length, 0);
					CMLib.commands().postSay(iM, forM, L(getStartPhrases()[x]));
				}
			}
			else
			{
				CMLib.commands().postSay(iM, forM, L("I see..."));
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source() == affected)
		&&(affected == invoker())
		&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_QUIT)
			||(msg.sourceMinor()==CMMsg.TYP_RECALL))
			unInvoke();
	}

	protected static AutoProperties[] getApplicableAward(final MOB mob, final Filterer<AutoProperties> playerFilter,
														 final int num)
	{
		final Map<CompiledZMask,Boolean> playerTried = new HashMap<CompiledZMask,Boolean>();
		final Map<AutoProperties, TimeClock> clocks = new HashMap<AutoProperties, TimeClock>();
		final Set<AutoProperties> awards = new TreeSet<AutoProperties>(new Comparator<AutoProperties>() {
			@Override
			public int compare(final AutoProperties o1, final AutoProperties o2)
			{
				final TimeClock c1 = clocks.get(o1);
				final TimeClock c2 = clocks.get(o2);
				if(c1.isEqual(c2))
					return 0;
				if(c1.isBefore(c2))
					return -1;
				return 1;
			}
		});
		for(final Enumeration<AutoProperties> p = CMLib.awards().getAutoProperties();p.hasMoreElements();)
		{
			final AutoProperties P = p.nextElement();
			if((P.getProps() != null)
			&&(P.getProps().length>0))
			{
				Boolean playerCheck;
				if((P.getPlayerCMask() == null) || (P.getPlayerCMask().empty()))
					playerCheck=Boolean.TRUE;
				else
				{
					playerCheck = playerTried.get(P.getPlayerCMask()); // for this specific test run, only do a mask on target once
					if(playerCheck == null)
					{
						playerCheck = Boolean.valueOf(CMLib.masking().maskCheck(P.getPlayerCMask(), mob, true));
						playerTried.put(P.getPlayerCMask(), playerCheck);
					}
				}
				if(playerCheck.booleanValue()
				&& (playerFilter.passesFilter(P)))
				{
					final TimeClock C = CMLib.masking().dateMaskToNextTimeClock(mob, P.getDateCMask());
					if(C == null)
						continue;
					clocks.put(P, C);
					awards.add(P);
				}
			}
		}
		int ct = 0;
		final List<AutoProperties> winner = new ArrayList<AutoProperties>();
		for(final Iterator<AutoProperties> pi = awards.iterator(); pi.hasNext();)
		{
			final AutoProperties P = pi.next();
			if(ct<num)
			{
				winner.add(P);
				ct++;
			}
			else
			if(CMLib.dice().rollPercentage()<20)
				winner.set(CMLib.dice().roll(1, winner.size(), -1), P);
		}
		if(winner.size()==0)
			return null;
		return winner.toArray(new AutoProperties[winner.size()]);
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	public String getSuccessMsg()
	{
		return L("<S-NAME> cast(s) rune cubes for <T-NAMESELF>...");
	}

	public String getFailureMsg()
	{
		return L("<S-NAME> cast(s) rune cubes for <T-NAMESELF>, but <S-IS-ARE> confused.");
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=super.getTarget(mob, commands, givenTarget, false, false);
		if(target==null)
			return false;

		if((!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.ALLSKILLS))
		&&(doneMs.contains(target))
		&&(!auto))
		{
			final long expirationTime = doneMs.getExpiration(target);
			final TimeClock C = CMLib.time().homeClock(mob);
			if(expirationTime >= System.currentTimeMillis())
			{
				mob.tell(L("You would not be able to see @x1's future again for another @x2.",target.name(mob),
						CMLib.time().date2EllapsedMudTime(C, expirationTime-System.currentTimeMillis(), TimeDelta.SECOND, false)));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob, target,this,CMMsg.MSG_THIEF_ACT,
					auto?L("<T-NAME> has a vision of the future!"):getSuccessMsg());
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Thief_Runecasting rA = (Thief_Runecasting)beneficialAffect(mob, target, asLevel, 0);
				if(rA != null)
				{
					rA.forM = target;
					rA.tickUp = 0;
					final long tm = Math.round(CMath.mul(getExpirationTime(),1.0-CMath.mul(super.getXTIMELevel(mob),0.09)));
					doneMs.add(target, tm);
				}
			}
		}
		else
			beneficialVisualFizzle(mob, target, getFailureMsg());
		return success;
	}
}
