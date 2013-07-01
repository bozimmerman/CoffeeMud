package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ItemData extends StdWebMacro
{
	public String name() { return "ItemData"; }

	private static final String[] okparms={
	  "NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
	  "LEVEL","ABILITY","REJUV","MISCTEXT",
	  "MATERIALS","ISGENERIC","ISFOOD","NOURISHMENT",
	  "ISDRINK","LIQUIDHELD","QUENCHED","ISCONTAINER",
	  "CAPACITY","ISARMOR","ARMOR","WORNDATA",
	  "HEIGHT","ISWEAPON","WEAPONTYPE","WEAPONCLASS",
	  "ATTACK","DAMAGE","MINRANGE","MAXRANGE",
	  "SECRETIDENTITY","ISGETTABLE","ISREMOVABLE","ISDROPPABLE",
	  "ISTWOHANDED","ISTRAPPED","READABLESPELLS","ISWAND",
	  "USESREMAIN","VALUE","WEIGHT","ISMAP",
	  "MAPAREAS","ISREADABLE","ISPILL","ISSUPERPILL",
	  "ISPOTION","LIQUIDTYPES","AMMOTYPE","AMMOCAP",
	  "READABLESPELL","ISRIDEABLE","RIDEABLETYPE","MOBSHELD",
	  "HASALID","HASALOCK","KEYCODE","ISWALLPAPER",
	  "READABLETEXT","CONTAINER","ISLIGHTSOURCE","DURATION",
	  "ISUNTWOHANDED","ISCOIN","ISSCROLL","BEINGWORN","NONLOCATABLE",
	  "ISKEY", "CONTENTTYPES","ISINSTRUMENT","INSTRUMENTTYPE",
	  "ISAMMO","ISMOBITEM","ISDUST","ISPERFUME","SMELLS",
	  "IMAGE","ISEXIT","EXITNAME","EXITCLOSEDTEXT","NUMCOINS",
	  "CURRENCY","DENOM","ISRECIPE","RECIPESKILL","RECIPEDATA",
	  "LAYER","SEETHRU","MULTIWEAR","ISCATALOGED","CATARATE",
	  "CATALIVE","CATAMASK","BITE","MAXUSES","ISELECTRONIC"};
	
	public ItemData()
	{
		super();

	}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String player=httpReq.getUrlParameter("PLAYER");
		String last=httpReq.getUrlParameter("ROOM");
		if((last==null)&&(player==null)) return " @break@";
		String itemCode=httpReq.getUrlParameter("ITEM");
		if(itemCode==null) return "@break@";

		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		String mobNum=httpReq.getUrlParameter("MOB");
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
				R=CMLib.map().getRoom(last);
				if(R==null)
					return "No Room?!";
				CMLib.map().resetRoom(R);
				httpReq.getRequestObjects().put(last,R);
			}
		}
		Item I=null;
		MOB M=null;
		String sync=("SYNC"+((R!=null)?R.roomID():player));
		synchronized(sync.intern())
		{
			if(R!=null) R=CMLib.map().getRoom(R);

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
						StringBuffer str=new StringBuffer("No MOB?!");
						str.append(" Got: "+mobNum);
						str.append(", Includes: ");
						if(R!=null)
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M2=R.fetchInhabitant(m);
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
						I=RoomData.getItemFromCode(M,itemCode);
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
						I=RoomData.getItemFromCode(R,itemCode);
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
			StringBuffer str=new StringBuffer("No Item?!");
			str.append(" Got: "+itemCode);
			str.append(", Includes: ");
			if(M==null)
			{
				if(R!=null)
				for(int i=0;i<R.numItems();i++)
				{
					Item I2=R.getItem(i);
					if(I2!=null) str.append(I2.Name()+"="+RoomData.getItemCode(R,I2));
				}
			}
			else
				for(int i=0;i<M.numItems();i++)
				{
					Item I2=M.getItem(i);
					if(I2!=null) str.append(RoomData.getItemCode(M,I2));
				}
			return clearWebMacros(str);
		}

		Item oldI=I;
		// important generic<->non generic swap!
		String newClassID=httpReq.getUrlParameter("CLASSES");
		if((newClassID!=null)
		&&(!newClassID.equals(CMClass.classID(I)))
		&&(CMClass.getItem(newClassID)!=null))
		{
			I=CMClass.getItem(newClassID);
			if(I instanceof ArchonOnly) I=oldI;
		}

		boolean changedClass=((httpReq.isUrlParameter("CHANGEDCLASS")
							 &&httpReq.getUrlParameter("CHANGEDCLASS").equals("true"))
							 &&(itemCode.equals("NEW")||itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")));
		boolean changedLevel=(httpReq.isUrlParameter("CHANGEDLEVEL"))
							 &&(httpReq.getUrlParameter("CHANGEDLEVEL")).equals("true");
		if((changedLevel)&&(I.isGeneric()))
		{
			int level=CMath.s_int(httpReq.getUrlParameter("LEVEL"));
			int material=CMath.s_int(httpReq.getUrlParameter("MATERIALS"));
			int hands=1;
			if(httpReq.isUrlParameter("ISTWOHANDED")&&(httpReq.getUrlParameter("ISTWOHANDED").equalsIgnoreCase("on")))
			   hands=2;
			Map<String,String> vals=null;
			if(I instanceof Weapon)
			{
				int wclass=CMath.s_int(httpReq.getUrlParameter("WEAPONCLASS"));
				int reach=CMath.s_int(httpReq.getUrlParameter("MINRANGE"));

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
						if(httpReq.isUrlParameter("WORNDATA"+(Integer.toString(i))))
							worndata=worndata|CMath.s_int(httpReq.getUrlParameter("WORNDATA"+(Integer.toString(i))));
						else
							break;
				}
				vals=CMLib.itemBuilder().timsItemAdjustments(I,
															 level,
															 material,
															 hands,
															 0,
															 0,
															 worndata);
			}
			for(String key : vals.keySet())
			{
				String val=vals.get(key);
				httpReq.addFakeUrlParameter(key,val);
			}
		}
		boolean firstTime=(!httpReq.isUrlParameter("ACTION"))
				||(!(httpReq.getUrlParameter("ACTION")).equals("MODIFYITEM"))
				||changedClass;

		if(I!=null)
		{
			StringBuffer str=new StringBuffer("");
			for(int o=0;o<okparms.length;o++)
			if(parms.containsKey(okparms[o]))
			{
				String old=httpReq.getUrlParameter(okparms[o]);
				if(old==null) old="";
				switch(o)
				{
				case 0: // name
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
				case 1: // classes
					{
						if(firstTime) old=CMClass.classID(I);
						Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-ITEMS2:"+parms.containsKey("GENERICONLY"));
						if(sorted==null)
						{
							Vector sortMe=new Vector();
							CMClass.addAllItemClassNames(sortMe,true,false,parms.containsKey("GENERICONLY"));
							sorted=(new TreeSet(sortMe)).toArray();
							Resources.submitResource("MUDGRINDER-ITEMS2:"+parms.containsKey("GENERICONLY"),sorted);
						}
						if(parms.containsKey("CLASSESID"))
							str.append(old);
						else
						for(int r=0;r<sorted.length;r++)
						{
							String cnam=(String)sorted[r];
							str.append("<OPTION VALUE=\""+cnam+"\"");
							if(old.equals(cnam))
								str.append(" SELECTED");
							str.append(">"+cnam);
						}
					}
					break;
				case 2: // displaytext
					if(firstTime) old=I.displayText();
					str.append(old);
					break;
				case 3: // description
					if(firstTime) old=I.description();
					str.append(old);
					break;
				case 4: // level
					if(firstTime) old=""+I.basePhyStats().level();
					str.append(old);
					break;
				case 5: // ability;
					if(firstTime) old=""+I.basePhyStats().ability();
					str.append(old);
					break;
				case 6: // rejuv;
					if(firstTime) old=""+I.basePhyStats().rejuv();
					if(old.equals(""+Integer.MAX_VALUE))
						str.append("0");
					else
						str.append(old);
					break;
				case 7: // misctext
					if(firstTime) old=I.text();
					str.append(old);
					break;
				case 8: // materials
					if(firstTime) old=""+I.material();
					for(int r : RawMaterial.CODES.ALL_SBN())
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==CMath.s_int(old))
							str.append(" SELECTED");
						str.append(">"+RawMaterial.CODES.NAME(r));
					}
					break;
				case 9: // is generic
					if(I.isGeneric())
						return "true";
					return "false";
				case 10: // is food
					if(I instanceof Food) return "true";
					return "false";
				case 11: // nourishment
					if((firstTime)&&(I instanceof Food))
						old=""+((Food)I).nourishment();
					str.append(old);
					break;
				case 12: // is drink
					if(I instanceof Drink) return "true";
					return "false";
				case 13: // liquid held
					if((firstTime)&&(I instanceof Drink))
						old=""+((Drink)I).liquidHeld();
					str.append(old);
					break;
				case 14: // quenched
					if((firstTime)&&(I instanceof Drink))
						old=""+((Drink)I).thirstQuenched();
					str.append(old);
					break;
				case 15: // is container
					if(I instanceof Container) return "true";
					return "false";
				case 16: // capacity
					if((firstTime)&&(I instanceof Container))
						old=""+((Container)I).capacity();
					str.append(old);
					break;
				case 17: // is armor
					if(I instanceof Armor) return "true";
					return "false";
				case 18: // armor
					if(firstTime) old=""+I.basePhyStats().armor();
					str.append(old);
					break;
				case 19: // worn data
					{
					long climate=I.rawProperLocationBitmap();
					if(httpReq.isUrlParameter("WORNDATA"))
					{
						climate=CMath.s_int(httpReq.getUrlParameter("WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.isUrlParameter("WORNDATA"+(Integer.toString(i))))
								climate=climate|CMath.s_int(httpReq.getUrlParameter("WORNDATA"+(Integer.toString(i))));
							else
								break;
					}
					Wearable.CODES codes = Wearable.CODES.instance();
					for(int i=1;i<codes.total();i++)
					{
						String climstr=codes.name(i);
						long mask=codes.get(i);
						str.append("<OPTION VALUE="+mask);
						if((climate&mask)>0) str.append(" SELECTED");
						str.append(">"+climstr);
					}
					}
					break;
				case 20: // height
					if(firstTime) old=""+I.basePhyStats().height();
					str.append(old);
					break;
				case 21: // is weapon
					if(I instanceof Weapon) return "true";
					return "false";
				case 22: // weapon type
					if((firstTime)&&(I instanceof Weapon))
						old=""+((Weapon)I).weaponType();
					for(int r=0;r<Weapon.TYPE_DESCS.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==CMath.s_int(old))
							str.append(" SELECTED");
						str.append(">"+Weapon.TYPE_DESCS[r]);
					}
					break;
				case 23: // weapon class
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
				case 24: // attack
					if(firstTime) old=""+I.basePhyStats().attackAdjustment();
					str.append(old);
					break;
				case 25: // damage
					if(firstTime) old=""+I.basePhyStats().damage();
					str.append(old);
					break;
				case 26: // min range
					if(firstTime) old=""+I.minRange();
					str.append(old);
					break;
				case 27: // max range
					if(firstTime) old=""+I.maxRange();
					str.append(old);
					break;
				case 28: // secret identity
					if(firstTime) old=I.rawSecretIdentity();
					str.append(old);
					break;
				case 29: // is gettable
					if(firstTime)
						old=(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 30: // is removable
					if(firstTime)
						old=(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 31: // is droppable
					if(firstTime)
						old=(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 32: // is two handed
					if(firstTime)
						old=I.rawLogicalAnd()?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 33: // is trapped
					break;
				case 34: // readable spells
					if(I instanceof SpellHolder)
						str.append(readableSpells((SpellHolder)I,httpReq,parms,1));
					break;
				case 35: // is wand
					if(I instanceof Wand) return "true";
					return "false";
				case 36: // uses
					if(firstTime) old=""+I.usesRemaining();
					str.append(old);
					break;
				case 37: // value
					if(firstTime) old=""+I.baseGoldValue();
					str.append(old);
					break;
				case 38: // weight
					if(firstTime) old=""+I.basePhyStats().weight();
					str.append(old);
					break;
				case 39: // is map
					if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap) return "true";
					return "false";
				case 40: // map areas
					{
					String mask=";"+I.readableText();
					if(httpReq.isUrlParameter("MAPAREAS"))
					{
						mask=";"+httpReq.getUrlParameter("MAPAREAS");
						for(int i=1;;i++)
							if(httpReq.isUrlParameter("MAPAREAS"+(Integer.toString(i))))
								mask+=";"+httpReq.getUrlParameter("MAPAREAS"+(Integer.toString(i)));
							else
								break;
					}
					mask=mask.toUpperCase()+";";
					for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
					{
						Area A2=(Area)a.nextElement();
						str.append("<OPTION VALUE=\""+A2.Name()+"\"");
						if(mask.indexOf(";"+A2.Name().toUpperCase()+";")>=0) str.append(" SELECTED");
						str.append(">"+A2.name());
					}
					}
					break;
				case 41: // is readable
					if(firstTime)
						old=(CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 42: // is pill
					if(I instanceof Pill) return "true";
					return "false";
				case 43: // is super pill
					if((I instanceof Pill)&&(CMClass.classID(I).indexOf("SuperPill")>0)) return "true";
					return "false";
				case 44: // is potion
					if(I instanceof Potion) return "true";
					return "false";
				case 45: // liquid types
					if((firstTime)&&(I instanceof Drink))
						old=""+((Drink)I).liquidType();
					List<Integer> liquids=RawMaterial.CODES.COMPOSE_RESOURCES(RawMaterial.MATERIAL_LIQUID);
					for(Integer liquid : liquids)
					{
						str.append("<OPTION VALUE=\""+liquid.intValue()+"\"");
						if(liquid.intValue()==CMath.s_int(old))
							str.append(" SELECTED");
						str.append(">"+RawMaterial.CODES.NAME(liquid.intValue()));
					}
					break;
				case 46: // ammo types
					if(firstTime)
					{
						if(I instanceof Ammunition)
							old=""+((Ammunition)I).ammunitionType();
						else
						if(I instanceof Weapon)
							old=""+((Weapon)I).ammunitionType();
					}
					str.append(old);
					break;
				case 47: // ammo capacity
					if((firstTime)&&(I instanceof Weapon))
						old=""+((Weapon)I).ammunitionCapacity();
					str.append(old);
					break;
				case 48: // readable spell
					{
						if((firstTime)&&(I instanceof Wand))
							old=""+((((Wand)I).getSpell()!=null)?((Wand)I).getSpell().ID():"");
						for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
						{
							String cnam=a.nextElement().ID();
							str.append("<OPTION VALUE=\""+cnam+"\"");
							if(old.equals(cnam))
								str.append(" SELECTED");
							str.append(">"+cnam);
						}
					}
					break;
				case 49: // is rideable
					if(I instanceof Rideable) return "true";
					return "false";
				case 50: // rideable type
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
				case 51: // rideable capacity
					if((firstTime)&&(I instanceof Rideable))
						old=""+((Rideable)I).riderCapacity();
					str.append(old);
					break;
				case 52: // has a lid
					if((firstTime)&&(I instanceof Container))
						old=((Container)I).hasALid()?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 53: // has a lock
					if((firstTime)&&(I instanceof Container))
						old=((Container)I).hasALock()?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 54: // key code
					if((firstTime)&&(I instanceof Container))
						old=""+((Container)I).keyName();
					str.append(old);
					break;
				case 55: // is wallpaper
					if(CMClass.classID(I).indexOf("Wallpaper")>=0)
						return "true";
					return "false";
				case 56: // readabletext
					if(firstTime) old=""+I.readableText();
					str.append(old);
					break;
				case 57:
					// pushed back to room/mob, where it belongs
					//str.append(container(R,M,oldI,I,old,firstTime));
					break;
				case 58: // is light
					if(I instanceof Light) return "true";
					return "false";
				case 59:
					if((firstTime)&&(I instanceof Light))
						old=""+((Light)I).getDuration();
					str.append(old);
					break;
				case 60: // is two handed
					if(firstTime)
						old=I.rawLogicalAnd()?"":"checked";
					else
					{
						old=httpReq.getUrlParameter("ISTWOHANDED");
						if(old==null) old="";
						if(old.equals(""))
							old="checked";
					}
					str.append(old);
					break;
				case 61:
					if(I instanceof Coins) return "true";
					return "false";
				case 62:
					if(I instanceof Scroll) return "true";
					return "false";
				case 63: // being worn -- pushed back to mob/room
					if(firstTime)
						old=I.amWearingAt(Wearable.IN_INVENTORY)?"":"checked";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 64: // non-locatable
					if(firstTime)
						old=CMLib.flags().canBeLocated(I)?"":"checked";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 65: // is key
					if(I instanceof DoorKey) return "true";
					return "false";
				case 66: // content types
					if(I instanceof Container)
					{
						long contains=((Container)I).containTypes();
						if(httpReq.isUrlParameter("CONTENTTYPES"))
						{
							contains=CMath.s_long(httpReq.getUrlParameter("CONTENTTYPES"));
							if(contains>0)
							for(int i=1;;i++)
								if(httpReq.isUrlParameter("CONTENTTYPES"+(Integer.toString(i))))
									contains=contains|CMath.s_int(httpReq.getUrlParameter("CONTENTTYPES"+(Integer.toString(i))));
								else
									break;
						}
						str.append("<OPTION VALUE=0");
						if(contains==0) str.append(" SELECTED");
						str.append(">"+Container.CONTAIN_DESCS[0]);
						for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
						{
							String constr=Container.CONTAIN_DESCS[i];
							int mask=(int)CMath.pow(2,i-1);
							str.append("<OPTION VALUE="+mask);
							if((contains&mask)>0) str.append(" SELECTED");
							str.append(">"+constr);
						}
					}
					break;
				case 67: // is instrument
					if(I instanceof MusicalInstrument) return "true";
					return "false";
				case 68: // instrument types
					if((firstTime)&&(I instanceof MusicalInstrument))
						old=""+((MusicalInstrument)I).instrumentType();
					for(int r=0;r<MusicalInstrument.TYPE_DESC.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==CMath.s_int(old))
							str.append(" SELECTED");
						str.append(">"+MusicalInstrument.TYPE_DESC[r]);
					}
					break;
				case 69: // is ammunition
					if(I instanceof Ammunition) return "true";
					return "false";
				case 70: // is mob item
					if(M!=null) return "true";
					return "false";
				case 71: // is dust
					if(I instanceof MagicDust) return "true";
					return "false";
				case 72: // is perfume
					if(I instanceof Perfume) return "true";
					return "false";
				case 73: // smells
					if((firstTime)&&(I instanceof Perfume))
						old=""+((Perfume)I).getSmellList();
					str.append(old);
					break;
				case 74: // image
					if(firstTime)
						old=I.rawImage();
					str.append(old);
					break;
				case 75: // is exit
					if(I instanceof Exit) return "true";
					return "false";
				case 76: // exit name
					if((firstTime)&&(I instanceof Exit))
						old=""+((Exit)I).doorName();
					str.append(old);
					break;
				case 77: // exit closed text
					if((firstTime)&&(I instanceof Exit))
						old=""+((Exit)I).closedText();
					str.append(old);
					break;
				case 78: // numcoins
					if((firstTime)&&(I instanceof Coins))
						old=""+((Coins)I).getNumberOfCoins();
					str.append(old);
					break;
				case 79: // currency
				{
					if((firstTime)&&(I instanceof Coins))
						old=""+((Coins)I).getCurrency();
					List<String> cs=CMLib.beanCounter().getAllCurrencies();
					str.append("<OPTION VALUE=\"\"");
					if(old.length()==0)
						str.append(" SELECTED");
					str.append(">Default currency");
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
				case 80: // denomination
				{
					String currency=(I instanceof Coins)?currency=((Coins)I).getCurrency():"";
					if((firstTime)&&(I instanceof Coins))
						old=""+((Coins)I).getDenomination();
					MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(currency);
					for(int i=0;i<DV.length;i++)
					{
						str.append("<OPTION VALUE=\""+DV[i].value+"\"");
						if(DV[i].value==CMath.s_double(old))
							str.append(" SELECTED");
						str.append(">"+DV[i].name);
					}
					break;
				}
				case 81: // isrecipe
					if(I instanceof Recipe) return "true";
					return "false";
				case 82: // recipeskill
				{
					Ability A=null;
					if((firstTime)&&(I instanceof Recipe))
						old=""+((Recipe)I).getCommonSkillID();
					for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
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
				case 83: // recipedata
					if(I instanceof Recipe)
					{
						String prefix=parms.get("RECIPEPREFIX");
						if(prefix==null) prefix="";
						String postfix=parms.get("RECIPEPOSTFIX");
						if(postfix==null) postfix="";
						String fieldName=parms.get("RECIPEFIELDNAME");
						if(fieldName==null)
							str.append("!!ERROR!!");
						else
						{
							String thisFieldName=CMStrings.replaceAll(fieldName,"###","0");
							List<String> allData=new LinkedList<String>();
							int x=0;
							if(httpReq.isUrlParameter(thisFieldName))
							{
								while(httpReq.isUrlParameter(thisFieldName))
								{
									String value=httpReq.getUrlParameter(thisFieldName);
									if(value.length()>0)
										allData.add(value);
									thisFieldName=CMStrings.replaceAll(fieldName,"###",""+(++x));
								}
							}
							else
							{
								String[] allRecipes=((Recipe)I).getRecipeCodeLines();
								for(String recipe : allRecipes)
								{
									if(recipe.length()>0)
										allData.add(CMStrings.replaceAll(recipe,"\t",","));
								}
							}
							allData.add("");
							for(x=0;x<allData.size();x++)
							{
								String recipeLine=allData.get(x);
								thisFieldName=CMStrings.replaceAll(fieldName,"###",""+x);
								String myPrefix=CMStrings.replaceAll(prefix,fieldName,thisFieldName);
								String myPostfix=CMStrings.replaceAll(postfix,fieldName,thisFieldName);
								str.append(myPrefix).append(super.htmlOutgoingFilter(recipeLine)).append(myPostfix);
							}
						}
					}
					break;
				case 84: // layer
					if((firstTime)&&(I instanceof Armor))
						old=""+((Armor)I).getClothingLayer();
					str.append(old);
					break;
				case 85: // see-thru
					if((firstTime)&&(I instanceof Armor))
						old=CMath.bset(((Armor)I).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH)?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 86: // multi-layer
					if((firstTime)&&(I instanceof Armor))
						old=CMath.bset(((Armor)I).getLayerAttributes(),Armor.LAYERMASK_MULTIWEAR)?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 87: // iscataloged
					str.append(""+CMLib.flags().isCataloged(I));
					break;
				case 88: // catarate
					if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
					{
						String name=itemCode.substring(8);
						CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
						if(data!=null)
							old=CMath.toPct(data.getRate());
					}
					str.append(old+", ");
					break;
				case 89: // catalive
					if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
					{
						String name=itemCode.substring(8);
						CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
						if(data!=null)
							old=data.getWhenLive()?"on":"";
					}
					str.append(((old.equalsIgnoreCase("on"))?"CHECKED":"")+", ");
					break;
				case 90: // catamask
					if((firstTime)&&(itemCode.startsWith("CATALOG-")||itemCode.startsWith("NEWCATA-")))
					{
						String name=itemCode.substring(8);
						CatalogLibrary.CataData data=CMLib.catalog().getCatalogItemData(name);
						if(data!=null)
							old=""+data.getMaskStr();
					}
					str.append(htmlOutgoingFilter(old)+", ");
					break;
				case 91: // bite
					if((firstTime)&&(I instanceof Food))
						old=""+((Food)I).bite();
					str.append(old);
					break;
				case 92: // max uses
					if((firstTime)&&(I instanceof Wand))
						old=""+((Wand)I).maxUses();
					str.append(old);
					break;
				case 93: // iselectronic
					str.append(I instanceof Electronics);
					break;
				}
				if(firstTime)
					httpReq.addFakeUrlParameter(okparms[o],old.equals("checked")?"on":old);
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
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("READABLESPELLS"))
		{
			Vector theclasses=new Vector();
			Vector theparms=new Vector();
			if(httpReq.isUrlParameter("RSPELL1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("RSPELL"+num);
				String theparm=httpReq.getUrlParameter("RSPDATA"+num);
				while((behav!=null)&&(theparm!=null))
				{
					if(behav.length()>0)
					{
						theclasses.addElement(behav);
						String t=theparm;
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
					num++;
					behav=httpReq.getUrlParameter("RSPELL"+num);
					theparm=httpReq.getUrlParameter("RSPDATA"+num);
				}
			}
			else
			{
				List<Ability> SP=P.getSpells();
				for(int a=0;a<SP.size();a++) // readable spells
				{
					Ability Able=SP.get(a);
					if((Able!=null)&&(Able.isSavable()))
					{
						theclasses.addElement(CMClass.classID(Able));
						String t=Able.text();
						t=CMStrings.replaceAll(t,"\"","&quot;");
						theparms.addElement(t);
					}
				}
			}
			str.append("<TABLE WIDTH=100% BORDER=\""+borderSize+"\" CELLSPACING=0 CELLPADDING=0>");
			HashSet<String> alreadyHave=new HashSet<String>();
			for(int i=0;i<theclasses.size();i++)
			{
				String theclass=(String)theclasses.elementAt(i);
				alreadyHave.add(theclass.toLowerCase());
				String theparm=(String)theparms.elementAt(i);
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
			for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=a.nextElement();
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
