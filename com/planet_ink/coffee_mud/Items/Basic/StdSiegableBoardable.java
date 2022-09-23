package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdNavigableBoardable.NavigatingCommand;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;

import java.util.*;

/*
   Copyright 2021-2022 Bo Zimmerman

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
public class StdSiegableBoardable extends StdBoardable implements SiegableItem
{
	protected PairList<Weapon, int[]>	aimings			= new PairVector<Weapon, int[]>();
	protected PairList<Item, int[]>		coordinates		= null;
	protected volatile SiegableItem		siegeTarget		= null;
	protected volatile Room				siegeCombatRoom	= null;
	protected volatile int				lastSpamCt		= 0;
	protected volatile String			lastSpamMsg		= "";
	protected Set<String>				disableCmds		= new HashSet<String>();

	@Override
	public String ID()
	{
		return "StdSiegableBoardable";
	}

	private final static Map<String, SiegeCommand> commandWords = new Hashtable<String, SiegeCommand>();

	protected SiegeCommand findSiegeCommand(final String word, final String secondWord)
	{
		if(word == null)
			return null;
		SiegeCommand cmd=null;
		if(commandWords.size()==0)
		{
			for(final SiegeCommand N : SiegeCommand.values())
				commandWords.put(N.name().toUpperCase().trim(), N);
		}

		if((secondWord!=null)&&(secondWord.length()>0))
			cmd = commandWords.get((word+"_"+secondWord).toUpperCase().trim());
		if(cmd == null)
			cmd = commandWords.get(word.toUpperCase().trim());
		return cmd;
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return true;
	}

	@Override
	public void setRangeToTarget(final int newRange)
	{
		//nothing to do atm
	}

	@Override
	public PairList<Item, int[]> getCombatField()
	{
		return this.coordinates;
	}

	@Override
	public int rangeToTarget()
	{
		return getTacticalDistance(siegeTarget);
	}

	@Override
	public boolean mayPhysicallyAttack(final PhysicalAgent victim)
	{
		if(!mayIFight(victim))
			return false;
		return CMLib.map().roomLocation(this) == CMLib.map().roomLocation(victim);
	}

	@Override
	public boolean isInCombat()
	{
		final Physical siegeTarget=this.siegeTarget;
		if((siegeTarget != null)&& (siegeCombatRoom != null))
		{
			if(siegeTarget.amDestroyed())
			{
				this.clearTacticalModeInternal();
				return false;
			}
			return true;

		}
		return false;
	}

	@Override
	public boolean mayIFight(final PhysicalAgent victim)
	{
		final Area myArea=this.getArea();
		final PhysicalAgent defender=victim;
		MOB mob = null;
		if(myArea != null)
		{
			final LegalLibrary law=CMLib.law();
			int legalLevel=0;
			for(final Enumeration<Room> r=myArea.getProperMap();r.hasMoreElements() && (legalLevel<2);)
			{
				final Room R=r.nextElement();
				if((R!=null)&&(R.numInhabitants()>0))
				{
					for(final Enumeration<MOB> i=R.inhabitants();i.hasMoreElements();)
					{
						final MOB M=i.nextElement();
						if(M != null)
						{
							if(mob == null)
								mob = M;
							if((legalLevel==0)&&(mob.isMonster())&&(law.doesHaveWeakPriviledgesHere(M, R)))
							{
								mob=M;
								legalLevel=1;
							}
							if(M.isPlayer())
							{
								if(!mob.isPlayer())
								{
									mob=M;
									legalLevel=0;
								}
								if((legalLevel<2) && (law.doesHavePriviledgesHere(M, R)))
								{
									legalLevel=2;
									mob=M;
								}
								if((legalLevel<1) && (law.doesHaveWeakPriviledgesHere(M, R)))
								{
									legalLevel=1;
									mob=M;
								}
							}
						}
					}
				}
			}
		}
		if(mob==null)
			return false;
		return CMLib.combat().mayIAttackThisVessel(mob, defender);
	}

	protected void fixArea(final Area area)
	{
		final Ability oldA=area.fetchEffect("SiegableListener");
		if(oldA!=null)
			area.delEffect(oldA);
		final ExtendableAbility extAble = (ExtendableAbility)CMClass.getAbility("ExtAbility");
		extAble.setAbilityID("SiegableListener");
		extAble.setName("Siegable Listener");
		extAble.setSavable(false);
		final StdSiegableBoardable thisMe = this;
		extAble.setMsgListener(new MsgListener()
		{
			final StdSiegableBoardable me=thisMe;
			final Area meA=area;

			protected void lookOverBow(final Room R, final CMMsg msg)
			{
				msg.addTrailerRunnable(new Runnable()
				{
					@Override
					public void run()
					{
						if(CMLib.flags().canBeSeenBy(R, msg.source()) && (msg.source().session()!=null))
							msg.source().session().print(L(me.head_offTheDeck));
						final CMMsg msg2=CMClass.getMsg(msg.source(), R, msg.tool(), msg.sourceCode(), null, msg.targetCode(), null, msg.othersCode(), null);
						if((msg.source().isAttributeSet(MOB.Attrib.AUTOEXITS))
						&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
							msg2.addTrailerMsg(CMClass.getMsg(msg.source(),R,null,CMMsg.MSG_LOOK_EXITS,null));
						if(R.okMessage(msg.source(), msg))
							R.send(msg.source(),msg2);
					}
				});
			}

			@Override
			public void executeMsg(final Environmental myHost, final CMMsg msg)
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_LOOK:
				case CMMsg.TYP_EXAMINE:
					if((msg.target() instanceof Exit)&&(((Exit)msg.target()).isOpen()))
					{
						final Room hereR=msg.source().location();
						if((hereR!=null)
						&&(me.canViewOuterRoom(hereR))
						&&(hereR.getArea()==meA))
						{
							final Room lookingR=hereR.getRoomInDir(CMLib.map().getExitDir(hereR, (Exit)msg.target()));
							final Room R=CMLib.map().roomLocation(me);
							if(lookingR==R)
								lookOverBow(R,msg);
						}
					}
					else
					if((msg.target() instanceof Room)
					&&(me.canViewOuterRoom((Room)msg.target()))
					&&(((Room)msg.target()).getArea()==meA))
					{
						if(msg.targetMinor()==CMMsg.TYP_EXAMINE)
						{
							final Room R=CMLib.map().roomLocation(me);
							if((R!=null)
							&&(R.getArea()!=meA))
								lookOverBow(R,msg);
						}
					}
					break;
				}
			}

			@Override
			public boolean okMessage(final Environmental myHost, final CMMsg msg)
			{
				return true;
			}

		});
		area.addNonUninvokableEffect(extAble);
		extAble.setSavable(false);
	}

	@Override
	public Area getArea()
	{
		if((!destroyed)
		&&(area==null))
		{
			final Area area=super.getArea();
			if(area != null)
				area.setTheme(Area.THEME_FANTASY);
			fixArea(area);
			return area;
		}
		return super.getArea();
	}


	@Override
	public void setArea(final String xml)
	{
		super.setArea(xml);
		if(this.area!=null)
			fixArea(this.area);
	}

	@Override
	public void makePeace(final boolean includePlayerFollowers)
	{
		clearTacticalModeInternal();
	}

	@Override
	public PhysicalAgent getCombatant()
	{
		return this.siegeTarget;
	}

	@Override
	public void setCombatant(final PhysicalAgent other)
	{
		final Room R=(owner() instanceof Room)?(Room)owner():CMLib.map().roomLocation(this);
		if(other == null)
			clearTacticalModeInternal();
		else
		{
			if(other instanceof SiegableItem)
				siegeTarget = (SiegableItem)other;
			if(R != null)
				siegeCombatRoom = R;
			if(other instanceof Combatant)
			{
				if(((Combatant)other).getCombatant()==null)
					((Combatant)other).setCombatant(this);
			}
			amInTacticalMode(); // now he is in combat
		}
	}

	@Override
	public int[] getTacticalCoords()
	{
		final PairList<Item, int[]> coords = this.coordinates;
		if(coords != null)
		{
			for(final Iterator<Pair<Item,int[]>> i = coords.iterator(); i.hasNext();)
			{
				final Pair<Item,int[]> P=i.next();
				if(P.first == this)
					return P.second;
			}
		}
		return null;
	}

	@Override
	public int getDirectionToTarget()
	{
		return this.getDirectionToTarget(this.siegeTarget);
	}

	@Override
	public PairList<Weapon,int[]> getSiegeWeaponAimings()
	{
		return this.aimings;
	}

	protected int getTacticalDistance(final SiegableItem targetI)
	{
		if(targetI==null)
			return CMLib.map().roomLocation(this).maxRange() + 1;
		final int[] fromCoords = this.getTacticalCoords();
		final PairList<Item,int[]> coords = this.getCombatField(); // might not yet be set.
		int lowest = Integer.MAX_VALUE;
		if((coords != null) && (fromCoords != null))
		{
			final int p = coords.indexOfFirst(targetI);
			if(p >=0)
			{
				final Pair<Item,int[]> P=coords.get(p);
				final int distance = (int)Math.round(Math.ceil(Math.sqrt(Math.pow(P.second[0]-fromCoords[0],2.0) + Math.pow(P.second[1]-fromCoords[1],2.0))));
				if(distance < lowest)
					lowest=distance;
			}
		}
		if(lowest == Integer.MAX_VALUE)
			return CMLib.map().roomLocation(this).maxRange() + 1;
		return lowest;
	}

	@Override
	public String getTacticalView(final SiegableItem viewer)
	{
		final int[] targetCoords = getTacticalCoords();
		final int[] myCoords;
		final String dist = ""+getTacticalDistance(viewer);
		if(viewer instanceof SiegableItem)
		{
			myCoords = viewer.getTacticalCoords();
			if((myCoords!=null)&&(targetCoords != null))
			{
				final String dirFromYou = CMLib.directions().getDirectionName(Directions.getRelative11Directions(myCoords, targetCoords));
				return L("@x1 is @x2 of you at a distance of @x3.",name(),dirFromYou,dist);
			}
			else
				return L("@x1 is at a distance of @x2.",name(),dist);
		}
		else
			return L("@x1 is at a distance of @x2.",name(),dist);
	}

	protected int getLowestTacticalDistanceFromThis()
	{
		final int[] fromCoords = this.getTacticalCoords();
		final PairList<Item,int[]> coords = this.coordinates;
		int lowest = Integer.MAX_VALUE;
		if((coords != null) && (fromCoords != null))
		{
			for(int p=0;p<coords.size();p++)
			{
				try
				{
					final Pair<Item,int[]> P = coords.get(p);
					if((P.second != fromCoords)
					&&(this.siegeCombatRoom != null)
					&&(this.siegeCombatRoom.isHere(P.first))
					&&(P.first instanceof SiegableItem)
					&&(((SiegableItem)P.first).getCombatant() == this))
					{
						final int distance = (int)Math.round(Math.ceil(Math.sqrt(Math.pow(P.second[0]-fromCoords[0],2.0) + Math.pow(P.second[1]-fromCoords[1],2.0))));
						if(distance < lowest)
							lowest=distance;
					}
				}
				catch(final Exception e)
				{
				}
			}
		}
		if(lowest == Integer.MAX_VALUE)
			return CMLib.map().roomLocation(this).maxRange();
		return lowest;
	}

	protected int[] getMagicCoords()
	{
		final Room R=CMLib.map().roomLocation(this);
		final int[] coords;
		//final int middle = (int)Math.round(Math.floor(R.maxRange() / 2.0));
		final int extreme = R.maxRange()-1;
		final int extremeRandom = (extreme > 0) ? CMLib.dice().roll(1, R.maxRange(), -1) : 0;
		final int extremeRandom2 = (extreme > 0) ? CMLib.dice().roll(1, R.maxRange(), -1) : 0;
		coords = new int[] {extremeRandom, extremeRandom2};
		return coords;
	}

	protected synchronized boolean amInTacticalMode()
	{
		final Item siegeTarget = this.siegeTarget;
		final Room siegeCombatRoom = this.siegeCombatRoom;
		if((siegeTarget != null)
		&& (!siegeTarget.amDestroyed())
		&& (siegeCombatRoom != null)
		&& (siegeCombatRoom.isContent(siegeTarget))
		&& (siegeCombatRoom.isContent(this))
		)
		{
			PairList<Item,int[]> coords = this.coordinates;
			if(coords == null)
			{
				synchronized(CMClass.getSync((""+siegeCombatRoom + "_SIEGE_TACTICAL")))
				{
					coords = this.coordinates;
					if(coords == null)
					{
						for(int i=0;i<siegeCombatRoom.numItems();i++)
						{
							final Item I=siegeCombatRoom.getItem(i);
							if((I instanceof SiegableItem)
							&&(((SiegableItem)I).getCombatField() != null))
							{
								this.coordinates = ((SiegableItem)I).getCombatField();
							}
						}
						if(coordinates == null)
						{
							this.coordinates = new SPairList<Item,int[]>();
						}
						coords = this.coordinates;
					}
				}
				if(coords != null)
				{
					if(!coords.containsFirst(this))
					{
						int[] newCoords = null;
						for(int i=0;i<10;i++)
						{
							newCoords = this.getMagicCoords();
							if(!isAnyoneAtCoords(newCoords))
								break;
						}
						coords.add(new Pair<Item,int[]>(this,newCoords));
					}
				}
			}
			return true;
		}
		else
		{
			this.siegeTarget = null;
			this.siegeCombatRoom = null;
			this.coordinates = null;
			return false;
		}
	}

	protected void clearTacticalMode()
	{
		synchronized(CMClass.getSync((""+siegeCombatRoom + "_SIEGE_TACTICAL")))
		{
			final PairList<Item,int[]> coords = this.coordinates;
			if(coords != null)
			{
				coords.removeFirst(this);
			}
		}
		this.siegeTarget = null;
		this.siegeCombatRoom = null;
		this.coordinates = null;
		this.aimings.clear();
	}

	protected synchronized void clearTacticalModeInternal()
	{
		final Room siegeCombatRoom = this.siegeCombatRoom;
		if(siegeCombatRoom != null)
		{
			PairList<Item,int[]> coords = null;
			synchronized(CMClass.getSync((""+siegeCombatRoom + "_SIEGE_TACTICAL")))
			{
				 coords = this.coordinates;
			}
			clearTacticalMode();
			if(coords != null)
			{
				for(final Iterator<Item> s = coords.firstIterator();s.hasNext();)
				{
					final Item I=s.next();
					if((I instanceof SiegableItem)
					&&(((SiegableItem)I).getCombatant() == this))
						((SiegableItem)I).setCombatant(null);
				}
			}
		}
	}

	protected boolean isAnyoneAtCoords(final int[] xy)
	{
		final PairList<Item, int[]> coords = this.coordinates;
		if(coords != null)
		{
			for(final Iterator<int[]> i = coords.secondIterator(); i.hasNext();)
			{
				if(Arrays.equals(xy, i.next()))
					return true;
			}
		}
		return false;
	}

	protected int getDirectionToTarget(final SiegableItem dirTarget)
	{
		if((dirTarget != null)&&(dirTarget instanceof SiegableItem))
		{
			final SiegableItem siegeTarget = this.siegeTarget;
			final int[] targetCoords = siegeTarget.getTacticalCoords();
			final int[] myCoords = this.getTacticalCoords();
			if((myCoords!=null)&&(targetCoords != null))
				return Directions.getRelative11Directions(myCoords, targetCoords);
		}
		return -1;
	}

	protected String getDirectionStrToTarget(final SiegableItem siegeTarget)
	{
		if(siegeTarget != null)
		{
			final int[] targetCoords = siegeTarget.getTacticalCoords();
			final int[] myCoords = this.getTacticalCoords();
			if((myCoords!=null)&&(targetCoords != null))
				return CMLib.directions().getDirectionName(Directions.getRelative11Directions(myCoords, targetCoords));
		}
		return "";
	}

	protected MOB getFactoryAttacker(final Room thisRoom)
	{
		final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
		mob.setRiding(this);
		if((getOwnerName()!=null)&&(getOwnerName().length()>0))
		{
			final Clan clan = CMLib.clans().fetchClanAnyHost(getOwnerName());
			if(clan != null)
				mob.setClan(clan.name(), clan.getAutoPosition());
		}
		return mob;
	}

	protected Boolean startAttack(final MOB sourceM, final Room thisRoom, final String rest)
	{
		Item I=thisRoom.findItem(rest);
		if((I instanceof SiegableItem)
		&&(I!=this)
		&&(CMLib.flags().canBeSeenBy(I, sourceM)))
		{
			if(!sourceM.mayPhysicallyAttack(I))
			{
				sourceM.tell(L("You are not permitted to attack @x1",I.name()));
				return Boolean.FALSE;
			}
			final MOB mob = getFactoryAttacker(thisRoom);
			try
			{
				final CMMsg maneuverMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_ADVANCE,null,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE,null,
						CMMsg.MSG_ADVANCE,L("^F<S-NAME> engage(s) @x1.^?",I.Name()));
				if(thisRoom.okMessage(mob, maneuverMsg))
				{
					thisRoom.send(mob, maneuverMsg);
					siegeTarget	 = (SiegableItem)I;
					siegeCombatRoom	 = thisRoom;
					if(I instanceof SiegableItem)
					{
						final SiegableItem otherI=(SiegableItem)I;
						if(otherI.getCombatant() == null)
							otherI.setCombatant(this);
					}
					amInTacticalMode(); // now he is in combat
					//also support ENGAGE <name> as an alternative to attack?
					return Boolean.TRUE;
				}
			}
			finally
			{
				mob.destroy();
			}
		}
		else
		if(I==null)
		{
			final Room wasR=sourceM.location();
			final MOB M=thisRoom.fetchInhabitant(rest);
			if((M!=null)
			&&(CMLib.flags().canBeSeenBy(M, sourceM))
			&&(wasR!=null))
			{
				if(!this.canViewOuterRoom(wasR))
				{
					sourceM.tell(L("You can't attack @x1 from here.",M.name(sourceM)));
					return Boolean.FALSE;
				}
				if(!sourceM.mayIFight(M))
				{
					sourceM.tell(L("You are not permitted to attack @x1",M.name()));
					return Boolean.FALSE;
				}
				I=sourceM.fetchWieldedItem();
				if(I==null)
				{
					sourceM.tell(L("You can't attack @x1 from here.",M.name(sourceM)));
					return Boolean.FALSE;
				}
				else
				if((!(I instanceof Weapon))
				||((((Weapon)I).weaponClassification()!=Weapon.CLASS_RANGED)
					&&(((Weapon)I).weaponClassification()!=Weapon.CLASS_THROWN)))
				{
					sourceM.tell(L("You can't attack @x1 with @x2 from here.",M.name(sourceM),I.name(sourceM)));
					return Boolean.FALSE;
				}
				final Command C=CMClass.getCommand("Kill");
				final double actionCost = (C==null)?0:C.actionsCost(sourceM, new XVector<String>("Kill",rest));
				if((C==null)||(sourceM.actions()<=actionCost))
				{
					sourceM.tell(L("You aren't quite ready to attack just this second."));
					return Boolean.FALSE;
				}
				if(sourceM.isInCombat())
				{
					sourceM.tell(L("You are already in combat!"));
					return Boolean.FALSE;
				}
				final String uniqueID=Name()+"_"+sourceM.Name()+"_"+System.currentTimeMillis();
				final ExtendableAbility namerA=(ExtendableAbility)CMClass.getAbility("ExtAbility");
				{
					namerA.setAbilityID(uniqueID);
					namerA.setStatsAffector(new StatsAffecting() {
						final String newName = Name();
						@Override
						public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
						{
							affectableStats.setName(newName);
						}

						@Override
						public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
						{
						}

						@Override
						public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
						{
						}
					});
				}
				try
				{
					sourceM.addNonUninvokableEffect(namerA);
					sourceM.recoverPhyStats();
					thisRoom.bringMobHere(sourceM, false);
					CMLib.combat().postAttack(sourceM, M, I);
					sourceM.setActions(sourceM.actions()-actionCost);
				}
				finally
				{
					sourceM.delAbility(namerA);
					wasR.bringMobHere(sourceM, false);
					sourceM.makePeace(true);
				}
				return Boolean.FALSE;
			}
		}
		return null;
	}

	protected final boolean isASiegeWeaponReadyToFire(final Item I)
	{
		if(CMLib.combat().isASiegeWeapon(I))
		{
			if(((Rideable)I).riderCapacity() > 0)
				return ((Rideable)I).numRiders() >= ((Rideable)I).riderCapacity();
			return true;
		}
		return false;
	}

	protected static String staticL(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public String healthText(final MOB viewer)
	{
		final StringBuilder str=new StringBuilder("");
		if(this.subjectToWearAndTear())
		{
			final double pct=(CMath.div(usesRemaining(),100.0));
			appendCondition(str, pct, name());
		}
		return str.toString();
	}


	public static void appendCondition(final StringBuilder visualCondition, final double pct, final String name)
	{
		if(pct<=0.0)
			visualCondition.append(staticL("\n\r^r@x1^r is nothing but wreckage!^N",name));
		else
		if(pct<.10)
			visualCondition.append(staticL("\n\r^r@x1^r is near destruction!^N",name));
		else
		if(pct<.20)
			visualCondition.append(staticL("\n\r^r@x1^r is massively splintered and damaged.^N",name));
		else
		if(pct<.30)
			visualCondition.append(staticL("\n\r^r@x1^r is extremely splintered and damaged.^N",name));
		else
		if(pct<.40)
			visualCondition.append(staticL("\n\r^y@x1^y is very splintered and damaged.^N",name));
		else
		if(pct<.50)
			visualCondition.append(staticL("\n\r^y@x1^y is splintered and damaged.^N",name));
		else
		if(pct<.60)
			visualCondition.append(staticL("\n\r^p@x1^p is splintered and slightly damaged.^N",name));
		else
		if(pct<.70)
			visualCondition.append(staticL("\n\r^p@x1^p is showing large splinters.^N",name));
		else
		if(pct<.80)
			visualCondition.append(staticL("\n\r^g@x1^g is showing some splinters.^N",name));
		else
		if(pct<.90)
			visualCondition.append(staticL("\n\r^g@x1^g is showing small splinters.^N",name));
		else
		if(pct<.99)
			visualCondition.append(staticL("\n\r^g@x1^g is no longer in perfect condition.^N",name));
		else
			visualCondition.append(staticL("\n\r^c@x1^c is in perfect condition.^N",name));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(area == CMLib.map().areaLocation(msg.source())))
		{
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			final String secondWord=(cmds.size()>1) ? cmds.get(1).toUpperCase() : "";
			final SiegeCommand cmd=this.findSiegeCommand(word, secondWord);
			if(cmd != null)
			{
				switch(cmd)
				{
				case TARGET:
				{
					if(cmds.size()==1)
					{
						msg.source().tell(L("You must specify a target."));
						return false;
					}
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This "+noun_word+" is nowhere to be found!"));
						return false;
					}
					final String rest = CMParms.combine(cmds,1);
					final Boolean result = startAttack(msg.source(),thisRoom,rest);
					if(result  == Boolean.TRUE)
					{
						if(this.siegeTarget != null)
						{
							msg.source().tell(L("You are now targeting @x1.",this.siegeTarget.Name()));
							msg.source().tell(this.siegeTarget.getTacticalView(this));
						}
						return false;
					}
					else
					if(result  == Boolean.FALSE)
						return false;
					else
					{
						msg.source().tell(L("You don't see '@x1' here to target",rest));
						return false;
					}
				}
				case IMPLODE:
				{
					if(!CMSecurity.isAllowedEverywhere(msg.source(), CMSecurity.SecFlag.CMDROOMS))
						return true;
					final CMMsg damageMsg=CMClass.getMsg(msg.source(), this, CMMsg.MSG_DAMAGE, "SINK!!!");
					damageMsg.setValue(99999);
					this.executeMsg(this, damageMsg);
					return false;
				}
				case AIM:
				{
					final Room thisRoom = (Room)owner();
					if(thisRoom==null)
					{
						msg.source().tell(L("This "+noun_word+" is nowhere to be found!"));
						return false;
					}
					if((!this.amInTacticalMode())
					||(this.siegeTarget==null)
					||(!thisRoom.isContent(this.siegeTarget)))
					{
						msg.source().tell(L("Your "+noun_word+" must be targeting an enemy to aim weapons."));
						return false;
					}
					if((cmds.size()<3)&&(this.siegeTarget instanceof NavigableItem))
					{
						msg.source().tell(L("Aim what weapon how far ahead?"));
						msg.source().tell(this.siegeTarget.getTacticalView(this));
						return false;
					}
					final String leadStr = cmds.remove(cmds.size()-1);
					final String weaponStr = CMParms.combine(cmds,1);
					final Room mobR=msg.source().location();
					if((!CMath.isInteger(leadStr))||(CMath.s_int(leadStr)<0))
					{
						if(this.siegeTarget!=null)
							msg.source().tell(L("'@x1' is not a valid distance ahead of @x2 to fire.",leadStr,this.siegeTarget.name()));
						else
							msg.source().tell(L("'@x1' is not a valid distance.",leadStr));
						return false;
					}
					if(mobR!=null)
					{
						final Item I=mobR.findItem(null, weaponStr);
						if((I==null)||(!CMLib.flags().canBeSeenBy(I,msg.source())))
						{
							msg.source().tell(L("You don't see any siege weapon called '@x1' here.",leadStr));
							return false;
						}
						if(!CMLib.combat().isASiegeWeapon(I))
						{
							msg.source().tell(L("@x1 is not a usable siege weapon.",leadStr));
							return false;
						}
						final AmmunitionWeapon weapon=(AmmunitionWeapon)I;
						int distance = weapon.maxRange();
						int[] targetCoords = new int[2];
						int leadAmt=0;
						if(this.siegeTarget instanceof SiegableItem)
						{
							targetCoords = this.siegeTarget.getTacticalCoords();
							if(targetCoords == null)
							{
								msg.source().tell(L("You must be targeting an enemy to aim weapons."));
								return false;
							}
							distance = rangeToTarget();
							leadAmt = CMath.s_int(leadStr);
							final int direction;
							if(this.siegeTarget instanceof NavigableItem)
								direction = ((NavigableItem)this.siegeTarget).getDirectionFacing();
							else
								direction = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
							for(int i=0;i<leadAmt;i++)
								targetCoords = Directions.adjustXYByDirections(targetCoords[0], targetCoords[1], direction);
						}
						if((weapon.maxRange() < distance)||(weapon.minRange() > distance))
						{
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+" target is presently at distance of "+distance+", but "+weapon.Name()+" range is "+weapon.minRange()+" to "+weapon.maxRange());
							msg.source().tell(L("Your target is presently at distance of @x1, but this weapons range is @x2 to @x3.",
												""+distance,""+weapon.minRange(),""+weapon.maxRange()));
							return false;
						}
						if(weapon.requiresAmmunition()
						&& (weapon.ammunitionCapacity() > 0)
						&& (weapon.ammunitionRemaining() == 0))
						{
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+": "+weapon.Name()+" wasn't loaded, couldn't be aimed.");
							msg.source().tell(L("@x1 needs to be LOADed first.",weapon.Name()));
							return false;
						}
						final String timeToFire=""+(CMLib.threads().getTimeMsToNextTick((Tickable)CMLib.combat(), Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK) / 1000);
						final String msgStr=L("<S-NAME> aim(s) <O-NAME> at <T-NAME> (@x1).",""+leadAmt);
						if(msg.source().isMonster() && aimings.containsFirst(weapon))
						{
							msg.source().tell(L("@x1 is already aimed.",weapon.Name()));
							return false;
						}
						final CMMsg msg2=CMClass.getMsg(msg.source(), siegeTarget, weapon,
														CMMsg.MSG_NOISYMOVEMENT, msgStr,
														CMMsg.MSG_NOISYMOVEMENT, SiegableItem.SiegeCommand.AIM.name(),
														CMMsg.MSG_NOISYMOVEMENT, msgStr);
						if(mobR.okMessage(msg.source(), msg2))
						{
							this.aimings.removeFirst(weapon);
							this.aimings.add(new Pair<Weapon,int[]>(weapon,targetCoords));
							mobR.send(msg.source(), msg2);
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+": aimed "+weapon.Name()+" at : "+CMParms.toListString(targetCoords));
							if((!(I instanceof AmmunitionWeapon))
							||(!((AmmunitionWeapon)I).requiresAmmunition()))
								msg.source().tell(L("@x1 is now aimed and will be engage in @x2 seconds.",I.name(),timeToFire));
							else
								msg.source().tell(L("@x1 is now aimed and will be fired in @x2 seconds.",I.name(),timeToFire));
						}
					}
					return false;
				}
				}
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0))
		{
			switch(Character.toUpperCase(msg.targetMessage().charAt(0)))
			{
			case 'A':
			{
				final List<String> parsedFail = CMParms.parse(msg.targetMessage());
				final String cmd=parsedFail.get(0).toUpperCase();
				if(("ATTACK".startsWith(cmd))&&(owner() instanceof Room))
				{
					final Room thisRoom = (Room)owner();
					final String rest = CMParms.combine(parsedFail,1);
					if(!securityCheck(msg.source()))
					{
						msg.source().tell(L("The captain does not permit you."));
						return false;
					}
					final Boolean result = startAttack(msg.source(),thisRoom,rest);
					if(result == Boolean.FALSE)
						return false;
					else
					if(result == Boolean.TRUE)
					{
						if(this.siegeTarget != null)
							msg.source().tell(this.siegeTarget.getTacticalView(this));
						return false;
					}
				}
				break;
			}
			case 'E':
			case 'L':
			{
				final List<String> parsedFail = CMParms.parse(msg.targetMessage());
				final String cmd=parsedFail.get(0).toUpperCase();
				if(("LOOK".startsWith(cmd)||"LLOOK".startsWith(cmd)||"EXAMINE".startsWith(cmd))
				&&(owner() instanceof Room)
				&&(this.canViewOuterRoom((Room)owner())))
				{
					final Room R = (Room)owner();
					final String rest = CMParms.combine(parsedFail,1);
					final Item I = R.findItem(null, rest);
					if(I!=null)
					{
						final int msgType = "EXAMINE".startsWith(cmd) ? CMMsg.MSG_EXAMINE : CMMsg.MSG_LOOK;
						final CMMsg lookMsg=CMClass.getMsg(msg.source(),I,null,msgType,null,msgType,null,msgType,null);
						if(R.okMessage(msg.source(),lookMsg))
						{
							R.send(msg.source(),lookMsg);
							return false;
						}
					}
				}
				break;
			}
			case 'T': // throwing things to another base, like a grapple
			{
				if(disableCmds.contains("THROW"))
					return true;
				final List<String> parsedFail = CMParms.parse(msg.targetMessage());
				final String cmd=parsedFail.get(0).toUpperCase();
				if(("THROW".startsWith(cmd))
				&&(owner() instanceof Room)
				&&(msg.source().location()!=null)
				&&(super.canViewOuterRoom(msg.source().location()))
				&&(parsedFail.size()>2))
				{
					parsedFail.remove(0);
					final MOB mob=msg.source();
					final Room R = (Room)owner();
					String str=parsedFail.get(parsedFail.size()-1);
					parsedFail.remove(str);
					final String what=CMParms.combine(parsedFail,0);
					Item item=mob.fetchItem(null,Wearable.FILTER_WORNONLY,what);
					if(item==null)
						item=mob.findItem(null,what);
					if((item!=null)
					&&(CMLib.flags().canBeSeenBy(item,mob))
					&&((item.amWearingAt(Wearable.WORN_HELD))||(item.amWearingAt(Wearable.WORN_WIELD))))
					{
						str=str.toLowerCase();
						if(str.equals("water")||str.equals("overboard")||CMLib.english().containsString(R.displayText(), str))
						{
							final Room target=R;
							final CMMsg msg2=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<S-NAME> throw(s) <O-NAME> overboard."));
							final CMMsg msg3=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<O-NAME> fl(ys) in from @x1.",name()));
							if(mob.location().okMessage(mob,msg2)&&target.okMessage(mob,msg3))
							{
								mob.location().send(mob,msg2);
								target.sendOthers(mob,msg3);
							}
							return false;
						}

						final Item I = R.findItem(null, str);
						if(I!=this)
						{
							if(I instanceof SiegableItem)
							{
								if((!amInTacticalMode())
								||(I.maxRange() < getTacticalDistance((SiegableItem)I)))
								{
									msg.source().tell(L("You can't throw @x1 at @x2, it's too far away!",item.name(msg.source()),I.name(msg.source())));
									return false;
								}
								else
								if(getTacticalCoords()!=null)
								{
									final int[] targetCoords = ((StdNavigableBoardable)I).getTacticalCoords();
									if(targetCoords!=null)
									{
										final int dir = Directions.getRelativeDirection(getTacticalCoords(), targetCoords);
										final String inDir=CMLib.directions().getShipInDirectionName(dir);
										final String fromDir=CMLib.directions().getFromShipDirectionName(Directions.getOpDirectionCode(dir));
										Room target = ((StdNavigableBoardable)I).getRandomOutsideRoom();
										if(target == null)
											target=R;
										final CMMsg msg2=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<S-NAME> throw(s) <O-NAME> @x1.",inDir.toLowerCase()));
										final CMMsg msg3=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<O-NAME> fl(ys) in from @x1.",fromDir.toLowerCase()));
										if(mob.location().okMessage(mob,msg2)&&target.okMessage(mob,msg3))
										{
											mob.location().send(mob,msg2);
											target.sendOthers(mob,msg3);
										}
										return false;
									}
									else
									{
										msg.source().tell(L("You can't throw @x1 at @x2, it's too far away!",item.name(msg.source()),I.name(msg.source())));
										return false;
									}
								}
								else
								{
									msg.source().tell(L("You can't throw @x1 at @x2, it's too far away!",item.name(msg.source()),I.name(msg.source())));
									return false;
								}
							}
						}
					}
				}
				break;
			}
			default:
				break;
			}
		}

		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_SPECIALCOMBAT)
		{
			if(this.amInTacticalMode())
			{
				final List<Weapon> weapons = new LinkedList<Weapon>();
				for(final Enumeration<Room> r=this.getArea().getProperMap();r.hasMoreElements();)
				{
					try
					{
						final Room R=r.nextElement();
						if(R!=null)
						{
							for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
							{
								try
								{
									final Item I=i.nextElement();
									if(isASiegeWeaponReadyToFire(I))
										weapons.add((Weapon)I);
								}
								catch(final NoSuchElementException ne)
								{
								}
							}
						}
					}
					catch(final NoSuchElementException ne)
					{
					}
				}
				if(weapons.size()>0)
				{
					final MOB mob = getFactoryAttacker(null);
					final int[] coordsToHit;
					final SiegableItem siegeTarget;
					synchronized(this)
					{
						siegeTarget=this.siegeTarget;
					}
					coordsToHit = siegeTarget.getTacticalCoords();
					try
					{
						int notLoaded = 0;
						int notAimed = 0;
						final PairList<Weapon,int[]> aimings=this.aimings;
						for(final Weapon w : weapons)
						{
							final Room R=CMLib.map().roomLocation(w);
							if(R!=null)
							{
								mob.setLocation(R);
								if((w instanceof AmmunitionWeapon)
								&&(((AmmunitionWeapon)w).requiresAmmunition())
								&&(((AmmunitionWeapon)w).ammunitionRemaining() <=0))
									notLoaded++;
								else
								if(aimings!=null)
								{
									//mob.setRangeToTarget(0);
									final int index = aimings.indexOfFirst(w);
									if(index >= 0)
									{
										final int[] coordsAimedAt = aimings.remove(index).second;
										final boolean wasHit = Arrays.equals(coordsAimedAt, coordsToHit);
										CMLib.combat().postSiegeAttack(mob, this, siegeTarget, w, wasHit);
										if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
										{
											final String targetedName=siegeTarget!=null?siegeTarget.Name():"Unknown";
											Log.debugOut("SiegeCombat: "+Name()+" aimed "+w.Name()+" at "+CMParms.toListString(coordsAimedAt)
															+" and "+(wasHit?"hit ":"missed ")+targetedName+" at "+CMParms.toListString(coordsToHit));
										}
									}
									else
										notAimed++;
								}
							}
						}
						final String spamMsg;
						if((notLoaded > 0) && (notAimed > 0))
							spamMsg = L("@x1 of your weapons were not loaded, and @x2 were ready but not aimed.",""+notLoaded, ""+notAimed);
						else
						if(notLoaded > 0)
							spamMsg = L("@x1 of your weapons were not loaded.",""+notLoaded);
						else
						if(notAimed > 0)
							spamMsg = L("@x1 of your weapons were ready but not aimed.",""+notAimed);
						else
							spamMsg = "";
						if(spamMsg.length()>0)
						{
							if(spamMsg.equals(lastSpamMsg))
							{
								if(lastSpamCt < 3)
								{
									if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
									{
										final String targetedName=siegeTarget!=null?siegeTarget.Name():"Unknown";
										Log.debugOut("SiegeCombat: "+Name()+" targeted: "+targetedName+", status: "+spamMsg);
									}
									announceToOuterViewers(spamMsg);
									lastSpamCt++;
								}
							}
							else
							{
								if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								{
									final String targetedName=siegeTarget!=null?siegeTarget.Name():"Unknown";
									Log.debugOut("SiegeCombat: "+Name()+" targeted: "+targetedName+", status: "+spamMsg);
								}
								announceToOuterViewers(spamMsg);
								lastSpamCt=0;
							}
						}
						lastSpamMsg=spamMsg;
					}
					finally
					{
						mob.setRangeToTarget(0);
						mob.destroy();
					}
				}
			}
		}
		return super.tick(ticking, tickID);
	}

	protected Item doCombatDefeat(final MOB victorM, final boolean createBody)
	{
		return null;
	}

	@Override
	public Item killMeDead(final boolean createBody)
	{
		final MOB mob=CMClass.getFactoryMOB(name(), phyStats().level(), CMLib.map().roomLocation(this));
		try
		{
			return doCombatDefeat(mob, createBody);
		}
		finally
		{
			mob.destroy();
		}
	}

	@Override
	public int getMaxHullPoints()
	{
		return (5 * getArea().numberOfProperIDedRooms())+(phyStats().armor());
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target() instanceof Room)
		&&(msg.target() == owner()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if((CMLib.map().areaLocation(msg.source())==area))
				{
					final StringBuilder visualCondition = new StringBuilder("");
					if(this.subjectToWearAndTear() && (usesRemaining() <= 100)
					&& (this.siegeTarget != null)
					&& (!this.siegeTarget.amDestroyed()))
					{
						final double pct=(CMath.div(usesRemaining(),100.0));
						appendCondition(visualCondition,pct,name(msg.source()));
					}
					if(visualCondition.length()>0)
						msg.addTrailerMsg(CMClass.getMsg(msg.source(), null, null, CMMsg.MSG_OK_VISUAL, visualCondition.toString(), -1, null, -1, null));
				}
				break;
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_ENTER:
				if((owner() instanceof Room)
				&&(msg.target() instanceof Room)
				&&(((Room)msg.target()).getArea()!=area))
				{
					if(((msg.source().riding() == this)
						&&(msg.source().Name().equals(Name())))
					||((this.siegeTarget!=null)
						&&(msg.source().riding() == siegeTarget)
						&&(msg.source().Name().equals(siegeTarget.Name()))))
					{
						clearTacticalModeInternal();
					}
				}
				break;
			}
		}
		else
		if(msg.target()  == this)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(msg.target() instanceof SiegableItem)
				{
					final String otherInfo=((SiegableItem)msg.target()).getTacticalView(this);
					if(otherInfo.length()>0)
					{
						msg.addTrailerRunnable(new Runnable()
						{
							@Override
							public void run()
							{
								msg.source().tell(otherInfo);
								msg.trailerRunnables().remove(this);
							}
						});
					}
				}
				break;
			case CMMsg.TYP_ENTER:
				break;
			case CMMsg.TYP_WEAPONATTACK:
				{
					Weapon weapon=null;
					if((msg.tool() instanceof Weapon))
						weapon=(Weapon)msg.tool();
					if((weapon!=null)
					&&(((msg.source().riding()!=null)&&(owner() instanceof Room))
						||((msg.source().location()!=null) && (weapon.owner()==null))))
					{
						final Room baseRoom=(Room)owner();
						final boolean isHit=msg.value()>0;
						if(isHit && CMLib.combat().isASiegeWeapon(weapon)
						&& (((AmmunitionWeapon)weapon).ammunitionCapacity() > 1))
						{
							int shotsRemaining = ((AmmunitionWeapon)weapon).ammunitionRemaining() + 1;
							((AmmunitionWeapon)weapon).setAmmoRemaining(0);
							final Area A=this.getArea();
							final ArrayList<Pair<MOB,Room>> targets = new ArrayList<Pair<MOB,Room>>(5);
							if(A!=null)
							{
								for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
								{
									final Room R=r.nextElement();
									if((R!=null)&&((R.domainType()&Room.INDOORS)==0))
									{
										for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
											targets.add(new Pair<MOB,Room>(m.nextElement(),R));
									}
								}
							}
							final int chanceToHit = targets.size() * 20;
							final Room oldRoom=msg.source().location();
							try
							{
								final double pctLoss = CMath.div(CMLib.dice().roll(1, weapon.phyStats().damage(),0), 100.0);
								while(shotsRemaining-- > 0)
								{
									final Pair<MOB,Room> randomPair = (targets.size()>0)? targets.get(CMLib.dice().roll(1,targets.size(),-1)) : null;
									if(randomPair != null)
									{
										// assume msg.source() is a factory mob
										msg.source().basePhyStats().setLevel(randomPair.first.basePhyStats().level());
										msg.source().phyStats().setLevel(randomPair.first.phyStats().level());
										msg.source().setLocation(baseRoom);
									}
									if((CMLib.dice().rollPercentage() < chanceToHit)&&(randomPair != null))
									{
										final int pointsLost = (int)Math.round(pctLoss * randomPair.first.maxState().getHitPoints());
										CMLib.combat().postWeaponDamage(msg.source(), randomPair.first, weapon, pointsLost);
									}
									else
									if(randomPair != null)
										CMLib.combat().postWeaponAttackResult(msg.source(), randomPair.first, weapon, 0, false);
									else
										this.announceToOuterViewers(msg.source(), msg.target(), weapon, weapon.missString());
								}
							}
							finally
							{
								msg.source().setLocation(oldRoom);
							}
						}
						else
						{
							MOB factoryM = null;
							try
							{
								PhysicalAgent attacker;
								if(msg.source().riding() instanceof Boardable)
									attacker=msg.source().riding();
								else
								{
									final Room R=msg.source().location();
									if((R!=null)
									&&(R.getArea() instanceof Boardable))
										attacker=((Boardable)R.getArea()).getBoardableItem();
									else
									{
										factoryM=CMClass.getFactoryMOB(L("someone"), 1, CMLib.map().roomLocation(this));
										attacker=factoryM;
									}
								}
								if(attacker != null)
									CMLib.combat().postSiegeWeaponAttackResult(msg.source(), attacker, this, weapon, isHit);
							}
							finally
							{
								if(factoryM!=null)
									factoryM.destroy();
							}
						}
					}
				}
				break;
			case CMMsg.TYP_DAMAGE:
				if(msg.value() > 0)
				{
					final int maxHullPoints = getMaxHullPoints();
					final double pctLoss = CMath.div(msg.value(), maxHullPoints);
					final int pointsLost = msg.value();
					final int pctLostAmt = (int)Math.round(pctLoss * 100.0);
					if(pctLostAmt > 0)
					{
						final int weaponType = (msg.tool() instanceof Weapon) ? ((Weapon)msg.tool()).weaponDamageType() : Weapon.TYPE_BASHING;
						final String hitWord = CMLib.combat().standardHitWord(weaponType, pctLoss);
						final String msgStr = (msg.targetMessage() == null) ? L("<O-NAME> fired from <S-NAME> hits and @x1 @x2.",hitWord,name()) : msg.targetMessage();
						final CMMsg deckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, msgStr);
						this.sendAreaMessage(msg.source(), deckHitMsg, true);
						final CMMsg underdeckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, L("Something hits and @x1 the "+noun_word+".",hitWord));
						this.sendAreaMessage(msg.source(), underdeckHitMsg, false);
						if(pctLostAmt >= this.usesRemaining())
						{
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+" takes "+pointsLost+"/"+maxHullPoints+", which is the remaining "+usesRemaining()+"%, and is defeated.");
							this.setUsesRemaining(0);
							this.recoverPhyStats(); // takes away the swimmability!
							this.doCombatDefeat(msg.source(), true);
							this.clearTacticalModeInternal();
						}
						else
						{
							this.setUsesRemaining(this.usesRemaining() - pctLostAmt);
							if(CMSecurity.isDebugging(DbgFlag.SIEGECOMBAT))
								Log.debugOut("SiegeCombat: "+Name()+" takes "+pointsLost+"/"+maxHullPoints+" points of damage, and has "+usesRemaining()+"% of points remaining.");
						}
					}
				}
				break;
			}
		}
		else
		if(msg.target() instanceof AmmunitionWeapon)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_RELOAD:
				if((msg.tool() instanceof Ammunition)
				&&(CMLib.combat().isASiegeWeapon((Item)msg.target())))
				{
					final MOB tellM=msg.source();
					final Item I= (Item)msg.target();
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							tellM.tell(L("@x1 is now loaded. Don't forget to aim.",I.name()));
						}
					});
				}
				break;
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean amDead()
	{
		return amDestroyed();
	}

	@Override
	public void setStat(final String code, final String val)
	{
		final String up_code = (""+code).toUpperCase();
		if(up_code.startsWith("SPECIAL_"))
		{
			if(up_code.equals("SPECIAL_DISABLE_CMDS"))
			{
				disableCmds.clear();
				disableCmds.addAll(CMParms.parseCommas(val.toUpperCase().trim(),true));
			}
		}
		super.setStat(up_code, val);
	}
}
