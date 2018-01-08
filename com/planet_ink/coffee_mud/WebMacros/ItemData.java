package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.grinder.GrinderItems.ItemDataField;

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
public class ItemData extends StdWebMacro
{
	@Override
	public String name()
	{
		return "ItemData";
	}

	public ItemData()
	{
		super();

	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String player=httpReq.getUrlParameter("PLAYER");
		final String last=httpReq.getUrlParameter("ROOM");
		if((last==null)&&(player==null))
			return " @break@";
		final String itemCode=httpReq.getUrlParameter("ITEM");
		if(itemCode==null)
			return "@break@";

		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final String mobNum=httpReq.getUrlParameter("MOB");
		MOB playerM=null;
		Room R=null;
		if(player!=null)
			playerM=CMLib.players().getLoadPlayer(player);
		else
			R=(Room)httpReq.getRequestObjects().get(last);
		if((R==null)&&(player==null)&&(last!=null))
		{
			if(!last.equalsIgnoreCase("ANY"))
			{
				R=MUDGrinder.getRoomObject(httpReq,last);
				if(R==null)
					return "No Room?!";
				CMLib.map().resetRoom(R);
				httpReq.getRequestObjects().put(last,R);
			}
		}
		Item I=null;
		MOB M=null;
		final String sync=("SYNC"+((R!=null)?R.roomID():player));
		synchronized(sync.intern())
		{
			if(R!=null)
				R=CMLib.map().getRoom(R);
			
			if((playerM!=null)&&(R==null))
			{
				I=(Item)httpReq.getRequestObjects().get("PLAYER/"+player+"/"+itemCode);
				if(I==null)
				{
					if(itemCode.equals("NEW"))
						I=CMClass.getItem("GenItem");
					else
						I=RoomData.getItemFromCode(playerM,itemCode);
					if(I!=null)
						httpReq.getRequestObjects().put("PLAYER/"+player+"/"+itemCode,I);
				}
				M=playerM;
			}
			else
			if((mobNum!=null)&&(mobNum.length()>0))
			{
				if(R!=null)
					M=(MOB)httpReq.getRequestObjects().get(R.roomID()+"/"+mobNum);
				else
					M=(MOB)httpReq.getRequestObjects().get(mobNum);
				if(M==null)
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
						return clearWebMacros(str);
					}
					if(R!=null)
						httpReq.getRequestObjects().put(R.roomID()+"/"+mobNum,M);
					else
						httpReq.getRequestObjects().put(mobNum,M);
				}
				if(R!=null)
					I=(Item)httpReq.getRequestObjects().get(R.roomID()+"/"+mobNum+"/"+itemCode);
				else
					I=(Item)httpReq.getRequestObjects().get(mobNum+"/"+itemCode);
				if(I==null)
				{
					if(itemCode.equals("NEW"))
						I=CMClass.getItem("GenItem");
					else
					{
						I=RoomData.getItemFromCode(M,itemCode);
						if(I==null)
							I=RoomData.getItemFromCode((MOB)null,itemCode);
					}
					if(I!=null)
					{
						if(R!=null)
							httpReq.getRequestObjects().put(R.roomID()+"/"+mobNum+"/"+itemCode,I);
						else
							httpReq.getRequestObjects().put(mobNum+"/"+itemCode,I);
					}
				}
			}
			else
			if(R!=null)
			{
				I=(Item)httpReq.getRequestObjects().get(R.roomID()+"/"+itemCode);
				if(I==null)
				{
					if(itemCode.equals("NEW"))
						I=CMClass.getItem("GenItem");
					else
					{
						I=RoomData.getItemFromCode(R,itemCode);
						if(I==null)
							I=RoomData.getItemFromCode((Room)null,itemCode);
					}
					if(I!=null)
						httpReq.getRequestObjects().put(R.roomID()+"/"+itemCode,I);
				}
			}
			else
			{
				I=(Item)httpReq.getRequestObjects().get(itemCode);
				if(I==null)
				{
					if(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
					{
						I=CMLib.catalog().getCatalogItem(itemCode.substring(8));
						if(I==null)
							I=CMClass.getItem("GenItem");
						else
							I=(Item)I.copyOf();
					}
					else
					if(itemCode.equals("NEW"))
						I=CMClass.getItem("GenItem");
					else
						I=RoomData.getItemFromAnywhere(RoomData.getItemCache(),itemCode);
					if(I!=null)
						httpReq.getRequestObjects().put(itemCode,I);
				}
			}
		}

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
						str.append(RoomData.getItemCode(M,I2));
				}
			}
			return clearWebMacros(str);
		}

		final int theme = (R!=null) ? R.getArea().getTheme() : CMProps.getIntVar(CMProps.Int.MUDTHEME);
		
		final Item oldI=I;
		// important generic<->non generic swap!
		final String newClassID=httpReq.getUrlParameter("CLASSES");
		if((newClassID!=null)
		&&(!newClassID.equals(CMClass.classID(I)))
		&&(CMClass.getItem(newClassID)!=null))
		{
			I=CMClass.getItem(newClassID);
			if(I instanceof ArchonOnly)
				I=oldI;
		}

		final boolean changedClass=((httpReq.isUrlParameter("CHANGEDCLASS")
							 &&httpReq.getUrlParameter("CHANGEDCLASS").equals("true"))
							 &&(itemCode.equals("NEW")||itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")));
		final boolean changedLevel=(httpReq.isUrlParameter("CHANGEDLEVEL"))
							 &&(httpReq.getUrlParameter("CHANGEDLEVEL")).equals("true");
		if((changedLevel)&&(I.isGeneric()))
		{
			final int level=CMath.s_int(httpReq.getUrlParameter("LEVEL"));
			final int material=CMath.s_int(httpReq.getUrlParameter("MATERIALS"));
			int hands=1;
			if(httpReq.isUrlParameter("ISTWOHANDED")&&(httpReq.getUrlParameter("ISTWOHANDED").equalsIgnoreCase("on")))
				hands=2;
			Map<String,String> vals=null;
			if(I instanceof Weapon)
			{
				final int wclass=CMath.s_int(httpReq.getUrlParameter("WEAPONCLASS"));
				final int reach=CMath.s_int(httpReq.getUrlParameter("MINRANGE"));

				vals=CMLib.itemBuilder().timsItemAdjustments(I,
															 level,
															 material,
															 hands,
															 wclass,
															 reach,
															 0);
			}
			else
			{
				long worndata=I.rawProperLocationBitmap();
				if(httpReq.isUrlParameter("WORNDATA"))
				{
					worndata=CMath.s_int(httpReq.getUrlParameter("WORNDATA"));
					for(int i=1;;i++)
					{
						if(httpReq.isUrlParameter("WORNDATA"+(Integer.toString(i))))
							worndata=worndata|CMath.s_int(httpReq.getUrlParameter("WORNDATA"+(Integer.toString(i))));
						else
							break;
					}
				}
				vals=CMLib.itemBuilder().timsItemAdjustments(I,
															 level,
															 material,
															 hands,
															 0,
															 0,
															 worndata);
			}
			for(final String key : vals.keySet())
			{
				final String val=vals.get(key);
				httpReq.addFakeUrlParameter(key,val);
			}
		}
		final boolean firstTime=(!httpReq.isUrlParameter("ACTION"))
				||(!(httpReq.getUrlParameter("ACTION")).equals("MODIFYITEM"))
				||changedClass;

		if(I!=null)
		{
			final StringBuffer str=new StringBuffer("");
			for(final ItemDataField o : ItemDataField.values())
			{
				final String parmName=o.name();
				if(parms.containsKey(parmName))
				{
					String old=httpReq.getUrlParameter(parmName);
					if(old==null)
						old="";
					switch(o)
					{
					case NAME: // name
						if(firstTime)
						{
							if((itemCode.equalsIgnoreCase("NEW")||itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-"))
							&&(httpReq.isUrlParameter("NEWITEMNAME")))
								old=httpReq.getUrlParameter("NEWITEMNAME");
							else
								old=I.Name();
						}
						str.append(old);
						break;
					case CLASSES: // classes
						{
							if(firstTime)
								old=CMClass.classID(I);
							Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-ITEMS2:"+parms.containsKey("GENERICONLY")+theme);
							if(sorted==null)
							{
								final List<String> sortMe=new ArrayList<String>();
								CMClass.addAllItemClassNames(sortMe,true,false,parms.containsKey("GENERICONLY"),theme);
								Collections.sort(sortMe);
								sorted=sortMe.toArray();
								Resources.submitResource("MUDGRINDER-ITEMS2:"+parms.containsKey("GENERICONLY")+theme,sorted);
							}
							if(parms.containsKey("CLASSESID"))
								str.append(old);
							else
							{
								boolean found=false;
								for (final Object element : sorted)
								{
									final String cnam=(String)element;
									str.append("<OPTION VALUE=\""+cnam+"\"");
									if(old.equals(cnam))
									{
										str.append(" SELECTED");
										found=true;
									}
									str.append(">"+cnam);
								}
								if(!found)
									str.append("<OPTION SELECTED VALUE=\""+old+"\">"+old);
							}
						}
						break;
					case DISPLAYTEXT: // displaytext
						if(firstTime)
							old=I.displayText();
						str.append(old);
						break;
					case DESCRIPTION: // description
						if(firstTime)
							old=I.description();
						str.append(old);
						break;
					case LEVEL: // level
						if(firstTime)
							old=""+I.basePhyStats().level();
						str.append(old);
						break;
					case ABILITY: // ability;
						if(firstTime)
							old=""+I.basePhyStats().ability();
						str.append(old);
						break;
					case REJUV: // rejuv;
						if(firstTime)
							old=""+I.basePhyStats().rejuv();
						if(old.equals(""+Integer.MAX_VALUE))
							str.append("0");
						else
							str.append(old);
						break;
					case MISCTEXT: // misctext
						if(firstTime)
							old=I.text();
						str.append(old);
						break;
					case MATERIALS: // materials
						if(firstTime)
							old=""+I.material();
						for(final int r : RawMaterial.CODES.ALL_SBN())
						{
							str.append("<OPTION VALUE=\""+r+"\"");
							if(r==CMath.s_int(old))
								str.append(" SELECTED");
							str.append(">"+RawMaterial.CODES.NAME(r));
						}
						break;
					case ISGENERIC: // is generic
						if(I.isGeneric())
							return "true";
						return "false";
					case ISFOOD: // is food
						if(I instanceof Food)
							return "true";
						return "false";
					case NOURISHMENT: // nourishment
						if((firstTime)&&(I instanceof Food))
							old=""+((Food)I).nourishment();
						str.append(old);
						break;
					case ISDRINK: // is drink
						if(I instanceof Drink)
							return "true";
						return "false";
					case LIQUIDHELD: // liquid held
						if((firstTime)&&(I instanceof Drink))
							old=""+((Drink)I).liquidHeld();
						str.append(old);
						break;
					case QUENCHED: // quenched
						if((firstTime)&&(I instanceof Drink))
							old=""+((Drink)I).thirstQuenched();
						str.append(old);
						break;
					case ISCONTAINER: // is container
						if(I instanceof Container)
							return "true";
						return "false";
					case CAPACITY: // capacity
						if((firstTime)&&(I instanceof Container))
							old=""+((Container)I).capacity();
						str.append(old);
						break;
					case ISARMOR: // is armor
						if(I instanceof Armor)
							return "true";
						return "false";
					case ARMOR: // armor
						if(firstTime)
							old=""+I.basePhyStats().armor();
						str.append(old);
						break;
					case WORNDATA: // worn data
					{
						long climate = I.rawProperLocationBitmap();
						if (httpReq.isUrlParameter("WORNDATA"))
						{
							climate = CMath.s_int(httpReq.getUrlParameter("WORNDATA"));
							for (int i = 1;; i++)
							{
								if (httpReq.isUrlParameter("WORNDATA" + (Integer.toString(i))))
									climate = climate | CMath.s_int(httpReq.getUrlParameter("WORNDATA" + (Integer.toString(i))));
								else
									break;
							}
						}
						final Wearable.CODES codes = Wearable.CODES.instance();
						for (int i = 1; i < codes.total(); i++)
						{
							final String climstr = codes.name(i);
							final long mask = codes.get(i);
							str.append("<OPTION VALUE=" + mask);
							if ((climate & mask) > 0)
								str.append(" SELECTED");
							str.append(">" + climstr);
						}
						break;
					}
					case HEIGHT: // height
						if(firstTime)
							old=""+I.basePhyStats().height();
						str.append(old);
						break;
					case ISWEAPON: // is weapon
						if(I instanceof Weapon)
							return "true";
						return "false";
					case WEAPONTYPE: // weapon type
						if((firstTime)&&(I instanceof Weapon))
							old=""+((Weapon)I).weaponDamageType();
						for(int r=0;r<Weapon.TYPE_DESCS.length;r++)
						{
							str.append("<OPTION VALUE=\""+r+"\"");
							if(r==CMath.s_int(old))
								str.append(" SELECTED");
							str.append(">"+Weapon.TYPE_DESCS[r]);
						}
						break;
					case WEAPONCLASS: // weapon class
						if((firstTime)&&(I instanceof Weapon))
							old=""+((Weapon)I).weaponClassification();
						for(int r=0;r<Weapon.CLASS_DESCS.length;r++)
						{
							str.append("<OPTION VALUE=\""+r+"\"");
							if(r==CMath.s_int(old))
								str.append(" SELECTED");
							str.append(">"+Weapon.CLASS_DESCS[r]);
						}
						break;
					case ATTACK: // attack
						if(firstTime)
							old=""+I.basePhyStats().attackAdjustment();
						str.append(old);
						break;
					case DAMAGE: // damage
						if(firstTime)
							old=""+I.basePhyStats().damage();
						str.append(old);
						break;
					case MINRANGE: // min range
						if(firstTime)
							old=""+I.minRange();
						str.append(old);
						break;
					case MAXRANGE: // max range
						if(firstTime)
							old=""+I.maxRange();
						str.append(old);
						break;
					case SECRETIDENTITY: // secret identity
						if(firstTime)
							old=I.rawSecretIdentity();
						str.append(old);
						break;
					case ISGETTABLE: // is gettable
						if(firstTime)
							old=(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET))?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISREMOVABLE: // is removable
						if(firstTime)
							old=(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE))?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISDROPPABLE: // is droppable
						if(firstTime)
							old=(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP))?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISUNTWOHANDED: // is untwo handed
						if(firstTime)
							old=I.rawLogicalAnd()?"":"checked";
						else
						if(old.equals(""))
							old="checked";
						str.append(old);
						break;
					case ISTRAPPED: // is trapped
						break;
					case READABLESPELLS: // readable spells
						if(I instanceof SpellHolder)
							str.append(readableSpells((SpellHolder)I,httpReq,parms,1));
						break;
					case ISWAND: // is wand
						if(I instanceof Wand)
							return "true";
						return "false";
					case USESREMAIN: // uses
						if(firstTime)
							old=""+I.usesRemaining();
						str.append(old);
						break;
					case VALUE: // value
						if(firstTime)
							old=""+I.baseGoldValue();
						str.append(old);
						break;
					case WEIGHT: // weight
						if(firstTime)
							old=""+I.basePhyStats().weight();
						str.append(old);
						break;
					case ISMAP: // is map
						if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
							return "true";
						return "false";
					case ISCLOAK: // is cloak
						return Boolean.toString(I.ID().equalsIgnoreCase("GenCloak"));
					case MAPAREAS: // map areas
					{
						String mask=";"+I.readableText();
						if(httpReq.isUrlParameter("MAPAREAS"))
						{
							mask=";"+httpReq.getUrlParameter("MAPAREAS");
							for(int i=1;;i++)
							{
								if(httpReq.isUrlParameter("MAPAREAS"+(Integer.toString(i))))
									mask+=";"+httpReq.getUrlParameter("MAPAREAS"+(Integer.toString(i)));
								else
									break;
							}
						}
						mask=mask.toUpperCase()+";";
						for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
						{
							final Area A2=a.nextElement();
							if(!(A2 instanceof SpaceObject))
							{
								str.append("<OPTION VALUE=\""+A2.Name()+"\"");
								if(mask.indexOf(";"+A2.Name().toUpperCase()+";")>=0)
									str.append(" SELECTED");
								str.append(">"+A2.name());
							}
						}
						break;
					}
					case ISREADABLE: // is readable
						if(firstTime)
							old=(CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE))?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISPILL: // is pill
						if(I instanceof Pill)
							return "true";
						return "false";
					case ISSUPERPILL: // is super pill
						if((I instanceof Pill)&&(CMClass.classID(I).indexOf("SuperPill")>0))
							return "true";
						return "false";
					case ISPOTION: // is potion
						if(I instanceof Potion)
							return "true";
						return "false";
					case LIQUIDTYPES: // liquid types
						if((firstTime)&&(I instanceof Drink))
							old=""+((Drink)I).liquidType();
						final List<Integer> liquids=RawMaterial.CODES.COMPOSE_RESOURCES(RawMaterial.MATERIAL_LIQUID);
						for(final Integer liquid : liquids)
						{
							str.append("<OPTION VALUE=\""+liquid.intValue()+"\"");
							if(liquid.intValue()==CMath.s_int(old))
								str.append(" SELECTED");
							str.append(">"+RawMaterial.CODES.NAME(liquid.intValue()));
						}
						break;
					case AMMOTYPE: // ammo types
						if(firstTime)
						{
							if(I instanceof Ammunition)
								old=""+((Ammunition)I).ammunitionType();
							else
							if(I instanceof AmmunitionWeapon)
								old=""+((AmmunitionWeapon)I).ammunitionType();
						}
						str.append(old);
						break;
					case AMMOCAP: // ammo capacity
						if((firstTime)&&(I instanceof AmmunitionWeapon))
							old=""+((AmmunitionWeapon)I).ammunitionCapacity();
						str.append(old);
						break;
					case READABLESPELL: // readable spell
					{
						if((firstTime)&&(I instanceof Wand))
							old=""+((((Wand)I).getSpell()!=null)?((Wand)I).getSpell().ID():"");
						for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
						{
							final Ability A=a.nextElement();
							if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)!=Ability.DOMAIN_ARCHON))
							{
								final String cnam=A.ID();
								str.append("<OPTION VALUE=\""+cnam+"\"");
								if(old.equals(cnam))
									str.append(" SELECTED");
								str.append(">"+cnam);
							}
						}
						break;
					}
					case ISRIDEABLE: // is rideable
						if(I instanceof Rideable)
							return "true";
						return "false";
					case RIDEABLETYPE: // rideable type
						if((firstTime)&&(I instanceof Rideable))
							old=""+((Rideable)I).rideBasis();
						for(int r=0;r<Rideable.RIDEABLE_DESCS.length;r++)
						{
							str.append("<OPTION VALUE=\""+r+"\"");
							if(r==CMath.s_int(old))
								str.append(" SELECTED");
							str.append(">"+Rideable.RIDEABLE_DESCS[r]);
						}
						break;
					case MOBSHELD: // mobsheld rideable capacity
						if((firstTime)&&(I instanceof Rideable))
							old=""+((Rideable)I).riderCapacity();
						str.append(old);
						break;
					case HASALID: // has a lid
						if((firstTime)&&(I instanceof Container))
							old=((Container)I).hasADoor()?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case HASALOCK: // has a lock
						if((firstTime)&&(I instanceof Container))
							old=((Container)I).hasALock()?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case KEYCODE: // key code
						if((firstTime)&&(I instanceof Container))
							old=""+((Container)I).keyName();
						str.append(old);
						break;
					case ISWALLPAPER: // is wallpaper
						if(CMClass.classID(I).indexOf("Wallpaper")>=0)
							return "true";
						return "false";
					case READABLETEXT: // readabletext
						if(firstTime)
							old=""+I.readableText();
						if(I instanceof BoardableShip)
						{
							final BoardableShip ship=(BoardableShip)I;
							if((ship.getShipArea()!=null)
							&&(ship.getShipArea().getRoom(old)==null)
							&&(ship.getShipArea().getProperMap().hasMoreElements()))
								old=ship.getShipArea().getProperMap().nextElement().roomID();
						}
						str.append(old);
						break;
					case CONTAINER:
						// pushed back to room/mob, where it belongs
						//str.append(container(R,M,oldI,I,old,firstTime));
						break;
					case ISLIGHTSOURCE: // is light
						if(I instanceof Light)
							return "true";
						return "false";
					case DURATION:
						if((firstTime)&&(I instanceof Light))
							old=""+((Light)I).getDuration();
						str.append(old);
						break;
					case ISTWOHANDED: // is two handed
						if(firstTime)
							old=I.rawLogicalAnd()?"checked":"";
						else
						{
							old=httpReq.getUrlParameter("ISTWOHANDED");
							if(old==null)
								old="";
							if(old.equals("on"))
								old="checked";
						}
						str.append(old);
						break;
					case ISCOIN:
						if(I instanceof Coins)
							return "true";
						return "false";
					case ISWEARANDTEAR:
						return I.subjectToWearAndTear()?"true":"false";
					case ISSCROLL:
						if(I instanceof Scroll)
							return "true";
						return "false";
					case BEINGWORN: // being worn -- pushed back to mob/room
						if(firstTime)
							old=I.amWearingAt(Wearable.IN_INVENTORY)?"":"checked";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case NONLOCATABLE: // non-locatable
						if(firstTime)
							old=CMLib.flags().canBeLocated(I)?"":"checked";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case CONTENTSACCESS:
						if(firstTime)
							old=CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_INSIDEACCESSIBLE)?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case BLENDEDVIEW:
						if(firstTime)
							old=CMath.bset(I.basePhyStats().sensesMask(), PhyStats.SENSE_ALWAYSCOMPRESSED)?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISKEY: // is key
						if(I instanceof DoorKey)
							return "true";
						return "false";
					case CONTENTTYPES: // content types
						if(I instanceof Container)
						{
							long contains=((Container)I).containTypes();
							if(httpReq.isUrlParameter("CONTENTTYPES"))
							{
								contains=CMath.s_long(httpReq.getUrlParameter("CONTENTTYPES"));
								if(contains>0)
								for(int i=1;;i++)
								{
									if(httpReq.isUrlParameter("CONTENTTYPES"+(Integer.toString(i))))
										contains=contains|CMath.s_int(httpReq.getUrlParameter("CONTENTTYPES"+(Integer.toString(i))));
									else
										break;
								}
							}
							str.append("<OPTION VALUE=0");
							if(contains==0)
								str.append(" SELECTED");
							str.append(">"+Container.CONTAIN_DESCS[0]);
							for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
							{
								final String constr=Container.CONTAIN_DESCS[i];
								final int mask=(int)CMath.pow(2,i-1);
								str.append("<OPTION VALUE="+mask);
								if((contains&mask)>0)
									str.append(" SELECTED");
								str.append(">"+constr);
							}
						}
						break;
					case ISINSTRUMENT: // is instrument
						if(I instanceof MusicalInstrument)
							return "true";
						return "false";
					case INSTRUMENTTYPE: // instrument types
						if((firstTime)&&(I instanceof MusicalInstrument))
							old=""+((MusicalInstrument)I).getInstrumentType().ordinal();
						for(int r=0;r<InstrumentType.values().length;r++)
						{
							str.append("<OPTION VALUE=\""+r+"\"");
							if(r==CMath.s_int(old))
								str.append(" SELECTED");
							str.append(">"+InstrumentType.values()[r].name());
						}
						break;
					case ISAMMO: // is ammunition
						if(I instanceof Ammunition)
							return "true";
						return "false";
					case ISMOBITEM: // is mob item
						if(M!=null)
							return "true";
						return "false";
					case ISDUST: // is dust
						if(I instanceof MagicDust)
							return "true";
						return "false";
					case ISPERFUME: // is perfume
						if(I instanceof Perfume)
							return "true";
						return "false";
					case SMELLS: // smells
						if((firstTime)&&(I instanceof Perfume))
							old=""+((Perfume)I).getSmellList();
						str.append(old);
						break;
					case IMAGE: // image
						if(firstTime)
							old=I.rawImage();
						str.append(old);
						break;
					case ISEXIT: // is exit
						if(I instanceof Exit)
							return "true";
						return "false";
					case EXITNAME: // exit name
						if((firstTime)&&(I instanceof Exit))
							old=""+((Exit)I).doorName();
						str.append(old);
						break;
					case EXITCLOSEDTEXT: // exit closed text
						if((firstTime)&&(I instanceof Exit))
							old=""+((Exit)I).closedText();
						str.append(old);
						break;
					case NUMCOINS: // numcoins
						if((firstTime)&&(I instanceof Coins))
							old=""+((Coins)I).getNumberOfCoins();
						str.append(old);
						break;
					case CURRENCY: // currency
					{
						if((firstTime)&&(I instanceof Coins))
							old=""+((Coins)I).getCurrency();
						final List<String> cs=CMLib.beanCounter().getAllCurrencies();
						str.append("<OPTION VALUE=\"\"");
						if(old.length()==0)
							str.append(" SELECTED");
						str.append(L(">Default currency"));
						for(int i=0;i<cs.size();i++)
						{
							if(cs.get(i).length()>0)
							{
								str.append("<OPTION VALUE=\""+(cs.get(i))+"\"");
								if(cs.get(i).equalsIgnoreCase(old))
									str.append(" SELECTED");
								str.append(">"+(cs.get(i)));
							}
						}
						break;
					}
					case DENOM: // denomination
					{
						String currency=(I instanceof Coins)?currency=((Coins)I).getCurrency():"";
						if((firstTime)&&(I instanceof Coins))
							old=""+((Coins)I).getDenomination();
						final MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(currency);
						for (final MoneyDenomination element : DV)
						{
							str.append("<OPTION VALUE=\""+element.value()+"\"");
							if(element.value()==CMath.s_double(old))
								str.append(" SELECTED");
							str.append(">"+element.name());
						}
						break;
					}
					case ISRECIPE: // isrecipe
						if(I instanceof Recipe)
							return "true";
						return "false";
					case RECIPESKILL: // recipeskill
					{
						Ability A=null;
						if((firstTime)&&(I instanceof Recipe))
							old=""+((Recipe)I).getCommonSkillID();
						for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
						{
							A=e.nextElement();
							if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
							&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL))
							{
								str.append("<OPTION VALUE=\""+A.ID()+"\"");
								if(A.ID().equalsIgnoreCase(old))
									str.append(" SELECTED");
								str.append(">"+A.name());
							}
						}
						break;
					}
					case RECIPESKILLHELP: // recipeskillhelp
					{
						Ability A=null;
						if((firstTime)&&(I instanceof Recipe))
						{
							A=CMClass.getAbility(((Recipe)I).getCommonSkillID());
							if(A==null)
							{
								for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
								{
									Ability A2=a.nextElement();
									if(A2 instanceof ItemCraftor)
									{
										A=A2;
										break;
									}
								}
							}
						}
						else
							A=CMClass.getAbility(httpReq.getUrlParameter("RECIPESKILL"));
						if(A instanceof ItemCraftor)
							str.append(((ItemCraftor)A).parametersFormat()).append(", ");
						break;
					}
					case RECIPEDATA: // recipedata
						if(I instanceof Recipe)
						{
							String prefix=parms.get("RECIPEPREFIX");
							if(prefix==null)
								prefix="";
							String postfix=parms.get("RECIPEPOSTFIX");
							if(postfix==null)
								postfix="";
							final String fieldName=parms.get("RECIPEFIELDNAME");
							if(fieldName==null)
								str.append("!!ERROR!!");
							else
							{
								String thisFieldName=CMStrings.replaceAll(fieldName,"###","0");
								final List<String> allData=new LinkedList<String>();
								int x=0;
								if(httpReq.isUrlParameter(thisFieldName))
								{
									while(httpReq.isUrlParameter(thisFieldName))
									{
										final String value=httpReq.getUrlParameter(thisFieldName);
										if(value.length()>0)
											allData.add(value);
										thisFieldName=CMStrings.replaceAll(fieldName,"###",""+(++x));
									}
								}
								else
								{
									final String[] allRecipes=((Recipe)I).getRecipeCodeLines();
									for(final String recipe : allRecipes)
									{
										if(recipe.length()>0)
											allData.add(CMStrings.replaceAll(recipe,"\t",","));
									}
								}
								allData.add("");
								for(x=0;x<allData.size();x++)
								{
									final String recipeLine=allData.get(x);
									thisFieldName=CMStrings.replaceAll(fieldName,"###",""+x);
									final String myPrefix=CMStrings.replaceAll(prefix,fieldName,thisFieldName);
									final String myPostfix=CMStrings.replaceAll(postfix,fieldName,thisFieldName);
									str.append(myPrefix).append(super.htmlOutgoingFilter(recipeLine)).append(myPostfix);
								}
							}
						}
						break;
					case LAYER: // layer
						if((firstTime)&&(I instanceof Armor))
							old=""+((Armor)I).getClothingLayer();
						str.append(old);
						break;
					case SEETHRU: // see-thru
						if((firstTime)&&(I instanceof Armor))
							old=CMath.bset(((Armor)I).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH)?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case MULTIWEAR: // multiwear multi-layer
						if((firstTime)&&(I instanceof Armor))
							old=CMath.bset(((Armor)I).getLayerAttributes(),Armor.LAYERMASK_MULTIWEAR)?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISCATALOGED: // iscataloged
						str.append(""+CMLib.flags().isCataloged(I));
						break;
					case CATARATE: // catarate
						if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
						{
							final String name=itemCode.substring(8);
							final CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
							if(data!=null)
								old=CMath.toPct(data.getRate());
						}
						str.append(old+", ");
						break;
					case CATALIVE: // catalive
						if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
						{
							final String name=itemCode.substring(8);
							final CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
							if(data!=null)
								old=data.getWhenLive()?"on":"";
						}
						str.append(((old.equalsIgnoreCase("on"))?"CHECKED":"")+", ");
						break;
					case CATAMASK: // catamask
						if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
						{
							final String name=itemCode.substring(8);
							final CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
							if(data!=null)
								old=""+data.getMaskStr();
						}
						str.append(htmlOutgoingFilter(old)+", ");
						break;
					case BITE: // bite
						if((firstTime)&&(I instanceof Food))
							old=""+((Food)I).bite();
						str.append(old);
						break;
					case OPENTICKS: // open ticks
						if(((firstTime)&&(I instanceof CloseableLockable))||(old.length()==0))
							old=""+((CloseableLockable)I).openDelayTicks();
						str.append(old);
						break;
					case MAXUSES: // max uses
						if((firstTime)&&(I instanceof Wand))
							old=""+((Wand)I).maxUses();
						str.append(old);
						break;
					case ISELECTRONIC: // iselectronic
						str.append(I instanceof Electronics);
						break;
					case CATACAT: // catacat
						if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
						{
							final String name=itemCode.substring(8);
							final CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
							if(data!=null)
								old=data.category();
						}
						str.append(old+", ");
						break;
					case ISPORTAL: // isportal
						return (((I instanceof Rideable)&&(I instanceof Exit))?"true":"false");
					case PUTSTR: // putstr
						if((firstTime)&&(I instanceof Rideable))
							old=((Rideable)I).putString(CMClass.sampleMOB());
						str.append(old+", ");
						break;
					case MOUNTSTR: // mountstr
						if((firstTime)&&(I instanceof Rideable))
							old=((Rideable)I).mountString(0,CMClass.sampleMOB());
						str.append(old+", ");
						break;
					case DISMOUNTSTR: // dismountstr
						if((firstTime)&&(I instanceof Rideable))
							old=((Rideable)I).dismountString(CMClass.sampleMOB());
						str.append(old+", ");
						break;
					case STATESTR: // statestr
						if((firstTime)&&(I instanceof Rideable))
							old=((Rideable)I).stateString(CMClass.sampleMOB());
						str.append(old+", ");
						break;
					case STATESUBJSTR: // statesubjstr
						if((firstTime)&&(I instanceof Rideable))
							old=((Rideable)I).stateStringSubject(CMClass.sampleMOB());
						str.append(old+", ");
						break;
					case RIDERSTR: // riderstr
						if((firstTime)&&(I instanceof Rideable))
							old=((Rideable)I).rideString(CMClass.sampleMOB());
						str.append(old+", ");
						break;
					case DEFAULTSCLOSED: // defaults closed
						if((firstTime)&&(I instanceof Container))
							old=((Container)I).defaultsClosed()?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case DEFAULTSLOCKED: // has a lock
						if((firstTime)&&(I instanceof Container))
							old=((Container)I).defaultsLocked()?"checked":"";
						else
						if(old.equals("on"))
							old="checked";
						str.append(old);
						break;
					case ISBOARDABLEITEM:
						str.append(I instanceof BoardableShip);
						break;
					case ISPRIVATEPROPERTY:
						str.append(I instanceof PrivateProperty);
						break;
					case OWNER:
						if((firstTime)&&(I instanceof PrivateProperty))
							old=((PrivateProperty)I).getOwnerName();
						str.append(old);
						break;
					case ISTECHCOMPONENT:
						str.append(I instanceof TechComponent);
						break;
					case ISSHIPENGINE:
						str.append(I instanceof ShipEngine);
						break;
					case ISSHIPWARCOMP:
						str.append(I instanceof ShipWarComponent);
						break;
					case ISPANEL:
						str.append((I instanceof ElecPanel)&&(!(I instanceof Computer)));
						break;
					case ISFUELCONSUMER:
						str.append(I instanceof FuelConsumer);
						break;
					case ISPOWERGENERATION:
						str.append(I instanceof PowerGenerator);
						break;
					case MANUFACTURER:
						if(I instanceof Electronics)
						{
							if(firstTime)
								old=((Electronics)I).getManufacturerName();
							str.append("<OPTION VALUE=\"RANDOM");
							if(old.equalsIgnoreCase("RANDOM"))
								str.append("\" SELECTED>Random");
							else
								str.append("\">Random");
							for(Iterator<Manufacturer> m = CMLib.tech().manufacterers(); m.hasNext(); )
							{
								Manufacturer M1=m.next();
								if(M1.isManufactureredType((Technical)I) || (old.equalsIgnoreCase(M1.name())))
								{
									str.append("<OPTION VALUE=\""+M1.name());
									if(old.equalsIgnoreCase(M1.name()))
										str.append("\" SELECTED>"+M1.name());
									else
										str.append("\">"+M1.name());
								}
							}
						}
						break;
					case POWCAPACITY:
						if(I instanceof Electronics)
							str.append((firstTime) ? (""+((Electronics)I).powerCapacity()) : old).append(", ");
						break;
					case POWREMAINING:
						if(I instanceof Electronics)
							str.append((firstTime) ? (""+((Electronics)I).powerRemaining()) : old).append(", ");
						break;
					case ACTIVATED:
						if(I instanceof Electronics)
							str.append((firstTime) ? (((Electronics)I).activated()?"CHECKED":"") : (old.equalsIgnoreCase("on")?"CHECKED":"")).append(", ");
						break;
					case SWARNUMPORTS:
						if(I instanceof ShipWarComponent)
							str.append((firstTime) ? (""+((ShipWarComponent)I).getPermittedNumDirections()) : old).append(", ");
						break;
					case SWARPORTS:
						if(I instanceof ShipWarComponent)
							str.append((firstTime) ? (""+CMParms.toListString(((ShipWarComponent)I).getPermittedDirections())) : old).append(", ");
						break;
					case SWARMTYPES:
						if(I instanceof ShipWarComponent)
						{
							final Set<Integer> msgTypes=new TreeSet<Integer>(); 
							if(firstTime)
							{
								for(int typ : ((ShipWarComponent)I).getDamageMsgTypes())
									msgTypes.add(Integer.valueOf(typ));
							}
							else
							{
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
							}
							for(final int r : ShipWarComponent.AVAIL_DAMAGE_TYPES)
							{
								str.append("<OPTION VALUE=\""+r+"\"");
								if(msgTypes.contains(Integer.valueOf(r)))
									str.append(" SELECTED");
								str.append(">"+CMMsg.TYPE_DESCS[r]);
							}
						}
						str.append(", ");
						break;
					case ISCONSTTHRUST:
						if(I instanceof ShipEngine)
							str.append((firstTime) ? (((ShipEngine)I).isConstantThruster()?"CHECKED":"") : (old.equalsIgnoreCase("on")?"CHECKED":"")).append(", ");
						break;
					case MAXTHRUST:
						if(I instanceof ShipEngine)
							str.append((firstTime) ? (""+((ShipEngine)I).getMaxThrust()) : old).append(", ");
						break;
					case MINTHRUST:
						if(I instanceof ShipEngine)
							str.append((firstTime) ? (""+((ShipEngine)I).getMinThrust()) : old).append(", ");
						break;
					case AVAILPORTS:
						if(I instanceof ShipEngine)
							str.append((firstTime) ? (""+CMParms.toListString(((ShipEngine)I).getAvailPorts())) : old).append(", ");
						break;
					case SPECIMPULSE:
						if(I instanceof ShipEngine)
							str.append((firstTime) ? (""+((ShipEngine)I).getSpecificImpulse()) : old).append(", ");
						break;
					case FUELEFFICIENCY:
						if(I instanceof ShipEngine)
							str.append((firstTime) ? CMath.toPct(((ShipEngine)I).getFuelEfficiency()) : old).append(", ");
						break;
					case INSTALLFACTOR:
						if(I instanceof TechComponent)
							str.append((firstTime) ? CMath.toPct(((TechComponent)I).getInstalledFactor()) : old).append(", ");
						break;
					case RECHARGERATE:
						if(I instanceof TechComponent)
							str.append((firstTime) ? CMath.toPct(((TechComponent)I).getRechargeRate()) : old).append(", ");
						break;
					case PANELTYPE:
						if(I instanceof ElecPanel)
						{
							TechType type=((ElecPanel)I).panelType();
							if(httpReq.isUrlParameter("PANELTYPE"))
								type=(TechType)CMath.s_valueOf(TechType.class,httpReq.getUrlParameter("PANELTYPE"));
							for(TechType t : TechType.values())
							{
								str.append("<OPTION VALUE="+t.toString());
								if(t==type)
									str.append(" SELECTED");
								str.append(">"+t.getDisplayName());
							}
						}
						str.append(", ");
						break;
					case GENAMTPERTICK:
						if(I instanceof PowerGenerator)
							str.append((firstTime) ? (""+((PowerGenerator)I).getGeneratedAmountPerTick()) : old).append(", ");
						break;
					case CONSUMEDMATS:
						if(I instanceof FuelConsumer)
						{
							final Set<Integer> consumedFuel=new TreeSet<Integer>(); 
							if(firstTime)
							{
								for(int mat : ((FuelConsumer)I).getConsumedFuelTypes())
									consumedFuel.add(Integer.valueOf(mat));
							}
							else
							{
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
							}
							for(final int r : RawMaterial.CODES.ALL_SBN())
							{
								str.append("<OPTION VALUE=\""+r+"\"");
								if(consumedFuel.contains(Integer.valueOf(r)))
									str.append(" SELECTED");
								str.append(">"+RawMaterial.CODES.NAME(r));
							}
						}
						break;
					case AREAXML:
						if(I instanceof BoardableShip)
						{
							str.append((firstTime) ? 
								CMLib.xml().parseOutAngleBracketsAndQuotes(CMLib.coffeeMaker().getAreaObjectXML(((BoardableShip)I).getShipArea(), null, null, null, true).toString()) :
								old).append(", ");
						}
						break;
					case ISBOOK:
						str.append(""+(I instanceof Book));
						break;
					case MAXPAGES:
						if(I instanceof Book)
						{
							if(firstTime)
								old=""+((Book)I).getMaxPages();
						}
						str.append(old).append(", ");
						break;
					case MAXCHARSPAGE:
						if(I instanceof Book)
						{
							if(firstTime)
								old=""+((Book)I).getMaxCharsPerPage();
						}
						str.append(old).append(", ");
						break;
					}
					if(firstTime)
						httpReq.addFakeUrlParameter(parmName,old.equals("checked")?"on":old);
				}
			}
			str.append(ExitData.dispositions(I,firstTime,httpReq,parms));
			str.append(AreaData.affects(I,httpReq,parms,1));
			str.append(AreaData.behaves(I,httpReq,parms,1));
			I.setContainer(oldI.container());
			I.setRawWornCode(oldI.rawWornCode());

			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return clearWebMacros(strstr);
		}
		return "";
	}

	public static StringBuffer readableSpells(SpellHolder P, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		final StringBuffer str=new StringBuffer("");
		if(parms.containsKey("READABLESPELLS"))
		{
			final ArrayList<String> theclasses=new ArrayList<String>();
			final ArrayList<String> theparms=new ArrayList<String>();
			if(httpReq.isUrlParameter("RSPELL1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("RSPELL"+num);
				String theparm=httpReq.getUrlParameter("RSPDATA"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(behav.length()>0)
					{
						theclasses.add(behav);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.add(t);
					}
					num++;
					behav=httpReq.getUrlParameter("RSPELL"+num);
					theparm=httpReq.getUrlParameter("RSPDATA"+num);
				}
			}
			else
			{
				final List<Ability> SP=P.getSpells();
				for(int a=0;a<SP.size();a++) // readable spells
				{
					final Ability Able=SP.get(a);
					if((Able!=null)&&(Able.isSavable()))
					{
						theclasses.add(CMClass.classID(Able));
						String t=Able.text();
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.add(t);
					}
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			final HashSet<String> alreadyHave=new HashSet<String>();
			for(int i=0;i<theclasses.size();i++)
			{
				final String theclass=theclasses.get(i);
				alreadyHave.add(theclass.toLowerCase());
				final String theparm=theparms.get(i);
				str.append("<TR><TD WIDTH=50%>");
				str.append("<SELECT ONCHANGE=\"EditAffect(this);\" NAME=RSPELL"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+theclass+"\" SELECTED>"+theclass);
				str.append("</SELECT>");
				str.append("</TD><TD WIDTH=50%>");
				str.append("<INPUT TYPE=TEXT SIZE=30 NAME=RSPDATA"+(i+1)+" VALUE=\""+theparm+"\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=50%>");
			str.append("<SELECT ONCHANGE=\"AddAffect(this);\" NAME=RSPELL"+(theclasses.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select an Effect");
			for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(((!A.canAffect(P))||(alreadyHave.contains(A.ID().toLowerCase())))
				||((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ARCHON))
					continue;
				str.append("<OPTION VALUE=\""+A.ID()+"\">"+A.ID());
			}
			str.append("</SELECT>");
			str.append("</TD><TD WIDTH=50%>");
			str.append("<INPUT TYPE=TEXT SIZE=30 NAME=RSPDATA"+(theclasses.size()+1)+" VALUE=\"\">");
			str.append("</TD></TR>");
			str.append("</TABLE>");
		}
		return str;
	}
}
