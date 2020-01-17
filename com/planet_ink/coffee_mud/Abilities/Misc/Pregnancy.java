package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2020 Bo Zimmerman

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
public class Pregnancy extends StdAbility implements HealthCondition
{
	@Override
	public String ID()
	{
		return "Pregnancy";
	}

	private final static String	localizedName	= CMLib.lang().L("Pregnancy");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected long	monthsRemaining	= -1;
	protected long	daysRemaining	= -1;

	@Override
	public String getHealthConditionDesc()
	{
		final long start = getPregnancyStartTime();
		if (start >= 0)
		{
			final TimeClock C = CMLib.time().localClock(affected);
			final long divisor = CMProps.getTickMillis() * CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
			final long days = (System.currentTimeMillis() - start) / divisor; // down
																				// to
																				// days;
			final long months = days / C.getDaysInMonth();
			if (days < 1)
				return "less than 1 day pregnant";
			else
			if (months < 1)
				return days + " day(s) pregnant";
			else
				return months + " month(s) pregnant";
		}
		return "";
	}

	protected long getPregnancyStartTime()
	{
		final String text = text();
		final int x = text.indexOf('/');
		if (x > 0)
		{
			final int y = text.indexOf('/', x + 1);
			if (y >= 0)
				return CMath.s_long(text.substring(0, x));
		}
		return -1;
	}

	protected void setPregnancyStartTime(final long newTime)
	{
		final String text = text();
		final int x = text.indexOf('/');
		if (x > 0)
		{
			final int y = text.indexOf('/', x + 1);
			if (y >= 0)
				this.setMiscText(newTime + text.substring(x));
		}
	}

	protected long getPregnancyEndTime()
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y >= 0)
				return CMath.s_long(text().substring(x + 1, y));
		}
		return -1;
	}

	protected void setPregnancyEndTime(final long newTime)
	{
		final String text = text();
		final int x = text.indexOf('/');
		if (x > 0)
		{
			final int y = text.indexOf('/', x + 1);
			if (y >= 0)
				this.setMiscText(text.substring(0, x + 1) + newTime + text.substring(y));
		}
	}

	protected String getFathersName()
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y > x)
			{
				final int z = text().indexOf('/', y + 1);
				if (z > y)
					return text().substring(y + 1, z);
			}
		}
		return "";
	}

	protected void setFathersName(final String name)
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y > x)
			{
				final int z = text().indexOf('/', y + 1);
				if (z > y)
					super.setMiscText(text().substring(0,y+1)+name+text().substring(z));
			}
		}
	}

	protected String getFathersRace()
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y > x)
			{
				final int z = text().indexOf('/', y + 1);
				if (z > y)
				{
					final String race2 = text().substring(z + 1).trim();
					if((race2.length()>2)
					&& Character.isDigit(race2.charAt(0))
					&& race2.charAt(1)=='X')
						return race2.substring(2);
					else
						return race2;
				}
			}
		}
		return "";
	}

	protected void setFathersRace(final String raceID)
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y > x)
			{
				final int z = text().indexOf('/', y + 1);
				if (z > y)
				{
					final int numKids = this.getNumKids();
					final String race2;
					if(numKids > 1)
						race2=numKids+"X"+raceID;
					else
						race2=raceID;
					super.setMiscText(text().substring(0,z+1)+race2);
				}
			}
		}
	}

	protected int getNumKids()
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y > x)
			{
				final int z = text().indexOf('/', y + 1);
				if (z > y)
				{
					final String race2 = text().substring(z + 1).trim();
					if((race2.length()>2)
					&& Character.isDigit(race2.charAt(0))
					&& race2.charAt(1)=='X')
						return CMath.s_int(race2.substring(0,1));
				}
			}
		}
		return 1;
	}

	protected void setNumKids(final int num)
	{
		final int x = text().indexOf('/');
		if (x > 0)
		{
			final int y = text().indexOf('/', x + 1);
			if (y > x)
			{
				final int z = text().indexOf('/', y + 1);
				if (z > y)
				{
					String race2 = text().substring(z + 1).trim();
					if((race2.length()>2)
					&& Character.isDigit(race2.charAt(0))
					&& race2.charAt(1)=='X')
						race2=race2.substring(2);
					if(num > 1)
						race2=num+"X"+race2;
					super.setMiscText(text().substring(0,z+1)+race2);
				}
			}
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(code != null)
		{
			if (code.equalsIgnoreCase("PREGSTART"))
				this.setPregnancyStartTime(CMath.s_long(val));
			else
			if (code.equalsIgnoreCase("PREGEND"))
				this.setPregnancyEndTime(CMath.s_long(val));
			else
			if (code.equalsIgnoreCase("FATHERNAME"))
				this.setFathersName(val);
			else
			if (code.equalsIgnoreCase("FATHERRACE"))
				this.setFathersRace(val);
			else
			if (code.equalsIgnoreCase("NUMBABIES"))
				this.setNumKids(Math.min(9, CMath.s_int(val)));
			else
				super.setStat(code, val);
		}
	}

	@Override
	public String getStat(final String code)
	{
		if(code != null)
		{
			if (code.equalsIgnoreCase("PREGSTART"))
				return "" + this.getPregnancyStartTime();
			else
			if (code.equalsIgnoreCase("PREGEND"))
				return "" + this.getPregnancyEndTime();
			else
			if (code.equalsIgnoreCase("MOTHERNAME"))
				return (affected == null) ? "" : affected.Name();
			else
			if (code.equalsIgnoreCase("MOTHERRACE"))
				return (!(affected instanceof MOB)) ? "" : ((MOB)affected).baseCharStats().getMyRace().ID();
			else
			if (code.equalsIgnoreCase("FATHERNAME"))
				return "" + this.getFathersName();
			else
			if (code.equalsIgnoreCase("FATHERRACE"))
				return "" + this.getFathersRace();
			else
			if (code.equalsIgnoreCase("NUMBABIES"))
				return "" + this.getNumKids();
			else
				return super.getStat(code);
		}
		return "";
	}

	@Override
	public String displayText()
	{
		final String text = getHealthConditionDesc();
		if (text.length() > 0)
			return "(" + text + ")";
		return "";
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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "IMPREGNATE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return false;
	}

	@Override
	public boolean isSavable()
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	protected int	ticksInLabor	= 0;

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if ((msg.target() == affected)
		&& ((msg.targetMinor() == CMMsg.TYP_LOOK) || (msg.targetMinor() == CMMsg.TYP_EXAMINE))
		&& (CMLib.flags().canBeSeenBy(affected, msg.source()))
		&& (affected instanceof MOB)
		&& ((daysRemaining > 0) && (monthsRemaining <= 3)))
		{
			msg.addTrailerMsg(CMClass.getMsg((MOB)affected, null, null, CMMsg.MSG_OK_VISUAL, L("\n\r<S-NAME> <S-IS-ARE> obviously with child.\n\r"), CMMsg.NO_EFFECT, null, CMMsg.NO_EFFECT, null));
		}
		super.executeMsg(host, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (!super.tick(ticking, tickID))
			return false;
		if ((tickID == Tickable.TICKID_MOB)
		&& (affected instanceof MOB)
		&& (CMLib.flags().isInTheGame((MOB) affected, true)))
		{
			final MOB mob = (MOB) affected;
			final long end=this.getPregnancyEndTime();
			if(end != -1)
			{
				final TimeClock C = CMLib.time().localClock(mob.getStartRoom());
				final long divisor = CMProps.getTickMillis() * CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
				daysRemaining = (end - System.currentTimeMillis()) / divisor; // down to days
				monthsRemaining = daysRemaining / C.getDaysInMonth(); // down to months
				if ((CMLib.dice().roll(1, 200, 0) == 1)
				&&(!CMSecurity.isDisabled(DisFlag.AUTOMOODS)))
				{
					final Ability A = CMClass.getAbility("Mood");
					if (A != null)
						A.invoke(mob, new XVector<String>("RANDOM"), mob, true, 0);
				}
				if (daysRemaining < 7) // BIRTH!
				{
					if (CMLib.flags().isSleeping(mob))
						mob.enqueCommand(CMParms.parse("WAKE"), MUDCmdProcessor.METAFLAG_FORCED, 0);
					if (CMLib.dice().rollPercentage() > 50)
					{
						if (mob.charStats().getStat(CharStats.STAT_INTELLIGENCE) > 5)
							mob.location().show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> moan(s) and scream(s) in labor pain!!"));
						else
							mob.location().show(mob, null, CMMsg.MSG_NOISE, L("<S-NAME> look(s) like <S-HE-SHE> is ready to give birth!!"));
					}
					ticksInLabor++;
					if (ticksInLabor >= 45)
					{
						ticksInLabor = 0;
						final String race1 = mob.baseCharStats().getMyRace().ID();
						String classID="GenMOB";
						if(mob.isGeneric()
						&& (!mob.ID().equalsIgnoreCase(classID))
						&&(!(mob instanceof ShopKeeper)))
							classID=mob.ID();
						final int numKids=this.getNumKids();
						String race2 = this.getFathersRace();
						if((race2==null)||(race2.length()==0))
							race2=mob.baseCharStats().getMyRace().ID();
						String otherParentName = this.getFathersName();
						if((otherParentName!=null)&&(otherParentName.length()==0))
							otherParentName = null;
						MOB otherParentM = null;
						if((otherParentName!=null)&&(otherParentName.length()>0))
							otherParentM=CMLib.players().getLoadPlayer(otherParentName);
						for(int k=0;k<numKids;k++)
						{
							char gender = 'F';
							String sondat = "daughter";
							if (CMLib.dice().rollPercentage() > 50)
							{
								gender = 'M';
								sondat = "son";
							}
							final MOB babe = CMClass.getMOB(classID);
							babe.addTattoo("PARENT:" + mob.Name());
							final String desc;
							if(otherParentName==null)
								desc = L("The " + sondat + " of @x1.",mob.Name());
							else
								desc = L("The " + sondat + " of @x1 and @x2.",mob.Name(),otherParentName);
							if(otherParentM != null)
								babe.addTattoo("PARENT:" + otherParentM.Name());
							if((!mob.isPlayer())
							&&((otherParentM==null)
								||(otherParentName==null)
								||(otherParentName.length()==0)
								||(!CMLib.players().playerExistsAllHosts(otherParentName))))
							{
								babe.addTattoo("PARENTAGE:NPC");
							}

							mob.curState().setMovement(0);
							mob.curState().setHitPoints(mob.curState().getHitPoints() / 2);
							mob.location().show(mob, null, CMMsg.MSG_NOISE, L("***** <S-NAME> !!!GIVE(S) BIRTH!!! ******"));
							if (CMLib.dice().rollPercentage() > 5)
							{
								Ability A = mob.fetchEffect(ID());
								while (A != null)
								{
									mob.delEffect(A);
									A.setAffectedOne(null);
									A = mob.fetchEffect(ID());
								}
								A = mob.fetchAbility(ID());
								while (A != null)
								{
									mob.delAbility(A);
									A.setAffectedOne(null);
									A = mob.fetchAbility(ID());
								}
							}
							final int numRaces = CMClass.numPrototypes(CMObjectType.RACE);
							boolean newRaceGenerated = false;
							Race R = CMLib.utensils().getMixedRace(race1, race2, false);
							if (R == null)
								R = mob.baseCharStats().getMyRace();
							else
							if(numRaces > CMClass.numPrototypes(CMObjectType.RACE))
								newRaceGenerated = true;
							String name = CMLib.english().startWithAorAn(R.makeMobName(gender, 2)).toLowerCase();
							babe.setName(name);
							CMLib.factions().setAlignment(babe, Faction.Align.GOOD);
							if ((mob.isMonster()) && (otherParentM != null) && (!otherParentM.isMonster()))
							{
								for (final Pair<Clan, Integer> p : CMLib.clans().findRivalrousClans(otherParentM))
								{
									final Pair<Clan,Integer> clanRole=otherParentM.getClanRole(p.first.clanID());
									if((clanRole!=null)
									&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
										babe.setClan(p.first.clanID(), p.first.getAutoPosition());
								}
							}
							else
							if(!mob.isMonster())
							{
								for (final Pair<Clan, Integer> p : CMLib.clans().findRivalrousClans(mob))
								{
									final Pair<Clan,Integer> clanRole=mob.getClanRole(p.first.clanID());
									if((clanRole!=null)
									&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
										babe.setClan(p.first.clanID(), p.first.getAutoPosition());
								}
							}
							babe.setLiegeID(mob.getLiegeID());
							babe.setDescription(desc);
							babe.setDisplayText(L("@x1 is here.", name));
							CMLib.beanCounter().clearZeroMoney(babe, null);
							babe.setMoneyVariation(0);
							babe.baseCharStats().setMyRace(R);
							babe.baseCharStats().setMyClasses("Apprentice");
							babe.baseCharStats().setStat(CharStats.STAT_CHARISMA, 10);
							babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION, 6);
							babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY, 2);
							babe.baseCharStats().setStat(CharStats.STAT_GENDER, gender);
							babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE, 2);
							babe.baseCharStats().setStat(CharStats.STAT_STRENGTH, 1);
							babe.baseCharStats().setStat(CharStats.STAT_WISDOM, 1);
							babe.baseCharStats().getMyRace().startRacing(babe, false);
							babe.basePhyStats().setHeight(babe.basePhyStats().height() / 10);
							babe.basePhyStats().setWeight(babe.basePhyStats().weight() / 10);
							babe.baseState().setHitPoints(1);
							babe.baseState().setMana(0);
							babe.baseState().setMovement(0);
							if(CMLib.flags().isAnimalIntelligence(mob)
							&&((otherParentM==null)||(CMLib.flags().isAnimalIntelligence(otherParentM))))
								babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE, 1);
							babe.recoverCharStats();
							Ability retainA=mob.fetchEffect("Prop_Retainable");
							if(retainA!=null)
							{
								String s=retainA.text();
								if(CMLib.flags().isAnimalIntelligence(mob))
								{
									final int xs=s.indexOf(';');
									if(xs>0)
										s=s.substring(0,xs);
								}
								retainA=CMClass.getAbility("Prop_Retainable");
								retainA.setMiscText(s);
								babe.addNonUninvokableEffect(retainA);
							}
							if (!CMLib.flags().isAnimalIntelligence(babe))
							{
								if (CMLib.dice().rollPercentage() > 50)
								{
									Ability A = mob.fetchEffect("Allergies");
									if (A != null)
									{
										A = (Ability) A.copyOf();
										babe.addNonUninvokableEffect(A);
									}
									else
									{
										A = CMClass.getAbility("Allergies");
										if (A != null)
											A.invoke(babe, babe, true, 0);
									}
								}
								final Ability STAT = CMClass.getAbility("Prop_StatTrainer");
								if (STAT != null)
								{
									STAT.setMiscText("CHA=10 CON=6 DEX=2 INT=2 STR=1 WIS=1");
									babe.addNonUninvokableEffect(STAT);
								}
							}
							final Ability SAFE = CMClass.getAbility("Prop_SafePet");
							if (SAFE != null)
								babe.addNonUninvokableEffect(SAFE);
							{
								final Ability AGE = CMClass.getAbility("Age");
								if (AGE != null)
								{
									AGE.setMiscText("" + System.currentTimeMillis());
									babe.addNonUninvokableEffect(AGE);
								}
							}
							babe.recoverCharStats();
							babe.recoverPhyStats();
							babe.recoverMaxState();
							babe.resetToMaxState();
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.BIRTHS, 1, babe);
							if((otherParentM != null) && (otherParentM.isPlayer()))
								CMLib.achievements().possiblyBumpAchievement(otherParentM, AchievementLibrary.Event.BIRTHS, 1, babe);
							if(newRaceGenerated)
							{
								CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.RACEBIRTH, 1, babe);
								if((otherParentM != null) && (otherParentM.isPlayer()))
									CMLib.achievements().possiblyBumpAchievement(otherParentM, AchievementLibrary.Event.RACEBIRTH, 1, babe);
							}
							final Item I = CMClass.getItem("GenCaged");
							((CagedAnimal) I).cageMe(babe);
							((CagedAnimal) I).setCageFlagsBitmap(CagedAnimal.CAGEFLAG_TO_MOB_PROGRAMMATICALLY);
							final Ability AGE = CMClass.getAbility("Age");
							if (AGE != null)
							{
								AGE.setMiscText("" + System.currentTimeMillis());
								I.addNonUninvokableEffect(AGE);
							}
							if(CMLib.flags().isEggLayer(mob.charStats().getMyRace()))
								name = CMLib.english().startWithAorAn(L("@x1 egg",R.name())).toLowerCase();
							else
								name = CMLib.english().startWithAorAn(R.makeMobName((char) babe.baseCharStats().getStat(CharStats.STAT_GENDER), 0)).toLowerCase();
							I.setName(name);
							I.setDisplayText(L("@x1 is here.", name));
							I.recoverPhyStats();
							mob.location().addItem(I);
							if (!CMLib.flags().isEggLayer(mob.charStats().getMyRace()))
							{
								final Behavior B = CMClass.getBehavior("Emoter");
								B.setParms(Age.happyBabyEmoter);
								B.setSavable(true);
								I.addBehavior(B);
							}
							I.text();
							if ((!mob.isMonster()) && (mob.soulMate() == null))
							{
								if ((CMLib.dice().rollPercentage() < 20) && (mob.fetchEffect("Disease_Depression") == null))
								{
									final Ability A = CMClass.getAbility("Disease_Depression");
									if (A != null)
										A.invoke(mob, mob, true, 0);
								}
							}
							if ((mob.playerStats() != null) || ((otherParentM != null) && (otherParentM.playerStats() != null)))
							{
								CMLib.coffeeTables().bump(mob, CoffeeTableRow.STAT_BIRTHS);
								final List<String> channels = CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.BIRTHS, mob);
								for (int i = 0; i < channels.size(); i++)
									CMLib.commands().postChannel(mob, channels.get(i), L("@x1 has just given birth to @x2!", mob.name(), I.name()), true);
								String parent = mob.Name();
								if (mob.isMonster() && (otherParentM != null))
									parent = otherParentM.Name();
								if (AGE != null)
									CMLib.database().DBCreatePlayerData(parent, "HEAVEN", parent + "/HEAVEN/" + AGE.text(), I.ID() + "/" + I.basePhyStats().ability() + "/" + I.text());
							}
						}
					}
					else
						mob.tell(L("You are in labor!!"));
				}
				else
				{
					// pregnant folk get fatigued more often.
					if (mob.maxState().getFatigue() > Long.MIN_VALUE / 2)
						mob.curState().adjFatigue(monthsRemaining * 100, mob.maxState());
					if ((monthsRemaining <= 1)
					&& (CMLib.dice().rollPercentage() == 1))
					{
						if (CMLib.flags().isSleeping(mob))
							mob.enqueCommand(CMParms.parse("WAKE"), MUDCmdProcessor.METAFLAG_FORCED, 0);
						mob.tell(L("Oh! You had a contraction!"));
					}
					else
					if ((monthsRemaining <= 3)
					&& (CMLib.dice().rollPercentage() == 1)
					&& (CMLib.dice().rollPercentage() == 1))
						mob.tell(L("You feel a kick in your gut."));
					else
					if ((monthsRemaining > 8)
					&& (mob.location() != null)
					&& (mob.location().getArea().getTimeObj().getHourOfDay() < 2)
					&& (CMLib.dice().rollPercentage() == 1))
					{
						if (CMLib.dice().rollPercentage() > 25)
							mob.tell(L("You feel really sick this morning."));
						else
							mob.location().show(mob, null, CMMsg.MSG_NOISYMOVEMENT, L("**BLEH** <S-NAME> just threw up."));
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target = super.getTarget(mob, commands, givenTarget,false,true);
		if (target == null)
			return false;
		if (!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		final boolean success = proficiencyCheck(mob, 0, auto);
		long start = System.currentTimeMillis();
		final Race R = target.charStats().getMyRace();
		long tickspermudmonth = CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
		final TimeClock C = CMLib.time().localClock(target.getStartRoom());
		tickspermudmonth = tickspermudmonth * C.getDaysInMonth();
		int birthmonths = (int) Math.round(CMath.mul((R.getAgingChart()[1] - R.getAgingChart()[0]) * C.getMonthsInYear(), 0.75));
		if (birthmonths <= 0)
			birthmonths = 5;
		final long ticksperbirthperiod = tickspermudmonth * birthmonths;
		final long millisperbirthperiod = ticksperbirthperiod * CMProps.getTickMillis();

		if(target.fetchEffect(ID())!=null)
		{
			final Ability A=target.fetchEffect(ID());
			if(A!=null)
			{
				if(mob.location().show(mob, target, this, CMMsg.TYP_GENERAL, auto ? null : L("<S-NAME> birthifies <T-NAMESELF>.")))
				{
					final String[] parts=A.text().split("/");
					final StringBuilder str=new StringBuilder((start-millisperbirthperiod) + "/" + start);
					for(int i=2;i<parts.length;i++)
						str.append("/").append(parts[i]);
					A.setMiscText(str.toString());
					return true;
				}
				return false;
			}
		}

		long end = start + millisperbirthperiod;
		if (success)
		{
			if (!auto)
			{
				end = start;
				start -= millisperbirthperiod;
			}
			int numKids = 1;
			while((CMLib.dice().rollPercentage()==1)
			&&(numKids < 9))
				numKids++;
			final String numKidMarker=(numKids<=1)?"":(""+numKids+"X");
			if (mob.location().show(mob, target, this, CMMsg.TYP_GENERAL, auto ? null : L("<S-NAME> imgregnate(s) <T-NAMESELF>.")))
			{
				setMiscText(start + "/" + end + "/" + mob.Name() + "/" + numKidMarker+mob.charStats().getMyRace().ID());
				final List<String> channels = CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CONCEPTIONS, mob);
				for (int i = 0; i < channels.size(); i++)
					CMLib.commands().postChannel(channels.get(i), mob.clans(), L("@x1 is now in a 'family way'.", target.name()), true);
				final Pregnancy P=(Pregnancy)copyOf();
				if((!mob.isPlayer())
				&&(!target.isPlayer()))
				{
				}
				target.addNonUninvokableEffect(P);
			}
		}
		else
		if (!auto)
			return beneficialVisualFizzle(mob, target, L("<S-NAME> attempt(s) to impregnate <T-NAMESELF>, but fail(s)!"));
		return success;
	}
}
