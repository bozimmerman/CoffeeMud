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
		myUses=newUses;
	}
	
	private String armorHealth()
	{
		if(usesRemaining()>=100)
			return "";
		else
		if(usesRemaining()>=75)
		{
			switch(material())
			{
			case Item.CLOTH: return name()+" has a small tear ("+usesRemaining()+"%)";
			case Item.GLASS: return name()+" has a few hairline cracks ("+usesRemaining()+"%)";
			case Item.LEATHER: return name()+" is a bit scuffed ("+usesRemaining()+"%)";
			case Item.METAL:
			case Item.MITHRIL: return name()+" has some small dents ("+usesRemaining()+"%)";
			case Item.WOODEN: return name()+" has a few smell splinters ("+usesRemaining()+"%)";
			default: return name()+" is slightly damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=50)
		{
			switch(material())
			{
			case Item.CLOTH: return name()+" has a a few tears and rips ("+usesRemaining()+"%)";
			case Item.GLASS: return name()+" is cracked ("+usesRemaining()+"%)";
			case Item.LEATHER: return name()+" is torn ("+usesRemaining()+"%)";
			case Item.METAL:
			case Item.MITHRIL: return name()+" is dented ("+usesRemaining()+"%)";
			case Item.WOODEN: return name()+" is splintered ("+usesRemaining()+"%)";
			default: return name()+" is damaged ("+usesRemaining()+"%)";
			}
		}
		else
		if(usesRemaining()>=25)
		{
			switch(material())
			{
			case Item.CLOTH: return name()+" has numerous tears and rips ("+usesRemaining()+"%)";
			case Item.GLASS: return name()+" has numerous streaking cracks ("+usesRemaining()+"%)";
			case Item.LEATHER: return name()+" is badly torn up ("+usesRemaining()+"%)";
			case Item.METAL:
			case Item.MITHRIL: return name()+" is badly dented and cracked ("+usesRemaining()+"%)";
			case Item.WOODEN: return name()+" is badly cracked and splintered ("+usesRemaining()+"%)";
			default: return name()+" is badly damaged ("+usesRemaining()+"%)";
			}
		}
		else
		{
			switch(material())
			{
			case Item.CLOTH: return name()+" is a shredded mess ("+usesRemaining()+"%)";
			case Item.GLASS: return name()+" is practically shardes ("+usesRemaining()+"%)";
			case Item.LEATHER: return name()+" is badly shredded and ripped ("+usesRemaining()+"%)";
			case Item.METAL:
			case Item.MITHRIL: return name()+" is a crumpled mess ("+usesRemaining()+"%)";
			case Item.WOODEN: return name()+" is nothing but splinters ("+usesRemaining()+"%)";
			default: return name()+" is horribly damaged ("+usesRemaining()+"%)";
			}
		}
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		// lets do some damage!
		if((affect.amITarget(this))
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING)
		&&(usesRemaining()<100)
		&&(usesRemaining()>=0)
		&&(Sense.canBeSeenBy(this,affect.source())))
			affect.source().tell(armorHealth());
		else
		if((!amWearingAt(Item.INVENTORY))
		&&(myOwner()!=null)
		&&(myOwner() instanceof MOB)
		&&((!Sense.isABonusItems(this))||(Dice.rollPercentage()>envStats().level()*2))
		&&(affect.amITarget(myOwner())))
		{
			if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				Weapon tool=(Weapon)affect.tool();
				switch(material())
				{
				case Item.CLOTH:
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
				case Item.GLASS:
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
				case Item.LEATHER:
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
				case Item.MITHRIL:
					if(Dice.rollPercentage()==1)
						setUsesRemaining(usesRemaining()-1);
					break;
				case Item.METAL:
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
				case Item.WOODEN:
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
				if((material()==Item.METAL)
				||((material()==Item.MITHRIL)&&(Dice.rollPercentage()<25))
				||(material()==Item.WOODEN)&&(Dice.rollPercentage()<50))
					setUsesRemaining(usesRemaining()-5);
			}
			else
			if((affect.targetMinor()==Affect.TYP_FIRE)&&(!affect.wasModified()))
			{
				if((material()==Item.CLOTH)
				||((material()==Item.WOODEN)&&(Dice.rollPercentage()<15))
				||(material()==Item.LEATHER)&&(Dice.rollPercentage()<50))
					setUsesRemaining(usesRemaining()-10);
			}
			else
			if((affect.targetMinor()==Affect.TYP_WATER)&&(!affect.wasModified()))
			{
				if(material()==Item.METAL)
					setUsesRemaining(usesRemaining()-1);
			}
			
			if((usesRemaining()<=0)
			&&(myOwner()!=null)
			&&(myOwner() instanceof MOB))
			{
				setUsesRemaining(100);
				affect.addTrailerMsg(new FullMsg(((MOB)myOwner()),null,null,Affect.MSG_OK_VISUAL,name()+" is destroyed!!",Affect.NO_EFFECT,null,Affect.MSG_OK_VISUAL,name()+" being worn by <S-NAME> is destroyed!"));
				remove();
				destroyThis();
				((MOB)myOwner()).recoverEnvStats();
				((MOB)myOwner()).recoverCharStats();
				((MOB)myOwner()).recoverMaxState();
				((MOB)myOwner()).location().recoverRoomStats();
			}
		}
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
	public int value()
	{
		if(usesRemaining()<100)
			return (int)Math.round(Util.mul(super.value(),Util.div(usesRemaining(),100)));
		else 
			return super.value();
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
