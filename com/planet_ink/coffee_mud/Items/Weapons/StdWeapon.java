package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdWeapon extends StdItem implements Weapon
{
	protected int weaponType=TYPE_NATURAL;
	protected int weaponClassification=CLASS_NATURAL;
	protected boolean useExtendedMissString=false;
	protected int minRange=0;
	protected int maxRange=0;

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
		baseEnvStats().setDamage(0);
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
	public String description()
	{
		if(!requiresAmmunition())
			return super.description();
		else
			return super.description()+"\n\r"+ammunitionType()+" remaining: "+ammunitionRemaining()+"/"+ammunitionCapacity()+".";
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

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if((affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(affect.tool()==this)
		   &&(requiresAmmunition())
		   &&(ammunitionCapacity()>0))
		{
			if(ammunitionRemaining()>ammunitionCapacity())
				setAmmoRemaining(ammunitionCapacity());
			if(ammunitionRemaining()<=0)
			{
				boolean reLoaded=false;
				if((affect.source().isMine(this))
				   &&(affect.source().location()!=null)
				   &&(Sense.aliveAwakeMobile(affect.source(),true)))
				{
					MOB mob=affect.source();
					for(int i=0;i<mob.inventorySize();i++)
					{
						Item I=mob.fetchInventory(i);
						if(!((I instanceof Armor)
						   ||(I instanceof Container)
						   ||(I instanceof Weapon)
						   ||(I.usesRemaining()==0)
						   ||(I.usesRemaining()==Integer.MAX_VALUE)
						   ||(I.location()!=null)
						   ||(!I.amWearingAt(Item.INVENTORY))
						   ||(!I.rawSecretIdentity().equalsIgnoreCase(ammunitionType()))))
						{
							mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> get(s) "+I.name()+".");
							setAmmoRemaining(I.usesRemaining());
							I.setUsesRemaining(0);
							I.destroyThis();
							reLoaded=true;
						}
					}
				}
				if(!reLoaded)
				{
					setAmmoRemaining(0);
					affect.source().tell("You have no more "+ammunitionType()+".");
					ExternalPlay.remove(affect.source(),this);
					return false;
				}
			}
			else
				setUsesRemaining(usesRemaining()-1);
		}
		return true;
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
	
	public String missString()
	{
		return ExternalPlay.standardMissString(weaponType,weaponClassification,name(),useExtendedMissString);
	}
	public String hitString(int damageAmount)
	{
		return "<S-NAME> "+ExternalPlay.standardHitWord(weaponType,damageAmount)+" <T-NAMESELF> with "+name();
	}
	public int minRange(){return minRange;}
	public int maxRange(){return maxRange;}
	public void setRanges(int min, int max){minRange=min;maxRange=max;}
	public boolean requiresAmmunition(){return readableText().length()>0;};
	public void setAmmunitionType(String ammo){setReadableText(ammo);}
	public String ammunitionType(){return readableText();}
	public int ammunitionRemaining(){return usesRemaining();}
	public void setAmmoRemaining(int amount)
	{
		if(amount==Integer.MAX_VALUE)
			amount=20;
		setUsesRemaining(amount);
	}
	public int ammunitionCapacity(){return capacity();}
	public void setAmmoCapacity(int amount){setCapacity(amount);}
}
