package com.planet_ink.coffee_mud.Items.BasicTech;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2021-2025 Bo Zimmerman

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
public class StdGun extends StdTechItem implements Weapon, AmmunitionWeapon
{
	@Override
	public String ID()
	{
		return "StdGun";
	}

	protected int		weaponDamageType		= Weapon.TYPE_SHOOT;
	protected int		weaponClassification	= Weapon.CLASS_RANGED;
	protected boolean	useExtendedMissString	= false;
	protected int		minRange				= 0;
	protected int		maxRange				= 10;
	protected int		ammoCapacity			= 0;
	protected long		lastReloadTime			= 0;

	public StdGun()
	{
		super();

		setName("a gun");
		setDisplayText("a gun sits here.");
		setDescription("");

		setRawLogicalAnd(false);
		setRawProperLocationBitmap(Wearable.WORN_HELD|Wearable.WORN_WIELD);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(20);
		basePhyStats().setLevel(1);
		setBaseValue(1500);
		setRanges(1,10);
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setAmmunitionType("bullets");
		setAmmoCapacity(10);
		setAmmoRemaining(10);
		recoverPhyStats();
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
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		final int timsLevel = CMLib.itemBuilder().timsLevelCalculator(this);
		if(phyStats().ability()>0)
			id=name()+" +"+phyStats().ability()+((id.length()>0)?"\n":"")+id;
		else
		if(phyStats().ability()<0)
			id=name()+" "+phyStats().ability()+((id.length()>0)?"\n":"")+id;
		if(timsLevel != phyStats.level())
			id += " (Power level: "+timsLevel+")";
		return id+L("\n\rAttack: @x1, Damage: @x2",""+phyStats().attackAdjustment(),""+phyStats().damage());
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(amWearingAt(Wearable.WORN_WIELD))
		{
			if(phyStats().attackAdjustment()!=0)
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(phyStats().attackAdjustment()));
			if(phyStats().damage()!=0)
				affectableStats.setDamage(affectableStats.damage()+phyStats().damage());
		}
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		final PhyStats phyStats = phyStats();
		if(phyStats.damage()!=0)
		{
			if(phyStats.ability() != 0)
			{
				final int ability=super.wornLogicalAnd ? (phyStats.ability()*CMath.numberOfSetBits(super.properWornBitmap)) : phyStats.ability();
				if(ability != 0)
				{
					phyStats.setDamage(phyStats.damage()+(ability*2));
					phyStats.setAttackAdjustment(phyStats.attackAdjustment()+(ability*10));
				}
			}
		}
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
						msg.source().tell(L("@x1 remaining: @x2/@x3.",ammunitionType(),""+ammunitionRemaining(),""+ammunitionCapacity()));
				}
				break;
			case CMMsg.TYP_RELOAD:
				if(msg.tool() instanceof Ammunition)
				{
					boolean recover=false;
					final Ammunition I=(Ammunition)msg.tool();
					final int ammoCapacity = ammunitionCapacity();
					int howMuchToTake=ammoCapacity;
					if(I.ammunitionRemaining()<howMuchToTake)
						howMuchToTake=I.ammunitionRemaining();
					if(ammoCapacity - this.ammunitionRemaining() < howMuchToTake)
						howMuchToTake=ammoCapacity - this.ammunitionRemaining();
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
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));
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
			{
				if(lastReloadTime != msg.source().lastTickedDateTime())
				{
					if(msg.source().isMonster() && (owner() instanceof Room))
						((Room)owner()).showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 is out of @x2.",name(),ammunitionType()));
					else
						msg.source().tell(L("@x1 is out of @x2.",name(),ammunitionType()));
					if((msg.source().isMine(this))
					&&(msg.source().location()!=null)
					&&(CMLib.flags().isAliveAwakeMobile(msg.source(),true)))
					{
						lastReloadTime=msg.source().lastTickedDateTime();
						final String name = CMStrings.replaceAll(name(), "\"", "\\\"");
						if((!msg.source().isMonster())||inventoryAmmoCheck(msg.source()))
							msg.source().enqueCommand(CMParms.parse("LOAD ALL \"$"+name+"$\""), 0, 0);
						else
							msg.source().enqueCommand(CMParms.parse("REMOVE \"$"+name+"$\""), 0, 0);
					}
				}
				return false;
			}
			else
				setUsesRemaining(usesRemaining()-1);
		}
		return true;
	}

	protected boolean inventoryAmmoCheck(final MOB M)
	{
		if(M==null)
			return false;
		for(int i=0;i<M.numItems();i++)
		{
			final Item I=M.getItem(i);
			if((I instanceof Ammunition)
			&&(((Ammunition)I).ammunitionType().equalsIgnoreCase(ammunitionType())))
				return true;
		}
		return false;
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
		final String message;
		switch(weaponClassification())
		{
		case Weapon.CLASS_AXE:
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED:
		case Weapon.CLASS_POLEARM:
		case Weapon.CLASS_SWORD:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEAPON_CONDITION_EDGED, ordinal);
			break;
		default:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.WEAPON_CONDITION_OTHER, ordinal);
			break;
		}
		return L(message,name(),""+usesRemaining());
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
		if(CMath.bset(phyStats().armor(),Weapon.MASK_MINRANGEFLAG))
			return (phyStats().armor()&Weapon.MASK_MINRANGEBITS)>>Weapon.MASK_MINRANGESHFT;
		return minRange;
	}

	@Override
	public int maxRange()
	{
		if(CMath.bset(phyStats().armor(),Weapon.MASK_MAXRANGEFLAG))
			return (phyStats().armor()&Weapon.MASK_MAXRANGEBITS)>>Weapon.MASK_MAXRANGESHFT;
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
		if(readableText()==null)
			return false;
		return readableText().length()>0;
	}

	@Override
	public boolean isFreeStanding()
	{
		return false;
	}

	@Override
	public void setAmmunitionType(final String ammo)
	{
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
		final ItemPossessor myOwner=owner;
		if((oldAmount>0)
		&&(amount==0)
		&&(myOwner instanceof MOB)
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
		if(CMath.bset(phyStats().armor(),Weapon.MASK_MOAMMOFLAG))
			return (phyStats().armor()&Weapon.MASK_MOAMMOBITS) >> Weapon.MASK_MOAMMOSHFT;
		return ammoCapacity;
	}

	@Override
	public int rawAmmunitionCapacity()
	{
		return ammoCapacity;
	}

	@Override
	public void setAmmoCapacity(final int amount)
	{
		ammoCapacity = amount;
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return false;
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
