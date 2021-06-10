package com.planet_ink.coffee_mud.Items.Weapons;
import java.util.*;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdRideable;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2016-2021 Bo Zimmerman

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
public class StdSiegeWeapon extends StdRideable implements AmmunitionWeapon, SiegableItem
{
	@Override
	public String ID()
	{
		return "StdSiegeWeapon";
	}

	protected int		weaponDamageType		= TYPE_PIERCING;
	protected int		weaponClassification	= CLASS_RANGED;
	protected boolean	useExtendedMissString	= false;
	protected int		minRange				= 0;
	protected int		maxRange				= 10;
	protected int		ammoCapacity			= 1;

	public StdSiegeWeapon()
	{
		super();
		setName("a siege weapon bow");
		setDisplayText("a siege weapon is mounted here.");
		setDescription("It looks like it might fire special ammunition");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(500);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(20);
		//basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.SENSE_ITEMNOTGET);
		setAmmunitionType("spears");
		setAmmoCapacity(1);
		setAmmoRemaining(1);
		baseGoldValue=15000;
		recoverPhyStats();
		minRange=1;
		maxRange=10;
		setRiderCapacity(0);
		weaponDamageType=Weapon.TYPE_PIERCING;
		material=RawMaterial.RESOURCE_WOOD;
		weaponClassification=Weapon.CLASS_RANGED;
		properWornBitmap=0;
		wornLogicalAnd = false;
	}

	@Override
	public int weaponDamageType()
	{
		return weaponDamageType;
	}

	@Override
	public int weaponClassification()
	{
		return weaponClassification;
	}

	@Override
	public void setWeaponDamageType(final int newType)
	{
		weaponDamageType = newType;
	}

	@Override
	public void setWeaponClassification(final int newClassification)
	{
		weaponClassification = newClassification;
	}

	@Override
	public boolean isFreeStanding()
	{
		return true;
	}

	@Override
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(phyStats().ability()>0)
			id=name()+" +"+phyStats().ability()+((id.length()>0)?"\n":"")+id;
		else
		if(phyStats().ability()<0)
			id=name()+" "+phyStats().ability()+((id.length()>0)?"\n":"")+id;
		return id+"\n\rAttack: "+phyStats().attackAdjustment()+", Damage: "+phyStats().damage();
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(phyStats().damage()!=0)
		{
			final int ability=super.wornLogicalAnd ? (phyStats().ability()*CMath.numberOfSetBits(super.myWornCode)) : phyStats().ability();
			phyStats().setDamage(phyStats().damage()+(ability*2));
			phyStats().setAttackAdjustment(phyStats().attackAdjustment()+(ability*10));
		}
		if((subjectToWearAndTear())&&(usesRemaining()<100))
			phyStats().setDamage(((int)Math.round(CMath.mul(phyStats().damage(),CMath.div(usesRemaining(),100)))));
	}

	protected SiegableItem				siegeTarget		= null;
	protected Room						siegeCombatRoom	= null;
	protected PairList<Item, int[]>		coordinates		= null;
	protected PairList<Weapon, int[]>	aimings			= new PairVector<Weapon, int[]>();

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
	public final int getMaxHullPoints()
	{
		return 12;
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
				clearTacticalModeInternal();
				return false;
			}
			return true;

		}
		return false;
	}

	@Override
	public boolean isDefeated()
	{
		return amDestroyed();
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
		this.aimings.clear();
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
		return this.aimings;
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
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(CMLib.flags().canBeSeenBy(this,msg.source()))
				{
					if(requiresAmmunition())
						msg.source().tell(L("@x1 remaining: @x2/@x3.",CMStrings.capitalizeAndLower(ammunitionType()),""+ammunitionRemaining(),""+ammunitionCapacity()));
					if((subjectToWearAndTear())&&(usesRemaining()<100))
						msg.source().tell(weaponHealth());
				}
				break;
			case CMMsg.TYP_RELOAD:
				if(msg.tool() instanceof Ammunition)
				{
					boolean recover=false;
					final Ammunition I=(Ammunition)msg.tool();
					int howMuchToTake=ammunitionCapacity();
					if(I.ammunitionRemaining()<howMuchToTake)
						howMuchToTake=I.ammunitionRemaining();
					if(this.ammunitionCapacity() - this.ammunitionRemaining() < howMuchToTake)
						howMuchToTake=this.ammunitionCapacity() - this.ammunitionRemaining();
					setAmmoRemaining(this.ammunitionRemaining() + howMuchToTake);
					I.setAmmoRemaining(I.ammunitionRemaining()-howMuchToTake);
					final LinkedList<Ability> removeThese=new LinkedList<Ability>();
					for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(!A.isSavable())&&(A.invoker()==null))
							removeThese.add(A);
					}
					for(final Ability A : removeThese)
						delEffect(A);
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						Ability A=a.nextElement();
						if((A!=null)&&(A.isSavable())&&(fetchEffect(A.ID())==null))
						{
							A=(Ability)A.copyOf();
							A.setInvoker(null);
							A.setSavable(false);
							addEffect(A);
							recover=true;
						}
					}
					if(I.ammunitionRemaining()<=0)
						I.destroy();
					if(recover)
						recoverOwner();
				}
				break;
			case CMMsg.TYP_UNLOAD:
				if(msg.tool() instanceof Ammunition)
				{
					final Ammunition ammo=(Ammunition)msg.tool();
					for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(!A.isSavable())&&(A.invoker()==null))
						{
							final Ability ammoA=(Ability)A.copyOf();
							ammo.addNonUninvokableEffect(ammoA);
						}
					}
					setAmmoRemaining(0);
					final Room R=msg.source().location();
					if(R!=null)
					{
						R.addItem(ammo, ItemPossessor.Expire.Player_Drop);
						CMLib.commands().postGet(msg.source(), null, ammo, true);
					}
				}
				break;
			case CMMsg.TYP_DAMAGE:
				if(msg.value() > 0)
				{
					int level = phyStats().level();
					if(level < 10)
						level = 10;
					final double pctLoss = CMath.div(msg.value(), level) * 10.0; // siege weapons against rideables is harsh
					final int pointsLost = (int)Math.round(pctLoss * level);
					if(pointsLost > 0)
					{
						final int weaponType = (msg.tool() instanceof Weapon) ? ((Weapon)msg.tool()).weaponDamageType() : Weapon.TYPE_BASHING;
						final String hitWord = CMLib.combat().standardHitWord(weaponType, pctLoss);
						final String msgStr = (msg.targetMessage() == null) ? L("<O-NAME> fired from <S-NAME> hits and @x1 @x2.",hitWord,name()) : msg.targetMessage();
						final CMMsg deckHitMsg=CMClass.getMsg(msg.source(), this, msg.tool(),CMMsg.MSG_OK_ACTION, msgStr);
						final Room targetRoom=CMLib.map().roomLocation(this);
						if(targetRoom.okMessage(msg.source(), deckHitMsg))
							targetRoom.send(msg.source(), deckHitMsg);
						if(pointsLost >= this.usesRemaining())
						{
							this.setUsesRemaining(0);
							this.recoverPhyStats(); // takes away the swimmability!
							final Room shipR=CMLib.map().roomLocation(this);
							if(shipR!=null)
							{
								final String sinkString = L("<T-NAME> <T-IS-ARE> destroyed!");
								shipR.show(msg.source(), this, CMMsg.MSG_OK_ACTION, sinkString);
								this.destroy();
							}
						}
						else
						{
							this.setUsesRemaining(this.usesRemaining() - pointsLost);
						}
					}
				}
				break;
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target()==this)
		&&(msg.value()>0)
		&&(subjectToWearAndTear())
		&&((!CMLib.flags().isABonusItems(this))||(CMLib.dice().rollPercentage() > phyStats().level()))
		&&((material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ENERGY)
		&&((material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GAS))
		{
			CMLib.combat().postItemDamage(msg.source(), this, null, 1, CMMsg.TYP_JUSTICE, null);
		}
	}

	protected MOB getFactoryAttacker(final Room thisRoom)
	{
		final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),thisRoom);
		mob.setRiding(this);
		return mob;
	}

	protected Boolean startAttack(final MOB sourceM, final Room thisRoom, final String rest)
	{
		final Item I=thisRoom.findItem(rest);
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
				final CMMsg maneuverMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_ADVANCE,null,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE,null,CMMsg.MSG_ADVANCE,L("<S-NAME> engage(s) @x1.",I.Name()));
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
		return null;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.tool()==this)
		&&(requiresAmmunition())
		&&(ammunitionCapacity()>0))
		{
			if(ammunitionRemaining()>ammunitionCapacity())
				setAmmoRemaining(ammunitionCapacity());
			if(ammunitionRemaining()<=0)
				return false;
			else
				setUsesRemaining(usesRemaining()-1);
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(owner() instanceof Room)
		&&(!(((Room)owner()) instanceof BoardableItem)))
		{
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()<1)
				return true;
			final String word=cmds.get(0).toUpperCase();
			if("TARGET".startsWith(word))
			{
				final boolean isRiding=msg.source().riding()==this;
				if((cmds.size()==1)
				||((!isRiding)&&(cmds.size()<3)))
				{
					if(isRiding)
						msg.source().tell(L("You must specify a target."));
					else
						msg.source().tell(L("You must which weapon to target, and at what."));
					return false;
				}
				final Room thisRoom = (Room)owner();
				if(thisRoom==null)
				{
					msg.source().tell(L("@x1 is nowhere to be found!",name()));
					return false;
				}
				if(!isRiding)
				{
					final String what=cmds.get(1);
					if(msg.source().location().findItem(null, what)!=this)
						return true;
					cmds.remove(1);
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
		}
		return true;
	}

	@Override
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	protected String weaponHealth()
	{
		if(usesRemaining()>=100)
			return "";
		else
		if(usesRemaining()>=95)
			return name()+" looks slightly used ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>=85)
			 return name()+" is somewhat worn ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>=75)
			 return name()+" is worn ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>50)
			return name()+" is damaged ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>25)
			return name()+" is heavily damaged ("+usesRemaining()+"%)";
		else
			return name()+" is so damaged, it is practically harmless ("+usesRemaining()+"%)";
	}

	@Override
	public String missString()
	{
		return CMLib.combat().standardMissString(weaponDamageType,weaponClassification,name(),useExtendedMissString);
	}

	@Override
	public String hitString(final int damageAmount)
	{
		return CMLib.combat().standardHitString(weaponDamageType, weaponClassification,damageAmount,name());
	}

	@Override
	public int minRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMINRANGE))
			return 0;
		return minRange;
	}

	@Override
	public int maxRange()
	{
		if(CMath.bset(phyStats().sensesMask(),PhyStats.SENSE_ITEMNOMAXRANGE))
			return 100;
		return maxRange;
	}

	@Override
	public void setRanges(final int min, final int max)
	{
		minRange = min;
		maxRange = max;
	}

	@Override
	public int[] getRanges()
	{
		return new int[] { minRange, maxRange };
	}

	@Override
	public boolean requiresAmmunition()
	{
		if((ammunitionType()==null)||(this instanceof Wand))
			return false;
		return ammunitionType().length()>0 && (ammunitionCapacity()>0);
	}

	@Override
	public void setAmmunitionType(final String ammo)
	{
		if(!(this instanceof Wand))
			setReadableText(ammo);
	}

	@Override
	public String ammunitionType()
	{
		return readableText();
	}

	@Override
	public int ammunitionRemaining()
	{
		return usesRemaining();
	}

	@Override
	public void setAmmoRemaining(int amount)
	{
		final int oldAmount=ammunitionRemaining();
		if(amount==Integer.MAX_VALUE)
			amount=20;
		setUsesRemaining(amount);
		if((oldAmount>0)
		&&(amount==0)
		&&(ammunitionCapacity()>0))
		{
			boolean recover=false;
			for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(!A.isSavable())&&(A.invoker()==null))
				{
					recover=true;
					delEffect(A);
				}
			}
			if(recover)
				recoverOwner();
		}
	}

	@Override
	public int ammunitionCapacity()
	{
		return ammoCapacity;
	}

	@Override
	public void setAmmoCapacity(final int amount)
	{
		ammoCapacity = amount;
	}

	@Override
	public int value()
	{
		if((subjectToWearAndTear())&&(usesRemaining()<1000))
			return (int)Math.round(CMath.mul(super.value(),CMath.div(usesRemaining(),100)));
		return super.value();
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return((!requiresAmmunition())
			&&(!(this instanceof Wand))
			&&(usesRemaining()<=1000)
			&&(usesRemaining()>=0));
	}

	public void recoverOwner()
	{
		final ItemPossessor myOwner=owner;
		if(myOwner instanceof MOB)
		{
			((MOB)myOwner).recoverCharStats();
			((MOB)myOwner).recoverMaxState();
			((MOB)myOwner).recoverPhyStats();
		}
		else
		if(myOwner!=null)
			myOwner.recoverPhyStats();
	}
}
