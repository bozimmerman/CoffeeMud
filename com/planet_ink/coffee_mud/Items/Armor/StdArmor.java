package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdContainer;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class StdArmor extends StdContainer implements Armor
{
	public String ID(){	return "StdArmor";}
	int sheath=0;
	public StdArmor()
	{
		super();

		setName("a shirt of armor");
		setDisplayText("a thick armored shirt sits here.");
		setDescription("Thick padded leather with strips of metal interwoven.");
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=150;
		setCapacity(0);
		setLidsNLocks(false,true,false,false);
		setUsesRemaining(100);
		recoverEnvStats();
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
			case EnvResource.MATERIAL_PLASTIC:
			case EnvResource.MATERIAL_GLASS: return name()+" has a few hairline cracks ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_LEATHER: return name()+" is a bit scuffed ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL: return name()+" has some small dents ("+usesRemaining()+"%)";
			case EnvResource.MATERIAL_WOODEN: return name()+" has a few small splinters ("+usesRemaining()+"%)";
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
			case EnvResource.MATERIAL_PLASTIC:
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
			case EnvResource.MATERIAL_PLASTIC:
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
			case EnvResource.MATERIAL_PLASTIC:
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

	public final static long strangeDeviations=Item.FLOATING_NEARBY|Item.ON_MOUTH|Item.ON_EYES|Item.ON_EARS|Item.ON_NECK;
	public final static long deviation20=Item.ON_TORSO|Item.ON_LEGS|Item.ON_WAIST|Item.ON_ARMS|Item.ON_HANDS|Item.ON_FEET;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.amITarget(this))
		&&(envStats().height()>0)
		&&(msg.source().envStats().height()>0)
		&&(msg.targetMinor()==CMMsg.TYP_WEAR))
		{
			int devianceAllowed=200;
			if((rawProperLocationBitmap()&deviation20)>0)
				devianceAllowed=20;
			else
			if((rawProperLocationBitmap()&strangeDeviations)>0)
			{
				long wcode=rawProperLocationBitmap();

				if(Util.bset(wcode,Item.HELD))
					wcode=wcode-Item.HELD;
				if(wcode==Item.FLOATING_NEARBY) devianceAllowed=-1;
				else
				if(wcode==Item.ON_MOUTH) devianceAllowed=-1;
				else
				if(wcode==Item.ON_EYES) devianceAllowed=1000;
				else
				if(wcode==Item.ON_EARS) devianceAllowed=1000;
				else
				if(wcode==Item.ON_NECK) devianceAllowed=5000;
			}
			if(devianceAllowed>0)
			{
				if(msg.source().envStats().height()<(envStats().height()-devianceAllowed))
				{
					msg.source().tell(name()+" doesn't fit you -- it's too big.");
					return false;
				}
				if(msg.source().envStats().height()>(envStats().height()+devianceAllowed))
				{
					msg.source().tell(name()+" doesn't fit you -- it's too small.");
					return false;
				}
			}
		}
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// lets do some damage!
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(subjectToWearAndTear())
		&&(usesRemaining()<100)
		&&(Sense.canBeSeenBy(this,msg.source())))
			msg.source().tell(armorHealth());
		else
		if((!amWearingAt(Item.INVENTORY))
		&&(owner()!=null)
		&&(owner() instanceof MOB)
		&&(msg.amITarget(owner()))
		&&(Dice.rollPercentage()>((envStats().level()/2)+(10*envStats().ability())+(Sense.isABonusItems(this)?20:0)))
		&&(subjectToWearAndTear())
		&&(Dice.rollPercentage()>(((MOB)owner()).charStats().getStat(CharStats.DEXTERITY))))
		{
			int weaponType=-1;
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&((msg.value())>0))
			{
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Weapon))
				   weaponType=((Weapon)msg.tool()).weaponType();
				else
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_FIRE:
					weaponType=Weapon.TYPE_BURNING;
					break;
				case CMMsg.TYP_WATER:
					weaponType=Weapon.TYPE_FROSTING;
					break;
				case CMMsg.TYP_ACID:
					weaponType=Weapon.TYPE_MELTING;
					break;
				case CMMsg.TYP_COLD:
					weaponType=Weapon.TYPE_FROSTING;
					break;
				case CMMsg.TYP_GAS:
					weaponType=Weapon.TYPE_GASSING;
					break;
				case CMMsg.TYP_ELECTRIC:
					weaponType=Weapon.TYPE_STRIKING;
					break;
				}
			}
			int oldUses=usesRemaining();
			if(weaponType>=0)
			{
				switch(material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_CLOTH:
				case EnvResource.MATERIAL_PAPER:
					switch(weaponType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_STRIKING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-Dice.roll(1,5,0));
						break;
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-Dice.roll(1,15,0));
						break;
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
					switch(weaponType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
						break;
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_FROSTING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-Dice.roll(1,20,0));
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<10)
							setUsesRemaining(usesRemaining()-Dice.roll(1,10,0));
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-Dice.roll(1,15,0));
						break;
					}
					break;
				case EnvResource.MATERIAL_LEATHER:
					switch(weaponType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_STRIKING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-Dice.roll(1,5,0));
						break;
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-Dice.roll(1,15,0));
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
						if(Dice.rollPercentage()<10)
							setUsesRemaining(usesRemaining()-Dice.roll(1,4,0));
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
					switch(weaponType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_MELTING:
						if(Dice.rollPercentage()<25)
							setUsesRemaining(usesRemaining()-Dice.roll(1,15,0));
						break;
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
						if((rawWornCode()==Armor.ON_HEAD)
						&&(Dice.rollPercentage()==1)
						&&(Dice.rollPercentage()==1)
						&&((msg.value())>10))
						{
							Ability A=CMClass.getAbility("Disease_Tinnitus");
							if((A!=null)&&(owner().fetchEffect(A.ID())==null))
								A.invoke((MOB)owner(),owner(),true,0);
						}
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_STRIKING:
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
				case EnvResource.MATERIAL_PLASTIC:
					switch(weaponType)
					{
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_MELTING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-Dice.roll(1,10,0));
						break;
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_NATURAL:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-Dice.roll(1,4,0));
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-Dice.roll(1,8,0));
						break;
					case Weapon.TYPE_SLASHING:
						break;
					}
					break;
				case EnvResource.MATERIAL_ROCK:
				case EnvResource.MATERIAL_PRECIOUS:
					switch(weaponType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_MELTING:
						if(Dice.rollPercentage()<5)
							setUsesRemaining(usesRemaining()-Dice.roll(1,5,0));
						break;
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_STRIKING:
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
					switch(weaponType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_STRIKING:
						if(Dice.rollPercentage()<20)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
						if(Dice.rollPercentage()<20)
							setUsesRemaining(usesRemaining()-Dice.roll(1,5,0));
						break;
					case Weapon.TYPE_BASHING:
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
				case EnvResource.MATERIAL_ENERGY:
					break;
				default:
					if(Dice.rollPercentage()==1)
						setUsesRemaining(usesRemaining()-1);
					break;
				}
			}

			if(oldUses!=usesRemaining())
				recoverEnvStats();

			if((usesRemaining()<10)
			&&(oldUses!=usesRemaining())
			&&(owner()!=null)
			&&(owner() instanceof MOB)
			&&(usesRemaining()>0))
				((MOB)owner()).tell(name()+" is nearly destroyed! ("+usesRemaining()+"%)");
			else
			if((usesRemaining()<=0)
			&&(owner()!=null)
			&&(owner() instanceof MOB))
			{
				MOB owner=(MOB)owner();
				setUsesRemaining(100);
				msg.addTrailerMsg(new FullMsg(((MOB)owner()),null,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" is destroyed!!^?",CMMsg.NO_EFFECT,null,CMMsg.MSG_OK_VISUAL,"^I"+name()+" being worn by <S-NAME> is destroyed!^?"));
				unWear();
				destroy();
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
			if(amWearingAt(Item.ON_TORSO))
				affectableStats.setArmor(affectableStats.armor()-(envStats().ability()*10));
			else
			if((amWearingAt(Item.ON_HEAD))||(this.amWearingAt(Item.HELD)))
				affectableStats.setArmor(affectableStats.armor()-(envStats().ability()*5));
			else
			if(!amWearingAt(Item.FLOATING_NEARBY))
				affectableStats.setArmor(affectableStats.armor()-envStats().ability());
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
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
		case EnvResource.MATERIAL_PLASTIC:
			affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+2);
			break;
		case EnvResource.MATERIAL_ENERGY:
			affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+2);
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
