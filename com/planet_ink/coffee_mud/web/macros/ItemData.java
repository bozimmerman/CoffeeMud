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
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";
		String itemCode=(String)httpReq.getRequestParameters().get("ITEM");
		if(itemCode==null) return "@break@";
		int itemNum=Util.s_int(itemCode);
		
		String mobNum=(String)httpReq.getRequestParameters().get("MOB");
		
		Room R=null;
		for(int i=0;i<httpReq.cache().size();i++)
		{
			Object O=httpReq.cache().elementAt(i);
			if((O instanceof Room)&&(((Room)O).ID().equals(last)))
				R=(Room)O;
		}
		if(R==null)
		{
			R=CMMap.getRoom(last);
			if(R==null)
				return "No Room?!";
			ExternalPlay.resetRoom(R);
			httpReq.cache().addElement(R);
		}
		
		Item I=null;
		MOB M=null;
		if(itemCode.equals("NEW"))
			I=CMClass.getItem("GenItem");
		else
		if(mobNum!=null)
		{
			M=R.fetchInhabitant(Util.s_int(mobNum)-1);
			if(M==null)
				return "No MOB?!";
			I=M.fetchInventory(Util.s_int(itemCode)-1);
		}
		else
			I=R.fetchItem(Util.s_int(itemCode)-1);
		
		if(I==null)
			return "No Item?!";
		
		boolean classChanged=(((String)httpReq.getRequestParameters().get("CLASSCHANGED")!=null)
							 &&(((String)httpReq.getRequestParameters().get("CLASSCHANGED")).equals("true")));
		
		Item oldI=I;
		// important generic<->non generic swap!
		String newClassID=(String)httpReq.getRequestParameters().get("CLASSES");
		boolean firstTime=(newClassID==null);
		if((newClassID!=null)&&(!newClassID.equals(CMClass.className(I))))
			I=CMClass.getItem(newClassID);

		if(I!=null)
		{
			StringBuffer str=new StringBuffer("");
			boolean resetIfNecessary=false;
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
							  "READABLETEXT","CONTAINER","ISLIGHT","DURATION"};
			for(int o=0;o<okparms.length;o++)
			if(parms.containsKey(okparms[o]))
			{
				String old=(String)httpReq.getRequestParameters().get(okparms[o]);
				if(classChanged&&(itemCode.equals("NEW")))
				   firstTime=true;
				String oldold=old;
				if(old==null) old="";
				switch(o)
				{
				case 0: // name
					if(firstTime) old=I.name();
					str.append(old);
					break;
				case 1: // classes
					{
						if(firstTime) old=CMClass.className(I); 
						for(int r=0;r<CMClass.items.size();r++)
						{
							Item cnam=(Item)CMClass.items.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
						for(int r=0;r<CMClass.weapons.size();r++)
						{
							Item cnam=(Item)CMClass.weapons.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
						for(int r=0;r<CMClass.armor.size();r++)
						{
							Item cnam=(Item)CMClass.armor.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
						}
						for(int r=0;r<CMClass.miscMagic.size();r++)
						{
							Item cnam=(Item)CMClass.miscMagic.elementAt(r);
							str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
							if(old.equalsIgnoreCase(CMClass.className(cnam)))
								str.append(" SELECTED");
							str.append(">"+CMClass.className(cnam));
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
					long climate=I.rawWornCode();
					if(httpReq.getRequestParameters().containsKey("WORNDATA"))
					{
						climate=Util.s_int((String)httpReq.getRequestParameters().get("WORNDATA"));
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("WORNDATA"+(new Integer(i).toString())))
								climate=climate|Util.s_int((String)httpReq.getRequestParameters().get("CLIMATE"+(new Integer(i).toString())));
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
						old=I.isGettable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 30: // is removable
					if(firstTime) 
						old=I.isRemovable()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 31: // is droppable
					if(firstTime) 
						old=I.isDroppable()?"checked":""; 
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
					if(firstTime) 
						old=I.isTrapped()?"checked":""; 
					else 
					if(old.equals("on")) 
						old="checked";
					str.append(old);
					break;
				case 34: // readable spells
					if(I instanceof Potion)
						old=";"+((Potion)I).getSpellList();
					if(I instanceof Pill)
						old=";"+((Pill)I).getSpellList();
					if(httpReq.getRequestParameters().containsKey("READABLESPELLS"))
					{
						old=";"+(String)httpReq.getRequestParameters().get("READABLESPELLS");
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("READABLESPELLS"+(new Integer(i).toString())))
								old+=";"+(String)httpReq.getRequestParameters().get("READABLESPELLS"+(new Integer(i).toString()));
							else
								break;
					}
					old=old.toUpperCase()+";";
					for(int i=1;i<CMClass.abilities.size();i++)
					{
						Ability A2=(Ability)CMClass.abilities.elementAt(i);
						str.append("<OPTION VALUE=\""+A2.ID()+"\"");
						if(old.indexOf(";"+A2.ID().toUpperCase()+";")>=0) str.append(" SELECTED");
						str.append(">"+A2.name());
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
					String mask=";"+I.readableText();
					if(httpReq.getRequestParameters().containsKey("MAPAREAS"))
					{
						mask=";"+(String)httpReq.getRequestParameters().get("MAPAREAS");
						for(int i=1;;i++)
							if(httpReq.getRequestParameters().containsKey("MAPAREAS"+(new Integer(i).toString())))
								mask+=";"+(String)httpReq.getRequestParameters().get("MAPAREAS"+(new Integer(i).toString()));
							else
								break;
					}
					mask=mask.toUpperCase()+";";
					for(int i=1;i<CMMap.numAreas();i++)
					{
						Area A2=CMMap.getArea(i);
						str.append("<OPTION VALUE=\""+A2.name()+"\"");
						if(mask.indexOf(";"+A2.name().toUpperCase()+";")>=0) str.append(" SELECTED");
						str.append(">"+A2.name());
					}
					break;
				case 41: // is readable
					if(firstTime) 
						old=I.isReadable()?"checked":""; 
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
					if((firstTime)&&(I instanceof Weapon))
						old=""+((Weapon)I).ammunitionType();
					str.append(old);
					break;
				case 47: // ammo capacity
					if((firstTime)&&(I instanceof Weapon))
						old=""+((Weapon)I).ammunitionCapacity();
					str.append(old);
					break;
				case 48: // readable spell
					if((firstTime)&&(I instanceof Wand))
						old=""+((((Wand)I).getSpell()!=null)?((Wand)I).getSpell().ID():"");
					for(int r=0;r<CMClass.abilities.size();r++)
					{
						Ability A=(Ability)CMClass.abilities.elementAt(r);
						str.append("<OPTION VALUE=\""+A.ID()+"\"");
						if(old.equals(A.ID()))
							str.append(" SELECTED");
						str.append(">"+A.name());
					}
					break;
				case 49: // is map
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
				case 51: // ammo capacity
					if((firstTime)&&(I instanceof Rideable))
						old=""+((Rideable)I).mobCapacity();
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
						oldContents=((Container)I).getContents();
					if(M==null)
					{
						if((firstTime)&&(I.container()!=null))
							old=""+(RoomData.getItemCardinality(R,I.container())+1);
						else
							old=(firstTime)?"":old;
						str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">Inventory");
						for(int i=0;i<R.numItems();i++)
						{
							Item I2=R.fetchItem(i);
							if((I2!=I)&&(I2!=oldI)&&(I2.isAContainer())&&(!oldContents.contains(I2)))
							{
								str.append("<OPTION VALUE="+(i+1));
								if(Util.s_int(old)==(i+1))
									str.append(" SELECTED");
								str.append(">"+I2.name()+" ("+CMClass.className(I2)+")"+((I2.container()==null)?"":(" in "+I2.container().name())));
							}
						}
					}
					else
					{
						if((firstTime)&&(I.container()!=null))
							old=""+(RoomData.getItemCardinality(M,I.container())+1);
						else
							old=(firstTime)?"":old;
						str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">Inventory");
						for(int i=0;i<M.inventorySize();i++)
						{
							Item I2=M.fetchInventory(i);
							if((I2!=I)&&(I2!=I)&&(I2.isAContainer())&&(!oldContents.contains(I2)))
							{
								str.append("<OPTION VALUE="+(i+1));
								if(Util.s_int(old)==(i+1))
									str.append(" SELECTED");
								str.append(">"+" ("+CMClass.className(I2)+")"+((I2.container()==null)?"":(" in "+I2.container().name())));
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
				}
				if((oldold==null)&&(!firstTime))
				{
					resetIfNecessary=true;
					httpReq.getRequestParameters().put(okparms[o],old.equals("checked")?"on":old);
				}
				
			}
			str.append(ExitData.dispositions(I,httpReq,parms));
			str.append(AreaData.affectsNBehaves(I,httpReq,parms));
		
			if(resetIfNecessary)
				httpReq.resetRequestEncodedParameters();
			
			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return strstr;
		}
		return "";
	}
}
