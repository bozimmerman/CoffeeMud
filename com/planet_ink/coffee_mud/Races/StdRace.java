package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdRace implements Race
{
	public String ID(){	return "StdRace"; }
	public String name(){ return "StdRace"; }
	protected int practicesAtFirstLevel(){return 0;}
	protected int trainsAtFirstLevel(){return 0;}
	protected int shortestMale(){return 24;}
	protected int shortestFemale(){return 24;}
	protected int heightVariance(){return 5;}
	protected int lightestWeight(){return 60;}
	protected int weightVariance(){return 10;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unknown";}
	
	protected Weapon naturalWeapon=null;
	protected Vector naturalWeaponChoices=null;
	
	public boolean playerSelectable(){return false;}

	public Race copyOf()
	{
		try
		{
			StdRace E=(StdRace)this.clone();
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}
	
	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{

	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{

	}
	
	public boolean okAffect(MOB myChar, Affect affect)
	{
		
		if((affect.amISource(myChar))&&((affect.sourceCode()&Affect.MASK_GENERAL)==0))
		{
			switch(affect.sourceMinor())
			{
			case Affect.TYP_JUSTICE:
				if((affect.target()!=null)
				&&(affect.target() instanceof Item)
				&&(forbiddenWornBits()&Item.HELD)>0)
				{
					myChar.tell("Your anatomy prevents you from doing that.");
					return false;
				}
				break;
			case Affect.TYP_CLOSE:
			case Affect.TYP_DELICATE_HANDS_ACT:
			case Affect.TYP_DROP:
			case Affect.TYP_FILL:
			case Affect.TYP_GET:
			case Affect.TYP_GIVE:
			case Affect.TYP_HANDS:
			case Affect.TYP_LOCK:
			case Affect.TYP_OPEN:
			case Affect.TYP_PULL:
			case Affect.TYP_PUSH:
			case Affect.TYP_THROW:
			case Affect.TYP_PUT:
			case Affect.TYP_UNLOCK:
			case Affect.TYP_WRITE:
				if((forbiddenWornBits()&Item.HELD)>0)
				{
					myChar.tell("Your anatomy prevents you from doing that.");
					return false;
				}
				break;
			case Affect.TYP_HOLD:
			case Affect.TYP_WIELD:
				if((forbiddenWornBits()&Item.HELD)==0) 
					break;
			case Affect.TYP_WEAR:
				if((affect.target()!=null)
				&&(affect.target() instanceof Item)
				&&(!canWear((Item)affect.target())))
				{
					switch(affect.targetMinor())
					{
						case Affect.TYP_WEAR:
							affect.source().tell("You lack the anatomy to wear "+affect.target().name()+".");
							break;
						case Affect.TYP_HOLD:
							affect.source().tell("You lack the anatomy to hold "+affect.target().name()+".");
							break;
						case Affect.TYP_WIELD:
							affect.source().tell("You lack the anatomy to wield "+affect.target().name()+".");
							break;
					}
					return false;
				}
				break;
			case Affect.TYP_DRINK:
				if((forbiddenWornBits()&Item.HELD)>0)
				{
					if(affect.target()==null) return true;
					if(!myChar.isMine(affect.target())) return true;
					myChar.tell("You cannot drink from that.");
					return false;
				}
				break;
			}
		}
		else
		if((affect.amITarget(myChar))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&((forbiddenWornBits()&Item.HELD)>0))
		{
			affect.source().tell("You cannot give anything to the "+name()+".");
			return false;
		}
		return true;
	}

	public void affect(MOB myChar, Affect affect)
	{
	}
	public void wearOutfit(MOB mob, Armor s1, Armor s2, Armor p1)
	{
		if((s1!=null)&&(mob.fetchInventory(s1.ID())==null))
		{
			mob.addInventory(s1);
			if(!mob.amWearingSomethingHere(Item.ON_TORSO))
				s1.wearAt(Item.ON_TORSO);
		}
		if((p1!=null)&&(mob.fetchInventory(p1.ID())==null))
		{
			mob.addInventory(p1);
			if(!mob.amWearingSomethingHere(Item.ON_LEGS))
				p1.wearAt(Item.ON_LEGS);
		}
		if((s2!=null)&&(mob.fetchInventory(s2.ID())==null))
		{
			mob.addInventory(s2);
			if(!mob.amWearingSomethingHere(Item.ON_FEET))
				s2.wearAt(Item.ON_FEET);
		}
	}
	public String arriveStr()
	{
		return "arrives";
	}
	public String leaveStr()
	{
		return "leaves";
	}
	public void outfit(MOB mob)
	{
	}
	public void level(MOB mob)
	{
	}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			if(mob.baseEnvStats().level()<=1)
			{
				mob.setPractices(mob.getPractices()+practicesAtFirstLevel());
				mob.setTrains(mob.getTrains()+trainsAtFirstLevel());
			}
			setHeightWeight(mob.baseEnvStats(),(char)mob.baseCharStats().getStat(CharStats.GENDER));
		}
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
			naturalWeapon=(Weapon)CMClass.getWeapon("Natural");
		return naturalWeapon;
	}
	
	public String healthText(MOB mob)
	{
		return CommonStrings.standardMobCondition(mob);
	}
	
	public Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				switch(i)
				{
					case 1:
					case 2:
					case 3:
					naturalWeapon.setName("a quick punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 4:
					naturalWeapon.setName("fingernails and teeth");
					naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
					case 5:
					naturalWeapon.setName("an elbow");
					naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
					break;
					case 6:
					naturalWeapon.setName("a backhand");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 7:
					naturalWeapon.setName("a strong jab");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 8:
					naturalWeapon.setName("a stinging punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 9:
					naturalWeapon.setName("a knee");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 10:
					naturalWeapon.setName("a head butt");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				naturalWeaponChoices.addElement(naturalWeapon);
			}
		}
		return (Weapon)naturalWeaponChoices.elementAt(Dice.roll(1,naturalWeaponChoices.size(),0)-1);
	}
	public Vector myResources(){return new Vector();}
	public void setHeightWeight(EnvStats stats, char gender)
	{
		int weightModifier=0;
		if(weightVariance()>0)
			weightModifier=Dice.roll(1,weightVariance(),0);
		stats.setWeight(lightestWeight()+weightModifier);
		int heightModifier=0;
		if(heightVariance()>0)
			heightModifier=Dice.roll(1,heightVariance(),0);
		if (gender == 'M')
			stats.setHeight(shortestMale()+heightModifier);
 		else
			stats.setHeight(shortestFemale()+heightModifier);
	}
	
	public boolean canWear(Item item)
	{
		if((item.rawLogicalAnd())&&((item.rawProperLocationBitmap()&forbiddenWornBits())>0))
			return false;
		else
		if((!item.rawLogicalAnd())&&((item.rawProperLocationBitmap()&(Integer.MAX_VALUE-forbiddenWornBits()))==0))
			return false;
		return true;
	}
	
	protected Item makeResource(String name, int type)
	{
		Item I=null;
		if(((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH)
		||((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		I.setName(name);
		I.setDisplayText(name+" has been left here.");
		I.setDescription("It looks like "+name());
		I.setMaterial(type);
		I.setBaseValue(EnvResource.RESOURCE_DATA[type&EnvResource.RESOURCE_MASK][1]);
		I.baseEnvStats().setWeight(1);
		I.recoverEnvStats();
		return I;
	}

	public DeadBody getCorpse(MOB mob, Room room)
	{
		DeadBody Body=(DeadBody)CMClass.getItem("Corpse");
		Body.setCharStats(mob.baseCharStats().cloneCharStats());
		Body.baseEnvStats().setLevel(mob.baseEnvStats().level());
		Body.baseEnvStats().setWeight(mob.baseEnvStats().weight());
		if(!mob.isMonster())
			Body.baseEnvStats().setRejuv(Body.baseEnvStats().rejuv()*10);
		Body.setName("the body of "+mob.name());
		Body.setSecretIdentity(mob.name()+"/"+mob.description());
		Body.setDisplayText("the body of "+mob.name()+" lies here.");
		room.addItem(Body);
		Body.recoverEnvStats();
		Vector items=new Vector();
		for(int i=0;i<mob.inventorySize();)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)&&(thisItem.savable()))
			{
				if(mob.isMonster())
				{
					Item newItem=(Item)thisItem.copyOf();
					newItem.setContainer(null);
					newItem.setDispossessionTime(System.currentTimeMillis()+(Item.REFUSE_MONSTER_EQ*IQCalendar.MILI_HOUR));
					newItem.recoverEnvStats();
					thisItem=newItem;
					i++;
				}
				else
					mob.delInventory(thisItem);
				thisItem.remove();
				if(thisItem.container()==null)
					thisItem.setContainer(Body);
				room.addItem(thisItem);
				items.addElement(thisItem);
			}
			else
			if(thisItem!=null)
				mob.delInventory(thisItem);
			else
				i++;
		}
		if(mob.getMoney()>0)
		{
			Item C=(Item)CMClass.getItem("StdCoins");
			C.baseEnvStats().setAbility(mob.getMoney());
			C.recoverEnvStats();
			C.setContainer(Body);
			room.addItemRefuse(C,Item.REFUSE_MONSTER_EQ);
			mob.setMoney(0);
		}
		return Body;
	}
}
