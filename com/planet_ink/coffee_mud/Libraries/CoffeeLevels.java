package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Bool;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpLevelLibrary.ModXP;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Trophy;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/*
   Copyright 2006-2024 Bo Zimmerman

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
	protected final int[] experienceCaps = new int[256];
	protected ModXP[] xpMods = null;

	@Override
	public String ID()
	{
		return "CoffeeLevels";
	}

	public int getManaBonusNextLevel(final MOB mob)
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
		return (int)Math.round(CMath.parseMathExpression(charClass.getManaFormula()+CMProps.getVar(Str.FORMULA_CLASSMNADD), variables));
	}

	@Override
	public int getLevelMana(final MOB mob)
	{
		return CMProps.getIntVar(CMProps.Int.STARTMANA)+
			((mob.basePhyStats().level()-1)*getManaBonusNextLevel(mob));
	}

	@Override
	public int getLevelHitPoints(final MOB mob)
	{
		final int hpCode = mob.basePhyStats().ability();
		final int level = mob.basePhyStats().level();
		int hp = CMLib.dice().rollHP(level,hpCode);
		if((CMProps.getVar(Str.FORMULA_CLASSHPADD).length()>0)&&(level>1))
		{
			final double[] variables={ mob.basePhyStats().level(), 10, 18, 10, 18, 10, 18, 10, 10 };
			hp = (level * CMath.parseIntExpression((hp/level)+CMProps.getVar(Str.FORMULA_CLASSHPADD),variables));
		}
		return hp;
	}

	public int getAttackBonusNextLevel(final MOB mob)
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
	public int getLevelAttack(final MOB mob)
	{
		//also update timsLevelCalculator(final MOB M)
		return ((mob.basePhyStats().level()-1)*getAttackBonusNextLevel(mob)) + mob.basePhyStats().level();
	}

	@Override
	public int getLevelMOBArmor(final MOB mob)
	{
		//also update timsLevelCalculator(final MOB M)
		final double levelMid = CMath.mul(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL),0.44);
		final double levelLow = levelMid / 2.0;
		final double lvl = mob.basePhyStats().level();
		return 102-(int)Math.round((lvl+levelMid)*(lvl/levelLow));
	}

	@Override
	public int getLevelMOBDamage(final MOB mob)
	{
		//also update timsLevelCalculator(final MOB M)
		return (mob.basePhyStats().level());
	}

	@Override
	public double getLevelMOBSpeed(final MOB mob)
	{
		//also update timsLevelCalculator(final MOB M)
		return 1.0+Math.floor(CMath.div(mob.basePhyStats().level(),30.0));
	}

	@Override
	public int getPowerLevel(final MOB M)
	{
		final double dmgLevel = M.phyStats().damage();
		final double speedLevel = 1.0 + (M.phyStats().speed()-1.0)*30.0;
		final double armLevel = ((103 - M.phyStats().armor()) / 3.0);
		final double bonus = (1.0 + getAttackBonusNextLevel(M));
		final double attLevel = (bonus!=0)?(M.phyStats().attackAdjustment() / bonus) : 0;
		return (int)Math.round((dmgLevel+speedLevel+armLevel+attLevel)/4.0);
	}

	public int getMoveBonusNextLevel(final MOB mob)
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
		return (int)Math.round(CMath.parseMathExpression(charClass.getMovementFormula()+CMProps.getVar(Str.FORMULA_CLASSMVADD), variables));
	}

	@Override
	public int getLevelMove(final MOB mob)
	{
		int move=CMProps.getIntVar(CMProps.Int.STARTMOVE);
		if(mob.basePhyStats().level()>1)
			move+=(mob.basePhyStats().level()-1) * getMoveBonusNextLevel(mob);
		return move;
	}

	public int getPlayerHPBonusNextLevel(final MOB mob)
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
		final int newHitPointGain=(int)Math.round(CMath.parseMathExpression(
												charClass.getHitPointsFormula()+CMProps.getVar(Str.FORMULA_CLASSHPADD),
												variables));
		if(newHitPointGain<=0)
		{
			if(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)>=1)
				return 1;
			return 0;
		}
		return newHitPointGain;
	}

	@Override
	public int getPlayerHitPoints(final MOB mob)
	{
		final int hp=CMProps.getIntVar(CMProps.Int.STARTHP);
		return hp+((mob.phyStats().level()-1)*getPlayerHPBonusNextLevel(mob));
	}

	@Override
	public MOB fillOutMOB(final CharClass C, final Race R, final int level)
	{
		final MOB mob=CMClass.getFactoryMOB();
		if(C!=null)
		{
			mob.baseCharStats().setCurrentClass(C);
			mob.charStats().setCurrentClass(C);
		}
		if(R!=null)
		{
			CMLib.database().registerRaceUsed(R);
			mob.baseCharStats().setMyRace(R);
			mob.charStats().setMyRace(R);
		}
		mob.baseCharStats().setCurrentClassLevel(level);
		mob.charStats().setCurrentClassLevel(level);
		mob.basePhyStats().setLevel(level);
		mob.phyStats().setLevel(level);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
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
	public MOB fillOutMOB(MOB mob, final int level)
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
		mob.baseState().setHitPoints(getLevelHitPoints(mob));
		mob.baseState().setMana(getLevelMana(mob));
		mob.baseState().setMovement(getLevelMove(mob));
		if(mob.getWimpHitPoint()>0)
			mob.setWimpHitPoint((int)Math.round(CMath.mul(mob.curState().getHitPoints(),.10)));
		mob.setExperience(getLevelExperience(mob, mob.basePhyStats().level()));
		return mob;
	}

	@Override
	public double[] getLevelMoneyRange(final MOB mob)
	{
		return new double[]{2,mob.basePhyStats().level()+10};
	}

	protected String doBaseLevelAdjustment(final MOB mob, final int[] costGains, final boolean gain)
	{
		synchronized(mob.basePhyStats())
		{
			mob.basePhyStats().setLevel(mob.basePhyStats().level()+(gain?1:-1));
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
			mob.baseCharStats().setClassLevel(curClass,oldClassLevel+(gain?1:-1));
		}
		synchronized(mob.charStats())
		{
			mob.charStats().setClassLevel(curClass,oldClassLevel+(gain?1:-1));
		}
		final int classLevel;
		synchronized(mob.baseCharStats())
		{
			classLevel=mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass());
		}
		int gained=mob.getExperience()-mob.getExpNextLevel();
		if(gained<50)
			gained=50;

		costGains[CostDef.CostType.XP.ordinal()] = gained;
		final StringBuilder theNews=new StringBuilder("");

		mob.recoverCharStats();
		mob.recoverPhyStats();
		theNews.append("^HYou are now a "+mob.charStats().displayClassLevel(mob,false)+".^N\n\r");

		final int oldHpCost = costGains[CostDef.CostType.HITPOINT.ordinal()];
		final int newHitPointGain = (gain||oldHpCost==0)?getPlayerHPBonusNextLevel(mob) : -oldHpCost;
		if(mob.getWimpHitPoint() > 0)
		{
			final double wimpPct = CMath.div(mob.getWimpHitPoint(), mob.baseState().getHitPoints());
			mob.setWimpHitPoint((int)Math.round(CMath.ceiling(CMath.mul(mob.baseState().getHitPoints()+newHitPointGain,wimpPct))));
		}
		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+newHitPointGain);
		if(mob.baseState().getHitPoints()<CMProps.getIntVar(CMProps.Int.STARTHP))
			mob.baseState().setHitPoints(CMProps.getIntVar(CMProps.Int.STARTHP));
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		costGains[CostDef.CostType.HITPOINT.ordinal()] = newHitPointGain;
		theNews.append("^NYou have gained ^H"+newHitPointGain+"^? hit " +
			(newHitPointGain!=1?"points":"point") + ", ^H");

		final int oldMvCost = costGains[CostDef.CostType.MOVEMENT.ordinal()];
		final int mvGain = (gain||oldMvCost==0)?getMoveBonusNextLevel(mob) : -oldMvCost;
		mob.baseState().setMovement(mob.baseState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		costGains[CostDef.CostType.MOVEMENT.ordinal()] = mvGain;
		theNews.append(mvGain+"^N move " + (mvGain!=1?"points":"point") + ", ^H");

		final int oldAttCost = costGains[CostDef.CostType.QP.ordinal()];
		final int attGain=(gain||oldAttCost==0)?getAttackBonusNextLevel(mob) : -oldAttCost;
		mob.basePhyStats().setAttackAdjustment(mob.basePhyStats().attackAdjustment()+attGain);
		mob.phyStats().setAttackAdjustment(mob.phyStats().attackAdjustment()+attGain);
		costGains[CostDef.CostType.QP.ordinal()] = attGain;
		if(attGain>0)
			theNews.append(attGain+"^N attack " + (attGain!=1?"points":"point") + ", ^H");

		final int oldManaCost = costGains[CostDef.CostType.MANA.ordinal()];
		final int manaGain = (gain||oldManaCost==0)?getManaBonusNextLevel(mob) : -oldManaCost;
		mob.baseState().setMana(mob.baseState().getMana()+manaGain);
		costGains[CostDef.CostType.MANA.ordinal()] = manaGain;
		theNews.append(manaGain+"^N " + (manaGain!=1?"points":"point") + " of mana,");

		if(curClass.getLevelsPerBonusDamage()!=0)
		{
			if((!gain)&&(((classLevel+1)%curClass.getLevelsPerBonusDamage())==0))
				mob.basePhyStats().setDamage(mob.basePhyStats().damage()-1);
			else
			if((gain)&&((classLevel%curClass.getLevelsPerBonusDamage())==0))
				mob.basePhyStats().setDamage(mob.basePhyStats().damage()+1);
		}
		mob.recoverMaxState();
		return theNews.toString();
	}

	@Override
	public void unLevel(final MOB mob)
	{
		if((mob.basePhyStats().level()<2)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		||(mob.charStats().getCurrentClass().leveless())
		||(mob.charStats().getMyRace().leveless()))
			return;
		int xpOverLast = mob.getExpNeededDelevel();
		if(xpOverLast < 0)
			xpOverLast = 0;
		final CMMsg msg=CMClass.getMsg(mob,CMMsg.MSG_LEVEL,null,mob.basePhyStats().level()-1);
		if(!CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_LEVEL,msg))
			return;

		mob.tell(L("^ZYou have ****LOST A LEVEL****^.^N\n\r\n\r@x1",CMLib.protocol().msp("doh.wav",60)));
		if(!mob.isMonster())
		{
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOSTLEVELS, mob);
			if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has just lost a level.",mob.Name()),true);
		}

		final int level = mob.basePhyStats().level();
		mob.setExperience(mob.getExperience() - mob.getExpNeededDelevel());
		final CharClass curClass=mob.baseCharStats().getCurrentClass();
		final int oldClassLevel=mob.baseCharStats().getClassLevel(curClass);
		int[] costGains = new int[CostDef.CostType.values().length];
		if(mob.playerStats() != null)
			costGains = Arrays.copyOf(mob.playerStats().leveledCostGains(level), costGains.length);
		doBaseLevelAdjustment(mob,costGains,false);
		int prac2Stat=mob.charStats().getStat(CharStats.STAT_WISDOM);
		final int maxPrac2Stat=(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_WISDOM)));
		if(prac2Stat>maxPrac2Stat)
			prac2Stat=maxPrac2Stat;
		int practiceGain=(int)Math.floor(CMath.div(prac2Stat,6.0))+curClass.getBonusPracLevel();
		if(costGains[CostDef.CostType.PRACTICE.ordinal()]!=0)
			practiceGain = costGains[CostDef.CostType.PRACTICE.ordinal()];
		if(practiceGain<=0)
			practiceGain=1;
		mob.setPractices(mob.getPractices()-practiceGain);
		int trainGain=0;
		if(costGains[CostDef.CostType.TRAIN.ordinal()]!=0)
			trainGain = costGains[CostDef.CostType.TRAIN.ordinal()];
		if(trainGain<=0)
			trainGain=1;
		mob.setTrains(mob.getTrains()-trainGain);

		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
		mob.tell(L("^HYou are now a level @x1 @x2^N.\n\r",""+mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),mob.charStats().getCurrentClass().name(mob.charStats().getCurrentClassLevel())));
		curClass.unLevel(mob);
		Ability A=null;
		final List<Ability> lose=new ArrayList<Ability>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			A=mob.fetchAbility(a);
			if((CMLib.ableMapper().getQualifyingLevel(curClass.ID(),false,A.ID())==oldClassLevel)
			&&(CMLib.ableMapper().getDefaultGain(curClass.ID(),false,A.ID()))
			&&(CMLib.ableMapper().classOnly(mob,curClass.ID(),A.ID())))
				lose.add(A);
		}
		for(int l=0;l<lose.size();l++)
		{
			A=lose.get(l);
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
		mob.setExperience(mob.getExperience()-mob.getExpNeededDelevel()+xpOverLast);
		mob.delExpertise(null); // clears the cache
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.LEVELSGAINED, -1, mob);
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CLASSLEVELSGAINED, -1, mob);
		if(mob.isPlayer()
		&&(!CMLib.flags().isInTheGame(mob, true)))
			CMLib.database().DBUpdatePlayerMOBOnly(mob);
	}

	@Override
	public void loseExperience(final MOB mob, final String sourceId, int amount)
	{
		if((mob==null)||(mob.soulMate()!=null))
			return;
		if(Log.combatChannelOn())
		{
			final String room=CMLib.map().getExtendedRoomID((mob.location()!=null)?mob.location():null);
			final String mobName=mob.Name();
			Log.killsOut("-EXP",room+":"+mobName+":"+amount);
		}
		amount = modGlobalExperience(mob,null,sourceId,amount);
		amount = loseLeigeExperience(mob, amount);
		amount = loseClanExperience(mob, amount);
		mob.setExperience(mob.getExperience()-amount);
		checkedLevelLosses(mob);
	}

	protected void checkedLevelLosses(final MOB mob)
	{
		int neededLowest=getLevelExperience(mob, mob.basePhyStats().level()-2);
		synchronized(mob) {} // does this really do anything?
		boolean checkAgain=true;
		while(checkAgain
		&&(mob.getExperience()<neededLowest)
		&&(mob.basePhyStats().level()>1)
		&&(neededLowest>0))
		{
			checkAgain=false;
			final int baseLevel = mob.basePhyStats().level();
			unLevel(mob);
			neededLowest=getLevelExperience(mob, mob.basePhyStats().level()-2);
			checkAgain = mob.basePhyStats().level() < baseLevel;
		}
	}

	protected int loseClanExperience(final MOB mob, int amount)
	{
		if((amount>2)
		&&(!mob.isMonster()))
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
						C.adjExp(mob, clanshare*-1);
						C.update();
					}
				}
			}
		}
		return amount;
	}

	protected int loseLeigeExperience(final MOB mob, final int amount)
	{
		if((mob.getLiegeID().length()>0)
		&&(amount>2)
		&&(!mob.isMonster()))
		{
			final MOB sire=CMLib.players().getPlayerAllHosts(mob.getLiegeID());
			if((sire!=null)&&(CMLib.flags().isInTheGame(sire,true)))
			{
				int sireShare=(int)Math.round(CMath.div(amount,10.0));
				if((sireShare=-postExperience(sire,"LIEGE:"+mob.name(),null,"",-sireShare, true))>0)
					sire.tell(L("^N^!You lose ^H@x1^N^! experience points from @x2.^N",""+sireShare,mob.Name()));
				return amount - sireShare;
			}
		}
		return amount;
	}

	protected int modGlobalExperience(final MOB mob, final MOB target, final String sourceId, int amount)
	{
		ModXP[] mods = this.xpMods;
		if(mods == null)
		{
			mods = parseXPMods(CMProps.getVar(CMProps.Str.XPMOD));
			this.xpMods=mods;
		}
		if(mods.length>0)
		{
			for(final ModXP m : mods)
				amount = this.handleXPMods(mob, target, m, sourceId, false, amount);
		}
		return amount;
	}

	@Override
	public void loseRPExperience(final MOB mob, final String sourceId, int amount)
	{
		if((mob==null)||(mob.soulMate()!=null))
			return;
		amount = modGlobalExperience(mob,null,sourceId,amount);
		amount = loseLeigeExperience(mob, amount);
		amount = loseClanExperience(mob, amount);
		mob.setExperience(mob.getExperience()-amount);
		checkedLevelLosses(mob);
	}

	protected int gainClanExperience(final MOB mob, int amount)
	{
		if(mob.phyStats().level()>=CMProps.getIntVar(CMProps.Int.MINCLANLEVEL))
		{
			for(final Pair<Clan,Integer> p : mob.clans())
			{
				if(amount>2)
					amount=p.first.applyExpMods(mob, amount);
			}
		}
		return amount;
	}

	protected int gainLeigeExperience(final MOB mob, final int amount, final boolean quiet)
	{
		if((mob.getLiegeID().length()>0)&&(amount>2))
		{
			final MOB sire=CMLib.players().getLoadPlayer(mob.getLiegeID());
			if(sire!=null)
			{
				int sireShare=(int)Math.round(CMath.div(amount,10.0));
				if(sireShare<=0)
					sireShare=1;
				postExperience(sire,"HOMAGE"+mob.Name(),null," from "+mob.name(sire),sireShare, quiet);
				return amount-sireShare;
			}
		}
		return amount;
	}

	protected void checkedLevelGains(final MOB mob)
	{
		if(mob == null)
			return;
		boolean checkAgain = true;
		while(checkAgain
		&&(mob.getExperience()>=mob.getExpNextLevel())
		&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
		{
			checkAgain=false;
			synchronized(CMClass.getSync(("SYSTEM_LEVELING_"+mob.Name())))
			{
				synchronized(mob) // does this really do anything?
				{}
				if((mob.getExperience()>=mob.getExpNextLevel())
				&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
				{
					final int prevLevel = mob.basePhyStats().level();
					level(mob);
					checkAgain = (mob.basePhyStats().level()>prevLevel);
				}
			}
		}
	}

	protected int translateAmount(int amount, final String val)
	{
		if(amount<0)
			amount=-amount;
		if(val.endsWith("%"))
			return (int)Math.round(CMath.mul(amount,CMath.div(CMath.s_int(val.substring(0,val.length()-1)),100)));
		return CMath.s_int(val);
	}

	protected String translateNumber(final String val)
	{
		if(val.endsWith("%"))
			return "( @x1 * (" + val.substring(0,val.length()-1) + " / 100) )";
		return Integer.toString(CMath.s_int(val));
	}

	@Override
	public int handleXPMods(final MOB mob, final MOB target,
							final ModXP m,
							final String sourceID, final boolean useTarget,
							final int amount)
	{
		if(m==null)
			return amount;
		if((m.targetOnly)
		&&((target==null)
			||(target==mob)))
			return amount;

		if((m.tmask.length()>0)
		&&(!CMStrings.matches(sourceID, m.tmask, false)))
			return amount;

		if(m.dir==ModXP.DirectionCheck.POSITIVE)
		{
			if(amount<0)
				return amount;
		}
		else
		if(m.dir==ModXP.DirectionCheck.NEGATIVE)
		{
			if(amount>0)
				return amount;
		}
		if(m.mask!=null)
		{
			if(useTarget
			&&(target != null))
			{
				if(!CMLib.masking().maskCheck(m.mask,target,true))
					return amount;
			}
			else
			if(!CMLib.masking().maskCheck(m.mask,mob,true))
				return amount;
		}
		return (int)CMath.parseMathExpression(m.operation, new double[]{amount}, 0.0);
	}

	@Override
	public ModXP[] parseXPMods(final String modStr)
	{
		final List<ModXP> newMods = new ArrayList<ModXP>();
		String newText = modStr.trim();
		while(newText.length()>0)
		{
			String s = newText;
			if(newText.startsWith("["))
			{
				int x=newText.indexOf(']');
				while(x>0)
				{
					final String ss=newText.substring(x+1);
					if(ss.trim().startsWith(","))
					{
						s=newText.substring(1,x);
						newText=newText.substring(newText.indexOf(',',x)+1).trim();
						break;
					}
					else
					if(ss.trim().length()==0)
					{
						s=newText.substring(1,x);
						newText="";
						break;
					}
					else
						x=newText.indexOf(']',x+1);
				}
				if(x<0)
					newText="";
			}
			else
				newText="";
			s=s.trim();
			if(s.length()==0)
				continue;
			final ModXP m = new ModXP();
			newMods.add(m);
			int x=s.lastIndexOf(";;");
			if(x>=0)
			{
				m.tmask=s.substring(x+2).trim();
				s=s.substring(0,x).trim();
			}
			x=s.indexOf(';');
			if(x>=0)
			{
				m.mask=CMLib.masking().getPreCompiledMask(s.substring(x+1).trim());
				s=s.substring(0,x).trim();
			}
			String us=s.toUpperCase();
			x=us.indexOf("SELF");
			if(x>=0)
			{
				m.selfXP=true;
				s=s.substring(0,x)+s.substring(x+4);
				us=s.toUpperCase();
			}
			x=us.indexOf("TARGET");
			if(x>=0)
			{
				m.targetOnly=true;
				s=s.substring(0,x)+s.substring(x+6);
				us=s.toUpperCase();
			}
			x=us.indexOf("RIDEOK");
			if(x>=0)
			{
				m.rideOK=true;
				s=s.substring(0,x)+s.substring(x+6);
				us=s.toUpperCase();
			}
			m.dir = ModXP.DirectionCheck.POSITIVE;
			for(final ModXP.DirectionCheck d : ModXP.DirectionCheck.values())
			{
				x=us.indexOf(d.name());
				if(x>=0)
				{
					m.dir = d;
					s=s.substring(0,x)+s.substring(x+d.name().length());
					us=s.toUpperCase();
				}
			}

			m.operationFormula="Amount "+s;
			final List<String> ops = new ArrayList<String>();
			int paren=0;
			final StringBuilder curr=new StringBuilder("");
			for(int i=0;i<s.length();i++)
			{
				if(paren > 0)
				{
					if(s.charAt('i')=='(')
					{
						if(paren == 0)
						{
							if(curr.length()>0)
								ops.add(curr.toString().trim());
							curr.setLength(0);
						}
						paren++;
					}
					else
					if(s.charAt('i')==')')
						paren--;
					curr.append(s.charAt(i));
				}
				else
				switch(s.charAt(i))
				{
				case '=':
				case '+':
				case '-':
				case '*':
				case '/':
					if(curr.length()>0)
						ops.add(curr.toString().trim());
					curr.setLength(0);
					curr.append(s.charAt(i));
					break;
				default:
					curr.append(s.charAt(i));
					break;
				}
			}
			if(curr.length()>0)
				ops.add(curr.toString().trim());
			StringBuilder finalOps = new StringBuilder("");
			for(final String op : ops)
			{
				if(op.startsWith("="))
					finalOps = new StringBuilder(translateNumber(op.substring(1)).trim());
				else
				if(op.startsWith("(")&&(op.endsWith(")")))
					finalOps = new StringBuilder(op);
				else
				if(op.startsWith("+")||op.startsWith("-")||op.startsWith("*")||op.startsWith("/"))
				{
					if(finalOps.length()==0)
						finalOps.append("@x1");
					finalOps.append(" ").append(op.charAt(0)).append(" ");
					finalOps.append(translateNumber(op.substring(1)).trim());
				}
				else
					finalOps=new StringBuilder(translateNumber(s.trim()));
			}
			if(finalOps.length()>0)
				m.operation = CMath.compileMathExpression(finalOps.toString());
			m.operationFormula=CMStrings.replaceAll(m.operationFormula, "@x1", "Amount");
		}
		return newMods.toArray(new ModXP[newMods.size()]);
	}

	@Override
	public void gainExperience(final MOB mob, final String sourceId, final MOB victim, String homageMessage, int amount, final boolean quiet)
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

		if(mob.charStats().getStat(CharStats.STAT_XP_ADJ_PCT)!=0)
		{
			amount += (int)Math.round(CMath.mul(CMath.div(mob.charStats().getStat(CharStats.STAT_XP_ADJ_PCT), 100.0),amount));
			if(amount<0)
				amount=0;
		}

		amount = modGlobalExperience(mob,victim,sourceId,amount);

		amount=adjustedExperience(mob,victim,amount);

		amount=gainClanExperience(mob, amount);

		if((victim != null) && (victim != mob))
			amount=gainLeigeExperience(mob, amount, quiet);

		CMLib.get(mob.session())._players().bumpPrideStat(mob,PrideStat.EXPERIENCE_GAINED, amount);
		if(homageMessage==null)
			homageMessage="";

		final PlayerStats pStats=mob.playerStats();
		if((pStats!=null)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		&&(!mob.charStats().getCurrentClass().expless())
		&&(!mob.charStats().getMyRace().expless())
		&&(mob.getExpNeededLevel()!=Integer.MAX_VALUE)
		&&(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0))
		{
			final long lastTime = pStats.getLastXPAwardMillis();
			final long nextTime = lastTime + (CMProps.getIntVar(CMProps.Int.EXPDEFER_SECS) * 1000L);
			if((CMProps.getVar(CMProps.Str.EXPDEFER_COMMAND).length()==0)
			&&(System.currentTimeMillis() > nextTime))
			{
				amount += pStats.getDeferredXP();
				pStats.setDeferredXP(0);
				amount += pStats.getRolePlayXP();
				pStats.setRolePlayXP(0);
			}
			else
			{
				if(pStats.getMaxDeferredXP()==0)
					ensureMaxDeferredXP(mob, pStats);
				if(pStats.getDeferredXP() + pStats.getRolePlayXP() < pStats.getMaxDeferredXP())
				{
					pStats.setDeferredXP(pStats.getDeferredXP() + amount);
					if(!quiet)
					{
						if(amount>1)
							mob.tell(L("^N^!You've earned ^H@x1^N^! deferred experience points@x2.^N",""+amount,homageMessage));
						else
						if(amount>0)
							mob.tell(L("^N^!You've earned ^H@x1^N^! deferred experience point@x2.^N",""+amount,homageMessage));
						if(((mob.getExperience()+pStats.getDeferredXP() + pStats.getRolePlayXP())>=mob.getExpNextLevel())
						&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
							mob.tell(L("^N^!You've earned enough experience to gain a level.^N",""+amount));
					}
				}
				//else
				//if(!quiet)
				//	mob.tell(L("^N^!You cannot defer any more experience for later.^N",""+amount));
				return;
			}
		}

		if((mob.basePhyStats().level() < CMProps.get(mob.session()).getInt(CMProps.Int.LASTPLAYERLEVEL))
		||(mob.getExperience()<getGainedExperienceCap(mob)))
		{
			mob.setExperience(mob.getExperience()+amount);
			if(pStats != null)
				pStats.setLastXPAwardMillis(System.currentTimeMillis());
			if(!quiet)
			{
				if(amount>1)
					mob.tell(L("^N^!You gain ^H@x1^N^! experience points@x2.^N",""+amount,homageMessage));
				else
				if(amount>0)
					mob.tell(L("^N^!You gain ^H@x1^N^! experience point@x2.^N",""+amount,homageMessage));
			}
		}
		checkedLevelGains(mob);
	}

	@Override
	public void gainRPExperience(final MOB mob, final String sourceId, final MOB target, final String homageMessage, int amount, final boolean quiet)
	{
		if(mob==null)
			return;

		amount=modGlobalExperience(mob,target,sourceId,amount);
		//amount=gainLeigeExperience(mob, amount, quiet);
		amount=gainClanExperience(mob, amount);

		CMLib.get(mob.session())._players().bumpPrideStat(mob,PrideStat.EXPERIENCE_GAINED, amount);
		final PlayerStats pStats=mob.playerStats();
		if((pStats!=null)
		&&(CMProps.getIntVar(CMProps.Int.RP_AWARD_PCT)>0)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		&&(!mob.charStats().getCurrentClass().expless())
		&&(!mob.charStats().getMyRace().expless())
		&&(mob.getExpNeededLevel()!=Integer.MAX_VALUE))
		{
			if(pStats.getMaxRolePlayXP()==0)
				ensureMaxRPXP(mob, pStats);
			if(pStats.getRolePlayXP() < pStats.getMaxRolePlayXP())
			{
				if(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0)
				{
					if(pStats.getMaxDeferredXP()==0)
						ensureMaxDeferredXP(mob, pStats);
					if(pStats.getDeferredXP() + pStats.getRolePlayXP() < pStats.getMaxDeferredXP())
					{
						pStats.setRolePlayXP(pStats.getRolePlayXP() + amount);
						final long lastTime = pStats.getLastXPAwardMillis();
						final long nextTime = lastTime + (CMProps.getIntVar(CMProps.Int.EXPDEFER_SECS) * 1000L);
						if((CMProps.getVar(CMProps.Str.EXPDEFER_COMMAND).length()==0)
						&&(System.currentTimeMillis() > nextTime))
						{
							amount += pStats.getDeferredXP();
							pStats.setDeferredXP(0);
							amount += pStats.getRolePlayXP();
							pStats.setRolePlayXP(0);
						}
						else
						{
							if(((mob.getExperience()+pStats.getDeferredXP()+pStats.getRolePlayXP())>=mob.getExpNextLevel())
							&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
								mob.tell(L("^N^!You've earned enough experience to gain a level.^N"));
							return;
						}
					}
					//else
					//if(!quiet)
					//	mob.tell(L("^N^!You can not defer any more experience for later.^N",""+amount));
				}
				else
					pStats.setRolePlayXP(pStats.getRolePlayXP() + amount);
			}
			else
				return;
		}

		if((mob.basePhyStats().level() < CMProps.get(mob.session()).getInt(CMProps.Int.LASTPLAYERLEVEL))
		||(mob.getExperience()<getGainedExperienceCap(mob)))
		{
			mob.setExperience(mob.getExperience()+amount);
			if(pStats!=null)
				pStats.setLastXPAwardMillis(System.currentTimeMillis());
			//if(homageMessage==null)
			//	homageMessage="";
			//if(!quiet)
			//	mob.tell(L("^N^!You gain ^H@x1^N^! roleplay XP@x2.^N",""+amount,homageMessage));
		}

		checkedLevelGains(mob);
	}

	@Override
	public int postExperience(final MOB mob,final String sourceID,final MOB victim,final String homage,final int amount, final boolean quiet)
	{
		if((mob==null)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		||mob.charStats().getCurrentClass().expless()
		||mob.charStats().getMyRace().expless())
			return 0;
		final Room R=mob.location();
		if(R!=null)
		{
			final CMMsg msg=CMClass.getMsg(mob,victim,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_EXPCHANGE,sourceID,CMMsg.NO_EFFECT,homage,CMMsg.NO_EFFECT,""+quiet);
			msg.setValue(amount);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				return msg.value();
			}
			else
				return 0;
		}
		else
		{
			if(amount>=0)
				gainExperience(mob,sourceID,victim,homage,amount, quiet);
			else
				loseExperience(mob,sourceID, -amount);
			return amount;
		}
	}

	@Override
	public boolean postRPExperience(final MOB mob, final String sourceID, final MOB target, final String homage, final int amount, final boolean quiet)
	{
		if((mob==null)
		||(CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		||mob.charStats().getCurrentClass().expless()
		||mob.charStats().getMyRace().expless()
		||(CMProps.getIntVar(CMProps.Int.RP_AWARD_PCT)<=0))
			return false;
		final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_RPXPCHANGE,sourceID,CMMsg.NO_EFFECT,homage,CMMsg.NO_EFFECT,""+quiet);
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
			gainRPExperience(mob,sourceID,target,homage,amount, quiet);
		else
			loseRPExperience(mob,sourceID, -amount);
		return true;
	}

	protected int getGainedExperienceCap(final MOB mob)
	{
		final char threadId;
		if(mob != null)
		{
			final Session sess=mob.session();
			if(sess != null)
				threadId=(char)sess.getGroupID();
			else
				threadId=Thread.currentThread().getThreadGroup().getName().charAt(0);
		}
		else
			threadId=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(experienceCaps[threadId] <= 0)
			experienceCaps[threadId] = (int)Math.round(CMath.mul(getLevelExperience(mob, CMProps.instance(threadId).getInt(CMProps.Int.LASTPLAYERLEVEL)), 1.02));
		return experienceCaps[threadId];
	}

	@Override
	public int getLevelExperience(final MOB mob, final int level)
	{
		if(level<0)
			return 0;
		final int[] levelingChart = CMProps.instance(mob)._getListFileIntList(CMProps.ListFile.EXP_CHART);
		if(level<levelingChart.length)
			return levelingChart[level];
		final int lastDiff=levelingChart[levelingChart.length-1] - levelingChart[levelingChart.length-2];
		return levelingChart[levelingChart.length-1] + ((1+(level-levelingChart.length)) * lastDiff);
	}

	@Override
	public int getLevelExperienceJustThisLevel(final MOB mob, final int level)
	{
		if(level<0)
			return 0;
		final int[] levelingChart = CMProps.instance(mob)._getListFileIntList(CMProps.ListFile.EXP_CHART);
		if(level==0)
			return levelingChart[0];
		else
		if(level<levelingChart.length)
			return levelingChart[level]-levelingChart[level-1];
		final int lastDiff=levelingChart[levelingChart.length-1] - levelingChart[levelingChart.length-2];
		return ((1+(level-levelingChart.length)) * lastDiff);
	}

	@Override
	public void level(final MOB mob)
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

		final int[] costGains = new int[CostDef.CostType.values().length];
		final String levelAdjustmentMsg = doBaseLevelAdjustment(mob,costGains,true);

		final StringBuilder theNews=new StringBuilder("^xYou have L E V E L E D ! ! ! ! ! ^.^N\n\r\n\r"+CMLib.protocol().msp("levelgain.wav",60));
		CharClass curClass=mob.baseCharStats().getCurrentClass();
		theNews.append(levelAdjustmentMsg);

		int prac2Stat=mob.charStats().getStat(CharStats.STAT_WISDOM);
		final int maxPrac2Stat=(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.CODES.toMAXBASE(CharStats.STAT_WISDOM)));
		if(prac2Stat>maxPrac2Stat)
			prac2Stat=maxPrac2Stat;
		int practiceGain=(int)Math.floor(CMath.div(prac2Stat,6.0))+curClass.getBonusPracLevel();
		if(practiceGain<=0)
			practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		costGains[CostDef.CostType.PRACTICE.ordinal()] = practiceGain;
		theNews.append(" ^H" + practiceGain+"^N practice " +
			( practiceGain != 1? "points" : "point" ) + ", ");

		int trainGain=1;
		if(trainGain<=0)
			trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		costGains[CostDef.CostType.TRAIN.ordinal()] = trainGain;
		theNews.append("and ^H"+trainGain+"^N training "+ (trainGain != 1? "sessions" : "session" )+".\n\r^N");

		if(mob.playerStats()!=null)
		{
			mob.playerStats().recordLevelData(mob.basePhyStats().level(),mob.getAgeMinutes(),room, costGains);
			final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DETAILEDLEVELS, mob);
			final List<String> channels2=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LEVELS, mob);
			channels2.removeAll(channels);
			if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has just gained a level at @x2.",mob.Name(),CMLib.map().getDescriptiveExtendedRoomID(room)),true);
			if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels2.size();i++)
				CMLib.commands().postChannel(channels2.get(i),mob.clans(),L("@x1 has just gained a level.",mob.Name()),true);
			if(mob.soulMate()==null)
				CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LEVELSGAINED);
			for(final Pair<Clan,Integer> p : mob.clans())
				p.first.bumpTrophyData(Trophy.PlayerLevelsGained, 1);
		}

		mob.tell(theNews.toString());
		curClass=mob.baseCharStats().getCurrentClass();
		final Set<String> oldAbilityIDs=new HashSet<String>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				oldAbilityIDs.add(A.ID());
		}
		final Map<String,Integer> oldExpertises=new TreeMap<String,Integer>();
		for(final Enumeration<String> e=mob.expertises();e.hasMoreElements();)
		{
			final Pair<String,Integer> pair = mob.fetchExpertise(e.nextElement());
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
			if((A!=null)&&(!oldAbilityIDs.contains(A.ID())))
				newAbilityIDs.add(A.ID());
		}

		for(final String newAbilityID : newAbilityIDs)
		{
			if(!oldAbilityIDs.contains(newAbilityID))
			{
				final Ability A=mob.fetchAbility(newAbilityID);
				if(A!=null)
				{
					final String type=Ability.ACODE.DESCS.get((A.classificationCode()&Ability.ALL_ACODES)).toLowerCase();
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
		final PlayerStats pStats=mob.playerStats();
		if(pStats!=null)
		{
			if(CMProps.getIntVar(CMProps.Int.RP_AWARD_PCT)>0)
			{
				if(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)==0)
					pStats.setRolePlayXP(0);
				ensureMaxRPXP(mob, pStats);
			}
			if(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT)>0)
				ensureMaxDeferredXP(mob, pStats);
		}

		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.LEVELSGAINED, 1, mob);
		CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CLASSLEVELSGAINED, 1, mob);
		if(mob.isPlayer()
		&&(!CMLib.flags().isInTheGame(mob, true)))
			CMLib.database().DBUpdatePlayerMOBOnly(mob);
	}

	protected void ensureMaxDeferredXP(final MOB mob, final PlayerStats pStats)
	{
		final double pct=CMath.div(CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT), 100.0);
		final int maxDeferXP=(int)Math.round(pct * mob.getExpNeededLevel());
		pStats.setMaxDeferredXP(maxDeferXP);
	}

	protected void ensureMaxRPXP(final MOB mob, final PlayerStats pStats)
	{
		final double pct=CMath.div(CMProps.getIntVar(CMProps.Int.RP_AWARD_PCT), 100.0);
		final int maxRpXP=(int)Math.round(pct * mob.getExpNeededLevel());
		pStats.setMaxRolePlayXP(maxRpXP);
	}

	protected boolean fixMobStatsIfNecessary(final MOB mob, final int direction)
	{
		if((mob.playerStats()==null)
		&&(mob.baseCharStats().getCurrentClass().name().equals("mob"))) // mob leveling
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
	public int getEffectFudgedLevel(final MOB mob)
	{
		if(mob != null)
		{
			int levels=mob.phyStats().level();
			final int effectCXL=CMProps.getIntVar(CMProps.Int.EFFECTCXL);
			if(effectCXL != 0)
			{
				final int triggerLevel=levels+CMProps.getIntVar(CMProps.Int.EXPRATE);
				for(final Enumeration<Ability> e = mob.effects();e.hasMoreElements();)
				{
					final Ability eA=e.nextElement();
					if((eA!=null)
					&&(eA.canBeUninvoked()))
					{
						final MOB invokerM=eA.invoker();
						if((invokerM!=null)
						&&(invokerM!=mob)
						&&(invokerM.phyStats().level()>triggerLevel)
						&&(!(invokerM instanceof Deity)))
						{
							if(eA.abstractQuality()==Ability.QUALITY_MALICIOUS)
								levels-=effectCXL;
							else
								levels+=effectCXL;
						}
					}
				}
			}
			return levels;
		}
		return 0;
	}
	@Override
	public int adjustedExperience(final MOB mob, final MOB victim, int amount)
	{
		final int killerLevel=getEffectFudgedLevel(mob);
		int highestLevelPC = killerLevel;
		final Room R=mob.location();
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
			if((R!=null)
			&&(CMLib.flags().getDirType(victim)==Directions.DirType.COMPASS))
			{
				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB M=R.fetchInhabitant(m);
					if((M!=null)
					&&(M!=mob)
					&&(M!=victim)
					&&(M.isPlayer()))
					{
						final int fudgedLevel=getEffectFudgedLevel(M);
						if(fudgedLevel>highestLevelPC)
							highestLevelPC = fudgedLevel;
					}
				}
			}
			final int vicLevel=getEffectFudgedLevel(victim);
			final double levelLimit=CMProps.getIntVar(CMProps.Int.EXPRATE);
			final double levelDiff=vicLevel-killerLevel;

			if(levelDiff<(-levelLimit) )
				amount=0;
			else
			if(levelLimit>0)
			{
				final int highVicDiff=highestLevelPC - vicLevel;
				final int highPartyDiff=highestLevelPC - killerLevel;
				if(highVicDiff>levelLimit)
					amount=(int)Math.round(CMath.mul(amount,CMath.div(levelLimit*levelLimit,highVicDiff*highVicDiff)));
				if(highPartyDiff>levelLimit)
					amount=(int)Math.round(CMath.mul(amount,CMath.div(levelLimit*levelLimit,highPartyDiff*highPartyDiff)));
				else
				if((highPartyDiff<=levelLimit)
				||(levelDiff<0))
				{
					double levelFactor=levelDiff / levelLimit;
					if( levelFactor > levelLimit )
						levelFactor = levelLimit;
					amount+=(int)Math.round(levelFactor *  amount);
				}
			}
		}

		return amount;
	}

	@Override
	public void handleExperienceChange(final CMMsg msg)
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
							   msg.sourceMessage(),
							   expFromKilledmob,
							   msg.targetMessage(),
							   msg.value(), CMath.s_bool(msg.othersMessage()));
			}
			else
				loseExperience(mob, msg.sourceMessage(), -msg.value());
		}
	}

	@Override
	public Command deferCommandCheck(final MOB mob, final Command C, final List<String> cmds)
	{
		if(mob.isPlayer() && (CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT) >0))
		{
			final PlayerStats pStats=mob.playerStats();
			final long lastTime = pStats.getLastXPAwardMillis();
			final long nextTime = lastTime + (CMProps.getIntVar(CMProps.Int.EXPDEFER_SECS) * 1000L);
			final String pcmd = CMProps.getVar(CMProps.Str.EXPDEFER_COMMAND);
			if(pcmd.length()>0)
			{
				if(C != null)
				{
					if(!CMStrings.contains(C.getAccessWords(),pcmd))
						return C;
				}
				else
				if((cmds != null)&&(cmds.size()>0))
				{
					if(!pcmd.equalsIgnoreCase(cmds.get(0)))
						return C;
				}
				final String parg = CMProps.getVar(CMProps.Str.EXPDEFER_ARGUMENT);
				if(parg.length() == 0)
				{
					if((cmds!=null)&&(cmds.size()>1))
						return C;
				}
				else
				if(!parg.equals("*"))
				{
					final String comb=CMParms.combine(cmds,1);
					if(parg.startsWith("*"))
					{
						if(!comb.toLowerCase().endsWith(parg.substring(1).toLowerCase()))
							return C;
					}
					else
					if(parg.endsWith("*"))
					{
						if(!comb.toLowerCase().startsWith(parg.substring(0,parg.length()-1).toLowerCase()))
							return C;
					}
					else
					if(!comb.equalsIgnoreCase(parg))
						return C;
				}
				if(System.currentTimeMillis() < nextTime)
				{
					final Area A=CMLib.map().areaLocation(mob);
					String diffStr=L("a later time");
					if(A!=null)
					{
						final TimeClock C2=A.getTimeObj().deriveClock(nextTime);
						diffStr = C2.getShortTimeDescription();
						final String nowStr = A.getTimeObj().getShortTimeDescription();
						if(nowStr.equals(diffStr))
							diffStr = null;
					}
					if(diffStr != null)
					{
						mob.tell(L("You can not be awarded more experience until @x1.",diffStr));
						return C;
					}
				}
			}
			else
			if(System.currentTimeMillis() < nextTime)
				return C;
			if(CMProps.getVar(CMProps.Str.EXPDEFER_MASK).length()>0)
			{
				final Room R=mob.location();
				boolean found=false;
				if(R.numInhabitants()>1)
				{
					String mask=CMProps.getVar(CMProps.Str.EXPDEFER_MASK);
					int x=mask.indexOf('{');
					while(x>0)
					{
						final int y=mask.indexOf('}',x+1);
						if(y>x)
						{
							final String tag=mask.substring(x+1, y);
							mask=mask.substring(0,x)+CMLib.coffeeMaker().getAnyGenStat(mob, tag)+mask.substring(y+1);
						}
						x=mask.indexOf('{',x+1);
					}
					final MaskingLibrary lib=CMLib.masking();
					final MaskingLibrary.CompiledZMask fmask=lib.getPreCompiledMask(mask);
					for(final Enumeration<MOB> r=R.inhabitants();r.hasMoreElements();)
					{
						final MOB M=r.nextElement();
						found = found || lib.maskCheck(fmask, M, true);
					}
				}
				if(!found)
					return C;
			}
			final Command deferC=(Command)CMClass.getCommand("DeferCmd").newInstance();
			if(CMProps.getBoolVar(Bool.EXPDEFER_PASSTHRU))
			{
				try
				{
					deferC.executeInternal(mob, 0, C);
				}
				catch (final IOException e)
				{
				}
			}
			return deferC;
		}
		return C;
	}

	@Override
	public void handleRPExperienceChange(final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE)
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			MOB expFromTarget=null;
			if(msg.target() instanceof MOB)
				expFromTarget=(MOB)msg.target();

			if(msg.value()>=0)
			{
				gainRPExperience(mob,
							   msg.sourceMessage(),
							   expFromTarget,
							   msg.targetMessage(),
							   msg.value(), CMath.s_bool(msg.othersMessage()));
			}
			else
				loseRPExperience(mob,msg.sourceMessage(), -msg.value());
		}
	}

	@Override
	public boolean postExperienceToAllAboard(final Physical possibleShip, final String sourceID, final int amount, final Physical target)
	{
		boolean posted = false;
		if(possibleShip instanceof Boardable)
		{
			boolean destroyTargetMob=false;
			final MOB targetM;
			if(target instanceof MOB)
				targetM=(MOB)target;
			else
			if(target==null)
				targetM=null;
			else
			{
				targetM=CMClass.getFactoryMOB(target.Name(), target.phyStats().level(), null);
				if(target instanceof Rideable)
				{
					synchronized(target)
					{
						targetM.setRiding((Rideable)target);
					}
				}
				destroyTargetMob=true;
			}
			try
			{
				final Area A=((Boardable)possibleShip).getArea();
				if(A!=null)
				{
					posted = true;
					for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if(R!=null)
						{
							for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if(targetM != null)
								{
									targetM.basePhyStats().setLevel(M.phyStats().level());
									targetM.phyStats().setLevel(M.phyStats().level());
								}
								posted = (postExperience(M, sourceID, targetM, null, amount, false)>0) && posted;
							}
						}
					}
				}
			}
			finally
			{
				if(destroyTargetMob
				&&(targetM!=null))
				{
					if(target != null)
					{
						synchronized(target)
						{
							targetM.setRiding(null);
						}
					}
					targetM.destroy();
				}
			}
		}
		return posted;
	}

	@Override
	public void propertiesLoaded()
	{
		Arrays.fill(experienceCaps, 0);
		this.xpMods = null;
	}
}
