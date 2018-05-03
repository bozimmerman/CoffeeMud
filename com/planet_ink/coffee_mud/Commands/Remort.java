package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.Agent;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementLoadFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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

public class Remort extends StdCommand
{
	public Remort()
	{
	}

	private final String[]	access	= I(new String[] { "REMORT" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}
	
	private enum RemortRetain
	{
		HITPOINT, 
		MANA, 
		MOVE, 
		LEVEL, 
		ATTACK, 
		DEFENSE,
		DAMAGE,
		SKILL,
		SKILLSAT100,
		FACTION,
		EXPERTISE,
		BONUSSTATPOINT,
		QUESTPOINT
	}
	
	private static void recoverEverything(final MOB mob)
	{
		mob.recoverCharStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.recoverPhyStats();
	}

	protected void slowStop(final Session sess, final MOB mob, final PlayerAccount oldAcct) throws IOException
	{
		PlayerStats pStats = mob.playerStats();
		if(pStats != null)
		{
			if(oldAcct != null)
			{
				// gets rid of any gains or changes from the remort process.
				PlayerAccount realAcct = pStats.getAccount();
				if(realAcct != null)
					oldAcct.copyInto(realAcct);
			}
		}
		sess.stopSession(true,true,false);
		CMLib.s_sleep(3000);
		sess.stopSession(true,true,false);
		if(pStats != null)
		{
			pStats.getExtItems().delAllItems(true);
		}
		CMLib.players().delPlayer(mob);
		throw new IOException("Session stopped");
	}
	
	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final Session session=mob.session();
		if(session==null)
			return false;
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return false;

		if(!CMLib.masking().maskCheck(CMProps.getVar(CMProps.Str.REMORTMASK), mob, true))
		{
			mob.tell(L("You do not meet the requirements to re-mortalize at this time."));
			mob.tell(L("The requirements are: @x1",CMLib.masking().maskDesc(CMProps.getVar(CMProps.Str.REMORTMASK))));
			return false;
		}
		
		final int[] newLevel = new int[] { 1 };
		final int[] newMana = new int[] { CMProps.getIntVar(CMProps.Int.STARTMANA) };
		final int[] newDamage = new int[] { 0 };
		final int[] newHp = new int[] { CMProps.getIntVar(CMProps.Int.STARTHP) };
		final int[] newMove = new int[] { CMProps.getIntVar(CMProps.Int.STARTMOVE) };
		final int[] newAttack = new int[] { 0 };
		final int[] newDefense = new int[] { 100 };
		final int[] bonusPointsPerStat = new int[] { 0 };
		final int[] questPoint = new int[] { 0 };
		final List<Pair<String,Integer>> factions=new ArrayList<Pair<String,Integer>>();
		final List<Triad<String,String,Integer>> abilities=new ArrayList<Triad<String,String,Integer>>();
		final List<Triad<String,String,Integer>> abilities100=new ArrayList<Triad<String,String,Integer>>();
		final List<String> expertises=new ArrayList<String>();
		final List<String> allRetains=CMParms.parseCommas(CMProps.getVar(CMProps.Str.REMORTRETAIN), true);
		final int retainRace = allRetains.indexOf("RACE");
		if(retainRace >=0)
			allRetains.remove(retainRace);
		final int retainGender = allRetains.indexOf("GENDER");
		if(retainGender >=0)
			allRetains.remove(retainGender);
		final int retainCharClass = allRetains.indexOf("CHARCLASS");
		if(retainCharClass >=0)
			allRetains.remove(retainCharClass);
		final int retainStats = allRetains.indexOf("STATS");
		if(retainStats >=0)
			allRetains.remove(retainStats);
		for(String thing : allRetains)
		{
			thing=thing.toUpperCase().trim();
			RemortRetain retainer = (RemortRetain)CMath.s_valueOf(RemortRetain.class, thing);
			String rest="";
			if(retainer == null)
			{
				for(RemortRetain r : RemortRetain.values())
				{
					if(thing.endsWith(r.name()))
					{
						retainer = r;
						rest = thing.substring(0,thing.length()-r.name().length()).trim();
					}
					else
					if(thing.endsWith(r.name()+"S"))
					{
						retainer = r;
						rest = thing.substring(0,thing.length()-r.name().length()-1).trim();
					}
					else
					if(thing.startsWith(r.name()))
					{
						retainer = r;
						rest = thing.substring(r.name().length()).trim();
					}
					else
					if(thing.startsWith(r.name()+"S"))
					{
						retainer = r;
						rest = thing.substring(r.name().length()+1).trim();
					}
				}
			}
			if(retainer != null)
			{
				int flatAmount=0;
				double pctAmount = 1.0;
				if(CMath.isPct(rest))
				{
					pctAmount = CMath.s_pct(rest);
				}
				else
				if(CMath.isInteger(rest))
				{
					flatAmount = CMath.s_int(rest);
				}
				else
				if(CMath.isNumber(rest))
				{
					double d=CMath.s_double(rest);
					if((d>0.0)&&(d<1.0))
						pctAmount = d;
					else
						pctAmount = d/100.0;
				}
				switch(retainer)
				{
				case ATTACK:
				{
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, mob.basePhyStats().attackAdjustment()));
					total += flatAmount;
					newAttack[0] += total;
					break;
				}
				case QUESTPOINT:
				{
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, mob.getQuestPoint()));
					total += flatAmount;
					questPoint[0] += total;
					break;
				}
				case DEFENSE:
				{
					int total = 0;
					if(pctAmount != 0)
					{
						int armorDiff = 0;
						if(mob.basePhyStats().armor() < 0)
							armorDiff =  (-mob.basePhyStats().armor()) + 100;
						else
							armorDiff = 100 - mob.basePhyStats().armor();
						total -= (int)Math.round(CMath.mul(pctAmount, armorDiff));
					}
					total -= flatAmount;
					newDefense[0] += total;
					break;
				}
				case HITPOINT:
				{
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, mob.baseState().getHitPoints())) - CMProps.getIntVar(CMProps.Int.STARTHP);
					total += flatAmount;
					newHp[0] += total;
					break;
				}
				case LEVEL:
				{
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, mob.basePhyStats().level())) - 1;
					total += flatAmount;
					if(total > 1)
						newLevel[0] = total;
					break;
				}
				case MANA:
				{
					int total = 0;
					if(pctAmount != 0)
						total = (int)Math.round(CMath.mul(pctAmount, mob.baseState().getMana())) - CMProps.getIntVar(CMProps.Int.STARTMANA);
					total += flatAmount;
					newMana[0] += total;
					break;
				}
				case DAMAGE:
				{
					int total = 0;
					if(pctAmount != 0)
						total = (int)Math.round(CMath.mul(pctAmount, mob.basePhyStats().damage()));
					total += flatAmount;
					newDamage[0] += total;
					break;
				}
				case MOVE:
				{
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, mob.baseState().getMovement())) - CMProps.getIntVar(CMProps.Int.STARTMOVE);
					total += flatAmount;
					newMove[0] += total;
					break;
				}
				case SKILL:
				{
					for(Enumeration<Ability> a=mob.abilities();a.hasMoreElements();)
					{
						Ability A=a.nextElement();
						abilities.add(new Triad<String,String,Integer>(A.ID(),A.text(),new Integer(A.proficiency())));
					}
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, abilities.size()));
					total += flatAmount;
					while(abilities.size()>total)
						abilities.remove(CMLib.dice().roll(1, abilities.size(), -1));
					break;
				}
				case SKILLSAT100:
				{
					for(Enumeration<Ability> a=mob.abilities();a.hasMoreElements();)
					{
						Ability A=a.nextElement();
						if(A.proficiency() >= 100)
							abilities100.add(new Triad<String,String,Integer>(A.ID(),A.text(),new Integer(A.proficiency())));
					}
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, abilities.size()));
					total += flatAmount;
					while(abilities100.size()>total)
						abilities100.remove(CMLib.dice().roll(1, abilities100.size(), -1));
					break;
				}
				case FACTION:
				{
					for(Enumeration<String> f = mob.factions();f.hasMoreElements();)
					{
						String facID = f.nextElement();
						factions.add(new Pair<String,Integer>(facID,new Integer(mob.fetchFaction(facID))));
					}
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, factions.size()));
					total += flatAmount;
					while(factions.size()>total)
						factions.remove(CMLib.dice().roll(1, factions.size(), -1));
					break;
				}
				case EXPERTISE:
				{
					for(Enumeration<String> f = mob.expertises();f.hasMoreElements();)
					{
						String expID = f.nextElement();
						expertises.add(expID);
					}
					int total = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, expertises.size()));
					total += flatAmount;
					while(expertises.size()>total)
						expertises.remove(CMLib.dice().roll(1, expertises.size(), -1));
					break;
				}
				case BONUSSTATPOINT:
				{
					
					int total = 0;
					int totalnow = 0;
					for(int stat : CharStats.CODES.BASECODES())
						totalnow += mob.baseCharStats().getStat(stat);
					int startStat=CMProps.getIntVar(CMProps.Int.STARTSTAT);
					if((CMSecurity.isDisabled(CMSecurity.DisFlag.ATTRIBS)&&(startStat<=0)))
						totalnow -= (10 * CharStats.CODES.BASECODES().length);
					else
					{
						totalnow -= CMProps.getIntVar(CMProps.Int.BASEMINSTAT) * CharStats.CODES.BASECODES().length;
						totalnow -= CMLib.login().getTotalBonusStatPoints(pstats, pstats.getAccount());
					}
					if(totalnow < 0)
						totalnow = 0;
					if(pctAmount != 0)
						total += (int)Math.round(CMath.mul(pctAmount, totalnow));
					total += flatAmount;
					bonusPointsPerStat[0] += total;
					break;
				}
				}
			}
		}
		
		mob.tell(L("^HThis will drop your level back to @x1!",""+newLevel[0]));
		session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",120000)
		{
			@Override
			public void showPrompt()
			{
				session.promptPrint(L("If that's what you want, re-enter your password: "));
			}

			@Override
			public void timedOut()
			{
			}

			@Override
			public void callBack()
			{
				if(input.trim().length()==0)
					return;
				if(!pstats.matchesPassword(input.trim()))
					mob.tell(L("Password incorrect."));
				else
				{
					Log.sysOut("Remort: "+mob.Name());
					if(mob.numFollowers()>0)
						CMLib.commands().forceStandardCommand(mob, "Nofollow",new XVector<String>("NOFOLLOW","ALL"));
					
					final PlayerStats pStats = mob.playerStats();
					final PlayerAccount oldAccount;
					if((pStats!=null)&&(pStats.getAccount()!=null))
						oldAccount = (PlayerAccount)pStats.getAccount().copyOf();
					else
						oldAccount = null;
					CMLib.achievements().possiblyBumpAchievement(mob, Event.REMORT, 1);
					mob.basePhyStats().setLevel(1);
					mob.basePhyStats().setArmor(newDefense[0]);
					mob.basePhyStats().setDamage(newDamage[0]);
					mob.basePhyStats().setAttackAdjustment(newAttack[0]);
					mob.basePhyStats().setSpeed(1.0);
					mob.baseState().setHitPoints(newHp[0]);
					mob.baseState().setMana(newMana[0]);
					mob.baseState().setMovement(newMove[0]);
					if(pStats!=null)
					{
						final Race R=mob.baseCharStats().getMyRace();
						final Room startR=mob.getStartRoom();
						final TimeClock C=CMLib.time().localClock(startR);
						final int age=pStats.initializeBirthday(C,0,R);
						mob.baseCharStats().setStat(CharStats.STAT_AGE,age);
					}
					for(int code : CharStats.CODES.SAVING_THROWS())
						mob.baseCharStats().setStat(code, 0);
					for(int code : CharStats.CODES.MAXCODES())
						mob.baseCharStats().setStat(code, 0);
					mob.delAllAbilities();
					mob.delAllBehaviors();
					mob.delAllScripts();
					mob.delAllExpertises();
					mob.setExperience(0);
					for(Triad<String,String,Integer> PA : abilities)
					{
						Ability A=CMClass.getAbility(PA.first);
						if(A!=null)
						{
							A.setMiscText(A.text());
							A.setProficiency(0);
							mob.addAbility(A);
						}
					}
					for(Triad<String,String,Integer> PA : abilities100)
					{
						Ability A=CMClass.getAbility(PA.first);
						if(A!=null)
						{
							A.setMiscText(A.text());
							A.setProficiency(100);
							mob.addAbility(A);
						}
					}
					for(Pair<String,Integer> PA : factions)
					{
						mob.addFaction(PA.first, PA.second.intValue());
					}
					for(String PA : expertises)
					{
						mob.addExpertise(PA);
					}
					List<Achievement> reAwardTattoos = new LinkedList<Achievement>();
					List<Tattoo> delTattoo =  new LinkedList<Tattoo>();
					for(Enumeration<Tattoo> t=mob.tattoos();t.hasMoreElements();)
					{
						Tattoo T=t.nextElement();
						if(T != null)
						{
							Achievement A=CMLib.achievements().getAchievement(T.getTattooName());
							if((A != null) && (A.getAgent() == Agent.PLAYER))
								reAwardTattoos.add(A);
							else
								delTattoo.add(T);
						}
						else
							delTattoo.add(T);
					}
					for(Iterator<Tattoo> t=delTattoo.iterator();t.hasNext();)
						mob.delTattoo(t.next());
					mob.setStartRoom(CMLib.login().getDefaultStartRoom(mob));
					mob.getStartRoom().bringMobHere(mob, true);
					final String failsafeID = "RemoteFailSafe";
					MsgListener finishRemoteListener = new MsgListener()
					{
						final MsgListener me=this;
						
						@Override
						public void executeMsg(Environmental myHost, CMMsg msg)
						{
							if((msg.source() == myHost)
							&&(myHost instanceof MOB)
							&&(msg.sourceMinor()==CMMsg.TYP_LIFE))
							{
								int tryTheme = mob.playerStats().getTheme();
								if((tryTheme < 0)&&(mob.location()!=null))
									tryTheme=mob.location().getArea().getTheme();
								if((CMath.numberOfSetBits(tryTheme&Area.THEME_ALLTHEMES)) > 1)
								{
									if((tryTheme&Area.THEME_FANTASY) != 0)
										tryTheme = Area.THEME_FANTASY;
									else
									if((tryTheme&Area.THEME_TECHNOLOGY) != 0)
										tryTheme = Area.THEME_TECHNOLOGY;
								}
								final int theme=tryTheme;
								Runnable remortRun = new Runnable()
								{
									@Override
									public void run()
									{
										final String[] PCODE_RESTORE={
											"BONUSCHARSTATS"
										};
										final PlayerStats pStats = mob.playerStats();
										final PlayerStats oldPStats =(PlayerStats)pStats.copyOf();
										try
										{
											pStats.setSavable(false); // protect vulnerable weakling from saves so restore works
											final Session sess = mob.session();
											mob.baseCharStats().setMyClasses("StdCharClass");
											mob.baseCharStats().setMyLevels("1");
											mob.basePhyStats().setLevel(1);
											for (final Enumeration<Ability> a = mob.personalEffects(); a.hasMoreElements();)
											{
												final Ability A = a.nextElement();
												if(A!=null)
												{
													if (A.canBeUninvoked()
													||(A.isNowAnAutoEffect()))
													{
														A.unInvoke();
														mob.delEffect(A);
													}
													
												}
											}
											Ability oldFailSafeA=mob.fetchEffect(failsafeID);
											if(oldFailSafeA!=null)
												mob.delEffect(oldFailSafeA);
											ExtendableAbility failsafeA=(ExtendableAbility)CMClass.getAbility("ExtAbility");
											if(failsafeA!=null)
											{
												failsafeA.setAbilityID(failsafeID);
												failsafeA.setMsgListener(me);
												failsafeA.setSavable(false);
												mob.addNonUninvokableEffect(failsafeA);
											}
											recoverEverything(mob);
											if(retainRace < 0)
											{
												try
												{
													mob.baseCharStats().setMyRace(CMLib.login().promptRace(theme, mob, mob.session()));
												}
												catch(Throwable x)
												{
													sess.stopSession(true, true, false);
												}
												if(sess.isStopped())
												{
													slowStop(sess,mob,oldAccount);
												}
												recoverEverything(mob);
											}
											if(retainGender < 0)
											{
												try
												{
													mob.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.login().promptGender(theme, mob, mob.session()));
												}
												catch(Throwable x)
												{
													sess.stopSession(true, true, false);
												}
												if(sess.isStopped())
												{
													slowStop(sess,mob,oldAccount);
												}
												recoverEverything(mob);
											}
											mob.setPractices(0);
											mob.setTrains(0);
											CMLib.achievements().reloadPlayerAwards(mob,AchievementLoadFlag.REMORT_PRELOAD);
											if(retainStats < 0)
											{
												// loadAccountAchievements already done by crcrinit in promptplayerstats
												try
												{
													CMLib.login().promptBaseCharStats(theme, mob, 300, mob.session(), bonusPointsPerStat[0]);
												}
												catch(Throwable x)
												{
													sess.stopSession(true, true, false);
												}
												if(sess.isStopped())
												{
													slowStop(sess,mob,oldAccount);
												}
												recoverEverything(mob);
											}
											else
												CMLib.achievements().loadAccountAchievements(mob,AchievementLoadFlag.REMORT_PRELOAD);
											for(String prevCode : PCODE_RESTORE)
												pStats.setStat(prevCode, oldPStats.getStat(prevCode));
											mob.basePhyStats().setSensesMask(0);
											mob.baseCharStats().getMyRace().startRacing(mob,false);
											mob.setWimpHitPoint(5);
											mob.setQuestPoint(questPoint[0]);
											recoverEverything(mob);
											if(retainCharClass < 0)
											{
												try
												{
													mob.baseCharStats().setCurrentClass(CMLib.login().promptCharClass(theme, mob, mob.session()));
												}
												catch(Throwable x)
												{
													sess.stopSession(true, true, false);
												}
												if(sess.isStopped())
												{
													slowStop(sess,mob,oldAccount);
												}
												recoverEverything(mob);
											}
											for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
												mob.playerStats().unVisit(a.nextElement());
											mob.baseCharStats().getCurrentClass().startCharacter(mob, false, false);
											mob.baseCharStats().getCurrentClass().grantAbilities(mob, false);
											recoverEverything(mob);
											recoverEverything(mob);
											CMLib.achievements().reloadPlayerAwards(mob,AchievementLoadFlag.REMORT_POSTLOAD);
											CMLib.achievements().loadAccountAchievements(mob,AchievementLoadFlag.REMORT_POSTLOAD);
											CMLib.achievements().loadPlayerSkillAwards(mob, mob.playerStats());
											CMLib.commands().postLook(mob, true);
											if((!mob.charStats().getCurrentClass().leveless())
											&&(!mob.charStats().isLevelCapped(mob.charStats().getCurrentClass()))
											&&(!mob.charStats().getMyRace().leveless())
											&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
											{
												for(int i=1;i<newLevel[0];i++)
												{
													if((mob.getExpNeededLevel()==Integer.MAX_VALUE)
													||(mob.charStats().getCurrentClass().expless())
													||(mob.charStats().getMyRace().expless()))
														CMLib.leveler().level(mob);
													else
														CMLib.leveler().postExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
												}
											}
											recoverEverything(mob);
											CMLib.utensils().confirmWearability(mob);
											recoverEverything(mob);
											mob.tell(L("You have remorted back to level @x1!",""+mob.phyStats().level()));
											Ability A=mob.fetchEffect(failsafeID);
											if(A!=null)
												mob.delEffect(A);
											CMLib.database().DBUpdatePlayer(mob);
										}
										catch(IOException e)
										{
										}
										catch(Exception e)
										{
											Log.errOut(e);
										}
										finally
										{
											pStats.setSavable(true);
										}
									}
								};
								if(msg.sourceMajor(CMMsg.MASK_CNTRLMSG))
									remortRun.run();
								else
									CMLib.threads().scheduleRunnable(remortRun,500);
							}
						}
							
						@Override
						public boolean okMessage(Environmental myHost, CMMsg msg)
						{
							return true;
						}
					};
					ExtendableAbility failsafeA=(ExtendableAbility)CMClass.getAbility("ExtAbility");
					if(failsafeA!=null)
					{
						failsafeA.setAbilityID(failsafeID);
						failsafeA.setMsgListener(finishRemoteListener);
						failsafeA.setSavable(false);
						mob.addNonUninvokableEffect(failsafeA);
					}
					finishRemoteListener.executeMsg(mob, CMClass.getMsg(mob, CMMsg.MSG_BRINGTOLIFE | CMMsg.MASK_CNTRLMSG, null));
				}
			}
		});
		return false;
	}
	
	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}
}
