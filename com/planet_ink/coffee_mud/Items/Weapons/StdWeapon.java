package com.planet_ink.coffee_mud.Items.Weapons;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class StdWeapon extends StdItem implements Weapon, AmmunitionWeapon
{
	@Override
	public String ID()
	{
		return "StdWeapon";
	}

	protected int		weaponDamageType		= TYPE_NATURAL;
	protected int		weaponClassification	= CLASS_NATURAL;
	protected boolean	useExtendedMissString	= false;
	protected int[]		ranges					= new int[] { 0, 0 };
	protected int		ammoCapacity			= 0;
	protected long		lastReloadTime			= 0;

	public StdWeapon()
	{
		super();

		setName("weapon");
		setDisplayText(" sits here.");
		setDescription("This is a deadly looking weapon.");
		wornLogicalAnd=false;
		properWornBitmap=Wearable.WORN_HELD|Wearable.WORN_WIELD;
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		basePhyStats().setAbility(0);
		baseGoldValue=15;
		material=RawMaterial.RESOURCE_STEEL;
		setUsesRemaining(100);
		recoverPhyStats();
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return CMLib.english().startWithAorAn(Weapon.CLASS_DESCS[weaponClassification()].toLowerCase());
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
		return true;
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		final PhyStats phyStats = phyStats();
		if(phyStats.ability() != 0)
		{
			final int ability = phyStats.ability();
			if(phyStats.damage()!=0)
			{
				phyStats.setDamage(phyStats.damage()+ability);
				phyStats.setAttackAdjustment(phyStats.attackAdjustment()+(ability*10));
			}
			if(abilityImbuesMagic())
				phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_BONUS);
		}
		if(this instanceof MiscMagic)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_BONUS);
		if(subjectToWearAndTear() && (usesRemaining()<100) && (phyStats().damage()>0))
			phyStats.setDamage(((int)Math.round(CMath.mul(phyStats.damage(),CMath.div(usesRemaining(),100)))));
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
					{
						msg.source().tell(L("@x1 remaining: @x2/@x3.",
										ammunitionType(),
										""+ammunitionRemaining(),
										""+ammunitionCapacity()));
					}
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
			}
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));

		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==this)
		&&(amWearingAt(Wearable.WORN_WIELD))
		&&(weaponClassification()!=Weapon.CLASS_NATURAL)
		&&(weaponDamageType()!=Weapon.TYPE_NATURAL)
		&&(msg.target() instanceof MOB)
		&&((msg.value())>0)
		&&(owner() instanceof MOB)
		&&(msg.amISource((MOB)owner())))
		{
			final MOB ownerM=(MOB)owner();
			final int hurt=(msg.value());
			final MOB tmob=(MOB)msg.target();
			if((hurt>(tmob.maxState().getHitPoints()/10)||(hurt>50))
			&&(tmob.curState().getHitPoints()>hurt))
			{
				if((!tmob.isMonster())
				   &&(CMLib.dice().rollPercentage()==1)
				   &&(CMLib.dice().rollPercentage()>(tmob.charStats().getStat(CharStats.STAT_CONSTITUTION)*4))
				   &&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					Ability A=null;
					if(subjectToWearAndTear()
					&&(usesRemaining()<25)
					&&((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
					{
						if(CMLib.dice().rollPercentage()>50)
							A=CMClass.getAbility("Disease_Lockjaw");
						else
							A=CMClass.getAbility("Disease_Tetanus");
						if((A!=null)&&(tmob.fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							A.invoke(msg.source(),tmob,true,phyStats().level());
					}
				}
			}

			if((subjectToWearAndTear())
			&&(CMLib.dice().rollPercentage()==1)
			&&(msg.source().rangeToTarget()==0)
			&&(CMLib.dice().rollPercentage()>((phyStats().level()/2)+(10*phyStats().ability())+(CMLib.flags().isABonusItems(this)?20:0)))
			&&((material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ENERGY)
			&&((material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GAS))
			{
				CMLib.combat().postItemDamage(ownerM, this, null, 1, CMMsg.TYP_JUSTICE, null);
			}
		}
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
					final Room R = msg.source().location();
					if((msg.source().isMine(this))
					&&(R!=null)
					&&(CMLib.flags().isAliveAwakeMobile(msg.source(),true)))
					{
						lastReloadTime=msg.source().lastTickedDateTime();
						final CMMsg msg2 = CMClass.getMsg(msg.source(), this, null, CMMsg.MSG_NEEDRELOAD, null, CMMsg.MSG_NEEDRELOAD, null, CMMsg.NO_EFFECT, null);
						if(R.okMessage(msg.source(), msg2))
							R.send(msg.source(),msg2);
					}
				}
				return false;
			}
			else
				setUsesRemaining(usesRemaining()-1);
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
		return ranges[0];
	}

	@Override
	public int maxRange()
	{
		if(CMath.bset(phyStats().armor(),Weapon.MASK_MAXRANGEFLAG))
			return (phyStats().armor()&Weapon.MASK_MAXRANGEBITS)>>Weapon.MASK_MAXRANGESHFT;
		return ranges[1];
	}

	@Override
	public void setRanges(final int min, final int max)
	{
		ranges = new int[] {min, max};
	}

	@Override
	public int[] getRanges()
	{
		return ranges;
	}

	@Override
	public boolean requiresAmmunition()
	{
		if((readableText()==null)||(this instanceof Wand))
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
