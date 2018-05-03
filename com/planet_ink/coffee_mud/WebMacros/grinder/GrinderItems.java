package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
public class GrinderItems
{
	public enum ItemDataField
	{
		NAME,CLASSES,DISPLAYTEXT,DESCRIPTION,
		LEVEL(false),ABILITY(false),REJUV(false),MISCTEXT(false),
		MATERIALS,ISGENERIC,ISFOOD,NOURISHMENT,
		ISDRINK,LIQUIDHELD,QUENCHED,ISCONTAINER,
		CAPACITY,ISARMOR,ARMOR,WORNDATA,
		HEIGHT(false),ISWEAPON,WEAPONTYPE,WEAPONCLASS,
		ATTACK,DAMAGE,MINRANGE,MAXRANGE,
		SECRETIDENTITY,ISGETTABLE,ISREMOVABLE,ISDROPPABLE,
		ISTWOHANDED,ISTRAPPED,READABLESPELLS,ISWAND,
		USESREMAIN(false),VALUE,WEIGHT,ISMAP,
		MAPAREAS,ISREADABLE,ISPILL,ISSUPERPILL,
		ISPOTION,LIQUIDTYPES,AMMOTYPE,AMMOCAP,
		READABLESPELL,ISRIDEABLE,RIDEABLETYPE,MOBSHELD,
		HASALID,HASALOCK,KEYCODE,ISWALLPAPER,
		READABLETEXT,CONTAINER,ISLIGHTSOURCE,DURATION,
		ISUNTWOHANDED,ISCOIN,ISSCROLL,BEINGWORN,NONLOCATABLE,
		ISKEY, CONTENTTYPES,ISINSTRUMENT,INSTRUMENTTYPE,
		ISAMMO,ISMOBITEM,ISDUST,ISPERFUME,SMELLS,
		IMAGE,ISEXIT,EXITNAME,EXITCLOSEDTEXT,NUMCOINS,
		CURRENCY,DENOM,ISRECIPE,RECIPESKILL,RECIPEDATA,
		LAYER,SEETHRU,MULTIWEAR,ISCATALOGED,CATARATE,
		CATALIVE,CATAMASK,BITE,MAXUSES,ISELECTRONIC,
		CATACAT,ISPORTAL,PUTSTR,MOUNTSTR,DISMOUNTSTR,
		DEFAULTSCLOSED,DEFAULTSLOCKED,ISWEARANDTEAR,
		ISBOARDABLEITEM, ISPRIVATEPROPERTY, OWNER, 
		ISTECHCOMPONENT,ISSHIPENGINE,ISPANEL,ISFUELCONSUMER,ISPOWERGENERATION,
		MANUFACTURER,POWCAPACITY,POWREMAINING,ACTIVATED,
		MAXTHRUST,SPECIMPULSE,FUELEFFICIENCY,INSTALLFACTOR,
		PANELTYPE,GENAMTPERTICK,CONSUMEDMATS,AREAXML,RECIPESKILLHELP,
		MINTHRUST,ISCONSTTHRUST,AVAILPORTS,CONTENTSACCESS,BLENDEDVIEW,
		ISSHIPWARCOMP,SWARNUMPORTS,SWARPORTS,SWARMTYPES,
		RECHARGERATE,OPENTICKS,ISCLOAK,ISBOOK,MAXPAGES,MAXCHARSPAGE,
		STATESTR,STATESUBJSTR,RIDERSTR
		;
		public boolean isGenField;
		private ItemDataField(boolean isGeneric)
		{
			this.isGenField=isGeneric;
		}

		private ItemDataField()
		{
			isGenField = true;
		}
	}
	
	public static String editItem(HTTPRequest httpReq,
								  java.util.Map<String,String> parms,
								  MOB whom,
								  Room R,
								  MOB playerM)
	{
		String itemCode=httpReq.getUrlParameter("ITEM");
		if(itemCode==null)
			return "@break@";

		final String mobNum=httpReq.getUrlParameter("MOB");
		final String newClassID=httpReq.getUrlParameter("CLASSES");

		String shopItemCode=httpReq.getUrlParameter("SHOPITEM");
		if(shopItemCode==null)
			shopItemCode="";
		
		final String sync=("SYNC"+((R==null)?((playerM!=null)?playerM.Name():null):R.roomID()));
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
					M=RoomData.getMOBFromCode(RoomData.getMOBCache(),mobNum);
				if(M==null)
				{
					final StringBuffer str=new StringBuffer("No MOB?!");
					str.append(" Got: "+mobNum);
					str.append(", Includes: ");
					if(R!=null)
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M2=R.fetchInhabitant(m);
						if((M2!=null)&&(M2.isSavable()))
							str.append(M2.Name()+"="+RoomData.getMOBCode(R,M2));
					}
					return str.toString();
				}
			}
			if(itemCode.equals("NEW"))
				I=CMClass.getItem(newClassID);
			else
			if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
				I=CMClass.getItem(newClassID);
			else
			if(M!=null)
				I=RoomData.getItemFromCode(M,itemCode);
			else
				I=RoomData.getItemFromCode(R,itemCode);

			if(I==null)
			{
				final StringBuffer str=new StringBuffer("No Item?!");
				str.append(" Got: "+itemCode);
				str.append(", Includes: ");
				if(M==null)
				{
					if(R!=null)
					for(int i=0;i<R.numItems();i++)
					{
						final Item I2=R.getItem(i);
						if(I2!=null)
							str.append(I2.Name()+"="+RoomData.getItemCode(R,I2));
					}
				}
				else
				{
					for(int i=0;i<M.numItems();i++)
					{
						final Item I2=M.getItem(i);
						if(I2!=null)
							str.append(I2.Name()+"="+RoomData.getItemCode(M,I2));
					}
				}
				return str.toString();
			}
			Item copyItem=(Item)I.copyOf();
			final Item oldI=I;
			if((newClassID!=null)&&(!newClassID.equals(CMClass.classID(I))))
			{
				I=CMClass.getItem(newClassID);
				if(I==null)
					Log.errOut("GrinderItems","Error: bad class id: "+newClassID);
			}

			if(I==null)
			{
				copyItem.destroy();
				return "[error]";
			}

			CatalogLibrary.CataData cataData=null;

			for(final ItemDataField o : ItemDataField.values())
			{
				final String fieldName=o.name();
				final boolean generic=o.isGenField;
				if((!httpReq.isUrlParameter(fieldName))
				&&(newClassID==null)
				&&(CMLib.flags().isCataloged(oldI))
				&&(!fieldName.equalsIgnoreCase("CONTAINER"))
				&&(!fieldName.equalsIgnoreCase("BEINGWORN")))
					continue;

				String old=httpReq.getUrlParameter(fieldName);
				if(old==null)
					old="";

				if((I.isGeneric()||(!generic)))
				switch(o)
				{
				case NAME: // name
					I.setName(old);
					break;
				case CLASSES: // classes
					break;
				case DISPLAYTEXT: // displaytext
					I.setDisplayText(old);
					break;
				case DESCRIPTION: // description
					I.setDescription(old);
					break;
				case LEVEL: // level
					I.basePhyStats().setLevel(CMath.s_int(old));
					break;
				case ABILITY: // ability;
					I.basePhyStats().setAbility(CMath.s_int(old));
					break;
				case REJUV: // rejuv;
					I.basePhyStats().setRejuv(CMath.s_int(old));
					break;
				case MISCTEXT: // misctext
					if(!I.isGeneric())
						I.setMiscText(old);
					break;
				case MATERIALS: // materials
					I.setMaterial(CMath.s_int(old));
					break;
				case ISGENERIC: // is generic
					break;
				case ISREADABLE: // isreadable
					CMLib.flags().setReadable(I,old.equals("on"));
					break;
				case READABLETEXT: // readable text
					if((!(I instanceof Ammunition))&&(!(I instanceof SpellHolder))&&(!(I instanceof Wand)))
					{
						if(httpReq.isUrlParameter(fieldName)) // must do this to keep special fields from being clobbered
							I.setReadableText(old);
					}
					break;
				case ISDRINK: // is drink
					break;
				case LIQUIDHELD: // liquid held
					if(I instanceof Drink)
					{
						((Drink)I).setLiquidHeld(CMath.s_int(old));
						((Drink)I).setLiquidRemaining(CMath.s_int(old));
					}
					break;
				case QUENCHED: // quenched
					if(I instanceof Drink)
						((Drink)I).setThirstQuenched(CMath.s_int(old));
					break;
				case ISCONTAINER: // is container
					break;
				case CAPACITY: // capacity
					if(I instanceof Container)
						((Container)I).setCapacity(CMath.s_int(old));
					break;
				case ISARMOR: // is armor
					break;
				case ARMOR: // armor
					if(I instanceof Armor)
						I.basePhyStats().setArmor(CMath.s_int(old));
					break;
				case WORNDATA: // worn data
					if(((I instanceof Armor)||(I instanceof MusicalInstrument))
					&&(httpReq.isUrlParameter("WORNDATA")))
					{
						int climate=CMath.s_int(httpReq.getUrlParameter("WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.isUrlParameter("WORNDATA"+(Integer.toString(i))))
								climate=climate|CMath.s_int(httpReq.getUrlParameter("WORNDATA"+(Integer.toString(i))));
							else
								break;
						I.setRawProperLocationBitmap(climate);
					}
					break;
				case HEIGHT: // height
					if(I instanceof Armor)
						I.basePhyStats().setHeight(CMath.s_int(old));
					break;
				case ISWEAPON: // is weapon
					break;
				case WEAPONTYPE: // weapon type
					if(I instanceof Weapon)
						((Weapon)I).setWeaponDamageType(CMath.s_int(old));
					break;
				case WEAPONCLASS: // weapon class
					if(I instanceof Weapon)
						((Weapon)I).setWeaponClassification(CMath.s_int(old));
					break;
				case ATTACK: // attack
					if(I instanceof Weapon)
						I.basePhyStats().setAttackAdjustment(CMath.s_int(old));
					break;
				case DAMAGE: // damage
					if(I instanceof Weapon)
						I.basePhyStats().setDamage(CMath.s_int(old));
					break;
				case MINRANGE: // min range
					if(I instanceof Weapon)
						((Weapon)I).setRanges(CMath.s_int(old),I.maxRange());
					break;
				case MAXRANGE: // max range
					if(I instanceof Weapon)
						((Weapon)I).setRanges(I.minRange(),CMath.s_int(old));
					break;
				case SECRETIDENTITY: // secret identity
					I.setSecretIdentity(old);
					break;
				case ISGETTABLE: // is gettable
					CMLib.flags().setGettable(I,old.equals("on"));
					break;
				case ISREMOVABLE: // is removable
					CMLib.flags().setRemovable(I,old.equals("on"));
					break;
				case ISDROPPABLE: // is droppable
					CMLib.flags().setDroppable(I,old.equals("on"));
					break;
				case ISTWOHANDED: // is two handed
					if((I instanceof Weapon)||(I instanceof Armor))
						I.setRawLogicalAnd(old.equals("on"));
					break;
				case ISTRAPPED: // is trapped
					break;
				case READABLESPELLS: // readable spells
					if(((I instanceof SpellHolder))
					&&(CMClass.classID(I).indexOf("SuperPill")<0))
					{
						final StringBuilder sp=new StringBuilder("");
						if(httpReq.isUrlParameter("RSPELL1"))
						{
							int num=1;
							String aff=httpReq.getUrlParameter("RSPELL"+num);
							String theparm=httpReq.getUrlParameter("RSPDATA"+num);
							while((aff!=null)&&(theparm!=null))
							{
								if(aff.length()>0)
								{
									final Ability B=CMClass.getAbility(aff);
									if(B==null)
										return "Unknown Ability '"+aff+"'.";
									if(sp.length()>0)
										sp.append(";");
									sp.append(B.ID());
									if(theparm.trim().length()>0)
										sp.append("(").append(theparm).append(")");
								}
								num++;
								aff=httpReq.getUrlParameter("RSPELL"+num);
								theparm=httpReq.getUrlParameter("RSPDATA"+num);
							}
						}
						((SpellHolder)I).setSpellList(sp.toString());
					}
					break;
				case ISWAND: // is wand
					break;
				case USESREMAIN: // uses
					I.setUsesRemaining(CMath.s_int(old));
					break;
				case VALUE: // value
					I.setBaseValue(CMath.s_int(old));
					break;
				case WEIGHT: // weight
					I.basePhyStats().setWeight(CMath.s_int(old));
					break;
				case MAPAREAS: // map areas
					if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
					{
						final Vector<String> V=new Vector<String>();
						if(httpReq.isUrlParameter("MAPAREAS"))
						{
							old=httpReq.getUrlParameter("MAPAREAS").trim();
							if(old.length()>0)
								V.add(old);
							for(int i=1;;i++)
							{
								if(httpReq.isUrlParameter("MAPAREAS"+(Integer.toString(i))))
								{
									old=httpReq.getUrlParameter("MAPAREAS"+(Integer.toString(i))).trim();
									if(old.length()>0)
										V.add(old);
								}
								else
									break;
							}
						}
						old = CMParms.toSemicolonListString(V);
						CMLib.flags().setReadable(I,false);
						I.setReadableText(old);
					}
					break;
				case ISPILL: // is pill
					break;
				case ISSUPERPILL: // is super pill
					break;
				case ISPOTION: // is potion
					break;
				case LIQUIDTYPES: // liquid types
					if((I instanceof Drink)&&(!(I instanceof Potion)))
						((Drink)I).setLiquidType(CMath.s_int(old));
					break;
				case AMMOTYPE: // ammo types
					if(I instanceof Ammunition)
						((Ammunition)I).setAmmunitionType(old);
					else
					if((I instanceof AmmunitionWeapon)&&(!(I instanceof Wand)))
						((AmmunitionWeapon)I).setAmmunitionType(old);
					break;
				case AMMOCAP: // ammo capacity
					if((I instanceof AmmunitionWeapon)&&(!(I instanceof Wand)))
					{
						((AmmunitionWeapon)I).setAmmoCapacity(CMath.s_int(old));
						if((((AmmunitionWeapon)I).requiresAmmunition())||(((AmmunitionWeapon)I).ammunitionCapacity()>0))
							((AmmunitionWeapon)I).setAmmoRemaining(CMath.s_int(old));
					}
					break;
				case READABLESPELL: // readable spell
					if(I instanceof Wand)
						((Wand)I).setSpell(CMClass.findAbility(old));
					break;
				case ISMAP: // is map
					break;
				case RIDEABLETYPE: // rideable type
					if(I instanceof Rideable)
						((Rideable)I).setRideBasis(CMath.s_int(old));
					break;
				case MOBSHELD: // mob capacity
					if(I instanceof Rideable)
						((Rideable)I).setRiderCapacity(CMath.s_int(old));
					break;
				case HASALID: // has a lid
					if(I instanceof Container)
						((Container)I).setDoorsNLocks(old.equals("on"),!old.equals("on"),old.equals("on") && ((Container)I).defaultsClosed(),((Container)I).hasALock(),((Container)I).hasALock(),((Container)I).defaultsLocked());
					break;
				case HASALOCK: // has a lock
					if(I instanceof Container)
					{
						final boolean hasALid=((Container)I).hasADoor();
						((Container)I).setDoorsNLocks(hasALid||old.equals("on"),!(hasALid||old.equals("on")),!(hasALid||old.equals("on")),old.equals("on"),old.equals("on"),old.equals("on"));
					}
					break;
				case KEYCODE: // key code
					if((I instanceof Container)&&(((Container)I).hasALock()))
						((Container)I).setKeyName(old);
					break;
				case ISWALLPAPER: // is wallpaper
					break;
				case NOURISHMENT: // nourishment
					if(I instanceof Food)
						((Food)I).setNourishment(CMath.s_int(old));
					break;
				case CONTAINER: // container
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
				case ISLIGHTSOURCE: // is light
					break;
				case DURATION:
					if(I instanceof Light)
						((Light)I).setDuration(CMath.s_int(old));
					break;
				case NONLOCATABLE:
					if(old.equals("on"))
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_UNLOCATABLE);
					else
					if((I.basePhyStats().sensesMask()&PhyStats.SENSE_UNLOCATABLE)>0)
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_UNLOCATABLE);
					break;
				case CONTENTSACCESS:
					if(old.equals("on"))
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_INSIDEACCESSIBLE);
					else
					if((I.basePhyStats().sensesMask()&PhyStats.SENSE_INSIDEACCESSIBLE)>0)
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_INSIDEACCESSIBLE);
					break;
				case BLENDEDVIEW:
					if(old.equals("on"))
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED);
					else
					if((I.basePhyStats().sensesMask()&PhyStats.SENSE_ALWAYSCOMPRESSED)>0)
						I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()-PhyStats.SENSE_ALWAYSCOMPRESSED);
					break;
				case ISKEY: // is key
					break;
				case CONTENTTYPES: // content types
					if((I instanceof Container)&&(httpReq.isUrlParameter("CONTENTTYPES")))
					{
						long content=CMath.s_long(httpReq.getUrlParameter("CONTENTTYPES"));
						if(content>0)
						for(int i=1;;i++)
						{
							if(httpReq.isUrlParameter("CONTENTTYPES"+(Integer.toString(i))))
								content=content|CMath.s_int(httpReq.getUrlParameter("CONTENTTYPES"+(Integer.toString(i))));
							else
								break;
						}
						((Container)I).setContainTypes(content);
					}
					break;
				case ISINSTRUMENT: // is instrument:
					break;
				case INSTRUMENTTYPE: // instrumenttype
					if(I instanceof MusicalInstrument)
						((MusicalInstrument)I).setInstrumentType(CMath.s_int(old));
					break;
				case ISAMMO: // isammo
					break;
				case ISMOBITEM: // is mob type
					break;
				case ISDUST: // is dust
					break;
				case ISPERFUME: // is perfume
					break;
				case SMELLS: // smells
					if(I instanceof Perfume)
						((Perfume)I).setSmellList(old);
					break;
				case IMAGE:
					I.setImage(old);
					break;
				case ISEXIT: // is exit
					break;
				case EXITNAME: // exit name
					if(I instanceof Exit)
						((Exit)I).setExitParams(old,((Exit)I).closeWord(),((Exit)I).openWord(),((Exit)I).closedText());
					break;
				case EXITCLOSEDTEXT: // exit closed text
					if(I instanceof Exit)
						((Exit)I).setExitParams(((Exit)I).doorName(),((Exit)I).closeWord(),((Exit)I).openWord(),old);
					break;
				case NUMCOINS: // numcoins
					if(I instanceof Coins)
						((Coins)I).setNumberOfCoins(CMath.s_long(old));
					break;
				case CURRENCY: // currency
					if(I instanceof Coins)
						((Coins)I).setCurrency(old);
					break;
				case DENOM: // denomination
					if(I instanceof Coins)
						((Coins)I).setDenomination(CMath.s_double(old));
					break;
				case ISRECIPE: // isrecipe
					break;
				case RECIPESKILL: // recipeskill
					if(I instanceof Recipe)
						((Recipe)I).setCommonSkillID(old);
					break;
				case RECIPESKILLHELP: // recipeskillhelp
					break;
				case RECIPEDATA: // recipedata
					if(I instanceof Recipe)
					{
						final String recipeFieldName="RECIPEDATA###";
						int x=0;
						String thisFieldname = CMStrings.replaceAll(recipeFieldName,"###", ""+x);
						final List<String> finalData=new ArrayList<String>();
						while(httpReq.isUrlParameter(thisFieldname))
						{
							old = httpReq.getUrlParameter(thisFieldname);
							if(old.trim().length()>0)
								finalData.add(CMStrings.replaceAll(old,",","\t"));
							thisFieldname = CMStrings.replaceAll(recipeFieldName,"###", ""+(++x));
						}
						final String rAstr=httpReq.getUrlParameter("RECIPESKILL");
						final ItemCraftor rA=(ItemCraftor)CMClass.getAbility(rAstr);
						if(rA==null)
							return CMLib.lang().L("Skill @x1 is not a crafting skill!",rAstr);
						else
						{
							try
							{
								for(String line : finalData)
								{
									CMLib.ableParms().testRecipeParsing(new StringBuffer(line), rA.parametersFormat());
								}
							}
							catch(final CMException cme)
							{
								return cme.getMessage();
							}
						}
						((Recipe)I).setRecipeCodeLines(finalData.toArray(new String[0]));
					}
					break;
				case LAYER: // layer
					if(I instanceof Armor)
						((Armor)I).setClothingLayer(CMath.s_short(old));
					break;
				case SEETHRU: // see-thru
					if(I instanceof Armor)
					{
						if(old.equals("on"))
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()|Armor.LAYERMASK_SEETHROUGH));
						else
						if((((Armor)I).getLayerAttributes()&Armor.LAYERMASK_SEETHROUGH)>0)
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()-Armor.LAYERMASK_SEETHROUGH));
					}
					break;
				case MULTIWEAR: // multi-wear
					if(I instanceof Armor)
					{
						if(old.equals("on"))
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()|Armor.LAYERMASK_MULTIWEAR));
						else
						if((((Armor)I).getLayerAttributes()&Armor.LAYERMASK_MULTIWEAR)>0)
							((Armor)I).setLayerAttributes((short)(((Armor)I).getLayerAttributes()-Armor.LAYERMASK_MULTIWEAR));
					}
					break;
				case ISCATALOGED: // iscataloged
					break;
				case CATARATE: // catarate
					if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setRate(CMath.s_pct(old));
					}
					break;
				case CATALIVE: // catalive
					if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setWhenLive((old.equalsIgnoreCase("on")));
					}
					break;
				case CATAMASK: // catamask
					if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setMaskStr(old);
					}
					break;
				case BITE: // bite
					if(I instanceof Food)
						((Food)I).setBite(CMath.s_int(old));
					break;
				case MAXUSES: // max uses
					if(I instanceof Wand)
						((Wand)I).setMaxUses(CMath.s_int(old));
					break;
				case OPENTICKS: // open ticks
					if((I instanceof CloseableLockable)&&(old.length()>0))
						((CloseableLockable)I).setOpenDelayTicks(CMath.s_int(old));
					break;
				case CATACAT: // catacat
					if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setCategory(old.toUpperCase().trim());
					}
					break;
				case ISPORTAL: // isportal
					break;
				case PUTSTR: // putstr
					if(I instanceof Rideable)
						((Rideable) I).setPutString(old);
					break;
				case MOUNTSTR: // mountstr
					if(I instanceof Rideable)
						((Rideable) I).setMountString(old);
					break;
				case DISMOUNTSTR: // dismountstr
					if(I instanceof Rideable)
						((Rideable) I).setDismountString(old);
					break;
				case STATESTR: // statestr
					if(I instanceof Rideable)
						((Rideable) I).setStateString(old);
					break;
				case STATESUBJSTR: // statesubjstr
					if(I instanceof Rideable)
						((Rideable) I).setStateStringSubject(old);
					break;
				case RIDERSTR: // riderstr
					if(I instanceof Rideable)
						((Rideable) I).setRideString(old);
					break;
				case BEINGWORN:
					break;
				case ISCOIN:
					break;
				case ISWEARANDTEAR:
					break;
				case ISBOARDABLEITEM:
				case ISPRIVATEPROPERTY:
					break;
				case OWNER:
					if(I instanceof PrivateProperty)
						((PrivateProperty)I).setOwnerName(old);
					break;
				case ISELECTRONIC:
					break;
				case ISFOOD:
					break;
				case ISRIDEABLE:
					break;
				case ISSCROLL:
					break;
				case ISUNTWOHANDED:
					break;
				case DEFAULTSCLOSED: // defaultsClosed
					if(I instanceof Container)
						((Container)I).setDoorsNLocks(((Container)I).hasADoor(),((Container)I).isOpen(),old.equals("on"),((Container)I).hasALock(),((Container)I).hasALock(),((Container)I).defaultsLocked());
					break;
				case DEFAULTSLOCKED: // has a lock
					if(I instanceof Container)
						((Container)I).setDoorsNLocks(((Container)I).hasADoor(),((Container)I).isOpen(),((Container)I).defaultsClosed(),((Container)I).hasALock(),((Container)I).hasALock(),old.equals("on"));
					break;
				case ISTECHCOMPONENT:
				case ISSHIPENGINE:
				case ISPANEL:
				case ISFUELCONSUMER:
				case ISPOWERGENERATION:
				case ISSHIPWARCOMP:
				case ISCLOAK:
					break;
				case MANUFACTURER:
					if(I instanceof Electronics)
						((Electronics)I).setManufacturerName(old);
					break;
				case POWCAPACITY:
					if(I instanceof Electronics)
						((Electronics)I).setPowerCapacity(CMath.s_long(old));
					break;
				case POWREMAINING:
					if(I instanceof Electronics)
						((Electronics)I).setPowerRemaining(CMath.s_long(old));
					break;
				case ACTIVATED:
					if(I instanceof Electronics)
						((Electronics)I).activate(old.equalsIgnoreCase("on"));
					break;
				case SWARNUMPORTS:
					if(I instanceof ShipWarComponent)
						((ShipWarComponent)I).setPermittedNumDirections(CMath.s_int(old));
					break;
				case SWARPORTS:
					if(I instanceof ShipWarComponent)
						((ShipWarComponent)I).setPermittedDirections(CMParms.parseEnumList(TechComponent.ShipDir.class,old.toUpperCase(),',').toArray(new TechComponent.ShipDir[0]));
					break;
				case SWARMTYPES:
					if(I instanceof ShipWarComponent)
					{
						final Set<Integer> msgTypes=new TreeSet<Integer>(); 
						if(httpReq.isUrlParameter("SWARMTYPES"))
						{
							msgTypes.add(Integer.valueOf(CMath.s_int(httpReq.getUrlParameter("SWARMTYPES"))));
							for(int i=1;;i++)
							{
								if(httpReq.isUrlParameter("SWARMTYPES"+(Integer.toString(i))))
									msgTypes.add(Integer.valueOf(CMath.s_int(httpReq.getUrlParameter("SWARMTYPES"+(Integer.toString(i))))));
								else
									break;
							}
						}
						final int[] types=new int[msgTypes.size()];
						final Integer[] TYPES=msgTypes.toArray(new Integer[0]);
						for(int i=0;i<types.length;i++)
							types[i]=TYPES[i].intValue();
						((ShipWarComponent)I).setDamageMsgTypes(types);
					}
					break;
				case ISCONSTTHRUST:
					if(I instanceof ShipEngine)
						((ShipEngine)I).setConstantThruster(old.equalsIgnoreCase("on"));
					break;
				case MAXTHRUST:
					if(I instanceof ShipEngine)
						((ShipEngine)I).setMaxThrust(CMath.s_int(old));
					break;
				case MINTHRUST:
					if(I instanceof ShipEngine)
						((ShipEngine)I).setMinThrust(CMath.s_int(old));
					break;
				case AVAILPORTS:
					if(I instanceof ShipEngine)
						((ShipEngine)I).setAvailPorts(CMParms.parseEnumList(TechComponent.ShipDir.class,old.toUpperCase(),',').toArray(new TechComponent.ShipDir[0]));
					break;
				case SPECIMPULSE:
					if(I instanceof ShipEngine)
						((ShipEngine)I).setSpecificImpulse(CMath.s_long(old));
					break;
				case FUELEFFICIENCY:
					if(I instanceof ShipEngine)
						((ShipEngine)I).setFuelEfficiency(CMath.s_pct(old));
					break;
				case INSTALLFACTOR:
					if(I instanceof TechComponent)
						((TechComponent)I).setInstalledFactor((float)CMath.s_pct(old));
					break;
				case RECHARGERATE:
					if(I instanceof TechComponent)
						((TechComponent)I).setRechargeRate((float)CMath.s_parseMathExpression(old));
					break;
				case PANELTYPE:
					if(I instanceof ElecPanel)
					{
						TechType type=(TechType)CMath.s_valueOf(TechType.class,httpReq.getUrlParameter("PANELTYPE"));
						if(type != null)
							((ElecPanel)I).setPanelType(type);
					}
					break;
				case GENAMTPERTICK:
					if(I instanceof PowerGenerator)
						((PowerGenerator)I).setGeneratedAmountPerTick(CMath.s_int(old));
					break;
				case CONSUMEDMATS:
					if(I instanceof FuelConsumer)
					{
						final Set<Integer> consumedFuel=new TreeSet<Integer>(); 
						if(httpReq.isUrlParameter("CONSUMEDMATS"))
						{
							consumedFuel.add(Integer.valueOf(CMath.s_int(httpReq.getUrlParameter("CONSUMEDMATS"))));
							for(int i=1;;i++)
							{
								if(httpReq.isUrlParameter("CONSUMEDMATS"+(Integer.toString(i))))
									consumedFuel.add(Integer.valueOf(CMath.s_int(httpReq.getUrlParameter("CONSUMEDMATS"+(Integer.toString(i))))));
								else
									break;
							}
						}
						final int[] mats=new int[consumedFuel.size()];
						final Integer[] MATS=consumedFuel.toArray(new Integer[0]);
						for(int i=0;i<mats.length;i++)
							mats[i]=MATS[i].intValue();
						((FuelConsumer)I).setConsumedFuelType(mats);
					}
					break;
				case AREAXML:
					if((I instanceof BoardableShip)&&(old.trim().length()>0))
						((BoardableShip)I).setShipArea(CMLib.xml().restoreAngleBrackets(old));
					break;
				case ISBOOK:
					break;
				case MAXPAGES:
					if(I instanceof Book)
						((Book)I).setMaxPages(CMath.s_int(old));
					break;
				case MAXCHARSPAGE:
					if(I instanceof Book)
						((Book)I).setMaxCharsPerPage(CMath.s_int(old));
					break;
				default:
					break;
				}
			}
			if(I.isGeneric()&&(!CMLib.flags().isCataloged(I)))
			{
				String error=GrinderExits.dispositions(I,httpReq,parms);
				if(error.length()>0)
					return error;
				error=GrinderAreas.doAffects(I,httpReq,parms);
				if(error.length()>0)
					return error;
				error=GrinderAreas.doBehavs(I,httpReq,parms);
				if(error.length()>0)
					return error;
			}

			I.recoverPhyStats();
			I.text();
			if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
			{
				final Item I2=CMLib.catalog().getCatalogItem(itemCode.substring(8));
				if((I2!=null)&&(!I.Name().equalsIgnoreCase(I2.Name())))
					I.setName(I2.Name());
				httpReq.addFakeUrlParameter("ITEM",itemCode);
				if(I2==null)
				{
					String category=null;
					if(cataData!=null)
						category=cataData.category();
					CMLib.catalog().addCatalog(category,I);
					Log.infoOut("GrinderItems",whom.Name()+" created catalog ITEM "+I.Name());
				}
				else
				{
					if(cataData!=null)
					{
						final CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(I.Name());
						data.build(cataData.data(null));
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
					if((shopItemCode.equals(mobNum)||shopItemCode.equals("NEW")) && (shopItemCode.length()>0))
						((ShopKeeper)M).getShop().addStoreInventory(I);
					else
						M.addItem(I);
					M.recoverPhyStats();
					if((mobNum==null)||(!mobNum.startsWith("CATALOG-")))
						M.text();
					if(R!=null)
						R.recoverRoomStats();
				}
			}
			else
			if(I!=oldI)
			{
				final ItemPossessor oldOwner=oldI.owner();
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
							final Item I2=R.getItem(i);
							if((I2.container()!=null)
							&&(I2.container()==oldI))
							{
								if(I instanceof Container)
									I2.setContainer((Container)I);
								else
									I2.setContainer(null);
							}
						}
					}
				}
				else
				{
					if((shopItemCode.equals(mobNum)||shopItemCode.equals("NEW")) && (shopItemCode.length()>0))
					{
						((ShopKeeper)M).getShop().delAllStoreInventory(oldI);
						((ShopKeeper)M).getShop().addStoreInventory(I);
					}
					else
					{
						M.delItem(oldI);
						M.addItem(I);
					}
					M.recoverPhyStats();
					if((mobNum==null)||(!mobNum.startsWith("CATALOG-")))
						M.text();
					if(R!=null)
						R.recoverRoomStats();
					for(int i=0;i<M.numItems();i++)
					{
						final Item I2=M.getItem(i);
						if((I2.container()!=null)
						&&(I2.container()==oldI))
						{
							if(I instanceof Container)
								I2.setContainer((Container)I);
							else
								I2.setContainer(null);
						}
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
						RoomData.contributeItems(new XVector<Item>(I));
						httpReq.addFakeUrlParameter("ITEM",RoomData.getItemCode(RoomData.getItemCache(),I));
					}
				}
				else
				{
					CMLib.database().DBUpdateItems(R);
					httpReq.addFakeUrlParameter("ITEM",RoomData.getItemCode(R,I));
					R.startItemRejuv();
				}
			}
			else
			{
				if((httpReq.isUrlParameter("BEINGWORN"))
				&&((httpReq.getUrlParameter("BEINGWORN")).equals("on")))
				{
					// deprecated back to room/mob, where it belongs
					//if(I.amWearingAt(Wearable.IN_INVENTORY))
					//	I.wearEvenIfImpossible(M);
				}
				//else I.wearAt(Wearable.IN_INVENTORY);
				if((R!=null)&&(playerM==null))
				{
					CMLib.database().DBUpdateMOBs(R);
					httpReq.addFakeUrlParameter("MOB",RoomData.getMOBCode(R,M));
				}
				httpReq.addFakeUrlParameter("ITEM",RoomData.getItemCode(M,I));
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
