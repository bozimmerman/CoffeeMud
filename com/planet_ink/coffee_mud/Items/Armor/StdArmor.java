package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;

public class StdArmor extends StdItem implements Armor
{
	public StdArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a shirt of armor";
		displayText="a thick armored shirt sits here.";
		description="Thick padded leather with strips of metal interwoven.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=150;
		setUsesRemaining(100);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		switch (Dice.roll(1,10,-1))
		{
			case 0:
				return new ChainMailArmor();
			case 1:
				return new FullPlate();
			case 2:
				return new LeatherArmor();
			case 3:
				return new BandedArmor();
			case 4:
				return new FieldPlate();
			default:
				return new StdArmor();

		}
	}
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}
	
	private String armorHealth()
	{
		if(usesRemaining()>=100)
			return "";
		else
		if(usesRemaining()>=75)
		{
			switch(material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_CLOTH: return name()+" has a small tear ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_GLASS: return name()+" has a few hairline cracks ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_LEATHER: return name()+" is a bit scuffed ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL: return name()+" has some small dents ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_WOODEN: return name()+" has a few smell splinters ("+usesRemaining()+"%)";
			default: return name()+" is slightly damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=50)
		{
			switch(material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_CLOTH: 
			case EnvResource.MATERIAL_PAPER: return name()+" has a a few tears and rips ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_GLASS: return name()+" is cracked ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_FLESH:
			case EnvResource.MATERIAL_LEATHER: return name()+" is torn ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL: return name()+" is dented ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_WOODEN: return name()+" is splintered ("+usesRemaining()+"%)";
			default: return name()+" is damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=25)
		{
			switch(material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_PAPER: 
			case EnvResource.MATERIAL_CLOTH: return name()+" has numerous tears and rips ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_GLASS: return name()+" has numerous streaking cracks ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_FLESH:
			case EnvResource.MATERIAL_LEATHER: return name()+" is badly torn up ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL: return name()+" is badly dented and cracked ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_WOODEN: return name()+" is badly cracked and splintered ("+usesRemaining()+"%)";
			default: return name()+" is badly damaged ("+usesRemaining()+"%)";
			}
		}
		else
		{
			switch(material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_PAPER: 
			case EnvResource.MATERIAL_CLOTH: return name()+" is a shredded mess ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_GLASS: return name()+" is practically shardes ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_FLESH:
			case EnvResource.MATERIAL_LEATHER: return name()+" is badly shredded and ripped ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL: return name()+" is a crumpled mess ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_WOODEN: return name()+" is nothing but splinters ("+usesRemaining()+"%)";
			default: return name()+" is horribly damaged ("+usesRemaining()+"%)";
			}
		}
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) 
			return false;
		if((affect.amITarget(this))
		&&(envStats().height()>0)
		&&(affect.targetMinor()==Affect.TYP_WEAR))
		{
			int devianceAllowed=200;
			if(((rawProperLocationBitmap()&Item.ON_TORSO)>0)
			||((rawProperLocationBitmap()&Item.ON_LEGS)>0)
			||((rawProperLocationBitmap()&Item.ON_WAIST)>0)
			||((rawProperLocationBitmap()&Item.ON_ARMS)>0)
			||((rawProperLocationBitmap()&Item.ON_HANDS)>0)
			||((rawProperLocationBitmap()&Item.ON_FEET)>0))
				devianceAllowed=20;
			if(affect.source().envStats().height()<(envStats().height()-devianceAllowed))
			{
				affect.source().tell(name()+" doesn't fit you -- it's too big.");
				return false;
			}
			if(affect.source().envStats().height()>(envStats().height()+devianceAllowed))
			{
				affect.source().tell(name()+" doesn't fit you -- it's too small.");
				return false;
			}
		}
		return true;
	}
	public void affect(Affect affect)
	{
		super.affect(affect);
		// lets do some damage!
		if((affect.amITarget(this))
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
		&&(subjectToWearAndTear())
		&&(usesRemaining()<100)
		&&(Sense.canBeSeenBy(this,affect.source())))
			affect.source().tell(armorHealth());
		else
		if((!amWearingAt(Item.INVENTORY))
		&&(owner()!=null)
		&&(owner() instanceof MOB)
		&&(affect.amITarget(owner()))
		&&((!Sense.isABonusItems(this))||(Dice.rollPercentage()>envStats().level()*2))
		&&(subjectToWearAndTear()))
		{
			if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				Weapon tool=(Weapon)affect.tool();
				switch(material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_CLOTH:
				case EnvResource.MATERIAL_PAPER:
					switch(tool.weaponType())
					{
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-15);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					}
					break;
				case EnvResource.MATERIAL_GLASS:
					switch(tool.weaponType())
					{
					case Weapon.TYPE_BURNING:
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<10)
							setUsesRemaining(usesRemaining()-10);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-15);
						break;
					}
					break;
				case EnvResource.MATERIAL_LEATHER:
					switch(tool.weaponType())
					{
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-15);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
						if(Dice.rollPercentage()<10)
							setUsesRemaining(usesRemaining()-4);
						break;
					case Weapon.TYPE_SLASHING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				case EnvResource.MATERIAL_MITHRIL:
					if(Dice.rollPercentage()==1)
						setUsesRemaining(usesRemaining()-1);
					break;
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_ROCK:
				case EnvResource.MATERIAL_PRECIOUS:
					switch(tool.weaponType())
					{
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(Dice.rollPercentage()<2)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				case EnvResource.MATERIAL_WOODEN:
					switch(tool.weaponType())
					{
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()<20)
							setUsesRemaining(usesRemaining()-4);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(Dice.rollPercentage()<2)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				default:
					if(Dice.rollPercentage()==1)
						setUsesRemaining(usesRemaining()-1);
					break;
				}
			}
			else
			if((affect.targetMinor()==Affect.TYP_ACID)&&(!affect.wasModified()))
			{
				switch(material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_ROCK:
				case EnvResource.MATERIAL_PRECIOUS:
				case EnvResource.MATERIAL_VEGETATION:
				case EnvResource.MATERIAL_FLESH:
					setUsesRemaining(usesRemaining()-5);
					break;
				case EnvResource.MATERIAL_MITHRIL:
					if(Dice.rollPercentage()<25)
						setUsesRemaining(usesRemaining()-5);
					break;
				case EnvResource.MATERIAL_WOODEN:
					if(Dice.rollPercentage()<50)
						setUsesRemaining(usesRemaining()-5);
					break;
				default:
					break;
				}
			}
			else
			if((affect.targetMinor()==Affect.TYP_FIRE)&&(!affect.wasModified()))
			{
				switch(material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_CLOTH:
				case EnvResource.MATERIAL_PAPER:
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_LEATHER:
					setUsesRemaining(usesRemaining()-10);
					break;
				default:
					break;
				}
			}
			else
			if((affect.targetMinor()==Affect.TYP_WATER)&&(!affect.wasModified()))
			{
				switch(material())
				{
				case EnvResource.RESOURCE_IRON:
				case EnvResource.RESOURCE_STEEL:
				case EnvResource.RESOURCE_PAPER:
					setUsesRemaining(usesRemaining()-1);
					break;
				default:
					break;
				}
			}
			
			if((usesRemaining()<=0)
			&&(owner()!=null)
			&&(owner() instanceof MOB))
			{
				MOB owner=(MOB)owner();
				setUsesRemaining(100);
				affect.addTrailerMsg(new FullMsg(((MOB)owner()),null,null,Affect.MSG_OK_VISUAL,name()+" is destroyed!!",Affect.NO_EFFECT,null,Affect.MSG_OK_VISUAL,name()+" being worn by <S-NAME> is destroyed!"));
				remove();
				destroyThis();
				owner.recoverEnvStats();
				owner.recoverCharStats();
				owner.recoverMaxState();
				owner.location().recoverRoomStats();
			}
		}
	}
	
	public void recoverEnvStats()
	{
		super.recoverEnvStats();
		if((baseEnvStats().height()==0)
		   &&(!amWearingAt(Item.INVENTORY))
		   &&(owner() instanceof MOB))
			baseEnvStats().setHeight(((MOB)owner()).baseEnvStats().height());
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&((!this.amWearingAt(Item.HELD))||(this instanceof Shield)))
		{
			affectableStats.setArmor(affectableStats.armor()-envStats().armor());
			if(this.amWearingAt(Item.ON_TORSO))
				affectableStats.setArmor(affectableStats.armor()-(envStats().ability()*10));
			else
			if((this.amWearingAt(Item.ON_HEAD))||(this.amWearingAt(Item.HELD)))
				affectableStats.setArmor(affectableStats.armor()-(envStats().ability()*5));
			else
				affectableStats.setArmor(affectableStats.armor()-envStats().ability());
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(!amWearingAt(Item.INVENTORY))
		switch(material()&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_METAL: 
			affectableStats.setStat(CharStats.SAVE_ELECTRIC,affectableStats.getStat(CharStats.SAVE_ELECTRIC)-2);
			break;
		case EnvResource.MATERIAL_LEATHER: 
			affectableStats.setStat(CharStats.SAVE_ACID,affectableStats.getStat(CharStats.SAVE_ACID)+2);
			break;
		case EnvResource.MATERIAL_MITHRIL: 
			affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+2);
			break;
		case EnvResource.MATERIAL_CLOTH: 
		case EnvResource.MATERIAL_PAPER:
			affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)-2);
			break;
		case EnvResource.MATERIAL_GLASS: 
		case EnvResource.MATERIAL_ROCK: 
		case EnvResource.MATERIAL_PRECIOUS: 
		case EnvResource.MATERIAL_VEGETATION: 
		case EnvResource.MATERIAL_FLESH:
			affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_ACID)+2);
			break;
		}
	}
	public int value()
	{
		if(usesRemaining()<1000)
			return (int)Math.round(Util.mul(super.value(),Util.div(usesRemaining(),100)));
		else 
			return super.value();
	}
	public boolean subjectToWearAndTear()
	{
		if((usesRemaining()<=1000)&&(usesRemaining()>=0))
			return true;
		return false;
	}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(envStats().ability()>0)
			id=name()+" +"+envStats().ability()+((id.length()>0)?"\n\r":"")+id;
		else
		if(envStats().ability()<0)
			id=name()+" "+envStats().ability()+((id.length()>0)?"\n\r":"")+id;
		return id+"\n\rProtection: "+envStats().armor();
	}
}
