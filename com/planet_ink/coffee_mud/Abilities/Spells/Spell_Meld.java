package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Meld extends Spell
{
	public String ID() { return "Spell_Meld"; }
	public String name(){return "Meld";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Meld();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean shinBone(Item one, Item two, long locationOne, long locationTwo)
	{
		if((one.canBeWornAt(locationOne)&&two.canBeWornAt(locationTwo))
		   &&(!one.canBeWornAt(locationTwo))
		   &&(!two.canBeWornAt(locationOne)))
			return true;
		else
		if((two.canBeWornAt(locationOne)&&one.canBeWornAt(locationTwo))
		   &&(!two.canBeWornAt(locationTwo))
		   &&(!one.canBeWornAt(locationOne)))
			return true;
		return false;
	}
	int[] heiarchy={EnvResource.MATERIAL_FLESH,
					EnvResource.MATERIAL_PAPER,
					EnvResource.MATERIAL_CLOTH,
					EnvResource.MATERIAL_LEATHER,
					EnvResource.MATERIAL_VEGETATION,
					EnvResource.MATERIAL_WOODEN,
					EnvResource.MATERIAL_METAL,
					EnvResource.MATERIAL_ROCK,
					EnvResource.MATERIAL_PRECIOUS,
					EnvResource.MATERIAL_MITHRIL,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// add something to disable traps
		//
		if(commands.size()<2)
		{
			mob.tell("Meld what and what else together?");
			return false;
		}
		Item itemOne=mob.fetchInventory((String)commands.elementAt(0));
		if((itemOne==null)||((itemOne!=null)&&(!Sense.canBeSeenBy(itemOne,mob))))
		{
			mob.tell("You don't seem to have a '"+((String)commands.elementAt(0))+"'.");
			return false;
		}
		Item itemTwo=mob.fetchInventory(Util.combine(commands,1));
		if((itemTwo==null)||((itemTwo!=null)&&(!Sense.canBeSeenBy(itemTwo,mob))))
		{
			mob.tell("You don't seem to have a '"+Util.combine(commands,1)+"'.");
			return false;
		}

		Environmental melded=null;
		
		if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
		{
			if(shinBone(itemOne,itemTwo,Item.ON_HEAD,Item.ON_NECK)
			   ||shinBone(itemOne,itemTwo,Item.ON_HEAD,Item.ON_EARS)
			   ||shinBone(itemOne,itemTwo,Item.ON_HEAD,Item.ON_EYES)
			   ||shinBone(itemOne,itemTwo,Item.ON_HEAD,Item.ON_TORSO)
			   ||shinBone(itemOne,itemTwo,Item.ON_NECK,Item.ON_TORSO)
			   ||shinBone(itemOne,itemTwo,Item.ON_TORSO,Item.ON_ARMS)
			   ||shinBone(itemOne,itemTwo,Item.ON_TORSO,Item.ON_WAIST)
			   ||shinBone(itemOne,itemTwo,Item.ON_WAIST,Item.ON_LEGS)
			   ||shinBone(itemOne,itemTwo,Item.ON_ARMS,Item.ON_LEFT_WRIST)
			   ||shinBone(itemOne,itemTwo,Item.ON_ARMS,Item.ON_HANDS)
			   ||shinBone(itemOne,itemTwo,Item.ON_HANDS,Item.ON_LEFT_WRIST)
			   ||shinBone(itemOne,itemTwo,Item.ON_HANDS,Item.ON_RIGHT_FINGER)
			   ||shinBone(itemOne,itemTwo,Item.ON_TORSO,Item.ON_LEGS)
			   ||shinBone(itemOne,itemTwo,Item.ON_LEGS,Item.ON_FEET))
			{

			}
			else
			{
				mob.tell(itemOne.displayName()+" and "+itemTwo.displayName()+" aren't worn in compatible places, and thus can't be melded.");
				return false;
			}
		}
		else
		if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
		{
			if(!itemOne.canBeWornAt(Item.HELD))
			{
				mob.tell(itemOne.displayName()+" can't be held, and thus can't be melded with "+itemTwo.displayName()+".");
				return false;
			}
			if(!itemTwo.canBeWornAt(Item.HELD))
			{
				mob.tell(itemTwo.displayName()+" can't be held, and thus can't be melded with "+itemOne.displayName()+".");
				return false;
			}
			if(itemOne.rawLogicalAnd())
			{
				mob.tell(itemOne.displayName()+" is two handed, and thus can't be melded with "+itemTwo.displayName()+".");
				return false;
			}
			if(itemTwo.rawLogicalAnd())
			{
				mob.tell(itemTwo.displayName()+" is two handed, and thus can't be melded with "+itemOne.displayName()+".");
				return false;
			}
		}
		else
		if((itemOne instanceof Container)&&(itemTwo instanceof Container))
		{

		}
		else
		{
			mob.tell("You can't meld those together.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> meld(s) "+itemOne.displayName()+" and "+itemTwo.displayName()+".^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);

				String itemOneName=itemOne.name();
				String itemTwoName=itemTwo.name();
				int x=itemOneName.indexOf("melded together");
				if(x>0) itemOneName=itemOneName.substring(0,x).trim();
				x=itemTwoName.indexOf("melded together");
				if(x>0) itemTwoName=itemTwoName.substring(0,x).trim();

				String newName=itemOneName+" and "+itemTwoName+" melded together";
				if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
				{
					int material=((Armor)itemOne).material();
					if(heiarchy[material&EnvResource.MATERIAL_MASK]<heiarchy[((Armor)itemTwo).material()&EnvResource.MATERIAL_MASK])
						material=((Armor)itemTwo).material();

					long wornLocation=itemOne.rawProperLocationBitmap()|itemTwo.rawProperLocationBitmap();
					if((wornLocation&Item.HELD)==(Item.HELD))
						wornLocation-=Item.HELD;
					if(((wornLocation&Item.ON_LEFT_FINGER)==(Item.ON_LEFT_FINGER))
					   &&((wornLocation&Item.ON_RIGHT_FINGER)==(Item.ON_RIGHT_FINGER)))
					{
						if(((wornLocation&Item.ON_LEFT_WRIST)==(Item.ON_LEFT_WRIST))
						&&((wornLocation&Item.ON_RIGHT_WRIST)==0))
						   wornLocation-=Item.ON_RIGHT_FINGER;
						else
						if(((wornLocation&Item.ON_RIGHT_WRIST)==(Item.ON_RIGHT_WRIST))
						&&((wornLocation&Item.ON_LEFT_WRIST)==0))
						   wornLocation-=Item.ON_LEFT_FINGER;
						else
						{
							if(Dice.rollPercentage()>50)
								wornLocation-=Item.ON_RIGHT_FINGER;
							else
								wornLocation-=Item.ON_LEFT_FINGER;
						}
					}

					if(((wornLocation&Item.ON_LEFT_WRIST)==(Item.ON_LEFT_WRIST))
					   &&((wornLocation&Item.ON_RIGHT_WRIST)==(Item.ON_RIGHT_WRIST)))
					{
						if(((wornLocation&Item.ON_LEFT_FINGER)==(Item.ON_LEFT_FINGER))
						&&((wornLocation&Item.ON_RIGHT_FINGER)==0))
						   wornLocation-=Item.ON_RIGHT_WRIST;
						else
						if(((wornLocation&Item.ON_RIGHT_FINGER)==(Item.ON_RIGHT_FINGER))
						&&((wornLocation&Item.ON_LEFT_FINGER)==0))
						   wornLocation-=Item.ON_LEFT_WRIST;
						else
						{
							if(Dice.rollPercentage()>50)
								wornLocation-=Item.ON_RIGHT_WRIST;
							else
								wornLocation-=Item.ON_LEFT_WRIST;
						}
					}


					Armor gc=(Armor)CMClass.getArmor("GenArmor");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.baseEnvStats().setWeight(itemOne.baseEnvStats().weight()+itemTwo.baseEnvStats().weight());
					gc.baseEnvStats().setArmor(itemOne.baseEnvStats().armor()+itemTwo.baseEnvStats().armor());
					gc.setMaterial(material);
					gc.setCapacity(0);
					if(itemOne instanceof Container)
						gc.setCapacity(gc.capacity()+((Container)itemOne).capacity());
					if(itemTwo instanceof Container)
						gc.setCapacity(gc.capacity()+((Container)itemTwo).capacity());
					gc.setRawLogicalAnd(true);
					gc.setRawProperLocationBitmap(wornLocation);

					gc.baseEnvStats().setLevel(itemOne.baseEnvStats().level());
					if(itemTwo.baseEnvStats().level()>itemOne.baseEnvStats().level())
						gc.baseEnvStats().setLevel(itemTwo.baseEnvStats().level());
					gc.baseEnvStats().setAbility(itemOne.baseEnvStats().ability()+itemTwo.baseEnvStats().ability());
					melded=gc;
					mob.addInventory(gc);
				}
				else
				if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
				{
					Weapon gc=(Weapon)CMClass.getWeapon("GenWeapon");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.baseEnvStats().setWeight(itemOne.baseEnvStats().weight()+itemTwo.baseEnvStats().weight());
					gc.baseEnvStats().setAttackAdjustment(itemOne.baseEnvStats().attackAdjustment()+itemTwo.baseEnvStats().attackAdjustment());
					gc.baseEnvStats().setDamage(itemOne.baseEnvStats().damage()+itemTwo.baseEnvStats().damage());
					gc.setWeaponType(((Weapon)itemOne).weaponType());
					gc.setWeaponClassification(((Weapon)itemTwo).weaponClassification());
					gc.setRawLogicalAnd(true);

					gc.baseEnvStats().setLevel(itemOne.baseEnvStats().level());
					if(itemTwo.baseEnvStats().level()>itemOne.baseEnvStats().level())
						gc.baseEnvStats().setLevel(itemTwo.baseEnvStats().level());
					gc.baseEnvStats().setAbility(itemOne.baseEnvStats().ability()+itemTwo.baseEnvStats().ability());
					melded=gc;
					mob.addInventory(gc);
				}
				else
				if((itemOne instanceof Container)&&(itemTwo instanceof Container))
				{
					boolean isLocked=((Container)itemOne).hasALock();
					String keyName=((Container)itemOne).keyName();
					if(!isLocked)
					{
						isLocked=((Container)itemTwo).hasALock();
						keyName=((Container)itemTwo).keyName();
					}
					Container gc=(Container)CMClass.getItem("GenContainer");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setGettable(itemOne.isGettable()&&itemTwo.isGettable());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.baseEnvStats().setWeight(itemOne.baseEnvStats().weight()+itemTwo.baseEnvStats().weight());
					gc.setCapacity(((Container)itemOne).capacity()+((Container)itemTwo).capacity());
					gc.setLidsNLocks((((Container)itemOne).hasALid()||((Container)itemTwo).hasALid()),true,isLocked,false);
					gc.setKeyName(keyName);

					gc.baseEnvStats().setLevel(itemOne.baseEnvStats().level());
					if(itemTwo.baseEnvStats().level()>itemOne.baseEnvStats().level())
						gc.baseEnvStats().setLevel(itemTwo.baseEnvStats().level());
					gc.baseEnvStats().setAbility(itemOne.baseEnvStats().ability()+itemTwo.baseEnvStats().ability());
					melded=gc;
					mob.addInventory(gc);
				}
				if(melded!=null)
				{
					for(int a=0;a<itemOne.numAffects();a++)
					{
						Ability aff=itemOne.fetchAffect(a);
						if((aff!=null)&&(melded.fetchAffect(aff.ID())==null))
							melded.addAffect(aff);
					}
					for(int a=0;a<itemTwo.numAffects();a++)
					{
						Ability aff=itemTwo.fetchAffect(a);
						if((aff!=null)&&(melded.fetchAffect(aff.ID())==null))
							melded.addAffect(aff);
					}
					for(int a=0;a<itemOne.numBehaviors();a++)
					{
						Behavior B=itemOne.fetchBehavior(a);
						if(B!=null)	melded.addBehavior(B);
					}
					for(int a=0;a<itemTwo.numBehaviors();a++)
					{
						Behavior B=itemTwo.fetchBehavior(a);
						if(B!=null)	melded.addBehavior(B);
					}
					melded.recoverEnvStats();
				}
				itemOne.destroyThis();
				itemTwo.destroyThis();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) "+itemOne.displayName()+" and "+itemTwo.displayName()+", but fail(s).");

		// return whether it worked
		return success;
	}
}