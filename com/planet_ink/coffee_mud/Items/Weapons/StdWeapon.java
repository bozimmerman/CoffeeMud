package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdWeapon extends StdItem implements Weapon
{
	protected int weaponType=TYPE_NATURAL;
	protected int weaponClassification=CLASS_NATURAL;

	public StdWeapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="weapon";
		displayText=" sits here.";
		description="This is a deadly looking weapon.";
		wornLogicalAnd=false;
		properWornBitmap=Item.HELD|Item.WIELD;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=15;
		material=Item.METAL;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdWeapon();
	}

	public int weaponType(){return weaponType;}
	public int weaponClassification(){return weaponClassification;}
	public void setWeaponType(int newType){weaponType=newType;}
	public void setWeaponClassification(int newClassification){weaponClassification=newClassification;}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(envStats().ability()>0)
			id=name()+" +"+envStats().ability()+((id.length()>0)?"\n":"")+id;
		else
		if(envStats().ability()<0)
			id=name()+" "+envStats().ability()+((id.length()>0)?"\n":"")+id;
		return id+"\n\rAttack: "+envStats().attackAdjustment()+", Damage: "+envStats().damage();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(amWearingAt(Item.WIELD))
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(envStats().attackAdjustment()+(envStats().ability()*5)));
			affectableStats.setDamage(affectableStats.damage()+(envStats().damage()*(envStats().ability()+1)));
		}
	}

	public String typeDescription()
	{
		switch(weaponType)
		{
		case TYPE_NATURAL:
			return "NATURAL";
		case TYPE_SLASHING:
			return "SLASHING";
		case TYPE_PIERCING:
			return "PIERCING";
		case TYPE_BASHING:
			return "BASHING";
		case TYPE_SHOOT:
			return "SHOOTING";
		case TYPE_BURSTING:
			return "BURSTING";
		case TYPE_BURNING:
			return "BURNING";
		}
		return "";
	}

	public String classifictionDescription()
	{
		switch(weaponClassification)
		{
		case CLASS_AXE:
			return "AXE";
		case CLASS_BLUNT:
			return "BLUNT";
		case CLASS_EDGED:
			return "EDGED";
		case CLASS_FLAILED:
			return "FLAILED";
		case CLASS_HAMMER:
			return "HAMMER";
		case CLASS_NATURAL:
			return "KARATE";
		case CLASS_POLEARM:
			return "POLEARM";
		case CLASS_RANGED:
			return "RANGED";
		case CLASS_SWORD:
			return "SWORD";
		case CLASS_DAGGER:
			return "DAGGER";
		case CLASS_STAFF:
			return "STAFF";
		}
		return "";
	}
}
