package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Dice;
import java.util.*;

public class StdRace implements Race
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="MOB";
	protected int practicesAtFirstLevel=0;
	protected int trainsAtFirstLevel=0;
	protected Weapon naturalWeapon=null;
	protected Vector naturalWeaponChoices=null;
	
	protected int shortestMale=24;
	protected int shortestFemale=24;
	protected int heightVariance=5;
	protected int lightestWeight=60;
	protected int weightVariance=10;
	protected long forbiddenWornBits=0;
	
	public String ID()
	{
		return myID;
	}
	public String name()
	{
		return name;
	}
	public boolean playerSelectable(){return false;}

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
		if((forbiddenWornBits&Item.HELD)>0)
		{
			if((affect.amISource(myChar))&&((affect.sourceCode()&Affect.ACT_GENERAL)==0))
			{
				switch(affect.sourceMinor())
				{
				case Affect.TYP_CLOSE:
				case Affect.TYP_DELICATE_HANDS_ACT:
				case Affect.TYP_DROP:
				case Affect.TYP_FILL:
				case Affect.TYP_GET:
				case Affect.TYP_GIVE:
				case Affect.TYP_HANDS:
				case Affect.TYP_HOLD:
				case Affect.TYP_LOCK:
				case Affect.TYP_OPEN:
				case Affect.TYP_PULL:
				case Affect.TYP_PUSH:
				case Affect.TYP_PUT:
				case Affect.TYP_UNLOCK:
				case Affect.TYP_WEAR:
				case Affect.TYP_WIELD:
				case Affect.TYP_WRITE:
					myChar.tell("Your anatomy prevents you from doing that.");
					return false;
				case Affect.TYP_DRINK:
					if(affect.tool()==null) return true;
					if(!myChar.isMine(affect.tool())) return true;
					myChar.tell("You cannot drink from that.");
					return false;
				}
			}
			else
			if(affect.amITarget(myChar))
			{
				switch(affect.targetMinor())
				{
				case Affect.TYP_GIVE:
					affect.source().tell("You cannot give anything to the "+name()+".");
					return false;
				}
			}
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
				mob.setPractices(mob.getPractices()+practicesAtFirstLevel);
				mob.setTrains(mob.getTrains()+trainsAtFirstLevel);
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
		if(weightVariance>0)
			weightModifier=Dice.roll(1,weightVariance,0);
		stats.setWeight(lightestWeight+weightModifier);
		int heightModifier=0;
		if(heightVariance>0)
			heightModifier=Dice.roll(1,heightVariance,0);
		if (gender == 'M')
			stats.setHeight(shortestMale+heightModifier);
 		else
			stats.setHeight(shortestFemale+heightModifier);
	}
	
	public void confirmGear(MOB mob)
	{
		if(mob==null) return;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item!=null)&&(!item.amWearingAt(Item.INVENTORY)))
			{
				long oldCode=item.rawWornCode();
				item.remove();
				int msgCode=Affect.MSG_WEAR;
				if((oldCode&Item.WIELD)>0)
					msgCode=Affect.MSG_WIELD;
				else
				if((oldCode&Item.HELD)>0)
					msgCode=Affect.MSG_HOLD;
				FullMsg msg=new FullMsg(mob,item,null,Affect.NO_EFFECT,null,msgCode,null,Affect.NO_EFFECT,null);
				if(item.okAffect(msg)) item.wearAt(oldCode);
			}
		}
	}
	
	public boolean canWear(Item item)
	{
		if((item.rawLogicalAnd())&&((item.rawProperLocationBitmap()&forbiddenWornBits)>0))
			return false;
		else
		if((!item.rawLogicalAnd())&&((item.rawProperLocationBitmap()&(Integer.MAX_VALUE-forbiddenWornBits))==0))
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
		I.setDescription("It looks like "+name);
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
					newItem.setPossessionTime(Calendar.getInstance());
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
			room.addItemRefuse(C);
			mob.setMoney(0);
		}
		return Body;
	}
}
