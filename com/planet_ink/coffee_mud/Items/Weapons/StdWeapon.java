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
		material=EnvResource.RESOURCE_STEEL;
		setUsesRemaining(100);
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
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		if((subjectToWearAndTear())&&(usesRemaining()<100))
			envStats().setDamage(((int)Math.round(Util.mul(envStats().damage(),Util.div(usesRemaining(),100)))));
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		
		if((affect.amITarget(this))
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
		&&(Sense.canBeSeenBy(this,affect.source())))
		{
			if(requiresAmmunition())
				affect.source().tell(ammunitionType()+" remaining: "+ammunitionRemaining()+"/"+ammunitionCapacity()+".");
			if((subjectToWearAndTear())&&(usesRemaining()<100))
				affect.source().tell(weaponHealth());
		}
		else
		if((affect.tool()==this)
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			affect.addTrailerMsg(new FullMsg(affect.source(),this,Affect.MSG_DROP,null));
		else
		if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool()==this)
		&&(subjectToWearAndTear())
		&&(Dice.rollPercentage()<10)
		&&(owner()!=null)
		&&(amWearingAt(Item.WIELD))
		&&(owner() instanceof MOB)
		&&(affect.amISource((MOB)owner()))
		&&((!Sense.isABonusItems(this))||(Dice.rollPercentage()>envStats().level()*4)))
		{
			setUsesRemaining(usesRemaining()-1);
			if((usesRemaining()<=0)
			&&(owner()!=null)
			&&(owner() instanceof MOB))
			{
				MOB owner=(MOB)owner();
				setUsesRemaining(100);
				affect.addTrailerMsg(new FullMsg(((MOB)owner()),null,null,Affect.MSG_OK_VISUAL,name()+" is destroyed!!",Affect.NO_EFFECT,null,Affect.MSG_OK_VISUAL,name()+" being wielded by <S-NAME> is destroyed!"));
				remove();
				destroyThis();
				owner.recoverEnvStats();
				owner.recoverCharStats();
				owner.recoverMaxState();
				owner.location().recoverRoomStats();
			}
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
						   ||(I.container()!=null)
						   ||(!I.rawSecretIdentity().equalsIgnoreCase(ammunitionType()))))
						{
							mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> get(s) "+ammunitionType()+" from "+I.name()+".");
							int howMuchToTake=ammunitionCapacity();
							if(I.usesRemaining()<howMuchToTake)
								howMuchToTake=I.usesRemaining();
							setAmmoRemaining(howMuchToTake);
							I.setUsesRemaining(I.usesRemaining()-howMuchToTake);
							if(I.usesRemaining()<=0) I.destroyThis();
							reLoaded=true;
							break;
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
	
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}
	
	private String weaponHealth()
	{
		if(usesRemaining()>=100)
			return "";
		else
		if(usesRemaining()>=95)
			return name()+" looks slightly used ("+usesRemaining()+"%)";
		else
		if(usesRemaining()>=85)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" is somewhat dull ("+usesRemaining()+"%)";
			default: 
				 return name()+" is somewhat worn ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=75)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" is dull ("+usesRemaining()+"%)";
			default: 
				 return name()+" is worn ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>50)
		{
			switch(weaponClassification())
			{
			case Weapon.CLASS_AXE:
			case Weapon.CLASS_DAGGER:
			case Weapon.CLASS_EDGED:
			case Weapon.CLASS_POLEARM:
			case Weapon.CLASS_SWORD:
				return name()+" has some notches and chinks ("+usesRemaining()+"%)";
			default: 
				return name()+" is damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>25)
			return name()+" is heavily damaged ("+usesRemaining()+"%)";
		else
			return name()+" is so damaged, it is practically harmless ("+usesRemaining()+"%)";
	}
	public String missString()
	{
		return CommonStrings.standardMissString(weaponType,weaponClassification,name(),useExtendedMissString);
	}
	public String hitString(int damageAmount)
	{
		return CommonStrings.standardHitString(weaponType,weaponClassification,damageAmount,name());
	}
	public int minRange(){return minRange;}
	public int maxRange(){return maxRange;}
	public void setRanges(int min, int max){minRange=min;maxRange=max;}
	public boolean requiresAmmunition(){if(readableText()!=null) return readableText().length()>0;return false;};
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
	public int value()
	{
		if((subjectToWearAndTear())&&(usesRemaining()<1000))
			return (int)Math.round(Util.mul(super.value(),Util.div(usesRemaining(),100)));
		else 
			return super.value();
	}
	public boolean subjectToWearAndTear()
	{
		return((!requiresAmmunition())
			&&(!(this instanceof Wand))
			&&(usesRemaining()<=1000)
			&&(usesRemaining()>=0));
	}
	
}
