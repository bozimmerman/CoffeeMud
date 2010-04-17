package com.planet_ink.coffee_mud.WebMacros.grinder;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;


/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class GrinderItems
{
    private static final String[] okparms={
          "NAME","CLASSES","DISPLAYTEXT","DESCRIPTION"," LEVEL",
          " ABILITY"," REJUV"," MISCTEXT","MATERIALS","ISGENERIC",
          "ISREADABLE","READABLETEXT","ISDRINK","LIQUIDHELD","QUENCHED",
          "ISCONTAINER","CAPACITY","ISARMOR","ARMOR","WORNDATA",
          " HEIGHT","ISWEAPON","WEAPONTYPE","WEAPONCLASS","ATTACK",
          "DAMAGE","MINRANGE","MAXRANGE","SECRETIDENTITY",
          "ISGETTABLE","ISREMOVABLE","ISDROPPABLE","ISTWOHANDED","ISTRAPPED",
          "READABLESPELLS","ISWAND"," USESREMAIN","VALUE","WEIGHT",
          "ISMAP","MAPAREAS","ISFOOD","ISPILL","ISSUPERPILL",
          "ISPOTION","LIQUIDTYPES","AMMOTYPE","AMMOCAP","READABLESPELL",
          "ISRIDEABLE","RIDEABLETYPE","MOBSHELD","HASALID","HASALOCK",
          "KEYCODE","ISWALLPAPER","NOURISHMENT","CONTAINER","ISLIGHTSOURCE",
          "DURATION","NONLOCATABLE","ISKEY","CONTENTTYPES","ISINSTRUMENT",
          "INSTRUMENTTYPE","ISAMMO","ISMOBITEM","ISDUST","ISPERFUME",
          "SMELLS","IMAGE","ISEXIT","EXITNAME","EXITCLOSEDTEXT",
          "NUMCOINS","CURRENCY","DENOM","ISRECIPE","RECIPESKILL",
          "RECIPEDATA", "LAYER","SEETHRU","MULTIWEAR","ISCATALOGED",
          "CATARATE","CATALIVE","CATAMASK","BITE"};
	public static String editItem(ExternalHTTPRequests httpReq,
								  Hashtable parms,
								  MOB whom,
								  Room R,
                                  MOB playerM)
	{
		String itemCode=httpReq.getRequestParameter("ITEM");
		if(itemCode==null) return "@break@";

		String mobNum=httpReq.getRequestParameter("MOB");
		String newClassID=httpReq.getRequestParameter("CLASSES");

        String sync=("SYNC"+((R==null)?((playerM!=null)?playerM.Name():null):R.roomID()));
    	synchronized(sync.intern())
    	{
    		if(R!=null)
            {
                R=CMLib.map().getRoom(R);
                CMLib.map().resetRoom(R);
            }

			Item I=null;
			MOB M=null;
            if(playerM!=null)
                M=playerM;
            else
			if((mobNum!=null)&&(mobNum.length()>0))
			{
				if(R!=null)
					M=RoomData.getMOBFromCode(R,mobNum);
                else
                    M=RoomData.getMOBFromCode(RoomData.mobs,mobNum);
				if(M==null)
				{
					StringBuffer str=new StringBuffer("No MOB?!");
					str.append(" Got: "+mobNum);
					str.append(", Includes: ");
                    if(R!=null)
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M2=R.fetchInhabitant(m);
						if((M2!=null)&&(M2.savable()))
						   str.append(M2.Name()+"="+RoomData.getMOBCode(R,M2));
					}
					return str.toString();
				}
			}
			if(itemCode.equals("NEW")||itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
				I=CMClass.getItem(newClassID);
			else
			if(M!=null)
				I=RoomData.getItemFromCode(M,itemCode);
			else
				I=RoomData.getItemFromCode(R,itemCode);

			if(I==null)
			{
				StringBuffer str=new StringBuffer("No Item?!");
				str.append(" Got: "+itemCode);
				str.append(", Includes: ");
				if(M==null)
				{
					if(R!=null)
					for(int i=0;i<R.numItems();i++)
					{
						Item I2=R.fetchItem(i);
						if(I2!=null) str.append(I2.Name()+"="+RoomData.getItemCode(R,I2));
					}
				}
				else
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I2=M.fetchInventory(i);
						if(I2!=null) str.append(I2.Name()+"="+RoomData.getItemCode(M,I2));
					}
				return str.toString();
			}
			Item copyItem=(Item)I.copyOf();
			Item oldI=I;
			if((newClassID!=null)&&(!newClassID.equals(CMClass.classID(I))))
            {
				I=CMClass.getItem(newClassID);
                if(I==null) Log.errOut("GrinderItems","Error: bad class id: "+newClassID);
            }
			
			if(I==null)
			{
				copyItem.destroy();
				return "[error]";
			}
			
			CatalogLibrary.CataData cataData=null;

			for(int o=0;o<okparms.length;o++)
			{
				String parm=okparms[o];
				boolean generic=true;
				if(parm.startsWith(" "))
				{
					generic=false;
					parm=parm.substring(1);
				}
				if((!httpReq.isRequestParameter(parm))
				&&(oldI!=null)
				&&(newClassID==null)
				&&(CMLib.flags().isCataloged(oldI))
				&&(!parm.equalsIgnoreCase("CONTAINER"))
				&&(!parm.equalsIgnoreCase("BEINGWORN")))
				    continue;

				String old=httpReq.getRequestParameter(parm);
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
					I.baseEnvStats().setLevel(CMath.s_int(old));
					break;
				case 5: // ability;
					I.baseEnvStats().setAbility(CMath.s_int(old));
					break;
				case 6: // rejuv;
					I.baseEnvStats().setRejuv(CMath.s_int(old));
					break;
				case 7: // misctext
					if(!I.isGeneric())
						I.setMiscText(old);
					break;
				case 8: // materials
					I.setMaterial(CMath.s_int(old));
					break;
				case 9: // is generic
					break;
				case 10: // isreadable
					CMLib.flags().setReadable(I,old.equals("on"));
					break;
				case 11: // readable text
					if(!(I instanceof Ammunition))
						I.setReadableText(old);
					break;
				case 12: // is drink
					break;
				case 13: // liquid held
					if(I instanceof Drink)
					{
						((Drink)I).setLiquidHeld(CMath.s_int(old));
						((Drink)I).setLiquidRemaining(CMath.s_int(old));
					}
					break;
				case 14: // quenched
					if(I instanceof Drink)
						((Drink)I).setThirstQuenched(CMath.s_int(old));
					break;
				case 15: // is container
					break;
				case 16: // capacity
					if(I instanceof Container)
						((Container)I).setCapacity(CMath.s_int(old));
					break;
				case 17: // is armor
					break;
				case 18: // armor
					if(I instanceof Armor)
						I.baseEnvStats().setArmor(CMath.s_int(old));
					break;
				case 19: // worn data
					if(((I instanceof Armor)||(I instanceof MusicalInstrument))
					&&(httpReq.isRequestParameter("WORNDATA")))
					{
						int climate=CMath.s_int(httpReq.getRequestParameter("WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.isRequestParameter("WORNDATA"+(Integer.toString(i))))
								climate=climate|CMath.s_int(httpReq.getRequestParameter("WORNDATA"+(Integer.toString(i))));
							else
								break;
						I.setRawProperLocationBitmap(climate);
					}
					break;
				case 20: // height
					if(I instanceof Armor)
						I.baseEnvStats().setHeight(CMath.s_int(old));
					break;
				case 21: // is weapon
					break;
				case 22: // weapon type
					if(I instanceof Weapon)
						((Weapon)I).setWeaponType(CMath.s_int(old));
					break;
				case 23: // weapon class
					if(I instanceof Weapon)
						((Weapon)I).setWeaponClassification(CMath.s_int(old));
					break;
				case 24: // attack
					if(I instanceof Weapon)
						I.baseEnvStats().setAttackAdjustment(CMath.s_int(old));
					break;
				case 25: // damage
					if(I instanceof Weapon)
						I.baseEnvStats().setDamage(CMath.s_int(old));
					break;
				case 26: // min range
					if(I instanceof Weapon)
						((Weapon)I).setRanges(CMath.s_int(old),I.maxRange());
					break;
				case 27: // max range
					if(I instanceof Weapon)
						((Weapon)I).setRanges(I.minRange(),CMath.s_int(old));
					break;
				case 28: // secret identity
					I.setSecretIdentity(old);
					break;
				case 29: // is gettable
					CMLib.flags().setGettable(I,old.equals("on"));
					break;
				case 30: // is removable
					CMLib.flags().setRemovable(I,old.equals("on"));
					break;
				case 31: // is droppable
					CMLib.flags().setDroppable(I,old.equals("on"));
					break;
				case 32: // is two handed
					if((I instanceof Weapon)||(I instanceof Armor))
						I.setRawLogicalAnd(old.equals("on"));
					break;
				case 33: // is trapped
					break;
				case 34: // readable spells
					if(((I instanceof SpellHolder))
					&&(CMClass.classID(I).indexOf("SuperPill")<0))
					{
						if(httpReq.isRequestParameter("READABLESPELLS"))
						{
							old=";"+httpReq.getRequestParameter("READABLESPELLS");
							for(int i=1;;i++)
								if(httpReq.isRequestParameter("READABLESPELLS"+(Integer.toString(i))))
									old+=";"+httpReq.getRequestParameter("READABLESPELLS"+(Integer.toString(i)));
								else
									break;
						}
						old=old+";";
						((SpellHolder)I).setSpellList(old);
					}
					break;
				case 35: // is wand
					break;
				case 36: // uses
					I.setUsesRemaining(CMath.s_int(old));
					break;
				case 37: // value
					I.setBaseValue(CMath.s_int(old));
					break;
				case 38: // weight
					I.baseEnvStats().setWeight(CMath.s_int(old));
					break;
				case 39: // is map
					break;
				case 40: // map areas
					if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
					{
						Vector<String> V=new Vector<String>();
						if(httpReq.isRequestParameter("MAPAREAS"))
						{
							old=httpReq.getRequestParameter("MAPAREAS").trim();
							if(old.length()>0)
								V.add(old);
							for(int i=1;;i++)
								if(httpReq.isRequestParameter("MAPAREAS"+(Integer.toString(i))))
								{
									old=httpReq.getRequestParameter("MAPAREAS"+(Integer.toString(i))).trim();
									if(old.length()>0)
										V.add(old);
								}
								else
									break;
						}
						old = CMParms.toSemicolonList(V);
						CMLib.flags().setReadable(I,false);
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
						((Drink)I).setLiquidType(CMath.s_int(old));
					break;
				case 46: // ammo types
					if(I instanceof Ammunition)
						((Ammunition)I).setAmmunitionType(old);
					else
					if((I instanceof Weapon)&&(!(I instanceof Wand)))
						((Weapon)I).setAmmunitionType(old);
					break;
				case 47: // ammo capacity
					if((I instanceof Weapon)&&(!(I instanceof Wand)))
					{
						((Weapon)I).setAmmoCapacity(CMath.s_int(old));
						((Weapon)I).setAmmoRemaining(CMath.s_int(old));
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
						((Rideable)I).setRideBasis(CMath.s_int(old));
					break;
				case 51: // mob capacity
					if(I instanceof Rideable)
						((Rideable)I).setRiderCapacity(CMath.s_int(old));
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
						((Food)I).setNourishment(CMath.s_int(old));
					break;
				case 57: // container
				    /* pushed back to room/mob, where it belongs
					if(!RoomData.isAllNum(old))
						I.setContainer(null);
					else
					if(M==null)
						I.setContainer(RoomData.getItemFromCode(R,old));
					else
						I.setContainer(RoomData.getItemFromCode(M,old));
				    */
					break;
				case 58: // is light
					break;
				case 59:
					if(I instanceof Light)
						((Light)I).setDuration(CMath.s_int(old));
					break;
				case 60:
					if(old.equals("on"))
						I.baseEnvStats().setSensesMask(I.baseEnvStats().sensesMask()|EnvStats.SENSE_UNLOCATABLE);
					else
					if((I.baseEnvStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)>0)
						I.baseEnvStats().setSensesMask(I.baseEnvStats().sensesMask()-EnvStats.SENSE_UNLOCATABLE);
					break;
				case 61: // is key
					break;
				case 62: // content types
					if((I instanceof Container)&&(httpReq.isRequestParameter("CONTENTTYPES")))
					{
						long content=CMath.s_long(httpReq.getRequestParameter("CONTENTTYPES"));
						if(content>0)
						for(int i=1;;i++)
							if(httpReq.isRequestParameter("CONTENTTYPES"+(Integer.toString(i))))
								content=content|CMath.s_int(httpReq.getRequestParameter("CONTENTTYPES"+(Integer.toString(i))));
							else
								break;
						((Container)I).setContainTypes(content);
					}
					break;
				case 63: // is instrument:
					break;
				case 64: // instrumenttype
					if(I instanceof MusicalInstrument)
						((MusicalInstrument)I).setInstrumentType(CMath.s_int(old));
					break;
				case 65: // isammo
					break;
				case 66: // is mob type
					break;
				case 67: // is dust
					break;
				case 68: // is perfume
					break;
				case 69: // smells
					if(I instanceof Perfume)
						((Perfume)I).setSmellList(old);
					break;
				case 70:
				    I.setImage(old);
				    break;
				case 71: // is exit
				    break;
				case 72: // exit name
				    if(I instanceof Exit)
				        ((Exit)I).setExitParams(old,((Exit)I).closeWord(),((Exit)I).openWord(),((Exit)I).closedText());
				    break;
				case 73: // exit closed text
				    if(I instanceof Exit)
				        ((Exit)I).setExitParams(((Exit)I).doorName(),((Exit)I).closeWord(),((Exit)I).openWord(),old);
				    break;
				case 74: // numcoins
				    if(I instanceof Coins)
				        ((Coins)I).setNumberOfCoins(CMath.s_long(old));
				    break;
				case 75: // currency
				    if(I instanceof Coins)
				        ((Coins)I).setCurrency(old);
				    break;
				case 76: // denomination
				    if(I instanceof Coins)
				        ((Coins)I).setDenomination(CMath.s_double(old));
				    break;
				case 77: // isrecipe
				    break;
				case 78: // recipeskill
				    if(I instanceof Recipe)
				        ((Recipe)I).setCommonSkillID(old);
				    break;
				case 79: // recipedata
				    if(I instanceof Recipe)
				        ((Recipe)I).setRecipeCodeLine(CMStrings.replaceAll(old,",","\t"));
				    break;
				case 80: // layer
					if(I instanceof Armor)
						((Armor)I).setClothingLayer(CMath.s_short(old));
					break;
				case 81: // see-thru
					if(I instanceof Armor)
					{
						if(old.equals("on"))
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()|Armor.LAYERMASK_SEETHROUGH));
						else
						if((((Armor)I).getLayerAttributes()&Armor.LAYERMASK_SEETHROUGH)>0)
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()-Armor.LAYERMASK_SEETHROUGH));
					}
					break;
				case 82: // multi-wear
					if(I instanceof Armor)
					{
						if(old.equals("on"))
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()|Armor.LAYERMASK_MULTIWEAR));
						else
						if((((Armor)I).getLayerAttributes()&Armor.LAYERMASK_MULTIWEAR)>0)
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()-Armor.LAYERMASK_MULTIWEAR));
					}
					break;
				case 83: // iscataloged
				    break;
				case 84: // catarate
				    if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
				    {
				        if(cataData==null) cataData=CMLib.catalog().sampleCataData("");
				        cataData.setRate(CMath.s_pct(old));
				    }
				    break;
                case 85: // catalive
                    if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
                    {
                        if(cataData==null) cataData=CMLib.catalog().sampleCataData("");
                        cataData.setWhenLive(((old!=null)&&(old.equalsIgnoreCase("on"))));
                    }
                    break;
                case 86: // catamask
                    if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
                    {
                        if(cataData==null) cataData=CMLib.catalog().sampleCataData("");
                        cataData.setMaskStr(old);
                    }
                    break;
                case 87: // bite
                    if(I instanceof Food)
                        ((Food)I).setBite(CMath.s_int(old));
                    break;
				}
			}
			if(I.isGeneric()&&(!CMLib.flags().isCataloged(I)))
			{
				String error=GrinderExits.dispositions(I,httpReq,parms);
				if(error.length()>0) return error;
				error=GrinderAreas.doAffectsNBehavs(I,httpReq,parms);
				if(error.length()>0) return error;
			}

			I.recoverEnvStats();
			I.text();
			if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
			{
                Item I2=CMLib.catalog().getCatalogItem(itemCode.substring(8));
                if((I2!=null)&&(!I.Name().equalsIgnoreCase(I2.Name())))
                    I.setName(I2.Name());
                httpReq.addRequestParameters("ITEM",itemCode);
                if(I2==null)
                {
                    CMLib.catalog().addCatalog(I);
	                Log.infoOut("GrinderItems",whom.Name()+" created catalog ITEM "+I.Name());
                }
                else
                {
                	if(cataData!=null)
                	{
	                    CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(I.Name());
	                    data.build(cataData.data());
                	}
                	CMLib.catalog().updateCatalog(I);
	                Log.infoOut("GrinderItems",whom.Name()+" updated catalog ITEM "+I.Name());
                }
				copyItem.destroy();
                copyItem=(Item)I.copyOf();
			}
			else
			if(itemCode.equals("NEW"))
			{
				if(M==null)
				{
					if(R==null)
					{

					}
					else
					{
						R.addItem(I);
						R.recoverRoomStats();
					}
				}
				else
				{
					M.addInventory(I);
					M.recoverEnvStats();
					if((mobNum==null)||(!mobNum.startsWith("CATALOG-")))
						M.text();
					if(R!=null) R.recoverRoomStats();
				}
			}
			else
			if(I!=oldI)
			{
				Environmental oldOwner=oldI.owner();
				if(M==null)
				{
					if(R==null)
					{

					}
					else
					{
						R.delItem(oldI);
						R.addItem(I);
						R.recoverRoomStats();
						for(int i=0;i<R.numItems();i++)
						{
							Item I2=R.fetchItem(i);
							if((I2.container()!=null)
							&&(I2.container()==oldI))
								if(I instanceof Container)
									I2.setContainer(I);
								else
									I2.setContainer(null);
						}
					}
				}
				else
				{
					M.delInventory(oldI);
					M.addInventory(I);
					M.recoverEnvStats();
					if((mobNum==null)||(!mobNum.startsWith("CATALOG-")))
						M.text();
                    if(R!=null) R.recoverRoomStats();
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I2=M.fetchInventory(i);
						if((I2.container()!=null)
						&&(I2.container()==oldI))
							if(I instanceof Container)
								I2.setContainer(I);
							else
								I2.setContainer(null);
					}
				}
				oldI.setOwner(oldOwner); // necesssary for destroy this to work.
				oldI.destroy();
			}
			if(M==null)
			{
				if(R==null)
				{
				    if((!itemCode.startsWith("CATALOG-"))
				    &&(!itemCode.startsWith("NEWCATA-")))
				    {
    					RoomData.contributeItems(CMParms.makeVector(I));
    					httpReq.addRequestParameters("ITEM",RoomData.getItemCode(RoomData.items,I));
				    }
				}
				else
				{
					CMLib.database().DBUpdateItems(R);
					httpReq.addRequestParameters("ITEM",RoomData.getItemCode(R,I));
					R.startItemRejuv();
				}
			}
			else
			{
				if((httpReq.isRequestParameter("BEINGWORN"))
			    &&((httpReq.getRequestParameter("BEINGWORN")).equals("on")))
				{
				    // deprecated back to room/mob, where it belongs
					//if(I.amWearingAt(Wearable.IN_INVENTORY))
					//	I.wearEvenIfImpossible(M);
				}
				//else I.wearAt(Wearable.IN_INVENTORY);
                if((R!=null)&&(playerM==null))
                {
    				CMLib.database().DBUpdateMOBs(R);
    				httpReq.addRequestParameters("MOB",RoomData.getMOBCode(R,M));
                }
				httpReq.addRequestParameters("ITEM",RoomData.getItemCode(M,I));
			    if((mobNum==null)||(mobNum.startsWith("CATALOG-"))||(mobNum.startsWith("NEWCATA-")))
			    {
			    	CMLib.catalog().updateCatalog(M);
			    	M.text();
			    }
			}
			if(!copyItem.sameAs(I))
				Log.sysOut("Grinder",whom.Name()+" modified item "+copyItem.Name()+((M!=null)?" on mob "+M.Name():"")+((R!=null)?" in room "+R.roomID():"")+".");
			copyItem.destroy();
    	}
		return "";
	}
}
