package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.web.macros.RoomData;

public class GrinderItems
{
	public static String editItem(ExternalHTTPRequests httpReq, 
								  Hashtable parms, 
								  Room R)
	{
		Hashtable reqs=httpReq.getRequestParameters();
		String itemCode=(String)reqs.get("ITEM");
		if(itemCode==null) return "@break@";

		String mobNum=(String)reqs.get("MOB");
		String newClassID=(String)reqs.get("CLASSES");
		
		ExternalPlay.resetRoom(R);
		
		Item I=null;
		MOB M=null;
		if((mobNum!=null)&&(mobNum.length()>0))
		{
			M=RoomData.getMOBAtCardinality(R,Util.s_int(mobNum)-1);
			if(M==null)
				return "No MOB?!";
		}
		if(itemCode.equals("NEW"))
			I=CMClass.getItem(newClassID);
		else
		if(M!=null)
			I=M.fetchInventory(Util.s_int(itemCode)-1);
		else
			I=R.fetchItem(Util.s_int(itemCode)-1);
		
		if(I==null)
			return "No Item?!";
		Item oldI=I;
		if((newClassID!=null)&&(!newClassID.equals(CMClass.className(I))))
			I=CMClass.getItem(newClassID);
		
		
		StringBuffer str=new StringBuffer("");
		String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
						  " LEVEL"," ABILITY"," REJUV"," MISCTEXT",
						  "MATERIALS","ISGENERIC","ISREADABLE","READABLETEXT",
						  "ISDRINK","LIQUIDHELD","QUENCHED","ISCONTAINER",
						  "CAPACITY","ISARMOR","ARMOR","WORNDATA"," HEIGHT",
						  "ISWEAPON","WEAPONTYPE","WEAPONCLASS","ATTACK","DAMAGE","MINRANGE",
						  "MAXRANGE","SECRETIDENTITY","ISGETTABLE","ISREMOVABLE",
						  "ISDROPPABLE","ISTWOHANDED","ISTRAPPED","READABLESPELLS",
						  "ISWAND"," USESREMAIN","VALUE","WEIGHT","ISMAP","MAPAREAS","ISFOOD",
						  "ISPILL","ISSUPERPILL","ISPOTION","LIQUIDTYPES","AMMOTYPE",
						  "AMMOCAP","READABLESPELL","ISRIDEABLE","RIDEABLETYPE","MOBSHELD",
						  "HASALID","HASALOCK","KEYCODE","ISWALLPAPER","NOURISHMENT","CONTAINER",
						  "ISLIGHTSOURCE","DURATION","NONLOCATABLE"};
		for(int o=0;o<okparms.length;o++)
		{
			String parm=okparms[o];
			boolean generic=true;
			if(parm.startsWith(" "))
			{
				generic=false;
				parm=parm.substring(1);
			}
			String old=(String)reqs.get(parm);
			if(old==null) old="";
			
			if((I.isGeneric()||(!generic)))
			switch(o)
			{
			case 0: // name
				I.setName(old);
				break;
			case 1: // classes
				break;
			case 2: // displaytext
				I.setDisplayText(old);
				break;
			case 3: // description
				I.setDescription(old);
				break;
			case 4: // level
				I.baseEnvStats().setLevel(Util.s_int(old));
				break;
			case 5: // ability;
				I.baseEnvStats().setAbility(Util.s_int(old));
				break;
			case 6: // rejuv;
				I.baseEnvStats().setRejuv(Util.s_int(old));
				break;
			case 7: // misctext
				if(!I.isGeneric())
					I.setMiscText(old);
				break;
			case 8: // materials
				I.setMaterial(Util.s_int(old));
				break;
			case 9: // is generic
				break;
			case 10: // isreadable
				I.setReadable(old.equals("on"));
				break;
			case 11: // readable text
				I.setReadableText(old);
				break;
			case 12: // is drink
				break;
			case 13: // liquid held
				if(I instanceof Drink)
				{
					((Drink)I).setLiquidHeld(Util.s_int(old));
					((Drink)I).setLiquidRemaining(Util.s_int(old));
				}
				break;
			case 14: // quenched
				if(I instanceof Drink)
					((Drink)I).setThirstQuenched(Util.s_int(old));
				break;
			case 15: // is container
				break;
			case 16: // capacity
				if(I instanceof Container)
					((Container)I).setCapacity(Util.s_int(old));
				break;
			case 17: // is armor
				break;
			case 18: // armor
				if(I instanceof Armor)
					I.baseEnvStats().setArmor(Util.s_int(old));
				break;
			case 19: // worn data
				if((I instanceof Armor)&&(reqs.containsKey("WORNDATA")))
				{
					int climate=Util.s_int((String)reqs.get("WORNDATA"));
					for(int i=1;;i++)
						if(reqs.containsKey("WORNDATA"+(new Integer(i).toString())))
							climate=climate|Util.s_int((String)reqs.get("WORNDATA"+(new Integer(i).toString())));
						else
							break;
					((Armor)I).setRawProperLocationBitmap(climate);
				}
				break;
			case 20: // height
				if(I instanceof Armor)
					I.baseEnvStats().setHeight(Util.s_int(old));
				break;
			case 21: // is weapon
				break;
			case 22: // weapon type
				if(I instanceof Weapon)
					((Weapon)I).setWeaponType(Util.s_int(old));
				break;
			case 23: // weapon class
				if(I instanceof Weapon)
					((Weapon)I).setWeaponClassification(Util.s_int(old));
				break;
			case 24: // attack
				if(I instanceof Weapon)
					I.baseEnvStats().setAttackAdjustment(Util.s_int(old));
				break;
			case 25: // damage
				if(I instanceof Weapon)
					I.baseEnvStats().setDamage(Util.s_int(old));
				break;
			case 26: // min range
				if(I instanceof Weapon)
					((Weapon)I).setRanges(Util.s_int(old),I.maxRange());
				break;
			case 27: // max range
				if(I instanceof Weapon)
					((Weapon)I).setRanges(I.minRange(),Util.s_int(old));
				break;
			case 28: // secret identity
				I.setSecretIdentity(old);
				break;
			case 29: // is gettable
				I.setGettable(old.equals("on"));
				break;
			case 30: // is removable
				I.setRemovable(old.equals("on"));
				break;
			case 31: // is droppable
				I.setDroppable(old.equals("on"));
				break;
			case 32: // is two handed
				if((I instanceof Weapon)||(I instanceof Armor))
					I.setRawLogicalAnd(old.equals("on"));
				break;
			case 33: // is trapped
				I.setTrapped(old.equals("on"));
				break;
			case 34: // readable spells
				if(((I instanceof Pill)||(I instanceof Scroll)||(I instanceof Potion))&&(CMClass.className(I).indexOf("SuperPill")<0))
				{
					if(httpReq.getRequestParameters().containsKey("READABLESPELLS"))
					{
						old=";"+(String)httpReq.getRequestParameters().get("READABLESPELLS");
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("READABLESPELLS"+(new Integer(i).toString())))
								old+=";"+(String)httpReq.getRequestParameters().get("READABLESPELLS"+(new Integer(i).toString()));
							else
								break;
					}
					old=old+";";
					if(I instanceof Pill)
						((Pill)I).setSpellList(old);
					if(I instanceof Potion)
						((Potion)I).setSpellList(old);
					if(I instanceof Scroll)
						((Scroll)I).setScrollText(old);
				}
				break;
			case 35: // is wand
				break;
			case 36: // uses
				I.setUsesRemaining(Util.s_int(old));
				break;
			case 37: // value
				I.setBaseValue(Util.s_int(old));
				break;
			case 38: // weight
				I.baseEnvStats().setWeight(Util.s_int(old));
				break;
			case 39: // is map
				break;
			case 40: // map areas
				if(I instanceof com.planet_ink.coffee_mud.interfaces.Map)
				{
					if(httpReq.getRequestParameters().containsKey("MAPAREAS"))
					{
						old=";"+(String)httpReq.getRequestParameters().get("MAPAREAS");
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("MAPAREAS"+(new Integer(i).toString())))
								old+=";"+(String)httpReq.getRequestParameters().get("MAPAREAS"+(new Integer(i).toString()));
							else
								break;
					}
					old=old+";";
					I.setReadable(false);
					I.setReadableText(old);
				}
				break;
			case 41: // is readable
				break;
			case 42: // is pill
				break;
			case 43: // is super pill
				break;
			case 44: // is potion
				break;
			case 45: // liquid types
				if((I instanceof Drink)&&(!(I instanceof Potion)))
					((Drink)I).setLiquidType(Util.s_int(old));
				break;
			case 46: // ammo types
				if((I instanceof Weapon)&&(!(I instanceof Wand)))
					((Weapon)I).setAmmunitionType(old);
				break;
			case 47: // ammo capacity
				if((I instanceof Weapon)&&(!(I instanceof Wand)))
				{
					((Weapon)I).setAmmoCapacity(Util.s_int(old));
					((Weapon)I).setAmmoRemaining(Util.s_int(old));
				}
				break;
			case 48: // readable spell
				if(I instanceof Wand)
					((Wand)I).setSpell(CMClass.findAbility(old));
				break;
			case 49: // is map
				break;
			case 50: // rideable type
				if(I instanceof Rideable)
					((Rideable)I).setRideBasis(Util.s_int(old));
				break;
			case 51: // mob capacity
				if(I instanceof Rideable)
					((Rideable)I).setMobCapacity(Util.s_int(old));
				break;
			case 52: // has a lid
				if(I instanceof Container)
					((Container)I).setLidsNLocks(old.equals("on"),!old.equals("on"),((Container)I).hasALock(),((Container)I).hasALock());
				break;
			case 53: // has a lock
				if(I instanceof Container)
				{
					boolean hasALid=((Container)I).hasALid();
					((Container)I).setLidsNLocks(hasALid||old.equals("on"),!(hasALid||old.equals("on")),old.equals("on"),old.equals("on"));
				}
				break;
			case 54: // key code
				if((I instanceof Container)&&(((Container)I).hasALock()))
					((Container)I).setKeyName(old);
				break;
			case 55: // is wallpaper
				break;
			case 56: // nourishment
				if(I instanceof Food)
					((Food)I).setNourishment(Util.s_int(old));
				break;
			case 57: // container
				if(Util.s_int(old)<=0)
					I.setContainer(null);
				else
				if(M==null)
					I.setContainer(R.fetchItem(Util.s_int(old)-1));
				else
					I.setContainer(M.fetchInventory(Util.s_int(old)-1));
				break;
			case 58: // is light
				break;
			case 59:
				if(I instanceof Light)
					((Light)I).setDuration(Util.s_int(old));
				break;
			case 60:
				if(old.equals("on"))
					I.baseEnvStats().setSensesMask(I.baseEnvStats().sensesMask()|EnvStats.CAN_SEE);
				else
				if((I.baseEnvStats().sensesMask()&EnvStats.CAN_SEE)>0)
					I.baseEnvStats().setSensesMask(I.baseEnvStats().sensesMask()-EnvStats.CAN_SEE);
				break;
			}
		}
		if(I.isGeneric())
		{
			String error=GrinderExits.dispositions(I,httpReq,parms);
			if(error.length()>0) return error;
			error=GrinderAreas.doAffectsNBehavs(I,httpReq,parms);
			if(error.length()>0) return error;
		}
		
		I.recoverEnvStats();
		I.text();
		if(itemCode.equals("NEW"))
		{
			if(M==null)
			{
				R.addItem(I);
				R.recoverRoomStats();
			}
			else
			{
				M.addInventory(I);
				M.recoverEnvStats();
				M.text();
				R.recoverRoomStats();
			}
		}
		else
		if(I!=oldI)
		{
			if(M==null)
			{
				R.delItem(oldI);
				R.addItem(I);
				R.recoverRoomStats();
				for(int i=0;i<R.numItems();i++)
				{
					Item I2=R.fetchItem(i);
					if((I2.container()!=null)
					&&(I2.container()==oldI))
						if(I.isAContainer())
							I2.setContainer(I);
						else
							I2.setContainer(null);
				}
			}
			else
			{
				M.delInventory(oldI);
				M.addInventory(I);
				M.recoverEnvStats();
				M.text();
				R.recoverRoomStats();
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I2=M.fetchInventory(i);
					if((I2.container()!=null)
					&&(I2.container()==oldI))
						if(I.isAContainer())
							I2.setContainer(I);
						else
							I2.setContainer(null);
				}
			}
		}
		if(M==null)
			ExternalPlay.DBUpdateItems(R);
		else
		{
			if((reqs.get("BEINGWORN")!=null)
			   &&(((String)reqs.get("BEINGWORN")).equals("on")))
			{
				if(I.amWearingAt(Item.INVENTORY))
					I.wearIfPossible(M);
			}
			else
				I.wearAt(Item.INVENTORY);
			ExternalPlay.DBUpdateMOBs(R);
		}
		R.startItemRejuv();
		return "";
	}
}