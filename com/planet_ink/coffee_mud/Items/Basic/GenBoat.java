package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2001-2021 Bo Zimmerman

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
public class GenBoat extends GenRideable implements SiegableItem
{
	@Override
	public String ID()
	{
		return "GenBoat";
	}

	public GenBoat()
	{
		super();
		setName("a boat");
		setDisplayText("a boat is here.");
		setDescription("Looks like a boat");
		rideBasis=Rideable.Basis.WATER_BASED;
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	protected SiegableItem				siegeTarget		= null;
	protected Room						siegeCombatRoom	= null;
	protected PairList<Item, int[]>		coordinates		= null;

	@Override
	public void setRangeToTarget(final int newRange)
	{
		//nothing to do atm
	}

	protected int getDirectionToTarget(final PhysicalAgent dirTarget)
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

	@Override
	public boolean subjectToWearAndTear()
	{
		return true;
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
	public final int getMaxHullPoints()
	{
		return 12+(phyStats().armor());
	}

	@Override
	public boolean isInCombat()
	{
		final Physical siegeTarget=this.siegeTarget;
		if((siegeTarget != null)&& (siegeCombatRoom != null))
		{
			if(siegeTarget.amDestroyed())
			{
				clearTacticalModeInternal();
				return false;
			}
			return true;

		}
		return false;
	}

	@Override
	public boolean mayIFight(final PhysicalAgent victim)
	{
		final PhysicalAgent defender=victim;
		MOB mob = null;
		for(final Enumeration<Rider> r=riders();r.hasMoreElements();)
		{
			final Rider R=r.nextElement();
			if(R instanceof MOB)
				mob=(MOB)R;
		}
		if(mob==null)
			return true;
		return CMLib.combat().mayIAttackThisVessel(mob, defender);
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
			if(coordinates == null)
			{
				synchronized((""+siegeCombatRoom + "_SIEGE_TACTICAL").intern())
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
				}
				final PairList<Item,int[]> coords = this.coordinates;
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
		synchronized((""+siegeCombatRoom + "_SIEGE_TACTICAL").intern())
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
	}

	protected synchronized void clearTacticalModeInternal()
	{
		final Room siegeCombatRoom = this.siegeCombatRoom;
		if(siegeCombatRoom != null)
		{
			PairList<Item,int[]> coords = null;
			synchronized((""+siegeCombatRoom + "_SIEGE_TACTICAL").intern())
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
		return new PairVector<Weapon, int[]>();
	}

	@Override
	public String getTacticalView(final SiegableItem viewer)
	{
		final int[] targetCoords = getTacticalCoords();
		final int[] myCoords;
		final String dist = ""+getTacticalDistance(viewer);
		if(viewer instanceof PhysicalAgent)
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

	@Override
	public PairList<Item, int[]> getCombatField()
	{
		return coordinates;
	}

	@Override
	public boolean amDead()
	{
		final ItemPossessor owner = owner();
		if(owner instanceof Room)
		{
			final Room R = (Room)owner;
			final boolean isWater=CMLib.flags().isUnderWateryRoom(R);
			return isWater && (R.getGridParent()!=null) && (R.getGridParent().roomID().length()==0);
		}
		return amDestroyed();
	}

	@Override
	public Item killMeDead(final boolean createBody)
	{
		final Room shipR=CMLib.map().roomLocation(this);
		final MOB sinkerM=CMClass.getFactoryMOB(name(), phyStats().level(), shipR);
		try
		{
			if(CMLib.flags().isDeepWaterySurfaceRoom(shipR))
			{
				CMLib.tracking().makeSink(this, shipR, false);
				final String sinkString = L("<T-NAME> start(s) sinking!");
				shipR.show(sinkerM, this, CMMsg.MSG_OK_ACTION, sinkString);
				return this;
			}
			else
			{
				final String sinkString = L("<T-NAME> <T-IS-ARE> destroyed!");
				shipR.show(sinkerM, this, CMMsg.MSG_OK_ACTION, sinkString);
				final Item newI = CMLib.utensils().ruinItem(this);
				if(newI != this)
					shipR.addItem(newI, Expire.Monster_EQ);
				this.destroy();
				return newI;
			}
		}
		finally
		{
			sinkerM.destroy();
		}
	}

	@Override
	public String healthText(final MOB viewer)
	{
		final int[] condSet = new int[]{95, 85, 75, 50, 25, 10, 5, 0};
		int ordinal=0;
		for(int i=0;i<condSet.length;i++)
		{
			if(usesRemaining()>=condSet[i])
			{
				ordinal=i;
				break;
			}
		}
		final String message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEAPON_CONDITION_OTHER, ordinal);
		return L(message,name(),""+usesRemaining());
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DAMAGE:
			if((msg.target()==this)
			&& (msg.value() > 0)
			&& (!this.amDead()))
			{
				int level = phyStats().level();
				if(level < 10)
					level = 10;
				final double pctLoss = CMath.div(msg.value(), level) * 10.0; // siege weapons against rideables is harsh
				final int pointsLost = (int)Math.round(pctLoss * level);
				if(pointsLost > 0)
				{
					/*
					final int weaponType = (msg.tool() instanceof Weapon) ? ((Weapon)msg.tool()).weaponDamageType() : Weapon.TYPE_BASHING;
					final String hitWord = CMLib.combat().standardHitWord(weaponType, pctLoss);
					final String msgStr = L("<O-NAME> fired from <S-NAME> hits and @x1 @x2.",hitWord,name());
					final CMMsg deckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, msgStr);
					final Room targetRoom=CMLib.map().roomLocation(this);
					if(targetRoom.okMessage(msg.source(), deckHitMsg))
						targetRoom.send(msg.source(), deckHitMsg);
					*/
					if(pointsLost >= this.usesRemaining())
					{
						this.setUsesRemaining(0);
						this.recoverPhyStats(); // takes away the swimmability!
						final Room shipR=CMLib.map().roomLocation(this);
						if(shipR!=null)
							this.killMeDead(true);
						/*
						if(!CMLib.leveler().postExperienceToAllAboard(msg.source().riding(), 500, this))
							CMLib.leveler().postExperience(msg.source(), null, null, 500, false);
						*/
					}
					else
					{
						this.setUsesRemaining(this.usesRemaining() - pointsLost);
					}
				}
			}
			break;
		default:
			break;
		}
	}
}
