package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdWeapon extends StdItem implements Weapon
{
	public String ID(){	return "StdWeapon";}
	protected int weaponType=TYPE_NATURAL;
	protected int weaponClassification=CLASS_NATURAL;
	protected boolean useExtendedMissString=false;
	protected int minRange=0;
	protected int maxRange=0;
	protected int ammoCapacity=0;

	public StdWeapon()
	{
		super();

		setName("weapon");
		setDisplayText(" sits here.");
		setDescription("This is a deadly looking weapon.");
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
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(envStats().attackAdjustment()+(envStats().ability()*10)));
			affectableStats.setDamage(affectableStats.damage()+(envStats().damage()+(envStats().ability()*2)));
		}
	}
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		if((subjectToWearAndTear())&&(usesRemaining()<100))
			envStats().setDamage(((int)Math.round(Util.mul(envStats().damage(),Util.div(usesRemaining(),100)))));
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(Sense.canBeSeenBy(this,msg.source())))
		{
			if(requiresAmmunition())
				msg.source().tell(ammunitionType()+" remaining: "+ammunitionRemaining()+"/"+ammunitionCapacity()+".");
			if((subjectToWearAndTear())&&(usesRemaining()<100))
				msg.source().tell(weaponHealth());
		}
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			msg.addTrailerMsg(new FullMsg(msg.source(),this,CMMsg.MSG_DROP,null));
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==this)
		&&(amWearingAt(Item.WIELD))
		&&(weaponClassification()!=Weapon.CLASS_NATURAL)
		&&(weaponType()!=Weapon.TYPE_NATURAL)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&((msg.value())>0)
		&&(owner()!=null)
		&&(owner() instanceof MOB)
		&&(msg.amISource((MOB)owner())))
		{
			int hurt=(msg.value());
			MOB tmob=(MOB)msg.target();
			if((hurt>(tmob.maxState().getHitPoints()/10)||(hurt>50))
			&&(tmob.curState().getHitPoints()>hurt))
			{
				if((!tmob.isMonster())
				   &&(Dice.rollPercentage()==1)
				   &&(Dice.rollPercentage()>(tmob.charStats().getStat(CharStats.CONSTITUTION)*4)))
				{
					Ability A=null;
					if(subjectToWearAndTear()
					&&(usesRemaining()<25)
					&&((material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
					{
						if(Dice.rollPercentage()>50)
							A=CMClass.getAbility("Disease_Lockjaw");
						else
							A=CMClass.getAbility("Disease_Tetanus");
					}
					else
						A=CMClass.getAbility("Disease_Infection");

					if((A!=null)&&(msg.target().fetchEffect(A.ID())==null))
						A.invoke(msg.source(),msg.target(),true);
				}
				else
				if((hurt>100)
				&&((this instanceof Electronics)
				   ||(hurt>(tmob.maxState().getHitPoints()/10))))
				{
					switch(weaponType())
					{
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					default:
						{
							Ability A=CMClass.getAbility("Amputation");
							if(A!=null) A.invoke(msg.source(),msg.target(),true);
						}
						break;
					}
				}
			}

			if((subjectToWearAndTear())
			&&(Dice.rollPercentage()<5)
			&&(msg.source().rangeToTarget()==0)
			&&((!Sense.isABonusItems(this))||(Dice.rollPercentage()>envStats().level()*5))
			&&((material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_ENERGY))
			{
				setUsesRemaining(usesRemaining()-1);
				if((usesRemaining()<=0)
				&&(owner()!=null)
				&&(owner() instanceof MOB))
				{
					MOB owner=(MOB)owner();
					setUsesRemaining(100);
					msg.addTrailerMsg(new FullMsg(((MOB)owner()),null,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" is destroyed!!^?",CMMsg.NO_EFFECT,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" being wielded by <S-NAME> is destroyed!^?"));
					unWear();
					destroy();
					owner.recoverEnvStats();
					owner.recoverCharStats();
					owner.recoverMaxState();
					owner.location().recoverRoomStats();
				}
			}
		}
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
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
				boolean reLoaded=false;
				if((msg.source().isMine(this))
				   &&(msg.source().location()!=null)
				   &&(Sense.aliveAwakeMobile(msg.source(),true)))
				{
					MOB mob=msg.source();
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
							mob.location().show(mob,null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> get(s) "+ammunitionType()+" from "+I.name()+".");
							int howMuchToTake=ammunitionCapacity();
							if(I.usesRemaining()<howMuchToTake)
								howMuchToTake=I.usesRemaining();
							setAmmoRemaining(howMuchToTake);
							I.setUsesRemaining(I.usesRemaining()-howMuchToTake);
							if(I.usesRemaining()<=0) I.destroy();
							reLoaded=true;
							break;
						}
					}
				}
				if(!reLoaded)
				{
					setAmmoRemaining(0);
					msg.source().tell("You have no more "+ammunitionType()+".");
					CommonMsgs.remove(msg.source(),this,false);
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
	public int minRange()
	{
		if(Util.bset(envStats().sensesMask(),EnvStats.SENSE_ITEMNOMINRANGE))
			return 0;
		else
			return minRange;
	}
	public int maxRange()
	{
		if(Util.bset(envStats().sensesMask(),EnvStats.SENSE_ITEMNOMAXRANGE))
			return 100;
		else
			return maxRange;
	}
	public void setRanges(int min, int max){minRange=min;maxRange=max;}
	public boolean requiresAmmunition()
	{
		if((readableText()==null)||(this instanceof Wand))
			return false;
		return readableText().length()>0;
	};
	public void setAmmunitionType(String ammo)
	{
		if(!(this instanceof Wand))
			setReadableText(ammo);
	}
	public String ammunitionType()
	{
		return readableText();
	}
	public int ammunitionRemaining()
	{
		return usesRemaining();
	}
	public void setAmmoRemaining(int amount)
	{
		if(amount==Integer.MAX_VALUE)
			amount=20;
		setUsesRemaining(amount);
	}
	public int ammunitionCapacity(){return ammoCapacity;}
	public void setAmmoCapacity(int amount){ammoCapacity=amount;}
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
