package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ItemData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ROOM");
		if(last==null) return " @break@";
		String itemCode=httpReq.getRequestParameter("ITEM");
		if(itemCode==null) return "@break@";

		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return CommonStrings.getVar(CommonStrings.SYSTEM_MUDSTATUS);

		String mobNum=httpReq.getRequestParameter("MOB");

		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			R=CMMap.getRoom(last);
			if(R==null)
				return "No Room?!";
			CoffeeUtensils.resetRoom(R);
			httpReq.getRequestObjects().put(last,R);
		}

		Item I=null;
		MOB M=null;
		if((mobNum!=null)&&(mobNum.length()>0))
		{
			M=(MOB)httpReq.getRequestObjects().get(R.roomID()+"/"+mobNum);
			if(M==null)
			{
				M=RoomData.getMOBFromCode(R,mobNum);
				if(M==null)
				{
					StringBuffer str=new StringBuffer("No MOB?!");
					str.append(" Got: "+mobNum);
					str.append(", Includes: ");
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M2=R.fetchInhabitant(m);
						if((M2!=null)&&(M2.isEligibleMonster()))
						   str.append(M2.Name()+"="+RoomData.getMOBCode(R,M2));
					}
					return str.toString();
				}
				else
					httpReq.getRequestObjects().put(R.roomID()+"/"+mobNum,M);
			}
			I=(Item)httpReq.getRequestObjects().get(R.roomID()+"/"+mobNum+"/"+itemCode);
			if(I==null)
			{
				if(itemCode.equals("NEW"))
					I=CMClass.getItem("GenItem");
				else
					I=RoomData.getItemFromCode(M,itemCode);
				if(I!=null)
					httpReq.getRequestObjects().put(R.roomID()+"/"+mobNum+"/"+itemCode,I);
			}
		}
		else
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

		if(I==null)
		{
			StringBuffer str=new StringBuffer("No Item?!");
			str.append(" Got: "+itemCode);
			str.append(", Includes: ");
			if(M==null)
				for(int i=0;i<R.numItems();i++)
				{
					Item I2=R.fetchItem(i);
					if(I2!=null) str.append(I2.Name()+"="+RoomData.getItemCode(R,I2));
				}
			else
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I2=M.fetchInventory(i);
					if(I2!=null) str.append(RoomData.getItemCode(M,I2));
				}
			return str.toString();
		}

		Item oldI=I;
		// important generic<->non generic swap!
		String newClassID=httpReq.getRequestParameter("CLASSES");
		if((newClassID!=null)
		&&(!newClassID.equals(CMClass.className(I)))
		&&(CMClass.getItem(newClassID)!=null))
		{
			I=CMClass.getItem(newClassID);
			if(I instanceof ArchonOnly) I=oldI;
		}

		boolean changedClass=(((httpReq.isRequestParameter("CHANGEDCLASS"))&&(httpReq.getRequestParameter("CHANGEDCLASS")).equals("true"))&&(itemCode.equals("NEW")));
		boolean changedLevel=(httpReq.isRequestParameter("CHANGEDLEVEL"))&&(httpReq.getRequestParameter("CHANGEDLEVEL")).equals("true");
		if((changedLevel)&&(I.isGeneric()))
		{
			int level=Util.s_int(httpReq.getRequestParameter("LEVEL"));
			int material=Util.s_int(httpReq.getRequestParameter("MATERIALS"));
			int weight=Util.s_int(httpReq.getRequestParameter("WEIGHT"));
			int hands=1;
			if(httpReq.isRequestParameter("ISTWOHANDED")&&(httpReq.getRequestParameter("ISTWOHANDED").equalsIgnoreCase("on")))
			   hands=2;
			Hashtable vals=null;
			if(I instanceof Weapon)
			{
				int wclass=Util.s_int(httpReq.getRequestParameter("WEAPONCLASS"));
				int reach=Util.s_int(httpReq.getRequestParameter("MINRANGE"));

				vals=CoffeeMaker.timsItemAdjustments(I,
													level,
													material,
													weight,
													hands,
													wclass,
													reach,
													0);
			}
			else
			{
				long worndata=I.rawProperLocationBitmap();
				if(httpReq.isRequestParameter("WORNDATA"))
				{
					worndata=Util.s_int(httpReq.getRequestParameter("WORNDATA"));
					for(int i=1;;i++)
						if(httpReq.isRequestParameter("WORNDATA"+(new Integer(i).toString())))
							worndata=worndata|Util.s_int(httpReq.getRequestParameter("WORNDATA"+(new Integer(i).toString())));
						else
							break;
				}
				vals=CoffeeMaker.timsItemAdjustments(I,
													level,
													material,
													weight,
													hands,
													0,
													0,
													worndata);
			}
			for(Enumeration e=vals.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				String val=(String)vals.get(key);
				httpReq.addRequestParameters(key,val);
			}
		}
		boolean firstTime=(!httpReq.isRequestParameter("ACTION"))
				||(!(httpReq.getRequestParameter("ACTION")).equals("MODIFYITEM"))
				||changedClass;

		if(I!=null)
		{
			StringBuffer str=new StringBuffer("");
			String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
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
							  "ISAMMO","ISMOBITEM"};
			for(int o=0;o<okparms.length;o++)
			if(parms.containsKey(okparms[o]))
			{
				String old=httpReq.getRequestParameter(okparms[o]);
				String oldold=old;
				if(old==null) old="";
				switch(o)
				{
				case 0: // name
					if(firstTime) old=I.Name();
					str.append(old);
					break;
				case 1: // classes
					{
						if(firstTime) old=CMClass.className(I);
						Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-ITEMS2");
						if(sorted==null)
						{
							Vector sortMe=new Vector();
							CMClass.addAllItemClassNames(sortMe,true,false);
							sorted=(Object[])(new TreeSet(sortMe)).toArray();
							Resources.submitResource("MUDGRINDER-ITEMS2",sorted);
						}
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
					if(firstTime) old=""+I.baseEnvStats().level();
					str.append(old);
					break;
				case 5: // ability;
					if(firstTime) old=""+I.baseEnvStats().ability();
					str.append(old);
					break;
				case 6: // rejuv;
					if(firstTime) old=""+I.baseEnvStats().rejuv();
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
					for(int r=0;r<EnvResource.RESOURCE_DESCS.length;r++)
					{
						str.append("<OPTION VALUE=\""+EnvResource.RESOURCE_DATA[r][0]+"\"");
						if(EnvResource.RESOURCE_DATA[r][0]==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+EnvResource.RESOURCE_DESCS[r]);
					}
					break;
				case 9: // is generic
					if(I.isGeneric()) return "true";
					else return "false";
				case 10: // is food
					if(I instanceof Food) return "true";
					else return "false";
				case 11: // nourishment
					if((firstTime)&&(I instanceof Food))
						old=""+((Food)I).nourishment();
					str.append(old);
					break;
				case 12: // is drink
					if(I instanceof Drink) return "true";
					else return "false";
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
					else return "false";
				case 16: // capacity
					if((firstTime)&&(I instanceof Container))
						old=""+((Container)I).capacity();
					str.append(old);
					break;
				case 17: // is armor
					if(I instanceof Armor) return "true";
					else return "false";
				case 18: // armor
					if(firstTime) old=""+I.baseEnvStats().armor();
					str.append(old);
					break;
				case 19: // worn data
					{
					long climate=I.rawProperLocationBitmap();
					if(httpReq.isRequestParameter("WORNDATA"))
					{
						climate=Util.s_int(httpReq.getRequestParameter("WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.isRequestParameter("WORNDATA"+(new Integer(i).toString())))
								climate=climate|Util.s_int(httpReq.getRequestParameter("WORNDATA"+(new Integer(i).toString())));
							else
								break;
					}
					for(int i=1;i<Item.wornLocation.length;i++)
					{
						String climstr=Item.wornLocation[i];
						int mask=Util.pow(2,i-1);
						str.append("<OPTION VALUE="+mask);
						if((climate&mask)>0) str.append(" SELECTED");
						str.append(">"+climstr);
					}
					}
					break;
				case 20: // height
					if(firstTime) old=""+I.baseEnvStats().height();
					str.append(old);
					break;
				case 21: // is weapon
					if(I instanceof Weapon) return "true";
					else return "false";
				case 22: // weapon type
					if((firstTime)&&(I instanceof Weapon))
						old=""+((Weapon)I).weaponType();
					for(int r=0;r<Weapon.typeDescription.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+Weapon.typeDescription[r]);
					}
					break;
				case 23: // weapon class
					if((firstTime)&&(I instanceof Weapon))
						old=""+((Weapon)I).weaponClassification();
					for(int r=0;r<Weapon.classifictionDescription.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+Weapon.classifictionDescription[r]);
					}
					break;
				case 24: // attack
					if(firstTime) old=""+I.baseEnvStats().attackAdjustment();
					str.append(old);
					break;
				case 25: // damage
					if(firstTime) old=""+I.baseEnvStats().damage();
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
						old=(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 30: // is removable
					if(firstTime)
						old=(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 31: // is droppable
					if(firstTime)
						old=(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))?"checked":"";
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
					{
						if(I instanceof Potion)
							old=";"+((Potion)I).getSpellList();
						if(I instanceof Pill)
							old=";"+((Pill)I).getSpellList();
						if(I instanceof Scroll)
							old=";"+((Scroll)I).getScrollText();
						if(httpReq.isRequestParameter("READABLESPELLS"))
						{
							old=";"+httpReq.getRequestParameter("READABLESPELLS");
							for(int i=1;;i++)
								if(httpReq.isRequestParameter("READABLESPELLS"+(new Integer(i).toString())))
									old+=";"+httpReq.getRequestParameter("READABLESPELLS"+(new Integer(i).toString()));
								else
									break;
						}
						old=old.toUpperCase()+";";
						for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
						{
							String cnam=((Ability)a.nextElement()).ID();
							str.append("<OPTION VALUE=\""+cnam+"\"");
							if(old.indexOf(";"+cnam.toUpperCase()+";")>=0)
								str.append(" SELECTED");
							str.append(">"+cnam);
						}
					}
					break;
				case 35: // is wand
					if(I instanceof Wand) return "true";
					else return "false";
				case 36: // uses
					if(firstTime) old=""+I.usesRemaining();
					str.append(old);
					break;
				case 37: // value
					if(firstTime) old=""+I.baseGoldValue();
					str.append(old);
					break;
				case 38: // weight
					if(firstTime) old=""+I.baseEnvStats().weight();
					str.append(old);
					break;
				case 39: // is map
					if(I instanceof com.planet_ink.coffee_mud.interfaces.Map) return "true";
					else return "false";
				case 40: // map areas
					{
					String mask=";"+I.readableText();
					if(httpReq.isRequestParameter("MAPAREAS"))
					{
						mask=";"+httpReq.getRequestParameter("MAPAREAS");
						for(int i=1;;i++)
							if(httpReq.isRequestParameter("MAPAREAS"+(new Integer(i).toString())))
								mask+=";"+httpReq.getRequestParameter("MAPAREAS"+(new Integer(i).toString()));
							else
								break;
					}
					mask=mask.toUpperCase()+";";
					for(Enumeration a=CMMap.areas();a.hasMoreElements();)
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
						old=(Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE))?"checked":"";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 42: // is pill
					if(I instanceof Pill) return "true";
					else return "false";
				case 43: // is super pill
					if((I instanceof Pill)&&(CMClass.className(I).indexOf("SuperPill")>0)) return "true";
					else return "false";
				case 44: // is potion
					if(I instanceof Potion) return "true";
					else return "false";
				case 45: // liquid types
					if((firstTime)&&(I instanceof Drink))
						old=""+((Drink)I).liquidType();
					for(int r=0;r<EnvResource.RESOURCE_DESCS.length;r++)
					{
						if((EnvResource.RESOURCE_DATA[r][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
						{
							str.append("<OPTION VALUE=\""+EnvResource.RESOURCE_DATA[r][0]+"\"");
							if(r==Util.s_int(old))
								str.append(" SELECTED");
							str.append(">"+EnvResource.RESOURCE_DESCS[r]);
						}
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
						for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
						{
							String cnam=((Ability)a.nextElement()).ID();
							str.append("<OPTION VALUE=\""+cnam+"\"");
							if(old.equals(cnam))
								str.append(" SELECTED");
							str.append(">"+cnam);
						}
					}
					break;
				case 49: // is rideable
					if(I instanceof Rideable) return "true";
					else return "false";
				case 50: // rideable type
					if((firstTime)&&(I instanceof Rideable))
						old=""+((Rideable)I).rideBasis();
					for(int r=0;r<Rideable.RIDEABLE_DESCS.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==Util.s_int(old))
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
					if(CMClass.className(I).indexOf("Wallpaper")>0) return "true";
					else return "false";
				case 56: // readabletext
					if(firstTime) old=""+I.readableText();
					str.append(old);
					break;
				case 57:
					Vector oldContents=new Vector();
					if(oldI instanceof Container)
						oldContents=((Container)oldI).getContents();
					if(M==null)
					{
						if((firstTime)&&(I.container()!=null))
							old=""+RoomData.getItemCode(R,I.container());
						else
							old=(firstTime)?"":old;
						str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">Inventory");
						for(int i=0;i<R.numItems();i++)
						{
							Item I2=R.fetchItem(i);
							if((I2!=I)&&(I2!=oldI)&&(I2 instanceof Container)&&(!oldContents.contains(I2)))
							{
								str.append("<OPTION VALUE=\""+RoomData.getItemCode(R,I2)+"\"");
								if(old.equals(RoomData.getItemCode(R,I2)))
									str.append(" SELECTED");
								str.append(">"+I2.Name()+" ("+I2.ID()+")"+((I2.container()==null)?"":(" in "+I2.container().Name())));
							}
						}
					}
					else
					{
						if((firstTime)&&(I.container()!=null))
							old=""+RoomData.getItemCode(M,I.container());
						else
							old=(firstTime)?"":old;
						str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">Inventory");
						for(int i=0;i<M.inventorySize();i++)
						{
							Item I2=M.fetchInventory(i);
							if((I2!=I)&&(I2!=I)&&(I2 instanceof Container)&&(!oldContents.contains(I2)))
							{
								str.append("<OPTION VALUE=\""+RoomData.getItemCode(M,I2)+"\"");
								if(old.equals(RoomData.getItemCode(M,I2)))
									str.append(" SELECTED");
								str.append(">"+I2.Name()+" ("+I2.ID()+")"+((I2.container()==null)?"":(" in "+I2.container().Name())));
							}
						}
					}
					break;
				case 58: // is light
					if(I instanceof Light) return "true";
					else return "false";
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
						old=httpReq.getRequestParameter("ISTWOHANDED");
						oldold=old;
						if(old==null) old="";
						if(old.equals(""))
							old="checked";
					}
					str.append(old);
					break;
				case 61:
					if(I instanceof Coins) return "true";
					else return "false";
				case 62:
					if(I instanceof Scroll) return "true";
					else return "false";
				case 63: // being worn
					if(firstTime)
						old=I.amWearingAt(Item.INVENTORY)?"":"checked";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 64: // non-locatable
					if(firstTime)
						old=Sense.canBeLocated(I)?"":"checked";
					else
					if(old.equals("on"))
						old="checked";
					str.append(old);
					break;
				case 65: // is key
					if(I instanceof Key) return "true";
					else return "false";
				case 66: // content types
					if(I instanceof Container)
					{
						long contains=((Container)I).containTypes();
						if(httpReq.isRequestParameter("CONTENTTYPES"))
						{
							contains=Util.s_long(httpReq.getRequestParameter("CONTENTTYPES"));
							if(contains>0)
							for(int i=1;;i++)
								if(httpReq.isRequestParameter("CONTENTTYPES"+(new Integer(i).toString())))
									contains=contains|Util.s_int(httpReq.getRequestParameter("CONTENTTYPES"+(new Integer(i).toString())));
								else
									break;
						}
						str.append("<OPTION VALUE=0");
						if(contains==0) str.append(" SELECTED");
						str.append(">"+Container.CONTAIN_DESCS[0]);
						for(int i=1;i<Container.CONTAIN_DESCS.length;i++)
						{
							String constr=Container.CONTAIN_DESCS[i];
							int mask=Util.pow(2,i-1);
							str.append("<OPTION VALUE="+mask);
							if((contains&mask)>0) str.append(" SELECTED");
							str.append(">"+constr);
						}
					}
					break;
				case 67: // is instrument
					if(I instanceof MusicalInstrument) return "true";
					else return "false";
				case 68: // instrument types
					if((firstTime)&&(I instanceof MusicalInstrument))
						old=""+((MusicalInstrument)I).instrumentType();
					for(int r=0;r<MusicalInstrument.TYPE_DESC.length;r++)
					{
						str.append("<OPTION VALUE=\""+r+"\"");
						if(r==Util.s_int(old))
							str.append(" SELECTED");
						str.append(">"+MusicalInstrument.TYPE_DESC[r]);
					}
					break;
				case 69: // is ammunition
					if(I instanceof Ammunition) return "true";
					else return "false";
				case 70: // is mob item
					if(M!=null) return "true";
					else return "false";
				}
				if(firstTime)
					httpReq.addRequestParameters(okparms[o],old.equals("checked")?"on":old);

			}
			str.append(ExitData.dispositions(I,firstTime,httpReq,parms));
			str.append(AreaData.affectsNBehaves(I,httpReq,parms));

			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return strstr;
		}
		return "";
	}
}