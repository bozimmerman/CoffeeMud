package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.EachApplicable.ApplyAffectPhyStats;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.CharStatOutOfRangeException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.State;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FData;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.PlayerCombatStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;

/*
 Copyright 2000-2024 Bo Zimmerman

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, e\ither express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
//STOP-ECLIPSE-FORMATTING
// @formatter: off
public class StdMOB implements MOB
{
	@Override
	public String ID()
	{
		return "StdMOB";
	}

	public String				_name				= "";

	protected CharStats			baseCharStats		= (CharStats) CMClass.getCommon("DefaultCharStats");
	protected CharStats			charStats			= (CharStats) CMClass.getCommon("DefaultCharStats");

	protected PhyStats			phyStats			= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected PhyStats			basePhyStats		= (PhyStats) CMClass.getCommon("DefaultPhyStats");

	protected PlayerStats		playerStats			= null;

	protected Triggerer			triggerer			= (Triggerer) CMClass.getCommon("NonTriggerer");

	protected boolean			amDestroyed			= false;
	protected boolean			removeFromGame		= false;
	protected volatile boolean	amDead				= false;

	protected volatile Room		location			= null;
	protected volatile Room		lastLocation		= null;
	protected Rideable			riding				= null;

	protected volatile Session	mySession			= null;
	protected Object			description			= null;
	protected String			displayText			= "";
	protected String			rawImageName		= null;
	protected String			cachedImageName		= null;
	protected Object			miscText			= null;
	protected String[]			xtraValues			= null;

	// gained attributes
	protected int				experience			= 0;
	protected int				practices			= 0;
	protected int				trains				= 0;
	protected long				ageMinutes			= 0;
	protected int				money				= 0;
	protected double			moneyVariation		= 0.0;
	protected double			speedAdj			= CMProps.getSpeedAdjustment();
	protected long				attributesBitmap	= MOB.Attrib.NOTEACH.getBitCode();
	protected String			databaseID			= "";

	protected int				tickAgeCounter		= 0;
	protected int				recoverTickCter		= 1;
	protected int				validChkCounter		= 60;
	private long				expirationDate		= 0;
	private int					manaConsumeCter		= CMLib.dice().roll(1, 10, 0);
	private volatile double		freeActions			= 0.0;

	// the core state values
	public CharState			curState			= (CharState) CMClass.getCommon("DefaultCharState");
	public CharState			maxState			= (CharState) CMClass.getCommon("DefaultCharState");
	public CharState			baseState			= (CharState) CMClass.getCommon("DefaultCharState");
	private long				lastTickedTime		= 0;
	private long				lastCommandTime		= System.currentTimeMillis();
	protected Room				possStartRoom		= null;
	protected String			liegeID				= "";
	protected int				wimpHitPoint		= 0;
	protected int				questPoint			= 0;
	protected MOB				victim				= null;
	protected Followable<MOB>	amFollowing			= null;
	protected MOB				soulMate			= null;
	protected int				atRange				= -1;
	protected long				peaceTime			= 0;
	protected boolean			kickFlag			= false;
	protected MOB				me					= this;

	protected int				tickStatus			= Tickable.STATUS_NOT;

	/* containers of items and attributes */
	protected SVector<Item>					inventory			= new SVector<Item>(1);
	protected CMUniqSortSVec<Ability>		abilitys			= new CMUniqSortSVec<Ability>(1);
	protected int[]							abilityUseTrig		= new int[Ability.USAGEINDEX_TOTAL];
	protected STreeMap<String, int[][]>		abilityUseCache		= new STreeMap<String, int[][]>();
	protected STreeMap<String, Integer>		expertises			= new STreeMap<String, Integer>();
	protected SVector<Ability>				affects				= new SVector<Ability>(1);
	protected CMUniqSortSVec<Behavior>		behaviors			= new CMUniqSortSVec<Behavior>(1);
	protected CMUniqNameSortSVec<Tattoo>	tattoos				= new CMUniqNameSortSVec<Tattoo>(1);
	protected volatile PairList<MOB, Short>	followers			= null;
	protected volatile int					followOrder			= -1;
	protected LinkedList<QMCommand>			commandQue			= new LinkedList<QMCommand>();
	protected SVector<ScriptingEngine>		scripts				= new SVector<ScriptingEngine>(1);
	protected volatile List<Ability>		racialAffects		= null;
	protected volatile List<Ability>		clanAffects			= null;
	protected SHashtable<String, FData>		factions			= new SHashtable<String, FData>(1);
	protected volatile WeakReference<Item>	possWieldedItem		= null;
	protected volatile WeakReference<Item>	possHeldItem		= null;

	protected ApplyAffectPhyStats<Ability>	affectPhyStats		= new ApplyAffectPhyStats<Ability>(this);
	protected ApplyRecAffectPhyStats<Item>	recoverAffectP		= new ApplyRecAffectPhyStats<Item>(this);
	@SuppressWarnings("rawtypes")
	protected ApplyAffectCharStats			affectCharStats		= new ApplyAffectCharStats(this);
	@SuppressWarnings("rawtypes")
	protected ApplyAffectCharState			affectCharState		= new ApplyAffectCharState(this);

	protected OrderedMap<String, Pair<Clan, Integer>>	clans	= new OrderedMap<String, Pair<Clan, Integer>>();

	public StdMOB()
	{
		super();
		// CMClass.bumpCounter(this,CMClass.CMObjectType.MOB);//removed for mem
		// & perf
		Race R = CMClass.getRace("Human");
		if(R == null)
			R = CMClass.getRace("StdRace");
		baseCharStats().setMyRace(R);
		basePhyStats().setLevel(1);
		basePhyStats().setAbility(CMProps.getMobHPBase());
		speedAdj = CMProps.getSpeedAdjustment();
		xtraValues = CMProps.getExtraStatCodesHolder(this);
	}

	private static class QMCommand
	{
		public Object		commandObj		= null;
		public double		actionCost		= 0.0;
		public long			nextCheck		= System.currentTimeMillis() - 1;
		public int			seconds			= -1;
		public int			metaFlags		= 0;
		public List<String>	commandVector	= null;
	}

	private static final Converter<Pair<MOB, Short>, MOB>	FollowerConverter	= new Pair.FirstConverter<MOB, Short>();

	/**
	 * EachApplicable class that recovers item and affect phyStats
	 */
	public static final class ApplyRecAffectPhyStats<T extends StatsAffecting> extends ApplyAffectPhyStats<T>
	{
		public ApplyRecAffectPhyStats(final Physical me)
		{
			super(me);
		}

		@Override
		public void apply(final T a)
		{
			((Affectable) a).recoverPhyStats();
			super.apply(a);
		}
	}

	/**
	 * EachApplicable class that affect charStats
	 */
	public static class ApplyAffectCharStats<T extends StatsAffecting> implements EachApplicable<T>
	{
		protected final MOB	me;

		public ApplyAffectCharStats(final MOB me)
		{
			this.me = me;
		}

		@Override
		public void apply(final T a)
		{
			a.affectCharStats(me, me.charStats());
		}
	}

	/**
	 * EachApplicable class that affect charState
	 */
	public static class ApplyAffectCharState<T extends StatsAffecting> implements EachApplicable<T>
	{
		protected final MOB	me;

		public ApplyAffectCharState(final MOB me)
		{
			this.me = me;
		}

		@Override
		public void apply(final T a)
		{
			a.affectCharState(me, me.maxState());
		}
	}

	@Override
	public long lastTickedDateTime()
	{
		return lastTickedTime;
	}

	@Override
	public void flagVariableEq()
	{
		lastTickedTime = -3;
	}

	@Override
	public long getAgeMinutes()
	{
		return ageMinutes;
	}

	@Override
	public int getPractices()
	{
		return practices;
	}

	@Override
	public int getExperience()
	{
		return experience;
	}

	@Override
	public int getExpNextLevel()
	{
		return CMLib.leveler().getLevelExperience(this, basePhyStats().level());
	}

	@Override
	public int getExpPrevLevel()
	{
		if(basePhyStats().level() <= 1)
			return 0;
		final int neededLowest = CMLib.leveler().getLevelExperience(this, basePhyStats().level() - 1);
		return neededLowest;
	}

	@Override
	public int getExpNeededDelevel()
	{
		if(basePhyStats().level() <= 1)
			return 0;
		if((CMSecurity.instance(session())._isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		|| (charStats().getCurrentClass().expless())
		|| (charStats().getMyRace().expless()))
			return 0;
		int ExpPrevLevel = getExpPrevLevel();
		if(ExpPrevLevel > getExperience())
			ExpPrevLevel = getExperience() - 1000;
		return getExperience() - ExpPrevLevel;
	}

	@Override
	public int getExpNeededLevel()
	{
		final int lastPlayerLevel = CMProps.get(session()).getInt(CMProps.Int.LASTPLAYERLEVEL);
		if((lastPlayerLevel > 0) && (lastPlayerLevel <= basePhyStats().level()))
			return Integer.MAX_VALUE;
		if((CMSecurity.instance(session())._isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		|| (charStats().getCurrentClass().expless())
		|| (charStats().getMyRace().expless()))
			return Integer.MAX_VALUE;
		int expNextLevel = getExpNextLevel();
		if(expNextLevel < getExperience())
			expNextLevel = getExperience() + 1;
		return expNextLevel - getExperience();
	}

	@Override
	public int getTrains()
	{
		return trains;
	}

	@Override
	public int getMoney()
	{
		return money;
	}

	@Override
	public double getMoneyVariation()
	{
		return moneyVariation;
	}

	@Override
	public long getAttributesBitmap()
	{
		return attributesBitmap;
	}

	@Override
	public void setAgeMinutes(final long newVal)
	{
		ageMinutes = newVal;
	}

	@Override
	public void setExperience(final int newVal)
	{
		if((newVal > experience) && (this.playerStats() != null))
		{
			this.playerStats().bumpLevelCombatStat(PlayerCombatStat.EXPERIENCE_TOTAL, basePhyStats().level(), newVal - experience);
			this.playerStats().setLastXPAwardMillis(System.currentTimeMillis());
		}
		experience = newVal;
	}

	@Override
	public void setPractices(final int newVal)
	{
		practices = newVal;
	}

	@Override
	public void setTrains(final int newVal)
	{
		trains = newVal;
	}

	@Override
	public void setMoney(final int newVal)
	{
		money = newVal;
	}

	@Override
	public void setMoneyVariation(final double newVal)
	{
		moneyVariation = newVal;
	}

	@Override
	public void setAttributesBitmap(final long bitmap)
	{
		this.attributesBitmap = bitmap;
	}

	@Override
	public void setAttribute(final Attrib attrib, final boolean set)
	{
		if(set)
		{
			attributesBitmap = attributesBitmap | attrib.getBitCode();
		}
		else
		{
			attributesBitmap = attributesBitmap & ~attrib.getBitCode();
		}
	}

	@Override
	public boolean isAttributeSet(final MOB.Attrib attrib)
	{
		return (attributesBitmap & attrib.getBitCode()) != 0;
	}

	@Override
	public String getFactionListing()
	{
		final StringBuffer msg = new StringBuffer();
		for(final Iterator<String> e = new XTreeSet<String>(factions()).iterator(); e.hasNext();)
		{
			final Faction F = CMLib.factions().getFaction(e.next());
			if(F != null)
				msg.append(F.name() + "(" + fetchFaction(F.factionID()) + ");");
		}
		return msg.toString();
	}

	@Override
	public String getLiegeID()
	{
		return liegeID;
	}

	@Override
	public int getWimpHitPoint()
	{
		return wimpHitPoint;
	}

	@Override
	public int getQuestPoint()
	{
		return questPoint;
	}

	@Override
	public void setLiegeID(final String newVal)
	{
		liegeID = newVal;
	}

	@Override
	public void setWimpHitPoint(final int newVal)
	{
		wimpHitPoint = newVal;
	}

	@Override
	public void setQuestPoint(final int newVal)
	{
		questPoint = newVal;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new StdMOB();
	}

	@Override
	public Room getStartRoom()
	{
		return CMLib.map().getRoom(possStartRoom);
	}

	@Override
	public void setStartRoom(final Room room)
	{
		possStartRoom = room;
	}

	@Override
	public void setDatabaseID(final String id)
	{
		databaseID = id;
	}

	@Override
	public boolean canSaveDatabaseID()
	{
		return true;
	}

	@Override
	public String databaseID()
	{
		return databaseID;
	}

	@Override
	public String Name()
	{
		return _name;
	}

	@Override
	public void setName(final String newName)
	{
		_name = newName;
	}

	@Override
	public String name()
	{
		if(phyStats().newName() != null)
			return phyStats().newName();
		return _name;
	}

	@Override
	public String titledName()
	{
		if(playerStats == null)
			return name();
		return CMStrings.replaceAll(playerStats.getActiveTitle(), "*", Name());
	}

	@Override
	public String genericName()
	{
		if((charStats().getStat(CharStats.STAT_AGE) > 0) && (!CMSecurity.isDisabled(CMSecurity.DisFlag.ALL_AGEING)))
			return charStats().ageName().toLowerCase() + " " + charStats().raceName().toLowerCase();
		return charStats().raceName().toLowerCase();
	}

	@Override
	public String image()
	{
		if(cachedImageName == null)
		{
			if((rawImageName != null)
			&& (rawImageName.length() > 0))
				cachedImageName = rawImageName;
			else
				cachedImageName = CMLib.protocol().getDefaultMXPImage(this);
		}
		if(!baseCharStats().getMyRace().name().equalsIgnoreCase(charStats().raceName()))
			return CMLib.protocol().getDefaultMXPImage(this);
		if(cachedImageName == null)
			return "";
		return cachedImageName;
	}

	@Override
	public String rawImage()
	{
		if(rawImageName == null)
			return "";
		return rawImageName;
	}

	@Override
	public void setImage(final String newImage)
	{
		if((newImage == null)
		|| (newImage.trim().length() == 0))
			rawImageName = null;
		else
			rawImageName = newImage;
		if((cachedImageName != null)
		&& (!cachedImageName.equals(newImage)))
			cachedImageName = null;
	}

	@Override
	public long expirationDate()
	{
		return expirationDate;
	}

	@Override
	public void setExpirationDate(final long time)
	{
		expirationDate = time;
	}

	// protected void finalize()
	// CMClass.unbumpCounter(this,CMClass.CMObjectType.MOB); }//removed for mem
	// & perf
	@Override
	public final boolean amDestroyed()
	{
		return this.amDestroyed;
	}

	protected final void setDestroyed(final boolean truefalse)
	{
		this.amDestroyed = truefalse;
	}

	@SuppressWarnings("rawtypes")
	protected void cloneFix(final MOB M)
	{
		if(M == null)
			return;
		synchronized(this)
		{
			me = this;
		}
		synchronized(M)
		{
			if(!isGeneric())
			{
				final PhyStats oldBase = basePhyStats;
				basePhyStats = (PhyStats) M.basePhyStats().copyOf();
				phyStats = (PhyStats) M.phyStats().copyOf();
				basePhyStats.setAbility(oldBase.ability());
				basePhyStats.setRejuv(oldBase.rejuv());
				basePhyStats.setLevel(oldBase.level());
				phyStats.setAbility(oldBase.ability());
				phyStats.setRejuv(oldBase.rejuv());
				phyStats.setLevel(oldBase.level());
			}
			else
			{
				basePhyStats = (PhyStats) M.basePhyStats().copyOf();
				phyStats = (PhyStats) M.phyStats().copyOf();
			}
		}

		affectPhyStats = new ApplyAffectPhyStats<Ability>(this);
		recoverAffectP = new ApplyRecAffectPhyStats<Item>(this);
		affectCharStats = new ApplyAffectCharStats(this);
		affectCharState = new ApplyAffectCharState(this);

		affects = new SVector<Ability>();
		baseCharStats = (CharStats) M.baseCharStats().copyOf();
		charStats = (CharStats) M.charStats().copyOf();
		baseState = (CharState) M.baseState().copyOf();
		curState = (CharState) M.curState().copyOf();
		maxState = (CharState) M.maxState().copyOf();
		removeFromGame = false;

		inventory = new SVector<Item>();
		abilitys = new CMUniqSortSVec<Ability>();
		abilityUseTrig = new int[Ability.USAGEINDEX_TOTAL];
		abilityUseCache = new STreeMap<String, int[][]>();
		behaviors = new CMUniqSortSVec<Behavior>();
		tattoos = new CMUniqNameSortSVec<Tattoo>();
		expertises = new STreeMap<String, Integer>();
		followers = null;
		followOrder = -1;
		commandQue = new LinkedList<QMCommand>();
		scripts = new SVector<ScriptingEngine>();
		racialAffects = null;
		clanAffects = null;
		factions = new SHashtable<String, FData>(1);
		possWieldedItem = null;
		possHeldItem = null;
		clans = new OrderedMap<String, Pair<Clan, Integer>>();

		for(final Pair<Clan, Integer> p : M.clans())
		{
			setClan(p.first.clanID(), p.second.intValue());
		}
		for(final Enumeration<String> e = M.factions(); e.hasMoreElements();)
		{
			final String fac = e.nextElement();
			addFaction(fac, M.fetchFaction(fac));
		}
		for(final Enumeration<Tattoo> e = M.tattoos(); e.hasMoreElements();)
		{
			final Tattoo t = e.nextElement();
			addTattoo((Tattoo) t.copyOf());
		}
		for(final Enumeration<String> s = M.expertises(); s.hasMoreElements();)
			addExpertise(s.nextElement());

		Item I = null;
		for(int i = 0; i < M.numItems(); i++)
		{
			I = M.getItem(i);
			if(I != null)
				addItem((Item) I.copyOf());
		}
		Item I2 = null;
		for(int i = 0; i < numItems(); i++)
		{
			I = getItem(i);
			if((I != null)
			&& (I.container() != null)
			&& (!isMine(I.container())))
			{
				for(final Enumeration<Item> e = M.items(); e.hasMoreElements();)
				{
					I2 = e.nextElement();
					if((I2 == I.container())
					&& (I2 instanceof Container))
					{
						I.setContainer((Container) I2);
						break;
					}
				}
			}
		}
		Ability A = null;
		for(int i = 0; i < M.numAbilities(); i++)
		{
			A = M.fetchAbility(i);
			if(A != null)
				addAbility((Ability) A.copyOf());
		}
		try
		{
			setDestroyed(true);
			for(final Enumeration<Ability> a = M.personalEffects(); a.hasMoreElements();)
			{
				A = a.nextElement();
				if(A != null)
				{
					final MOB oldInvoker = A.invoker();
					A = (Ability) A.copyOf();
					addEffect(A);
					if(A.canBeUninvoked())
					{
						A.unInvoke();
						delEffect(A);
					}
					else
					if(oldInvoker==M)
						A.setInvoker(this);
				}
			}
		}
		finally
		{
			setDestroyed(false);
		}
		for(final Enumeration<Behavior> e = M.behaviors(); e.hasMoreElements();)
		{
			final Behavior B = e.nextElement();
			if(B != null) // iteration during a clone would just be messed up.
				behaviors.addElement((Behavior) B.copyOf());
		}
		ScriptingEngine SE = null;
		for(final Enumeration<ScriptingEngine> e = M.scripts(); e.hasMoreElements();)
		{
			SE = e.nextElement();
			if(SE != null)
				addScript((ScriptingEngine) SE.copyOf());
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdMOB E = (StdMOB) this.clone();
			// CMClass.bumpCounter(E,CMClass.CMObjectType.MOB);//removed for mem
			// & perf
			E.xtraValues = (xtraValues == null) ? null : (String[]) xtraValues.clone();
			E.cloneFix(this);
			CMLib.catalog().newInstance(this);
			return E;
		}
		catch (final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public PhyStats phyStats()
	{
		return phyStats;
	}

	@Override
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}

	@Override
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		if(speedAdj!=1.0)
		{
			final double baseSpeed=phyStats.speed();
			if(baseSpeed>1.0)
				phyStats.setSpeed(1.0+((baseSpeed-1.0)*speedAdj));
		}
		phyStats.setWeight(phyStats.weight() + charStats.getStat(CharStats.STAT_WEIGHTADJ));
		if(location() != null)
			location().affectPhyStats(this, phyStats);
		if(getMoney() > 0)
			phyStats().setWeight(phyStats().weight() + (getMoney() / 100));
		final Rideable riding = riding();
		if(riding != null)
			riding.affectPhyStats(this, phyStats);
		final Deity deity = charStats().getMyDeity();
		if(deity != null)
			deity.affectPhyStats(this, phyStats);
		final CharStats cStats = charStats;
		if(cStats != null)
		{
			for(int c = 0; c < cStats.numClasses(); c++)
				cStats.getMyClass(c).affectPhyStats(this, phyStats);
			cStats.getMyRace().affectPhyStats(this, phyStats);
		}
		eachItem(recoverAffectP);
		eachEffect(affectPhyStats);
		for(final Enumeration<FData> e = factions.elements(); e.hasMoreElements();)
			e.nextElement().affectPhyStats(this, phyStats);
		/* the follower light exception -- BUT WHY?  This is WEIRD! And location doesn't matter?! */
		if((numFollowers()>0)&&(!CMLib.flags().isLightSource(this)))
		{
			for(final Enumeration<Pair<MOB, Short>> f = followers(); f.hasMoreElements();)
			{
				final Pair<MOB, Short> F = f.nextElement();
				if(CMLib.flags().isLightSource(F.first))
					phyStats.setDisposition(phyStats().disposition() | PhyStats.IS_LIGHTSOURCE);
			}
		}
	}

	@Override
	public void setBasePhyStats(final PhyStats newStats)
	{
		basePhyStats = (PhyStats) newStats.copyOf();
	}

	@Override
	public int baseWeight()
	{
		if(charStats().getMyRace() == baseCharStats().getMyRace())
			return basePhyStats().weight() + charStats().getStat(CharStats.STAT_WEIGHTADJ);
		return charStats().getMyRace().lightestWeight() + charStats().getStat(CharStats.STAT_WEIGHTADJ) + charStats().getMyRace().weightVariance();
	}

	@Override
	public int maxCarry()
	{
		return CMLib.login().getMaxCarry(this);
	}

	@Override
	public int maxItems()
	{
		return CMLib.login().getMaxItems(this);
	}

	@Override
	public int maxFollowers()
	{
		return CMLib.login().getMaxFollowers(this);
	}

	@Override
	public int totalFollowers()
	{
		int total = 0;
		try
		{
			final PairList<MOB, Short> followers = this.followers;
			if(followers != null)
			{
				for(final Iterator<Pair<MOB, Short>> f = followers.iterator(); f.hasNext();)
				{
					final MOB F = f.next().first;
					if(F != this)
						total += (1 + F.totalFollowers());
					else
						f.remove();
				}
			}
		}
		catch (final Exception t)
		{
		}
		return total;
	}

	@Override
	public Triggerer triggerer()
	{
		return triggerer;
	}

	@Override
	public void setTriggerer(final Triggerer triggerer)
	{
		if(triggerer != null)
			this.triggerer = triggerer;
	}

	@Override
	public CharStats baseCharStats()
	{
		return baseCharStats;
	}

	@Override
	public CharStats charStats()
	{
		return charStats;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void recoverCharStats()
	{
		baseCharStats.setClassLevel(baseCharStats.getCurrentClass(), basePhyStats().level() - baseCharStats().combinedSubLevels());
		baseCharStats.copyInto(charStats);

		final Rideable riding = riding();
		if(riding != null)
			riding.affectCharStats(this, charStats);
		final Deity deity = baseCharStats.getMyDeity();
		if(deity != null)
			deity.affectCharStats(this, charStats);

		final int num = charStats.numClasses();
		for(int c = 0; c < num; c++)
			charStats.getMyClass(c).affectCharStats(this, charStats);
		charStats.getMyRace().affectCharStats(this, charStats);
		baseCharStats.getMyRace().agingAffects(this, baseCharStats, charStats);
		eachEffect(affectCharStats);
		eachItem(affectCharStats);
		if(location() != null)
			location().affectCharStats(this, charStats);
		for(final Enumeration<FData> e = factions.elements(); e.hasMoreElements();)
			e.nextElement().affectCharStats(this, charStats);
		if((playerStats != null) && (soulMate == null) && (playerStats.getHygiene() >= PlayerStats.HYGIENE_DELIMIT))
		{
			final int chaAdjust = (int) (playerStats.getHygiene() / PlayerStats.HYGIENE_DELIMIT);
			if((charStats.getStat(CharStats.STAT_CHARISMA) / 2) > chaAdjust)
				charStats.setStat(CharStats.STAT_CHARISMA, charStats.getStat(CharStats.STAT_CHARISMA) - chaAdjust);
			else
				charStats.setStat(CharStats.STAT_CHARISMA, charStats.getStat(CharStats.STAT_CHARISMA) / 2);
		}
	}

	@Override
	public void setBaseCharStats(final CharStats newBaseCharStats)
	{
		baseCharStats = (CharStats) newBaseCharStats.copyOf();
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(affected instanceof Room)
		{
			if(CMLib.flags().isLightSource(this))
				affectableStats.setDisposition((affectableStats.disposition() & ~PhyStats.IS_DARK) | PhyStats.IS_LIGHTSOURCE);
			else
			if(CMLib.flags().isInDark(this))
				affectableStats.setDisposition((affectableStats.disposition() & ~PhyStats.IS_LIGHTSOURCE) | PhyStats.IS_DARK);
		}
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
	}

	@Override
	public boolean isMarriedToLiege()
	{
		if(getLiegeID().length() == 0)
			return false;
		if(getLiegeID().equals(Name()))
			return false;
		if(!CMLib.players().playerExistsAllHosts(getLiegeID()))
		{
			setLiegeID("");
			return false;
		}
		final String otherLiegeID=CMLib.players().getLiegeOfUserAllHosts(getLiegeID());
		return ((otherLiegeID!=null)&&(otherLiegeID.equalsIgnoreCase(Name())));
	}

	@Override
	public CharState curState()
	{
		return curState;
	}

	@Override
	public CharState maxState()
	{
		return maxState;
	}

	@Override
	public CharState baseState()
	{
		return baseState;
	}

	@Override
	public PlayerStats playerStats()
	{
		if((playerStats == null) && (soulMate != null) && (soulMate != this))
			return soulMate.playerStats();
		return playerStats;
	}

	@Override
	public void setPlayerStats(final PlayerStats newStats)
	{
		playerStats = newStats;
	}

	@Override
	public void setBaseState(final CharState newState)
	{
		baseState = (CharState) newState.copyOf();
		maxState = (CharState) newState.copyOf();
	}

	@Override
	public void resetToMaxState()
	{
		recoverMaxState();
		maxState.copyInto(curState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void recoverMaxState()
	{
		baseState.copyInto(maxState);
		if(charStats.getMyRace() != null)
			charStats.getMyRace().affectCharState(this, maxState);
		final Rideable riding = riding();
		if(riding != null)
			riding.affectCharState(this, maxState);
		final int num = charStats.numClasses();
		for(int c = 0; c < num; c++)
			charStats.getMyClass(c).affectCharState(this, maxState);
		eachEffect(affectCharState);
		eachItem(affectCharState);
		for(final Enumeration<Faction.FData> e = factions.elements(); e.hasMoreElements();)
			e.nextElement().affectCharState(this, maxState);
		if(location() != null)
			location().affectCharState(this, maxState);
	}

	@Override
	public boolean amDead()
	{
		return amDead || removeFromGame;
	}

	@Override
	public boolean amActive()
	{
		return !removeFromGame;
	}

	@Override
	public void dispossess(final boolean forceLook)
	{
		final MOB mate = soulMate();
		if(mate == null)
			return;
		if((mate.soulMate() != null) && (mate != this))
			mate.dispossess(forceLook);
		final Session s = session();
		if(s != null)
		{
			s.setMob(mate);
			mate.setSession(s);
			setSession(null);
			if(forceLook)
				CMLib.commands().postLook(mate, true);
			setSoulMate(null);
		}
	}

	@Override
	public void destroy()
	{
		CMLib.map().registerWorldObjectDestroyed(null, getStartRoom(), this);
		try
		{
			CMLib.catalog().changeCatalogUsage(this, false);
		}
		catch (final Exception t)
		{
		}
		if((CMSecurity.isDebugging(CMSecurity.DbgFlag.MISSINGKIDS))
		&& (fetchEffect("Age") != null)
		&& CMath.isInteger(fetchEffect("Age").text())
		&& (CMath.s_long(fetchEffect("Age").text()) > Short.MAX_VALUE))
			Log.debugOut("MISSKIDS", new Exception(Name() + " went missing form " + CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(this))));
		if(soulMate() != null)
			dispossess(false);
		final MOB possessor = CMLib.utensils().getMobPossessingAnother(this);
		if(possessor != null)
			possessor.dispossess(false);
		if(session() != null)
		{
			session().stopSession(false, false, false);
			CMLib.s_sleep(1000);
		}
		if(playerStats != null)
			CMLib.players().changePlayersLocation(this, null);
		removeFromGame(session() != null, true);
		delAllBehaviors();
		delAllEffects(false);
		delAllAbilities();
		delAllItems(true);
		delAllExpertises();
		delAllScripts();
		if(kickFlag)
			CMLib.threads().deleteTick(this, -1);
		kickFlag = false;
		clans.clear();
		clanAffects = null;
		baseCharStats.reset();
		charStats.reset();
		basePhyStats.reset();
		phyStats.reset();
		playerStats = null;
		location = null;
		lastLocation = null;
		riding = null;
		mySession = null;
		rawImageName = null;
		cachedImageName = null;
		inventory.setSize(0);
		followers = null;
		followOrder = -1;
		abilitys.setSize(0);
		triggerer.setObsolete();
		abilityUseCache.clear();
		affects.setSize(0);
		behaviors.setSize(0);
		tattoos.setSize(0);
		expertises.clear();
		factions.clear();
		commandQue.clear();
		scripts.setSize(0);
		curState = maxState;
		liegeID = "";
		victim = null;
		amFollowing = null;
		soulMate = null;
		possStartRoom = null;
		setDestroyed(true);
	}

	@Override
	public void removeFromGame(final boolean preserveFollowers, final boolean killSession)
	{
		removeFromGame = true;
		final PlayerStats pStats = playerStats;
		if((location != null)
		&& (location.isInhabitant(this)))
		{
			location().delInhabitant(this);
			if((session() != null) && (!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
			{
				final String msg = ((pStats!=null)&&(pStats.getPoofOut().length()>0))?
									pStats.getPoofOut():
									L("<S-NAME> vanish(es) in a puff of smoke.");
				location().showOthers(this, null, CMMsg.MSG_OK_ACTION,msg);
			}
		}
		if(pStats != null)
			CMLib.players().changePlayersLocation(this, null);
		setFollowing(null);
		final PairList<MOB, Integer> oldFollowers = new PairArrayList<MOB, Integer>();
		while(numFollowers() > 0)
		{
			final MOB follower = fetchFollower(0);
			if(follower != null)
			{
				if((follower.isMonster())
				&& (!follower.isPossessing()))
					oldFollowers.add(follower, Integer.valueOf(fetchFollowerOrder(follower)));
				follower.setFollowing(null);
				delFollower(follower);
			}
		}

		if(preserveFollowers)
		{
			for(final Pair<MOB, Integer> p : oldFollowers)
			{
				final MOB follower = p.first;
				if(follower.location() != null)
				{
					final MOB newFol = (MOB) follower.copyOf();
					newFol.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newFol.phyStats().setRejuv(PhyStats.NO_REJUV);
					follower.killMeDead(false);
					addFollower(newFol, p.second.intValue());
				}
			}
			if(pStats != null)
			{
				pStats.setLastDateTime(System.currentTimeMillis());
				CMLib.database().DBUpdateFollowers(this);
				if(CMSecurity.isASysOp(this)
				|| ((!CMSecurity.isDisabled(CMSecurity.DisFlag.LOGOUTS)))
					&& (CMLib.masking().maskCheck(CMProps.getVar(CMProps.Str.LOGOUTMASK), this, true)))
				{
					if(pStats != null) // cant do this when logouts are suspended -- folks might get killed!
						CMLib.threads().suspendResumeRecurse(this, false, true);
				}
			}
			if(killSession && (session() != null))
				session().stopSession(false, false, false);
		}
		setRiding(null);
	}

	@Override
	public void bringToLife()
	{
		amDead = false;
		removeFromGame = false;
		speedAdj = CMProps.getSpeedAdjustment();

		// will ensure no duplicate ticks, this obj, this id
		kickFlag = true;
		CMLib.threads().startTickDown(this, Tickable.TICKID_MOB, 1);
		if(tickStatus == Tickable.STATUS_NOT)
		{
			final boolean isImMobile = CMath.bset(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE);
			try
			{
				phyStats.setSensesMask(phyStats.sensesMask() | PhyStats.CAN_NOT_MOVE);
				tick(this, Tickable.TICKID_MOB); // slap on the butt
			}
			finally
			{
				phyStats.setSensesMask(CMath.dobit(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE, isImMobile));
			}
		}
	}

	protected final boolean confirmLocation(final Room whereHeShouldBeR)
	{
		if(location() == null)
		{
			if(getStartRoom() != null)
			{
				Log.errOut("StdMOB", name() + " of " + CMLib.map().getDescriptiveExtendedRoomID(whereHeShouldBeR) + " was auto-killed for being nowhere!");
				killMeDead(false);
			}
			else
			{
				Log.errOut("StdMOB", name() + " of " + CMLib.map().getDescriptiveExtendedRoomID(whereHeShouldBeR) + " was auto-destroyed by its tick!!");
				destroy();
			}
			return false;
		}
		return true;
	}

	@Override
	public void bringToLife(final Room newLocation, final boolean resetStats)
	{
		amDead = false;
		speedAdj = CMProps.getSpeedAdjustment();
		if((miscText != null) && (resetStats) && (isGeneric()))
		{
			if(CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS)
			&& (miscText instanceof byte[]))
			{
				final String decompressedStr = CMLib.encoder().decompressString((byte[]) miscText);
				final String unpackedStr = CMLib.coffeeMaker().getGenMOBTextUnpacked(this, decompressedStr);
				CMLib.coffeeMaker().resetGenMOB(this,unpackedStr );
			}
			else
			{
				final String unpackedStr = CMLib.coffeeMaker().getGenMOBTextUnpacked(this, CMStrings.bytesToStr(miscText));
				CMLib.coffeeMaker().resetGenMOB(this, unpackedStr);
			}
		}
		if(CMLib.map().getStartRoom(this) == null)
			setStartRoom(isMonster() ? newLocation : CMLib.login().getDefaultStartRoom(this));
		setLocation(newLocation);
		if(location() == null)
		{
			setLocation(CMLib.map().getStartRoom(this));
			if(!confirmLocation(newLocation))
				return;
		}
		if(!location().isInhabitant(this))
			location().addInhabitant(this);
		removeFromGame = false;

		if(session() != null)
		{
			final Area area = CMLib.map().areaLocation(location());
			if(area != null)
				CMLib.sessions().moveSessionToCorrectThreadGroup(session(), area.getTheme());
		}

		// will ensure no duplicate ticks, this obj, this id
		kickFlag = true;
		CMLib.threads().startTickDown(this, Tickable.TICKID_MOB, 1);

		Ability A = null;
		for(int a = 0; a < numAbilities(); a++)
		{
			A = fetchAbility(a);
			if(A != null)
				A.autoInvocation(this, false);
		}
		if(!confirmLocation(newLocation))
			return;

		if((this.tattoos.size() == 0) || (findTattoo("SYSTEM_SUMMONED") == null))
			CMLib.factions().updatePlayerFactions(this, location(), false);
		if(tickStatus == Tickable.STATUS_NOT)
		{
			final boolean isImMobile = CMath.bset(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE);
			try
			{
				phyStats.setSensesMask(phyStats.sensesMask() | PhyStats.CAN_NOT_MOVE);
				tick(this, Tickable.TICKID_MOB); // slap on the butt
			}
			catch (final Exception t)
			{
				t.printStackTrace();
			}
			finally
			{
				phyStats.setSensesMask(CMath.dobit(phyStats.sensesMask(), PhyStats.CAN_NOT_MOVE, isImMobile));
			}
		}
		if(!confirmLocation(newLocation))
			return;

		location().recoverRoomStats();
		if((!isGeneric()) && (resetStats))
		{
			resetToMaxState();
		}

		if(!confirmLocation(newLocation))
			return;

		if(isMonster())
		{
			final Item dropItem = CMLib.catalog().getDropItem(this, true);
			if(dropItem != null)
				addItem(dropItem);
		}

		if(isPlayer())
			CMLib.awards().giveAutoProperties(me, true);

		CMLib.map().registerWorldObjectLoaded(null, getStartRoom(), this);
		location().show(this, null, CMMsg.MSG_BRINGTOLIFE, null);
		if(!amDestroyed)
		{
			if(CMLib.flags().isSleeping(this))
				tell(L("(You are asleep)"));
			else
				CMLib.commands().postLook(this, true);
		}
		possWieldedItem = null;
		possHeldItem = null;
		inventory.trimToSize();
		abilitys.trimToSize();
		affects.trimToSize();
		behaviors.trimToSize();
	}

	@Override
	public boolean isInCombat()
	{
		if(victim == null)
			return false;
		try
		{
			final Room vicR = victim.location();
			if((vicR == null)
			|| (location() == null)
			|| (vicR != location())
			|| (victim.amDead()))
			{
				if((victim instanceof StdMOB) && (((StdMOB) victim).victim == this))
					victim.setVictim(null);
				setVictim(null);
				return false;
			}
			return true;
		}
		catch (final NullPointerException n)
		{
		}
		return false;
	}

	protected boolean isEitherOfUsDead(final MOB mob)
	{
		if(location() == null)
			return true;
		if(mob.location() == null)
			return true;
		if(mob.amDead())
			return true;
		if(mob.curState().getHitPoints() <= 0)
			return true;
		if(amDead())
			return true;
		if(curState().getHitPoints() <= 0)
			return true;
		return false;
	}

	protected boolean isPermissableToFight(final MOB mob)
	{
		if(mob == null)
			return false;
		final boolean targetIsMonster = mob.isMonster();
		final boolean iAmMonster = isMonster();
		if(targetIsMonster)
		{
			final MOB fol = mob.amFollowing();
			if((fol != null) && (!isEitherOfUsDead(fol)))
			{
				if(!isPermissableToFight(fol))
					return false;
			}
		}
		if(iAmMonster)
		{
			final MOB fol = amFollowing();
			if((fol != null) && (!isEitherOfUsDead(fol)))
			{
				if(!fol.mayIFight(mob))
					return false;
			}
		}
		if(CMLib.flags().isUnattackable(mob) && (!mob.isInCombat()))
			return false;
		if(targetIsMonster || iAmMonster)
			return true;
		if((mob.soulMate() != null) || (soulMate() != null))
			return true;
		if(mob == this)
			return true;
		if(CMLib.clans().isAtClanWar(this, mob))
			return true;
		if(this.isAttributeSet(MOB.Attrib.PLAYERKILL))
		{
			if(CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.PKILL) || (mob.isAttributeSet(MOB.Attrib.PLAYERKILL)))
				return true;
			return false;
		}
		else
		if(mob.isAttributeSet(MOB.Attrib.PLAYERKILL))
		{
			if(CMSecurity.isAllowed(mob, location(), CMSecurity.SecFlag.PKILL) || (this.isAttributeSet(MOB.Attrib.PLAYERKILL)))
				return true;
			return false;
		}
		else
			return false;
	}

	@Override
	public boolean mayIFight(final PhysicalAgent victim)
	{
		if(!(victim instanceof MOB))
		{
			if(CMLib.flags().isUnattackable(victim))
				return false;
			if(victim instanceof Rideable)
				return CMLib.combat().mayIAttackThisVessel(this, victim);
			return false;
		}
		if(isEitherOfUsDead((MOB) victim))
			return false;
		return isPermissableToFight((MOB) victim);
	}

	@Override
	public boolean mayPhysicallyAttack(final PhysicalAgent victim)
	{
		final Room myLocation = location();
		if((!mayIFight(victim)) || (myLocation == null))
			return false;
		if(victim instanceof MOB)
		{
			if(myLocation != ((MOB) victim).location())
				return false;
		}
		else
		{
			final Room R = CMLib.map().roomLocation(victim);
			final Area myArea = myLocation.getArea();
			if((R == null) || (myArea == null))
				return false;
			if((myLocation != R)
			&& ((!(myArea instanceof Boardable)) || (!R.isContent(((Boardable) myArea).getBoardableItem()))))
				return false;
		}
		if((!CMLib.flags().isInTheGame(this, false))
		|| (!CMLib.flags().isInTheGame(victim, false)))
			return false;
		return true;
	}

	@Override
	public long getPeaceTime()
	{
		return peaceTime;
	}

	@Override
	public void setRangeToTarget(final int newRange)
	{
		atRange = newRange;
		if(newRange >=0)
			CMLib.combat().fixDependentRanges(this);
	}

	@Override
	public int rangeToTarget()
	{
		return atRange;
	}

	@Override
	public int getDirectionToTarget()
	{
		return -1;
	}

	@Override
	public int maxRange()
	{
		if(location() != null)
			return location().maxRange();
		return 10;
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	private int maxRangeWith(final Environmental tool)
	{
		final int max = maxRange();
		if(tool == null)
			return minRange();
		if(tool.maxRange() < max)
			return tool.maxRange();
		return max;
	}

	private int minRangeWith(final Environmental tool)
	{
		if(tool != null)
			return tool.minRange();
		return minRange();
	}

	@Override
	public void makePeace(final boolean includePlayerFollowers)
	{
		final MOB myVictim = victim;
		setVictim(null);
		for(int f = 0; f < numFollowers(); f++)
		{
			final MOB M = fetchFollower(f);
			if((M != null)
			&& (M.isInCombat())
			&& (includePlayerFollowers || M.isMonster()))
				M.makePeace(true);
		}
		if(myVictim != null)
		{
			final MOB oldVictim = myVictim.getVictim();
			if(oldVictim == this)
				myVictim.makePeace(true);
		}
	}

	@Override
	public MOB getVictim()
	{
		if(!isInCombat())
			return null;
		return victim;
	}

	@Override
	public PhysicalAgent getCombatant()
	{
		return getVictim();
	}

	@Override
	public void setCombatant(final PhysicalAgent other)
	{
		if((other == null) || (other instanceof MOB))
			setVictim((MOB) other);
	}

	@Override
	public void setVictim(final MOB other)
	{
		if(other == null)
		{
			if(victim != null)
			{
				synchronized(commandQue)
				{
					commandQue.clear();
				}
			}
		}
		if(victim == other)
			return;
		if(other == this)
			return;
		victim = other;
		if(other == null)
			setRangeToTarget(-1);
		recoverPhyStats();
		recoverCharStats();
		recoverMaxState();
		if(other != null)
		{
			if((other.location() == null)
			|| (location() == null)
			|| (other.amDead())
			|| (amDead())
			|| (other.location() != location())
			|| (!location().isInhabitant(this))
			|| (!location().isInhabitant(other)))
			{
				if(victim != null)
					victim.setVictim(null);
				victim = null;
				setRangeToTarget(-1);
			}
			else
			{
				if(Log.combatChannelOn())
				{
					final Item I = fetchWieldedItem();
					final Item VI = other.fetchWieldedItem();
					Log.combatOut("STRT", Name()
										+ ":" + phyStats().getCombatStats()
										+ ":" + curState().getCombatStats()
										+ ":" + ((I == null) ? "null" : I.name())
										+ ":" + other.Name()
										+ ":" + other.phyStats().getCombatStats()
										+ ":" + other.curState().getCombatStats()
										+ ":" + ((VI == null) ? "null" : VI.name()));
				}
				other.recoverCharStats();
				other.recoverPhyStats();
				other.recoverMaxState();
			}
		}
	}

	@Override
	public DeadBody killMeDead(final boolean createBody)
	{
		final Room corpseRoom;
		Room deathRoom = location();
		if(isMonster())
			corpseRoom = deathRoom;
		else
			corpseRoom = CMLib.login().getDefaultBodyRoom(this);
		if(deathRoom != null)
			deathRoom.delInhabitant(this);
		amDead = true;
		DeadBody bodyI = null;
		if(createBody)
		{
			bodyI = charStats().getMyRace().getCorpseContainer(this, corpseRoom);
			if((bodyI != null)
			&& (playerStats() != null))
			{
				bodyI.setSavable(false); // if the player is saving it, rooms are NOT.
				playerStats().getExtItems().addItem(bodyI);
			}
			if(corpseRoom != null)
				corpseRoom.show(this, bodyI, CMMsg.MASK_ALWAYS|CMMsg.MSG_BODYDROP, null);
		}
		makePeace(false);
		setRiding(null);
		synchronized(commandQue)
		{
			commandQue.clear();
		}
		Ability A = null;
		for(int a = numEffects() - 1; a >= 0; a--)
		{
			A = fetchEffect(a);
			if(A != null)
				A.unInvoke();
		}
		setLocation(null);
		if(isMonster())
		{
			while(numFollowers() > 0)
			{
				final MOB follower = fetchFollower(0);
				if(follower != null)
				{
					follower.setFollowing(null);
					delFollower(follower);
				}
			}
			setFollowing(null);
		}
		if((!isMonster()) && (soulMate() == null))
		{
			deathRoom = CMLib.login().getDefaultDeathRoom(this);
			bringToLife(deathRoom, true);
		}
		if(corpseRoom != null)
			corpseRoom.recoverRoomStats();
		return bodyI;
	}

	@Override
	public Room location()
	{
		if(location == null)
			return lastLocation;
		return location;
	}

	@Override
	public void setLocation(final Room newRoom)
	{
		lastLocation = location;
		location = newRoom;
		if((playerStats != null) && (lastLocation != newRoom))
		{
			CMLib.players().changePlayersLocation(this, newRoom);
			if((!playerStats.isPoseConstant()) && (playerStats.getSavedPose().length()>0))
			{
				playerStats.setSavedPose("", true);
				setDisplayText("");
			}
		}
	}

	@Override
	public Rideable riding()
	{
		return riding;
	}

	@Override
	public void setRiding(final Rideable ride)
	{
		final Rideable amRiding = riding();
		if((ride != null)
		&& (amRiding != null)
		&& (amRiding == ride)
		&& (amRiding.amRiding(this)))
			return;
		if((amRiding != null)
		&& (amRiding.amRiding(this)))
			amRiding.delRider(this);
		riding = ride;
		if((ride != null) && (!ride.amRiding(this)))
			ride.addRider(this);
	}

	@Override
	public final Session session()
	{
		return mySession == null ? null : mySession.isFake() ? null : mySession;
	}

	@Override
	public void setSession(final Session newSession)
	{
		mySession = newSession;
		setAttributesBitmap(getAttributesBitmap());
	}

	@Override
	public Weapon getNaturalWeapon()
	{
		final Weapon W;
		if((charStats() != null) && (charStats().getMyRace() != null))
			W = charStats().getMyRace().getNaturalWeapon();
		else
			W = CMClass.getWeapon("Natural");
		if(W.subjectToWearAndTear())
			W.setUsesRemaining(100);
		return W;
	}

	@Override
	public String titledName(final MOB viewer)
	{
		if(CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM)
		&& (playerStats() != null)
		&& (viewer != null)
		&& (viewer != this)
		&& (viewer.playerStats() != null)
		&& (!viewer.playerStats().isIntroducedTo(Name()))
		&& (!CMSecurity.isASysOp(viewer)))
			return CMLib.english().startWithAorAn(genericName()).toLowerCase();
		return titledName();
	}

	@Override
	public String name(final MOB viewer)
	{
		if(CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM)
		&& (playerStats() != null)
		&& (viewer != null)
		&& (viewer != this)
		&& (viewer.playerStats() != null)
		&& (!viewer.playerStats().isIntroducedTo(Name()))
		&& (!CMSecurity.isASysOp(viewer)))
			return CMLib.english().startWithAorAn(genericName()).toLowerCase();
		return name();
	}

	@Override
	public String displayText(final MOB viewerMob)
	{
		if((displayText.length() == 0)
		|| (!name(viewerMob).equals(Name()))
		|| (!titledName().equals(Name()))
		|| (CMLib.flags().isSleeping(this))
		|| (CMLib.flags().isSitting(this)) || (riding() != null)
		|| ((amFollowing() != null)
			&& (amFollowing().fetchFollowerOrder(this) > 0))
		|| ((this instanceof Rideable)
			&& (((Rideable) this).numRiders() > 0)
			&& CMLib.flags().isWithSeenContents(this))
		|| (isInCombat()))
		{
			final String localName;
			if(!name(viewerMob).equals(Name()))
				localName = name(viewerMob);
			else
				localName = titledName(viewerMob);
			final StringBuilder sendBack;
			if(displayText.startsWith(Name()))
				sendBack = new StringBuilder(localName).append(displayText.substring(Name().length()));
			else
			{
				sendBack = new StringBuilder(localName);
				sendBack.append(" ");
				sendBack.append(L(CMLib.flags().getPresentDispositionVerb(this, CMFlagLibrary.ComingOrGoing.IS) + " here"));
			}
			if(riding() != null)
			{
				sendBack.append(" " + riding().stateString(this) + " ");
				if(riding() == viewerMob)
					sendBack.append(L("YOU"));
				else
				if(!CMLib.flags().canBeSeenBy(riding(), viewerMob))
				{
					if(riding() instanceof Item)
						sendBack.append(L("something"));
					else
						sendBack.append(L("someone"));
				}
				else
					sendBack.append(riding().name());
			}
			else
			if((this instanceof Rideable)
			&& (((Rideable) this).numRiders() > 0)
			&& (((Rideable) this).stateStringSubject(((Rideable) this).fetchRider(0)).length() > 0))
			{
				final Rideable me = (Rideable) this.me;
				final String first = me.stateStringSubject(me.fetchRider(0));
				sendBack.append(" " + first + " ");
				for(int r = 0; r < me.numRiders(); r++)
				{
					final Rider rider = me.fetchRider(r);
					if((rider != null) && (me.stateStringSubject(rider).equals(first)))
					{
						if(r > 0)
						{
							sendBack.append(", ");
							if(r == me.numRiders() - 1)
								sendBack.append("and ");
						}
						if(rider == viewerMob)
							sendBack.append("you");
						else
						if(!CMLib.flags().canBeSeenBy(riding(), viewerMob))
						{
							if(riding() instanceof Item)
								sendBack.append(L("something"));
							else
								sendBack.append(L("someone"));
						}
						else
							sendBack.append(rider.name());
					}
				}
			}
			if((isInCombat())
			&& (CMLib.flags().canMove(this))
			&& (!CMLib.flags().isSleeping(this)))
			{
				if(getVictim() == viewerMob)
					sendBack.append(L(" fighting YOU"));
				else
				if(!CMLib.flags().canBeSeenBy(getVictim(), viewerMob))
					sendBack.append(L(" fighting someone"));
				else
					sendBack.append(L(" fighting @x1", getVictim().name()));
			}
			if((amFollowing() != null)
			&& (amFollowing().fetchFollowerOrder(this) > 0))
			{
				final List<MOB> whoseAhead = CMLib.combat().getFormationFollowed(this);
				if((whoseAhead != null) && (!whoseAhead.isEmpty()))
				{
					sendBack.append(L(", behind "));
					for(int v = 0; v < whoseAhead.size(); v++)
					{
						final MOB ahead = whoseAhead.get(v);
						if(v > 0)
						{
							sendBack.append(", ");
							if(v == whoseAhead.size() - 1)
								sendBack.append(L("and "));
						}
						if(ahead == viewerMob)
							sendBack.append(L("you"));
						else
						if(!CMLib.flags().canBeSeenBy(ahead, viewerMob))
							sendBack.append(L("someone"));
						else
							sendBack.append(ahead.name());
					}
				}
			}
			sendBack.append(".");
			return sendBack.toString();
		}
		return displayText;
	}

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
		displayText = newDisplayText;
	}

	@Override
	public String description()
	{
		if(description == null)
			return "";
		else
		if(description instanceof byte[])
		{
			final byte[] descriptionBytes = (byte[]) description;
			if(descriptionBytes.length == 0)
				return "";
			if(CMProps.getBoolVar(CMProps.Bool.MOBDCOMPRESS))
				return CMLib.encoder().decompressString(descriptionBytes);
			else
				return CMStrings.bytesToStr(descriptionBytes);
		}
		else
			return (String) description;
	}

	@Override
	public String description(final MOB viewerMob)
	{
		return description();
	}

	@Override
	public void setDescription(final String newDescription)
	{
		if(newDescription.length() == 0)
			description = null;
		else
		if(CMProps.getBoolVar(CMProps.Bool.MOBDCOMPRESS))
			description = CMLib.encoder().compressString(newDescription);
		else
			description = newDescription;
	}

	@Override
	public void setMiscText(final String newText)
	{
		if(newText.length() == 0)
			miscText = null;
		else
		if(CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS))
			miscText = CMLib.encoder().compressString(newText);
		else
			miscText = newText;
	}

	@Override
	public String text()
	{
		if(miscText == null)
			return "";
		else
		if(miscText instanceof byte[])
		{
			final byte[] miscTextBytes = (byte[]) miscText;
			if(miscTextBytes.length == 0)
				return "";
			if(CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS))
				return CMLib.encoder().decompressString(miscTextBytes);
			else
				return CMStrings.bytesToStr(miscTextBytes);
		}
		else
			return (String) miscText;
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public String healthText(final MOB viewer)
	{
		final String mxp = "^<!ENTITY vicmaxhp \"" + maxState().getHitPoints() + "\"^>"
						+ "^<!ENTITY vichp \"" + curState().getHitPoints() + "\"^>"
						+ "^<Health^>^<HealthText \"" + CMStrings.removeColors(name(viewer)) + "\"^>";
		if((charStats() != null) && (charStats().getMyRace() != null))
			return mxp + charStats().getMyRace().healthText(viewer, this) + "^</HealthText^>";
		return mxp + CMLib.combat().standardMobCondition(viewer, this) + "^</HealthText^>";
	}

	@Override
	public double actions()
	{
		return freeActions;
	}

	@Override
	public void setActions(final double remain)
	{
		freeActions = remain;
	}

	@Override
	public int commandQueSize()
	{
		return commandQue.size();
	}

	@Override
	public void clearCommandQueue()
	{
		commandQue.clear();
	}

	@Override
	public Pair<Object, List<String>> getTopCommand()
	{
		Pair<Object, List<String>> top = null;
		synchronized(commandQue)
		{
			if(!commandQue.isEmpty())
			{
				final QMCommand QM=commandQue.peekFirst();
				top=new Pair<Object, List<String>>(QM.commandObj, QM.commandVector);
			}
		}
		return top;
	}

	@Override
	public boolean dequeCommand()
	{
		while((!removeFromGame)
		&& (!amDestroyed())
		&& ((session() == null) || (!session().isStopped())))
		{
			QMCommand doCommand = null;
			synchronized(commandQue)
			{
				if(commandQue.isEmpty())
					return false;
				QMCommand cmd = commandQue.getFirst();
				final double diff = actions() - cmd.actionCost;
				if(diff >= 0.0)
				{
					final long reqEllapse = Math.round(cmd.actionCost / phyStats().speed() * CMProps.getTickMillisD());
					final long nextTime = lastCommandTime + reqEllapse;
					if((System.currentTimeMillis() < nextTime) && (session() != null))
						return false;
					cmd = commandQue.removeFirst();
					setActions(diff);
					doCommand = cmd;
				}
			}
			if(doCommand != null)
			{
				lastCommandTime = System.currentTimeMillis();
				doCommand(doCommand.commandObj, doCommand.commandVector, doCommand.metaFlags);
				synchronized(commandQue)
				{
					if(!commandQue.isEmpty())
					{
						final QMCommand cmd = commandQue.getFirst();
						final Object O = cmd.commandObj;
						cmd.actionCost = calculateActionCost(O, cmd.commandVector, 0.0);
					}
					else
						return false;
					return true;
				}
			}

			QMCommand cmd = null;
			synchronized(commandQue)
			{
				if(commandQue.isEmpty())
					return false;
				cmd = commandQue.getFirst();
				if((cmd == null) || (System.currentTimeMillis() < cmd.nextCheck))
					return false;
			}

			final double diff = actions() - cmd.actionCost;
			final Object O = cmd.commandObj;
			final List<String> commands = new XVector<String>(cmd.commandVector);
			cmd.nextCheck = cmd.nextCheck + 1000;
			cmd.seconds += 1;
			final int secondsElapsed = cmd.seconds;
			final int metaFlags = cmd.metaFlags;
			try
			{
				// record an action taken in personal combat stats
				final MOB combatant = victim;
				if((cmd.actionCost > 0) && (combatant != null))
				{
					if(playerStats != null)
						playerStats.bumpLevelCombatStat(PlayerCombatStat.ACTIONS_DONE, basePhyStats().level(), 1);
					if(combatant.playerStats() != null)
						combatant.playerStats().bumpLevelCombatStat(PlayerCombatStat.ACTIONS_TAKEN, combatant.basePhyStats().level(), 1);
				}

				// actually do the command queued up
				if(O instanceof Command)
				{
					if(!((Command) O).preExecute(this, commands, metaFlags, secondsElapsed, -diff))
					{
						commandQue.remove(cmd);
						return true;
					}
				}
				else
				if(O instanceof Ability)
				{
					if(!CMLib.english().preInvokeSkill(this, commands, secondsElapsed, -diff))
					{
						commandQue.remove(cmd);
						return true;
					}
				}
			}
			catch (final Exception e)
			{
				return false;
			}
		}
		return false;
	}

	@Override
	public void doCommand(final List<String> commands, final int metaFlags)
	{
		final CMObject O = CMLib.english().findCommand(this, commands);
		if(O != null)
			doCommand(O, commands, metaFlags);
		else
			CMLib.commands().handleUnknownCommand(this, commands);
	}

	protected void doCommand(final Object O, final List<String> commands, final int metaFlags)
	{
		try
		{
			if(O instanceof Command)
			{
				if(playerStats != null)
				{
					CMLib.achievements().possiblyBumpAchievement(this, AchievementLibrary.Event.CMDUSE, 1, (Command) O);
					CMLib.coffeeTables().bump((Command) O, CoffeeTableRow.STAT_CMDUSE);
				}
				((Command) O).execute(this, new XVector<String>(commands), metaFlags);
			}
			else
			if(O instanceof Social)
				((Social) O).invoke(this, new XVector<String>(commands), null, false);
			else
			if(O instanceof Ability)
				CMLib.english().invokeSkill(this, new XVector<String>(commands));
			else
				CMLib.commands().handleUnknownCommand(this, commands);
		}
		catch (final java.io.IOException io)
		{
			Log.errOut("StdMOB", CMParms.toListString(commands));
			if(io.getMessage() != null)
				Log.errOut("StdMOB", io.getMessage());
			else
				Log.errOut("StdMOB", io);
			tell(L("Oops-- you ran into a game bug! Please inform the administrators."));
		}
		catch (final Exception e)
		{
			Log.errOut("StdMOB", Name() + " did '" + CMParms.toListString(commands) + "' in " + CMLib.map().getExtendedRoomID(location()));
			if((e!=null)&&(e.getMessage() != null))
				Log.errOut("StdMOB", e.getMessage());
			Log.errOut("StdMOB", e);
			tell(L("Oops-- you ran into a game bug! Please inform the administrators."));
		}
	}

	protected double calculateActionCost(final Object command, final List<String> commands, double overrideActionCost)
	{
		if(overrideActionCost <= 0.0)
		{
			if(command == null)
			{
				tell(L("Huh?!"));
				return -1.0;
			}
			if(command instanceof Command)
				overrideActionCost = ((Command) command).checkedActionsCost(this, commands);
			else
			if(command instanceof Ability)
				overrideActionCost = ((Ability) command).checkedCastingCost(this, commands);
			else
			if(command instanceof Social)
				overrideActionCost = ((Social) command).checkedActionsCost(this, commands);
			else
				overrideActionCost = 1.0;
		}
		return overrideActionCost;
	}

	protected void checkCommandCancel()
	{
		synchronized(commandQue)
		{
			if(!commandQue.isEmpty())
			{
				final QMCommand cmd = commandQue.peekFirst();
				if((cmd.commandObj instanceof Command)
				&&(((Command)cmd.commandObj).canBeCancelled()))
				{
					try
					{
						((Command)cmd.commandObj).preExecute(me, cmd.commandVector, cmd.metaFlags, -1, cmd.actionCost);
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
					if((!commandQue.isEmpty())&&(commandQue.peekFirst() == cmd))
						commandQue.removeFirst();
				}
			}
		}
	}

	@Override
	public void prequeCommand(final List<String> commands, final int metaFlags, double actionCost)
	{
		if(commands == null)
			return;
		final CMObject O = CMLib.english().findCommand(this, commands);
		if(O == null)
		{
			CMLib.commands().handleUnknownCommand(this, commands);
			return;
		}
		actionCost = calculateActionCost(O, commands, actionCost);
		if(actionCost < 0.0)
			return;
		if(actionCost == 0.0)
			doCommand(O, commands, metaFlags);
		else
		{
			checkCommandCancel();
			synchronized(commandQue)
			{
				final QMCommand cmd = new QMCommand();
				cmd.nextCheck = System.currentTimeMillis() - 1;
				cmd.seconds = -1;
				cmd.actionCost = actionCost;
				cmd.metaFlags = metaFlags;
				cmd.commandObj = O;
				cmd.commandVector = commands;
				commandQue.addFirst(cmd);
			}
		}
		dequeCommand();
	}

	@Override
	public void prequeCommands(final List<List<String>> commands, final int metaFlags)
	{
		if((commands == null) || (commands.size() == 0))
			return;
		final List<QMCommand> queueUp = new ArrayList<QMCommand>(commands.size());
		for(final List<String> command : commands)
		{
			final CMObject O = CMLib.english().findCommand(this, command);
			if(O == null)
			{
				CMLib.commands().handleUnknownCommand(this, command);
				return;
			}
			final double actionCost = calculateActionCost(O, command, 0.0);
			if(actionCost < 0.0)
				return;
			synchronized(commandQue)
			{
				final QMCommand cmd = new QMCommand();
				cmd.nextCheck = System.currentTimeMillis() - 1;
				cmd.seconds = -1;
				cmd.actionCost = actionCost;
				cmd.metaFlags = metaFlags;
				cmd.commandObj = O;
				cmd.commandVector = command;
				queueUp.add(cmd);
			}
		}
		if(queueUp.size() > 0)
		{
			if(this.commandQue.size() == 0)
				this.commandQue.addAll(queueUp);
			else
				this.commandQue.addAll(0, queueUp);
		}
		dequeCommand();
	}

	@Override
	public void enqueCommand(final List<String> commands, final int metaFlags, double actionCost)
	{
		if(commands == null)
			return;
		final CMObject O = CMLib.english().findCommand(this, commands);
		if((O == null)
		|| ((O instanceof Ability)
			&& CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_ORDER)
			&& CMath.bset(((Ability) O).flags(), Ability.FLAG_NOORDERING)))
		{
			CMLib.commands().handleUnknownCommand(this, commands);
			return;
		}
		actionCost = calculateActionCost(O, commands, actionCost);
		if(actionCost < 0.0)
			return;
		if((actionCost == 0.0) && (!CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_INORDER)))
			doCommand(commands, metaFlags);
		else
		{
			checkCommandCancel();
			synchronized(commandQue)
			{
				final QMCommand cmd = new QMCommand();
				cmd.nextCheck = System.currentTimeMillis() - 1;
				cmd.seconds = -1;
				cmd.actionCost = actionCost;
				cmd.metaFlags = metaFlags;
				cmd.commandObj = O;
				cmd.commandVector = commands;
				commandQue.addLast(cmd);
			}
		}
		dequeCommand();
	}

	@Override
	public void enqueCommands(final List<List<String>> commands, final int metaFlags)
	{
		if((commands == null) || (commands.size() == 0))
			return;
		for(final List<String> cmds : commands)
			enqueCommand(cmds, metaFlags, 0.0);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final Deity deity = charStats().getMyDeity();
		if((deity != null) && (deity != this) && (!deity.okMessage(this, msg)))
			return false;

		final CharStats cStats = charStats;
		if(cStats != null)
		{
			for(int c = 0; c < cStats.numClasses(); c++)
			{
				if(!cStats.getMyClass(c).okMessage(this, msg))
					return false;
			}
			if(!cStats.getMyRace().okMessage(this, msg))
				return false;
		}

		// the order here is significant (between eff and item -- see focus)
		for(final Enumeration<Ability> a = effects(); a.hasMoreElements();)
		{
			final Ability A = a.nextElement();
			if(!A.okMessage(this, msg))
				return false;
		}

		for(final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if(!I.okMessage(this, msg))
				return false;
		}

		for(final Enumeration<Behavior> b = behaviors(); b.hasMoreElements();)
		{
			final Behavior B = b.nextElement();
			if(!B.okMessage(this, msg))
				return false;
		}

		for(final Enumeration<ScriptingEngine> s = scripts(); s.hasMoreElements();)
		{
			final ScriptingEngine S = s.nextElement();
			if(!S.okMessage(this, msg))
				return false;
		}

		for(final Enumeration<Faction.FData> e = factions.elements(); e.hasMoreElements();)
		{
			final Faction.FData fD = e.nextElement();
			if(!fD.getFaction().okMessage(this, msg))
				return false;
			if(!fD.okMessage(this, msg))
				return false;
		}

		final MOB srcM = msg.source();
		if((msg.sourceCode() != CMMsg.NO_EFFECT) && (srcM==this))
		{
			if((msg.tool() instanceof Item)
			&&(((Item)msg.tool()).owner()==null)
			&&(!((Item)msg.tool()).okMessage(myHost, msg)))
				return false;

			if((msg.sourceMinor() == CMMsg.TYP_DEATH)
			&& (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.IMMORT)))
			{
				curState().setHitPoints(1);
				if((msg.tool() != this) && (msg.tool() instanceof MOB))
					((MOB) msg.tool()).tell(this,null,null,L("<S-NAME> is immortal, and can not die."));
				tell(L("You are immortal, and can not die."));
				return false;
			}

			if(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			{
				final CMFlagLibrary flags = CMLib.flags();
				if(amDead())
				{
					tell(L("You are DEAD!"));
					return false;
				}

				if(msg.sourceMajor(CMMsg.MASK_MALICIOUS))
				{
					if(msg.target() instanceof MOB)
					{
						final MOB target = (MOB) msg.target();
						if((amFollowing() != null) && (target == amFollowing()))
						{
							tell(L("You like @x1 too much.", amFollowing().charStats().himher()));
							return false;
						}
						if((getLiegeID().length() > 0) && (target.Name().equals(getLiegeID())))
						{
							if(isMarriedToLiege())
								tell(L("You are married to '@x1'!", getLiegeID()));
							else
								tell(L("You are serving '@x1'!", getLiegeID()));
							return false;
						}
						CMLib.combat().establishRange(this, (MOB) msg.target(), msg.tool()); // why this here?
					}
				}

				if(msg.sourceMajor(CMMsg.MASK_EYES))
				{
					if(flags.isSleeping(this))
					{
						tell(L("Not while you are sleeping."));
						return false;
					}
					if(!(msg.target() instanceof Room))
					{
						if(!flags.canBeSeenBy(msg.target(), this))
						{
							if(msg.target() instanceof Item)
								tell(L("You don't see @x1 here.", ((Item) msg.target()).name(this)));
							else
								tell(L("You can't see that!"));
							return false;
						}
					}
				}
				if(msg.sourceMajor(CMMsg.MASK_MOUTH))
				{
					if(((msg.sourceMinor() != CMMsg.TYP_LIST)
						|| srcM.amDead()
						|| flags.isSleeping(srcM))
					&& (!flags.isAliveAwakeMobile(this, false)))
						return false;

					if(msg.sourceMajor(CMMsg.MASK_SOUND))
					{
						if((msg.tool() == null)
						|| (!(msg.tool() instanceof Ability))
						|| (!((Ability) msg.tool()).isNowAnAutoEffect()))
						{
							if(flags.isSleeping(this))
							{
								tell(L("Not while you are sleeping."));
								return false;
							}
							if(!flags.canSpeak(this))
							{
								tell(L("You can't make sounds!"));
								return false;
							}
							if((!flags.canBreatheHere(this, location))
							&&(!CMSecurity.isAllowed(this,location,CMSecurity.SecFlag.ALLSKILLS)))
							{
								tell(L("You can't make sounds due to being unable to breathe!"));
								return false;
							}
							if((flags.isAnimalIntelligence(this)) && (!CMath.bset(phyStats().sensesMask(), PhyStats.CAN_GRUNT_WHEN_STUPID)))
							{
								tell(L("You aren't smart enough to speak."));
								return false;
							}
						}
					}
					else
					if(msg.sourceMajor(CMMsg.MASK_HANDS))
					{
						if((!flags.canBeSeenBy(msg.target(), this))
						&& (!isMine(msg.target()))
						&& (msg.target() instanceof Item))
						{
							srcM.tell(L("You don't see '@x1' here.", ((Item) msg.target()).name(this)));
							return false;
						}
						if(!flags.canTaste(this))
						{
							if((msg.sourceMinor() == CMMsg.TYP_EAT) || (msg.sourceMinor() == CMMsg.TYP_DRINK))
								tell(L("You can't eat or drink."));
							else
								tell(L("Your mouth is out of order."));
							return false;
						}
					}
				}
				if(msg.sourceMajor(CMMsg.MASK_HANDS))
				{
					if((!flags.canBeSeenBy(msg.target(), this))
					&& (!(isMine(msg.target()) && (msg.target() instanceof Item)))
					&& ((!(msg.target() instanceof Boardable))
						|| (((Boardable) msg.target()).getArea() != location().getArea()))
					&& (!((isInCombat()) && (msg.target() == victim)))
					&& (msg.targetMajor(CMMsg.MASK_HANDS))
					&& (!(msg.target() instanceof Room)))
					{
						if(msg.target() instanceof Physical)
						{
							srcM.tell(L("You don't see '@x1' here.", ((Physical) msg.target()).name(this)));
						}
						else
						if(msg.target() != null)
							srcM.tell(L("You don't see '@x1' here.", msg.target().name()));
						else
							srcM.tell(L("You don't see that here."));
						return false;
					}
					if(!flags.isAliveAwakeMobile(this, false))
						return false;

					if((flags.isSitting(this))
					&& ((msg.sourceMessage() != null) || (msg.othersMessage() != null)))
					{
						switch(msg.sourceMinor())
						{
						case CMMsg.TYP_SITMOVE:
						case CMMsg.TYP_BUY:
						case CMMsg.TYP_BID:
						case CMMsg.TYP_OK_VISUAL:
							break;
						default:
							if(((!CMLib.utensils().reachableItem(this, msg.target())) || (!CMLib.utensils().reachableItem(this, msg.tool())))
							&& (location() != null)
							&& (!CMath.bset(location().phyStats().sensesMask(), PhyStats.SENSE_ROOMCRUNCHEDIN)))
							{
								tell(L("You need to stand up!"));
								return false;
							}
							break;
						}
					}
				}

				if(msg.sourceMajor(CMMsg.MASK_MOVE))
				{
					final boolean sleeping = flags.isSleeping(this);
					final boolean sitting;
					if(flags.isSitting(this))
					{
						switch(msg.sourceMinor())
						{
						case CMMsg.TYP_LEAVE:
						case CMMsg.TYP_ENTER:
							sitting=false;
							break;
						case CMMsg.TYP_DELICATE_HANDS_ACT:
							sitting=(msg.targetMinor() != CMMsg.NO_EFFECT);
							break;
						default:
							sitting=true;
							break;
						}
					}
					else
						sitting=false;
					if(sleeping || sitting)
					{
						switch(msg.sourceMinor())
						{
						case CMMsg.TYP_STAND:
						case CMMsg.TYP_SITMOVE:
						case CMMsg.TYP_SIT:
						case CMMsg.TYP_SLEEP:
							break;
						case CMMsg.TYP_WEAPONATTACK:
						case CMMsg.TYP_THROW:
							tell(sleeping ? L("You need to wake up!") : L("You need to stand up!"));
							break;
						default:
							tell(sleeping ? L("You need to wake up!") : L("You need to stand up!"));
							return false;
						}
					}
					if(!flags.canMove(this))
					{
						tell(L("You can't move!"));
						return false;
					}
				}

				// limb, weight check
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_PULL:
				case CMMsg.TYP_PUSH:
				{
					if(msg.target() instanceof Physical)
					{
						final int totalWeight = CMLib.utensils().getPullWeight((Physical) msg.target());
						if((maxCarry() * 10) < totalWeight)
						{
							if(msg.targetMinor() == CMMsg.TYP_PUSH)
								tell(L("That's way too heavy to push."));
							else
								tell(L("That's way too heavy to pull."));
							return false;
						}
					}
				}
				//$FALL-THROUGH$
				case CMMsg.TYP_GET:
				case CMMsg.TYP_REMOVE:
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if((charStats().getBodyPart(Race.BODY_ARM) == 0) && (baseCharStats().getMyRace().bodyMask()[Race.BODY_ARM] > 0))
					{
						tell(L("You need arms to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_DELICATE_HANDS_ACT:
					if((charStats().getBodyPart(Race.BODY_HAND) == 0) && (msg.othersMinor() != CMMsg.NO_EFFECT))
					{
						tell(L("You need hands to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_JUSTICE:
					if((charStats().getBodyPart(Race.BODY_HAND) == 0)
					&& (baseCharStats().getMyRace().bodyMask()[Race.BODY_HAND] > 0)
					&& (msg.target() instanceof Item)
					&& (!((msg.tool() instanceof Ability)
					&& CMLib.ableMapper().qualifiesOnlyByRace(msg.source(), (Ability) msg.tool()))))
					{
						tell(L("You need hands to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_GIVE:
				case CMMsg.TYP_PUT:
					if((charStats().getBodyPart(Race.BODY_ARM) == 0)
					&& (baseCharStats().getMyRace().bodyMask()[Race.BODY_ARM] > 0))
					{
						tell(L("You need arms to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_HANDS:
				case CMMsg.TYP_INSTALL:
				case CMMsg.TYP_REPAIR:
				case CMMsg.TYP_ENHANCE:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_WRITE:
				case CMMsg.TYP_REWRITE:
					if(charStats().getBodyPart(Race.BODY_HAND) == 0)
					{
						tell(L("You need hands to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_DRINK:
					if((charStats().getBodyPart(Race.BODY_HAND) == 0) && (baseCharStats().getMyRace().bodyMask()[Race.BODY_HAND] > 0))
					{
						if((msg.target() != null) && (isMine(msg.target())))
						{
							tell(L("You need hands to do that."));
							return false;
						}
					}
					break;
				}

				// activity check
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_WEAR:
				case CMMsg.TYP_HOLD:
				case CMMsg.TYP_WIELD:
				case CMMsg.TYP_REMOVE:
					possWieldedItem = null;
					possHeldItem = null;
					break;
				case CMMsg.TYP_RPXPCHANGE:
					if((curState().getHunger() < 1)
					|| (curState().getThirst() < 1)
					|| (curState().getFatigue() > CharState.FATIGUED_MILLIS))
					{
						// silent
						return false;
					}
					break;
				case CMMsg.TYP_JUSTICE:
					if((isInCombat()) && (msg.target() instanceof Item))
					{
						tell(L("Not while you are fighting!"));
						return false;
					}
					break;
				case CMMsg.TYP_RETREAT:
				{
					final int retreatCost;
					if(location() != null)
					{
						retreatCost = location().pointsPerMove()*5;
						if(curState().getMovement() < retreatCost)
						{
							tell(L("You are too tired."));
							return false;
						}
						if(rangeToTarget() >= location().maxRange())
						{
							tell(L("You cannot retreat any further."));
							return false;
						}
					}
					else
						retreatCost=10;
					curState().adjMovement(-retreatCost, maxState());
					recoverPhyStats();
					break;
				}
				case CMMsg.TYP_THROW:
					if(charStats().getBodyPart(Race.BODY_ARM) == 0)
					{
						tell(L("You need arms to do that."));
						return false;
					}
					break;
				case CMMsg.TYP_OPEN:
				case CMMsg.TYP_CLOSE:
					if(isInCombat())
					{
						if(((msg.target() instanceof Exit) || (srcM.isMine(msg.target()))))
							break;
						tell(L("Not while you are fighting!"));
						return false;
					}
					break;
				case CMMsg.TYP_LEAVE:
					if((isInCombat())
					&& (location() != null)
					&& (!msg.sourceMajor(CMMsg.MASK_MAGIC))
					&& (!flags.isFalling(this)))
					{
						for(final Enumeration<MOB> m = location().inhabitants(); m.hasMoreElements();)
						{
							final MOB M = m.nextElement();
							if((M != this)
							&& (M.getVictim() == this)
							&& (flags.isAliveAwakeMobile(M, true))
							&& (flags.canSenseEnteringLeaving(srcM, M))
							&& (CMProps.getVar(CMProps.Str.PLAYERFLEE) != null)
							&& (CMProps.getVar(CMProps.Str.PLAYERFLEE).length() > 0))
							{
								tell(L("Not while you are fighting!"));
								return false;
							}
						}
					}
					break;
				case CMMsg.TYP_READ:
					if(isInCombat()
					&& (!msg.sourceMajor(CMMsg.MASK_MAGIC))
					&& (!(msg.target() instanceof Scroll)))
					{
						tell(L("You are too busy fighting to read!"));
						return false;
					}
					break;
				case CMMsg.TYP_SIT: // SIT is waking!
					if(flags.isSleeping(this))
						break;
					//$FALL-THROUGH$
				case CMMsg.TYP_BUY:
				case CMMsg.TYP_BID:
				case CMMsg.TYP_DELICATE_HANDS_ACT:
				case CMMsg.TYP_FILL:
				case CMMsg.TYP_LIST:
				case CMMsg.TYP_LOCK:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_UNLOCK:
				case CMMsg.TYP_VALUE:
				case CMMsg.TYP_SELL:
				case CMMsg.TYP_VIEW:
					if(isInCombat()
					&& (!msg.sourceMajor(CMMsg.MASK_MAGIC)))
					{
						tell(L("Not while you are fighting!"));
						return false;
					}
					break;
				case CMMsg.TYP_REBUKE:
					if((msg.target() == null) || (!(msg.target() instanceof Deity)))
					{
						if(msg.target() != null)
						{
							if((msg.target() instanceof MOB)
							&& (!flags.canBeHeardSpeakingBy(this, (MOB) msg.target())))
							{
								tell(L("@x1 can't hear you!", ((Physical) msg.target()).name(this)));
								return false;
							}
							else
							if((msg.target() instanceof MOB)
							&& (((MOB) msg.target()).amFollowing() == srcM)
							&& (srcM.isFollowedBy((MOB) msg.target())))
							{
								// should work.
							}
							else
							if((!((msg.target() instanceof MOB)  && (((MOB) msg.target()).getLiegeID().equals(Name()))))
							&& (!msg.target().Name().equals(getLiegeID())))
							{
								tell(L("@x1 does not serve you, and you do not serve @x2.", ((Physical) msg.target()).name(this), ((Physical) msg.target()).name(this)));
								return false;
							}
							else
							if((msg.target() instanceof MOB)
							&& (((MOB) msg.target()).getLiegeID().equals(Name()))
							&& (getLiegeID().equals(msg.target().Name()))
							&& (((MOB) msg.target()).isMarriedToLiege()))
							{
								tell(L("You cannot rebuke @x1.  You must get an annulment or a divorce.", ((Physical) msg.target()).name(this)));
								return false;
							}
						}
						else
						if(getLiegeID().length() == 0)
						{
							tell(L("You aren't serving anyone!"));
							return false;
						}
					}
					else
					if(charStats().getWorshipCharID().length() == 0)
					{
						tell(L("You aren't worshipping anyone!"));
						return false;
					}
					break;
				case CMMsg.TYP_SERVE:
					if(msg.target() == null)
						return false;
					if(msg.target() == this)
					{
						tell(L("You can't serve yourself!"));
						return false;
					}
					if(msg.target() instanceof Deity)
						break;
					if((msg.target() instanceof MOB)
					&& (!flags.canBeHeardSpeakingBy(this, (MOB) msg.target())))
					{
						tell(L("@x1 can't hear you!", ((Physical) msg.target()).name(this)));
						return false;
					}
					if(getLiegeID().length() > 0)
					{
						tell(L("You are already serving '@x1'.", getLiegeID()));
						return false;
					}
					if((msg.target() instanceof MOB) && (((MOB) msg.target()).getLiegeID().equals(Name())))
					{
						tell(L("You can not serve each other!"));
						return false;
					}
					break;
				case CMMsg.TYP_CAST_SPELL:
					if(charStats().getStat(CharStats.STAT_INTELLIGENCE) < 5)
					{
						if((!(msg.tool() instanceof Ability)) // racial ability exemption
						||(charStats().getMyRace().racialAbilities(this).find((Ability)msg.tool())==null))
						{
							tell(L("You aren't smart enough to do magic."));
							return false;
						}
					}
					break;
				default:
					break;
				}

				if((msg.target() instanceof MOB)
				&& (msg.target() != this)
				&& (location() != null)
				&& (location() == ((MOB) msg.target()).location()))
				{
					// and now, the consequences of range
					if(((msg.targetMinor() == CMMsg.TYP_WEAPONATTACK)
						&& (rangeToTarget() > maxRangeWith(msg.tool())))
					|| ((msg.sourceMinor() == CMMsg.TYP_THROW)
						&& (rangeToTarget() > 2)
						&& (maxRangeWith(msg.tool()) <= 0)))
					{
						final MOB trgM = (MOB) msg.target();
						msg.modify(this, trgM, null, CMMsg.MSG_ADVANCE, L("<S-NAME> advance(s) at <T-NAME>."));
						return location().okMessage(this, msg);
					}
					else
					if(msg.tool() != null)
					{
						switch(msg.sourceMinor()) // yes, virginia, this is much faster
						{
						case CMMsg.TYP_BUY:
						case CMMsg.TYP_BID:
						case CMMsg.TYP_SELL:
						case CMMsg.TYP_VIEW:
							break;
						default:
						{
							// this reason this is here and not in stdability protecting
							// mana usage is because the target must be determined for
							// last second ranging, which stdability invoke won't know.
							int useRange = -1;
							final Environmental tool = msg.tool();
							if(getVictim() != null)
							{
								if(getVictim() == msg.target())
									useRange = rangeToTarget();
								else
								{
									final MOB trgM = (MOB) msg.target();
									if(trgM.getVictim() == this)
										useRange = trgM.rangeToTarget();
									else
										useRange = maxRangeWith(tool);
								}
							}
							if((useRange >= 0) && (maxRangeWith(tool) < useRange))
							{
								final MOB trgM = (MOB) msg.target();
								srcM.tell(L("You are too far away from @x1 to use @x2.", trgM.name(srcM), tool.name()));
								return false;
							}
							else
							if((useRange >= 0) && (minRangeWith(tool) > useRange))
							{
								final MOB trgM = (MOB) msg.target();
								srcM.tell(L("You are too close to @x1 to use @x2.", trgM.name(srcM), tool.name()));
								if((msg.targetMinor() == CMMsg.TYP_WEAPONATTACK)
								&& (tool instanceof Weapon)
								&& (!((Weapon) tool).amWearingAt(Wearable.IN_INVENTORY)))
									CMLib.commands().postRemove(this, (Weapon) msg.tool(), false);
								return false;
							}
						}
						break;
						}
					}
				}
			}
		}

		if((msg.targetMinor() != CMMsg.NO_EFFECT) && (msg.amITarget(this)))
		{
			if((amDead()) || (location() == null))
				return false;
			if(msg.targetMajor(CMMsg.MASK_MALICIOUS))
			{
				if(Log.combatChannelOn())
				{
					Log.combatOut(srcM.Name() + ":" + Name() + ":" + CMMsg.TYPE_DESCS[msg.targetMinor()] + ":" + ((msg.tool() != null) ? msg.tool().Name() : "null"));
				}

				if((msg.amISource(this))
				&& (!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&& ((msg.tool() == null)
					|| (!(msg.tool() instanceof Ability))
					|| (!((Ability) msg.tool()).isNowAnAutoEffect())))
				{
					srcM.tell(L("You like yourself too much."));
					if(victim == this)
					{
						victim = null;
						setRangeToTarget(-1);
					}
					return false;
				}

				if((!mayIFight(srcM))
				&& ((!(msg.tool() instanceof Ability))
					|| (((((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_POISON)
						&& ((((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_DISEASE))
					|| ((srcM == this) && (srcM.isMonster()))))
				{
					srcM.tell(L("You may not attack @x1.", name(srcM)));
					srcM.setVictim(null);
					if(victim == srcM)
						setVictim(null);
					return false;
				}

				if((srcM != this)
				&& (!isMonster())
				&& (!srcM.isMonster())
				&& (!msg.targetMajor(CMMsg.MASK_INTERMSG))
				&& (soulMate() == null)
				&& (srcM.soulMate() == null)
				&& (CMath.abs(srcM.phyStats().level() - phyStats().level()) > CMProps.getPKillLevelDiff())
				&& (!CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.PKILL))
				&& (!CMSecurity.isAllowed(srcM, srcM.location(), CMSecurity.SecFlag.PKILL))
				&& ((!CMLib.law().doesHavePriviledgesHere(srcM, location())) // castle doctrine
					|| (victim == null)
					|| (srcM.isInCombat()))
				&& ((!CMLib.flags().canNotBeCamped(srcM))||(!CMLib.flags().canNotBeCamped(this))) // duel exception
				&& ((!(msg.tool() instanceof Ability))
					|| (((Ability) msg.tool()).classificationCode() & Ability.ALL_ACODES) != Ability.ACODE_DISEASE))
				{
					srcM.tell(L("That is not EVEN a fair fight."));
					srcM.setVictim(null);
					if(victim == srcM)
						setVictim(null);
					return false;
				}

				if((amFollowing() == srcM)
				&& (!(msg.tool() instanceof DiseaseAffect)))
					setFollowing(null);

				if(isInCombat())
				{
					if((rangeToTarget() > 0)
					&& (getVictim() != srcM)
					&& (srcM.getVictim() == this)
					&& (srcM.rangeToTarget() == 0))
					{
						setVictim(srcM);
						setRangeToTarget(0);
					}
				}
				if(!CMLib.combat().checkSavingThrows(this, msg))
					return false;
			}

			if((rangeToTarget() >= 0) && (!isInCombat()))
				setRangeToTarget(-1);

			switch(msg.targetMinor())
			{
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_DRINK:
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_EAT:
			case CMMsg.TYP_FILL:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_UNLOCK:
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_WIELD:
				srcM.tell(srcM, this, null, L("You can't do that to <T-NAMESELF>."));
				return false;
			case CMMsg.TYP_POUR:
			{
				boolean permitted = false;
				if(msg.tool() instanceof Potion)
				{
					for(final Ability A : ((Potion) msg.tool()).getSpells())
					{
						if(A.castingQuality(msg.source(), this) != Ability.QUALITY_MALICIOUS)
							permitted = true;
					}
				}
				else
					permitted = true;
				if(!permitted)
				{
					srcM.tell(srcM, this, null, L("You can't do that to <T-NAMESELF>."));
					return false;
				}
				break;
			}
			case CMMsg.TYP_TEACH:
				if(playerStats() == null)
				{
					srcM.tell(srcM, this, null, L("<T-NAME> isn't interested."));
					return false;
				}
				if(playerStats().isIgnored(msg.source()))
					return false;
				if(!CMLib.expertises().canBeTaught(msg.source(), (MOB) msg.target(), msg.tool(), msg.targetMessage()))
					return false;
				break;
			case CMMsg.TYP_DAMAGE:
				if(!CMLib.combat().checkDamageSaves(this, msg))
					return false;
				break;
			case CMMsg.TYP_PULL:
				if((!CMLib.flags().isBoundOrHeld(this)) && (!CMLib.flags().isSleeping(this)))
				{
					srcM.tell(srcM, this, null, L("You can't do that to <T-NAMESELF>."));
					return false;
				}
				if(phyStats().weight() > (srcM.maxCarry() / 2))
				{
					srcM.tell(srcM, this, null, L("<T-NAME> is too big for you to pull."));
					return false;
				}
				if((srcM.curState().getMovement() < phyStats.movesReqToPull())
				&& (srcM.curState().getMovement() < srcM.maxState().getMovement()))
				{
					srcM.tell(L("You don't have enough movement."));
					return false;
				}
				break;
			case CMMsg.TYP_PUSH:
				if((!CMLib.flags().isBoundOrHeld(this)) && (!CMLib.flags().isSleeping(this)))
				{
					srcM.tell(srcM, this, null, L("You can't do that to <T-NAMESELF>."));
					return false;
				}
				if(phyStats().weight() > (srcM.maxCarry() / 2))
				{
					srcM.tell(srcM, this, null, L("<T-NAME> is too heavy for you to push."));
					return false;
				}
				if((srcM.curState().getMovement() < phyStats.movesReqToPush())
				&& (srcM.curState().getMovement() < srcM.maxState().getMovement()))
				{
					srcM.tell(L("You don't have enough movement."));
					return false;
				}
				break;
			case CMMsg.TYP_MOUNT:
			case CMMsg.TYP_DISMOUNT:
				if(!(this instanceof Rideable))
				{
					srcM.tell(srcM, this, null, L("You can't do that to <T-NAMESELF>."));
					return false;
				}
				break;
			case CMMsg.TYP_GIVE:
				if(!(msg.tool() instanceof Item))
					return false;
				if(CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.ORDER)
				|| (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.CMDMOBS) && (isMonster()))
				|| (CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.CMDROOMS) && (isMonster())))
					return true;
				if((charStats().getBodyPart(Race.BODY_ARM) == 0)
				&& (baseCharStats().getMyRace().bodyMask()[Race.BODY_ARM] > 0)
				&& (!CMath.bset(msg.targetMajor(), CMMsg.MASK_ALWAYS)))
				{
					srcM.tell(L("@x1 is unable to accept that from you.", name(srcM)));
					return false;
				}
				if((!CMLib.flags().canBeSeenBy(msg.tool(), this))
				&& (!CMath.bset(msg.targetMajor(), CMMsg.MASK_ALWAYS)))
				{
					srcM.tell(L("@x1 can't see what you are giving.", name(srcM)));
					return false;
				}
				final int GC = msg.targetMajor() & CMMsg.MASK_ALWAYS;
				CMMsg msg2 = CMClass.getMsg(srcM, msg.tool(), null,
						CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null,
						CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null,
						CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null);
				if(!location().okMessage(srcM, msg2))
					return false;
				if(msg.target() instanceof MOB)
				{
					msg2 = CMClass.getMsg((MOB) msg.target(), msg.tool(), null,
							GC | CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null,
							GC | CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null,
							GC | CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null);
					if(!location().okMessage(msg.target(), msg2))
					{
						srcM.tell(L("@x1 cannot seem to accept @x2.", ((Physical) msg.target()).name(srcM), ((Physical) msg.tool()).name(this)));
						return false;
					}
				}
				break;
			case CMMsg.TYP_FOLLOW:
				if(totalFollowers() + srcM.totalFollowers() >= maxFollowers())
				{
					srcM.tell(L("@x1 can't accept any more followers.", name(srcM)));
					return false;
				}
				if((CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF) > 0)
				&& (!isMonster())
				&& (!srcM.isMonster())
				&& (!CMSecurity.isAllowed(this, location(), CMSecurity.SecFlag.ORDER))
				&& (!CMSecurity.isAllowed(srcM, srcM.location(), CMSecurity.SecFlag.ORDER)))
				{
					if(phyStats.level() > (srcM.phyStats().level() + CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						srcM.tell(L("@x1 is too advanced for you.", name(srcM)));
						return false;
					}
					if(phyStats.level() < (srcM.phyStats().level() - CMProps.getIntVar(CMProps.Int.FOLLOWLEVELDIFF)))
					{
						srcM.tell(L("@x1 is too inexperienced for you.", name(srcM)));
						return false;
					}
				}
				break;
			}
		}
		if((srcM != this)
		&& (msg.target() != this))
		{
			if((msg.othersMinor() == CMMsg.TYP_DEATH)
			&& (msg.sourceMinor() == CMMsg.TYP_DEATH))
			{
				if((followers != null)
				&& (followers.containsFirst(srcM))
				&& (CMLib.dice().rollPercentage() == 1)
				&& (fetchEffect("Disease_Depression") == null)
				&& (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A = CMClass.getAbility("Disease_Depression");
					if((A != null) && (!CMSecurity.isAbilityDisabled(A.ID())))
						A.invoke(this, this, true, 0);
				}
			}

		}
		return true;
	}

	@Override
	public void tell(final MOB source, final Environmental target, final Environmental tool, final String msg)
	{
		final Session S = mySession;
		if((S != null) && (msg != null))
			S.stdPrintln(source, target, tool, msg);
	}

	@Override
	public void tell(final String msg)
	{
		tell(this, this, null, msg);
	}

	protected String fixChannelColors(final int channelCode, final String message)
	{

		final Session session = mySession;
		if((session != null)
		&& ((128 + channelCode) >= 0)
		&& ((128 + channelCode) < session.getColorCodes().length)
		&& (session.getColorCodes()[128 + channelCode] != null))
			return CMStrings.replaceAll(message, ColorLibrary.SpecialColor.CHANNEL.getEscapeCode(), mySession.getColorCodes()[128 + channelCode]);
		else
		{
			final CMChannel chan = CMLib.channels().getChannel(channelCode);
			if(chan == null)
				Log.errOut("Unknown channel number: " + channelCode);
			else
			if((chan.colorOverrideANSICodes() != null)
			&& (chan.colorOverrideANSICodes().length() > 0))
				return CMStrings.replaceAll(message, ColorLibrary.SpecialColor.CHANNEL.getEscapeCode(), chan.colorOverrideANSICodes());
		}
		return message;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final Deity deity = charStats().getMyDeity();
		if((deity != null) && (deity != this))
			deity.executeMsg(this, msg);

		final CharStats cStats = charStats;
		if(cStats != null)
		{
			for(int c = 0; c < cStats.numClasses(); c++)
				cStats.getMyClass(c).executeMsg(this, msg);
			cStats.getMyRace().executeMsg(this, msg);
		}

		if(numBehaviors() > 0)
		{
			eachBehavior(new EachApplicable<Behavior>()
			{
				@Override
				public final void apply(final Behavior B)
				{
					B.executeMsg(me, msg);
				}
			});
		}
		if(numScripts() > 0)
		{
			eachScript(new EachApplicable<ScriptingEngine>()
			{
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.executeMsg(me, msg);
				}
			});
		}

		final MOB srcM = msg.source();
		final CMFlagLibrary flagLib = CMLib.flags();
		final boolean asleep = flagLib.isSleeping(this);
		final boolean canseesrc = flagLib.canBeSeenBy(srcM, this);
		final boolean canhearsrc = (msg.targetMinor() == CMMsg.TYP_SPEAK) ?
									flagLib.canBeHeardSpeakingBy(srcM, this) :
									flagLib.canBeHeardMovingBy(srcM, this);

		// first do special cases...
		if(msg.amITarget(this) && (!amDead))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_HEALING:
				CMLib.combat().handleBeingHealed(msg);
				break;
			case CMMsg.TYP_SNIFF:
				CMLib.commands().handleBeingSniffed(msg);
				break;
			case CMMsg.TYP_DAMAGE:
				CMLib.combat().handleBeingDamaged(msg);
				break;
			case CMMsg.TYP_TEACH:
				if(msg.target() instanceof MOB)
					CMLib.expertises().handleBeingTaught(srcM, (MOB) msg.target(), msg.tool(), msg.targetMessage(), msg.value());
				break;
			case CMMsg.TYP_GRAVITY:
				CMLib.combat().handleBeingGravitied(this, msg);
				break;
			default:
				break;
			}
		}

		// now go on to source activities
		if((msg.sourceCode() != CMMsg.NO_EFFECT) && (msg.amISource(this)))
		{
			if((msg.tool() instanceof Item)
			&&(((Item)msg.tool()).owner()==null))
				((Item)msg.tool()).executeMsg(myHost, msg);
			if((msg.sourceMajor(CMMsg.MASK_MALICIOUS))
			&& (!msg.sourceMajor(CMMsg.MASK_INTERMSG))
			&& (msg.target() instanceof MOB)
			&& (getVictim() != msg.target())
			&&(msg.source()!=msg.target())
			&& ((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				|| (!(msg.tool() instanceof DiseaseAffect))))
			{
				CMLib.combat().establishRange(this, (MOB)msg.target(), msg.tool());
				if(!((MOB)msg.target()).isPlayer())
					CMLib.awards().giveAutoProperties((MOB)msg.target(), false);
				if(!isPlayer())
					CMLib.awards().giveAutoProperties(this, false);
				if((msg.tool() instanceof Weapon)
				|| (msg.sourceMinor() == CMMsg.TYP_WEAPONATTACK)
				|| (!flagLib.isAliveAwakeMobileUnbound((MOB)msg.target(), true)))
				{
					setVictim((MOB)msg.target());
					combatStarted();
				}
			}

			if(msg.sourceMajor(CMMsg.MASK_CHANNEL))
			{
				int channelCode;
				if(msg.othersMajor(CMMsg.MASK_CHANNEL))
					channelCode = msg.othersMinor() - CMMsg.TYP_CHANNEL;
				else
				if(msg.targetMajor(CMMsg.MASK_CHANNEL))
					channelCode = msg.targetMinor() - CMMsg.TYP_CHANNEL;
				else
				{
					tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					return;
				}
				tell(srcM, msg.target(), msg.tool(), fixChannelColors(channelCode, msg.sourceMessage()));
			}
			else
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_LIFE:
					CMLib.commands().handleComeToLife(this, msg);
					break;
				case CMMsg.TYP_PANIC:
					CMLib.commands().postFlee(this, "");
					break;
				case CMMsg.TYP_EXPCHANGE:
					CMLib.leveler().handleExperienceChange(msg);
					break;
				case CMMsg.TYP_RPXPCHANGE:
					CMLib.leveler().handleRPExperienceChange(msg);
					break;
				case CMMsg.TYP_RETREAT:
					{
						setRangeToTarget(rangeToTarget() + 1);
						if(victim != null)
						{
							victim.setRangeToTarget(rangeToTarget());
							victim.recoverPhyStats();
						}
						else
							setRangeToTarget(-1);
						recoverPhyStats();
						if(msg.sourceMessage() != null)
							tell(this, msg.target(), msg.tool(), msg.sourceMessage());
					}
					break;
				case CMMsg.TYP_ADVANCE:
					if(rangeToTarget()>=1)
					{
						setRangeToTarget(rangeToTarget() - 1);
						if(victim != null)
						{
							victim.setRangeToTarget(rangeToTarget());
							victim.recoverPhyStats();
						}
						else
							setRangeToTarget(-1);
						recoverPhyStats();
						if(msg.sourceMessage() != null)
							tell(this, msg.target(), msg.tool(), msg.sourceMessage());
					}
					break;
				case CMMsg.TYP_FACTIONCHANGE:
					if(msg.othersMessage() != null)
					{
						if((msg.value() == Integer.MAX_VALUE)
						|| (msg.value() == Integer.MIN_VALUE))
							removeFaction(msg.othersMessage());
						else
							adjustFaction(msg.othersMessage(), msg.value());
					}
					if(msg.sourceMessage() != null)
						tell(this, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_DEATH:
					CMLib.get(mySession)._combat().handleDeath(msg);
					break;
				case CMMsg.TYP_REBUKE:
					if((getLiegeID().length() > 0)
					&& (!isMarriedToLiege()))
					{
						if((msg.target() == null)
						|| ((msg.target().Name().equals(getLiegeID())) && (!isMarriedToLiege())))
							setLiegeID("");
					}
					tell(this, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_SERVE:
					if((msg.target() != null)
					&& (!(msg.target() instanceof Deity)))
						setLiegeID(msg.target().Name());
					tell(this, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_LOOK:
				case CMMsg.TYP_EXAMINE:
					if(msg.target() == this)
						CMLib.commands().handleBeingLookedAt(msg);
					break;
				case CMMsg.TYP_READ:
					if((flagLib.canBeSeenBy(this, srcM))
					&& (msg.amITarget(this)))
						srcM.tell(L("There is nothing written on @x1", name(srcM)));
					break;
				case CMMsg.TYP_SIT:
					CMLib.commands().handleSit(msg);
					break;
				case CMMsg.TYP_SLEEP:
					CMLib.commands().handleSleep(msg);
					break;
				case CMMsg.TYP_QUIT:
					tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_STAND:
					CMLib.commands().handleStand(msg);
					break;
				case CMMsg.TYP_RECALL:
					CMLib.commands().handleRecall(msg);
					break;
				case CMMsg.TYP_FOLLOW:
					if(msg.target() instanceof MOB)
					{
						if(!isPlayer())
							CMLib.awards().giveAutoProperties(me, false);
						setFollowing((MOB) msg.target());
						tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					}
					break;
				case CMMsg.TYP_NOFOLLOW:
					setFollowing(null);
					tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_WEAR:
				case CMMsg.TYP_HOLD:
				case CMMsg.TYP_WIELD:
				case CMMsg.TYP_REMOVE:
					possWieldedItem = null;
					possHeldItem = null;
					tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_ATTACKMISS:
					if(!isAttributeSet(Attrib.NOBATTLESPAM))
						tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				case CMMsg.TYP_WROTE:
					if((msg.target() instanceof Item)
					&& (msg.targetMessage() != null)
					&& (msg.targetMessage().length() > 0)
					&& (!CMSecurity.isDisabled(DisFlag.AUTODISEASE)))
					{
						final String msgStr = msg.targetMessage().trim();
						if(msgStr.length() > 10)
						{
							final int chc = 30 + (((msg.target() instanceof Book) && ((Book) msg.target()).isJournal()) ? 30 : 0);
							if((CMLib.dice().rollPercentage() < chc)
							&& (CMLib.dice().rollPercentage() > CMLib.english().probabilityOfBeingEnglish(msgStr)))
							{
								final Ability A = CMClass.getAbility("Disease_WritersBlock");
								if((A != null)
								&& (fetchEffect(A.ID()) == null) && (!CMSecurity.isAbilityDisabled(A.ID())))
									A.invoke(this, this, true, 0);
							}
						}
					}
					break;
				case CMMsg.TYP_EXPIRE:
					if(msg.target() instanceof Area)
					{
						if(!isMonster())
							CMLib.achievements().possiblyBumpAchievement(this, Event.INSTANCEEXPIRE, 1, new Object[] { (Area) msg.target(), Integer.valueOf(msg.value()) });
						break;
					}
					break;
				default:
					if((msg.targetMinor() == CMMsg.TYP_DAMAGE)
					&& (isAttributeSet(Attrib.NOBATTLESPAM))
					&& (msg.target() instanceof Physical)
					&& (CMLib.combat().handleDamageSpam(this, (Physical) msg.target(), msg.value())))
						break;
					// you pretty much always know what you are doing, if you can do it.
					if(!CMath.bset(msg.sourceMajor(), CMMsg.MASK_CNTRLMSG))
						tell(srcM, msg.target(), msg.tool(), msg.sourceMessage());
					break;
				}
			}
			if(triggerer.isObsolete() || (!triggerer.isDisabled()))
				CMLib.ableComponents().handleAbilityComponentTriggers(msg);
		}
		else
		if((msg.targetMinor() != CMMsg.NO_EFFECT)
		&& (msg.amITarget(this)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_HEALING:
			case CMMsg.TYP_DAMAGE:
				// handled as special cases above
				break;
			case CMMsg.TYP_GIVE:
				if(msg.tool() instanceof Item)
					CMLib.commands().handleBeingGivenTo(msg);
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(flagLib.canBeSeenBy(this, srcM))
					CMLib.commands().handleBeingLookedAt(msg);
				break;
			case CMMsg.TYP_REBUKE:
				if((srcM.Name().equals(getLiegeID()) && (!isMarriedToLiege())))
					setLiegeID("");
				break;
			case CMMsg.TYP_SPEAK:
			{
				if((CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM)
					|| CMProps.get(mySession).getBool(CMProps.Bool.INTRODUCTIONSYSTEM))
				&& (!asleep) && (canhearsrc))
					CMLib.commands().handleIntroductions(srcM, this, msg.targetMessage());
				CMLib.commands().handleBeingSpokenTo(srcM, this, msg.targetMessage());
				break;
			}
			case CMMsg.TYP_ORDER:
			{
				if(msg.targetMessage()!=null)
					enqueCommand(CMParms.parse(CMStrings.getSayFromMessage(msg.targetMessage())),MUDCmdProcessor.METAFLAG_ORDER,0);
				break;
			}
			default:
				if((CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS)) && (!amDead))
					CMLib.combat().handleBeingAssaulted(msg);
				else
				if(msg.targetMajor(CMMsg.MASK_CHANNEL))
				{
					final int channelCode = msg.targetMinor() - CMMsg.TYP_CHANNEL;
					if((playerStats() != null)
					&& (!this.isAttributeSet(MOB.Attrib.QUIET))
					&& (!CMath.isSet(playerStats().getChannelMask(), channelCode)))
						tell(srcM, msg.target(), msg.tool(), fixChannelColors(channelCode, msg.targetMessage()));
				}
				break;
			}

			// now do the says
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE:
			{
				if((!isAttributeSet(Attrib.NOBATTLESPAM))
				|| (!(msg.target() instanceof Physical))
				|| (!CMLib.combat().handleDamageSpam(this, (Physical) msg.target(), msg.value())))
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				break;
			}
			case CMMsg.TYP_HEALING:
			{
				tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				break;
			}
			case CMMsg.TYP_ATTACKMISS:
			{
				if(!isAttributeSet(Attrib.NOBATTLESPAM))
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				break;
			}
			default:
				if((msg.targetMajor(CMMsg.MASK_SOUND))
				&& (canhearsrc) && (!asleep))
				{
					if((msg.targetMinor() == CMMsg.TYP_SPEAK)
					&& (srcM != null)
					&& (playerStats() != null)
					&& (!srcM.isMonster())
					&& (flagLib.canBeHeardSpeakingBy(srcM, this)))
						playerStats().setReplyTo(srcM, PlayerStats.REPLY_SAY);
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				}
				else
				if(msg.targetMajor(CMMsg.MASK_ALWAYS))
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				else
				if((msg.targetMajor(CMMsg.MASK_EYES)) && ((!asleep) && (canseesrc)))
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				else
				if(msg.targetMajor(CMMsg.MASK_MALICIOUS))
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				else
				if(((msg.targetMajorAny(CMMsg.MASK_HANDS|CMMsg.MASK_MOVE))
					|| ((msg.targetMajor(CMMsg.MASK_MOUTH) && (!msg.targetMajor(CMMsg.MASK_SOUND)))))
				&& (!asleep) && ((canhearsrc) || (canseesrc)))
					tell(srcM, msg.target(), msg.tool(), msg.targetMessage());
				break;
			}
		}
		else
		if((msg.othersCode() != CMMsg.NO_EFFECT)
		&& (!msg.amISource(this))
		&& (!msg.amITarget(this)))
		{
			final int othersMinor = msg.othersMinor();
			if(msg.othersMajor(CMMsg.MASK_MALICIOUS)
			&&(!msg.othersMajor(CMMsg.MASK_INTERMSG))
			&& (msg.target() instanceof MOB)
			&& ((!msg.sourceMajor(CMMsg.MASK_ALWAYS)) || (!(msg.tool() instanceof DiseaseAffect))))
				CMLib.combat().makeFollowersFight(this, (MOB) msg.target(), srcM);

			if(isAttributeSet(Attrib.NOBATTLESPAM)
			&& (((msg.targetMinor() == CMMsg.TYP_DAMAGE)
					&& (msg.target() instanceof Physical)
					&& (CMLib.combat().handleDamageSpam(this, (Physical) msg.target(), msg.value())))
				|| (msg.targetMinor() == CMMsg.TYP_ATTACKMISS)))
			{
				// don't say diddly
			}
			else
			switch(othersMinor)
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_FLEE:
			case CMMsg.TYP_LEAVE:
			{
				if(((!asleep) || (othersMinor == CMMsg.TYP_ENTER))
				&& (flagLib.canSenseEnteringLeaving(srcM, this)))
				{
					tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
					if((mySession != null)
					&& (mySession.getClientTelnetMode(Session.TELNET_GMCP)))
					{
						if (msg.othersMinor() == CMMsg.TYP_ENTER)
							mySession.sendGMCPEvent("room.enter", "\"" + MiniJSON.toJSONString(srcM.Name()) + "\"");
						if (msg.othersMinor() == CMMsg.TYP_LEAVE)
							mySession.sendGMCPEvent("room.leave", "\"" + MiniJSON.toJSONString(srcM.Name()) + "\"");
					}
				}
				if((!isMonster())
				&& (riding != null)
				&& (riding.rideBasis() == Rideable.Basis.WATER_BASED)
				&& (CMLib.dice().rollPercentage() == 1)
				&& (CMLib.dice().rollPercentage() < 10)
				&& (flagLib.isWateryRoom(location()))
				&& (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A = CMClass.getAbility("Disease_SeaSickness");
					if((A != null)
					&& (fetchEffect(A.ID()) == null)
					&& (!CMSecurity.isAbilityDisabled(A.ID())))
						A.invoke(this, this, true, 0);
				}
				break;
			}
			default:
				if(msg.othersMessage() != null)
				{
					if(msg.othersMajor(CMMsg.MASK_SPAMMY)
					&&(isAttributeSet(Attrib.NOSPAM)))
					{}
					else
					if(msg.othersMajor(CMMsg.MASK_CHANNEL))
					{
						final int channelCode = (msg.othersMinor() - CMMsg.TYP_CHANNEL);
						if((playerStats() != null)
						&& (!this.isAttributeSet(MOB.Attrib.QUIET))
						&& (!CMath.isSet(playerStats().getChannelMask(), channelCode)))
							tell(srcM, msg.target(), msg.tool(), fixChannelColors(channelCode, msg.othersMessage()));
					}
					else
					if((msg.othersMajor(CMMsg.MASK_SOUND)) && (!asleep) && (canhearsrc))
					{
						if((msg.othersMinor() == CMMsg.TYP_SPEAK)
						&& (CMProps.getBoolVar(CMProps.Bool.INTRODUCTIONSYSTEM) || CMProps.get(mySession).getBool(CMProps.Bool.INTRODUCTIONSYSTEM)))
							CMLib.commands().handleIntroductions(srcM, this, msg.othersMessage());
						tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
					}
					else
					if(othersMinor == CMMsg.TYP_AROMA)
					{
						if(flagLib.canSmell(this))
							tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
					}
					else
					if((msg.othersMajorAny(CMMsg.MASK_EYES|CMMsg.MASK_HANDS|CMMsg.MASK_ALWAYS))
					&& (!msg.othersMajor(CMMsg.MASK_CNTRLMSG))
					&& ((!asleep) && (canseesrc)))
						tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
					else
					if(((msg.othersMajor(CMMsg.MASK_MOVE))
						|| (msg.othersMajor(CMMsg.MASK_MOUTH) && (!msg.othersMajor(CMMsg.MASK_SOUND))))
					&& (!asleep) && ((canseesrc) || (canhearsrc)))
						tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
					else
					if((msg.sourceMinor() == CMMsg.TYP_TELL)
					&& (msg.targetCode() == CMMsg.NO_EFFECT)) // group// tell
						tell(srcM, msg.target(), msg.tool(), msg.othersMessage());
				}
				break;
			}

			switch(othersMinor)
			{
			case CMMsg.TYP_ADVANCE:
			case CMMsg.TYP_ENTER:
				if(!isMonster())
				{
					final Room R = location();
					if((R != null)
					&& (R.getArea() instanceof Boardable)
					&& (srcM.riding() == ((Boardable)R.getArea()).getBoardableItem())
					&& (CMLib.dice().rollPercentage() == 1)
					&& (!CMSecurity.isDisabled(DisFlag.AUTODISEASE))
					&& (flagLib.isWateryRoom(CMLib.map().roomLocation(((Boardable)R.getArea()).getBoardableItem())))
					&& (CMLib.dice().rollPercentage() < 10))
					{
						final Ability A = CMClass.getAbility((CMLib.dice().rollPercentage() < 20) ? "Disease_Scurvy" : "Disease_SeaSickness");
						if((A != null)
						&& (fetchEffect(ID()) == null)
						&& (!CMSecurity.isAbilityDisabled(A.ID())))
							A.invoke(this, this, true, 0);
					}
				}
				break;
			case CMMsg.TYP_DEATH:
				CMLib.combat().handleObserveDeath(this, victim, msg);
				break;
			default:
				if(msg.sourceMinor() == CMMsg.TYP_LIFE)
					CMLib.commands().handleObserveComesToLife(this, srcM, msg);
				break;
			}
		}

		// the order here is significant (between eff and item -- see focus)
		if(numItems() > 0)
		{
			eachItem(new EachApplicable<Item>()
			{
				@Override
				public final void apply(final Item I)
				{
					I.executeMsg(me, msg);
				}
			});
		}

		if(numAllEffects() > 0)
		{
			eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public final void apply(final Ability A)
				{
					A.executeMsg(me, msg);
				}
			});
		}

		for(final Enumeration<Faction.FData> e = factions.elements(); e.hasMoreElements();)
		{
			final Faction.FData fD = e.nextElement();
			fD.getFaction().executeMsg(this, msg);
			fD.executeMsg(this, msg);
		}
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	protected final void combatStarted()
	{
		if((!isMonster()) && (this.peaceTime > 0))
		{
			if(isAttributeSet(MOB.Attrib.NOBATTLESPAM))
				tell(L("^F^<FIGHT^>You are now in combat.^</FIGHT^>^N"));
			if(isPlayer())
				playerStats().bumpLevelCombatStat(PlayerCombatStat.COMBATS_TOTAL, basePhyStats().level(), 1);
			this.peaceTime = 0;
			if(mySession!=null)
				mySession.setStat("PPING", "true");
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(removeFromGame)
			return false;
		tickStatus = Tickable.STATUS_START;
		if(tickID == Tickable.TICKID_MOB)
		{
			final boolean isMonster = isMonster();
			if(amDead)
			{
				boolean isOk = !removeFromGame;
				tickStatus = Tickable.STATUS_DEAD;
				if(isMonster)
				{
					if((phyStats().rejuv() != PhyStats.NO_REJUV) && (basePhyStats().rejuv() > 0))
					{
						phyStats().setRejuv(phyStats().rejuv() - 1);
						if((phyStats().rejuv() < 0) || (CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
						{
							final Room startRoom = CMLib.map().getStartRoom(this);
							if((startRoom == null)
							|| (startRoom.amDestroyed())
							|| (startRoom.getArea() == null)
							|| (startRoom.getArea().amDestroyed()))
							{
								tickStatus = Tickable.STATUS_END;
								if(soulMate() == null)
									destroy();
								isOk = false;
							}
							else
							{
								if((CMLib.flags().canNotBeCamped(this) || CMLib.flags().canNotBeCamped(startRoom))
								&& (startRoom.numPCInhabitants() > 0)
								&& (!CMLib.hunt().isAnAdminHere(startRoom, false)))
								{
									phyStats().setRejuv(0);
									tickStatus = Tickable.STATUS_NOT;
									lastTickedTime = System.currentTimeMillis();
									return isOk;
								}
								tickStatus = Tickable.STATUS_REBIRTH;
								cloneFix(CMClass.getMOBPrototype(ID()));
								bringToLife(startRoom, true);
								final Room room = location();
								if(room != null)
								{
									final Area A = room.getArea();
									if((lastTickedTime < 0)
									&& room.getMobility()
									&& (A.getAreaState() != Area.State.FROZEN)
									&& (A.getAreaState() != Area.State.STOPPED))
										lastTickedTime = CMLib.utensils().processVariableEquipment(this, true);
									room.showOthers(this, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> appears!"));
								}
							}
						}
					}
					else
					{
						tickStatus = Tickable.STATUS_END;
						if(soulMate() == null)
							destroy();
						isOk = false;
					}
				}
				tickStatus = Tickable.STATUS_NOT;
				lastTickedTime = System.currentTimeMillis();
				return isOk;
			}
			else
			{
				final Room R = location();
				final Area A = (R == null) ? null : R.getArea();
				if(isMonster && (--validChkCounter <= 0))
				{
					validChkCounter = 60;
					final String mobReport = CMLib.flags().validCheck(this);
					if(mobReport != null)
					{
						if(mobReport.startsWith("*"))
						{
							CMLib.s_sleep(1);
							final Room locR = location();
							if((locR == null) || (amDead) || (!locR.isInhabitant(this)))
							{
								Log.warnOut("Killing: " + mobReport);
								killMeDead(false);
							}
						}
						else
						{
							Log.warnOut("Destroy: " + mobReport);
							this.destroy();
							return false;
						}
					}
					if((R != getStartRoom())
					&&(getStartRoom() != null))
					{
						// if npc is not mobile, perhaps it should consider going home?
					}
				}
				if((R != null) && (A != null))
				{
					final CMFlagLibrary flag = CMLib.flags();

					// handle variable equipment!
					if((lastTickedTime < 0)
					&& isMonster
					&& R.getMobility()
					&& (A.getAreaState() != Area.State.FROZEN)
					&& (A.getAreaState() != Area.State.STOPPED))
					{
						if(lastTickedTime == -1)
							lastTickedTime = CMLib.utensils().processVariableEquipment(this, false);
						else
							lastTickedTime++;
					}

					tickStatus = Tickable.STATUS_ALIVE;

					if( isInCombat()
					&&(CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) == CombatLibrary.CombatSystem.TURNBASED.ordinal()))
					{
						if(CMLib.combat().doTurnBasedCombat(this, R, A))
						{
							if(lastTickedTime >= 0)
								lastTickedTime = System.currentTimeMillis();
							tickStatus = Tickable.STATUS_NOT;
							return !removeFromGame;
						}
					}
					else
					{
						if(commandQueSize() == 0)
							setActions(actions() - Math.floor(actions()));
						setActions(actions() + (flag.isSitting(this) ? phyStats().speed() / 2.0 : phyStats().speed()));
					}

					if(!flag.isGolem(this))
					{
						if(!flag.canBreathe(this))
						{
							final MOB killerM = CMLib.combat().getBreatheKiller(this);
							// R.show(this, this, CMMsg.MSG_OK_VISUAL,
							CMLib.combat().postDamage(killerM, this, this, (int) Math.round(CMath.mul(Math.random(), basePhyStats().level() + 2)),
									CMMsg.MASK_ALWAYS | CMMsg.TYP_WATER, -1,
									L("^Z<T-NAME> can't breathe!^.^?") + CMLib.protocol().msp("choke.wav", 10));
							recoverTickCter = CMProps.getIntVar(CMProps.Int.RECOVERRATE)
									* (CharState.REAL_TICK_ADJUST_FACTOR + charStats().getStat(CharStats.STAT_RECOVERRATE5_ADJ));
						}
						else
						if(!flag.canBreatheHere(this, R))
						{
							final int atmo = R.getAtmosphere();
							if((atmo & RawMaterial.MATERIAL_MASK) == RawMaterial.MATERIAL_LIQUID)
							{
								final MOB killerM = CMLib.combat().getBreatheKiller(this);
								// R.show(this, this, CMMsg.MSG_OK_VISUAL, );
								final String msgStr;
								if(!flag.isSeeable(this))
									msgStr = null;
								else
									msgStr = L("^Z<T-NAME> <T-IS-ARE> drowning in @x1!^.^?", RawMaterial.CODES.NAME(atmo).toLowerCase()) + CMLib.protocol().msp("choke.wav", 10);
								CMLib.combat().postDamage(killerM, this, this, (int) Math.round(CMath.mul(Math.random(), basePhyStats().level() + 2)),
										CMMsg.MASK_ALWAYS | CMMsg.TYP_WATER, -1, msgStr);
								recoverTickCter = CMProps.getIntVar(CMProps.Int.RECOVERRATE)
										* (CharState.REAL_TICK_ADJUST_FACTOR + charStats().getStat(CharStats.STAT_RECOVERRATE5_ADJ));
							}
							else
							{
								final String msgStr;
								if(!flag.isSeeable(this))
									msgStr = null;
								else
								if(atmo == 0)
									msgStr = L("^Z<T-NAME> can't breathe!^.^?") + CMLib.protocol().msp("choke.wav", 10);
								else
									msgStr = L("^Z<T-NAME> <T-IS-ARE> choking on @x1!^.^?", RawMaterial.CODES.NAME(atmo).toLowerCase());
								final MOB killerM = CMLib.combat().getBreatheKiller(this);
								CMLib.combat().postDamage(killerM, this, this, (int) Math.round(CMath.mul(Math.random(), basePhyStats().level() + 2)),
										CMMsg.MASK_ALWAYS | CMMsg.TYP_GAS, -1, msgStr);
								recoverTickCter = CMProps.getIntVar(CMProps.Int.RECOVERRATE)
										* (CharState.REAL_TICK_ADJUST_FACTOR + charStats().getStat(CharStats.STAT_RECOVERRATE5_ADJ));
							}
						}
					}

					if((--recoverTickCter) <= 0)
					{
						CMLib.combat().recoverTick(this);
						recoverTickCter = CMProps.getIntVar(CMProps.Int.RECOVERRATE)
								* (CharState.REAL_TICK_ADJUST_FACTOR + charStats().getStat(CharStats.STAT_RECOVERRATE5_ADJ));
					}
					if(!isMonster)
						CMLib.combat().expendEnergy(this, false);

					if(isInCombat())
					{
						if(CMProps.getIntVar(CMProps.Int.COMBATSYSTEM) == CombatLibrary.CombatSystem.DEFAULT.ordinal())
							setActions(actions() + 1.0); // bonus action is employed in default system
						tickStatus = Tickable.STATUS_FIGHT;
						combatStarted();
						if(CMLib.flags().canAutoAttack(this))
							CMLib.combat().tickCombat(this);
						if(!isMonster)
						{
							if(playerStats() != null)
								playerStats().bumpLevelCombatStat(PlayerCombatStat.ROUNDS_TOTAL, basePhyStats().level(), 1);
							CMLib.combat().handleDamageSpamSummary(this);
						}
					}
					else
					{
						peaceTime += CMProps.getTickMillis();
						if(this.isAttributeSet(MOB.Attrib.AUTODRAW)
						&& (peaceTime >= START_SHEATH_TIME)
						&& (peaceTime < END_SHEATH_TIME)
						&& (CMLib.flags().isAliveAwakeMobileUnbound(this, true)))
							CMLib.commands().postSheath(this, true);
						if((!isMonster)
						&& isAttributeSet(MOB.Attrib.NOBATTLESPAM)
						&& (playerStats() != null)
						&& (playerStats().getCombatSpams().size() > 0))
							CMLib.combat().handleDamageSpamSummary(this);
					}

					tickStatus = Tickable.STATUS_OTHER;
					if((!isMonster)
					&& (maxState().getFatigue() > Long.MIN_VALUE / 2)
					&& (!CMSecurity.isDisabled(DisFlag.FATIGUE))
					&& (!charStats().getMyRace().infatigueable()))
					{
						if(CMLib.flags().isSleeping(this))
							curState().adjFatigue(-CharState.REST_PER_SLEEP, maxState());
						// rest/sit isn't here because fatigue is sleepiness, not exhaustion per se
						else
						if(!CMSecurity.isAllowed(this, R, CMSecurity.SecFlag.IMMORT))
						{
							curState().adjFatigue(Math.round(CMProps.getTickMillis()), maxState());
							if(curState().getFatigue() > CharState.FATIGUED_MILLIS)
							{
								final boolean smallChance = (CMLib.dice().rollPercentage() == 1);
								if((curState().getFatigue() > 3 * CharState.FATIGUED_MILLIS)
								&&smallChance
								&&(curState().getHitPoints()>=maxState().getHitPoints())
								&&(fetchEffect("Mood")==null)
								&&(!CMSecurity.isDisabled(DisFlag.AUTOMOODS))
								&&(!phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
								{
									final Ability moodA = CMClass.getAbility("Mood");
									if(moodA != null)
										moodA.invoke(this, new XVector<String>("SILLY"), this, true, 0);
								}
								if(smallChance && (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
								{
									final Ability yawnsA = CMClass.getAbility("Disease_Yawning");
									if((yawnsA != null) && (!CMSecurity.isAbilityDisabled(A.ID())))
										yawnsA.invoke(this, this, true, 0);
								}
								if(smallChance && curState().getFatigue() > (CharState.FATIGUED_EXHAUSTED_MILLIS))
								{
									R.show(this, null, CMMsg.MSG_OK_ACTION, L("<S-NAME> fall(s) asleep from exhaustion!!"));
									basePhyStats().setDisposition(basePhyStats().disposition() | PhyStats.IS_SLEEPING);
									phyStats().setDisposition(phyStats().disposition() | PhyStats.IS_SLEEPING);
									if((CMLib.dice().rollPercentage() < 10) && (!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
									{
										final Ability sleepA = CMClass.getAbility("Disease_Sleepwalking");
										if((sleepA != null) && (!CMSecurity.isAbilityDisabled(A.ID())))
											sleepA.invoke(this, this, true, 0);
									}
								}
							}
						}
					}
					else
					while((!amDead()) && (!amDestroyed) && dequeCommand())
					{
					}

					if((riding() != null) && (CMLib.map().roomLocation(riding()) != R))
						CMLib.tracking().doFallenOffCheck(this);

					if((!isMonster) && (soulMate() == null) && (ageMinutes >= 0))
					{
						CMLib.coffeeTables().bump(this, CoffeeTableRow.STAT_TICKSONLINE);
						if(((++tickAgeCounter) * CMProps.getTickMillis()) >= AGE_MILLIS_THRESHOLD)
						{
							final long secondsPassed = (tickAgeCounter * CMProps.getTickMillis()) / 1000;
							CMLib.achievements().possiblyBumpAchievement(this, AchievementLibrary.Event.TIMEPLAYED, (int) secondsPassed, this);
							tickAgeCounter = 0;
							if(inventory != null)
								inventory.trimToSize();
							if(affects != null)
								affects.trimToSize();
							if(abilitys != null)
								abilitys.trimToSize();
							CMLib.commands().tickAging(this, AGE_MILLIS_THRESHOLD);
						}
					}
				}
				else
				if(isMonster && (!isPlayer()))
				{
					Log.errOut("Destroying " + Name() + " because he's not ticking anywhere at all!");
					this.destroy();
					return false;
				}
			}

			tickStatus = Tickable.STATUS_AFFECT;
			if(numAllEffects() > 0)
			{
				eachEffect(new EachApplicable<Ability>()
				{
					@Override
					public final void apply(final Ability A)
					{
						if(!A.tick(ticking, tickID))
							A.unInvoke();
					}
				});
			}

			manaConsumeCter = CMLib.commands().tickManaConsumption(this, manaConsumeCter);

			if(triggerer.isObsolete() || (!triggerer.isDisabled()))
				CMLib.ableComponents().tickAbilityComponentTriggers(this);

			tickStatus = Tickable.STATUS_BEHAVIOR;
			if(numBehaviors() > 0)
			{
				eachBehavior(new EachApplicable<Behavior>()
				{
					@Override
					public final void apply(final Behavior B)
					{
						B.tick(ticking, tickID);
					}
				});
			}
			tickStatus = Tickable.STATUS_SCRIPT;
			if(numScripts() > 0)
			{
				eachScript(new EachApplicable<ScriptingEngine>()
				{
					@Override
					public final void apply(final ScriptingEngine S)
					{
						S.tick(ticking, tickID);
					}
				});
			}
			// if(isMonster) why wouldn't this be by players Especially?!
			{
				for(final Enumeration<Faction.FData> t = factions.elements(); t.hasMoreElements();)
				{
					final Faction.FData T = t.nextElement();
					if(T.requiresUpdating())
					{
						final String factionID = T.getFaction().factionID();
						final Faction F = CMLib.factions().getFaction(factionID);
						if(F != null)
						{
							final int oldValue = T.value();
							F.updateFactionData(this, T);
							T.setValue(oldValue);
						}
						else
							removeFaction(factionID);
					}
				}
			}

			tickStatus = Tickable.STATUS_OTHER;
			for(final Enumeration<Faction.FData> t = factions.elements(); t.hasMoreElements();)
			{
				final Faction.FData T = t.nextElement();
				T.tick(ticking, tickID);
			}

			final CharStats cStats = charStats();
			final int num = cStats.numClasses();
			tickStatus = Tickable.STATUS_CLASS;
			for(int c = 0; c < num; c++)
				cStats.getMyClass(c).tick(ticking, tickID);
			tickStatus = Tickable.STATUS_RACE;
			cStats.getMyRace().tick(ticking, tickID);
			tickStatus = Tickable.STATUS_END;

			if(lastTickedTime >= 0)
			{
				lastTickedTime = System.currentTimeMillis();
				for(final Tattoo tattoo : tattoos)
				{
					if((tattoo != null)
					&& (tattoo.expirationDate() > 0)
					&& (lastTickedTime > tattoo.expirationDate()))
						delTattoo(tattoo);
				}
			}
		}
		tickStatus = Tickable.STATUS_NOT;
		return !removeFromGame;
	}

	@Override
	public boolean isPlayer()
	{
		return playerStats != null;
	}

	@Override
	public boolean isMonster()
	{
		return (mySession == null) || (mySession.isFake());
	}

	@Override
	public boolean isPossessing()
	{
		try
		{
			for(final Session S : CMLib.sessions().allIterable())
			{
				if((S.mob() != null) && (S.mob().soulMate() == this))
					return true;
			}
		}
		catch (final Exception e)
		{
		}
		return false;
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void addItem(final Item item)
	{
		if((item != null) && (!item.amDestroyed()))
		{
			item.setOwner(this);
			inventory.addElement(item);
			item.recoverPhyStats();
		}
	}

	@Override
	public void addItem(final Item item, final ItemPossessor.Expire expire)
	{
		addItem(item);
	}

	@Override
	public void delItem(final Item item)
	{
		inventory.removeElement(item);
		item.recoverPhyStats();
	}

	@Override
	public void delAllItems(final boolean destroy)
	{
		if((destroy) && (numItems() > 0))
		{
			final LinkedList<Item> items = new LinkedList<Item>();
			items.addAll(inventory);
			inventory.clear();
			for(final Iterator<Item> i = items.descendingIterator(); i.hasNext();)
			{
				final Item I = i.next();
				if(I != null)
				{
					// since were deleting you AND all your peers, no need for Item to do it.
					I.setOwner(null);
					I.destroy();
				}
			}
		}
		inventory.clear();
	}

	@Override
	public int numItems()
	{
		return inventory.size();
	}

	@Override
	public Enumeration<Item> items()
	{
		return inventory.elements();
	}

	@Override
	public boolean isContent(final Item I)
	{
		return inventory.contains(I);
	}

	@Override
	public List<Item> findItems(final Item goodLocation, final String itemName)
	{
		if(inventory.isEmpty())
			return new Vector<Item>(1);
		List<Item> items = CMLib.english().fetchAvailableItems(inventory, itemName, goodLocation, Wearable.FILTER_ANY, true);
		if(items.isEmpty())
			items = CMLib.english().fetchAvailableItems(inventory, itemName, goodLocation, Wearable.FILTER_ANY, false);
		return items;
	}

	@Override
	public Item getItem(final int index)
	{
		try
		{
			return inventory.elementAt(index);
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public void eachItem(final EachApplicable<Item> applier)
	{
		final List<Item> contents = this.inventory;
		if(contents != null)
		{
			try
			{
				for(int a = 0; a < contents.size(); a++)
				{
					final Item I = contents.get(a);
					if(I != null)
					{
						try
						{
							applier.apply(I);
						}
						catch(final Exception e)
						{
							Log.errOut(e);
						}
					}
				}
			}
			catch (final IndexOutOfBoundsException x)
			{
			}
		}
	}

	@Override
	public Item getRandomItem()
	{
		if(numItems() == 0)
			return null;
		return getItem(CMLib.dice().roll(1, numItems(), -1));
	}

	public Item fetchFromInventory(final Item goodLocation, final String itemName, final Filterer<Environmental> filter, final boolean respectLocationAndWornCode)
	{
		if(inventory.isEmpty())
			return null;
		final SVector<Item> inv = inventory;
		Item item = null;
		if(respectLocationAndWornCode)
		{
			item = CMLib.english().fetchAvailableItem(inv, itemName, goodLocation, filter, true);
			if(item == null)
				item = CMLib.english().fetchAvailableItem(inv, itemName, goodLocation, filter, false);
		}
		else
		{
			item = (Item) CMLib.english().fetchEnvironmental(inv, itemName, true);
			if(item == null)
				item = (Item) CMLib.english().fetchEnvironmental(inv, itemName, false);
		}
		return item;
	}

	@Override
	public Item findItem(final String itemName)
	{
		return fetchFromInventory(null, itemName, Wearable.FILTER_ANY, false);
	}

	@Override
	public Item findItem(final Item goodLocation, final String itemName)
	{
		return fetchFromInventory(goodLocation, itemName, Wearable.FILTER_ANY, true);
	}

	@Override
	public Item fetchItem(final Item goodLocation, final Filterer<Environmental> filter, final String itemName)
	{
		return fetchFromInventory(goodLocation, itemName, filter, true);
	}

	@Override
	public String getContextName(final Environmental E)
	{
		if(E instanceof Item)
		{
			final String ctxName=CMLib.english().getContextName(inventory,E);
			if(ctxName!=null)
				return ctxName;
		}
		return "nothing";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Item> findItems(final String itemName)
	{
		if(!inventory.isEmpty())
		{
			List V = CMLib.english().fetchEnvironmentals(inventory, itemName, true);
			if((V != null) && (!V.isEmpty()))
				return V;
			V = CMLib.english().fetchEnvironmentals(inventory, itemName, false);
			if(V != null)
				return V;
		}
		return new Vector<Item>(1);
	}

	@Override
	public void addFollower(final MOB follower, final int order)
	{
		if(follower != null)
		{
			if(follower == this)
			{
				followOrder = order;
				return;
			}
			if(followers == null)
				followers = new SPairList<MOB, Short>();
			else
			{
				for(final Pair<MOB, Short> F : followers)
				{
					if(F.first == follower)
					{
						F.second = Short.valueOf((short) order);
						return;
					}
				}
			}
			followers.add(follower, Short.valueOf((short) order));
		}
	}

	@Override
	public void delFollower(final MOB follower)
	{
		if((follower != null) && (followers != null))
		{
			for(final Pair<MOB, Short> F : followers)
			{
				if(F.first == follower)
					followers.remove(F);
			}
		}
	}

	@Override
	public int numFollowers()
	{
		return (followers == null) ? 0 : followers.size();
	}

	private static final Enumeration<Pair<MOB, Short>>	emptyFollowers	= new EmptyEnumeration<Pair<MOB, Short>>();

	@Override
	public Enumeration<Pair<MOB, Short>> followers()
	{
		return (followers == null) ? emptyFollowers : new IteratorEnumeration<Pair<MOB, Short>>(followers.iterator());
	}

	@Override
	public int fetchFollowerOrder(final MOB thisOne)
	{
		if(thisOne == this)
			return followOrder;
		for(final Enumeration<Pair<MOB, Short>> f = followers(); f.hasMoreElements();)
		{
			final Pair<MOB, Short> F = f.nextElement();
			if(F.first == thisOne)
				return F.second.intValue();
		}
		return -1;
	}

	@Override
	public MOB fetchFollower(final String named)
	{
		if(followers == null)
			return null;
		final List<MOB> list = new ConvertingList<Pair<MOB, Short>, MOB>(followers, FollowerConverter);
		MOB mob = (MOB) CMLib.english().fetchEnvironmental(list, named, true);
		if(mob == null)
			mob = (MOB) CMLib.english().fetchEnvironmental(list, named, false);
		return mob;
	}

	@Override
	public MOB fetchFollower(final int index)
	{
		try
		{
			if(followers == null)
				return null;
			return followers.get(index).first;
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public boolean isFollowedBy(final MOB thisOne)
	{
		for(final Enumeration<Pair<MOB, Short>> f = followers(); f.hasMoreElements();)
		{
			final Pair<MOB, Short> F = f.nextElement();
			if(F.first == thisOne)
				return true;
		}
		return false;
	}

	@Override
	public boolean willFollowOrdersOf(final MOB mob)
	{
		if(mob != null)
		{
			if((amFollowing() == mob)
			|| ((isMonster() && CMSecurity.isAllowed(mob, location(), CMSecurity.SecFlag.ORDER)))
			|| (getLiegeID().equals(mob.Name()))
			|| (CMLib.law().doesOwnThisProperty(mob, CMLib.map().getStartRoom(this))))
				return true;
			if((!isMonster())
			&& (CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.ORDER))
			&& ((!CMSecurity.isASysOp(this)) || CMSecurity.isASysOp(mob)))
				return true;
			for(final Triad<Clan, Integer, Integer> t : CMLib.clans().findCommonRivalrousClans(this, mob))
			{
				final Clan C = t.first;
				final int myRole = t.second.intValue();
				final int hisRole = t.third.intValue();
				if((C.getAuthority(hisRole, Clan.Function.ORDER_UNDERLINGS) != Clan.Authority.CAN_NOT_DO)
				&& (C.doesOutRank(hisRole, myRole)))
					return true;
				else
				if((isMonster())
				&& (C.getAuthority(hisRole, Clan.Function.ORDER_CONQUERED) != Clan.Authority.CAN_NOT_DO)
				&& (getStartRoom() != null))
				{
					final LegalBehavior B = CMLib.law().getLegalBehavior(getStartRoom());
					if((B != null) && (mob.getClanRole(B.rulingOrganization()) != null))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public MOB getGroupLeader()
	{
		Followable<MOB> following = amFollowing;
		if(following == null)
			return this;
		if(following.amFollowing() == null)
			return (MOB)following;
		final HashSet<Followable<MOB>> seen = new HashSet<Followable<MOB>>();
		while((following != null)
		&& (following.amFollowing() != null)
		&& (!seen.contains(following)))
		{
			seen.add(following);
			following = following.amFollowing();
		}
		return (MOB) following;
	}

	@Override
	public MOB amFollowing()
	{
		final Followable<MOB> following = amFollowing;
		if(following != null)
		{
			if(!following.isFollowedBy(this))
				amFollowing = null;
		}
		return (MOB) amFollowing;
	}

	@Override
	public void setFollowing(final MOB mob)
	{
		if(mob == this)
			return;
		if((amFollowing != null) && (amFollowing != mob))
		{
			if(amFollowing.isFollowedBy(this))
				amFollowing.delFollower(this);
		}
		if(mob != null)
		{
			if(!mob.isFollowedBy(this))
				mob.addFollower(this, -1);
		}
		amFollowing = mob;
	}

	@Override
	public Set<MOB> getRideBuddies(final Set<MOB> list)
	{
		if(list == null)
			return list;
		if(!list.contains(this))
			list.add(this);
		if(riding() != null)
			riding().getRideBuddies(list);
		return list;
	}

	@Override
	public Set<MOB> getGroupMembers(final Set<MOB> list)
	{
		if(list == null)
			return list;
		if(!list.contains(this))
			list.add(this);
		final MOB following = amFollowing();
		if((following != null) && (!list.contains(following)))
			following.getGroupMembers(list);
		for(final Enumeration<Pair<MOB, Short>> f = followers(); f.hasMoreElements();)
		{
			final Pair<MOB, Short> F = f.nextElement();
			if((F.first != null) && (!list.contains(F.first)))
				F.first.getGroupMembers(list);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends Rider> getGroupMembersAndRideables(final Set<? extends Rider> list)
	{
		if(list == null)
			return list;
		getGroupMembers((Set<MOB>) list);
		final List<Rider> riders = new ArrayList<Rider>();
		riders.addAll(list);
		int startDex = 0;
		while(startDex < riders.size())
		{
			int i = startDex;
			startDex = riders.size();
			for(; i < riders.size(); i++)
			{
				final Rider P = riders.get(i);
				final Rideable Pr = P.riding();
				if((!riders.contains(Pr)) && (Pr != null))
					riders.add(Pr);
				if(P instanceof Rideable)
				{
					for(final Enumeration<Rider> r = ((Rideable) P).riders(); r.hasMoreElements();)
					{
						final Rider R2 = r.nextElement();
						if(!riders.contains(R2))
							riders.add(R2);
					}
				}
			}
		}
		return list;
	}

	@Override
	public boolean isSavable()
	{
		if((!isMonster()) && (soulMate() == null))
			return false;
		if(!CMLib.flags().isSavable(this))
			return false;
		if(CMLib.utensils().getMobPossessingAnother(this) != null)
			return false;
		final MOB followed = amFollowing();
		if(followed != null)
		{
			if(!followed.isMonster())
				return false;
		}
		return true;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
		CMLib.flags().setSavable(this, truefalse);
	}

	@Override
	public MOB soulMate()
	{
		return soulMate;
	}

	@Override
	public void setSoulMate(final MOB mob)
	{
		soulMate = mob;
	}

	@Override
	public void addAbility(final Ability to)
	{
		if(to == null)
			return;
		if(abilitys.find(to.ID()) != null)
			return;
		to.setInvoker(this);
		abilitys.addElement(to);
		triggerer.setObsolete();
	}

	@Override
	public void delAbility(final Ability to)
	{
		abilitys.removeElement(to);
		triggerer.setObsolete();
	}

	@Override
	public void delAllAbilities()
	{
		abilitys.clear();
		abilityUseCache.clear();
		triggerer.setObsolete();
	}

	@Override
	public int numAbilities()
	{
		return abilitys.size();
	}

	@Override
	public Enumeration<Ability> abilities()
	{
		return abilitys.elements();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<Ability> allAbilities()
	{
		final MultiListEnumeration<Ability> multi = new MultiListEnumeration<Ability>(new List[] { abilitys, charStats().getMyRace().racialAbilities(this) });
		for(final Pair<Clan, Integer> p : clans())
			multi.addEnumeration(p.first.clanAbilities(this));
		return multi;
	}

	@Override
	public int numAllAbilities()
	{
		int size = abilitys.size() + charStats().getMyRace().racialAbilities(this).size();
		for(final Pair<Clan, Integer> p : clans())
			size += p.first.clanAbilities(this).size();
		return size;
	}

	@Override
	public Ability fetchRandomAbility()
	{
		if(numAllAbilities() == 0)
			return null;
		return fetchAbility(CMLib.dice().roll(1, numAllAbilities(), -1));
	}

	@Override
	public boolean isRacialAbility(final String abilityID)
	{
		final List<Ability> racialAbilities = charStats().getMyRace().racialAbilities(this);
		if(racialAbilities.size() == 0)
			return false;
		for(int i = 0; i < racialAbilities.size(); i++)
		{
			final Ability A = racialAbilities.get(i);
			if((A != null) && (A.ID().equalsIgnoreCase(abilityID)))
				return true;
		}
		return false;
	}

	@Override
	public Ability fetchAbility(int index)
	{
		try
		{
			if(index < abilitys.size())
				return abilitys.elementAt(index);
			final List<Ability> racialAbilities = charStats().getMyRace().racialAbilities(this);
			if(index < abilitys.size() + racialAbilities.size())
				return racialAbilities.get(index - abilitys.size());
			index -= (abilitys.size() + racialAbilities.size());
			for(final Pair<Clan, Integer> p : clans())
			{
				final SearchIDList<Ability> list = p.first.clanAbilities(this);
				if(index < list.size())
					return list.get(index);
				index -= list.size();
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Ability fetchAbility(final String ID)
	{
		Ability A = abilitys.find(ID);
		if(A != null)
			return A;
		if(clans.size() > 0)
		{
			for(final Pair<Clan, Integer> p : clans())
			{
				A = p.first.clanAbilities(this).find(ID);
				if(A != null)
					return A;
			}
		}
		return charStats().getMyRace().racialAbilities(this).find(ID);
	}

	@Override
	public Ability findAbility(final String ID)
	{
		final Race R = charStats().getMyRace();
		Ability A = (Ability) CMLib.english().fetchEnvironmental(abilitys, ID, true);
		if(A == null)
			A = (Ability) CMLib.english().fetchEnvironmental(R.racialAbilities(this), ID, true);
		if(A == null)
		{
			for(final Pair<Clan, Integer> p : clans())
			{
				A = (Ability) CMLib.english().fetchEnvironmental(p.first.clanAbilities(this), ID, true);
				if(A != null)
					return A;
			}
		}
		if(A == null)
			A = (Ability) CMLib.english().fetchEnvironmental(abilitys, ID, false);
		if(A == null)
			A = (Ability) CMLib.english().fetchEnvironmental(R.racialAbilities(this), ID, false);
		if(A == null)
		{
			for(final Pair<Clan, Integer> p : clans())
			{
				A = (Ability) CMLib.english().fetchEnvironmental(p.first.clanAbilities(this), ID, false);
				if(A != null)
					return A;
			}
		}
		if(A == null)
			A = fetchAbility(ID);
		return A;
	}

	protected final List<Ability> racialEffects()
	{
		if(racialAffects == null)
			racialAffects = charStats.getMyRace().racialEffects(this);
		return racialAffects;
	}

	protected final List<Ability> clanEffects()
	{
		List<Ability> affects = clanAffects;
		if(affects == null)
		{
			final Iterator<Pair<Clan, Integer>> c = clans().iterator();
			if(!c.hasNext())
				affects = CMLib.clans().getDefaultGovernment().getClanLevelEffects(this, null, null);
			else
			{
				final ReadOnlyMultiList<Ability> effects = new ReadOnlyMultiList<Ability>();
				for(; c.hasNext();)
					effects.addList(c.next().first.clanEffects(this));
				affects = effects;
			}
			clanAffects = affects;
		}
		return affects;
	}

	@Override
	public Iterable<Pair<Clan, Integer>> clans()
	{
		return this.clans;
	}

	@Override
	public Pair<Clan, Integer> getClanRole(final String clanID)
	{
		if((clanID == null) || (clanID.length() == 0))
			return null;
		return clans.get(clanID);
	}

	@Override
	public void setClan(final String clanID, final int role)
	{
		if((clanID == null) || (clanID.length() == 0))
		{
			if(role == Integer.MIN_VALUE)
			{
				clans.clear();
				clanAffects = null;
			}
			return;
		}
		if(role < 0)
		{
			final Pair<Clan, Integer> p = clans.get(clanID);
			if(p != null)
			{
				clans.remove(clanID);
				clanAffects = null;
				if((isPlayer()) && CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
					Log.debugOut("User '" + Name() + "' had membership in '" + clanID + "' negated");
			}
		}
		else
		{
			Pair<Clan, Integer> p = clans.get(clanID);
			if(p == null)
			{
				final Clan C = CMLib.clans().getClanAnyHost(clanID);
				if(C == null)
					Log.errOut("StdMOB", "Unknown clan: " + clanID + " on " + Name() + " in " + CMLib.map().getDescriptiveExtendedRoomID(location()));
				else
				{
					p = new Pair<Clan, Integer>(C, Integer.valueOf(role));
					clans.put(clanID, p);
					clanAffects = null;
					if((isPlayer()) && CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
						Log.debugOut("User '" + Name() + "' had membership in '" + clanID + "' added with role " + role);
				}
			}
			else
			{
				if(p.second.intValue() != role)
				{
					if((isPlayer()) && CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
						Log.debugOut("User '" + Name() + "' had membership in '" + clanID + "' changed from " + p.second.toString() + " to role " + role);
					p.second = Integer.valueOf(role);
				}
				clans.put(clanID, p);
			}
		}
	}

	@Override
	public void addNonUninvokableEffect(final Ability to)
	{
		if(to == null)
			return;
		if(fetchEffect(to.ID()) != null)
			return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void addPriorityEffect(final Ability to)
	{
		if(to == null)
			return;
		if(fetchEffect(to.ID()) != null)
			return;
		if(affects.isEmpty())
			affects.addElement(to);
		else
			affects.insertElementAt(to, 0);
		to.setAffectedOne(this);
	}

	@Override
	public void addEffect(final Ability to)
	{
		if(to == null)
			return;
		if(fetchEffect(to.ID()) != null)
			return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void delEffect(final Ability to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects = this.affects;
		if(affects != null)
		{
			for(int a = 0; a < affects.size(); a++)
			{
				final Ability A;
				try { A=affects.get(a);}catch(final IndexOutOfBoundsException e){ break;  /** this happens **/ }
				try
				{
					applier.apply(A);
				}
				catch(final Exception e)
				{
					Log.errOut(e);
				}
			}
		}
		final List<Ability> racialEffects = racialEffects();
		try
		{
			if(!racialEffects.isEmpty())
			{
				for(final Ability A : racialEffects)
				{
					try
					{
						applier.apply(A);
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		try
		{
			if(!clans.isEmpty())
			{
				for(final Ability A : clanEffects())
				{
					try
					{
						applier.apply(A);
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
			}
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
	}

	@Override
	public void delAllEffects(final boolean unInvoke)
	{
		for(int a = numEffects() - 1; a >= 0; a--)
		{
			final Ability A = fetchEffect(a);
			if(A != null)
			{
				if(unInvoke)
					A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects.clear();
	}

	@Override
	public int numAllEffects()
	{
		int size = affects.size() + charStats().getMyRace().numRacialEffects(this);
		for(final Pair<Clan, Integer> p : clans())
			size += p.first.numClanEffects(this);
		return size;
	}

	@Override
	public int numEffects()
	{
		return affects.size();
	}

	@Override
	public Ability fetchEffect(final int index)
	{
		try
		{
			if(index < affects.size())
				return affects.elementAt(index);
			if(index < affects.size() + charStats().getMyRace().numRacialEffects(this))
				return racialEffects().get(index - affects.size());
			return clanEffects().get(index - affects.size() - racialEffects().size());
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Ability fetchEffect(final String ID)
	{
		for(final Enumeration<Ability> a = effects(); a.hasMoreElements();)
		{
			final Ability A = a.nextElement();
			if(A.ID().equals(ID))
				return A;
		}
		return null;
	}

	@Override
	public Enumeration<Ability> personalEffects()
	{
		return affects.elements();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<Ability> effects()
	{
		return new MultiListEnumeration<Ability>(new List[] { affects, racialEffects(), clanEffects() });
	}

	/**
	 * Manipulation of Behavior objects, which includes movement, speech,
	 * spellcasting, etc, etc.
	 */
	@Override
	public void addBehavior(final Behavior to)
	{
		if(to == null)
			return;
		if(fetchBehavior(to.ID()) != null)
			return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}

	@Override
	public void delBehavior(final Behavior to)
	{
		if(behaviors.removeElement(to))
			to.endBehavior(this);
	}

	@Override
	public void delAllBehaviors()
	{
		behaviors.clear();
	}

	@Override
	public int numBehaviors()
	{
		return behaviors.size();
	}

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return behaviors.elements();
	}

	@Override
	public Behavior fetchBehavior(final int index)
	{
		try
		{
			return behaviors.elementAt(index);
		}
		catch (final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Behavior fetchBehavior(final String ID)
	{
		return behaviors.find(ID);
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors = this.behaviors;
		if(behaviors != null)
		{
			for(int a = 0; a < behaviors.size(); a++)
			{
				final Behavior B;
				try{ B = behaviors.get(a);}catch(final IndexOutOfBoundsException e){ break;  /** this happens **/ }
				try
				{
					applier.apply(B);
				}
				catch(final Exception e)
				{
					Log.errOut(e);
				}
			}
		}
	}

	@Override
	public int[][] getAbilityUsageCache(final String abilityID)
	{
		int[][] ableCache;
		synchronized(abilityUseCache)
		{
			ableCache = abilityUseCache.get(abilityID);
		}
		if(ableCache == null)
		{
			ableCache = new int[Ability.CACHEINDEX_TOTAL][];
			abilityUseCache.put(abilityID, ableCache);
		}
		final CharStats charStats = charStats();
		final CharClass charClass = charStats.getCurrentClass();
		final int[] ableUseTrig;
		synchronized(abilityUseTrig)
		{
			ableUseTrig = this.abilityUseTrig;
		}
		if((phyStats().level() != ableUseTrig[0])
		|| (charStats.getCurrentClassLevel() != ableUseTrig[1])
		|| (charClass.hashCode() != ableUseTrig[2]))
		{
			clearAbilityUsageCache();
			ableUseTrig[0] = phyStats().level();
			ableUseTrig[1] = charStats.getCurrentClassLevel();
			ableUseTrig[2] = charClass.hashCode();
		}
		return ableCache;
	}

	private void clearAbilityUsageCache()
	{
		Arrays.fill(abilityUseTrig, 0);
		abilityUseCache.clear();
	}

	@Override
	public void addExpertise(final String code)
	{
		final Entry<String, Integer> p = CMath.getStringFollowedByNumber(code, true);
		final String key = p.getKey().toUpperCase();
		final Integer oldNum = expertises.get(key);
		if((oldNum == null) || ((p.getValue() != null) && (oldNum.intValue() < p.getValue().intValue())))
		{
			expertises.put(key, p.getValue());
			clearAbilityUsageCache();
		}
	}

	@Override
	public void delExpertise(final String baseCode)
	{
		if(baseCode == null)
		{
			clearAbilityUsageCache();
			return;
		}
		if(expertises.remove(baseCode.toUpperCase()) == null)
		{
			final Entry<String, Integer> p = CMath.getStringFollowedByNumber(baseCode, true);
			if(expertises.remove(p.getKey().toUpperCase()) != null)
				clearAbilityUsageCache();
		}
		else
			clearAbilityUsageCache();
	}

	@Override
	public Pair<String, Integer> fetchExpertise(final String baseCode)
	{
		if(baseCode == null)
			return null;
		final Entry<String, Integer> p = CMath.getStringFollowedByNumber(baseCode, false);
		final String key = p.getKey().toUpperCase();
		final Integer num = expertises.get(key);
		if((expertises.containsKey(key))
		&& ((num == null) || (p.getValue() == null) || (p.getValue().intValue() <= num.intValue())))
		{
			final Integer i = (p.getValue() == null) ? num : p.getValue();
			return new Pair<String, Integer>(key, i);
		}
		return null;
	}

	@Override
	public void delAllExpertises()
	{
		if(!expertises.isEmpty())
		{
			expertises.clear();
			clearAbilityUsageCache();
		}
	}

	@Override
	public Enumeration<String> expertises()
	{
		return new Enumeration<String>()
		{
			final Iterator<Entry<String, Integer>>	i	= expertises.entrySet().iterator();

			@Override
			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			@Override
			public String nextElement()
			{
				final Entry<String, Integer> s = i.next();
				if(s.getValue() == null)
					return s.getKey();
				return s.getKey() + s.getValue().toString();
			}
		};
	}

	/** Manipulation of the scripts list */
	@Override
	public void addScript(final ScriptingEngine S)
	{
		if(S == null)
			return;
		if(!scripts.contains(S))
		{
			for(final ScriptingEngine S2 : scripts)
			{
				if(S2.getScript().equalsIgnoreCase(S.getScript()))
					return;
			}
			scripts.addElement(S);
		}
	}

	@Override
	public void delScript(final ScriptingEngine S)
	{
		if(S == null)
			return;
		scripts.removeElement(S);
	}

	@Override
	public void delAllScripts()
	{
		scripts.clear();
	}

	@Override
	public int numScripts()
	{
		return (scripts == null) ? 0 : scripts.size();
	}

	private static final Enumeration<ScriptingEngine>	emptyScripts	= new EmptyEnumeration<ScriptingEngine>();

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return (scripts == null) ? emptyScripts : scripts.elements();
	}

	@Override
	public ScriptingEngine fetchScript(final int x)
	{
		try
		{
			return scripts.elementAt(x);
		}
		catch (final Exception e)
		{
		}
		return null;
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts = this.scripts;
		if(scripts != null)
		{
			try
			{
				for(int a = 0; a < scripts.size(); a++)
				{
					final ScriptingEngine S = scripts.get(a);
					if(S != null)
						applier.apply(S);
				}
			}
			catch (final IndexOutOfBoundsException x)
			{
			}
		}
	}

	/** Manipulation of the tatoo list */
	@Override
	public void addTattoo(final String of)
	{
		final Tattoo T = (Tattoo) CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of));
	}

	@Override
	public void addTattoo(final String of, final int tickDown)
	{
		final Tattoo T = (Tattoo) CMClass.getCommon("DefaultTattoo");
		addTattoo(T.set(of, tickDown));
	}

	@Override
	public boolean delTattoo(final String of)
	{
		final Tattoo T = findTattoo(of);
		if(T != null)
			 return tattoos.remove(T);
		return false;
	}

	@Override
	public void addTattoo(final Tattoo of)
	{
		if((of == null)
		|| (of.getTattooName() == null)
		|| (of.getTattooName().length() == 0)
		|| findTattoo(of.getTattooName()) != null)
			return;
		tattoos.addElement(of);
	}

	@Override
	public void delTattoo(final Tattoo of)
	{
		if((of == null)
		|| (of.getTattooName() == null)
		|| (of.getTattooName().length() == 0))
			return;
		final Tattoo tat = findTattoo(of.getTattooName());
		if(tat == null)
			return;
		tattoos.remove(tat);
	}

	@Override
	public Enumeration<Tattoo> tattoos()
	{
		return tattoos.elements();
	}

	@Override
	public Tattoo findTattoo(final String of)
	{
		if((of == null) || (of.length() == 0))
			return null;
		if(of.endsWith("*"))
			return tattoos.findStartsWith(of.substring(0, of.length() - 1));
		return tattoos.find(of.trim());
	}

	@Override
	public Tattoo findTattooStartsWith(final String of)
	{
		if((of == null) || (of.length() == 0))
			return null;
		return tattoos.findStartsWith(of.trim());
	}

	/** Manipulation of the factions list */
	@Override
	public void addFaction(String which, int start)
	{
		which = which.toUpperCase();
		final Faction F = CMLib.factions().getFaction(which);
		if(F == null)
			return;
		if(start > F.maximum())
			start = F.maximum();
		if(start < F.minimum())
			start = F.minimum();
		which = F.factionID().toUpperCase();
		Faction.FData data = factions.get(which);
		if(data == null)
		{
			data = F.makeFactionData(this);
			factions.put(which, data);
		}
		data.setValue(start);
	}

	@Override
	public void adjustFaction(String which, final int amount)
	{
		which = which.toUpperCase();
		final Faction F = CMLib.factions().getFaction(which);
		if(F == null)
			return;
		which = F.factionID().toUpperCase();
		if(!factions.containsKey(which))
			addFaction(which, amount);
		else
			addFaction(which, fetchFaction(which) + amount);
	}

	@Override
	public Enumeration<String> factions()
	{
		return factions.keys();
	}

	@Override
	public int fetchFaction(final String which)
	{
		final Faction.FData data = factions.get(which.toUpperCase());
		if(data == null)
			return Integer.MAX_VALUE;
		return data.value();
	}

	@Override
	public Faction.FData fetchFactionData(final String which)
	{
		return factions.get(which.toUpperCase());
	}

	@Override
	public void removeFaction(final String which)
	{
		factions.remove(which.toUpperCase());
	}

	@Override
	public void copyFactions(final FactionMember source)
	{
		for(final Enumeration<String> e = source.factions(); e.hasMoreElements();)
		{
			final String fID = e.nextElement();
			addFaction(fID, source.fetchFaction(fID));
		}
	}

	@Override
	public boolean hasFaction(final String which)
	{
		final Faction F = CMLib.factions().getFaction(which);
		if(F == null)
			return false;
		return factions.containsKey(F.factionID().toUpperCase());
	}

	@Override
	public List<String> fetchFactionRanges()
	{
		final Vector<String> V = new Vector<String>(factions.size());
		for(final Enumeration<String> e = factions(); e.hasMoreElements();)
		{
			final Faction F = CMLib.factions().getFaction(e.nextElement());
			if(F == null)
				continue;
			final Faction.FRange FR = CMLib.factions().getRange(F.factionID(), fetchFaction(F.factionID()));
			if(FR != null)
				V.addElement(FR.codeName());
		}
		return V;
	}

	@Override
	public int freeWearPositions(final long wornCode, final short belowLayer, final short layerAttributes)
	{
		int x = getWearPositions(wornCode);
		if(x <= 0)
			return 0;
		final int maxItemsEver = CMProps.getIntVar(CMProps.Int.MAXITEMSWORN);
		final List<Item> allItems = fetchWornItems(Long.MIN_VALUE, belowLayer, layerAttributes);
		if((maxItemsEver > 0) && (allItems.size() >= maxItemsEver))
			return 0;
		x -= counItemsWornAt(allItems, wornCode);
		if(x <= 0)
			return 0;
		return x;
	}

	@Override
	public int getWearPositions(final long wornCode)
	{
		if((charStats().getWearableRestrictionsBitmap() & wornCode) > 0)
			return 0;
		final int maxPerSlot = CMProps.getIntVar(CMProps.Int.MAXWEARPERLOC);
		if(wornCode == Wearable.WORN_FLOATING_NEARBY)
			return (maxPerSlot == 0) ? 6 : maxPerSlot;
		int total;
		int add = 0;
		boolean found = false;
		for(int i = 0; i < Race.BODY_WEARGRID.length; i++)
		{
			if((Race.BODY_WEARGRID[i][0] > 0) && ((Race.BODY_WEARGRID[i][0] & wornCode) == wornCode))
			{
				found = true;
				total = charStats().getBodyPart(i);
				if(Race.BODY_WEARGRID[i][1] < 0)
				{
					if(total > 0)
						return 0;
				}
				else
				if(total < 1)
					return 0;
				else
				if(i == Race.BODY_HAND)
				{
					// casting is ok here since these are all originals that fall below the int/long fall.
					if(wornCode > Integer.MAX_VALUE)
						add += total;
					else
					{
						switch((int) wornCode)
						{
						case (int) Wearable.WORN_HANDS:
							if(total < 2)
								add += 1;
							else
								add += total / 2;
							break;
						case (int) Wearable.WORN_WIELD:
						case (int) Wearable.WORN_RIGHT_FINGER:
						case (int) Wearable.WORN_RIGHT_WRIST:
							add += 1;
							break;
						case (int) Wearable.WORN_HELD:
						case (int) Wearable.WORN_LEFT_FINGER:
						case (int) Wearable.WORN_LEFT_WRIST:
							add += total - 1;
							break;
						default:
							add += total;
							break;
						}
					}
				}
				else
				{
					final int num = total / ((int) Race.BODY_WEARGRID[i][1]);
					if(num < 1)
						add += 1;
					else
						add += num;
				}
			}
		}
		if(!found)
			return 1;
		if((add > maxPerSlot) && (maxPerSlot > 0))
			return maxPerSlot;
		return add;
	}

	protected int counItemsWornAt(final List<Item> items, final long wornCode)
	{
		int ct = 0;
		for(final Item thisItem : items)
		{
			if(thisItem.amWearingAt(wornCode))
				ct++;
		}
		return ct;
	}

	@Override
	public List<Item> fetchWornItems(final long wornCode, final short aboveOrAroundLayer, final short layerAttributes)
	{
		final Vector<Item> V = new Vector<Item>(); // return value
		final boolean equalOk = (layerAttributes & Armor.LAYERMASK_MULTIWEAR) > 0;
		final boolean allWorn = wornCode == Long.MIN_VALUE;
		int lay = 0;
		for(final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item thisItem = i.nextElement();
			if((allWorn && (!thisItem.amWearingAt(Item.IN_INVENTORY)))
			|| thisItem.amWearingAt(wornCode))
			{
				if(thisItem instanceof Armor)
				{
					lay = ((Armor) thisItem).getClothingLayer();
					if(lay >= (aboveOrAroundLayer - 1))
					{
						if(((lay > aboveOrAroundLayer - 2)
							&& (lay < aboveOrAroundLayer + 2)
							&& ((!equalOk) || ((((Armor) thisItem).getLayerAttributes() & Armor.LAYERMASK_MULTIWEAR) == 0)))
						|| (lay > aboveOrAroundLayer))
							V.addElement(thisItem);
					}
				}
				else
					V.addElement(thisItem);
			}
		}
		return V;
	}

	@Override
	public boolean hasOnlyGoldInInventory()
	{
		for(int i = 0; i < numItems(); i++)
		{
			final Item I = getItem(i);
			if(I.amWearingAt(Wearable.IN_INVENTORY)
			&& ((I.container() == null)
				|| (I.ultimateContainer(null).amWearingAt(Wearable.IN_INVENTORY)))
			&& (!(I instanceof Coins)))
				return false;
		}
		return true;
	}

	@Override
	public Item fetchFirstWornItem(final long wornCode)
	{
		for(final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item thisItem = i.nextElement();
			if(thisItem.amWearingAt(wornCode))
				return thisItem;
		}
		return null;
	}

	@Override
	public Item fetchWieldedItem()
	{
		final WeakReference<Item> wieldRef = possWieldedItem;
		if(wieldRef != null)
		{
			final Item I = wieldRef.get();
			if(I == null)
				return null;
			if((I.owner() == this)
			&& (I.amWearingAt(Wearable.WORN_WIELD))
			&& (!I.amDestroyed())
			&& (I.container() == null))
				return I;
			possWieldedItem = null;
		}
		for(final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if((I != null)
			&& (I.owner() == this)
			&& (I.amWearingAt(Wearable.WORN_WIELD))
			&& (I.container() == null))
			{
				possWieldedItem = new WeakReference<Item>(I);
				return I;
			}
		}
		possWieldedItem = new WeakReference<Item>(null);
		return null;
	}

	@Override
	public Item fetchHeldItem()
	{
		final WeakReference<Item> heldRef = possHeldItem;
		if(heldRef != null)
		{
			final Item I = heldRef.get();
			if(I == null)
				return null;
			if((I.owner() == this)
			&& (I.amWearingAt(Wearable.WORN_HELD))
			&& (!I.amDestroyed())
			&& (I.container() == null))
				return I;
			possHeldItem = null;
		}
		for(final Enumeration<Item> i = items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if((I != null)
			&& (I.owner() == this)
			&& (I.amWearingAt(Wearable.WORN_HELD))
			&& (I.container() == null))
			{
				possHeldItem = new WeakReference<Item>(I);
				return I;
			}
		}
		possHeldItem = new WeakReference<Item>(null);
		return null;
	}

	@Override
	public boolean isMine(final Environmental env)
	{
		if(env instanceof Item)
		{
			if(inventory.contains(env))
				return true;
			return false;
		}
		else
		if(env instanceof MOB)
		{
			if(isFollowedBy((MOB) env))
				return true;
			return false;
		}
		else
		if(env instanceof Ability)
		{
			if((abilitys.find(env.ID()) == env)
			||(affects.contains(env)))
				return true;
			return false;
		}
		return false;
	}

	@Override
	public void moveItemTo(final Item container, final ItemPossessor.Expire expire, final Move... moveFlags)
	{
		moveItemTo(container);
	}

	@Override
	public void moveItemTo(final Item container)
	{
		// caller is responsible for recovering any env
		// stat changes!
		if(CMLib.flags().isHidden(container))
			container.basePhyStats().setDisposition(container.basePhyStats().disposition() & ((int) PhyStats.ALLMASK - PhyStats.IS_HIDDEN));

		// ensure its out of its previous place
		Environmental owner = location();
		if(container.owner() != null)
		{
			owner = container.owner();
			if(owner instanceof Room)
				((Room) owner).delItem(container);
			else
			if(owner instanceof MOB)
				((MOB) owner).delItem(container);
		}
		location().delItem(container);

		container.unWear();

		if(!isMine(container))
			addItem(container);
		container.recoverPhyStats();

		boolean nothingDone = true;
		boolean doBugFix = true;
		while(doBugFix || !nothingDone)
		{
			doBugFix = false;
			nothingDone = true;
			if(owner instanceof Room)
			{
				final Room R = (Room) owner;
				for(final Enumeration<Item> i = R.items(); i.hasMoreElements();)
				{
					final Item thisItem = i.nextElement();
					if(thisItem.container() == container)
					{
						moveItemTo(thisItem);
						nothingDone = false;
						break;
					}
				}
			}
			else
			if(owner instanceof MOB)
			{
				final MOB M = (MOB) owner;
				for(final Enumeration<Item> i = M.items(); i.hasMoreElements();)
				{
					final Item thisItem = i.nextElement();
					if(thisItem.container() == container)
					{
						moveItemTo(thisItem);
						nothingDone = false;
						break;
					}
				}
			}
		}
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	protected static String[]	CODES	= { "CLASS", "LEVEL", "ABILITY", "TEXT" };

	@Override
	public String getStat(final String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + basePhyStats().level();
		case 2:
			return "" + basePhyStats().ability();
		case 3:
			return text();
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break;
		case 2:
			basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setMiscText(val);
			break;
		}
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		for(int i = 0; i < CODES.length; i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdMOB))
			return false;
		final String[] codes = getStatCodes();
		for(int i = 0; i < codes.length; i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
