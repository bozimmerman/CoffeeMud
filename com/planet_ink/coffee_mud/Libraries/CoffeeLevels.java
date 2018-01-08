package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class CoffeeLevels extends StdLibrary implements ExpLevelLibrary
{
	@Override
	public String ID()
	{
		return "CoffeeLevels";
	}

	public int getManaBonusNextLevel(MOB mob)
	{
		final CharClass charClass = mob.baseCharStats().getCurrentClass();
		final double[] variables={
				mob.phyStats().level(),
				mob.charStats().getStat(CharStats.STAT_WISDOM),
				CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_WISDOM)),
				mob.charStats().getStat(CharStats.STAT_INTELLIGENCE),
				CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_INTELLIGENCE)),
				mob.charStats().getStat(charClass.getAttackAttribute()),
				CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(charClass.getAttackAttribute())),
				mob.charStats().getStat(CharStats.STAT_CHARISMA),
				mob.charStats().getStat(CharStats.STAT_CONSTITUTION)
			};
		return (int)Math.round(CMath.parseMathExpression(charClass.getManaFormula(), variables));
	}

	@Override
	public int getLevelMana(MOB mob)
	{
		return CMProps.getIntVar(CMProps.Int.STARTMANA)+
			((mob.basePhyStats().level()-1)*getManaBonusNextLevel(mob));
	}

	public int getAttackBonusNextLevel(MOB mob)
	{
		final CharClass charClass = mob.baseCharStats().getCurrentClass();
		final int rawAttStat = mob.charStats().getStat(charClass.getAttackAttribute());
		int attStat= rawAttStat;
		final int maxAttStat=(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.CODES.toMAXBASE(charClass.getAttackAttribute())));
		if(attStat>=maxAttStat)
			attStat=maxAttStat;
		int attGain=(int)Math.floor(CMath.div(attStat,18.0))+charClass.getBonusAttackLevel();
		if(attStat>=25)
			attGain+=2;
		else
		if(attStat>=22)
			attGain+=1;
		return attGain;
	}

	@Override
	public int getLevelAttack(MOB mob)
	{
		return ((mob.basePhyStats().level()-1)*getAttackBonusNextLevel(mob)) + mob.basePhyStats().level();
	}

	@Override
	public int getLevelMOBArmor(MOB mob)
	{
		return 100-(int)Math.round(CMath.mul(mob.basePhyStats().level(),3.0));
	}

	@Override
	public int getLevelMOBDamage(MOB mob)
	{
		return (mob.basePhyStats().level());
	}

	@Override
	public double getLevelMOBSpeed(MOB mob)
	{
		return 1.0+Math.floor(CMath.div(mob.basePhyStats().level(),30.0));
	}

	public int getMoveBonusNextLevel(MOB mob)
	{
		final CharClass charClass = mob.baseCharStats().getCurrentClass();
		final double[] variables={
			mob.phyStats().level(),
			mob.charStats().getStat(CharStats.STAT_STRENGTH),
			CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_STRENGTH)),
			mob.charStats().getStat(CharStats.STAT_DEXTERITY),
			CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_DEXTERITY)),
			mob.charStats().getStat(CharStats.STAT_CONSTITUTION),
			CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_CONSTITUTION)),
			mob.charStats().getStat(CharStats.STAT_WISDOM),
			mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)
		};
		return (int)Math.round(CMath.parseMathExpression(charClass.getMovementFormula(), variables));
	}

	@Override
	public int getLevelMove(MOB mob)
	{
		int move=CMProps.getIntVar(CMProps.Int.STARTMOVE);
		if(mob.basePhyStats().level()>1)
			move+=(mob.basePhyStats().level()-1) * getMoveBonusNextLevel(mob);
		return move;
	}

	public int getPlayerHPBonusNextLevel(MOB mob)
	{
		final CharClass charClass = mob.baseCharStats().getCurrentClass();
		final double[] variables={
			mob.phyStats().level(),
			mob.charStats().getStat(CharStats.STAT_STRENGTH),
			CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_STRENGTH)),
			mob.charStats().getStat(CharStats.STAT_DEXTERITY),
			CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_DEXTERITY)),
			mob.charStats().getStat(CharStats.STAT_CONSTITUTION),
			CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_CONSTITUTION)),
			mob.charStats().getStat(CharStats.STAT_WISDOM),
			mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)
		};
		final int newHitPointGain=(int)Math.round(CMath.parseMathExpression(charClass.getHitPointsFormula(), variables));
		if(newHitPointGain<=0)
		{
			if(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)>=1)
				return 1;
			return 0;
		}
		return newHitPointGain;
	}

	@Override
	public int getPlayerHitPoints(MOB mob)
	{
		final int hp=CMProps.getIntVar(CMProps.Int.STARTHP);
		return hp+((mob.phyStats().level()-1)*getPlayerHPBonusNextLevel(mob));
	}

	@Override
	public MOB fillOutMOB(CharClass C, int level)
	{
		final MOB mob=CMClass.getFactoryMOB();
		mob.baseCharStats().setCurrentClass(C);
		mob.charStats().setCurrentClass(C);
		mob.baseCharStats().setCurrentClassLevel(level);
		mob.charStats().setCurrentClassLevel(level);
		mob.basePhyStats().setLevel(level);
		mob.phyStats().setLevel(level);
		fillOutMOB(mob,level);
		return mob;
	}

	public boolean isFilledOutMOB(final MOB mob)
	{
		if(!mob.isMonster())
			return false;
		final PhyStats mobP=mob.basePhyStats();

		final MOB filledM=fillOutMOB((MOB)null,mobP.level());
		final PhyStats filP=filledM.basePhyStats();
		if((mobP.speed()==filP.speed())
		&&(mobP.armor()==filP.armor())
		&&(mobP.damage()==filP.damage())
		&&(mobP.attackAdjustment()==filP.attackAdjustment()))
		{
			filledM.destroy();
			return true;
		}
		filledM.destroy();
		return false;
	}

	@Override
	public MOB fillOutMOB(MOB mob, int level)
	{
		if(mob==null)
			mob=CMClass.getFactoryMOB();
		if(!mob.isMonster())
			return mob;

		long rejuv=CMProps.getTicksPerMinute()+CMProps.getTicksPerMinute()+(level*CMProps.getTicksPerMinute()/2);
		if(rejuv>(CMProps.getTicksPerMinute()*20))
			rejuv=(CMProps.getTicksPerMinute()*20);
		mob.basePhyStats().setLevel(level);
		mob.basePhyStats().setRejuv((int)rejuv);
		mob.basePhyStats().setSpeed(getLevelMOBSpeed(mob));
		mob.basePhyStats().setArmor(getLevelMOBArmor(mob));
		mob.basePhyStats().setDamage(getLevelMOBDamage(mob));
		//mob.basePhyStats().setDamage((int)Math.round(CMath.div(getLevelMOBDamage(mob),mob.basePhyStats().speed())));
		mob.basePhyStats().setAttackAdjustment(getLevelAttack(mob));
		mob.setMoney(CMLib.dice().roll(1,level,0)+CMLib.dice().roll(1,10,0));
		mob.baseState().setHitPoints(CMLib.dice().rollHP(mob.basePhyStats().level(),mob.basePhyStats().ability()));
		mob.baseState().setMana(getLevelMana(mob));
		mob.baseState().setMovement(getLevelMove(mob));
		if(mob.getWimpHitPoint()>0)
			mob.setWimpHitPoint((int)Math.round(CMath.mul(mob.curState().getHitPoints(),.10)));
		mob.setExperience(CMLib.leveler().getLevelExperience(mob.phyStats().level()));
		return mob;
	}
	
	@Override
	public double[] getLevelMoneyRange(final MOB mob)
	{
		return new double[]{2,mob.basePhyStats().level()+10};
	}

	@Override
	public String doBaseLevelAdjustment(final MOB mob, final int adjuster)
	{
		synchronized(mob.basePhyStats())
		{
			mob.basePhyStats().setLevel(mob.basePhyStats().level()+adjuster);
		}
		synchronized(mob.phyStats())
		{
			mob.phyStats().setLevel(mob.basePhyStats().level());
		}
		final CharClass curClass;
		final int oldClassLevel;
		synchronized(mob.baseCharStats())
		{
			curClass=mob.baseCharStats().getCurrentClass();
			oldClassLevel = mob.baseCharStats().getClassLevel(curClass);
			mob.baseCharStats().setClassLevel(curClass,oldClassLevel+adjuster);
		}
		synchronized(mob.charStats())
		{
			mob.charStats().setClassLevel(curClass,oldClassLevel+adjuster);
		}
		final int classLevel;
		synchronized(mob.baseCharStats())
		{
			classLevel=mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass());
		}
		int gained=mob.getExperience()-mob.getExpNextLevel();
		if(gained<50)
			gained=50;

		final StringBuilder theNews=new StringBuilder("");

		mob.recoverCharStats();
		mob.recoverPhyStats();
		theNews.append("^HYou are now a "+mob.charStats().displayClassLevel(mob,false)+".^N\n\r");

		final int newHitPointGain = getPlayerHPBonusNextLevel(mob) * adjuster;
		if(mob.getWimpHitPoint() > 0)
		{
			double wimpPct = CMath.div(mob.getWimpHitPoint(), mob.baseState().getHitPoints());
			mob.setWimpHitPoint((int)Math.round(CMath.ceiling(CMath.mul(mob.baseState().getHitPoints()+newHitPointGain,wimpPct))));
		}
		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+newHitPointGain);
		if(mob.baseState().getHitPoints()<CMProps.getIntVar(CMProps.Int.STARTHP))
			mob.baseState().setHitPoints(CMProps.getIntVar(CMProps.Int.STARTHP));
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		theNews.append("^NYou have gained ^H"+newHitPointGain+"^? hit " +
			(newHitPointGain!=1?"points":"point") + ", ^H");

		final int mvGain = getMoveBonusNextLevel(mob) * adjuster;
		mob.baseState().setMovement(mob.baseState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		theNews.append(mvGain+"^N move " + (mvGain!=1?"points":"point") + ", ^H");

		final int attGain=getAttackBonusNextLevel(mob) * adjuster;
		mob.basePhyStats().setAttackAdjustment(mob.basePhyStats().attackAdjustment()+attGain);
		mob.phyStats().setAttackAdjustment(mob.phyStats().attackAdjustment()+attGain);
		if(attGain>0)
			theNews.append(attGain+"^N attack " + (attGain!=1?"points":"point") + ", ^H");

		final int manaGain = getManaBonusNextLevel(mob) * adjuster;
		mob.baseState().setMana(mob.baseState().getMana()+manaGain);
		theNews.append(manaGain+"^N " + (manaGain!=1?"points":"point") + " of mana,");

		if(curClass.getLevelsPerBonusDamage()!=0)
		{
			if((adjuster<0)&&(((classLevel+1)%curClass.getLevelsPerBonusDamage())==0))
				mob.basePhyStats().setDamage(mob.basePhyStats().damage()-1);
			else
			if((adjuster>0)&&((classLevel%curClass.getLevelsPerBonusDamage())==0))
				mob.basePhyStats().setDamage(mob.basePhyStats().damage()+1);
		}
		mob.recoverMaxState();
		return theNews.toString();
	}

	@Override
	public void unLevel(MOB mob)
	{
		if((mob.basePhyStats().level()<2)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		||(mob.charStats().getCurrentClass().leveless())
		||(mob.charStats().getMyRace().leveless()))
			return;
		final CMMsg msg=CMClass.getMsg(mob,CMMsg.MSG_LEVEL,null,mob.basePhyStats().level()-1);
		if(!CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_LEVEL,msg))
			return;
		
		mob.tell(L("^ZYou have ****LOST A LEVEL****^.^N\n\r\n\r@x1",CMLib.protocol().msp("doh.wav",60)));
		if(!mob.isMonster())
		{
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOSTLEVELS);
			if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has just lost a level.",mob.Name()),true);
		}

		final CharClass curClass=mob.baseCharStats().getCurrentClass();
		final int oldClassLevel=mob.baseCharStats().getClassLevel(curClass);
		doBaseLevelAdjustment(mob,-1);
		int prac2Stat=mob.charStats().getStat(CharStats.STAT_WISDOM);
		final int maxPrac2Stat=(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_WISDOM)));
		if(prac2Stat>maxPrac2Stat)
			prac2Stat=maxPrac2Stat;
		int practiceGain=(int)Math.floor(CMath.div(prac2Stat,6.0))+curClass.getBonusPracLevel();
		if(practiceGain<=0)
			practiceGain=1;
		mob.setPractices(mob.getPractices()-practiceGain);
		int trainGain=0;
		if(trainGain<=0)
			trainGain=1;
		mob.setTrains(mob.getTrains()-trainGain);

		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
		mob.tell(L("^HYou are now a level @x1 @x2^N.\n\r",""+mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel())));
		curClass.unLevel(mob);
		Ability A=null;
		final Vector<Ability> lose=new Vector<Ability>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			A=mob.fetchAbility(a);
			if((CMLib.ableMapper().getQualifyingLevel(curClass.ID(),false,A.ID())==oldClassLevel)
			&&(CMLib.ableMapper().getDefaultGain(curClass.ID(),false,A.ID()))
			&&(CMLib.ableMapper().classOnly(mob,curClass.ID(),A.ID())))
				lose.addElement(A);
		}
		for(int l=0;l<lose.size();l++)
		{
			A=lose.elementAt(l);
			mob.delAbility(A);
			mob.tell(L("^HYou have forgotten @x1.^N.\n\r",A.name()));
			A=mob.fetchEffect(A.ID());
			if((A!=null)&&(A.isNowAnAutoEffect()))
			{
				A.unInvoke();
				mob.delEffect(A);
			}
		}
		fixMobStatsIfNecessary(mob,-1);
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.LEVELSGAINED, -1, mob);
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CLASSLEVELSGAINED, -1, mob);
	}

	@Override
	public void loseExperience(MOB mob, int amount)
	{
		if((mob==null)||(mob.soulMate()!=null))
			return;
		if(Log.combatChannelOn())
		{
			final String room=CMLib.map().getExtendedRoomID((mob.location()!=null)?mob.location():null);
			final String mobName=mob.Name();
			Log.killsOut("-EXP",room+":"+mobName+":"+amount);
		}
		if((mob.getLiegeID().length()>0)&&(amount>2)&&(!mob.isMonster()))
		{
			final MOB sire=CMLib.players().getPlayer(mob.getLiegeID());
			if((sire!=null)&&(CMLib.flags().isInTheGame(sire,true)))
			{
				final int sireShare=(int)Math.round(CMath.div(amount,10.0));
				amount-=sireShare;
				if(postExperience(sire,null,"",-sireShare,true))
					sire.tell(L("^N^!You lose ^H@x1^N^! experience points from @x2.^N",""+sireShare,mob.Name()));
			}
		}
		if((amount>2)&&(!mob.isMonster()))
		{
			for(final Pair<Clan,Integer> p : mob.clans())
			{
				final Clan C=p.first;
				if(C.getTaxes()>0.0)
				{
					final int clanshare=(int)Math.round(CMath.mul(amount,C.getTaxes()));
					if(clanshare>0)
					{
						amount-=clanshare;
						C.adjExp(clanshare*-1);
						C.update();
					}
				}
			}
		}
		mob.setExperience(mob.getExperience()-amount);
		int neededLowest=getLevelExperience(mob.basePhyStats().level()-2);
		if((mob.getExperience()<neededLowest)
		&&(mob.basePhyStats().level()>1))
		{
			unLevel(mob);
			neededLowest=getLevelExperience(mob.basePhyStats().level()-2);
		}
	}

	@Override
	public boolean postExperience(MOB mob,MOB victim,String homage,int amount,boolean quiet)
	{
		if((mob==null)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		||mob.charStats().getCurrentClass().expless()
		||mob.charStats().getMyRace().expless())
			return false;
		final CMMsg msg=CMClass.getMsg(mob,victim,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_EXPCHANGE,null,CMMsg.NO_EFFECT,homage,CMMsg.NO_EFFECT,""+quiet);
		msg.setValue(amount);
		final Room R=mob.location();
		if(R!=null)
		{
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
			else
				return false;
		}
		else
		if(amount>=0)
			gainExperience(mob,victim,homage,amount,quiet);
		else
			loseExperience(mob,-amount);
		return true;
	}

	@Override
	public int getLevelExperience(int level)
	{
		if(level<0)
			return 0;
		final int[] levelingChart = CMProps.getListFileIntList(CMProps.ListFile.EXP_CHART);
		if(level<levelingChart.length)
			return levelingChart[level];
		final int lastDiff=levelingChart[levelingChart.length-1] - levelingChart[levelingChart.length-2];
		return levelingChart[levelingChart.length-1] + ((1+(level-levelingChart.length)) * lastDiff);
	}

	@Override
	public int getLevelExperienceJustThisLevel(int level)
	{
		if(level<0)
			return 0;
		final int[] levelingChart = CMProps.getListFileIntList(CMProps.ListFile.EXP_CHART);
		if(level==0)
			return levelingChart[0];
		else
		if(level<levelingChart.length)
			return levelingChart[level]-levelingChart[level-1];
		final int lastDiff=levelingChart[levelingChart.length-1] - levelingChart[levelingChart.length-2];
		return ((1+(level-levelingChart.length)) * lastDiff);
	}

	@Override
	public void level(MOB mob)
	{
		if((CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		||(mob.charStats().getCurrentClass().leveless())
		||(mob.charStats().isLevelCapped(mob.charStats().getCurrentClass()))
		||(mob.charStats().getMyRace().leveless()))
			return;
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,CMMsg.MSG_LEVEL,null,mob.basePhyStats().level()+1);
		if(!CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_LEVEL,msg))
			return;
		if(room!=null)
		{
			if(!room.okMessage(mob,msg))
				return;
			room.send(mob,msg);
		}

		if(mob.getGroupMembers(new HashSet<MOB>()).size()>1)
		{
			final Command C=CMClass.getCommand("GTell");
			try
			{
				if(C!=null)
					C.execute(mob,new XVector<String>("GTELL","I have gained a level."),MUDCmdProcessor.METAFLAG_FORCED);
			}
			catch(final Exception e)
			{
			}
		}
		
		final String levelAdjustmentMsg = doBaseLevelAdjustment(mob,1); 
		
		final StringBuilder theNews=new StringBuilder("^xYou have L E V E L E D ! ! ! ! ! ^.^N\n\r\n\r"+CMLib.protocol().msp("levelgain.wav",60));
		CharClass curClass=mob.baseCharStats().getCurrentClass();
		theNews.append(levelAdjustmentMsg);
		if(mob.playerStats()!=null)
		{
			mob.playerStats().setLeveledDateTime(mob.basePhyStats().level(),mob.getAgeMinutes(),room);
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DETAILEDLEVELS);
			final List<String> channels2=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LEVELS);
			if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has just gained a level at @x2.",mob.Name(),CMLib.map().getDescriptiveExtendedRoomID(room)),true);
			if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels2.size();i++)
				CMLib.commands().postChannel(channels2.get(i),mob.clans(),L("@x1 has just gained a level.",mob.Name()),true);
			if(mob.soulMate()==null)
				CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LEVELSGAINED);
		}

		int prac2Stat=mob.charStats().getStat(CharStats.STAT_WISDOM);
		final int maxPrac2Stat=(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_WISDOM)));
		if(prac2Stat>maxPrac2Stat)
			prac2Stat=maxPrac2Stat;
		int practiceGain=(int)Math.floor(CMath.div(prac2Stat,6.0))+curClass.getBonusPracLevel();
		if(practiceGain<=0)
			practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		theNews.append(" ^H" + practiceGain+"^N practice " +
			( practiceGain != 1? "points" : "point" ) + ", ");

		int trainGain=1;
		if(trainGain<=0)
			trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		theNews.append("and ^H"+trainGain+"^N training "+ (trainGain != 1? "sessions" : "session" )+".\n\r^N");

		mob.tell(theNews.toString());
		curClass=mob.baseCharStats().getCurrentClass();
		final Set<String> oldAbilities=new HashSet<String>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				oldAbilities.add(A.ID());
		}
		final Map<String,Integer> oldExpertises=new TreeMap<String,Integer>();
		for(final Enumeration<String> e=mob.expertises();e.hasMoreElements();)
		{
			Pair<String,Integer> pair = mob.fetchExpertise(e.nextElement());
			if(pair != null)
			{
				if((!oldExpertises.containsKey(pair.first))
				||(oldExpertises.get(pair.first).intValue() < pair.second.intValue()))
					oldExpertises.put(pair.first, pair.second);
			}
		}

		curClass.grantAbilities(mob,false);
		mob.baseCharStats().getMyRace().grantAbilities(mob, false);
		mob.charStats().getMyRace().startRacing(mob, true);
		CMLib.achievements().grantAbilitiesAndExpertises(mob);

		// check for autoinvoking abilities
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			{
				final Ability eA=mob.fetchEffect(A.ID());
				if((eA==null)||(!eA.isNowAnAutoEffect()))
					A.autoInvocation(mob, false);
			}
		}

		final List<String> newAbilityIDs=new Vector<String>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!oldAbilities.contains(A.ID())))
				newAbilityIDs.add(A.ID());
		}

		for(int a=0;a<newAbilityIDs.size();a++)
		{
			if(!oldAbilities.contains(newAbilityIDs.get(a)))
			{
				final Ability A=mob.fetchAbility(newAbilityIDs.get(a));
				if(A!=null)
				{
					final String type=Ability.ACODE_DESCS[(A.classificationCode()&Ability.ALL_ACODES)].toLowerCase();
					mob.tell(L("^NYou have learned the @x1 ^H@x2^?.^N",type,A.name()));
				}
			}
		}

		for(final Enumeration<String> e=mob.expertises();e.hasMoreElements();)
		{
			final Pair<String,Integer> pair = mob.fetchExpertise(e.nextElement());
			if(pair != null)
			{
				ExpertiseDefinition def=CMLib.expertises().findDefinition(pair.first+pair.second.toString(),true);
				if(def == null)
					def=CMLib.expertises().findDefinition(pair.first+pair.second.toString(),false);
				if(def == null)
					 def=CMLib.expertises().findDefinition(pair.first,true);
				if(def == null)
					 def=CMLib.expertises().findDefinition(pair.first,false);
				if(def != null)
				{
					if((!oldExpertises.containsKey(pair.first))
					||(oldExpertises.get(pair.first).intValue() < pair.second.intValue()))
						mob.tell(L("^NYou have learned ^H@x1^?.^N",def.name()));
				}
			}
		}
		
		fixMobStatsIfNecessary(mob,1);

		// wrap it all up
		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.recoverMaxState();

		curClass.level(mob,newAbilityIDs);
		mob.charStats().getMyRace().level(mob,newAbilityIDs);
		
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.LEVELSGAINED, 1, mob);
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CLASSLEVELSGAINED, 1, mob);
	}

	protected boolean fixMobStatsIfNecessary(MOB mob, int direction)
	{
		if((mob.playerStats()==null)&&(mob.baseCharStats().getCurrentClass().name().equals("mob"))) // mob leveling
		{
			mob.basePhyStats().setSpeed(getLevelMOBSpeed(mob));
			mob.basePhyStats().setArmor(getLevelMOBArmor(mob));
			mob.basePhyStats().setDamage(getLevelMOBDamage(mob));
			mob.basePhyStats().setAttackAdjustment(getLevelAttack(mob));
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+((mob.basePhyStats().ability()/2)*direction));
			mob.baseState().setMana(getLevelMana(mob));
			mob.baseState().setMovement(getLevelMove(mob));
			if(mob.getWimpHitPoint()>0)
				mob.setWimpHitPoint((int)Math.round(CMath.mul(mob.curState().getHitPoints(),.10)));
			return true;
		}
		return false;
	}

	@Override
	public int adjustedExperience(MOB mob, MOB victim, int amount)
	{
		int highestLevelPC = 0;
		final Room R=mob.location();
		if(R!=null)
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M!=mob)&&(M!=victim)&&(!M.isMonster())&&(M.phyStats().level()>highestLevelPC))
					highestLevelPC = M.phyStats().level();
			}

		}
		final Set<MOB> group=mob.getGroupMembers(new HashSet<MOB>());
		CharClass charClass=null;
		Race charRace=null;

		for (final MOB allyMOB : group)
		{
			charClass = allyMOB.charStats().getCurrentClass();
			charRace = allyMOB.charStats().getMyRace();
			if(charClass != null)
				amount = charClass.adjustExperienceGain(allyMOB, mob, victim, amount);
			if(charRace != null)
				amount = charRace.adjustExperienceGain(allyMOB, mob, victim, amount);
		}

		if(victim!=null)
		{
			final double levelLimit=CMProps.getIntVar(CMProps.Int.EXPRATE);
			final double levelDiff=victim.phyStats().level()-mob.phyStats().level();

			if(levelDiff<(-levelLimit) )
				amount=0;
			else
			if((levelLimit>0)&&((highestLevelPC - mob.phyStats().level())<=levelLimit))
			{
				double levelFactor=levelDiff / levelLimit;
				if( levelFactor > levelLimit )
					levelFactor = levelLimit;
				amount+=(int)Math.round(levelFactor *  amount);
			}
		}

		return amount;
	}

	@Override
	public void gainExperience(MOB mob, MOB victim, String homageMessage, int amount, boolean quiet)
	{
		if(mob==null)
			return;
		if((Log.combatChannelOn())
		&&((mob.location()!=null)
			||((victim!=null)&&(victim.location()!=null))))
		{
			final String room=CMLib.map().getExtendedRoomID((mob.location()!=null)?mob.location():(victim==null)?null:victim.location());
			final String mobName=mob.Name();
			final String vicName=(victim!=null)?victim.Name():"null";
			Log.killsOut("+EXP",room+":"+mobName+":"+vicName+":"+amount+":"+homageMessage);
		}

		amount=adjustedExperience(mob,victim,amount);

		if(mob.phyStats().level()>=CMProps.getIntVar(CMProps.Int.MINCLANLEVEL))
		{
			for(final Pair<Clan,Integer> p : mob.clans())
			{
				if(amount>2)
					amount=p.first.applyExpMods(amount);
			}
		}

		if((mob.getLiegeID().length()>0)&&(amount>2))
		{
			final MOB sire=CMLib.players().getLoadPlayer(mob.getLiegeID());
			if(sire!=null)
			{
				int sireShare=(int)Math.round(CMath.div(amount,10.0));
				if(sireShare<=0)
					sireShare=1;
				amount-=sireShare;
				CMLib.leveler().postExperience(sire,null," from "+mob.name(sire),sireShare,quiet);
			}
		}

		CMLib.players().bumpPrideStat(mob,PrideStat.EXPERIENCE_GAINED, amount);
		mob.setExperience(mob.getExperience()+amount);
		if(homageMessage==null)
			homageMessage="";
		if(!quiet)
		{
			if(amount>1)
				mob.tell(L("^N^!You gain ^H@x1^N^! experience points@x2.^N",""+amount,homageMessage));
			else
			if(amount>0)
				mob.tell(L("^N^!You gain ^H@x1^N^! experience point@x2.^N",""+amount,homageMessage));
		}

		if((mob.getExperience()>=mob.getExpNextLevel())
		&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
			level(mob);
	}

	@Override
	public void handleExperienceChange(CMMsg msg)
	{
		final MOB mob=msg.source();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE)
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			MOB expFromKilledmob=null;
			if(msg.target() instanceof MOB)
				expFromKilledmob=(MOB)msg.target();

			if(msg.value()>=0)
			{
				gainExperience(mob,
							   expFromKilledmob,
							   msg.targetMessage(),
							   msg.value(),
							   CMath.s_bool(msg.othersMessage()));
			}
			else
				loseExperience(mob,-msg.value());
		}
	}
	
	@Override
	public boolean postExperienceToAllAboard(Physical possibleShip, int amount)
	{
		boolean posted = false;
		if(possibleShip instanceof BoardableShip)
		{
			final Area A=((BoardableShip)possibleShip).getShipArea();
			if(A!=null)
			{
				posted = true;
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							posted = CMLib.leveler().postExperience(M, null, null, amount, false) && posted;
						}
					}
				}
			}
		}
		return posted;
	}

}
